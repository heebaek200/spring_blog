package com.tenco.spring_blog.payment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tenco.spring_blog._core.util.MyDateUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

public class PaymentResponse {

    // 1단계 응답 : 브라우저가 결제창 띄울 때 사용
    @Data
    public static class PrepareDto {
        private String paymentId;       // 서버가 발급한 결제건 식별자
        private int amount;             // 결제 요청 금액
        private String storeId;         // 포트원 상점 ID
        private String channelKey;      // 포트원 채널 키

        @Builder
        public PrepareDto(String paymentId, int amount, String storeId, String channelKey) {
            this.paymentId = paymentId;
            this.amount = amount;
            this.storeId = storeId;
            this.channelKey = channelKey;
        }
    }

    // 3단계 응답 : 충전 결과
    @Data
    @AllArgsConstructor
    public static class CompleteDto {
        private int amount;         // 충전된 포인트
        private int currentPoint;   // 충전 후 보유 포인트
    }

    // 포트원 v2 결제 단건 조회 응답 (필요한 필드만 사용)
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PortOnePayment {
        private String id;          // paymentId
        private String status;      // READY, PAID FAILED, CANCELED
        private String pgTxId;      // pg사 거래번호
        private Amount amount;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Amount {
            private Integer total;
        }
    }

    // 결제 내역 DTO
    @Data
    public static class ListDto {
        private Long id;
        private int amount;
        private String paymentId;
        private String pgTxId;
        private String paymentStatusDisplay;
        private String createdAt;

        public ListDto(Payment payment) {
            this.id = payment.getId();
            this.amount = payment.getAmount();
            this.paymentId = payment.getPaymentId();
            this.pgTxId = payment.getPgTxId();
            this.paymentStatusDisplay = payment.getStatus().getDisplayName();
            this.createdAt = MyDateUtil.timestampFormat(payment.getCreatedAt());
        }
    }

    // 페이지 번호 버튼 하나의 정보
    @Data
    @AllArgsConstructor
    public static class PageLink {
        private int number;                 // 표시할 페이지 번호
        private boolean active;             // 현재 페이지 여부 (강조 표시용)
    }

    // 구매 내역 페이지 정보 응답 DTO
    @Data
    public static class PageDto {
        private List<PaymentResponse.ListDto> content;      // 현재 페이지의 게시글 목록
        private boolean hasContent;
        private int size;                   // 페이지 크기
        private int currentPage;            // 현재 페이지 (1-base 보정)
        private int totalPages;             // 전체 페이지 수
        private long totalElements;         // 전체 게시글 수
        private boolean hasPrevious;        // 이전 페이지 여부
        private int previousPage;
        private boolean hasNext;            // 다음 페이지 여부
        private int nextPage;
        private List<PaymentResponse.PageLink> pageLinks;  // 화면에 표시할 페이지 번호 목록

        public PageDto(Page<Payment> page) {
            this.content = page.getContent().stream().map(PaymentResponse.ListDto::new).toList();
            this.hasContent = ! content.isEmpty();
            this.size = page.getSize();
            this.currentPage = page.getNumber() + 1;    // 0-base to 1-base
            this.totalPages = page.getTotalPages();
            this.totalElements = page.getTotalElements();
            this.hasPrevious = page.hasPrevious();
            this.previousPage = currentPage - 1;
            this.hasNext = page.hasNext();
            this.nextPage = currentPage + 1;
            this.pageLinks = makePageLinks(currentPage, totalPages);
        }

        private List<PaymentResponse.PageLink> makePageLinks(int currentPage, int totalPages) {
            List<PaymentResponse.PageLink> links = new ArrayList<>();
            int startPage = Math.max(1, currentPage - 2);           // 1보다 작아지지 않게
            int endPage = Math.min(totalPages, currentPage + 2);    // 전체 페이지 넘지 않게

            for (int i = startPage ; i <= endPage ; i++) {
                links.add(new PaymentResponse.PageLink(i, i == currentPage));
            }
            return links;
        }
    }
}
