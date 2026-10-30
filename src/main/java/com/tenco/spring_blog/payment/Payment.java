package com.tenco.spring_blog.payment;

import com.tenco.spring_blog.user.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

// 결제 내역 엔티티 (포트원 V2)
// - paymentId : 우리 서버가 발급한 결제 건 식별자 (중복 지급 방지, 유니크)
// - amount : 결제 예정 금액 (실제 결제는 포트원에서 처리 -> 응답 -> 실제 결제 금액과 우리 사이트 결제 금액 비교)
@Entity
@Getter
@NoArgsConstructor
@Table(
        name = "payment_tb"
)
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 결제 건 식별자
    @Column(unique = true, nullable = false, length = 100)
    private String paymentId;

    // PG사 거래 번호 (결제 완료 후 추적용)
    @Column(nullable = true, length = 255)
    private String pgTxId;

    // 결제한 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 결제 금액 (충전할 포인트)
    @Column(nullable = false)
    private int amount;

    // 결제 대기, 환불, 결제 완료 구분
    @Enumerated(value = EnumType.STRING)
    private PaymentStatus status;

    @CreationTimestamp
    private Timestamp createdAt;

    @Builder
    public Payment(String paymentId, User user, int amount) {
        this.paymentId = paymentId;
        this.user = user;
        this.amount = amount;
        this.status = PaymentStatus.READY;
    }

    // 편의 메서드 : 결제를 요청한 사용자인지 확인
    public boolean isOwner(Long userId) {
        return this.user.getId().equals(userId);
    }

    // 편의 메서드 : 이미 처리(포인트 지급)된 결제인지 확인
    public boolean isPaid() {
        return this.status == PaymentStatus.PAID;
    }

    // 검증 통과 후 결제 완료로 변경 처리 (더티 체킹)
    public void markPaid(String pgTxId) {
        // 결제 상태를 완료로 변경
        this.status = PaymentStatus.PAID;
        this.pgTxId = pgTxId;
    }

    public void markCancelled() {
        this.status = PaymentStatus.CANCELLED;
    }
}
