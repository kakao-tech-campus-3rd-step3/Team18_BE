package com.kakaotech.team18.backend_server.global.commandLineRunner;

import com.kakaotech.team18.backend_server.domain.answer.entity.Answer;
import com.kakaotech.team18.backend_server.domain.answer.repository.AnswerRepository;
import com.kakaotech.team18.backend_server.domain.application.entity.Application;
import com.kakaotech.team18.backend_server.domain.application.entity.Stage;
import com.kakaotech.team18.backend_server.domain.application.entity.Status;
import com.kakaotech.team18.backend_server.domain.application.repository.ApplicationRepository;
import com.kakaotech.team18.backend_server.domain.club.entity.Category;
import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import com.kakaotech.team18.backend_server.domain.club.entity.ClubIntroduction;
import com.kakaotech.team18.backend_server.domain.club.repository.ClubRepository;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.entity.ClubApplyForm;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.repository.ClubApplyFormRepository;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.ActiveStatus;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.ClubMember;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.Role;
import com.kakaotech.team18.backend_server.domain.clubMember.repository.ClubMemberRepository;
import com.kakaotech.team18.backend_server.domain.clubReview.entity.ClubReview;
import com.kakaotech.team18.backend_server.domain.clubReview.repository.ClubReviewRepository;
import com.kakaotech.team18.backend_server.domain.comment.repository.CommentRepository;
import com.kakaotech.team18.backend_server.domain.files.entity.File;
import com.kakaotech.team18.backend_server.domain.files.repository.FileDataRepository;
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

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//@Component
@Order(1)
@RequiredArgsConstructor
@Profile({"prod", "default"})
public class DataInitializer implements CommandLineRunner {

    private final ClubRepository clubRepository;
    private final ClubApplyFormRepository clubApplyFormRepository;
    private final FormQuestionRepository formQuestionRepository;
    private final ApplicationRepository applicationRepository;
    private final ClubMemberRepository clubMemberRepository;
    private final ClubReviewRepository clubReviewRepository;
    private final FileDataRepository  fileDataRepository;
    private final NoticeRepository noticeRepository;
    private final UserRepository userRepository;
    private final AnswerRepository answerRepository;

    @Override
    @Transactional
    public void run(String... args) {
        //if (clubRepository.count() > 0) return; // 이미 데이터 있으면 전체 seed 스킵

        List<User> users = seedUsers();
        List<Club> clubs = seedClubsWithIntroAndImages();
        seedApplyFormsAndQuestions(clubs);
        List<Application> applications = seedApplications();
        seedAnswers();
        seedClubMembers(users, clubs, applications);

        // 새로 추가
        List<Notice> notices = seedNotices();
        seedFiles(notices);
        seedClubReviews(clubs);
    }

    private List<User> seedUsers() {
        //if (userRepository.count() > 0) return userRepository.findAll();

        List<User> users = new ArrayList<>();

        users.add(User.builder()
                .kakaoId(10001L)
                .email("yuna.park@jnu.ac.kr")
                .name("박유나")
                .studentId("20250001")
                .phoneNumber("010-2934-5123")
                .department("Computer Science")
                .build());

        users.add(User.builder()
                .kakaoId(10002L)
                .email("taehyun.kim@jnu.ac.kr")
                .name("김태현")
                .studentId("20250002")
                .phoneNumber("010-9451-2134")
                .department("Electrical Engineering")
                .build());

        users.add(User.builder()
                .kakaoId(10003L)
                .email("sohee.lee@jnu.ac.kr")
                .name("이소희")
                .studentId("20250003")
                .phoneNumber("010-3184-6621")
                .department("Mathematics")
                .build());

        users.add(User.builder()
                .kakaoId(10004L)
                .email("junwoo.choi@jnu.ac.kr")
                .name("최준우")
                .studentId("20250004")
                .phoneNumber("010-5763-2448")
                .department("Physics")
                .build());

        users.add(User.builder()
                .kakaoId(10005L)
                .email("hyerin.jung@jnu.ac.kr")
                .name("정혜린")
                .studentId("20250005")
                .phoneNumber("010-8362-1098")
                .department("Chemistry")
                .build());

        users.add(User.builder()
                .kakaoId(10006L)
                .email("minjae.kang@jnu.ac.kr")
                .name("강민재")
                .studentId("20250006")
                .phoneNumber("010-7142-5567")
                .department("Biology")
                .build());

        users.add(User.builder()
                .kakaoId(10007L)
                .email("jiwon.song@jnu.ac.kr")
                .name("송지원")
                .studentId("20250007")
                .phoneNumber("010-9856-3477")
                .department("Statistics")
                .build());

        users.add(User.builder()
                .kakaoId(10008L)
                .email("haneul.yoo@jnu.ac.kr")
                .name("유하늘")
                .studentId("20250008")
                .phoneNumber("010-2634-0985")
                .department("Business Administration")
                .build());

        users.add(User.builder()
                .kakaoId(10009L)
                .email("seungmin.han@jnu.ac.kr")
                .name("한승민")
                .studentId("20250009")
                .phoneNumber("010-6432-7744")
                .department("Economics")
                .build());

        users.add(User.builder()
                .kakaoId(10010L)
                .email("ara.kim@jnu.ac.kr")
                .name("김아라")
                .studentId("20250010")
                .phoneNumber("010-1112-9983")
                .department("Design")
                .build());

        users.add(User.builder()
                .kakaoId(10011L)
                .email("gyuri.park@jnu.ac.kr")
                .name("박규리")
                .studentId("20250011")
                .phoneNumber("010-8654-3339")
                .department("Media & Communication")
                .build());

        users.add(User.builder()
                .kakaoId(10012L)
                .email("donghyun.lee@jnu.ac.kr")
                .name("이동현")
                .studentId("20250012")
                .phoneNumber("010-2854-7722")
                .department("Education")
                .build());

        users.add(User.builder()
                .kakaoId(10013L)
                .email("eunji.jo@jnu.ac.kr")
                .name("조은지")
                .studentId("20250013")
                .phoneNumber("010-9934-1155")
                .department("Philosophy")
                .build());

        users.add(User.builder()
                .kakaoId(10014L)
                .email("suhyun.kwon@jnu.ac.kr")
                .name("권수현")
                .studentId("20250014")
                .phoneNumber("010-4257-2299")
                .department("Sociology")
                .build());

        users.add(User.builder()
                .kakaoId(10015L)
                .email("haeun.cho@jnu.ac.kr")
                .name("조하은")
                .studentId("20250015")
                .phoneNumber("010-6712-5543")
                .department("History")
                .build());

        users.add(User.builder()
                .kakaoId(10016L)
                .email("junseo.yang@jnu.ac.kr")
                .name("양준서")
                .studentId("20250016")
                .phoneNumber("010-9832-4711")
                .department("Artificial Intelligence")
                .build());

        users.add(User.builder()
                .kakaoId(10017L)
                .email("jihye.yoon@jnu.ac.kr")
                .name("윤지혜")
                .studentId("20250017")
                .phoneNumber("010-5623-7812")
                .department("Data Science")
                .build());

        users.add(User.builder()
                .kakaoId(10018L)
                .email("byungwoo.kim@jnu.ac.kr")
                .name("김병우")
                .studentId("20250018")
                .phoneNumber("010-7356-1985")
                .department("Civil Engineering")
                .build());

        users.add(User.builder()
                .kakaoId(10019L)
                .email("sumin.hwang@jnu.ac.kr")
                .name("황수민")
                .studentId("20250019")
                .phoneNumber("010-2178-3345")
                .department("Architecture")
                .build());

        users.add(User.builder()
                .kakaoId(10020L)
                .email("nayeon.lee@jnu.ac.kr")
                .name("이나연")
                .studentId("20250020")
                .phoneNumber("010-6943-5023")
                .department("Korean Literature")
                .build());

        users.add(User.builder()
                .kakaoId(10021L)
                .email("jaemin.park@jnu.ac.kr")
                .name("박재민")
                .studentId("20250021")
                .phoneNumber("010-2183-6602")
                .department("Computer Science")
                .build());

        users.add(User.builder()
                .kakaoId(10022L)
                .email("jiyoon.kim@jnu.ac.kr")
                .name("김지윤")
                .studentId("20250022")
                .phoneNumber("010-8755-4431")
                .department("Economics")
                .build());

        users.add(User.builder()
                .kakaoId(10023L)
                .email("minwoo.choi@jnu.ac.kr")
                .name("최민우")
                .studentId("20250023")
                .phoneNumber("010-6734-8710")
                .department("Industrial Design")
                .build());

        users.add(User.builder()
                .kakaoId(10024L)
                .email("haerin.yoo@jnu.ac.kr")
                .name("유해린")
                .studentId("20250024")
                .phoneNumber("010-9834-1123")
                .department("Philosophy")
                .build());

        users.add(User.builder()
                .kakaoId(10025L)
                .email("chsick9@gmail.com")
                .name("김춘식")
                .studentId("20250025")
                .phoneNumber("010-5182-7384")
                .department("Electrical Engineering")
                .build());

        users.add(User.builder()
                .kakaoId(10026L)
                .email("dohyun.lee@jnu.ac.kr")
                .name("이도현")
                .studentId("20250026")
                .phoneNumber("010-8321-9499")
                .department("Mechanical Engineering")
                .build());

        users.add(User.builder()
                .kakaoId(10027L)
                .email("welkin@naver.com")
                .name("이상현")
                .studentId("20250027")
                .phoneNumber("010-1557-8848")
                .department("Industrial Engineering")
                .build());

        users.add(User.builder()
                .kakaoId(10028L)
                .email("yejin.kim@jnu.ac.kr")
                .name("김예진")
                .studentId("20250028")
                .phoneNumber("010-6571-2390")
                .department("Computer Science")
                .build());

        users.add(User.builder()
                .kakaoId(10029L)
                .email("artjin@example.com")
                .name("박예진")
                .studentId("20250029")
                .phoneNumber("010-8765-4321")
                .department("Business Administration")
                .build());

        users.add(User.builder()
                .kakaoId(10030L)
                .email("hyunwoo.jung@jnu.ac.kr")
                .name("정현우")
                .studentId("20250030")
                .phoneNumber("010-3421-5512")
                .department("Economics")
                .build());

        users.add(User.builder()
                .kakaoId(10031L)
                .email("suhyeon.lee@jnu.ac.kr")
                .name("이수현")
                .studentId("20250031")
                .phoneNumber("010-6853-9432")
                .department("Artificial Intelligence")
                .build());

        users.add(User.builder()
                .kakaoId(10032L)
                .email("minji.park@jnu.ac.kr")
                .name("박민지")
                .studentId("20250032")
                .phoneNumber("010-9324-8123")
                .department("Data Science")
                .build());

        users.add(User.builder()
                .kakaoId(10033L)
                .email("jiho.kang@jnu.ac.kr")
                .name("강지호")
                .studentId("20250033")
                .phoneNumber("010-4421-7321")
                .department("Psychology")
                .build());

        users.add(User.builder()
                .kakaoId(10034L)
                .email("yujin.son@jnu.ac.kr")
                .name("손유진")
                .studentId("20250034")
                .phoneNumber("010-7712-6254")
                .department("Education")
                .build());

        users.add(User.builder()
                .kakaoId(10035L)
                .email("soobin.ahn@jnu.ac.kr")
                .name("안수빈")
                .studentId("20250035")
                .phoneNumber("010-9863-2541")
                .department("Visual Design")
                .build());

        users.add(User.builder()
                .kakaoId(10036L)
                .email("dongyeon.kim@jnu.ac.kr")
                .name("김동연")
                .studentId("20250036")
                .phoneNumber("010-3511-7843")
                .department("Statistics")
                .build());

        users.add(User.builder()
                .kakaoId(10037L)
                .email("yeona.han@jnu.ac.kr")
                .name("한연아")
                .studentId("20250037")
                .phoneNumber("010-2938-1599")
                .department("Chemistry")
                .build());

        users.add(User.builder()
                .kakaoId(10038L)
                .email("gunwoo.park@jnu.ac.kr")
                .name("박건우")
                .studentId("20250038")
                .phoneNumber("010-7654-8893")
                .department("Civil Engineering")
                .build());

        users.add(User.builder()
                .kakaoId(10039L)
                .email("suhyun.kim@jnu.ac.kr")
                .name("김수현")
                .studentId("20250039")
                .phoneNumber("010-4738-3322")
                .department("Biology")
                .build());

        users.add(User.builder()
                .kakaoId(10040L)
                .email("yuri.lee@jnu.ac.kr")
                .name("이유리")
                .studentId("20250040")
                .phoneNumber("010-5123-9984")
                .department("Physics")
                .build());

        users.add(User.builder()
                .kakaoId(10041L)
                .email("jaeho.cho@jnu.ac.kr")
                .name("조재호")
                .studentId("20250041")
                .phoneNumber("010-8641-2148")
                .department("Architecture")
                .build());

        users.add(User.builder()
                .kakaoId(10042L)
                .email("haeun.kwon@jnu.ac.kr")
                .name("권해은")
                .studentId("20250042")
                .phoneNumber("010-7412-8975")
                .department("Philosophy")
                .build());

        users.add(User.builder()
                .kakaoId(10043L)
                .email("gyubin.kim@jnu.ac.kr")
                .name("김규빈")
                .studentId("20250043")
                .phoneNumber("010-2321-5638")
                .department("Computer Science")
                .build());

        users.add(User.builder()
                .kakaoId(10044L)
                .email("siyoon.han@jnu.ac.kr")
                .name("한시윤")
                .studentId("20250044")
                .phoneNumber("010-4123-9921")
                .department("Economics")
                .build());

        users.add(User.builder()
                .kakaoId(10045L)
                .email("jinho.park@jnu.ac.kr")
                .name("박진호")
                .studentId("20250045")
                .phoneNumber("010-7766-4422")
                .department("Computer Science")
                .build());

        users.add(User.builder()
                .kakaoId(10046L)
                .email("minjeong.kim@jnu.ac.kr")
                .name("김민정")
                .studentId("20250046")
                .phoneNumber("010-9123-6233")
                .department("Electrical Engineering")
                .build());

        users.add(User.builder()
                .kakaoId(10047L)
                .email("woojin.choi@jnu.ac.kr")
                .name("최우진")
                .studentId("20250047")
                .phoneNumber("010-2132-8854")
                .department("Mathematics")
                .build());

        users.add(User.builder()
                .kakaoId(10048L)
                .email("seoyoung.yoon@jnu.ac.kr")
                .name("윤서영")
                .studentId("20250048")
                .phoneNumber("010-5511-4343")
                .department("Physics")
                .build());

        users.add(User.builder()
                .kakaoId(10049L)
                .email("taehwan.jang@jnu.ac.kr")
                .name("장태환")
                .studentId("20250049")
                .phoneNumber("010-8344-8823")
                .department("Chemistry")
                .build());

        users.add(User.builder()
                .kakaoId(10050L)
                .email("yebin.kim@jnu.ac.kr")
                .name("김예빈")
                .studentId("20250050")
                .phoneNumber("010-1267-9901")
                .department("Biology")
                .build());

        users.add(User.builder()
                .kakaoId(10051L)
                .email("hyejin.park@jnu.ac.kr")
                .name("박혜진")
                .studentId("20250051")
                .phoneNumber("010-7612-1185")
                .department("Statistics")
                .build());

        users.add(User.builder()
                .kakaoId(10052L)
                .email("junho.yoon@jnu.ac.kr")
                .name("윤준호")
                .studentId("20250052")
                .phoneNumber("010-6254-3712")
                .department("Business Administration")
                .build());

        users.add(User.builder()
                .kakaoId(10053L)
                .email("sua.lee@jnu.ac.kr")
                .name("이수아")
                .studentId("20250053")
                .phoneNumber("010-9931-4741")
                .department("Economics")
                .build());

        users.add(User.builder()
                .kakaoId(10054L)
                .email("minkyu.kim@jnu.ac.kr")
                .name("김민규")
                .studentId("20250054")
                .phoneNumber("010-7483-6214")
                .department("Design")
                .build());

        users.add(User.builder()
                .kakaoId(10055L)
                .email("eunsol.choi@jnu.ac.kr")
                .name("최은솔")
                .studentId("20250055")
                .phoneNumber("010-3412-7725")
                .department("Media Studies")
                .build());

        users.add(User.builder()
                .kakaoId(10056L)
                .email("jinhyuk.kang@jnu.ac.kr")
                .name("강진혁")
                .studentId("20250056")
                .phoneNumber("010-6234-8824")
                .department("Education")
                .build());

        users.add(User.builder()
                .kakaoId(10057L)
                .email("yeseo.park@jnu.ac.kr")
                .name("박예서")
                .studentId("20250057")
                .phoneNumber("010-9732-4122")
                .department("Philosophy")
                .build());

        users.add(User.builder()
                .kakaoId(10058L)
                .email("jiwoo.kim@jnu.ac.kr")
                .name("김지우")
                .studentId("20250058")
                .phoneNumber("010-2745-8811")
                .department("Sociology")
                .build());

        users.add(User.builder()
                .kakaoId(10059L)
                .email("doyoung.han@jnu.ac.kr")
                .name("한도영")
                .studentId("20250059")
                .phoneNumber("010-6321-6612")
                .department("History")
                .build());

        users.add(User.builder()
                .kakaoId(10060L)
                .email("hajin.yoo@jnu.ac.kr")
                .name("유하진")
                .studentId("20250060")
                .phoneNumber("010-8899-5543")
                .department("Artificial Intelligence")
                .build());

        users.add(User.builder()
                .kakaoId(10061L)
                .email("yujin.kang@jnu.ac.kr")
                .name("강유진")
                .studentId("20250061")
                .phoneNumber("010-1442-3338")
                .department("Data Science")
                .build());

        users.add(User.builder()
                .kakaoId(10062L)
                .email("haneul.kim@jnu.ac.kr")
                .name("김하늘")
                .studentId("20250062")
                .phoneNumber("010-3232-8222")
                .department("Civil Engineering")
                .build());

        users.add(User.builder()
                .kakaoId(10063L)
                .email("sihyeon.lee@jnu.ac.kr")
                .name("이시현")
                .studentId("20250063")
                .phoneNumber("010-9123-7765")
                .department("Architecture")
                .build());

        users.add(User.builder()
                .kakaoId(10064L)
                .email("yujin.lim@jnu.ac.kr")
                .name("임유진")
                .studentId("20250064")
                .phoneNumber("010-5534-9332")
                .department("Korean Literature")
                .build());

        users.add(User.builder()
                .kakaoId(10065L)
                .email("gunwoo.jang@jnu.ac.kr")
                .name("장건우")
                .studentId("20250065")
                .phoneNumber("010-7311-4883")
                .department("Civil Engineering")
                .build());

        users.add(User.builder()
                .kakaoId(10066L)
                .email("minji.yoon@jnu.ac.kr")
                .name("윤민지")
                .studentId("20250066")
                .phoneNumber("010-8253-6662")
                .department("Architecture")
                .build());

        users.add(User.builder()
                .kakaoId(10067L)
                .email("suhyun.kim2@jnu.ac.kr")
                .name("김수현")
                .studentId("20250067")
                .phoneNumber("010-9511-7789")
                .department("Korean Literature")
                .build());

        return userRepository.saveAll(users);
    }

