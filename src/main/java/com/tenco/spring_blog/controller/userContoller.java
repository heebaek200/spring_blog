package com.tenco.spring_blog.controller;

import com.tenco.spring_blog._core.error.Exception400;
import com.tenco.spring_blog._core.error.Exception404;
import com.tenco.spring_blog._core.util.Define;
import com.tenco.spring_blog.user.User;
import com.tenco.spring_blog.user.UserPersistenceRepository;
import com.tenco.spring_blog.user.UserRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Map;

@RequiredArgsConstructor
@Slf4j
@Controller
public class userContoller {

    private final UserPersistenceRepository userPersistenceRepository;

    // GET http://localhost:8080/join
    @GetMapping("/join")
    public String joinForm() {

        return "user/join-form";        // templates/user/join-form.mustache
    }

    // POST http://localhost:8080/join
    @PostMapping("/join")
    public String join(UserRequest.JoinDto joinDto, Model model) {
        // 1 유효성 검사
        joinDto.validate();

        // 2. 사용자명 중복 체크
        User existingUser = userPersistenceRepository.findByUsername(joinDto.getUsername());
        if (existingUser != null) {
            throw new Exception400("이미 존재하는 사용자명입니다.");
        }

        // 3. DTO 객체를 Entity로 변환
        User user = joinDto.toEntity();

        // 4. DB에 회원정보 저장
        User userEntity = userPersistenceRepository.save(user);

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
    public String login(UserRequest.LoginDto loginDto, HttpSession session, Model model) {
        // 1. 입력 데이터 검증
        loginDto.validate();

        // 2. 사용자명과 비밀번호로 사용자 조회
        User sessionUser = userPersistenceRepository.findByUsernameAndPassword(
                loginDto.getUsername(),
                loginDto.getPassword()
        );

        // 3. 로그인 실패 처리 -> 인터셉터
        if (sessionUser == null) {
            throw new Exception400(
                    "사용자명 또는 비밀번호가 올바르지 않습니다."
            );
        }

        // 4. 로그인 성공 : 세션에 사용자 정보를 저장
        sessionUser.setPassword(null);  // 보안
        session.setAttribute(Define.SESSION_USER, sessionUser);

        log.info("로그인한 사용자 : {}", sessionUser.getUsername());

        // 5. 메인 페이지로 리다이렉트 처리
        return "redirect:/";
    }

    // GET http://localhost:8080/user/update
    @GetMapping("/user/update")
    public String updateForm(
            Model model,
            HttpSession session
    ) {

        // 1. 인증검사
        User sessionUser = (User) session.getAttribute(Define.SESSION_USER);

        User user = userPersistenceRepository.findById(sessionUser.getId());
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
        // 1. 인증검사
        User sessionUser = (User) session.getAttribute(Define.SESSION_USER);

        // 2. 권한검사
        // 다른 사람의 정보는 수정할 수 없음
        User user = userPersistenceRepository.findById(sessionUser.getId());
        if (user == null) {
            throw new Exception404("사용자를 찾을 수 없습니다.");
        }

        // 3. 유효성검사
        updateDto.validate();

        // 수정
        user = userPersistenceRepository.update(user, updateDto);

        // 4. 세션 동기화 : 수정된 정보를 세션에 반영
        user.setPassword(null);  // 보안
        session.setAttribute(Define.SESSION_USER, user);

        // 5. 성공 후 메인페이지로 리다이렉트
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
