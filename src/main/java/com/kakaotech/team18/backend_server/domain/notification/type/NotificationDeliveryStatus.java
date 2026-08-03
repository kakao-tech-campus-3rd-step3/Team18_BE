package com.kakaotech.team18.backend_server.domain.notification.type;

/**
 * 알림 발송 작업의 처리 상태입니다.
 * 일시적 오류는 별도 실패 상태에 머무르지 않고 다음 시도 시각과 함께 {@link #PENDING}으로 돌아갑니다.
 */
public enum NotificationDeliveryStatus {
    /** 발송 전이거나 호출 한도 및 재시도 시각을 기다리는 상태 */
    PENDING,

    /** 발송 작업이 선점되어 외부 발송 요청을 처리 중인 상태 */
    SENDING,

    /** SOLAPI가 문자 요청을 접수했지만 단말 수신 결과는 아직 확인되지 않은 상태 */
    ACCEPTED,

    /** 이메일 서버 수락 또는 SOLAPI 성공 웹훅으로 발송 성공이 확인된 종료 상태 */
    SENT,

    /** SOLAPI 접수 후 통신사 결과에서 발송 실패가 확인된 종료 상태 */
    FAILED,

    /** 타임아웃 등으로 외부 시스템의 접수 여부를 알 수 없어 자동 재발송하지 않는 상태 */
    UNKNOWN,

    /** 입력·인증 오류 또는 재시도 소진으로 더 이상 발송하지 않는 종료 상태 */
    PERMANENTLY_FAILED
}
