package com.kakaotech.team18.backend_server.global.commandLineRunner;

import com.kakaotech.team18.backend_server.domain.club.entity.Category;
import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import com.kakaotech.team18.backend_server.domain.club.entity.ClubIntroduction;
import com.kakaotech.team18.backend_server.domain.club.repository.ClubRepository;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.entity.ClubApplyForm;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.repository.ClubApplyFormRepository;
import com.kakaotech.team18.backend_server.domain.formQuestion.entity.FieldType;
import com.kakaotech.team18.backend_server.domain.formQuestion.entity.FormQuestion;
import com.kakaotech.team18.backend_server.domain.formQuestion.repository.FormQuestionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Order(1)
@RequiredArgsConstructor
@Profile({"prod", "default"})
public class DataInitializer implements CommandLineRunner {

    private final ClubRepository clubRepository;
    private final ClubApplyFormRepository clubApplyFormRepository;
    private final FormQuestionRepository formQuestionRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (clubRepository.count() > 0) return; // 이미 데이터 있으면 skip

        List<Club> clubs = seedSimpleClubsWithIntroImages();
        seedSimpleApplyForms(clubs);
    }

    /**
     * CLUB + CLUB_INTRODUCTION + CLUB_IMAGE (3 clubs)
     */
    private List<Club> seedSimpleClubsWithIntroImages() {
        List<Club> clubs = new ArrayList<>();

        // Club 1
        ClubIntroduction intro1 = ClubIntroduction.builder()
                .overview("문제를 함께 해결합니다.")
                .activities("스터디 및 프로젝트 운영")
                .ideal("협업과 성장에 열려있는 사람")
                .build();
        intro1.addImages(List.of(
                "https://example.com/img/club01-1.jpg",
                "https://example.com/img/club01-2.jpg"
        ));

        Club club1 = Club.builder()
                .name("인터엑스")
                .category(Category.STUDY)
                .location("공7 201호")
                .shortIntroduction("사회 문제 해결 동아리")
                .introduction(intro1)
                .build();

        // Club 2
        ClubIntroduction intro2 = ClubIntroduction.builder()
                .overview("코딩 실력 향상을 목표로 합니다.")
                .activities("알고리즘 스터디, 해커톤 참가")
                .ideal("학습 욕심 있는 개발자")
                .build();
        intro2.addImages(List.of("https://example.com/img/club02.jpg"));

        Club club2 = Club.builder()
                .name("코드마스터")
                .category(Category.STUDY)
                .location("공5 102호")
                .shortIntroduction("프로그래밍 학습 동아리")
                .introduction(intro2)
                .build();

        // Club 3
        ClubIntroduction intro3 = ClubIntroduction.builder()
                .overview("창의적인 작품 활동 중심")
                .activities("전시회, 작품 제작")
                .ideal("예술 감성과 실행력을 가진 사람")
                .build();
        intro3.addImages(List.of("https://example.com/img/club03.jpg"));

        Club club3 = Club.builder()
                .name("아트픽")
                .category(Category.LITERATURE)
                .location("예술관 301호")
                .shortIntroduction("예술 창작 동아리")
                .introduction(intro3)
                .build();

        clubs.add(club1);
        clubs.add(club2);
        clubs.add(club3);

        return clubRepository.saveAll(clubs);
    }

    /**
     * CLUB_APPLY_FORM + FORM_QUESTION
     */
    private void seedSimpleApplyForms(List<Club> clubs) {
        for (Club club : clubs) {
            ClubApplyForm form = clubApplyFormRepository.save(
                    ClubApplyForm.builder()
                            .club(club)
                            .title(club.getName() + " 2025 상반기 모집")
                            .description("지원서를 작성해주세요.")
                            .build()
            );

            formQuestionRepository.save(FormQuestion.builder()
                    .clubApplyForm(form)
                    .question("지원 동기를 작성해주세요.")
                    .fieldType(FieldType.TEXT)
                    .isRequired(true)
                    .displayOrder(1L)
                    .build());

            formQuestionRepository.save(FormQuestion.builder()
                    .clubApplyForm(form)
                    .question("자기소개를 해주세요.")
                    .fieldType(FieldType.TEXT)
                    .isRequired(true)
                    .displayOrder(2L)
                    .build());
            formQuestionRepository.save(FormQuestion.builder()
                    .clubApplyForm(form)
                    .question("관련 경험이 있으신가요?")
                    .fieldType(FieldType.RADIO)
                    .options(List.of("예", "아니오"))
                    .isRequired(true)
                    .displayOrder(3L)
                    .build());
            formQuestionRepository.save(FormQuestion.builder()
                    .clubApplyForm(form)
                    .question("면접 가능 시간을 선택해주세요.")
                    .fieldType(FieldType.TIME_SLOT)
                    .isRequired(true)
                    .displayOrder(4L)
                    .build());
        }
    }
}