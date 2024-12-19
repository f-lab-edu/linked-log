package flab.Linkedlog.controller.response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.naming.AuthenticationException;
import java.nio.file.AccessDeniedException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ApiResponse<ErrorResponse> handleException(Exception e) {
        logger.error("예외 발생: {}", e.getMessage(), e);

        return ApiResponse.error("INTERNAL_SERVER_ERROR", "서버에서 오류가 발생했습니다.");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ApiResponse<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        logger.error("잘못된 요청: {}", e.getMessage(), e);

        return ApiResponse.error("INVALID_ARGUMENT", e.getMessage());
    }


    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<ErrorResponse> handleIllegalStateException(IllegalStateException e) {
        logger.error("잘못된 상태 요청: {}", e.getMessage(), e);

        return ApiResponse.error("INVALID_STATE", e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ApiResponse<ErrorResponse> handleRuntimeException(RuntimeException e) {
        logger.error("런타임 예외 발생: {}", e.getMessage(), e);

        return ApiResponse.error("RUNTIME_EXCEPTION", "런타임 오류가 발생했습니다.");
    }

    // 인증 실패 처리
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResponse<ErrorResponse> handleAuthenticationException(AuthenticationException e) {
        logger.error("인증 실패: {}", e.getMessage(), e);

        return ApiResponse.error("UNAUTHORIZED", "인증되지 않은 사용자입니다.");
    }

    // 권한 부족 처리
    @ExceptionHandler(AccessDeniedException.class)
    public ApiResponse<ErrorResponse> handleAccessDeniedException(AccessDeniedException e) {
        logger.error("권한 부족: {}", e.getMessage(), e);

        return ApiResponse.error("FORBIDDEN", "권한이 없습니다.");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ApiResponse<ErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        logger.error("파라미터 변환 실패: {}", e.getMessage(), e);

        return ApiResponse.error("INVALID_STATUS", "유효하지 않은 상태 값입니다.");
    }


    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED) // 401 상태 코드 반환
    public ApiResponse<ErrorResponse> handleBadCredentialsException(BadCredentialsException e) {
        logger.error("인증 실패: {}", e.getMessage(), e);

        return ApiResponse.error("UNAUTHORIZED", e.getMessage());
    }
}
