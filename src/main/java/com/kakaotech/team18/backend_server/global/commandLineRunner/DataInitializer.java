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

@Component
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
                .studentId("202501")
                .phoneNumber("010-2934-5123")
                .department("컴퓨터공학과")
                .build());

        users.add(User.builder()
                .kakaoId(10002L)
                .email("taehyun.kim@jnu.ac.kr")
                .name("김태현")
                .studentId("202502")
                .phoneNumber("010-9451-2134")
                .department("전기공학과")
                .build());

        users.add(User.builder()
                .kakaoId(10003L)
                .email("sohee.lee@jnu.ac.kr")
                .name("이소희")
                .studentId("202503")
                .phoneNumber("010-3184-6621")
                .department("수학과")
                .build());

        users.add(User.builder()
                .kakaoId(10004L)
                .email("junwoo.choi@jnu.ac.kr")
                .name("최준우")
                .studentId("202504")
                .phoneNumber("010-5763-2448")
                .department("물리학과")
                .build());

        users.add(User.builder()
                .kakaoId(10005L)
                .email("hyerin.jung@jnu.ac.kr")
                .name("정혜린")
                .studentId("202505")
                .phoneNumber("010-8362-1098")
                .department("화학과")
                .build());

        users.add(User.builder()
                .kakaoId(10006L)
                .email("minjae.kang@jnu.ac.kr")
                .name("강민재")
                .studentId("202506")
                .phoneNumber("010-7142-5567")
                .department("생물학과")
                .build());

        users.add(User.builder()
                .kakaoId(10007L)
                .email("jiwon.song@jnu.ac.kr")
                .name("송지원")
                .studentId("202507")
                .phoneNumber("010-9856-3477")
                .department("통계학과")
                .build());

        users.add(User.builder()
                .kakaoId(10008L)
                .email("haneul.yoo@jnu.ac.kr")
                .name("유하늘")
                .studentId("202508")
                .phoneNumber("010-2634-0985")
                .department("경영학과")
                .build());

        users.add(User.builder()
                .kakaoId(10009L)
                .email("seungmin.han@jnu.ac.kr")
                .name("한승민")
                .studentId("202509")
                .phoneNumber("010-6432-7744")
                .department("경제학과")
                .build());

        users.add(User.builder()
                .kakaoId(10010L)
                .email("ara.kim@jnu.ac.kr")
                .name("김아라")
                .studentId("202510")
                .phoneNumber("010-1112-9983")
                .department("디자인학과")
                .build());

        users.add(User.builder()
                .kakaoId(10011L)
                .email("gyuri.park@jnu.ac.kr")
                .name("박규리")
                .studentId("202511")
                .phoneNumber("010-8654-3339")
                .department("미디어커뮤니케이션학과")
                .build());

        users.add(User.builder()
                .kakaoId(10012L)
                .email("donghyun.lee@jnu.ac.kr")
                .name("이동현")
                .studentId("202512")
                .phoneNumber("010-2854-7722")
                .department("교육학과")
                .build());

        users.add(User.builder()
                .kakaoId(10013L)
                .email("eunji.jo@jnu.ac.kr")
                .name("조은지")
                .studentId("202513")
                .phoneNumber("010-9934-1155")
                .department("철학과")
                .build());

        users.add(User.builder()
                .kakaoId(10014L)
                .email("suhyun.kwon@jnu.ac.kr")
                .name("권수현")
                .studentId("202514")
                .phoneNumber("010-4257-2299")
                .department("사회학과")
                .build());

        users.add(User.builder()
                .kakaoId(10015L)
                .email("haeun.cho@jnu.ac.kr")
                .name("조하은")
                .studentId("202515")
                .phoneNumber("010-6712-5543")
                .department("사학과")
                .build());

        users.add(User.builder()
                .kakaoId(10016L)
                .email("junseo.yang@jnu.ac.kr")
                .name("양준서")
                .studentId("202516")
                .phoneNumber("010-9832-4711")
                .department("인공지능학과")
                .build());

        users.add(User.builder()
                .kakaoId(10017L)
                .email("jihye.yoon@jnu.ac.kr")
                .name("윤지혜")
                .studentId("202517")
                .phoneNumber("010-5623-7812")
                .department("데이터사이언스학과")
                .build());

        users.add(User.builder()
                .kakaoId(10018L)
                .email("byungwoo.kim@jnu.ac.kr")
                .name("김병우")
                .studentId("202518")
                .phoneNumber("010-7356-1985")
                .department("토목공학과")
                .build());

        users.add(User.builder()
                .kakaoId(10019L)
                .email("sumin.hwang@jnu.ac.kr")
                .name("황수민")
                .studentId("202519")
                .phoneNumber("010-2178-3345")
                .department("건축학과")
                .build());

        users.add(User.builder()
                .kakaoId(10020L)
                .email("nayeon.lee@jnu.ac.kr")
                .name("이나연")
                .studentId("202520")
                .phoneNumber("010-6943-5023")
                .department("국어국문학과")
                .build());

        users.add(User.builder()
                .kakaoId(10021L)
                .email("jaemin.park@jnu.ac.kr")
                .name("박재민")
                .studentId("202521")
                .phoneNumber("010-2183-6602")
                .department("컴퓨터공학과")
                .build());

        users.add(User.builder()
                .kakaoId(10022L)
                .email("jiyoon.kim@jnu.ac.kr")
                .name("김지윤")
                .studentId("202522")
                .phoneNumber("010-8755-4431")
                .department("경제학과")
                .build());

        users.add(User.builder()
                .kakaoId(10023L)
                .email("minwoo.choi@jnu.ac.kr")
                .name("최민우")
                .studentId("202523")
                .phoneNumber("010-6734-8710")
                .department("산업디자인학과")
                .build());

        users.add(User.builder()
                .kakaoId(10024L)
                .email("haerin.yoo@jnu.ac.kr")
                .name("유해린")
                .studentId("202524")
                .phoneNumber("010-9834-1123")
                .department("철학과")
                .build());

        users.add(User.builder()
                .kakaoId(10025L)
                .email("chsick9@gmail.com")
                .name("김춘식")
                .studentId("202525")
                .phoneNumber("010-5182-7384")
                .department("전기공학과")
                .build());

        users.add(User.builder()
                .kakaoId(10026L)
                .email("dohyun.lee@jnu.ac.kr")
                .name("이도현")
                .studentId("202526")
                .phoneNumber("010-8321-9499")
                .department("기계공학과")
                .build());

        users.add(User.builder()
                .kakaoId(10027L)
                .email("welkin@naver.com")
                .name("이상현")
                .studentId("202527")
                .phoneNumber("010-1557-8848")
                .department("산업공학과")
                .build());

        users.add(User.builder()
                .kakaoId(10028L)
                .email("yejin.kim@jnu.ac.kr")
                .name("김예진")
                .studentId("202528")
                .phoneNumber("010-6571-2390")
                .department("컴퓨터공학과")
                .build());

        users.add(User.builder()
                .kakaoId(10029L)
                .email("artjin@example.com")
                .name("박예진")
                .studentId("202529")
                .phoneNumber("010-8765-4321")
                .department("경영학과")
                .build());

        users.add(User.builder()
                .kakaoId(10030L)
                .email("hyunwoo.jung@jnu.ac.kr")
                .name("정현우")
                .studentId("202530")
                .phoneNumber("010-3421-5512")
                .department("경제학과")
                .build());

        users.add(User.builder()
                .kakaoId(10031L)
                .email("suhyeon.lee@jnu.ac.kr")
                .name("이수현")
                .studentId("202531")
                .phoneNumber("010-6853-9432")
                .department("인공지능학과")
                .build());

        users.add(User.builder()
                .kakaoId(10032L)
                .email("minji.park@jnu.ac.kr")
                .name("박민지")
                .studentId("202532")
                .phoneNumber("010-9324-8123")
                .department("데이터사이언스학과")
                .build());

        users.add(User.builder()
                .kakaoId(10033L)
                .email("jiho.kang@jnu.ac.kr")
                .name("강지호")
                .studentId("202533")
                .phoneNumber("010-4421-7321")
                .department("심리학과")
                .build());

        users.add(User.builder()
                .kakaoId(10034L)
                .email("yujin.son@jnu.ac.kr")
                .name("손유진")
                .studentId("202534")
                .phoneNumber("010-7712-6254")
                .department("교육학과")
                .build());

        users.add(User.builder()
                .kakaoId(10035L)
                .email("soobin.ahn@jnu.ac.kr")
                .name("안수빈")
                .studentId("202535")
                .phoneNumber("010-9863-2541")
                .department("시각디자인학과")
                .build());

        users.add(User.builder()
                .kakaoId(10036L)
                .email("dongyeon.kim@jnu.ac.kr")
                .name("김동연")
                .studentId("202536")
                .phoneNumber("010-3511-7843")
                .department("통계학과")
                .build());

        users.add(User.builder()
                .kakaoId(10037L)
                .email("yeona.han@jnu.ac.kr")
                .name("한연아")
                .studentId("202537")
                .phoneNumber("010-2938-1599")
                .department("화학과")
                .build());

        users.add(User.builder()
                .kakaoId(10038L)
                .email("gunwoo.park@jnu.ac.kr")
                .name("박건우")
                .studentId("202538")
                .phoneNumber("010-7654-8893")
                .department("토목공학과")
                .build());

        users.add(User.builder()
                .kakaoId(10039L)
                .email("suhyun.kim@jnu.ac.kr")
                .name("김수현")
                .studentId("202539")
                .phoneNumber("010-4738-3322")
                .department("생물학과")
                .build());

        users.add(User.builder()
                .kakaoId(10040L)
                .email("yuri.lee@jnu.ac.kr")
                .name("이유리")
                .studentId("202540")
                .phoneNumber("010-5123-9984")
                .department("물리학과")
                .build());

        users.add(User.builder()
                .kakaoId(10041L)
                .email("jaeho.cho@jnu.ac.kr")
                .name("조재호")
                .studentId("202541")
                .phoneNumber("010-8641-2148")
                .department("건축학과")
                .build());

        users.add(User.builder()
                .kakaoId(10042L)
                .email("haeun.kwon@jnu.ac.kr")
                .name("권해은")
                .studentId("202542")
                .phoneNumber("010-7412-8975")
                .department("철학과")
                .build());

        users.add(User.builder()
                .kakaoId(10043L)
                .email("gyubin.kim@jnu.ac.kr")
                .name("김규빈")
                .studentId("202543")
                .phoneNumber("010-2321-5638")
                .department("컴퓨터공학과")
                .build());

        users.add(User.builder()
                .kakaoId(10044L)
                .email("siyoon.han@jnu.ac.kr")
                .name("한시윤")
                .studentId("202544")
                .phoneNumber("010-4123-9921")
                .department("경제학과")
                .build());

        users.add(User.builder()
                .kakaoId(10045L)
                .email("jinho.park@jnu.ac.kr")
                .name("박진호")
                .studentId("202545")
                .phoneNumber("010-7766-4422")
                .department("컴퓨터공학과")
                .build());

        users.add(User.builder()
                .kakaoId(10046L)
                .email("minjeong.kim@jnu.ac.kr")
                .name("김민정")
                .studentId("202546")
                .phoneNumber("010-9123-6233")
                .department("전기공학과")
                .build());

        users.add(User.builder()
                .kakaoId(10047L)
                .email("woojin.choi@jnu.ac.kr")
                .name("최우진")
                .studentId("202547")
                .phoneNumber("010-2132-8854")
                .department("수학과")
                .build());

        users.add(User.builder()
                .kakaoId(10048L)
                .email("seoyoung.yoon@jnu.ac.kr")
                .name("윤서영")
                .studentId("202548")
                .phoneNumber("010-5511-4343")
                .department("물리학과")
                .build());

        users.add(User.builder()
                .kakaoId(10049L)
                .email("taehwan.jang@jnu.ac.kr")
                .name("장태환")
                .studentId("202549")
                .phoneNumber("010-8344-8823")
                .department("화학과")
                .build());

        users.add(User.builder()
                .kakaoId(10050L)
                .email("yebin.kim@jnu.ac.kr")
                .name("김예빈")
                .studentId("202550")
                .phoneNumber("010-1267-9901")
                .department("생물학과")
                .build());

        users.add(User.builder()
                .kakaoId(10051L)
                .email("hyejin.park@jnu.ac.kr")
                .name("박혜진")
                .studentId("202551")
                .phoneNumber("010-7612-1185")
                .department("통계학과")
                .build());

        users.add(User.builder()
                .kakaoId(10052L)
                .email("junho.yoon@jnu.ac.kr")
                .name("윤준호")
                .studentId("202552")
                .phoneNumber("010-6254-3712")
                .department("경영학과")
                .build());

        users.add(User.builder()
                .kakaoId(10053L)
                .email("sua.lee@jnu.ac.kr")
                .name("이수아")
                .studentId("202553")
                .phoneNumber("010-9931-4741")
                .department("경제학과")
                .build());

        users.add(User.builder()
                .kakaoId(10054L)
                .email("minkyu.kim@jnu.ac.kr")
                .name("김민규")
                .studentId("202554")
                .phoneNumber("010-7483-6214")
                .department("디자인학과")
                .build());

        users.add(User.builder()
                .kakaoId(10055L)
                .email("eunsol.choi@jnu.ac.kr")
                .name("최은솔")
                .studentId("202555")
                .phoneNumber("010-3412-7725")
                .department("미디어학과")
                .build());

        users.add(User.builder()
                .kakaoId(10056L)
                .email("jinhyuk.kang@jnu.ac.kr")
                .name("강진혁")
                .studentId("202556")
                .phoneNumber("010-6234-8824")
                .department("교육학과")
                .build());

        users.add(User.builder()
                .kakaoId(10057L)
                .email("yeseo.park@jnu.ac.kr")
                .name("박예서")
                .studentId("202557")
                .phoneNumber("010-9732-4122")
                .department("철학과")
                .build());

        users.add(User.builder()
                .kakaoId(10058L)
                .email("jiwoo.kim@jnu.ac.kr")
                .name("김지우")
                .studentId("202558")
                .phoneNumber("010-2745-8811")
                .department("사회학과")
                .build());

        users.add(User.builder()
                .kakaoId(10059L)
                .email("doyoung.han@jnu.ac.kr")
                .name("한도영")
                .studentId("202559")
                .phoneNumber("010-6321-6612")
                .department("사학과")
                .build());

        users.add(User.builder()
                .kakaoId(10060L)
                .email("hajin.yoo@jnu.ac.kr")
                .name("유하진")
                .studentId("202560")
                .phoneNumber("010-8899-5543")
                .department("인공지능학과")
                .build());

        users.add(User.builder()
                .kakaoId(10061L)
                .email("yujin.kang@jnu.ac.kr")
                .name("강유진")
                .studentId("202561")
                .phoneNumber("010-1442-3338")
                .department("데이터사이언스학과")
                .build());

        users.add(User.builder()
                .kakaoId(10062L)
                .email("haneul.kim@jnu.ac.kr")
                .name("김하늘")
                .studentId("202562")
                .phoneNumber("010-3232-8222")
                .department("토목공학과")
                .build());

        users.add(User.builder()
                .kakaoId(10063L)
                .email("sihyeon.lee@jnu.ac.kr")
                .name("이시현")
                .studentId("202563")
                .phoneNumber("010-9123-7765")
                .department("건축학과")
                .build());

        users.add(User.builder()
                .kakaoId(10064L)
                .email("yujin.lim@jnu.ac.kr")
                .name("임유진")
                .studentId("202564")
                .phoneNumber("010-5534-9332")
                .department("국어국문학과")
                .build());

        users.add(User.builder()
                .kakaoId(10065L)
                .email("gunwoo.jang@jnu.ac.kr")
                .name("장건우")
                .studentId("202565")
                .phoneNumber("010-7311-4883")
                .department("토목공학과")
                .build());

        users.add(User.builder()
                .kakaoId(10066L)
                .email("minji.yoon@jnu.ac.kr")
                .name("윤민지")
                .studentId("202566")
                .phoneNumber("010-8253-6662")
                .department("건축학과")
                .build());

        users.add(User.builder()
                .kakaoId(10067L)
                .email("suhyun.kim2@jnu.ac.kr")
                .name("김수현")
                .studentId("202567")
                .phoneNumber("010-9511-7789")
                .department("국어국문학과")
                .build());

        return userRepository.saveAll(users);
    }

    private List<Club> seedClubsWithIntroAndImages() {
        //if (clubRepository.count() > 0) return clubRepository.findAll();

        List<Club> clubs = new ArrayList<>();

        // 1. 인터엑스 (사회문제 탐구)
        ClubIntroduction intro1 = ClubIntroduction.builder()
                .overview("인터엑스(INTER-X)는 ‘사회와 교차하다(Intersect with Society)’라는 의미를 담은 사회문제 탐구 동아리로, " +
                        "우리 주변의 다양한 사회적 현상을 비판적으로 바라보고, 실제 변화를 만들어가는 것을 목표로 합니다. " +
                        "단순히 뉴스를 소비하거나 담론을 나누는 수준을 넘어, 문제의 구조를 분석하고 실질적인 대안을 모색하는 데 초점을 맞추고 있습니다. " +
                        "우리는 사회 문제를 학문적 관점에서 탐구하면서도, 현장에서 체감할 수 있는 실천적 접근을 중요하게 생각합니다. " +
                        "경제적 불평등, 젠더 이슈, 환경 위기, 세대 갈등, 기술 윤리 등 매 학기 다양한 주제를 다루며, " +
                        "회원 각자가 가진 전공과 관심사를 토대로 팀을 구성하여 심층적인 연구와 행동을 병행합니다. " +
                        "이 과정에서 단순한 ‘지식 습득’을 넘어, 스스로 문제를 정의하고 해결을 설계하는 사고력과 협력 능력을 함께 기를 수 있습니다. " +
                        "인터엑스는 지적 담론의 장이자, 변화를 실험하는 플랫폼으로서, ‘생각을 행동으로 옮기는 대학생 공동체’를 지향합니다. " +
                        "서로 다른 시각을 가진 사람들이 모여 대화하고, 함께 사회를 이해하며, 작은 변화를 쌓아가는 과정 자체가 인터엑스의 철학입니다.")
                .activities("인터엑스의 핵심 활동은 세 가지 축으로 이루어져 있습니다. " +
                        "첫째, 주제 세미나입니다. 매주 한 가지 사회 문제를 중심으로 회원들이 직접 발표와 토론을 진행하며, " +
                        "이슈의 역사적 맥락, 이해관계자, 정책적 대안 등을 심층적으로 분석합니다. 세미나는 단순한 토론을 넘어, " +
                        "비판적 사고력과 논리적 표현 능력을 훈련하는 공간입니다. " +
                        "둘째, 프로젝트 활동입니다. 학기 초 구성된 소모임 단위로 각자의 주제를 선정해 조사와 인터뷰, 데이터 분석, 캠페인 기획 등을 진행합니다. " +
                        "예를 들어, 청년 주거 문제를 다룬 팀은 실제 원룸 임대 현황을 조사하고, 지역 청년정책팀과의 간담회를 추진하기도 했습니다. " +
                        "또한 여성 안전, 기후 행동, 장애 인식 개선 등 구체적 사회 의제를 중심으로 외부 기관(NGO, 시민단체, 언론 등)과 협업하는 경우도 많습니다. " +
                        "셋째, 성과 공유와 외부 교류입니다. 학기 말에는 ‘인터엑스 포럼’을 개최해 각 팀의 연구·활동 결과를 발표하고, " +
                        "이를 정리한 ‘인터엑스 리서치북’을 발간합니다. 해당 자료는 매년 아카이브 형태로 보존되어, " +
                        "후속 기수의 참고 자료이자 대학 사회문제 연구 커뮤니티의 자산으로 남습니다. " +
                        "이외에도 정기적인 지역사회 봉사활동, 사회혁신 관련 워크숍, 타 대학 동아리와의 공동 세미나 등 다양한 외부 프로그램을 통해 " +
                        "단순한 교내 동아리를 넘어 하나의 ‘사회참여 네트워크’로 발전하고 있습니다. " +
                        "회원들은 이러한 과정을 통해 연구력뿐 아니라 기획력, 리더십, 그리고 시민의식까지 함께 성장시키게 됩니다.")
                .ideal("인터엑스는 ‘의문을 품고, 끝까지 탐구하며, 함께 해답을 만들어가는 사람’을 기다립니다.\n\n" +
                        "[자격요건]\n" +
                        "• 사회문제에 관심이 많고, 학문적 또는 실천적 접근을 즐기는 분\n" +
                        "• 다양한 관점에서 현상을 분석하고 근거를 제시할 수 있는 비판적 사고력을 가진 분\n" +
                        "• 글쓰기와 문서 정리를 통해 생각을 구조화할 수 있는 분\n" +
                        "• 주 1회 세미나 및 프로젝트에 꾸준히 참여할 수 있는 성실한 분\n" +
                        "• 타인의 의견을 경청하고 협력적 논의를 이끌 수 있는 커뮤니케이션 역량이 있는 분\n\n" +
                        "[우대사항]\n" +
                        "• 사회학, 정치학, 경제학, 심리학, 환경학 등 인문·사회 분야에 관심이 있는 분\n" +
                        "• 데이터 분석, 시각화, 인터뷰, 설문조사 등 연구적 방법론 경험이 있는 분\n" +
                        "• 캠페인, 봉사, 정책 제안 등 사회참여 활동 경험이 있는 분\n" +
                        "• 발표나 리포트 작성, 인포그래픽 제작 등 콘텐츠 표현 능력이 있는 분\n" +
                        "• 프로젝트 리딩 경험 또는 팀워크를 통해 공동 성과를 만들어본 경험이 있는 분\n" +
                        "• 타 대학 동아리, 시민단체, 학회 등 외부 협업 경험이 있는 분\n\n" +
                        "인터엑스는 전공, 학년, 배경에 관계없이 ‘문제를 진지하게 바라보고, 함께 해결을 고민하는 사람’을 환영합니다. " +
                        "사회문제를 탐구하고 실천으로 연결하고 싶은 분이라면, 인터엑스에서 그 첫걸음을 내딛어 보세요.")
                .build();
        intro1.addImages(List.of(
                "https://images.unsplash.com/photo-1543269865-cbf427effbad?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&q=80&w=1170",
                "https://images.unsplash.com/photo-1526976668912-1a811878dd37?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&q=80&w=1170",
                "https://images.unsplash.com/photo-1576267423445-b2e0074d68a4?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&q=80&w=1170",
                "https://images.unsplash.com/photo-1609234656388-0ff363383899?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&q=80&w=1932"
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
                .overview("AIDive는 인공지능(AI)과 데이터 분석을 깊이 탐구하며, 이론적 이해와 실무 역량을 함께 키우는 학술 동아리입니다. " +
                        "‘데이터 속에서 통찰을 발견하고, 기술로 문제를 해결한다’는 목표로 최신 AI 연구를 공부하고 실제 프로젝트에 적용합니다. " +
                        "회원들은 논문 리뷰, 대회 참여, 코드 구현, 협업 툴 활용 등 다양한 과정을 통해 AI 기술의 본질과 응용을 동시에 경험합니다.")
                .activities("• 최신 논문 리뷰 및 토론 세션 진행\n" +
                        "• Kaggle / Dacon 등 데이터 분석 대회 팀 단위 참가\n" +
                        "• TensorFlow, PyTorch, Scikit-learn 등 실습 중심 스터디\n" +
                        "• 산업체 연계 세미나 및 실무 멘토링\n" +
                        "• 학기 말 AIDive Showcase 및 논문집 발간")
                .ideal("AIDive는 기술적 깊이와 협업적 사고를 함께 성장시키고자 하는 인재를 찾고 있습니다.\n\n" +
                        "[자격요건]\n" +
                        "• 인공지능, 데이터 과학, 통계, 수학 등 기술적 주제에 관심이 있는 분\n" +
                        "• Python, R 등 프로그래밍 언어를 활용한 기본적인 데이터 분석 경험이 있는 분\n" +
                        "• 논리적 사고력과 문제 해결 능력을 기반으로 프로젝트를 수행할 수 있는 분\n" +
                        "• 새로운 기술을 스스로 탐구하고 꾸준히 학습할 의지가 있는 분\n" +
                        "• 팀 단위 프로젝트에서 협업과 소통을 즐기는 분\n" +
                        "• GitHub, Notion, Slack 등의 협업 도구 사용에 익숙하거나 배우려는 의지가 있는 분\n\n" +
                        "[우대사항]\n" +
                        "• 머신러닝/딥러닝 프레임워크(PyTorch, TensorFlow 등) 실습 경험이 있는 분\n" +
                        "• Kaggle, Dacon 등 데이터 분석 대회 참가 또는 프로젝트 경험이 있는 분\n" +
                        "• 통계적 분석, 데이터 시각화(Matplotlib, Seaborn 등)에 능숙한 분\n" +
                        "• 논문 리뷰, 모델 구현, 리서치 리포트 작성 등 연구형 활동에 흥미가 있는 분\n" +
                        "• 산업계 AI 트렌드, AI 윤리, 데이터 거버넌스 등에 관심이 있는 분\n" +
                        "• 수학적 사고력(선형대수, 확률, 최적화 등)을 활용해 모델을 이해하고 설명할 수 있는 분\n\n" +
                        "AIDive는 단순한 기술 습득이 아닌, ‘AI를 통한 문제 해결의 가치’를 함께 고민하는 사람들을 환영합니다. " +
                        "AI의 원리를 이해하고, 데이터를 통해 사회적·산업적 인사이트를 창출하고 싶은 분이라면, " +
                        "AIDive가 당신의 성장 여정을 함께할 최고의 팀이 될 것입니다.")
                .build();
        intro2.addImages(List.of(
                "https://images.unsplash.com/photo-1549057446-9f5c6ac91a04?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&q=80&w=1334",
                "https://plus.unsplash.com/premium_photo-1663040303769-cd3ee2dfb172?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&q=80&w=688",
                "https://images.unsplash.com/photo-1531545514256-b1400bc00f31?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&q=80&w=1074",
                "https://images.unsplash.com/photo-1523240795612-9a054b0db644?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&q=80&w=1170"        ));

        Club club2 = Club.builder()
                .name("에이아이디브")
                .category(Category.STUDY)
                .location("공5 202호")
                .shortIntroduction("AI·데이터 분석 연구를 통해 기술을 배우는 학술 동아리")
                .introduction(intro2)
                .recruitStart(LocalDateTime.of(2025, 9, 3, 0, 0, 0))
                .recruitEnd(LocalDateTime.of(2025, 11, 30, 23, 59, 0))
                .regularMeetingInfo("매주 수요일 오후 7시")
                .caution("프로그래밍 기초자 우대")
                .build();
        clubs.add(club2);

        // 3. 코드마스터 (프로그래밍·개발)
        ClubIntroduction intro3 = ClubIntroduction.builder()
                .overview("코드마스터(CodeMaster)는 프로그래밍을 중심으로 다양한 개발 기술을 탐구하고, 실제 서비스를 만들어보며 성장하는 개발 동아리입니다. " +
                        "최신 언어와 프레임워크를 학습하고, 학기마다 팀 단위로 프로젝트를 진행하여 실무형 개발 경험을 쌓습니다. " +
                        "웹, 앱, 인공지능, 게임 등 다양한 분야의 개발을 다루며, 단순한 코딩 실습을 넘어 ‘문제를 정의하고 해결하는 능력’을 함께 키우는 것을 목표로 합니다. " +
                        "회원들은 함께 스터디를 운영하고 코드 리뷰를 진행하며, 개발 지식뿐 아니라 협업 문화와 소프트웨어 설계 감각도 익히게 됩니다. " +
                        "코드마스터는 ‘코드를 통해 세상을 바꾸는 경험’을 함께 만들어가는 대학생 개발자 커뮤니티입니다.")
                .activities("• 주 1회 개발 세미나 및 코드 리뷰 세션\n" +
                        "• 알고리즘·자료구조 스터디 운영 (백준, 프로그래머스 등)\n" +
                        "• 웹/앱/AI 분야 팀 프로젝트 수행 및 포트폴리오 제작\n" +
                        "• GitHub를 활용한 버전 관리 및 협업 실습\n" +
                        "• 오픈소스 기여 활동 및 외부 해커톤 참가\n" +
                        "• 학기 말 Demo Day(결과 발표회) 개최로 프로젝트 성과 공유")
                .ideal("코드마스터는 ‘함께 배우고, 함께 성장하는 개발자 공동체’를 지향합니다.\n\n" +
                        "[자격요건]\n" +
                        "• 프로그래밍 학습에 열정이 있고 꾸준히 실습을 이어갈 수 있는 분\n" +
                        "• 웹, 앱, AI, 게임 등 특정 개발 분야에 관심이 있는 분\n" +
                        "• 새로운 기술을 배우고 직접 적용해보는 것을 즐기는 분\n" +
                        "• 오류를 두려워하지 않고 문제 해결 과정을 즐길 수 있는 분\n" +
                        "• 협업과 피드백을 긍정적으로 받아들이는 태도를 가진 분\n" +
                        "• 주 1회 이상 세션 및 프로젝트 활동에 성실히 참여할 수 있는 분\n\n" +
                        "[우대사항]\n" +
                        "• Java, Python, JavaScript 등 주요 언어를 활용한 간단한 프로그램 개발 경험이 있는 분\n" +
                        "• Git, Notion, Slack 등 협업 툴을 사용해본 경험이 있는 분\n" +
                        "• 개인 혹은 팀 단위로 프로젝트를 진행해본 경험이 있는 분\n" +
                        "• 알고리즘 문제 풀이(백준, 프로그래머스 등)에 꾸준히 참여하고 있는 분\n" +
                        "• UI/UX, 데이터베이스, 네트워크 등 컴퓨터공학 기초 지식이 있는 분\n" +
                        "• 해커톤, 공모전, 학술제 등 대외 활동에 관심이 있는 분\n\n" +
                        "코드마스터는 학년이나 전공과 관계없이 ‘배움에 진심인 사람’을 환영합니다. " +
                        "개발이 처음이어도 괜찮습니다. 함께 배우고, 스스로 만들어가며, 성장하는 경험을 하고 싶은 분이라면 코드마스터와 잘 맞을 것입니다.")
                .build();
        intro3.addImages(List.of(
                "https://images.unsplash.com/photo-1519389950473-47ba0277781c?w=1600&auto=format&fit=crop&q=60",
                "https://images.unsplash.com/photo-1587620962725-abab7fe55159?w=1600&auto=format&fit=crop&q=60",
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

        // 4. 로보테크 (스포츠·스포츠테크)
        ClubIntroduction intro4 = ClubIntroduction.builder()
                .overview("로보테크(RoboTech)는 ‘기술과 스포츠’를 함께 즐기는 스포츠·스포츠테크 동아리입니다. " +
                        "축구, 농구, 배드민턴, 러닝 등 다양한 종목을 함께 즐기면서, 동시에 로봇·센서·트래킹 장비 등을 활용해 " +
                        "운동 기록을 측정하고 분석하는 활동도 함께 진행합니다. " +
                        "단순히 경기를 하는 데서 그치지 않고, 나와 팀의 움직임을 데이터로 보고, 더 똑똑하게 훈련하는 문화를 지향합니다. " +
                        "예를 들어, 경기 영상 분석, 심박수·속도·이동 경로 측정, 간단한 자동 기록 장비(득점 센서, 타이머 등)를 직접 만들어보는 등 " +
                        "스포츠와 공학적 호기심을 한 번에 만족시킬 수 있는 활동으로 채워져 있습니다. " +
                        "운동을 좋아하는 학생, 기술을 함께 얹어보고 싶은 학생이라면 누구나 함께할 수 있는 열린 스포츠 동아리입니다.")
                .activities("• 주 1회 이상 정기 스포츠 활동 (축구, 농구, 배드민턴 등 종목은 학기별 논의 후 결정)\n" +
                        "• 러닝·체력 향상 프로그램 및 기록 측정(거리, 페이스, 심박 등)\n" +
                        "• 스마트워치, 스포츠 트래커, 액션캠 등을 활용한 퍼포먼스 분석\n" +
                        "• 간단한 스포츠 보조 장비 제작(타이머, 득점판, 센서 기반 기록 시스템 등)\n" +
                        "• 교내·외 체육대회, 친선 경기, 스포츠 페스티벌 참가\n" +
                        "• 학기 말 기록 공유 및 ‘로보테크 스포츠 챌린지’ 개최")
                .ideal("로보테크는 ‘함께 뛰고, 함께 기록하고, 함께 성장하는 사람’을 찾습니다.\n\n" +
                        "[자격요건]\n" +
                        "• 운동을 즐기고, 새로운 스포츠 종목에 도전해보고 싶은 학부생\n" +
                        "• 승패보다 팀워크와 과정 자체를 중요하게 생각하는 분\n" +
                        "• 정기적인 운동 모임에 꾸준히 참여할 수 있는 성실한 분\n" +
                        "• 기록 측정, 운동 데이터 분석, 스포츠테크에 관심이 있는 분\n" +
                        "• 다른 사람과 함께 땀 흘리는 것을 즐기고, 기본적인 예의를 갖춘 분\n" +
                        "• 초보라도 배우면서 즐길 마음가짐이 있는 분\n\n" +
                        "[우대사항]\n" +
                        "• 축구, 농구, 배드민턴, 러닝 등 한 가지 이상 종목에 자신 있는 분\n" +
                        "• 스마트워치, 스포츠 트래커, 액션캠 등 기기를 활용해본 경험이 있는 분\n" +
                        "• 간단한 프로그래밍 또는 하드웨어(센서, 아두이노 등)에 관심이 있는 분\n" +
                        "• 팀 스포츠에서 주장, 리더, 매니저 등 역할을 맡아본 경험이 있는 분\n" +
                        "• 교내 체육대회, 대외 스포츠 대회 등 참가 경험이 있는 분\n" +
                        "• 스포츠 콘텐츠(하이라이트 영상, 기록 정리, 카드뉴스 등) 제작에 흥미가 있는 분\n\n" +
                        "로보테크는 ‘운동을 좋아하는 사람’과 ‘기술을 좋아하는 사람’이 함께 어울릴 수 있는 공간입니다. " +
                        "숨이 차도록 뛰는 순간도, 기록을 보며 웃고 떠드는 시간도 모두 즐기고 싶은 분이라면 로보테크와 잘 어울릴 것입니다.")
                .build();
        intro4.addImages(List.of(
                "https://images.unsplash.com/photo-1642775073532-65020022b8d0?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&q=80&w=1170",
                "https://images.unsplash.com/photo-1755053757912-a63da9d6e0e2?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&q=80&w=1171",
                "https://plus.unsplash.com/premium_photo-1716396589501-fe11f268a0ca?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&q=80&w=1170",
                "https://images.unsplash.com/photo-1742767069929-0c663150b164?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&q=80&w=687"
        ));

        Club club4 = Club.builder()
                .name("로보테크")
                .category(Category.STUDY)
                .location("공4 104호")
                .shortIntroduction("로봇 설계와 제어를 함께 배우는 공학 창작 동아리")
                .introduction(intro4)
                .recruitStart(LocalDateTime.of(2025, 3, 4, 9, 0, 0))
                .recruitEnd(LocalDateTime.of(2025, 3, 18, 18, 0, 0))
                .regularMeetingInfo("매주 목요일 오후 6시")
                .caution("기계·전기전자 전공자 우대")
                .build();
        clubs.add(club4);

// 5. 아트픽 (예술·디자인 재능기부 봉사)
        ClubIntroduction intro5 = ClubIntroduction.builder()
                .overview("아트픽(ArtPick)은 예술·디자인 재능을 활용해 지역사회에 따뜻한 변화를 만들어가는 봉사·나눔 동아리입니다. " +
                        "벽화 그리기, 공간 디자인, 교육 프로그램 지원, 캠페인 포스터 제작 등 시각 예술을 기반으로 한 다양한 재능기부 활동을 진행합니다. " +
                        "지역 아동센터, 사회복지시설, 학교 행사 등과 연계해 환경을 아름답게 꾸미거나, 아이들과 함께 그림·공예 활동을 하며 정서적 지지를 나누는 것이 아트픽의 주요 목표입니다. " +
                        "아트픽은 ‘잘 그리는 사람들만 모이는 곳’이 아니라, 그림과 디자인을 좋아하는 마음을 바탕으로 작은 재능을 함께 나누고 싶은 학생들이 모이는 커뮤니티입니다. " +
                        "완벽한 실력보다, 함께 준비하고 땀 흘리며 한 공간을 바꾸고 하나의 프로젝트를 완성하는 과정을 소중히 여깁니다.")
                .activities("• 지역 아동센터, 복지시설 등과 연계한 벽화·공간 꾸미기 봉사\n" +
                        "• 아이들을 위한 미술·공예 체험 프로그램 기획 및 진행 보조\n" +
                        "• 학교·지역 행사용 포스터, 배너, 카드뉴스 등 디자인 재능기부\n" +
                        "• 캠페인(환경, 안전, 인권 등) 관련 시각물 제작과 나눔 활동\n" +
                        "• 봉사 활동 결과물 아카이빙(사진, 기록집, 온라인 갤러리) 제작\n" +
                        "• 학기 말 활동 공유 세션 및 작은 결과 전시회 개최")
                .ideal("아트픽은 ‘작은 예술을 따뜻한 나눔으로 바꾸는 사람’을 찾습니다.\n\n" +
                        "[자격요건]\n" +
                        "• 그림, 디자인, 공예 등 예술적 표현에 관심이 있는 학부생\n" +
                        "• 완성도보다 함께 준비하고 참여하는 과정 자체를 즐기는 분\n" +
                        "• 아이들·지역 주민 등과의 소통에 부담이 없고, 친절하게 대화할 수 있는 분\n" +
                        "• 정기 모임 및 봉사 일정에 성실히 참여할 수 있는 분\n" +
                        "• 타인의 공간과 시간을 존중하고 책임감 있게 행동할 수 있는 분\n" +
                        "• 팀 단위 프로젝트에서 역할을 맡아 끝까지 수행할 수 있는 분\n\n" +
                        "[우대사항]\n" +
                        "• 드로잉, 회화, 일러스트, 그래픽 디자인 등 한 가지 이상 분야의 작업 경험이 있는 분\n" +
                        "• Photoshop, Illustrator, Procreate 등 디자인 툴 사용 경험이 있는 분\n" +
                        "• 벽화, 포스터, 굿즈 제작 등 시각물 제작 경험이 있는 분\n" +
                        "• 아동·청소년 대상 교육 봉사 또는 프로그램 진행 보조 경험이 있는 분\n" +
                        "• 봉사활동 기획, 행사 운영, 기록 정리(사진·글 등)에 관심이 있는 분\n" +
                        "• 예술 재능기부나 사회공헌 활동에 꾸준히 참여해보고 싶은 분\n\n" +
                        "아트픽은 ‘완벽한 예술가’보다 ‘따뜻한 마음을 가진 예술 동아리원’을 기다립니다. " +
                        "작은 선과 색으로 누군가의 하루를 밝히고 싶은 분이라면, 아트픽에서 그 마음을 함께 나누어 주세요.")
                .build();
        intro5.addImages(List.of(
                "https://plus.unsplash.com/premium_photo-1663054644344-0d6e2da6e6f7?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&q=80&w=1170",
                "https://plus.unsplash.com/premium_photo-1663091477072-91baad11832a?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&q=80&w=1170",
                "https://images.unsplash.com/photo-1571277129183-8a5ed585a9a4?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&q=80&w=1170",
                "https://plus.unsplash.com/premium_photo-1723514542872-ef0ff7597213?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&q=80&w=2070"        ));

        Club club5 = Club.builder()
                .name("아트픽")
                .category(Category.LITERATURE)
                .location("예술관 301호")
                .shortIntroduction("창작 활동과 전시를 중심으로 활동하는 예술 동아리")
                .introduction(intro5)
                .recruitStart(LocalDateTime.of(2025, 9, 7, 0, 0, 0))
                .recruitEnd(LocalDateTime.of(2025, 11, 30, 23, 59, 0))
                .regularMeetingInfo("매주 금요일 오후 5시")
                .caution("학년·전공 무관, 예술 열정만 있으면 OK")
                .build();
        clubs.add(club5);

        // 6. 리버스 (음악·공연 봉사)
        ClubIntroduction intro6 = ClubIntroduction.builder()
                .overview("리버스(Reverse)는 음악을 통해 따뜻한 마음을 전하고, 공연으로 세상에 긍정적인 에너지를 나누는 봉사·공연 동아리입니다. " +
                        "밴드, 보컬, 연주, 작곡 등 음악적 재능을 살려 복지시설, 병원, 지역 행사 등에서 공연 봉사를 진행하며, 음악이 주는 위로와 즐거움을 사람들에게 전하는 것을 목표로 합니다. " +
                        "정기적으로 교내 공연을 열어 모인 수익을 기부하거나, 사회적 메시지를 담은 버스킹을 통해 음악의 가치를 확장합니다. " +
                        "리버스는 실력보다 ‘진심 어린 연주’와 ‘관객과의 공감’을 중요하게 생각하며, 음악을 통해 함께 웃고 울 수 있는 따뜻한 무대를 만들어갑니다. " +
                        "음악으로 마음을 나누고 싶은 모든 학생에게 열려 있습니다.")
                .activities("• 지역 아동센터, 요양원, 병원 등 방문 공연 봉사\n" +
                        "• 학교 축제 및 교내 정기 공연 수익 기부 프로젝트\n" +
                        "• 사회적 메시지를 담은 버스킹·거리 공연 기획\n" +
                        "• 보컬·악기·합주 등 파트별 정기 연습\n" +
                        "• 음원 제작 및 녹음 프로젝트 (후원 캠페인 송 등)\n" +
                        "• 교외 문화행사·자선 공연·지역 축제 참가")
                .ideal("리버스는 ‘음악으로 따뜻한 마음을 전하는 사람’을 찾습니다.\n\n" +
                        "[자격요건]\n" +
                        "• 음악을 사랑하고, 이를 통해 긍정적인 변화를 만들어가고 싶은 학부생\n" +
                        "• 보컬, 기타, 베이스, 드럼, 키보드 등 밴드 세션이나 작곡·편곡에 관심이 있는 분\n" +
                        "• 정기적인 합주·공연 준비에 꾸준히 참여할 수 있는 성실한 분\n" +
                        "• 봉사 공연, 지역 행사 등 외부 무대 참여에 적극적인 분\n" +
                        "• 팀 단위 협업을 즐기며, 함께 무대를 완성하려는 책임감을 가진 분\n" +
                        "• 무대 경험이 없어도, 음악을 통해 나눔을 실천하고 싶은 분\n\n" +
                        "[우대사항]\n" +
                        "• 보컬 또는 밴드 세션(기타, 베이스, 드럼, 키보드 등) 연주 경험이 있는 분\n" +
                        "• 작곡, 편곡, 음향 장비(믹서, 오디오 인터페이스 등) 사용 경험이 있는 분\n" +
                        "• 공연 기획, 무대 연출, 조명, 영상 촬영 등에 관심이 있는 분\n" +
                        "• 자선 공연, 병원·복지시설 공연 등 봉사활동 경험이 있는 분\n" +
                        "• 음원 제작, 녹음, 홍보 콘텐츠 제작 경험이 있는 분\n" +
                        "• 음악으로 사회적 메시지를 전달하는 활동(환경, 평화, 인권 등)에 관심이 있는 분\n\n" +
                        "리버스는 ‘음악으로 나누는 봉사’를 실천하는 사람들의 무대입니다. " +
                        "화려하지 않아도 괜찮습니다. 한 사람의 하루를 따뜻하게 만드는 노래 한 곡, 작은 공연 하나로도 세상을 바꿀 수 있다고 믿습니다. " +
                        "당신의 진심 어린 목소리와 연주를 리버스의 무대에서 함께 들려주세요.")
                .build();
        intro6.addImages(List.of(
                "https://plus.unsplash.com/premium_photo-1661335458798-74635c70b8a8?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&q=80&w=1170",
                "https://plus.unsplash.com/premium_photo-1664298415497-60325b13618f?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&q=80&w=1170",
                "https://images.unsplash.com/photo-1723816411168-175869ed9797?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&q=80&w=1171",
                "https://images.unsplash.com/photo-1687585612054-2fd94d0aec00?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&q=80&w=1170"        ));

        Club club6 = Club.builder()
                .name("리버스")
                .category(Category.VOLUNTEER)
                .location("음악관 B101")
                .shortIntroduction("밴드·보컬·작곡 등 음악 공연을 함께하는 동아리")
                .introduction(intro6)
                .recruitStart(LocalDateTime.of(2025, 3, 6, 9, 0, 0))
                .recruitEnd(LocalDateTime.of(2025, 3, 20, 18, 0, 0))
                .regularMeetingInfo("매주 화요일 오후 6시")
                .caution("오디션 후 합격자 활동 가능")
                .build();
        clubs.add(club6);

        // 7. 포커스 (사진·영상 봉사)
        ClubIntroduction intro7 = ClubIntroduction.builder()
                .overview("포커스(FOCUS)는 사진과 영상을 통해 사회의 따뜻한 이야기를 기록하고 전하는 봉사·나눔 동아리입니다. " +
                        "카메라와 영상 기술을 활용하여 복지기관, 지역 행사, 사회적 캠페인 등에서 재능기부 활동을 진행하며, " +
                        "세상을 밝히는 사람들의 모습을 담고 그 의미를 널리 알리는 것을 목표로 합니다. " +
                        "회원들은 사진 촬영, 영상 제작, 편집, 콘텐츠 기획 등 다양한 역할을 맡아, 시각적 스토리텔링으로 사회의 긍정적인 변화를 기록합니다. " +
                        "포커스는 ‘기록을 통한 나눔’을 실천하는 동아리로, 예술과 봉사의 가치를 동시에 추구하는 학생들이 함께합니다.")
                .activities("• 지역 복지기관, 봉사 현장, 행사 등에서 사진·영상 촬영 봉사\n" +
                        "• 학교·지역 사회 공익 캠페인(환경, 안전, 인권 등) 영상 제작\n" +
                        "• 사회적 이슈나 봉사활동을 주제로 한 다큐멘터리·인터뷰 영상 기획\n" +
                        "• 복지시설 홍보물(소개 영상, 사진 포스터 등) 제작 지원\n" +
                        "• 사진전 및 영상 상영회를 통한 나눔 문화 확산 활동\n" +
                        "• 사진·영상 제작 관련 교육 세션 및 후배 멘토링 진행")
                .ideal("포커스는 ‘세상의 선한 순간을 기록하고, 따뜻한 시선을 나누는 사람’을 찾습니다.\n\n" +
                        "[자격요건]\n" +
                        "• 사진, 영상, 콘텐츠 제작에 관심이 있고 이를 통해 사회에 기여하고 싶은 학부생\n" +
                        "• 사람, 현장, 사회 문제를 관찰하고 진정성 있게 기록하고 싶은 분\n" +
                        "• 지역사회 행사나 봉사활동에 꾸준히 참여할 수 있는 성실한 분\n" +
                        "• 팀 단위 프로젝트(촬영, 편집, 기획 등)에 협력적으로 참여할 수 있는 분\n" +
                        "• 영상미보다 ‘메시지’와 ‘진심’을 중요하게 여기는 분\n" +
                        "• 작품 공개(사진전, 상영회 등)에 부담 없이 참여할 수 있는 분\n\n" +
                        "[우대사항]\n" +
                        "• DSLR, 미러리스, 캠코더 등 촬영 장비 사용 경험이 있는 분\n" +
                        "• 사진 후보정(라이트룸, 포토샵) 또는 영상 편집(Premiere Pro, DaVinci Resolve 등) 경험이 있는 분\n" +
                        "• 인터뷰, 다큐멘터리, 홍보영상 등 실사 기반 콘텐츠 제작 경험이 있는 분\n" +
                        "• 지역 행사·공익 캠페인 등 사회공헌 프로젝트에 참여한 경험이 있는 분\n" +
                        "• 스토리텔링, 영상 연출, 내레이션 등 서사적 감각이 뛰어난 분\n" +
                        "• 사진·영상 매체를 통해 ‘사람’을 담는 데에 흥미가 있는 분\n\n" +
                        "포커스는 기술보다 ‘진심 어린 시선’을 더 중요하게 생각합니다. " +
                        "누군가의 웃음, 노력, 따뜻한 순간을 카메라에 담고 싶다면, 포커스와 함께 세상을 조금 더 빛나게 만들어보세요.")
                .build();

        Club club7 = Club.builder()
                .name("포커스")
                .category(Category.LITERATURE) // 봉사 동아리
                .location("미디어관 204호")
                .shortIntroduction("사진과 영상으로 세상의 따뜻한 이야기를 기록하는 봉사 동아리")
                .introduction(intro7)
                .recruitStart(LocalDateTime.of(2025, 9, 3, 0, 0, 0))
                .recruitEnd(LocalDateTime.of(2025, 11, 30, 23, 59, 0))
                .regularMeetingInfo("매주 목요일 오후 5시")
                .caution("촬영 장비 보유 여부와 관계없이 참여 가능")
                .build();
        clubs.add(club7);

        intro7.addImages(List.of(
                "https://images.unsplash.com/photo-1650006414512-ba9f6aaf5bf4?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&q=80&w=687",
                "https://images.unsplash.com/photo-1609748629093-a5c6c6e4ca6a?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&q=80&w=1170",
                "https://images.unsplash.com/photo-1468109320504-a5e48f17cfb7?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&q=80&w=1237",
                "https://images.unsplash.com/photo-1663046064997-4f0822dbc797?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&q=80&w=1972"        ));

        // 8. 온기나눔 (봉사·나눔)
        ClubIntroduction intro8 = ClubIntroduction.builder()
                .overview("온기나눔은 지역 사회에 작은 따뜻함을 전하고자 모인 봉사·나눔 동아리입니다. " +
                        "사회복지시설, 지역 아동센터, 노인복지관, 장애인 복지기관 등과 협력하여 정기적인 봉사활동을 기획하고 실행합니다. " +
                        "단순히 일회성 봉사에 그치지 않고, 꾸준한 관계 맺기와 지속 가능한 활동을 통해 지역 사회에 긍정적인 변화를 만들어가는 것을 목표로 합니다. " +
                        "회원들은 아이들과의 학습·놀이 활동, 어르신 말벗 및 생활 지원, 환경 정화 활동, 후원 캠페인 등 다양한 프로그램을 함께 준비합니다. " +
                        "온기나눔은 ‘거창한 영웅’이 아니라, 일상 속에서 할 수 있는 작은 실천을 모아 큰 변화를 만들어가는 학생들의 공간입니다. " +
                        "함께 웃고, 함께 돕고, 함께 성장하는 경험을 하고 싶은 학생이라면 누구나 온기나눔의 활동에 참여할 수 있습니다.")
                .activities("• 사회복지시설, 지역 아동센터, 노인복지관 등과 연계한 정기 봉사활동\n" +
                        "• 아동·청소년 멘토링, 학습 지도, 놀이·문화 체험 프로그램 진행\n" +
                        "• 독거 어르신 말벗·생활 지원, 도시락·간식 나눔 활동\n" +
                        "• 환경 정화, 캠퍼스 및 지역 사회 청소, 플로깅 등 환경 봉사\n" +
                        "• 후원 캠페인, 바자회, 기부 연계 프로그램 기획 및 실행\n" +
                        "• 봉사 후기집·활동 리포트 제작 및 공유를 통한 나눔 문화 확산")
                .ideal("온기나눔은 ‘작은 배려를 행동으로 옮기는 사람’을 찾습니다.\n\n" +
                        "[자격요건]\n" +
                        "• 타인의 입장에서 생각하고 공감하려는 마음을 가진 학부생\n" +
                        "• 정기적인 봉사활동과 준비 모임에 성실하게 참여할 수 있는 분\n" +
                        "• 눈에 띄지 않는 일이라도 꾸준히 맡아서 수행할 수 있는 책임감 있는 분\n" +
                        "• 동아리 구성원 및 기관 관계자와 원활하게 소통할 수 있는 분\n" +
                        "• 사진·글 등으로 봉사 기록을 남기고 공유하는 데 부담이 없는 분\n" +
                        "• 봉사를 통해 본인의 성장과 지역 사회의 변화를 함께 경험하고 싶은 분\n\n" +
                        "[우대사항]\n" +
                        "• 사회복지시설, 지역 아동센터, 봉사 단체 등에서 활동해본 경험이 있는 분\n" +
                        "• 프로그램 기획, 행사 운영, 안내·진행 등 사람들과 함께하는 활동을 좋아하는 분\n" +
                        "• 포스터·카드뉴스 제작 등 홍보 콘텐츠 제작 경험이 있는 분\n" +
                        "• 사진 촬영, 글쓰기 등을 통해 봉사 활동을 기록하고 아카이빙하는 데 관심이 있는 분\n" +
                        "• 사회복지, 교육, 상담, 심리 등 관련 분야에 관심이 있거나 전공 중인 분\n" +
                        "• 장기적인 관점에서 한 기관 또는 한 대상과 꾸준히 관계를 맺고 싶은 분\n\n" +
                        "온기나눔은 완벽한 사람을 찾지 않습니다. " +
                        "작은 시간과 마음을 내어 타인에게 온기를 전하고 싶은 마음이 있다면, 그 자체로 충분한 시작입니다. " +
                        "함께라서 더 따뜻한 봉사를 경험하고 싶은 분들을 온기나눔이 기다리고 있습니다.")
                .build();
        intro8.addImages(List.of(
                "https://images.unsplash.com/photo-1616680214429-d79397e56688?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&q=80&w=1170",
                "https://plus.unsplash.com/premium_photo-1681140560906-4610ee700d1b?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&q=80&w=1170",
                "https://images.unsplash.com/photo-1758599669327-83d310882929?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&q=80&w=1332",
                "https://plus.unsplash.com/premium_photo-1663040337189-fa6906bf2bc4?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&q=80&w=1170"
        ));

        Club club8 = Club.builder()
                .name("온기나눔")
                .category(Category.VOLUNTEER)
                .location("사회관 108호")
                .shortIntroduction("지역사회 봉사와 나눔 실천을 중심으로 하는 봉사 동아리")
                .introduction(intro8)
                .recruitStart(LocalDateTime.of(2025, 9, 3, 0, 0, 0))
                .recruitEnd(LocalDateTime.of(2025, 11, 30, 23, 59, 0))
                .regularMeetingInfo("매주 토요일 오전 10시")
                .caution("정기 봉사 참여 필수")
                .build();
        clubs.add(club8);

        // 9. 비즈온 (창업·비즈니스)
        ClubIntroduction intro9 = ClubIntroduction.builder()
                .overview("비즈온(BizOn)은 ‘비즈니스로 세상을 바꾸자’는 비전을 가진 창업·경영 동아리입니다. " +
                        "창의적인 아이디어를 실현 가능한 비즈니스 모델로 발전시키고, 실제 시장 검증과 실행을 통해 스타트업의 전 과정을 경험합니다. " +
                        "회원들은 팀을 구성해 문제를 정의하고 고객을 분석하며, 서비스 기획부터 마케팅, 재무 모델링까지 전 과정을 직접 수행합니다. " +
                        "단순히 이론을 배우는 것을 넘어 ‘실행 중심의 학습(learning by doing)’을 추구하며, 실제 창업 생태계 속에서 성장할 수 있는 기회를 제공합니다. " +
                        "비즈온은 스타트업, 사회적 기업, 테크 비즈니스 등 다양한 형태의 창업을 함께 탐구하며, 도전적인 아이디어가 현실이 되는 과정을 함께 만들어갑니다.")
                .activities("• 비즈니스 모델(BM) 수립 및 고객 문제 탐색 워크숍\n" +
                        "• 팀 단위 창업 아이디어 기획 및 시제품(MVP) 개발\n" +
                        "• 교내·외 창업 경진대회 참가 및 피칭 실습\n" +
                        "• 기업가 정신, 마케팅, 재무 등 실무 중심 세미나 운영\n" +
                        "• 스타트업 대표 및 전문가 초청 멘토링 세션\n" +
                        "• 투자 유치 모의 피칭, IR 자료 제작, 사업계획서 컨설팅")
                .ideal("비즈온은 ‘아이디어를 실행으로 옮기고, 시장을 통해 검증하는 사람’을 찾습니다.\n\n" +
                        "[자격요건]\n" +
                        "• 창업, 스타트업, 비즈니스 기획에 관심이 있는 학부생\n" +
                        "• 문제를 발견하고 새로운 해결책을 제시하는 데 흥미가 있는 분\n" +
                        "• 도전정신이 강하고 불확실한 환경에서도 실행력을 보여줄 수 있는 분\n" +
                        "• 팀 단위로 기획, 개발, 마케팅 등 다양한 역할을 수행할 수 있는 협업형 인재\n" +
                        "• 매주 정기 세션 및 프로젝트에 꾸준히 참여할 수 있는 성실한 분\n" +
                        "• 시장조사, 사용자 인터뷰, 피칭 등 실전 경험을 쌓고자 하는 분\n\n" +
                        "[우대사항]\n" +
                        "• 창업 관련 대회, 해커톤, 공모전 등에 참가한 경험이 있는 분\n" +
                        "• 비즈니스 모델 캔버스, 린 캔버스 등 기획 프레임워크 활용 경험이 있는 분\n" +
                        "• 제품 기획, 디자인, 개발, 마케팅, 재무 등 한 분야 이상에 실무적 이해가 있는 분\n" +
                        "• 스타트업, 인큐베이팅, 액셀러레이팅 프로그램 등에 관심이 있는 분\n" +
                        "• 피칭, 프레젠테이션, 커뮤니케이션 능력이 뛰어난 분\n" +
                        "• 사회문제를 창의적으로 해결하는 임팩트 비즈니스에 관심이 있는 분\n\n" +
                        "비즈온은 ‘도전’과 ‘실행’을 즐기는 모든 학생에게 열려 있습니다. " +
                        "아이디어가 있다면 현실로 옮기고, 없더라도 함께 만들어가면 됩니다. " +
                        "당신의 첫 창업 경험, 비즈온에서 시작해보세요.")
                .build();
        intro9.addImages(List.of(
                "https://images.unsplash.com/photo-1700241956206-79b70da5f4b8?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&q=80&w=1161",
                "https://images.unsplash.com/photo-1621856625680-282ec3a17db8?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&q=80&w=1170",
                "https://images.unsplash.com/photo-1565882694798-4c9d004e65b7?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&q=80&w=1176",
                "https://plus.unsplash.com/premium_photo-1661400034625-d9ff65962f48?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&q=80&w=1332"
        ));

        Club club9 = Club.builder()
                .name("비즈온")
                .category(Category.STUDY)
                .location("경영관 201호")
                .shortIntroduction("창업 아이디어를 현실로 만드는 비즈니스 창업 동아리")
                .introduction(intro9)
                .recruitStart(LocalDateTime.of(2025, 9, 3, 0, 0, 0))
                .recruitEnd(LocalDateTime.of(2025, 11, 30, 23, 59, 0))
                .regularMeetingInfo("매주 월요일 오후 6시")
                .caution("창업 경진대회 참가 의지 필수")
                .build();
        clubs.add(club9);

        // 10. 핀라이트 (금융·투자)
        ClubIntroduction intro10 = ClubIntroduction.builder()
                .overview("핀라이트(Finlight)는 금융과 투자를 중심으로 경제적 사고력과 분석 능력을 기르는 학술 동아리입니다. " +
                        "‘금융(Finance)’과 ‘빛(Light)’의 합성어로, 세상의 흐름을 읽고 경제의 본질을 이해하며, 올바른 금융 지식을 통해 스스로의 미래를 밝히자는 뜻을 담고 있습니다. " +
                        "회원들은 국내외 경제 이슈를 토론하고, 실제 주식 및 자산 운용 사례를 분석하며, 투자와 재테크의 기본 원리를 학문적으로 탐구합니다. " +
                        "또한 단기 수익보다 ‘지속 가능한 투자’를 목표로 하여, 장기적 관점에서 합리적인 의사결정을 내릴 수 있는 금융인으로 성장하는 것을 지향합니다. " +
                        "핀라이트는 금융을 단순히 돈을 버는 수단이 아닌, 사회와 개인을 이해하는 지식의 한 축으로 바라봅니다.")
                .activities("• 주식 및 ETF 모의투자 프로그램 운영\n" +
                        "• 거시경제, 산업 트렌드, 기업 재무분석 세미나\n" +
                        "• 경제 시사 토론 및 주간 리포트 발표\n" +
                        "• 금융 관련 자격증(투자자산운용사, AFPK 등) 스터디 진행\n" +
                        "• 가상 포트폴리오 구성 및 수익률 분석\n" +
                        "• 금융 전문가 초청 특강 및 금융권 진로 멘토링 세션")
                .ideal("핀라이트는 ‘데이터를 읽고, 흐름을 이해하며, 금융을 통해 사고하는 사람’을 찾습니다.\n\n" +
                        "[자격요건]\n" +
                        "• 경제, 경영, 금융, 투자 등 관련 분야에 관심이 있는 학부생\n" +
                        "• 주식, 채권, 부동산, 대체투자 등 다양한 자산군에 흥미가 있는 분\n" +
                        "• 숫자와 데이터 해석에 강점을 가지고 있으며, 논리적 사고를 즐기는 분\n" +
                        "• 경제 뉴스를 꾸준히 탐독하고 스스로 시장의 흐름을 분석하려는 학습 태도를 가진 분\n" +
                        "• 단기 성과보다는 장기적 관점에서 금융 지식을 쌓고자 하는 분\n" +
                        "• 팀 세미나 및 리서치 프로젝트에 꾸준히 참여할 수 있는 분\n\n" +
                        "[우대사항]\n" +
                        "• 주식, ETF, 가상자산 등 실제 투자 경험이 있는 분\n" +
                        "• 재무제표 분석, 밸류에이션(가치평가), 산업 리서치 경험이 있는 분\n" +
                        "• Excel, Power BI, Python 등 데이터 분석 도구를 활용할 수 있는 분\n" +
                        "• 경제·금융 관련 공모전, 경진대회, 리서치 챌린지 참가 경험이 있는 분\n" +
                        "• CFA, AFPK, 투자자산운용사 등 금융 자격증 취득을 준비하거나 보유한 분\n" +
                        "• 금융권 진로(자산운용, 투자은행, 컨설팅 등)에 관심이 있는 분\n\n" +
                        "핀라이트는 ‘빠른 수익’보다 ‘깊은 이해’를 추구합니다. " +
                        "시장의 흐름을 공부하며 금융의 원리를 탐구하고, 경제적 통찰력을 키워가고 싶은 분이라면 핀라이트에서 함께 성장해보세요.")
                .build();
        intro10.addImages(List.of(
                "https://plus.unsplash.com/premium_photo-1661782751910-da7995e9baa7?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&q=80&w=1169",
                "https://plus.unsplash.com/premium_photo-1661611260273-4312872f53da?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&q=80&w=1992",
                "https://plus.unsplash.com/premium_photo-1661746154460-1ee9008a501b?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&q=80&w=1170",
                "https://plus.unsplash.com/premium_photo-1663040170703-cb0d52d66165?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&q=80&w=1170"        ));

        Club club10 = Club.builder()
                .name("핀라이트")
                .category(Category.STUDY)
                .location("경상관 110호")
                .shortIntroduction("경제·금융 지식을 함께 배우는 투자 학술 동아리")
                .introduction(intro10)
                .recruitStart(LocalDateTime.of(2025, 3, 10, 9, 0, 0))
                .recruitEnd(LocalDateTime.of(2025, 3, 24, 18, 0, 0))
                .regularMeetingInfo("매주 금요일 오후 7시")
                .caution("경제신문 정기 구독자 우대")
                .build();
        clubs.add(club10);

// 11. 글빛 (문학·창작)
        ClubIntroduction intro11 = ClubIntroduction.builder()
                .overview("글빛은 시, 소설, 수필, 에세이 등 다양한 문학 창작 활동을 통해 생각과 감정을 글로 표현하는 문예·창작 동아리입니다. " +
                        "‘빛나는 글로 마음을 잇는다’는 의미처럼, 개인의 이야기를 예술적 언어로 표현하고, 서로의 작품을 읽고 나누며 성장하는 것을 목표로 합니다. " +
                        "학기 중에는 정기 창작 모임을 통해 글을 쓰고 피드백을 주고받으며, 문학적 표현력과 사유의 깊이를 함께 발전시켜 나갑니다. " +
                        "또한 문학 작품 낭독회, 글쓰기 워크숍, 교내 문예대회 참가 등 다양한 활동을 통해 문학을 즐기고 공유하는 문화를 만들어가고 있습니다. " +
                        "글빛은 글을 ‘잘 쓰는 사람’보다, 글을 통해 세상을 더 깊이 이해하고 싶은 사람들의 모임입니다.")
                .activities("• 주제별 창작 모임 및 자유 창작 세션 운영 (시, 소설, 수필 등)\n" +
                        "• 완성된 작품을 바탕으로 한 합평회(피드백 모임) 진행\n" +
                        "• 문학 낭독회 및 창작 발표회 개최\n" +
                        "• 교내·외 문예 공모전 및 대회 참가\n" +
                        "• 학기 말 ‘글빛 동인지’ 발간 및 전시 부스 운영\n" +
                        "• 문학 강연, 창작 워크숍, 독서토론회 등 정기 세미나 진행")
                .ideal("글빛은 ‘사유를 글로 표현하고, 감정을 나누는 사람’을 기다립니다.\n\n" +
                        "[자격요건]\n" +
                        "• 글쓰기와 문학에 관심이 있는 학부생 (전공 무관)\n" +
                        "• 시, 소설, 수필 등 자유로운 형식의 창작에 도전하고 싶은 분\n" +
                        "• 자신의 생각과 감정을 언어로 풀어내는 데 즐거움을 느끼는 분\n" +
                        "• 다른 사람의 작품을 존중하고 진심 어린 피드백을 나눌 수 있는 분\n" +
                        "• 정기 모임 및 합평회에 꾸준히 참여할 수 있는 성실한 분\n" +
                        "• 완성도보다 ‘표현의 진정성’을 중요하게 생각하는 분\n\n" +
                        "[우대사항]\n" +
                        "• 교내·외 문예대회, 공모전, 블로그 등에서 창작 경험이 있는 분\n" +
                        "• 출판, 편집, 스토리텔링, 시나리오 등 문학적 글쓰기에 흥미가 있는 분\n" +
                        "• 낭독회, 전시, 오디오북 등 문학 콘텐츠 제작에 관심이 있는 분\n" +
                        "• 비평문, 에세이, 리뷰 등 분석적 글쓰기를 좋아하는 분\n" +
                        "• 인문학·철학·예술 분야에 관심이 있어 글에 깊이를 더하고 싶은 분\n" +
                        "• 꾸준히 글을 쓰고자 하는 습관을 기르고 싶은 분\n\n" +
                        "글빛은 ‘문학이 어려운 사람’에게도 열려 있습니다. " +
                        "중요한 것은 표현의 완성도가 아니라, 자신만의 이야기를 진심으로 전하려는 마음입니다. " +
                        "단어 하나, 문장 하나를 통해 세상을 바라보는 시선이 확장되는 경험—그 여정을 함께하고 싶은 분이라면, 글빛이 당신의 이야기를 기다리고 있습니다.")
                .build();

        intro11.addImages(List.of(
                "https://plus.unsplash.com/premium_photo-1661661591757-89ebfe7b357a?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&q=80&w=1183",
                "https://plus.unsplash.com/premium_photo-1661694072616-955dc258120e?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&q=80&w=1159",
                "https://images.unsplash.com/photo-1655472355485-d949925e67bb?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&q=80&w=1052",
                "https://plus.unsplash.com/premium_photo-1725408127758-fb45b0f11ad9?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&q=80&w=1224"
        ));

        Club club11 = Club.builder()
                .name("글빛")
                .category(Category.LITERATURE)
                .location("인문관 113호")
                .shortIntroduction("시·소설·수필 등 창작을 통해 감성과 표현을 나누는 문예 동아리")
                .introduction(intro11)
                .recruitStart(LocalDateTime.of(2025, 3, 13, 9, 0, 0))
                .recruitEnd(LocalDateTime.of(2025, 3, 27, 18, 0, 0))
                .regularMeetingInfo("매주 금요일 오후 6시")
                .caution("창작 경험자 우대, 신입회원 대상 글쓰기 워크숍 운영 예정")
                .build();
        clubs.add(club11);


        // 12. 무대열전 (연극·공연예술 · 종교)
        ClubIntroduction intro12 = ClubIntroduction.builder()
                .overview("무대열전은 연극과 공연예술을 통해 신앙과 삶의 이야기를 풀어내는 공연예술 동아리입니다. " +
                        "우리는 무대를 하나의 ‘작은 예배 공간’이자 ‘이야기의 장’으로 바라보며, 사랑, 용서, 희망, 회복과 같은 주제를 연극으로 표현합니다. " +
                        "성경 속 이야기나 신앙인의 삶, 일상에서 느끼는 고민과 질문들을 각색하여, 관객이 공감하고 위로를 받을 수 있는 작품을 만드는 것을 목표로 합니다. " +
                        "무대열전의 공연은 단순한 오락을 넘어, 관객과 함께 삶의 의미를 돌아보고, 신앙적 메시지를 나누는 시간이 되기를 지향합니다. " +
                        "연기와 무대를 처음 접하는 사람이라도, 진심으로 ‘이야기를 전하고 싶다’는 마음이 있다면 누구나 함께할 수 있습니다.")
                .activities("• 신앙과 삶을 주제로 한 공연 대본 기획 및 각색 작업\n" +
                        "• 연기 기본기·발성·동선 등 연습 및 역할별 리허설\n" +
                        "• 무대 연출, 조명, 음향, 소품·무대미술 워크숍 진행\n" +
                        "• 교내 공연, 채플·예배 특송 공연, 지역 교회·기관 초청 공연\n" +
                        "• 공연 전후 묵상·나눔 시간 및 작품 주제에 대한 신앙·인문 토론\n" +
                        "• 학기 말 정기 공연 및 간증/작품 해설이 포함된 관객과의 대화 시간 마련")
                .ideal("무대열전은 ‘무대를 통해 믿음과 이야기를 전하고 싶은 사람’을 찾습니다.\n\n" +
                        "[자격요건]\n" +
                        "• 연극, 공연, 무대예술에 관심이 있고 함께 작품을 만들어보고 싶은 학부생\n" +
                        "• 사랑, 용서, 소망, 용기 등 신앙적 가치와 삶의 이야기를 나누는 데 마음이 있는 분\n" +
                        "• 팀 연습과 공연 준비에 꾸준히 참여할 수 있는 성실한 분\n" +
                        "• 무대 위·뒤에서 맡은 역할을 책임감 있게 수행할 수 있는 분\n" +
                        "• 관객과의 소통, 나눔, 피드백을 기쁘게 받아들일 수 있는 분\n" +
                        "• 특정 종교에 대한 적대감이 없고, 신앙적 메시지를 담은 공연에 자연스럽게 참여할 수 있는 분\n\n" +
                        "[우대사항]\n" +
                        "• 연극·뮤지컬·연기 동아리 또는 학교·교회 공연 활동 경험이 있는 분\n" +
                        "• 대본 집필, 각색, 시나리오 작성 등 글쓰기 경험이 있는 분\n" +
                        "• 무대 연출, 조명, 음향, 무대미술, 의상 등 스태프 업무에 관심이 있는 분\n" +
                        "• 찬양팀, 성가대, 워십팀 등 예배 관련 예술 활동 경험이 있는 분\n" +
                        "• 사람들 앞에서 말하기, 발표, 진행(사회) 경험이 있는 분\n" +
                        "• 신앙, 종교, 철학, 인문학적 주제를 작품에 녹여보고 싶은 분\n\n" +
                        "무대열전은 ‘완벽한 배우’를 찾지 않습니다. " +
                        "서툴러도 괜찮습니다. 중요한 것은 무대를 통해 전하고 싶은 진심과, 함께 준비하는 과정을 소중히 여기는 마음입니다. " +
                        "신앙과 예술, 공연과 나눔이 만나는 무대에서 새로운 이야기를 함께 써 내려가고 싶은 분들을 기다리고 있습니다.")
                .build();

        intro12.addImages(List.of(
                "https://images.unsplash.com/photo-1563175594-561873ec6588?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&q=80&w=1170",
                "https://images.unsplash.com/photo-1590758369991-5aec00cf3387?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&q=80&w=1334",
                "https://images.unsplash.com/photo-1741681001067-848c04ff9849?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&q=80&w=1170",
                "https://images.unsplash.com/photo-1752300779727-13d587a42881?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&q=80&w=627"
        ));

        Club club12 = Club.builder()
                .name("무대열전")
                .category(Category.LITERATURE)
                .location("예술관 114호")
                .shortIntroduction("신앙과 삶의 이야기를 연극과 공연으로 전하는 공연예술 동아리")
                .introduction(intro12)
                .recruitStart(LocalDateTime.of(2025, 9, 3, 0, 0, 0))
                .recruitEnd(LocalDateTime.of(2025, 11, 30, 23, 59, 0))
                .regularMeetingInfo("매주 토요일 오후 1시")
                .caution("공연 준비 기간에는 추가 연습이 있을 수 있으며, 공연 전 참여율 80% 이상 필요")
                .build();
        clubs.add(club12);


        // 13. FC JNU (축구)
        ClubIntroduction intro13 = ClubIntroduction.builder()
                .overview("FC JNU는 축구를 사랑하는 학생들이 모여 땀과 열정을 함께 나누는 전남대학교 대표 축구 동아리입니다. " +
                        "단순히 공을 차는 모임이 아니라, 스포츠맨십과 팀워크를 중심으로 건강한 교류 문화를 만들어가는 것이 목표입니다. " +
                        "회원들은 정기적인 훈련과 연습 경기를 통해 기술을 향상시키고, 교내 리그전과 지역 친선 경기에서 실력을 겨룹니다. " +
                        "축구를 매개로 학과, 학년, 전공의 경계를 넘어 새로운 인연을 만들며, 팀원 간의 단결력과 협동심을 기릅니다. " +
                        "FC JNU는 승리보다 ‘함께 뛰는 즐거움’을 더 중요하게 생각하는 공동체입니다. 초보부터 숙련자까지, 축구를 진심으로 즐기고 싶은 학생이라면 누구나 환영합니다.")
                .activities("• 주 2회 정기 훈련 및 내부 연습 경기 (기초 체력 + 기술 향상 훈련)\n" +
                        "• 전남대학교 교내 리그전 및 학과별 친선 대회 참가\n" +
                        "• 지역 대학·아마추어팀과의 교류전 및 스파링 경기\n" +
                        "• 전술·포지션·팀워크 중심의 전략 회의 및 훈련 세션\n" +
                        "• 체력 강화 프로그램(러닝, 스트레칭, 근력운동) 병행\n" +
                        "• 방학 중 전지훈련 및 ‘FC JNU 컵’ 자체 토너먼트 개최\n" +
                        "• 경기 후 회식 및 팀 커뮤니티 모임을 통한 친목 도모")
                .ideal("FC JNU는 ‘함께 뛰며, 함께 성장하는 선수’를 찾습니다.\n\n" +
                        "[자격요건]\n" +
                        "• 축구를 좋아하고 정기 훈련 및 경기 참여가 가능한 학부생\n" +
                        "• 팀워크를 존중하며 동료를 배려할 줄 아는 분\n" +
                        "• 승패에 연연하기보다 운동 자체를 즐길 줄 아는 긍정적인 마인드\n" +
                        "• 주 1회 이상 연습 경기 또는 훈련에 꾸준히 참여할 수 있는 분\n" +
                        "• 운동 중 안전 수칙을 지키고 책임감 있게 활동할 수 있는 분\n" +
                        "• 포지션(공격수, 미드필더, 수비수, 골키퍼)에 관계없이 팀 내 역할을 충실히 수행할 수 있는 분\n\n" +
                        "[우대사항]\n" +
                        "• 중·고등학교 또는 동호회 수준의 축구 경험이 있는 분\n" +
                        "• 포지션별 전문성(세트피스, 골키핑, 수비 라인 조율 등)을 갖춘 분\n" +
                        "• 경기 운영, 전략 구상, 코칭 등 리더십 역량이 있는 분\n" +
                        "• 심판 자격증, 스포츠 지도사 등 관련 자격 보유자\n" +
                        "• 체육 관련 전공 또는 스포츠 분석, 트레이닝에 관심 있는 분\n" +
                        "• 축구 외에도 러닝, 풋살 등 체력 향상 프로그램 참여 의지가 있는 분\n\n" +
                        "FC JNU는 ‘승리를 위한 팀’이 아니라 ‘함께 뛰며 성장하는 팀’입니다. " +
                        "축구화를 신는 순간부터 서로가 한 팀이 됩니다. " +
                        "열정과 땀, 웃음으로 가득한 운동장에서 진짜 팀워크를 느끼고 싶은 분이라면, FC JNU가 그 무대를 준비하고 있습니다.")
                .build();

        intro13.addImages(List.of(
                "https://images.unsplash.com/photo-1762053275412-03726506562a?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&q=80&w=1170",
                "https://images.unsplash.com/photo-1591953551286-91b742a52199?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&q=80&w=1025",
                "https://images.unsplash.com/photo-1720904926069-553422e33314?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&q=80&w=1208",
                "https://images.unsplash.com/photo-1739550635585-484633b21450?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&q=80&w=1170"
        ));

        Club club13 = Club.builder()
                .name("FC JNU")
                .category(Category.SPORTS)
                .location("운동장")
                .shortIntroduction("축구를 통해 교류하고 팀워크를 다지는 전남대학교 대표 체육 동아리")
                .introduction(intro13)
                .recruitStart(LocalDateTime.of(2025, 9, 3, 0, 0, 0))
                .recruitEnd(LocalDateTime.of(2025, 11, 30, 23, 59, 0))
                .regularMeetingInfo("매주 수요일 오후 7시, 주말 연습 경기 진행")
                .caution("연습 경기 참여 필수, 부상 예방을 위한 기본 스트레칭 필수")
                .build();
        clubs.add(club13);

        // 14. 엘피스 (기독교 신앙·예배)
        ClubIntroduction intro14 = ClubIntroduction.builder()
                .overview("엘피스(Elpis)는 ‘소망’이라는 뜻의 헬라어에서 이름을 따온 전남대학교 기독교 신앙 공동체 동아리입니다. " +
                        "하나님을 예배하고, 말씀을 배우며, 서로의 삶을 나누는 따뜻한 믿음의 공동체를 지향합니다. " +
                        "엘피스는 단순한 종교 모임이 아니라, 대학이라는 공간 속에서 신앙의 의미를 함께 고민하고, " +
                        "진리를 배우며, 사랑과 섬김의 삶을 실천하는 신앙인들의 모임입니다. " +
                        "매주 정기 예배와 소그룹 성경 공부를 통해 신앙의 기초를 다지고, 기도 모임과 찬양을 통해 하나님과 더 깊이 교제합니다. " +
                        "또한 학기 중에는 교내외 봉사활동, 찬양콘서트, 수련회, 캠퍼스 전도 등 다양한 신앙 프로그램을 함께 진행하며, " +
                        "‘믿음이 있는 지성인, 지성이 있는 신앙인’을 목표로 성장해갑니다.")
                .activities("• 주 1회 정기 예배 및 찬양 모임\n" +
                        "• 소그룹 성경 공부(Bible Study) 및 묵상 나눔\n" +
                        "• 신앙 간증 및 주제별 세미나(진로, 인간관계, 사회문제 등)\n" +
                        "• 교내외 봉사활동(노숙인 급식 봉사, 지역아동센터 멘토링 등)\n" +
                        "• 연합 수련회, 리트릿, 찬양콘서트 등 신앙 교류 행사\n" +
                        "• 시험기간 기도모임 및 신입생 환영예배 진행")
                .ideal("엘피스는 ‘믿음을 나누고, 사랑으로 섬기는 사람’을 찾습니다.\n\n" +
                        "[자격요건]\n" +
                        "• 기독교 신앙을 가진 학생 또는 신앙에 관심이 있는 학부생\n" +
                        "• 말씀을 배우고 예배에 참여하며 신앙 공동체의 일원으로 함께하고 싶은 분\n" +
                        "• 정기 모임(예배, 소그룹, 기도모임 등)에 꾸준히 참여할 수 있는 분\n" +
                        "• 타인을 존중하고 열린 마음으로 신앙과 생각을 나눌 수 있는 분\n" +
                        "• 공동체 생활 속에서 서로를 위해 기도하고 돕는 것을 즐기는 분\n" +
                        "• 교내외 봉사, 찬양, 나눔 활동에 관심이 있는 분\n\n" +
                        "[우대사항]\n" +
                        "• 찬양팀(보컬, 악기 연주 등) 또는 예배 스태프(영상, 음향, 조명 등) 참여 경험이 있는 분\n" +
                        "• 성경 공부나 소그룹 인도 경험이 있는 분\n" +
                        "• 예배 기획, 봉사 프로그램 운영, 교회 사역 등 리더십 경험이 있는 분\n" +
                        "• 신앙적 상담, 기도 모임 진행, 후배 멘토링에 관심이 있는 분\n" +
                        "• 교회, 선교 단체 등에서 활동 중이거나 신앙 네트워크를 확장하고 싶은 분\n\n" +
                        "엘피스는 신앙의 깊이와 인간적인 따뜻함이 공존하는 공동체입니다. " +
                        "하나님을 향한 찬양, 서로를 위한 기도, 그리고 삶으로 드리는 예배 속에서 진정한 소망을 찾고 싶은 분들을 초대합니다. " +
                        "엘피스의 무대는 교회가 아닌 ‘캠퍼스’이며, 우리의 예배는 일상의 모든 순간에 이어집니다.")
                .build();

        intro14.addImages(List.of(
                "https://plus.unsplash.com/premium_photo-1734014584899-f98e51c73960?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&q=80&w=687",
                "https://plus.unsplash.com/premium_photo-1734014582198-408d2aae4931?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&q=80&w=687",
                "https://plus.unsplash.com/premium_photo-1723914175304-04f9b50b13e6?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&q=80&w=1170",
                "https://images.unsplash.com/photo-1723745707852-fa60211d73a8?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&q=80&w=1170"
        ));

        Club club14 = Club.builder()
                .name("엘피스")
                .category(Category.RELIGION)
                .location("학생회관 207호")
                .shortIntroduction("예배와 나눔을 통해 신앙의 소망을 함께 세워가는 기독교 동아리")
                .introduction(intro14)
                .recruitStart(LocalDateTime.of(2025, 3, 14, 9, 0, 0))
                .recruitEnd(LocalDateTime.of(2025, 3, 28, 18, 0, 0))
                .regularMeetingInfo("매주 목요일 오후 6시 예배, 주말 소그룹 모임")
                .caution("특정 교단 소속 없이, 모든 기독교 신앙인을 환영합니다.")
                .build();
        clubs.add(club14);


        return clubRepository.saveAll(clubs);
    }

    private void seedApplyFormsAndQuestions(List<Club> clubs) {

        Map<String, Club> clubByName = clubs.stream()
                .collect(Collectors.toMap(Club::getName, c -> c));

        List<ClubApplyForm> forms = new ArrayList<>();

        ClubApplyForm form1 = clubApplyFormRepository.save(
                ClubApplyForm.builder()
                        .club(clubByName.get("인터엑스"))
                        .title("인터엑스 2025 상반기 모집")
                        .description("사회문제 해결과 토론에 열정이 있는 분들을 모집합니다. 함께 배우고 실천하며 작은 변화를 만들어가요.")
                        .build()
        );
        forms.add(form1);

        ClubApplyForm form2 = clubApplyFormRepository.save(
                ClubApplyForm.builder()
                        .club(clubByName.get("에이아이디브"))
                        .title("에이아이디브 2025 상반기 신입 부원 모집")
                        .description("AI와 데이터 분석에 관심 있는 학생들을 환영합니다. 논문 스터디와 실전 프로젝트를 함께해요.")
                        .build()
        );
        forms.add(form2);

        ClubApplyForm form3 = clubApplyFormRepository.save(
                ClubApplyForm.builder()
                        .club(clubByName.get("코드마스터"))
                        .title("코드마스터 2025 상반기 리쿠르팅")
                        .description("프로그래밍 실력을 키우고 싶은 개발자 지망생을 모집합니다. 함께 공부하고 실제 서비스를 만들어봅시다.")
                        .build()
        );
        forms.add(form3);

        // 로보테크는 스포츠·스포츠테크 동아리로 변경된 버전에 맞게 수정
        ClubApplyForm form4 = clubApplyFormRepository.save(
                ClubApplyForm.builder()
                        .club(clubByName.get("로보테크"))
                        .title("로보테크 2025 스포츠·스포츠테크 팀원 모집")
                        .description("축구·농구 등 다양한 스포츠를 즐기며, 기록 측정과 스포츠테크에도 도전해보고 싶은 학생을 찾습니다. 함께 뛰고, 함께 데이터로 성장해요.")
                        .build()
        );
        forms.add(form4);

        // 아트픽은 예술·디자인 재능기부 봉사 동아리로 수정
        ClubApplyForm form5 = clubApplyFormRepository.save(
                ClubApplyForm.builder()
                        .club(clubByName.get("아트픽"))
                        .title("아트픽 2025 예술 재능기부 봉사단 모집")
                        .description("벽화, 공간 꾸미기, 포스터 제작 등 예술·디자인 재능을 나누고 싶은 분을 모집합니다. 전공 무관, 그림과 디자인을 좋아하는 마음이면 충분해요.")
                        .build()
        );
        forms.add(form5);

        // 리버스는 음악 공연 봉사 동아리로 수정
        ClubApplyForm form6 = clubApplyFormRepository.save(
                ClubApplyForm.builder()
                        .club(clubByName.get("리버스"))
                        .title("리버스 2025 음악 봉사팀 신입 모집")
                        .description("보컬, 연주, 작곡 등 음악에 열정 있는 분을 찾습니다. 복지시설 공연과 자선무대를 함께 준비하며 음악으로 따뜻함을 나눠요.")
                        .build()
        );
        forms.add(form6);

        // 포커스는 사진·영상 재능기부 봉사 동아리로 수정
        ClubApplyForm form7 = clubApplyFormRepository.save(
                ClubApplyForm.builder()
                        .club(clubByName.get("포커스"))
                        .title("포커스 2025 사진·영상 봉사팀 모집")
                        .description("사진과 영상으로 봉사 현장과 공익 캠페인을 기록하고 싶은 분을 모집합니다. 장비가 없어도, 배워보고 싶은 열정만 있으면 환영해요.")
                        .build()
        );
        forms.add(form7);

        ClubApplyForm form8 = clubApplyFormRepository.save(
                ClubApplyForm.builder()
                        .club(clubByName.get("온기나눔"))
                        .title("온기나눔 2025 봉사활동 참가자 모집")
                        .description("지역 아동센터 및 복지시설 봉사활동에 함께할 따뜻한 마음의 부원을 찾습니다. 꾸준히 함께할 분이라면 전공·학년 상관없이 환영합니다.")
                        .build()
        );
        forms.add(form8);

        ClubApplyForm form9 = clubApplyFormRepository.save(
                ClubApplyForm.builder()
                        .club(clubByName.get("비즈온"))
                        .title("비즈온 2025 창업 아이디어팀 모집")
                        .description("창업과 비즈니스에 관심 있는 학생들을 위한 리쿠르팅입니다. 함께 아이디어를 사업으로 발전시키고, 창업 경진대회에도 도전해봐요.")
                        .build()
        );
        forms.add(form9);

        ClubApplyForm form10 = clubApplyFormRepository.save(
                ClubApplyForm.builder()
                        .club(clubByName.get("핀라이트"))
                        .title("핀라이트 2025 경제·금융 세미나 참여자 모집")
                        .description("금융과 투자에 관심 있는 학생을 모집합니다. 모의투자와 시사 경제 세미나를 통해 함께 공부하고 분석해요.")
                        .build()
        );
        forms.add(form10);


        // 11. 글빛 (문학·창작)
        ClubApplyForm form11 = clubApplyFormRepository.save(
                ClubApplyForm.builder()
                        .club(clubByName.get("글빛"))
                        .title("글빛 2025 상반기 창작 멤버 모집")
                        .description("시, 소설, 수필 등 글쓰기를 좋아하는 분들을 모집합니다. 합평 모임과 동인지 제작에 함께 참여해요.")
                        .build()
        );
        forms.add(form11);

        // 12. 무대열전 (연극·공연예술·종교)
        ClubApplyForm form12 = clubApplyFormRepository.save(
                ClubApplyForm.builder()
                        .club(clubByName.get("무대열전"))
                        .title("무대열전 2025 신앙·연극 공연팀 모집")
                        .description("신앙과 삶의 이야기를 연극과 공연으로 전하고 싶은 분을 모집합니다. 연기, 대본, 연출, 조명 등 다양한 포지션에 도전해보세요.")
                        .build()
        );
        forms.add(form12);

        // 13. FC JNU (축구)
        ClubApplyForm form13 = clubApplyFormRepository.save(
                ClubApplyForm.builder()
                        .club(clubByName.get("FC JNU"))
                        .title("FC JNU 2025 시즌 신규 선수 모집")
                        .description("축구를 좋아하고 함께 땀 흘릴 팀원을 찾습니다. 정기 훈련과 교내 리그전에서 진짜 팀워크를 느껴봐요.")
                        .build()
        );
        forms.add(form13);

        // 14. 엘피스 (기독교 신앙·예배)
        ClubApplyForm form14 = clubApplyFormRepository.save(
                ClubApplyForm.builder()
                        .club(clubByName.get("엘피스"))
                        .title("엘피스 2025 신입 공동체 회원 모집")
                        .description("예배와 말씀, 교제를 통해 신앙을 함께 세워갈 기독교 공동체입니다. 캠퍼스에서 믿음의 동행을 찾고 싶은 분을 초대합니다.")
                        .build()
        );
        forms.add(form14);

        // ========== 6) FORM_QUESTION ==========

// --- form1 (인터엑스: 사회문제 탐구) ---
        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form1)
                .question("간단한 자기소개와 전공, 그리고 관심 있는 사회 문제를 함께 적어주세요.")
                .fieldType(FieldType.TEXT)
                .isRequired(true)
                .displayOrder(1L)
                .build());

        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form1)
                .question("주 1회 정기 세미나(평일 저녁)에 꾸준히 참여가 가능하신가요?")
                .fieldType(FieldType.RADIO)
                .isRequired(true)
                .displayOrder(2L)
                .options(List.of("예", "아니오"))
                .build());

        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form1)
                .question("인터엑스에 지원하게 된 동기와 함께 깊이 탐구해보고 싶은 사회 문제를 구체적으로 작성해주세요.")
                .fieldType(FieldType.TEXT)
                .isRequired(true)
                .displayOrder(3L)
                .build());

        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form1)
                .question("인터엑스 1차 면접 가능 시간대를 선택해주세요.")
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


