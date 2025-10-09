package com.kakaotech.team18.backend_server.domain.club.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.kakaotech.team18.backend_server.domain.application.repository.ApplicationRepository;
import com.kakaotech.team18.backend_server.domain.club.dto.ClubListResponseDto;
import com.kakaotech.team18.backend_server.domain.club.dto.ClubSummary;
import com.kakaotech.team18.backend_server.domain.club.entity.Category;
import com.kakaotech.team18.backend_server.domain.club.repository.ClubRepository;
import com.kakaotech.team18.backend_server.domain.club.util.RecruitStatus;
import com.kakaotech.team18.backend_server.domain.club.util.RecruitStatusCalculator;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.repository.ClubApplyFormRepository;
import com.kakaotech.team18.backend_server.domain.clubMember.repository.ClubMemberRepository;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

class ClubServiceImplTest {

    private ClubRepository clubRepository;
    private ClubService clubService;

    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");
    private static final Instant FIXED_INSTANT = LocalDateTime.of(2025, 1, 1, 0, 0).atZone(ZONE).toInstant();

    @BeforeEach
    void setUp() {
        clubRepository = mock(ClubRepository.class);
        ClubMemberRepository clubMemberRepository = mock(ClubMemberRepository.class);
        ApplicationRepository applicationRepository = mock(ApplicationRepository.class);
        ClubApplyFormRepository clubApplyFormRepository = mock(ClubApplyFormRepository.class);
        clubService = new ClubServiceImpl(clubRepository, applicationRepository, clubMemberRepository, clubApplyFormRepository);
    }

    @Getter
    @AllArgsConstructor
    private static class TestClubSummary implements ClubSummary {
        private final Long id;
        private final String name;
        private final Category category;
        private final String shortIntroduction;
        private final LocalDateTime recruitStart;
        private final LocalDateTime recruitEnd;
    }

    @Nested
    @DisplayName("getAllClubs")
    class GetAllClubs {

        @Test
        @DisplayName("RecruitStatusCalculator의 결과를 DTO에 올바르게 매핑한다")
        void mapsRecruitStatusCorrectly() {
            // try-with-resources 구문을 사용하여 테스트가 끝나면 mock이 자동으로 해제되도록 합니다.
            try (MockedStatic<RecruitStatusCalculator> mockedCalculator = Mockito.mockStatic(RecruitStatusCalculator.class)) {
                // given
                final RecruitStatus DUMMY_STATUS_ENUM = RecruitStatus.RECRUITING;
                var summary = new TestClubSummary(1L, "A", Category.SPORTS, "si", null, null);

                // RecruitStatusCalculator.calculate가 어떤 인자로 호출되든 DUMMY_STATUS_ENUM을 반환하도록 설정
                mockedCalculator.when(() -> RecruitStatusCalculator.calculate(summary.getRecruitStart(), summary.getRecruitEnd()))
                        .thenReturn(DUMMY_STATUS_ENUM);

                when(clubRepository.findAllProjectedBy()).thenReturn(List.of(summary));

                //when
                List<ClubListResponseDto> result = clubService.getAllClubs();

                //then
                assertThat(result).hasSize(1);
                ClubListResponseDto wrapper = result.get(0);
                assertThat(wrapper.clubs()).hasSize(1);
                assertThat(wrapper.clubs().get(0).recruitStatus()).isEqualTo(DUMMY_STATUS_ENUM.getDisplayName());
                verify(clubRepository, times(1)).findAllProjectedBy();
                verifyNoMoreInteractions(clubRepository);
            }
        }
    }

    @Nested
    @DisplayName("getClubByCategory")
    class GetByCategory {

        @Test
        @DisplayName("category == null 이면 전체 요약을 조회한다")
        void nullCategoryUsesAll() {
            when(clubRepository.findAllProjectedBy()).thenReturn(List.of());

            List<ClubListResponseDto> result = clubService.getClubByCategory(null);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).clubs()).isEmpty();
            verify(clubRepository, times(1)).findAllProjectedBy();
            verify(clubRepository, never()).findSummariesByCategory(ArgumentMatchers.any());
        }

        @Test
        @DisplayName("category 가 주어지면 카테고리 요약을 조회한다")
        void givenCategoryUsesByCategory() {
            when(clubRepository.findSummariesByCategory(Category.SPORTS)).thenReturn(List.of(
                    new TestClubSummary(10L, "Run Club", Category.SPORTS, "run", null, null)
            ));

            List<ClubListResponseDto> result = clubService.getClubByCategory(Category.SPORTS);

            assertThat(result).hasSize(1);
            ClubListResponseDto wrapper = result.get(0);
            assertThat(wrapper.clubs()).hasSize(1);
            assertThat(wrapper.clubs().get(0).name()).isEqualTo("Run Club");

            verify(clubRepository, times(1)).findSummariesByCategory(Category.SPORTS);
            verify(clubRepository, never()).findAllProjectedBy();
        }
    }

    @Nested
    @DisplayName("getClubByName")
    class GetByName {

        @Test
        @DisplayName("name 이 null/blank 이면 전체 요약을 조회한다")
        void blankUsesAll() {
            when(clubRepository.findAllProjectedBy()).thenReturn(List.of());

            List<ClubListResponseDto> r1 = clubService.getClubByName(null);
            List<ClubListResponseDto> r2 = clubService.getClubByName("");
            List<ClubListResponseDto> r3 = clubService.getClubByName("   ");

            assertThat(r1).hasSize(1);
            assertThat(r1.get(0).clubs()).isEmpty();

            assertThat(r2).hasSize(1);
            assertThat(r2.get(0).clubs()).isEmpty();

            assertThat(r3).hasSize(1);
            assertThat(r3.get(0).clubs()).isEmpty();

            verify(clubRepository, times(3)).findAllProjectedBy(); // 세 번 호출
            verify(clubRepository, never()).findSummariesByNameContaining(anyString());
        }

        @Test
        @DisplayName("name 이 주어지면 like 검색 요약을 조회한다")
        void givenNameUsesContaining() {
            when(clubRepository.findSummariesByNameContaining("Inter"))
                    .thenReturn(List.of(new TestClubSummary(7L, "InterX", Category.STUDY, "si", null, null)));

            List<ClubListResponseDto> result = clubService.getClubByName("Inter");

            assertThat(result).hasSize(1);
            ClubListResponseDto wrapper = result.get(0);
            assertThat(wrapper.clubs()).hasSize(1);
            assertThat(wrapper.clubs().get(0).name()).isEqualTo("InterX");

            verify(clubRepository, times(1)).findSummariesByNameContaining("Inter");
            verify(clubRepository, never()).findAllProjectedBy();
        }
    }
}
