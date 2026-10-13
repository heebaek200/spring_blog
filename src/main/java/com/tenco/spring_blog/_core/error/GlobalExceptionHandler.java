package com.tenco.spring_blog._core.error;

import com.tenco.spring_blog._core.util.Define;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// 모든 컨트롤러에서 발생하는 예외를 이 클래스에서 처리함
@Slf4j
@ControllerAdvice   // IoC -> Bean객체가 됨
public class GlobalExceptionHandler {

    // 특정 예외 타입이 발생했을 때 실행될 메서드로 지정
    @ExceptionHandler(Exception400.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String ex400(
            Exception400 e,
            HttpServletRequest request,
            Model model
    ) {
        log.warn("==== 400 Bad Request 발생 ====");
        log.warn("요청 URL : {}", request.getRequestURL());
        log.warn("에러 메시지 : {}", e.getMessage());
        log.warn("예외 클래스 : {}", e.getClass().getSimpleName());

        // 헤더의 sessionUserDto
        addSessionUserDto(request, model);

        model.addAttribute("msg", e.getMessage());

        return "err/400";
    }

    @ExceptionHandler(Exception401.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public String ex401(
            Exception401 e,
            HttpServletRequest request,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        log.warn("==== 401 Unauthorized 발생 ====");
        log.warn("요청 URL : {}", request.getRequestURL());
        log.warn("인증 오류 : {}", e.getMessage());
        log.warn("예외 클래스 : {}", e.getClass().getSimpleName());

        model.addAttribute("msg", e.getMessage());

        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        return "redirect:/login";
    }

    @ExceptionHandler(Exception403.class)
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
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String ex404(
            Exception404 e,
            HttpServletRequest request,
            Model model
    ) {
        log.info("==== 404 Not Found 발생 ====");
        log.info("요청 URL : {}", request.getRequestURL());
        log.info("패스 오류 : {}", e.getMessage());
        log.info("예외 클래스 : {}", e.getClass().getSimpleName());

        // 헤더의 sessionUserDto
        addSessionUserDto(request, model);

        model.addAttribute("msg", e.getMessage());

        // 헤더의 sessionUserDto
        addSessionUserDto(request, model);

        return "err/404";
    }

    @ExceptionHandler(Exception500.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String ex500(
            Exception500 e,
            HttpServletRequest request,
            Model model
    ) {
        log.error("==== 500 Internal Server Error 발생 ====");
        log.error("요청 URL : {}", request.getRequestURL());
        log.error("서버 오류 : {}", e.getMessage());
        log.error("스택 트레이스 : {}", e);

        // 헤더의 sessionUserDto
        addSessionUserDto(request, model);

        model.addAttribute("msg", "네트워크 일시적인 장애");

        return "err/500";
    }

    // 기타 모든 RuntimeException 처리
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
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

    // 오류 화면 헤더의 로그인 메뉴가 보이도록 sessionUserDto를 Model에 추가
    private void addSessionUserDto(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute(Define.SESSION_USER) != null) {
            model.addAttribute(Define.SESSION_USER, session.getAttribute(Define.SESSION_USER));
        }
    }

}
