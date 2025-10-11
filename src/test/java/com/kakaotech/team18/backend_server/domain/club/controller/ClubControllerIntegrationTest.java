package com.kakaotech.team18.backend_server.domain.club.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import com.kakaotech.team18.backend_server.domain.club.repository.ClubRepository;
import com.kakaotech.team18.backend_server.domain.email.eventListener.ApplicationNotificationListener;
import com.kakaotech.team18.backend_server.domain.email.sender.SmtpEmailSender;
import com.kakaotech.team18.backend_server.domain.email.service.EmailService;
import com.kakaotech.team18.backend_server.domain.user.entity.User;
import com.kakaotech.team18.backend_server.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;

import static com.kakaotech.team18.backend_server.domain.club.entity.Category.SPORTS;
import static com.kakaotech.team18.backend_server.domain.club.entity.Category.STUDY;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class ClubControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        clubRepository.deleteAll();
        userRepository.deleteAll();

        entityManager.createNativeQuery("ALTER TABLE users ALTER COLUMN user_id RESTART WITH 1").executeUpdate();
        entityManager.createNativeQuery("ALTER TABLE club ALTER COLUMN club_id RESTART WITH 1").executeUpdate();

        User user1 = User.builder()
                .name("회장1")
                .email("president1@test.com")
                .phoneNumber("010-1234-5678")
                .studentId("123456")
                .department("컴퓨터공학과")
                .build();
        userRepository.save(user1);

        User user2 = User.builder()
                .name("회장2")
                .email("president2@test.com")
                .phoneNumber("010-2222-2222")
                .studentId("456789")
                .department("인공지능학부")
                .build();
        userRepository.save(user2);

        Club club1 = Club.builder()
                .name("동아리1")
                .category(STUDY)
                .shortIntroduction("짧은 소개1")
                .location("장소1")
                .build();

        Club club2 = Club.builder()
                .name("동아리2")
                .category(SPORTS)
                .shortIntroduction("짧은 소개2")
                .location("장소2")
                .build();

        clubRepository.save(club1);
        clubRepository.save(club2);

        entityManager.flush();
        entityManager.clear();
    }

    @DisplayName("전체 동아리 목록 조회 통합 테스트")
    @Test
    void getAllClubs_integration_test() throws Exception {
        // given

        // when
        ResultActions resultActions = mockMvc.perform(get("/api/clubs"));

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.clubs.size()").value(2))
                .andExpect(jsonPath("$.clubs[0].id").value(1))
                .andExpect(jsonPath("$.clubs[0].name").value("동아리1"))
                .andExpect(jsonPath("$.clubs[0].category").value("STUDY"))
                .andExpect(jsonPath("$.clubs[1].id").value(2))
                .andExpect(jsonPath("$.clubs[1].name").value("동아리2"))
                .andExpect(jsonPath("$.clubs[1].category").value("SPORTS"))
                .andDo(print());
    }

    @DisplayName("전체 동아리 목록 필터링 조회 통합 테스트")
    @Test
    void getAllClubsByCategory_integration_test() throws Exception {
        // given
        String category = "STUDY";

        // when
        ResultActions resultActions = mockMvc.perform(get("/api/clubs").param("category", category));

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.clubs.size()").value(1))
                .andExpect(jsonPath("$.clubs[0].id").value(1))
                .andExpect(jsonPath("$.clubs[0].name").value("동아리1"))
                .andExpect(jsonPath("$.clubs[0].category").value("STUDY"))
                .andDo(print());
    }


}
