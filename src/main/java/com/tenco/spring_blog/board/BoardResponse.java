package com.tenco.spring_blog.board;

import com.tenco.spring_blog._core.util.MyDateUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

/**
 * 게시글 응답 DTO
 *
 * - 서비스에서 엔티티를 DTO로 변환하여 반환한다.
 * - OSIV 설정을 끄더라도 지연 로딩 오류가 나지 않음.
 */
public class BoardResponse {

    // 게시글 상세 응답 DTO
    @Data
    public static class DetailDto {
        private Long id;
        private String title;
        private String content;
        private Long userId;
        private String username;
        private String createdAt;

        private boolean premium;    // 유료 게시글 여부
        private int price;          // 가격 (안내 문구용)
        private boolean owner;      // 로그인 사용자의 작성자 여부
        private boolean purchased;  // 로그인 사용자의 구매 여부
        private boolean canRead;    // 본문 열람 가능 여부 (계산해서 내려보냄. 무료글 or 작성자 or 구매자)

        public DetailDto(Board board, boolean owner, boolean purchased) {
            this.id = board.getId();
            this.title = board.getTitle();
            this.content = board.getContent();

            // 지연 로딩 오류 방지를 위한 방어적 코드 : 평탄화 작업
            if (board.getUser() != null) {
                this.userId = board.getUser().getId();
                this.username = board.getUser().getUsername();
            }
            if (board.getCreatedAt() != null) {
                this.createdAt = MyDateUtil.timestampFormat(board.getCreatedAt());
            }

            this.premium = board.isPremium();
            this.price = 500;   // 추후 수정
            this.owner = owner;
            this.purchased = purchased;
            this.canRead = !premium || owner || purchased;

            // 읽을 수 있는 여부에 따라 내용을 지워 내려보내는 방법이 있다
            if (! this.canRead) {
                this.content = null;
            }
        }
    }

    // 게시글 목록 페이지 정보 응답 DTO
    @Data
    public static class PageDto {
        private List<ListDto> content;      // 현재 페이지의 게시글 목록
        private int size;                   // 페이지 크기
        private int currentPage;            // 현재 페이지 (1-base 보정)
        private int totalPages;             // 전체 페이지 수
        private long totalElements;         // 전체 게시글 수
        private boolean hasPrevious;        // 이전 페이지 여부
        private int previousPage;
        private boolean hasNext;            // 다음 페이지 여부
        private int nextPage;
        private List<PageLink> pageLinks;  // 화면에 표시할 페이지 번호 목록

        public PageDto(Page<Board> page) {
            this.content = page.getContent().stream().map(ListDto::new).toList();
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

        private List<PageLink> makePageLinks(int currentPage, int totalPages) {
            List<PageLink> links = new ArrayList<>();
            int startPage = Math.max(1, currentPage - 2);           // 1보다 작아지지 않게
            int endPage = Math.min(totalPages, currentPage + 2);    // 전체 페이지 넘지 않게

            for (int i = startPage ; i <= endPage ; i++) {
                links.add(new PageLink(i, i == currentPage));
            }
            return links;
        }
    }

    // 페이지 번호 버튼 하나의 정보
    @Data
    @AllArgsConstructor
    public static class PageLink {
        private int number;                 // 표시할 페이지 번호
        private boolean active;             // 현재 페이지 여부 (강조 표시용)
    }

    // 게시글 목록 응답 DTO
    @Data
    public static class ListDto {
        private Long id;
        private String title;
        private String username;
        private String createdAt;

        public ListDto(Board board) {
            this.id = board.getId();
            this.title = board.getTitle();

            if (board.getUser() != null) {
                this.username = board.getUser().getUsername();
            }

            this.createdAt = board.getTime();
        }
    }

    // 게시글 수정 화면 응답 DTO
    @Data
    public static class UpdateFormDto {
        private Long id;
        private String title;
        private String content;
        private boolean premium;

        public UpdateFormDto(Board board) {
            this.id = board.getId();
            this.title = board.getTitle();
            this.content = board.getContent();
            this.premium = board.isPremium();
        }
    }
}
