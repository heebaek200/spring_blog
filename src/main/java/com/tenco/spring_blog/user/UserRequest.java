package com.tenco.spring_blog.user;

import lombok.Data;

public class UserRequest {

    // 회원가입용 DTO
    @Data
    public static class JoinDto {
        private String username;
        private String password;    // 추후 진도에서 암호화
        private String email;

        // 회원 가입 시 데이터 검증 메서드
        public void validate() {
            if (username == null || username.trim().isEmpty()) {
                throw new IllegalArgumentException("사용자명은 필수입니다.");
            }
            if (password == null || password.trim().isEmpty()) {
                throw new IllegalArgumentException("사용자명은 필수입니다.");
            }
            if (email == null || email.trim().isEmpty()) {
                throw new IllegalArgumentException("사용자명은 필수입니다.");
            }
            // 간단하게 이메일 형식 검증
            if (!email.contains("@")) {
                throw new IllegalArgumentException("올바른 이메일 형식이 아닙니다.");
            }
        }

        // DTO에서 User 엔티티로 변환하는 메서드
        // 계층간 데이터 변환을 명확하게 분리하는 것이 좋다.
        public User toEntity() {
//            validate();

            return User.builder()
                    .username(username)
                    .password(password)
                    .email(email)
                    .build();
        }
    } // end of join dto

    @Data
    public static class LoginDto {
        private String username;
        private String password;

        public void validate() {
            if (username == null || username.trim().isEmpty()) {
                throw new IllegalArgumentException("사용자명은 필수입니다.");
            }
            if (password == null || password.trim().isEmpty()) {
                throw new IllegalArgumentException("사용자명은 필수입니다.");
            }
        }
    }

}
