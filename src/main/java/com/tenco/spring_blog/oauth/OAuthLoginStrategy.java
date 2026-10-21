package com.tenco.spring_blog.oauth;

public interface OAuthLoginStrategy {

    /**
     * 현재 전략이 담당하는 로그인 제공자를 반환합니다.
     * 공통 서비스는 이 값을 이용해 전략을 구분합니다.
     * LOCAL 회원 로그인은 전략 대상에서 제외합니다.
     */
    OAuthProvider provider();

    /**
     * 제공자의 인증 및 동의 화면 URL을 생성합니다.
     * 요청별로 생성된 state 값을 함께 전달합니다.
     * URL 생성에 필요한 제공자별 설정은 구현체에서 처리합니다.
     */
    String getAuthorizeUrl(String state);

    /**
     * 인가 코드를 이용하여 사용자 정보를 획득합니다.
     * 토큰 발급 및 사용자 정보 조회는 구현체가 담당합니다.
     * 조회 결과를 공통 OAuthUserProfile 형식으로 반환합니다.
     */
    OAuthResponse.UserProfile getUserProfile(String code, String state);
}