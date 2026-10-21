package com.tenco.spring_blog.user;

import com.tenco.spring_blog._core.util.Define;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequiredArgsConstructor
@Slf4j
@Controller
public class UserController {

    private final UserService userService;
    private final KakaoLoginService kakaoLoginService;

    // 카카오 로그인 화면으로 리다이렉트
    @GetMapping("/kakao-login")
    public String kakaoLogin() {
        return "redirect:%s".formatted(
                kakaoLoginService.getAuthorizeUrl()
        );
    }

    // 카카오 로그인 화면 이후 처리
    @GetMapping("/oauth/kakao")
    public String kakaoCallback(
            @RequestParam(name = "code", required = false) String code,
            HttpSession session
    ) {
        // 카카오 로그인
        SessionUserDto sessionUserDto = kakaoLoginService.login(code);

        // 세션 저장
        session.setAttribute(Define.SESSION_USER, sessionUserDto);

        // 리다이렉트
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
