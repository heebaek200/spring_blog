package com.tenco.spring_blog.admin;

import com.tenco.spring_blog._core.error.Exception400;
import com.tenco.spring_blog._core.util.Define;
import com.tenco.spring_blog.refund.PortOneRefundService;
import com.tenco.spring_blog.refund.RefundResponse;
import com.tenco.spring_blog.refund.RefundService;
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

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Controller
public class AdminController {

    private final RefundService refundService;
    private final PortOneRefundService portOneRefundService;

    // GET http://localhost:8080/admin/dashboard
    @GetMapping("/admin/dashboard")
    public String dashboard(Model model, HttpSession session) {
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);

        model.addAttribute("adminName", sessionUserDto.getUsername());

        return "admin/dashboard";
    }

    // 관리자 환불 요청 관리
    // GET http://localhost:8080/admin/refund/list
    @GetMapping("/admin/refund/list")
    public String refundList(Model model) {

        List<RefundResponse.AdminListDto> refundList = refundService.findAllForAdmin();

        model.addAttribute("refundList", refundList);

        return "admin/refund-list";
    }

    // POST http://localhost:8080/admin/refund/{id}/reject
    @PostMapping("/admin/refund/{id}/reject")
    public String rejectRefund(@PathVariable(name = "id") Long id, @RequestParam(name = "rejectReason") String rejectReason) {
        // 유효성 검사
        if (rejectReason == null || rejectReason.trim().isEmpty()) {
            throw new Exception400("거절 사유를 반드시 입력하세요.");
        }
        if (rejectReason.length() >= 500) {
            throw new Exception400("거절 사유는 500자 이하로 입력해주세요.");
        }

        refundService.reject(id, rejectReason);

        // 같은 페이지로 PRG
        return "redirect:/admin/refund/list";
    }

    // POST http://localhost:8080/admin/refund/{id}/approve (환불 승인 + 포트원 결제 취소)
    @PostMapping("/admin/refund/{id}/approve")
    public String approveRefund(@PathVariable Long id) {
        portOneRefundService.approve(id);

        return "redirect:/admin/refund/list";
    }


}
