package com.tenco.spring_blog._core.error;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

// 모든 컨트롤러에서 발생하는 예외를 이 클래스에서 처리함
@Slf4j
@ControllerAdvice   // IoC -> Bean객체가 됨
public class GlobalExceptionHandler {

    // 특정 예외 타입이 발생했을 때 실행될 메서드로 지정
    @ExceptionHandler(Exception400.class)
    public String ex400(
            Exception400 e,
            HttpServletRequest request,
            Model model
    ) {
        log.warn("==== 400 Bad Request 발생 ====");
        log.warn("요청 URL : {}", request.getRequestURL());
        log.warn("에러 메시지 : {}", e.getMessage());
        log.warn("예외 클래스 : {}", e.getClass().getSimpleName());

        model.addAttribute("msg", e.getMessage());

        return "err/400";
    }

    @ExceptionHandler(Exception401.class)
    public String ex401(
            Exception401 e,
            HttpServletRequest request,
            Model model
    ) {
        log.warn("==== 401 Unauthorized 발생 ====");
        log.warn("요청 URL : {}", request.getRequestURL());
        log.warn("인증 오류 : {}", e.getMessage());
        log.warn("예외 클래스 : {}", e.getClass().getSimpleName());

        model.addAttribute("msg", e.getMessage());

        return "err/401";
    }

    @ExceptionHandler(Exception403.class)
//    public String ex403(
//            Exception403 e,
//            HttpServletRequest request,
//            Model model
//    ) {
//        log.warn("==== 403 Forbidden 발생 ====");
//        log.warn("요청 URL : {}", request.getRequestURL());
//        log.warn("권한 오류 : {}", e.getMessage());
//        log.warn("예외 클래스 : {}", e.getClass().getSimpleName());
//
//        model.addAttribute("msg", e.getMessage());
//
//        return "err/403";
//    }
    @ResponseBody   // alert이 뜨도록 수정
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String ex403(
            Exception403 e,
            HttpServletRequest request,
            Model model
    ) {
        log.warn("==== 403 Forbidden 발생 ====");
        log.warn("요청 URL : {}", request.getRequestURL());
        log.warn("권한 오류 : {}", e.getMessage());
        log.warn("예외 클래스 : {}", e.getClass().getSimpleName());

        String msg = e.getMessage().replace("'", "\\'");
        return """
                <script>
                    alert('%s');
                    history.back();
                </script>
                """.formatted(msg);
    }

    @ExceptionHandler(Exception404.class)
    public String ex404(
            Exception404 e,
            HttpServletRequest request,
            Model model
    ) {
        log.info("==== 404 Not Found 발생 ====");
        log.info("요청 URL : {}", request.getRequestURL());
        log.info("패스 오류 : {}", e.getMessage());
        log.info("예외 클래스 : {}", e.getClass().getSimpleName());

        model.addAttribute("msg", e.getMessage());

        return "err/404";
    }

    @ExceptionHandler(Exception500.class)
    public String ex500(
            Exception500 e,
            HttpServletRequest request,
            Model model
    ) {
        log.error("==== 500 Internal Server Error 발생 ====");
        log.error("요청 URL : {}", request.getRequestURL());
        log.error("서버 오류 : {}", e.getMessage());
        log.error("스택 트레이스 : {}", e);

        model.addAttribute("msg", "네트워크 일시적인 장애");

        return "err/500";
    }

    // 기타 모든 RuntimeException 처리
    @ExceptionHandler(RuntimeException.class)
    public String ex500(
            RuntimeException e,
            HttpServletRequest request,
            Model model
    ) {
        log.error("==== 예상치 못한 런타임 에러 발생 ====");
        log.error("요청 URL : {}", request.getRequestURL());
        log.error("에러 타입 : {}", e.getClass().getSimpleName());
        log.error("에러 메시지 : {}", e.getMessage());
        log.error("스택 트레이스 : {}", e);

        model.addAttribute("msg", "시스템 오류 발생. 관리자에게 문의해주세요.");

        return "err/500";
    }

}
