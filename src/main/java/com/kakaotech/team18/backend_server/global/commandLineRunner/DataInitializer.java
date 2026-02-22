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
@Profile({ "prod", "default" })
public class DataInitializer implements CommandLineRunner {

        private final ClubRepository clubRepository;
        private final ClubApplyFormRepository clubApplyFormRepository;
        private final FormQuestionRepository formQuestionRepository;
        private final ApplicationRepository applicationRepository;
        private final ClubMemberRepository clubMemberRepository;
        private final ClubReviewRepository clubReviewRepository;
        private final FileDataRepository fileDataRepository;
        private final NoticeRepository noticeRepository;
        private final UserRepository userRepository;
        private final AnswerRepository answerRepository;

        @Override
        @Transactional
        public void run(String... args) {
                if (clubRepository.count() > 0)
                        return; // 이미 데이터 있으면 전체 seed 스킵

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
                // if (userRepository.count() > 0) return userRepository.findAll();

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

                // 15번 동아리(ECHO) 회장
                users.add(User.builder()
                                .kakaoId(10068L)
                                .email("eunji.choi@jnu.ac.kr")
                                .name("최은지")
                                .studentId("202568")
                                .phoneNumber("010-3341-8820")
                                .department("영어영문학과")
                                .build());

                // 16번 동아리(어푸어푸) 회장
                users.add(User.builder()
                                .kakaoId(10069L)
                                .email("dohyun.nam@jnu.ac.kr")
                                .name("남도현")
                                .studentId("202569")
                                .phoneNumber("010-7762-4415")
                                .department("체육교육학과")
                                .build());

                return userRepository.saveAll(users);
        }


