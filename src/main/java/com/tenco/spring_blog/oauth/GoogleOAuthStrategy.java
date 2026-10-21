package com.tenco.spring_blog.oauth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

// GOOGLE 서버와 Http 통신
@Slf4j
@Component
public class GoogleOAuthStrategy implements OAuthLoginStrategy {

    @Value("${oauth.google.client-id}")
    private String clientId;

    @Value("${oauth.google.client-secret}")
    private String clientSecret;

    @Value("${oauth.google.redirect-uri}")
    private String redirectUri;

    // 인가 요청 주소
    private static String AUTHORIZE_URL = "https://accounts.google.com/o/oauth2/v2/auth";

    // 액세스 토큰 요청 주소
    private static String TOKEN_URL = "https://oauth2.googleapis.com/token";

    // 프로필 이미지 요청 주소
    private static String PROFILE_URL = "https://openidconnect.googleapis.com/v1/userinfo";

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public OAuthProvider provider() {
        return OAuthProvider.GOOGLE;
    }

    @Override
    public String getAuthorizeUrl(String state) {
        return AUTHORIZE_URL + (
                "?client_id=%s" +
                "&redirect_uri=%s" +
                "&response_type=code" +
                "&scope=openid%%20email%%20profile" +
                "&state=%s"
        ).formatted(clientId, redirectUri, state);
    }

    @Override
    public OAuthResponse.UserProfile getUserProfile(String code, String state) {
        // 1. 인가 코드로 액세스 토큰 발급 (서버 통신)
        OAuthResponse.OAuthToken oAuthToken = requestAccessToken(code, state);

        // 2. 액세스 토큰으로 사용자 정보 조회 (서버)
        return requestProfile(oAuthToken.getAccessToken());
    }

    // 인가 코드로 액세스 토큰 요청 (POST, 폼 데이터)
    private OAuthResponse.OAuthToken requestAccessToken(String code, String state) {
        // 받은 인가 코드를 활용하여 토큰 요청 (POST)
        HttpHeaders headers = new HttpHeaders();
        // Content-Type: application/x-www-form-urlencoded;charset=utf-8
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 응답 바디
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("code", code);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri); // 구글은 이때도 redirect_uri?

        HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity(params, headers);

        ResponseEntity<OAuthResponse.OAuthToken> response = restTemplate.exchange(
                TOKEN_URL,
                HttpMethod.POST,
                tokenRequest,
                OAuthResponse.OAuthToken.class
        );
        log.info("구글 액세스 토큰 발급 완료"); // 토큰 값은 로그에 남기지 않음
        return response.getBody();
    }

    // 액세스 토큰으로 사용자 정보 요청 (GET, 인증 헤더)
    private OAuthResponse.UserProfile requestProfile(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity request = new HttpEntity(headers);

        ResponseEntity<OAuthResponse.GoogleUserProfile> response = restTemplate.exchange(
                PROFILE_URL,
                HttpMethod.GET,
                request,
                OAuthResponse.GoogleUserProfile.class
        );

        return response.getBody().toUserProfile();
    }
}
