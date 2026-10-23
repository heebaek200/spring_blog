package com.tenco.spring_blog.sms;

import com.tenco.spring_blog._core.error.Exception400;
import lombok.Getter;

public class SmsRequest {

    // 휴대폰 번호 인증 요청 DTO
    @Getter
    public static class SendDto {
        private String tel;
        private String code;

        public void validate() {
            if (tel == null || tel.trim().isEmpty()) {
                throw new Exception400("전화번호를 입력해주세요");
            }

            // 하이픈과 공백 제거
            tel = tel.replaceAll("[\\s-]", "");

            // 010 또는 기존 01X 이동전화 번호 검증
            if (!tel.matches("01(?:0\\d{8}|[16789]\\d{7,8})")) {
                throw new Exception400("올바른 휴대전화 번호를 입력해주세요.");
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

}
