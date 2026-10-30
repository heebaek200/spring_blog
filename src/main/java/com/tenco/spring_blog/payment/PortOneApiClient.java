package com.tenco.spring_blog.payment;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

// 포트원 서버와 HTTP 통신만 담당하는 클래스 (포트원 설정값도 이곳에서 관리)
@Slf4j
@Getter
@Component
public class PortOneApiClient {

    @Value("${pg-payment.port-one.store-id}")
    private String storeId;

    @Value("${pg-payment.port-one.channel-key}")
    private String channelKey;

    @Value("${pg-payment.port-one.api-secret}")
    private String apiSecret;

    private static final String PAYMENT_URL
            = "https://api.portone.io/payments";

    // 여러 요청에서 함께 써도 안전!
    private final RestTemplate restTemplate = new RestTemplate();

    // 포트원 서버에 결제 단건 조회
    // GET, 헤더 설정
    public PaymentResponse.PortOnePayment getPayment(String paymentId) {
        // 요청 헤더 작성
        HttpHeaders headers = new HttpHeaders();

        // 반드시 공백 한 칸
        headers.set(HttpHeaders.AUTHORIZATION, "PortOne %s".formatted(apiSecret));

        // GET 요청이라 바디 없이 헤더만 담음
        HttpEntity request = new HttpEntity(headers);

        ResponseEntity<PaymentResponse.PortOnePayment> response = restTemplate.exchange(
                "%s/%s".formatted(PAYMENT_URL, paymentId),
                HttpMethod.GET,
                request,
                PaymentResponse.PortOnePayment.class
        );

        log.info("포트원 결제 조회 완료 : {}", response);

        return response.getBody();
    }

    // 포트원 서버에 결제 취소 요청
    // 문서 확인 : amount를 넣으면 그 금액만큼 취소됨(부분 환불 가능), 넣지 않으면 전액 취소.
    //
    public PaymentResponse.PortOneCancel cancelPayment(String paymentId, int amount) {
        // 요청 헤더 작성
        HttpHeaders requestHeaders = new HttpHeaders();

        requestHeaders.set(HttpHeaders.AUTHORIZATION, "PortOne %s".formatted(apiSecret));
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);

        // 요청 바디 작성
        Map<String, Object> requestBody = Map.of(
                "amount", amount,
                "reason", "관리자 환불 승인"
        );

        HttpEntity request = new HttpEntity(requestBody, requestHeaders);

        // 요청 및 응답
        ResponseEntity<PaymentResponse.PortOneCancel> response = restTemplate.exchange(
                "%s/%s/cancel".formatted(PAYMENT_URL, paymentId),
                HttpMethod.POST,
                request,
                PaymentResponse.PortOneCancel.class
        );

        return response.getBody();
    }
}
