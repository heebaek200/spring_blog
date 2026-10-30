package com.tenco.spring_blog.refund;

import com.tenco.spring_blog.payment.Payment;
import com.tenco.spring_blog.user.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;

@Getter
@NoArgsConstructor
@Entity
@Table(
        name = "refund_tb"
)
public class Refund {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                // pk

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;              // 환불 요청한 사용자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false, unique = true)
    private Payment payment;        // 결제 데이터 (부분환불없는 1:1 -> 혹시모를 1:N 확장을 위해 ManyToOne으로 열어놓고 유니크 제약 추가)

    @Column(nullable = false, length = 500)
    private String reason;          // 환불 사유

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RefundStatus status;    // 환불 상태 (최초 : 대기)

    @Column(length = 500)
    private String rejectReason;    // 관리자 거절 사유

    @CreationTimestamp
    private Timestamp createdAt;    // 환불 요청 일시

    @UpdateTimestamp
    private Timestamp updatedAt;    // 마지막 변경 시각 (승인, 거절)

    @Builder
    public Refund(User user, Payment payment, String reason) {
        this.user = user;
        this.payment = payment;
        this.reason = reason;

        this.status = RefundStatus.PENDING;
    }

    // 관리자가 아직 처리하지 않은 요청인지 확인
    public boolean isPending() {
        return this.status == RefundStatus.PENDING;
    }

    // 승인 처리
    public void approve() {
        this.status = RefundStatus.APPROVED;
    }

    // 거절 처리 + 거절 사유
    public void reject(String rejectReason) {
        this.status = RefundStatus.REJECTED;
        this.rejectReason = rejectReason;
    }

}
