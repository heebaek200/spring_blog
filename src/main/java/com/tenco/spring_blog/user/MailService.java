package com.tenco.spring_blog.user;

import com.tenco.spring_blog._core.error.Exception500;
import com.tenco.spring_blog._core.util.Define;
import com.tenco.spring_blog._core.util.MailUtil;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;

// 메일 발송, 인증번호 확인
@Slf4j
@RequiredArgsConstructor
@Service
public class MailService {

    private final JavaMailSender javaMailSender;

    // 인증번호 발송
    public void sendVerificationCode(String email, HttpSession session) {
        // 6자리 인증번호 생성
        String code = MailUtil.generateCode();

        // 메일 작성(MimeMessage : HTML 본문, 첨부 파일 등을 담을 수 있는 메일 형식)
        MimeMessage message = javaMailSender.createMimeMessage();
        try {
            // false : 첨부 파일 없음, "UTF-8" 한글 깨짐 방지
            MimeMessageHelper helper = new MimeMessageHelper(message,
                    false,
                    "UTF-8");
            helper.setTo(email);    // 받는 사람 이메일 주소
            helper.setSubject("[스프링블로그] 회원가입 인증번호");
            helper.setText("<h3>인증번호는 %s입니다</h3>".formatted(code), true);


            // 1-3 발송 (실패하면...)
            javaMailSender.send(message);

        } catch (Exception e) {
            log.error("메일 작성 중 오류가 발생했습니다. : {}", e.getMessage());
            log.error("{}", e);
            throw new Exception500("메일 작성 중 오류가 발생했습니다.");
        }

        session.setAttribute("%s%s".formatted(Define.EMAIL_CODE_PREFIX, email), code);
        log.info("인증번호 발송 완료 - 이메일 : {}", email);
    }

    // 인증번호 확인 : 일치하면 true 반환
    public boolean verifyCode(String email, String code, HttpSession session) {
        String savedCode = (String) session.getAttribute("%s%s".formatted(Define.EMAIL_CODE_PREFIX, email));

        if (savedCode == null || ! savedCode.equals(code)) {
            return false;
        }

        // 사용한 인증번호는 삭제
        session.removeAttribute("%s%s".formatted(Define.EMAIL_CODE_PREFIX, email));

        // 인증을 마친 이메일을 세션에 기록 (회원가입 시 서버가 사용)
        session.setAttribute(Define.VERIFIED_EMAIL, email);
        return true;
    }

    // 메일검증
    public boolean verifyMail(String email, HttpSession session) {
        String savedMail = (String) session.getAttribute(Define.VERIFIED_EMAIL);

        if (savedMail == null || ! savedMail.equals(email)) {
            return false;
        }

        // 검증에 사용한 이메일은 삭제
        session.removeAttribute(Define.VERIFIED_EMAIL);
        return true;
    }
}
