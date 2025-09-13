package com.kakaotech.team18.backend_server.global.exception.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kakaotech.team18.backend_server.global.exception.code.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponseDto {

    @JsonProperty("error_code")
    private final String errorCode;
    private final String message;
    private final String detail;

    /**
     * ErrorCode와 상세 정보(detail)을 기반으로 ErrorResponseDto를 생성합니다.
     * <p>
     * 에러가 특정 값 때문에 발생하여, "어떤 값 때문에?" 라는 개발자의 질문에 답해야 할 때 사용합니다. (예: 요청된 userId: 123)
     */
    public static ErrorResponseDto of(final ErrorCode code, final String detail) {
        return new ErrorResponseDto(code.name(), code.getMessage(), detail);
    }

    /**
     * ErrorCode를 기반으로 ErrorResponseDto를 생성합니다. (상세 정보 detail 없음)
     * <p>
     * 에러 메시지만으로 의미가 충분하거나, 내부 로직 같은 민감정보를 외부에 노출하면 안 될 때 (예: 접근 권한이 없습니다.)
     */
    public static ErrorResponseDto from(final ErrorCode code) {
        return new ErrorResponseDto(code.name(), code.getMessage(), null);
    }
}
