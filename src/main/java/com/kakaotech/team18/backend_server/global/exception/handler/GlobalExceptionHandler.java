package com.kakaotech.team18.backend_server.global.exception.handler;

import com.kakaotech.team18.backend_server.global.exception.code.ErrorCode;
import com.kakaotech.team18.backend_server.global.exception.dto.ErrorResponseDto;
import com.kakaotech.team18.backend_server.global.exception.exceptions.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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

}