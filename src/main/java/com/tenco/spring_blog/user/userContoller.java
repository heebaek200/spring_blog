package com.tenco.spring_blog.user;

import com.tenco.spring_blog._core.util.Define;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@RequiredArgsConstructor
@Slf4j
@Controller
public class userContoller {

    private final UserService userService;

    // GET http://localhost:8080/join
    @GetMapping("/join")
    public String joinForm() {

        return "user/join-form";        // templates/user/join-form.mustache
    }

    // POST http://localhost:8080/join
    @PostMapping("/join")
    public String join(UserRequest.JoinDto joinDto, Model model) {
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

        User user = userService.login(loginDto);

        // 로그인 성공 : 세션에 사용자 정보를 저장
        user.setPassword(null);  // 보안
        session.setAttribute(Define.SESSION_USER, user);

        // 메인 페이지로 리다이렉트 처리
        return "redirect:/";
    }

    // GET http://localhost:8080/user/update
    @GetMapping("/user/update")
    public String updateForm(
            Model model,
            HttpSession session
    ) {
        User sessionUser = (User) session.getAttribute(Define.SESSION_USER);
        User user = userService.findById(sessionUser.getId());

        model.addAttribute("user", user);

        return "user/update-form";
    }

    // POST http://localhost:8080/user/update
    @PostMapping("/user/update")
    public String update(
            UserRequest.UpdateDto updateDto,
            HttpSession session,
            Model model
    ) {
        updateDto.validate();

        User sessionUser = (User) session.getAttribute(Define.SESSION_USER);

        User user = userService.updateById(sessionUser.getId(), updateDto);

        // 세션 동기화 : 수정된 정보를 세션에 반영
        user.setPassword(null);  // 보안
        session.setAttribute(Define.SESSION_USER, user);

        // 성공 후 메인페이지로 리다이렉트
        return "redirect:/";
    }

    // GET http://localhost:8080/logout
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        // 세션 무효화 처리
        session.invalidate();

        return "redirect:/";
    }

}
