package com.kakaotech.team18.backend_server.domain.statistics.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.entity.ClubApplyForm;
import com.kakaotech.team18.backend_server.domain.statistics.dto.StatisticsResponseDto;
import com.kakaotech.team18.backend_server.domain.statistics.entity.ClubApplyFormStatisticsSnapshot;
import com.kakaotech.team18.backend_server.domain.statistics.entity.StatisticsDimension;
import com.kakaotech.team18.backend_server.domain.statistics.entity.StatisticsRulesVersion;
import com.kakaotech.team18.backend_server.domain.statistics.repository.ClubApplyFormStatisticsSnapshotRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("통계 스냅샷")
class StatisticsSnapshotServiceTest {

    private static final Long FORM_ID = 12L;

    @Mock
    ClubApplyFormStatisticsSnapshotRepository snapshotRepository;
    @Mock
    StatisticsServiceImpl statisticsService;

    ObjectMapper objectMapper;
    ClubApplyForm form;
    StatisticsSnapshotService snapshotService;
    StatisticsSnapshotReader snapshotReader;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        form = mock(ClubApplyForm.class);
        lenient().when(form.getId()).thenReturn(FORM_ID);

        snapshotService = new StatisticsSnapshotService(snapshotRepository, statisticsService, objectMapper);
        snapshotReader = new StatisticsSnapshotReader(snapshotRepository, objectMapper);
    }

    /** 불합격자를 포함한, 삭제 전의 온전한 분포. */
    private StatisticsResponseDto fullDistribution() {
        return new StatisticsResponseDto(FORM_ID, 100, false, OffsetDateTime.now(), List.of(
                new StatisticsResponseDto.DimensionResult(
                        StatisticsDimension.GENDER, StatisticsDimension.GENDER.getType(), null, null,
                        List.of(
                                new StatisticsResponseDto.Bucket(
                                        "MALE", "남성", 60, new BigDecimal("0.600"), null),
                                new StatisticsResponseDto.Bucket(
                                        "FEMALE", "여성", 40, new BigDecimal("0.400"), null)))
        ));
    }

    @Nested
    @DisplayName("저장")
    class Saving {

        @DisplayName("스냅샷이 없으면 현재 집계 결과를 확정 저장한다")
        @Test
        void savesWhenAbsent() {
            when(snapshotRepository.existsByClubApplyFormId(FORM_ID)).thenReturn(false);
            when(statisticsService.calculate(any(), any())).thenReturn(fullDistribution());

            assertThat(snapshotService.saveIfAbsent(form)).isTrue();

            ArgumentCaptor<ClubApplyFormStatisticsSnapshot> captor =
                    ArgumentCaptor.forClass(ClubApplyFormStatisticsSnapshot.class);
            verify(snapshotRepository).save(captor.capture());

            ClubApplyFormStatisticsSnapshot saved = captor.getValue();
            assertThat(saved.getTotalApplicants()).isEqualTo(100);
            assertThat(saved.getRulesVersion()).isEqualTo(StatisticsRulesVersion.CURRENT);
            assertThat(saved.getPayload()).contains("MALE", "FEMALE");
        }

        @DisplayName("이미 스냅샷이 있으면 덮어쓰지 않는다")
        @Test
        void doesNotOverwriteExistingSnapshot() {
            when(snapshotRepository.existsByClubApplyFormId(FORM_ID)).thenReturn(true);

            assertThat(snapshotService.saveIfAbsent(form)).isFalse();

            // 이 시점에는 이미 불합격자가 빠져나간 왜곡된 분포만 계산할 수 있다.
            verify(statisticsService, never()).calculate(any(), any());
            verify(snapshotRepository, never()).save(any());
        }

        @DisplayName("개별 지원자 정보는 저장하지 않는다")
        @Test
        void storesNoIdentifyingInformation() {
            when(snapshotRepository.existsByClubApplyFormId(FORM_ID)).thenReturn(false);
            when(statisticsService.calculate(any(), any())).thenReturn(fullDistribution());

            snapshotService.saveIfAbsent(form);

            ArgumentCaptor<ClubApplyFormStatisticsSnapshot> captor =
                    ArgumentCaptor.forClass(ClubApplyFormStatisticsSnapshot.class);
            verify(snapshotRepository).save(captor.capture());

            String payload = captor.getValue().getPayload();
            assertThat(payload)
                    .doesNotContain("studentId")
                    .doesNotContain("email")
                    .doesNotContain("phoneNumber")
                    .doesNotContain("applicationId");
        }

        @DisplayName("저장에 실패하면 예외를 삼키지 않는다")
        @Test
        void propagatesFailureSoCallerRollsBack() {
            when(snapshotRepository.existsByClubApplyFormId(FORM_ID)).thenReturn(false);
            when(statisticsService.calculate(any(), any())).thenReturn(fullDistribution());
            doThrow(new RuntimeException("DB 장애")).when(snapshotRepository).save(any());

            // 예외가 전파되어야 호출자의 트랜잭션이 롤백되고 불합격 지원서 삭제도 취소된다.
            // 여기서 삼키면 스냅샷 없이 원본만 사라진다.
            assertThatThrownBy(() -> snapshotService.saveIfAbsent(form))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("조회")
    class Reading {

        @DisplayName("불합격 지원서가 삭제된 뒤에도 삭제 전 분포가 그대로 유지된다")
        @Test
        void survivesRejectedApplicationDeletion() {
            // 삭제 전에 저장된 스냅샷 (남 60 / 여 40, 총 100명)
            when(snapshotRepository.existsByClubApplyFormId(FORM_ID)).thenReturn(false);
            when(statisticsService.calculate(any(), any())).thenReturn(fullDistribution());
            snapshotService.saveIfAbsent(form);

            ArgumentCaptor<ClubApplyFormStatisticsSnapshot> captor =
                    ArgumentCaptor.forClass(ClubApplyFormStatisticsSnapshot.class);
            verify(snapshotRepository).save(captor.capture());
            ClubApplyFormStatisticsSnapshot stored = captor.getValue();

            // 이후 불합격자가 삭제되어 DB에는 합격자만 남았더라도, 조회는 스냅샷을 본다.
            when(snapshotRepository.findByClubApplyFormId(FORM_ID)).thenReturn(Optional.of(stored));

            StatisticsResponseDto restored = snapshotReader.find(FORM_ID).orElseThrow();

            assertThat(restored.totalApplicants()).isEqualTo(100);
            assertThat(restored.snapshot()).isTrue();
            assertThat(restored.results().get(0).buckets())
                    .extracting(StatisticsResponseDto.Bucket::count)
                    .containsExactly(60L, 40L);
        }

        @DisplayName("스냅샷이 없으면 비어 있는 결과를 반환한다")
        @Test
        void emptyWhenNoSnapshot() {
            when(snapshotRepository.findByClubApplyFormId(FORM_ID)).thenReturn(Optional.empty());

            assertThat(snapshotReader.find(FORM_ID)).isEmpty();
        }

        @DisplayName("스냅샷 존재 여부를 확인할 수 있다")
        @Test
        void checksExistence() {
            when(snapshotRepository.existsByClubApplyFormId(anyLong())).thenReturn(true);

            assertThat(snapshotReader.exists(FORM_ID)).isTrue();
        }
    }
}
