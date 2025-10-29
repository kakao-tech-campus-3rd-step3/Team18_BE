package com.kakaotech.team18.backend_server.global.exception.handler;

import com.kakaotech.team18.backend_server.domain.application.entity.Status;
import com.kakaotech.team18.backend_server.global.exception.code.ErrorCode;
import com.kakaotech.team18.backend_server.global.exception.dto.ErrorResponseDto;
import com.kakaotech.team18.backend_server.global.exception.exceptions.CustomException;
import com.kakaotech.team18.backend_server.global.exception.exceptions.StatusNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * 애플리케이션 전역에서 발생하는 예외를 중앙에서 처리하는 클래스입니다.
 *
 * @RestControllerAdvice 어노테이션을 통해 모든 @RestController 에서 발생하는 예외를 감지 및 처리합니다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * CustomException 예외를 처리
     * <p>
     * CustomException 발생 시, 해당 예외의 ErrorCode와 detail을 사용하여 ErrorResponseDto를 생성하고, 적절한 HttpStatus와
     * 함께 응답합니다.
     *
     * @return ErrorResponseDto와 해당 ErrorCode에 매핑된 HttpStatus를 포함하는 ResponseEntity
     */
    @ExceptionHandler(CustomException.class)
    protected ResponseEntity<ErrorResponseDto> handleCustomException(final CustomException e) {

        final ErrorCode errorCode = e.getErrorCode();
        final String detail = e.getDetail();
        final ErrorResponseDto response = (detail != null) ?
                ErrorResponseDto.of(errorCode, detail) :
                ErrorResponseDto.from(errorCode);

        log.warn("handleCustomException: {} (detail: {})",
                errorCode.getMessage(),
                detail);

        return new ResponseEntity<>(response, errorCode.getHttpStatus());
    }

    /**
     * MethodArgumentNotValidException 예외를 처리
     * <p>
     * MethodArgumentNotValidException: @Valid 어노테이션을 사용한 DTO 유효성 검사 실패 시 발생
     * <p>
     * BindingResult 에서 유효성 검증 실패 정보를 가공 및 취합하여 ErrorResponseDto 형태로 반환합니다.
     * <p>
     *
     * @return ErrorResponseDto와 해당 ErrorCode에 매핑된 HttpStatus를 포함하는 ResponseEntity
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponseDto> handleMethodArgumentNotValidException(
            final MethodArgumentNotValidException e) {

        // BindingResult 에서 유효성 검증 실패 정보를 가공
        final BindingResult bindingResult = e.getBindingResult();
        final StringBuilder sb = new StringBuilder();
        for (final FieldError fieldError : bindingResult.getFieldErrors()) {
            sb.append(fieldError.getField()).append(": ").append(fieldError.getDefaultMessage())
                    .append(", ");
        }
        // 마지막 ", " 제거
        final String detail = sb.substring(0, sb.length() - 2);

        final ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;
        final ErrorResponseDto response = ErrorResponseDto.of(errorCode, detail);

        log.warn("MethodArgumentNotValidException: {} (detail: {})",
                errorCode.getMessage(),
                detail);

        return new ResponseEntity<>(response, errorCode.getHttpStatus());
    }

    @ExceptionHandler({ MethodArgumentTypeMismatchException.class, ConversionFailedException.class })
    public ResponseEntity<ErrorResponseDto> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        // Status 파라미터 변환 실패면 커스텀 메시지
        if (e.getRequiredType() == Status.class || hasCause(e, StatusNotFoundException.class)) {
            final ErrorCode errorCode = ErrorCode.STATUS_NOT_FOUND;
            final String detail = "잘못된 상태값";
            final ErrorResponseDto response = ErrorResponseDto.of(errorCode, detail);
            log.warn("TypeMisMatch for Status: {} (detail : {})", errorCode.getMessage(), detail);
            return new ResponseEntity<>(response, errorCode.getHttpStatus());
        }
        // 그 외 파라미터 타입 오류
        final ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;
        final ErrorResponseDto response = ErrorResponseDto.from(errorCode);
        log.warn("TypeMisMatch: {}", errorCode.getMessage(), e);
        return new ResponseEntity<>(response, errorCode.getHttpStatus());
    }

    /**
     * MissingRequestCookieException 예외를 처리
     * <p>
     * @CookieValue 어노테이션으로 필수 쿠키가 지정되었으나, 요청에 해당 쿠키가 포함되지 않았을 때 발생합니다.
     * HTTP 400 Bad Request와 함께 적절한 에러 메시지를 반환합니다.
     *
     * @param e MissingRequestCookieException
     * @return 400 Bad Request 상태 코드와 표준 에러 응답
     */
    @ExceptionHandler(MissingRequestCookieException.class)
    protected ResponseEntity<ErrorResponseDto> handleMissingRequestCookieException(final MissingRequestCookieException e) {
        final ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE; // 클라이언트 요청 오류이므로 INVALID_INPUT_VALUE 사용
        final String detail = "필수 쿠키 '" + e.getCookieName() + "'가 요청에 포함되지 않았습니다.";
        final ErrorResponseDto response = ErrorResponseDto.of(errorCode, detail);

        log.warn("MissingRequestCookieException: {} (detail: {})",
                errorCode.getMessage(),
                detail);

        return new ResponseEntity<>(response, errorCode.getHttpStatus());
    }



    /**
     * 모든 예상치 못한 예외를 처리
     */

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponseDto> handleException(final Exception e) {

        final ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        final ErrorResponseDto response = ErrorResponseDto.from(errorCode);

        log.error("Unhandled Exception : {}", errorCode.getMessage(), e);

        return new ResponseEntity<>(response, errorCode.getHttpStatus());
    }

    private boolean hasCause(Throwable t, Class<? extends Throwable> target) {
        while (t != null) {
            if (target.isInstance(t)) {
                return true;
            }
            t = t.getCause();
        }
        return false;
    }

}