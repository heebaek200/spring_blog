package com.tenco.spring_blog.refund;

import com.tenco.spring_blog._core.util.MyDateUtil;
import lombok.Data;

public class RefundResponse {

    public static class ListDto {
        private Long id;        // 환불 요청 PK
        private int amount;
        private String paymentId;
        private String requestedAt;
        private String reason;
        private String rejectReason;
        private String statusDisplay;

        private boolean pending;
        private boolean approved;
        private boolean rejected;

        public ListDto(Refund refund) {
            this.id = refund.getId();
            this.amount = refund.getPayment().getAmount();
            this.paymentId = refund.getPayment().getPaymentId();

            if (refund.getCreatedAt() != null) {
                this.requestedAt = MyDateUtil.timestampFormat(refund.getCreatedAt());
            }
            this.reason = refund.getReason();
            if (refund.getRejectReason() != null) {
                this.rejectReason = refund.getRejectReason();
            }
            this.statusDisplay = refund.getStatus().getDisplayName();

            this.pending  = refund.getStatus() == RefundStatus.PENDING;
            this.approved = refund.getStatus() == RefundStatus.APPROVED;
            this.rejected = refund.getStatus() == RefundStatus.REJECTED;
        }
    }

    @Data
    public static class AdminListDto {
        private Long id;                // 환불 요청 PK
        private String username;        // 환불 신청자 이름
        private String paymentId;       // 포트원 결제 건 식별자
        private int amount;             // 환불 금액
        private String reason;          // 신청자 환불 사유
        private String statusDisplay;   // 대기중 / 승인됨 / 거절됨

        private String requestedAt;     // 환불 신청일시
        private boolean pending;        // 대기 중 여부 (모달 버튼, 배지색)
        private boolean approved;       // 승인됨 여부 (배지 색)

        public AdminListDto(Refund refund) {
            this.id             = refund.getId();
            this.username       = refund.getUser().getUsername();
            this.paymentId      = refund.getPayment().getPaymentId();
            this.amount         = refund.getPayment().getAmount();
            this.reason         = refund.getReason();
            this.statusDisplay  = refund.getStatus().getDisplayName();

            if (refund.getCreatedAt() != null) {
                this.requestedAt = MyDateUtil.timestampFormat(refund.getCreatedAt());
            }
            this.pending        = refund.getStatus() == RefundStatus.PENDING;
            this.approved       = refund.getStatus() == RefundStatus.APPROVED;
        }
    }

    @Data
    public static class ApproveDto {
        private String paymentId;   // 포트원 결제건 식별자(경로)
        private int amount;         // DB 조회해야 알 수 있음

        public ApproveDto(String paymentId, int amount) {
            this.paymentId = paymentId;
            this.amount = amount;
        }
    }
}
