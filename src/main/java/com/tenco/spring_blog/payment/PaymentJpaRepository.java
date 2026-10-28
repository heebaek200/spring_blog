package com.tenco.spring_blog.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

}
