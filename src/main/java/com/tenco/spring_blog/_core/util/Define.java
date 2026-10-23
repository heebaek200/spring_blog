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

    // 인증번호 유효 시간 5분
    public static final int CODE_EXPIRE_MINUTES = 5;

    // 휴대폰 번호 인증
    public static final String SMS_CODE_PREFIX = "smsCode_";    // smsCode_01011112222
    // SMS 인증번호 재발송 제한 시간 관리
    public static final String SMS_RESEND_INTERVAL = "smsResendInterval";
    // SMS 인증번호 재발송 제한 시간 (60초)
    public static final int SMS_RESEND_INTERVAL_SECONDS = 60;
    // 입력 횟수 제한 (5회)
    public static final int SMS_INPUT_LIMIT = 5;

}
