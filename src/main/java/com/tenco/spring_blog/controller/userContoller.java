package com.tenco.spring_blog.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@Controller
public class userContoller {

    // GET http://localhost:8080/join
    @GetMapping("/join")
    public String joinForm() {

        return "user/join-form";        // templates/user/join-form.mustache
    }

    // GET http://localhost:8080/login
    @GetMapping("/login")
    public String loginForm() {

        return "user/login-form";
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
    public String logout() {

        return "redirect:/";
    }



}
