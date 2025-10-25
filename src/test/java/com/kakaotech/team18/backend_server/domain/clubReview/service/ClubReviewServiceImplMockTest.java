package com.kakaotech.team18.backend_server.domain.clubReview.service;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import com.kakaotech.team18.backend_server.domain.club.repository.ClubRepository;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.ClubMember;
import com.kakaotech.team18.backend_server.domain.clubMember.repository.ClubMemberRepository;
import com.kakaotech.team18.backend_server.domain.clubReview.dto.ClubReviewRequestDto;
import com.kakaotech.team18.backend_server.domain.clubReview.dto.ClubReviewResponseDto;
import com.kakaotech.team18.backend_server.domain.clubReview.entity.ClubReview;
import com.kakaotech.team18.backend_server.domain.clubReview.repository.ClubReviewRepository;
import com.kakaotech.team18.backend_server.domain.user.entity.User;
import com.kakaotech.team18.backend_server.global.dto.SuccessResponseDto;
import com.kakaotech.team18.backend_server.global.exception.exceptions.ClubNotFoundException;
import com.kakaotech.team18.backend_server.global.exception.exceptions.UnRegisteredUserException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class ClubReviewServiceImplMockTest {

    @Mock
    private ClubReviewRepository clubReviewRepository;
    @Mock
    private ClubRepository clubRepository;
    @Mock
    private ClubMemberRepository clubMemberRepository;
    @InjectMocks
    private ClubReviewServiceImpl clubReviewService;

    @DisplayName("동아리 후기 생성 - 성공")
    @Test
    void createClubReview_Success() {
        // given
        Long clubId = 1L;
        String studentId = "20221234";
        ClubReviewRequestDto requestDto = new ClubReviewRequestDto("최고의 개발 동아리입니다.", studentId);
        Club club = Club.builder().name("Test Club").build();
        User user = User.builder()
                .name("Test User")
                .studentId(studentId)
                .build();
        ReflectionTestUtils.setField(club, "id", clubId);
        ClubMember clubMember = ClubMember.builder()
                .user(user)
                .club(club).build();
        ClubReview clubReview = ClubReview.builder()
                .club(club)
                .content(requestDto.content())
                .writer(requestDto.studentId())
                .build();

        given(clubMemberRepository.findByClubIdAndUserStudentId(clubId, studentId)).willReturn(Optional.of(clubMember));
        given(clubRepository.findById(clubId)).willReturn(Optional.of(club));
        given(clubReviewRepository.save(any(ClubReview.class))).willReturn(clubReview);

        // when
        SuccessResponseDto response = clubReviewService.createClubReview(clubId, requestDto);

        // then
        assertThat(response.success()).isTrue();
        then(clubRepository).should(times(1)).findById(clubId);
        then(clubReviewRepository).should(times(1)).save(any(ClubReview.class));
    }

    @DisplayName("동아리 후기 생성 - 실패 (동아리를 찾을 수 없음)")
    @Test
    void createClubReview_ClubNotFound() {
        // given
        Long clubId = 1L;
        String studentId = "20221234";
        ClubReviewRequestDto requestDto = new ClubReviewRequestDto("최고의 개발 동아리입니다.", studentId);
        Club club = Club.builder().name("Test Club").build();
        User user = User.builder()
                .name("Test User")
                .studentId(studentId)
                .build();
        ReflectionTestUtils.setField(club, "id", clubId);
        ClubMember clubMember = ClubMember.builder()
                .user(user)
                .club(club).build();
        ClubReview clubReview = ClubReview.builder()
                .club(club)
                .content(requestDto.content())
                .writer(requestDto.studentId())
                .build();
        given(clubMemberRepository.findByClubIdAndUserStudentId(clubId, studentId)).willReturn(Optional.of(clubMember));
        given(clubRepository.findById(clubId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> clubReviewService.createClubReview(clubId, requestDto))
                .isInstanceOf(ClubNotFoundException.class)
                .hasMessageContaining("해당 동아리가 존재하지 않습니다.");

        then(clubRepository).should(times(1)).findById(clubId);
        then(clubReviewRepository).should(never()).save(any(ClubReview.class));
    }

    @DisplayName("동아리 후기 생성 - 실패 (미등록 사용자)")
    @Test
    void createClubReview_UnRegisteredUser() {
        // given
        Long clubId = 1L;
        String studentId = "20221234";
        String unregisteredStudentId = "20225678";
        ClubReviewRequestDto requestDto = new ClubReviewRequestDto("최고의 개발 동아리입니다.", unregisteredStudentId);
        Club club = Club.builder().name("Test Club").build();
        User user = User.builder()
                .name("Test User")
                .studentId(studentId)
                .build();
        ReflectionTestUtils.setField(club, "id", clubId);
        ClubMember clubMember = ClubMember.builder()
                .user(user)
                .club(club).build();


        // when & then
        assertThatThrownBy(() -> clubReviewService.createClubReview(clubId, requestDto))
                .isInstanceOf(UnRegisteredUserException.class)
                .hasMessageContaining("동아리에 가입되지 않은 유저입니다.");
        then(clubRepository).should(never()).findById(any(Long.class));
        then(clubReviewRepository).should(never()).save(any(ClubReview.class));
    }

    @DisplayName("동아리 후기 조회 - 성공")
    @Test
    void getClubReview_Success() {
        // given
        Long clubId = 1L;
        Club club = Club.builder().name("Test Club").build();
        ReflectionTestUtils.setField(club, "id", clubId);

        ClubReview review1 = ClubReview.builder()
                .club(club)
                .content("리뷰 내용 1")
                .writer("20250001")
                .build();
        ReflectionTestUtils.setField(review1, "id", 1L);

        ClubReview review2 = ClubReview.builder()
                .club(club)
                .content("리뷰 내용 2")
                .writer("20250002")
                .build();
        ReflectionTestUtils.setField(review2, "id", 2L);

        given(clubReviewRepository.findByClubId(clubId)).willReturn(List.of(review1, review2));

        // when
        ClubReviewResponseDto response = clubReviewService.getClubReview(clubId);

        // then
        String firstWriter = response.reviews().get(0).writer();
        String secondWriter = response.reviews().get(1).writer();
        assertThat(response.reviews().size()).isEqualTo(2);
        assertThat(response.reviews().get(0).content()).isEqualTo("리뷰 내용 1");
        assertThat(response.reviews().get(1).content()).isEqualTo("리뷰 내용 2");

        assertThat(firstWriter).containsAnyOf("코끼리", "여우", "펭귄", "돌고래", "사자", "호랑이");
        assertThat(secondWriter).containsAnyOf("코끼리", "여우", "펭귄", "돌고래", "사자", "호랑이");
        assertThat(firstWriter).isNotEqualTo(secondWriter);

        //동일 학번이면 항상 같은 익명 이름 (재호출 테스트)
        ClubReviewResponseDto again = clubReviewService.getClubReview(clubId);
        assertThat(response.reviews().get(0).writer())
                .isEqualTo(again.reviews().get(0).writer());

        //위에서 재호출해서 2번
        then(clubReviewRepository).should(times(2)).findByClubId(clubId);
    }

    @DisplayName("동아리 후기 조회 - 빈 리스트 (후기가 없는 경우)")
    @Test
    void getClubReview_NoReviews() {
        // given
        Long clubId = 1L;
        given(clubReviewRepository.findByClubId(clubId)).willReturn(List.of());

        // when
        ClubReviewResponseDto response = clubReviewService.getClubReview(clubId);

        // then
        assertThat(response.reviews()).isEmpty();
        then(clubReviewRepository).should(times(1)).findByClubId(clubId);
    }

}