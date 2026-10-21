package com.tenco.spring_blog.oauth;

import com.tenco.spring_blog._core.error.Exception401;
import com.tenco.spring_blog._core.error.Exception404;
import com.tenco.spring_blog._core.util.Define;
import com.tenco.spring_blog.user.SessionUserDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@Controller
public class OAuthController {
    private final OAuthLoginService oAuthLoginService;


    // 소셜 로그인 화면으로 리다이렉트
    @GetMapping("/oauth-login/{social}")
    public String oauthLogin(
            @PathVariable(name = "social") String social,
            HttpSession session
    ) {
        OAuthProvider provider;
        try {
            provider = OAuthProvider.valueOf(social.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.error("지원하지 않는 형태의 소셜 로그인입니다. : {}", social);
            log.error("TO DEV: OAuthProvider의 요소를 추가하고, OAuthLoginStrategy를 구현한 클래스를 작성해주세요. 참고: '전략 패턴'");
            throw new Exception404("지원하지 않는 형태의 소셜 로그인입니다.");
        }

        // OAuth CSRF 방지용 state 생성 및 세션 저장
        String state = UUID.randomUUID().toString();
        session.setAttribute(Define.SESSION_OAUTH_STATE, state);

        return "redirect:%s".formatted(
                oAuthLoginService.getAuthorizeUrl(provider, state)
        );
    }

    // 소셜 로그인 화면 이후 처리
    @GetMapping("/oauth/{social}")
    public String socialCallback(
            @PathVariable(name = "social") String social,
            @RequestParam(name = "code", required = false) String code,
            HttpSession session
    ) {
        OAuthProvider provider;
        try {
            provider = OAuthProvider.valueOf(social.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("지원하지 않는 형태의 소셜 로그인입니다 : {}", social);
            throw new Exception404("지원하지 않는 형태의 소셜 로그인입니다.");
        }

        String state = (String) session.getAttribute(Define.SESSION_OAUTH_STATE);
        if (state == null) {
            throw new Exception401("소셜 로그인 중 오류가 발생했습니다.");
        }

        // 소셜 로그인
        SessionUserDto sessionUserDto = oAuthLoginService.login(provider, code, state);

        // 세션 저장
        session.setAttribute(Define.SESSION_USER, sessionUserDto);

        // 리다이렉트
        return "redirect:/";
    }

}
