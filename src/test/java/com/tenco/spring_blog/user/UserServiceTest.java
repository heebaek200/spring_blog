package com.tenco.spring_blog.user;

import com.tenco.spring_blog._core.error.Exception400;
import com.tenco.spring_blog._core.error.Exception401;
import com.tenco.spring_blog._core.error.Exception404;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserJpaRepository userJpaRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    /**
     * 정상적인 회원가입 흐름을 검증한다.
     * Repository와 PasswordEncoder는 실제 객체가 아니라 Mockito Mock을 사용하며,
     * 중복 사용자가 없을 때 Entity가 저장되는지만 확인한다.
     */
    @Test
    void join_정상회원가입_테스트() {
        // given: 중복되지 않은 회원가입 DTO와 Mock 동작을 준비한다.
        UserRequest.JoinDto joinDto =
                mock(UserRequest.JoinDto.class);

        User user = User.builder() // 확인 필요: 현재 User 빌더 구조 기준
                .username("ssar")
                .password("encoded-password")
                .email("ssar@test.com")
                .provider(OAuthProvider.LOCAL)
                .build();

        given(joinDto.getUsername()).willReturn("ssar");
        given(joinDto.getPassword()).willReturn("1234");
        given(joinDto.getEmail()).willReturn("ssar@test.com");
        given(joinDto.getProfileImage()).willReturn(null);

        given(userJpaRepository.findByUsername("ssar"))
                .willReturn(Optional.empty());

        given(userJpaRepository.findByEmail("ssar@test.com"))
                .willReturn(Optional.empty());

        given(passwordEncoder.encode("1234"))
                .willReturn("encoded-password");

        given(
                joinDto.toEntity(
                        null,
                        "encoded-password"
                )
        ).willReturn(user); // 확인 필요: 업로드한 UserService의 toEntity(profileImage, encodedPassword) 호출 기준

        given(userJpaRepository.save(user))
                .willReturn(user);

        // when: 회원가입 서비스를 실행한다.
        userService.join(joinDto);

        // then: 최종 User Entity 저장이 한 번 호출되었는지 확인한다.
        verify(userJpaRepository).save(user);
    }

    /**
     * 이미 존재하는 사용자명으로 가입하는 경우를 검증한다.
     * 사용자명 중복 단계에서 즉시 Exception400이 발생해야 한다.
     * 이후의 저장 로직은 실행되지 않아야 한다.
     */
    @Test
    void join_사용자명중복_테스트() {
        // given: 이미 존재하는 사용자명을 준비한다.
        UserRequest.JoinDto joinDto =
                mock(UserRequest.JoinDto.class);

        User existingUser = createUser(
                "ssar",
                "encoded",
                "old@test.com",
                OAuthProvider.LOCAL
        );

        given(joinDto.getUsername()).willReturn("ssar");

        given(userJpaRepository.findByUsername("ssar"))
                .willReturn(Optional.of(existingUser));

        // when & then: 사용자명 중복 예외와 메시지를 확인한다.
        assertThatThrownBy(
                () -> userService.join(joinDto)
        )
                .isInstanceOf(Exception400.class)
                .hasMessage(
                        "이미 존재하는 사용자명입니다."
                );

        verify(userJpaRepository, never())
                .save(any(User.class));
    }

    /**
     * 사용자명은 중복되지 않지만 이메일이 중복되는 경우를 검증한다.
     * 이메일 중복 확인 단계에서 Exception400이 발생해야 한다.
     * 실패한 회원가입에서는 Entity 저장이 호출되지 않아야 한다.
     */
    @Test
    void join_이메일중복_테스트() {
        // given: 이메일만 기존 회원과 중복되도록 준비한다.
        UserRequest.JoinDto joinDto =
                mock(UserRequest.JoinDto.class);

        User existingUser = createUser(
                "cos",
                "encoded",
                "ssar@test.com",
                OAuthProvider.LOCAL
        );

        given(joinDto.getUsername()).willReturn("ssar");
        given(joinDto.getEmail())
                .willReturn("ssar@test.com");

        given(userJpaRepository.findByUsername("ssar"))
                .willReturn(Optional.empty());

        given(
                userJpaRepository.findByEmail(
                        "ssar@test.com"
                )
        ).willReturn(Optional.of(existingUser));

        // when & then: 이메일 중복 예외를 확인한다.
        assertThatThrownBy(
                () -> userService.join(joinDto)
        )
                .isInstanceOf(Exception400.class)
                .hasMessage(
                        "이미 사용 중인 이메일입니다."
                );

        verify(userJpaRepository, never())
                .save(any(User.class));
    }

    /**
     * 사용자명과 비밀번호가 모두 올바른 로그인 흐름을 검증한다.
     * Repository가 사용자 Entity를 반환하고 PasswordEncoder의 matches가 true를 반환하도록 설정한다.
     * 로그인 성공 시 SessionUserDto가 생성되는지 확인한다.
     */
    @Test
    void login_정상로그인_테스트() {
        // given: 저장된 사용자와 올바른 비밀번호를 준비한다.
        UserRequest.LoginDto loginDto =
                new UserRequest.LoginDto();

        loginDto.setUsername("ssar");
        loginDto.setPassword("1234");

        User user = createUser(
                "ssar",
                "encoded-password",
                "ssar@test.com",
                OAuthProvider.LOCAL
        );
        user.addRole(Role.USER);

        given(
                userJpaRepository.findByUsernameWithRoles(
                        "ssar"
                )
        ).willReturn(Optional.of(user));

        given(
                passwordEncoder.matches(
                        "1234",
                        "encoded-password"
                )
        ).willReturn(true);

        // when: 로그인 서비스를 실행한다.
        SessionUserDto result =
                userService.login(loginDto);

        // then: 로그인한 사용자명이 Session DTO에 담겼는지 확인한다.
        assertThat(result.getUsername())
                .isEqualTo("ssar");
    }

    /**
     * 존재하지 않는 사용자명의 로그인 실패를 검증한다.
     * Repository가 Optional.empty()를 반환하도록 설정한다.
     * Service는 사용자 존재 여부를 외부에 노출하지 않는 공통 메시지를 반환한다.
     */
    @Test
    void login_존재하지않는사용자_테스트() {
        // given: 존재하지 않는 사용자명을 준비한다.
        UserRequest.LoginDto loginDto =
                new UserRequest.LoginDto();

        loginDto.setUsername("unknown");
        loginDto.setPassword("1234");

        given(
                userJpaRepository.findByUsernameWithRoles(
                        "unknown"
                )
        ).willReturn(Optional.empty());

        // when & then: 401 예외와 공통 로그인 실패 메시지를 확인한다.
        assertThatThrownBy(
                () -> userService.login(loginDto)
        )
                .isInstanceOf(Exception401.class)
                .hasMessage(
                        "사용자명 또는 비밀번호가 올바르지 않습니다."
                );
    }

    /**
     * 사용자는 존재하지만 비밀번호가 다른 상황을 검증한다.
     * PasswordEncoder.matches가 false를 반환하도록 Mock의 동작을 정의한다.
     * 사용자명 오류와 동일한 Exception401 메시지가 반환되어야 한다.
     */
    @Test
    void login_비밀번호불일치_테스트() {
        // given: 존재하는 사용자와 잘못된 입력 비밀번호를 준비한다.
        UserRequest.LoginDto loginDto =
                new UserRequest.LoginDto();

        loginDto.setUsername("ssar");
        loginDto.setPassword("wrong");

        User user = createUser(
                "ssar",
                "encoded-password",
                "ssar@test.com",
                OAuthProvider.LOCAL
        );

        given(
                userJpaRepository.findByUsernameWithRoles(
                        "ssar"
                )
        ).willReturn(Optional.of(user));

        given(
                passwordEncoder.matches(
                        "wrong",
                        "encoded-password"
                )
        ).willReturn(false);

        // when & then: 비밀번호 불일치 예외를 확인한다.
        assertThatThrownBy(
                () -> userService.login(loginDto)
        )
                .isInstanceOf(Exception401.class)
                .hasMessage(
                        "사용자명 또는 비밀번호가 올바르지 않습니다."
                );
    }

    /**
     * 존재하지 않는 사용자 마이페이지 조회를 검증한다.
     * 내부 findById가 Repository의 Optional.empty()를 처리하는 흐름을 확인한다.
     * 최종적으로 Exception404가 발생해야 한다.
     */
    @Test
    void findDetail_존재하지않는사용자_테스트() {
        // given: ID 999 사용자가 존재하지 않도록 설정한다.
        given(userJpaRepository.findById(999L))
                .willReturn(Optional.empty());

        // when & then: 사용자 없음 예외를 확인한다.
        assertThatThrownBy(
                () -> userService.findDetail(999L)
        )
                .isInstanceOf(Exception404.class)
                .hasMessage(
                        "사용자를 찾을 수 없습니다."
                );
    }

    /**
     * 이미 가입된 카카오 사용자의 소셜 로그인을 검증한다.
     * 카카오 회원번호로 만든 username의 사용자가 존재한다고 가정한다.
     * provider가 KAKAO라면 새 사용자를 저장하지 않고 기존 사용자로 로그인한다.
     */
    @Test
    void kakaoLogin_기존카카오회원_테스트() {
        // given: 카카오 ID와 기존 카카오 사용자를 준비한다.
        UserResponse.KakaoUserInfo kakaoUserInfo =
                mock(UserResponse.KakaoUserInfo.class);

        given(kakaoUserInfo.getId())
                .willReturn(12345L);

        User user = createUser(
                "kakao_12345",
                "encoded",
                "kakao_12345@kakao.com",
                OAuthProvider.KAKAO
        );
        user.addRole(Role.USER);

        given(
                userJpaRepository.findByUsername(
                        "kakao_12345"
                )
        ).willReturn(Optional.of(user));

        // when: 카카오 로그인을 수행한다.
        SessionUserDto result =
                userService.kakaoLogin(kakaoUserInfo);

        // then: 기존 카카오 사용자의 username이 반환되는지 확인한다.
        assertThat(result.getUsername())
                .isEqualTo("kakao_12345");

        verify(userJpaRepository, never())
                .save(any(User.class));
    }

    /**
     * 카카오가 사용할 username을 LOCAL 회원이 이미 사용하는 상황을 검증한다.
     * 동일 username이라도 provider가 KAKAO가 아니라면 로그인시키지 않는다.
     * Service의 가입 경로 검증 규칙이 작동하는지 확인한다.
     */
    @Test
    void kakaoLogin_일반회원과사용자명충돌_테스트() {
        // given: 같은 username을 사용하는 LOCAL 사용자를 준비한다.
        UserResponse.KakaoUserInfo kakaoUserInfo =
                mock(UserResponse.KakaoUserInfo.class);

        given(kakaoUserInfo.getId())
                .willReturn(12345L);

        User localUser = createUser(
                "kakao_12345",
                "encoded",
                "local@test.com",
                OAuthProvider.LOCAL
        );

        given(
                userJpaRepository.findByUsername(
                        "kakao_12345"
                )
        ).willReturn(Optional.of(localUser));

        // when & then: 가입 경로 충돌 예외를 확인한다.
        assertThatThrownBy(
                () -> userService.kakaoLogin(kakaoUserInfo)
        )
                .isInstanceOf(Exception400.class)
                .hasMessage(
                        "이미 사용 중인 사용자명입니다."
                );
    }

    /**
     * Service 테스트에서 반복해서 사용할 User Entity를 생성한다.
     * DB에 저장하지 않는 순수 단위 테스트이므로 실제 ID 생성은 필요하지 않다.
     * 필요한 도메인 값만 Builder를 통해 준비한다.
     */
    private User createUser(
            String username,
            String password,
            String email,
            OAuthProvider provider
    ) {
        return User.builder() // 확인 필요: 현재 프로젝트 User 빌더 구조 기준
                .username(username)
                .password(password)
                .email(email)
                .provider(provider)
                .build();
    }
}