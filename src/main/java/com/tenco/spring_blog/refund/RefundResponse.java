package com.tenco.spring_blog.refund;

import com.tenco.spring_blog._core.util.MyDateUtil;

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

}
