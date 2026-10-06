package com.tenco.spring_blog.controller;

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
        log.info("=== 회원가입 요청 ===");
        log.info("사용자명 : {}", joinDto.getUsername());
        log.info("비밀번호 : {}", joinDto.getPassword());
        log.info("이메일 : {}", joinDto.getEmail());

        try {
            // 1 유효성 검사
            joinDto.validate();

            // 2. 사용자명 중복 체크
            User existingUser = userPersistenceRepository.findByUsername(joinDto.getUsername());
            if (existingUser != null) {
                throw new IllegalArgumentException("이미 존재하는 사용자명입니다.");
            }

            // 3. DTO 객체를 Entity로 변환
            User user = joinDto.toEntity();

            // 4. DB에 회원정보 저장
            User userEntity = userPersistenceRepository.save(user);

            // 회원 가입 성공 시 로그인 화면으로 이동
            return "redirect:/login";
        } catch (Exception e) {
            log.error("회원가입 실패 : {}", e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
            return "user/join-form";
        }
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
        log.info("=== 로그인 요청 ===");
        log.info("사용자명 : {}", loginDto.getUsername());

        try {
            // 1. 입력 데이터 검증
            loginDto.validate();

            // 2. 사용자명과 비밀번호로 사용자 조회
            User sessionUser = userPersistenceRepository.findByUsernameAndPassword(
                    loginDto.getUsername(),
                    loginDto.getPassword()
            );

            // 3. 로그인 실패 처리
            if (sessionUser == null) {
                // 로그인 실패 : 일치하는 사용자 없음
                throw new IllegalArgumentException("사용자명 또는 비밀번호가 올바르지 않습니다.");
            }

            // 머스태치가 세션값을 읽도록 설정


            // 4. 로그인 성공 : 세션에 사용자 정보를 저장
            session.setAttribute("sessionUser", sessionUser);

            log.info("로그인한 사용자 : {}", sessionUser.getUsername());

            // 5. 메인 페이지로 리다이렉트 처리
            return "redirect:/";

        } catch (Exception e) {
            // 로그인 실패 시 에러 메시지와 함께 로그인 폼으로 돌려보내기
            model.addAttribute("errorMessage", e.getMessage());

            return "user/login-form";
        }
    }

    // GET http://localhost:8080/user/update
    @GetMapping("/user/update")
    public String updateForm(Model model) {

        model.addAttribute("user", Map.of(
                "username", "김민수",
                "email"   , "abc@naver.com"
        ));

        return "user/update-form";
    }

    // GET http://localhost:8080/logout
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        log.info("== 로그아웃 요청 ==");

        // 세션 무효화 처리
        session.invalidate();
        log.info("로그아웃 완료");

        return "redirect:/";
    }



}
