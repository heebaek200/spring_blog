package com.tenco.spring_blog.board;

import com.tenco.spring_blog._core.util.Define;
import com.tenco.spring_blog.purchase.PurchaseService;
import com.tenco.spring_blog.reply.ReplyResponse;
import com.tenco.spring_blog.reply.ReplyService;
import com.tenco.spring_blog.user.SessionUserDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j // 로거
@RequiredArgsConstructor // DI처리
@Controller
public class BoardController {

    private final BoardService boardService;
    private final ReplyService replyService;
    private final PurchaseService purchaseService;

    // 유료 게시글 구매 요청 URL 만들기
    // POST /board/{id}/purchase
    @PostMapping("/board/{id}/purchase")
    public String purchase(@PathVariable(name = "id") Long id, HttpSession session) {
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        purchaseService.purchase(id, sessionUserDto.getId());

        // 구매 후 상세보기로 이동처리 (이제 본문이 나와야 함)
        return "redirect:/board/%d".formatted(id);
    }

    // GET http://localhost:8080/
    // GET http://localhost:8080/board/list
    @GetMapping({"/", "/board/list"})
    public String list(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "3") int size,
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            Model model
    ) {
        BoardResponse.PageDto pageDto = boardService.findAll(page, size, keyword);
        model.addAttribute("boardPage", pageDto);

        model.addAttribute("keyword", keyword);
        model.addAttribute("isSearch", !keyword.isBlank());
        model.addAttribute("keywordParam", URLEncoder.encode(keyword, StandardCharsets.UTF_8));

        return "board/list";
    }

    // GET http://localhost:8080/board/3
    @GetMapping("/board/{id}")
    public String detail(
            @PathVariable(name = "id") Long id,
            Model model,
            HttpSession session
    ) {
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);

        Long sessionUserId = (sessionUserDto == null) ? null : sessionUserDto.getId();
        BoardResponse.DetailDto detailDto = boardService.findById(id, sessionUserId);
        model.addAttribute("board", detailDto);

        // 수정 및 삭제 버튼 표시 비표시 용 플래그
        // 로그인했으며, 작성자 본인일 때만 수정 삭제 버튼 표시
        boolean isOwner = sessionUserDto != null && detailDto.getUserId().equals(sessionUserId);
        model.addAttribute("isOwner", isOwner);

        // 댓글 목록 조회
        List<ReplyResponse.ListDto> replyList = replyService.findAllByBoardId(id, sessionUserId);
        model.addAttribute("replyList", replyList);

        return "board/detail";
    }

    // GET http://localhost:8080/board/save (화면 요청)
    @GetMapping("/board/save")
    public String saveForm() {
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

        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        boardService.save(saveDto, sessionUserDto);

        return "redirect:/";
    }

    // GET http://localhost:8080/board/1/update (수정 화면)
    @GetMapping("/board/{id}/update")
    public String updateForm(
            @PathVariable(name = "id") Long id,
            Model model,
            HttpSession session
    ) {
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        BoardResponse.UpdateFormDto updateFormDto = boardService.findByIdAndUpdateForm(id, sessionUserDto.getId());
        model.addAttribute("board", updateFormDto);

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

        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        boardService.updateById(id, updateDto, sessionUserDto);

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
        SessionUserDto sessionUserDto = (SessionUserDto)session.getAttribute(Define.SESSION_USER);

        boardService.deleteById(id, sessionUserDto);

        return "redirect:/";
    }

}
