package com.tenco.spring_blog._core.interceptor;

import com.tenco.spring_blog._core.error.Exception403;
import com.tenco.spring_blog._core.util.Define;
import com.tenco.spring_blog.user.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

// 도전과제2 : 블랙리스트를 구현해주세요.
@Slf4j
@Component
public class BlackListInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        HttpSession session = request.getSession(false);
        User sessionUser = (session != null) ? (User) session.getAttribute(Define.SESSION_USER) : null;

        log.debug("블랙리스트 확인:");
        if (sessionUser != null && sessionUser.getBanned()) {
            // 세션 정보 제거
            session.invalidate();
            throw new Exception403("해당 계정은 차단되었습니다.");
        }

        return true;
    }
}
