package com.tenco.spring_blog.refund;

import lombok.Getter;

public enum RefundStatus {
    PENDING("대기 중"),
    APPROVED("승인됨"),
    REJECTED("거절됨");

    @Getter
    private final String displayName;

    RefundStatus(String displayName) {
        this.displayName = displayName;
    }
}