    private List<Club> seedClubsWithIntroAndImages() {
        //if (clubRepository.count() > 0) return clubRepository.findAll();

        List<Club> clubs = new ArrayList<>();

        // 1. 인터엑스 (사회문제 탐구)
        ClubIntroduction intro1 = ClubIntroduction.builder()
                .overview("인터엑스는 사회 문제를 깊이 있게 탐구하고 이를 해결하기 위해 다양한 활동을 기획하는 동아리입니다. " +
                        "회원들은 토론, 조사, 캠페인 등을 통해 실제 사회 문제를 이해하고, 문제 해결을 위한 창의적 방법을 모색합니다. " +
                        "학문적 연구와 실질적 활동을 병행하며, 서로의 생각을 존중하고 협력하는 문화를 지향합니다.")
                .activities("매주 세미나와 그룹 토론, 지역 사회 봉사활동, 캠페인 기획 및 참여. " +
                        "관심 분야 프로젝트를 진행하고 발표/보고서로 성과 공유. 외부 전문가 초청 강연으로 실천 가능한 해결책 모색.")
                .ideal("성실하고 책임감 있으며 문제 해결에 관심이 많고 창의적 아이디어를 공유하는 인재. " +
                        "팀과 협력하며 꾸준히 학습/성장하려는 자세.")
                .build();
        intro1.addImages(List.of(
                "https://images.unsplash.com/photo-1556761175-4b46a572b786?w=1600&auto=format&fit=crop&q=60",
                "https://images.unsplash.com/photo-1573497019940-1c28c88b4f3e?w=1600&auto=format&fit=crop&q=60",
                "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=1600&auto=format&fit=crop&q=60",
                "https://images.unsplash.com/photo-1511988617509-a57c8a288659?w=1600&auto=format&fit=crop&q=60"
        ));

        Club club1 = Club.builder()
                .name("인터엑스")
                .category(Category.STUDY)
                .location("공7 201호")
                .shortIntroduction("사회문제 해결을 위한 토론과 프로젝트를 진행하는 학술 동아리")
                .introduction(intro1)
                .recruitStart(LocalDateTime.of(2025, 9, 3, 0, 0, 0))
                .recruitEnd(LocalDateTime.of(2025, 11, 30, 23, 59, 0))
                .regularMeetingInfo("매주 화요일 오후 6시")
                .caution("휴학생 제외, 1~3학년 우대")
                .build();
        clubs.add(club1);

        // 2. 에이아이디브 (AI·데이터 연구)
        ClubIntroduction intro2 = ClubIntroduction.builder()
                .overview("AIDive는 인공지능과 데이터 분석 기술을 탐구하는 학술 동아리입니다. " +
                        "최신 논문을 스터디하고 Kaggle, Dacon과 같은 플랫폼에서 데이터를 분석하며 AI 기술을 실습합니다.")
                .activities("논문 리뷰 세션, Kaggle 대회 팀 참가, TensorFlow/PyTorch 실습, 산업체 연계 세미나 개최.")
                .ideal("논리적 사고력과 꾸준한 학습 태도를 지닌 인재. 새로운 기술을 탐구하고 실무 응용에 관심이 많은 학생.")
                .build();
        intro2.addImages(List.of(
                "https://images.unsplash.com/photo-1556761175-4b46a572b786?w=1600&auto=format&fit=crop&q=60",
                "https://images.unsplash.com/photo-1573497019940-1c28c88b4f3e?w=1600&auto=format&fit=crop&q=60",
                "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=1600&auto=format&fit=crop&q=60",
                "https://images.unsplash.com/photo-1511988617509-a57c8a288659?w=1600&auto=format&fit=crop&q=60"        ));

        Club club2 = Club.builder()
                .name("에이아이디브")
                .category(Category.STUDY)
                .location("공5 202호")
                .shortIntroduction("AI·데이터 분석 연구를 통해 기술을 배우는 학술 동아리")
                .introduction(intro2)
                .recruitStart(LocalDateTime.of(2025, 3, 2, 9, 0, 0))
                .recruitEnd(LocalDateTime.of(2025, 3, 16, 18, 0, 0))
                .regularMeetingInfo("매주 수요일 오후 7시")
                .caution("프로그래밍 기초자 우대")
                .build();
        clubs.add(club2);

        // 3. 코드마스터 (프로그래밍·개발)
        ClubIntroduction intro3 = ClubIntroduction.builder()
                .overview("코드마스터는 최신 프로그래밍 언어와 프레임워크를 학습하고 팀 단위 프로젝트를 통해 실무 경험을 쌓는 개발 중심 동아리입니다.")
                .activities("매주 코드 리뷰 세션, 알고리즘 스터디, 팀 프로젝트 수행, 오픈소스 기여 활동, 해커톤 참여.")
                .ideal("배움에 열정적이며 문제 해결에 적극적인 개발자 지향 인재. 협업과 피드백을 즐기는 태도.")
                .build();
        intro3.addImages(List.of(
                "https://images.unsplash.com/photo-1519389950473-47ba0277781c?w=1600&auto=format&fit=crop&q=60",
                "https://images.unsplash.com/photo-1587620962725-abab7fe55159?w=1600&auto=format&fit=crop&q=60",
                "https://images.unsplash.com/photo-1587620931283-d91fbc3a188b?w=1600&auto=format&fit=crop&q=60",
                "https://images.unsplash.com/photo-1522202176988-66273c2fd55f?w=1600&auto=format&fit=crop&q=60"
        ));

        Club club3 = Club.builder()
                .name("코드마스터")
                .category(Category.STUDY)
                .location("공5 102호")
                .shortIntroduction("프로그래밍과 최신 기술을 함께 공부하고 프로젝트로 실습하는 동아리")
                .introduction(intro3)
                .recruitStart(LocalDateTime.of(2025, 9, 5, 0, 0, 0))
                .recruitEnd(LocalDateTime.of(2025, 11, 30, 23, 59, 0))
                .regularMeetingInfo("매주 수요일 오후 7시")
                .caution("모든 학년 지원 가능")
                .build();
        clubs.add(club3);

        // 4. 로보테크 (로봇공학)
        ClubIntroduction intro4 = ClubIntroduction.builder()
                .overview("로보테크는 하드웨어와 소프트웨어를 융합하여 로봇을 제작하고 제어하는 공학 동아리입니다. " +
                        "자율주행, 드론, 로봇팔 등 다양한 분야를 연구합니다.")
                .activities("ROS 스터디, 라인트레이서 제작, 자율주행 미션 대회 참가, 전시회 출품.")
                .ideal("기계적 구조와 제어에 흥미를 가지고, 꼼꼼하게 문제를 해결하는 인재.")
                .build();
        intro4.addImages(List.of(
                "https://images.unsplash.com/photo-1503387762-592deb58ef4e?w=1600&auto=format&fit=crop&q=60",
                "https://images.unsplash.com/photo-1534723328310-e82dad3ee43f?w=1600&auto=format&fit=crop&q=60",
                "https://images.unsplash.com/photo-1601049676869-702ea24cfd92?w=1600&auto=format&fit=crop&q=60",
                "https://images.unsplash.com/photo-1605559424843-9e4c228bf1c2?w=1600&auto=format&fit=crop&q=60"
        ));

        Club club4 = Club.builder()
                .name("로보테크")
                .category(Category.VOLUNTEER)
                .location("공4 104호")
                .shortIntroduction("로봇 설계와 제어를 함께 배우는 공학 창작 동아리")
                .introduction(intro4)
                .recruitStart(LocalDateTime.of(2025, 3, 4, 9, 0, 0))
                .recruitEnd(LocalDateTime.of(2025, 3, 18, 18, 0, 0))
                .regularMeetingInfo("매주 목요일 오후 6시")
                .caution("기계·전기전자 전공자 우대")
                .build();
        clubs.add(club4);

        // 5. 아트픽 (예술·디자인)
        ClubIntroduction intro5 = ClubIntroduction.builder()
                .overview("아트픽은 회원들이 창작 활동을 통해 작품을 제작하고 전시회를 통해 공유하는 것을 목표로 합니다. " +
                        "서로의 작품을 감상하고 피드백하며 예술적 감각을 향상시킵니다.")
                .activities("회화, 사진, 영상, 일러스트 등 창작 프로젝트 진행. 정기 전시회 개최 및 평가/토론.")
                .ideal("창의적이며 꾸준히 표현 활동을 이어갈 수 있는 인재. 감각과 협동심을 겸비한 학생.")
                .build();
        intro5.addImages(List.of(
                "https://images.unsplash.com/photo-1556761175-4b46a572b786?w=1600&auto=format&fit=crop&q=60",
                "https://images.unsplash.com/photo-1573497019940-1c28c88b4f3e?w=1600&auto=format&fit=crop&q=60",
                "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=1600&auto=format&fit=crop&q=60",
                "https://images.unsplash.com/photo-1511988617509-a57c8a288659?w=1600&auto=format&fit=crop&q=60"        ));

        Club club5 = Club.builder()
                .name("아트픽")
                .category(Category.VOLUNTEER)
                .location("예술관 301호")
                .shortIntroduction("창작 활동과 전시를 중심으로 활동하는 예술 동아리")
                .introduction(intro5)
                .recruitStart(LocalDateTime.of(2025, 9, 7, 0, 0, 0))
                .recruitEnd(LocalDateTime.of(2025, 11, 30, 23, 59, 0))
                .regularMeetingInfo("매주 금요일 오후 5시")
                .caution("학년·전공 무관, 예술 열정만 있으면 OK")
                .build();
        clubs.add(club5);

        // 6. 리버스 (음악·공연)
        ClubIntroduction intro6 = ClubIntroduction.builder()
                .overview("리버스는 밴드와 보컬, 작곡 등 다양한 음악 활동을 중심으로 한 공연 동아리입니다. " +
                        "학교 행사 및 지역 축제에서 무대를 선보입니다.")
                .activities("정기 공연 준비, 합주 연습, 작곡 워크숍, 음원 제작 프로젝트.")
                .ideal("열정적이고 팀워크를 중요시하며 무대에서 에너지를 표현할 수 있는 인재.")
                .build();
        intro6.addImages(List.of(
                "https://images.unsplash.com/photo-1556761175-4b46a572b786?w=1600&auto=format&fit=crop&q=60",
                "https://images.unsplash.com/photo-1573497019940-1c28c88b4f3e?w=1600&auto=format&fit=crop&q=60",
                "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=1600&auto=format&fit=crop&q=60",
                "https://images.unsplash.com/photo-1511988617509-a57c8a288659?w=1600&auto=format&fit=crop&q=60"        ));

        Club club6 = Club.builder()
                .name("리버스")
                .category(Category.RELIGION)
                .location("음악관 B101")
                .shortIntroduction("밴드·보컬·작곡 등 음악 공연을 함께하는 동아리")
                .introduction(intro6)
                .recruitStart(LocalDateTime.of(2025, 3, 6, 9, 0, 0))
                .recruitEnd(LocalDateTime.of(2025, 3, 20, 18, 0, 0))
                .regularMeetingInfo("매주 화요일 오후 6시")
                .caution("오디션 후 합격자 활동 가능")
                .build();
        clubs.add(club6);

        // 7. 포커스 (사진·영상)
        ClubIntroduction intro7 = ClubIntroduction.builder()
                .overview("포커스는 사진과 영상 제작을 통해 시각적 스토리텔링을 연구하는 동아리입니다.")
                .activities("야외 촬영 실습, 영상 편집 워크숍, 전시회 및 단편 영상제 출품.")
                .ideal("관찰력이 뛰어나며 감각적 표현을 즐기는 인재.")
                .build();
        intro7.addImages(List.of(
                "https://images.unsplash.com/photo-1556761175-4b46a572b786?w=1600&auto=format&fit=crop&q=60",
                "https://images.unsplash.com/photo-1573497019940-1c28c88b4f3e?w=1600&auto=format&fit=crop&q=60",
                "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=1600&auto=format&fit=crop&q=60",
                "https://images.unsplash.com/photo-1511988617509-a57c8a288659?w=1600&auto=format&fit=crop&q=60"        ));

        Club club7 = Club.builder()
                .name("포커스")
                .category(Category.VOLUNTEER)
                .location("미디어관 107호")
                .shortIntroduction("사진과 영상으로 세상을 기록하는 시각예술 동아리")
                .introduction(intro7)
                .recruitStart(LocalDateTime.of(2025, 3, 7, 9, 0, 0))
                .recruitEnd(LocalDateTime.of(2025, 3, 21, 18, 0, 0))
                .regularMeetingInfo("매주 수요일 오후 6시")
                .caution("개인 카메라 보유자 우대")
                .build();
        clubs.add(club7);

        // 8. 온기나눔 (봉사·나눔)
        ClubIntroduction intro8 = ClubIntroduction.builder()
                .overview("온기나눔은 사회복지시설, 지역 아동센터 등과 협력하여 봉사활동을 기획하는 동아리입니다.")
                .activities("정기 봉사활동, 후원 캠페인, 봉사 후기집 제작.")
                .ideal("공감 능력이 높고 성실하며, 타인에게 긍정적 영향을 주려는 학생.")
                .build();
        intro8.addImages(List.of(
                "https://images.unsplash.com/photo-1556761175-4b46a572b786?w=1600&auto=format&fit=crop&q=60",
                "https://images.unsplash.com/photo-1573497019940-1c28c88b4f3e?w=1600&auto=format&fit=crop&q=60",
                "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=1600&auto=format&fit=crop&q=60",
                "https://images.unsplash.com/photo-1511988617509-a57c8a288659?w=1600&auto=format&fit=crop&q=60"
        ));

        Club club8 = Club.builder()
                .name("온기나눔")
                .category(Category.VOLUNTEER)
                .location("사회관 108호")
                .shortIntroduction("지역사회 봉사와 나눔 실천을 중심으로 하는 봉사 동아리")
                .introduction(intro8)
                .recruitStart(LocalDateTime.of(2025, 3, 8, 9, 0, 0))
                .recruitEnd(LocalDateTime.of(2025, 3, 22, 18, 0, 0))
                .regularMeetingInfo("매주 토요일 오전 10시")
                .caution("정기 봉사 참여 필수")
                .build();
        clubs.add(club8);

        // 9. 비즈온 (창업·비즈니스)
        ClubIntroduction intro9 = ClubIntroduction.builder()
                .overview("비즈온은 창업과 경영에 관심 있는 학생들이 모여 실제 스타트업 아이디어를 발굴하고 실행하는 동아리입니다.")
                .activities("비즈니스 모델 기획, 창업 경진대회 참가, 멘토링 세션, 피칭 실습.")
                .ideal("도전 정신과 책임감을 갖춘 학생. 현실적 문제를 비즈니스로 해결하려는 태도.")
                .build();
        intro9.addImages(List.of(
                "https://images.unsplash.com/photo-1556761175-4b46a572b786?w=1600&auto=format&fit=crop&q=60",
                "https://images.unsplash.com/photo-1573497019940-1c28c88b4f3e?w=1600&auto=format&fit=crop&q=60",
                "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=1600&auto=format&fit=crop&q=60",
                "https://images.unsplash.com/photo-1511988617509-a57c8a288659?w=1600&auto=format&fit=crop&q=60"
        ));

        Club club9 = Club.builder()
                .name("비즈온")
                .category(Category.STUDY)
                .location("경영관 201호")
                .shortIntroduction("창업 아이디어를 현실로 만드는 비즈니스 창업 동아리")
                .introduction(intro9)
                .recruitStart(LocalDateTime.of(2025, 3, 9, 9, 0, 0))
                .recruitEnd(LocalDateTime.of(2025, 3, 23, 18, 0, 0))
                .regularMeetingInfo("매주 월요일 오후 6시")
                .caution("창업 경진대회 참가 의지 필수")
                .build();
        clubs.add(club9);

        // 10. 핀라이트 (금융·투자)
        ClubIntroduction intro10 = ClubIntroduction.builder()
                .overview("핀라이트는 경제, 금융, 주식 투자에 대해 연구하는 학술 동아리입니다.")
                .activities("주식 모의투자, 시사 경제 세미나, 기업분석 발표, 금융 관련 자격증 스터디.")
                .ideal("분석적 사고를 지니고 장기적 시각에서 학습하려는 인재.")
                .build();
        intro10.addImages(List.of(
                "https://images.unsplash.com/photo-1556761175-4b46a572b786?w=1600&auto=format&fit=crop&q=60",
                "https://images.unsplash.com/photo-1573497019940-1c28c88b4f3e?w=1600&auto=format&fit=crop&q=60",
                "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=1600&auto=format&fit=crop&q=60",
                "https://images.unsplash.com/photo-1511988617509-a57c8a288659?w=1600&auto=format&fit=crop&q=60"        ));

        Club club10 = Club.builder()
                .name("핀라이트")
                .category(Category.SPORTS)
                .location("경상관 110호")
                .shortIntroduction("경제·금융 지식을 함께 배우는 투자 학술 동아리")
                .introduction(intro10)
                .recruitStart(LocalDateTime.of(2025, 3, 10, 9, 0, 0))
                .recruitEnd(LocalDateTime.of(2025, 3, 24, 18, 0, 0))
                .regularMeetingInfo("매주 금요일 오후 7시")
                .caution("경제신문 정기 구독자 우대")
                .build();
        clubs.add(club10);

        // 11. 글로비아 (영어회화·국제교류)
        ClubIntroduction intro11 = ClubIntroduction.builder()
                .overview("글로비아는 영어 토론과 국제 문화 교류를 통해 글로벌 역량을 강화하는 동아리입니다.")
                .activities("영어 토론회, 외국인 교환학생과의 언어교환 프로그램, 해외 문화 세미나.")
                .ideal("개방적이고 소통을 즐기며 새로운 문화에 관심이 많은 인재.")
                .build();
        intro11.addImages(List.of(
                "https://images.unsplash.com/photo-1556761175-4b46a572b786?w=1600&auto=format&fit=crop&q=60",
                "https://images.unsplash.com/photo-1573497019940-1c28c88b4f3e?w=1600&auto=format&fit=crop&q=60",
                "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=1600&auto=format&fit=crop&q=60",
                "https://images.unsplash.com/photo-1511988617509-a57c8a288659?w=1600&auto=format&fit=crop&q=60"
        ));

        Club club11 = Club.builder()
                .name("글로비아")
                .category(Category.STUDY)
                .location("국제관 111호")
                .shortIntroduction("영어 토론과 국제 교류를 통해 글로벌 역량을 키우는 동아리")
                .introduction(intro11)
                .recruitStart(LocalDateTime.of(2025, 3, 11, 9, 0, 0))
                .recruitEnd(LocalDateTime.of(2025, 3, 25, 18, 0, 0))
                .regularMeetingInfo("매주 화요일 오후 7시")
                .caution("외국인 교류 프로그램 참여 필수")
                .build();
        clubs.add(club11);

        // 12. 사이언스큐브 (과학탐구)
        ClubIntroduction intro12 = ClubIntroduction.builder()
                .overview("사이언스큐브는 다양한 과학적 원리를 실험과 토론을 통해 탐구하는 과학 동아리입니다.")
                .activities("자율 실험 프로젝트, 과학탐구대회 참가, 과학 저널 발행.")
                .ideal("호기심 많고 논리적이며 실험적 사고를 가진 학생.")
                .build();
        intro12.addImages(List.of(
                "https://images.unsplash.com/photo-1556761175-4b46a572b786?w=1600&auto=format&fit=crop&q=60",
                "https://images.unsplash.com/photo-1573497019940-1c28c88b4f3e?w=1600&auto=format&fit=crop&q=60",
                "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=1600&auto=format&fit=crop&q=60",
                "https://images.unsplash.com/photo-1511988617509-a57c8a288659?w=1600&auto=format&fit=crop&q=60"
        ));

        Club club12 = Club.builder()
                .name("사이언스큐브")
                .category(Category.LITERATURE)
                .location("과학관 112호")
                .shortIntroduction("토론과 책을 통해 과학을 탐구하는 연구 동아리")
                .introduction(intro12)
                .recruitStart(LocalDateTime.of(2025, 3, 12, 9, 0, 0))
                .recruitEnd(LocalDateTime.of(2025, 3, 26, 18, 0, 0))
                .regularMeetingInfo("매주 목요일 오후 5시")
                .caution("팀별 연구 결과 발표 필수")
                .build();
        clubs.add(club12);

        // 13. 글빛 (문학·창작)
        ClubIntroduction intro13 = ClubIntroduction.builder()
                .overview("글빛은 시, 소설, 수필 등 창작 문학 활동을 중심으로 하는 인문 동아리입니다.")
                .activities("문학 세미나, 창작 워크숍, 동인지 제작, 교내 문예대회 참가.")
                .ideal("감수성이 풍부하고 사유의 깊이를 글로 표현할 줄 아는 인재.")
                .build();
        intro13.addImages(List.of(
                "https://images.unsplash.com/photo-1556761175-4b46a572b786?w=1600&auto=format&fit=crop&q=60",
                "https://images.unsplash.com/photo-1573497019940-1c28c88b4f3e?w=1600&auto=format&fit=crop&q=60",
                "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=1600&auto=format&fit=crop&q=60",
                "https://images.unsplash.com/photo-1511988617509-a57c8a288659?w=1600&auto=format&fit=crop&q=60"        ));

        Club club13 = Club.builder()
                .name("글빛")
                .category(Category.LITERATURE)
                .location("인문관 113호")
                .shortIntroduction("문학과 창작을 통해 감성과 표현을 나누는 문예 동아리")
                .introduction(intro13)
                .recruitStart(LocalDateTime.of(2025, 3, 13, 9, 0, 0))
                .recruitEnd(LocalDateTime.of(2025, 3, 27, 18, 0, 0))
                .regularMeetingInfo("매주 금요일 오후 6시")
                .caution("창작 경험자 우대")
                .build();
        clubs.add(club13);

        // 14. 무대열전 (연극·공연예술)
        ClubIntroduction intro14 = ClubIntroduction.builder()
                .overview("무대열전은 연극과 공연을 통해 사회와 인간의 이야기를 표현하는 공연예술 동아리입니다.")
                .activities("공연 대본 제작, 연기 연습, 무대 연출 및 조명 워크숍.")
                .ideal("협업과 표현력에 강하며 관객과의 소통을 즐기는 인재.")
                .build();
        intro14.addImages(List.of(
                "https://images.unsplash.com/photo-1556761175-4b46a572b786?w=1600&auto=format&fit=crop&q=60",
                "https://images.unsplash.com/photo-1573497019940-1c28c88b4f3e?w=1600&auto=format&fit=crop&q=60",
                "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=1600&auto=format&fit=crop&q=60",
                "https://images.unsplash.com/photo-1511988617509-a57c8a288659?w=1600&auto=format&fit=crop&q=60"        ));

        Club club14 = Club.builder()
                .name("성모아리아")
                .category(Category.RELIGION)
                .location("예술관 114호")
                .shortIntroduction("연극과 공연으로 이야기를 전하는 공연예술 동아리")
                .introduction(intro14)
                .recruitStart(LocalDateTime.of(2025, 3, 14, 9, 0, 0))
                .recruitEnd(LocalDateTime.of(2025, 3, 28, 18, 0, 0))
                .regularMeetingInfo("매주 토요일 오후 1시")
                .caution("공연 전 참여율 80% 이상 필요")
                .build();
        clubs.add(club14);

        // 15. 겜팩토리 (게임개발)
        ClubIntroduction intro15 = ClubIntroduction.builder()
                .overview("겜팩토리는 게임 전 과정을 팀 단위로 경험하는 동아리입니다. 기획, 설계, 테스트, 협업을 함께 다룹니다.")
                .activities("마스터 실습, 게임잼 참여, 완성작 전시회 개최.")
                .ideal("창의적 문제 해결력과 협업 능력을 지닌 학생. 즐겁게 몰입할 줄 아는 대학생.")
                .build();
        intro15.addImages(List.of(
                "https://images.unsplash.com/photo-1556761175-4b46a572b786?w=1600&auto=format&fit=crop&q=60",
                "https://images.unsplash.com/photo-1573497019940-1c28c88b4f3e?w=1600&auto=format&fit=crop&q=60",
                "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=1600&auto=format&fit=crop&q=60",
                "https://images.unsplash.com/photo-1511988617509-a57c8a288659?w=1600&auto=format&fit=crop&q=60"        ));

        Club club15 = Club.builder()
                .name("겜팩토리")
                .category(Category.SPORTS)
                .location("공6 115호")
                .shortIntroduction("게임 실력 향상을 위한 동아리")
                .introduction(intro15)
                .recruitStart(LocalDateTime.of(2025, 3, 15, 9, 0, 0))
                .recruitEnd(LocalDateTime.of(2025, 3, 29, 18, 0, 0))
                .regularMeetingInfo("매주 수요일 오후 6시")
                .caution("롤 또는 발로란트 실력자 우대")
                .build();
        clubs.add(club15);

        // 16. 프레임 (사진저널리즘)
        ClubIntroduction intro16 = ClubIntroduction.builder()
                .overview("프레임은 사진을 통해 사회 이슈를 기록하고 전달하는 저널리즘 동아리입니다.")
                .activities("현장 취재, 인터뷰, 사진전 개최, 기사 작성.")
                .ideal("관찰력과 표현력이 뛰어난 학생. 사회적 관심과 책임감을 갖춘 인재.")
                .build();
        intro16.addImages(List.of(
                "https://images.unsplash.com/photo-1556761175-4b46a572b786?w=1600&auto=format&fit=crop&q=60",
                "https://images.unsplash.com/photo-1573497019940-1c28c88b4f3e?w=1600&auto=format&fit=crop&q=60",
                "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=1600&auto=format&fit=crop&q=60",
                "https://images.unsplash.com/photo-1511988617509-a57c8a288659?w=1600&auto=format&fit=crop&q=60"        ));

        Club club16 = Club.builder()
                .name("프레임")
                .category(Category.RELIGION)
                .location("미디어관 116호")
                .shortIntroduction("사진저널리즘으로 사회를 기록하는 미디어 동아리")
                .introduction(intro16)
                .recruitStart(LocalDateTime.of(2025, 3, 16, 9, 0, 0))
                .recruitEnd(LocalDateTime.of(2025, 3, 30, 18, 0, 0))
                .regularMeetingInfo("매주 목요일 오후 7시")
                .caution("촬영·편집 워크숍 필수 참여")
                .build();
        clubs.add(club16);

        // 17. 로드메이커 (여행·문화탐방)
        ClubIntroduction intro17 = ClubIntroduction.builder()
                .overview("로드메이커는 국내외 다양한 장소를 탐방하며 문화적 경험을 나누는 여행 동아리입니다.")
                .activities("답사 기획, 여행 기록 공유, 지도 제작 및 포스터 전시.")
                .ideal("모험심과 팀워크를 중시하며 열린 사고를 가진 학생.")
                .build();
        intro17.addImages(List.of(
                "https://images.unsplash.com/photo-1556761175-4b46a572b786?w=1600&auto=format&fit=crop&q=60",
                "https://images.unsplash.com/photo-1573497019940-1c28c88b4f3e?w=1600&auto=format&fit=crop&q=60",
                "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=1600&auto=format&fit=crop&q=60",
                "https://images.unsplash.com/photo-1511988617509-a57c8a288659?w=1600&auto=format&fit=crop&q=60"        ));

        Club club17 = Club.builder()
                .name("로드메이커")
                .category(Category.LITERATURE)
                .location("인문관 117호")
                .shortIntroduction("여행과 문화탐방을 통해 세계를 배우는 교양 동아리")
                .introduction(intro17)
                .recruitStart(LocalDateTime.of(2025, 3, 17, 9, 0, 0))
                .recruitEnd(LocalDateTime.of(2025, 3, 31, 18, 0, 0))
                .regularMeetingInfo("매주 금요일 오후 6시")
                .caution("정기 탐방 참여 의무")
                .build();
        clubs.add(club17);

        // 18. FC JNU (축구)
        ClubIntroduction intro18 = ClubIntroduction.builder()
                .overview("FC JNU는 축구를 사랑하는 학생들이 모여 건강한 교류를 즐기는 체육 동아리입니다.")
                .activities("주 2회 연습 경기, 교내 리그전 참가, 체력 관리 세션.")
                .ideal("열정적이고 팀플레이를 중시하는 인재.")
                .build();
        intro18.addImages(List.of(
                "https://images.unsplash.com/photo-1556761175-4b46a572b786?w=1600&auto=format&fit=crop&q=60",
                "https://images.unsplash.com/photo-1573497019940-1c28c88b4f3e?w=1600&auto=format&fit=crop&q=60",
                "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=1600&auto=format&fit=crop&q=60",
                "https://images.unsplash.com/photo-1511988617509-a57c8a288659?w=1600&auto=format&fit=crop&q=60"        ));

        Club club18 = Club.builder()
                .name("FC JNU")
                .category(Category.SPORTS)
                .location("운동장")
                .shortIntroduction("축구를 통해 교류하고 건강을 챙기는 체육 동아리")
                .introduction(intro18)
                .recruitStart(LocalDateTime.of(2025, 3, 18, 9, 0, 0))
                .recruitEnd(LocalDateTime.of(2025, 4, 1, 18, 0, 0))
                .regularMeetingInfo("매주 수요일 오후 7시")
                .caution("연습 경기 참여 필수")
                .build();
        clubs.add(club18);

        // 19. 그린리프 (환경보호)
        ClubIntroduction intro19 = ClubIntroduction.builder()
                .overview("그린리프는 환경문제 해결을 목표로 하는 생태·환경 동아리입니다.")
                .activities("플로깅, 재활용 캠페인, 친환경 아이템 제작, 환경 세미나.")
                .ideal("지속가능성에 관심이 많고 실천을 중시하는 학생.")
                .build();
        intro19.addImages(List.of(
                "https://images.unsplash.com/photo-1556761175-4b46a572b786?w=1600&auto=format&fit=crop&q=60",
                "https://images.unsplash.com/photo-1573497019940-1c28c88b4f3e?w=1600&auto=format&fit=crop&q=60",
                "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=1600&auto=format&fit=crop&q=60",
                "https://images.unsplash.com/photo-1511988617509-a57c8a288659?w=1600&auto=format&fit=crop&q=60"        ));

        Club club19 = Club.builder()
                .name("그린리프")
                .category(Category.RELIGION)
                .location("자연대 119호")
                .shortIntroduction("환경 보호와 지속가능한 삶을 실천하는 친환경 동아리")
                .introduction(intro19)
                .recruitStart(LocalDateTime.of(2025, 3, 19, 9, 0, 0))
                .recruitEnd(LocalDateTime.of(2025, 4, 2, 18, 0, 0))
                .regularMeetingInfo("매주 목요일 오후 6시")
                .caution("플로깅 등 야외활동 참여 필수")
                .build();
        clubs.add(club19);

        // 20. 에어로랩 (드론·항공)
        ClubIntroduction intro20 = ClubIntroduction.builder()
                .overview("에어로랩은 드론과 항공기술을 연구하며 설계/제작하는 공학 동아리입니다.")
                .activities("드론 제작, 비행 테스트, 항공 원리 스터디, 대회 참가.")
                .ideal("기술적 호기심과 분석적 사고를 갖춘 인재. 정밀함과 끈기를 가진 학생.")
                .build();
        intro20.addImages(List.of(
                "https://images.unsplash.com/photo-1556761175-4b46a572b786?w=1600&auto=format&fit=crop&q=60",
                "https://images.unsplash.com/photo-1573497019940-1c28c88b4f3e?w=1600&auto=format&fit=crop&q=60",
                "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=1600&auto=format&fit=crop&q=60",
                "https://images.unsplash.com/photo-1511988617509-a57c8a288659?w=1600&auto=format&fit=crop&q=60"        ));

        Club club20 = Club.builder()
                .name("에어로랩")
                .category(Category.SPORTS)
                .location("공학관 옥상 실험실")
                .shortIntroduction("드론과 항공기술을 연구·제작하는 공학 동아리")
                .introduction(intro20)
                .recruitStart(LocalDateTime.of(2025, 3, 20, 9, 0, 0))
                .recruitEnd(LocalDateTime.of(2025, 4, 3, 18, 0, 0))
                .regularMeetingInfo("매주 금요일 오후 7시")
                .caution("비행 실험 안전수칙 준수 필수")
                .build();
        clubs.add(club20);

        return clubRepository.saveAll(clubs);
    }