// --- form2 (에이아이디브: AI·데이터 연구) ---
        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form2)
                .question("간단한 자기소개와 함께 프로그래밍 및 수학·통계 배경을 적어주세요.")
                .fieldType(FieldType.TEXT)
                .isRequired(true)
                .displayOrder(1L)
                .build());

        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form2)
                .question("Python을 사용해본 경험이 있으신가요?")
                .fieldType(FieldType.RADIO)
                .isRequired(true)
                .displayOrder(2L)
                .options(List.of("예", "아니오"))
                .build());

        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form2)
                .question("AIDive에서 집중적으로 공부해보고 싶은 AI/데이터 분야와 그 이유를 적어주세요.")
                .fieldType(FieldType.TEXT)
                .isRequired(true)
                .displayOrder(3L)
                .build());

        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form2)
                .question("AIDive 오리엔테이션 및 면접 가능 시간대를 선택해주세요.")
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


// --- form3 (코드마스터: 프로그래밍·개발) ---
        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form3)
                .question("사용 가능한 프로그래밍 언어와 각각의 숙련도를 적어주세요.")
                .fieldType(FieldType.TEXT)
                .isRequired(true)
                .displayOrder(1L)
                .build());

        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form3)
                .question("팀 단위 개발 프로젝트에 참여해본 경험이 있으신가요?")
                .fieldType(FieldType.RADIO)
                .isRequired(true)
                .displayOrder(2L)
                .options(List.of("예", "아니오"))
                .build());

        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form3)
                .question("코드마스터에서 만들고 싶은 서비스나 프로젝트 아이디어가 있다면 자유롭게 적어주세요.")
                .fieldType(FieldType.TEXT)
                .isRequired(false)
                .displayOrder(3L)
                .build());

        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form3)
                .question("코드마스터 면접 및 OT 가능 시간대를 선택해주세요.")
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


