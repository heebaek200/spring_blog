package com.tenco.spring_blog.user;

import lombok.Data;

public class UserResponse {

    @Data
    public static class JoinDto {
        private Long id;
        private String username;
        private String email;

        public JoinDto(User user) {
            this.id = user.getId();
            this.username = user.getUsername();
            this.email = user.getEmail();
        }
    }

    // 회원정보 수정 화면 응답 DTO
    @Data
    public static class UpdateFormDto {
        private Long id;
        private String username;
        private String email;

        public UpdateFormDto(User user) {
            this.id = user.getId();
            this.username = user.getUsername();
            this.email = user.getEmail();
        }
    }

}
