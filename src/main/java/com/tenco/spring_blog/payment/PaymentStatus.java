package com.tenco.spring_blog.payment;

import lombok.Getter;

public enum PaymentStatus {
    READY("결제 대기"),
    PAID("결제 완료"),
    CANCELLED("결제 취소");

    @Getter
    private final String displayName;

    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }
}