// --- form4 (로보테크: 스포츠·스포츠테크) ---
        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form4)
                .question("간단한 자기소개와 함께 좋아하는 스포츠 종목, 주 포지션(또는 관심 포지션)을 적어주세요.")
                .fieldType(FieldType.TEXT)
                .isRequired(true)
                .displayOrder(1L)
                .build());

        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form4)
                .question("주 1~2회 정기 운동 모임 및 연습 경기에 꾸준히 참여가 가능하신가요?")
                .fieldType(FieldType.RADIO)
                .isRequired(true)
                .displayOrder(2L)
                .options(List.of("예", "아니오"))
                .build());

        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form4)
                .question("스포츠 기록 측정(러닝 기록, 스탯, 영상 분석 등)이나 스포츠테크(기기·앱 활용)에 관심이 있는 부분이 있다면 적어주세요.")
                .fieldType(FieldType.TEXT)
                .isRequired(false)
                .displayOrder(3L)
                .build());

        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form4)
                .question("로보테크 OT 및 간단한 실기·적응 테스트가 가능한 시간대를 선택해주세요.")
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


// --- form5 (아트픽: 예술·디자인 재능기부 봉사) ---
        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form5)
                .question("관심 있는 예술·디자인 분야(예: 회화, 일러스트, 그래픽, 사진 등)와 본인의 작업 스타일 또는 강점을 적어주세요.")
                .fieldType(FieldType.TEXT)
                .isRequired(true)
                .displayOrder(1L)
                .build());

        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form5)
                .question("학기 중 정기 모임과 벽화·공간 꾸미기·포스터 제작 등 봉사 프로젝트에 꾸준히 참여가 가능하신가요?")
                .fieldType(FieldType.RADIO)
                .isRequired(true)
                .displayOrder(2L)
                .options(List.of("예", "아니오"))
                .build());

        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form5)
                .question("아트픽에서 해보고 싶은 예술 재능기부 활동(벽화, 교육, 디자인 제작 등)이나 개인적인 목표를 자유롭게 적어주세요.")
                .fieldType(FieldType.TEXT)
                .isRequired(false)
                .displayOrder(3L)
                .build());

        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form5)
                .question("아트픽 OT 및 인터뷰 가능 시간대를 선택해주세요.")
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


