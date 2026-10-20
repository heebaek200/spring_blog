package com.tenco.spring_blog.user;

import com.tenco.spring_blog._core.error.UnauthorizedException;
import com.tenco.spring_blog._core.util.Define;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
@Slf4j
@Controller
public class UserController {

    private final UserService userService;

    @Value("${oauth.kakao.client-id}")
    private String kakaoClientId;

    @Value("${oauth.kakao.redirect-uri}")
    private String redirectUri;

    @Value("${oauth.kakao.client-secret}")
    private String clientSecret;

    // 카카오 로그인 화면으로 리다이렉트
    @GetMapping("/kakao-login")
    public String kakaoLogin() {
        String url = "https://kauth.kakao.com/oauth/authorize?client_id=%s&redirect_uri=%s&response_type=code"
                .formatted(kakaoClientId, redirectUri);

        return "redirect:%s".formatted(url);
    }

    // 카카오 로그인 이후 처리
    @GetMapping("/oauth/kakao")
    public String kakaoCallback(
            @RequestParam(name = "code", required = false) String code,
            HttpSession session
    ) {
        if (code == null) throw new UnauthorizedException("카카오 로그인이 취소되었습니다.");

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<UserResponse.OauthToken> tokenResponse;

        try {
            // 받은 인가 코드를 활용하여 토큰 요청 (POST)
            HttpHeaders tokenHeaders = new HttpHeaders();
            // Content-Type: application/x-www-form-urlencoded;charset=utf-8
            tokenHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            // 응답 바디
            MultiValueMap<String, String> tokenParams = new LinkedMultiValueMap<>();
            tokenParams.add("grant_type", "authorization_code");
            tokenParams.add("client_id", kakaoClientId);
            tokenParams.add("redirect_uri", redirectUri);
            tokenParams.add("code", code);
            tokenParams.add("client_secret", clientSecret);

            HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity(tokenParams, tokenHeaders);

            tokenResponse = restTemplate.exchange(
                    "https://kauth.kakao.com/oauth/token",
                    HttpMethod.POST,
                    tokenRequest,
                    UserResponse.OauthToken.class
            );

        } catch (HttpClientErrorException e) {
            log.warn("액세스 토큰 정보 조회 중 문제 발생...");
            log.error("카카오 API 오류 : {}", e.getResponseBodyAsString());
            throw new UnauthorizedException("카카오 로그인에 실패했습니다.");
        }

        ResponseEntity<UserResponse.KakaoUserInfo> profileResponse;
        try {
            // 액세스 토큰으로 사용자 정보 요청(카카오 서버)

            HttpHeaders profileHeaders = new HttpHeaders();
            profileHeaders.setBearerAuth(tokenResponse.getBody().getAccessToken());
            profileHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity profileRequest = new HttpEntity(profileHeaders);

            profileResponse = restTemplate.exchange(
                    "https://kapi.kakao.com/v2/user/me",
                    HttpMethod.GET,
                    profileRequest,
                    UserResponse.KakaoUserInfo.class
            );

            log.info("사용자 정보 조회 : {}", profileResponse.getBody());

        } catch (HttpClientErrorException e) {
            log.warn("사용자 정보 조회 중 문제 발생...");
            log.error("카카오 API 오류 : {}", e.getResponseBodyAsString());
            throw new UnauthorizedException("카카오 로그인에 실패했습니다.");
        }

        // 최초 소셜 로그인 사용자라면 자동 회원가입 / 이미 가입되어 있다면 로그인
        UserResponse.KakaoUserInfo kakaoUserInfo = profileResponse.getBody();
        SessionUserDto sessionUserDto = userService.kakaoLogin(kakaoUserInfo);
        session.setAttribute(Define.SESSION_USER, sessionUserDto);

        return "redirect:/";
    }

    // 프로필 사진 삭제
    @PostMapping("/user/profile-image/delete")
    public String deleteProfileImage(HttpSession session) {
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        userService.deleteProfileImage(sessionUserDto.getId());

        return "redirect:/user/detail";
    }

    // GET http://localhost:8080/user/detail
    @GetMapping("/user/detail")
    public String detail(Model model, HttpSession session) {
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);

        // 조회
        UserResponse.DetailDto detailDto = userService.findDetail(sessionUserDto.getId());
        model.addAttribute("user", detailDto);

        return "user/detail";
    }

    // GET http://localhost:8080/join
    @GetMapping("/join")
    public String joinForm() {

        return "user/join-form";        // templates/user/join-form.mustache
    }

    // POST http://localhost:8080/join
    @PostMapping("/join")
    public String join(UserRequest.JoinDto joinDto) {
        // 유효성 검사
        joinDto.validate();

        userService.join(joinDto);

        // 회원 가입 성공 시 로그인 화면으로 이동
        return "redirect:/login";
    }

    // GET http://localhost:8080/login
    @GetMapping("/login")
    public String loginForm() {

        return "user/login-form";
    }

    // POST http://localhost:8080/login
    // 로그인 처리 (반드시 POST 요청)
    @PostMapping("/login")
    public String login(UserRequest.LoginDto loginDto, HttpSession session) {
        // 입력 데이터 검증
        loginDto.validate();

        SessionUserDto sessionUserDto = userService.login(loginDto);
        session.setAttribute(Define.SESSION_USER, sessionUserDto);

        // 메인 페이지로 리다이렉트 처리
        return "redirect:/";
    }

    // GET http://localhost:8080/user/update
    @GetMapping("/user/update")
    public String updateForm(
            Model model,
            HttpSession session
    ) {
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        UserResponse.UpdateFormDto updateFormDto = userService.findUpdateForm(sessionUserDto.getId());

        model.addAttribute("user", updateFormDto);

        return "user/update-form";
    }

    // POST http://localhost:8080/user/update
    @PostMapping("/user/update")
    public String update(
            UserRequest.UpdateDto updateDto,
            HttpSession session
    ) {
        updateDto.validate();

        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);

        SessionUserDto updateUserDto = userService.updateById(sessionUserDto.getId(), updateDto);

        // 세션 동기화 : 수정된 정보를 세션에 반영
        session.setAttribute(Define.SESSION_USER, updateUserDto);

        // 성공 후 메인페이지로 리다이렉트
        return "redirect:/user/detail";
    }

    // GET http://localhost:8080/logout
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        // 세션 무효화 처리
        session.invalidate();

        return "redirect:/";
    }

}
