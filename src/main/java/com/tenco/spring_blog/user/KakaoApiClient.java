package com.tenco.spring_blog.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

// 카카오 서버와 Http 통신만 담당하는 클래스
@Slf4j
@Component // bean 관리 (IoC 싱글톤)
public class KakaoApiClient {

    @Value("${oauth.kakao.client-id}")
    private String kakaoClientId;

    @Value("${oauth.kakao.redirect-uri}")
    private String redirectUri;

    @Value("${oauth.kakao.client-secret}")
    private String clientSecret;

    // 인가 요청 주소
    private static String AUTHORIZE_URL = "https://kauth.kakao.com/oauth/authorize";

    // 액세스 토큰 요청 주소
    private static String TOKEN_URL = "https://kauth.kakao.com/oauth/token";

    // 프로필 이미지 요청 주소
    private static String PROFILE_URL = "https://kapi.kakao.com/v2/user/me";

    // 한 번 만들면 여러 요청에서 함께 써도 안전하게 되어 있는 클래스
    private final RestTemplate restTemplate = new RestTemplate();

    // 1. 카카오 로그인 화면 주소 만들기 (로그인 + 동의 화면)
    public String getAuthorizeUrl() {
        return AUTHORIZE_URL
                + "?client_id=%s&redirect_uri=%s&response_type=code"
                .formatted(kakaoClientId, redirectUri);
    }

    // 2. 인가 코드로 액세스 토큰 요청 (POST, 폼 데이터)
    public UserResponse.OAuthToken requestAccessToken(String code) {
        // 받은 인가 코드를 활용하여 토큰 요청 (POST)
        HttpHeaders headers = new HttpHeaders();
        // Content-Type: application/x-www-form-urlencoded;charset=utf-8
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 응답 바디
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakaoClientId);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);
        params.add("client_secret", clientSecret);

        HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity(params, headers);

        ResponseEntity<UserResponse.OAuthToken> response = restTemplate.exchange(
                "https://kauth.kakao.com/oauth/token",
                HttpMethod.POST,
                tokenRequest,
                UserResponse.OAuthToken.class
        );
        log.info("카카오 액세스 토큰 발급 완료"); // 토큰 값은 로그에 남기지 않음
        return response.getBody();
    }

    // 3. 액세스 토큰으로 사용자 정보 요청 (GET, 인증 헤더)
    public UserResponse.KakaoUserInfo requestProfile(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity request = new HttpEntity(headers);

        ResponseEntity<UserResponse.KakaoUserInfo> response = restTemplate.exchange(
                PROFILE_URL,
                HttpMethod.GET,
                request,
                UserResponse.KakaoUserInfo.class
        );

        return response.getBody();
    }

}