// --- form6 (리버스: 음악·공연 봉사) ---
        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form6)
                .question("주로 활동하고 싶은 파트(보컬, 기타, 베이스, 드럼, 키보드 등)와 그동안의 음악 관련 경험을 적어주세요.")
                .fieldType(FieldType.TEXT)
                .isRequired(true)
                .displayOrder(1L)
                .build());

        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form6)
                .question("학기 중 주 1~2회 합주 연습과 봉사 공연 준비에 꾸준히 참여가 가능하신가요?")
                .fieldType(FieldType.RADIO)
                .isRequired(true)
                .displayOrder(2L)
                .options(List.of("예", "아니오"))
                .build());

        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form6)
                .question("리버스에서 함께 만들고 싶은 봉사 공연 형태(복지시설 공연, 자선 콘서트 등)나 음악으로 전하고 싶은 메시지가 있다면 적어주세요.")
                .fieldType(FieldType.TEXT)
                .isRequired(false)
                .displayOrder(3L)
                .build());

        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form6)
                .question("리버스 오디션 및 면접 가능 시간대를 선택해주세요.")
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


// --- form7 (포커스: 사진·영상 봉사) ---
        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form7)
                .question("사진과 영상 중 더 관심 있는 분야와 사용 중인 장비(카메라, 휴대폰 등), 그리고 경험 수준을 적어주세요.")
                .fieldType(FieldType.TEXT)
                .isRequired(true)
                .displayOrder(1L)
                .build());

        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form7)
                .question("편집 프로그램(프리미어, 다빈치 리졸브, 라이트룸 등)을 사용해본 경험이 있으신가요?")
                .fieldType(FieldType.RADIO)
                .isRequired(true)
                .displayOrder(2L)
                .options(List.of("예", "아니오"))
                .build());

        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form7)
                .question("포커스에서 제작해보고 싶은 공익/봉사 관련 사진·영상 콘텐츠(예: 봉사 현장 기록, 공익 캠페인 영상 등)가 있다면 적어주세요.")
                .fieldType(FieldType.TEXT)
                .isRequired(false)
                .displayOrder(3L)
                .build());

        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form7)
                .question("포커스 OT 및 인터뷰 가능 시간대를 선택해주세요.")
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


