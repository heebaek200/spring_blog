package com.tenco.spring_blog.sms;

import com.tenco.spring_blog._core.error.Exception401;
import com.tenco.spring_blog._core.util.Define;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SmsController {

    /**
     * 휴대폰 인증 화면
     * GET /user/sms
     */
    @GetMapping("/user/sms")
    public String smsForm(HttpSession session) {
        // 로그인 여부 확인
        if (session.getAttribute(Define.SESSION_USER) == null) {
            throw new Exception401("로그인이 필요합니다.");
        }

        return "sms/sms-form";
    }
}
