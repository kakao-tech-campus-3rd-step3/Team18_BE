package com.kakaotech.team18.backend_server.domain.club.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.kakaotech.team18.backend_server.domain.application.entity.Application;
import com.kakaotech.team18.backend_server.domain.application.entity.Status;
import com.kakaotech.team18.backend_server.domain.application.repository.ApplicationRepository;
import com.kakaotech.team18.backend_server.domain.club.dto.ClubDashBoardResponseDto;
import com.kakaotech.team18.backend_server.domain.club.entity.Category;
import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import com.kakaotech.team18.backend_server.domain.club.entity.ClubIntroduction;
import com.kakaotech.team18.backend_server.domain.club.repository.ClubRepository;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.entity.ClubApplyForm;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.repository.ClubApplyFormRepository;
import com.kakaotech.team18.backend_server.domain.clubMember.dto.ApplicantResponseDto;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.ActiveStatus;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.ClubMember;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.Role;
import com.kakaotech.team18.backend_server.domain.clubMember.repository.ClubMemberRepository;
import com.kakaotech.team18.backend_server.domain.user.entity.User;
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
    ClubMemberRepository clubMemberRepository;
    @Mock
    ApplicationRepository applicationRepository;
    @Mock
    ClubApplyFormRepository clubApplyFormRepository;

    @InjectMocks
    ClubServiceImpl clubService;

    @DisplayName("동아리 대쉬보드를 조회합니다.")
    @Test
    void getClubDashBoard() {
        //given
        ClubIntroduction clubIntroduction = mock(ClubIntroduction.class);
        Club club = createClub(clubIntroduction);
        ReflectionTestUtils.setField(club, "id", 1L);
        User user = createUser(club);
        ReflectionTestUtils.setField(user, "id", 1L);
        ClubApplyForm clubApplyForm = createClubApplyForm(club);
        ReflectionTestUtils.setField(clubApplyForm, "id", 1L);
        Application application = createApplication(user, clubApplyForm);
        ReflectionTestUtils.setField(application, "id", 1L);
        ClubMember clubMember = createClubMEmber(user, club, application);
        ReflectionTestUtils.setField(clubMember, "id", 1L);

//        given(clubRepository.findById(eq(1L))).willReturn(Optional.of(club));
//        given(clubApplyFormRepository.getByClub(eq(club))).willReturn(Optional.of(clubApplyForm));
//        given(clubMemberRepository.findByClubIdAndRole(eq(1L), eq(Role.APPLICANT))).willReturn(List.of(clubMember));
//        given(applicationRepository.findByClubApplyFormIdAndStatus(eq(1L), eq(Status.PENDING))).willReturn(List.of(application));

        doReturn(Optional.of(club)).when(clubRepository).findById(1L);
        doReturn(Optional.of(clubApplyForm)).when(clubApplyFormRepository).getByClub(club);
        doReturn(List.of(clubMember)).when(clubMemberRepository).findByClubIdAndRole(1L, Role.APPLICANT);
        doReturn(List.of(application)).when(applicationRepository).findByClubApplyFormIdAndStatus(1L, Status.PENDING);

        var captor = org.mockito.ArgumentCaptor.forClass(Long.class);
        clubService.getClubDashBoard(1L);
        verify(clubRepository).findById(captor.capture());
        assertThat(captor.getValue()).isEqualTo(1L);


        //when
        ClubDashBoardResponseDto expect = new ClubDashBoardResponseDto(1, 1,
                "2025-09-03", "2025-09-20",
                List.of(new ApplicantResponseDto("김춘식", "123456", "철학과", "010-1234-5678",
                        "123@email.com", Status.PENDING)));

        ClubDashBoardResponseDto actual = clubService.getClubDashBoard(1L);

        //then
        assertThat(actual).usingRecursiveComparison().isEqualTo(expect);
        verify(clubRepository).findById(1L);
        verify(clubApplyFormRepository).getByClub(club);
        verify(clubMemberRepository).findByClubIdAndRole(1L, Role.APPLICANT);
        verify(applicationRepository).findByClubApplyFormIdAndStatus(1L, Status.PENDING);


    }


    private static User createUser(Club club) {
        return User.builder()
                .loginId("loginId")
                .password("password")
                .name("김춘식")
                .email("123@email.com")
                .studentId("123456")
                .phoneNumber("010-1234-5678")
                .department("철학과")
                .build();
    }


    private Club createClub(
            ClubIntroduction clubIntroduction) {

        return Club.builder()
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

    private static ClubApplyForm createClubApplyForm(Club club) {
        return ClubApplyForm.builder()
                .club(club)
                .title("title")
                .description("description")
                .build();
    }

    private static ClubMember createClubMEmber(User user, Club club, Application application) {
        return ClubMember.builder()
                .user(user)
                .club(club)
                .activeStatus(ActiveStatus.ACTIVE)
                .role(Role.APPLICANT)
                .application(application)
                .build();
    }

    private static Application createApplication(User user, ClubApplyForm clubApplyForm) {
        return Application.builder()
                .user(user)
                .clubApplyForm(clubApplyForm)
                .build();
    }



}