        private List<Club> seedClubsWithIntroAndImages() {
                List<Club> clubs = new ArrayList<>();

                // --- 1~10: 실제 동아리 데이터 ---

                // 1. 얼라이브 (얼티밋 프리스비)
                ClubIntroduction intro1 = ClubIntroduction.builder()
                                .overview("얼라이브는 전남대학교 총동아리연합회 체육분과에 소속된 중앙동아리이며, " +
                                                "얼티밋 프리스비(Ultimate Frisbee, 원반을 사용하는 팀 스포츠)를 하고 있는 동아리입니다. " +
                                                "얼티밋 프리스비는 원반(프리스비)을 이용해 엔드존에 패스를 연결하며 득점하는 팀 스포츠로, " +
                                                "심판 없이 선수 스스로 판정하는 '스피릿 오브 더 게임(Spirit of the Game)' 정신을 기반으로 합니다. " +
                                                "초보자도 부담 없이 시작할 수 있으며, 함께 뛰고 성장하는 과정 자체가 얼라이브의 핵심 가치입니다.")
                                .activities("• 매주 화, 목 18시 30분~21시 30분 대운동장 정기 연습\n" +
                                                "• 기초 스로잉·캐칭부터 팀 전술 훈련까지 체계적 커리큘럼\n" +
                                                "• 전국 대학 얼티밋 프리스비 대회 참가\n" +
                                                "• 신입 부원 대상 기초 클리닉 및 룰 교육\n" +
                                                "• MT, 뒤풀이 등 팀 친목 활동")
                                .ideal("얼라이브는 '함께 뛰며 성장하는 팀 플레이어'를 찾습니다.\n\n" +
                                                "[자격요건]\n" +
                                                "• 새로운 스포츠에 도전하고 싶은 전남대 재학생\n" +
                                                "• 주 2회 정기 연습에 꾸준히 참여할 수 있는 분\n" +
                                                "• 팀워크와 페어플레이를 중요하게 생각하는 분\n\n" +
                                                "[모집 안내]\n" +
                                                "• 별도의 면접 없이 구글 폼 답변 내용을 통해 신입회원 선발\n" +
                                                "• 경험이 없어도 전혀 상관없습니다. 얼라이브에서 프리스비의 짜릿한 매력을 함께 느껴보세요.")
                                .build();
                clubs.add(Club.builder().name("얼라이브").category(Category.SPORTS).location("제2학생마루 306호").shortIntroduction("얼티밋 프리스비 스포츠 동아리").introduction(intro1).recruitStart(LocalDateTime.of(2026, 2, 16, 0, 0)).recruitEnd(LocalDateTime.of(2026, 3, 13, 23, 59)).regularMeetingInfo("매주 화, 목 18시 30분~21시 30분, 대운동장").caution("별도의 면접 없이 구글 폼 답변 내용을 통해 신입회원 선발").isInterviewRequired(false).isRegistered(true).everyTimeUrl("https://everytime.kr/418923/v/400314201").googleFormUrl("https://forms.gle/4Hz4UAkYLsfWXf2J6").build());

                // 2. 과실연 (과학 교육 봉사)
                ClubIntroduction intro2 = ClubIntroduction.builder()
                                .overview("과실연은 과학 교육 봉사를 중심으로 활동하는 전남대학교 봉사 동아리입니다. " +
                                                "과학이라는 도구를 통해 지역 청소년들에게 배움의 기회를 제공하고, " +
                                                "동시에 회원들 스스로도 가르치는 과정에서 과학적 사고력과 소통 능력을 기릅니다. " +
                                                "매주 정기 과실회담을 통해 봉사 활동을 기획하고 피드백을 나누며, " +
                                                "실험 중심의 체험형 교육 프로그램을 직접 설계하고 진행합니다.")
                                .activities("• 매주 화요일 18:30~20:00 정기 과실회담 (꿈교, 꿈과교를 위한 조 활동 진행)\n" +
                                                "• 한 학기 2번 청소년 과학교육봉사 프로그램 시행 (꿈자람 교실, 꿈자람 과학 교실)\n" +
                                                "• 대내외적 자체 봉사 프로그램 (플로깅, 북구 자원봉사센터 봉사, 쏙쏙캠프)\n" +
                                                "• MT, 감귤제 등 친목 활동\n" +
                                                "• 야구장, 보드게임 등 다양한 소모임 진행")
                                .ideal("과실연은 '과학으로 나눔을 실천하는 사람'을 찾습니다.\n\n" +
                                                "[자격요건]\n" +
                                                "• 과학 교육 봉사에 관심이 있는 전남대 재학생 (전공 무관)\n" +
                                                "• 매주 정기 모임 및 봉사 활동에 성실히 참여할 수 있는 분\n" +
                                                "• 청소년과 소통하며 가르치는 것에 보람을 느끼는 분\n\n" +
                                                "[유의사항]\n" +
                                                "• 정규활동인 과실회담과 꿈자람 과학 교실, 꿈자람 교실은 꼭 참석 필수\n" +
                                                "• 지원서를 바탕으로 면접날짜 배정 및 면접 진행 예정")
                                .build();
                clubs.add(Club.builder().name("과실연").category(Category.VOLUNTEER).location("제1학생회관").shortIntroduction("과학 교육 봉사 동아리").introduction(intro2).recruitStart(LocalDateTime.of(2026, 2, 12, 0, 0)).recruitEnd(LocalDateTime.of(2026, 3, 5, 18, 0)).regularMeetingInfo("매주 화요일 18:30~20:00 과실회담").caution("정규활동인 과실회담과 꿈자람 과학 교실, 꿈자람 교실은 꼭 참석 필수").isInterviewRequired(true).isRegistered(true).everyTimeUrl("https://everytime.kr/418923/v/400282998").googleFormUrl("https://naver.me/5WUhwzpc").build());

                // 3. 소리터관현악단 (비전공자 오케스트라)
                ClubIntroduction intro3 = ClubIntroduction.builder()
                                .overview("소리터관현악단은 1989년에 설립된 비전공자 중심의 오케스트라 동아리로, " +
                                                "자유로운 분위기 속에서 울림을 함께하고 있습니다. " +
                                                "음악을 사랑하는 사람들과 함께하며, 누구나 부담 없이 연주를 즐길 수 있습니다. " +
                                                "24시간 자유롭게 연습할 수 있는 넓은 동아리방을 보유하고 있으며, " +
                                                "매학기 개인 연주 및 소규모 합주 기회를 제공합니다.")
                                .activities("• 매 학기 정기 연주회에서 클래식, 영화·드라마 OST, 뉴에이지, 재즈 등 다양한 장르의 곡 연주\n" +
                                                "• 팀별 연주 (3~5인 구성으로 팀을 나누어 1~2곡 연습)\n" +
                                                "• 개인 연주 (희망자에 한해 개인 연주 기회 제공)\n" +
                                                "• 5월 말 1학기 정기 연주회 예정\n" +
                                                "• 모든 활동 참여는 선택!")
                                .ideal("소리터관현악단은 '음악을 함께 만들어가는 따뜻한 연주자'를 기다립니다.\n\n" +
                                                "[가입 대상]\n" +
                                                "• 기본적인 악기 연주 경험이 있는 비전공자\n\n" +
                                                "[모집 악기]\n" +
                                                "• 현악기: 바이올린, 첼로, 비올라\n" +
                                                "• 관악기: 플루트, 클라리넷, 금관악기 등\n" +
                                                "• 피아노\n\n" +
                                                "[유의사항]\n" +
                                                "• 전공자는 본인 전공 악기로 지원 불가\n" +
                                                "• 피아노와 플루트는 활동 인원이 많은 관계로 1~2명만 선발하거나 선발하지 않을 수 있음\n" +
                                                "• 방학 중 또는 알림아리 이후에 간단한 면접(악기 테스트 포함 가능) 예정")
                                .build();
                clubs.add(Club.builder().name("소리터관현악단").category(Category.LITERATURE).location("용지와 테니스코트 사이 건물 1층").shortIntroduction("비전공자 오케스트라").introduction(intro3).recruitStart(LocalDateTime.of(2026, 2, 10, 0, 0)).recruitEnd(LocalDateTime.of(2026, 3, 5, 0, 0)).caution("방학 중 또는 알림아리 이후에 간단한 면접이 있을 예정 (악기 테스트 가능)").isInterviewRequired(true).isRegistered(true).everyTimeUrl("https://everytime.kr/418923/v/399984087").googleFormUrl("https://forms.gle/zsFNfvkSRhF3Q2je8").build());

                // 4. 테크니션 (복싱)
                ClubIntroduction intro4 = ClubIntroduction.builder()
                                .overview("테크니션은 '참피온 복싱 체육관'과 제휴를 맺어 운동을 진행하고 있는 복싱 동아리입니다. " +
                                                "참피온 복싱 체육관에서는 관장님께 체계적이고 전문적인 복싱 지도를 받을 수 있습니다. " +
                                                "체육관에는 복싱링, 샌드백, 런닝머신, 간단한 웨이트 장비가 구비되어 있으며, 샤워실 및 탈의실이 마련되어 있습니다. " +
                                                "체육관은 평일 15:00~23:00까지 운영하며, 언제든 개인적으로 방문하여 운동하실 수 있습니다. " +
                                                "평일 15시 전이나 주말/공휴일은 체육관 비밀번호를 입력하여 자율운동이 가능합니다. " +
                                                "동아리에 가입하시면 체육관 일반 회원보다 두 배 할인된 가격으로 복싱을 배우실 수 있습니다.")
                                .activities("• 매주 화, 목 18:00~19:30 단체 운동 진행 (참가는 자율)\n" +
                                                "• 전문 관장님 지도 하 체계적인 복싱 기본기 습득\n" +
                                                "• 참피온 복싱 체육관 자유 이용 (평일 15:00~23:00)\n" +
                                                "• 일반 관원들과 함께 운동하는 열린 환경\n" +
                                                "• 정기 친목 모임")
                                .ideal("테크니션은 이런 분께 추천합니다.\n\n" +
                                                "• 평소에 복싱에 관심이 있지만, 가격이 부담스러워 망설이셨던 분들\n" +
                                                "• 멋들어진 취미를 가지고 싶으신 분들\n" +
                                                "• 다이어트, 자기관리를 즐겁게 하고 싶은 분들\n\n" +
                                                "[모집 안내]\n" +
                                                "• 신입부원 모집은 상시 진행\n" +
                                                "• 학년, 나이, 성별, 휴학생 상관없이 모집\n" +
                                                "• 면접 없음")
                                .build();
                clubs.add(Club.builder().name("테크니션").category(Category.SPORTS).location("제1 학생마루 4층 424호").introduction(intro4).recruitStart(LocalDateTime.of(1000, 1, 1, 0, 0)).recruitEnd(LocalDateTime.of(9999, 12, 31, 23, 59)).regularMeetingInfo("매주 화, 목 18:00-19:30 (참가 자율)").caution("신입부원 모집은 상시하고 있으며, 학년, 나이, 성별, 휴학생 상관없이 모집합니다. 면접은 없습니다.").isInterviewRequired(false).isRegistered(true).everyTimeUrl("https://everytime.kr/418923/v/400219834").googleFormUrl("010-2203-3863").build());

                // 5. LIT:CH (스트릿 댄스)
                ClubIntroduction intro5 = ClubIntroduction.builder()
                                .overview("LIT:CH(릿치)는 스트릿 문화를 기반으로 춤을 사랑하는 사람들이 모인 동아리입니다. " +
                                                "힙합, 팝핑, 락킹, 왁킹, 브레이킹 등 다양한 스트릿 댄스 장르를 다루며, " +
                                                "춤을 사랑하는 학생들이 모여 함께 연습하고 무대를 만들어가는 공동체입니다. " +
                                                "릿치는 열정과 성실도를 가장 중요하게 생각합니다!")
                                .activities("• 6기 멤버: 3월 3~4주차 기초 트레이닝 [월/화 16~18시, 19~21시] - 일주일에 네 타임 중 두 타임 선택 후 참여\n" +
                                                "• 신규 가입시 비기너로 구분되며, 승급과제 수행 후 멤버로서 무대 참여 가능\n" +
                                                "• 비디오그래퍼: 활동비 및 워크숍 비용 면제, 기초·장르 트레이닝 참여 가능 (영상 촬영을 위해 무대 참여는 불가)\n" +
                                                "• 교내 축제, 입학식 등 정기 무대 공연\n" +
                                                "• 외부 댄스 대회 및 배틀 참가\n" +
                                                "• 촬영(영상 작업) 및 SNS 콘텐츠 제작")
                                .ideal("릿치는 열정과 성실도를 가장 중요하게 생각합니다!\n\n" +
                                                "[자격요건]\n" +
                                                "• 1년 이상 활동이 가능하신 열정 넘치는 학우\n" +
                                                "• 신입생이든, 휴학생이든, 성별/나이 제한 없이 춤을 사랑하시는 분들 모두 환영\n\n" +
                                                "[선발 방식]\n" +
                                                "• 구글 폼으로 신청서 작성 및 제출\n" +
                                                "• 지원서 (70%) + 오디션 (30%)\n" +
                                                "• 1분 내외의 자유곡을 준비해주세요! (장르 불문)")
                                .build();
                clubs.add(Club.builder().name("LIT:CH").category(Category.LITERATURE).location("제1학생회관 314호").shortIntroduction("스트릿 댄스 동아리").introduction(intro5).recruitStart(LocalDateTime.of(2026, 2, 9, 0, 0)).recruitEnd(LocalDateTime.of(2026, 3, 8, 0, 0)).regularMeetingInfo("3월 3~4주차 기초 트레이닝 [월/화 16~18시, 19~21시]").caution("지원서 (70%) + 오디션 (30%), 1분 내외의 자유곡 준비 (장르 불문)").isInterviewRequired(true).isRegistered(true).everyTimeUrl("https://everytime.kr/418923/v/399873173").googleFormUrl("https://forms.gle/Bnq1qvqJrynbiYCt6").build());

                // 6. DOVE (축구)
                ClubIntroduction intro6 = ClubIntroduction.builder()
                                .overview("DOVE는 전남대학교 총동아리연합회 체육분과 소속 축구 동아리입니다. " +
                                                "체계적인 전술 훈련과 팀워크를 바탕으로 교내외 다양한 축구 대회에서 활약하고 있습니다. " +
                                                "DOVE는 단순히 공을 차는 모임이 아니라, 축구를 매개로 학과와 학년을 넘어 " +
                                                "새로운 인연을 만들고, 승리와 패배를 함께 경험하며 성장하는 공동체입니다.")
                                .activities("• 수상경력: 전남대 총장배 축구대회 11회 우승 (95, 96, 97, 98, 99, 00, 02, 03, 04, 19, 23)\n" +
                                                "• 교내 총장배 축구대회 및 리그전 참가\n" +
                                                "• 교외 대학동아리 축구대회 참가\n" +
                                                "• 체계적인 전술 훈련 진행\n" +
                                                "• 정기 회식 및 팀 빌딩 활동")
                                .ideal("DOVE는 이런 사람을 찾습니다.\n\n" +
                                                "• 축구에 열정이 있는 사람들\n" +
                                                "• 체계적인 전술 훈련을 원하는 사람들\n" +
                                                "• 다양한 대회(교내 총장배, 교외 대학동아리 축구대회 등)에 참가하여 대학생활에 추억을 쌓고 싶은 사람들\n\n" +
                                                "[연락처]\n" +
                                                "• 인스타, 신입회원 오픈채팅방 또는 연락처로 연락 주세요!")
                                .build();
                clubs.add(Club.builder().name("DOVE").category(Category.SPORTS).location("대운동장").introduction(intro6).recruitStart(LocalDateTime.of(1000, 1, 1, 0, 0)).recruitEnd(LocalDateTime.of(9999, 12, 31, 23, 59)).caution("인스타, 신입회원 오픈채팅방 또는 연락처로 연락 주세요").isInterviewRequired(false).isRegistered(true).everyTimeUrl("https://everytime.kr/418923/v/399818659").googleFormUrl("인스타: @cnu_dove").build());

                // 7. 관현악반 (CNUO 오케스트라)
                ClubIntroduction intro7 = ClubIntroduction.builder()
                                .overview("전남대 관현악반 CNUO는 47년의 역사를 바탕으로 정기연주회, 신입생 환영연주회, " +
                                                "교내·외 공연 등을 통해 전남대학교를 대표하는 오케스트라 동아리로 활동하고 있습니다. " +
                                                "교향곡, 협주곡, 서곡 등 폭넓은 클래식 레퍼토리를 다루며, " +
                                                "현악, 관악, 타악 파트의 조화로운 앙상블을 추구합니다.")
                                .activities("• 자축제, 신입생 환영연주회, 정기연주회, 외부 버스킹 공연 등 다채로운 연주 활동\n" +
                                                "• MT, 체육대회, 수련회, 야유회 등 따뜻하고 즐거운 친목 활동\n" +
                                                "• 월~금 중 주 3회 이상 정기 합주 연습\n" +
                                                "• 파트별 섹션 연습 및 전체 합주\n" +
                                                "• 가입비: 5,000원 / 정기 회비: 70,000원 (연주회 준비, MT, 야유회 등에 사용)\n" +
                                                "• 악기 대여비: 10,000원 (공용 악기 한정, 선착순 배정)")
                                .ideal("관현악반은 '오케스트라의 일원으로서 하모니를 만들어갈 연주자'를 찾습니다.\n\n" +
                                                "[가입 대상]\n" +
                                                "• 악기를 배우고 싶거나 연주를 꿈꾸는 신입생, 재학생, 복학생, 휴학생 누구나\n" +
                                                "• 오케스트라에서 사용되는 모든 악기에 관심 있는 분들\n\n" +
                                                "[가입 방법]\n" +
                                                "• 관현악반 프로필 링크에서 온라인 가입신청서 작성\n" +
                                                "• 3월 알림아리 때 관현악반 부스 방문하여 입회원서 작성 시 최종입부 완료\n" +
                                                "• 온라인 가입신청서는 1차 수요 조사 및 부스 방문 안내 용도")
                                .build();
                clubs.add(Club.builder().name("관현악반").category(Category.LITERATURE).location("제 1학생회관 307, 308호").shortIntroduction("47년 전통의 오케스트라").introduction(intro7).recruitStart(LocalDateTime.of(2026, 1, 31, 0, 0)).recruitEnd(LocalDateTime.of(2026, 3, 6, 23, 59)).regularMeetingInfo("월~금 중 주 3회 이상 정기적인 출석 필수").caution("온라인 가입신청서 작성 후 알림아리 때 부스를 직접 방문하여 입회원서 및 서약서를 작성해야 가입 완료").isInterviewRequired(true).isRegistered(true).everyTimeUrl("https://everytime.kr/418923/v/399569935").googleFormUrl("https://litt.ly/cnuo").build());

                // 8. 아이디어스 (발명특허·공모전)
                ClubIntroduction intro8 = ClubIntroduction.builder()
                                .overview("Turn your idea into reality! 아이디어스는 상상을 실현하는 전남대학교 유일 발명특허 및 공모전 동아리입니다. " +
                                                "일상 속 불편함에서 출발한 창의적 아이디어를 발명품이나 비즈니스 모델로 발전시키고, " +
                                                "실제 특허 출원 및 공모전 수상으로 이어가는 것을 목표로 합니다. " +
                                                "우수 활동자 선정(상금: 기프티콘), 단체 회식 지원비(개강·중간·종강파티), " +
                                                "팀별 지원비, 시험기간 간식사업 등 다양한 복지도 제공합니다.")
                                .activities("• 희망 공모전 분야에 맞춰 팀 빌딩\n" +
                                                "• 매주 월요일 19시, 전체회의를 통해 각 팀의 진행상황 및 결과 발표 (시험기간 제외)\n" +
                                                "• 피드백을 통한 아이디어 수정 보완 및 구체화\n" +
                                                "• 특허 출원 및 공모전을 통한 아이디어 구현\n" +
                                                "• 우수 활동자 선정 (매학기 최우수 1명, 우수 2명)")
                                .ideal("아이디어스는 이런 사람을 찾습니다.\n\n" +
                                                "• 전남대학교 재학생/휴학생 누구나\n" +
                                                "• 공모전에 나가고 싶은 사람\n" +
                                                "• 발표 능력을 향상하고 싶은 사람\n" +
                                                "• 스펙을 쌓고 싶은 사람\n" +
                                                "• 다양한 과 사람들과 교류하고 싶은 사람\n" +
                                                "• 특허를 내고 싶은 사람\n\n" +
                                                "[선발 일정]\n" +
                                                "• 서류합격자 발표: 2026.02.04\n" +
                                                "• 대면 면접: 2026.02.11~2026.02.12\n" +
                                                "• 최종합격자 발표: 2026.02.14")
                                .build();
                clubs.add(Club.builder().name("아이디어스").category(Category.STUDY).location("제1학생회관 412호").shortIntroduction("상상을 실현하는 발명특허·공모전 동아리").introduction(intro8).recruitStart(LocalDateTime.of(2026, 1, 21, 0, 0)).recruitEnd(LocalDateTime.of(2026, 2, 1, 0, 0)).regularMeetingInfo("매주 월요일 19시 전체회의 (시험기간 제외)").caution("서류합격자 발표: 2026.02.04, 대면 면접: 2026.02.11~02.12, 최종합격자 발표: 2026.02.14").isInterviewRequired(true).isRegistered(true).everyTimeUrl("https://everytime.kr/418923/v/399004379").googleFormUrl("https://docs.google.com/forms/d/e/example/viewform").build());

                // 9. 미담장학회 (교육 기부 봉사)
                ClubIntroduction intro9 = ClubIntroduction.builder()
                                .overview("미담장학회는 대한민국 청소년들이 경제적 여건과 관계없이 교육받을 권리를 목표로 하는 교육 기부 동아리입니다. " +
                                                "전국 국공립 대학생 멘토들이 함께하는 교육기부 시스템을 운영하며, " +
                                                "전남대학교 미담장학회는 초·중학생 교과목 멘토링을 주력으로 합니다. " +
                                                "교육부·한국창의재단 주관 '교육기부 우수기관'으로 선정된 바 있습니다.")
                                .activities("• 1:1 또는 1:N 방식의 교과 멘토링 및 놀이지도 (정기 봉사)\n" +
                                                "  - 초·중학생 교과목 멘토링: 매곡중앙, 보람, 서머힐북구, 오정, 해달별 지역아동센터\n" +
                                                "  - 학교 밖 청소년 대상 검정고시 및 수능 멘토링: 북구 학교밖청소년지원센터\n" +
                                                "• 자체 기획 봉사: 학교 밖 청소년 대상 진로 탐색 프로그램 '러닝메이트', 아동센터 멘티 대상 '원데이 클래스'\n" +
                                                "• 외부 연계 봉사: 진로 멘토링, 광주 교육청 수학과학축전 행사 보조, 심리·상담 멘토링\n" +
                                                "• 개강/종강파티, MT, 소풍, 마니또, 시험기간 응원사업 등 친목 활동")
                                .ideal("미담장학회는 '교육으로 세상을 바꾸는 따뜻한 멘토'를 찾습니다.\n\n" +
                                                "[자격요건]\n" +
                                                "• 책임감 있으신 분\n" +
                                                "• 교육 봉사에 관심 있으신 분\n" +
                                                "• 1~3학년 재학생\n" +
                                                "• 2학기 이상 활동 가능하신 분 (군 휴학 제외, 최소 한 학기 정기 봉사 참여 필수)\n\n" +
                                                "[정기 봉사 시간대]\n" +
                                                "• 아동센터: 13:30~15:30 / 14:00~16:00 / 15:00~17:00 / 15:30~17:30 / 16:00~18:00\n" +
                                                "• 학교밖청소년지원센터: 9:00~18:00 중 2시간 이상")
                                .build();
                clubs.add(Club.builder().name("미담장학회").category(Category.VOLUNTEER).location("제 2학생회관 301호").shortIntroduction("교육기부 우수기관 선정 교육 봉사 동아리").introduction(intro9).recruitStart(LocalDateTime.of(2026, 2, 13, 0, 0)).recruitEnd(LocalDateTime.of(2026, 2, 25, 0, 0)).isInterviewRequired(true).isRegistered(true).everyTimeUrl("https://everytime.kr/544806/v/400400343").googleFormUrl("https://naver.me/GVV5zstA").build());

                // 10. PPP (탁구)
                ClubIntroduction intro10 = ClubIntroduction.builder()
                                .overview("PPP는 전남대학교 탁구 중앙 동아리입니다. " +
                                                "탁구를 사랑하는 학생들이 모여 실력을 키우고, 즐겁게 운동하며, " +
                                                "교내외 대회에서 활약하는 것을 목표로 합니다. " +
                                                "대학(원)생, 재학생, 휴학생, 외국인 유학생 모두 가입 가능합니다.")
                                .activities("• 매주 정기 모임 3회 (18:00~22:00, 다양한 요일로 활동)\n" +
                                                "• 동아리 내 1:1 탁구 훈련\n" +
                                                "• 동아리 내 탁구 대회 (IPL)\n" +
                                                "• 호남권 대학 교류전\n" +
                                                "• 개강 및 종강 파티, MT\n" +
                                                "• 테마 행사(리그전 등)와 번개모임\n" +
                                                "• 지역생활체육대회 / 전국대학탁구대회 참여")
                                .ideal("PPP는 '탁구를 즐기며 함께 성장하고 싶은 사람'을 찾습니다.\n\n" +
                                                "[모집 대상]\n" +
                                                "• 적극적으로 활동 가능한 전남대생\n" +
                                                "• 대학(원)생, 재학생, 휴학생, 외국인 유학생 모두 포함\n\n" +
                                                "라켓을 잡아본 적 없어도 괜찮습니다. PPP에서 함께 핑퐁의 세계에 빠져보세요.")
                                .build();
                clubs.add(Club.builder().name("PPP").category(Category.SPORTS).location("제2학생회관 405호").shortIntroduction("전남대학교 탁구 중앙 동아리").introduction(intro10).recruitStart(LocalDateTime.of(2026, 2, 13, 0, 0)).recruitEnd(LocalDateTime.of(2026, 3, 5, 0, 0)).regularMeetingInfo("정기 모임 18:00~22:00, 주 3회 다양한 요일로 활동").isInterviewRequired(false).isRegistered(true).everyTimeUrl("https://everytime.kr/418923/v/400508043").googleFormUrl("https://docs.google.com/forms/d/e/example/viewform").build());


                // --- 11~16: 신규 실제 동아리 ---

                // 11. KUSA (유네스코 봉사)
                ClubIntroduction intro11 = ClubIntroduction.builder()
                                .overview("유네스코학생회 KUSA는 50년 전통의 동아리로, 유네스코한국위원회가 전국 대학에 설립하고 활동을 지원하며 설립된 동아리입니다. " +
                                                "현재 KUSA는 전국적으로 20여 개의 대학에서 활동 중입니다.")
                                .activities("☑️하계 해외봉사\n" +
                                                "☑️소모임 활동(보드게임, 운동, 독서)\n" +
                                                "☑️교육봉사(지역아동센터 위주)\n" +
                                                "☑️정기 플로깅\n" +
                                                "☑️MT\n" +
                                                "☑️유네스코 유적 탐방\n" +
                                                "☑️북구 및 서구 자원봉사 연계 외부봉사")
                                .ideal("전남대학교 재학생, 휴학생 중 한 학기 이상 활동 가능한 자")
                                .build();
                clubs.add(Club.builder().name("KUSA").category(Category.VOLUNTEER).location("").shortIntroduction("50년 전통의 유네스코 봉사 동아리").introduction(intro11).recruitStart(LocalDateTime.of(2026, 2, 19, 0, 0)).recruitEnd(LocalDateTime.of(2026, 2, 28, 23, 59)).caution("면접 절차는 생략하고, [2월 28일 23:59까지 폼 작성] 완료해주시면 폼 내용을 보고 선발할 예정입니다😊\n선발 결과는 3월 3일에 단톡방 초대 예정입니다.").isInterviewRequired(false).isRegistered(true).googleFormUrl("https://forms.gle/RG8J7Zx8SCtR4U5W9").build());

                // 12. 전검회 (검도)
                ClubIntroduction intro12 = ClubIntroduction.builder()
                                .overview("전검회는 전남대학교 검도 동아리입니다. 오치 검도관과 연계하여 체계적인 검도 수련을 진행하며, " +
                                                "입문자부터 경험자까지 모두 함께할 수 있는 열린 동아리입니다.")
                                .activities("연계 도장 '오치 검도관'에서 평일 4시/5시반/6시반/7시반/8시반 중 본인이 원하는 시간대에 검도 활동")
                                .ideal("나이, 학번, 학년 제한 없이 검도를 처음 배우시는 분들, 쉬어칼 하고 계셨던 분들 모두 환영합니다!")
                                .build();
                clubs.add(Club.builder().name("전검회").category(Category.SPORTS).location("오치 검도관").shortIntroduction("전남대학교 검도 동아리").introduction(intro12).recruitStart(LocalDateTime.of(1000, 1, 1, 0, 0)).recruitEnd(LocalDateTime.of(9999, 12, 31, 23, 59)).regularMeetingInfo("평일 4시/5시반/6시반/7시반/8시반 (오치 검도관)").caution("학과, 학번, 이름과 함께 문자를 보내주세요!\n연락처: 010-9186-0153").isInterviewRequired(false).isRegistered(true).everyTimeUrl("https://everytime.kr/418923/v/400783896").build());

                // 13. 별따오기 (천문 관측)
                ClubIntroduction intro13 = ClubIntroduction.builder()
                                .overview("별따오기는 전남대학교 천문 관측 동아리입니다. 정기관측, 반짝관측, 교내관측 등 다양한 관측 활동을 통해 " +
                                                "별과 우주를 직접 경험하고 탐구하는 것을 목표로 합니다.")
                                .activities("* 3, 5, 9, 11월 마지막 주 토요일 광주 근교로 1박 2일 간 떠나는 <정기관측>\n" +
                                                "* 하늘이 맑은 날에는 당일치기로 별을 보러 떠나는 <반짝관측>\n" +
                                                "* 교내에서 망원경을 통해 달과 행성을 관찰하는 <교내관측>\n" +
                                                "* 실링왁스 등으로 천체를 배우는 <원데이 클래스>")
                                .ideal("전남대학교 학생(신입생의 경우 학번이 아닌 수험번호를 기재해 주세요)")
                                .build();
                clubs.add(Club.builder().name("별따오기").category(Category.STUDY).location("").shortIntroduction("별따오기에 들어오셔서 멋진 별들을 직접 관측해 보세요").introduction(intro13).recruitStart(LocalDateTime.of(2026, 2, 18, 0, 0)).recruitEnd(LocalDateTime.of(2026, 2, 28, 23, 59)).caution("면접 일정\n- 3월 4일 ~ 6일 예정 (단톡 초대 후 추후 안내)").isInterviewRequired(true).isRegistered(true).everyTimeUrl("https://everytime.kr/418923/v/400757589").googleFormUrl("https://docs.google.com/forms/d/e/1FAIpQLSfqyr9wdPeWjhEvpPf-NP4gIa2C-ZpLxmuUqFXuTMO12N6P_Q/viewform?usp=header").build());

                // 14. SEA-FOX (스쿠버다이빙)
                ClubIntroduction intro14 = ClubIntroduction.builder()
                                .overview("Sea-fox는 40년 넘게 이어져 온 전남대학교 유일무이 스쿠버다이빙 동아리로 정기다이빙, 자격증 취득, " +
                                                "수영장 연습, 친목 활동까지 다양한 경험을 하실 수 있습니다. " +
                                                "또한 남해 미조, 울릉도, 제주도, 거문도, 통영, 여수 등 다양한 지역에서 다이빙을 진행합니다.")
                                .activities("계절별 정기 다이빙/수영장 연습/친목 활동/자격증 취득")
                                .ideal("🐠 새로운 경험을 하고 싶으신 분!\n\n" +
                                                "🐟 스킨스쿠버에 관심이 있으신 분!\n\n" +
                                                "🦈 물이나 바다를 좋아하시는 분!\n\n" +
                                                "🐋 다양한 사람들과 만나고 싶으신 분!\n\n" +
                                                "🐳 해양생물에 관심이 있으신 분!\n\n" +
                                                "나이와 학번 제한 없이 신청하실 수 있습니다:)")
                                .build();
                clubs.add(Club.builder().name("SEA-FOX").category(Category.SPORTS).location("").shortIntroduction("40년 전통 전남대 유일 스쿠버다이빙 동아리").introduction(intro14).recruitStart(LocalDateTime.of(2026, 2, 17, 0, 0)).recruitEnd(LocalDateTime.of(2026, 3, 5, 23, 59)).caution("네이버폼으로 가입 신청을 받은 후 동아리방에서 대면 면접이 진행될 예정입니다. / 상세 면접 시간은 추후 문자로 공지해드리겠습니다. 면접 불참 시 자동 탈락이며, 문자 수신 후 면접 참여 여부 답장 부탁드립니다.").isInterviewRequired(true).isRegistered(true).googleFormUrl("https://naver.me/GYDkioaa").build());

                // 15. ECHO (영어 회화)
                ClubIntroduction intro15 = ClubIntroduction.builder()
                                .overview("Q : ECHO 활동, 무엇이 좋은가요?\n\n" +
                                                "A : 주별로 다양한 주제를 연습하며 OPIC등 스피킹 시험 및 회화 실력 향상에 필수적인 영어 발화량 확보가 가능하고, " +
                                                "전남대 교내 봉사, 교환학생(유학) 준비, 나아가 취업 준비를 위한 영어 면접 대비까지 가능합니다! " +
                                                "꾸준한 영어회화를 위해 많은 인원들이 방학 때도 ZOOM으로 수업을 참여 중입니다.")
                                .activities("Q : 월요일 리더 클래스의 진행 방식이 궁금합니다!\n\n" +
                                                "A : 회원들이 돌아가며 리더를 맡게 되는데 리더는 본인이 관심 있는 주제로 수업자료를 준비하게 됩니다. " +
                                                "리더 수업은 주제에 대한 리더의 발표와 리더가 준비한 3가지 질문에 회원들이 답변을 하는 Question time으로 진행됩니다.\n" +
                                                "📍Question time은 회원분들의 집중력과 영어 사용 빈도를 높이기 위해 그룹별로 진행됩니다.\n\n" +
                                                "Q : 목요일 소규모 그룹 클래스의 진행 방식이 궁금합니다!\n\n" +
                                                "A : 목요일 수업은 Keyword time, Speaker time으로 구성되며 소규모 그룹 수업으로 진행됩니다.\n" +
                                                "📍Keyword time : 수업 시작 직후 제시되는 주제에 대한 Free talking으로 진행합니다.\n" +
                                                "📍Speaker time : 수업 시작 후 제시되는 주제에 대해 각자 이야기와 질문을 하며 조원들끼리 대화를 나누는 시간입니다.")
                                .ideal("에코는 신입생, 고학년, 휴학생 모두 모두 환영합니다!\n" +
                                                "1년간 성실하게 활동하실 수만 있다면 주저하지 말고 지원하세요~ " +
                                                "참고로 6개월간 성실하게 활동한 회원에 대하여 몇 달간 휴회하는 것을 허용하고 있습니다.")
                                .build();
                clubs.add(Club.builder().name("ECHO").category(Category.STUDY).location("제2학생회관 308호").shortIntroduction("매주 월·목 영어회화 실력을 키우는 영어 스터디 동아리").introduction(intro15).recruitStart(LocalDateTime.of(2026, 3, 2, 0, 0)).recruitEnd(LocalDateTime.of(2026, 3, 13, 23, 59)).regularMeetingInfo("매주 월요일·목요일 18:15~19:20 (동아리방 및 강의실)").caution("구글폼 링크에 들어가시면 \"지원서류\"를 첨부해 두었습니다. 작성 후 제출하시면 됩니다! 면접: 3월 17일(화)부터 19일(목)까지 동아리방(제2학생회관 308호)에서 대면으로 진행합니다.").isInterviewRequired(true).isRegistered(true).everyTimeUrl("https://everytime.kr/418923/v/400660768").googleFormUrl("https://docs.google.com/forms/d/e/1FAIpQLSd8EBNbSSTgs6nmWIQhD_ilMcL3c5uNYdgNOFWqxJrXWeO8VQ/viewform").build());

                // 16. 어푸어푸 (수영)
                ClubIntroduction intro16 = ClubIntroduction.builder()
                                .overview("어푸어푸는 수영을 좋아하는 전남대, 광주교대 학생들이 모인 동아리인데요.\n" +
                                                "같은 취미를 가진 사람들이 모여 통하는 것도 많고, 멘토-멘티 프로그램을 통한 피드백으로 수영실력 향상이 가능합니다.")
                                .activities("Q. 수영은 어떻게 진행되나요?\n" +
                                                "A. 매주 토요일 4시~6시까지 광주 우산수영장에서 멘토링 프로그램과 기본적인 준비 수영 후에 진행합니다~")
                                .ideal("수영을 좋아하는 누구나, 전남대학교 재/휴학생, 대학원생")
                                .build();
                clubs.add(Club.builder().name("어푸어푸").category(Category.SPORTS).location("광주 우산수영장").shortIntroduction("전남대·광주교대 연합 수영 동아리").introduction(intro16).recruitStart(LocalDateTime.of(2026, 2, 20, 0, 0)).recruitEnd(LocalDateTime.of(2026, 3, 5, 23, 59)).regularMeetingInfo("매주 토요일 16:00~18:00 (광주 우산수영장)").caution("지원 및 모집방식: 알림아리 기간(3월 4~5일)\n직접 홍보부스로 찾아오시거나 모집기간 동안 구글 폼 제출하셔서 면접 시간을 잡으시면 됩니다.").isInterviewRequired(true).isRegistered(true).everyTimeUrl("https://everytime.kr/418923/v/401078194").googleFormUrl("https://form.naver.com/response/-rm-550f6VArzKzF5qYCKg").build());

                return clubRepository.saveAll(clubs);
        }

