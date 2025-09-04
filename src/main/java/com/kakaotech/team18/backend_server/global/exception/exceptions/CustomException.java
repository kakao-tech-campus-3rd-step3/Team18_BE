package com.kakaotech.team18.backend_server.global.exception.exceptions;

import com.kakaotech.team18.backend_server.global.exception.code.ErrorCode;
import lombok.Getter;

/**
 * [확장 가이드라인]
 * <p>
 * 보다 명확한 예외 표현을 위해 CustomException을 직접 사용하기보다,
 * <p>
 * 이를 상속받는 구체적인 예외 클래스를 만드는 것을 권장합니다.
 * <p>
 * 예시: UserNotFoundException과 같은 사용자 관련 예외는
 * <p>
 * com.kakaotech.team18.backend_server.user.exception 패키지를 생성하고,
 * <p>
 * 해당 패키지 내에서 CustomException을 상속받아 작성합니다.
 * <p>
 * 이렇게 하면 예외의 책임과 발생 위치를 명확히 할 수 있습니다.
 */

@Getter
public class CustomException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String detail; // 디버깅 및 로깅을 위한 상세 정보

    // detail 정보가 필요한 경우
    public CustomException(ErrorCode errorCode, String detail) {
        // 부모 생성자에 에러 메시지를 전달하여 스택 트레이스에 포함되도록 함
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.detail = detail;
    }

    // detail 정보가 필요 없는 경우
    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.detail = null; // detail은 null로 설정
    }
}
