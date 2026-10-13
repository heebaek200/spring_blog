package com.tenco.spring_blog.board;

import com.tenco.spring_blog._core.util.MyDateUtil;
import lombok.Data;

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

        public DetailDto(Board board) {
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
        }
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

        public UpdateFormDto(Board board) {
            this.id = board.getId();
            this.title = board.getTitle();
            this.content = board.getContent();
        }
    }
}
