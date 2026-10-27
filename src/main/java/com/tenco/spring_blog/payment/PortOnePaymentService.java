package com.tenco.spring_blog.payment;

import com.tenco.spring_blog._core.error.Exception400;
import com.tenco.spring_blog._core.error.Exception500;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

@Slf4j
@RequiredArgsConstructor
@Service
public class PortOnePaymentService {

    private final PortOneApiClient portOneApiClient;
    private final PaymentService paymentService;

    public PaymentResponse.PrepareDto prepare(Long sessionUserId, Integer amount) {
        String paymentId = paymentService.prepare(sessionUserId, amount);

        return PaymentResponse.PrepareDto.builder()
                .paymentId(paymentId)
                .amount(amount)
                .storeId(portOneApiClient.getStoreId())
                .channelKey(portOneApiClient.getChannelKey())
                .build();
    }

    // 3단계 : 포트원 조회 (트랜잭션 밖) -> 검증(포트원 서버) -> 충전 (트랜잭션 안)
    public PaymentResponse.CompleteDto complete(Long sessionUserId, String paymentId) {

        PaymentResponse.PortOnePayment portOnePayment;

        try {
            // 1. 포트원 서버에 실제 결제 조회 (외부 통신)
            portOnePayment = portOneApiClient.getPayment(paymentId);

        } catch (HttpClientErrorException e) {
            // 포트원 4XX 응답 (없는 결제건, API 시크릿 키 오류)
            throw new Exception400("결제 서버에 연결할 수 없습니다.");
        } catch (ResourceAccessException e) {
            // 포트원 서버 연결 실패 (네트워크 오류, 시간 초과)
            throw new Exception500("결제 서버에 연결할 수 없습니다.");
        }

        if (portOnePayment == null) {
            throw new Exception400("결제 정보를 확인할 수 없습니다.");
        }

        // 2. 검증 (포트원 응답 데이터, 결제 DB 정보 비교 검증) --> 포인트 충전
        PaymentResponse.CompleteDto completeDto = paymentService.complete(sessionUserId, paymentId, portOnePayment);

        return completeDto;
    }
}
