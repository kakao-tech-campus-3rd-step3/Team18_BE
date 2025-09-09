package com.kakaotech.team18.backend_server.domain.club.service;

import static com.kakaotech.team18.backend_server.domain.club.entity.Category.LITERATURE;
import static org.assertj.core.api.Assertions.assertThat;

import com.kakaotech.team18.backend_server.domain.club.dto.ClubDetailResponseDto;
import com.kakaotech.team18.backend_server.domain.club.entity.Category;
import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import com.kakaotech.team18.backend_server.domain.club.repository.ClubRepository;
import com.kakaotech.team18.backend_server.domain.user.entity.Users;
import com.kakaotech.team18.backend_server.domain.user.repository.UsersRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
class ClubServiceTest {

    @Autowired
    private ClubService clubService;

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private UsersRepository usersRepository;


    @DisplayName("클럽Id를 통해 클럽 상세 정보를 조회할 수 있다.")
    @Test
    void getClubDetail() {
        //given
        Users user = createUser("김춘식", "ex@gmail.com", "소프트웨어공학과", "123456", "010-1234-5678");
        Users savedUser = usersRepository.save(user);

        Club club = createClub(savedUser, "카태켐", LITERATURE, "공대7호관 201호", "카카오 부트캠프", "ex.image",
                "개발자로 성장할 수 있는 부트캠프입니다.", "총 3단계로 이루어진 코스", "열심열심", "매주 화요일 오후 6시");

        Club savedClub = clubRepository.save(club);

        //when
        ClubDetailResponseDto response = clubService.getClubDetail(savedClub.getId());


        //then
        assertThat(response)
                .extracting("clubName", "location", "category", "shortIntroduction",
                        "introductionImage", "introductionIntroduce", "introductionActivity",
                        "introductionWannabe", "regularMeetingInfo", "recruitStatus",
                        "presidentName", "presidentPhoneNumber", "recruitStart", "recruitEnd")
                .contains("카태켐", "공대7호관 201호", LITERATURE, "카카오 부트캠프", "ex.image",
                        "개발자로 성장할 수 있는 부트캠프입니다.", "총 3단계로 이루어진 코스", "열심열심", "매주 화요일 오후 6시", "모집중",
                        "김춘식", "010-1234-5678", LocalDateTime.of(2025, 9, 3, 0, 0),
                        LocalDateTime.of(2025, 9, 20, 23, 59)
                );

    }



    private Users createUser(String name, String email, String department, String studentId, String phoneNumber) {
        return Users.builder()
                .name(name)
                .email(email)
                .department(department)
                .studentId(studentId)
                .phoneNumber(phoneNumber)
                .build();
    }


    private Club createClub(
            Users president,
            String name,
            Category category,
            String location,
            String shortIntroduction,
            String image,
            String Introduce,
            String activity,
            String wannabe,
            String regularMeetingInfo) {
        return Club.builder()
                .president(president)
                .name(name)
                .category(category)
                .location(location)
                .shortIntroduction(shortIntroduction)
                .introductionImage(image)
                .introductionIntroduce(Introduce)
                .introductionActivity(activity)
                .introductionWannabe(wannabe)
                .recruitStart(LocalDateTime.of(2025, 9, 3, 0, 0))
                .recruitEnd(LocalDateTime.of(2025, 9, 20, 23, 59))
                .regularMeetingInfo(regularMeetingInfo)
                .build();
    }

}