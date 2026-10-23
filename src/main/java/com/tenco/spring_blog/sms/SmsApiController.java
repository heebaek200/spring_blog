package com.tenco.spring_blog.sms;

import com.tenco.spring_blog._core.error.Exception400;
import com.tenco.spring_blog._core.util.Define;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RestController
public class SmsApiController {

    private final SmsService smsService;

    /**
     * 6자리 인증번호 발송
     * POST /api/sms/send
     */
    @PostMapping("/api/sms/send")
    public ResponseEntity<?> sendCode(
        @RequestBody SmsRequest.SendDto sendDto,
        HttpSession session
    ) {
        // 인증검사는 인터셉터

        // 입력 유효성 검사
        sendDto.validate();

        // 발송 (재발송 가능 검사 포함)
        smsService.sendVerificationCode(sendDto.getTel(), session);

        return ResponseEntity.ok(Map.of(
                "message",
                "인증번호 발송되었습니다.%d분 안에 입력하세요.".formatted(Define.CODE_EXPIRE_MINUTES)
        ));
    }

    /**
     * 인증번호 검증
     * POST /api/sms/verify
     */
    @PostMapping("/api/sms/verify")
    public ResponseEntity<?> verifyCode(
            @RequestBody SmsRequest.SendDto sendDto,
            HttpSession session
    ) {
        // 입력값 유효성 (인증번호 포함)
        sendDto.validateCode();

        // 검증
        boolean verified = smsService.verifyCode(
                sendDto.getTel(),
                sendDto.getCode(),
                session
        );
        if (!verified) {
            return ResponseEntity.badRequest().body(Map.of("message", "인증번호가 일치하지 않습니다"));
        }

        return ResponseEntity.ok().body(Map.of("message", "인증되었습니다."));
    }

    // 임시 예외처리 (API 응답용)
    @ExceptionHandler(Exception400.class)
    public ResponseEntity<Map<String, String>> handleEx400(Exception400 e) {
        log.info("{}", e);
        return ResponseEntity.badRequest().body(Map.of(
                "message", e.getMessage()
        ));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleEx500(RuntimeException e) {
        log.info("{}", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "message", "오류가 발생했습니다. 잠시 후 다시 시도해주세요."
        ));
    }

}
