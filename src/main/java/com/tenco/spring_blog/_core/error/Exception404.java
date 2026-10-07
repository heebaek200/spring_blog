package com.tenco.spring_blog._core.error;

// 404 Not Found 상황에서 사용할 사용자 정의 예외 클래스
// 언체크드 예외 (RuntimeException 상속)
public class Exception404 extends RuntimeException {

    // 예외 메시지를 받을 수 있도록 String 패러미터 설계
    public Exception404(String msg) {
        super(msg);
    }
}
