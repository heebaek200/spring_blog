package com.tenco.spring_blog.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.time.ZonedDateTime;

public class OAuthResponse {

    // 소셜 액세스 토큰 응답 DTO
    @Getter
    @Setter
    public static class OAuthToken {

        @JsonProperty("access_token")
        private String accessToken;

        @JsonProperty("token_type")
        private String tokenType;

        @JsonProperty("refresh_token")
        private String refreshToken;

        @JsonProperty("expires_in")
        private Long expiresIn;

        @JsonProperty("scope")
        private String scope;

        @JsonProperty("refresh_token_expires_in")
        private Long refreshTokenExpiresIn;
    }

    // 공통 소셜 샤용자 정보 응답 DTO
    @Getter
    @NoArgsConstructor
    @ToString
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class UserProfile {

        private String id;
        private String username;
        private String profileImage;
        private String email;

        @Builder
        public UserProfile(String id, String username, String profileImage, String email) {
            this.id = id;
            this.username = username;
            this.profileImage = profileImage;
            this.email = email;
        }
    }

    // 카카오 소셜 사용자 정보 응답 DTO
    @Getter
    @NoArgsConstructor
    @ToString
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class KakaoUserProfile {

        private  Long id;                  // JSON의 (소셜 회원번호)
        private ZonedDateTime connectedAt; // 연결된 시각 (RFC3339 규격 호환)
        private Properties properties;

        @Getter
        @NoArgsConstructor
        @ToString
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class Properties {
            private String nickname;
            private String profileImage;
            private String thumbnailImage;
        }

        public UserProfile toUserProfile() {
            return UserProfile.builder()
                    .id(String.valueOf(id))
                    .username(this.properties.nickname)
                    .profileImage(this.properties.profileImage)
                    .email(this.properties.nickname + "@kakao.com")
                    .build();
        }
    }

    // 구글 소셜 사용자 정보 응답 DTO
    @Getter
    @NoArgsConstructor
    @ToString
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class GoogleUserProfile {
        private String sub;
        private String name;
        private String email;
        private boolean emailVerified;
        private String picture;

        public UserProfile toUserProfile() {
            return UserProfile.builder()
                    .id(this.sub)
                    .username(this.name)
                    .profileImage(this.picture)
                    .email(this.email)
                    .build();
        }
    }

    // 네이버 소셜 사용자 정보 응답 DTO
    @Getter
    @NoArgsConstructor
    @ToString
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class NaverUserProfile {

        private String resultcode;
        private String connectedAt;
        private Response response;

        @Getter
        @NoArgsConstructor
        @ToString
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class Response {
            private String id;
            private String email;
            private String nickname;
            private String profileImage;
        }

        public UserProfile toUserProfile() {
            String tempNickname = (response.nickname == null || response.nickname.isEmpty()) ? "naver_" + response.id : response.nickname;

            return UserProfile.builder()
                    .id(response.id)
                    .username( tempNickname )
                    .profileImage((response.profileImage == null || response.profileImage.isEmpty()) ? null : response.profileImage)
                    .email((response.email == null || response.email.isEmpty()) ? tempNickname + "@naver.com" : response.email)
                    .build();
        }
    }
}
