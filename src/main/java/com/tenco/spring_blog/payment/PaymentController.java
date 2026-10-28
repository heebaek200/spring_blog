package com.tenco.spring_blog.payment;

import com.tenco.spring_blog._core.util.Define;
import com.tenco.spring_blog.user.SessionUserDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@RequiredArgsConstructor
@Controller
public class PaymentController {

    private final PaymentService paymentService;

    // 결제 내역
    // GET http://localhost:8080/my/payment
    @GetMapping("/my/payment")
    public String list(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "5") int size,
            HttpSession session,
            Model model
    ) {
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);

        PaymentResponse.PageDto pageDto = paymentService.findAllByUserId(sessionUserDto.getId(), page, size);

        model.addAttribute("paymentPage", pageDto);

        log.debug("서늘 : {}", (pageDto == null) ? null : (
                pageDto.getContent()
        ));

        return "payment/list";
    }

}
