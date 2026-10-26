package com.tenco.spring_blog.user;

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
public class UserApiController {

    private final UserService userService;
    private final MailService mailService;

    // POST /api/email/send
    // 요청 바디 : {"email": "a@naver.com"}
    @PostMapping("/api/email/send")
    public ResponseEntity<?> sendCode(
            @RequestBody UserRequest.EmailCheckDto emailCheckDto,
            HttpSession session
    ) {
        // 인증검사 없음 (로그인 전)
        // 입력 유효성 검사
        emailCheckDto.validate();

        // 이미 가입된 이메일이 있으면 400
        userService.checkEmailAvailable(emailCheckDto.getEmail());

        // SMPT 사용해서 인증번호 발송
        mailService.sendVerificationCode(emailCheckDto.getEmail(), session);

        return ResponseEntity.ok(Map.of("message", "인증번호 발송되었습니다.%d분 안에 입력하세요.".formatted(Define.CODE_EXPIRE_MINUTES)));
    }

    // POST /api/email/verify
    // 요청 바디 : {"email": "a@naver.com", "code": "123456"}
    @PostMapping("/api/email/verify")
    public ResponseEntity<?> verifyCode(
            @RequestBody UserRequest.EmailCheckDto emailCheckDto,
            HttpSession session
    ) {
        emailCheckDto.validateCode();
        boolean verified = mailService.verifyCode(emailCheckDto.getEmail(), emailCheckDto.getCode(), session);
        if (!verified) {
            return ResponseEntity.badRequest().body(Map.of("message", "인증번호가 일치하지 않습니다"));
        }

        return ResponseEntity.ok().body(Map.of("message", "인증되었습니다."));
    }

    // POST http://localhost:8080/api/point/charge
    // 요청 바디 : {"amount": 1_000}
    @PostMapping("/api/point/charge")
    public ResponseEntity<?> chargePoint(
            @RequestBody UserRequest.PointChargeDto chargeDto,
            HttpSession session) {

        // 1. 인증검사
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        if (sessionUserDto == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "message", "로그인이 필요합니다."
                    ));
        }

        // 2. 유효성 검사
        chargeDto.validate();

        // 3. 서비스에 위임
        int point = userService.chargePoint(sessionUserDto.getId(), chargeDto.getAmount());

        // 4. 응답처리
        return ResponseEntity.ok(Map.of(
                "message", "포인트가 충전되었습니다.",
                "point", point
        ));
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
                "message", e.getMessage()
        ));
    }


}
