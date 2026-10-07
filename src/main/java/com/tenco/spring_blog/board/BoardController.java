package com.tenco.spring_blog.board;

import com.tenco.spring_blog._core.error.Exception403;
import com.tenco.spring_blog._core.error.Exception404;
import com.tenco.spring_blog._core.util.Define;
import com.tenco.spring_blog.user.User;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Slf4j // 로거
@RequiredArgsConstructor // DI처리
@Controller
public class BoardController {

    private final BoardPersistenceRepository boardPersistenceRepository;

    // GET http://localhost:8080/
    // GET http://localhost:8080/board/list
    @GetMapping({"/", "/board/list"})
    public String list(Model model) {
        List<Board> boardList = boardPersistenceRepository.findAll();
        model.addAttribute("boardList", boardList);

        return "board/list";
    }

    // GET http://localhost:8080/board/3
    @GetMapping("/board/{id}")
    public String detail(
            @PathVariable(name = "id") Long id,
            Model model
    ) {
//        Board board = boardPersistenceRepository.findById(id);
        Board board = boardPersistenceRepository.findByWithJPQL(id);
        if (board == null) {
            // 추후 404 에러 페이지를 만들어서 처리할 예정
            throw new Exception404("게시물을 찾을 수 없습니다. : " + id);
        }

        model.addAttribute("board", board);
        return "board/detail";
    }

    // GET http://localhost:8080/board/save (화면 요청)
    @GetMapping("/board/save")
    public String saveForm(HttpSession session) {
        // 1. 인증 검사 : 로그인 안된 사용자는 이 페이지에 접근 못하게 처리
        User sessionUser = (User) session.getAttribute(Define.SESSION_USER);
        if (sessionUser == null) {
            return "redirect:/login";
        }

        return "board/save-form";
    }

   // POST http://localhost:8080/board/save
    @PostMapping("/board/save")
    // Spring 폼 데이터를 객체로 변환하는 과정 (데이터 바인딩 메커니즘)
    // 폼 데이터 바인딩 : Spring이 HTTP 요청 패러미터를 객체로 자동 변환
    public String save(
            BoardRequest.SaveDto saveDto,
            HttpSession session
    ) {
        // 1. 인증검사
        User sessionUser = (User) session.getAttribute(Define.SESSION_USER);
        if (sessionUser == null) {
            return "redirect:/login";
        }
        // 2. 유효성 검사
        // 입력 데이터 검증
        saveDto.validate();

        // DTO에서 Board 객체 생성
        Board board = saveDto.toEntity(sessionUser);

        // Board 저장
        Board savedBoard = boardPersistenceRepository.save(board);

        return "redirect:/";
    }

    // GET http://localhost:8080/board/1/update (수정 화면)
    @GetMapping("/board/{id}/update")
    public String updateForm(
            @PathVariable(name = "id") Long id,
            Model model,
            HttpSession session
    ) {
        // 1. 인증검사
        User sessionUser = (User) session.getAttribute(Define.SESSION_USER);
        if (sessionUser == null) {
            return "redirect:/login";
        }
        
        // 2. 권한 체크를 위한
        Board board = boardPersistenceRepository.findById(id);

        // 3. 권한 체크
        if (!board.isOwner(sessionUser.getId())) {
            throw new Exception403("수정 권한이 없습니다.");
        }

        // 수정하기 화면 요청 (먼저 조회부터)
        model.addAttribute("board", board);

        return "board/update-form";
    }

    // POST http://localhost:8080/board/1/update (게시글 수정 기능 요청)
    @PostMapping("/board/{id}/update")
    public String update(
            @PathVariable(name = "id") Long id,
            BoardRequest.UpdateDto updateDto,
            HttpSession session
    ) {
        // 1. 인증검사
        User sessionUser = (User) session.getAttribute(Define.SESSION_USER);
        if (sessionUser == null) {
            return "redirect:/login";
        }

        // 2. 권한검사
        Board boardEntity = boardPersistenceRepository.findById(id);

        if (! boardEntity.isOwner(sessionUser.getId())) {
            throw new Exception403("수정 권한이 없습니다.");
        }

        // 3. 유효성검사
        updateDto.validate();  // 유효성 검사 실패 (throw)

        // 4. Dirty Checking을 통한 수정 실행
        boardPersistenceRepository.updateById(id, updateDto);

        // 5. 수정 완료 후 PRG 패턴(상세 페이지로 이동)
        return "redirect:/board/" + id;
    }

    // 게시글 삭제
    // POST http://localhost:8080/board/11/delete
    @PostMapping("/board/{id}/delete")
    public String delete(
            @PathVariable Long id,
            HttpSession session
    ) {
        // 1. 인증 검사
        // 2. 권한 확인
        // 2.1 - 관리자 권한이 있다면 통과

        // 1.
        User sessionUser = (User)session.getAttribute(Define.SESSION_USER);
        if (sessionUser == null) {
            return "redirect:/login";
        }

        // 2.
        // 삭제할 게시글 조회
        Board boardEntity = boardPersistenceRepository.findById(id);

        // 3. 권한 체크: 본인이 작성한 게시글만 삭제
        if (!boardEntity.isOwner(sessionUser.getId())) {
            throw new Exception403("삭제 권한이 없습니다.");
        }

        // 4. 권한 확인 후 삭제 실행
        boardPersistenceRepository.deleteById(id);

        // 5. 삭제 성공 후 메인 페이지 리다이렉트
        return "redirect:/";
    }

}
