package com.tenco.spring_blog.refund;

import com.tenco.spring_blog._core.error.Exception400;
import com.tenco.spring_blog._core.error.Exception403;
import com.tenco.spring_blog._core.error.Exception404;
import com.tenco.spring_blog.payment.Payment;
import com.tenco.spring_blog.payment.PaymentJpaRepository;
import com.tenco.spring_blog.payment.PaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class RefundService {

    private final RefundRepository refundRepository;
    private final PaymentJpaRepository paymentJpaRepository;

    /**
     * 환불 요청 폼에 보여줄 '결제' 정보
     */
    public PaymentResponse.ListDto findRequestForm(Long paymentPk, Long userId) {
        // 환불 요청 가능 여부 확인
        Payment payment = checkRefundable(paymentPk, userId);

        List<Long> refundedPkList = refundRepository.findPaymentPkListByUserId(userId);

        // 응답 정상 동작하면, 응답 DTO 생성 후 리턴
        return new PaymentResponse.ListDto(payment, refundedPkList);
    }

    private Payment checkRefundable(Long paymentPk, Long userId) {
        // 1. Payment 테이블 결제 내역 조회
        Payment payment = paymentJpaRepository.findById(paymentPk)
                .orElseThrow(() -> new Exception404("결제 내역을 찾을 수 없습니다."));

         // 2. 본인 결제인지 확인
        if (! payment.isOwner(userId)) {
            throw new Exception403("본인 결제만 환불 요청할 수 있습니다.");
        }

        // 3. 결제 완료 상태만 환불 가능 (READY, CANCELLED는 불가)
        if (! payment.isPaid()) {
            throw new Exception400("결제 완료된 건만 환불 요청할 수 있습니다.");
        }

        // 4. 환불 테이블에서 이미 환불 요청한 결제인지 확인 (대기 중, 승인됨, 거절됨 모두 신청 불가)
        if (refundRepository.findByPaymentPK(paymentPk).isPresent()) {
            throw new Exception400("이미 환불 요청한 결제입니다.");
        }

        return payment;
    }

    /**
     * 환불 요청 저장
     */
    @Transactional
    public void save(RefundRequest.SaveDto saveDto, Long userId) {
        // 1. 폼 요청 화면처럼 검사를 한 번 더 실행 (주소 직접 요청, 중복 제출 방지)
        Payment payment = checkRefundable(saveDto.getPaymentPk(), userId);

        // 2. 환불 요청 저장
        Refund refund = Refund.builder()
                .user(payment.getUser())
                .payment(payment)
                .reason(saveDto.getReason().trim())
                .build();

        refundRepository.save(refund);
    }

    /**
     * 환불 요청 내역 조회
     */
    public List<RefundResponse.ListDto> findAllByUserId(Long sessionUserId) {
        return refundRepository.findAllByUserIdJoinPayment(sessionUserId)
                .stream()
                .map(RefundResponse.ListDto::new)
                .toList();
    }
}
