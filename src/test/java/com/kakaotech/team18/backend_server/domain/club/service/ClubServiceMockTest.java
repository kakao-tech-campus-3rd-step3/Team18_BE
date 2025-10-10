package com.kakaotech.team18.backend_server.domain.club.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.kakaotech.team18.backend_server.domain.application.entity.Application;
import com.kakaotech.team18.backend_server.domain.application.entity.Status;
import com.kakaotech.team18.backend_server.domain.application.repository.ApplicationRepository;
import com.kakaotech.team18.backend_server.domain.club.dto.ClubDashBoardResponseDto;
import com.kakaotech.team18.backend_server.domain.club.dto.ClubDetailRequestDto;
import com.kakaotech.team18.backend_server.domain.club.dto.ClubDetailResponseDto;
import com.kakaotech.team18.backend_server.domain.club.entity.Category;
import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import com.kakaotech.team18.backend_server.domain.club.entity.ClubImage;
import com.kakaotech.team18.backend_server.domain.club.entity.ClubIntroduction;
import com.kakaotech.team18.backend_server.domain.club.repository.ClubRepository;
import com.kakaotech.team18.backend_server.domain.club.util.RecruitStatus;
import com.kakaotech.team18.backend_server.domain.club.util.RecruitStatusCalculator;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.entity.ClubApplyForm;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.repository.ClubApplyFormRepository;
import com.kakaotech.team18.backend_server.domain.clubMember.dto.ApplicantResponseDto;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.ActiveStatus;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.ClubMember;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.Role;
import com.kakaotech.team18.backend_server.domain.clubMember.repository.ClubMemberRepository;
import com.kakaotech.team18.backend_server.domain.user.entity.User;
import com.kakaotech.team18.backend_server.global.dto.SuccessResponseDto;
import com.kakaotech.team18.backend_server.global.exception.exceptions.ClubNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
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
        User user = createUser("loginId", "123456");
        ReflectionTestUtils.setField(user, "id", 1L);
        ClubApplyForm clubApplyForm = createClubApplyForm(club);
        ReflectionTestUtils.setField(clubApplyForm, "id", 1L);
        Application application = createApplication(user, clubApplyForm, Status.PENDING);
        ReflectionTestUtils.setField(application, "id", 1L);
        ClubMember clubMember = createClubMember(user, club, application, Role.APPLICANT, ActiveStatus.ACTIVE);
        ReflectionTestUtils.setField(clubMember, "id", 1L);

        given(clubRepository.findById(eq(clubId))).willReturn(Optional.of(club));
        given(clubApplyFormRepository.findByClubId(eq(club.getId()))).willReturn(Optional.of(clubApplyForm));
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
        verify(clubApplyFormRepository).findByClubId(club.getId());
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
        User user = createUser("loginId", "123456");
        ReflectionTestUtils.setField(user, "id", 1L);
        ClubApplyForm clubApplyForm = createClubApplyForm(club);
        ReflectionTestUtils.setField(clubApplyForm, "id", 1L);
        Application application = createApplication(user, clubApplyForm, Status.APPROVED);
        ReflectionTestUtils.setField(application, "id", 1L);
        ClubMember clubMember = createClubMember(user, club, application, Role.APPLICANT, ActiveStatus.ACTIVE);
        ReflectionTestUtils.setField(clubMember, "id", 1L);

        given(clubRepository.findById(eq(clubId))).willReturn(Optional.of(club));
        given(clubApplyFormRepository.findByClubId(eq(club.getId()))).willReturn(Optional.of(clubApplyForm));
        given(clubMemberRepository.findByClubIdAndRole(eq(clubId), eq(Role.APPLICANT))).willReturn(List.of());
        given(applicationRepository.findByClubApplyFormIdAndStatus(eq(1L), eq(Status.PENDING))).willReturn(List.of());

        ClubDashBoardResponseDto expect = new ClubDashBoardResponseDto(0, 0, "2025-09-03", "2025-09-20", List.of());

        //when
        ClubDashBoardResponseDto actual = clubService.getClubDashBoard(clubId);

        //then
        assertThat(actual).isEqualTo(expect);
        verify(clubRepository).findById(eq(clubId));
        verify(clubApplyFormRepository).findByClubId(club.getId());
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
        User user = createUser("loginId", "123456");
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
        User user = createUser("loginId", "123456");
        ReflectionTestUtils.setField(user, "id", 1L);
        ClubMember clubMember = createClubMember(user, club, mock(Application.class), Role.CLUB_ADMIN, ActiveStatus.ACTIVE);
        ReflectionTestUtils.setField(clubMember, "id", 1L);

        // try-with-resources 구문을 사용하여 테스트가 끝나면 mock이 자동으로 해제되도록 합니다.
        try (MockedStatic<RecruitStatusCalculator> mockedCalculator = Mockito.mockStatic(RecruitStatusCalculator.class)) {
            // given
            // RecruitStatusCalculator.calculate가 어떤 인자로 호출되든 "모집중"을 반환하도록 설정합니다.
            mockedCalculator.when(() -> RecruitStatusCalculator.calculate(club.getRecruitStart(), club.getRecruitEnd()))
                    .thenReturn(RecruitStatus.RECRUITING);

            given(clubRepository.findClubDetailById(eq(clubId))).willReturn(Optional.of(club));
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
                    "모집중", // mock이 반환할 값과 일치
                    "김춘식",
                    "010-1234-5678",
                    LocalDateTime.of(2025, 9, 3, 0, 0),
                    LocalDateTime.of(2025, 9, 20, 23, 59),
                    "주의사항"
            );

            //when
            ClubDetailResponseDto actual = clubService.getClubDetail(clubId);

            //then
            assertThat(actual).isEqualTo(expect);
            verify(clubRepository).findClubDetailById(eq(clubId));
            verify(clubMemberRepository).findClubAdminByClubIdAndRole(eq(clubId), eq(Role.CLUB_ADMIN));
        }
    }

    @DisplayName("Club Detail 조회시 존재하지 않는 clubId를 사용할 때 ClubNotFoundException이 실행된다.")
    @Test
    void getClubDetailWithWrongClubId() {
        //given
        Long missingId = 0L;

        ClubIntroduction clubIntroduction = createClubIntroduction();
        ClubImage image = createClubImage(clubIntroduction);
        clubIntroduction.addImage(image);
        Club club = createClub(clubIntroduction, LocalDateTime.of(2025, 9, 3, 0, 0), LocalDateTime.of(2025, 9, 20, 23, 59));
        User user = createUser( "loginId", "123456");
        ClubMember clubMember = createClubMember(user, club, mock(Application.class), Role.CLUB_ADMIN, ActiveStatus.ACTIVE);

        given(clubRepository.findClubDetailById(eq(missingId))).willReturn(Optional.empty());

        //when
        assertThatThrownBy(() -> clubService.getClubDetail(missingId))
                .isInstanceOf(ClubNotFoundException.class)
                .hasMessage("해당 동아리가 존재하지 않습니다.");

        //then
        verifyNoInteractions(clubMemberRepository);
    }

    @Test
    @DisplayName("동아리 상세 업데이트: 기존 Introduction을 새 값으로 교체 (이미지 포함)")
    void updateClubDetail_replaceIntroduction_allNew() {
        // given
        Club club = sampleClubWithIntroduction(
                "기존동아리", Category.STUDY, "공대 1호관", "old short",
                "old overview", "old activity", "old ideal",
                List.of("old1.jpg", "old2.jpg")
        );

        given(clubRepository.findClubDetailById(1L)).willReturn(Optional.of(club));

        ClubDetailRequestDto dto = ClubDetailRequestDto.builder()
                .clubName("새로운동아리")
                .category(Category.SPORTS)
                .location("인문대 2호관")
                .shortIntroduction("new short")
                .introductionOverview("new overview")
                .introductionActivity("new activity")
                .introductionIdeal("new ideal")
                .introductionImages(List.of("n1.png", "n2.png", "n1.png"))
                .applicationNotices("주의사항")
                .recruitStart(LocalDateTime.of(2025, 10, 1, 0, 0))
                .recruitEnd(LocalDateTime.of(2025, 10, 31, 23, 59))
                .regularMeetingInfo("매주 수 18:00")
                .build();

        // when
        SuccessResponseDto res = clubService.updateClubDetail(1L, dto);

        // then
        assertThat(res.success()).isTrue();

        assertThat(club.getName()).isEqualTo("새로운동아리");
        assertThat(club.getCategory()).isEqualTo(Category.SPORTS);
        assertThat(club.getLocation()).isEqualTo("인문대 2호관");
        assertThat(club.getShortIntroduction()).isEqualTo("new short");
        assertThat(club.getCaution()).isEqualTo("주의사항");
        assertThat(club.getRecruitStart()).isEqualTo(LocalDateTime.of(2025, 10, 1, 0, 0));
        assertThat(club.getRecruitEnd()).isEqualTo(LocalDateTime.of(2025, 10, 31, 23, 59));
        assertThat(club.getRegularMeetingInfo()).isEqualTo("매주 수 18:00");

        // introduction 교체 확인
        assertThat(club.getIntroduction()).isNotNull();
        assertThat(club.getIntroduction().getOverview()).isEqualTo("new overview");
        assertThat(club.getIntroduction().getActivities()).isEqualTo("new activity");
        assertThat(club.getIntroduction().getIdeal()).isEqualTo("new ideal");

        // 이미지 세팅 확인
        assertThat(club.getIntroduction().getImages()).hasSize(3);
        assertThat(club.getIntroduction().getImages())
                .extracting(ClubImage::getImageUrl)
                .containsExactly("n1.png", "n2.png", "n1.png");

        // repository.save 호출이 없다면(더티체킹 전략) 이 검증은 생략 가능
        //then(clubRepository).should().findClubDetailById(1L);
        //then(clubRepository).shouldHaveNoMoreInteractions();
    }
    private Club sampleClubWithIntroduction(
            String name,
            Category category,
            String location,
            String shortIntro,
            String overview,
            String activity,
            String ideal,
            List<String> imageUrls
    ) {
        Club club = Club.builder()
                .name(name)
                .category(category)
                .location(location)
                .shortIntroduction(shortIntro)
                .regularMeetingInfo("수 18:00")
                .build();

        ClubIntroduction intro = ClubIntroduction.builder()
                .overview(overview)
                .activities(activity)
                .ideal(ideal)
                .build();

        for (String url : imageUrls) {
            intro.addImage(ClubImage.builder().imageUrl(url).build());
        }

        club.updateDetail(
                ClubDetailRequestDto.builder()
                        .clubName(name)
                        .category(category)
                        .location(location)
                        .shortIntroduction(shortIntro)
                        .introductionOverview(overview)
                        .introductionActivity(activity)
                        .introductionIdeal(ideal)
                        .introductionImages(imageUrls)
                        .regularMeetingInfo("수 18:00")
                        .build()
        );
        return club;
    }


    @DisplayName("지원자의 지원 상태에 따라 지원자를 필터링해 조회할 수 있다.")
    @ParameterizedTest
    @MethodSource("provideStatusAndClubMembers")
    void viewApplicantsByFilter(Status status, List<ClubMember> mockResult, int expectedSize) {
        // given
        Long clubId = 1L;
        given(clubMemberRepository.findByClubIdAndRoleAndApplicationStatus(
                eq(clubId), eq(Role.APPLICANT), eq(status)))
                .willReturn(mockResult);

        // when
        List<ApplicantResponseDto> actual = clubService.getApplicantsByStatus(clubId, status);

        // then
        assertThat(actual).hasSize(expectedSize);
        assertThat(actual).allMatch(dto -> dto.status().equals(status));
    }

    private static Stream<Arguments> provideStatusAndClubMembers() {
        Club club = createClub(mock(ClubIntroduction.class),
                LocalDateTime.of(2025, 9, 3, 0, 0),
                LocalDateTime.of(2025, 9, 20, 23, 59));
        ClubApplyForm clubApplyForm = createClubApplyForm(club);

        User user1 = createUser("loginId1", "111111");
        User user2 = createUser("loginId2", "222222");
        User user3 = createUser("loginId3", "333333");

        ClubMember clubMember1 = createClubMember(user1, club,
                createApplication(user1, clubApplyForm, Status.PENDING),
                Role.APPLICANT, ActiveStatus.ACTIVE);
        ClubMember clubMember2 = createClubMember(user2, club,
                createApplication(user2, clubApplyForm, Status.PENDING),
                Role.APPLICANT, ActiveStatus.ACTIVE);
        ClubMember clubMember3 = createClubMember(user3, club,
                createApplication(user3, clubApplyForm, Status.REJECTED),
                Role.APPLICANT, ActiveStatus.ACTIVE);

        return Stream.of(
                Arguments.of(Status.PENDING, List.of(clubMember1, clubMember2), 2),
                Arguments.of(Status.REJECTED, List.of(clubMember3), 1),
                Arguments.of(Status.APPROVED, List.of(), 0)
        );
    }

    @DisplayName("지원자들을 조회할 때 필터링 값인 status를 주지 않으면 전체 지원자가 조회된다.")
    @Test
    void viewApplicantsByNoFilter() {
        //given
        Long clubId = 1L;
        Club club = createClub(mock(ClubIntroduction.class), LocalDateTime.of(2025, 9, 3, 0, 0), LocalDateTime.of(2025, 9, 20, 23, 59));
        User user1 = createUser( "loginId1", "111111");
        User user2 = createUser( "loginId2", "222222");
        User user3 = createUser( "loginId3", "333333");
        ClubApplyForm clubApplyForm = createClubApplyForm(club);

        ClubMember clubMember1 = createClubMember(user1, club, createApplication(user1, clubApplyForm, Status.PENDING), Role.APPLICANT, ActiveStatus.ACTIVE);
        ClubMember clubMember2 = createClubMember(user2, club, createApplication(user2, clubApplyForm, Status.APPROVED), Role.APPLICANT, ActiveStatus.ACTIVE);
        ClubMember clubMember3 = createClubMember(user3, club, createApplication(user3, clubApplyForm, Status.REJECTED), Role.APPLICANT, ActiveStatus.ACTIVE);

        // club -> clubMember -> user -> application
        given(clubMemberRepository.findByClubIdAndRole(eq(clubId), eq(Role.APPLICANT))).willReturn(List.of(clubMember1, clubMember2, clubMember3));

        List<ApplicantResponseDto> expect = List.of(
                new ApplicantResponseDto("김춘식", "111111", "철학과", "010-1234-5678", "123@email.com",
                        Status.PENDING),
                new ApplicantResponseDto("김춘식", "222222", "철학과", "010-1234-5678", "123@email.com",
                        Status.APPROVED),
                new ApplicantResponseDto("김춘식", "333333", "철학과", "010-1234-5678", "123@email.com",
                        Status.REJECTED)
        );

        //when
        List<ApplicantResponseDto> actual = clubService.getApplicantsByStatus(clubId, null);

        //then
        assertThat(actual).isEqualTo(expect);
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


    private static User createUser(String loginId, String studentId) {
        return User.builder()
                .name("김춘식")
                .email("123@email.com")
                .studentId(studentId)
                .phoneNumber("010-1234-5678")
                .department("철학과")
                .build();
    }


    private static Club createClub(
            ClubIntroduction clubIntroduction,
            LocalDateTime recruitStart,
            LocalDateTime recruitEnd
    ) {
        return Club.builder()
                .name("카태켐")
                .category(Category.LITERATURE)
                .location("공7 1호관")
                .shortIntroduction("함께 배우는 카태켐")
                .introduction(clubIntroduction)
                .caution("주의사항")
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

    private static ClubMember createClubMember(User user, Club club, Application application, Role role, ActiveStatus active) {
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
