package com.kakaotech.team18.backend_server.domain.statistics.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.entity.ClubApplyForm;
import com.kakaotech.team18.backend_server.domain.statistics.dto.StatisticsResponseDto;
import com.kakaotech.team18.backend_server.domain.statistics.entity.ClubApplyFormStatisticsSnapshot;
import com.kakaotech.team18.backend_server.domain.statistics.entity.StatisticsDimension;
import com.kakaotech.team18.backend_server.domain.statistics.entity.StatisticsRulesVersion;
import com.kakaotech.team18.backend_server.domain.statistics.repository.ClubApplyFormStatisticsSnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 지원서가 삭제되기 전에 통계를 확정 저장합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsSnapshotService {

    private final ClubApplyFormStatisticsSnapshotRepository snapshotRepository;
    private final StatisticsServiceImpl statisticsService;
    private final ObjectMapper objectMapper;

    /**
     * 스냅샷이 아직 없으면 지금의 집계 결과를 확정 저장합니다.
     * <p>
     * <strong>불합격 지원서를 삭제하기 전에 반드시 호출해야 한다.</strong> 호출자와 같은 트랜잭션에서 동작하므로
     * 여기서 예외가 나면 삭제를 포함한 전체 작업이 롤백된다. 저장에 실패했는데 삭제만 진행되어 원본과 집계를
     * 모두 잃는 상황을 막기 위해 <strong>예외를 삼키지 않는다.</strong>
     * <p>
     * 이미 스냅샷이 있으면 덮어쓰지 않는다. 첫 단계 전환 시점의 전체 지원자 분포가 확정본이고, 그 뒤로는
     * 불합격자가 빠져나간 뒤의 왜곡된 분포만 계산할 수 있기 때문이다.
     *
     * @param form 대상 지원폼
     * @return 이번 호출에서 새로 저장했으면 true
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.MANDATORY)
    public boolean saveIfAbsent(ClubApplyForm form) {
        if (snapshotRepository.existsByClubApplyFormId(form.getId())) {
            log.debug("통계 스냅샷이 이미 존재해 재저장하지 않습니다. clubApplyFormId={}", form.getId());
            return false;
        }

        StatisticsResponseDto payload = statisticsService.calculate(form, StatisticsDimension.defaults());
        String json = serialize(payload);

        snapshotRepository.save(ClubApplyFormStatisticsSnapshot.builder()
                .clubApplyForm(form)
                .totalApplicants(payload.totalApplicants())
                .rulesVersion(StatisticsRulesVersion.CURRENT)
                .payload(json)
                .build());

        log.info("통계 스냅샷 저장. clubApplyFormId={}, totalApplicants={}, rulesVersion={}",
                form.getId(), payload.totalApplicants(), StatisticsRulesVersion.CURRENT);
        return true;
    }

    private String serialize(StatisticsResponseDto payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            // 직렬화 실패는 그대로 전파해 호출자의 트랜잭션을 롤백시킨다.
            // 여기서 삼키면 스냅샷 없이 불합격 지원서만 삭제된다.
            throw new IllegalStateException("통계 스냅샷 직렬화 실패", e);
        }
    }

}