        private void seedApplyFormsAndQuestions(List<Club> clubs) {

                Map<String, Club> clubByName = clubs.stream()
                                .collect(Collectors.toMap(Club::getName, c -> c));

                List<ClubApplyForm> forms = new ArrayList<>();

                ClubApplyForm form1 = clubApplyFormRepository.save(
                                ClubApplyForm.builder()
                                                .club(clubByName.get("얼라이브"))
                                                .title("얼라이브 2025 상반기 모집")
                                                .description("사회문제 해결과 토론에 열정이 있는 분들을 모집합니다. 함께 배우고 실천하며 작은 변화를 만들어가요.")
                                                .build());
                forms.add(form1);

                ClubApplyForm form2 = clubApplyFormRepository.save(
                                ClubApplyForm.builder()
                                                .club(clubByName.get("과실연"))
                                                .title("과실연 2025 상반기 신입 부원 모집")
                                                .description("AI와 데이터 분석에 관심 있는 학생들을 환영합니다. 논문 스터디와 실전 프로젝트를 함께해요.")
                                                .build());
                forms.add(form2);

                ClubApplyForm form3 = clubApplyFormRepository.save(
                                ClubApplyForm.builder()
                                                .club(clubByName.get("소리터관현악단"))
                                                .title("소리터관현악단 2025 상반기 리쿠르팅")
                                                .description("프로그래밍 실력을 키우고 싶은 개발자 지망생을 모집합니다. 함께 공부하고 실제 서비스를 만들어봅시다.")
                                                .build());
                forms.add(form3);

                // 테크니션는 스포츠·스포츠테크 동아리로 변경된 버전에 맞게 수정
                ClubApplyForm form4 = clubApplyFormRepository.save(
                                ClubApplyForm.builder()
                                                .club(clubByName.get("테크니션"))
                                                .title("테크니션 2025 스포츠·스포츠테크 팀원 모집")
                                                .description("축구·농구 등 다양한 스포츠를 즐기며, 기록 측정과 스포츠테크에도 도전해보고 싶은 학생을 찾습니다. 함께 뛰고, 함께 데이터로 성장해요.")
                                                .build());
                forms.add(form4);

                // LIT:CH은 예술·디자인 재능기부 봉사 동아리로 수정
                ClubApplyForm form5 = clubApplyFormRepository.save(
                                ClubApplyForm.builder()
                                                .club(clubByName.get("LIT:CH"))
                                                .title("LIT:CH 2025 예술 재능기부 봉사단 모집")
                                                .description("벽화, 공간 꾸미기, 포스터 제작 등 예술·디자인 재능을 나누고 싶은 분을 모집합니다. 전공 무관, 그림과 디자인을 좋아하는 마음이면 충분해요.")
                                                .build());
                forms.add(form5);

                // DOVE는 음악 공연 봉사 동아리로 수정
                ClubApplyForm form6 = clubApplyFormRepository.save(
                                ClubApplyForm.builder()
                                                .club(clubByName.get("DOVE"))
                                                .title("DOVE 2025 음악 봉사팀 신입 모집")
                                                .description("보컬, 연주, 작곡 등 음악에 열정 있는 분을 찾습니다. 복지시설 공연과 자선무대를 함께 준비하며 음악으로 따뜻함을 나눠요.")
                                                .build());
                forms.add(form6);

                // 관현악반는 사진·영상 재능기부 봉사 동아리로 수정
                ClubApplyForm form7 = clubApplyFormRepository.save(
                                ClubApplyForm.builder()
                                                .club(clubByName.get("관현악반"))
                                                .title("관현악반 2025 사진·영상 봉사팀 모집")
                                                .description("사진과 영상으로 봉사 현장과 공익 캠페인을 기록하고 싶은 분을 모집합니다. 장비가 없어도, 배워보고 싶은 열정만 있으면 환영해요.")
                                                .build());
                forms.add(form7);

                ClubApplyForm form8 = clubApplyFormRepository.save(
                                ClubApplyForm.builder()
                                                .club(clubByName.get("아이디어스"))
                                                .title("아이디어스 2025 봉사활동 참가자 모집")
                                                .description("지역 아동센터 및 복지시설 봉사활동에 함께할 따뜻한 마음의 부원을 찾습니다. 꾸준히 함께할 분이라면 전공·학년 상관없이 환영합니다.")
                                                .build());
                forms.add(form8);

                ClubApplyForm form9 = clubApplyFormRepository.save(
                                ClubApplyForm.builder()
                                                .club(clubByName.get("미담장학회"))
                                                .title("미담장학회 2025 창업 아이디어팀 모집")
                                                .description("창업과 비즈니스에 관심 있는 학생들을 위한 리쿠르팅입니다. 함께 아이디어를 사업으로 발전시키고, 창업 경진대회에도 도전해봐요.")
                                                .build());
                forms.add(form9);

                ClubApplyForm form10 = clubApplyFormRepository.save(
                                ClubApplyForm.builder()
                                                .club(clubByName.get("PPP"))
                                                .title("PPP 2025 경제·금융 세미나 참여자 모집")
                                                .description("금융과 투자에 관심 있는 학생을 모집합니다. 모의투자와 시사 경제 세미나를 통해 함께 공부하고 분석해요.")
                                                .build());
                forms.add(form10);

                // 11. KUSA (유네스코 봉사)
                ClubApplyForm form11 = clubApplyFormRepository.save(
                                ClubApplyForm.builder()
                                                .club(clubByName.get("KUSA"))
                                                .title("KUSA 2026 상반기 신입 회원 모집")
                                                .description("전남대학교 유네스코학생회 KUSA에서 함께할 신입 회원을 모집합니다. 봉사와 국제교류에 관심 있는 분을 환영합니다.")
                                                .build());
                forms.add(form11);

                // 12. 전검회 (검도)
                ClubApplyForm form12 = clubApplyFormRepository.save(
                                ClubApplyForm.builder()
                                                .club(clubByName.get("전검회"))
                                                .title("전검회 2026 신입 단원 모집")
                                                .description("검도에 관심 있는 누구나 환영합니다. 경험 유무에 관계없이 함께 수련해요.")
                                                .build());
                forms.add(form12);

                // 13. 별따오기 (천문 관측)
                ClubApplyForm form13 = clubApplyFormRepository.save(
                                ClubApplyForm.builder()
                                                .club(clubByName.get("별따오기"))
                                                .title("별따오기 2026 신입 관측단 모집")
                                                .description("별과 우주에 관심 있는 분들을 모집합니다. 함께 밤하늘을 관측하며 우주의 신비를 탐구해요.")
                                                .build());
                forms.add(form13);

                // 14. SEA-FOX (스쿠버다이빙)
                ClubApplyForm form14 = clubApplyFormRepository.save(
                                ClubApplyForm.builder()
                                                .club(clubByName.get("SEA-FOX"))
                                                .title("SEA-FOX 2026 신입 다이버 모집")
                                                .description("스쿠버다이빙에 관심 있는 분이라면 누구든 환영합니다. 자격증 취득부터 정기 다이빙까지 함께해요.")
                                                .build());
                forms.add(form14);

                // 15. ECHO (영어 회화)
                ClubApplyForm form15 = clubApplyFormRepository.save(
                                ClubApplyForm.builder()
                                                .club(clubByName.get("ECHO"))
                                                .title("ECHO 2026 상반기 신입 회원 모집")
                                                .description("매주 월·목 영어 회화를 함께 연습할 신입 회원을 모집합니다. 영어 실력보다 열정이 중요합니다!")
                                                .build());
                forms.add(form15);

                // 16. 어푸어푸 (수영)
                ClubApplyForm form16 = clubApplyFormRepository.save(
                                ClubApplyForm.builder()
                                                .club(clubByName.get("어푸어푸"))
                                                .title("어푸어푸 2026 신입 회원 모집")
                                                .description("수영을 사랑하는 전남대·광주교대 학생들을 모집합니다. 멘토링 프로그램으로 함께 실력을 키워요.")
                                                .build());
                forms.add(form16);

                // ========== 6) FORM_QUESTION ==========

                // --- form1 (얼라이브: 사회문제 탐구) ---
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
                                .question("얼라이브에 지원하게 된 동기와 함께 깊이 탐구해보고 싶은 사회 문제를 구체적으로 작성해주세요.")
                                .fieldType(FieldType.TEXT)
                                .isRequired(true)
                                .displayOrder(3L)
                                .build());

