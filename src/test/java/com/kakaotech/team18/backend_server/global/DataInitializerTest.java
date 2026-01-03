package com.kakaotech.team18.backend_server.global;

import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import com.kakaotech.team18.backend_server.domain.club.repository.ClubRepository;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.entity.ClubApplyForm;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.repository.ClubApplyFormRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Disabled("Datainitializer 프로필에 test 추가하고 실행하세요")
@ActiveProfiles("test") // initializer가 test 프로필에서도 돌도록 설정했을 때
class DataInitializerTest {

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private ClubApplyFormRepository clubApplyFormRepository;

    @DisplayName("애플리케이션 기동 시 DataInitializer가 기본 동아리 데이터를 넣는다")
    @Test
    void dataInitializer_shouldSeedClubs() {
        // when
        long clubCount = clubRepository.count();

        // then
        assertThat(clubCount).isGreaterThan(0);

        Optional<Club> interx = clubRepository.findByName("인터엑스");
        assertThat(interx).isPresent();
    }

    @DisplayName("애플리케이션 기동 시 DataInitializer가 동아리 지원서 양식을 넣는다")
    @Test
    void dataInitializer_shouldSeedClubApplyForms() {
        // when
        long formCount = clubApplyFormRepository.count();

        // then
        assertThat(formCount).isGreaterThan(0);

        Optional<ClubApplyForm> form = clubApplyFormRepository
                .findByTitle("인터엑스 2025 상반기 모집");

        assertThat(form).isPresent();
        assertThat(form.get().getDescription())
                .contains("사회문제 해결과 토론"); // 네가 initializer에 넣은 설명 일부
    }
}