// --- form8 (온기나눔: 봉사·나눔) ---
        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form8)
                .question("이전에 참여해본 봉사활동이 있다면 경험과 기관, 활동 내용을 간단히 적어주세요.")
                .fieldType(FieldType.TEXT)
                .isRequired(false)
                .displayOrder(1L)
                .build());

        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form8)
                .question("정기 봉사(월 1~2회)에 꾸준히 참여가 가능하신가요?")
                .fieldType(FieldType.RADIO)
                .isRequired(true)
                .displayOrder(2L)
                .options(List.of("예", "아니오"))
                .build());

        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form8)
                .question("온기나눔 활동을 통해 이루고 싶은 목표나 기대하는 점을 자유롭게 작성해주세요.")
                .fieldType(FieldType.TEXT)
                .isRequired(true)
                .displayOrder(3L)
                .build());

        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form8)
                .question("온기나눔 OT 및 활동 안내를 위한 만남이 가능한 시간대를 선택해주세요.")
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


// --- form9 (비즈온: 창업·비즈니스) ---
        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form9)
                .question("간단한 자기소개와 함께 관심 있는 산업·비즈니스 분야를 적어주세요.")
                .fieldType(FieldType.TEXT)
                .isRequired(true)
                .displayOrder(1L)
                .build());

        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form9)
                .question("창업 경진대회나 공모전, 해커톤 등에 참여해본 경험이 있으신가요?")
                .fieldType(FieldType.RADIO)
                .isRequired(true)
                .displayOrder(2L)
                .options(List.of("예", "아니오"))
                .build());

        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form9)
                .question("비즈온에서 함께 구현해보고 싶은 창업 아이디어나 해결하고 싶은 문제를 구체적으로 적어주세요.")
                .fieldType(FieldType.TEXT)
                .isRequired(false)
                .displayOrder(3L)
                .build());

        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form9)
                .question("비즈온 면접 및 아이디어 피드백 세션 참여 가능 시간대를 선택해주세요.")
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


