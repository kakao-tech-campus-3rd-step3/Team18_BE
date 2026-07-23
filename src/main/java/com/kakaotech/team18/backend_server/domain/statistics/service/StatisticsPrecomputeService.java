package com.kakaotech.team18.backend_server.domain.statistics.service;

import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import com.kakaotech.team18.backend_server.domain.club.util.RecruitStatus;
import com.kakaotech.team18.backend_server.domain.club.util.RecruitStatusCalculator;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.entity.ClubApplyForm;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.repository.ClubApplyFormRepository;
import com.kakaotech.team18.backend_server.domain.statistics.config.StatisticsProperties;
import com.kakaotech.team18.backend_server.domain.statistics.dto.StatisticsResponseDto;
import com.kakaotech.team18.backend_server.domain.statistics.entity.StatisticsDimension;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.OptionalLong;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 통계를 주기적으로 미리 계산해 캐시에 기록합니다.
 * <p>
 * 통계는 비로그인 공개 API이고 조회 빈도 제한도 없으므로, <strong>사전 계산 캐시가 사실상 유일한 부하 방어
 * 수단이다.</strong> 요청마다 집계하면 조회 수에 비례해 쿼리가 늘고, 하필 가장 중요한 지원서 제출 트래픽과
 * 같은 시각에 DB를 두고 경합한다.
 * <p>
 * 지원서 제출 경로에서는 이 클래스를 호출하지 않는다. 제출 시점에 집계를 트리거하면 마감 직전 몰림 구간에서
 * 집계 횟수가 지원 건수에 비례해 늘어나, 사전 계산을 도입한 이유가 사라진다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsPrecomputeService {

    /** 마감 후 이 시간 안에 있는 지원폼까지 사전 계산 대상에 포함해 최종 수치를 반영한다. */
    private static final Duration RECENTLY_CLOSED_WINDOW = Duration.ofDays(1);

    private final ClubApplyFormRepository clubApplyFormRepository;
    private final StatisticsServiceImpl statisticsService;
    private final StatisticsCacheStore cacheStore;
    private final StatisticsSnapshotReader snapshotReader;
    private final StatisticsProperties properties;

    /**
     * 사전 계산 대상 지원폼 전체를 훑어 캐시를 갱신합니다.
     *
     * @return 실제로 캐시를 갱신한 지원폼 수
     */
    public int precomputeAll() {
        if (!properties.precompute().enabled()) {
            log.debug("통계 사전 계산이 비활성화되어 있습니다.");
            return 0;
        }

        LocalDateTime now = LocalDateTime.now();
        List<ClubApplyForm> targets = clubApplyFormRepository.findAllForPrecompute(
                now, now.minus(RECENTLY_CLOSED_WINDOW));

        int updated = 0;
        for (ClubApplyForm form : targets) {
            try {
                if (precomputeOne(form)) {
                    updated++;
                }
            } catch (Exception e) {
                // 한 지원폼의 실패가 나머지 집계를 막지 않게 한다.
                log.error("통계 사전 계산 실패. clubApplyFormId={}", form.getId(), e);
            }
        }

        log.info("통계 사전 계산 완료. 대상={}, 갱신={}", targets.size(), updated);
        return updated;
    }

    /**
     * 지원폼 하나의 통계를 계산해 갱신 조건을 만족하면 캐시에 씁니다.
     *
     * @return 캐시를 갱신했으면 true
     */
    @Transactional(readOnly = true)
    public boolean precomputeOne(ClubApplyForm form) {
        Long formId = form.getId();

        // 여러 인스턴스가 같은 주기에 깨어나므로, 잠금을 얻은 하나만 집계한다.
        String token = UUID.randomUUID().toString();
        if (!cacheStore.tryLock(formId, token)) {
            log.debug("다른 인스턴스가 집계 중이라 건너뜁니다. clubApplyFormId={}", formId);
            return false;
        }

        try {
            StatisticsResponseDto payload =
                    statisticsService.calculate(form, StatisticsDimension.defaults());

            if (!shouldPublish(form, payload.totalApplicants())) {
                return false;
            }

            cacheStore.put(formId, payload);
            log.info("통계 캐시 갱신. clubApplyFormId={}, totalApplicants={}",
                    formId, payload.totalApplicants());
            return true;
        } finally {
            cacheStore.unlock(formId, token);
        }
    }

    /**
     * 이번 집계 결과를 공개할지 판단합니다.
     * <p>
     * 지원자가 없으면 공개할 분포 자체가 없으므로 건너뛴다. 신규 지원자가 없으면 직전 공개본과 결과가 같으므로
     * 굳이 다시 쓰지 않는다. 증가분이 공개 갱신 단위에 미달하면 직전 공개본을 그대로 유지한다.
     * <p>
     * 단, <strong>모집이 종료된 뒤에는 증가분과 무관하게 최종 수치를 공개한다.</strong> 그러지 않으면 마감
     * 직전에 들어온 몇 건이 영영 반영되지 않은 채로 남는다.
     */
    private boolean shouldPublish(ClubApplyForm form, long totalApplicants) {
        if (snapshotReader.exists(form.getId())) {
            // 스냅샷이 확정본이므로 조회는 그쪽을 본다. 여기서 계산해 봐야 쓰이지 않는 데다,
            // 이 시점에는 불합격 지원서가 이미 삭제되어 왜곡된 값이 나온다.
            log.debug("확정 스냅샷이 있어 사전 계산 대상에서 제외합니다. clubApplyFormId={}", form.getId());
            return false;
        }

        if (totalApplicants == 0) {
            log.debug("지원자가 없어 사전 계산 대상에서 제외합니다. clubApplyFormId={}", form.getId());
            return false;
        }

        OptionalLong published = cacheStore.findPublishedTotal(form.getId());
        if (published.isEmpty()) {
            // 공개 이력이 없으면 첫 공개다.
            return true;
        }

        long delta = totalApplicants - published.getAsLong();
        if (delta <= 0) {
            log.debug("신규 지원자가 없어 집계를 건너뜁니다. clubApplyFormId={}", form.getId());
            return false;
        }

        if (isClosed(form)) {
            return true;
        }

        if (delta < properties.precompute().publishStep()) {
            log.debug("증가분이 공개 갱신 단위 미만이라 직전 공개본을 유지합니다. clubApplyFormId={}, delta={}",
                    form.getId(), delta);
            return false;
        }
        return true;
    }

    private boolean isClosed(ClubApplyForm form) {
        Club club = form.getClub();
        return RecruitStatusCalculator.calculate(club.getRecruitStart(), club.getRecruitEnd())
                == RecruitStatus.CLOSED;
    }
}
