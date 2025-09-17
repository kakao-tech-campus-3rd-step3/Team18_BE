package com.kakaotech.team18.backend_server.domain.club.service;

import static com.kakaotech.team18.backend_server.domain.club.entity.Category.LITERATURE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.kakaotech.team18.backend_server.domain.club.dto.ClubDetailResponseDto;
import com.kakaotech.team18.backend_server.domain.club.entity.Category;
import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import com.kakaotech.team18.backend_server.domain.club.entity.ClubImage;
import com.kakaotech.team18.backend_server.domain.club.entity.ClubIntroduction;
import com.kakaotech.team18.backend_server.domain.club.repository.ClubRepository;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.ActiveStatus;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.ClubMember;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.ClubMember.ClubMemberBuilder;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.Role;
import com.kakaotech.team18.backend_server.domain.clubMember.repository.ClubMemberRepository;
import com.kakaotech.team18.backend_server.domain.user.entity.User;
import com.kakaotech.team18.backend_server.domain.user.repository.UserRepository;
import com.kakaotech.team18.backend_server.global.exception.exceptions.ClubNotFoundException;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ClubServiceTest {

    @Autowired
    private ClubService clubService;

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClubMemberRepository clubMemberRepository;


    @DisplayName("클럽Id를 통해 클럽 상세 정보를 조회할 수 있다.")
    @Test
    void getClubDetail() {
        //given
        User user = createUser("김춘식", "ex1", "password1", "ex@gmail.com", "소프트웨어공학과", "123456", "010-1234-5678");
        User savedUser = userRepository.save(user);
        List<String> images = List.of("ex.image1", "ex.image2", "ex.image3", "ex.image4", "ex.image5", "ex.image6");

        Club club = createClub(savedUser, "카태켐", LITERATURE, "공대7호관 201호", "카카오 부트캠프", images,
                "개발자로 성장할 수 있는 부트캠프입니다.", "총 3단계로 이루어진 코스", "열심열심", "매주 화요일 오후 6시");

        Club savedClub = clubRepository.save(club);

        ClubMemberBuilder clubMember = ClubMember.builder()
                .user(savedUser)
                .club(savedClub)
                .activeStatus(ActiveStatus.ACTIVE)
                .role(Role.CLUB_ADMIN);
        clubMemberRepository.save(clubMember.build());

        //when
        ClubDetailResponseDto response = clubService.getClubDetail(savedClub.getId());


        //then
        assertThat(response)
                .extracting("clubName", "location", "category", "shortIntroduction",
                        "introductionImages", "introductionOverview", "introductionActivity",
                        "introductionIdeal", "regularMeetingInfo", "recruitStatus",
                        "presidentName", "presidentPhoneNumber", "recruitStart", "recruitEnd")
                .contains("카태켐", "공대7호관 201호", LITERATURE, "카카오 부트캠프", images,
                        "개발자로 성장할 수 있는 부트캠프입니다.", "총 3단계로 이루어진 코스", "열심열심", "매주 화요일 오후 6시", "모집중",
                        "김춘식", "010-1234-5678", LocalDateTime.of(2025, 9, 3, 0, 0),
                        LocalDateTime.of(2025, 9, 20, 23, 59)
                );
    }

    @DisplayName("Club Detail 조회시 존재하지 않는 clubId를 사용할 때 ClubNotFoundException이 실행된다.")
    @Test
    void getClubDetailWithWrongClubId() {
        //given
        Long wrongClubId = 0L;
        User user = createUser("김춘식", "ex1", "password1", "ex@gmail.com", "소프트웨어공학과", "123456", "010-1234-5678");
        User savedUser = userRepository.save(user);

        Club club = createClub(savedUser, "카태켐", LITERATURE, "공대7호관 201호", "카카오 부트캠프", List.of("ex.image"),
                "개발자로 성장할 수 있는 부트캠프입니다.", "총 3단계로 이루어진 코스", "열심열심", "매주 화요일 오후 6시");

        Club savedClub = clubRepository.save(club);

        //when // then
        assertThatThrownBy(() -> clubService.getClubDetail(wrongClubId))
                .isInstanceOf(ClubNotFoundException.class)
                .hasMessage("해당 동아리가 존재하지 않습니다.");
    }


    private User createUser(String name, String loginId, String password, String email, String department, String studentId, String phoneNumber) {
        return User.builder()
                .name(name)
                .loginId(loginId)
                .password(password)
                .email(email)
                .department(department)
                .studentId(studentId)
                .phoneNumber(phoneNumber)
                .build();
    }


    private Club createClub(
            User president,
            String name,
            Category category,
            String location,
            String shortIntroduction,
            List<String> images,
            String overview,
            String activity,
            String ideal,
            String regularMeetingInfo) {

        ClubIntroduction intro = ClubIntroduction.builder()
                .overview(overview)
                .activities(activity)
                .ideal(ideal)
                .build();

        images.forEach(imageUrl -> {
            ClubImage clubImage = ClubImage.builder()
                    .imageUrl(imageUrl)
                    .clubIntroduction(intro)
                    .build();
            intro.getImages().add(clubImage);
        });

        return Club.builder()
                .name(name)
                .category(category)
                .location(location)
                .shortIntroduction(shortIntroduction)
                .introduction(intro)
                .recruitStart(LocalDateTime.of(2025, 9, 3, 0, 0))
                .recruitEnd(LocalDateTime.of(2025, 9, 20, 23, 59))
                .regularMeetingInfo(regularMeetingInfo)
                .build();
    }

}