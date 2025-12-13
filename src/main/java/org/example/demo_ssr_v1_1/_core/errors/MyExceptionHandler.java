package org.example.demo_ssr_v1_1._core.errors;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.example.demo_ssr_v1_1._core.errors.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

// @ControllerAdvice - 모든 컨트롤러에서 발생하는 예외를 이 클래스에서 중앙 집중화 시킴
// @RestControllerAdvice = @ControllerAdvice + @ResponseBody
@ControllerAdvice
@Slf4j
public class MyExceptionHandler {

    // 내가 지켜볼 예외를 명시를 해주면 ControllerAdvice 가 가지고와 처리 함
    @ExceptionHandler(Exception400.class)
    public String ex400(Exception400 e, HttpServletRequest request) {
        log.warn("=== 400 에러 발생  ===");
        log.warn("요청 URL : {}", request.getRequestURL());
        log.warn("에러 메세지 : {}", e.getMessage());
        log.warn("예외 클래스 : {}", e.getClass().getSimpleName());
        request.setAttribute("msg", e.getMessage());
        return "err/400";
    }

    // 401 인증 오류
    @ExceptionHandler(Exception401.class)
    public String ex401(Exception401 e, HttpServletRequest request) {
        log.warn("=== 401 에러 발생  ===");
        log.warn("요청 URL : {}", request.getRequestURL());
        log.warn("에러 메세지 : {}", e.getMessage());
        log.warn("예외 클래스 : {}", e.getClass().getSimpleName());
        request.setAttribute("msg", e.getMessage());
        return "err/401";
    }

    // 403 인가 오류
//    @ExceptionHandler(Exception403.class)
//    public String ex401(Exception403 e, HttpServletRequest request) {
//        log.warn("=== 403 에러 발생  ===");
//        log.warn("요청 URL : {}", request.getRequestURL());
//        log.warn("에러 메세지 : {}", e.getMessage());
//        log.warn("예외 클래스 : {}", e.getClass().getSimpleName());
//        request.setAttribute("msg", e.getMessage());
//        return "err/403";
//    }

    @ExceptionHandler(Exception403.class)
    @ResponseBody
    public ResponseEntity<String> ex403(Exception403 e, HttpServletRequest request) {
        String script = "<script>alert('"+e.getMessage()+"');" +
                "history.back();" +
                "</script>";

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .contentType(MediaType.TEXT_HTML)
                .body(script);
    }


    // 404 인가 오류
    @ExceptionHandler(Exception404.class)
    public String ex404(Exception404 e, HttpServletRequest request) {
        log.warn("=== 404 에러 발생  ===");
        log.warn("요청 URL : {}", request.getRequestURL());
        log.warn("에러 메세지 : {}", e.getMessage());
        log.warn("예외 클래스 : {}", e.getClass().getSimpleName());
        request.setAttribute("msg", e.getMessage());
        return "err/404";
    }

    // 500 서버 내부 오류
    @ExceptionHandler(Exception500.class)
    public String ex500(Exception500 e, HttpServletRequest request) {
        log.warn("=== 500 에러 발생  ===");
        log.warn("요청 URL : {}", request.getRequestURL());
        log.warn("에러 메세지 : {}", e.getMessage());
        log.warn("예외 클래스 : {}", e.getClass().getSimpleName());
        request.setAttribute("msg", e.getMessage());
        return "err/500";
    }

    // 기타 모든 실행시점 오류 처리
    @ExceptionHandler(RuntimeException.class)
    public String handleRuntimeException(RuntimeException e,
                                         HttpServletRequest request) {
        log.warn("=== 예상하지 못한 에러 발생  ===");
        log.warn("요청 URL : {}", request.getRequestURL());
        log.warn("에러 메세지 : {}", e.getMessage());
        log.warn("예외 클래스 : {}", e.getClass().getSimpleName());
        request.setAttribute("msg", e.getMessage());
        return "err/500";
    }



}
