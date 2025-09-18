package com.kakaotech.team18.backend_server.domain.club.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.kakaotech.team18.backend_server.domain.application.entity.Application;
import com.kakaotech.team18.backend_server.domain.application.entity.Status;
import com.kakaotech.team18.backend_server.domain.application.repository.ApplicationRepository;
import com.kakaotech.team18.backend_server.domain.club.dto.ClubDashBoardResponseDto;
import com.kakaotech.team18.backend_server.domain.club.dto.ClubDetailResponseDto;
import com.kakaotech.team18.backend_server.domain.club.entity.Category;
import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import com.kakaotech.team18.backend_server.domain.club.entity.ClubImage;
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
        Long clubId = 1L;
        Club club = createClub(mock(ClubIntroduction.class), LocalDateTime.of(2025, 9, 3, 0, 0), LocalDateTime.of(2025, 9, 20, 23, 59));
        ReflectionTestUtils.setField(club, "id", 1L);
        User user = createUser(club);
        ReflectionTestUtils.setField(user, "id", 1L);
        ClubApplyForm clubApplyForm = createClubApplyForm(club);
        ReflectionTestUtils.setField(clubApplyForm, "id", 1L);
        Application application = createApplication(user, clubApplyForm, Status.PENDING);
        ReflectionTestUtils.setField(application, "id", 1L);
        ClubMember clubMember = createClubMember(user, club, application, Role.APPLICANT, ActiveStatus.ACTIVE);
        ReflectionTestUtils.setField(clubMember, "id", 1L);

        given(clubRepository.findById(eq(clubId))).willReturn(Optional.of(club));
        given(clubApplyFormRepository.getByClub(eq(club))).willReturn(Optional.of(clubApplyForm));
        given(clubMemberRepository.findByClubIdAndRole(eq(clubId), eq(Role.APPLICANT))).willReturn(List.of(clubMember));
        given(applicationRepository.findByClubApplyFormIdAndStatus(eq(1L), eq(Status.PENDING))).willReturn(List.of(application));

        ClubDashBoardResponseDto expect = new ClubDashBoardResponseDto(1, 1,
                "2025-09-03", "2025-09-20",
                List.of(new ApplicantResponseDto("김춘식", "123456", "철학과", "010-1234-5678",
                        "123@email.com", Status.PENDING)));

        //when
        ClubDashBoardResponseDto actual = clubService.getClubDashBoard(clubId);

        //then
        assertThat(actual).isEqualTo(expect);
        verify(clubRepository).findById(eq(clubId));
        verify(clubApplyFormRepository).getByClub(club);
        verify(clubMemberRepository).findByClubIdAndRole(1L, Role.APPLICANT);
        verify(applicationRepository).findByClubApplyFormIdAndStatus(1L, Status.PENDING);
    }

    @DisplayName("동아리 대쉬보드를 조회시 지원자가 없으면 빈 지원자를 반환한다.")
    @Test
    void getClubDashBoardWithEmptyData() {
        //given
        Long clubId = 1L;
        Club club = createClub(mock(ClubIntroduction.class), LocalDateTime.of(2025, 9, 3, 0, 0), LocalDateTime.of(2025, 9, 20, 23, 59));
        ReflectionTestUtils.setField(club, "id", 1L);
        User user = createUser(club);
        ReflectionTestUtils.setField(user, "id", 1L);
        ClubApplyForm clubApplyForm = createClubApplyForm(club);
        ReflectionTestUtils.setField(clubApplyForm, "id", 1L);
        Application application = createApplication(user, clubApplyForm, Status.APPROVED);
        ReflectionTestUtils.setField(application, "id", 1L);
        ClubMember clubMember = createClubMember(user, club, application, Role.APPLICANT, ActiveStatus.ACTIVE);
        ReflectionTestUtils.setField(clubMember, "id", 1L);

        given(clubRepository.findById(eq(clubId))).willReturn(Optional.of(club));
        given(clubApplyFormRepository.getByClub(eq(club))).willReturn(Optional.of(clubApplyForm));
        given(clubMemberRepository.findByClubIdAndRole(eq(clubId), eq(Role.APPLICANT))).willReturn(List.of());
        given(applicationRepository.findByClubApplyFormIdAndStatus(eq(1L), eq(Status.PENDING))).willReturn(List.of());

        ClubDashBoardResponseDto expect = new ClubDashBoardResponseDto(0, 0, "2025-09-03", "2025-09-20", List.of());

        //when
        ClubDashBoardResponseDto actual = clubService.getClubDashBoard(clubId);

        //then
        assertThat(actual).isEqualTo(expect);
        verify(clubRepository).findById(eq(clubId));
        verify(clubApplyFormRepository).getByClub(club);
        verify(clubMemberRepository).findByClubIdAndRole(1L, Role.APPLICANT);
        verify(applicationRepository).findByClubApplyFormIdAndStatus(1L, Status.PENDING);

    }

    @DisplayName("존재하지 않는 동아리의 대쉬보드에 접속하면 에러를 반환한다.")
    @Test
    void getClubDashBoardWithWrongClubId() {
        //given
        Long clubId = 1L;
        Long missingId = 0L;
        Club club = createClub(mock(ClubIntroduction.class), LocalDateTime.of(2025, 9, 3, 0, 0), LocalDateTime.of(2025, 9, 20, 23, 59));
        ReflectionTestUtils.setField(club, "id", 1L);
        User user = createUser(club);
        ReflectionTestUtils.setField(user, "id", 1L);
        ClubApplyForm clubApplyForm = createClubApplyForm(club);
        ReflectionTestUtils.setField(clubApplyForm, "id", 1L);
        Application application = createApplication(user, clubApplyForm, Status.APPROVED);
        ReflectionTestUtils.setField(application, "id", 1L);
        ClubMember clubMember = createClubMember(user, club, application, Role.APPLICANT, ActiveStatus.ACTIVE);
        ReflectionTestUtils.setField(clubMember, "id", 1L);

        given(clubRepository.findById(eq(missingId))).willReturn(Optional.empty());

        //when
        assertThatThrownBy(() -> clubService.getClubDashBoard(missingId))
                .isInstanceOf(ClubNotFoundException.class)
                .hasMessage("해당 동아리가 존재하지 않습니다.");

        verifyNoInteractions(clubApplyFormRepository, clubMemberRepository, applicationRepository);
    }

    @DisplayName("클럽Id를 통해 클럽 상세 정보를 조회할 수 있다.")
    @Test
    void getClubDetail() {
        //given
        Long clubId = 1L;
        ClubIntroduction clubIntroduction = createClubIntroduction();
        ReflectionTestUtils.setField(clubIntroduction, "id", 1L);
        ClubImage image = createClubImage(clubIntroduction);
        clubIntroduction.addImage(image);
        ReflectionTestUtils.setField(image, "id", 1L);
        Club club = createClub(clubIntroduction, LocalDateTime.of(2025, 9, 3, 0, 0), LocalDateTime.of(2025, 9, 20, 23, 59));
        ReflectionTestUtils.setField(club, "id", 1L);
        User user = createUser(club);
        ReflectionTestUtils.setField(user, "id", 1L);
        ClubMember clubMember = createClubMember(user, club, mock(Application.class), Role.CLUB_ADMIN, ActiveStatus.ACTIVE);
        ReflectionTestUtils.setField(clubMember, "id", 1L);

        given(clubRepository.findById(eq(clubId))).willReturn(Optional.of(club));
        given(clubMemberRepository.findClubAdminByClubIdAndRole(eq(clubId), eq(Role.CLUB_ADMIN))).willReturn(Optional.of(clubMember));

        ClubDetailResponseDto expect = new ClubDetailResponseDto(
                "카태켐",
                "공7 1호관",
                Category.LITERATURE,
                "함께 배우는 카태켐",
                List.of("image1.url"),
                "overview",
                "activities",
                "ideal",
                "매주 화요일 오후 6시반",
                "모집중",
                "김춘식",
                "010-1234-5678",
                LocalDateTime.of(2025, 9, 3, 0, 0),
                LocalDateTime.of(2025, 9, 20, 23, 59)
        );

        //when
        ClubDetailResponseDto actual = clubService.getClubDetail(clubId);

        //then
        assertThat(actual).isEqualTo(expect);
        verify(clubRepository).findById(eq(clubId));
        verify(clubMemberRepository).findClubAdminByClubIdAndRole(eq(clubId), eq(Role.CLUB_ADMIN));
    }

    @DisplayName("Club Detail 조회시 존재하지 않는 clubId를 사용할 때 ClubNotFoundException이 실행된다.")
    @Test
    void getClubDetailWithWrongClubId() {
        //given
        Long clubId = 1L;
        Long missingId = 0L;

        ClubIntroduction clubIntroduction = createClubIntroduction();
        ReflectionTestUtils.setField(clubIntroduction, "id", 1L);
        ClubImage image = createClubImage(clubIntroduction);
        clubIntroduction.addImage(image);
        ReflectionTestUtils.setField(image, "id", 1L);
        Club club = createClub(clubIntroduction, LocalDateTime.of(2025, 9, 3, 0, 0), LocalDateTime.of(2025, 9, 20, 23, 59));
        ReflectionTestUtils.setField(club, "id", 1L);
        User user = createUser(club);
        ReflectionTestUtils.setField(user, "id", 1L);
        ClubMember clubMember = createClubMember(user, club, mock(Application.class), Role.CLUB_ADMIN, ActiveStatus.ACTIVE);
        ReflectionTestUtils.setField(clubMember, "id", 1L);

        given(clubRepository.findById(eq(missingId))).willReturn(Optional.empty());

        //when
        assertThatThrownBy(() -> clubService.getClubDetail(missingId))
                .isInstanceOf(ClubNotFoundException.class)
                .hasMessage("해당 동아리가 존재하지 않습니다.");

        //then
        verifyNoInteractions(clubMemberRepository);


    }

    private static ClubImage createClubImage(ClubIntroduction clubIntroduction) {
        return ClubImage.builder()
                .imageUrl("image1.url")
                .clubIntroduction(clubIntroduction).build();
    }

    private static ClubIntroduction createClubIntroduction() {
        return ClubIntroduction.builder()
                .overview("overview")
                .activities("activities")
                .ideal("ideal")
                .build();
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
            ClubIntroduction clubIntroduction, LocalDateTime recruitStart, LocalDateTime recruitEnd) {

        return Club.builder()
                .name("카태켐")
                .category(Category.LITERATURE)
                .location("공7 1호관")
                .shortIntroduction("함께 배우는 카태켐")
                .introduction(clubIntroduction)
                .recruitStart(recruitStart)
                .recruitEnd(recruitEnd)
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

    private static ClubMember createClubMember(User user, Club club, Application application,
            Role role, ActiveStatus active) {
        return ClubMember.builder()
                .user(user)
                .club(club)
                .activeStatus(active)
                .role(role)
                .application(application)
                .build();
    }

    private static Application createApplication(User user, ClubApplyForm clubApplyForm, Status status) {
        return Application.builder()
                .user(user)
                .clubApplyForm(clubApplyForm)
                .status(status)
                .build();
    }



}
