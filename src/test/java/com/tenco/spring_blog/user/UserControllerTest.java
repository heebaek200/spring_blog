package com.tenco.spring_blog.user;

import com.tenco.spring_blog._core.util.Define;
import com.tenco.spring_blog.oauth.OAuthProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.sql.Timestamp;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * UserController 단위 테스트.
 * Spring ApplicationContext 전체를 띄우지 않고 standalone MockMvc를 사용한다.
 * Controller의 요청 처리, Service 호출, View/Redirect/Session 결과만 검증한다.
 */
@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    /**
     * 각 테스트 실행 전에 UserController와 MockMvc를 직접 생성한다.
     * Spring ApplicationContext를 사용하지 않아 다른 Config나 Interceptor의 영향을 받지 않는다.
     * OAuth 설정값은 @Value 대신 ReflectionTestUtils로 테스트 값을 직접 주입한다.
     */
    @BeforeEach
    void setUp() {
        // Controller에 Mock Service를 직접 주입한다.
        UserController userController =
                new UserController(userService);

        // @Value 필드는 Spring을 띄우지 않으므로 테스트 값을 직접 주입한다.
        ReflectionTestUtils.setField(
                userController,
                "kakaoClientId",
                "test-client-id"
        );

        ReflectionTestUtils.setField(
                userController,
                "redirectUri",
                "http://localhost:8080/oauth/kakao"
        );

        ReflectionTestUtils.setField(
                userController,
                "clientSecret",
                "test-client-secret"
        );

        // Controller 하나만 대상으로 MockMvc를 구성한다.
        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .build();
    }

    /**
     * 회원가입 화면 GET 요청을 검증한다.
     * 별도의 비즈니스 로직 없이 View 이름을 반환하는 메서드다.
     * HTTP 상태와 반환된 View 이름만 확인한다.
     */
    @Test
    void joinForm_회원가입화면_테스트() throws Exception {
        // given: 별도로 준비할 데이터가 없다.

        // when: 회원가입 화면을 요청한다.
        var result = mockMvc.perform(
                get("/join")
        );

        // then: 정상 상태와 회원가입 View를 확인한다.
        result
                .andExpect(status().isOk())
                .andExpect(
                        view().name("user/join-form")
                );
    }

    /**
     * 정상적인 회원가입 요청을 검증한다.
     * DTO 자체의 입력값 검증을 통과할 수 있는 Form 데이터를 전달한다.
     * Service 호출과 로그인 화면 Redirect 결과를 확인한다.
     */
    @Test
    void join_정상회원가입_테스트() throws Exception {
        // given: Mock Service는 정상 호출 시 별도의 Stub이 필요 없다.

        // when: 정상 회원가입 데이터를 POST로 전달한다.
        var result = mockMvc.perform(
                post("/join")
                        .param("username", "ssar")
                        .param("password", "1234")
                        .param("email", "ssar@test.com")
        );

        // then: 회원가입 후 로그인 화면으로 이동한다.
        result
                .andExpect(status().is3xxRedirection())
                .andExpect(
                        redirectedUrl("/login")
                );

        // then: 실제 회원가입 처리를 Service에 위임했는지 확인한다.
        verify(userService)
                .join(any(UserRequest.JoinDto.class));
    }

    /**
     * 정상 로그인 요청을 검증한다.
     * 실제 사용자 조회와 비밀번호 검증은 Service 테스트에서 처리한다.
     * Controller가 SessionUserDto를 Session에 저장하고 메인으로 이동하는지 확인한다.
     */
    @Test
    void login_정상로그인_테스트() throws Exception {
        // given: 로그인 성공 시 반환할 Session DTO를 준비한다.
        SessionUserDto sessionUserDto =
                createSessionUser();

        given(
                userService.login(
                        any(UserRequest.LoginDto.class)
                )
        ).willReturn(sessionUserDto);

        // when: 로그인 정보를 POST로 전달한다.
        var result = mockMvc.perform(
                post("/login")
                        .param("username", "ssar")
                        .param("password", "1234")
        );

        // then: 로그인 성공 후 메인 페이지로 이동한다.
        result
                .andExpect(status().is3xxRedirection())
                .andExpect(
                        redirectedUrl("/")
                )
                // then: 로그인 정보가 Session에 저장된다.
                .andExpect(
                        request().sessionAttribute(
                                Define.SESSION_USER,
                                sessionUserDto
                        )
                );
    }

    /**
     * 로그인한 사용자의 마이페이지 조회를 검증한다.
     * Session 사용자 ID를 이용해 Service를 호출하는 흐름을 확인한다.
     * Service 결과가 user라는 이름으로 Model에 저장되는지 검증한다.
     */
    @Test
    void detail_로그인사용자_테스트() throws Exception {
        // given: 로그인 사용자와 마이페이지 DTO를 준비한다.
        SessionUserDto sessionUserDto =
                createSessionUser();

        UserResponse.DetailDto detailDto =
                createDetailDto();

        given(userService.findDetail(1L))
                .willReturn(detailDto);

        // when: 로그인 Session과 함께 마이페이지를 요청한다.
        var result = mockMvc.perform(
                get("/user/detail")
                        .sessionAttr(
                                Define.SESSION_USER,
                                sessionUserDto
                        )
        );

        // then: 마이페이지 View와 Model 데이터를 확인한다.
        result
                .andExpect(status().isOk())
                .andExpect(
                        view().name("user/detail")
                )
                .andExpect(
                        model().attribute(
                                "user",
                                detailDto
                        )
                );

        // then: Session 사용자의 ID로 Service를 호출했는지 확인한다.
        verify(userService)
                .findDetail(1L);
    }

    /**
     * 로그인한 사용자의 회원정보 수정 화면을 검증한다.
     * Session 사용자 ID로 수정 화면 데이터를 조회하는 흐름을 확인한다.
     * 반환된 DTO가 user라는 Model 속성으로 전달되는지 검증한다.
     */
    @Test
    void updateForm_로그인사용자_테스트() throws Exception {
        // given: 로그인 Session과 수정 화면 DTO를 준비한다.
        SessionUserDto sessionUserDto =
                createSessionUser();

        UserResponse.UpdateFormDto updateFormDto =
                createUpdateFormDto();

        given(userService.findUpdateForm(1L))
                .willReturn(updateFormDto);

        // when: 로그인 Session과 함께 수정 화면을 요청한다.
        var result = mockMvc.perform(
                get("/user/update")
                        .sessionAttr(
                                Define.SESSION_USER,
                                sessionUserDto
                        )
        );

        // then: 수정 화면 View와 Model 데이터를 확인한다.
        result
                .andExpect(status().isOk())
                .andExpect(
                        view().name("user/update-form")
                )
                .andExpect(
                        model().attribute(
                                "user",
                                updateFormDto
                        )
                );

        // then: Session 사용자의 ID로 Service를 호출했는지 확인한다.
        verify(userService)
                .findUpdateForm(1L);
    }

    /**
     * 회원정보 수정 요청을 검증한다.
     * 입력 DTO 검증을 통과할 수 있도록 변경할 비밀번호를 전달한다.
     * 수정된 SessionUserDto가 Session에 다시 저장되는지 확인한다.
     */
    @Test
    void update_정상수정_테스트() throws Exception {
        // given: 기존 로그인 사용자와 수정 후 Session DTO를 준비한다.
        SessionUserDto sessionUserDto =
                createSessionUser();

        SessionUserDto updatedSessionUserDto =
                createSessionUser();

        given(
                userService.updateById(
                        any(Long.class),
                        any(UserRequest.UpdateDto.class)
                )
        ).willReturn(updatedSessionUserDto);

        // when: 새 비밀번호를 입력해 회원정보 수정을 요청한다.
        var result = mockMvc.perform(
                post("/user/update")
                        .sessionAttr(
                                Define.SESSION_USER,
                                sessionUserDto
                        )
                        .param(
                                "password",
                                "5678"
                        )
        );

        // then: 수정 완료 후 마이페이지로 Redirect한다.
        result
                .andExpect(status().is3xxRedirection())
                .andExpect(
                        redirectedUrl("/user/detail")
                )
                // then: 수정된 사용자 정보로 Session을 갱신한다.
                .andExpect(
                        request().sessionAttribute(
                                Define.SESSION_USER,
                                updatedSessionUserDto
                        )
                );

        // then: 현재 로그인 사용자의 ID로 수정 Service를 호출한다.
        verify(userService)
                .updateById(
                        any(Long.class),
                        any(UserRequest.UpdateDto.class)
                );
    }

    /**
     * 프로필 이미지 삭제 요청을 검증한다.
     * Session에서 로그인 사용자 ID를 꺼내 Service에 전달하는지 확인한다.
     * 삭제 처리 후 마이페이지로 Redirect되는지 검증한다.
     */
    @Test
    void deleteProfileImage_정상삭제_테스트()
            throws Exception {

        // given: 로그인 Session 사용자를 준비한다.
        SessionUserDto sessionUserDto =
                createSessionUser();

        // when: 프로필 이미지 삭제 요청을 보낸다.
        var result = mockMvc.perform(
                post("/user/profile-image/delete")
                        .sessionAttr(
                                Define.SESSION_USER,
                                sessionUserDto
                        )
        );

        // then: 삭제 후 마이페이지로 이동한다.
        result
                .andExpect(status().is3xxRedirection())
                .andExpect(
                        redirectedUrl("/user/detail")
                );

        // then: 현재 로그인 사용자의 ID로 삭제 Service를 호출한다.
        verify(userService)
                .deleteProfileImage(1L);
    }

    /**
     * 로그아웃 요청을 검증한다.
     * 기존 Session을 가진 상태에서 로그아웃 요청을 수행한다.
     * Session이 무효화되고 메인 페이지로 이동하는지 확인한다.
     */
    @Test
    void logout_로그아웃_테스트() throws Exception {
        // given: 로그인 사용자 정보를 Session에 넣는다.
        SessionUserDto sessionUserDto =
                createSessionUser();

        // when: 로그아웃을 요청한다.
        var result = mockMvc.perform(
                get("/logout")
                        .sessionAttr(
                                Define.SESSION_USER,
                                sessionUserDto
                        )
        );

        // then: 메인 페이지로 Redirect한다.
        result
                .andExpect(status().is3xxRedirection())
                .andExpect(
                        redirectedUrl("/")
                );
    }

    /**
     * 카카오 로그인 시작 요청을 검증한다.
     * 실제 카카오 서버를 호출하지 않고 인증 URL 생성까지만 확인한다.
     * 테스트에서 직접 주입한 OAuth 설정값이 URL에 반영되는지 검증한다.
     */
    @Test
    void kakaoLogin_로그인화면리다이렉트_테스트()
            throws Exception {

        // given: OAuth 테스트 설정은 setUp()에서 직접 주입했다.

        // when: 카카오 로그인 시작 URL을 요청한다.
        var result = mockMvc.perform(
                get("/kakao-login")
        );

        // then: 예상한 카카오 Authorization URL로 Redirect한다.
        result
                .andExpect(status().is3xxRedirection())
                .andExpect(
                        redirectedUrl(
                                "https://kauth.kakao.com/oauth/authorize"
                                        + "?client_id=test-client-id"
                                        + "&redirect_uri=http://localhost:8080/oauth/kakao"
                                        + "&response_type=code"
                        )
                );
    }

    /**
     * 테스트에서 사용할 로그인 사용자 Session DTO를 생성한다.
     * 실제 SessionUserDto와 동일하게 User Entity를 기반으로 생성한다.
     * 기본 USER 역할을 부여해 일반 사용자를 표현한다.
     */
    private SessionUserDto createSessionUser() {
        User user = createUser(null);

        // 일반 사용자 역할을 부여한다.
        user.addRole(Role.USER);

        return new SessionUserDto(user);
    }

    /**
     * 마이페이지에서 사용할 실제 DetailDto를 생성한다.
     * Entity와 DTO 모두 실제 객체를 사용한다.
     * 화면 테스트에 필요한 사용자 값을 동일하게 유지한다.
     */
    private UserResponse.DetailDto createDetailDto() {
        User user = createUser(null);

        return new UserResponse.DetailDto(user);
    }

    /**
     * 회원정보 수정 화면에서 사용할 실제 UpdateFormDto를 생성한다.
     * 프로필 이미지가 존재하는 사용자를 준비한다.
     * 실제 DTO를 사용해 테스트 데이터를 단순하게 구성한다.
     */
    private UserResponse.UpdateFormDto createUpdateFormDto() {
        User user = createUser("profile.jpg");

        return new UserResponse.UpdateFormDto(user);
    }

    /**
     * 각 테스트에서 사용할 User Entity를 생성한다.
     * DB에 저장하지 않고 Controller 테스트에 필요한 데이터만 구성한다.
     * 호출할 때마다 새 객체를 생성해 테스트 간 상태 공유를 막는다.
     */
    private User createUser(String profileImage) {
        return new User(
                1L,
                "ssar",
                "encoded-password",
                "ssar@test.com",
                Timestamp.valueOf(
                        "2026-10-21 10:00:00"
                ),
                profileImage,
                new ArrayList<>(),
                OAuthProvider.LOCAL,
                false
        );
    }
}