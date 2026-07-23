package com.kakaotech.team18.backend_server.domain.statistics.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kakaotech.team18.backend_server.domain.statistics.dto.StatisticsResponseDto;
import com.kakaotech.team18.backend_server.domain.statistics.entity.ClubApplyFormStatisticsSnapshot;
import com.kakaotech.team18.backend_server.domain.statistics.repository.ClubApplyFormStatisticsSnapshotRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 확정된 통계 스냅샷을 읽습니다.
 * <p>
 * 저장({@link StatisticsSnapshotService})과 분리한 이유는 순환 참조를 끊기 위해서다. 저장하려면 집계를
 * 해야 하므로 {@code StatisticsServiceImpl}이 필요한데, 조회 경로의 {@code StatisticsServiceImpl}은
 * 스냅샷을 먼저 확인해야 한다. 읽기만 하는 이 컴포넌트는 집계에 의존하지 않으므로 양쪽이 함께 참조해도
 * 문제가 없다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StatisticsSnapshotReader {

    private final ClubApplyFormStatisticsSnapshotRepository snapshotRepository;
    private final ObjectMapper objectMapper;

    public boolean exists(Long clubApplyFormId) {
        return snapshotRepository.existsByClubApplyFormId(clubApplyFormId);
    }

    /**
     * 확정된 스냅샷을 조회합니다.
     *
     * @return 스냅샷이 없거나 역직렬화할 수 없으면 비어 있음
     */
    @Transactional(readOnly = true)
    public Optional<StatisticsResponseDto> find(Long clubApplyFormId) {
        return snapshotRepository.findByClubApplyFormId(clubApplyFormId)
                .flatMap(this::deserialize);
    }

    private Optional<StatisticsResponseDto> deserialize(ClubApplyFormStatisticsSnapshot snapshot) {
        try {
            StatisticsResponseDto dto =
                    objectMapper.readValue(snapshot.getPayload(), StatisticsResponseDto.class);
            // 저장 당시에는 진행 중 집계본으로 만들어졌더라도, 조회 시점에는 확정본임을 명시한다.
            return Optional.of(new StatisticsResponseDto(
                    dto.clubApplyFormId(),
                    dto.totalApplicants(),
                    true,
                    dto.calculatedAt(),
                    dto.results(),
                    dto.notice()));
        } catch (Exception e) {
            // 스냅샷을 못 읽는다고 조회가 실패하면 안 된다. 다만 이 경우 남은 지원서로 다시 집계하면
            // 불합격자가 빠진 왜곡된 값이 나가므로, 반드시 확인이 필요한 상황이라 error로 남긴다.
            log.error("통계 스냅샷 역직렬화 실패. clubApplyFormId={}",
                    snapshot.getClubApplyForm().getId(), e);
            return Optional.empty();
        }
    }
}
