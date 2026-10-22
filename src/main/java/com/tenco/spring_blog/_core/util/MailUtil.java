package com.tenco.spring_blog._core.util;

import java.security.SecureRandom;

public class MailUtil {

    // 예측하기 어려운 난수 생성기
    private static final SecureRandom RANDOM = new SecureRandom();

    // 6자리 인증번호 생성 (100000 ~ 999999)
    public static String generateCode() {
        return String.valueOf(RANDOM.nextInt(900_000) + 100_000);
    }

}