// --- form10 (핀라이트: 금융·투자) ---
        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form10)
                .question("경제·금융에 관심을 갖게 된 계기와 현재 관심 있는 투자 분야를 적어주세요.")
                .fieldType(FieldType.TEXT)
                .isRequired(true)
                .displayOrder(1L)
                .build());

        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form10)
                .question("실제 투자(실전 또는 모의투자)를 해본 경험이 있으신가요?")
                .fieldType(FieldType.RADIO)
                .isRequired(true)
                .displayOrder(2L)
                .options(List.of("예", "아니오"))
                .build());

        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form10)
                .question("핀라이트에서 배우고 싶은 내용이나 스스로 세운 금융·투자 관련 목표를 구체적으로 적어주세요.")
                .fieldType(FieldType.TEXT)
                .isRequired(false)
                .displayOrder(3L)
                .build());

        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form10)
                .question("핀라이트 OT 및 면담 가능 시간대를 선택해주세요.")
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


// --- form11 (글빛: 문학·창작) ---
        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form11)
                .question("간단한 자기소개와 함께 주로 쓰는(또는 써보고 싶은) 글쓰기 장르(시, 소설, 수필 등)와 경험을 적어주세요.")
                .fieldType(FieldType.TEXT)
                .isRequired(true)
                .displayOrder(1L)
                .build());

        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form11)
                .question("주 1회 정기 모임 및 합평(작품 피드백 모임)에 꾸준히 참여가 가능하신가요?")
                .fieldType(FieldType.RADIO)
                .isRequired(true)
                .displayOrder(2L)
                .options(List.of("예", "아니오"))
                .build());

        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form11)
                .question("글빛에 지원하게 된 동기와 앞으로 글빛에서 쓰고 싶은 글의 주제나 분위기를 자유롭게 적어주세요.")
                .fieldType(FieldType.TEXT)
                .isRequired(false)
                .displayOrder(3L)
                .build());

        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form11)
                .question("글빛 OT 및 간단한 면담이 가능한 시간대를 선택해주세요.")
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


// --- form12 (무대열전: 연극·공연예술·종교) ---
        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form12)
                .question("간단한 자기소개와 함께 연극·공연 경험(또는 관심 분야)과 신앙 배경(있다면)을 적어주세요.")
                .fieldType(FieldType.TEXT)
                .isRequired(true)
                .displayOrder(1L)
                .build());

        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form12)
                .question("정기 연습(주 1회 이상)과 예배·나눔 모임에 꾸준히 참여가 가능하신가요?")
                .fieldType(FieldType.RADIO)
                .isRequired(true)
                .displayOrder(2L)
                .options(List.of("예", "아니오"))
                .build());

        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form12)
                .question("무대열전에서 함께 다뤄보고 싶은 이야기나 작품의 주제(신앙, 삶, 인간관계 등)가 있다면 적어주세요.")
                .fieldType(FieldType.TEXT)
                .isRequired(false)
                .displayOrder(3L)
                .build());

        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form12)
                .question("무대열전 오디션/면접 및 오리엔테이션이 가능한 시간대를 선택해주세요.")
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


// --- form13 (FC JNU: 축구) ---
        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form13)
                .question("본인의 주 포지션(또는 선호 포지션), 축구 경험(학교·동호회·취미 등), 그리고 간단한 자기소개를 적어주세요.")
                .fieldType(FieldType.TEXT)
                .isRequired(true)
                .displayOrder(1L)
                .build());

        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form13)
                .question("주 1~2회 정기 훈련 및 주말 연습 경기/리그전에 꾸준히 참여가 가능하신가요?")
                .fieldType(FieldType.RADIO)
                .isRequired(true)
                .displayOrder(2L)
                .options(List.of("예", "아니오"))
                .build());

        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form13)
                .question("FC JNU에서 이루고 싶은 목표(기량 향상, 교내 리그 우승, 체력 관리 등)와 팀에 기여할 수 있다고 생각하는 점을 적어주세요.")
                .fieldType(FieldType.TEXT)
                .isRequired(false)
                .displayOrder(3L)
                .build());

        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form13)
                .question("FC JNU OT 및 간단한 실기·체력 체크가 가능한 시간대를 선택해주세요.")
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


