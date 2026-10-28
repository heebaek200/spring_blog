package com.tenco.spring_blog.purchase;

import com.tenco.spring_blog._core.util.MyDateUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

public class PurchaseResponse {

    // 구매 내역 DTO
    @Data
    public static class ListDto {
        private Long id;
        private int price;
        private Long boardId;
        private String boardTitle;
        private String boardUsername;
        private String createdAt;

        public ListDto(Purchase purchase) {
            // 평탄화
            this.id = purchase.getId();
            this.price = purchase.getPrice();

            if (purchase.getBoard() != null) {
                this.boardId = purchase.getBoard().getId();
                this.boardTitle = purchase.getBoard().getTitle();

                if (purchase.getBoard().getUser() != null) {
                    this.boardUsername = purchase.getBoard().getUser().getUsername();
                }
            }

            this.createdAt = MyDateUtil.timestampFormat(purchase.getCreatedAt());
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
        private List<PurchaseResponse.ListDto> content;      // 현재 페이지의 게시글 목록
        private boolean hasContent;
        private int size;                   // 페이지 크기
        private int currentPage;            // 현재 페이지 (1-base 보정)
        private int totalPages;             // 전체 페이지 수
        private long totalElements;         // 전체 게시글 수
        private boolean hasPrevious;        // 이전 페이지 여부
        private int previousPage;
        private boolean hasNext;            // 다음 페이지 여부
        private int nextPage;
        private List<PurchaseResponse.PageLink> pageLinks;  // 화면에 표시할 페이지 번호 목록

        public PageDto(Page<Purchase> page) {
            this.content = page.getContent().stream().map(PurchaseResponse.ListDto::new).toList();
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

        private List<PurchaseResponse.PageLink> makePageLinks(int currentPage, int totalPages) {
            List<PurchaseResponse.PageLink> links = new ArrayList<>();
            int startPage = Math.max(1, currentPage - 2);           // 1보다 작아지지 않게
            int endPage = Math.min(totalPages, currentPage + 2);    // 전체 페이지 넘지 않게

            for (int i = startPage ; i <= endPage ; i++) {
                links.add(new PurchaseResponse.PageLink(i, i == currentPage));
            }
            return links;
        }
    }
}
