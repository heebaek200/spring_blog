package com.tenco.spring_blog.board;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Slf4j // 로거
@RequiredArgsConstructor // DI처리
@Controller
public class BoardController {

    private final BoardNativeRepository boardNativeRepository;

    // GET http://localhost:8080/
    // GET http://localhost:8080/board/list
    @GetMapping({"/", "/board/list"})
    public String list(Model model) {

        List<Board> boardList = boardNativeRepository.findAll();
        model.addAttribute("boardList", boardList);

        return "board/list";
    }

    // GET http://localhost:8080/board/3
    @GetMapping("/board/{id}")
    public String detail(
            @PathVariable(name = "id") Long id,
            Model model
    ) {

        Board board = boardNativeRepository.findById(id);
        if (board == null) {
            return "redirect:/";
        }

        model.addAttribute("board", board);

        return "board/detail";
    }

    // GET http://localhost:8080/board/save (화면 요청)
    @GetMapping("/board/save")
    public String saveForm() {

        return "board/save-form";
    }

    // POST http://localhost:8080/board/save
    // 스프링부트의 데이터 기본 파싱 전략 key=value
    // name 속성 기준으로 값을 추출할 수 있다.
    @PostMapping("/board/save")
    public String save(
            @RequestParam("username") String username,
            @RequestParam("title") String title,
            @RequestParam("content") String content
    ) {
        // 폼의 name 속성과 매개변수명이 일치하면 자동으로 값이 바인딩됨.
        // name="title" --> String title로 자동 매핑
        log.info("username {}", username);
        log.info("title {}", title);
        log.info("content {}", content);

        // DAO 객체에게 데이터를 전달하여 저장하는 일 위임
        boardNativeRepository.save(title, content, username);

        // 게시물 목록으로 리다이렉트
        return "redirect:/";
    }

    // GET http://localhost:8080/board/1/update (수정 화면)
    @GetMapping("/board/{id}/update")
    public String updateForm(
            @PathVariable(name = "id") Long id,
            Model model
    ) {

        // 수정하기 화면 요청 (먼저 조회부터)
        Board board = boardNativeRepository.findById(id);
        model.addAttribute("board", board);

        return "board/update-form";
    }

    // POST http://localhost:8080/board/1/update (게시글 수정 기능 요청)
    @PostMapping("/board/{id}/update")
    public String update(
            @PathVariable(name = "id") Long id,
            @RequestParam(name = "title") String title,
            @RequestParam(name = "content") String content
    ) {
        boardNativeRepository.updateById(title, content, id);

        // PRG 패턴
        return "redirect:/board/detail/" + id;
    }

    // 게시글 삭제
    // POST http://localhost:8080/board/11/delete
    @PostMapping("/board/{id}/delete")
    public String delete(@PathVariable Long id) {
        boardNativeRepository.deleteById(id);

        // PRG 패턴 사용
        return "redirect:/";
    }

}
