package com.tenco.spring_blog.user;

import com.tenco.spring_blog._core.util.MyDateUtil;
import lombok.Data;

public class UserResponse {

    // 마이페이지 응답 DTO
    @Data
    public static class DetailDto {
        private Long id;
        private String username;
        private String email;
        private String profileImage;    // 파일 이름 (없으면 null)
        private String createdAt;       // 포맷된 가입일

        public DetailDto(User user) {
            this.id = user.getId();
            this.username = user.getUsername();
            this.email = user.getEmail();
            this.profileImage = user.getProfilePath();
            this.createdAt = MyDateUtil.timestampFormat(user.getCreatedAt());
        }
    }

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
        private String profileImage;

        public UpdateFormDto(User user) {
            this.id = user.getId();
            this.username = user.getUsername();
            this.email = user.getEmail();
            this.profileImage = user.getProfilePath();
        }
    }

}
