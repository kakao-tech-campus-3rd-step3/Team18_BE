package com.kakaotech.team18.backend_server.domain.statistics.dto;

/**
 * 마스킹·비율 계산 전의 집계 버킷.
 * <p>
 * 서비스 내부 전용이며 응답으로 나가지 않는다. 응답용 버킷은 {@link StatisticsResponseDto.Bucket}이다.
 *
 * @param key            표기 변경에 영향받지 않는 코드값
 * @param label          화면 표시용 문자열
 * @param count          지원자 수
 * @param distinctValues '기타' 버킷에 병합된 원래 값의 종류 수 (그 외에는 null)
 */
public record RawBucket(
        String key,
        String label,
        long count,
        Integer distinctValues
) {

    public static RawBucket of(String key, String label, long count) {
        return new RawBucket(key, label, count, null);
    }

    public RawBucket plus(long more) {
        return new RawBucket(key, label, count + more, distinctValues);
    }
}
