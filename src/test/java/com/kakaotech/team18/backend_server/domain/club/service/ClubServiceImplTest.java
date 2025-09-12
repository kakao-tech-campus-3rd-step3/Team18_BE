package com.kakaotech.team18.backend_server.domain.club.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.kakaotech.team18.backend_server.domain.club.dto.ClubListResponseDto;
import com.kakaotech.team18.backend_server.domain.club.dto.ClubSummary;
import com.kakaotech.team18.backend_server.domain.club.entity.Category;
import com.kakaotech.team18.backend_server.domain.club.repository.ClubRepository;
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
import org.mockito.ArgumentMatchers;

class ClubServiceImplTest {

    private ClubRepository clubRepository;
    private ClubServiceImpl clubService;

    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");
    private static final Instant FIXED_INSTANT = LocalDateTime.of(2025, 1, 1, 0, 0).atZone(ZONE).toInstant();

    @BeforeEach
    void setUp() {
        clubRepository = mock(ClubRepository.class);
        clubService = new ClubServiceImpl(clubRepository);
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
        @DisplayName("모집 상태 라벨이 올바르게 계산&매핑된다 (미정/준비중/모집중/종료)")
        void mapsRecruitStatusLabels() {
            // given
            LocalDateTime now = LocalDateTime.now();

            var undecided = new TestClubSummary(1L, "A", Category.SPORTS, "si",null, null);//미정
            var prepare = new TestClubSummary(2L, "B", Category.SPORTS, "si",now.plusDays(10),now.plusDays(20));//준비중
            var open = new TestClubSummary(3L, "C", Category.SPORTS, "si",now.minusDays(10),now.plusDays(10));//모집중
            var closed = new TestClubSummary(4L, "D", Category.SPORTS, "si",now.minusDays(30),now.minusDays(1));//종료
            when(clubRepository.findAllSummaries()).thenReturn(List.of(undecided, prepare, open, closed));

            //when
            List<ClubListResponseDto> result = clubService.getAllClubs();

            //then
            assertThat(result).hasSize(4);
            assertThat(result.get(0).recruitStatus()).isEqualTo("모집 일정 미정");
            assertThat(result.get(1).recruitStatus()).isEqualTo("모집 준비중");
            assertThat(result.get(2).recruitStatus()).isEqualTo("모집중");
            assertThat(result.get(3).recruitStatus()).isEqualTo("모집 종료");

            verify(clubRepository, times(1)).findAllSummaries();
            verifyNoMoreInteractions(clubRepository);
        }
    }

    @Nested
    @DisplayName("getClubByCategory")
    class GetByCategory {

        @Test
        @DisplayName("category == null 이면 전체 요약을 조회한다")
        void nullCategoryUsesAll() {
            when(clubRepository.findAllSummaries()).thenReturn(List.of());

            List<ClubListResponseDto> result = clubService.getClubByCategory(null);

            assertThat(result).isEmpty();
            verify(clubRepository, times(1)).findAllSummaries();
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
            assertThat(result.getFirst().name()).isEqualTo("Run Club");

            verify(clubRepository, times(1)).findSummariesByCategory(Category.SPORTS);
            verify(clubRepository, never()).findAllSummaries();
        }
    }

    @Nested
    @DisplayName("getClubByName")
    class GetByName {

        @Test
        @DisplayName("name 이 null/blank 이면 전체 요약을 조회한다")
        void blankUsesAll() {
            when(clubRepository.findAllSummaries()).thenReturn(List.of());

            assertThat(clubService.getClubByName(null)).isEmpty();
            assertThat(clubService.getClubByName("")).isEmpty();
            assertThat(clubService.getClubByName("   ")).isEmpty();

            verify(clubRepository, times(3)).findAllSummaries(); // 세 번 호출
            verify(clubRepository, never()).findSummariesByNameContaining(anyString());
        }

        @Test
        @DisplayName("name 이 주어지면 like 검색 요약을 조회한다")
        void givenNameUsesContaining() {
            when(clubRepository.findSummariesByNameContaining("Inter"))
                    .thenReturn(List.of(new TestClubSummary(7L, "InterX", Category.STUDY, "si", null, null)));

            List<ClubListResponseDto> result = clubService.getClubByName("Inter");

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().name()).isEqualTo("InterX");

            verify(clubRepository, times(1)).findSummariesByNameContaining("Inter");
            verify(clubRepository, never()).findAllSummaries();
        }
    }
}
