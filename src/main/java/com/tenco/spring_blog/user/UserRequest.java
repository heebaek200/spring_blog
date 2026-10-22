package com.tenco.spring_blog.user;

import com.tenco.spring_blog._core.error.Exception400;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

public class UserRequest {

    // 이메일 인증 요청 DTO
    @Data
    public static class EmailCheckDto {
        private String email;   // 이메일 주소
        private String code;    // 인증번호 (2번째 요청 시 사용)

        public void validate() {
            if (email == null || email.trim().isEmpty()) {
                throw new Exception400("이메일을 입력해주세요");
            }

            if (!email.contains("@")) {
                throw new Exception400("올바른 이메일 형식이 아닙니다");
            }
        }

        // 인증번호 확인: API에서 추가로 호출할 예정
        public void validateCode() {
            validate();
            if (code == null || code.trim().isEmpty()) {
                throw new Exception400("인증번호를 입력해주세요");
            }
        }
    }

    // 회원정보 수정용 DTO
    @Data
    public static class UpdateDto {
        private String password;
        private MultipartFile profileImage;     // 프로필 이미지 (선택 사항)

        // 비밀번호를 입력했는지 확인 (null, "", 공백 입력 시 -> 입력 안함으로 판정)
        public boolean hasPassword() {
            // 비밀번호 입력 시 true 반환
            return password != null && !password.isBlank();
        }

        // 새 프로필 사진을 골랐는지 확인
        public boolean hasProfileImage() {
            return profileImage != null && !profileImage.isEmpty();
        }

        public void validate() {
            if (!hasPassword() && !hasProfileImage()) {
                throw new Exception400("변경할 비밀번호 또는 프로필 사진을 입력해주세요");
            }

            if (hasPassword() && password.length() < 4) {
                throw new Exception400("비밀번호는 4글자 이상이어야 합니다.");
            }
            // 필요하다면 길이 수, 특수문자 포함여부(정규식) 활용 가능
        }
    }

    // 회원가입용 DTO
    @Data
    public static class JoinDto {
        private String username;
        private String password;    // 추후 진도에서 암호화
        private String email;
        private MultipartFile profileImage;     // 프로필 이미지 (선택 사항)

        // 회원 가입 시 데이터 검증 메서드
        public void validate() {
            if (username == null || username.trim().isEmpty()) {
                throw new Exception400("사용자명은 필수입니다.");
            }
            if (password == null || password.trim().isEmpty()) {
                throw new Exception400("비밀번호는 필수입니다.");
            }
            if (email == null || email.trim().isEmpty()) {
                throw new Exception400("이메일은 필수입니다.");
            }
            // 간단하게 이메일 형식 검증
            if (!email.contains("@")) {
                throw new Exception400("올바른 이메일 형식이 아닙니다.");
            }
        }

        // DTO에서 User 엔티티로 변환하는 메서드
        // 계층간 데이터 변환을 명확하게 분리하는 것이 좋다.
        public User toEntity(String profileImageFilename, String encodedPassword) {
//            validate();

            return User.builder()
                    .username(username)
                    .password(encodedPassword)
                    .email(email)
                    .profileImage(profileImageFilename)
                    .build();
        }
    } // end of join dto

    @Data
    public static class LoginDto {
        private String username;
        private String password;

        public void validate() {
            if (username == null || username.trim().isEmpty()) {
                throw new Exception400("사용자명은 필수입니다.");
            }
            if (password == null || password.trim().isEmpty()) {
                throw new Exception400("비밀번호는 필수입니다.");
            }
        }
    }

}
