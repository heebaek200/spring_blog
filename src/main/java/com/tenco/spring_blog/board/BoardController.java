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

    private final BoardService boardService;

    // GET http://localhost:8080/
    // GET http://localhost:8080/board/list
    @GetMapping({"/", "/board/list"})
    public String list(Model model) {
        List<Board> boardList = boardService.findAll();
        model.addAttribute("boardList", boardList);

        return "board/list";
    }

    // GET http://localhost:8080/board/3
    @GetMapping("/board/{id}")
    public String detail(
            @PathVariable(name = "id") Long id,
            Model model
    ) {
        Board board = boardService.findByid(id);

        model.addAttribute("board", board);
        return "board/detail";
    }

    // GET http://localhost:8080/board/save (화면 요청)
    @GetMapping("/board/save")
    public String saveForm(HttpSession session) {
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
        // 유효성 검사
        saveDto.validate();

        User sessionUser = (User) session.getAttribute(Define.SESSION_USER);
        boardService.save(saveDto, sessionUser);

        return "redirect:/";
    }

    // TODO : Response 구현 후 인가 처리
    // GET http://localhost:8080/board/1/update (수정 화면)
    @GetMapping("/board/{id}/update")
    public String updateForm(
            @PathVariable(name = "id") Long id,
            Model model,
            HttpSession session
    ) {
        Board board = boardService.findByid(id);

        User sessionUser = (User) session.getAttribute(Define.SESSION_USER);
        if (! board.isOwner(sessionUser.getId())) {
            throw new Exception403("수정할 권한이 없습니다.");
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
        updateDto.validate();  // 유효성 검사 실패 (throw)

        User sessionUser = (User) session.getAttribute(Define.SESSION_USER);
        boardService.updateById(id, updateDto, sessionUser);

        // 수정 완료 후 PRG 패턴(상세 페이지로 이동)
        return "redirect:/board/" + id;
    }

    // 게시글 삭제
    // POST http://localhost:8080/board/11/delete
    @PostMapping("/board/{id}/delete")
    public String delete(
            @PathVariable Long id,
            HttpSession session
    ) {
        User sessionUser = (User)session.getAttribute(Define.SESSION_USER);

        boardService.deleteById(id, sessionUser);

        return "redirect:/";
    }

}
