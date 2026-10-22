package com.tenco.spring_blog._core.util;

public class Define {

    // 로그인 유저
    public static final String SESSION_USER = "sessionUserDto";

    // 소셜 로그인 state
    public static final String SESSION_OAUTH_STATE = "oauthState";

    // 이메일 인증
    public static final String EMAIL_CODE_PREFIX = "emailCode_";    // emailCode_a@naver.com
    // 인증을 마친 이메일 주소
    public static final String VERIFIED_EMAIL = "verifiedEmail";
    // 인증번호 유효 시간 5분 (테스트 1분)
    public static final int CODE_EXPIRE_MINUTES = 5;

}
