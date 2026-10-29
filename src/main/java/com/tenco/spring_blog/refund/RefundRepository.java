package com.tenco.spring_blog.refund;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RefundRepository extends JpaRepository<Refund, Long> {

    // 결제 (payment.id)로 조회 기능 추가
    @Query("""
SELECT
    r
FROM Refund AS r
WHERE
    r.payment.id = :paymentPk
""")
    Optional<Refund> findByPaymentPK(@Param("paymentPk") Long paymentPk);

    // 사용자가 환불 요청한 결제 PK 목록 (결제 내역 화면에 환불 버튼 표시로 사용됨)
    @Query("""
SELECT
    r.payment.id
FROM
    Refund AS r
WHERE
    r.user.id = :userId
""")
    List<Long> findPaymentPkListByUserId(
            @Param("userId") Long userId
    );

    // 로그인한 사용자의 환불 요청 내역 (결제 정보 함께 조회, 최신순)
    @Query("""
SELECT
    r
FROM
    Refund AS r
JOIN FETCH
    r.payment
WHERE
    r.user.id = :userId
ORDER BY
    r.id DESC
""")
    List<Refund> findAllByUserIdJoinPayment(
            @Param("userId") Long userId
    );

    // 관리자 전체 환불 요청 목록

    // 관리자가 승인할 환불 요청 1건 정보 조회

}
