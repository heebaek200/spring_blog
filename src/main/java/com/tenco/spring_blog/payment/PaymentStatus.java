package com.tenco.spring_blog.payment;

public enum PaymentStatus {
    READY("결제 대기"),     // 결제 예정
    PAID("결제 완료");      // 결제 확인 및 포인트 지급 완료

    private final String displayName;

    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
