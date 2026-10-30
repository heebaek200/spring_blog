package com.tenco.spring_blog.payment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PaymentJpaRepository extends JpaRepository<Payment, Long> {

    @Query("""
SELECT
    p
FROM Payment AS p
WHERE
    p.paymentId = :paymentId
""")
    Optional<Payment> findByPaymentId(@Param("paymentId") String paymentId);

    @Query("""
SELECT
    CASE
        WHEN COUNT(p) > 0 THEN
            TRUE
        ELSE
            FALSE
    END
FROM Payment AS p
WHERE
    p.paymentId = :paymentId
""")
    boolean existsByPaymentId(@Param("paymentId") String paymentId);

    // 결제 내역 조회 (로그인 사용자)
    @Query("""
    SELECT
        p
    FROM Payment AS p
    WHERE
        p.user.id = :userId
    AND p.status  = :status
""")
    Page<Payment> findAllByUserIdAndStatus(
            @Param("userId") Long userId,
            @Param("status") PaymentStatus status,
            Pageable pageable
    );

    // 결제 내역 조회 (로그인 사용자)
    @Query("""
    SELECT
        p
    FROM Payment AS p
    WHERE
        p.user.id = :userId
    AND p.status in :statusList
""")
    Page<Payment> findAllByUserIdAndStatusList(
            @Param("userId") Long userId,
            @Param("statusList") List<PaymentStatus> statusList,
            Pageable pageable
    );
}