// --- form14 (엘피스: 기독교 신앙·예배) ---
        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form14)
                .question("간단한 자기소개와 현재 신앙 상태(출석 교회, 신앙 유무 또는 관심 계기 등)를 적어주세요.")
                .fieldType(FieldType.TEXT)
                .isRequired(true)
                .displayOrder(1L)
                .build());

        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form14)
                .question("정기 예배 모임(주 1회)과 소그룹 모임에 꾸준히 참여하실 의향이 있으신가요?")
                .fieldType(FieldType.RADIO)
                .isRequired(true)
                .displayOrder(2L)
                .options(List.of("예", "아니오"))
                .build());

        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form14)
                .question("엘피스에서 기대하는 점(예: 신앙 성장, 공동체 교제, 찬양·봉사 참여 등)과 함께 나누고 싶은 기도제목이나 고민이 있다면 적어주세요.")
                .fieldType(FieldType.TEXT)
                .isRequired(false)
                .displayOrder(3L)
                .build());

        formQuestionRepository.save(FormQuestion.builder()
                .clubApplyForm(form14)
                .question("엘피스 환영 모임 및 간단한 면담이 가능한 시간대를 선택해주세요.")
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

        clubApplyFormRepository.saveAll(forms);
    }
    private List<Application> seedApplications() {

        // user: studentId 기준으로 맵핑
        Map<String, User> userByStudentId = userRepository.findAll().stream()
                .collect(Collectors.toMap(User::getStudentId, u -> u));

        // clubApplyForm: club 이름 기준으로 맵핑
        Map<String, ClubApplyForm> formByClubName = clubApplyFormRepository.findAll().stream()
                .collect(Collectors.toMap(form -> form.getClub().getName(), f -> f));

        List<Application> apps = new ArrayList<>();

        int sid = 202515; // 15 ~ 53까지 사용할 예정

        // 공통 함수 느낌으로 쓰지만, 그냥 로컬 변수로 처리
        // 1~5번 동아리: 각각 6개 (3 status x 2 stage)

        // ===== 1. 인터엑스 =====
        apps.add(buildApp(userByStudentId, formByClubName, sid++, "인터엑스", Status.PENDING,  Stage.INTERVIEW, 0.0));
        apps.add(buildApp(userByStudentId, formByClubName, sid++, "인터엑스", Status.PENDING,  Stage.FINAL,     0.0));
        apps.add(buildApp(userByStudentId, formByClubName, sid++, "인터엑스", Status.APPROVED, Stage.INTERVIEW, 0.0));
        apps.add(buildApp(userByStudentId, formByClubName, sid++, "인터엑스", Status.APPROVED, Stage.FINAL,    0.0));
        apps.add(buildApp(userByStudentId, formByClubName, sid++, "인터엑스", Status.REJECTED, Stage.INTERVIEW, 0.0));
        apps.add(buildApp(userByStudentId, formByClubName, sid++, "인터엑스", Status.REJECTED, Stage.FINAL,    0.0));

        // ===== 2. 에이아이디브 =====
        apps.add(buildApp(userByStudentId, formByClubName, sid++, "에이아이디브", Status.PENDING,  Stage.INTERVIEW,0.0));
        apps.add(buildApp(userByStudentId, formByClubName, sid++, "에이아이디브", Status.PENDING,  Stage.FINAL,    0.0));
        apps.add(buildApp(userByStudentId, formByClubName, sid++, "에이아이디브", Status.APPROVED, Stage.INTERVIEW,0.0));
        apps.add(buildApp(userByStudentId, formByClubName, sid++, "에이아이디브", Status.APPROVED, Stage.FINAL,    0.0));
        apps.add(buildApp(userByStudentId, formByClubName, sid++, "에이아이디브", Status.REJECTED, Stage.INTERVIEW,0.0));
        apps.add(buildApp(userByStudentId, formByClubName, sid++, "에이아이디브", Status.REJECTED, Stage.FINAL,    0.0));

        // ===== 3. 코드마스터 =====
        apps.add(buildApp(userByStudentId, formByClubName, sid++, "코드마스터", Status.PENDING,  Stage.INTERVIEW, 0.0));
        apps.add(buildApp(userByStudentId, formByClubName, sid++, "코드마스터", Status.PENDING,  Stage.FINAL,     0.0));
        apps.add(buildApp(userByStudentId, formByClubName, sid++, "코드마스터", Status.APPROVED, Stage.INTERVIEW, 0.0));
        apps.add(buildApp(userByStudentId, formByClubName, sid++, "코드마스터", Status.APPROVED, Stage.FINAL,    0.0));
        apps.add(buildApp(userByStudentId, formByClubName, sid++, "코드마스터", Status.REJECTED, Stage.INTERVIEW, 0.0));
        apps.add(buildApp(userByStudentId, formByClubName, sid++, "코드마스터", Status.REJECTED, Stage.FINAL,    0.0));

        // ===== 4. 로보테크 (스포츠·스포츠테크) =====
        apps.add(buildApp(userByStudentId, formByClubName, sid++, "로보테크", Status.PENDING,  Stage.INTERVIEW, 0.0));
        apps.add(buildApp(userByStudentId, formByClubName, sid++, "로보테크", Status.PENDING,  Stage.FINAL,     0.0));
        apps.add(buildApp(userByStudentId, formByClubName, sid++, "로보테크", Status.APPROVED, Stage.INTERVIEW, 0.0));
        apps.add(buildApp(userByStudentId, formByClubName, sid++, "로보테크", Status.APPROVED, Stage.FINAL,    0.0));
        apps.add(buildApp(userByStudentId, formByClubName, sid++, "로보테크", Status.REJECTED, Stage.INTERVIEW, 0.0));
        apps.add(buildApp(userByStudentId, formByClubName, sid++, "로보테크", Status.REJECTED, Stage.FINAL,    0.0));

        // ===== 5. 아트픽 (예술 재능기부 봉사) =====
        apps.add(buildApp(userByStudentId, formByClubName, sid++, "아트픽", Status.PENDING,  Stage.INTERVIEW, 0.0));
        apps.add(buildApp(userByStudentId, formByClubName, sid++, "아트픽", Status.PENDING,  Stage.FINAL,     0.0));
        apps.add(buildApp(userByStudentId, formByClubName, sid++, "아트픽", Status.APPROVED, Stage.INTERVIEW, 0.0));
        apps.add(buildApp(userByStudentId, formByClubName, sid++, "아트픽", Status.APPROVED, Stage.FINAL,    0.0));
        apps.add(buildApp(userByStudentId, formByClubName, sid++, "아트픽", Status.REJECTED, Stage.INTERVIEW, 0.0));
        apps.add(buildApp(userByStudentId, formByClubName, sid++, "아트픽", Status.REJECTED, Stage.FINAL,    0.0));

        // ===== 나머지 9개 동아리: 각 1개 (PENDING + INTERVIEW) =====

        apps.add(buildApp(userByStudentId, formByClubName, sid++, "리버스",   Status.PENDING, Stage.INTERVIEW, 0.0));
        apps.add(buildApp(userByStudentId, formByClubName, sid++, "포커스",   Status.PENDING, Stage.INTERVIEW, 0.0));
        apps.add(buildApp(userByStudentId, formByClubName, sid++, "온기나눔", Status.PENDING, Stage.INTERVIEW, 0.0));
        apps.add(buildApp(userByStudentId, formByClubName, sid++, "비즈온",   Status.PENDING, Stage.INTERVIEW, 0.0));
        apps.add(buildApp(userByStudentId, formByClubName, sid++, "핀라이트", Status.PENDING, Stage.INTERVIEW, 0.0));
        apps.add(buildApp(userByStudentId, formByClubName, sid++, "글빛",     Status.PENDING, Stage.INTERVIEW, 0.0));
        apps.add(buildApp(userByStudentId, formByClubName, sid++, "무대열전", Status.PENDING, Stage.INTERVIEW, 0.0));
        apps.add(buildApp(userByStudentId, formByClubName, sid++, "FC JNU",   Status.PENDING, Stage.INTERVIEW, 0.0));
        apps.add(buildApp(userByStudentId, formByClubName, sid, "엘피스",   Status.PENDING, Stage.INTERVIEW, 0.0));

        return applicationRepository.saveAll(apps);
    }

    /**
     * 편하게 쓰려고 만든 헬퍼 메서드
     */
    private Application buildApp(Map<String, User> userByStudentId,
                                 Map<String, ClubApplyForm> formByClubName,
                                 int studentId,
                                 String clubName,
                                 Status status,
                                 Stage stage,
                                 double avgRating) {

        Application app = Application.builder()
                .user(userByStudentId.get(String.valueOf(studentId)))
                .clubApplyForm(formByClubName.get(clubName))
                .status(status)
                .stage(stage)
                .build();
        app.updateAverageRating(avgRating);
        return app;
    }


    private void seedAnswers() {

        // Application 1 ~ 39 미리 로드 (1-based)
        Application[] apps = new Application[40];
        for (long i = 1; i <= 39; i++) {
            long finalI = i;
            apps[(int) i] = applicationRepository.findById(i)
                    .orElseThrow(() -> new IllegalStateException("Application not found: " + finalI));
        }

        // FormQuestion 1 ~ 56 미리 로드 (1-based)
        FormQuestion[] questions = new FormQuestion[57];
        for (long i = 1; i <= 56; i++) {
            long finalI = i;
            questions[(int) i] = formQuestionRepository.findById(i)
                    .orElseThrow(() -> new IllegalStateException("FormQuestion not found: " + finalI));
        }

        List<Answer> answers = new ArrayList<>();

        // ========== 1. 인터엑스 (form1, questions 1~4) ==========
        for (int i = 1; i <= 6; i++) {
            Application app = apps[i];

            answers.add(Answer.builder()
                    .application(app)
                    .formQuestion(questions[1])
                    .answer("안녕하세요, 사회 문제에 관심이 많은 학부생입니다. 특히 청년 세대의 주거 문제와 불평등, 젠더 이슈에 관심이 있어 관련 기사와 책을 꾸준히 찾아보고 있습니다.")
                    .build());

            answers.add(Answer.builder()
                    .application(app)
                    .formQuestion(questions[2])
                    .answer("예")
                    .build());

            answers.add(Answer.builder()
                    .application(app)
                    .formQuestion(questions[3])
                    .answer("혼자 고민하던 사회 문제를 구조적으로 이해하고, 비슷한 문제의식을 가진 사람들과 함께 토론하며 작은 행동으로 옮기고 싶어서 인터엑스에 지원했습니다.")
                    .build());

            answers.add(Answer.builder()
                    .application(app)
                    .formQuestion(questions[4])
                    .answer("2025-10-15 10:00~12:00, 2025-10-16 10:00~12:00 모두 면접 참여 가능합니다.")
                    .build());
        }

        // ========== 2. 에이아이디브 (form2, questions 5~8) ==========
        for (int i = 7; i <= 12; i++) {
            Application app = apps[i];

            answers.add(Answer.builder()
                    .application(app)
                    .formQuestion(questions[5])
                    .answer("안녕하세요, 컴퓨터 관련 전공의 학부생입니다. 파이썬 기초와 선형대수·확률통계를 수강했고, 간단한 ML 모델을 구현해 본 경험이 있습니다.")
                    .build());

            answers.add(Answer.builder()
                    .application(app)
                    .formQuestion(questions[6])
                    .answer("예")
                    .build());

            answers.add(Answer.builder()
                    .application(app)
                    .formQuestion(questions[7])
                    .answer("AIDive에서 자연어 처리와 추천 시스템을 중심으로 공부해 보고 싶습니다. 논문을 함께 읽고, 캐글/데이콘 대회에 팀으로 참가해 끝까지 모델을 완성해 보는 것이 목표입니다.")
                    .build());

            answers.add(Answer.builder()
                    .application(app)
                    .formQuestion(questions[8])
                    .answer("2025-10-15 10:00~12:00, 2025-10-16 10:00~12:00 중 어느 시간대든 참여 가능합니다.")
                    .build());
        }

        // ========== 3. 코드마스터 (form3, questions 9~12) ==========
        for (int i = 13; i <= 18; i++) {
            Application app = apps[i];

            answers.add(Answer.builder()
                    .application(app)
                    .formQuestion(questions[9])
                    .answer("자바와 파이썬, 자바스크립트를 기초 수준으로 사용할 수 있고, 백준 실버 난이도 문제를 꾸준히 풀면서 알고리즘을 연습하고 있습니다.")
                    .build());

            answers.add(Answer.builder()
                    .application(app)
                    .formQuestion(questions[10])
                    .answer("예")
                    .build());

            answers.add(Answer.builder()
                    .application(app)
                    .formQuestion(questions[11])
                    .answer("코드마스터에서 풀스택 웹 서비스를 팀과 함께 기획하고 개발해 배포까지 경험해 보고 싶습니다. 코드 리뷰를 통해 좋은 개발 습관도 만들고 싶습니다.")
                    .build());

            answers.add(Answer.builder()
                    .application(app)
                    .formQuestion(questions[12])
                    .answer("2025-10-15 10:00~12:00, 2025-10-16 10:00~12:00 모두 OT 및 면접 참석 가능합니다.")
                    .build());
        }

        // ========== 4. 로보테크 (스포츠·스포츠테크, form4, questions 13~16) ==========
        for (int i = 19; i <= 24; i++) {
            Application app = apps[i];

            answers.add(Answer.builder()
                    .application(app)
                    .formQuestion(questions[13])
                    .answer("축구와 농구를 좋아하는 학생입니다. 축구에서는 주로 수비형 미드필더 포지션을 맡았고, 팀워크와 커버 플레이를 중요하게 생각합니다.")
                    .build());

            answers.add(Answer.builder()
                    .application(app)
                    .formQuestion(questions[14])
                    .answer("예")
                    .build());

            answers.add(Answer.builder()
                    .application(app)
                    .formQuestion(questions[15])
                    .answer("경기 기록을 앱이나 웨어러블 기기로 측정해 체력과 움직임을 데이터로 분석해 보고 싶습니다. 단순히 운동하는 것에서 나아가 스포츠테크를 함께 경험해 보고 싶습니다.")
                    .build());

            answers.add(Answer.builder()
                    .application(app)
                    .formQuestion(questions[16])
                    .answer("2025-10-15 10:00~12:00, 2025-10-16 10:00~12:00 중 어느 시간대든 OT 및 테스트 참여 가능합니다.")
                    .build());
        }

        // ========== 5. 아트픽 (예술 재능기부 봉사, form5, questions 17~20) ==========
        for (int i = 25; i <= 30; i++) {
            Application app = apps[i];

            answers.add(Answer.builder()
                    .application(app)
                    .formQuestion(questions[17])
                    .answer("디지털 일러스트와 그래픽 디자인에 관심이 많습니다. 아이패드와 포토샵으로 인물 드로잉과 간단한 포스터 작업을 자주 하고 있습니다.")
                    .build());

            answers.add(Answer.builder()
                    .application(app)
                    .formQuestion(questions[18])
                    .answer("예")
                    .build());

            answers.add(Answer.builder()
                    .application(app)
                    .formQuestion(questions[19])
                    .answer("벽화 봉사나 포스터·배너 제작 봉사에 참여해 제가 가진 예술적인 감각을 지역사회와 나눠 보고 싶습니다. 특히 공간을 밝게 만드는 작업에 참여해 보고 싶습니다.")
                    .build());

            answers.add(Answer.builder()
                    .application(app)
                    .formQuestion(questions[20])
                    .answer("2025-10-15 10:00~12:00, 2025-10-16 10:00~12:00 모두 인터뷰 참석 가능합니다.")
                    .build());
        }

        // ======== 6. 리버스 (음악 봉사, form6, questions 21~24) – app31 ========
        {
            Application app = apps[31];

            answers.add(Answer.builder()
                    .application(app)
                    .formQuestion(questions[21])
                    .answer("보컬 파트로 활동하고 싶습니다. 학교 밴드부에서 발라드와 팝송을 중심으로 무대 경험이 있으며, 사람들 앞에서 노래하는 것을 좋아합니다.")
                    .build());

            answers.add(Answer.builder()
                    .application(app)
                    .formQuestion(questions[22])
                    .answer("예")
                    .build());

            answers.add(Answer.builder()
                    .application(app)
                    .formQuestion(questions[23])
                    .answer("요양원이나 아동 시설에서 작은 공연을 열어 음악으로 위로를 전해 보고 싶습니다. 함께 준비한 자선 공연도 꼭 한 번 해보고 싶습니다.")
                    .build());

            answers.add(Answer.builder()
                    .application(app)
                    .formQuestion(questions[24])
                    .answer("2025-10-15 10:00~12:00, 2025-10-16 10:00~12:00 모두 오디션 참여 가능합니다.")
                    .build());
        }

        // ======== 7. 포커스 (사진·영상 봉사, form7, questions 25~28) – app32 ========
        {
            Application app = apps[32];

            answers.add(Answer.builder()
                    .application(app)
                    .formQuestion(questions[25])
                    .answer("사진과 영상 모두 좋아하지만, 특히 짧은 영상 만들기에 관심이 많습니다. 미러리스 카메라와 휴대폰으로 브이로그와 행사 기록 영상을 찍어봤습니다.")
                    .build());

            answers.add(Answer.builder()
                    .application(app)
                    .formQuestion(questions[26])
                    .answer("예")
                    .build());

            answers.add(Answer.builder()
                    .application(app)
                    .formQuestion(questions[27])
                    .answer("봉사 현장을 기록하는 영상과 사진을 만들어, 활동의 의미를 더 많은 사람들과 공유할 수 있는 콘텐츠를 제작해 보고 싶습니다.")
                    .build());

            answers.add(Answer.builder()
                    .application(app)
                    .formQuestion(questions[28])
                    .answer("2025-10-15 10:00~12:00, 2025-10-16 10:00~11:30 사이 OT 및 인터뷰 참석 가능합니다.")
                    .build());
        }

        // ======== 8. 온기나눔 (봉사·나눔, form8, questions 29~32) – app33 ========
        {
            Application app = apps[33];

            answers.add(Answer.builder()
                    .application(app)
                    .formQuestion(questions[29])
                    .answer("고등학교 시절 지역 아동센터에서 학습 지도 봉사와 놀이지도 봉사를 해본 경험이 있습니다. 짧은 기간이었지만 아이들과 관계를 쌓는 과정이 인상 깊었습니다.")
                    .build());

            answers.add(Answer.builder()
                    .application(app)
                    .formQuestion(questions[30])
                    .answer("예")
                    .build());

            answers.add(Answer.builder()
                    .application(app)
                    .formQuestion(questions[31])
                    .answer("한 기관과 꾸준히 만나는 봉사를 통해, 아이들이 성장하는 과정을 함께 보는 경험을 해보고 싶습니다. 단순 참여자가 아니라, 프로그램 기획에도 함께 참여해 보고 싶습니다.")
                    .build());

            answers.add(Answer.builder()
                    .application(app)
                    .formQuestion(questions[32])
                    .answer("2025-10-15 10:00~12:00, 2025-10-16 10:00~12:00 모두 만남이 가능합니다.")
                    .build());
        }

        // ======== 9. 비즈온 (창업·비즈니스, form9, questions 33~36) – app34 ========
        {
            Application app = apps[34];

            answers.add(Answer.builder()
                    .application(app)
                    .formQuestion(questions[33])
                    .answer("경영학과 재학생으로, IT 서비스와 교육 분야 비즈니스에 관심이 많습니다. 사용자의 불편을 해결하는 서비스를 만드는 데 흥미를 느끼고 있습니다.")
                    .build());

            answers.add(Answer.builder()
                    .application(app)
                    .formQuestion(questions[34])
                    .answer("예")
                    .build());

            answers.add(Answer.builder()
                    .application(app)
                    .formQuestion(questions[35])
                    .answer("학생들의 공부 습관을 돕는 서비스 아이디어를 팀과 함께 구체화해보고 싶습니다. 고객 인터뷰부터 간단한 MVP 제작, 피칭까지 경험해보는 것이 목표입니다.")
                    .build());

            answers.add(Answer.builder()
                    .application(app)
                    .formQuestion(questions[36])
                    .answer("2025-10-15 10:00~12:00 사이 면접 참여가 가능하며, 필요 시 같은 주 평일 저녁도 조정할 수 있습니다.")
                    .build());
        }

        // ======== 10. 핀라이트 (금융·투자, form10, questions 37~40) – app35 ========
        {
            Application app = apps[35];

            answers.add(Answer.builder()
                    .application(app)
                    .formQuestion(questions[37])
                    .answer("경제학과 재학 중이며, 2학년 때부터 소액으로 국내 주식과 ETF에 장기 투자 중입니다. 거시경제 기사와 리포트를 읽는 것을 좋아합니다.")
                    .build());

            answers.add(Answer.builder()
                    .application(app)
                    .formQuestion(questions[38])
                    .answer("예")
                    .build());

            answers.add(Answer.builder()
                    .application(app)
                    .formQuestion(questions[39])
                    .answer("기업 분석과 산업 분석을 체계적으로 배우고 싶습니다. 팀으로 모의 포트폴리오를 구성해 장기 수익률을 비교해 보는 활동을 특히 해보고 싶습니다.")
                    .build());

            answers.add(Answer.builder()
                    .application(app)
                    .formQuestion(questions[40])
                    .answer("2025-10-15 10:00~12:00, 2025-10-16 10:00~12:00 모두 OT 및 면담 참여 가능합니다.")
                    .build());
        }

        // ======== 11. 글빛 (문학·창작, form11, questions 41~44) – app36 ========
        {
            Application app = apps[36];

            answers.add(Answer.builder()
                    .application(app)
                    .formQuestion(questions[41])
                    .answer("시와 짧은 산문을 쓰는 것을 좋아하는 학생입니다. 일상에서 느끼는 감정을 짧은 문장으로 기록하는 습관이 있습니다.")
                    .build());

            answers.add(Answer.builder()
                    .application(app)
                    .formQuestion(questions[42])
                    .answer("예")
                    .build());

            answers.add(Answer.builder()
                    .application(app)
                    .formQuestion(questions[43])
                    .answer("글빛에서 정기 합평을 통해 다른 사람의 시선을 배우고, 제 글도 조금 더 단단하게 다듬고 싶습니다. 동인지 제작에도 꼭 참여해 보고 싶습니다.")
                    .build());

            answers.add(Answer.builder()
                    .application(app)
                    .formQuestion(questions[44])
                    .answer("2025-10-15 10:00~12:00, 2025-10-16 10:00~12:00 모두 OT 및 면담이 가능합니다.")
                    .build());
        }

        // ======== 12. 무대열전 (연극·공연예술·종교, form12, questions 45~48) – app37 ========
        {
            Application app = apps[37];

            answers.add(Answer.builder()
                    .application(app)
                    .formQuestion(questions[45])
                    .answer("연극과 찬양을 좋아하는 기독교 신앙인입니다. 교회에서 성극과 찬양팀 활동을 한 경험이 있습니다.")
                    .build());

            answers.add(Answer.builder()
                    .application(app)
                    .formQuestion(questions[46])
                    .answer("예")
                    .build());

            answers.add(Answer.builder()
                    .application(app)
                    .formQuestion(questions[47])
                    .answer("무대열전에서 신앙과 삶의 이야기를 담은 연극을 함께 만들어보고 싶습니다. 연기뿐 아니라, 무대 연출과 기획에도 관심이 있습니다.")
                    .build());

            answers.add(Answer.builder()
                    .application(app)
                    .formQuestion(questions[48])
                    .answer("2025-10-15 10:00~12:00, 2025-10-16 10:00~12:00 중에서 면접 및 오디션 참여 가능합니다.")
                    .build());
        }

        // ======== 13. FC JNU (축구, form13, questions 49~52) – app38 ========
        {
            Application app = apps[38];

            answers.add(Answer.builder()
                    .application(app)
                    .formQuestion(questions[49])
                    .answer("축구를 5년 이상 해온 학생입니다. 주 포지션은 중앙 미드필더이며, 패스와 연계 플레이에 강점이 있습니다.")
                    .build());

            answers.add(Answer.builder()
                    .application(app)
                    .formQuestion(questions[50])
                    .answer("예")
                    .build());

            answers.add(Answer.builder()
                    .application(app)
                    .formQuestion(questions[51])
                    .answer("FC JNU에서 꾸준히 훈련하며 체력을 기르고, 교내 리그전에서 팀 우승에 기여해 보고 싶습니다. 팀 분위기를 좋게 만드는 역할도 함께 하고 싶습니다.")
                    .build());

            answers.add(Answer.builder()
                    .application(app)
                    .formQuestion(questions[52])
                    .answer("2025-10-15 10:00~12:00, 2025-10-16 10:00~12:00 모두 OT 및 실기 참여가 가능합니다.")
                    .build());
        }

        // ======== 14. 엘피스 (기독교 신앙·예배, form14, questions 53~56) – app39 ========
        {
            Application app = apps[39];

            answers.add(Answer.builder()
                    .application(app)
                    .formQuestion(questions[53])
                    .answer("모태신앙으로 자라 현재도 주일마다 교회에 출석하고 있는 학생입니다. 대학 생활 속에서 신앙을 함께 나눌 공동체를 찾고 있습니다.")
                    .build());

            answers.add(Answer.builder()
                    .application(app)
                    .formQuestion(questions[54])
                    .answer("예")
                    .build());

            answers.add(Answer.builder()
                    .application(app)
                    .formQuestion(questions[55])
                    .answer("엘피스에서 말씀과 찬양을 함께 나누며 신앙을 다시 정비하고 싶습니다. 찬양팀이나 예배 스태프로 섬길 수 있다면 기꺼이 돕고 싶습니다.")
                    .build());

            answers.add(Answer.builder()
                    .application(app)
                    .formQuestion(questions[56])
                    .answer("2025-10-15 10:00~12:00, 2025-10-16 10:00~12:00 모두 환영 모임 및 면담 참여 가능합니다.")
                    .build());
        }

        // 실제 insert
        answerRepository.saveAll(answers);
    }


    private void seedClubMembers(List<User> users, List<Club> clubs, List<Application> applications) {
        // if (clubMemberRepository.count() > 0) return;

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

        // ===== 1~14번 동아리 회장(CLUB_ADMIN) =====
        for (int i = 1; i <= 14; i++) {
            members.add(ClubMember.builder()
                    .user(userArr[i])             // user 1~14
                    .club(clubArr[i])             // club 1~14
                    .application(null)
                    .activeStatus(ActiveStatus.ACTIVE)
                    .role(Role.CLUB_ADMIN)
                    .build());
        }

        // ===== 15~53번 지원자(APPLICANT) =====
        for (int i = 15; i <= 53; i++) {
            members.add(ClubMember.builder()
                    .user(userArr[i])
                    .club(apps[i-14].getClubApplyForm().getClub()) // application으로부터 클럽 자동 매칭
                    .application(apps[i-14])
                    .activeStatus(ActiveStatus.ACTIVE)
                    .role(Role.APPLICANT)
                    .build());
        }

        // ===== 54번: 시스템 관리자 (SYSTEM_ADMIN) =====
        members.add(ClubMember.builder()
                .user(userArr[54])
                .club(clubArr[1]) // 1번 동아리 (예: 인터엑스)
                .application(null)
                .activeStatus(ActiveStatus.ACTIVE)
                .role(Role.SYSTEM_ADMIN)
                .build());

        clubMemberRepository.saveAll(members);
    }

    private List<Notice> seedNotices() {
        List<Notice> notices = new ArrayList<>();

        // ===== 1. 시스템 오픈 안내 =====
        notices.add(Notice.builder()
                .title("[중요] 동아리 관리 시스템(Dongarium) 오픈 안내")
                .content("""
                    안녕하세요, 총동아리연합회입니다.

                    2026학년도 1학기부터 중앙동아리의 모든 행정 절차(등록, 모집, 보고 등)는
                    'Dongarium'을 통해 진행됩니다.

                    ■ 주요 기능
                    - 동아리 정보 및 운영진 등록
                    - 신입 부원 모집 공고 및 지원서 관리
                    - 학기별 활동 보고 및 계획서 제출
                    - 총동연 공지사항 확인

                    ■ 필수 안내
                    - 각 동아리 회장 및 운영진은 3월 15일까지 등록을 완료해 주세요.
                    - 미등록 시 중앙 지원 대상에서 제외될 수 있습니다.

                    """)
                .isAlive(true)
                .build());

        // ===== 2. 시스템 점검 안내 =====
        notices.add(Notice.builder()
                .title("[시스템] Dongarium 정기 점검 안내 (3월 9일 00:00~02:00)")
                .content("""
                    동아리 관리 시스템 안정적 운영을 위한 정기 점검이 예정되어 있습니다.

                    ■ 점검 일시
                    - 2026년 3월 9일(월) 00:00 ~ 02:00

                    ■ 영향
                    - 점검 시간 동안 서비스 접속이 일시적으로 제한됩니다.
                    - 서류 제출 및 모집 공고 등록은 점검 전 완료해 주세요.

                    불편을 드려 죄송하며, 안정적 운영을 위해 최선을 다하겠습니다.
                    """)
                .isAlive(true)
                .build());

        // ===== 3. 중앙동아리 등록 안내 =====
        notices.add(Notice.builder()
                .title("[총동연] 2026-1학기 중앙동아리 등록 서류 안내")
                .content("""
                    2026학년도 1학기 중앙동아리 등록을 위한 서류 제출을 안내드립니다.

                    ■ 제출 대상
                    - 중앙동아리로 활동을 희망하는 모든 단체

                    ■ 제출 기간
                    - 2026년 2월 28일(토) ~ 3월 14일(토) 23:59

                    ■ 제출 서류
                      1) 전 학기 활동 보고서
                      2) 이번 학기 활동 계획서
                      3) 회장 및 운영진 명단
                      4) 회원 명부

                    제출 방법 및 경로는 서류 내에 안내되어 있습니다.
                    """)
                .isAlive(true)
                .build());

        // ===== 4. 안전교육 안내 =====
        notices.add(Notice.builder()
                .title("[총동연] 동아리 안전교육 이수 안내")
                .content("""
                    모든 중앙동아리 회장 및 운영진은 학기 초 안전교육을 반드시 이수해야 합니다.

                    ■ 일정
                    - 1차: 3월 6일(금) 18:00, 제 2 학생회관 대강당
                    - 2차: 3월 10일(화) 18:00, 제 1 학생회관 1층 세미나실

                    이수방법은 첨부파일 참고 바랍니다. 
                    미이수 시 일부 지원이 제한될 수 있습니다.
                    """)
                .isAlive(true)
                .build());

        // ===== 5. 회계 보고 양식 업로드 =====
        notices.add(Notice.builder()
                .title("[총동연] 2026-1학기 회계 보고 양식 업로드 안내")
                .content("""
                    학기 말 회계 보고를 위한 최신 양식이 업로드되었습니다.

                    ■ 제출 기한
                    - 2026년 6월 29일(월) 23:59까지

                    제출 경로와 작성 방법은 파일 내에 상세히 안내되어 있습니다.
                    """)
                .isAlive(true)
                .build());

        // ===== 6. 알림아리 부스 신청 =====
        notices.add(Notice.builder()
                .title("[알림아리] 2026학년도 중앙동아리 홍보 부스 신청 안내")
                .content("""
                    신입생 대상 중앙동아리 통합 홍보 행사 '알림아리' 부스 신청을 받습니다.

                    ■ 행사 일시
                    - 2026년 3월 18일(수) 10:00~17:00
                    - 장소: 중앙 잔디광장 일대

                    ■ 신청 기간
                    - 3월 4일(수) ~ 3월 9일(월)

                    제출 경로와 작성 방법은 첨부파일 내에 상세히 안내되어 있습니다.
                    """)
                .isAlive(true)
                .build());

        // ===== 7. 무대 참가 팀 모집 =====
        notices.add(Notice.builder()
                .title("[알림아리] 무대 공연 참여 동아리 모집")
                .content("""
                    알림아리 중앙무대 공연에 참여할 동아리를 모집합니다.

                    ■ 모집 대상
                    - 밴드, 댄스, 연극, 퍼포먼스 등 공연 가능 동아리

                    ■ 신청 기한
                    - 2026년 3월 8일(일) 23:59까지

                    신청 방법은 공지 파일 내 안내를 참고해 주세요.
                    """)
                .isAlive(true)
                .build());

        // ===== 8. 알림아리 행사 일정 및 운영 안내 =====
        notices.add(Notice.builder()
                .title("[알림아리] 2026학년도 알림아리 행사 일정 및 운영 안내")
                .content("""
            2026학년도 1학기 중앙동아리 통합 홍보 행사 ‘알림아리’의 전체 일정을 안내드립니다.

            ■ 행사 일시
            - 2026년 3월 19일(목) 10:00 ~ 17:00
            - 장소: 전남대학교 중앙 잔디광장 일대

            ■ 주요 일정
            - 09:00 ~ 10:00 부스 설치 및 준비
            - 10:00 ~ 17:00 부스 운영 및 홍보
            - 13:00 ~ 16:00 중앙무대 공연
            - 17:00 ~ 18:00 정리 및 철수

            ■ 운영 안내
            - 부스 위치는 총동연 사전 배정표에 따라 설치 바랍니다.
            - 전력 사용, 확성기 등 음향기기 사용 시 안전요원 안내에 따라야 합니다.
            - 쓰레기 및 장비는 행사 종료 후 반드시 원상 복구해야 합니다.

            자세한 세부 운영 지침은 첨부파일 내에 안내되어 있습니다.
            """)
                .isAlive(true)
                .build());

        // ===== 9. 신입 부원 모집 공고 안내 =====
        notices.add(Notice.builder()
                .title("[시스템] 2026-1학기 신입 부원 모집 공고 등록 안내")
                .content("""
                    각 동아리는 Dongarium 상단의 [모집폼 관리] 메뉴에서
                    모집 폼과 일정을 등록할 수 있습니다.

                    ■ 등록 절차
                    1) [모집폼 관리]에서 모집 내용 및 일정 등록
                    2) [지원자 관리]에서 실시간 지원 현황 확인

                    등록 방법은 시스템 내 안내문을 참고해 주세요.
                    """)
                .isAlive(true)
                .build());

        // ===== 10. 면접 안내 =====
        notices.add(Notice.builder()
                .title("[시스템] 신입 부원 면접 진행 안내")
                .content("""
                    각 동아리의 신입 부원 면접 진행 시 유의사항을 안내드립니다.

                    ■ 유의 사항
                    - 면접 일정은 동아리 내부에서 자율적으로 운영 가능합니다.
                    - 지원자에게는 일정 변경 및 안내를 개별적으로 전달해 주세요.
                    - 면접 결과는 Dongarium을 통해 합격자 상태 변경으로 반영합니다.
                    """)
                .isAlive(true)
                .build());

        // ===== 11. 합격자 발표 안내 =====
        notices.add(Notice.builder()
                .title("[시스템] 신입 부원 합격자 발표 방법 안내")
                .content("""
                    Dongarium을 통해 합격자 발표를 진행할 수 있습니다.

                    ■ 방법
                    - [지원서 관리] → 지원자 상태 변경 (‘합격/불합격/대기’)
                    - 알림 이메일 자동 발송 가능

                    개인정보 보호를 위해 공개 게시판 공지는 금지됩니다.
                    """)
                .isAlive(true)
                .build());

        // ===== 12. 동아리방 점검 안내 =====
        notices.add(Notice.builder()
                .title("[점검] 동아리방 점검 및 정리 기간 안내")
                .content("""
                    ■ 점검 기간
                    - 2026년 4월 1일(수) ~ 4월 7일(화)

                    ■ 주요 내용
                    - 전기, 소방, 비상 대피로 점검
                    - 물품 정리 및 청소
                    - 출입 비밀번호 갱신

                    각 동아리 대표자는 점검 기간 내 방문 바랍니다.
                    """)
                .isAlive(true)
                .build());

        // ===== 13. 활동 사진 공모전 =====
        notices.add(Notice.builder()
                .title("[총동연] 동아리 활동 사진 공모전 개최 안내")
                .content("""
                    중앙동아리의 활발한 활동을 기록하기 위한 사진 공모전을 개최합니다.

                    ■ 접수 기간: 2026년 4월 10일 ~ 4월 30일
                    ■ 주제: 우리 동아리의 열정과 순간

                    제출 방법과 양식은 공지 파일 내에 안내되어 있습니다.
                    """)
                .isAlive(true)
                .build());

        // ===== 14. 동아리 교류전 =====
        notices.add(Notice.builder()
                .title("[총동연] 2026년 상반기 동아리 교류전 안내")
                .content("""
                    동아리 간 친목과 교류를 위한 ‘동아리 교류전’을 개최합니다.

                    ■ 일정: 2026년 5월 16일(토)
                    ■ 장소: 전남대학교 체육관
                    ■ 참가 대상: 모든 중앙동아리

                    자세한 일정과 신청 절차는 추후 공지될 예정입니다.
                    """)
                .isAlive(true)
                .build());

        // ===== 15. 시스템 개선 공지 =====
        notices.add(Notice.builder()
                .title("[시스템] Dongarium 기능 개선 안내")
                .content("""
                    Dongarium 시스템 안정화 및 편의 기능이 개선되었습니다.

                    ■ 개선 내용
                    - 모집폼 등록 오류 수정
                    - 지원자 조회 속도 개선
                    - 이메일 전송 오류 수정

                    앞으로도 사용자 피드백을 반영해 지속적으로 개선하겠습니다.
                    """)
                .isAlive(true)
                .build());

        // ===== 16. 하계 워크숍 안내 =====
        notices.add(Notice.builder()
                .title("[총동연] 하계 연합 워크숍 사전 안내")
                .content("""
                    각 동아리 대표 및 임원을 대상으로 하는 하계 연합 워크숍이 예정되어 있습니다.

                    ■ 일시: 2026년 7월 4일(토) ~ 7월 5일(일)
                    ■ 장소: 전남대학교 여수캠퍼스 연수원
                    ■ 내용: 리더십 강연, 운영 사례 공유, 네트워킹 세션

                    세부 일정은 별도 공지로 안내될 예정입니다.
                    """)
                .isAlive(true)
                .build());

        // ===== 17. 시스템 문의 안내 =====
        notices.add(Notice.builder()
                .title("[시스템] 시스템 문의 및 오류 제보 안내")
                .content("""
                    Dongarium 사용 중 오류나 문의사항이 있을 경우 아래 이메일로 문의해 주세요.

                    ■ 문의 메일
                    - jnupole004@gmail.com

                    ■ 문의 시 포함사항
                    - 이름 / 소속 동아리 / 문제 상황 설명

                    확인 후 신속히 답변드리겠습니다.
                    """)
                .isAlive(true)
                .build());

        // ===== 18. 중간 점검 보고 안내 =====
        notices.add(Notice.builder()
                .title("[총동연] 2026-1학기 활동 중간 점검 보고 안내")
                .content("""
                    중앙동아리의 활동 현황 점검을 위한 중간 보고 일정을 안내드립니다.

                    ■ 제출 기간: 2026년 5월 1일 ~ 5월 10일
                    ■ 제출 항목: 활동 진행 현황, 사진, 회의록 등

                    제출 경로는 보고서 내에 안내되어 있습니다.
                    """)
                .isAlive(true)
                .build());

        // ===== 19. 사무실 이전 안내 =====
        notices.add(Notice.builder()
                .title("[총동연] 총동연 사무실 이전 안내")
                .content("""
                    총동아리연합회 사무실이 이전되었습니다.

                    ■ 위치
                    - (구)제 1 학생회관 2층 201호 → (신)제 2 학생회관 3층 307호

                    ■ 운영 시간
                    - 평일 10:00 ~ 17:00 (점심 12:00~13:00)

                    방문 시 새로운 위치를 참고해 주세요.
                    """)
                .isAlive(true)
                .build());

        // ===== 20. 단체사진 촬영 안내 =====
        notices.add(Notice.builder()
                .title("[행사] 2026-1학기 동아리 단체사진 촬영 안내")
                .content("""
                    중앙동아리 단체사진 촬영 일정을 안내드립니다.

                    ■ 일시: 2026년 4월 17일(금) 10:00~17:00
                    ■ 장소: 제 1학생회관 앞 봉지
                    ■ 대상: 등록 완료 중앙동아리 전원

                    세부 일정은 행사 당일 현장에서 조정될 예정입니다.
                    """)
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
                .notice(notices.get(2)) // notice_id 3
                .name("2026-1학기_중앙동아리_등록_서류_안내문.docx ")
                .type("docx")
                .objectUri("https://dongarium-attachments.s3.ap-northeast-2.amazonaws.com/attachments/2026-1%ED%95%99%EA%B8%B0_%EC%A4%91%EC%95%99%EB%8F%99%EC%95%84%EB%A6%AC_%EB%93%B1%EB%A1%9D_%EC%84%9C%EB%A5%98_%EC%95%88%EB%82%B4%EB%AC%B8.docx")
                .build());

        files.add(File.builder()
                .notice(notices.get(3)) // notice_id 4
                .name("동아리_안전교육_이수_안내문.docx")
                .type("docx")
                .objectUri("https://dongarium-attachments.s3.ap-northeast-2.amazonaws.com/attachments/%EB%8F%99%EC%95%84%EB%A6%AC_%EC%95%88%EC%A0%84%EA%B5%90%EC%9C%A1_%EC%9D%B4%EC%88%98_%EC%95%88%EB%82%B4%EB%AC%B8.docx")
                .build());

        files.add(File.builder()
                .notice(notices.get(4)) // notice_id 5
                .name("2026-1학기_회계_보고_양식_안내문.hwp")
                .type("hwp")
                .objectUri("https://dongarium-attachments.s3.ap-northeast-2.amazonaws.com/attachments/2026-1%ED%95%99%EA%B8%B0_%ED%9A%8C%EA%B3%84_%EB%B3%B4%EA%B3%A0_%EC%96%91%EC%8B%9D_%EC%95%88%EB%82%B4%EB%AC%B8.hwp")
                .build());

        files.add(File.builder()
                .notice(notices.get(4)) // notice_id 5
                .name("2026-1학기_회계_보고_양식_안내문.docx")
                .type("docx")
                .objectUri("https://dongarium-attachments.s3.ap-northeast-2.amazonaws.com/attachments/2026-1%ED%95%99%EA%B8%B0_%ED%9A%8C%EA%B3%84_%EB%B3%B4%EA%B3%A0_%EC%96%91%EC%8B%9D_%EC%95%88%EB%82%B4%EB%AC%B8.docx")
                .build());

        files.add(File.builder()
                .notice(notices.get(5)) // notice_id 6
                .name("알림아리_부스_신청_안내문.docx")
                .type("docx")
                .objectUri("https://dongarium-attachments.s3.ap-northeast-2.amazonaws.com/attachments/%EC%95%8C%EB%A6%BC%EC%95%84%EB%A6%AC_%EB%B6%80%EC%8A%A4_%EC%8B%A0%EC%B2%AD_%EC%95%88%EB%82%B4%EB%AC%B8.docx")
                .build());

        files.add(File.builder()
                .notice(notices.get(6)) // notice_id 7
                .name("알림아리_무대_참가_모집_안내문.docx")
                .type("docx")
                .objectUri("https://dongarium-attachments.s3.ap-northeast-2.amazonaws.com/attachments/%EC%95%8C%EB%A6%BC%EC%95%84%EB%A6%AC_%EB%AC%B4%EB%8C%80_%EC%B0%B8%EA%B0%80_%EB%AA%A8%EC%A7%91_%EC%95%88%EB%82%B4%EB%AC%B8.docx")
                .build());

        files.add(File.builder()
                .notice(notices.get(7)) // notice_id 8
                .name("알림아리_행사_운영_안내문.docx")
                .type("docx")
                .objectUri("https://dongarium-attachments.s3.ap-northeast-2.amazonaws.com/attachments/%EC%95%8C%EB%A6%BC%EC%95%84%EB%A6%AC_%ED%96%89%EC%82%AC_%EC%9A%B4%EC%98%81_%EC%95%88%EB%82%B4%EB%AC%B8.docx")
                .build());

        files.add(File.builder()
                .notice(notices.get(12)) // notice_id 13
                .name("동아리_활동_사진_공모전_안내문.docx")
                .type("docx")
                .objectUri("https://dongarium-attachments.s3.ap-northeast-2.amazonaws.com/attachments/%EB%8F%99%EC%95%84%EB%A6%AC_%ED%99%9C%EB%8F%99_%EC%82%AC%EC%A7%84_%EA%B3%B5%EB%AA%A8%EC%A0%84_%EC%95%88%EB%82%B4%EB%AC%B8.docx")
                .build());

        files.add(File.builder()
                .notice(notices.get(17)) // notice_id 18
                .name("동아리_활동_중간_점검_보고_안내문.docx")
                .type("docx")
                .objectUri("https://dongarium-attachments.s3.ap-northeast-2.amazonaws.com/attachments/%EB%8F%99%EC%95%84%EB%A6%AC_%ED%99%9C%EB%8F%99_%EC%A4%91%EA%B0%84_%EC%A0%90%EA%B2%80_%EB%B3%B4%EA%B3%A0_%EC%95%88%EB%82%B4%EB%AC%B8.docx")
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
                .writer("202525")
                .build());

        reviews.add(ClubReview.builder()
                .club(club1)
                .content("동아리원들끼리의 협업이 잘 되고, 실제 캠페인도 진행해볼 수 있어서 뜻깊은 경험이었습니다. 추천합니다!")
                .writer("202522")
                .build());

        reviews.add(ClubReview.builder()
                .club(club1)
                .content("처음에는 낯설었지만 금방 친해지고, 사회문제에 대한 시각이 넓어졌어요. 프로젝트 중심이라 참여감이 높습니다.")
                .writer("202523")
                .build());

        clubReviewRepository.saveAll(reviews);
    }

}
