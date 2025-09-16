package com.kakaotech.team18.backend_server.domain.club.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.kakaotech.team18.backend_server.domain.applicant.dto.ApplicantResponseDto;
import com.kakaotech.team18.backend_server.domain.applicant.entity.Applicant;
import com.kakaotech.team18.backend_server.domain.applicant.repository.ApplicantRepository;
import com.kakaotech.team18.backend_server.domain.application.entity.Application;
import com.kakaotech.team18.backend_server.domain.application.entity.Status;
import com.kakaotech.team18.backend_server.domain.application.repository.ApplicationRepository;
import com.kakaotech.team18.backend_server.domain.applicationForm.entity.ApplicationForm;
import com.kakaotech.team18.backend_server.domain.club.dto.ClubDashBoardResponseDto;
import com.kakaotech.team18.backend_server.domain.club.entity.Category;
import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import com.kakaotech.team18.backend_server.domain.club.entity.ClubIntroduction;
import com.kakaotech.team18.backend_server.domain.club.repository.ClubRepository;
import com.kakaotech.team18.backend_server.domain.user.entity.User;
import com.kakaotech.team18.backend_server.global.exception.exceptions.ClubNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
@ExtendWith(MockitoExtension.class)
public class ClubServiceMockTest {

    @Mock
    ClubRepository clubRepository;
    @Mock
    ApplicantRepository applicantRepository;
    @Mock
    ApplicationRepository applicationRepository;

    @InjectMocks
    ClubServiceImpl clubService;

    @DisplayName("동아리 대쉬보드를 조회합니다.")
    @Test
    void getClubDashBoard() {
        //given
        Long clubId = 1L;
        User mockUser = mock(User.class);
        ClubIntroduction mockIntro = mock(ClubIntroduction.class);
        Club club = createClub(mockUser, mockIntro);
        ReflectionTestUtils.setField(club, "id", clubId);
        Applicant applicant = createApplicant(club);
        ApplicationForm mockApplicationForm = mock(ApplicationForm.class);
        Application application = Application.builder()
                .user(mockUser)
                .club(club)
                .applicationForm(mockApplicationForm)
                .applicant(applicant)
                .build();


        given(clubRepository.findById(eq(clubId)))
                .willReturn(Optional.of(club));

        given(applicantRepository.findByClubId(eq(clubId)))
                .willReturn(List.of(applicant));

        given(applicationRepository.findByClubIdAndStatus(
                        eq(clubId), eq(Status.PENDING)))
                .willReturn(List.of(application));
        //when
        ClubDashBoardResponseDto expect = new ClubDashBoardResponseDto(
                1,
                1,
                "2025-09-03",
                "2025-09-20",
                List.of(new ApplicantResponseDto("김춘식", "123456", "철학과", "010-1234-5678", "123@email.com", Status.PENDING))
        );

        ClubDashBoardResponseDto actual = clubService.getClubDashBoard(clubId);

        //then
        assertThat(actual).isEqualTo(expect);
        verify(clubRepository).findById(eq(clubId));
        verify(applicantRepository).findByClubId(eq(clubId));
        verify(applicationRepository).findByClubIdAndStatus(eq(clubId), eq(Status.PENDING));
    }

    @DisplayName("동아리 대쉬보드를 조회시 지원자가 없으면 빈 지원자를 반환한다.")
    @Test
    void getClubDashBoardWithEmptyData() {
        //given
        Long clubId = 1L;
        User mockUser = mock(User.class);
        ClubIntroduction mockIntro = mock(ClubIntroduction.class);
        Club club = createClub(mockUser, mockIntro);
        ReflectionTestUtils.setField(club, "id", clubId);
        Applicant applicant = createApplicant(club);
        ApplicationForm mockApplicationForm = mock(ApplicationForm.class);
        Application application = Application.builder()
                .user(mockUser)
                .club(club)
                .applicationForm(mockApplicationForm)
                .applicant(applicant)
                .build();


        given(clubRepository.findById(eq(clubId)))
                .willReturn(Optional.of(club));

        given(applicantRepository.findByClubId(eq(clubId)))
                .willReturn(List.of());

        given(applicationRepository.findByClubIdAndStatus(
                        eq(clubId), eq(Status.PENDING)))
                .willReturn(List.of());
        //when
        ClubDashBoardResponseDto expect = new ClubDashBoardResponseDto(
                0,
                0,
                "2025-09-03",
                "2025-09-20",
                List.of());


        ClubDashBoardResponseDto actual = clubService.getClubDashBoard(clubId);

        //then
        assertThat(actual).isEqualTo(expect);
        verify(clubRepository).findById(eq(clubId));
        verify(applicantRepository).findByClubId(eq(clubId));
        verify(applicationRepository).findByClubIdAndStatus(eq(clubId), eq(Status.PENDING));
    }

    @DisplayName("존재하지 않는 동아리의 대쉬보드에 접속하면 에러를 반환한다.")
    @Test
    void getClubDashBoardWithWrongClubId() {
        // given
        Long missingId = 2L;

        // 존재하지 않는 ID에 대해 Optional.empty()를 리턴하도록 스텁
        given(clubRepository.findById(eq(missingId)))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> clubService.getClubDashBoard(missingId))
                .isInstanceOf(ClubNotFoundException.class)
                .hasMessage("해당 동아리가 존재하지 않습니다.");

        // 클럽이 없으면 다른 레포는 호출되지 않아야 함
        verifyNoInteractions(applicantRepository, applicationRepository);
    }
    private static Applicant createApplicant(Club club) {
        return Applicant.builder()
                .name("김춘식")
                .email("123@email.com")
                .studentId("123456")
                .phoneNumber("010-1234-5678")
                .department("철학과")
                .club(club)
                .build();
    }


    private Club createClub(
            User president,
            ClubIntroduction clubIntroduction) {

        return Club.builder()
                .president(president)
                .name("카태켐")
                .category(Category.LITERATURE)
                .location("공7 1호관")
                .shortIntroduction("함께 배우는 카태켐")
                .introduction(clubIntroduction)
                .recruitStart(LocalDateTime.of(2025, 9, 3, 0, 0))
                .recruitEnd(LocalDateTime.of(2025, 9, 20, 23, 59))
                .regularMeetingInfo("매주 화요일 오후 6시반")
                .build();
    }


}
