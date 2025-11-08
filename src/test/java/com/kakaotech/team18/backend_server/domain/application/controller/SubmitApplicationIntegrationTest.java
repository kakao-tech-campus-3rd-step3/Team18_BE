package com.kakaotech.team18.backend_server.domain.application.controller;

import static com.kakaotech.team18.backend_server.domain.formQuestion.entity.FieldType.TEXT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kakaotech.team18.backend_server.domain.club.entity.Category;
import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import com.kakaotech.team18.backend_server.domain.club.repository.ClubRepository;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.entity.ClubApplyForm;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.repository.ClubApplyFormRepository;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.ActiveStatus;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.ClubMember;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.Role;
import com.kakaotech.team18.backend_server.domain.clubMember.repository.ClubMemberRepository;
import com.kakaotech.team18.backend_server.domain.formQuestion.entity.FormQuestion;
import com.kakaotech.team18.backend_server.domain.formQuestion.repository.FormQuestionRepository;
import com.kakaotech.team18.backend_server.domain.user.entity.User;
import com.kakaotech.team18.backend_server.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class SubmitApplicationIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ClubRepository clubRepository;

    @Autowired
    ClubApplyFormRepository clubApplyFormRepository;

    @Autowired
    FormQuestionRepository formQuestionRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    private ClubMemberRepository clubMemberRepository;

    Long clubId;

    @BeforeEach
    void setUp() {
        User user = userRepository.save(User.builder()
                .email("admin@example.com")
                .name("동아리 회장")
                .studentId("20230000")
                .phoneNumber("010-0000-0000")
                .department("컴퓨터공학과")
                .build());

        Club club = clubRepository.save(
                Club.builder()
                        .name("Test Club")
                        .location("Test Location")
                        .category(Category.STUDY)
                        .build());
        this.clubId = club.getId();

        clubMemberRepository.save(
                ClubMember.builder()
                        .club(club)
                        .activeStatus(ActiveStatus.ACTIVE)
                        .user(user)
                        .role(Role.CLUB_ADMIN)
                        .build());

        ClubApplyForm clubApplyForm = clubApplyFormRepository.save(
                ClubApplyForm.builder()
                        .club(club)
                        .title("지원폼 제목")
                        .description("설명")
                        .build()
        );

        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(clubApplyForm)
                .question("자기소개")
                .fieldType(TEXT)
                .isRequired(true)
                .displayOrder(1L)
                .build());
    }

    private String payload(String email, String studentId, String phone) {
        return """
        {
          "email":"%s",
          "name":"홍길동",
          "studentId":"%s",
          "phoneNumber":"%s",
          "department":"컴퓨터공학과",
          "answers": [
            {"questionNum":0,"question":"q","answer":"자기소개입니다"}
          ]
        }
        """.formatted(email, studentId, phone);
    }

    @Test
    @DisplayName("이메일 중복 시 → 400 DUPLICATE_EMAIL 반환")
    void duplicateEmail_returns400() throws Exception {
        // 첫 번째 정상 생성
        mockMvc.perform(post("/api/clubs/{clubId}/apply-submit", clubId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload("aaa@example.com", "111111", "010-1111-2222")))
                .andExpect(status().isCreated());

        // 동일 이메일로 다시 생성 → DB 중복 발생 → GlobalExceptionHandler가 400 반환해야 함
        mockMvc.perform(post("/api/clubs/{clubId}/apply-submit", clubId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload("aaa@example.com", "111112","010-3333-4444")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error_code").value("DUPLICATE_EMAIL"));
    }

    @Test
    @DisplayName("전화번호 중복 시 → 400 DUPLICATE_PHONE_NUMBER 반환")
    void duplicatePhone_returns400() throws Exception {
        // 첫 번째 정상 생성
        mockMvc.perform(post("/api/clubs/{clubId}/apply-submit", clubId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload("abc@example.com", "111111","010-5555-6666")))
                .andExpect(status().isCreated());

        // 동일 전화번호로 재등록 → DB 에러 → 400
        mockMvc.perform(post("/api/clubs/{clubId}/apply-submit", clubId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload("xyz@example.com", "111112","010-5555-6666")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error_code").value("DUPLICATE_PHONE_NUMBER"));
    }
}