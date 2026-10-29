package com.tenco.spring_blog.refund;

import com.tenco.spring_blog._core.util.Define;
import com.tenco.spring_blog.payment.PaymentResponse;
import com.tenco.spring_blog.user.SessionUserDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Controller
public class RefundController {

    private final RefundService refundService;

    // 환불 요청 용 Form 표시
    // GET : http://localhost:8080/my/refund/request/{paymentPk}
    @GetMapping("/my/refund/request/{paymentPk}")
    public String requestForm(
            @PathVariable Long paymentPk,
            HttpSession session,
            Model model
    ) {

        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);

        PaymentResponse.ListDto paymentListDto = refundService.findRequestForm(paymentPk, sessionUserDto.getId());
        model.addAttribute("payment", paymentListDto);

        return "refund/request-form";
    }

    // 환불 요청
    // POST : http://localhost:8080/my/refund/request
    @PostMapping("/my/refund/request")
    public String request(RefundRequest.SaveDto saveDto, HttpSession session) {
        saveDto.validate();

        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        refundService.save(saveDto, sessionUserDto.getId());

        return "redirect:/my/refund";
    }

    // 환불 내역
    // GET : http://localhost:8080/my/refund
    @GetMapping("/my/refund")
    public String list(Model model, HttpSession session) {
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);

        List<RefundResponse.ListDto> refundList = refundService.findAllByUserId(sessionUserDto.getId());

        model.addAttribute("refundList", refundList);

        return "refund/list";
    }

}
