package com.tenco.spring_blog.refund;

import com.tenco.spring_blog._core.error.Exception400;
import lombok.Data;

public class RefundRequest {

    @Data
    public static class SaveDto {
        private Long paymentPk;     // 결제 내역 id(PK)
        private String reason;      // 환불 사유

        public void validate() {
            if (paymentPk == null) {
                throw new Exception400("결제 정보가 없습니다.");
            }

            if (reason == null || reason.trim().isEmpty()) {
                throw new Exception400("환불 사유는 필수입니다.");
            }

            if (reason.length() > 500) {
                throw new Exception400("환불 사유는 500자 이하로 입력해주세요.");
            }
        }
    }

}
