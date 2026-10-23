package com.tenco.spring_blog.sms;

import com.solapi.sdk.SolapiClient;
import com.solapi.sdk.message.exception.SolapiMessageNotReceivedException;
import com.solapi.sdk.message.model.Message;
import com.solapi.sdk.message.service.DefaultMessageService;
import com.tenco.spring_blog._core.error.Exception400;
import com.tenco.spring_blog._core.error.Exception500;
import com.tenco.spring_blog._core.util.Define;
import com.tenco.spring_blog._core.util.MailUtil;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

// SMS 발송, 인증번호 확인
@Slf4j
@RequiredArgsConstructor
@Service
public class SmsService {

    @Value("${sms.solapi.api-key}")
    private String apiKey;

    @Value("${sms.solapi.api-secret}")
    private String apiSecret;

    @Value("${sms.solapi.sender}")
    private String sender;

    // 인증 코드와 유효시간, 그리고 입력 횟수 제한
    private record SmsCode(
            String code,
            LocalDateTime expiresAt,
            AtomicInteger requestCount
    ) {
        public SmsCode(String code, LocalDateTime expiresAt) {
            this(code, expiresAt, new AtomicInteger(0));
        }

        boolean isExpired() {
            return LocalDateTime.now().isAfter(expiresAt);
        }
    }

    // 재발송 가능 시간
    public record SmsResendInterval(
            LocalDateTime resendAvailableAt
    ) {
        boolean isResendAvailable() {
            return LocalDateTime.now().isAfter(resendAvailableAt);
        }
    }

    // SMS 발송 제한 검사와 선점을 보호하는 공통 잠금
    private final Object resendLock = new Object();

    // 인증번호 발송
    public void sendVerificationCode(String tel, HttpSession session) {
        String keySmsCode = "%s%s".formatted(Define.SMS_CODE_PREFIX, tel);

        synchronized (resendLock) { // 동시 요청 취약성 보완 (단일 서버, 같은 세션 한정)

            SmsResendInterval savedSmsResendInterval = (SmsResendInterval) session.getAttribute(Define.SMS_RESEND_INTERVAL);

            // 재발송 60초 제한
            if (savedSmsResendInterval != null) {
                if (!savedSmsResendInterval.isResendAvailable()) {
                    throw new Exception400("마지막 인증번호 전송으로부터 60초 후에 전송할 수 있습니다.");
                }
            }

            session.setAttribute(Define.SMS_RESEND_INTERVAL,
                    new SmsResendInterval(
                            LocalDateTime.now().plusSeconds(Define.SMS_RESEND_INTERVAL_SECONDS)
                    ));

        }

        // 6자리 인증번호 생성
        String code = MailUtil.generateCode();

        // SMS 발송
        DefaultMessageService messageService =  SolapiClient.INSTANCE.createInstance(apiKey, apiSecret);
        Message message = new Message();
        message.setFrom(sender);
        message.setTo(tel);
        message.setText("인증 번호는 %s 입니다. %d분 안에 입력해주세요. [이희백의 스프링블로그]"
                .formatted(
                        code, Define.CODE_EXPIRE_MINUTES
                ));

        try {
            // send 메소드로 ArrayList<Message> 객체를 넣어도 동작합니다!
            messageService.send(message);

        } catch (SolapiMessageNotReceivedException e) {
            log.error("SMS 발송 중 오류가 발생했습니다 : {}", e.getMessage());
            log.debug("{}", e.getFailedMessageList());

            throw new Exception500("SMS 작성 중 오류가 발생했습니다.");
        } catch (Exception exception) {
            throw new Exception500("SMS 작성 중 예상치 못한 오류가 발생했습니다.");
        }

        // 세션 갱신
        session.setAttribute(keySmsCode,
                new SmsCode(
                        code,
                        LocalDateTime.now().plusMinutes(Define.CODE_EXPIRE_MINUTES)
                ));
        log.info("인증번호 발송 완료 - 전화번호 : {}", tel);
    }

    // 인증번호 확인 : 일치하면 true 반환
    public boolean verifyCode(String tel, String code, HttpSession session) {
        String key = "%s%s".formatted(Define.SMS_CODE_PREFIX, tel);
        SmsCode savedSmsCode = (SmsCode) session.getAttribute(key);

        if (savedSmsCode == null) {
            throw new Exception400("인증번호를 먼저 입력해주세요");
        }

        // 인증번호 입력 횟수 제한
        int requestCount = savedSmsCode.requestCount().incrementAndGet();
        if (requestCount > Define.SMS_INPUT_LIMIT) {
            // 횟수 넘은 인증번호는 삭제
            session.removeAttribute(key);
            throw new Exception400("인증번호 입력 횟수 제한 %d를 초과했습니다. 다시 요청해주세요."
                    .formatted(Define.SMS_INPUT_LIMIT));
        }

        if (savedSmsCode.isExpired()) {
            // 만료한 인증번호는 삭제
            session.removeAttribute(key);
            throw new Exception400("인증번호가 만료되었습니다. 다시 요청해주세요.");
        }

        if (! savedSmsCode.code().equals(code)) {
            throw new Exception400("인증번호가 일치하지 않습니다.");
        }

        // 사용한 인증번호는 삭제
        session.removeAttribute(key);

        // to dev: 인증을 마친 전화번호는 어딘가에 쓰겠지... 이번 예제에서는 패스

        return true;
    }
}
