package com.tenco.spring_blog._core.config;

import com.tenco.spring_blog._core.interceptor.AdminInterceptor;
import com.tenco.spring_blog._core.interceptor.IpBlockInterceptor;
import com.tenco.spring_blog._core.interceptor.LoginInterceptor;
import com.tenco.spring_blog._core.interceptor.SessionUserModelInterceptor;
import com.tenco.spring_blog._core.util.FileUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@RequiredArgsConstructor
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final SessionUserModelInterceptor sessionUserModelInterceptor;
    private final IpBlockInterceptor ipBlockInterceptor;
    private final LoginInterceptor loginInterceptor;
    private final AdminInterceptor adminInterceptor;

    // 업로드 폴더의 파일 /images/** 주소로 제공


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 업로드 폴더 경로를 file://.... 형식의 주로 변환  (윈도우 기준 : C:\Users\{사용자명}\blog_uploads\
        String location = Paths.get(FileUtil.UPLOAD_DIR).toUri().toString();

        // 폴더 위치는 반드시 /로 끝나야 함
        if (! location.endsWith("/")) {
            location += "/";
        }
        registry.addResourceHandler("/images/**")
                .addResourceLocations(location);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        // SessionUserModelInterceptor을 등록: 모든
        registry.addInterceptor(sessionUserModelInterceptor)
                        .addPathPatterns("/**");

        // IpBlockInterceptor를 시스템에 등록
        registry.addInterceptor(ipBlockInterceptor);

        // LoginInterceptor를 시스템에 등록
        registry.addInterceptor(loginInterceptor)
                // 인터셉터가 동작할 URL 패턴 지정
                .addPathPatterns("/user/**", "/board/**", "/reply/**", "/admin/**", "/my/**")
                // 인터셉터가 제외할 URL 패턴 지정
                .excludePathPatterns("/board/list", "/board/{id:\\d+}");

        // AdminInterceptor를 시스템에 등록
        registry.addInterceptor(adminInterceptor)
                // 인터셉터가 동작할 URL 패턴 지정
                .addPathPatterns("/admin/**");
    }

    // 비밀번호 해싱 도구를 빈으로 등록 (필요한 곳에서 DI할 수 있다)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
