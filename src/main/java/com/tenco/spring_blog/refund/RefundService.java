package com.tenco.spring_blog.refund;

import com.tenco.spring_blog._core.error.Exception400;
import com.tenco.spring_blog._core.error.Exception403;
import com.tenco.spring_blog._core.error.Exception404;
import com.tenco.spring_blog.payment.Payment;
import com.tenco.spring_blog.payment.PaymentJpaRepository;
import com.tenco.spring_blog.payment.PaymentResponse;
import com.tenco.spring_blog.user.User;
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

    // 관리자 환불 요청 조회
    public List<RefundResponse.AdminListDto> findAllForAdmin() {
        List<Refund> refundList = refundRepository.findAllJoinUserAndPayment();

        return refundList.stream()
                .map(RefundResponse.AdminListDto::new)
                .toList();
    }

    // 환불 요청 거절
    @Transactional
    public void reject(Long refundId, String rejectReason) {
        // 1. 넘겨받은 refundId 기준으로 엔티티 조회 (더티 체킹)
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new Exception404("환불 요청을 찾을 수 없습니다."));

        // 2. 이미 승인, 거절된 요청은 처리하지 않는다.
        if (! refund.isPending()) {
            throw new Exception400("대기 중인 환불 요청만 거절할 수 있습니다.");
        }

        // 단일 책임
        refund.reject(rejectReason);
    }


    /**
     * 관리자 승인 1단계 : 승인 가능한지 검사 (DB만 조회)
     * @param refundId
     * @return : 포트원 결제 취소에 필요한 값
     */
    public RefundResponse.ApproveDto checkApprove(Long refundId) {
        // 환불 엔티티
        Refund refund = refundRepository.findByIdJoinUserAndPayment(refundId)
                .orElseThrow(() -> new Exception404("환불 요청을 찾을 수 없습니다."));

        if (! refund.isPending()) {
            throw new Exception400("대기 중인 환불 요청만 승인할 수 있습니다.");
        }

        Payment payment = refund.getPayment();
        if (! payment.isPaid()) {
            throw new Exception400("결제 완료 상태만 환불 신청할 수 있습니다.");
        }

        // User 충전한 포인트를 이미 사용했다면, 돌려받을 포인트가 없어서 환불 불가.
        User user = refund.getUser();
        if (user.getPoint() < payment.getAmount()) {
            throw new Exception400("포인트 잔액이 부족하여 환불할 수 없습니다.");
        }

        return new RefundResponse.ApproveDto(payment.getPaymentId(), payment.getAmount());
    }

    // 관리자 승인 4단계 : 포트원 결제 취소가 끝난 뒤 DB 반영
    @Transactional
    public void approve(Long refundId) {
        Refund refund = refundRepository.findByIdJoinUserAndPayment(refundId)
                .orElseThrow(() -> new Exception404("환불 요청을 찾을 수 없습니다."));

        // 1~4단계 사이에 같은건 처리가 있을 수 있어 한 번 더 확인
        if (! refund.isPending()) {
            throw new Exception400("대기 중인 환불 요청만 승인할 수 있습니다.");
        }

        Payment payment = refund.getPayment();
        User user = refund.getUser();

        // 포인트 차감
        user.deductPoint(payment.getAmount());

        // 결제 -> 취소
        payment.markCancelled();

        // 환불 요청 -> 승인
        refund.approve();
    }
}
