package com.tenco.spring_blog.board;

import com.tenco.spring_blog.user.User;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

@Slf4j // 로거
@RequiredArgsConstructor // DI처리
@Controller
public class BoardController {

    private final BoardNativeRepository boardNativeRepository;
    private final BoardPersistenceRepository boardPersistenceRepository;

    // GET http://localhost:8080/
    // GET http://localhost:8080/board/list
//    @GetMapping({"/", "/board/list"})
//    public String list(Model model) {
//
//        List<Board> boardList = boardNativeRepository.findAll();
//        model.addAttribute("boardList", boardList);
//
//        return "board/list";
//    }
    @GetMapping({"/", "/board/list"})
    public String list(Model model) {

        List<Board> boardList = boardPersistenceRepository.findAll();
        model.addAttribute("boardList", boardList);

        return "board/list";
    }

    // GET http://localhost:8080/board/3
//    @GetMapping("/board/{id}")
//    public String detail(
//            @PathVariable(name = "id") Long id,
//            Model model
//    ) {
//
//        Board board = boardNativeRepository.findById(id);
//        if (board == null) {
//            return "redirect:/";
//        }
//
//        model.addAttribute("board", board);
//
//        return "board/detail";
//    }
    @GetMapping("/board/{id}")
    public String detail(
            @PathVariable(name = "id") Long id,
            Model model
    ) {
//        Board board = boardPersistenceRepository.findById(id);
        Board board = boardPersistenceRepository.findByWithJPQL(id);
        if (board == null) {
            // 추후 404 에러 페이지를 만들어서 처리할 예정
            throw new RuntimeException("게시물을 찾을 수 없습니다. : " + id);
        }

        model.addAttribute("board", board);
        return "board/detail";
    }

    // GET http://localhost:8080/board/save (화면 요청)
    @GetMapping("/board/save")
    public String saveForm(HttpSession session) {
        // 1. 인증 검사 : 로그인 안된 사용자는 이 페이지에 접근 못하게 처리
        User sessionUser = (User) session.getAttribute("sessionUser");
        if (sessionUser == null) {
            return "redirect:/login";
        }

        return "board/save-form";
    }

//    // POST http://localhost:8080/board/save
//    // 스프링부트의 데이터 기본 파싱 전략 key=value
//    // name 속성 기준으로 값을 추출할 수 있다.
//    @PostMapping("/board/save")
//    public String save(
//            @RequestParam("username") String username,
//            @RequestParam("title") String title,
//            @RequestParam("content") String content
//    ) {
//        // DAO 객체에게 데이터를 전달하여 저장하는 일 위임
//        boardNativeRepository.save(title, content, username);
//
//        // 게시물 목록으로 리다이렉트
//        return "redirect:/";
//    }
    @PostMapping("/board/save")
    // Spring 폼 데이터를 객체로 변환하는 과정 (데이터 바인딩 메커니즘)
    // 폼 데이터 바인딩 : Spring이 HTTP 요청 패러미터를 객체로 자동 변환
    public String save(
            BoardRequest.SaveDto saveDto,
            HttpSession session
    ) {
        // 1. 인증검사
        User sessionUser = (User) session.getAttribute("sessionUser");
        if (sessionUser == null) {
            return "redirect:/login";
        }
        // 2. 유효성 검사
        try {
            // 입력 데이터 검증
            saveDto.validate();

            // DTO에서 Board 객체 생성
            Board board = saveDto.toEntity(sessionUser);

            // Board 저장
            Board savedBoard = boardPersistenceRepository.save(board);

            return "redirect:/";
        } catch (Exception e) {
            // 검증 실패 시 메시지와 함께 작성 폼으로 돌아가기
            log.error(e.getMessage());
            return "board/save-form";
        }
    }

//    // GET http://localhost:8080/board/1/update (수정 화면)
//    @GetMapping("/board/{id}/update")
//    public String updateForm(
//            @PathVariable(name = "id") Long id,
//            Model model
//    ) {
//
//        // 수정하기 화면 요청 (먼저 조회부터)
//        Board board = boardNativeRepository.findById(id);
//        model.addAttribute("board", board);
//
//        return "board/update-form";
//    }
    // GET http://localhost:8080/board/1/update (수정 화면)
    @GetMapping("/board/{id}/update")
    public String updateForm(
            @PathVariable(name = "id") Long id,
            Model model
    ) {

        // 수정하기 화면 요청 (먼저 조회부터)
        Board board = boardPersistenceRepository.findById(id);
        model.addAttribute("board", board);

        return "board/update-form";
    }

    // POST http://localhost:8080/board/1/update (게시글 수정 기능 요청)
//    @PostMapping("/board/{id}/update")
//    public String update(
//            @PathVariable(name = "id") Long id,
//            @RequestParam(name = "title") String title,
//            @RequestParam(name = "content") String content
//    ) {
//        boardNativeRepository.updateById(title, content, id);
//
//        // PRG 패턴
//        return "redirect:/board/" + id;
//    }
    @PostMapping("/board/{id}/update")
    public String update(
            @PathVariable(name = "id") Long id,
            BoardRequest.UpdateDto reqDto
    ) {
        reqDto.validate();  // 유효성 검사 실패 (throw)

        boardPersistenceRepository.updateById(id, reqDto);

        // PRG 패턴
        return "redirect:/board/" + id;
    }

    // 게시글 삭제
    // POST http://localhost:8080/board/11/delete
//    @PostMapping("/board/{id}/delete")
//    public String delete(@PathVariable Long id) {
//        boardNativeRepository.deleteById(id);
//
//        // PRG 패턴 사용
//        return "redirect:/";
//    }
    @PostMapping("/board/{id}/delete")
    public String delete(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        // 1. 인증 검사
        // 2. 권한 확인
        // 2.1 - 관리자 권한이 있다면 통과

        // 1.
        User sessionUser = (User)session.getAttribute("sessionUser");
        if (sessionUser == null) {
            return "redirect:/login";
        }

        try {
            // 2.
            // 삭제할 게시글 조회
            Board boardEntity = boardPersistenceRepository.findById(id);

            // 3. 권한 체크: 본인이 작성한 게시글만 삭제
            if (!boardEntity.isOwner(sessionUser.getId())) {
                throw new RuntimeException("삭제 권한이 없습니다.");
            }

            // 4. 권한 확인 후 삭제 실행
            boardPersistenceRepository.deleteById(id);

            // 5. 삭제 성공 후 메인 페이지 리다이렉트
            return "redirect:/";
        } catch (Exception e) {
            log.error("삭제 실패 : {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

            // 권한 없음 또는 기타 오류
            return "redirect:/board/" + id;
        }
    }

}
