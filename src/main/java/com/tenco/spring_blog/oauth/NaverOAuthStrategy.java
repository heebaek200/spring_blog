package com.tenco.spring_blog.oauth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;

// NAVER 서버와의 Http 통신
@Slf4j
@Component
public class NaverOAuthStrategy implements OAuthLoginStrategy {

    @Value("${oauth.naver.client-id}")
    private String clientId;

    @Value("${oauth.naver.client-secret}")
    private String clientSecret;

    @Value("${oauth.naver.redirect-uri}")
    private String redirectUri;

    // 인가 요청 주소
    private static String AUTHORIZE_URL = "https://nid.naver.com/oauth2.0/authorize";

    // 액세스 토큰 요청 주소
    private static String TOKEN_URL = "https://nid.naver.com/oauth2.0/token";

    // 프로필 이미지 요청 주소
    private static String PROFILE_URL = "https://openapi.naver.com/v1/nid/me";

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public OAuthProvider provider() {
        return OAuthProvider.NAVER;
    }

    @Override
    public String getAuthorizeUrl(String state) {
        return AUTHORIZE_URL + (
                "?response_type=code" +
                "&client_id=%s" +
                "&redirect_uri=%s" +
                "&state=%s"
        ).formatted(
                clientId,
                UriUtils.encode(redirectUri, StandardCharsets.UTF_8),
                UriUtils.encode(state, StandardCharsets.UTF_8));
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
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("code", code);
        params.add("state", state); // 네이버는 이때도 state?

        HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity(params, headers);

        ResponseEntity<OAuthResponse.OAuthToken> response = restTemplate.exchange(
                TOKEN_URL,
                HttpMethod.POST,
                tokenRequest,
                OAuthResponse.OAuthToken.class
        );
        log.info("네이버 액세스 토큰 발급 완료"); // 토큰 값은 로그에 남기지 않음
        return response.getBody();
    }

    // 액세스 토큰으로 사용자 정보 요청 (GET, 인증 헤더)
    private OAuthResponse.UserProfile requestProfile(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity request = new HttpEntity(headers);

        ResponseEntity<OAuthResponse.NaverUserProfile> response = restTemplate.exchange(
                PROFILE_URL,
                HttpMethod.GET,
                request,
                OAuthResponse.NaverUserProfile.class
        );

        return response.getBody().toUserProfile();
    }
}
