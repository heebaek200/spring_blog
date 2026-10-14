package com.tenco.spring_blog.reply;

import com.tenco.spring_blog._core.util.Define;
import com.tenco.spring_blog.user.SessionUserDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@RequiredArgsConstructor
@Controller
public class ReplyController {

    private final ReplyService replyService;

    // 댓글 작성
    @PostMapping("/reply/save")
    public String save(
            ReplyRequest.SaveDto saveDto,
            HttpSession session
    ) {
        // 1. 인증검사 (인터셉터에서 하고 있으므로 패스)

        // 2. 유효성검사
        saveDto.validate();

        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);

        // 3. 서비스 계층으로 위임 (저장 요청)
        replyService.save(saveDto, sessionUserDto);

        // 4. 리다이렉트 처리
        return "redirect:/board/" + saveDto.getBoardId();
    }

    // 댓글 삭제 기능
    @PostMapping("/reply/{id}/delete")
    public String delete(@PathVariable(name = "id") Long id, HttpSession session) {
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);

        Long boardId = replyService.deleteById(id, sessionUserDto);

        return "redirect:/board/" + boardId;
    }

}
