package com.tenco.spring_blog.payment;

import com.tenco.spring_blog._core.util.Define;
import com.tenco.spring_blog.user.SessionUserDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RequiredArgsConstructor
@RestController
public class PaymentApiController {

    private final PortOnePaymentService portOnePaymentService;

    // POST /api/payment/prepare
    // 요청 바디 {"amount": 5000}
    @PostMapping("/api/payment/prepare")
    public ResponseEntity<?> prepare(
            @RequestBody PaymentRequest.PrepareDto prepareDto, HttpSession session) {

        // 인증 검사
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        if (sessionUserDto == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "로그인이 필요합니다."));
        }

        // 유효성 검사
        prepareDto.validate();

        PaymentResponse.PrepareDto prepareDto1 =
                portOnePaymentService.prepare(
                        sessionUserDto.getId(),
                        prepareDto.getAmount()
                );

        return ResponseEntity.ok(prepareDto1);
    }

    // POST /api/payment/complete
    @PostMapping("/api/payment/complete")
    public ResponseEntity<?> complete(
            @RequestBody PaymentRequest.CompleteDto completeDto, HttpSession session) {

        // 인증 검사
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        if (sessionUserDto == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "로그인이 필요합니다."));
        }
        completeDto.validate();

        return ResponseEntity.ok(
            portOnePaymentService.complete(sessionUserDto.getId(), completeDto.getPaymentId())
        );
    }
}