    private void seedApplyFormsAndQuestions(List<Club> clubs) {

        Map<String, Club> clubByName = clubs.stream()
                .collect(Collectors.toMap(Club::getName, c -> c));

        List<ClubApplyForm> forms = new ArrayList<>();

        // ========== 5) CLUB_APPLY_FORM ==========

        ClubApplyForm form1 = clubApplyFormRepository.save(
                ClubApplyForm.builder()
                        .club(clubByName.get("인터엑스"))
                        .title("인터엑스 2025 상반기 모집")
                        .description("사회문제 해결과 토론에 열정이 있는 분들을 모집합니다. 함께 배우고 실천하며 변화를 만들어가요.")
                        .build()
        );
        forms.add(form1);

        ClubApplyForm form2 = clubApplyFormRepository.save(
                ClubApplyForm.builder()
                        .club(clubByName.get("에이아이디브"))
                        .title("에이아이디브 2025 상반기 신입 부원 모집")
                        .description("AI와 데이터 분석에 관심 있는 학생들을 환영합니다. 논문 스터디와 프로젝트를 함께해요.")
                        .build()
        );
        forms.add(form2);

        ClubApplyForm form3 = clubApplyFormRepository.save(
                ClubApplyForm.builder()
                        .club(clubByName.get("코드마스터"))
                        .title("코드마스터 2025 상반기 리쿠르팅")
                        .description("프로그래밍 실력을 키우고 싶은 개발자 지망생을 모집합니다. 함께 배우고 성장합시다.")
                        .build()
        );
        forms.add(form3);

        ClubApplyForm form4 = clubApplyFormRepository.save(
                ClubApplyForm.builder()
                        .club(clubByName.get("로보테크"))
                        .title("로보테크 2025 신입 회원 모집")
                        .description("로봇공학, 드론, 제어 시스템에 관심 있는 학생을 모집합니다. 하드웨어 제작부터 코딩까지 함께합니다.")
                        .build()
        );
        forms.add(form4);

        ClubApplyForm form5 = clubApplyFormRepository.save(
                ClubApplyForm.builder()
                        .club(clubByName.get("아트픽"))
                        .title("아트픽 2025 전시 참여 회원 모집")
                        .description("창작 활동과 전시에 참여할 예술적 감각을 가진 분을 찾습니다. 미술·디자인 전공자 우대.")
                        .build()
        );
        forms.add(form5);

        ClubApplyForm form6 = clubApplyFormRepository.save(
                ClubApplyForm.builder()
                        .club(clubByName.get("리버스"))
                        .title("리버스 2025 공연팀 신입 모집")
                        .description("보컬, 연주, 작곡 등 음악에 열정 있는 부원을 모집합니다. 정기 공연을 함께 만들어가요.")
                        .build()
        );
        forms.add(form6);

        ClubApplyForm form7 = clubApplyFormRepository.save(
                ClubApplyForm.builder()
                        .club(clubByName.get("포커스"))
                        .title("포커스 2025 상반기 신입 회원 모집")
                        .description("사진과 영상 제작에 관심 있는 분을 찾습니다. 장비가 없어도 열정만 있으면 환영합니다.")
                        .build()
        );
        forms.add(form7);

        ClubApplyForm form8 = clubApplyFormRepository.save(
                ClubApplyForm.builder()
                        .club(clubByName.get("온기나눔"))
                        .title("온기나눔 2025 봉사활동 참가자 모집")
                        .description("지역 아동센터 및 복지시설 봉사활동에 함께할 따뜻한 마음의 부원을 찾습니다.")
                        .build()
        );
        forms.add(form8);

        ClubApplyForm form9 = clubApplyFormRepository.save(
                ClubApplyForm.builder()
                        .club(clubByName.get("비즈온"))
                        .title("비즈온 2025 창업 아이디어팀 모집")
                        .description("창업과 비즈니스에 관심 있는 학생들을 위한 리쿠르팅입니다. 함께 스타트업을 만들어봐요.")
                        .build()
        );
        forms.add(form9);

        ClubApplyForm form10 = clubApplyFormRepository.save(
                ClubApplyForm.builder()
                        .club(clubByName.get("핀라이트"))
                        .title("핀라이트 2025 경제·금융 세미나 참여자 모집")
                        .description("금융과 투자에 관심 있는 학생을 모집합니다. 주식 모의투자와 시사 세미나를 함께합니다.")
                        .build()
        );
        forms.add(form10);

        ClubApplyForm form11 = clubApplyFormRepository.save(
                ClubApplyForm.builder()
                        .club(clubByName.get("글로비아"))
                        .title("글로비아 2025 상반기 글로벌 토론 동아리 모집")
                        .description("영어 회화와 국제 교류에 관심 있는 학생을 모집합니다. 외국인 교환학생과의 토론 세션을 운영합니다.")
                        .build()
        );
        forms.add(form11);

        ClubApplyForm form12 = clubApplyFormRepository.save(
                ClubApplyForm.builder()
                        .club(clubByName.get("사이언스큐브"))
                        .title("사이언스큐브 2025 연구 프로젝트 팀원 모집")
                        .description("과학 실험과 탐구에 열정 있는 학생을 모집합니다. 팀별 실험을 설계하고 논문 형식으로 정리합니다.")
                        .build()
        );
        forms.add(form12);

        ClubApplyForm form13 = clubApplyFormRepository.save(
                ClubApplyForm.builder()
                        .club(clubByName.get("글빛"))
                        .title("글빛 2025 문예창작 신입 회원 모집")
                        .description("문학, 시, 소설, 수필에 관심 있는 학생을 환영합니다. 창작과 토론을 통해 감성을 나눕니다.")
                        .build()
        );
        forms.add(form13);

        ClubApplyForm form14 = clubApplyFormRepository.save(
                ClubApplyForm.builder()
                        .club(clubByName.get("무대열전"))
                        .title("무대열전 2025 공연팀 오디션 안내")
                        .description("연극과 공연예술에 관심 있는 학생을 모집합니다. 배우·연출·조명 등 다양한 역할이 있습니다.")
                        .build()
        );
        forms.add(form14);

        ClubApplyForm form15 = clubApplyFormRepository.save(
                ClubApplyForm.builder()
                        .club(clubByName.get("겜팩토리"))
                        .title("겜팩토리 2025 상반기 개발팀 모집")
                        .description("게임 기획·디자인·프로그래밍 등 팀 단위 창작에 함께할 인원을 찾습니다. Unity 경험자 환영.")
                        .build()
        );
        forms.add(form15);

        ClubApplyForm form16 = clubApplyFormRepository.save(
                ClubApplyForm.builder()
                        .club(clubByName.get("프레임"))
                        .title("프레임 2025 상반기 사진기자단 모집")
                        .description("사진저널리즘에 관심 있는 학생을 모집합니다. 사회 이슈를 기록하는 프로젝트를 진행합니다.")
                        .build()
        );
        forms.add(form16);

        ClubApplyForm form17 = clubApplyFormRepository.save(
                ClubApplyForm.builder()
                        .club(clubByName.get("로드메이커"))
                        .title("로드메이커 2025 문화탐방 참가자 모집")
                        .description("여행과 답사를 통해 다양한 문화를 배우고 싶은 학생을 찾습니다. 학기 중 소규모 탐방 진행.")
                        .build()
        );
        forms.add(form17);

        ClubApplyForm form18 = clubApplyFormRepository.save(
                ClubApplyForm.builder()
                        .club(clubByName.get("FC JNU"))
                        .title("FC JNU 2025 신입 선수 모집")
                        .description("축구를 좋아하는 누구나 환영합니다! 교내 리그 및 정기 연습 경기에 참여할 팀원을 모집합니다.")
                        .build()
        );
        forms.add(form18);

        ClubApplyForm form19 = clubApplyFormRepository.save(
                ClubApplyForm.builder()
                        .club(clubByName.get("그린리프"))
                        .title("그린리프 2025 친환경 프로젝트 부원 모집")
                        .description("환경 보호와 지속 가능한 활동에 관심 있는 학생을 모집합니다. 캠페인 기획 및 플로깅 활동 진행.")
                        .build()
        );
        forms.add(form19);

        ClubApplyForm form20 = clubApplyFormRepository.save(
                ClubApplyForm.builder()
                        .club(clubByName.get("에어로랩"))
                        .title("에어로랩 2025 드론 연구팀 신입 모집")
                        .description("드론과 항공기술을 연구할 열정적인 부원을 찾습니다. 직접 제작 및 비행 테스트에 참여합니다.")
                        .build()
        );
        forms.add(form20);

        // ========== 6) FORM_QUESTION ==========

        // --- form1: TEXT + RADIO + TEXT + TIME_SLOT ---
        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form1)
                .question("자기소개를 작성해주세요.")
                .fieldType(FieldType.TEXT)
                .isRequired(true)
                .displayOrder(1L)
                .build());

        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form1)
                .question("개발 경험이 있으신가요?")
                .fieldType(FieldType.RADIO)
                .isRequired(true)
                .displayOrder(2L)
                .options(List.of("예", "아니오"))
                .build());

        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form1)
                .question("자기소개를 간단히 적어주세요.")
                .fieldType(FieldType.TEXT)
                .isRequired(false)
                .displayOrder(3L)
                .build());

        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form1)
                .question("면접가능 날짜는?")
                .fieldType(FieldType.TIME_SLOT)
                .isRequired(true)
                .displayOrder(4L)
                .timeSlotOptions(List.of(
                        new TimeSlotOption(
                                "2025-10-15 ~ 2025-10-16",
                                new TimeSlotOption.TimeRange(
                                        LocalTime.of(10, 0),
                                        LocalTime.of(12, 0)
                                )
                        )
                ))
                .build());

        // --- form2 ~ form20: TEXT 하나씩 ---
        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form2)
                .question("지원 동기를 작성해주세요.")
                .fieldType(FieldType.TEXT)
                .isRequired(true)
                .displayOrder(1L)
                .build());

        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form3)
                .question("활동 가능 요일을 적어주세요.")
                .fieldType(FieldType.TEXT)
                .isRequired(true)
                .displayOrder(1L)
                .build());

        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form4)
                .question("관련 경험을 소개해주세요.")
                .fieldType(FieldType.TEXT)
                .isRequired(true)
                .displayOrder(1L)
                .build());

        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form5)
                .question("장단점을 적어주세요.")
                .fieldType(FieldType.TEXT)
                .isRequired(true)
                .displayOrder(1L)
                .build());

        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form6)
                .question("동아리에서 이루고 싶은 목표는?")
                .fieldType(FieldType.TEXT)
                .isRequired(true)
                .displayOrder(1L)
                .build());

        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form7)
                .question("협업 경험을 소개해주세요.")
                .fieldType(FieldType.TEXT)
                .isRequired(true)
                .displayOrder(1L)
                .build());

        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form8)
                .question("선호하는 역할은?")
                .fieldType(FieldType.TEXT)
                .isRequired(true)
                .displayOrder(1L)
                .build());

        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form9)
                .question("시간 관리 방법은?")
                .fieldType(FieldType.TEXT)
                .isRequired(true)
                .displayOrder(1L)
                .build());

        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form10)
                .question("리더십 경험이 있나요?")
                .fieldType(FieldType.TEXT)
                .isRequired(true)
                .displayOrder(1L)
                .build());

        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form11)
                .question("갈등 해결 경험은?")
                .fieldType(FieldType.TEXT)
                .isRequired(true)
                .displayOrder(1L)
                .build());

        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form12)
                .question("최근 읽은 책/논문은?")
                .fieldType(FieldType.TEXT)
                .isRequired(true)
                .displayOrder(1L)
                .build());

        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form13)
                .question("포트폴리오 요약을 작성해주세요.")
                .fieldType(FieldType.TEXT)
                .isRequired(true)
                .displayOrder(1L)
                .build());

        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form14)
                .question("학습 계획을 적어주세요.")
                .fieldType(FieldType.TEXT)
                .isRequired(true)
                .displayOrder(1L)
                .build());

        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form15)
                .question("봉사 경험이 있나요?")
                .fieldType(FieldType.TEXT)
                .isRequired(true)
                .displayOrder(1L)
                .build());

        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form16)
                .question("운동/동호회 경험은?")
                .fieldType(FieldType.TEXT)
                .isRequired(true)
                .displayOrder(1L)
                .build());

        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form17)
                .question("프로젝트 경험을 적어주세요.")
                .fieldType(FieldType.TEXT)
                .isRequired(true)
                .displayOrder(1L)
                .build());

        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form18)
                .question("강점 3가지를 적어주세요.")
                .fieldType(FieldType.TEXT)
                .isRequired(true)
                .displayOrder(1L)
                .build());

        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form19)
                .question("약점 3가지를 적어주세요.")
                .fieldType(FieldType.TEXT)
                .isRequired(true)
                .displayOrder(1L)
                .build());

        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form20)
                .question("마지막으로 하고 싶은 말")
                .fieldType(FieldType.TEXT)
                .isRequired(true)
                .displayOrder(1L)
                .build());
    }


    private List<Application> seedApplications() {

        // user: studentId 기준으로 맵핑
        Map<String, User> userByStudentId = userRepository.findAll().stream()
                .collect(Collectors.toMap(User::getStudentId, u -> u));

        // clubApplyForm: club 이름 기준으로 맵핑
        Map<String, ClubApplyForm> formByClubName = clubApplyFormRepository.findAll().stream()
                .collect(Collectors.toMap(form -> form.getClub().getName(), f -> f));

        List<Application> apps = new ArrayList<>();

        // (1,1,'PENDING','INTERVIEW',3.2)
        Application app1 = Application.builder()
                .user(userByStudentId.get("20250001"))   // user_id 1
                .clubApplyForm(formByClubName.get("인터엑스")) // form_id 1
                .status(Status.PENDING)
                .stage(Stage.INTERVIEW)
                .build();
        app1.updateAverageRating(3.2);
        apps.add(app1);

        // (2,2,'APPROVED','FINAL',4.3)
        Application app2 = Application.builder()
                .user(userByStudentId.get("20250002"))
                .clubApplyForm(formByClubName.get("에이아이디브"))
                .status(Status.APPROVED)
                .stage(Stage.FINAL)
                .build();
        app2.updateAverageRating(4.3);
        apps.add(app2);

        // (3,3,'REJECTED','INTERVIEW',2.5)
        Application app3 = Application.builder()
                .user(userByStudentId.get("20250003"))
                .clubApplyForm(formByClubName.get("코드마스터"))
                .status(Status.REJECTED)
                .stage(Stage.INTERVIEW)
                .build();
        app3.updateAverageRating(2.5);
        apps.add(app3);

        // (4,4,'PENDING','FINAL',3.5)
        Application app4 = Application.builder()
                .user(userByStudentId.get("20250004"))
                .clubApplyForm(formByClubName.get("로보테크"))
                .status(Status.PENDING)
                .stage(Stage.FINAL)
                .build();
        app4.updateAverageRating(3.5);
        apps.add(app4);

        // (5,5,'APPROVED','INTERVIEW',4.1)
        Application app5 = Application.builder()
                .user(userByStudentId.get("20250005"))
                .clubApplyForm(formByClubName.get("아트픽"))
                .status(Status.APPROVED)
                .stage(Stage.INTERVIEW)
                .build();
        app5.updateAverageRating(4.1);
        apps.add(app5);

        // (6,6,'REJECTED','FINAL',2.7)
        Application app6 = Application.builder()
                .user(userByStudentId.get("20250006"))
                .clubApplyForm(formByClubName.get("리버스"))
                .status(Status.REJECTED)
                .stage(Stage.FINAL)
                .build();
        app6.updateAverageRating(2.7);
        apps.add(app6);

        // (7,7,'PENDING','INTERVIEW',3.8)
        Application app7 = Application.builder()
                .user(userByStudentId.get("20250007"))
                .clubApplyForm(formByClubName.get("포커스"))
                .status(Status.PENDING)
                .stage(Stage.INTERVIEW)
                .build();
        app7.updateAverageRating(3.8);
        apps.add(app7);

        // (8,8,'APPROVED','FINAL',4.6)
        Application app8 = Application.builder()
                .user(userByStudentId.get("20250008"))
                .clubApplyForm(formByClubName.get("온기나눔"))
                .status(Status.APPROVED)
                .stage(Stage.FINAL)
                .build();
        app8.updateAverageRating(4.6);
        apps.add(app8);

        // (9,9,'REJECTED','INTERVIEW',2.8)
        Application app9 = Application.builder()
                .user(userByStudentId.get("20250009"))
                .clubApplyForm(formByClubName.get("비즈온"))
                .status(Status.REJECTED)
                .stage(Stage.INTERVIEW)
                .build();
        app9.updateAverageRating(2.8);
        apps.add(app9);

        // (10,10,'PENDING','FINAL',3.9)
        Application app10 = Application.builder()
                .user(userByStudentId.get("20250010"))
                .clubApplyForm(formByClubName.get("핀라이트"))
                .status(Status.PENDING)
                .stage(Stage.FINAL)
                .build();
        app10.updateAverageRating(3.9);
        apps.add(app10);

        // (11,11,'APPROVED','INTERVIEW',4.2)
        Application app11 = Application.builder()
                .user(userByStudentId.get("20250011"))
                .clubApplyForm(formByClubName.get("글로비아"))
                .status(Status.APPROVED)
                .stage(Stage.INTERVIEW)
                .build();
        app11.updateAverageRating(4.2);
        apps.add(app11);

        // (12,12,'REJECTED','FINAL',2.9)
        Application app12 = Application.builder()
                .user(userByStudentId.get("20250012"))
                .clubApplyForm(formByClubName.get("사이언스큐브"))
                .status(Status.REJECTED)
                .stage(Stage.FINAL)
                .build();
        app12.updateAverageRating(2.9);
        apps.add(app12);

        // (13,13,'PENDING','INTERVIEW',3.4)
        Application app13 = Application.builder()
                .user(userByStudentId.get("20250013"))
                .clubApplyForm(formByClubName.get("글빛"))
                .status(Status.PENDING)
                .stage(Stage.INTERVIEW)
                .build();
        app13.updateAverageRating(3.4);
        apps.add(app13);

        // (14,14,'APPROVED','FINAL',4.4)
        Application app14 = Application.builder()
                .user(userByStudentId.get("20250014"))
                .clubApplyForm(formByClubName.get("무대열전"))
                .status(Status.APPROVED)
                .stage(Stage.FINAL)
                .build();
        app14.updateAverageRating(4.4);
        apps.add(app14);

        // (15,15,'REJECTED','INTERVIEW',2.6)
        Application app15 = Application.builder()
                .user(userByStudentId.get("20250015"))
                .clubApplyForm(formByClubName.get("겜팩토리"))
                .status(Status.REJECTED)
                .stage(Stage.INTERVIEW)
                .build();
        app15.updateAverageRating(2.6);
        apps.add(app15);

        // (16,16,'PENDING','FINAL',3.7)
        Application app16 = Application.builder()
                .user(userByStudentId.get("20250016"))
                .clubApplyForm(formByClubName.get("프레임"))
                .status(Status.PENDING)
                .stage(Stage.FINAL)
                .build();
        app16.updateAverageRating(3.7);
        apps.add(app16);

        // (17,17,'APPROVED','INTERVIEW',4.5)
        Application app17 = Application.builder()
                .user(userByStudentId.get("20250017"))
                .clubApplyForm(formByClubName.get("로드메이커"))
                .status(Status.APPROVED)
                .stage(Stage.INTERVIEW)
                .build();
        app17.updateAverageRating(4.5);
        apps.add(app17);

        // (18,18,'REJECTED','FINAL',2.4)
        Application app18 = Application.builder()
                .user(userByStudentId.get("20250018"))
                .clubApplyForm(formByClubName.get("FC JNU"))
                .status(Status.REJECTED)
                .stage(Stage.FINAL)
                .build();
        app18.updateAverageRating(2.4);
        apps.add(app18);

        // (19,19,'PENDING','INTERVIEW',3.6)
        Application app19 = Application.builder()
                .user(userByStudentId.get("20250019"))
                .clubApplyForm(formByClubName.get("그린리프"))
                .status(Status.PENDING)
                .stage(Stage.INTERVIEW)
                .build();
        app19.updateAverageRating(3.6);
        apps.add(app19);

        // (20,20,'APPROVED','FINAL',4.0)
        Application app20 = Application.builder()
                .user(userByStudentId.get("20250020"))
                .clubApplyForm(formByClubName.get("에어로랩"))
                .status(Status.APPROVED)
                .stage(Stage.FINAL)
                .build();
        app20.updateAverageRating(4.0);
        apps.add(app20);

        // ===== 뒤부터는 거의 다 인터엑스 폼 재사용 =====

        // (21,1,'PENDING','INTERVIEW',3.1)
        Application app21 = Application.builder()
                .user(userByStudentId.get("20250021"))
                .clubApplyForm(formByClubName.get("인터엑스"))
                .status(Status.PENDING)
                .stage(Stage.INTERVIEW)
                .build();
        app21.updateAverageRating(3.1);
        apps.add(app21);

        // (22,1,'PENDING','FINAL',3.4)
        Application app22 = Application.builder()
                .user(userByStudentId.get("20250022"))
                .clubApplyForm(formByClubName.get("인터엑스"))
                .status(Status.PENDING)
                .stage(Stage.FINAL)
                .build();
        app22.updateAverageRating(3.4);
        apps.add(app22);

        // (23,1,'APPROVED','INTERVIEW',4.3)
        Application app23 = Application.builder()
                .user(userByStudentId.get("20250023"))
                .clubApplyForm(formByClubName.get("인터엑스"))
                .status(Status.APPROVED)
                .stage(Stage.INTERVIEW)
                .build();
        app23.updateAverageRating(4.3);
        apps.add(app23);

        // (24,1,'REJECTED','FINAL',2.7)
        Application app24 = Application.builder()
                .user(userByStudentId.get("20250024"))
                .clubApplyForm(formByClubName.get("인터엑스"))
                .status(Status.REJECTED)
                .stage(Stage.FINAL)
                .build();
        app24.updateAverageRating(2.7);
        apps.add(app24);

        // (25,1,'APPROVED','INTERVIEW',4.5)
        Application app25 = Application.builder()
                .user(userByStudentId.get("20250025"))
                .clubApplyForm(formByClubName.get("인터엑스"))
                .status(Status.APPROVED)
                .stage(Stage.INTERVIEW)
                .build();
        app25.updateAverageRating(4.5);
        apps.add(app25);

        // (26,2,'APPROVED','FINAL',4.4)
        Application app26 = Application.builder()
                .user(userByStudentId.get("20250026"))
                .clubApplyForm(formByClubName.get("에이아이디브"))
                .status(Status.APPROVED)
                .stage(Stage.FINAL)
                .build();
        app26.updateAverageRating(4.4);
        apps.add(app26);

        // (27,3,'APPROVED','INTERVIEW',4.7)
        Application app27 = Application.builder()
                .user(userByStudentId.get("20250027"))
                .clubApplyForm(formByClubName.get("코드마스터"))
                .status(Status.APPROVED)
                .stage(Stage.INTERVIEW)
                .build();
        app27.updateAverageRating(4.7);
        apps.add(app27);

        // (28,4,'APPROVED','FINAL',4.8)
        Application app28 = Application.builder()
                .user(userByStudentId.get("20250028"))
                .clubApplyForm(formByClubName.get("로보테크"))
                .status(Status.APPROVED)
                .stage(Stage.FINAL)
                .build();
        app28.updateAverageRating(4.8);
        apps.add(app28);

        // (29,5,'APPROVED','INTERVIEW',4.6)
        Application app29 = Application.builder()
                .user(userByStudentId.get("20250029"))
                .clubApplyForm(formByClubName.get("아트픽"))
                .status(Status.APPROVED)
                .stage(Stage.INTERVIEW)
                .build();
        app29.updateAverageRating(4.6);
        apps.add(app29);

        // (30,6,'APPROVED','FINAL',4.9)
        Application app30 = Application.builder()
                .user(userByStudentId.get("20250030"))
                .clubApplyForm(formByClubName.get("리버스"))
                .status(Status.APPROVED)
                .stage(Stage.FINAL)
                .build();
        app30.updateAverageRating(4.9);
        apps.add(app30);

        // (31,7,'APPROVED','INTERVIEW',4.2)
        Application app31 = Application.builder()
                .user(userByStudentId.get("20250031"))
                .clubApplyForm(formByClubName.get("포커스"))
                .status(Status.APPROVED)
                .stage(Stage.INTERVIEW)
                .build();
        app31.updateAverageRating(4.2);
        apps.add(app31);

        // (32,8,'APPROVED','FINAL',4.3)
        Application app32 = Application.builder()
                .user(userByStudentId.get("20250032"))
                .clubApplyForm(formByClubName.get("온기나눔"))
                .status(Status.APPROVED)
                .stage(Stage.FINAL)
                .build();
        app32.updateAverageRating(4.3);
        apps.add(app32);

        // (33,9,'APPROVED','INTERVIEW',4.5)
        Application app33 = Application.builder()
                .user(userByStudentId.get("20250033"))
                .clubApplyForm(formByClubName.get("비즈온"))
                .status(Status.APPROVED)
                .stage(Stage.INTERVIEW)
                .build();
        app33.updateAverageRating(4.5);
        apps.add(app33);

        // (34,10,'APPROVED','FINAL',4.1)
        Application app34 = Application.builder()
                .user(userByStudentId.get("20250034"))
                .clubApplyForm(formByClubName.get("핀라이트"))
                .status(Status.APPROVED)
                .stage(Stage.FINAL)
                .build();
        app34.updateAverageRating(4.1);
        apps.add(app34);

        // (35,11,'APPROVED','INTERVIEW',4.7)
        Application app35 = Application.builder()
                .user(userByStudentId.get("20250035"))
                .clubApplyForm(formByClubName.get("글로비아"))
                .status(Status.APPROVED)
                .stage(Stage.INTERVIEW)
                .build();
        app35.updateAverageRating(4.7);
        apps.add(app35);

        // (36,12,'APPROVED','FINAL',4.8)
        Application app36 = Application.builder()
                .user(userByStudentId.get("20250036"))
                .clubApplyForm(formByClubName.get("사이언스큐브"))
                .status(Status.APPROVED)
                .stage(Stage.FINAL)
                .build();
        app36.updateAverageRating(4.8);
        apps.add(app36);

        // (37,13,'APPROVED','INTERVIEW',4.4)
        Application app37 = Application.builder()
                .user(userByStudentId.get("20250037"))
                .clubApplyForm(formByClubName.get("글빛"))
                .status(Status.APPROVED)
                .stage(Stage.INTERVIEW)
                .build();
        app37.updateAverageRating(4.4);
        apps.add(app37);

        // (38,14,'APPROVED','FINAL',4.6)
        Application app38 = Application.builder()
                .user(userByStudentId.get("20250038"))
                .clubApplyForm(formByClubName.get("무대열전"))
                .status(Status.APPROVED)
                .stage(Stage.FINAL)
                .build();
        app38.updateAverageRating(4.6);
        apps.add(app38);

        // (39,15,'APPROVED','INTERVIEW',4.9)
        Application app39 = Application.builder()
                .user(userByStudentId.get("20250039"))
                .clubApplyForm(formByClubName.get("겜팩토리"))
                .status(Status.APPROVED)
                .stage(Stage.INTERVIEW)
                .build();
        app39.updateAverageRating(4.9);
        apps.add(app39);

        // (40,16,'APPROVED','FINAL',4.2)
        Application app40 = Application.builder()
                .user(userByStudentId.get("20250040"))
                .clubApplyForm(formByClubName.get("프레임"))
                .status(Status.APPROVED)
                .stage(Stage.FINAL)
                .build();
        app40.updateAverageRating(4.2);
        apps.add(app40);

        // (41,17,'APPROVED','INTERVIEW',4.5)
        Application app41 = Application.builder()
                .user(userByStudentId.get("20250041"))
                .clubApplyForm(formByClubName.get("로드메이커"))
                .status(Status.APPROVED)
                .stage(Stage.INTERVIEW)
                .build();
        app41.updateAverageRating(4.5);
        apps.add(app41);

        // (42,18,'APPROVED','FINAL',4.3)
        Application app42 = Application.builder()
                .user(userByStudentId.get("20250042"))
                .clubApplyForm(formByClubName.get("FC JNU"))
                .status(Status.APPROVED)
                .stage(Stage.FINAL)
                .build();
        app42.updateAverageRating(4.3);
        apps.add(app42);

        // (43,19,'APPROVED','INTERVIEW',4.7)
        Application app43 = Application.builder()
                .user(userByStudentId.get("20250043"))
                .clubApplyForm(formByClubName.get("그린리프"))
                .status(Status.APPROVED)
                .stage(Stage.INTERVIEW)
                .build();
        app43.updateAverageRating(4.7);
        apps.add(app43);

        // (44,20,'APPROVED','FINAL',4.8)
        Application app44 = Application.builder()
                .user(userByStudentId.get("20250044"))
                .clubApplyForm(formByClubName.get("에어로랩"))
                .status(Status.APPROVED)
                .stage(Stage.FINAL)
                .build();
        app44.updateAverageRating(4.8);
        apps.add(app44);

        // (45,1,'PENDING','INTERVIEW',3.2)
        Application app45 = Application.builder()
                .user(userByStudentId.get("20250045"))
                .clubApplyForm(formByClubName.get("인터엑스"))
                .status(Status.PENDING)
                .stage(Stage.INTERVIEW)
                .build();
        app45.updateAverageRating(3.2);
        apps.add(app45);

        // (46,1,'PENDING','FINAL',3.3)
        Application app46 = Application.builder()
                .user(userByStudentId.get("20250046"))
                .clubApplyForm(formByClubName.get("인터엑스"))
                .status(Status.PENDING)
                .stage(Stage.FINAL)
                .build();
        app46.updateAverageRating(3.3);
        apps.add(app46);

        // (47,1,'APPROVED','INTERVIEW',4.1)
        Application app47 = Application.builder()
                .user(userByStudentId.get("20250047"))
                .clubApplyForm(formByClubName.get("인터엑스"))
                .status(Status.APPROVED)
                .stage(Stage.INTERVIEW)
                .build();
        app47.updateAverageRating(4.1);
        apps.add(app47);

        // (48,1,'APPROVED','FINAL',4.5)
        Application app48 = Application.builder()
                .user(userByStudentId.get("20250048"))
                .clubApplyForm(formByClubName.get("인터엑스"))
                .status(Status.APPROVED)
                .stage(Stage.FINAL)
                .build();
        app48.updateAverageRating(4.5);
        apps.add(app48);

        // (49,1,'REJECTED','INTERVIEW',2.8)
        Application app49 = Application.builder()
                .user(userByStudentId.get("20250049"))
                .clubApplyForm(formByClubName.get("인터엑스"))
                .status(Status.REJECTED)
                .stage(Stage.INTERVIEW)
                .build();
        app49.updateAverageRating(2.8);
        apps.add(app49);

        // (50,1,'REJECTED','FINAL',2.9)
        Application app50 = Application.builder()
                .user(userByStudentId.get("20250050"))
                .clubApplyForm(formByClubName.get("인터엑스"))
                .status(Status.REJECTED)
                .stage(Stage.FINAL)
                .build();
        app50.updateAverageRating(2.9);
        apps.add(app50);

        // (51,1,'PENDING','INTERVIEW',3.0)
        Application app51 = Application.builder()
                .user(userByStudentId.get("20250051"))
                .clubApplyForm(formByClubName.get("인터엑스"))
                .status(Status.PENDING)
                .stage(Stage.INTERVIEW)
                .build();
        app51.updateAverageRating(3.0);
        apps.add(app51);

        // (52,1,'PENDING','FINAL',3.4)
        Application app52 = Application.builder()
                .user(userByStudentId.get("20250052"))
                .clubApplyForm(formByClubName.get("인터엑스"))
                .status(Status.PENDING)
                .stage(Stage.FINAL)
                .build();
        app52.updateAverageRating(3.4);
        apps.add(app52);

        // (53,1,'APPROVED','INTERVIEW',4.6)
        Application app53 = Application.builder()
                .user(userByStudentId.get("20250053"))
                .clubApplyForm(formByClubName.get("인터엑스"))
                .status(Status.APPROVED)
                .stage(Stage.INTERVIEW)
                .build();
        app53.updateAverageRating(4.6);
        apps.add(app53);

        // (54,1,'APPROVED','FINAL',4.7)
        Application app54 = Application.builder()
                .user(userByStudentId.get("20250054"))
                .clubApplyForm(formByClubName.get("인터엑스"))
                .status(Status.APPROVED)
                .stage(Stage.FINAL)
                .build();
        app54.updateAverageRating(4.7);
        apps.add(app54);

        // (55,1,'REJECTED','INTERVIEW',2.5)
        Application app55 = Application.builder()
                .user(userByStudentId.get("20250055"))
                .clubApplyForm(formByClubName.get("인터엑스"))
                .status(Status.REJECTED)
                .stage(Stage.INTERVIEW)
                .build();
        app55.updateAverageRating(2.5);
        apps.add(app55);

        // (56,1,'REJECTED','FINAL',2.6)
        Application app56 = Application.builder()
                .user(userByStudentId.get("20250056"))
                .clubApplyForm(formByClubName.get("인터엑스"))
                .status(Status.REJECTED)
                .stage(Stage.FINAL)
                .build();
        app56.updateAverageRating(2.6);
        apps.add(app56);

        // (57,1,'PENDING','INTERVIEW',3.3)
        Application app57 = Application.builder()
                .user(userByStudentId.get("20250057"))
                .clubApplyForm(formByClubName.get("인터엑스"))
                .status(Status.PENDING)
                .stage(Stage.INTERVIEW)
                .build();
        app57.updateAverageRating(3.3);
        apps.add(app57);

        // (58,1,'PENDING','FINAL',3.6)
        Application app58 = Application.builder()
                .user(userByStudentId.get("20250058"))
                .clubApplyForm(formByClubName.get("인터엑스"))
                .status(Status.PENDING)
                .stage(Stage.FINAL)
                .build();
        app58.updateAverageRating(3.6);
        apps.add(app58);

        // (59,1,'APPROVED','INTERVIEW',4.3)
        Application app59 = Application.builder()
                .user(userByStudentId.get("20250059"))
                .clubApplyForm(formByClubName.get("인터엑스"))
                .status(Status.APPROVED)
                .stage(Stage.INTERVIEW)
                .build();
        app59.updateAverageRating(4.3);
        apps.add(app59);

        // (60,1,'APPROVED','FINAL',4.8)
        Application app60 = Application.builder()
                .user(userByStudentId.get("20250060"))
                .clubApplyForm(formByClubName.get("인터엑스"))
                .status(Status.APPROVED)
                .stage(Stage.FINAL)
                .build();
        app60.updateAverageRating(4.8);
        apps.add(app60);

        // (61,1,'REJECTED','INTERVIEW',2.4)
        Application app61 = Application.builder()
                .user(userByStudentId.get("20250061"))
                .clubApplyForm(formByClubName.get("인터엑스"))
                .status(Status.REJECTED)
                .stage(Stage.INTERVIEW)
                .build();
        app61.updateAverageRating(2.4);
        apps.add(app61);

        // (62,1,'REJECTED','FINAL',2.7)
        Application app62 = Application.builder()
                .user(userByStudentId.get("20250062"))
                .clubApplyForm(formByClubName.get("인터엑스"))
                .status(Status.REJECTED)
                .stage(Stage.FINAL)
                .build();
        app62.updateAverageRating(2.7);
        apps.add(app62);

        // (63,1,'PENDING','INTERVIEW',3.5)
        Application app63 = Application.builder()
                .user(userByStudentId.get("20250063"))
                .clubApplyForm(formByClubName.get("인터엑스"))
                .status(Status.PENDING)
                .stage(Stage.INTERVIEW)
                .build();
        app63.updateAverageRating(3.5);
        apps.add(app63);

        // (64,1,'APPROVED','FINAL',4.4)
        Application app64 = Application.builder()
                .user(userByStudentId.get("20250064"))
                .clubApplyForm(formByClubName.get("인터엑스"))
                .status(Status.APPROVED)
                .stage(Stage.FINAL)
                .build();
        app64.updateAverageRating(4.4);
        apps.add(app64);

        // (65,3,'APPROVED','INTERVIEW',4.4)
        Application app65 = Application.builder()
                .user(userByStudentId.get("20250065"))
                .clubApplyForm(formByClubName.get("코드마스터"))
                .status(Status.APPROVED)
                .stage(Stage.INTERVIEW)
                .build();
        app65.updateAverageRating(4.4);
        apps.add(app65);

        // (66,3,'REJECTED','FINAL',2.9)
        Application app66 = Application.builder()
                .user(userByStudentId.get("20250066"))
                .clubApplyForm(formByClubName.get("코드마스터"))
                .status(Status.REJECTED)
                .stage(Stage.FINAL)
                .build();
        app66.updateAverageRating(2.9);
        apps.add(app66);

        // (67,3,'APPROVED','FINAL',4.4)
        Application app67 = Application.builder()
                .user(userByStudentId.get("20250067"))
                .clubApplyForm(formByClubName.get("코드마스터"))
                .status(Status.APPROVED)
                .stage(Stage.FINAL)
                .build();
        app67.updateAverageRating(4.4);
        apps.add(app67);

        return applicationRepository.saveAll(apps);
    }

    private void seedAnswers() {

        // Application 1 ~ 67 미리 로드
        Application[] apps = new Application[68]; // 1-based 사용
        for (long i = 1; i <= 67; i++) {
            long finalI = i;
            apps[(int) i] = applicationRepository.findById(i)
                    .orElseThrow(() -> new IllegalStateException("Application not found: " + finalI));
        }

        // FormQuestion 1 ~ 20 미리 로드
        FormQuestion[] questions = new FormQuestion[21]; // 1-based
        for (long i = 1; i <= 20; i++) {
            long finalI = i;
            questions[(int) i] = formQuestionRepository.findById(i)
                    .orElseThrow(() -> new IllegalStateException("FormQuestion not found: " + finalI));
        }

        List<Answer> answers = new ArrayList<>();

        // ============ application 1 ============
        answers.add(Answer.builder()
                .application(apps[1])
                .formQuestion(questions[1])
                .answer("안녕하세요. 컴퓨터공학과 3학년 홍길동입니다. 백엔드 개발과 서버 운영에 관심이 많습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[1])
                .formQuestion(questions[2])
                .answer("예, 개인 프로젝트로 간단한 게시판 서비스를 만들어 본 경험이 있습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[1])
                .formQuestion(questions[3])
                .answer("현재 자바와 Spring을 중심으로 웹 백엔드 개발을 공부하고 있으며, 동아리 활동을 통해 실제 서비스 개발을 경험해 보고 싶습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[1])
                .formQuestion(questions[4])
                .answer("2025-10-15 10:00~12:00 사이 면접 참여가 가능하며, 필요하다면 약간의 시간 조정도 가능합니다.")
                .build());

        // ============ application 2 ~ 20 (각 1개) ============
        answers.add(Answer.builder()
                .application(apps[2])
                .formQuestion(questions[2])
                .answer("예, 정기 모임과 세미나에 꾸준히 참여할 수 있는 일정입니다.")
                .build());

        answers.add(Answer.builder()
                .application(apps[3])
                .formQuestion(questions[3])
                .answer("월요일과 수요일 저녁, 토요일 오후에 정기 활동 참여가 가능합니다.")
                .build());

        answers.add(Answer.builder()
                .application(apps[4])
                .formQuestion(questions[4])
                .answer("학과 팀 프로젝트에서 백엔드 개발을 맡아 API 설계와 데이터베이스 연동을 경험해 본 적이 있습니다.")
                .build());

        answers.add(Answer.builder()
                .application(apps[5])
                .formQuestion(questions[5])
                .answer("책임감이 강해 맡은 일은 끝까지 해내는 편이지만, 완벽하게 해내고 싶어 결정이 느려질 때가 있는 편입니다.")
                .build());

        answers.add(Answer.builder()
                .application(apps[6])
                .formQuestion(questions[6])
                .answer("1년 안에 스스로 설계한 웹 서비스를 하나 이상 배포해 보고, 이후에는 인턴이나 공모전을 통해 실무 경험을 쌓는 것이 목표입니다.")
                .build());

        answers.add(Answer.builder()
                .application(apps[7])
                .formQuestion(questions[7])
                .answer("5인 팀으로 진행한 웹 서비스 프로젝트에서 역할을 분담하고 코드 리뷰를 통해 협업한 경험이 있습니다.")
                .build());

        answers.add(Answer.builder()
                .application(apps[8])
                .formQuestion(questions[8])
                .answer("백엔드 개발자로 참여해 API 설계, 비즈니스 로직 구현, 데이터베이스 모델링을 중심으로 기여하고 싶습니다.")
                .build());

        answers.add(Answer.builder()
                .application(apps[9])
                .formQuestion(questions[9])
                .answer("주간 단위로 해야 할 일을 정리하고, 캘린더와 할 일 목록을 사용해 마감일을 관리하며 우선순위에 따라 시간을 배분합니다.")
                .build());

        answers.add(Answer.builder()
                .application(apps[10])
                .formQuestion(questions[10])
                .answer("알고리즘 스터디에서 스터디장을 맡아 매주 문제를 선정하고 진행을 조율하며 팀원들을 이끌었던 경험이 있습니다.")
                .build());

        answers.add(Answer.builder()
                .application(apps[11])
                .formQuestion(questions[11])
                .answer("팀 프로젝트에서 일정 지연으로 갈등이 발생했을 때, 회의를 열어 서로의 상황을 공유하고 업무 분담을 다시 조정해 문제를 해결한 경험이 있습니다.")
                .build());

        answers.add(Answer.builder()
                .application(apps[12])
                .formQuestion(questions[12])
                .answer("클린 코드와 도메인 주도 설계 관련 서적을 읽으며 코드 품질과 설계 방식에 대해 많이 고민해 보았습니다.")
                .build());

        answers.add(Answer.builder()
                .application(apps[13])
                .formQuestion(questions[13])
                .answer("개인 블로그 서비스와 일정 관리 웹 서비스를 구현한 포트폴리오가 있으며, 코드와 설명을 GitHub에 정리해 두었습니다.")
                .build());

        answers.add(Answer.builder()
                .application(apps[14])
                .formQuestion(questions[14])
                .answer("학기 중에는 자바와 Spring 심화 학습에 집중하고, 방학에는 배포 자동화와 모니터링까지 포함한 전체 플로우를 경험해 볼 계획입니다.")
                .build());

        answers.add(Answer.builder()
                .application(apps[15])
                .formQuestion(questions[15])
                .answer("지역 아동센터에서 중·고등학생을 대상으로 기초 코딩과 IT 진로를 소개하는 봉사 활동에 참여한 경험이 있습니다.")
                .build());

        answers.add(Answer.builder()
                .application(apps[16])
                .formQuestion(questions[16])
                .answer("대학 1학년 때 IT 학회에 참여해 세미나를 준비하고, 교내 해커톤에 참가해 팀 프로젝트를 수행한 경험이 있습니다.")
                .build());

        answers.add(Answer.builder()
                .application(apps[17])
                .formQuestion(questions[17])
                .answer("교내 공모전에 출품한 스터디 매칭 플랫폼 프로젝트에서 백엔드 개발과 간단한 인프라 구성을 담당했습니다.")
                .build());

        answers.add(Answer.builder()
                .application(apps[18])
                .formQuestion(questions[18])
                .answer("새로운 기술을 빠르게 익히는 편이며, 문서화와 정리를 좋아해 팀원들이 이해하기 쉽도록 정리된 자료를 만드는 것이 강점입니다.")
                .build());

        answers.add(Answer.builder()
                .application(apps[19])
                .formQuestion(questions[19])
                .answer("여러 일을 한 번에 맡으려는 경향이 있어 가끔 과로하게 되지만, 최근에는 우선순위를 나누어 일정과 업무량을 조절하려고 노력하고 있습니다.")
                .build());

        answers.add(Answer.builder()
                .application(apps[20])
                .formQuestion(questions[20])
                .answer("동아리 활동을 통해 함께 성장하면서 실제로 사용할 수 있는 서비스를 만들어 보고 싶습니다. 적극적으로 참여하겠습니다.")
                .build());

        // ============ application 21 ============
        answers.add(Answer.builder()
                .application(apps[21])
                .formQuestion(questions[1])
                .answer("안녕하세요. 소프트웨어학부 2학년 김지원입니다. 백엔드와 협업 문화에 관심이 많습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[21])
                .formQuestion(questions[2])
                .answer("예, 정기 세션과 프로젝트에 꾸준히 참여할 수 있습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[21])
                .formQuestion(questions[3])
                .answer("팀 프로젝트를 좋아하고, 사람들과 의견을 나누며 서비스 방향을 정하는 과정을 즐깁니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[21])
                .formQuestion(questions[4])
                .answer("2025-10-16 14:00~16:00 면접 참여가 가능하며, 그 외 저녁 시간대도 조정 가능합니다.")
                .build());

        // ============ application 22 ============
        answers.add(Answer.builder()
                .application(apps[22])
                .formQuestion(questions[1])
                .answer("안녕하세요. 통계학과 3학년 이서준입니다. 데이터 분석과 서버 개발을 함께 경험해 보고 싶습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[22])
                .formQuestion(questions[2])
                .answer("아니오, 교환학생 준비로 인해 이번 학기에는 전 기간 활동이 어려워 한 학기 정도만 참여가 가능합니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[22])
                .formQuestion(questions[3])
                .answer("데이터 분석과 시각화에 관심이 많고, 분석 결과를 실제 서비스 기능으로 연결해 보고 싶습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[22])
                .formQuestion(questions[4])
                .answer("2025-10-15 10:00~12:00 사이 면접이 가장 좋으며, 오전 시간대 조정은 가능합니다.")
                .build());

        // ============ application 23 ============
        answers.add(Answer.builder()
                .application(apps[23])
                .formQuestion(questions[1])
                .answer("안녕하세요. 컴퓨터공학과 2학년 박민지입니다. 동아리 활동을 통해 실전 경험을 쌓고 싶습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[23])
                .formQuestion(questions[2])
                .answer("예, 학기 중 일정 조율이 가능해 정기 모임에 성실히 참여할 수 있습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[23])
                .formQuestion(questions[3])
                .answer("이미 다른 동아리에서 기획과 운영을 경험해 보았고, 이번에는 개발에 집중해 보고 싶습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[23])
                .formQuestion(questions[4])
                .answer("2025-10-16 14:00~16:00 면접 참여 가능하며, 이후 저녁 시간도 일부 가능합니다.")
                .build());

        // ============ application 24 ============
        answers.add(Answer.builder()
                .application(apps[24])
                .formQuestion(questions[1])
                .answer("안녕하세요. 정보통신공학과 4학년 정영훈입니다. 졸업 전 마지막으로 의미 있는 프로젝트를 해 보고 싶습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[24])
                .formQuestion(questions[2])
                .answer("예, 졸업 학기이지만 시간 조율이 가능해 정기 활동에 참여할 수 있습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[24])
                .formQuestion(questions[3])
                .answer("과 대표와 프로젝트 팀장을 맡아본 경험이 있어 리더십과 책임감을 발휘하는 데 자신이 있습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[24])
                .formQuestion(questions[4])
                .answer("2025-10-15 10:00~12:00 면접을 선호하지만, 오후에도 일정 조정이 가능합니다.")
                .build());

        // ============ application 25 ============
        answers.add(Answer.builder()
                .application(apps[25])
                .formQuestion(questions[1])
                .answer("안녕하세요. 미디어학부 2학년 한서윤입니다. 프론트엔드와 디자인 협업에 관심이 많습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[25])
                .formQuestion(questions[2])
                .answer("예, 수업과 병행 가능하여 주당 정해진 시간 이상 활동할 수 있습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[25])
                .formQuestion(questions[3])
                .answer("프론트엔드와 UI에 관심이 있어 사용자 입장에서 보기 편한 화면을 만드는 것을 좋아합니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[25])
                .formQuestion(questions[4])
                .answer("2025-10-16 14:00~16:00 면접 참여 가능하며, 저녁 시간도 일부 가능합니다.")
                .build());

        // ============ application 26 ============
        answers.add(Answer.builder()
                .application(apps[26])
                .formQuestion(questions[1])
                .answer("안녕하세요. 컴퓨터공학과 1학년 지원자입니다. 학교 생활과 함께 개발 경험을 쌓고 싶습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[26])
                .formQuestion(questions[2])
                .answer("예, 수업 이후 저녁 시간에 정기 모임에 꾸준히 참여할 수 있습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[26])
                .formQuestion(questions[3])
                .answer("기초 문법을 배우는 단계이지만, 프로젝트를 통해 빠르게 성장하고 싶어 동아리에 지원했습니다.")
                .build());

        // ============ application 27 ============
        answers.add(Answer.builder()
                .application(apps[27])
                .formQuestion(questions[1])
                .answer("안녕하세요. 소프트웨어학부 2학년 지원자입니다. 웹 개발 전반을 배우고 싶습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[27])
                .formQuestion(questions[2])
                .answer("예, 시험 기간을 제외하고 대부분의 정기 활동에 참석할 수 있습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[27])
                .formQuestion(questions[3])
                .answer("혼자 공부하는 것보다 함께 공부하며 피드백을 주고받는 것을 좋아해 동아리를 찾게 되었습니다.")
                .build());

        // ============ application 28 ============
        answers.add(Answer.builder()
                .application(apps[28])
                .formQuestion(questions[1])
                .answer("안녕하세요. 정보보호학과 3학년 지원자입니다. 보안과 서버 개발에 모두 관심이 있습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[28])
                .formQuestion(questions[2])
                .answer("예, 주중 저녁과 주말 일부 시간을 동아리 활동에 투자할 수 있습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[28])
                .formQuestion(questions[3])
                .answer("기초적인 웹 보안 지식을 갖추고 있으며, 안전한 백엔드 구조를 고민해 보고 싶습니다.")
                .build());

        // ============ application 29 ============
        answers.add(Answer.builder()
                .application(apps[29])
                .formQuestion(questions[1])
                .answer("안녕하세요. 전자공학과 2학년 지원자입니다. 하드웨어와 소프트웨어를 함께 이해하고 싶습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[29])
                .formQuestion(questions[2])
                .answer("예, 실험 과목과 병행하면서도 정기 모임에 참여할 수 있는 일정입니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[29])
                .formQuestion(questions[3])
                .answer("임베디드와 서버를 연결하는 프로젝트에 관심이 있어 관련 경험을 쌓고자 지원했습니다.")
                .build());

        // ============ application 30 ============
        answers.add(Answer.builder()
                .application(apps[30])
                .formQuestion(questions[1])
                .answer("안녕하세요. 컴퓨터공학과 4학년 지원자입니다. 졸업 전 팀 프로젝트 경험을 더 쌓고 싶습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[30])
                .formQuestion(questions[2])
                .answer("예, 졸업 준비와 병행하면서도 주 2회 이상 활동이 가능합니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[30])
                .formQuestion(questions[3])
                .answer("이미 몇 차례 팀 프로젝트를 진행해 봤으며, 이번에는 서비스 완성도에 더 집중해보고 싶습니다.")
                .build());

        // ============ application 31 ============
        answers.add(Answer.builder()
                .application(apps[31])
                .formQuestion(questions[1])
                .answer("안녕하세요. 인공지능학과 1학년 지원자입니다. AI와 백엔드의 연계를 배우고 싶습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[31])
                .formQuestion(questions[2])
                .answer("예, 학기 대부분의 기간 동안 정기 활동이 가능합니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[31])
                .formQuestion(questions[3])
                .answer("파이썬과 기초 머신러닝을 공부 중이고, 이를 실제 서비스에 적용해 보고 싶습니다.")
                .build());

        // ============ application 32 ============
        answers.add(Answer.builder()
                .application(apps[32])
                .formQuestion(questions[1])
                .answer("안녕하세요. 수학과 3학년 지원자입니다. 알고리즘과 논리적인 문제 해결을 좋아합니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[32])
                .formQuestion(questions[2])
                .answer("예, 학기 중 평일 저녁에 꾸준히 참여할 수 있습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[32])
                .formQuestion(questions[3])
                .answer("수학적 사고를 바탕으로 안정적인 서버 로직을 설계해 보고 싶습니다.")
                .build());

        // ============ application 33 ============
        answers.add(Answer.builder()
                .application(apps[33])
                .formQuestion(questions[1])
                .answer("안녕하세요. 경영학과 2학년 지원자입니다. 서비스 기획과 개발 협업에 관심이 있습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[33])
                .formQuestion(questions[2])
                .answer("예, 수업 외 시간에 프로젝트와 스터디에 적극적으로 참여할 수 있습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[33])
                .formQuestion(questions[3])
                .answer("기획 관점에서 아이디어를 제안하고, 개발 과정도 함께 이해하고 싶어 지원했습니다.")
                .build());

        // ============ application 34 ============
        answers.add(Answer.builder()
                .application(apps[34])
                .formQuestion(questions[1])
                .answer("안녕하세요. 산업공학과 3학년 지원자입니다. 데이터 기반 서비스에 관심이 많습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[34])
                .formQuestion(questions[2])
                .answer("예, 평일 저녁과 주말 일부 시간을 투자할 수 있습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[34])
                .formQuestion(questions[3])
                .answer("프로세스 개선과 데이터 분석 경험을 바탕으로 서비스 품질을 높이는 데 기여하고 싶습니다.")
                .build());

        // ============ application 35 ============
        answers.add(Answer.builder()
                .application(apps[35])
                .formQuestion(questions[1])
                .answer("안녕하세요. 컴퓨터공학과 복수전공 중인 인문대학 3학년 지원자입니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[35])
                .formQuestion(questions[2])
                .answer("예, 일정 조율을 통해 정기 모임에 참여할 수 있습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[35])
                .formQuestion(questions[3])
                .answer("문과와 이과의 관점을 모두 가진 구성원으로서 소통과 문서화에 강점이 있습니다.")
                .build());

        // ============ application 36 ============
        answers.add(Answer.builder()
                .application(apps[36])
                .formQuestion(questions[1])
                .answer("안녕하세요. 소프트웨어학부 1학년 지원자입니다. 개발을 처음부터 제대로 배우고 싶습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[36])
                .formQuestion(questions[2])
                .answer("예, 주중 저녁 시간에 꾸준히 활동할 수 있습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[36])
                .formQuestion(questions[3])
                .answer("아직은 기초 단계지만 열정적으로 배우며 팀에 도움이 되고 싶습니다.")
                .build());

        // ============ application 37 ============
        answers.add(Answer.builder()
                .application(apps[37])
                .formQuestion(questions[1])
                .answer("안녕하세요. 정보통신공학과 2학년 지원자입니다. 네트워크와 서버에 관심이 있습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[37])
                .formQuestion(questions[2])
                .answer("예, 주당 2회 이상 정기 모임 참여가 가능합니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[37])
                .formQuestion(questions[3])
                .answer("네트워크 지식을 살려 안정적인 서비스 구조를 설계해 보고 싶습니다.")
                .build());

        // ============ application 38 ============
        answers.add(Answer.builder()
                .application(apps[38])
                .formQuestion(questions[1])
                .answer("안녕하세요. 데이터사이언스 전공 2학년 지원자입니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[38])
                .formQuestion(questions[2])
                .answer("예, 프로젝트와 스터디에 적극적으로 참여할 수 있습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[38])
                .formQuestion(questions[3])
                .answer("데이터 분석 결과를 실제 기능으로 구현하는 경험을 해 보고 싶습니다.")
                .build());

        // ============ application 39 ============
        answers.add(Answer.builder()
                .application(apps[39])
                .formQuestion(questions[1])
                .answer("안녕하세요. 글로벌소프트웨어학과 1학년 지원자입니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[39])
                .formQuestion(questions[2])
                .answer("예, 영어 강의와 병행하면서도 동아리 활동에 참여할 수 있습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[39])
                .formQuestion(questions[3])
                .answer("영어 자료를 활용해 최신 기술 트렌드를 팀원들과 공유하고 싶습니다.")
                .build());

        // ============ application 40 ============
        answers.add(Answer.builder()
                .application(apps[40])
                .formQuestion(questions[1])
                .answer("안녕하세요. 컴퓨터공학과 편입 예정인 지원자입니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[40])
                .formQuestion(questions[2])
                .answer("예, 학교 적응과 함께 동아리 활동에도 적극적으로 참여할 계획입니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[40])
                .formQuestion(questions[3])
                .answer("다른 학교에서의 경험을 바탕으로 다양한 관점을 공유할 수 있습니다.")
                .build());

        // ============ application 41 ============
        answers.add(Answer.builder()
                .application(apps[41])
                .formQuestion(questions[1])
                .answer("안녕하세요. 소프트웨어학부 휴학생 지원자입니다. 올해는 개발에 집중하고 싶습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[41])
                .formQuestion(questions[2])
                .answer("예, 휴학 기간 동안 시간 제약 없이 활동에 참여할 수 있습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[41])
                .formQuestion(questions[3])
                .answer("시간이 여유로운 만큼 프로젝트에 깊게 관여해 완성도를 높이고 싶습니다.")
                .build());

        // ============ application 42 ============
        answers.add(Answer.builder()
                .application(apps[42])
                .formQuestion(questions[1])
                .answer("안녕하세요. 컴퓨터공학과 2학년 지원자입니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[42])
                .formQuestion(questions[2])
                .answer("예, 수업과 과제를 제외한 대부분의 시간을 개발 공부에 쓰고 있습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[42])
                .formQuestion(questions[3])
                .answer("혼자 공부하던 백엔드 기술을 팀 프로젝트로 확장해 보고 싶습니다.")
                .build());

        // ============ application 43 ============
        answers.add(Answer.builder()
                .application(apps[43])
                .formQuestion(questions[1])
                .answer("안녕하세요. IT융합학과 3학년 지원자입니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[43])
                .formQuestion(questions[2])
                .answer("예, 학기 중 주 2~3회 정기 활동이 가능합니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[43])
                .formQuestion(questions[3])
                .answer("하드웨어와 소프트웨어를 함께 다루는 경험을 해 보고 싶습니다.")
                .build());

        // ============ application 44 ============
        answers.add(Answer.builder()
                .application(apps[44])
                .formQuestion(questions[1])
                .answer("안녕하세요. 융합전공을 준비 중인 2학년 지원자입니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[44])
                .formQuestion(questions[2])
                .answer("예, 프로젝트 일정에 맞춰 성실히 참여할 수 있습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[44])
                .formQuestion(questions[3])
                .answer("다양한 전공을 접해 본 경험을 바탕으로 팀에 새로운 시각을 더하고 싶습니다.")
                .build());

        // ============ application 45 ============
        answers.add(Answer.builder()
                .application(apps[45])
                .formQuestion(questions[1])
                .answer("안녕하세요. 컴퓨터공학과 3학년 지원자입니다. 팀 프로젝트 경험을 더 쌓고 싶습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[45])
                .formQuestion(questions[2])
                .answer("예, 정기 회의와 추가적인 프로젝트 일정 모두 참여 가능합니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[45])
                .formQuestion(questions[3])
                .answer("여러 팀 프로젝트를 진행하며 협업 도구와 코드 리뷰에 익숙해졌습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[45])
                .formQuestion(questions[4])
                .answer("2025-10-15 10:00~12:00 면접 참여가 가능하며, 이후 오후 시간도 일부 가능합니다.")
                .build());

        // ============ application 46 ============
        answers.add(Answer.builder()
                .application(apps[46])
                .formQuestion(questions[1])
                .answer("안녕하세요. 소프트웨어학부 2학년 지원자입니다. 알고리즘과 자료구조 공부를 꾸준히 하고 있습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[46])
                .formQuestion(questions[2])
                .answer("아니오, 현재는 시험 준비 기간과 겹쳐 모든 활동에 참여하기는 어렵습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[46])
                .formQuestion(questions[3])
                .answer("알고리즘 문제 풀이를 즐기며, 이를 실제 서비스 성능 최적화에 적용해 보고 싶습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[46])
                .formQuestion(questions[4])
                .answer("2025-10-16 14:00~16:00 사이 면접 참여가 가능합니다.")
                .build());

        // ============ application 47 ============
        answers.add(Answer.builder()
                .application(apps[47])
                .formQuestion(questions[1])
                .answer("안녕하세요. 컴퓨터공학과 1학년 지원자입니다. 자바와 Spring을 중심으로 공부하고 있습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[47])
                .formQuestion(questions[2])
                .answer("예, 수업 이후 대부분의 저녁 시간에 활동할 수 있습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[47])
                .formQuestion(questions[3])
                .answer("백엔드 개발자로 성장하는 것을 목표로 기본기를 탄탄히 다지는 중입니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[47])
                .formQuestion(questions[4])
                .answer("2025-10-15 10:00~12:00 면접을 선호합니다.")
                .build());

        // ============ application 48 ============
        answers.add(Answer.builder()
                .application(apps[48])
                .formQuestion(questions[1])
                .answer("안녕하세요. 통계학과 2학년 지원자입니다. 데이터 분석에 관심이 많습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[48])
                .formQuestion(questions[2])
                .answer("아니오, 현재는 다른 프로젝트와 일정이 겹쳐 전 기간 활동은 어렵습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[48])
                .formQuestion(questions[3])
                .answer("분석한 데이터를 서비스 기능과 연결해 보는 경험을 쌓고 싶습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[48])
                .formQuestion(questions[4])
                .answer("2025-10-16 14:00~16:00 면접 참여가 가능합니다.")
                .build());

        // ============ application 49 ============
        answers.add(Answer.builder()
                .application(apps[49])
                .formQuestion(questions[1])
                .answer("안녕하세요. 컴퓨터공학과 3학년 지원자입니다. 웹 백엔드 분야를 준비하고 있습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[49])
                .formQuestion(questions[2])
                .answer("예, 학기 내내 정기 활동에 참여할 수 있습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[49])
                .formQuestion(questions[3])
                .answer("HTTP와 REST API에 대해 공부하고 있으며, 실무에 가까운 프로젝트를 경험하고 싶습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[49])
                .formQuestion(questions[4])
                .answer("2025-10-15 10:00~12:00 면접이 가장 적합합니다.")
                .build());

        // ============ application 50 ============
        answers.add(Answer.builder()
                .application(apps[50])
                .formQuestion(questions[1])
                .answer("안녕하세요. 소프트웨어학부 2학년 지원자입니다. 협업과 커뮤니케이션에 강점이 있습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[50])
                .formQuestion(questions[2])
                .answer("아니오, 현재는 교내 활동과 겹쳐 모든 세션에 참여하기는 어렵습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[50])
                .formQuestion(questions[3])
                .answer("팀원들과 적극적으로 소통하며 문제를 함께 해결하는 과정을 좋아합니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[50])
                .formQuestion(questions[4])
                .answer("2025-10-16 14:00~16:00 면접 참여가 가능합니다.")
                .build());

        // ============ application 51 ============
        answers.add(Answer.builder()
                .application(apps[51])
                .formQuestion(questions[1])
                .answer("안녕하세요. 컴퓨터공학과 3학년 지원자입니다. 테스트 코드 작성에 관심이 많습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[51])
                .formQuestion(questions[2])
                .answer("예, 프로젝트와 스터디 일정에 맞추어 꾸준히 참여할 수 있습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[51])
                .formQuestion(questions[3])
                .answer("단위 테스트와 통합 테스트를 통해 안정적인 코드를 만드는 것에 흥미를 느끼고 있습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[51])
                .formQuestion(questions[4])
                .answer("2025-10-15 10:00~12:00 면접 참여가 가능합니다.")
                .build());

        // ============ application 52 ============
        answers.add(Answer.builder()
                .application(apps[52])
                .formQuestion(questions[1])
                .answer("안녕하세요. 디자인과 복수전공 중인 소프트웨어학부 2학년 지원자입니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[52])
                .formQuestion(questions[2])
                .answer("아니오, 디자인 스튜디오 일정으로 인해 모든 활동에 참여하기는 어렵습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[52])
                .formQuestion(questions[3])
                .answer("UI/UX 관점에서 사용성을 고려한 화면을 설계하고, 개발자와의 협업을 경험해 보고 싶습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[52])
                .formQuestion(questions[4])
                .answer("2025-10-16 14:00~16:00 면접이 가장 좋습니다.")
                .build());

        // ============ application 53 ============
        answers.add(Answer.builder()
                .application(apps[53])
                .formQuestion(questions[1])
                .answer("안녕하세요. 컴퓨터공학과 2학년 지원자입니다. REST API 설계에 관심이 많습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[53])
                .formQuestion(questions[2])
                .answer("예, 프로젝트 중심 활동에 적극적으로 참여할 수 있습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[53])
                .formQuestion(questions[3])
                .answer("실사용자를 고려한 API 설계를 동아리 활동을 통해 직접 경험해 보고 싶습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[53])
                .formQuestion(questions[4])
                .answer("2025-10-15 10:00~12:00 면접 참여가 가능합니다.")
                .build());

        // ============ application 54 ============
        answers.add(Answer.builder()
                .application(apps[54])
                .formQuestion(questions[1])
                .answer("안녕하세요. 정보시스템 전공 3학년 지원자입니다. DB 설계와 튜닝에 관심이 있습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[54])
                .formQuestion(questions[2])
                .answer("아니오, 현재 다른 연구실 활동과 병행 중이라 모든 일정 참석은 어렵습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[54])
                .formQuestion(questions[3])
                .answer("효율적인 쿼리와 스키마 설계를 고민하며 서비스 성능을 높이는 데 기여하고 싶습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[54])
                .formQuestion(questions[4])
                .answer("2025-10-16 14:00~16:00 면접 참여가 가능합니다.")
                .build());

        // ============ application 55 ============
        answers.add(Answer.builder()
                .application(apps[55])
                .formQuestion(questions[1])
                .answer("안녕하세요. 컴퓨터공학과 3학년 지원자입니다. 형상 관리 도구 사용에 익숙합니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[55])
                .formQuestion(questions[2])
                .answer("예, 정기 회의와 코드 리뷰 시간에 꾸준히 참여할 수 있습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[55])
                .formQuestion(questions[3])
                .answer("Git을 활용한 브랜치 전략과 코드 리뷰 문화를 동아리에서 함께 만들어 보고 싶습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[55])
                .formQuestion(questions[4])
                .answer("2025-10-15 10:00~12:00 면접이 가능하며, 오후 시간도 일부 조정 가능합니다.")
                .build());

        // ============ application 56 ============
        answers.add(Answer.builder()
                .application(apps[56])
                .formQuestion(questions[1])
                .answer("안녕하세요. 소프트웨어학부 2학년 지원자입니다. 테스트 자동화에 관심이 있습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[56])
                .formQuestion(questions[2])
                .answer("아니오, 일부 주차는 학교 수업과 시간대가 겹쳐 참석이 어려울 수 있습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[56])
                .formQuestion(questions[3])
                .answer("CI 도구를 활용해 테스트를 자동화하고 배포 과정에 녹여내는 경험을 해 보고 싶습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[56])
                .formQuestion(questions[4])
                .answer("2025-10-16 14:00~16:00 면접 참여가 가능합니다.")
                .build());

        // ============ application 57 ============
        answers.add(Answer.builder()
                .application(apps[57])
                .formQuestion(questions[1])
                .answer("안녕하세요. 컴퓨터공학과 3학년 지원자입니다. 클린 코드를 지향합니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[57])
                .formQuestion(questions[2])
                .answer("예, 프로젝트와 스터디 모두 적극적으로 참여할 수 있습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[57])
                .formQuestion(questions[3])
                .answer("가독성과 유지보수성을 고려한 코드를 작성하는 습관을 가지고 있습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[57])
                .formQuestion(questions[4])
                .answer("2025-10-15 10:00~12:00 면접을 희망합니다.")
                .build());

        // ============ application 58 ============
        answers.add(Answer.builder()
                .application(apps[58])
                .formQuestion(questions[1])
                .answer("안녕하세요. 소프트웨어학부 2학년 지원자입니다. 배포 자동화에 관심이 많습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[58])
                .formQuestion(questions[2])
                .answer("아니오, 현재는 인프라 스터디 일정과 겹쳐 전 일정 참여는 어렵습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[58])
                .formQuestion(questions[3])
                .answer("CI/CD 파이프라인을 구축해 서비스 배포를 자동화하는 경험을 쌓고 싶습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[58])
                .formQuestion(questions[4])
                .answer("2025-10-16 14:00~16:00 면접 참여가 가능합니다.")
                .build());

        // ============ application 59 ============
        answers.add(Answer.builder()
                .application(apps[59])
                .formQuestion(questions[1])
                .answer("안녕하세요. 컴퓨터공학과 3학년 지원자입니다. 도커와 CI 경험이 있습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[59])
                .formQuestion(questions[2])
                .answer("예, 학기 중에도 꾸준히 활동할 수 있습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[59])
                .formQuestion(questions[3])
                .answer("도커와 GitHub Actions를 활용해 간단한 배포 자동화를 구성해 본 경험이 있습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[59])
                .formQuestion(questions[4])
                .answer("2025-10-15 10:00~12:00 면접이 가장 적합합니다.")
                .build());

        // ============ application 60 ============
        answers.add(Answer.builder()
                .application(apps[60])
                .formQuestion(questions[1])
                .answer("안녕하세요. 소프트웨어학부 2학년 지원자입니다. 코드 리뷰 경험이 있습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[60])
                .formQuestion(questions[2])
                .answer("아니오, 시험 기간에는 일부 활동 참여가 어려울 수 있습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[60])
                .formQuestion(questions[3])
                .answer("코드 리뷰를 통해 서로의 코드를 이해하고 개선하는 과정을 중요하게 생각합니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[60])
                .formQuestion(questions[4])
                .answer("2025-10-16 14:00~16:00 면접 참여가 가능합니다.")
                .build());

        // ============ application 61 ============
        answers.add(Answer.builder()
                .application(apps[61])
                .formQuestion(questions[1])
                .answer("안녕하세요. 인공지능학과 2학년 지원자입니다. JPA를 공부하고 있습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[61])
                .formQuestion(questions[2])
                .answer("예, 정기 세션과 스터디 모임에 성실히 참여할 수 있습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[61])
                .formQuestion(questions[3])
                .answer("JPA와 객체 지향 설계를 실제 프로젝트에 적용해 보고 싶어 동아리에 지원했습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[61])
                .formQuestion(questions[4])
                .answer("2025-10-15 10:00~12:00 면접이 가장 좋습니다.")
                .build());

        // ============ application 62 ============
        answers.add(Answer.builder()
                .application(apps[62])
                .formQuestion(questions[1])
                .answer("안녕하세요. 컴퓨터공학과 3학년 지원자입니다. 성능 최적화에 관심이 있습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[62])
                .formQuestion(questions[2])
                .answer("아니오, 일부 시간대는 연구실 일정으로 인해 참여가 어렵습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[62])
                .formQuestion(questions[3])
                .answer("프로파일링 도구를 활용해 병목 구간을 찾고 개선하는 과정에 흥미를 느낍니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[62])
                .formQuestion(questions[4])
                .answer("2025-10-16 14:00~16:00 사이 면접 참여가 가능합니다.")
                .build());

        // ============ application 63 ============
        answers.add(Answer.builder()
                .application(apps[63])
                .formQuestion(questions[1])
                .answer("안녕하세요. 소프트웨어학부 2학년 지원자입니다. 이벤트 기반 설계에 관심이 있습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[63])
                .formQuestion(questions[2])
                .answer("예, 프로젝트 위주 활동에 적극적으로 참여할 수 있습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[63])
                .formQuestion(questions[3])
                .answer("이벤트 기반 아키텍처를 공부하며, 실제 동아리 프로젝트에 적용해 보고 싶습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[63])
                .formQuestion(questions[4])
                .answer("2025-10-15 10:00~12:00 면접을 희망합니다.")
                .build());

        // ============ application 64 ============
        answers.add(Answer.builder()
                .application(apps[64])
                .formQuestion(questions[1])
                .answer("안녕하세요. 컴퓨터공학과 3학년 지원자입니다. 모니터링과 로깅에 관심이 있습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[64])
                .formQuestion(questions[2])
                .answer("아니오, 현재 인턴십과 병행 중이라 모든 일정 참석은 어렵습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[64])
                .formQuestion(questions[3])
                .answer("로그를 기반으로 장애 상황을 분석하고, 모니터링 대시보드를 구성해 보고 싶습니다.")
                .build());
        answers.add(Answer.builder()
                .application(apps[64])
                .formQuestion(questions[4])
                .answer("2025-10-16 14:00~16:00 면접 참여가 가능합니다.")
                .build());

        // ============ application 65 ~ 67 (각 1개) ============
        answers.add(Answer.builder()
                .application(apps[65])
                .formQuestion(questions[1])
                .answer("안녕하세요. 컴퓨터공학과 1학년 지원자입니다. 기본기를 다지며 꾸준히 성장하고 싶습니다.")
                .build());

        answers.add(Answer.builder()
                .application(apps[66])
                .formQuestion(questions[1])
                .answer("안녕하세요. 소프트웨어학부 복수전공을 준비 중인 2학년 지원자입니다. 개발과 기획을 함께 경험해 보고 싶습니다.")
                .build());

        answers.add(Answer.builder()
                .application(apps[67])
                .formQuestion(questions[1])
                .answer("안녕하세요. 데이터 엔지니어링과 백엔드 개발에 관심이 많은 3학년 지원자입니다.")
                .build());

        // 실제 insert
        answerRepository.saveAll(answers);
    }

    private void seedClubMembers(List<User> users, List<Club> clubs, List<Application> applications) {
        //if (clubMemberRepository.count() > 0) return;

        User[] userArr = new User[users.size() + 1];
        for (int i = 0; i < users.size(); i++) {
            userArr[i + 1] = users.get(i);
        }

        Club[] clubArr = new Club[clubs.size() + 1];
        for (int i = 0; i < clubs.size(); i++) {
            clubArr[i + 1] = clubs.get(i);
        }

        Application[] apps = new Application[applications.size() + 1];
        for (int i = 0; i < applications.size(); i++) {
            apps[i + 1] = applications.get(i);
        }

        List<ClubMember> members = new ArrayList<>();

        members.add(ClubMember.builder()
                .user(userArr[1])
                .club(clubArr[1])
                .application(apps[1])
                .activeStatus(ActiveStatus.INACTIVE)
                .role(Role.CLUB_MEMBER)
                .build());

        members.add(ClubMember.builder()
                .user(userArr[2])
                .club(clubArr[2])
                .application(apps[2])
                .activeStatus(ActiveStatus.ACTIVE)
                .role(Role.CLUB_EXECUTIVE)
                .build());

        members.add(ClubMember.builder()
                .user(userArr[3])
                .club(clubArr[3])
                .application(apps[3])
                .activeStatus(ActiveStatus.INACTIVE)
                .role(Role.CLUB_MEMBER)
                .build());

        members.add(ClubMember.builder()
                .user(userArr[4])
                .club(clubArr[4])
                .application(apps[4])
                .activeStatus(ActiveStatus.ACTIVE)
                .role(Role.APPLICANT)
                .build());

        members.add(ClubMember.builder()
                .user(userArr[5])
                .club(clubArr[5])
                .application(apps[5])
                .activeStatus(ActiveStatus.INACTIVE)
                .role(Role.SYSTEM_ADMIN)
                .build());

        members.add(ClubMember.builder()
                .user(userArr[6])
                .club(clubArr[6])
                .application(apps[6])
                .activeStatus(ActiveStatus.ACTIVE)
                .role(Role.CLUB_MEMBER)
                .build());

        members.add(ClubMember.builder()
                .user(userArr[7])
                .club(clubArr[7])
                .application(apps[7])
                .activeStatus(ActiveStatus.INACTIVE)
                .role(Role.CLUB_EXECUTIVE)
                .build());

        members.add(ClubMember.builder()
                .user(userArr[8])
                .club(clubArr[8])
                .application(apps[8])
                .activeStatus(ActiveStatus.ACTIVE)
                .role(Role.CLUB_MEMBER)
                .build());

        members.add(ClubMember.builder()
                .user(userArr[9])
                .club(clubArr[9])
                .application(apps[9])
                .activeStatus(ActiveStatus.INACTIVE)
                .role(Role.APPLICANT)
                .build());

        members.add(ClubMember.builder()
                .user(userArr[10])
                .club(clubArr[10])
                .application(apps[10])
                .activeStatus(ActiveStatus.ACTIVE)
                .role(Role.SYSTEM_ADMIN)
                .build());

        members.add(ClubMember.builder()
                .user(userArr[11])
                .club(clubArr[11])
                .application(apps[11])
                .activeStatus(ActiveStatus.INACTIVE)
                .role(Role.CLUB_MEMBER)
                .build());

        members.add(ClubMember.builder()
                .user(userArr[12])
                .club(clubArr[12])
                .application(apps[12])
                .activeStatus(ActiveStatus.ACTIVE)
                .role(Role.CLUB_EXECUTIVE)
                .build());

        members.add(ClubMember.builder()
                .user(userArr[13])
                .club(clubArr[13])
                .application(apps[13])
                .activeStatus(ActiveStatus.INACTIVE)
                .role(Role.CLUB_MEMBER)
                .build());

        members.add(ClubMember.builder()
                .user(userArr[14])
                .club(clubArr[14])
                .application(apps[14])
                .activeStatus(ActiveStatus.ACTIVE)
                .role(Role.APPLICANT)
                .build());

        members.add(ClubMember.builder()
                .user(userArr[15])
                .club(clubArr[15])
                .application(apps[15])
                .activeStatus(ActiveStatus.INACTIVE)
                .role(Role.SYSTEM_ADMIN)
                .build());

        members.add(ClubMember.builder()
                .user(userArr[16])
                .club(clubArr[16])
                .application(apps[16])
                .activeStatus(ActiveStatus.ACTIVE)
                .role(Role.CLUB_MEMBER)
                .build());

        members.add(ClubMember.builder()
                .user(userArr[17])
                .club(clubArr[17])
                .application(apps[17])
                .activeStatus(ActiveStatus.INACTIVE)
                .role(Role.CLUB_EXECUTIVE)
                .build());

        members.add(ClubMember.builder()
                .user(userArr[18])
                .club(clubArr[18])
                .application(apps[18])
                .activeStatus(ActiveStatus.ACTIVE)
                .role(Role.CLUB_MEMBER)
                .build());

        members.add(ClubMember.builder()
                .user(userArr[19])
                .club(clubArr[19])
                .application(apps[19])
                .activeStatus(ActiveStatus.INACTIVE)
                .role(Role.APPLICANT)
                .build());

        members.add(ClubMember.builder()
                .user(userArr[20])
                .club(clubArr[20])
                .application(apps[20])
                .activeStatus(ActiveStatus.ACTIVE)
                .role(Role.SYSTEM_ADMIN)
                .build());

        members.add(ClubMember.builder()
                .user(userArr[21])
                .club(clubArr[1])
                .application(apps[21])
                .activeStatus(ActiveStatus.ACTIVE)
                .role(Role.APPLICANT)
                .build());

        members.add(ClubMember.builder()
                .user(userArr[22])
                .club(clubArr[1])
                .application(apps[22])
                .activeStatus(ActiveStatus.ACTIVE)
                .role(Role.APPLICANT)
                .build());

        members.add(ClubMember.builder()
                .user(userArr[23])
                .club(clubArr[1])
                .application(apps[23])
                .activeStatus(ActiveStatus.ACTIVE)
                .role(Role.APPLICANT)
                .build());

        members.add(ClubMember.builder()
                .user(userArr[24])
                .club(clubArr[1])
                .application(apps[24])
                .activeStatus(ActiveStatus.INACTIVE)
                .role(Role.APPLICANT)
                .build());

        members.add(ClubMember.builder()
                .user(userArr[25])
                .club(clubArr[1])
                .application(apps[25])
                .activeStatus(ActiveStatus.ACTIVE)
                .role(Role.CLUB_ADMIN)
                .build());

        members.add(ClubMember.builder()
                .user(userArr[26])
                .club(clubArr[2])
                .application(apps[26])
                .activeStatus(ActiveStatus.ACTIVE)
                .role(Role.CLUB_ADMIN)
                .build());

        members.add(ClubMember.builder()
                .user(userArr[27])
                .club(clubArr[3])
                .application(apps[27])
                .activeStatus(ActiveStatus.ACTIVE)
                .role(Role.CLUB_ADMIN)
                .build());

        members.add(ClubMember.builder()
                .user(userArr[28])
                .club(clubArr[4])
                .application(apps[28])
                .activeStatus(ActiveStatus.ACTIVE)
                .role(Role.CLUB_ADMIN)
                .build());

        members.add(ClubMember.builder()
                .user(userArr[29])
                .club(clubArr[5])
                .application(apps[29])
                .activeStatus(ActiveStatus.ACTIVE)
                .role(Role.CLUB_ADMIN)
                .build());

        members.add(ClubMember.builder()
                .user(userArr[30])
                .club(clubArr[6])
                .application(apps[30])
                .activeStatus(ActiveStatus.ACTIVE)
                .role(Role.CLUB_ADMIN)
                .build());

        members.add(ClubMember.builder()
                .user(userArr[31])
                .club(clubArr[7])
                .application(apps[31])
                .activeStatus(ActiveStatus.ACTIVE)
                .role(Role.CLUB_ADMIN)
                .build());

        members.add(ClubMember.builder()
                .user(userArr[32])
                .club(clubArr[8])
                .application(apps[32])
                .activeStatus(ActiveStatus.ACTIVE)
                .role(Role.CLUB_ADMIN)
                .build());

        members.add(ClubMember.builder()
                .user(userArr[33])
                .club(clubArr[9])
                .application(apps[33])
                .activeStatus(ActiveStatus.ACTIVE)
                .role(Role.CLUB_ADMIN)
                .build());

        members.add(ClubMember.builder()
                .user(userArr[34])
                .club(clubArr[10])
                .application(apps[34])
                .activeStatus(ActiveStatus.ACTIVE)
                .role(Role.CLUB_ADMIN)
                .build());

        members.add(ClubMember.builder()
                .user(userArr[35])
                .club(clubArr[11])
                .application(apps[35])
                .activeStatus(ActiveStatus.ACTIVE)
                .role(Role.CLUB_ADMIN)
                .build());

        members.add(ClubMember.builder()
                .user(userArr[36])
                .club(clubArr[12])
                .application(apps[36])
                .activeStatus(ActiveStatus.ACTIVE)
                .role(Role.CLUB_ADMIN)
                .build());

        members.add(ClubMember.builder()
                .user(userArr[37])
                .club(clubArr[13])
                .application(apps[37])
                .activeStatus(ActiveStatus.ACTIVE)
                .role(Role.CLUB_ADMIN)
                .build());

        members.add(ClubMember.builder()
                .user(userArr[38])
                .club(clubArr[14])
                .application(apps[38])
                .activeStatus(ActiveStatus.ACTIVE)
                .role(Role.CLUB_ADMIN)
                .build());

        members.add(ClubMember.builder()
                .user(userArr[39])
                .club(clubArr[15])
                .application(apps[39])
                .activeStatus(ActiveStatus.ACTIVE)
                .role(Role.CLUB_ADMIN)
                .build());

        members.add(ClubMember.builder()
                .user(userArr[40])
                .club(clubArr[16])
                .application(apps[40])
                .activeStatus(ActiveStatus.ACTIVE)
                .role(Role.CLUB_ADMIN)
                .build());

        members.add(ClubMember.builder()
                .user(userArr[41])
                .club(clubArr[17])
                .application(apps[41])
                .activeStatus(ActiveStatus.ACTIVE)
                .role(Role.CLUB_ADMIN)
                .build());

        members.add(ClubMember.builder()
                .user(userArr[42])
                .club(clubArr[18])
                .application(apps[42])
                .activeStatus(ActiveStatus.ACTIVE)
                .role(Role.CLUB_ADMIN)
                .build());

        members.add(ClubMember.builder()
                .user(userArr[43])
                .club(clubArr[19])
                .application(apps[43])
                .activeStatus(ActiveStatus.ACTIVE)
                .role(Role.CLUB_ADMIN)
                .build());

        members.add(ClubMember.builder()
                .user(userArr[44])
                .club(clubArr[20])
                .application(apps[44])
                .activeStatus(ActiveStatus.ACTIVE)
                .role(Role.CLUB_ADMIN)
                .build());

        members.add(ClubMember.builder()
                .user(userArr[45])
                .club(clubArr[1])
                .application(apps[45])
                .activeStatus(ActiveStatus.ACTIVE)
                .role(Role.APPLICANT)
                .build());

        members.add(ClubMember.builder()
                .user(userArr[46])
                .club(clubArr[1])
                .application(apps[46])
                .activeStatus(ActiveStatus.ACTIVE)
                .role(Role.APPLICANT)
                .build());

        members.add(ClubMember.builder()
                .user(userArr[47])
                .club(clubArr[1])
                .application(apps[47])
                .activeStatus(ActiveStatus.ACTIVE)
                .role(Role.APPLICANT)
                .build());

        members.add(ClubMember.builder()
                .user(userArr[48])
                .club(clubArr[1])
                .application(apps[48])
                .activeStatus(ActiveStatus.ACTIVE)
                .role(Role.APPLICANT)
                .build());

        members.add(ClubMember.builder()
                .user(userArr[49])
                .club(clubArr[1])
                .application(apps[49])
                .activeStatus(ActiveStatus.ACTIVE)
                .role(Role.APPLICANT)
                .build());

        members.add(ClubMember.builder()
                .user(userArr[50])
                .club(clubArr[1])
                .application(apps[50])
                .activeStatus(ActiveStatus.ACTIVE)
                .role(Role.APPLICANT)
                .build());

        members.add(ClubMember.builder()
                .user(userArr[51])
                .club(clubArr[1])
                .application(apps[51])
                .activeStatus(ActiveStatus.ACTIVE)
                .role(Role.APPLICANT)
                .build());

        members.add(ClubMember.builder()
                .user(userArr[52])
                .club(clubArr[1])
                .application(apps[52])
                .activeStatus(ActiveStatus.ACTIVE)
                .role(Role.APPLICANT)
                .build());

        members.add(ClubMember.builder()
                .user(userArr[53])
                .club(clubArr[1])
                .application(apps[53])
                .activeStatus(ActiveStatus.ACTIVE)
                .role(Role.APPLICANT)
                .build());

        members.add(ClubMember.builder()
                .user(userArr[54])
                .club(clubArr[1])
                .application(apps[54])
                .activeStatus(ActiveStatus.ACTIVE)
                .role(Role.APPLICANT)
                .build());

        members.add(ClubMember.builder()
                .user(userArr[55])
                .club(clubArr[1])
                .application(apps[55])
                .activeStatus(ActiveStatus.ACTIVE)
                .role(Role.APPLICANT)
                .build());

        members.add(ClubMember.builder()
                .user(userArr[56])
                .club(clubArr[1])
                .application(apps[56])
                .activeStatus(ActiveStatus.ACTIVE)
                .role(Role.APPLICANT)
                .build());

        members.add(ClubMember.builder()
                .user(userArr[57])
                .club(clubArr[1])
                .application(apps[57])
                .activeStatus(ActiveStatus.ACTIVE)
                .role(Role.APPLICANT)
                .build());

        members.add(ClubMember.builder()
                .user(userArr[58])
                .club(clubArr[1])
                .application(apps[58])
                .activeStatus(ActiveStatus.ACTIVE)
                .role(Role.APPLICANT)
                .build());

        members.add(ClubMember.builder()
                .user(userArr[59])
                .club(clubArr[1])
                .application(apps[59])
                .activeStatus(ActiveStatus.ACTIVE)
                .role(Role.APPLICANT)
                .build());

        members.add(ClubMember.builder()
                .user(userArr[60])
                .club(clubArr[1])
                .application(apps[60])
                .activeStatus(ActiveStatus.ACTIVE)
                .role(Role.APPLICANT)
                .build());

        members.add(ClubMember.builder()
                .user(userArr[61])
                .club(clubArr[1])
                .application(apps[61])
                .activeStatus(ActiveStatus.ACTIVE)
                .role(Role.APPLICANT)
                .build());

        members.add(ClubMember.builder()
                .user(userArr[62])
                .club(clubArr[1])
                .application(apps[62])
                .activeStatus(ActiveStatus.ACTIVE)
                .role(Role.APPLICANT)
                .build());

        members.add(ClubMember.builder()
                .user(userArr[63])
                .club(clubArr[1])
                .application(apps[63])
                .activeStatus(ActiveStatus.ACTIVE)
                .role(Role.APPLICANT)
                .build());

        members.add(ClubMember.builder()
                .user(userArr[64])
                .club(clubArr[1])
                .application(apps[64])
                .activeStatus(ActiveStatus.ACTIVE)
                .role(Role.APPLICANT)
                .build());

        members.add(ClubMember.builder()
                .user(userArr[65])
                .club(clubArr[3])
                .application(apps[65])
                .activeStatus(ActiveStatus.ACTIVE)
                .role(Role.APPLICANT)
                .build());

        members.add(ClubMember.builder()
                .user(userArr[66])
                .club(clubArr[3])
                .application(apps[66])
                .activeStatus(ActiveStatus.ACTIVE)
                .role(Role.APPLICANT)
                .build());

        members.add(ClubMember.builder()
                .user(userArr[67])
                .club(clubArr[3])
                .application(apps[67])
                .activeStatus(ActiveStatus.ACTIVE)
                .role(Role.APPLICANT)
                .build());

        clubMemberRepository.saveAll(members);
    }

    private List<Notice> seedNotices() {
        List<Notice> notices = new ArrayList<>();

        notices.add(Notice.builder()
                .title("학기 초 공지")
                .content("2025학년도 1학기 공지사항입니다. 반드시 확인하세요.")
                .isAlive(true)
                .build());

        notices.add(Notice.builder()
                .title("서버 점검 안내")
                .content("3월 10일 00:00~02:00 시스템 점검 예정입니다.")
                .isAlive(true)
                .build());

        notices.add(Notice.builder()
                .title("동아리 등록 마감")
                .content("이번 학기 동아리 등록은 3월 15일 자정에 마감됩니다.")
                .isAlive(true)
                .build());

        notices.add(Notice.builder()
                .title("신입 회원 OT 일정")
                .content("3월 5일 오후 6시, 본관 101호에서 오리엔테이션이 열립니다.")
                .isAlive(true)
                .build());

        notices.add(Notice.builder()
                .title("홈페이지 개편 안내")
                .content("UX 개선을 위한 디자인 리뉴얼 작업이 완료되었습니다.")
                .isAlive(true)
                .build());

        notices.add(Notice.builder()
                .title("지원자 결과 발표")
                .content("3월 12일 오후 2시에 이메일로 개별 통보됩니다.")
                .isAlive(true)
                .build());

        notices.add(Notice.builder()
                .title("행사 일정 안내")
                .content("3월 마지막 주 토요일, 교내 축제 부스 운영 예정입니다.")
                .isAlive(true)
                .build());

        notices.add(Notice.builder()
                .title("정기회의 일정")
                .content("매주 목요일 오후 7시, 학생회관 2층 회의실에서 진행됩니다.")
                .isAlive(true)
                .build());

        notices.add(Notice.builder()
                .title("사진 공모전")
                .content("이번 달 25일까지 작품을 제출하면 참가 가능합니다.")
                .isAlive(true)
                .build());

        notices.add(Notice.builder()
                .title("기숙사 점검")
                .content("기숙사 전기 설비 점검으로 4월 1일 13~15시 정전 예정입니다.")
                .isAlive(true)
                .build());

        notices.add(Notice.builder()
                .title("안전 교육 안내")
                .content("신입생 및 복학생 대상 안전 교육이 필수입니다.")
                .isAlive(true)
                .build());

        notices.add(Notice.builder()
                .title("장비 대여 규칙")
                .content("동아리 장비는 사전 예약 후 대여 가능합니다.")
                .isAlive(true)
                .build());

        notices.add(Notice.builder()
                .title("스터디 개설 신청")
                .content("4월부터 진행될 스터디 개설 신청을 받습니다.")
                .isAlive(true)
                .build());

        notices.add(Notice.builder()
                .title("워크숍 일정 안내")
                .content("봄학기 워크숍은 4월 12~13일 양일간 진행됩니다.")
                .isAlive(true)
                .build());

        notices.add(Notice.builder()
                .title("공모전 모집")
                .content("교내 AI 아이디어 공모전에 참여하세요!")
                .isAlive(true)
                .build());

        notices.add(Notice.builder()
                .title("대회 참가 안내")
                .content("프로그래밍 경진대회 신청은 이번 주까지입니다.")
                .isAlive(true)
                .build());

        notices.add(Notice.builder()
                .title("회의록 업로드")
                .content("지난 주 회의 내용이 게시판에 업로드되었습니다.")
                .isAlive(true)
                .build());

        notices.add(Notice.builder()
                .title("동아리 홍보 영상")
                .content("신입생 환영회용 홍보 영상이 게시되었습니다.")
                .isAlive(true)
                .build());

        notices.add(Notice.builder()
                .title("출석 체크 공지")
                .content("활동 출석 확인은 매주 일요일까지 입력해주세요.")
                .isAlive(true)
                .build());

        notices.add(Notice.builder()
                .title("운영진 모집")
                .content("다음 학기 운영진을 모집합니다. 많은 지원 바랍니다.")
                .isAlive(true)
                .build());

        return noticeRepository.saveAll(notices);
    }

    private void seedFiles(List<Notice> notices) {
        if (notices == null || notices.size() < 20) {
            return; // 방어 코드
        }

        List<File> files = new ArrayList<>();

        files.add(File.builder()
                .notice(notices.get(0)) // notice_id 1
                .name("81층짜리 집.png")
                .type("png")
                .objectUri("https://dongarium-attachments.s3.ap-northeast-2.amazonaws.com/attachments/81%EC%B8%B5%EC%A7%80%EB%A6%AC+%EC%A7%91.png")
                .build());

        files.add(File.builder()
                .notice(notices.get(1)) // notice_id 2
                .name("[건의]국무회의 건의안.hwp")
                .type("hwp")
                .objectUri("https://dongarium-attachments.s3.ap-northeast-2.amazonaws.com/attachments/%5B%EA%B1%B4%EC%9D%98%5D%EA%B5%AD%EB%AC%B4%ED%9A%8C%EC%9D%98+%EA%B1%B4%EC%9D%98%EC%95%88.hwp")
                .build());

        files.add(File.builder()
                .notice(notices.get(2)) // notice_id 3
                .name("[문서]대법원 회의.hwp")
                .type("hwp")
                .objectUri("https://dongarium-attachments.s3.ap-northeast-2.amazonaws.com/attachments/%5B%EB%AC%B8%EC%84%9C%5D%EB%8C%80%EB%B2%95%EC%9B%90++%ED%9A%8C%EC%9D%98.hwp")
                .build());

        files.add(File.builder()
                .notice(notices.get(3)) // notice_id 4
                .name("[법률]대법관 임기에 관한 법률.hwp")
                .type("hwp")
                .objectUri("https://dongarium-attachments.s3.ap-northeast-2.amazonaws.com/attachments/%5B%EB%B2%95%EB%A5%A0%5D%EB%8C%80%EB%B2%95%EA%B4%80+%EC%9E%84%EA%B8%B0%EC%97%90+%EA%B4%80%ED%95%9C+%EB%B2%95%EB%A5%A0.hwp")
                .build());

        files.add(File.builder()
                .notice(notices.get(4)) // notice_id 5
                .name("[보고서 양식]근로기준법.hwp")
                .type("hwp")
                .objectUri("https://dongarium-attachments.s3.ap-northeast-2.amazonaws.com/attachments/%5B%EB%B3%B4%EA%B3%A0%EC%84%9C+%EC%96%91%EC%8B%9D%5D%EA%B7%BC%EB%A1%9C%EA%B8%B0%EC%A4%80%EB%B2%95.hwp")
                .build());

        files.add(File.builder()
                .notice(notices.get(5)) // notice_id 6
                .name("[심사결과] 법률심사결과.hwp")
                .type("hwp")
                .objectUri("https://dongarium-attachments.s3.ap-northeast-2.amazonaws.com/attachments/%5B%EC%8B%AC%EC%82%AC%EA%B2%B0%EA%B3%BC%5D+%EB%B2%95%EB%A5%A0%EC%8B%AC%EC%82%AC%EA%B2%B0%EA%B3%BC.hwp")
                .build());

        files.add(File.builder()
                .notice(notices.get(6)) // notice_id 7
                .name("[양식] 지방자치단체.hwp")
                .type("hwp")
                .objectUri("https://dongarium-attachments.s3.ap-northeast-2.amazonaws.com/attachments/%5B%EC%96%91%EC%8B%9D%5D+%EC%A7%80%EB%B0%A9%EC%9E%90%EC%B9%98%EB%8B%A8%EC%B2%B4.hwp")
                .build());

        files.add(File.builder()
                .notice(notices.get(7)) // notice_id 8
                .name("[의결] 국회의결.hwp")
                .type("hwp")
                .objectUri("https://dongarium-attachments.s3.ap-northeast-2.amazonaws.com/attachments/%5B%EC%9D%98%EA%B2%B0%5D+%EA%B5%AD%ED%9A%8C%EC%9D%98%EA%B2%B0.hwp")
                .build());

        files.add(File.builder()
                .notice(notices.get(8)) // notice_id 9
                .name("[임기] 대법관 임기.hwp")
                .type("hwp")
                .objectUri("https://dongarium-attachments.s3.ap-northeast-2.amazonaws.com/attachments/%5B%EC%9E%84%EA%B8%B0%5D+%EB%8C%80%EB%B2%95%EA%B4%80+%EC%9E%84%EA%B8%B0.hwp")
                .build());

        files.add(File.builder()
                .notice(notices.get(9)) // notice_id 10
                .name("공무원법.txt")
                .type("txt")
                .objectUri("https://dongarium-attachments.s3.ap-northeast-2.amazonaws.com/attachments/%EA%B3%B5%EB%AC%B4%EC%9B%90%EB%B2%95.txt")
                .build());

        files.add(File.builder()
                .notice(notices.get(10)) // notice_id 11
                .name("국가안전보장위원회.txt")
                .type("txt")
                .objectUri("https://dongarium-attachments.s3.ap-northeast-2.amazonaws.com/attachments/%EA%B5%AD%EA%B0%80%EC%95%88%EC%A0%84%EB%B3%B4%EC%9E%A5%EC%9C%84%EC%9B%90%ED%9A%8C.txt")
                .build());

        files.add(File.builder()
                .notice(notices.get(11)) // notice_id 12
                .name("국무회의의장.txt")
                .type("txt")
                .objectUri("https://dongarium-attachments.s3.ap-northeast-2.amazonaws.com/attachments/%EA%B5%AD%EB%AC%B4%ED%9A%8C%EC%9D%98%EC%9D%98%EC%9E%A5.txt")
                .build());

        files.add(File.builder()
                .notice(notices.get(12)) // notice_id 13
                .name("국민경제자문회의.txt")
                .type("txt")
                .objectUri("https://dongarium-attachments.s3.ap-northeast-2.amazonaws.com/attachments/%EA%B5%AD%EB%AF%BC%EA%B2%BD%EC%A0%9C%EC%9E%90%EB%AC%B8%ED%9A%8C%EC%9D%98.txt")
                .build());

        files.add(File.builder()
                .notice(notices.get(13)) // notice_id 14
                .name("국민주권.txt")
                .type("txt")
                .objectUri("https://dongarium-attachments.s3.ap-northeast-2.amazonaws.com/attachments/%EA%B5%AD%EB%AF%BC%EC%A3%BC%EA%B6%8C.txt")
                .build());

        files.add(File.builder()
                .notice(notices.get(14)) // notice_id 15
                .name("대외무역.txt")
                .type("txt")
                .objectUri("https://dongarium-attachments.s3.ap-northeast-2.amazonaws.com/attachments/%EB%8C%80%EC%99%B8%EB%AC%B4%EC%97%AD.txt")
                .build());

        files.add(File.builder()
                .notice(notices.get(15)) // notice_id 16
                .name("대외무역육성.txt")
                .type("txt")
                .objectUri("https://dongarium-attachments.s3.ap-northeast-2.amazonaws.com/attachments/%EB%8C%80%EC%99%B8%EB%AC%B4%EC%97%AD%EC%9C%A1%EC%84%B1.txt")
                .build());

        files.add(File.builder()
                .notice(notices.get(16)) // notice_id 17
                .name("대통령 임기.txt")
                .type("txt")
                .objectUri("https://dongarium-attachments.s3.ap-northeast-2.amazonaws.com/attachments/%EB%8C%80%ED%86%B5%EB%A0%B9+%EC%9E%84%EA%B8%B0.txt")
                .build());

        files.add(File.builder()
                .notice(notices.get(17)) // notice_id 18
                .name("중앙선거관리위원.txt")
                .type("txt")
                .objectUri("https://dongarium-attachments.s3.ap-northeast-2.amazonaws.com/attachments/%EC%A4%91%EC%95%99%EC%84%A0%EA%B1%B0%EA%B4%80%EB%A6%AC%EC%9C%84%EC%9B%90.txt")
                .build());

        files.add(File.builder()
                .notice(notices.get(18)) // notice_id 19
                .name("중앙선관위법령.txt")
                .type("txt")
                .objectUri("https://dongarium-attachments.s3.ap-northeast-2.amazonaws.com/attachments/%EC%A4%91%EC%95%99%EC%84%A0%EA%B4%80%EC%9C%84%EB%B2%95%EB%A0%B9.txt")
                .build());

        files.add(File.builder()
                .notice(notices.get(19)) // notice_id 20
                .name("통신방송시설기준.txt")
                .type("txt")
                .objectUri("https://dongarium-attachments.s3.ap-northeast-2.amazonaws.com/attachments/%ED%86%B5%EC%8B%A0%EB%B0%A9%EC%86%A1%EC%8B%9C%EC%84%A4%EA%B8%B0%EC%A4%80.txt")
                .build());

        // SQL에는 20번째 notice에도 파일이 1개 더 있어:
        files.add(File.builder()
                .notice(notices.get(19)) // notice_id 20
                .name("헌법재판소.txt")
                .type("txt")
                .objectUri("https://dongarium-attachments.s3.ap-northeast-2.amazonaws.com/attachments/%ED%97%8C%EB%B2%95%EC%9E%AC%ED%8C%90%EC%86%8C.txt")
                .build());

        fileDataRepository.saveAll(files);
    }

    private void seedClubReviews(List<Club> clubs) {
        //if (clubReviewRepository.count() > 0) return;

        Club club1 = clubs.get(0); // club_id = 1, "인터엑스"

        List<ClubReview> reviews = new ArrayList<>();

        reviews.add(ClubReview.builder()
                .club(club1)
                .content("사회 문제를 다루는 다양한 세미나가 정말 인상 깊었어요. 토론 분위기도 자유롭고 모두가 진지하게 의견을 나누는 모습이 좋았습니다.")
                .writer("20250025")
                .build());

        reviews.add(ClubReview.builder()
                .club(club1)
                .content("동아리원들끼리의 협업이 잘 되고, 실제 캠페인도 진행해볼 수 있어서 뜻깊은 경험이었습니다. 추천합니다!")
                .writer("20250022")
                .build());

        reviews.add(ClubReview.builder()
                .club(club1)
                .content("처음에는 낯설었지만 금방 친해지고, 사회문제에 대한 시각이 넓어졌어요. 프로젝트 중심이라 참여감이 높습니다.")
                .writer("20250023")
                .build());

        clubReviewRepository.saveAll(reviews);
    }

}