package com.tenco.spring_blog._core.interceptor;

import com.tenco.spring_blog._core.error.Exception403;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

// 도전과제1 : 특정 IP를 차단하는 인터셉터를 구현해주세요. 여러개 가능
@Slf4j
@Component
public class IpBlockInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 차단된 IP 리스트
        List<String> bannedId = List.of(
                "192.168.5.20",
                "192.168.5.16"
        );

        String requestIpAddress = request.getRemoteAddr();
        log.debug("요청 IP : {}", requestIpAddress);

        if (bannedId.contains(requestIpAddress)) {
            log.debug("요청 IP는 차단 리스트에 포함됩니다.");
            throw new Exception403("해당 IP는 차단되었습니다.");
        } else {
            log.debug("요청 IP는 차단 리스트에 포함되지 않습니다.");
        }

        return true;
    }
}
