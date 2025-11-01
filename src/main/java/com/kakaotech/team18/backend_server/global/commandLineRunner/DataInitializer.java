package com.kakaotech.team18.backend_server.global.commandLineRunner;

import com.kakaotech.team18.backend_server.domain.answer.entity.Answer;
import com.kakaotech.team18.backend_server.domain.answer.repository.AnswerRepository;
import com.kakaotech.team18.backend_server.domain.application.entity.Application;
import com.kakaotech.team18.backend_server.domain.application.entity.Stage;
import com.kakaotech.team18.backend_server.domain.application.entity.Status;
import com.kakaotech.team18.backend_server.domain.application.repository.ApplicationRepository;
import com.kakaotech.team18.backend_server.domain.club.entity.*;
import com.kakaotech.team18.backend_server.domain.club.repository.ClubRepository;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.entity.ClubApplyForm;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.repository.ClubApplyFormRepository;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.ActiveStatus;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.ClubMember;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.Role;
import com.kakaotech.team18.backend_server.domain.clubMember.repository.ClubMemberRepository;
import com.kakaotech.team18.backend_server.domain.clubReview.entity.ClubReview;
import com.kakaotech.team18.backend_server.domain.clubReview.repository.ClubReviewRepository;
import com.kakaotech.team18.backend_server.domain.comment.entity.Comment;
import com.kakaotech.team18.backend_server.domain.comment.repository.CommentRepository;
import com.kakaotech.team18.backend_server.domain.formQuestion.entity.FieldType;
import com.kakaotech.team18.backend_server.domain.formQuestion.entity.FormQuestion;
import com.kakaotech.team18.backend_server.domain.formQuestion.entity.TimeSlotOption;
import com.kakaotech.team18.backend_server.domain.formQuestion.repository.FormQuestionRepository;
import com.kakaotech.team18.backend_server.domain.notices.entity.Notice;
import com.kakaotech.team18.backend_server.domain.notices.repository.NoticeRepository;
import com.kakaotech.team18.backend_server.domain.user.entity.User;
import com.kakaotech.team18.backend_server.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Component
@Order(1)
@RequiredArgsConstructor
@Profile({"prod", "default"})
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ClubRepository clubRepository;
    private final ClubApplyFormRepository clubApplyFormRepository;
    private final FormQuestionRepository formQuestionRepository;
    private final ApplicationRepository applicationRepository;
    private final AnswerRepository answerRepository;
    private final CommentRepository commentRepository;
    private final ClubMemberRepository clubMemberRepository;
    private final ClubReviewRepository clubReviewRepository;
    private final NoticeRepository noticeRepository;

    @Override
    @Transactional
    public void run(String... args) {
        // 이미 데이터가 있으면 아무 것도 하지 않음 (재기동 안전)
        if (userRepository.count() > 0 || clubRepository.count() > 0) return;

        // 1) USERS (64) — data.sql.disabled 패턴 그대로 생성
        List<User> users = seedUsers();

        // 2) CLUB_INTRODUCTION + CLUB_IMAGE (20) & 3) CLUB (20)
        List<Club> clubs = seedClubsWithIntroductionsAndImages();

        // 5) CLUB_APPLY_FORM (20)
        Map<Club, ClubApplyForm> formByClub = seedClubApplyForms(clubs);

        // 6) FORM_QUESTION (+ TIMESLOT for club#1 form)
        Map<ClubApplyForm, List<FormQuestion>> questionsByForm = seedFormQuestions(formByClub);

        // 7) APPLICATION (대량, data.sql.disabled 패턴/값 재현)
        List<Application> applications = seedApplications(users, formByClub);

        // 8) ANSWER (패턴 기반 — form#1 은 4문항, 그 외 폼은 1문항)
        seedAnswers(applications, questionsByForm);

        // 9) COMMENT (44개)
        seedComments(users, applications);

        // 10) CLUB_MEMBER (다량 — 오타 'CLUB_Member'는 CLUB_MEMBER로 정규화)
        seedClubMembers(users, clubs, applications);

        // 12) CLUB_REVIEW (3) — club#1
        seedClubReviews(clubs);

        // 13) NOTICE (20)
        seedNotices();
    }

    /* ---------------------------- USERS ---------------------------- */

    private List<User> seedUsers() {
        List<User> all = new ArrayList<>();
        // 1..64 생성 (특정 인덱스만 커스텀 메일/이름)
        Map<Integer, String> specialEmail = Map.of(
                25, "chsick9@gmail.com",
                27, "welkin@naver.com",
                29, "artjin@example.com"
        );
        Map<Integer, String> specialName = Map.of(
                25, "김춘식",
                27, "이상현",
                29, "박예진"
        );
        Map<Integer, String> specialPhone = Map.of(
                25, "010-5182-7384",
                27, "010-1557-8848",
                29, "010-8765-4321"
        );
        String[] departments = {
                "Computer Science","Electrical Engineering","Mathematics","Physics","Chemistry","Biology","Statistics",
                "Business","Economics","Design","Media","Education","Philosophy","Sociology","History","AI","Data Science",
                "Civil Engineering","Architecture","Korean Literature"
        };

        for (int i = 1; i <= 64; i++) {
            String email = specialEmail.getOrDefault(i, String.format("user%02d@example.com", i));
            String name = specialName.getOrDefault(i, "홍길동" + String.format("%02d", i));
            String phone = specialPhone.getOrDefault(i, "010-0000-" + String.format("%04d", i));
            String dept = departments[(i - 1) % departments.length];
            String studentId = "2025" + String.format("%04d", i);

            User u = User.builder()
                    .kakaoId((long) (10000 + i))
                    .email(email)
                    .name(name)
                    .studentId(studentId)
                    .phoneNumber(phone)
                    .department(dept)
                    .build();
            all.add(u);
        }
        return userRepository.saveAll(all);
    }

    /* ---------------------------- CLUBS + INTRO + IMAGES ---------------------------- */

    private List<Club> seedClubsWithIntroductionsAndImages() {
        List<Club> clubs = new ArrayList<>();

        // 20개의 ClubIntroduction용 베이스 텍스트 (data.sql과 동일/유사)
        String overview1 = "인터엑스는 사회 문제를 깊이 있게 탐구하고 이를 해결하기 위해 다양한 활동을 기획하는 동아리입니다. 회원들은 토론, 조사, 캠페인 등을 통해 실제 사회 문제를 이해하고, 문제 해결을 위한 창의적 방법을 모색합니다. 학문적 연구와 실질적 활동을 병행하며, 서로의 생각을 존중하고 협력하는 문화를 지향합니다.";
        String activities1 = "매주 세미나와 그룹 토론, 지역 사회 봉사활동, 캠페인 기획 및 참여. 관심 분야 프로젝트를 진행하고 발표/보고서로 성과 공유. 외부 전문가 초청 강연으로 실천 가능한 해결책 모색.";
        String ideal1 = "성실하고 책임감 있으며 문제 해결에 관심이 많고 창의적 아이디어를 공유하는 인재. 팀과 협력하며 꾸준히 학습/성장하려는 자세.";

        // 1) Club 1: 인터엑스 (상세 intro + 이미지 4장)
        ClubIntroduction intro1 = ClubIntroduction.builder()
                .overview(overview1)
                .activities(activities1)
                .ideal(ideal1)
                .build();
        intro1.updateImages(List.of(
                "https://plus.unsplash.com/premium_photo-1729880132913-4ca7d67f8eeb?q=80&w=1587",
                "https://plus.unsplash.com/premium_photo-1723917604890-418aa2307d2f?q=80&w=1470",
                "https://plus.unsplash.com/premium_photo-1704756437707-e9fee5c04bcf?q=80&w=1470",
                "https://plus.unsplash.com/premium_photo-1704756437647-559e43344877?q=80&w=1470"
        ));

        Club club1 = Club.builder()
                .name("인터엑스")
                .category(Category.STUDY)
                .location("공7 201호")
                .shortIntroduction("사회문제에 관심 있는 사람들을 위한 동아리")
                .introduction(intro1)
                .caution("현재 지원은 휴학생을 제외한 1~3학년만 받고 있습니다.")
                .recruitStart(LocalDateTime.of(2025, 9, 3, 0, 0))
                .recruitEnd(LocalDateTime.of(2025, 11, 30, 23, 59))
                .regularMeetingInfo("매주 화요일 오후 6시")
                .build();
        clubs.add(club1);

        // 2)~20) 나머지 intro/club — data.sql.disabled 패턴 재현
        String[] names = {
                "클럽 02","코드마스터","클럽 04","아트픽","클럽 06","클럽 07","클럽 08","클럽 09","클럽 10",
                "클럽 11","클럽 12","클럽 13","클럽 14","클럽 15","클럽 16","클럽 17","클럽 18","클럽 19","클럽 20"
        };
        Category[] cats = {
                Category.LITERATURE, Category.STUDY, Category.RELIGION, Category.LITERATURE, Category.STUDY,
                Category.LITERATURE, Category.VOLUNTEER, Category.RELIGION, Category.SPORTS, Category.STUDY,
                Category.LITERATURE, Category.VOLUNTEER, Category.RELIGION, Category.SPORTS, Category.STUDY,
                Category.LITERATURE, Category.VOLUNTEER, Category.RELIGION, Category.SPORTS
        };
        String[] locs = {
                "A-102","공5 102호","A-104","예술관 301호","A-106","A-107","A-108","A-109","A-110",
                "A-111","A-112","A-113","A-114","A-115","A-116","A-117","A-118","A-119","A-120"
        };
        String[] shorts = {
                "소개 02","프로그래밍과 최신 기술을 탐구하는 동아리","소개 04","창작 활동과 전시를 즐기는 예술 동아리","소개 06",
                "소개 07","소개 08","소개 09","소개 10","소개 11","소개 12","소개 13","소개 14","소개 15","소개 16","소개 17","소개 18","소개 19","소개 20"
        };
        String[] cautions = {
                "주의사항","1~4학년 모두 지원 가능합니다.","주의사항","모든 학년 지원 가능, 특별한 조건 없음.","주의사항","주의사항","주의사항",
                "주의사항","주의사항","주의사항","주의사항","주의사항","주의사항","주의사항","주의사항","주의사항","주의사항","주의사항","주의사항"
        };
        LocalDateTime[] start = {
                dt(2025,3,2,9,0), dt(2025,9,5,0,0), dt(2025,3,4,9,0), dt(2025,9,7,0,0), dt(2025,3,6,9,0),
                dt(2025,3,7,9,0), dt(2025,3,8,9,0), dt(2025,3,9,9,0), dt(2025,3,10,9,0), dt(2025,3,11,9,0),
                dt(2025,3,12,9,0), dt(2025,3,13,9,0), dt(2025,3,14,9,0), dt(2025,3,15,9,0), dt(2025,3,16,9,0),
                dt(2025,3,17,9,0), dt(2025,3,18,9,0), dt(2025,3,19,9,0), dt(2025,3,20,9,0)
        };
        LocalDateTime[] end = {
                dt(2025,3,16,18,0), dt(2025,11,30,23,59), dt(2025,3,18,18,0), dt(2025,11,30,23,59), dt(2025,3,20,18,0),
                dt(2027,3,21,18,0), dt(2025,3,22,18,0), dt(2025,3,23,18,0), dt(2025,3,24,18,0), dt(2025,3,25,18,0),
                dt(2025,3,26,18,0), dt(2025,3,27,18,0), dt(2025,3,28,18,0), dt(2025,3,29,18,0), dt(2025,3,30,18,0),
                dt(2025,3,31,18,0), dt(2025,4,1,18,0), dt(2025,4,2,18,0), dt(2027,4,3,18,0)
        };
        String[] meetings = {
                "매주 화 19:00","매주 수요일 오후 7시","매주 목 19:00","매주 금요일 오후 5시","매주 월 19:00",
                "매주 화 19:00","매주 수 19:00","매주 목 19:00","매주 금 19:00","매주 월 19:00","매주 화 19:00",
                "매주 수 19:00","매주 목 19:00","매주 금 19:00","매주 월 19:00","매주 화 19:00","매주 수 19:00","매주 목 19:00","매주 금 19:00"
        };

        // 각 intro 간단문구 + 이미지(일부 4장)
        for (int i = 0; i < 19; i++) {
            int idx = i + 2; // club id 의미상 2..20
            ClubIntroduction intro = ClubIntroduction.builder()
                    .overview((idx == 3) ? "코드마스터는 최신 프로그래밍 언어와 프레임워크를 학습하고 팀 단위 프로젝트를 통해 실무 경험을 쌓는 동아리입니다. 개발 관련 세미나와 스터디를 통해 기술 역량을 지속적으로 향상시킵니다."
                            : (idx == 5) ? "아트픽은 회원들이 창작 활동을 통해 작품을 제작하고 전시회를 통해 공유하는 것을 목표로 합니다. 서로의 작품을 감상/피드백하며 예술적 감각을 향상시킵니다."
                                    : "개요 " + String.format("%02d", idx))
                    .activities((idx == 3) ? "매주 코드 리뷰 세션, 알고리즘 스터디, 팀 프로젝트. 프로젝트 결과물 공유 및 문제 해결 협업. 외부 개발자 초청 강연으로 최신 기술 동향 습득."
                            : (idx == 5) ? "회화/조각/사진/영상 등 다양한 창작 프로젝트 진행. 정기 전시회 개최 및 평가/토론. 외부 예술가 워크숍으로 기법/표현 방식 학습."
                                    : "활동 " + String.format("%02d", idx))
                    .ideal((idx == 3) ? "적극적으로 배우고 실습할 의지가 있으며 새로운 기술 탐구와 협업에 열려 있는 인재."
                            : (idx == 5) ? "창의적이고 꾸준히 작품 활동을 이어갈 수 있으며 협력·성장 의지가 있는 인재."
                                    : "이상 " + String.format("%02d", idx))
                    .build();

            // 이미지 — 3,5는 4장, 나머지는 1장
            if (idx == 3) {
                intro.updateImages(List.of(
                        "https://images.unsplash.com/photo-1622675363311-3e1904dc1885?w=1600&auto=format&fit=crop&q=60&ixlib=rb-4.1.0",
                        "https://images.unsplash.com/photo-1637073849667-91120a924221?w=1600&auto=format&fit=crop&q=60&ixlib=rb-4.1.0",
                        "https://plus.unsplash.com/premium_photo-1683134153517-32015af21911?w=1600&auto=format&fit=crop&q=60&ixlib=rb-4.1.0",
                        "https://plus.unsplash.com/premium_photo-1704756437647-559e43344877?q=80&w=1470"
                ));
            } else if (idx == 5) {
                intro.updateImages(List.of(
                        "https://plus.unsplash.com/premium_photo-1729880132913-4ca7d67f8eeb?q=80&w=1587",
                        "https://plus.unsplash.com/premium_photo-1723917604890-418aa2307d2f?q=80&w=1470",
                        "https://plus.unsplash.com/premium_photo-1704756437707-e9fee5c04bcf?q=80&w=1470",
                        "https://plus.unsplash.com/premium_photo-1704756437647-559e43344877?q=80&w=1470"
                ));
            } else {
                intro.updateImages(List.of("https://example.com/img/club" + String.format("%02d", idx) + ".jpg"));
            }

            Club club = Club.builder()
                    .name(names[i])
                    .category(cats[i])
                    .location(locs[i])
                    .shortIntroduction(shorts[i])
                    .introduction(intro)
                    .caution(cautions[i])
                    .recruitStart(start[i])
                    .recruitEnd(end[i])
                    .regularMeetingInfo(meetings[i])
                    .build();
            clubs.add(club);
        }

        return clubRepository.saveAll(clubs);
    }

    /* ---------------------------- FORMS ---------------------------- */

    private Map<Club, ClubApplyForm> seedClubApplyForms(List<Club> clubs) {
        Map<Club, ClubApplyForm> map = new LinkedHashMap<>();
        for (int i = 0; i < clubs.size(); i++) {
            Club c = clubs.get(i);
            ClubApplyForm f = ClubApplyForm.builder()
                    .club(c)
                    .title((i == 0) ? "클럽01 2025 상반기 모집" : String.format("클럽%02d 2025 상반기 모집", i + 1))
                    .description("소개 및 지원 요강")
                    .build();
            map.put(c, f);
        }
        clubApplyFormRepository.saveAll(map.values());
        return map;
    }

    private Map<ClubApplyForm, List<FormQuestion>> seedFormQuestions(Map<Club, ClubApplyForm> formByClub) {
        Map<ClubApplyForm, List<FormQuestion>> map = new LinkedHashMap<>();
        for (Map.Entry<Club, ClubApplyForm> e : formByClub.entrySet()) {
            Club club = e.getKey();
            ClubApplyForm form = e.getValue();

            List<FormQuestion> qs = new ArrayList<>();
            if ("인터엑스".equals(club.getName())) {
                // club#1: 4문항 (TEXT, RADIO, TEXT, TIME_SLOT)
                qs.add(formQuestionRepository.save(FormQuestion.builder()
                        .clubApplyForm(form)
                        .question("자기소개를 작성해주세요.")
                        .fieldType(FieldType.TEXT)
                        .isRequired(true)
                        .displayOrder(1L)
                        .options(null)
                        .timeSlotOptions(null)
                        .build()));

                qs.add(formQuestionRepository.save(FormQuestion.builder()
                        .clubApplyForm(form)
                        .question("개발 경험이 있으신가요?")
                        .fieldType(FieldType.RADIO)
                        .isRequired(true)
                        .displayOrder(2L)
                        .options(List.of("예", "아니오"))
                        .timeSlotOptions(null)
                        .build()));

                qs.add(formQuestionRepository.save(FormQuestion.builder()
                        .clubApplyForm(form)
                        .question("자기소개를 간단히 적어주세요.")
                        .fieldType(FieldType.TEXT)
                        .isRequired(false)
                        .displayOrder(3L)
                        .options(null)
                        .timeSlotOptions(null)
                        .build()));

                // TIME_SLOT with 2 options
                TimeSlotOption tso1 = new TimeSlotOption(LocalDate.of(2025,10,15),
                        new TimeSlotOption.TimeRange(LocalTime.of(10,0), LocalTime.of(12,0)));
                TimeSlotOption tso2 = new TimeSlotOption(LocalDate.of(2025,10,16),
                        new TimeSlotOption.TimeRange(LocalTime.of(14,0), LocalTime.of(16,0)));

                qs.add(formQuestionRepository.save(FormQuestion.builder()
                        .clubApplyForm(form)
                        .question("면접가능 날짜는?")
                        .fieldType(FieldType.TIME_SLOT)
                        .isRequired(true)
                        .displayOrder(4L)
                        .options(null)
                        .timeSlotOptions(List.of(tso1, tso2))
                        .build()));
            } else {
                // 다른 클럽: 1문항 TEXT
                qs.add(formQuestionRepository.save(FormQuestion.builder()
                        .clubApplyForm(form)
                        .question("지원 동기를 작성해주세요.")
                        .fieldType(FieldType.TEXT)
                        .isRequired(true)
                        .displayOrder(1L)
                        .options(null)
                        .timeSlotOptions(null)
                        .build()));
            }
            map.put(form, qs);
        }
        return map;
    }

    /* ---------------------------- APPLICATIONS ---------------------------- */

    private List<Application> seedApplications(List<User> users, Map<Club, ClubApplyForm> formByClub) {
        List<Application> apps = new ArrayList<>();

        // 1..20 : (user i, form i)
        Status[] s1 = {Status.PENDING, Status.APPROVED, Status.REJECTED, Status.PENDING, Status.APPROVED,
                Status.REJECTED, Status.PENDING, Status.APPROVED, Status.REJECTED, Status.PENDING,
                Status.APPROVED, Status.REJECTED, Status.PENDING, Status.APPROVED, Status.REJECTED,
                Status.PENDING, Status.APPROVED, Status.REJECTED, Status.PENDING, Status.APPROVED};
        Stage[] st1 = {Stage.INTERVIEW, Stage.FINAL, Stage.INTERVIEW, Stage.FINAL, Stage.INTERVIEW,
                Stage.FINAL, Stage.INTERVIEW, Stage.FINAL, Stage.INTERVIEW, Stage.FINAL,
                Stage.INTERVIEW, Stage.FINAL, Stage.INTERVIEW, Stage.FINAL, Stage.INTERVIEW,
                Stage.FINAL, Stage.INTERVIEW, Stage.FINAL, Stage.INTERVIEW, Stage.FINAL};
        double[] avg1 = {0,4.3,2.5,0,4.1,2.7,0,4.6,2.8,0,4.2,2.9,0,4.4,2.6,0,4.5,2.4,0,4.0};

        List<Club> clubOrdered = clubRepository.findAll(); // 저장 순서상 1..20
        for (int i = 0; i < 20; i++) {
            User u = users.get(i);                 // user(i+1)
            ClubApplyForm f = formByClub.get(clubOrdered.get(i));
            Application a = Application.builder()
                    .user(u)
                    .clubApplyForm(f)
                    .status(s1[i])
                    .stage(st1[i])
                    .build();
            a.updateAverageRating(avg1[i]);
            apps.add(a);
        }

        // 21..44 : 모두 APPROVED, INTERVIEW/FINAL 번갈아, 4.x
        for (int i = 21; i <= 44; i++) {
            int idx = i - 1;
            User u = users.get(idx);
            ClubApplyForm f = formByClub.get(clubOrdered.get((i - 1) % 20));
            Stage stage = (i % 2 == 0) ? Stage.FINAL : Stage.INTERVIEW;
            double rating = 4.0 + ((i % 10) * 0.1);
            Application a = Application.builder()
                    .user(u)
                    .clubApplyForm(f)
                    .status(Status.APPROVED)
                    .stage(stage)
                    .build();
            a.updateAverageRating(Math.round(rating * 10) / 10.0);
            apps.add(a);
        }

        // 45..64 : 모두 club#1 로 지원 (data.sql.disabled 패턴)
        Club firstClub = clubOrdered.get(0);
        ClubApplyForm firstForm = formByClub.get(firstClub);
        Status[] s45 = {Status.PENDING,Status.PENDING,Status.APPROVED,Status.APPROVED,Status.REJECTED,Status.REJECTED,
                Status.PENDING,Status.PENDING,Status.APPROVED,Status.APPROVED,Status.REJECTED,Status.REJECTED,
                Status.PENDING,Status.PENDING,Status.APPROVED,Status.APPROVED,Status.REJECTED,Status.REJECTED,
                Status.PENDING,Status.APPROVED};
        Stage[] st45 = {Stage.INTERVIEW,Stage.FINAL,Stage.INTERVIEW,Stage.FINAL,Stage.INTERVIEW,Stage.FINAL,
                Stage.INTERVIEW,Stage.FINAL,Stage.INTERVIEW,Stage.FINAL,Stage.INTERVIEW,Stage.FINAL,
                Stage.INTERVIEW,Stage.FINAL,Stage.INTERVIEW,Stage.FINAL,Stage.INTERVIEW,Stage.FINAL,
                Stage.INTERVIEW,Stage.FINAL};
        double[] avg45 = {2.0,3.0,4.1,4.5,2.8,2.9,2.0,3.0,4.6,4.7,2.5,2.6,2.0,3.0,4.3,4.8,2.4,2.7,2.0,4.4};

        for (int i = 45; i <= 64; i++) {
            int idx = i - 1;
            Application a = Application.builder()
                    .user(users.get(idx))
                    .clubApplyForm(firstForm)
                    .status(s45[i - 45])
                    .stage(st45[i - 45])
                    .build();
            a.updateAverageRating(avg45[i - 45]);
            apps.add(a);
        }

        return applicationRepository.saveAll(apps);
    }

    /* ---------------------------- ANSWERS ---------------------------- */

    private void seedAnswers(List<Application> apps, Map<ClubApplyForm, List<FormQuestion>> questionsByForm) {
        List<Answer> answers = new ArrayList<>();

        for (Application a : apps) {
            List<FormQuestion> qs = questionsByForm.get(a.getClubApplyForm());
            // club#1 은 4문항, 그 외에는 1문항
            if (qs.size() == 4) {
                answers.add(Answer.builder().application(a).formQuestion(qs.get(0))
                        .answer("안녕하세요, 자기소개 01입니다. 백엔드에 관심이 많습니다.").build());
                answers.add(Answer.builder().application(a).formQuestion(qs.get(1))
                        .answer((a.getUser().getId() % 2 == 0) ? "예" : "아니오").build());
                answers.add(Answer.builder().application(a).formQuestion(qs.get(2))
                        .answer("간단 소개: 자바/Spring 학습 중").build());
                answers.add(Answer.builder().application(a).formQuestion(qs.get(3))
                        .answer((a.getUser().getId() % 2 == 0) ? "2025-10-16 14:00~16:00" : "2025-10-15 10:00~12:00").build());
            } else {
                answers.add(Answer.builder().application(a).formQuestion(qs.get(0))
                        .answer("지원 동기: 열심히 활동하겠습니다.").build());
            }
        }

        // data.sql에 있는 특정 추가 응답들(21~64 일부)은 위 로직으로 자연히 커버되도록 설계
        answerRepository.saveAll(answers);
    }

    /* ---------------------------- COMMENTS ---------------------------- */

    private void seedComments(List<User> users, List<Application> apps) {
        List<Comment> cs = new ArrayList<>();
        double[] rating = {
                4.5,3.8,4.2,4.0,3.7,4.6,4.1,3.9,4.7,4.3,4.8,3.6,4.4,4.1,3.5,4.9,4.2,3.8,4.0,4.6,
                4.2,4.0,4.5,3.8,4.7,4.3,3.9,4.8,4.2,3.6,4.5,4.0,4.9,4.1,3.7,4.4,4.8,4.0,3.9,4.6,4.2,3.8,4.5,4.7
        };
        for (int i = 0; i < 44 && i < apps.size(); i++) {
            Application a = apps.get(i);
            User commenter = users.get((i + 1) % users.size()); // ((i % 20)+1) 유사
            cs.add(Comment.builder()
                    .application(a)
                    .user(commenter)
                    .content(String.format("코멘트 %02d", i + 1))
                    .rating(rating[i])
                    .build());
        }
        commentRepository.saveAll(cs);
    }

    /* ---------------------------- CLUB MEMBERS ---------------------------- */

    private void seedClubMembers(List<User> users, List<Club> clubs, List<Application> apps) {
        List<ClubMember> cms = new ArrayList<>();

        // 1..20 : user i, club i, application i (active: 홀수 INACTIVE, 짝수 ACTIVE)
        Role[] firstRoles = {
                Role.CLUB_MEMBER, Role.CLUB_EXECUTIVE, Role.CLUB_MEMBER, Role.APPLICANT, Role.SYSTEM_ADMIN,
                Role.CLUB_MEMBER, Role.CLUB_EXECUTIVE, Role.CLUB_MEMBER, Role.APPLICANT, Role.SYSTEM_ADMIN,
                Role.CLUB_MEMBER, Role.CLUB_EXECUTIVE, Role.CLUB_MEMBER, Role.APPLICANT, Role.SYSTEM_ADMIN,
                Role.CLUB_MEMBER, Role.CLUB_EXECUTIVE, Role.CLUB_MEMBER, Role.APPLICANT, Role.SYSTEM_ADMIN
        };
        for (int i = 0; i < 20; i++) {
            ActiveStatus status = ((i + 1) % 2 == 0) ? ActiveStatus.ACTIVE : ActiveStatus.INACTIVE;
            cms.add(ClubMember.builder()
                    .user(users.get(i))
                    .club(clubs.get(i))
                    .activeStatus(status)
                    .application(apps.get(i))
                    .role(firstRoles[i])
                    .build());
        }

        // 21..44 : 모두 ACTIVE, CLUB_ADMIN
        for (int i = 21; i <= 44; i++) {
            cms.add(ClubMember.builder()
                    .user(users.get(i - 1))
                    .club(clubs.get((i - 1) % 20))
                    .activeStatus(ActiveStatus.ACTIVE)
                    .application(apps.get(i - 1))
                    .role(Role.CLUB_ADMIN)
                    .build());
        }

        // 45..64 : 모두 club#1, ACTIVE, APPLICANT (data.sql.disabled 패턴)
        Club club1 = clubs.get(0);
        for (int i = 45; i <= 64; i++) {
            cms.add(ClubMember.builder()
                    .user(users.get(i - 1))
                    .club(club1)
                    .activeStatus(ActiveStatus.ACTIVE)
                    .application(apps.get(i - 1))
                    .role(Role.APPLICANT) // data.sql의 'CLUB_Member' 오타는 CLUB_MEMBER로 정규화 가능
                    .build());
        }

        clubMemberRepository.saveAll(cms);
    }

    /* ---------------------------- CLUB REVIEWS ---------------------------- */

    private void seedClubReviews(List<Club> clubs) {
        Club c1 = clubs.get(0);
        List<ClubReview> rs = List.of(
                ClubReview.builder().club(c1).content("사회 문제를 다루는 다양한 세미나가 정말 인상 깊었어요. 토론 분위기도 자유롭고 모두가 진지하게 의견을 나누는 모습이 좋았습니다.").writer("20250025").build(),
                ClubReview.builder().club(c1).content("동아리원들끼리의 협업이 잘 되고, 실제 캠페인도 진행해볼 수 있어서 뜻깊은 경험이었습니다. 추천합니다!").writer("20250022").build(),
                ClubReview.builder().club(c1).content("처음에는 낯설었지만 금방 친해지고, 사회문제에 대한 시각이 넓어졌어요. 프로젝트 중심이라 참여감이 높습니다.").writer("20250023").build()
        );
        clubReviewRepository.saveAll(rs);
    }

    /* ---------------------------- NOTICES ---------------------------- */

    private void seedNotices() {
        String[] titles = {
                "학기 초 공지","서버 점검 안내","동아리 등록 마감","신입 회원 OT 일정","홈페이지 개편 안내","지원자 결과 발표",
                "행사 일정 안내","정기회의 일정","사진 공모전","기숙사 점검","안전 교육 안내","장비 대여 규칙","스터디 개설 신청",
                "워크숍 일정 안내","공모전 모집","대회 참가 안내","회의록 업로드","동아리 홍보 영상","출석 체크 공지","운영진 모집"
        };
        String[] contents = {
                "2025학년도 1학기 공지사항입니다. 반드시 확인하세요.","3월 10일 00:00~02:00 시스템 점검 예정입니다.","이번 학기 동아리 등록은 3월 15일 자정에 마감됩니다.",
                "3월 5일 오후 6시, 본관 101호에서 오리엔테이션이 열립니다.","UX 개선을 위한 디자인 리뉴얼 작업이 완료되었습니다.","3월 12일 오후 2시에 이메일로 개별 통보됩니다.",
                "3월 마지막 주 토요일, 교내 축제 부스 운영 예정입니다.","매주 목요일 오후 7시, 학생회관 2층 회의실에서 진행됩니다.","이번 달 25일까지 작품을 제출하면 참가 가능합니다.",
                "기숙사 전기 설비 점검으로 4월 1일 13~15시 정전 예정입니다.","신입생 및 복학생 대상 안전 교육이 필수입니다.","동아리 장비는 사전 예약 후 대여 가능합니다.",
                "4월부터 진행될 스터디 개설 신청을 받습니다.","봄학기 워크숍은 4월 12~13일 양일간 진행됩니다.","교내 AI 아이디어 공모전에 참여하세요!",
                "프로그래밍 경진대회 신청은 이번 주까지입니다.","지난 주 회의 내용이 게시판에 업로드되었습니다.","신입생 환영회용 홍보 영상이 게시되었습니다.",
                "활동 출석 확인은 매주 일요일까지 입력해주세요.","다음 학기 운영진을 모집합니다. 많은 지원 바랍니다."
        };

        List<Notice> list = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            list.add(Notice.builder()
                    .title(titles[i])
                    .content(contents[i])
                    .isAlive(true)
                    .build());
        }
        noticeRepository.saveAll(list);
    }

    /* ---------------------------- UTIL ---------------------------- */

    private static LocalDateTime dt(int y, int m, int d, int hh, int mm) {
        return LocalDateTime.of(y, m, d, hh, mm);
    }
}