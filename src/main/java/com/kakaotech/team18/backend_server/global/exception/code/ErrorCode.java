package com.kakaotech.team18.backend_server.global.exception.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 애플리케이션 전역에서 사용될 비즈니스 예외 코드를 정의하는 Enum 클래스입니다.
 * <p>
 * <strong>ErrorCode 원칙</strong>
 * <ul>
 *     <li>'예측 가능한 비즈니스 예외'를 명시적으로 다루기 위해 사용됩니다.</li>
 * </ul>
 *
 * <strong>활용 워크플로우 (정의 → 발생 → 처리)</strong>
 * <ol>
 *     <li><b>정의</b>: 이 Enum에 새로운 에러(메시지, HttpStatus)를 정의합니다.</li>
 *     <li><b>발생</b>: 서비스 계층에서 비즈니스 규칙 위반 시 <code>throw new CustomException(ErrorCode.YOUR_ERROR, "상세 정보");</code>를 호출합니다.</li>
 *     <li><b>처리</b>: <code>GlobalExceptionHandler</code>가 예외를 감지하여, 정의된 HttpStatus와 메시지로 일관된 에러 응답(JSON)을 생성합니다.</li>
 * </ol>
 */

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 400 BAD_REQUEST: 잘못된 요청
    INVALID_INPUT_VALUE("입력 값이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    INVALID_RATING_UNIT("별점은 0.5 단위로만 입력 가능합니다.", HttpStatus.BAD_REQUEST),
    DUPLICATE_KAKAO_ID("이미 다른 계정과 연동된 학번입니다.", HttpStatus.BAD_REQUEST),
    INVALID_INPUT_ANSWER("잘못된 답안 입력입니다", HttpStatus.BAD_REQUEST),
    PENDING_APPLICATION_EXIST("미처리 지원서가 존재합니다. 모든 지원서를 승인/거절로 확정한 뒤 발송하세요.", HttpStatus.BAD_REQUEST),
    EMAIL_INVALID_MESSAGE("이메일 메시지 구성 오류", HttpStatus.BAD_REQUEST),
    ILLEGAL_ARGUMENT_JWT("토큰의 인자가 잘못되었습니다.", HttpStatus.BAD_REQUEST),
    INVALID_FILE("잘못된 파일 형식입니다.", HttpStatus.BAD_REQUEST),
    TOO_LARGE_FILE("업로드 하려는 파일 크기가 너무 큽니다", HttpStatus.BAD_REQUEST),

    // 401 UNAUTHORIZED: 인증되지 않은 사용자
    UNAUTHENTICATED_USER("인증되지 않은 사용자입니다.", HttpStatus.UNAUTHORIZED),
    EXPIRED_ACCESS_TOKEN("만료된 Access Token입니다.", HttpStatus.UNAUTHORIZED),
    EXPIRED_REFRESH_TOKEN("만료된 Refresh Token입니다. 다시 로그인해주세요.", HttpStatus.UNAUTHORIZED),
    MALFORMED_JWT("잘못된 형식의 토큰입니다.", HttpStatus.UNAUTHORIZED),
    INVALID_JWT_SIGNATURE("토큰의 서명이 유효하지 않습니다.", HttpStatus.UNAUTHORIZED),
    UNSUPPORTED_JWT("지원하지 않는 형식의 토큰입니다.", HttpStatus.UNAUTHORIZED),
    NOT_REFRESH_TOKEN("Refresh Token이 아닙니다.", HttpStatus.UNAUTHORIZED),
    LOGGED_OUT_USER("로그아웃된 사용자입니다.", HttpStatus.UNAUTHORIZED),
    INVALID_REFRESH_TOKEN("유효하지 않은 Refresh Token입니다.", HttpStatus.UNAUTHORIZED),
    EMAIL_AUTH_FAILED("SMTP 인증 실패", HttpStatus.UNAUTHORIZED),

    // 403 FORBIDDEN: 권한 없음
    FORBIDDEN("해당 요청에 대한 권한이 없습니다.", HttpStatus.FORBIDDEN),
    COMMENT_ACCESS_DENIED("해당 댓글에 대한 접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
    EMAIL_POLICY_REJECTED   ("Gmail 정책/스팸/DMARC 거부", HttpStatus.FORBIDDEN),
    UNREGISTERED_USER("동아리에 가입되지 않은 유저입니다.", HttpStatus.FORBIDDEN),

    // 404 NOT_FOUND: 리소스를 찾을 수 없음
    USER_NOT_FOUND("해당 유저가 존재하지 않습니다.", HttpStatus.NOT_FOUND),
    CLUB_NOT_FOUND("해당 동아리가 존재하지 않습니다.", HttpStatus.NOT_FOUND),
    CLUB_MEMBER_NOT_FOUND("해당 클럽멤버가 존재하지 않습니다.", HttpStatus.NOT_FOUND),
    FORM_NOT_FOUND("지원폼이 존재하지 않습니다", HttpStatus.NOT_FOUND),
    APPLICATION_NOT_FOUND("해당 지원서를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    COMMENT_NOT_FOUND("해당 댓글을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    PRESIDENT_NOT_FOUND("해당 동아리의 회장이 없습니다.", HttpStatus.NOT_FOUND),
    STATUS_NOT_FOUND("해당 상태를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    NOTICE_NOT_FOUND("해당 공지사항을 찾을 수 없습니다", HttpStatus.NOT_FOUND),

    // 409 CONFLICT: 리소스 충돌
    USER_ALREADY_EXISTS("이미 존재하는 유저입니다.", HttpStatus.CONFLICT),
    TEMPORARY_SERVER_CONFLICT("일시적인 요청 충돌이 발생했습니다. 잠시 후 다시 시도해주세요.", HttpStatus.CONFLICT),

    // 422 UNPROCESSABLE_ENTITY
    EMAIL_RECIPIENT_INVALID ("수신자 주소가 존재하지 않음", HttpStatus.UNPROCESSABLE_ENTITY),

    // 500 INTERNAL_SERVER_ERROR: 서버 내부 에러
    INTERNAL_SERVER_ERROR("서버 내부에 문제가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    EMAIL_SEND_FAILED("이메일 전송 실패", HttpStatus.INTERNAL_SERVER_ERROR),
    IO_EXCEPTION("파일 입출력 실패.", HttpStatus.INTERNAL_SERVER_ERROR),
    AWS_EXCEPTION("AWS 에러 발생", HttpStatus.INTERNAL_SERVER_ERROR),

    // 503 SERVICE_UNAVAILABLE: 일시적 오류-나중에 다시 시도
    EMAIL_TEMPORARY_FAILURE ("Gmail 임시 오류/리밋/용량", HttpStatus.SERVICE_UNAVAILABLE),

    // 504 GATEWAY_TIMEOUT
    EMAIL_TIMEOUT("SMTP 타임아웃", HttpStatus.GATEWAY_TIMEOUT),
    KAKAO_API_TIMEOUT("카카오 API 응답이 지연되고 있습니다. 잠시 후 다시 시도해주세요.", HttpStatus.GATEWAY_TIMEOUT);

    private final String message;
    private final HttpStatus httpStatus;
}
