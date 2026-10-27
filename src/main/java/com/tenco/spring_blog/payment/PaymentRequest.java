package com.tenco.spring_blog.payment;

import com.tenco.spring_blog._core.error.Exception400;
import lombok.Data;

public class PaymentRequest {

    // 1단계 : 결제 건 생성 요청 {amount : 5000}
    @Data
    public static class PrepareDto {
        private Integer amount;

        public void validate() {
            if (amount == null || amount < 1_000) {
                throw new Exception400("최소 충전 금액은 1,000 포인트입니다.");
            }
            if (amount > 100_000) {
                throw new Exception400("최대 충전 금액은 100,000 포인트입니다.");
            }
        }
    }

    // 3단계 : 결제 검증 요청
    @Data
    public static class CompleteDto {
        private String paymentId;

        public void validate() {
            if (paymentId == null || paymentId.isBlank()) {
                throw new Exception400("결제 식별자가 필요합니다.");
            }
        }
    }

}
