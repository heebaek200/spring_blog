package com.tenco.spring_blog._core.interceptor;

import com.tenco.spring_blog._core.error.Exception403;
import com.tenco.spring_blog._core.util.Define;
import com.tenco.spring_blog.user.SessionUserDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 인가를 담당하는 인터셉터
 */
@Component
public class AdminInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        SessionUserDto sessionUser = (session == null) ? null : (SessionUserDto) session.getAttribute(Define.SESSION_USER);

        if (sessionUser == null || !sessionUser.isAdmin()) {
            throw new Exception403("관리자만 접근할 수 있는 페이지입니다.");
        }

        return true;
    }
}
