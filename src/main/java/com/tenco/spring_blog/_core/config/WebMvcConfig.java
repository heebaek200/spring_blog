package com.tenco.spring_blog._core.config;

import com.tenco.spring_blog._core.interceptor.LoginInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@RequiredArgsConstructor
@Configurable
public class WebMvcConfig implements WebMvcConfigurer {

    private final LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // LoginInterceptor를 시스템에 등록

        registry.addInterceptor(loginInterceptor)
                // 인터셉터가 동작할 URL 패턴 지정
                .addPathPatterns("/user/**", "/board/**")
                // 인터셉터가 제외할 URL 패턴 지정
                .excludePathPatterns("/board/list", "/board/{id:\\d+}");
    }
}
