package com.tenco.spring_blog.oauth;

import com.tenco.spring_blog._core.error.Exception400;
import com.tenco.spring_blog._core.error.UnauthorizedException;
import com.tenco.spring_blog.user.Role;
import com.tenco.spring_blog.user.SessionUserDto;
import com.tenco.spring_blog.user.User;
import com.tenco.spring_blog.user.UserJpaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OAuthLoginService {

    private final Map<OAuthProvider, OAuthLoginStrategy> oAuthLoginStrategyMap;
    private final UserJpaRepository userJpaRepository;
    private final PasswordEncoder passwordEncoder;

    // 전략 패턴 용 map에 할당하는 생성자. List 구조로 DI 받은 후 Map 구조로 변환, provider에 따라 전략 사용
    public OAuthLoginService(
            List<OAuthLoginStrategy> oAuthLoginStrategyList,
            UserJpaRepository userJpaRepository,
            PasswordEncoder passwordEncoder
    ) {

        this.oAuthLoginStrategyMap = oAuthLoginStrategyList.stream()
                .collect(Collectors.toUnmodifiableMap(
                        OAuthLoginStrategy::provider,
                        Function.identity()
                ));

        this.userJpaRepository = userJpaRepository;
        this.passwordEncoder = passwordEncoder;

    }

    // 소셜 로그인 화면 주소
    public String getAuthorizeUrl(OAuthProvider provider, String state) {
        return oAuthLoginStrategyMap.get(provider).getAuthorizeUrl(state);
    }

    // 소셜 로그인 : 인가 코드 -> 토큰 -> 사용자 정보 -> 가입 또는 로그인
    public SessionUserDto login(OAuthProvider provider, String code, String state) {
        if (code == null) {
            throw new UnauthorizedException("소셜 로그인이 취소되었습니다.");
        }

        try {
            // 소셜 서버 통신
            OAuthResponse.UserProfile userInfo = oAuthLoginStrategyMap
                    .get(provider)
                    .getUserProfile(code, state);

            // 자동 회원가입 또는 로그인
            return socialLogin(provider, userInfo);

        } catch (HttpClientErrorException e) {
            // 소셜 4XX 응답을 한 경우 (인가 코드 재사용, 키 오류 등)
            log.warn("소셜 API 오류 : {}", e.getResponseBodyAsString());
            throw new UnauthorizedException("소셜 로그인에 실패했습니다. 다시 시도해 주세요.");
        } catch (ResourceAccessException e) {
            // 소셜 서버에 연결하지 못한 경우 (네트워크 오류, 시간 초과 등)
            log.warn("소셜 서버에 연결 실패 : {}", e.getMessage());
            throw new UnauthorizedException("소셜 서버에 연결할 수 없습니다. 잠시 후 다시 시도하세요.");
        }
    }

    // 소셜 로그인 : 최초 소셜 로그인 사용자라면 자동 회원가입 / 이미 가입되어 있다면 로그인
    @Transactional
    private SessionUserDto socialLogin(OAuthProvider provider, OAuthResponse.UserProfile userInfo) {

        // 1. 소셜 회원번호로 우리 서비스의 사용자명 작성
        String username = provider + "_" + userInfo.getId();

        // 2. 이미 가입했는지 확인, 없으면 자동 회원가입
        User user = userJpaRepository.findByUsername(username)
                .orElseGet(() -> joinSocialUser(provider, username, userInfo));

        // 3. 같은 이름의 일반 회원이 있다면 소셜 사용자로 로그인 시키지 않음
        if (user.getProvider() != provider) {
            throw new Exception400("이미 사용 중인 사용자명입니다.");
        }

        // 트랜잭션 안에서 sessionUserDto 생성
        return new SessionUserDto(user);
    }

    private User joinSocialUser(OAuthProvider provider, String username, OAuthResponse.UserProfile UserInfo) {
        User newUser = User.builder()
                .username(username)
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .email(UserInfo.getEmail())
                .profileImage(UserInfo.getProfileImage())
                .provider(provider)
                .build();
        newUser.addRole(Role.USER);

        return userJpaRepository.save(newUser);
    }
}