                formQuestionRepository.save(FormQuestion.builder()
                                .clubApplyForm(form1)
                                .question("얼라이브 1차 면접 가능 시간대를 선택해주세요.")
                                .fieldType(FieldType.TIME_SLOT)
                                .isRequired(true)
                                .displayOrder(4L)
                                .timeSlotOptions(List.of(
                                                new TimeSlotOption(
                                                                "2025-10-15 ~ 2025-10-16",
                                                                new TimeSlotOption.TimeRange(
                                                                                LocalTime.of(10, 0),
                                                                                LocalTime.of(12, 0)))))
                                .build());

                // --- form2 (과실연: AI·데이터 연구) ---
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
                                                                                LocalTime.of(12, 0)))))
                                .build());

                // --- form3 (소리터관현악단: 프로그래밍·개발) ---
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
                                .question("소리터관현악단에서 만들고 싶은 서비스나 프로젝트 아이디어가 있다면 자유롭게 적어주세요.")
                                .fieldType(FieldType.TEXT)
                                .isRequired(false)
                                .displayOrder(3L)
                                .build());

                formQuestionRepository.save(FormQuestion.builder()
                                .clubApplyForm(form3)
                                .question("소리터관현악단 면접 및 OT 가능 시간대를 선택해주세요.")
                                .fieldType(FieldType.TIME_SLOT)
                                .isRequired(true)
                                .displayOrder(4L)
                                .timeSlotOptions(List.of(
                                                new TimeSlotOption(
                                                                "2025-10-15 ~ 2025-10-16",
                                                                new TimeSlotOption.TimeRange(
                                                                                LocalTime.of(10, 0),
                                                                                LocalTime.of(12, 0)))))
                                .build());

                // --- form4 (테크니션: 스포츠·스포츠테크) ---
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
                                .question("테크니션 OT 및 간단한 실기·적응 테스트가 가능한 시간대를 선택해주세요.")
                                .fieldType(FieldType.TIME_SLOT)
                                .isRequired(true)
                                .displayOrder(4L)
                                .timeSlotOptions(List.of(
                                                new TimeSlotOption(
                                                                "2025-10-15 ~ 2025-10-16",
                                                                new TimeSlotOption.TimeRange(
                                                                                LocalTime.of(10, 0),
                                                                                LocalTime.of(12, 0)))))
                                .build());

                // --- form5 (LIT:CH: 예술·디자인 재능기부 봉사) ---
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
                                .question("LIT:CH에서 해보고 싶은 예술 재능기부 활동(벽화, 교육, 디자인 제작 등)이나 개인적인 목표를 자유롭게 적어주세요.")
                                .fieldType(FieldType.TEXT)
                                .isRequired(false)
                                .displayOrder(3L)
                                .build());

                formQuestionRepository.save(FormQuestion.builder()
                                .clubApplyForm(form5)
                                .question("LIT:CH OT 및 인터뷰 가능 시간대를 선택해주세요.")
                                .fieldType(FieldType.TIME_SLOT)
                                .isRequired(true)
                                .displayOrder(4L)
                                .timeSlotOptions(List.of(
                                                new TimeSlotOption(
                                                                "2025-10-15 ~ 2025-10-16",
                                                                new TimeSlotOption.TimeRange(
                                                                                LocalTime.of(10, 0),
                                                                                LocalTime.of(12, 0)))))
                                .build());

                // --- form6 (DOVE: 음악·공연 봉사) ---
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
                                .question("DOVE에서 함께 만들고 싶은 봉사 공연 형태(복지시설 공연, 자선 콘서트 등)나 음악으로 전하고 싶은 메시지가 있다면 적어주세요.")
                                .fieldType(FieldType.TEXT)
                                .isRequired(false)
                                .displayOrder(3L)
                                .build());

                formQuestionRepository.save(FormQuestion.builder()
                                .clubApplyForm(form6)
                                .question("DOVE 오디션 및 면접 가능 시간대를 선택해주세요.")
                                .fieldType(FieldType.TIME_SLOT)
                                .isRequired(true)
                                .displayOrder(4L)
                                .timeSlotOptions(List.of(
                                                new TimeSlotOption(
                                                                "2025-10-15 ~ 2025-10-16",
                                                                new TimeSlotOption.TimeRange(
                                                                                LocalTime.of(10, 0),
                                                                                LocalTime.of(12, 0)))))
                                .build());

                // --- form7 (관현악반: 사진·영상 봉사) ---
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
                                .question("관현악반에서 제작해보고 싶은 공익/봉사 관련 사진·영상 콘텐츠(예: 봉사 현장 기록, 공익 캠페인 영상 등)가 있다면 적어주세요.")
                                .fieldType(FieldType.TEXT)
                                .isRequired(false)
                                .displayOrder(3L)
                                .build());

                formQuestionRepository.save(FormQuestion.builder()
                                .clubApplyForm(form7)
                                .question("관현악반 OT 및 인터뷰 가능 시간대를 선택해주세요.")
                                .fieldType(FieldType.TIME_SLOT)
                                .isRequired(true)
                                .displayOrder(4L)
                                .timeSlotOptions(List.of(
                                                new TimeSlotOption(
                                                                "2025-10-15 ~ 2025-10-16",
                                                                new TimeSlotOption.TimeRange(
                                                                                LocalTime.of(10, 0),
                                                                                LocalTime.of(12, 0)))))
                                .build());

                // --- form8 (아이디어스: 봉사·나눔) ---
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
                                .question("아이디어스 활동을 통해 이루고 싶은 목표나 기대하는 점을 자유롭게 작성해주세요.")
                                .fieldType(FieldType.TEXT)
                                .isRequired(true)
                                .displayOrder(3L)
                                .build());

                formQuestionRepository.save(FormQuestion.builder()
                                .clubApplyForm(form8)
                                .question("아이디어스 OT 및 활동 안내를 위한 만남이 가능한 시간대를 선택해주세요.")
                                .fieldType(FieldType.TIME_SLOT)
                                .isRequired(true)
                                .displayOrder(4L)
                                .timeSlotOptions(List.of(
                                                new TimeSlotOption(
                                                                "2025-10-15 ~ 2025-10-16",
                                                                new TimeSlotOption.TimeRange(
                                                                                LocalTime.of(10, 0),
                                                                                LocalTime.of(12, 0)))))
                                .build());

                // --- form9 (미담장학회: 창업·비즈니스) ---
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
                                .question("미담장학회에서 함께 구현해보고 싶은 창업 아이디어나 해결하고 싶은 문제를 구체적으로 적어주세요.")
                                .fieldType(FieldType.TEXT)
                                .isRequired(false)
                                .displayOrder(3L)
                                .build());

                formQuestionRepository.save(FormQuestion.builder()
                                .clubApplyForm(form9)
                                .question("미담장학회 면접 및 아이디어 피드백 세션 참여 가능 시간대를 선택해주세요.")
                                .fieldType(FieldType.TIME_SLOT)
                                .isRequired(true)
                                .displayOrder(4L)
                                .timeSlotOptions(List.of(
                                                new TimeSlotOption(
                                                                "2025-10-15 ~ 2025-10-16",
                                                                new TimeSlotOption.TimeRange(
                                                                                LocalTime.of(10, 0),
                                                                                LocalTime.of(12, 0)))))
                                .build());

                // --- form10 (PPP: 금융·투자) ---
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
                                .question("PPP에서 배우고 싶은 내용이나 스스로 세운 금융·투자 관련 목표를 구체적으로 적어주세요.")
                                .fieldType(FieldType.TEXT)
                                .isRequired(false)
                                .displayOrder(3L)
                                .build());

                formQuestionRepository.save(FormQuestion.builder()
                                .clubApplyForm(form10)
                                .question("PPP OT 및 면담 가능 시간대를 선택해주세요.")
                                .fieldType(FieldType.TIME_SLOT)
                                .isRequired(true)
                                .displayOrder(4L)
                                .timeSlotOptions(List.of(
                                                new TimeSlotOption(
                                                                "2025-10-15 ~ 2025-10-16",
                                                                new TimeSlotOption.TimeRange(
                                                                                LocalTime.of(10, 0),
                                                                                LocalTime.of(12, 0)))))
                                .build());

                // --- form11 (KUSA: 유네스코 봉사) ---
                formQuestionRepository.save(FormQuestion.builder()
                                .clubApplyForm(form11)
                                .question("간단한 자기소개와 함께 봉사활동 경험 및 KUSA에 지원하게 된 동기를 적어주세요.")
                                .fieldType(FieldType.TEXT)
                                .isRequired(true)
                                .displayOrder(1L)
                                .build());

                formQuestionRepository.save(FormQuestion.builder()
                                .clubApplyForm(form11)
                                .question("한 학기 이상 꾸준히 활동하실 수 있으신가요?")
                                .fieldType(FieldType.RADIO)
                                .isRequired(true)
                                .displayOrder(2L)
                                .options(List.of("예", "아니오"))
                                .build());

                formQuestionRepository.save(FormQuestion.builder()
                                .clubApplyForm(form11)
                                .question("가장 참여하고 싶은 활동을 선택해주세요.")
                                .fieldType(FieldType.RADIO)
                                .isRequired(false)
                                .displayOrder(3L)
                                .options(List.of("하계 해외봉사", "교육봉사", "플로깅", "소모임 활동"))
                                .build());

                // --- form12 (전검회: 검도) ---
                formQuestionRepository.save(FormQuestion.builder()
                                .clubApplyForm(form12)
                                .question("간단한 자기소개와 함께 검도 경험(유무, 기간 등)을 적어주세요.")
                                .fieldType(FieldType.TEXT)
                                .isRequired(true)
                                .displayOrder(1L)
                                .build());

                formQuestionRepository.save(FormQuestion.builder()
                                .clubApplyForm(form12)
                                .question("평일 도장 방문이 가능하신가요?")
                                .fieldType(FieldType.RADIO)
                                .isRequired(true)
                                .displayOrder(2L)
                                .options(List.of("예", "아니오"))
                                .build());

                formQuestionRepository.save(FormQuestion.builder()
                                .clubApplyForm(form12)
                                .question("전검회에 지원하게 된 동기를 자유롭게 적어주세요.")
                                .fieldType(FieldType.TEXT)
                                .isRequired(false)
                                .displayOrder(3L)
                                .build());

                // --- form13 (별따오기: 천문 관측) ---
                formQuestionRepository.save(FormQuestion.builder()
                                .clubApplyForm(form13)
                                .question("간단한 자기소개와 함께 천문 및 우주에 관심을 갖게 된 계기를 적어주세요.")
                                .fieldType(FieldType.TEXT)
                                .isRequired(true)
                                .displayOrder(1L)
                                .build());

                formQuestionRepository.save(FormQuestion.builder()
                                .clubApplyForm(form13)
                                .question("3, 5, 9, 11월 마지막 주 토요일 1박 2일 정기관측에 참여가 가능하신가요?")
                                .fieldType(FieldType.RADIO)
                                .isRequired(true)
                                .displayOrder(2L)
                                .options(List.of("예", "아니오"))
                                .build());

                formQuestionRepository.save(FormQuestion.builder()
                                .clubApplyForm(form13)
                                .question("별따오기에서 가장 해보고 싶은 활동을 자유롭게 적어주세요.")
                                .fieldType(FieldType.TEXT)
                                .isRequired(false)
                                .displayOrder(3L)
                                .build());

                formQuestionRepository.save(FormQuestion.builder()
                                .clubApplyForm(form13)
                                .question("면접 가능한 시간대를 선택해주세요. (3월 4일 ~ 6일)")
                                .fieldType(FieldType.TIME_SLOT)
                                .isRequired(true)
                                .displayOrder(4L)
                                .timeSlotOptions(List.of(
                                                new TimeSlotOption(
                                                                "2026-03-04 ~ 2026-03-06",
                                                                new TimeSlotOption.TimeRange(
                                                                                LocalTime.of(10, 0),
                                                                                LocalTime.of(18, 0)))))
                                .build());

                // --- form14 (SEA-FOX: 스쿠버다이빙) ---
                formQuestionRepository.save(FormQuestion.builder()
                                .clubApplyForm(form14)
                                .question("간단한 자기소개와 함께 스쿠버다이빙 또는 수영 경험(유무, 수준 등)을 적어주세요.")
                                .fieldType(FieldType.TEXT)
                                .isRequired(true)
                                .displayOrder(1L)
                                .build());

                formQuestionRepository.save(FormQuestion.builder()
                                .clubApplyForm(form14)
                                .question("계절별 정기 다이빙(국내 여행 포함)에 참여가 가능하신가요?")
                                .fieldType(FieldType.RADIO)
                                .isRequired(true)
                                .displayOrder(2L)
                                .options(List.of("예", "아니오"))
                                .build());

                formQuestionRepository.save(FormQuestion.builder()
                                .clubApplyForm(form14)
                                .question("SEA-FOX에 지원하게 된 동기와 기대하는 점을 자유롭게 적어주세요.")
                                .fieldType(FieldType.TEXT)
                                .isRequired(false)
                                .displayOrder(3L)
                                .build());

                formQuestionRepository.save(FormQuestion.builder()
                                .clubApplyForm(form14)
                                .question("대면 면접 가능한 시간대를 선택해주세요.")
                                .fieldType(FieldType.TIME_SLOT)
                                .isRequired(true)
                                .displayOrder(4L)
                                .timeSlotOptions(List.of(
                                                new TimeSlotOption(
                                                                "2026-03-01 ~ 2026-03-05",
                                                                new TimeSlotOption.TimeRange(
                                                                                LocalTime.of(10, 0),
                                                                                LocalTime.of(18, 0)))))
                                .build());

                // --- form15 (ECHO: 영어 회화) ---
                formQuestionRepository.save(FormQuestion.builder()
                                .clubApplyForm(form15)
                                .question("간단한 자기소개와 함께 영어 공부 경험 및 ECHO에 지원하게 된 동기를 적어주세요.")
                                .fieldType(FieldType.TEXT)
                                .isRequired(true)
                                .displayOrder(1L)
                                .build());

                formQuestionRepository.save(FormQuestion.builder()
                                .clubApplyForm(form15)
                                .question("매주 월요일·목요일 18:15~19:20 정기 수업에 꾸준히 참여가 가능하신가요?")
                                .fieldType(FieldType.RADIO)
                                .isRequired(true)
                                .displayOrder(2L)
                                .options(List.of("예", "아니오"))
                                .build());

                formQuestionRepository.save(FormQuestion.builder()
                                .clubApplyForm(form15)
                                .question("ECHO에서 향상시키고 싶은 영어 역량(회화, 발표, 면접 등)을 자유롭게 적어주세요.")
                                .fieldType(FieldType.TEXT)
                                .isRequired(false)
                                .displayOrder(3L)
                                .build());

                formQuestionRepository.save(FormQuestion.builder()
                                .clubApplyForm(form15)
                                .question("면접 가능한 시간대를 선택해주세요. (3월 17일 ~ 19일)")
                                .fieldType(FieldType.TIME_SLOT)
                                .isRequired(true)
                                .displayOrder(4L)
                                .timeSlotOptions(List.of(
                                                new TimeSlotOption(
                                                                "2026-03-17 ~ 2026-03-19",
                                                                new TimeSlotOption.TimeRange(
                                                                                LocalTime.of(10, 0),
                                                                                LocalTime.of(18, 0)))))
                                .build());

                // --- form16 (어푸어푸: 수영) ---
                formQuestionRepository.save(FormQuestion.builder()
                                .clubApplyForm(form16)
                                .question("간단한 자기소개와 함께 수영 실력(입문/중급/고급) 및 지원 동기를 적어주세요.")
                                .fieldType(FieldType.TEXT)
                                .isRequired(true)
                                .displayOrder(1L)
                                .build());

                formQuestionRepository.save(FormQuestion.builder()
                                .clubApplyForm(form16)
                                .question("매주 토요일 16:00~18:00 정기 수영에 참여가 가능하신가요?")
                                .fieldType(FieldType.RADIO)
                                .isRequired(true)
                                .displayOrder(2L)
                                .options(List.of("예", "아니오"))
                                .build());

                formQuestionRepository.save(FormQuestion.builder()
                                .clubApplyForm(form16)
                                .question("어푸어푸에서 기대하는 점을 자유롭게 적어주세요.")
                                .fieldType(FieldType.TEXT)
                                .isRequired(false)
                                .displayOrder(3L)
                                .build());

                formQuestionRepository.save(FormQuestion.builder()
                                .clubApplyForm(form16)
                                .question("면접 가능한 시간대를 선택해주세요. (알림아리 기간 3월 4~5일)")
                                .fieldType(FieldType.TIME_SLOT)
                                .isRequired(true)
                                .displayOrder(4L)
                                .timeSlotOptions(List.of(
                                                new TimeSlotOption(
                                                                "2026-03-04 ~ 2026-03-05",
                                                                new TimeSlotOption.TimeRange(
                                                                                LocalTime.of(10, 0),
                                                                                LocalTime.of(18, 0)))))
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

                // ===== 1. 얼라이브 =====
                apps.add(buildApp(userByStudentId, formByClubName, sid++, "얼라이브", Status.PENDING, Stage.INTERVIEW,
                                0.0));
                apps.add(buildApp(userByStudentId, formByClubName, sid++, "얼라이브", Status.PENDING, Stage.FINAL, 0.0));
                apps.add(buildApp(userByStudentId, formByClubName, sid++, "얼라이브", Status.APPROVED, Stage.INTERVIEW,
                                0.0));
                apps.add(buildApp(userByStudentId, formByClubName, sid++, "얼라이브", Status.APPROVED, Stage.FINAL, 0.0));
                apps.add(buildApp(userByStudentId, formByClubName, sid++, "얼라이브", Status.REJECTED, Stage.INTERVIEW,
                                0.0));
                apps.add(buildApp(userByStudentId, formByClubName, sid++, "얼라이브", Status.REJECTED, Stage.FINAL, 0.0));

                // ===== 2. 과실연 =====
                apps.add(buildApp(userByStudentId, formByClubName, sid++, "과실연", Status.PENDING, Stage.INTERVIEW,
                                0.0));
                apps.add(buildApp(userByStudentId, formByClubName, sid++, "과실연", Status.PENDING, Stage.FINAL, 0.0));
                apps.add(buildApp(userByStudentId, formByClubName, sid++, "과실연", Status.APPROVED, Stage.INTERVIEW,
                                0.0));
                apps.add(buildApp(userByStudentId, formByClubName, sid++, "과실연", Status.APPROVED, Stage.FINAL, 0.0));
                apps.add(buildApp(userByStudentId, formByClubName, sid++, "과실연", Status.REJECTED, Stage.INTERVIEW,
                                0.0));
                apps.add(buildApp(userByStudentId, formByClubName, sid++, "과실연", Status.REJECTED, Stage.FINAL, 0.0));

                // ===== 3. 소리터관현악단 =====
                apps.add(buildApp(userByStudentId, formByClubName, sid++, "소리터관현악단", Status.PENDING, Stage.INTERVIEW,
                                0.0));
                apps.add(buildApp(userByStudentId, formByClubName, sid++, "소리터관현악단", Status.PENDING, Stage.FINAL, 0.0));
                apps.add(buildApp(userByStudentId, formByClubName, sid++, "소리터관현악단", Status.APPROVED, Stage.INTERVIEW,
                                0.0));
                apps.add(buildApp(userByStudentId, formByClubName, sid++, "소리터관현악단", Status.APPROVED, Stage.FINAL, 0.0));
                apps.add(buildApp(userByStudentId, formByClubName, sid++, "소리터관현악단", Status.REJECTED, Stage.INTERVIEW,
                                0.0));
                apps.add(buildApp(userByStudentId, formByClubName, sid++, "소리터관현악단", Status.REJECTED, Stage.FINAL, 0.0));

                // ===== 4. 테크니션 (스포츠·스포츠테크) =====
                apps.add(buildApp(userByStudentId, formByClubName, sid++, "테크니션", Status.PENDING, Stage.INTERVIEW,
                                0.0));
                apps.add(buildApp(userByStudentId, formByClubName, sid++, "테크니션", Status.PENDING, Stage.FINAL, 0.0));
                apps.add(buildApp(userByStudentId, formByClubName, sid++, "테크니션", Status.APPROVED, Stage.INTERVIEW,
                                0.0));
                apps.add(buildApp(userByStudentId, formByClubName, sid++, "테크니션", Status.APPROVED, Stage.FINAL, 0.0));
                apps.add(buildApp(userByStudentId, formByClubName, sid++, "테크니션", Status.REJECTED, Stage.INTERVIEW,
                                0.0));
                apps.add(buildApp(userByStudentId, formByClubName, sid++, "테크니션", Status.REJECTED, Stage.FINAL, 0.0));

                // ===== 5. LIT:CH (예술 재능기부 봉사) =====
                apps.add(buildApp(userByStudentId, formByClubName, sid++, "LIT:CH", Status.PENDING, Stage.INTERVIEW, 0.0));
                apps.add(buildApp(userByStudentId, formByClubName, sid++, "LIT:CH", Status.PENDING, Stage.FINAL, 0.0));
                apps.add(buildApp(userByStudentId, formByClubName, sid++, "LIT:CH", Status.APPROVED, Stage.INTERVIEW,
                                0.0));
                apps.add(buildApp(userByStudentId, formByClubName, sid++, "LIT:CH", Status.APPROVED, Stage.FINAL, 0.0));
                apps.add(buildApp(userByStudentId, formByClubName, sid++, "LIT:CH", Status.REJECTED, Stage.INTERVIEW,
                                0.0));
                apps.add(buildApp(userByStudentId, formByClubName, sid++, "LIT:CH", Status.REJECTED, Stage.FINAL, 0.0));

                // ===== 나머지 9개 동아리: 각 1개 (PENDING + INTERVIEW) =====

                apps.add(buildApp(userByStudentId, formByClubName, sid++, "DOVE", Status.PENDING, Stage.INTERVIEW, 0.0));
                apps.add(buildApp(userByStudentId, formByClubName, sid++, "관현악반", Status.PENDING, Stage.INTERVIEW, 0.0));
                apps.add(buildApp(userByStudentId, formByClubName, sid++, "아이디어스", Status.PENDING, Stage.INTERVIEW,
                                0.0));
                apps.add(buildApp(userByStudentId, formByClubName, sid++, "미담장학회", Status.PENDING, Stage.INTERVIEW, 0.0));
                apps.add(buildApp(userByStudentId, formByClubName, sid, "PPP", Status.PENDING, Stage.INTERVIEW,
                                0.0));

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

                // Application 1 ~ 35 미리 로드 (1-based)
                Application[] apps = new Application[36];
                for (long i = 1; i <= 35; i++) {
                        long finalI = i;
                        apps[(int) i] = applicationRepository.findById(i)
                                        .orElseThrow(() -> new IllegalStateException(
                                                        "Application not found: " + finalI));
                }

                // FormQuestion 1 ~ 40 미리 로드 (1-based, 클럽 1~10의 질문)
                FormQuestion[] questions = new FormQuestion[41];
                for (long i = 1; i <= 40; i++) {
                        long finalI = i;
                        questions[(int) i] = formQuestionRepository.findById(i)
                                        .orElseThrow(() -> new IllegalStateException(
                                                        "FormQuestion not found: " + finalI));
                }

                List<Answer> answers = new ArrayList<>();

                // ========== 1. 얼라이브 (form1, questions 1~4) ==========
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
                                        .answer("혼자 고민하던 사회 문제를 구조적으로 이해하고, 비슷한 문제의식을 가진 사람들과 함께 토론하며 작은 행동으로 옮기고 싶어서 얼라이브에 지원했습니다.")
                                        .build());

                        answers.add(Answer.builder()
                                        .application(app)
                                        .formQuestion(questions[4])
                                        .answer("2025-10-15 10:00~12:00, 2025-10-16 10:00~12:00 모두 면접 참여 가능합니다.")
                                        .build());
                }

                // ========== 2. 과실연 (form2, questions 5~8) ==========
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

                // ========== 3. 소리터관현악단 (form3, questions 9~12) ==========
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
                                        .answer("소리터관현악단에서 풀스택 웹 서비스를 팀과 함께 기획하고 개발해 배포까지 경험해 보고 싶습니다. 코드 리뷰를 통해 좋은 개발 습관도 만들고 싶습니다.")
                                        .build());

                        answers.add(Answer.builder()
                                        .application(app)
                                        .formQuestion(questions[12])
                                        .answer("2025-10-15 10:00~12:00, 2025-10-16 10:00~12:00 모두 OT 및 면접 참석 가능합니다.")
                                        .build());
                }

                // ========== 4. 테크니션 (스포츠·스포츠테크, form4, questions 13~16) ==========
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

                // ========== 5. LIT:CH (예술 재능기부 봉사, form5, questions 17~20) ==========
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

                // ======== 6. DOVE (음악 봉사, form6, questions 21~24) – app31 ========
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

                // ======== 7. 관현악반 (사진·영상 봉사, form7, questions 25~28) – app32 ========
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

                // ======== 8. 아이디어스 (봉사·나눔, form8, questions 29~32) – app33 ========
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

                // ======== 9. 미담장학회 (창업·비즈니스, form9, questions 33~36) – app34 ========
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

                // ======== 10. PPP (금융·투자, form10, questions 37~40) – app35 ========
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
                                        .user(userArr[i])
                                        .club(clubArr[i])
                                        .application(null)
                                        .activeStatus(ActiveStatus.ACTIVE)
                                        .role(Role.CLUB_ADMIN)
                                        .build());
                }

                // ===== 15번 동아리(ECHO) 회장 – user 68 =====
                members.add(ClubMember.builder()
                                .user(userArr[68])
                                .club(clubArr[15])
                                .application(null)
                                .activeStatus(ActiveStatus.ACTIVE)
                                .role(Role.CLUB_ADMIN)
                                .build());

                // ===== 16번 동아리(어푸어푸) 회장 – user 69 =====
                members.add(ClubMember.builder()
                                .user(userArr[69])
                                .club(clubArr[16])
                                .application(null)
                                .activeStatus(ActiveStatus.ACTIVE)
                                .role(Role.CLUB_ADMIN)
                                .build());

                // ===== 15~49번 지원자(APPLICANT) =====
                for (int i = 15; i <= 49; i++) {
                        members.add(ClubMember.builder()
                                        .user(userArr[i])
                                        .club(apps[i - 14].getClubApplyForm().getClub()) // application으로부터 클럽 자동 매칭
                                        .application(apps[i - 14])
                                        .activeStatus(ActiveStatus.ACTIVE)
                                        .role(Role.APPLICANT)
                                        .build());
                }

                // ===== 54번: 시스템 관리자 (SYSTEM_ADMIN) =====
                members.add(ClubMember.builder()
                                .user(userArr[54])
                                .club(clubArr[1]) // 1번 동아리 (예: 얼라이브)
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
                // if (clubReviewRepository.count() > 0) return;

                List<ClubReview> reviews = new ArrayList<>();

                clubReviewRepository.saveAll(reviews);
        }

}
