package com.tenco.spring_blog.board;

import com.tenco.spring_blog._core.error.Exception400;
import com.tenco.spring_blog.user.User;
import lombok.Data;

public class BoardRequest {

    @Data
    public static class SaveDto {
        private String title;
        private String content;
        private String username;

        // 검증 메서드 (선택 사항)
        public void validate() {
            if (title == null || title.trim().isEmpty()) {
                throw new Exception400("제목은 필수입니다");
            }
            if (content == null || content.trim().isEmpty()) {
                throw new Exception400("내용은 필수입니다");
            }
        }

        // DTO에서 Entity로 변환하는 편의 메서드 설계
        public Board toEntity(User user) {
            return Board.builder()
                    .title(title)
                    .content(content)
                    .user(user)
                    .build();
        }
    }

    @Data
    public static class UpdateDto {
        private String title;
        private String content;

        // 검증 메서드 (선택 사항)
        public void validate() {
            if (title == null || title.trim().isEmpty()) {
                throw new Exception400("제목은 필수입니다");
            }
            if (content == null || content.trim().isEmpty()) {
                throw new Exception400("내용은 필수입니다");
            }
        }
    }

}
