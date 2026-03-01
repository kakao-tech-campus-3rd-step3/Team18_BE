package com.kakaotech.team18.backend_server.global.commandLineRunner;

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
import java.util.ArrayList;
import java.util.List;

@Component
@Order(1)
@RequiredArgsConstructor
@Profile({ "prod", "default" })
public class DataInitializer implements CommandLineRunner {

        private final ClubRepository clubRepository;
        private final ClubApplyFormRepository clubApplyFormRepository;
        private final FormQuestionRepository formQuestionRepository;
        private final ClubMemberRepository clubMemberRepository;
        private final ClubReviewRepository clubReviewRepository;
        private final FileDataRepository fileDataRepository;
        private final NoticeRepository noticeRepository;
        private final UserRepository userRepository;

        @Override
        @Transactional
        public void run(String... args) {
                if (clubRepository.count() > 0)
                        return; // 이미 데이터 있으면 전체 seed 스킵

                List<User> users = seedUsers();
                List<Club> clubs = seedClubsWithIntroAndImages();
                seedApplyFormsAndQuestions(clubs);
                seedClubMembers(users, clubs);

                // 새로 추가
                List<Notice> notices = seedNotices();
                seedFiles(notices);
                seedClubReviews(clubs);
        }

        private List<User> seedUsers() {
                List<User> users = new ArrayList<>();

                users.add(User.builder()
                                .kakaoId(10001L)
                                .email("unregistered01@jnu.ac.kr")
                                .name("오수현")
                                .studentId("100001")
                                .phoneNumber("010-7119-2484")
                                .department("미등록")
                                .build()); // 1번 동아리 회장: 관현악반

                users.add(User.builder()
                                .kakaoId(10002L)
                                .email("unregistered02@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100002")
                                .phoneNumber("010-6658-9019")
                                .department("미등록")
                                .build()); // 2번 동아리 회장: 뉴에라

                users.add(User.builder()
                                .kakaoId(10003L)
                                .email("unregistered03@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100003")
                                .phoneNumber("010-0000-0001")
                                .department("미등록")
                                .build()); // 3번 동아리 회장: 로터스

                users.add(User.builder()
                                .kakaoId(10004L)
                                .email("unregistered04@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100004")
                                .phoneNumber("010-0000-0004")
                                .department("미등록")
                                .build()); // 4번 동아리 회장: 맥킨토쉬

                users.add(User.builder()
                                .kakaoId(10005L)
                                .email("unregistered05@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100005")
                                .phoneNumber("010-9604-5714")
                                .department("미등록")
                                .build()); // 5번 동아리 회장: 메이플

                users.add(User.builder()
                                .kakaoId(10006L)
                                .email("unregistered06@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100006")
                                .phoneNumber("010-2654-5737")
                                .department("미등록")
                                .build()); // 6번 동아리 회장: 바이슨

                users.add(User.builder()
                                .kakaoId(10007L)
                                .email("unregistered07@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100007")
                                .phoneNumber("010-0000-0007")
                                .department("미등록")
                                .build()); // 7번 동아리 회장: 선율

                users.add(User.builder()
                                .kakaoId(10008L)
                                .email("unregistered08@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100008")
                                .phoneNumber("010-6611-5996")
                                .department("미등록")
                                .build()); // 8번 동아리 회장: 소리터관현악단

                users.add(User.builder()
                                .kakaoId(10009L)
                                .email("unregistered09@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100009")
                                .phoneNumber("010-0000-0009")
                                .department("미등록")
                                .build()); // 9번 동아리 회장: 열린만화창

                users.add(User.builder()
                                .kakaoId(10010L)
                                .email("unregistered10@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100010")
                                .phoneNumber("010-0000-0010")
                                .department("미등록")
                                .build()); // 10번 동아리 회장: 유스호스텔

                users.add(User.builder()
                                .kakaoId(10011L)
                                .email("unregistered11@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100011")
                                .phoneNumber("010-2190-6704")
                                .department("미등록")
                                .build()); // 11번 동아리 회장: 청불

                users.add(User.builder()
                                .kakaoId(10012L)
                                .email("unregistered12@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100012")
                                .phoneNumber("010-5614-1072")
                                .department("미등록")
                                .build()); // 12번 동아리 회장: 하이코드

                users.add(User.builder()
                                .kakaoId(10013L)
                                .email("unregistered13@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100013")
                                .phoneNumber("010-0000-0013")
                                .department("미등록")
                                .build()); // 13번 동아리 회장: Alchemy

                users.add(User.builder()
                                .kakaoId(10014L)
                                .email("unregistered14@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100014")
                                .phoneNumber("010-3780-1105")
                                .department("미등록")
                                .build()); // 14번 동아리 회장: CAST

                users.add(User.builder()
                                .kakaoId(10015L)
                                .email("unregistered15@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100015")
                                .phoneNumber("010-0000-0015")
                                .department("미등록")
                                .build()); // 15번 동아리 회장: Ctrl

                users.add(User.builder()
                                .kakaoId(10016L)
                                .email("unregistered16@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100016")
                                .phoneNumber("010-0000-0016")
                                .department("미등록")
                                .build()); // 16번 동아리 회장: LIT:CH

                users.add(User.builder()
                                .kakaoId(10017L)
                                .email("unregistered17@jnu.ac.kr")
                                .name("조주현")
                                .studentId("100017")
                                .phoneNumber("010-4224-4736")
                                .department("미등록")
                                .build()); // 17번 동아리 회장: SU:M

                users.add(User.builder()
                                .kakaoId(10018L)
                                .email("unregistered18@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100018")
                                .phoneNumber("010-5879-0079")
                                .department("미등록")
                                .build()); // 18번 동아리 회장: We‘z

                users.add(User.builder()
                                .kakaoId(10019L)
                                .email("unregistered19@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100019")
                                .phoneNumber("010-0000-0019")
                                .department("미등록")
                                .build()); // 19번 동아리 회장: ZOOM

                users.add(User.builder()
                                .kakaoId(10020L)
                                .email("unregistered20@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100020")
                                .phoneNumber("010-0000-0020")
                                .department("미등록")
                                .build()); // 20번 동아리 회장: 끄적끄적

                users.add(User.builder()
                                .kakaoId(10021L)
                                .email("unregistered21@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100021")
                                .phoneNumber("010-0000-0021")
                                .department("미등록")
                                .build()); // 21번 동아리 회장: 베이커스

                users.add(User.builder()
                                .kakaoId(10022L)
                                .email("unregistered22@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100022")
                                .phoneNumber("010-8109-5325")
                                .department("미등록")
                                .build()); // 22번 동아리 회장: 전대극회

                users.add(User.builder()
                                .kakaoId(10023L)
                                .email("unregistered23@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100023")
                                .phoneNumber("010-0000-0023")
                                .department("미등록")
                                .build()); // 23번 동아리 회장: CIC

                users.add(User.builder()
                                .kakaoId(10024L)
                                .email("unregistered24@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100024")
                                .phoneNumber("010-0000-0024")
                                .department("미등록")
                                .build()); // 24번 동아리 회장: W.B.C

                users.add(User.builder()
                                .kakaoId(10025L)
                                .email("unregistered25@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100025")
                                .phoneNumber("010-0000-0025")
                                .department("미등록")
                                .build()); // 25번 동아리 회장: 난파법학회

                users.add(User.builder()
                                .kakaoId(10026L)
                                .email("unregistered26@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100026")
                                .phoneNumber("010-0000-0026")
                                .department("미등록")
                                .build()); // 26번 동아리 회장: 랭스

                users.add(User.builder()
                                .kakaoId(10027L)
                                .email("unregistered27@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100027")
                                .phoneNumber("010-8999-3786")
                                .department("미등록")
                                .build()); // 27번 동아리 회장: 별따오기

                users.add(User.builder()
                                .kakaoId(10028L)
                                .email("unregistered28@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100028")
                                .phoneNumber("010-0000-0028")
                                .department("미등록")
                                .build()); // 28번 동아리 회장: 일본문화연구회

                users.add(User.builder()
                                .kakaoId(10029L)
                                .email("unregistered29@jnu.ac.kr")
                                .name("고건")
                                .studentId("100029")
                                .phoneNumber("010-8471-3003")
                                .department("미등록")
                                .build()); // 29번 동아리 회장: 일어회화반

                users.add(User.builder()
                                .kakaoId(10030L)
                                .email("unregistered30@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100030")
                                .phoneNumber("010-6223-8261")
                                .department("미등록")
                                .build()); // 30번 동아리 회장: 전대토스트마스터즈

                users.add(User.builder()
                                .kakaoId(10031L)
                                .email("unregistered31@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100031")
                                .phoneNumber("010-0000-0031")
                                .department("미등록")
                                .build()); // 31번 동아리 회장: 호버링

                users.add(User.builder()
                                .kakaoId(10032L)
                                .email("unregistered32@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100032")
                                .phoneNumber("010-7341-0526")
                                .department("미등록")
                                .build()); // 32번 동아리 회장: 흥사단

                users.add(User.builder()
                                .kakaoId(10033L)
                                .email("unregistered33@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100033")
                                .phoneNumber("010-0000-0033")
                                .department("미등록")
                                .build()); // 33번 동아리 회장: ECHO

                users.add(User.builder()
                                .kakaoId(10034L)
                                .email("unregistered34@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100034")
                                .phoneNumber("010-9430-7560")
                                .department("미등록")
                                .build()); // 34번 동아리 회장: ESU

                users.add(User.builder()
                                .kakaoId(10035L)
                                .email("unregistered35@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100035")
                                .phoneNumber("010-0000-0035")
                                .department("미등록")
                                .build()); // 35번 동아리 회장: F;ACT

                users.add(User.builder()
                                .kakaoId(10036L)
                                .email("unregistered36@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100036")
                                .phoneNumber("010-7641-6102")
                                .department("미등록")
                                .build()); // 36번 동아리 회장: 시퀀스

                users.add(User.builder()
                                .kakaoId(10037L)
                                .email("unregistered37@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100037")
                                .phoneNumber("010-7435-2388")
                                .department("미등록")
                                .build()); // 37번 동아리 회장: !DEAS

                users.add(User.builder()
                                .kakaoId(10038L)
                                .email("unregistered38@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100038")
                                .phoneNumber("010-0000-0038")
                                .department("미등록")
                                .build()); // 38번 동아리 회장: 오월빛

                users.add(User.builder()
                                .kakaoId(10039L)
                                .email("unregistered39@jnu.ac.kr")
                                .name("권민호")
                                .studentId("100039")
                                .phoneNumber("010-2777-2423")
                                .department("미등록")
                                .build()); // 39번 동아리 회장: 인액터스

                users.add(User.builder()
                                .kakaoId(10040L)
                                .email("unregistered40@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100040")
                                .phoneNumber("010-0000-0040")
                                .department("미등록")
                                .build()); // 40번 동아리 회장: SK LOOKIE

                users.add(User.builder()
                                .kakaoId(10041L)
                                .email("unregistered41@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100041")
                                .phoneNumber("010-0000-0041")
                                .department("미등록")
                                .build()); // 41번 동아리 회장: 에코노베이션

                users.add(User.builder()
                                .kakaoId(10042L)
                                .email("unregistered42@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100042")
                                .phoneNumber("010-8621-6245")
                                .department("미등록")
                                .build()); // 42번 동아리 회장: UNSA

                users.add(User.builder()
                                .kakaoId(10043L)
                                .email("unregistered43@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100043")
                                .phoneNumber("010-0000-0043")
                                .department("미등록")
                                .build()); // 43번 동아리 회장: 대학희망

                users.add(User.builder()
                                .kakaoId(10044L)
                                .email("unregistered44@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100044")
                                .phoneNumber("010-0000-0044")
                                .department("미등록")
                                .build()); // 44번 동아리 회장: 두드림

                users.add(User.builder()
                                .kakaoId(10045L)
                                .email("unregistered45@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100045")
                                .phoneNumber("010-0000-0045")
                                .department("미등록")
                                .build()); // 45번 동아리 회장: 미담장학회

                users.add(User.builder()
                                .kakaoId(10046L)
                                .email("unregistered46@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100046")
                                .phoneNumber("010-0000-0046")
                                .department("미등록")
                                .build()); // 46번 동아리 회장: 청사

                users.add(User.builder()
                                .kakaoId(10047L)
                                .email("unregistered47@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100047")
                                .phoneNumber("010-0000-0047")
                                .department("미등록")
                                .build()); // 47번 동아리 회장: 한사랑

                users.add(User.builder()
                                .kakaoId(10048L)
                                .email("unregistered48@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100048")
                                .phoneNumber("010-0000-0048")
                                .department("미등록")
                                .build()); // 48번 동아리 회장: ARC

                users.add(User.builder()
                                .kakaoId(10049L)
                                .email("unregistered49@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100049")
                                .phoneNumber("010-0000-0049")
                                .department("미등록")
                                .build()); // 49번 동아리 회장: ISF

                users.add(User.builder()
                                .kakaoId(10050L)
                                .email("unregistered50@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100050")
                                .phoneNumber("010-0000-0050")
                                .department("미등록")
                                .build()); // 50번 동아리 회장: KUSA

                users.add(User.builder()
                                .kakaoId(10051L)
                                .email("unregistered51@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100051")
                                .phoneNumber("010-0000-0051")
                                .department("미등록")
                                .build()); // 51번 동아리 회장: 과실연

                users.add(User.builder()
                                .kakaoId(10052L)
                                .email("unregistered52@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100052")
                                .phoneNumber("010-0000-0052")
                                .department("미등록")
                                .build()); // 52번 동아리 회장: 로타랙트

                users.add(User.builder()
                                .kakaoId(10053L)
                                .email("unregistered53@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100053")
                                .phoneNumber("010-0000-0053")
                                .department("미등록")
                                .build()); // 53번 동아리 회장: 가톨릭학생회

                users.add(User.builder()
                                .kakaoId(10054L)
                                .email("unregistered54@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100054")
                                .phoneNumber("010-9561-0940")
                                .department("미등록")
                                .build()); // 54번 동아리 회장: 마음쉬는곳

                users.add(User.builder()
                                .kakaoId(10055L)
                                .email("unregistered55@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100055")
                                .phoneNumber("010-0000-0055")
                                .department("미등록")
                                .build()); // 55번 동아리 회장: 전원회

                users.add(User.builder()
                                .kakaoId(10056L)
                                .email("unregistered56@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100056")
                                .phoneNumber("010-0000-0056")
                                .department("미등록")
                                .build()); // 56번 동아리 회장: CCC

                users.add(User.builder()
                                .kakaoId(10057L)
                                .email("unregistered57@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100057")
                                .phoneNumber("010-0000-0057")
                                .department("미등록")
                                .build()); // 57번 동아리 회장: COC

                users.add(User.builder()
                                .kakaoId(10058L)
                                .email("unregistered58@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100058")
                                .phoneNumber("010-0000-0058")
                                .department("미등록")
                                .build()); // 58번 동아리 회장: DFC

                users.add(User.builder()
                                .kakaoId(10059L)
                                .email("unregistered59@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100059")
                                .phoneNumber("010-0000-0059")
                                .department("미등록")
                                .build()); // 59번 동아리 회장: DSM

                users.add(User.builder()
                                .kakaoId(10060L)
                                .email("unregistered60@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100060")
                                .phoneNumber("010-0000-0060")
                                .department("미등록")
                                .build()); // 60번 동아리 회장: ENM

                users.add(User.builder()
                                .kakaoId(10061L)
                                .email("unregistered61@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100061")
                                .phoneNumber("010-2278-4106")
                                .department("미등록")
                                .build()); // 61번 동아리 회장: ESF

                users.add(User.builder()
                                .kakaoId(10062L)
                                .email("unregistered62@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100062")
                                .phoneNumber("010-0000-0062")
                                .department("미등록")
                                .build()); // 62번 동아리 회장: IVF

                users.add(User.builder()
                                .kakaoId(10063L)
                                .email("unregistered63@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100063")
                                .phoneNumber("010-8634-8193")
                                .department("미등록")
                                .build()); // 63번 동아리 회장: JDM

                users.add(User.builder()
                                .kakaoId(10064L)
                                .email("unregistered64@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100064")
                                .phoneNumber("010-5912-9765")
                                .department("미등록")
                                .build()); // 64번 동아리 회장: SFC

                users.add(User.builder()
                                .kakaoId(10065L)
                                .email("unregistered65@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100065")
                                .phoneNumber("010-0000-0065")
                                .department("미등록")
                                .build()); // 65번 동아리 회장: UBF

                users.add(User.builder()
                                .kakaoId(10066L)
                                .email("unregistered66@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100066")
                                .phoneNumber("010-0000-0066")
                                .department("미등록")
                                .build()); // 66번 동아리 회장: 기백

                users.add(User.builder()
                                .kakaoId(10067L)
                                .email("unregistered67@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100067")
                                .phoneNumber("010-3472-8650")
                                .department("미등록")
                                .build()); // 67번 동아리 회장: 당다라당

                users.add(User.builder()
                                .kakaoId(10068L)
                                .email("unregistered68@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100068")
                                .phoneNumber("010-0000-0068")
                                .department("미등록")
                                .build()); // 68번 동아리 회장: 블랙베어스

                users.add(User.builder()
                                .kakaoId(10069L)
                                .email("unregistered69@jnu.ac.kr")
                                .name("정주현")
                                .studentId("100069")
                                .phoneNumber("010-8590-7663")
                                .department("미등록")
                                .build()); // 69번 동아리 회장: 어푸어푸

                users.add(User.builder()
                                .kakaoId(10070L)
                                .email("unregistered70@jnu.ac.kr")
                                .name("강성영")
                                .studentId("100070")
                                .phoneNumber("010-6716-6807")
                                .department("미등록")
                                .build()); // 70번 동아리 회장: 얼라이브

                users.add(User.builder()
                                .kakaoId(10071L)
                                .email("unregistered71@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100071")
                                .phoneNumber("010-9186-0153")
                                .department("미등록")
                                .build()); // 71번 동아리 회장: 전검회

                users.add(User.builder()
                                .kakaoId(10072L)
                                .email("unregistered72@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100072")
                                .phoneNumber("010-0000-0072")
                                .department("미등록")
                                .build()); // 72번 동아리 회장: 전설

                users.add(User.builder()
                                .kakaoId(10073L)
                                .email("unregistered73@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100073")
                                .phoneNumber("010-5110-4034")
                                .department("미등록")
                                .build()); // 73번 동아리 회장: 태백회

                users.add(User.builder()
                                .kakaoId(10074L)
                                .email("unregistered74@jnu.ac.kr")
                                .name("김성현")
                                .studentId("100074")
                                .phoneNumber("010-2203-3863")
                                .department("미등록")
                                .build()); // 74번 동아리 회장: 테크니션

                users.add(User.builder()
                                .kakaoId(10075L)
                                .email("unregistered75@jnu.ac.kr")
                                .name("김한준")
                                .studentId("100075")
                                .phoneNumber("010-7256-1204")
                                .department("미등록")
                                .build()); // 75번 동아리 회장: DOVE

                users.add(User.builder()
                                .kakaoId(10076L)
                                .email("unregistered76@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100076")
                                .phoneNumber("010-8516-2425")
                                .department("미등록")
                                .build()); // 76번 동아리 회장: GRIP

                users.add(User.builder()
                                .kakaoId(10077L)
                                .email("unregistered77@jnu.ac.kr")
                                .name("신재민")
                                .studentId("100077")
                                .phoneNumber("010-8592-2324")
                                .department("미등록")
                                .build()); // 77번 동아리 회장: PPP

                users.add(User.builder()
                                .kakaoId(10078L)
                                .email("unregistered78@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100078")
                                .phoneNumber("010-5885-2869")
                                .department("미등록")
                                .build()); // 78번 동아리 회장: Sea-fox

                users.add(User.builder()
                                .kakaoId(10079L)
                                .email("unregistered79@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100079")
                                .phoneNumber("010-0000-0079")
                                .department("미등록")
                                .build()); // 79번 동아리 회장: 별하

                users.add(User.builder()
                                .kakaoId(10080L)
                                .email("unregistered80@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100080")
                                .phoneNumber("010-8506-7985")
                                .department("미등록")
                                .build()); // 80번 동아리 회장: 산악회

                users.add(User.builder()
                                .kakaoId(10081L)
                                .email("unregistered81@jnu.ac.kr")
                                .name("이름 미등록")
                                .studentId("100081")
                                .phoneNumber("010-3002-3106")
                                .department("미등록")
                                .build()); // 81번 동아리 회장: CNRC

                return userRepository.saveAll(users);
        }

        private List<Club> seedClubsWithIntroAndImages() {
                List<Club> clubs = new ArrayList<>();

                // ===== 1. 관현악반 =====
                ClubIntroduction intro1 = ClubIntroduction.builder()
                                .overview("전남대학교 관현악반(CNUO)은 47년의 역사를 가진 오케스트라 중앙동아리입니다. 정기연주회, 신입생 환영연주회, 교내외 공연을 통해 전남대를 대표하는 오케스트라로 활동합니다. 악기 초보자도 환영하며, 악기 대여도 지원합니다.")
                                .activities("정기연주회, 신입생 환영연주회, 교내외 공연, 파트별 개인 레슨 및 합주 연습을 진행합니다. 악기 대여 서비스도 제공합니다.")
                                .ideal("음악을 사랑하고 악기 연주에 관심 있는 학생이라면 누구든 환영합니다. 초보자도 전문가와 함께 성장할 수 있습니다.")
                                .build();
                intro1.addImages(List.of("https://cf-ea.everytime.kr/attach_thumbnail/406/80723512/everytime-1771673276409.jpg?Expires=1772367029&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=pego0UKLj8odvMkcfF7VdcRBJU5ju86rqXKJ8n5Pg8~1zWGEqKOfj34~sOQoJNqBM-1Acssg8WDvJ7hzn2dnbCahGotJRt5gpU8oEOcfcEpL79sNjLvCBDlJ0brxVVhy8PPkJV2oqj3FDcxp47nbO11wboF2zSLRY3RCT8xnDWrvNa5w4r0YZbB~LXG92ScM4jnPxefrUtq0gKs0NRf8jCB2NwGngNVgtsClpypgKeAlpA0MxXecHk1xnH~uaV-OoUyepXxLNkU6qud~xJ0f8xCa~4NtehkzcyNKgFxN8ZOHUnQqKBinXR7NRkJWEKq8y9uVj5mnS1k7Zzw9SqtSpw__", "https://cf-ea.everytime.kr/attach_thumbnail/856/80723513/everytime-1771673276847.jpg?Expires=1772367029&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=IgQk4LFNTjKgRmOWVtLK2J2hbdE-TpfV-3ArqwHhALqoYzgW-K4mAdyqqYIEGu8Jyy8e5i9TpW0ok-0m3PyjJy0-QNeMwUPYngiG-uxgJsihlI1KBabX-lVgSkOEuiWliCYrPxoeesKtNhj0QXuIeCVbkcn~kD5YXrP1V18lufj2MEvQtQ5rYZCMWDvhzU3BFgZeTEAYcCa1g0~sGAAwydTj6Dkluw0EpG6ffm1CL0a2WpTMJVMoADjpLSinZ-IMXYBsX0FQtcOi9Uz0paK5pZ-g4EQtK-~adXkpRCxuANXY175NPnUQdizv4oejpx-cfdzwGxmsebf9DIyXbpjGzA__", "https://cf-ea.everytime.kr/attach_thumbnail/357/80723514/everytime-1771673277167.jpg?Expires=1772367029&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=KIaKisZuhRgp0wcwqm8c-ZROmn6OxF-AiAXM59ay7McxQJLC8AcZfcx26qAVKPVIu37TTBU2DXzb8ndr-3uDSHsjVO-vNuZKmDAV~Dd92Npkyyyol-jpNOiQQ~ATrTjjH92UCuTRlEMYHV6PNSUROxuF~8uyCGBrRSxov1ZIPT49tmhWVwT5YY65HEtIx-ka8aRU4pxBY7QtX-n9ZxWYDfi3O1gfFUnhg~2u8kKrFAqOdsaSD5Ba~rDtavaZsEQ5srXWZVWyVpm9JuBsQv4XY2LqINLWpUCJSq8saM1riLCzRTwS617mrpjxVzEpfrhNXK4~ZBM4~XVZZ27WHnp~vQ__", "https://cf-ea.everytime.kr/attach_thumbnail/320/80723515/everytime-1771673277538.jpg?Expires=1772367029&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=JsfJLKS-sR0r9dI4HEPhDG4Q3zENhaZ8KDmdxTWv5KKqgE0xFEzLp5IQe7DPzTXfWxLQCcduszbgo-lNhKHUpanyEKpdpLoQT7AhHp3j-1q2mUoUL2~TlOPWbixohMeRB9MX0tlQwWMaRrJb7rqqEdvOhDzxtvNVeqOf92Ebu52xDn9Mbx3mDta7PXzhIN6zPh33jaOskA7jOcD8usJKM9GpNG~AGdxTJnsgI~ac-RS30AV7DxTJhrn4uEQyZMyhsX1LBj~fEbc5kICm7LUP9bLcrXomZ7otBx4FhE0EhBr7W0sl6ILzh1ixxBqT4XFu0b2hrTdKC1475jhDUDFZBg__", "https://cf-ea.everytime.kr/attach_thumbnail/446/80723516/everytime-1771673277888.jpg?Expires=1772367029&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=EttxI2DhZOdDpTgsJCzldmcPQX8Jsb9pleWMfnNMxoi6JaX0oNntqnJGYP-iK-CKdxjy8smffrDTD5wjD2RM7eKusOc-wqpxx36A1ukeL0d63qCDOR2B3bnuEOBqOU6gtvx8JZPRWbNQwyc1lxk~z5LjOGZ49FOBH0w819w4WcP3ZnwYxg9VZJH2Vp-cvQLFnxc6T3kXHsJk8XQB~t7FE4reKjMs~OpBXhdH1XIx6CFgABr8YpWWhPDRcHcNwrc0bwa9UAfm~1crM7KXbyT2qip48pxcyjVnDXlClC6~hbHMFDxhMptc3KqGwiuUR5rqHzmm2gxSn8asyqmAmCv4iA__"));
                clubs.add(Club.builder()
                                .name("관현악반")
                                .category(Category.LITERATURE)
                                .location("제1학생회관 307, 308호")
                                .shortIntroduction("47년 전통 오케스트라 동아리")
                                .introduction(intro1)
                                .recruitStart(LocalDateTime.of(2026, 1, 31, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 3, 6, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("정기 합주 및 연주회 참석은 필수입니다. 악기를 처음 다루는 경우 별도 면접 상담이 있을 수 있습니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl("https://everytime.kr/418923/v/401146276")
                                .googleFormUrl("https://litt.ly/cnuo")
                                .build());

                // ===== 2. 뉴에라 =====
                ClubIntroduction intro2 = ClubIntroduction.builder()
                                .overview("뉴에라는 힙합 댄스를 기반으로 활동하는 전남대학교 중앙동아리입니다. 부원들이 함께 연습하고 공연에 참여하며 실력을 키워나가는 동아리로, 초보자부터 숙련자까지 모두 환영합니다.")
                                .activities("힙합 댄스 정기 연습, 공연 기획 및 참가, 부원 간 콜라보 공연 등 다양한 댄스 활동을 합니다.")
                                .ideal("댄스에 열정이 있고 힙합 문화를 사랑하는 학생이라면 누구나 환영합니다.")
                                .build();
                intro2.addImages(List.of("https://cf-ea.everytime.kr/attach_thumbnail/488/80817600/everytime-1771842184641.jpg?Expires=1772367108&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=RvhpVW2W18Bi2gHeR2I2VBeetX4~H3QD8ENZgHkqPy1JrnYbMr372Znse51JxHSdsusrxYvo-OUSP2eCbCS9kQ6Ri3RU39kph--0byM4rbfLriLFc8kxP7xX63A5ALXAOqMT46Z~9oDv3IuiNtrwX2uhbKe7QMefJEPRNC4KG8e8joS57qzlzBqwb1QWZB-eSGf-ZfSXbSi6X7px9Dx9bj1ckC1wMx6TY3t0CZoJ1Xaa28XoqXe8cdQY0tO1VzzZ4vW4OTFwncJdLteXo8818BRDOEFYgQ62tFanW-Oq4FnCwSPSj1fp2pvGIMdm88Gs0RrowrzTA1q2dgR2Zvaumw__", "https://cf-ea.everytime.kr/attach_thumbnail/656/80817601/everytime-1771842185265.jpg?Expires=1772367108&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=Ccq12KaXTMgs7BeMTAIOA4YP~WTU7eoPeu7NB6itOM1UGKpGaw3jb8I~auyiibeOZs13Xx082sSat6kArHRRrNtM8nNIfok00SKyjT5MC74Fjb4ngptg0Qln9UfwxwYDmTvCaWpRYkqAjNajceVCzgYCvwk5vkJ8N60Z1E-zEhUDm6DuEgOQgC~ehqQzmYrtjUH1r-KVfsh4aVW1x7y5-1AkqQ-Zod65FUv8voIQyq3P8ARPWluM4fvEcQ3KvBzfVRM42tv1v2bYGNsFDpbURlHlMgdJpmxCIitC50S~DgoJJCjjK~WnSYLbZOko86UduEst-Obj1JzpCusVYn7Vyw__", "https://cf-ea.everytime.kr/attach_thumbnail/326/80817602/everytime-1771842185908.jpg?Expires=1772367108&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=UNSNPad9~H7VGv~xWx7BW50IDpJO87Gand8YX6scoZweDPbU7x4ZR4cnLxPbhIh1RYuss6851IoapEyarhdm85fUCHWPyardp13uT0aZbeOUKQ9nd3C-I8ImhTVylwDLUze1xnXV~UyqGPfnprXVZa0NeGnL4lmd7qgEdCzQE9hqwXu25~1qGjzqU4ZWjY3vopZZ8VZJMI8ecVOxfYwrOhISVqPsBh3RxMK7h6-9TL7KM-q~mgWqaXMMPJMH~hjaHCRDEExOpTfRpe16i~Lm5mUNfIG8lLtptjRxRcv3snENQKaTa2h6chXUkTU4JTgK0cq1F306zeDSj8ev6KnQTQ__", "https://cf-ea.everytime.kr/attach_thumbnail/992/80817604/everytime-1771842186554.jpg?Expires=1772367108&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=VjtmA3HQPiiuHUp4qjKSeg0-3Dob9oRLpj0VMEDHCjbyj0yav9NNZ5alk9652J2MiGXmPpjLYpwr8n-UDLJGRLQ8lVdjM9JRuSGpUdZ3qFyoYV8XZ0gmFmEG801KB6sv709WdL5IBFhONhAVZ-ADdfapbjOT6RgsBZBrg8oHV1g1Ffld1P~dDMJIQJeW~9Lnc8A7LhopZbGixPr2z4XqL3W9mVmTg5ZRSnZWXO53FZV~DsI40AbFaA8v5IW9WB476VUSqIzJckt~-U8rl0HzqxFtb2OcL97Z9PLlSVIDtV0ePU3JnozTzTp28EPJx2d7TB7QVwE9-i-jf0GqR-cVsQ__"));
                clubs.add(Club.builder()
                                .name("뉴에라")
                                .category(Category.LITERATURE)
                                .location("제1학생회관 430호")
                                .shortIntroduction("힙합 댄스 중앙동아리")
                                .introduction(intro2)
                                .recruitStart(LocalDateTime.of(2026, 2, 15, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 3, 9, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("정기 연습 참석이 필수입니다. 공연 일정은 사전에 공지됩니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl("https://everytime.kr/375208/v/401393894")
                                .googleFormUrl("https://everytime.kr/375208/v/401393894")
                                .build());

                // ===== 3. 로터스 =====
                ClubIntroduction intro3 = ClubIntroduction.builder()
                                .overview("로터스는 전남대학교의 사교댄스 동아리로, 왈츠·탱고·차차차 등 다양한 댄스 장르를 배우고 즐기는 동아리입니다. 정기적인 연습과 공연을 통해 함께 성장합니다.")
                                .activities("왈츠, 탱고, 차차차 등 사교댄스 정기 연습과 교내외 공연, 댄스 페스티벌 참가를 합니다.")
                                .ideal("사교댄스에 관심 있는 학생이라면 초보자도 환영합니다. 즐겁게 춤추고 싶은 모든 분을 기다립니다.")
                                .build();
                intro3.addImages(List.of("https://cf-ea.everytime.kr/attach_thumbnail/777/80987168/everytime-1772097122064.jpg?Expires=1772367169&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=prvTvuUTGejb4x8sXzyy0bgzU7csgesS1auAJ5EkPa18k1P~VAv4ok~Mu46bwGbd6MnmmUQnjOwiFkeBo7ftYNnzcFXeye8t9AN4rfBFUU1hJuCbbEpKpBnRw7dVVrviqdb1cBIK2IfzqYJYU6sTi8lZFSZ6wLZxgk8nIJ3fMKS5Lzn5cEo2T684K7YVghPicipQ6feUfjdPyITSZ7oJdv9pNWKWqFWtM0S8d-416X4JsWV~9Y9ozCq4~wxaHSrhG14OKcbr0dJcGsEjyQpZlnyS-cE-kJ3ssFpcHleOwbQcupU6CWqIum0XQ2Y~wwzg05K97s9e-Y2zJNsl2wYG-A__", "https://cf-ea.everytime.kr/attach_thumbnail/676/80987170/everytime-1772097122754.jpg?Expires=1772367169&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=qvXnqBuN~6PzJBIR8aoTmy6Ok8Boo4ETrYgWJPI9INgdLZhz~0ZwWp-nLuwIY7diKK3APw~jNiYcnJtjTTy8t1IUEjUwfg-1l2U0HoBTA1IOD9nT01rTR-hASRMsUnGuFWvnv3kMdSZk0a3F5UbGca4VPJSsksYjiRPz-ItsyC8mEQGF-3cAe-UClZngGj1NbaUhxw181Saj4sGt8wdyok-XHt-Lgl5BiGeYAsqgvG8o5a7hglJ7TcUEQDOlKxk2SsSMHe2yH8Fx~yUQ8ANheHFbqIftqgR3-YTTqyIJ22BX5J0Gobwnt2bFnJX3LZtQ9zQA6OWRunAZ6x-8NM89dw__", "https://cf-ea.everytime.kr/attach_thumbnail/718/80987171/everytime-1772097123234.jpg?Expires=1772367169&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=L9Cs5ewA5faPVd6r26SBbGSyq1GklvrFVwS33-rFwLcWb8xI-n5ICqUre5W7crHJ7Hyft4O3qgmB4b3cBIo0LVJx1PHLRF0pi2llKcP~wFZdqhF5ZzTCjeK-pLtkCMBSRXp6XJwhKUPvOAy33D0Ovi3AzYJPlUQzb59dYODIPCrwX0e6RW7amnrbNyAslXch0AP7-oP45lDa5W~n58L93b4Jhb-OvTujCDDWQcP1F9HDcaYj4~t30Q3ZeGrRuBmc7NBKJg42gzV3HLhrxRJmVwj-WJuu~AxUqZHHC2~7POfJmVz7jDCqGhmBWaBCujbbQnBvf7uqIXvxzEwwI5fOBA__", "https://cf-ea.everytime.kr/attach_thumbnail/281/80987172/everytime-1772097123791.jpg?Expires=1772367169&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=Z4I~--gNxEBtZCbs4bcbaGjQv3Trg77jlzjgUnpufttElurIUVWd6lnPKJiMjyR7VtAmlKKlqlPD2E2zGpgigL2cBsgeloq7y7vBAHMlTYoEjMVjFn7PPc1m~c~Fw1QtM1zA2k3CEE0MBIWfD1KMJq0MtM97XBIbvrSCxVJ~8nq~ie~UH613oLwwTnP7xQ6oz6Mdww1Yo5rt5EdcV8--48u4yv44pDXDi9sKytUD1mWOn~jAGjY8FZ--dwmtAT6EcOOeQX1lNjQBrTXkalLa1icOkM7hl6RW9G-kOBWrbNZct7Kw6e671pvmSqTeC7kCEEv-GsaTOjYSEbj5~B9CNQ__", "https://cf-ea.everytime.kr/attach_thumbnail/964/80987175/everytime-1772097124313.jpg?Expires=1772367169&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=jychqKxt8xr61zSxnoTfgZGO~9GbWBhJKo57IWWGePBmUC0B9tg8~Y3VxKlkh9cI4VRBh7qs8mHZd3DhEvsALogpHP8pgAnxaNODoHgXe7nKrDkhxSTiHU8NgVj5JJmTz~tYn4bXOiub4Ot9apt6gzJqr2zD01-If8BYLVLHtziWPbN5eSGVYsHSoFdynaDl353iRh8B14aOsSyUy2mYjrF~XIHwItdwoSp2dfQel81qTCZewixu-hP4HzCHKOtFjy1o82frhGy1XBbeSF8CYSb9HURzmLfh9-YDnNwMQfRwWhbarHGfOEOtI7GGLj7xu~Maz7sC-zBceoN~n292Kg__", "https://cf-ea.everytime.kr/attach_thumbnail/110/80987177/everytime-1772097124746.jpg?Expires=1772367169&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=awsZWFdSsfEKcmSRhdi8v3ZjiMnoC4EHeLaI-YUWp7JnMCEYLKuhs8kBWbFeWSFdRPJhFVQBsh89dqB3PThUyZ1LcWAkAEvjQODDNMMI53cMG9hpbo-EJ7o3xTEptsFwV6xGV1ZYjxgHn5aRqeltmRZTwUGlg9yad9Z1ZGAhSn7KYwBwyMjhGbMLb47DX3Ade0o44mh9Mlf-fhEzYLg53HBT-i~97ej6Km3SAvvK7ELldQofGx13uhqZUhMDX5urhYkP1Fclbf0yEOOwjI2A9dkZcbAweylJWV0hWP~Ln4NeU~OHyQlNDdLo0v2RukHvfurrGlrX5m9LnYfNDsqkpw__"));
                clubs.add(Club.builder()
                                .name("로터스")
                                .category(Category.LITERATURE)
                                .location("제1학생회관 B101호")
                                .shortIntroduction("사교댄스 전문 동아리")
                                .introduction(intro3)
                                .recruitStart(LocalDateTime.of(2026, 2, 26, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 3, 8, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("정기 연습에 성실히 참여해야 합니다. 공연 참가는 연습 출석률에 따라 결정됩니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl("https://everytime.kr/544806/v/401919016")
                                .googleFormUrl("https://open.kakao.com/o/sfIVEc9h")
                                .build());

                // ===== 4. 맥킨토쉬 =====
                ClubIntroduction intro4 = ClubIntroduction.builder()
                                .overview("맥킨토쉬는 Mac 컴퓨터를 기반으로 미디어 창작 활동을 하는 전남대학교 중앙동아리입니다. 영상, 음악, 그래픽 등 다양한 미디어 콘텐츠를 제작합니다.")
                                .activities("영상, 음악, 그래픽 등 Mac 기반 미디어 창작 워크숍과 콘텐츠 발표회를 진행합니다.")
                                .ideal("Mac을 활용한 창작 활동에 관심 있는 학생이라면 누구나 환영합니다.")
                                .build();
                intro4.addImages(List.of("https://cf-ea.everytime.kr/attach_thumbnail/180/74246790/everytime-web-1741417723025.jpg?Expires=1772367764&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=HtnCiF-tgTTKLSwDWK3vr5LtH8ewWzOqWeZALUESR1llFzEPGPcXqDL6sTmF6UkiO~IGfnlAvHdzZXF7il1yjDufOR69vkGb-0o1pO3Rerme8kKrXdKx-JgQSuL8008tLhtrQld4eDzjM4QWVeyCjhof4coJzYskAjATRVXze6Ei7hiFjJTONZLz9IrZQbMJxLPVdsU3NXCWqDnIxGORNtqutHkl72NpHMiTZY1neNLlPJ6cMzXUfHSOsXhgBDSnm9HfT1qRwqBY-KzUWqfryeuY0qJkSeP66SSVMAQsVaaV-PBuTW~aRDTKOvEiGYiX0IlWhyxd8DTEYIA3S4AWZA__", "https://cf-ea.everytime.kr/attach_thumbnail/306/74246792/everytime-web-1741417723027.jpg?Expires=1772367764&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=DTQpUG6kBiAkjATSgBJLh8xM72cYHsD5QYxZ5lthHlRIWqEt6ksP40q7CLM2JInUN-XY5US5eIR19d761Q-ZthyyqdhhHdrmhEdxiScBLwusY-aUSswRAnQjBhBR0KdR2grLfH0KdKAEjfcHbM~5~5us5finkcVVuln12L32ruIrMz54~WziHPfcDGG427kKWQoQ4BkuXfccOQnuKF6pLC-2DAwSPXvkzXtHJnHW2W0CwKyslDIjRStqAX5otzUg8yfnlXC97~PJ7lIzfFh3qOqGpDE8aQhyyTq2LTDgalyVQiKfhYayGlBRucnlXkhXS60Qyhv0jsEZzeAXvImCYQ__", "https://cf-ea.everytime.kr/attach_thumbnail/555/74246793/everytime-web-1741417723028.jpg?Expires=1772367764&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=dpIY54jVitp--~rkQETJrhs-vg2xmqfUkZH6aS5IVmxQ7AJX~itZHXtq-W2S9l-HZ7VKaj9IPn8XlfyIKlD6gavR7~XR8JkHMG9iCyB~JEmb5g4IcEH086ss5kPyPnjTqIbW049rFPHUeOykwOZ0xI3Njf8mCquu0yf1ANWEr6TEe93Y5GHgaGLlZFB6TChl6jathzw4Ari7i~vr75jOnRM~irRoV0XBhp7pLzynYKBON7T7SXu04QTlBRpVWbrMFemsYhy2m87U8-N9kpK0yvs9i2XL73B3lm6zgrvp~gF~7dchPVmlYA06dIKv3SAwChK6sAevGbsDR6ylq8suOg__", "https://cf-ea.everytime.kr/attach_thumbnail/29/74246794/everytime-web-1741417723030.jpg?Expires=1772367764&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=l3sVboTtO9T1W33cYwBfk4vbun~jrMbZwzvSmcn3tjvioGDH0g3VsV5LiXiyqgNJR9MnB3jt9HWO3EUoXGsCYR79XIXDp2V1XCSZTOH1hR8mVrupBRUvBjxnK-FUIMf~XvQAKBwgPuoutfU-DkurlQL7en3zpPyqewaukDIJkMXbubR9~CMXBfVjGYfTw791crC5JOtn9nyAxvuk33gOwR2PQ9JLy-~DyjW2M7cAkt-V0jzyzs2GuqD7HB9YzJrFnIzpiyR4AxUlw8db2pWL76~zPJmcOdjqZfHO8krWkB6CnG9ScPc96SqnQoiyVF4Lt1IlVksvapznDoDC9inB-g__", "https://cf-ea.everytime.kr/attach_thumbnail/901/74246795/everytime-web-1741417723033.jpg?Expires=1772367764&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=cCwiI6qUC4kH~xaMn00WPSu8m8k5emRG7KM7pVw96bMscQ7LzhVgNo0be4EjaiVuZBKd9Wp9BmOl5WJZV116HcG1E2eFVatPoSggz7GfoO3SCWDmnmZcgye7l8dSJ6i2sr7MrT7t2DVNaYAsV9fRT6SJUBPjCB6ScPM-YpoRmY1P9faS9LpkdJ5oJIruFxEQciEqifRNkW~dRCGxiHDDzG6ZiUGnke~rKBNm0dZDH96jgwwQqzH-7C-pAx7gFEFwP4yGS1LZAQVfcKvfgIuSmIX9pOTGf0JJPtun-OTODW5FAQd0gL7iBEIjR4RmrHnagLzEVENCPYRbWe9vZg~bFQ__", "https://cf-ea.everytime.kr/attach_thumbnail/777/74246797/everytime-web-1741417723035.jpg?Expires=1772367764&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=GCzvdBIM35UWLa2at-qZGCyTFYpBKczK8vthT5qmv~Uyqz18YjdN8PXSFzp-d5puoXGATLQfKV~0-V4e31~b45DTdoNumXtxPRp3p~wy~oz4bJtrVXdYmGLevs6tjMYrVRauM0FM-WkHTRkH-B8CRodj3OvvNuJsFFxjzDrPo8P8-Pij~WFXt7yNzLLBxh-SJu-5Jv3MXeK1qCeMtNe9rINuIDZrfyAAnMyw8FkjYr6mZuz8kqQ7MSPaIuV9yT6IbZ99pfSfEO00GLgor64Qe8ldP-JkvopmvPzMc7fBYCDbmnuQyknfoNbqiF5VpS026XmS9ODQsg5WYqm18AOMew__"));
                clubs.add(Club.builder()
                                .name("맥킨토쉬")
                                .category(Category.LITERATURE)
                                .location("용지동아리관 204호")
                                .shortIntroduction("Mac 기반 미디어 창작 동아리")
                                .introduction(intro4)
                                .recruitStart(LocalDateTime.of(2025, 2, 24, 0, 0))
                                .recruitEnd(LocalDateTime.of(2025, 3, 8, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("정기 모임 참석이 필수이며, 창작 활동에 적극적으로 참여해야 합니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl("https://everytime.kr/544806/v/372591729")
                                .googleFormUrl(null)
                                .build());

                // ===== 5. 메이플 =====
                ClubIntroduction intro5 = ClubIntroduction.builder()
                                .overview("메이플은 전남대학교의 실용음악 밴드 동아리입니다. 기타, 베이스, 드럼, 보컬 등 다양한 악기를 연주하며 함께 음악을 즐깁니다.")
                                .activities("기타, 베이스, 드럼, 보컬 합주 연습과 정기 공연, 밴드 세션 활동을 합니다.")
                                .ideal("음악을 좋아하고 밴드 활동에 관심 있는 학생이라면 누구나 환영합니다.")
                                .build();
                intro5.addImages(List.of("https://cf-ea.everytime.kr/attach_thumbnail/706/80895204/everytime-1771978470043.jpg?Expires=1772367920&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=fQET31hSRyTYK0wT6C9MySif4nF-ArOKeyIlbp1DSySoMZfxlKRQYwWChPmVPaX-nqdgFkEaH2lPyQz9hiGoZ20k4~BNuF0pKbR6P4UD9KXJHror9vemh8fSwhMV1K0u4BZPHayGDSEUl~agDp4gOjszM9Mw0CZ-nGU~AOxgY~yzqNCMBMF6CPf8qKiIy4ebmQxcTVeQJA9t8P-iv4GzltXbyp2xXQl4BB-NYM85qAGOv5uZYdpCo348YsC7n4BL79DP77JWbzNxhumBvhm-WbAhCferbJelqPV7FaPa8OUWNJzYKZrHWNATUciD5fJCcP5R-mqT8uOCQ4xtwEHEVg__", "https://cf-ea.everytime.kr/attach_thumbnail/108/80895205/everytime-1771978471002.jpg?Expires=1772367920&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=KU8PoS3IV~tMlpRF0xWTjudq6xw651F~jPJBsx5jNO~ZLSOboUtR99rGEJtVFA2vSJnymQYU0RjJhDa7cn5P6IdlZnWT6aepsqbdH3NSz0y5NCYDGtOnS8xyMDMjiMAvi9cma-J1Mx-DKFGx4aCRhrXISpyky~RUXetkloaXcquV-iELNnMqHyJ16Bz8na-hJ4Apn1IKssJ0S-futmj-WDj7HglWGoP7FnkcEu~mCIyL5UDDRWN4h7V3AKdnJKYPtMaNAvlHtfNNWXBKtNkgQg9-MvhCAbQm0oQUMPRefHN8em6HO93n8SfO2xPQoWe6DsXQtDCg2ICqzWWetbqAxA__", "https://cf-ea.everytime.kr/attach_thumbnail/166/80895206/everytime-1771978471401.jpg?Expires=1772367920&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=T7vhAhWMQ6MbzV9aS1ruXlMZRDrO3~IIToBm1shhC5ZQ7H4srwk5LkZMo-C-wEwrwQxt9sPeM~K21m4Re5kBSSBYnOhYRFC4MqUKphGfEIJ5IdCzSp3WmZNBuzIssLpL24-lCgq7SeyHNpjZqbhJyyFRoulIzwycvqdeWIypT2sugJLfTRp0IfVceGYYwMUqOeMYzeUW7hcuMDINWysXuxBnwwtJyhzpkU~d-3oTagZM-Snk28KU2VRbs4o3N-rJM4nor3Xy0JdLdhTE761zoU3ivBpAFiIDtppPMfKONIIpxtBZqcQ6FnNgRZ9TPSBM2p~5F7URJKOyB3hMe-FHHQ__"));
                clubs.add(Club.builder()
                                .name("메이플")
                                .category(Category.LITERATURE)
                                .location("용지동아리관 102호")
                                .shortIntroduction("실용음악 밴드 동아리")
                                .introduction(intro5)
                                .recruitStart(LocalDateTime.of(2026, 1, 1, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 12, 31, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("합주 연습 참석이 중요합니다. 악기 연주 경험이 있으면 좋습니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl("https://everytime.kr/418923/v/401647924")
                                .googleFormUrl("https://open.kakao.com/o/sbjDwcii")
                                .build());

                // ===== 6. 바이슨 =====
                ClubIntroduction intro6 = ClubIntroduction.builder()
                                .overview("바이슨은 전남대학교의 비보이 및 스트릿댄스 동아리입니다. 다양한 스트릿 댄스 장르를 익히고 공연 활동을 통해 끼와 열정을 발산합니다.")
                                .activities("비보이, 힙합, 팝핀 등 스트릿 댄스 연습과 공연 참가, 배틀 활동을 합니다.")
                                .ideal("스트릿 댄스를 사랑하고 무대에 서고 싶은 학생이라면 환영합니다.")
                                .build();
                intro6.addImages(List.of("https://cf-ea.everytime.kr/attach_thumbnail/910/74205975/everytime-1741335380182.jpg?Expires=1772366043&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=WnK0pw2mTo29cZvk1rnmsP8s1I3F0Uw2hsDgd-cJ~fN25PF6VPSj~Tu0y-7RR~GWif8nEczKslFewA5aYLeQlYdE5UfSe1-cm5agXkLiiSX0LnEm34~FjHDSl6iQbcCALEhSCmFkVvx8pZ~o2XrthWa8BP2GEv9CCSu6rrMptYWz6uM8eOHsZkd8ImYQ88JlncPfASJCS17LxHH2TxD7OL9MMSH41diH4ZqzcoysyqT0ZJic7SEN8Cavf7BSaNBCHEOmUMJp~AUK46LlM8lDNHx2WMz7phaf4clWU7qE1F3wKXzFiFIUFPQ~6ckbMgoq7zuc96llM-oV-m59-Xipkw__"));
                clubs.add(Club.builder()
                                .name("바이슨")
                                .category(Category.LITERATURE)
                                .location("제1학생회관 B102호")
                                .shortIntroduction("비보이 스트릿댄스 동아리")
                                .introduction(intro6)
                                .recruitStart(LocalDateTime.of(2026, 1, 1, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 12, 31, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("정기 연습에 성실히 참여해야 합니다. 무대 참여는 연습 출석을 기반으로 합니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl("https://everytime.kr/375210/v/372444469")
                                .googleFormUrl(null)
                                .build());

                // ===== 7. 선율 =====
                ClubIntroduction intro7 = ClubIntroduction.builder()
                                .overview("선율은 전남대학교의 클래식 기타 동아리입니다. 클래식 기타 앙상블 연주와 개인 연주 기술 향상을 목표로 정기적인 합주 연습과 공연을 진행합니다.")
                                .activities("클래식 기타 앙상블 정기 합주와 솔로 연주 연습, 교내외 연주회를 진행합니다.")
                                .ideal("클래식 기타를 배우고 싶거나 이미 연주하는 학생 모두 환영합니다.")
                                .build();
                clubs.add(Club.builder()
                                .name("선율")
                                .category(Category.LITERATURE)
                                .location("용지동아리관 201호")
                                .shortIntroduction("클래식 기타 앙상블 동아리")
                                .introduction(intro7)
                                .recruitStart(LocalDateTime.of(2026, 2, 26, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 3, 15, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("정기 합주에 참석해야 합니다. 기타 입문자는 별도 기초 레슨이 제공됩니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl("https://everytime.kr/544806/v/401954999")
                                .googleFormUrl("https://form.naver.com/response/RpPjvrmMKdA0AAP7gMxdLQ")
                                .build());

                // ===== 8. 소리터관현악단 =====
                ClubIntroduction intro8 = ClubIntroduction.builder()
                                .overview("소리터관현악단은 전남대학교의 현악 오케스트라 동아리입니다. 바이올린, 비올라, 첼로 등 현악기를 중심으로 앙상블을 이루며 정기 연주회를 개최합니다.")
                                .activities("현악 앙상블 정기 합주, 연주회 개최, 파트별 레슨 지원을 합니다.")
                                .ideal("현악기를 연주하고 함께 앙상블을 이루고 싶은 학생이라면 환영합니다.")
                                .build();
                intro8.addImages(List.of("https://cf-ea.everytime.kr/attach_thumbnail/566/81095516/everytime-1772283106399.jpg?Expires=1772368042&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=qxjoP5NaF4SSCM~LCrXMuNvPnl1~YCVvNIr-eXTuyF2~YAMTdXmtpgahhkMCvdjv19dpVHfO6FYkVXh-fXMf-mdNYR9ZpSKHD7vtDpf7o2AQRgA-jK~KCmf5yX0FqpSSfGh-kaz2H3XNzRepCrekdigNXgbXgVhEVrF72U~BPdYDsa5a8eExcSnIjTYDxUsLU6Bg5REEqGMR0PuDZdIUCqBTQMjJGUjVePa7pum3uMMUQBGvILkRqa84TZZglY0C275OnhjeJNa9GZq2faBT5BlNEuPKnet-HpzR7EfUp~gOkehAtsMzrry1Cj-d1oJsPeAd8KSdy9ecmgn7KiRJVw__", "https://cf-ea.everytime.kr/attach_thumbnail/365/81095518/everytime-1772283106938.jpg?Expires=1772368042&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=aws9YjeuQHU97NKdOXzDUNk6R5BZ~45oBmQiH1He5HCudEKRTRMDT31oRNr1UUF8LAhnvwRCyTWp0qZOFpEtN-I4me2FXp2KYaprFKDTgjXSWwajivwsxns4zewUjs0nM5nKTaUkAu5FKPOGnZ~DiiKfaXPCUPaWP2riGuraFYdbfjRqppYnAWYZP77Yyjd5OHPdPRo4PuSVUaZfyZi0MIKBavnp0CNEf4IBsLKRuium9fPuDY6WPMUjzV~nPzgkRAW1M84N2lzxKJBfvm-cQZx-J-fPE~ZcKzjqG~6L2R5~e-u26mHwI-PAxeKWx2~7COVcWfvL~HN2kz9ztu4StQ__", "https://cf-ea.everytime.kr/attach_thumbnail/459/81095519/everytime-1772283107443.jpg?Expires=1772368042&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=E4NgzdfLTa9sxzw5S~LK9CQRQnkVRSY7DJUuZH8Z6V8cj2DXJWSoebHs6AYXgfXbh6Nau0Qq2tG1uGwwSZlGShHKxQtfY5ZQ3HE8~RgO4W-eXxv52Fqgh4XU~MtKh1xf0laNz-E3h6luyC98Q5dhsH4p2siyJYZAcmEd6qNvEv84frIkf-CumXcsPdIGjprjNZ~dKAxveGXzLc2v-MrGfFpkMW~YWIEM5Mev8xlLcaRxZweNYdxtabmY8QdvI14KZHUK3VAh5xdRe6SDc8Kj~nhe~rNj2OccPYSeTpulo-4R8Q~SSmKlX~d732MqeLcQpQRAT5bd1jUyOB0VNDKvEA__"));
                clubs.add(Club.builder()
                                .name("소리터관현악단")
                                .category(Category.LITERATURE)
                                .location("용지동아리관 101호")
                                .shortIntroduction("현악 오케스트라 동아리")
                                .introduction(intro8)
                                .recruitStart(LocalDateTime.of(2026, 1, 1, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 3, 5, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("정기 합주 및 연주회 참석이 필수입니다. 현악기 경험자 우대입니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl("https://everytime.kr/418923/v/402240800")
                                .googleFormUrl("https://forms.gle/zsFNfvkSRhF3Q2je8")
                                .build());

                // ===== 9. 열린만화창 =====
                ClubIntroduction intro9 = ClubIntroduction.builder()
                                .overview("열린만화창은 전남대학교의 만화 및 웹툰 창작 동아리입니다. 부원들이 각자의 개성을 담은 작품을 그리고 서로의 작품을 공유하며 창작 역량을 기릅니다.")
                                .activities("만화·웹툰 작품 창작, 합평회, 교내 전시 및 온라인 공유 활동을 합니다.")
                                .ideal("만화와 웹툰 창작에 관심 있는 학생이라면 실력에 관계없이 환영합니다.")
                                .build();
                clubs.add(Club.builder()
                                .name("열린만화창")
                                .category(Category.LITERATURE)
                                .location("제2학생회관 402호")
                                .shortIntroduction("만화·웹툰 창작 동아리")
                                .introduction(intro9)
                                .recruitStart(LocalDateTime.of(2026, 1, 1, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 12, 31, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("정기 모임에 참석하고 창작 활동에 성실히 임해야 합니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl("https://everytime.kr/418923/v/387900683")
                                .googleFormUrl(null)
                                .build());

                // ===== 10. 유스호스텔 =====
                ClubIntroduction intro10 = ClubIntroduction.builder()
                                .overview("유스호스텔은 전남대학교의 사진, 영상, 여행 동아리입니다. 국내외 다양한 여행을 함께하며 사진과 영상으로 소중한 추억을 기록합니다.")
                                .activities("국내외 여행, 사진 및 영상 촬영, 사진 전시회, 영상 발표 등을 합니다.")
                                .ideal("여행, 사진, 영상에 관심 있는 활동적인 학생이라면 환영합니다.")
                                .build();
                intro10.addImages(List.of("https://cf-ea.everytime.kr/attach_thumbnail/758/81075052/everytime-web-1772256317161.jpg?Expires=1772368384&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=k18FlX26B2TUe6Hqy2Sxm7-q4kCc1Heg7W3iULct3BhK~veyL2K0hdm0eEpAzutdErl2vbyi40K2~fUydDIts7v92eiY96dDgAAf2M5QVIOjfIbR0WjWz2xHbSiWnWydRKXVLrey47Q4ycMkSlrRiL41jyHUPFnVSkTMavj3skOaY7mqxlhsG~MgrBZbkKdljjIcj4YqblTiS3sFmt2IkO2Z-R4bJ-4j9EHUh~JvsGoAqiBfvBTkFvxAXWI9hNbp4gM54OXXPfKnGhbry-l9hzcMiACQbi6zpXD2RMQBnjEHSu8AxbBW1cZVfdj3KM3I7Z-ZhP5YHsExqQiNIhV-NA__", "https://cf-ea.everytime.kr/attach_thumbnail/912/81075053/everytime-web-1772256317167.jpg?Expires=1772368384&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=dQFpl3-d5OE~27~yiRuduIfABceE6YK90ilNn2qFAQfd~bXSl2cj~QCLBhTN4u6WtoYfdVdbZPs9cHu8pbRwORSKmWjo4tkOoQH-9xRZMcHfPOxF6mS7tmUhqhn0pClTqDK3Wx36NQB8aajeI03bsHCdYs6N2P7nJS-lu66~55EfmMyqt3CQvYgnBMsRTD1sJn8wio9q1jEktVAgRdnri8IICRKirSWq8tb0A8AR4PkdmONrdgGoL8dCO9am4fdKH0O6NioWEAr5Cvcol4yCV7k-s~hPKwst6XsyV18kXYD7scF0JjLmNMxrOxZxoFv3q~2kBgjQ7tA3VYwRzE1KMw__", "https://cf-ea.everytime.kr/attach_thumbnail/732/81075054/everytime-web-1772256317171.jpg?Expires=1772368384&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=Qr~8h6F81y8DY6wYgahhuZqBe5IlFNn~v5b6Z4FObkq0tsFMYzJU-0l5DnEqmRa-ZOYC6cxAtMJg8gYxFMSp24mUOlkCSczfpfra-ewLU3bvHhW1EjibuBvyxDCIdq0hjwlj0bYgdzGJrs3WG1qCNpdwsX6XzI90Dc2vG60SqmptkL8696wWv5wFZJgDXHB6ppdHU05iWD1FAjimB1hphTDHwwLqXpptsUFU7c6Iyw2koQmUkayuEGuoupeJ2C8T92jrS818RLk714-ndeqIgd5SQ5U8WDYcS7BeT3s4NJSTKJiXJf0XNFIQ-pUn6lpg5Iqc7yhvctRdZjXbLyWUlA__", "https://cf-ea.everytime.kr/attach_thumbnail/669/81075055/everytime-web-1772256317175.jpg?Expires=1772368384&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=CmUPAQfpEzsMb0KeuNIyY1jsKaGIpcGiH~kWfCB7BO~RmhYQ8ZMVTNF2hPWG0ehQI5W2Sw2bgd9WIqGs12J~J1YICjWqaeLxD-c1Co8xYG86RHpxUCi8rtzczcS-kwVn2147VD4mYHaWrg777Ts8428HqczFKmsdhUdNz4OPW7-56ZnvxseSCll67X8xVfHie1tLw2InhScB8WI27pq6Rwz1wGtb9SpVH-EmkhMtoXvEOJjP9dtkyPP~eUjXFmOL8WylAXNbTHaroa6XXrSRh~kj2AjX1J-Ha0lEqN9SXlqb2raf5lgTCiU7tE4ZnEfHhrVMb4Wq056HOxCSGb2icQ__", "https://cf-ea.everytime.kr/attach_thumbnail/572/81075056/everytime-web-1772256317177.jpg?Expires=1772368384&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=VmLSL-eO9eZhJGow7AYbkikVldebelRJY4zVhgL2eL6nQvw1Sxp5Q775KkKyNn2cbTSj~AisoQ7~b8XV7ylnrroPB8xvp6UwDttPIoTkgs2fJ0WYhg3EYAjhK4Ug1XGCDWCeXYJP8g0aWVBU92wHdwNRvamiIAQH48ygv~W7T~N4XmnVI3gw~wxTOytJcOcYDgrM~cqYmGvSFWq9DJwuvr6yceg-lzoEN8JpbJthcmfwex9-iJiV7uHFrlB~e4iJO0XAjRSyixMXLbq~7olN5wC9RqupcoXbcUzucJq2VUHE6C5iy-BfVT98dRGNV~BIdn6BBjxSh7inGBtwAPVg0Q__", "https://cf-ea.everytime.kr/attach_thumbnail/787/81075057/everytime-web-1772256317178.jpg?Expires=1772368384&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=ia0S8AMP62yfaYfl9AZPqkmuHTGsyHXJxSgaVgqktxD458u24RQS2QnZ9TKKWNCu8yzvLJU4YAeOMZTFguGQQg~GbVvTP6XNbhdCprzoZONaZj5ckRZ5R22smO0tYRjrX0FeM5Bo4umG8nn-w95r0KDWw4tAcL168o8j-vsI1ZfSryoixyzEIZbSMsPiRUfwh6GXA8yTOaBMz1MMtLc1G~laTCy9b3LezeydG6jOgAhMNn064PtRpgKWZ4iYdotAeUQnEiAHyabYdMuM5lmEauVe0Ikjc2uU0kfIWjkLaCnY5kYGwPxBxWm5LT-ukm5R229Bn9OvBCKsUWn~P0e9oA__", "https://cf-ea.everytime.kr/attach_thumbnail/671/81075058/everytime-web-1772256317180.jpg?Expires=1772368384&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=LYjWyhzcDhXfdnSfyG~4GE2XRFyghvy55o3Qsw7UlpH-r28vOdwaWRsu9yG46UoubY9ilSVmG5DKjicTiIPfcSkM4~j~M7qsPHxKivRwoTCVlHdxc8bF-6nM6amCi91c0QMYDovrWd0O-ShRs68viD7MiibVPyNbOso1RSDNehv7NLfonFzBKTpBZvXQhrVAQp8Zdg6ZjCX5HqgRULhk4aIJdl1GMLILnL9DW3ugJ83qqZfqlcuXloYJg~KVTCqqWrLycQzSn6vDOSzb3Y8O1aO9w9fPopDcwAahE0Hl4lp63HlU0ORuAid6SPViUze20hu~SsKRYvW1ZpF181oa1g__", "https://cf-ea.everytime.kr/attach_thumbnail/4/81075059/everytime-web-1772256317182.jpg?Expires=1772368384&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=U1yQbB~9mo8S4Av1FdqM2Uv1N7Il~v23Oaf2yY3ffeEvZSNjP6eIAo3KMo2whbhcElFVmrZbN9AfjY6NSm~7jVGixGgOxQcuxY6e7kj-rHQtZAJf9tsE6xjogxeORAqKicFX2bx1YFoqApEBsvsL~IUhtVi33CNortkCf2mL~amTID6DIIZljWT6k3yB4QfQdKiZxtIyll7kMRQjrJMOi6Uw-FZ3O8~0oTi4DrvGi-lUkX1chZCltOv7n3Wj~NtzWPU3f6iJ0Cpz4o-ezTtAALHtUTmJ42m8GNWW9K~9UcBksa7z5ugLlh2krAauEI-vF63R52z7z4HoO5hDdnjIYQ__", "https://cf-ea.everytime.kr/attach_thumbnail/947/81075060/everytime-web-1772256317184.jpg?Expires=1772368384&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=anFiGe8dVZmu0R~OB5fnuz-o1XNM0hyzwFWLnaRTZ0i2HCrpFuBZUO5vSwWK4Naho~0d2LXlIKi4Mu9IIwNMvgh70MvhE-kMtyW42-p4quR8XYbHqGFl24PqJyC7kkjKmRMb6hR3n1HF2YP4pprLt8U7JVBNpBzqPvabVsU17EJBvz5~GSILjqIbRgG0U5h0XNCpZsC~T8MuRHZj8GJv-4~YdYMPfEx2JoG9sxjyeYoqlATZQmukegJHxrNpUVL6TFTMn~vEbdrL3eHR~w3XgqWCwzJ4czSmIwHBMveG9j0aToxI4aFt9HbQ3YmFiG-oRFpvlUTuik6GYZCTw4-0kQ__", "https://cf-ea.everytime.kr/attach_thumbnail/862/81075061/everytime-web-1772256317186.jpg?Expires=1772368384&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=RGnrtIHsOoBZdIfngVhQpzImfGItQkhH6A85A3cVkS66ju7Q6zYODKONE314rEG8p6qBMWON6~F3U6RKeZrSYx7SqFKCBqdzLcbPynNdVIOFQrVrs5gUPBwx1n4T0AMoeWgAB9tcs6cZ1z42fG6wIZT1uevOX7lHrp4K3iCKHu1dd7GiGKHXBZil7-sR3i3mC82xzUaG1nDuqTAm1usbmlXGIT7yAjBtKmRlOSNZAVapJ2fAmNI0xuV1F5RAk328RUo5iM9iBqqvXc4mR04cAok0XyNW33MA9vDj9ZAuvbpGs36t85PMZLekX40B0K1YgFf~q5Kry9JmSEVg4hk5pA__", "https://cf-ea.everytime.kr/attach_thumbnail/696/81075062/everytime-web-1772256317187.jpg?Expires=1772368384&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=suUl88XR-ldHFUtoHzuYtXx7ztdiKfofrZNw5ExvDRHWtWZpUNiqrI2Yyxu-SezPZ0RwwC3Nbmoc7hYEq5CkAsFc4P0H8lGDcGpisz5bQl7~wfPgGd0PLaGbp97pzEykv7xTQ2F3PKFfEbA1T22YoK5-Fhed7bPJ3n5gQCv8X2jic9pptD78UDnlq6R4waKfxoaHVmvRpoOzBR~mdky-6h9qZlcOtuM6jqBUqz-z5N3p~5YdCT1RFFGkNgLcZZVrQs2aj-nLlV7Li79srvddcKGqXQy3VE4fy3NSPoRZ5BdvfZgGyleA3dLmBy0u5Qg4ErQh5ciBgfwGyt0xyV2kag__"));
                clubs.add(Club.builder()
                                .name("유스호스텔")
                                .category(Category.LITERATURE)
                                .location("제1학생회관 433호")
                                .shortIntroduction("사진·영상·여행 동아리")
                                .introduction(intro10)
                                .recruitStart(LocalDateTime.of(2026, 3, 4, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 3, 5, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("여행 활동 참가 시 비용이 발생할 수 있습니다. 적극적인 참여 태도가 필요합니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl("https://everytime.kr/544806/v/402189017")
                                .googleFormUrl(null)
                                .build());

                // ===== 11. 청불 =====
                ClubIntroduction intro11 = ClubIntroduction.builder()
                                .overview("청불은 전남대학교의 재즈·모던 댄스 동아리입니다. 다양한 장르의 댄스를 배우고 공연하며 예술적 감각을 키워갑니다.")
                                .activities("재즈, 모던, 컨템포러리 댄스 정기 연습과 공연 기획 및 참가 활동을 합니다.")
                                .ideal("댄스를 좋아하고 예술적 표현에 열정이 있는 학생이라면 환영합니다.")
                                .build();
                clubs.add(Club.builder()
                                .name("청불")
                                .category(Category.LITERATURE)
                                .location("제2학생회관 304호")
                                .shortIntroduction("재즈·모던 댄스 동아리")
                                .introduction(intro11)
                                .recruitStart(LocalDateTime.of(2026, 2, 25, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 3, 10, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("정기 연습 참석이 중요합니다. 공연 참가는 연습 출석률을 기반으로 합니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl("https://everytime.kr/375210/v/402170313")
                                .googleFormUrl("https://linktr.ee/burningyouth1518?utm_source=linktree_profile_share&ltsid=af7f8e70-f9ff-4143-ade9-9268c21559cf")
                                .build());

                // ===== 12. 하이코드 =====
                ClubIntroduction intro12 = ClubIntroduction.builder()
                                .overview("하이코드는 전남대학교의 어쿠스틱 통기타 동아리입니다. 기타를 좋아하는 학생들이 모여 정기 공연과 합주 활동을 통해 음악적 즐거움을 나눕니다.")
                                .activities("통기타 연습과 합주, 교내외 버스킹, 정기 공연 활동을 합니다.")
                                .ideal("기타를 처음 배우는 학생부터 기존 연주자까지 모두 환영합니다.")
                                .build();
                clubs.add(Club.builder()
                                .name("하이코드")
                                .category(Category.LITERATURE)
                                .location("제2학생회관 406호")
                                .shortIntroduction("어쿠스틱 통기타 동아리")
                                .introduction(intro12)
                                .recruitStart(LocalDateTime.of(2026, 2, 23, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 3, 6, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("정기 모임 참석이 필수입니다. 기타 초보자도 환영하나 연습 성실도가 중요합니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl("https://everytime.kr/418923/v/401387740")
                                .googleFormUrl("https://naver.me/xeFDIo8C")
                                .build());

                // ===== 13. Alchemy =====
                ClubIntroduction intro13 = ClubIntroduction.builder()
                                .overview("Alchemy는 전남대학교의 일렉트로닉 음악 제작 동아리입니다. DAW를 활용한 음악 프로듀싱, 믹싱, 마스터링 등 전자음악 창작 활동을 합니다.")
                                .activities("DAW를 활용한 음악 프로듀싱 워크숍, 작품 발표회, 음악 트렌드 세미나를 합니다.")
                                .ideal("전자음악과 음악 프로듀싱에 흥미 있는 학생이라면 환영합니다.")
                                .build();
                intro13.addImages(List.of("https://cf-ea.everytime.kr/attach_thumbnail/757/81094207/everytime-1772280750943.jpg?Expires=1772368242&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=WGrME0zKDTsj1br8zGx0kaCTOHf9EMlNyoaao2uTyQ1csS59V17kK5hJWaovHf9iRFWfFaznCuML0NJX2ZHLEJjTgvJvxt7bqjJteWG1lpqfJyDy3~if3PUtlErPXlnMLPGYsqBZG-nSXE0tMYQeMvP-QUEycbeKAUO~lQypLYSAoYPH7rZkSjmNj8jL7ed7tLfKS-I2XtTghmjM1xjsvGpuYoyPOsZlF3gIYPV0D8oJ2~AeASIm6ylkZcPcp~nJUj5js3zsVSWu5yB-tGaU5~CeOIAqOp6DHQdAxlKSzWPmFwhDbZ8JNzYwRsLyP52YQm5JBe-1XbUcJgdVeFVoiQ__", "https://cf-ea.everytime.kr/attach_thumbnail/129/81094209/everytime-1772280752123.jpg?Expires=1772368242&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=XSFbYLkwh0WmH8x9yeuSg4f5AVOsA-gWMgcjCfM10MNLuQQ2ROqzsObbRAx8OXDT-cU-BZycs9cw0HQGOiapK7I1HTbMTUfJaMasoPUznnY0xVFNfXHsKzrRVdpRrV4z5kXyB-4VuZWpGUrmGTBMq1A-ZsflRyjvnz8UcOpQOdPak5wOuVHasPkaSPmhGeiz7b2p5~Uo-x6L5EguiXO3o2~9QFt1KbgW0r-2WPbCYKogC4TebQw3f-Olkj~4uC--jsvcrtoNgaDo3pP0Orl5LYKxGO3qWQzZMmXRPFCWT1B1w9qi3clXfqmhUfENxzurGsHSbF09ROG99oRfzCmMfQ__"));
                clubs.add(Club.builder()
                                .name("Alchemy")
                                .category(Category.LITERATURE)
                                .location("제1학생회관 B103호")
                                .shortIntroduction("일렉트로닉 음악 제작 동아리")
                                .introduction(intro13)
                                .recruitStart(LocalDateTime.of(2026, 2, 28, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 3, 8, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("음악 프로듀싱 소프트웨어 사용에 기본적인 관심이 있어야 합니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl("https://everytime.kr/418923/v/402235755")
                                .googleFormUrl("https://forms.gle/sVNfwePR5Y8Bq95LA")
                                .build());

                // ===== 14. CAST =====
                ClubIntroduction intro14 = ClubIntroduction.builder()
                                .overview("CAST는 전남대학교의 연기·뮤지컬 동아리입니다. 연기, 뮤지컬, 즉흥 연기 등 다양한 퍼포밍 아츠 활동을 통해 창의성과 표현력을 키웁니다.")
                                .activities("연기, 뮤지컬 넘버 연습, 즉흥 연기 워크숍, 정기 공연을 합니다.")
                                .ideal("연기와 무대에 관심 있는 학생이라면 경험에 상관없이 환영합니다.")
                                .build();
                clubs.add(Club.builder()
                                .name("CAST")
                                .category(Category.LITERATURE)
                                .location("제2학생회관 302호")
                                .shortIntroduction("연기·뮤지컬 퍼포밍아츠 동아리")
                                .introduction(intro14)
                                .recruitStart(LocalDateTime.of(2026, 3, 4, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 3, 5, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("정기 연습 참석이 필수이며, 공연에 성실히 임해야 합니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl("https://everytime.kr/375210/v/401371884")
                                .googleFormUrl(null)
                                .build());

                // ===== 15. Ctrl =====
                ClubIntroduction intro15 = ClubIntroduction.builder()
                                .overview("Ctrl은 전남대학교의 영상 미디어 창작 동아리입니다. 단편영화, 뮤직비디오, 유튜브 콘텐츠 등 다양한 영상을 기획하고 제작합니다.")
                                .activities("단편영화 제작, 뮤직비디오 촬영, 영상 편집 워크숍, 영화제 참가를 합니다.")
                                .ideal("영상 제작에 관심 있는 학생이라면 누구나 환영합니다.")
                                .build();
                intro15.addImages(List.of("https://cf-ea.everytime.kr/attach_thumbnail/407/81097365/everytime-web-1772285938894.jpg?Expires=1772368508&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=PGZ4ucJ8Y4p~SYVuz0fHsrkAeew1Oqa9OxN1lr0Nvq93kVXCixKXTEkC8X47I5ir-XaDrEsVBfa6Ykm98~GqVQuaSR26MxuONQvPBHv~NgemQXC948yqBJNQ6Os7S9pbyAr0DEi2Q2XlmaisNT9B3Eh~R2NQ6Lzj3eERDP7SxNEIFToh8Srb3lsqO0lfLMBKrOUUnxLakJuEm-jhe2gIZwWYoQE852lnhxhFeJSUtUkKOOkFMDQQM0i~iAnV8ORTteSzkxjfruPLniif0JtgJFipx7KamW9nv1KsQzGvKO7sYA~vI8IxsbzTOnZTVerTgyahVEqyf7VkBPVl5d2xcw__", "https://cf-ea.everytime.kr/attach_thumbnail/594/81097366/everytime-web-1772285938895.jpg?Expires=1772368508&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=dbRs-9jxDy7OkHm0tpX8EEdmW5lpqMghHQ5EXkDUqcRB9batuBndgm0xsQVkfxpPhrh8LkZk8BS0zqAm8IIkCJ2ElQDUYeCaRHskQNflXhsnJrtGUv9LPGwgQDrC~VUT5AJ8kwLwFJcO1VVkG~UbfoZzcGIfERLOKms3lzIJnvJ5WVDMtdZy2dpyh-2WKcmP4HGpn7v2Po2uS7ctstrUlNDYaVI6yJxSsZa3ztp3VBU0hD1s82G1BfDSbhaG7SOYK1DC3wrqBKj~Huv0UuDoLDgC-wzrRD6Eu2ikASTQAeBEBRbtXfszVn3ydOPPJHu1PIxMOVVOVlWW~Ppqe6Y8qQ__", "https://cf-ea.everytime.kr/attach_thumbnail/399/81097367/everytime-web-1772285938895.jpg?Expires=1772368508&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=Z36LBonRIW-XwLg~uzJdAnkR4dpvnryOl8~~i6tM7N75sZFuhxh3x4Xi9hkcP7ylkfLK2aOauEp4kVifWwgfjtIxLf~kLHBqV7x2EOCZXU-e1VIvmTlWP-qsErfNx~nSFySUcz3DCOdDd8IQGRUNPZU~7cMKwE5sP00i5hUZAIg4b-804xq7FS0cP-RgRV-OphFEbyT3d3v6jVPBYszTHsxx-UF9S3d8dQaB9Ajp-U-vHwzBw7QLAf0GsCwLfwqV2nsRH6ypwSZliUUa7AHjcqOKN5H9~KLGaog7Mw066fsK2bCx8L3grmyhUOy4~q7sQ3vGzYipR-RFfhKv8IW~rg__", "https://cf-ea.everytime.kr/attach_thumbnail/335/81097368/everytime-web-1772285938896.jpg?Expires=1772368508&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=GSBwxXwylNrhvqDnDM1skNEx6ql0YHAW~yeUyOjYyVYn-N1OQUBGPX~eDOfW3u0MQyl02aJsftpzFE0xLhWIJVAIfD0QXRgN6GhODP6p~r0Xb37zERMWKqY53BpDlZYpq2n6JQE0iysTFH3rvYfIDQ~0VBzaFPpIO6wQZ~jyKJlZhCb62e2CiMyJzwzLp3o9qYx4PyvAAEsOCVvQD64Td4d~9GJIbyEO3SSJXKSQNGobHO7-Tjd-gE0TyYrctQS58j0ctYcBa5nlweKbvMiagXJtX1JlAR~EhCoMNBvXHk3aSr1yqm8zWNBrNI3PddytHwTwLBLkbA-EN6e6jsexww__", "https://cf-ea.everytime.kr/attach_thumbnail/297/81097369/everytime-web-1772285938896.jpg?Expires=1772368508&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=UdBV6cMBttzgDj6bQcotmDi5agxRTlbgwuI~ya93UxXBVbybQnjBl5DDP~18sb1ABdUYzt8hga3Tx3fiKhwRCwoRRsheYwd166T4qs4korS-avjMl2PdlDAYchH~1hhj32zoJvaFlyGZfqEfA~yA2kilkWMkORGMyJ~nOsKIUHrJc2eiqn11~-x1sMB78djfEhh7JDGAJ-4Umrd6-nGnSHtGgR5YE9cz8luXj5XuU4u1peL19~Om~ex8G7jMl8kdqkyggcgGpFd1zX84tKqlGLqSwrIG6H7VPYtixAzkiX8fsdNYPFzaChUy7wfgqYS~35RIQz0qZdKuL8vEet11pw__", "https://cf-ea.everytime.kr/attach_thumbnail/677/81097370/everytime-web-1772285938897.jpg?Expires=1772368508&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=lApLdXHmQtP885X7tVCxi30N9g4ygtoOhx-XoarybGYniTGzWhI93V6ZzmsNT22Z~HdDLw0zrir1DrtZUkuag2lgZtuqS7FAJAVsRn2oSqPOI1qqw-KW2V794ZN~G8aUCHtTcUkGSYSOuYQF3~INfi2sqkbd6JK07Sryqka4D36u581vx8ikv4HSHHwwILd8lOYkwKUyMyADz9XdE4RSOsNxmMGbLjIpCY38TPY4kNUHyun1RYvw5rFzHGyGVC-Ckk06o3XaEeUmCt1xSBK4GypPzps0Luxi26yyJjOblgNeStgDtW8rzS31U3R0vaYHZxFn9YPaUeGolpc4gx~Rqw__", "https://cf-ea.everytime.kr/attach_thumbnail/422/81097372/everytime-web-1772285938898.jpg?Expires=1772368508&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=ItmPKcJh-OTJqlFP0pO4JYQjS6GkbgJToTGEe4kukdvJD2iV3WmHcy~SDGjHw7qzK34q2ZzYhvHLan~DEL~ynFx1hY37s008s0GEUI-fIY-Kjjsm0KfZMfIKxnt5zegsXPgDgksyy1Wynsudqesak5Yc2Ej4WPpvxLBHyPwT1zBAd5xa7iy0ZrIek~fNsZ9zTH6y89YbPqGdDeZ-F25lgdZHr-1wZaQqYY9FCgpSpVGgjtA9-icP2CKLU6qH~W8VlGrJY-BBh15oU6RFkL8rZwLafWkuqpJ0ES-x-9Ps4~5kKT-Kg9l0mwNk0zGMhTPH7GV7il3JLNWPjcxxtT0OGA__"));
                clubs.add(Club.builder()
                                .name("Ctrl")
                                .category(Category.LITERATURE)
                                .location("제1학생회관 417호")
                                .shortIntroduction("영상 미디어 창작 동아리")
                                .introduction(intro15)
                                .recruitStart(LocalDateTime.of(2026, 2, 26, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 3, 5, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("정기 모임에 참석하고 영상 제작 프로젝트에 적극 참여해야 합니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl("https://everytime.kr/375210/v/402247277")
                                .googleFormUrl("https://forms.gle/ce3mRYkP3K9zFWSL6")
                                .build());

                // ===== 16. LIT:CH =====
                ClubIntroduction intro16 = ClubIntroduction.builder()
                                .overview("LIT:CH는 전남대학교의 독립영화 창작 동아리입니다. 직접 시나리오를 쓰고 촬영·편집하여 독립영화를 만드는 창작 중심의 동아리입니다.")
                                .activities("시나리오 작성, 촬영, 편집 등 독립영화 제작 전 과정을 직접 진행합니다.")
                                .ideal("영화를 좋아하고 직접 만들어보고 싶은 학생이라면 환영합니다.")
                                .build();
                intro16.addImages(List.of("https://cf-ea.everytime.kr/attach_thumbnail/460/81117070/everytime-1772341680307.jpg?Expires=1772368073&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=fkGAswv1ZFs7Yy5MfLpH~8DseIadFtD1e3-5oLK5NbiYjRg08E6qISznQlNRy7OV5d9xiHl2NU3DhbGrZSfEh2ANKADQ~-cUaWvpGa5ooUS6ii7hqCmvsDWfwyRojp43nTT6kf15XTI4AQ37riFE4QXbSdAG01tkA8dJFJ1hfO7T57bxKIcrJ258F4fyUXGwpdvXJkog9I0b3DJJA9T1KVC~1zO7kgL8EiwwnhxEbh5DrCaDQSWTIPnn4dJv9fSsZEAZcFAJrmG61lU8QzfwsihU7aRC5Z0B2LzEisscYT2u1tytz9PSw8lcF50xEgkK7Uww2wIRWNBaKRvi5ywd2w__", "https://cf-ea.everytime.kr/attach_thumbnail/938/81117071/everytime-1772341680778.jpg?Expires=1772368073&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=EujASw557WUU9jvUI0EBw6UlBS8p5RbEPYuTMcqH9mJzpI0JCmKc2ikx7T~ZLNc~zMoyVJuQR9SZ6cfISzF7Qf7lAQQfMIwum5CWw8FYSleNu857ELQAhEE8pz~eU9KPr~eSX66QZiY~GgXW2OfZBWMOWy4ehWpR1HSBVryq8lA42bQjuvW69lrLVtPrqC5uQarm9zWyFKk9Rm2xJTnNX9z1ZErnKLdghHKmc3AHjuPAMVkybaJ3hK6WxNQO1uRrMT-d6v-x2zsf2QlvICbn-XWe~eyoLfOhYqo4AnBR8kKetemKTJOS4GjMpNVyWQlsy4mLDL46UqaRk8-lO-BhNA__", "https://cf-ea.everytime.kr/attach_thumbnail/147/81117072/everytime-1772341681063.jpg?Expires=1772368073&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=XoAYhlZdeNpP0ZA3PdVUGd5M9UzLUyl588iIboqhUi~2cQ9nBDOrDWO4scK-xLhJ-26gPZRAF9Z-0P7cjHtOFHE6ayOM3SOtqp2f5in9rvmYywVckF4Tgb5w9CzVfmgdzugSnGfWY-bZEQgUpagZUhzNVgcALYzgVpKWUK3AOE8vAS1VFBf9yZ2FvmE8kDaK5rxaVEsSzGcOm29iIYQxeHSuM~eQ6ZrfwESs4A4DNoXb~C1X~8naVU0OxNi~EeYDgZ~elk2Iwb3bbQ0QvdGgIJ2IsM~ZE78IB3yvpHSVvvJkhavPd9hjMZ8pV74VB5UmYAAST-ikc6JO4S91YFbuag__", "https://cf-ea.everytime.kr/attach_thumbnail/348/81117074/everytime-1772341681320.jpg?Expires=1772368073&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=AtOrmqJy5DTiBh6qJMSnhmKhdC3XNzlEQUow~2o563bOYWfxBBR2si8fARN7AxReK3ukNlXswvMS7URwY2KfCFSiyo~Yl4sTc-zu38NyDlkztOvx~BOmCKvmEPt19t7OWF2zJTBM1NnbqPPsp6TgGkilTbRQPq8v~nwcapjExBPe9BkWTE1Gu-4tbgEinjMRJHT2zDu1z5PPbKbgJQ~V7GcC6EPHH1qFCiMbalwKU26UVsl6cov7fd~1XULmJEyelnlNda9Ct0Brcq1K1CXx1jBbBRowa3WC-22FkflAGdsV9sK1sg-Bg9asiFGSMCe5Bs8oRNlu3f61vJCCnzVZZQ__"));
                clubs.add(Club.builder()
                                .name("LIT:CH")
                                .category(Category.LITERATURE)
                                .location("제1학생회관 314호")
                                .shortIntroduction("독립영화 창작 동아리")
                                .introduction(intro16)
                                .recruitStart(LocalDateTime.of(2026, 2, 9, 0, 0))
                                .recruitEnd(LocalDateTime.of(2024, 3, 8, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("촬영 및 편집 작업에 성실히 참여해야 합니다. 장비 사용법은 동아리에서 안내합니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl("https://everytime.kr/375210/v/402311077")
                                .googleFormUrl("https://forms.gle/Bnq1qvqJrynbiYCt6")
                                .build());

                // ===== 17. SU:M =====
                ClubIntroduction intro17 = ClubIntroduction.builder()
                                .overview("SU:M은 전남대학교의 문화예술 기획 동아리입니다. 다양한 문화예술 행사를 기획하고 운영하며 캠퍼스 문화를 풍성하게 만들어갑니다.")
                                .activities("문화예술 행사 기획, 캠퍼스 문화 콘텐츠 제작 및 홍보 활동을 합니다.")
                                .ideal("문화예술 기획과 창의적인 활동에 관심 있는 학생이라면 환영합니다.")
                                .build();
                clubs.add(Club.builder()
                                .name("SU:M")
                                .category(Category.LITERATURE)
                                .location("제2학생회관 412호")
                                .shortIntroduction("문화예술 기획 동아리")
                                .introduction(intro17)
                                .recruitStart(LocalDateTime.of(2026, 3, 9, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 3, 10, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("정기 모임 참석이 필수이며, 기획 활동에 적극적으로 참여해야 합니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl("https://everytime.kr/375182/v/402333822")
                                .googleFormUrl("https://everytime.kr/375182/v/402333822")
                                .build());

                // ===== 18. We‘z =====
                ClubIntroduction intro18 = ClubIntroduction.builder()
                                .overview("We'z는 전남대학교의 보컬 앙상블 및 중창 동아리입니다. 다양한 장르의 음악을 함께 노래하며 화음의 아름다움을 즐깁니다.")
                                .activities("보컬 앙상블 정기 연습, 중창 공연, 가스펠 및 팝 합창 활동을 합니다.")
                                .ideal("노래를 좋아하고 함께 화음을 이루고 싶은 학생이라면 환영합니다.")
                                .build();
                clubs.add(Club.builder()
                                .name("We‘z")
                                .category(Category.LITERATURE)
                                .location("제2학생회관 411호")
                                .shortIntroduction("보컬 앙상블 중창 동아리")
                                .introduction(intro18)
                                .recruitStart(LocalDateTime.of(2026, 1, 1, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 12, 31, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("정기 합창 연습에 성실히 참여해야 합니다. 음악적 열정이 중요합니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl("https://everytime.kr/418923/v/389307638")
                                .googleFormUrl("https://everytime.kr/418923/v/389307638")
                                .build());

                // ===== 19. ZOOM =====
                ClubIntroduction intro19 = ClubIntroduction.builder()
                                .overview("ZOOM은 전남대학교의 사진 창작 동아리입니다. 사진 촬영 기법과 감성적인 사진 표현을 익히며 다양한 전시와 활동을 합니다.")
                                .activities("사진 촬영 스터디, 출사, 사진 전시회, 포토워크 등 다양한 사진 활동을 합니다.")
                                .ideal("사진에 관심 있는 학생이라면 카메라 종류와 경력에 상관없이 환영합니다.")
                                .build();
                intro19.addImages(List.of("https://cf-ea.everytime.kr/attach_thumbnail/619/80980503/everytime-1772090593728.jpg?Expires=1772368492&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=bGMBln5cZbvsxnpqs6kbWz5rq2lCXc80UDk7J0YScRGpi~SsFG~TouZQwHndGdIcVxfxEbtxEhc~SRNP2lqzvcsoY7KqLt75p99HdY5g9n17-X3mKxYqZCctoFG0KuEkDBwt~BxzHwza7ZVVijb95tYUIYNwiiKuUkgnbCxemOaNWV~GG6rIOawdkUZlWE7YON1pF~q2IcNDFOlzJ3gyd-Gs83vwR0mdnNBEMTk51On36xgCKQQkcNwjGoJqHkKXf~16DpPS-etyEtw3bcD9axoSBV6ofHeMr5xji~IEWLn~x6F8iwFjgOadzCzGFErGWbl1yp6w1np72c2NbqEIkw__", "https://cf-ea.everytime.kr/attach_thumbnail/479/80980505/everytime-1772090595092.jpg?Expires=1772368492&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=W8B8gUPahYEJbsll6BOzmwMxfyOPgNW28z1-YtmlPwVITcGv80CXVl25HAw3L2k52fTE1gQT38SJaK5FEMWfQ~MjwoPCcIKK~XlWmoOG9R3xj~E4k3ECpooazfLex0OSkaoQbLfvkfcVQeUd2f9K8gr9H9ODbP7ghPyy7sOj~bKCnG0cShGs6IOyGBJrzGmPm-Nc4Ob78P7vf8GT9fjngmVCbU-BXrvk9~dHltinZ3M5C1cEaaD-cv1XG0D13Dwn7dMQN-ozaLbUfIKHH64zWJuc~nbmy3-EtPrPsgneUoaqmkoE8jJNDHQxRy74yC~dxSu2o3Pq1y7TEDcxEM9qag__", "https://cf-ea.everytime.kr/attach_thumbnail/560/80980508/everytime-1772090596052.jpg?Expires=1772368492&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=Gjtw9t89RbHNcvQXQc7cOY0CyKbdT3XHa9p93hhst53L6Goz2znkB0lipVrrLBwK5NKr49dq4Hu3EYsbAXOOlektyQQOS0kLL8plqU3SkAsKiwnq3zr4H03wfpYKfQkU7A5785W0wycXj35~Mwogubp9aa6W6~a4qmrbHIFvl-GT6HzVHCHd~czYc0Ul54pUGUvDI8Pa0r1KzFalae23bLCCgS0hr-r9V3nRMjJwip39r9C2ym6blXPTXUgRKRXRWPUg3gG9tTvbr-0fSHXgOvS5UBaBWtBsXQCdXI4JDX~r2Z-9K08U0G3E0-XKV636VBaILKiUsmLFwrzp1SAEvw__", "https://cf-ea.everytime.kr/attach_thumbnail/808/80980510/everytime-1772090596936.jpg?Expires=1772368492&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=fKgY3Bcz3TEPw817b0qfNO1~bIp9If2R~9iSpbHsEl65gm4K~rL30sIqmlIT7JsJeMw3-N~OclotxnGERU-y0LeGUJP1rpQ7~JKdTvj7ITocuOq37Xbe0yvUPyvzM-RF7siR15EjKtSXMBsDFcSe2D-~Ms20amp7vCqxXi6UJmasmqi7VpZrwXMTZIQvspE5xFLkkyKnjnvHPXzU4rQkW8hYsoR1ii6X2XfS4~ufZfL52KCtPO~2BSnipt2uPyFo1EJV5xLqRddImKkBVjzQMAT62USlqdgV01z8RSggAJJdAkKqxma3e6ZBPGGuClquVsM9O8UCy~Tbo046nRfCUw__", "https://cf-ea.everytime.kr/attach_thumbnail/368/80980512/everytime-1772090597635.jpg?Expires=1772368492&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=PCAE69-TfVUENzhZK~ZfAQ8Z7nUT1NWVTL0~wOl7NIz7N0Q8YZYlDWwTWIxnwOCZHqJgM04sw948DIaQx7QzFrRlHkUj-Vjue0cWgi0hjVHpi28kTT53rf3Dqv5ZahV0xL1NdI7tozhn2he1wpkRsGXAlL-PjedEjFMGsfoClEAdGioJTxH5D17mEAt5avE-mW0HGiq-6-DH~yB73H6tw90BgkHUl7zF9n-VIoiw1HDoum9AvSHLk573LnSrfNiaA7SobMaVnSxBZBaUfDa4Lz0Y-A0A9h0or-qRK4HpyqRpV9BS4uwI34ecMQsMrRwJJv1uRoX~Nxm7NDnmFkMnng__", "https://cf-ea.everytime.kr/attach_thumbnail/179/80980514/everytime-1772090598603.jpg?Expires=1772368492&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=AeWUft-V4kmYKY0-~LD6mg8t5m5LRV3oqAUIQE779X~8l-oyeaTO8T-hBQBgddBzFVszC7qGimIdtNqrRRGwGPH-t3OP3fF0cBeCUAxTZXxwAe5txD3sCQnymgq1KwlsDn4KX92IrzTIzqY1Jxfwkd44uq847vD~j08IlBzAKMukL2Ax3AKKZ8rA3XVdU9JVNxUsrRtEjlvf7KbtS8G7qYfwUifFg3SDqZGWTV3R2Yf71OXGAy2vFj9wsZ36sCXt5f1NdCOGW3fsOmY9Ouq2jW88NCzNfB8d2br~7UUM7jSkFhCCS5dkXTsQETBKtq51Y1tDPhsfXzcNs22z75fOZg__", "https://cf-ea.everytime.kr/attach_thumbnail/172/80980515/everytime-1772090599293.jpg?Expires=1772368492&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=aMHw9X3b1sAr0Ld0b1tu1ij9naXl~B9HOiorur50oxnq-EG96FQQca8KRkMci1PUoewdS-~RRqANbWl~5FCNAEtvLKO-K~7BYV9HDWnVa9pa0QfJXT-4wcwpn9o3XHo7PBIFq8pwhbq4PmiMTX9r05GXNbjRfBnxkwgRjSgYC6n0TBhuBwwIj0INi9Ju9wdWv3ZterQ7qrmMyr1EFDBWsbrwftCEeiZaP99CPCZQypBvqrub646NCi~pt0DGLEk1DeqzmznEt~c9wEf6cLEOMmcwRq5-B7rYl872mH8bXk~oP0GVrm9Pko0coIBBZrjxSx5HZLr2nHp4yoHb8pnrVQ__"));
                clubs.add(Club.builder()
                                .name("ZOOM")
                                .category(Category.LITERATURE)
                                .location("제1학생회관 404, 405호")
                                .shortIntroduction("사진 창작 동아리")
                                .introduction(intro19)
                                .recruitStart(LocalDateTime.of(2026, 2, 23, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 3, 5, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("정기 출사 및 스터디 참석이 필요합니다. 카메라는 스마트폰도 가능합니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl("https://everytime.kr/375210/v/401901866")
                                .googleFormUrl("https://docs.google.com/forms/d/e/1FAIpQLSdDWK-uAStqEFGcR4-kpEMvk8jGricyXl-dPWdH3krwXancpQ/viewform?usp=header")
                                .build());

                // ===== 20. 끄적끄적 =====
                ClubIntroduction intro20 = ClubIntroduction.builder()
                                .overview("끄적끄적은 전남대학교의 드로잉·일러스트 동아리입니다. 자유롭게 그림을 그리고 창작물을 공유하며 예술적 감수성을 키워갑니다.")
                                .activities("자유 드로잉, 일러스트 워크숍, 작품 전시, 아트마켓 참가 활동을 합니다.")
                                .ideal("그림 그리기를 좋아하고 창작 활동에 관심 있는 학생이라면 환영합니다.")
                                .build();
                clubs.add(Club.builder()
                                .name("끄적끄적")
                                .category(Category.LITERATURE)
                                .location("제1학생회관 404호")
                                .shortIntroduction("드로잉·일러스트 동아리")
                                .introduction(intro20)
                                .recruitStart(LocalDateTime.of(2026, 1, 1, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 12, 31, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("정기 모임에 참석하고 창작 활동에 꾸준히 임해야 합니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl(null)
                                .googleFormUrl(null)
                                .build());

                // ===== 21. 베이커스 =====
                ClubIntroduction intro21 = ClubIntroduction.builder()
                                .overview("베이커스는 전남대학교의 제빵·디저트 창작 동아리입니다. 다양한 빵과 디저트를 함께 만들고 나누며 즐거운 시간을 보냅니다.")
                                .activities("제빵·디저트 레시피 개발, 정기 베이킹 모임, 학교 행사 판매 부스 운영을 합니다.")
                                .ideal("제빵·디저트 만들기를 좋아하는 학생이라면 누구나 환영합니다.")
                                .build();
                intro21.addImages(List.of("https://cf-ea.everytime.kr/attach_thumbnail/309/81120152/everytime-1772345113000.jpg?Expires=1772368645&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=MHXt8-hx4~z9cf0MEFWNqcFz2-N8hQ-O9UbNecdKXs2CV9-mEfnEq6m2e1OLZTzK5gfX4gXqM2NPj5BGdmgmJSQBRnmlGMmoTrimnHRTY34-1nrrwSol1S9JJ3GVt21E4iO92n1wObSCHCFnJApZsvkigHLfTaOX7kD8-CTnPKvCtBWVh0M~rWYSm86MIrvqWVZCIaNLiKj7bRP68uAQ2sPT0qZe4aloLuy0T3ApgMLcNobT0D-v8-N5x2BbHUljVqQjev~v7g9kLJYORpLNR0OGBZcx6PwLefd3639m8qE5TjSlk3Z76BI2j~Y5D4O2uZTsb4N4r7aDsW7b4JMysg__", "https://cf-ea.everytime.kr/attach_thumbnail/529/81120154/everytime-1772345113925.jpg?Expires=1772368645&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=nk9ABUN6XDX8XwOrJYmpa42EL-7~6qtF5YjG8vQ8H05alz7fqJosIyuNice42BRFe23yhB1d8pZmSJnnJMiDH6JaIeHfHpFcmTt5AvgNXOPnflwo8voH~MPAskiGgzuRAhFZmtngp3qmOYmTbix7Zbw4MsqmxesEcRKHxzRTPnS114KzCt6Gm1ip55-3QcuWQF8pmEc194KCOTM1V~lp7cKcCrRKFQFtOGoDRYwLEHQjBhNpUks2dS09aAGKVJYPZPMUsXvCbVkcKtf~v2g3VlQ~p7MKMmYMX3X0cF23PGiRlMHnA8o5RBbD5MiBjlZbW2J8F5elY~tmU3dw3Ffrzg__", "https://cf-ea.everytime.kr/attach_thumbnail/869/81120156/everytime-1772345114508.jpg?Expires=1772368645&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=HvXhWPbGPa01lP03JYSvBCEuS1vNFb5iJJzhf4JRCvIyQnKGpNzme5F18u77-cWKmFJRnM7dbPEAbssFkb-yTlH1dd8~D1ID13zrmqEJw9pa9hDUgQaLCiwCC10227MfnfWshgCthOy6ygLuLdLt747XyEnBNNcVzqh2wSbRR3JUDpLuNRhPdEEAsh8t8Ji4~W4OKdbb1RPGnNJfSCsIBnZbufDK4BhJWNXuY4woFnjlt1-lKQXIiqTmfrg74rJeDel9h~fP2PLiuWJKO~R8FH9-HiBlZznzQuiqPkdRfUn-eA81oTWCStv-cGtovqSeQHrTDcCXGy-1tDjiMAl4fA__", "https://cf-ea.everytime.kr/attach_thumbnail/173/81120157/everytime-1772345115135.jpg?Expires=1772368645&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=n389jvO5JWcK4ecGlNkjRpx4wS8BczXe8~DSu1OKi4MAaHnL43WIpHaxAbL0xucMxw7Ydq5PZi7bdAE-ZsnTIqBpTQCmh8f9Jzlx521ESCnLu8tEKHaTcfZATRgeFJxByXqvyZBmchHJiyVA4oFaAa-xDKYvcV1GxyfhLJVoYbZnGMSfi49JBXjmqbFyVtFlal8ST5iDKDfrPky46Fwpy-GayFaOQBBBTvQ6HGKczlDHsEPR3id6TGUwOtOHRX88jLiJneT2HUUYYu15ovpBswSutEgC7Kvf9O3bAAu2j0HC6BcBF4wIuXc~8mIoo-YzM4LFwCYwN-pvezSj61rVaw__", "https://cf-ea.everytime.kr/attach_thumbnail/63/81120158/everytime-1772345115721.jpg?Expires=1772368645&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=dz5pb4-qtHpc8AtVrkVzPgv3iDRZkbgGjEMY6cSeZsIY5mWAfryAM7LhOZHAvK~f6ArEvOR1DCyJ2zPyRFLW8ArPRVckwMdUkLE~iocTVw9GacBbaXAGWGbnI9yC4LAI4fkf1JsHnolfJfegr3M3vHNNHs7fuq9gmXL2TrCkNS1OG6-uXTsMlELha27TnXRktdrtrB8C7OLw8FXn6q~O~5pTFXWPY~HjJ67bmuCB506rk2f4saAeikT4Wt4UbZH9duomEEwsf1p25RW~ifM57P0nW9W-gZEsiaHC6jBC1O9l2ZiAoUqp~asJzkrCg-P69YR6J-bE5TwPdEKiUE7yug__", "https://cf-ea.everytime.kr/attach_thumbnail/56/81120159/everytime-1772345116609.jpg?Expires=1772368645&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=pNViWj1W6ydbGJ9PX9CacAdobEFwQhSR-cIPlx6VZO1VO9uubAkCUQRzDFFCfmkJ5-GbCSBBdgSX7SdfQrFWZKNXFBvPjFCdIbeThDe4v47UUSS2Z6U0eb-LEP6MpJmJohq3prlpaxXEkvy3uweaLWoUskHIqq2RkB2wRBEqrHZthooN2OAdPxHRzFyh9i52DKv9aelq8W8VRLPAkNPEdMmSNS9Es2N4zsN2EYOeJkTgkM3cDd3ngaM261gYH7KmwEsRrorOnSs9adIEfIwHQ3rCzQqjAZM4DKDMueKz71s9i6f9ZYv3WJ-jZvwsp0N7ZpjfL1RZz0frtdV3oLzUtg__"));
                clubs.add(Club.builder()
                                .name("베이커스")
                                .category(Category.LITERATURE)
                                .location("제1학생회관 317호")
                                .shortIntroduction("제빵·디저트 창작 동아리")
                                .introduction(intro21)
                                .recruitStart(LocalDateTime.of(2026, 3, 1, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 3, 6, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("정기 베이킹 모임 참석이 필수입니다. 재료비는 부원이 부담합니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl("https://everytime.kr/375182/v/402317964")
                                .googleFormUrl("https://docs.google.com/forms/d/e/1FAIpQLSdCLOFVUAa92a_38qKWONVcIjdfPAto038n0_z2M9vvWb3CGQ/viewform?usp=header")
                                .build());

                // ===== 22. 전대극회 =====
                ClubIntroduction intro22 = ClubIntroduction.builder()
                                .overview("전대극회는 전남대학교의 연극 공연 동아리입니다. 연극 대본 분석, 연기 연습, 무대 연출까지 직접 기획하고 공연하는 동아리입니다.")
                                .activities("연극 대본 분석, 연기 연습, 무대 기획 및 공연, 교내외 연극제 참가를 합니다.")
                                .ideal("연극과 무대 예술에 관심 있는 학생이라면 누구나 환영합니다.")
                                .build();
                intro22.addImages(List.of("https://cf-ea.everytime.kr/attach_thumbnail/328/77941122/everytime-1757040215996.jpg?Expires=1772368840&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=hKjjKxhv5waF7zKRFZPxIefVqXODf~JfgbT-dz3mTANJ4XkKG~hyPdF3xQLJeoJAb6EVBBxozcxeaX9vtFwZRDKV~D1jJBPQnYHQdN0-oxSwxA9ilq8YgrGbZBjRRMSirwlbINmzDiEMo5m73o3fqn135mz84IzXVUVNZBMwGlCabHFEVi0gUUlPTl~RbGWNqzfWTXx6iF7rXmc3lpdPnpOGXLZNP1gLQQKOPv-6kg0lK4VWzSMsMAbEwpn7RijcysTf-UFlVe1tJGk7MGh-2PIybrCRJGWLB~KncmZ2L668ZyS3x5c0o7NzBpdf38XJOs64LZ2sC54YX6QgTAsrKw__", "https://cf-ea.everytime.kr/attach_thumbnail/882/77941123/everytime-1757040216555.jpg?Expires=1772368840&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=qHjuOtSU07XXXUNFEVOOAyDhb7TRU~Bq1uJV--sGAxr0RTtCCTqCv8YDXl1XBfdCWnEpvG2jTlqN9Pw67cFD6WdaSUgpPKtIlNdMtLOgDm8ty8lh0yXvvOr0rqlTFYk~nzyXPMb14L0KPI4~EfSGbtv8GKFL1Tvgw20eUTaddsxhfRTMka6-QDxdWREOrHRxuUj-OTfRSiAUS9JCVHzeW5pns~pn5uTqMZfXg3sv15fCyMdKncDZPT95yda38olN2vkOff-Gn28tlmobSmTlVW-3Zj2L0LSosF9~9KJw~q4wKOwrDIiwlijMeNK8U41M4Zow~PfdYZscjHfA6OvqUA__", "https://cf-ea.everytime.kr/attach_thumbnail/785/77941124/everytime-1757040216899.jpg?Expires=1772368840&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=H-XVx3qzyiRUCvYJUUQZsg0AozhXFSUwx0zM1ZUGRNTs5hSCxMTaZ3X~XSQTwzhKVrTQfSn9VeUfQAK1M5CHM1hGPTK2sesXFNbx9Qfs~X2yWHKVuXW3sxArNkJnhZAhJ9UVQQs4FnPVtX~7Et2X2dGiPnZayOn-lQ-SqozSGefEdS3oGp9erz9SX3uLTZrWYNM-E7prQ4FsNOpO5-fn2w-1wWauKv1B~kh4p3i3cDvdM7rzTF2mNf0pjI6kxHTmO01MISqlkgw5Yc-GAIVOtJzOkOUGiRLyexmOWEHZw8292~FtGLgRhVyGmDcRm2wgvRHH81OYpQjST4ssH652wg__", "https://cf-ea.everytime.kr/attach_thumbnail/466/77941125/everytime-1757040217238.jpg?Expires=1772368840&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=ltsTwucFNDLnFHL-teNpGPIcX1xFoJmu~~1XzdwdX4KGY1UNqVl~Wd-nIE2uq5dmT-FIQmGIBodtFzrgJwCE-kHFEloVocP1fSsQ87yevKY92AJpMqx7uPvMgJu~LkDXKvhPmLO136mKpoa9675fRdoieCoro-P0dYiGHpcK3LacJbUvz5iW2FYSvJQ8NipFD~UgcL4j-qjLdynxDvYd0ahJ~nmdBTZifGM09bc5qCyCk5ZdKB1J3-g~nKqKKQAU2ieC9IdycRHv5MVymbAd6yfMapEe6yLzQJkCu1ds6S7oIhAOOzrRViNPbE1yY-BNvjtgPMBRn5jnOiGCniIddw__", "https://cf-ea.everytime.kr/attach_thumbnail/853/77941126/everytime-1757040217557.jpg?Expires=1772368840&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=E24mjiZ0qjuw9~l2e7SF-jPIw4n0y5WTDT0v98s5rzk2QJzH-H2kssEEhr4XpKw9WCGyd7wRP7gFnc7JH1YiBL-dlG4UGJ3RUYshRgxjp4JYl3RREZg4ThXbO~hvi8HxzCqqZ85YoWCvlEBLlXkLRQYX3nH9s1zVtG74ZCDeB7miQXJzmLX~3j8PD6tLhw2oguUUfyRAYPodSVsy5~~eaNfoLDXWV~g6p4dafLbi374km1~KCOunnqGVF1BxJ93LP404Dn56JQVM~8s0S5DR0U-GemR5-y4GCSAoLx-sJ~8QIG9fB2rdSiZhQMfs~--Uzv5t5AHelYE5EipZPx7x7Q__", "https://cf-ea.everytime.kr/attach_thumbnail/438/77941127/everytime-1757040217888.jpg?Expires=1772368840&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=PlZkbyxUFmhblsm--tH~RC72SOhtOTzLoS5KLvchEpCPKQV~gSvbkgCh0CapjgbvTr6eC-VFSGalWwR6uqblF5JqgB--qb-~P~rkDVcsBZtmIhiiMSEtJ2c8RERScZbAoWVhhH8B5eJZHeg-WsHGVLlF-gMRFMvXjMqJ3IhCEB4RJqZM6qcF87DZS2KGxSxsZixS75MB6sNhFY34W9oVcRuoqgaBy4LUhVgg3JZ6ISJFpDV3tqLMT~SP0yXZTYvTC-a3IL8Z9HzLXhW52Z-VRCenLhA2IBC1x9Vj6b-OT-Y0DatFywsjJWy~DW6JbWrZbDK7NfYtLR0CWDiLVK0MrA__", "https://cf-ea.everytime.kr/attach_thumbnail/407/77941128/everytime-1757040218223.jpg?Expires=1772368840&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=bbI2OLpV984bwZZzWeKGBkyv6azxkNUvQCNGFdshES2uoucOzXqwu53ns6S6cNCaU9Kkyy7YS2sk9yEdZ8-dSUNclZlWFMdy8wJFNxE6w3HKgwbh5dzvxUDzRJ1q8-JJzCsWzPPgFg9d6XWuzDbE7xW1SUb73I4GjGV3DeEz5KM-OylQSKShYQA6KRQjqV4-HUWMayP~a7Vxp4Wtg6IryvVMroVJsrKoApkCOBYAHxkIwixE~V5vuz3yLo7HkJ-1SKJOEgJxUCq6d4dBg6EnQlDbuUPNAdOqRK~DgfdefP6Bel4lbynab6d9oFlsvXanKAunMGj3bgwXy9y2q9ZNkg__", "https://cf-ea.everytime.kr/attach_thumbnail/865/77941129/everytime-1757040218602.jpg?Expires=1772368840&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=KEBdZ4TnEHysA~E~MvZTEhwKYEvGfEKjARPH8p5x8iV3viPNLYWt3kwQC2OZyrXjYQRSyUPsv7uDHyQ08n17SfYM~qlL7dYwuiVsSajW62I1xcoWpOnO5LuVaphUomDDpLqp1EWz-nlBRxRAF5Gy9TpYw77wmy00xZXpd6xrt8sKKixrgsIwQVrxFZ3wr5Mlxb2-Gwxe-wHSK13LwSVU-b2gdubIAw5kTFmQ-hIPZ2dhP7AvY~IGOt0R1QDQreo1wyNPvRnZayAbCmsESItf0wS2NFlcwwpw0xnSCAoMrw8mUdOxjl0Qzg99X2VcB5vNJz70nLzucpO6xBQP4-STUA__"));
                clubs.add(Club.builder()
                                .name("전대극회")
                                .category(Category.LITERATURE)
                                .location("제2학생회관 414,415호")
                                .shortIntroduction("연극 공연 동아리")
                                .introduction(intro22)
                                .recruitStart(LocalDateTime.of(2026, 1, 1, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 12, 31, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("정기 연습 참석이 필수입니다. 대사 암기와 연습에 성실히 임해야 합니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl("https://everytime.kr/418923/v/389125679")
                                .googleFormUrl(null)
                                .build());

                // ===== 23. CIC =====
                ClubIntroduction intro23 = ClubIntroduction.builder()
                                .overview("CIC는 전남대학교의 크리에이티브 마케팅 동아리입니다. 광고, 브랜딩, 디지털 마케팅 등 창의적인 콘텐츠 마케팅 활동을 합니다.")
                                .activities("광고 기획, 브랜딩 프로젝트, 디지털 마케팅 캠페인, 포트폴리오 제작을 합니다.")
                                .ideal("마케팅과 창의적 기획에 관심 있는 학생이라면 환영합니다.")
                                .build();
                clubs.add(Club.builder()
                                .name("CIC")
                                .category(Category.LITERATURE)
                                .location("제2학생회관 408호")
                                .shortIntroduction("크리에이티브 마케팅 동아리")
                                .introduction(intro23)
                                .recruitStart(LocalDateTime.of(2026, 2, 23, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 3, 7, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("정기 모임 참석이 필요하며, 팀 프로젝트에 적극 참여해야 합니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl("https://everytime.kr/418923/v/402020143")
                                .googleFormUrl("https://forms.gle/eDGMaAHCuFRVLdzeA")
                                .build());

                // ===== 24. W.B.C =====
                ClubIntroduction intro24 = ClubIntroduction.builder()
                                .overview("W.B.C는 전남대학교의 농구 동아리입니다. 주기적인 연습과 교내외 경기를 통해 농구 실력을 키우고 팀워크를 다집니다.")
                                .activities("농구 정기 연습, 교내 농구 리그 참가, 타 학교와의 친선 경기를 합니다.")
                                .ideal("농구를 좋아하고 활발하게 활동하고 싶은 학생이라면 환영합니다.")
                                .build();
                intro24.addImages(List.of("https://cf-ea.everytime.kr/attach_thumbnail/956/81070312/everytime-1772251347717.jpg?Expires=1772369223&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=hKalsfEQIbGRsiKb3U2Jycm~AInUY5vMmtHHSK8kFbQm9fpE0-V5r56oF37vWKQTqHmB6SjMWYVScVD7se23oh7mLuRi4k0NehVIJ91XtZd5b-TgDiU5zVthd~fLW3Eh3JuIdb4bpAAhwcDA5QOxH4GEexY-Ul8fJRngFZZW5Ai77iTChw4rRAbyzI0Gbl5mOQy1NOn56g1T0TwtYfb1PQz~cK~lglZAp-Bjrzi~r0iVUFicgyrDGPB5eYfKS0nG54u1pSbqBVyvdY0bnlapayPoL7fq2E0dYeofZwuylcIq4CkzvuEM3A0qURC5osPGZz4hPbBx6KZMzaOGRb6n4w__", "https://cf-ea.everytime.kr/attach_thumbnail/473/81070314/everytime-1772251348758.jpg?Expires=1772369223&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=eG5Q5j2broKRJx2HBMzaeDNffkcaEd1bzHIB~jaIMOfF0QyScYVAGvMR3GxFb1zZ5jcakzXd4tgSsBS-l19h0lVQgt2pHX9ZkdsL4ytHAYvRGI3SCHdD1G1nvx6d8qBQ2jr2~1J0scoipFso-SnoXdiEknNUxHKtjSLBJPBzi7iLildIlJEX0UDa2-jMfoeRZhZ~Dct3zD89vEyRCsn5EaK3i3IaZszm~PtbHRXZK7b3f~nuVCnUeDxdfe3Eo3vNNNvnvYNoE-us6TA-F4qvP~9wNzWbog~hp9DEv-L-~XP~v0Kr-DoD1pP8G4nTA02ekkhbb4XUGJ-RGbNEceb~gQ__", "https://cf-ea.everytime.kr/attach_thumbnail/644/81070315/everytime-1772251349704.jpg?Expires=1772369223&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=S-o5rucxCh-Hgg-JJamZwmypnU4RDgO03tpgI~MeKSqEZ-VQHMUvCAGfUZDdtOme6MHFLrFLhAwtTHxqJNE4nJZK-VTD6y6WSPIo2Ywc~xV-HFRpaKBPPTSSeXUmj2gKvVcA6sx5GoHqSNXaRcFouksxSlOZ1jwHtpKp0qK-dcc7P3yS19DOCfZoFYmiMak~rMqf3GM3P6b-lXLSu7oJdxDAJI242meho~G8lxhy341gjYS-u6yoBhfIjyHWmUjImDlGXWUn7LVDb-Kf8Gyesw1upjZBcdqudyc48uTXHmBP6eMVZgJzJZxFA012JokXgg-JJ5LVrN~dzjP04xB8Vw__", "https://cf-ea.everytime.kr/attach_thumbnail/79/81070316/everytime-1772251350437.jpg?Expires=1772369223&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=Im7MR4e5zl1XnBfkyI4NS4e6SSi0I2Y8X2CrQG~MhHpmezVasUmY7O16FfI6p4Yjq9ojxRQKDmh7yn-03AvVxveu9Sv6Wam7TUQtZFKrmfvYqMu4Pk4gr72GUeSqwFg39OqP0JNwweL1E~gvI5zGsgxt-CRaQR~eAdKSm9Rt2oR4B~RPj-dHjlIMtwCwJbNqwXrPgK4vGuUmI3Z-MqAoDAMVOJStAQ0BMG5XP~Ndw-iJv52BnSC1zE7duJI81-a~8V~hlVJVzkC0N0LNNXo1Rkg1iwhXrI~pK2kN~ZKYJU-kUPr6CoVHqhpIy8~vpcR82FM-Gs3ayATetK3uYJiAtQ__"));
                clubs.add(Club.builder()
                                .name("W.B.C")
                                .category(Category.LITERATURE)
                                .location("미정")
                                .shortIntroduction("농구 동아리")
                                .introduction(intro24)
                                .recruitStart(LocalDateTime.of(2026, 1, 1, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 3, 5, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("정기 연습 참석이 중요하며, 팀워크를 중요시합니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl("https://everytime.kr/418923/v/402180519")
                                .googleFormUrl("https://open.kakao.com/o/s4WuYBii")
                                .build());

                // ===== 25. 난파법학회 =====
                ClubIntroduction intro25 = ClubIntroduction.builder()
                                .overview("난파법학회는 전남대학교 유일의 로스쿨 준비 동아리입니다. 58기 역사를 가진 법학 학술 동아리로, LEET 준비부터 자기소개서 첨삭, 면접 대비까지 지원합니다.")
                                .activities("LEET 스터디, 자기소개서 첨삭, 면접 대비, 현직 변호사 멘토링 LINK 프로그램을 운영합니다.")
                                .ideal("로스쿨 진학을 목표로 하거나 법학에 관심 있는 학생이라면 환영합니다.")
                                .build();
                clubs.add(Club.builder()
                                .name("난파법학회")
                                .category(Category.STUDY)
                                .location("제2학생회관 307호")
                                .shortIntroduction("로스쿨 준비 법학 동아리")
                                .introduction(intro25)
                                .recruitStart(LocalDateTime.of(2026, 1, 1, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 3, 9, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("정기 스터디 참석이 필수입니다. 로스쿨 진학에 진지한 학생을 우대합니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl("https://everytime.kr/375210/v/402290388")
                                .googleFormUrl("https://form.naver.com/response/6PpYUQ2IfgasJsfnIxxqQw")
                                .build());

                // ===== 26. 랭스 =====
                ClubIntroduction intro26 = ClubIntroduction.builder()
                                .overview("랭스는 전남대학교의 프랑스어 문화 연구 동아리입니다. 프랑스어를 배우고 프랑스 문화를 탐구하는 활동을 합니다.")
                                .activities("프랑스어 회화 스터디, 프랑스 영화 감상, 프랑스 문화 탐구 세미나를 합니다.")
                                .ideal("프랑스어와 프랑스 문화에 관심 있는 학생이라면 환영합니다.")
                                .build();
                clubs.add(Club.builder()
                                .name("랭스")
                                .category(Category.STUDY)
                                .location("제2학생회관 403호")
                                .shortIntroduction("프랑스어 문화 연구 동아리")
                                .introduction(intro26)
                                .recruitStart(LocalDateTime.of(2026, 1, 1, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 12, 31, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("정기 모임 참석이 필요합니다. 프랑스어 수준에 관계없이 환영합니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl(null)
                                .googleFormUrl(null)
                                .build());

                // ===== 27. 별따오기 =====
                ClubIntroduction intro27 = ClubIntroduction.builder()
                                .overview("별따오기는 전남대학교의 천문·우주과학 동아리입니다. 별자리 관측, 천체 사진 촬영, 우주과학 세미나 등 다양한 천문 활동을 합니다.")
                                .activities("별자리 관측, 천체 사진 촬영, 우주과학 세미나, 천문대 탐방 활동을 합니다.")
                                .ideal("밤하늘과 우주에 관심 있는 학생이라면 누구나 환영합니다.")
                                .build();
                intro27.addImages(List.of("https://cf-ea.everytime.kr/attach_thumbnail/119/81030464/everytime-1772172146411.jpg?Expires=1772368787&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=EZqWOmjw2EIviGjS4XiecJVgAoAC1zITCayCx41v5CIEUhV398rAQUZZ1kBW0WJf53k0e5PrvCM-oMilQ~RzHDGMHESvOHiC92yD5XxBXJsRPl4u2QcseyAuqKYA18hmHuitb1lV~iV9oe91vQqLsV-YlSnh-tQu2D~iFGPp53HBxrUkmLPuxpu~midGgViZo~iDi3RspdtEmahzQljoKoKEog-tOvEqZkOEACtf5mjA-qD6GSGvNiJzRxKJdih-PQ73-ob-yGaQvbGxLqXD8MgXhBjWd4cOCs8uddUGZmuK~IfxAxXKllgg8FI83DkmVA71jUY9fuGJphwxe-67lA__", "https://cf-ea.everytime.kr/attach_thumbnail/226/81030467/everytime-1772172146927.jpg?Expires=1772368787&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=qpTjYO1IjKA3tpoVVTHPsL4v9eWPr~pZdD0qQ~Vb1zQzdh-a~SouBZjcAGlt-4joOS3oCEplHnSfTiZ0tQMrswvz-HMDpcZVw5CrNeIVC28zwGcW9BHisb9dkFGENJQHmPBviST1aqc46ykWM0nzmjfuqTqYiLZ-9xyv66QwCrRyJKKk1nR1wZLeJjO9KsDBWRK2N6OzNNKSXHHFWlSAq3myb5uaeaK--wmuNp~8yht4RZMClpXJSOyXZWG2YkIAAruRvnh5RnQ7t8OrV4w1GJaZaUt~fC9Rv9BeAJC5tOk0u7lkhqXz0to19CQG3sEVh2t14TGUeyh-EJwpSeriKA__", "https://cf-ea.everytime.kr/attach_thumbnail/492/81030469/everytime-1772172147354.jpg?Expires=1772368787&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=mgsPAwEWiTYNkqJXIRxfxHx47TtcKIJ~njm6vP-1u3TjeM75bamon9MO-WxJeLF-hVGlxWvRQhPW7Bf1TNvAZ~e~tEYC7uAa1wvWg5uEk-GZOP4lj1rpPcnNg6q9BRbe6YzJviF6RnrjhBZ~pidHkZJJf6ou3rjwYKEKRjgTzllVosQVIA3dQSERtldb7ZUHdme5cib04paowNicUpeQ9jd9AELMYgabGSAkrl27ntFXagdmSYR1snQHp~EfuaXVcycEx2w2zqScAjfJYXcAsuu3R6JYecaB6wSksEIQQnQ4c5BrXYH0U1nFYDVyUPYnBazHsy7Y-GkpxHa8AT8hwA__", "https://cf-ea.everytime.kr/attach_thumbnail/109/81030470/everytime-1772172147668.jpg?Expires=1772368787&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=oNW78d57YN0EjM~G5qjVwQ7nJgXjGz30jt46L2ndG8bYKadPLk12MIVXn0CbDWvsr9Nl4Sg1XAgUQGl2C0BiYOMoJaK-nAK0Ja-K8VFR15YL2KnAh1~kqr8Wx2Ryffz4yniszeMyRHhviIUN-9Lgjl79ITSjYj80AcmfCf8qhTNg6vn8Ucfc1UDpMk3fTyMRfC36XoGQrAmrlZZTBlVFVis6O5TRR6YW7jvZ4reeBXEiO5HopMygi-N9T~twRDrTac4gH2CrHuhtwWXvyHD-H8LMVhlFED0yV9TYI2Iz6Evpunq-oRL4hifhlYw89lUVoGRNSJy886iC6m0wDe9H3g__", "https://cf-ea.everytime.kr/attach_thumbnail/862/81030473/everytime-1772172147948.jpg?Expires=1772368787&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=sv81WY2lPV60~rLUVSkJHjFn0agJ1PYaRVjQK~IIYUC9OVa9qpWQGCaPaMhSNGYjXLA3hWOpA5~Vk-8gqhBJ82VYf8lEsil3DkT-9bPdbNQaoqmve9uqSRL0zBLM~pp8jAa2fx1h4g5fC4y8oGqdpPpG8OyZtpV0ggHhNaENhhtf1FqQJuSVzlKOXPFzDu5VPKjYKtuFenfi-vyvXrlwQeAmDSal6JXq6sbo-h-EmHMCq~PGeiwvfMehYK~CKpeBZjMcHQ7o208gzF5sA8qRZF4H8~XoyFpRHoJbOcg67SwUICOK3QM9SWJnI6rV8lb1hDWs8uU6hRlleRZutAtnPg__", "https://cf-ea.everytime.kr/attach_thumbnail/856/81030475/everytime-1772172148262.jpg?Expires=1772368787&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=gp1Anr~ITm10je~6-cPX4SPU36v4lgo3tPNzbKOfZG9NY3PjB1U0zItK82EIfSpJJ0vDks2-wPFCn-0HtqoxByCDkHpwVOrqecLmPLhlXPbRl4TjRyT8pYitm86ZsSnfR5v3x1jhk2XuPJ5aFNdRjVLNbC1I4advRYLBMm9qvyuIHuBnDqy2QIPnJ09LUpjPsiS64CeIE8m1J~aQXvMaABuzIr28QdCo1m9Re14U-ekwJSjHcw~1nK1n3YuysJ8xSJYw~MkfGFspfLj~-ZsO8NgXdqIOwYWsjBwwywc35c9mCcoH8IIB1~oTVbpU~qH3AVf1YG8CBozZJ~2X~I2FpA__"));
                clubs.add(Club.builder()
                                .name("별따오기")
                                .category(Category.STUDY)
                                .location("제1학생회관 410호")
                                .shortIntroduction("천문·우주과학 동아리")
                                .introduction(intro27)
                                .recruitStart(LocalDateTime.of(2026, 2, 18, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 2, 28, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("정기 관측 및 모임 참석이 필요합니다. 야간 활동이 포함될 수 있습니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl("https://everytime.kr/375210/v/402061223")
                                .googleFormUrl("https://docs.google.com/forms/d/e/1FAIpQLSfqyr9wdPeWjhEvpPf-NP4gIa2C-ZpLxmuUqFXuTMO12N6P_Q/viewform?usp=header")
                                .build());

                // ===== 28. 일본문화연구회 =====
                ClubIntroduction intro28 = ClubIntroduction.builder()
                                .overview("일본문화연구회는 전남대학교의 일본 문화 연구 동아리입니다. 일본어, 일본 영화, 음식, 전통 문화 등을 함께 탐구합니다.")
                                .activities("일본어 회화, 일본 영화 감상, 일본 전통 문화 체험, 일본 음식 만들기 활동을 합니다.")
                                .ideal("일본 문화에 관심 있는 학생이라면 누구나 환영합니다.")
                                .build();
                intro28.addImages(List.of("https://cf-ea.everytime.kr/attach_thumbnail/924/81125294/everytime-1772350880603.jpg?Expires=1772368921&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=MjGTyz6aXcbYZLht4gl4j47GKOAVsXgJBjNwPdNilaLmoZwXBlhyTU1Tnons-XffNCdEf3-Dq7LK189jniytUeaku0o4IdGAmsPuQvbkvJ9IbshzWAUavdfoD8fRbCXnYnuq2qm9Vyt0~jmZODXMbu-yKR8rXHxy13Hdm3rx74vP7vQm96gkwzJy7hNmFRLmDAk3e9U6VDqpKFhYjQ4MJAtT8OdDegceQ2uwn4SIqsrs0EDpAE7NWhsV1XzH94JSYjEeeYiFvIqBXI9XO15FWgm2FdFRzTJNQdImj5rp3UshlI7YdyiHlOJbTbawMB4VWgOJ4bDksELEXfAOtACRHw__", "https://cf-ea.everytime.kr/attach_thumbnail/845/81125295/everytime-1772350881404.jpg?Expires=1772368921&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=khokyjrW1tNDYqftaCYpKZ33LbhbWJTIr2xhvCq5d1lP-LP4YcAMTa~LK6qPDA6kKJMOGr46n9qGXE7E7f~Ax5ncX~4Z1HoYeaVziucSFgp2LieWvOyxO98LGGjU5rKXdEW0J0U~BrnO1qJ2uU-Rjm1BG9dHkLoiQsNLQq0Rbxqt3~jKPMau-YD23E5gTv8ESykm~-gl13MAmnPPEqOU4mvlWpLmzQ5ZbjGmecwPfZUw50s5VRXElaWRTzVyivLHcW5y5zsIK~BsJYJ1-l94rWGK-LOXUqvjCY2nSABHZ-2JrC~hyxXFCIrj4eXzc8vN~j089GGiHPf50PKRIUln2Q__", "https://cf-ea.everytime.kr/attach_thumbnail/53/81125297/everytime-1772350882368.jpg?Expires=1772368921&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=AX6ipbKBvmem6iIjEM45aumDqGJrH4-lHOYuMJtI7GZLeKwroHVHF65WiyO7iEh5OKZ4sqmS4W3QGTbAn0oYstYlas6rUw5Mr6J0PZJuWoa-U4g~fEczZkCVF7B6mPgh-mCZVS7st50Jy9gr1OmA~TYnm9AG2yJBb32iekMZC0SvRlrshyr59Uvg4kQsdu6uEytmKOmPBLN44ziPROF-K8Tk47C19GtE8KyNjii8pSCoMr8QACkF2j3MPH-rBy-aRTftpwxPZVzEqS~LdpQ9eZ2QWwLPSkqFUYgI7oiQ6xRGWWwVgPz~HccvMXZ3hccH2k1jySaQHTjr00BCSCZqIA__", "https://cf-ea.everytime.kr/attach_thumbnail/351/81125298/everytime-1772350883003.jpg?Expires=1772368921&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=rs9RAxBY-ZCtqzkqu0ndFhPkBVNBBH5VcGXqDpN4icly3gWR0rUxN~Pvz2VTHJG9LWdagx~fwFoTGVwabmGtMgadi9v~RS7pTtSgyzrfvzK1TGKjrqdX4P0d4YN2EN4HMeJ02R4d1oLrStbxsZaVBr8hnAWj8P9WsSV1LcTGBkO04spBL2PqHmByKGsLo1vTC1uLZZsfRu4-lm290clakByNdiE4YTSA~DYMa2ub2gBcMHrBCSSRfbs39HHFV8CHgOBcNzjcACQEvty~bmoUMLgpuNpHgjPLPZ4ADI2g8uNJ8VR6LpHiQvKwuY3E6NpuAoign9038~oxU309sUbf5A__", "https://cf-ea.everytime.kr/attach_thumbnail/360/81125299/everytime-1772350883643.jpg?Expires=1772368921&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=aQVqLYbL27mQsHaE-xJ2~C5-UvK9OuAYjuKpD1DnYGBjnFImP5sp2oS8byZaLLdk5zFFI7J223uBX3T-0kAHp6ld9EoguMfc-vhB16L-RjD~-dXTvaL~yYni4gpAE12ddeybfjILWp0rp~RijucUUdKoNOsuJVykgNJbClO0E2B4hqkD9D-JUFLW0wghoOhOu6V-4M7lIZ6qt9Qx-FyGr6c9hK5-7B-5AyNRRkb0afOwTdtizk27rDC9i0SDU721gcC5ilpjG3Q5I2pxCdjxCi1REVDiCzn~78g5GUgs58SdpCwBwDAOfRS5cCJglLmOkBtcgmQJsZwFXa1zt0OOwQ__", "https://cf-ea.everytime.kr/attach_thumbnail/292/81125301/everytime-1772350884289.jpg?Expires=1772368921&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=KSmDSd3n4UDt~6mpzbei3ALTUClirVzIsA5GuGQQWNnsKDIiww1d0FR~iYKyG~Wzd7MBJBoDY8LU0GsnY03pCIh8C5Pqzl1ZJgJGl0d5elEO6BviXmJsSO24CXfo7DC95BXoaDuMbUgRPIeFnJgzKj08dK9fFHpjnwzsDBSRlBsdwFRuEu-cd2vwLD~8em4qPozd6Cth5lNz0IP8xcQRdVbi2Ss2SUQLkNxHqU~I3rchjZC1q8xb6nkmzHeQcMMpW~D95rvr4kIG~ycHpseDsDyUnJegZrm59V~Fwar27AlJj8JWS7ZStpsm99OIJPrJPSr3JVXqg5EDwHDjZP~Pbg__"));
                clubs.add(Club.builder()
                                .name("일본문화연구회")
                                .category(Category.STUDY)
                                .location("제1학생회관 431호")
                                .shortIntroduction("일본 문화 연구 동아리")
                                .introduction(intro28)
                                .recruitStart(LocalDateTime.of(2026, 2, 27, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 3, 5, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("정기 모임 참석이 필수입니다. 일본어 경험이 없어도 환영합니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl("https://everytime.kr/418923/v/402330287")
                                .googleFormUrl("https://open.kakao.com/o/s6S8FGdi")
                                .build());

                // ===== 29. 일어회화반 =====
                ClubIntroduction intro29 = ClubIntroduction.builder()
                                .overview("일어회화반은 전남대학교의 일본어 회화 학습 동아리입니다. 일상 일본어 회화 능력 향상을 목표로 정기 스터디와 교류 활동을 합니다.")
                                .activities("일본어 회화 스터디, 원어민 교류, 일본 문화 이벤트 참가, JLPT 준비 활동을 합니다.")
                                .ideal("일본어를 배우고 싶은 학생이라면 초급자도 환영합니다.")
                                .build();
                clubs.add(Club.builder()
                                .name("일어회화반")
                                .category(Category.STUDY)
                                .location("제1학생회관 428호")
                                .shortIntroduction("일본어 회화 동아리")
                                .introduction(intro29)
                                .recruitStart(LocalDateTime.of(2026, 1, 1, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 12, 31, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("정기 스터디 참석이 필요합니다. 일본어 초급자도 환영합니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl("https://everytime.kr/375210/v/401363840")
                                .googleFormUrl("https://open.kakao.com/o/sei9Mmhi")
                                .build());

                // ===== 30. 전대토스트마스터즈 =====
                ClubIntroduction intro30 = ClubIntroduction.builder()
                                .overview("전대토스트마스터즈(JNU Toastmasters)는 전남대학교의 영어 스피치 및 리더십 동아리입니다. Toastmasters International 공인 클럽으로 영어 발표와 리더십을 함께 기릅니다.")
                                .activities("영어 스피치 발표, 테이블 토픽스, 평가 피드백, Toastmasters 공식 프로그램을 진행합니다.")
                                .ideal("영어 스피치와 리더십 성장에 관심 있는 학생이라면 환영합니다.")
                                .build();
                intro30.addImages(List.of("https://cf-ea.everytime.kr/attach_thumbnail/617/79888982/everytime-1768836621348.jpg?Expires=1772369164&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=U9hItE1pRPpzLfr9HhosxVYJJMlPMGdhMTUkOAbyQ~osb56PAd5ueEwNFdhYq-qEUzI5fQ1kEqW8Yq-wlznIeVjLLCfBp15lt15VF3MOUFVydvTppIhfbi64J7C1MmgPRIIljk9-H6fUiGHnYg159-PAuyPC-um~AvLxDsIEBFYPYfjB33vjnwW3h2pBEz-LG2V76i~4pP8VghGm71N6Fr1zuoyfIwI22dQLve9C4Nm7mV4iYbRRPehpgA59AQSXbp-XAHn69RKvwrlSQ57blG2p0Yzq0yGsAJ2eTZ27ReYViBVuZSRA3bAdsj1XNhsQwlgMtYwMtzRlH23tEDSPtQ__", "https://cf-ea.everytime.kr/attach_thumbnail/62/79888983/everytime-1768836622470.jpg?Expires=1772369164&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=cIDFlF0sg4JO-mlmIW7aGwLoVFPiDDi2wbPDVuzhfF56jZtkt2icHAfW-HHgLAOhHfVkC9mK-DSEL6-d2IuSdCh1WyCTnRzvly-R~q2nFWZiKl6BHswGYR4SsFYAU7Lu7xhLxa548U3IlWkeNAWCzw7R5xYorR~TDZYjuAsel6f3MrZxehuVf5br4lhDir3YIRiKMVLS9OFUrsF5gllphm3d6DCOJFgFSe-GeiujgZHXx~ITkXUgAlA5goIWIAMRPXKRgx2RnTV6q1Bs5Nl7UD~ppOs1c08BTpGRvdz0a-QMKooDoZ5xeiU~Z0xXieTRclb00GuJ4liDCRQvsOwD9Q__", "https://cf-ea.everytime.kr/attach_thumbnail/923/79888984/everytime-1768836622955.jpg?Expires=1772369164&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=VO--z6vdToRulQ5cAMvMEBaVHkJZ8aZrLsDAt-VqeJMirLWqqY5U9203LDK~ZUdF-5u4-3XrfWx59gJQ-f7IesGsOCpgGhRpMtXtVVwQ30tj3Iawsqf2hk-ER3X82Xi5iJuamYlPe63BuKQYWH~Lt0wQ8UmWv-q1V1HtCZctxNGuZy1yLrH8FDXa-AHSR3La8NRMcC75dHO8iKJl90x4~jYb2nz~tr4qIW8Qw2ZrQvkON38~maK5uhF0NF3klsbSW4LgNHs7hZzaorqP637-k8y1SlxvhNtSl1N6BW3br9rEbNV5CkAq~n6jZTpX~FPXklY7iNpbGEEpP8mEt2zQOw__", "https://cf-ea.everytime.kr/attach_thumbnail/264/79888985/everytime-1768836623426.jpg?Expires=1772369164&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=A3Ety5KZ40QOmuWcytciORBg52uVJtRpZcxAGdcBV7oj0cAnZ-dQ7il9ddea46v7WfWY5jnfGjy-v7h6PvGKSFe27KYsREQzmyrHuHDmZtxzr0tyMLBKYcYF~oSITEZDcYvnlyftCHvV4dV3cZdMxlSayYCahbHx72SRZ9azaFIkMJ~BZt9OMREHsnXCUQfOxrWa45jmN9PtnKaXdqnijW5G1e2NwWgxNe7n-LlmaoUsuJIHVoMD~wxnvkW86DRA84u22azKIg7QPfFpETnaHUbWKDdkEvlV0BbjUvriMNf3UhdFUvB6CazcR-Rdpl84kMUQf3QlALFGEPavTMkuqw__", "https://cf-ea.everytime.kr/attach_thumbnail/757/79888986/everytime-1768836623933.jpg?Expires=1772369164&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=r5R0ukNR4FGP6-xfRffs7pjsu9fSlY9x9TgfjrGUXhAw8xLqx3V-dL4yg1yCTgtVQ2u7NwzviQ7l04XaN0LyyOh7dBvT2w1KT~5Ft~tlhtJNxWTw4Qf94wxr9FfTtGNuCjzn6oY7G5okyMNBXHmAXlnm2KwWsu3ZRc7I0pzUQrfynbqDre7uoBklJfIZSNpG8sJHlnyOr37vLu3RaHl0I-j4nIk2sMkLfdeRINDv-gqLKciqj9SzB9bTPVHHPVOHiyAaQ3a8KK6LZQ8Vjzi-i-l362b1aPVpuuWl1m5fslazvW~fsuJCNfRHeWl-349J0RxKWgJjcxx8EsUBPZTiVw__", "https://cf-ea.everytime.kr/attach_thumbnail/937/79888988/everytime-1768836624373.jpg?Expires=1772369164&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=Q~Gw3tKtOEtGVfDBs-KOpsBwiQ1tnFqgxPLjBmmgFkJ~0U7N-PLTWpAAwpPJIePlPCUdYb4aVULX3hjAhakeaLPiTGApMI24cPZPR4kWslqoWUgG~5wAKls8mKbOP7CLqCZZCTWmJcQLkcjekFwLeW6JJh7HcxJS7so-xtiJj8HlTmaIqKteIbVWVajTJONEv0JwLsd-2BX8vR5eT5EdvTxGFWUJDZS9-vBb56pk7ddvrZzZuu32~3SwEBlSu0xgKQbQH3dw3ty1MvIvTdzKNNkPZvZRghfHAf0LooDxWMoo9CwFKh7K5Gf8auhg7ja0x~hR5DTrBteHGWnNKRngNA__"));
                clubs.add(Club.builder()
                                .name("전대토스트마스터즈")
                                .category(Category.STUDY)
                                .location("제1학생회관 301, 302호")
                                .shortIntroduction("스피치·리더십 동아리")
                                .introduction(intro30)
                                .recruitStart(LocalDateTime.of(2026, 1, 1, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 12, 31, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("정기 회의 참석이 필수입니다. 영어 발표에 도전하려는 의지가 중요합니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl("https://everytime.kr/418923/v/398427828")
                                .googleFormUrl("https://open.kakao.com/o/gippE3re")
                                .build());

                // ===== 31. 호버링 =====
                ClubIntroduction intro31 = ClubIntroduction.builder()
                                .overview("호버링은 전남대학교의 드론·항공 기술 동아리입니다. 드론 조종, 항공 촬영, 자율비행 프로그래밍 등 다양한 항공 기술을 탐구합니다.")
                                .activities("드론 조종 실습, 항공 촬영, 자율비행 프로그래밍, 드론 대회 참가를 합니다.")
                                .ideal("드론과 항공 기술에 관심 있는 학생이라면 환영합니다.")
                                .build();
                intro31.addImages(List.of("https://cf-ea.everytime.kr/attach_thumbnail/844/78099611/everytime-1757556885480.jpg?Expires=1772368754&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=iE2-ghS4bpEKQBjz6MyqYSn4kXgbOj1~gTDlHjFdcLjoYqtdPWpURlkfidvxxV74CuUgWfyiTb5NYM5tgBQ5rQOao-PkSlrFVpYfivu~f-UcyL3AugQ13~9I-ENaJdmBFk0p5QX-rjJdrBEAK-PTK4OkwZxcvkXxWu17s1Dw486c1EM3MaKxNFzMRbVF5XUouaAuzCE2xbPXkzPvjXuAYpROliocXFRYPCpnctppNZXBPb9Ymcx7O9m31NHifmsLqNn7wqL3skn~XeMrBZpuod2CA83OUyVnoE3ikEObKQdbOc9Wa1r4xYlqywyvNtPJ9Bv4CFtuGPvF1fgXgasw9w__", "https://cf-ea.everytime.kr/attach_thumbnail/632/78099612/everytime-1757556886455.jpg?Expires=1772368754&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=kEHaBsJ4~2oKVrjWlEFs~i579A13q0f0dGl5lpB~m2rXkY8sPFlw8efT2tXrOItZc7NAHIw0awVZGsy0-2OPCEL675Ru7xakTOg2ufA-i4z2UlqFKPzpB9B9FwRNUEhhsQTXPrRPm8-41NxFt9DeO5zCvghTgz~Hpj~JUqo2Udoz-rmQihdps23SN7Sl06ytOO1crRabKe4YoFXwlmaS4I9Q94eAfo8wZLTz6y60n5toJK5-fl4FQBJS22w3hNXW2JGWXuExlyy9btjFhpd11r5qD0fexHnrHcItjPGKd4cI39uvKN4Jsk-Iz4U~URTYX1d4QLnq6cNdDsY~wg3T3Q__", "https://cf-ea.everytime.kr/attach_thumbnail/791/78099615/everytime-1757556898351.jpg?Expires=1772368754&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=h-R2t-NRudQp51u9yKBSlrbrq-Y98Ch0XoDMjz9M-cSXqCE2~7vZTYulEDOjruJmHOLBfaYaSPQoOxwKFxf6FuKvcVt0SA7eXbA3LOEKYzjw0arCXz~iyR1YPRSgjq8F7kIB~u2wGUlBhKod1VADU-9FSHpJ6~500s-V3yJNaE1RKm1jSYs8b-g69mhqL0FXEAdgdlcyBr69Fvbgj1RJZxlTbnDsJXnEafVn1mNGSBd~UfJNisuzWE2sJteTcW-LGKh36-hyWu1glGCZMw-N~7JSGFgFvBJk-5Qr2CI7YYPXGXc8YOXQlGbv55bQPuQDeNSqnRaQT9eI1l6Wp1aVLw__"));
                clubs.add(Club.builder()
                                .name("호버링")
                                .category(Category.STUDY)
                                .location("제2학생회관 410호")
                                .shortIntroduction("드론·항공 기술 동아리")
                                .introduction(intro31)
                                .recruitStart(LocalDateTime.of(2025, 9, 5, 0, 0))
                                .recruitEnd(LocalDateTime.of(2025, 9, 11, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("드론 조종 실습 시 안전 규정을 철저히 준수해야 합니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl("https://everytime.kr/544806/v/38965224")
                                .googleFormUrl(null)
                                .build());

                // ===== 32. 흥사단 =====
                ClubIntroduction intro32 = ClubIntroduction.builder()
                                .overview("흥사단은 민족 독립운동의 정신을 계승하는 전남대학교 중앙동아리입니다. 도산 안창호 선생의 정신을 바탕으로 민족 의식과 시민 정신을 함양합니다.")
                                .activities("도산 안창호 정신 탐구, 역사 강연, 사회 봉사, 민족 교육 세미나를 합니다.")
                                .ideal("민족 역사와 사회에 관심이 있고 올바른 가치관을 가진 학생이라면 환영합니다.")
                                .build();
                intro32.addImages(List.of("https://cf-ea.everytime.kr/attach_thumbnail/804/80931470/everytime-web-1772011864076.jpg?Expires=1772368890&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=fFAoo1bMM0tiUD1l1bBxis-Dt55U823yhYGGq1ChHXXOiIt5MNLnI2et07~rC642u59i8ONnEO0tfSo-mAyby1LfP0pSFtPwLrObAYmzl53VVvNHv2kLBtDHYWBAoM5VZKJYPdsqGAtqZ-NjyBGEsVeDu6lrV4uGlT-vr5-J5MxyevheHVDeWws9Wl2yeTI9ew4WDV34bgWPYeNIAxCrl9s6xY5L5sFJvQCrJfZ1HuTnXsfF3I0byNzfUE1zJ1zHG9Hq16WREZTCkO8C1fRZfqtAquCIOhXmZs1deujL84hOR~C-RNpG1PqKN1t3Y83cHHP-E~e~X7OsdUXTAT0SIQ__", "https://cf-ea.everytime.kr/attach_thumbnail/207/80931471/everytime-web-1772011864077.jpg?Expires=1772368890&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=TGMIXiTBG5BsstG-vEJideKBLXNjI3vKGLg9LkFSfeBwRPFRRqw~Pnuv860qgmjRQH9zHYDhi2Uv7uddlZadaxydIk-OGsd7pcK5XxyyZ8zaENSbsitxoZ9AQpvXhQF9lKM57Eo8Sv2qIZsEorA-rxXKhyKE~uPKsXa0sTXRwuqQPIZHQbot6MMRwNj562eN8EkGC7S2kybuCRAr0tklfTTNyVgqvf6RktzyzbVYSQdQjDPTrp6hPgNm~LrMR8XHKIxlA4MQKa92BqR5Or2GHMyY6PO3fHdz~EtxUFw-URNwDWJlJM1wM~rY2OZ1qmZlt2w1JcWL0Ffg~gN3zPjXgw__", "https://cf-ea.everytime.kr/attach_thumbnail/948/80931472/everytime-web-1772011864077.jpg?Expires=1772368890&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=aq7VCuZOi~zFpyuk2Pnn1lxeRbJnamLR4b-cIr4eTrLyO98onMGBjzkLRA9uAhXrBa~YK1qmvLH4tzC90H7aAF9zZQyVqnY2q9er-7D~1Cd9-Lt3C-k4Uy-F2MxHZ3w8QUMHaD43Xu91KPkIDcz-DlTHIFbB~Mwny9O-DNsyNytlrXDZv3KEJaAXykiwVTx0zkml-t4VHpZ91jMlFoQo203a81g2mxtzaUVS4IwoJpQ7gtx~dLPTxaNmwKxY9Y7kyQW213r3CMU7vbxrXAg3iNg~s3BXLMMrPtPS~7aHMVmXk02iYBiHPguplr3rDBU5yWQAJ4td55ApsNhp-spUAQ__", "https://cf-ea.everytime.kr/attach_thumbnail/139/80931473/everytime-web-1772011864078.jpg?Expires=1772368890&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=JHUC~Afa9PEfMfolBk9ScjYf1Wn1J5vDVvCnrnSVH1zRK684fJIWII77R6Dwgp3OHL039RU7Zt6twSB4CXzAcTQAAgFLApQGU3wKRGwTnuHbIZp2QcuEf8MkO5uMM-YriO6jPWcBa5s2GhE3-nx7OmobVPRSdeUbblMqMxhzXfII6GjPZ5Q7XCAkOaeV8Xw9GkLeRC5uRL1kPtlrUxrHm~9FMVpX20uWelGB1rT0tvWhbPOYSCnCaZY5f1JSwa1rHjU8sF7W~ScknDoR7o47qspKiFBbDUam5wHm620MmVVF9GYuFp5OZUFVNoUkvyuk2rChy~6XgSRroe9oc4jIbw__", "https://cf-ea.everytime.kr/attach_thumbnail/676/80931474/everytime-web-1772011864079.jpg?Expires=1772368890&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=D~Izi6YyhFGvoloEGdR6NjDd7qYxBQCtTt92m8V8cW5g3ix2XxK3GRS3h76-eAqstle1DzqiZ0TY0~DBnGft8wOwg1NPF~ApG1dNHuDzQ6~zq9lGVA14MrUwX3zR3K8VZqnd7HTaiQgSkFvTA80ADlXXwuSlxkZweKQR2bE7iyCjb955BLGgAfTZg0MSDZ3mXhiS77vukgN48d4gl4sqga7lMkf3hfJcGWV5aOqBJQmV9fdYb5TZKvDdXXm1nMeX8LXLAt6X5D0NSY5hzRUtkeN8~uFjIhCNH7Zmf1fOekGD0eRJOflA-TSz6E1pfQGSvk8aaukn1eYH7BPwzBheIg__"));
                clubs.add(Club.builder()
                                .name("흥사단")
                                .category(Category.STUDY)
                                .location("제1학생회관 315호")
                                .shortIntroduction("민족 독립 정신 계승 동아리")
                                .introduction(intro32)
                                .recruitStart(LocalDateTime.of(2026, 2, 25, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 3, 5, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("정기 모임 참석이 필요합니다. 민족 역사에 대한 관심이 필요합니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl("https://everytime.kr/418923/v/401743189")
                                .googleFormUrl(null)
                                .build());

                // ===== 33. ECHO =====
                ClubIntroduction intro33 = ClubIntroduction.builder()
                                .overview("ECHO는 전남대학교의 영어 토론·발표 동아리입니다. 영어 프레젠테이션, 디베이트, 인터뷰 연습을 통해 영어 의사소통 능력을 향상합니다.")
                                .activities("영어 발표 연습, 디베이트, 원어민 교류, 영어 인터뷰 준비 활동을 합니다.")
                                .ideal("영어 발표 능력을 키우고 싶고 열정 있는 학생이라면 환영합니다.")
                                .build();
                intro33.addImages(List.of("https://cf-ea.everytime.kr/attach_thumbnail/653/81043487/everytime-1772184937515.jpg?Expires=1772369057&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=W5PEOF~I~FAKJAm7FedxiuXgZGL~jJ~aTDI4tYzsm-sbgGIaRE94T5MEU2oMPrYpIhK-fWY9m3T1LFL4eWfGPXhjS4xzFQFvetNpRB5nM0QR8~odQWhJz8zqwr0-qTCC6lspVB9Bf1nZnX0Hf1J0BbB-79roJF-SR5bvrQImFegm~Er0pNdDNQyboPYa3c5Glc6Abk6d1tFAnpFnE4PQO0EPX0h2TAmeAx5JIdEFC4Zdn3Q1UhhjNN5pEtCP4uiz69FDsu3C4KyHuJXV9ztrvUPabPuCO~CsxQXCIa~XgX1JVn9jFE8LGumjBayunEjwa49-i0dOq7AbV-TOLX-~rQ__", "https://cf-ea.everytime.kr/attach_thumbnail/925/81043489/everytime-1772184938020.jpg?Expires=1772369057&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=XesiXw0BgsinEkkQDH9FwNLOmtGihrIK5M9X2cPygK5TaFYA3KGCHyOvucLo0oF80unrWBXOYAfvpERVhL5SIlz959za3EdpepOhs73syaMoC0eNLV3889HcdXWLN3R91ELpBfyiOneYXMJJFrH-l6eexGCxtW4SeiZy5K-4ll8~HsBn4MV4RrarUUXEe05djnosMWaOl4xg3GVY6wbT-RnET2W~tm49k-bIJ~fkA9vsr8l9eG7u1DIaGxILquT1E6UVlilt3JN~V8ucMQXZ0eBtBgvc1~ApvAPv6kwU17MMtqJnp9ZRlcDS2uc5teD0eju9vL0Y3NPgc6YtTFfcuQ__"));
                clubs.add(Club.builder()
                                .name("ECHO")
                                .category(Category.STUDY)
                                .location("제2학생회관 308호")
                                .shortIntroduction("영어 토론·발표 동아리")
                                .introduction(intro33)
                                .recruitStart(LocalDateTime.of(2026, 2, 27, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 3, 17, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("영어로 발표하고 토론하는 활동이 많으므로 기본적인 영어 능력이 필요합니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl("https://everytime.kr/375182/v/402093342")
                                .googleFormUrl("https://docs.google.com/forms/d/e/1FAIpQLSd8EBNbSSTgs6nmWIQhD_ilMcL3c5uNYdgNOFWqxJrXWeO8VQ/viewform")
                                .build());

                // ===== 34. ESU =====
                ClubIntroduction intro34 = ClubIntroduction.builder()
                                .overview("ESU는 전남대학교의 영어 스피치 및 토론 동아리입니다. 영어 발표 실력과 논리적 사고력을 기르는 다양한 영어 활동을 합니다.")
                                .activities("영어 스피치 대회 준비, 그룹 토론, 교내외 영어 대회 참가를 합니다.")
                                .ideal("영어 토론과 발표에 관심 있는 학생이라면 누구나 환영합니다.")
                                .build();
                intro34.addImages(List.of("https://cf-ea.everytime.kr/attach_thumbnail/211/80904525/everytime-1771988458380.jpg?Expires=1772369271&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=qeeiEIcU1xNBRfKf6PjEXoRGa9mDasa8Qwy2fufxvw0VghKKRM6UBycgvS1X6IaP9U148b5nnovfzLe9L9KSxDF0IHB3aFpGYABrzr6~vJueNwYHgciJg9ir5ZEgcWAOlDwES5FTXinCnBknodqzEoafStnbBWGPEJ2gPzsnqw1XFtgF92OwCchxHGak5t8tgfWa~l~rDqx4VRo6brasn-r81vHhOXnbZBdUXT1pxzHM3aG-hiDHB2oxR-V6eKJzAtazolMmVg8145gCyUpj~8KDM8LSHmPmkKlrH1~v3sJXfZ1l9OTI9sQj~76~iruw-ccFmHQ-fp-o9VE64nuPhA__", "https://cf-ea.everytime.kr/attach_thumbnail/946/80904526/everytime-1771988458959.jpg?Expires=1772369271&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=IOupaeNwP-tafkwel1m55uQeJQtzfsudpSa2qFPAqmD60cPLkYpEXp69UiEXlNap2tqOHoF6j8ihf~1XC7a0S~x1zLVYmJDZqIb6W-ar84evVShb~SVybZH0tUq80yeRS~IUxZeLh1JwoSZGKxlbeHC7E3-Z~HtbNFHPBBnaQhob3jZlqvv7h6JpH1T9up0iqnrOQSmCPRVE~U8M91BGFBzV4yK6UKVZHEXL69h0sTFw2Xo172D5kO0Xv1aE9ihIwFHubm92sWxwQOKe64V~rtCclDfG406yijtOq4eJ07oCqKAnySFDbUB1qBpOaFt6BSSq9-fqRvRsKL1W2nZobQ__", "https://cf-ea.everytime.kr/attach_thumbnail/156/80904527/everytime-1771988459309.jpg?Expires=1772369271&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=PiKE6EOyVCmIb~Uuz5yIXfuuMFWFf2NTqfIZXDgv-w3T4Cc7B7n8ZY3SacFolQos5UCKh0EKln9pnouC3ti6Nb2Ik-aCzZtuCfElnrAhRqeAm0YM~Bg1RV6lXGRlziLuhs1okZnV4yD7XdTulpUVVDx33K3e2fdI2BogXuf231oz5RP5kjYhrJI7AJGiVhspai5q3K57HFfjTnT6UlsidAzyhaKFiwYNLagLAy3gOfHOV1CXdJbn35vwMFsfjnqd-Mh1N~h5RKBb863--AGnbyRAvbXpVwLzbSliTf1nnmjUQPUFFtjJIqAeZTC2ZrK7QG7vvrSNibsfHT1~vdbCVg__", "https://cf-ea.everytime.kr/attach_thumbnail/444/80904529/everytime-1771988459752.jpg?Expires=1772369271&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=exfK5WTeIraedkiLqx1fciXhUwuio8QexZDF8IizY~0GauIwzVVghN29ylKl882arV8fLtEe6r2Aaqcl3abl2Vg8Q2t79izvxhhR8VFjALRXiJwKoIulaDOeCTsG6GhNq~9-9CLF9BjDsROiUhRDmrIz5Of3IFbVFN78GG~IiyPHPpQUUtcBxUGCsELgFFbBR0f8mkIf7tgN4-~ywTjB4hND2CfsVj8n~aZ85JJ0mxalZrThgHvOxiLhehDe3JNj09l4uPdPxmZV2ucni1qo9lZ-0Yj8tuWU7FMFPCbnDglvWUr53EE177BA84IPeftr~aegrTLA-kSWWi2vbawZ8Q__", "https://cf-ea.everytime.kr/attach_thumbnail/587/80904530/everytime-1771988460036.jpg?Expires=1772369271&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=c0DCc0TN-Cyd-a23slPJMrBgHvgrYNaY~weGMNuZy4DKyVm8bQwHG9ibkf2aaWHxpCOhQd7XeIUx9wKlSmRsz0AZtPksaRnKxHqPqV557rFfJ5~ribrjJctYpkGyEXMy0zc2fchuBWnaU4vXFJRm6UeTREUCq1m6YTU-6dy2yauapKe-AGKafYN6lG8AqfjEDGJosIFmjn2UJ4q4MzLquPwtDWtipEiBXwMvXoOpY5zpWXro-guyf2UQn7dI9svA52zr7BhOnyEoXoGp4xZbKxFLdsezfNH0f8PUnCiM-9gMVU9deIg4wmaf3tRWyDH8iqF9kSULiRf6CBWVuIPDeg__", "https://cf-ea.everytime.kr/attach_thumbnail/420/80904532/everytime-1771988460364.jpg?Expires=1772369271&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=g2C7U60g9xKJg3uFCobv5ufvKYYNfwuQFbsxXsxm93PBBeUJ4tL7lfvJK3j3Buazt42ZVw41wN-979JFIgMKu2SbPLr4S9S3PjoUl5lyOsbYoGQ8CVq9fgIbL-H7tRfqbVo993H78N7TQdsRiiLmoeVK2qlC09zpm9spluQKAs5lFh0F0FBbaMr7n2R4T4ait3COqYLafFL17mb7VMRiy80C3GSQ1c38XRA4tcx9x6-PEoEyoDTeETfXsDk01V5rU0Ih4fEQBcDGaBgAF07gw2VAuDvmaSFfot6oWqLJkhjyMXjDQfJN7Z1VRHecoe5~Fb~n0Q1u7d31pTUa6pN4dw__", "https://cf-ea.everytime.kr/attach_thumbnail/230/80904535/everytime-1771988460733.jpg?Expires=1772369271&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=ovYz-vUhUxAH0JS8KP2oPCXU5eqYhjETdrpGfIKERs3kGfWOhrZIELOD48obX5S9cUjByLqQTuYWRy-GY0O9P77OBnOJu3jPJEV1HMXORt4PzlVDuupBfcZ~sq0jPqVebRdgAU8pDNSe-mkehUHf40coVe2Y6qrRCIIlPQi5ak4IMXCBJdUCwSqH0IRBQ8xgPCsojcFAB9h3VUMZgHMVJ9LFrB-WkrWT-VXh5iu8IYbm-U3mORPnkgRzpauaH3VTM-tGJurswHQbOMmZFRLHk73bKZisl7EoV8kM3qhtVLfsvk0MbL5RC8PpXIh2GtuUz5CgGQFKu7Yq535nIyTHZw__", "https://cf-ea.everytime.kr/attach_thumbnail/260/80904537/everytime-1771988461008.jpg?Expires=1772369271&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=NlFjaUojMCLRf7RV82H-KblDtdX4FZmCcroeZ~zk~NlL9PiQr3wWIJovQgQKb5Uep4dDy0Zz9sofax~RPAELnXTeer6K00JRNYifoA05luZQlKLfqh4hdW5VH-vOMMBgdIqhYn0tFPxY-wM6xF49X3CJiFthpFNxPFPNs~VuGvG9AwFJs6N0C1uf4eMCawO1i2-fZqUV2V51tcyk2vj~ZsAK1IDSIV8JIo8TXNfM-fDwxxC5l~YuXEf72dCqDs8YQ8V9fFK3wy9z4Fk06EDweWMkiGCOBmbUN9P6DstMrmcCbSZKYNvdJwZt9fo4AO~qNM6no2BeCUCVgUxtCdRbdQ__", "https://cf-ea.everytime.kr/attach_thumbnail/683/80904540/everytime-1771988461433.jpg?Expires=1772369271&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=AMjSys9dzpXCklR85eqhUQO3ZZHEgk6IFAAn83cISgGJ1UiMaUAgOCKIZl7max3o1jcgIefovSBXbyClgYj2UhRkJtbkIzF~wjHOp6UOhAPHMDeRQP9wXAuacXxmYyIqtl4CPyXxLEMfAA6~UrpspTRRzKaSPLlIsPqTyZ7~a7BqosE3Pelg~euOq-nRtEUceVewAJ523JYyvxNEKf2XVPc6hKt2KPbkW0qzXwiouqsfD924cp5XYdHUYlC2bc4BqporM0DvEfZqLPmhjoSUWyYIK3vJxKHQ3nvvxMW2KWjw~MORmeQLudVcn7KIlQi3Ou5l6CaTYfd2GUvCaN3O9Q__"));
                clubs.add(Club.builder()
                                .name("ESU")
                                .category(Category.STUDY)
                                .location("제1학생회관 303, 304호")
                                .shortIntroduction("영어 스피치 토론 동아리")
                                .introduction(intro34)
                                .recruitStart(LocalDateTime.of(2026, 3, 3, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 3, 9, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("정기 활동 참석이 필수이며, 영어 발표에 적극적으로 임해야 합니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl("https://everytime.kr/375182/v/401680546")
                                .googleFormUrl("https://open.kakao.com/o/so1CCRhi")
                                .build());

                // ===== 35. F;ACT =====
                ClubIntroduction intro35 = ClubIntroduction.builder()
                                .overview("F;ACT는 전남대학교의 팩트체크·언론 비평 동아리입니다. 미디어 리터러시를 키우고 사회 현안을 비판적으로 분석하는 활동을 합니다.")
                                .activities("뉴스 팩트체크, 미디어 비평, 언론 분석 보고서 작성, 사회 이슈 토론을 합니다.")
                                .ideal("비판적 사고와 언론 분석에 관심 있는 학생이라면 환영합니다.")
                                .build();
                intro35.addImages(List.of("https://cf-ea.everytime.kr/attach_thumbnail/623/54713495/everytime-1671002236359.jpg?Expires=1772369459&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=YOC~-ozK-58gLQFMet8B2ABPN9HXHbqTNvOQ1xz0UUlEc33H8oCjweaj1vuZx~aDJOTPKf~I-8Z4NXiTq1xYFCj22dTL6eLo5bEl5yFD1c9n6ilxNgaJKrJmqVNkTGuHbCgDC0ORWGFSAjZgyhVEl28aTuIvj1VPnTr1vZGS38gJqkLD-YyGxL9qOt-Zwm72ETs60GSkeWCtkK453QYWqKXRwp81BA7uBvBpceKK7qTl639o0hPbuXN4A2YsTuTdlbXarqNVzyLqRUYjOmPZTaX6YZ6tnyQDpVgnS0XFVD0hlPt4UVyqI57k6Ga2g8abLIWxYQ2UK-jcrWyZapaeGg__", "https://cf-ea.everytime.kr/attach_thumbnail/852/54713496/everytime-1671002236785.jpg?Expires=1772369459&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=OFs5~oFZDIMZos9Q7d64l4-KOnYaWX0NA7ePgp8IW5mygWlrVqaJ928z4QEu1l6hVdxmNBxVhFuiFF0WrpHsQ9NTkEbqZUBnyAWZoaB9pOdqs7e3UA1zyrwulxuyLsJR~r7QsKiD7uh5KffP7Ekr1oZZ4NykVGYzNCap-FOwYnWjaZDz4i648Jte~0~Sng72X4IUgBT8~Bswdp-yKk9XC5ZIcw6iabYAG4BQBqKPEXnTNydEaTfAnC9BYu~ZvvnggsHozpu4V8HGX2QuFLqxHFYSqEzfwAB2mSVDR3ve1X9wS0wUFEt3sErPS~XchQP-PwPwFSENOXaC-etM1gI44Q__", "https://cf-ea.everytime.kr/attach_thumbnail/149/54713497/everytime-1671002237114.jpg?Expires=1772369459&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=RK0xSZPaS-7lnIAMykKx1sUGoFgwkqX9BBPbQ3KJyajX2O0RbgpwxQvvEEUwu3Zt8jpzaBXzh8cxfbsKQf7i5kYi4~u8p4dlyF3NwV30weu03DivuApjMkfdgzuJSnM1iNGvk8rgIf-ktYD5U-HS~2XF462D9BuL0osKoO89vGF~4I2PwX5q~yenEMeundeFSGEgF9ytZhOrhjPTbc3VMBJ52kAzXqZ0cwYCgvILIq2HFm~aHGnEVmiZgR3EiauvMY85yHO-L~9C9-EV5fX4zGPvkZsQECLruf5y4gw5b-4a9GMXXksD5ZWxnstRp~VZ0weniUt3NEbLU2b4HKSK8w__", "https://cf-ea.everytime.kr/attach_thumbnail/129/54713498/everytime-1671002237422.jpg?Expires=1772369459&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=TKKB929Vl9C5OeC36tGc2CLhJ6WOQ9QXjSqgB1AJRijz8KjRebi8~KCXdFFX2dTb19xAMN9KFqqMIl1-ZSDIhwo9dLQS6fUMUxRHu~y~lCnF-AJ873iWCjAZPJllzdjhtvuHJLU7hx~W6QyUf4ZhgGOwM8yv4sBz~2lc6OW4n55OZbWK9oXDGn01yp092Id8tCA~x5pFkm8dcoy-yTw-voFNhqNDKe7qlA8FHrsxKKp7xNkrNArJnRTK5kH2TXBw1Jg-J~92saDEV5tYCLGLB2lIL5aCazGLp8ExiCkYIphTX~SUesbC9T1G8g93XoMy0WHcYtcqkYHogP6Wt1mmKw__", "https://cf-ea.everytime.kr/attach_thumbnail/418/54713499/everytime-1671002237757.jpg?Expires=1772369459&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=gQUpLT71wHuBDjiVRCEL~s50WjnEzegisrvD5Bovvl16NlbmaPVyucqvuPbc9-CtzCh80mPWvjXfuFy74Zzh26nRlLBLvkljCsYPHVy2IONMQ40Vjsrbpep8wIaIl-9XpwoJI4VfKNCn0OGVT4wfUzxvZJDCjp4PISiTRBsg394wPd5yQNibfI~jmAcOUxr8Tg7VvJGlO7NXA3nvwepSpq6qZ0R41-MfpASb6RybSNXpDjpdcFpByygGEvZgjlsT9aiDIiCzEskTGVUWDlVte4890aA3BnbcHqhD1MzJaGXpOqwuneg634k6IVD0-CEWTKoq5g2lg8AWC3-NuTCsYg__"));
                clubs.add(Club.builder()
                                .name("F;ACT")
                                .category(Category.STUDY)
                                .location("제1학생회관 435호")
                                .shortIntroduction("팩트체크·언론 비평 동아리")
                                .introduction(intro35)
                                .recruitStart(LocalDateTime.of(2026, 1, 1, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 12, 31, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("비판적 사고와 글쓰기 능력이 필요합니다. 정기 모임 참석이 필수입니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl("https://everytime.kr/418923/v/279486839")
                                .googleFormUrl(null)
                                .build());

                // ===== 36. 시퀀스 =====
                ClubIntroduction intro36 = ClubIntroduction.builder()
                                .overview("시퀀스는 전남대학교의 IT·소프트웨어 개발 학술 동아리입니다. 프로그래밍, 알고리즘 스터디, 해커톤 참가 등 다양한 IT 활동을 합니다.")
                                .activities("알고리즘 스터디, 개인 프로젝트 개발, 해커톤 참가, IT 세미나 활동을 합니다.")
                                .ideal("IT와 소프트웨어 개발에 열정 있는 학생이라면 환영합니다.")
                                .build();
                intro36.addImages(List.of("https://cf-ea.everytime.kr/attach_thumbnail/503/81116259/everytime-1772340973533.jpg?Expires=1772369256&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=D4Hai0ttv52eHaADWkPEUKZE-Ozw-V0rK9onpijS7YaGt-jmk0MQfIhlrc4~l8kFxSq~uunP1lVfJc80w-2K0djeYPxhvIEX2IBi0xXBoSz5yZ-vB8dZ8x3~EVhi-FIXGVtFGBcEsAWQsVMsuaSfxn-joFbwDdOD6glkQP7bQfxQ34ncit2dNFLq~2ZDLDe5-Fbq8JSDzIt0Ozm9pkS4S3CbcorGHmRNh0jB-OVtCFMGccCFHuKcnX60eQ1GKwZaB8Pr1vEvHKGYFFX1Nr6ubfAQYQ~aiZo-nWmIk1sVxBSDS-8OaY2Odj3gsdMf0X1gmLwmXlJpUdhDBgXWMIL~8A__", "https://cf-ea.everytime.kr/attach_thumbnail/880/81116261/everytime-1772340974423.jpg?Expires=1772369256&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=cm3V1HszpnXgni1QOm7HUyq8ImaP1YiYVd9NIuxQkC~xtG5sFC9U1xmxGlP2tkA~UB9bZ-L8qN~DPqEpdIll5~XLe7opzfswhfnNZpJqJlyK5dvNOS3f8saa6o~B7uM19Qn4OEkA8xH-FPr~LOfGO6KRDygP0zGqdYjfcZPqUDKgHFDldTnpHVCpSzLErJaRCK2LG6NOujefiuvkc2zDC~fubi2QDRIdoRaCVyfPh~9Me7aAK1sgL5tZprKk1lRWXJKu9u4-LbqMo2arfHL-AN3Ea88YVRfzuWYlqNm351Xn3CxQxOfRQHyJP7Folp9t7~wTUXtHHotISVt7zkAsUA__", "https://cf-ea.everytime.kr/attach_thumbnail/445/81116260/everytime-1772340974003.jpg?Expires=1772369256&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=KToqkc2BnXfg6CQAAisuAKuRXTQ~9BYXQXLZcyIQ8wHb-ovtsx6jXffdvIGXhlyK4rXQ4KRXhxUoOyzuo3qIkstvifMzZI7goyXiDipai9jDLSBlmQYNGe37Rq8DLD-XtUjn-fMIdwphujaSD1FHH1iYsfIcLpJo6fgNLJgAzLrGYqJGi23~VBaEeTiwqSZ9IhDdqukeYmxT9pwAFFaNqx3FQme6YWXyAu8LNnNNzs2a3BnCbnEVgQ~szbJgRU2hjar~oMkGPIZhS8mfADm6xqfr3Jqc3XPiAYamvJgW2AgoteGpY~AbMH4qrdL~gX~a064P~BJtuvwqLa3oBsKQcA__", "https://cf-ea.everytime.kr/attach_thumbnail/987/81116264/everytime-1772340974931.jpg?Expires=1772369256&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=APOEakWmIG0M~~rivW7jfnBLx8p1fPGHc3-wU1VKDKG-YBxqs612bl6GJpMNWdR7YdjT94j8izOc9z3G3bDlT~cMojZSAKF92pQbaZ9EN9UFsrL3riOo8R2b9S8bC7t6b4nSz7~b7Ccp-PPlSVqPgYKryKWWtF1CgE2u8xR3FU9BEkmpOksA7hBEBvTPQHPxcWaDgghpZqohyaBh~ORJmVbi~H66l2OetfmlnAq77GpWoqkRMKvPkgByQVkCOjuXeeHrVgNYDJlqlrzcchbDNvrnHT4L3voULe40Y4aO3NLqkDFOdsBYekESPnehFD-mMHS8VAQAO0zkuqNCeeYePg__"));
                clubs.add(Club.builder()
                                .name("시퀀스")
                                .category(Category.STUDY)
                                .location("제1학생회관 320호")
                                .shortIntroduction("IT·SW 개발 학술 동아리")
                                .introduction(intro36)
                                .recruitStart(LocalDateTime.of(2026, 2, 23, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 3, 5, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("정기 스터디 참석이 필수이며, 코딩에 기본적인 관심이 있어야 합니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl("https://everytime.kr/544806/v/402309640")
                                .googleFormUrl("https://open.kakao.com/o/s7Qm3QNh")
                                .build());

                // ===== 37. !DEAS =====
                ClubIntroduction intro37 = ClubIntroduction.builder()
                                .overview("!DEAS는 전남대학교의 창업 아이디어 동아리입니다. 비즈니스 아이디어 발굴, 스타트업 탐방, 창업 멘토링 등을 통해 기업가 정신을 키웁니다.")
                                .activities("창업 아이디어 브레인스토밍, 비즈니스 모델 발표, 스타트업 탐방, 창업 멘토링을 합니다.")
                                .ideal("창업과 비즈니스에 관심 있고 도전 정신이 있는 학생이라면 환영합니다.")
                                .build();
                intro37.addImages(List.of("https://cf-ea.everytime.kr/attach_thumbnail/285/80038878/everytime-1769666102815.jpg?Expires=1772369379&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=PMmglM2-TUYwhJpI0XQyykW9szOKEaoEUnidfKRhgF3h7jiyQ3tSzd9z7gztwYrOrnc~oqbaaSrcv4-06ScGNcG7Vq71Irw4HOIt1uHbZYKmbO5R3q5xTsxeq31nY50ylAdMWsg-SccBxo0pwdPrKXC56UecyfXT2E2HQ7IRuhAbQPc3~mv-~ZqIkSNUi9b6TQYbq0wDsTnOELKWnkZB3LKEgUapyvpnl27B~T-oIoG3XUQGPVJjkY4v29lPcj1CgjaYkrkBxkDDRZ6cQOsla3KdC-fBjo63XyU9mk01SKRv2CrXIBgDloBC~j5Sld~JSxS1ZX-qVNkf6tP9MwQvow__", "https://cf-ea.everytime.kr/attach_thumbnail/100/80038880/everytime-1769666104041.jpg?Expires=1772369379&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=RsKomIlp3rnIMFEIQnNzdA2sgqxCAu7aX77DV0m3YajgJ-GPNuVYvB2wWhu9t93mgO~rHhnGNj9FsPnZBdNovUYC1bA9Bw0I3WtudGINzIKR6SMnmk0Qk4Ilqrd~1Kzyhk3PsrEhNydZ3KCl0kxJ-fQFjRUEE0FNkY6K0TMZp2FB--3rrsaNPyEzw~CVK2vsftAPosE47~WTJuCWfIfXyNgob~FonkRg6toWVG66ry4KkBKtZ1SPQw9ouci4vtlzLKmS3os53FchcBM9-n-Uj83m9Yj17XQ3P7qeSo5I068KPnmTUwp4a9~Ifwr~Ii0yVtNSm6U~WpXmkLwVHjg0wg__", "https://cf-ea.everytime.kr/attach_thumbnail/146/80038881/everytime-1769666104669.jpg?Expires=1772369379&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=rTwih44jwcx3-AXTpwg3nGkcN9np6CkzVUdGtxUJz6QLH6FwTQoqjfFkXNZavctYekQm4DSJUYbGLLxLqJmVxjS7zh9JsP4eyLPXZMzxMxDcOZlfGx9Q0rfECqv~0p0KtWAICd5WSy7N4YLuwJMmGNbZwNkG3odt-2nZVx~E5e7uwA93lPr2eJTgQSOIDabzvYBdzd5yJfLOqmDDtAxXDSQT5tTPGoi8jcQ1H6U9T9t-flLs9WKuhFiNQq0O74jUsQ5maq-99CGEunzk2T5hsu~JVLQt~7YL0pDQU5-XQ8GuvZD2QkHJTYq5~vvEdimxHkmFXIoSHmnyRXdVV7W05g__", "https://cf-ea.everytime.kr/attach_thumbnail/372/80038882/everytime-1769666105180.jpg?Expires=1772369379&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=JJRZvYGV-gWB23547qnRd3KCo2trfQaVhXtrPFUXoXCFkIwllgGBif0TtkYwMJ~7HEGDaPN~e70IHQfBQaK8DXVedYSvQhl~UbsmGZX9v9INYlDX67N7UItnSWPQOE82mQtcgKP0JiwsvEft33qiJ35p8QVJhULjQwhObbBIItFKQX6dpUcq-a2HeSyh0u0ZBrtHBzQ0qA2QcD6jZmg2jPM8LfPWPzca1F4SFstObfzvQETN5inoio0NPl4pK2svHSERTU3q444FiWbSqD~u-r2rFa9Jj2UaBObnwZCg~58ivZLk4aHC~nF9Q95BzbJXlFiu0ZCkwSQ2U-hPNtC0AQ__", "https://cf-ea.everytime.kr/attach_thumbnail/985/80038883/everytime-1769666105690.jpg?Expires=1772369379&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=jDBRRx3KWtxDRUnce7Z2ejEjedaUKkPQqxlIg1u8M0o2-u-E9p8csJ3jEK10w7kDtPx68q-U6Cw2vcYwBVBuRcOrGmS6Fg2dn1zsavdNH5imXjrUIJrpUXZE3Dn4WLZhC2MKJhP7AEZP-MHc7J316ZGi0gcaTBnm0ZuGbL1WOviPPVCs7lCj6LdS3V4FcLuVtZiN-u4Gv~6IKz9nGilxzjh6ckMEkSZUX~g0opNZ-UyHpoa3xefiXyH2NvS4wn3vcoS9WAMPZxzTMVgPmgDnUVByFMtWIArCxXHLt8P1TZTDdtxHWJ07TD4orV6~Fp0KrnwfdiIJGxEbPfonhbfvAQ__", "https://cf-ea.everytime.kr/attach_thumbnail/645/80038884/everytime-1769666106175.jpg?Expires=1772369379&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=cbBhMBrZ~P3Yy05P6PbSbKr8ngQdZq9ezSECruqUuUpWSDjKI9qlXcS~MBhTaqeXzRY6H~GLbioCSe35XrPwfGIBvIJMy~wTRj3Wzjthrf5vgEwykXlLQEYQtjnWJ8LoVt5m63hTqwuhaew~NnEF22Vvn8F6JKwT0nJ4y8cjvVTF9sDJ4EwD0qJPsWLD7R9LteASbcl51fmj8xNt0BuqfCtmjB4Yk6ohwMWsYjNCy4SOh8DH0yx5c84uAW9TR4lARMtoP5CpDR7-sDP4OxBMrazW7VOogMaFVbGH1ezA-BGk0Jce8gWqFMeBrzG6NAe0G3ub2uTcXKpqnSAxGRB4JQ__", "https://cf-ea.everytime.kr/attach_thumbnail/482/80038885/everytime-1769666106675.jpg?Expires=1772369379&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=Ah4UMJgvQPstdyRTf5Ih2P7EJrKDAnAb5VPBqnWVAuZjEceWA~SRpFj3TjBEmDE9Wo6flR7EckSY~GJP1273gUnYrLhqwaSwtQOc4~Mzc8Lewsexe610bS07BUeK6U-tiYtscHOSeiuffNxgw0f8mKiQVLkumc6s9sgDf1CVl9hod1Zzvr~Byrx-igw54b3~UxXP2uI89ve6fstD6PLHrqxpmgOR-nVIsHW6ur68wzBGEjYYrkzKXY4TG03l~pDlJR6e05BdE2Aqcatd02XbClLgU5axG0HnTW9cbT481GF4omAau-gl-lnxzkyF4r~4ie3vR-BJ~Kk35uUlzeMI2w__", "https://cf-ea.everytime.kr/attach_thumbnail/923/80038886/everytime-1769666107166.jpg?Expires=1772369379&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=i0ApUJhCNOaqbOinhAR3d1PilV6ETci8ljsp30aITSEdBSxsKhIr2~E35Y~ShIITe1AUSXQCgRqKgJzLwVLeetNxx7E3BvIEErsbWAoDdEle0KUkQaC0fdjpcxRMufFQS2C0rT8ld0R9mGvFeDa-2lQHOCuMhmGoNAtLiE-zNDZXg7BJrJ5HqFt3Ex7WwjiVshnNOSZvNRax-WfvlOhIFE7t-MjzEXamUEYU613hRgoahOn4ZzX71Dnw0FdQVV-A7EeGxeuTqMGl9yrkVmdI9kkF1sZY1NdLxma4pGFDLfJFC2YJ9VT9RWx4Hn-NMZorIdhIB57BolV3NEfv6GtDQw__", "https://cf-ea.everytime.kr/attach_thumbnail/24/80038887/everytime-1769666107821.jpg?Expires=1772369379&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=UtUOOZh33RpnzoyAKLu3LX1VPq8G1oZU6Hy~oVpiWXlH0HMLpHKQcevTBBj7cJGTHiYrfbf3PrvrA9pohqjsP656VaQTuDqOoqhR2tRhB4ScTDKv2MhCsaOnRBN-WPPp7ILY8RipA8amBeHHt~lj9RGrjUV3VX8CQGXMqS52aZrESayvjdpFEHDNEqaBiI2zas6Uzqe3rgeJcqZFEq-TcKE1JF3Ot42Gr8uNGOOG2BL3uaTIV5mon9ogYwbXQaAAd89fpEGDmGZzUaw79A75-a4PvAI5wyXIB3JrxNDO6RFXyv3YnGYJ3B5pctUzwmFoZg1ZOvibF4~J8aAKYi6ntA__", "https://cf-ea.everytime.kr/attach_thumbnail/440/80038888/everytime-1769666108396.jpg?Expires=1772369379&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=G~ryDvimynlygQvdaXyNmjvoQ-lB~nYhTKwnx~EcanT3LCAw9RdNrKToLYmJISUpEs6DrPpSrr0lKlltXnlcVXxv6-PLuThKHbTOWvgP4c3ZH~VnPvnF~WkwGNu~vGh0MVrszisYbMK1Jx1yoG1bTA1XpD~p~a0~aJ-4EtHFEvK9DqV2l2bbRHS2XLuV3G52G296hygACgwFEMxzE8aIau4bFaweGkzBHBvzTQ34alSiKyWO8k-rMbwtfZCajOlNyJ5GJo7lJxyafnjuHdWfkubAaBsmNaJWMctdhMrjGAsoTn1boTCXhWib95brvRXz7X085AtcSCCKJj4SRjWmkQ__", "https://cf-ea.everytime.kr/attach_thumbnail/425/80038889/everytime-1769666109161.jpg?Expires=1772369379&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=igyhsZKulNvD1Xzq2UnMgJHqIIwBEpI5DLhmcgPC7Zdrh~nxWt~2XX7NrxLVLS~gelI7OqhxjKP6bDBSPiN~nocpuqBhI6oTgPvsNbUFrEP31zYZ3uXJSzgzSMt6mZkP1oDH5pLS-qAknMlIgPmbhtQ2UGlJoymGaxQt-cUgmFd~qDuv-F6QflDlRZjc9aPxu4ujbgTaIMYBa2aTTKAD4StCYaRhhIRDwCiRUioIk3NeIEiMClWIrOPcpfSn3YF7-CZIf0LcVXmwK2fk8OQ5pPt5eRdMI24N5xennecam0eWvfihzaHitYMMll0vAG8sjMo6ZDV07spton9krj3r-w__", "https://cf-ea.everytime.kr/attach_thumbnail/505/80038891/everytime-1769666110074.jpg?Expires=1772369379&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=YzTkD92Va66nOhVEGBnqpHX0QGGho2eF3lDaeIWxLKIjTodWKV7I-tApMkZV1cryRVvwyp9ZDj8kFvJSo3m5bLEoqwoBtk8NvdArkJngid7~in2zUdDaXDEPm1cdXDdYpE3n51fB2djWyiCzZog7Ybyeb2YJ~TT1LiIeslPtexk6C09t3FejIhXLL-XNgYMsbQ2T8h9OZagh3wiYQzBBTWVtOwiOyMDTs9E982LXPhq9-4v7MCbN70p4unwFLqn4qakkqtqt-tGXYun6HhpqYCkbOpruorf9V34HWUziOFz24CTwChJ0jbz0VU0DKpzlp-OZlTK5RP8T7~SQe9VT5g__"));
                clubs.add(Club.builder()
                                .name("!DEAS")
                                .category(Category.STUDY)
                                .location("제1학생회관 412호")
                                .shortIntroduction("창업 아이디어 동아리")
                                .introduction(intro37)
                                .recruitStart(LocalDateTime.of(2026, 1, 21, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 2, 1, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("창업에 진지하게 임하는 자세가 필요합니다. 정기 모임 참석이 필수입니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl("https://everytime.kr/418923/v/399004379")
                                .googleFormUrl("https://docs.google.com/forms/d/e/1FAIpQLSfZYav-qzy2ijGNvYyJk6rneZtM8oDD5tvFAdIu4EA-tHlK7Q/viewform?usp=header")
                                .build());

                // ===== 38. 오월빛 =====
                ClubIntroduction intro38 = ClubIntroduction.builder()
                                .overview("오월빛은 전남대학교의 5.18 민주화운동 역사 연구 동아리입니다. 민주주의 역사를 탐구하고 사회적 연대 의식을 기르는 활동을 합니다.")
                                .activities("5.18 관련 사적지 탐방, 역사 세미나, 민주주의 교육 활동, 기념 행사 참가를 합니다.")
                                .ideal("역사와 사회에 관심 있고 민주주의 가치를 소중히 여기는 학생이라면 환영합니다.")
                                .build();
                clubs.add(Club.builder()
                                .name("오월빛")
                                .category(Category.STUDY)
                                .location("제2학생회관 309호")
                                .shortIntroduction("5.18 민주화운동 역사 동아리")
                                .introduction(intro38)
                                .recruitStart(LocalDateTime.of(2025, 8, 6, 0, 0))
                                .recruitEnd(LocalDateTime.of(2025, 8, 20, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("역사와 사회에 대한 진지한 관심이 필요합니다. 정기 활동 참석이 필수입니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl("https://everytime.kr/375208/v/387001786")
                                .googleFormUrl("https://naver.me/F8u7bJuJ")
                                .build());

                // ===== 39. 인액터스 =====
                ClubIntroduction intro39 = ClubIntroduction.builder()
                                .overview("인액터스는 전남대학교의 사회적 가치 창출 창업 동아리입니다. Enactus 국제 네트워크 소속으로 사회 문제를 기업가적 방법으로 해결합니다.")
                                .activities("사회 문제 프로젝트 기획, Enactus 대회 참가, 사회적 기업 탐방, 팀 프레젠테이션을 합니다.")
                                .ideal("사회적 문제에 관심 있고 실질적인 변화를 만들고 싶은 학생이라면 환영합니다.")
                                .build();
                intro39.addImages(List.of("https://cf-ea.everytime.kr/attach_thumbnail/51/81130982/everytime-1772358104607.jpg?Expires=1772369705&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=ft0qM2JJ3U93Y~YyOEm2HpKe9lnSHpS-zbPWAwXW-uvFDgyLMnnNwM0sj479hQsjkPBp1rz4MbCsMiy3J5fquYAad3uxY88oi0Hu1XTtQkNPj9eM1NWdbnlN7jFUzQUtioYl3DlKwko6fyTLcqyDCJgmtVUpmHpQAvxJNC4NzRvvgC5VGWLotfXEf9PWJE8QRsh7Es3DrAw00m334NyT83u05WZ~SqU4ubz79dawTdhIasWE2BgUb1sgVuIjbwEU4sXJJgm5a~7isvGu61jYFJxya-VCTUyF4NwH1~TW7Tdz2gUOVLqvXRtV5~jH3Y92yX9d62RAfAvERJ2PUnF~xw__", "https://cf-ea.everytime.kr/attach_thumbnail/571/81130983/everytime-1772358105527.jpg?Expires=1772369705&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=UJ4OQUlvvGiOiG9IxPuCBPox5jpGga1Fbw69wFwoiqaDOPbbTsjns6TmttR82JJGBrCs4GHpPw6BTYH6CztXJtpuZZQpoSrOtPLs8z1nywhnPYIGWqiF~DD7K0Nusq1TA7VTprWS5FlTI3UNZenBQDJ9PfyH4MYQcrUVe7HDd3QzRGjT9sPxRqKBFj~9A5dlfXRRLa8gPDSiqmtltLLIRuvY9k1O-c4A2UltY9OZR-aU5UvzP7WfBueMTcNPVSnBbEVvhVCk-T6rV78yH35M8ZgyMAOQq35nfLIyKN9TRs8bMYSclGuqo~vLjMKQ~RxA7Y1FGuSZlkSaleOXMwTrZg__"));
                clubs.add(Club.builder()
                                .name("인액터스")
                                .category(Category.STUDY)
                                .location("제1학생회관 409호")
                                .shortIntroduction("사회적 가치 창출 창업 동아리")
                                .introduction(intro39)
                                .recruitStart(LocalDateTime.of(2026, 2, 25, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 3, 7, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("프로젝트에 성실히 임해야 합니다. 팀 협업 능력이 중요합니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl("https://everytime.kr/375208/v/402347009")
                                .googleFormUrl("https://blog.naver.com/enactusjnu/224194545735")
                                .build());

                // ===== 40. SK LOOKIE =====
                ClubIntroduction intro40 = ClubIntroduction.builder()
                                .overview("SK LOOKIE는 SK그룹이 후원하는 전남대학교의 창업 동아리입니다. 창업 프로젝트 기획, 멘토링, 네트워킹 기회를 제공합니다.")
                                .activities("창업 프로젝트 기획, SK 멘토링 프로그램, 네트워킹 세션, 비즈니스 경진대회 참가를 합니다.")
                                .ideal("창업에 관심 있고 도전을 두려워하지 않는 학생이라면 환영합니다.")
                                .build();
                intro40.addImages(List.of("https://cf-ea.everytime.kr/attach_thumbnail/698/77970523/everytime-1757124896282.jpg?Expires=1772369812&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=I4pJCCs6CP~bcwYwzicvEeYStwAk0iTgXiCk~ruiDvYHtQQU~QM2ojjOVBXq05W5TFDs01obdb8JZLaFEPjvgBRiDuCnnuliDFyV3XHP3eZFa3fvUxZ645aJmgOavUoQ0AeLoSiQ8ZkU3K8~Ng4lDsPxVeaMnvj2dG4RCeMIWQ~nLleJm6bMXkRCvI8IopTfuhAeLsyqHYGKS6hnLyFk5dLqxaSuflNIPLAdLjNaFa52TMsyKJt~nEkjvSpYtS~Xjo4Z8lQZg0YNt-v3oU8d9xh4d2JP6Z8LW-HdnvRovhazmSEVf98MYh2MFdB8gWG6nhgMX2FL6NNfY0qgvm86qA__", "https://cf-ea.everytime.kr/attach_thumbnail/712/77970524/everytime-1757124897290.jpg?Expires=1772369812&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=q9vndtQlLhfrMhg5j3ggi6TxIBZtqOyukatBG-OWe1CaUv2V-O6z~OnwMZHNeFOuavRLBqcgSRVjXABv--C7RMBYZx0chGjQRT~77VTE5DPjJBi9R~i0rlvoLMyFVFYMeo1eONONvrG~LSQ7Zgv5u4aUu5hjbCa0ls6ogF2HeWVivwrtniMiGFFK-G2qyN1NBcGW9V9e7Qri-vsy73zj5z3dVpw3zPORXaFEZ76uT1PB7QCBzM-janNbnBWs3SMw62zFmv-aD3RtV2BIqpTB003o5UNcPFD6NSEf2BtKbA63~Hk3dIpgHX5wS3PZL222nJOPjykJKEMny3BgeLLlug__", "https://cf-ea.everytime.kr/attach_thumbnail/216/77970525/everytime-1757124898008.jpg?Expires=1772369812&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=eYFL40FHa9c8Nf8E7kOnU8ZvRzXQx6li3eMZ7yBbSKAHTvQ9Kc04JKcYHBW1f0p6d1BSmZVqnvIX3lKPb2SRwgZ2pbRyrqyYrG-BQouBBUN5AH3NokrgGeO2JnQ0gZ6Aeb8xME4IR~iTis79x2zn31RyhKbFwTpwjPuQSFw5pGV4sX9scGMXVRtn3fAVQMTtdwP8vE~qp6VgdXGoWWv5d-wg6Webrw~bGV6sRMKYu4-nT9~cUUVDX4M~o6m8T2YQhvSGJ5Uq9ETSHQihmJFkqjW-nPgugOfW0xGvoHwWZOLzTRlwFwr~gO40hb3ISn6jH0GvuQtPhc2xB5E~norvfg__", "https://cf-ea.everytime.kr/attach_thumbnail/67/77970526/everytime-1757124898671.jpg?Expires=1772369812&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=DOZsKi6kMeSxH8aZnBkUG4PDKn98n4E~i7FPt5UXBTsfn39aTnLl8LXB1Fqf~0AFZgW030g6H8yIMkMpGKdqVlQ1auJTqahLM07YGsuKpa37uiyJdFQKELiv0EahzixjBwyWV-mzmFO07rlbicLWTx8HE~sY-CUWD6JNwnQZDPsaGt-FDo~zxhQZsOoIMjZqS5xnaonDC~FNI9slilULNXDEBNLkI8i-CjC76~T0wwETvIXt3v55B6FvhvrfNNZhobEgXhVvwTXCcqXA8ZvtX1DPfEM-YrVvN57n7LYRjpqo800IpwjBjSSir7MDYuAZEIAn9XlGYm50JzuJRtQaig__"));
                clubs.add(Club.builder()
                                .name("SK LOOKIE")
                                .category(Category.STUDY)
                                .location("제1학생회관 418호")
                                .shortIntroduction("SK 후원 창업 동아리")
                                .introduction(intro40)
                                .recruitStart(LocalDateTime.of(2025, 8, 21, 0, 0))
                                .recruitEnd(LocalDateTime.of(2025, 9, 6, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("창업 프로젝트에 적극적으로 참여해야 합니다. 정기 모임 참석이 필수입니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl("https://everytime.kr/544806/v/389222891")
                                .googleFormUrl("https://naver.me/xHmD3RSQ")
                                .build());

                // ===== 41. 에코노베이션 =====
                ClubIntroduction intro41 = ClubIntroduction.builder()
                                .overview("에코노베이션은 전남대학교의 경제·경영 창업 학술 동아리입니다. 경제학 스터디, 창업 프로젝트, 비즈니스 케이스 발표 등 다양한 활동을 합니다.")
                                .activities("경제 스터디, 비즈니스 케이스 발표, 창업 프로젝트 진행, 기업 탐방을 합니다.")
                                .ideal("경제·경영에 관심 있고 창의적인 아이디어를 가진 학생이라면 환영합니다.")
                                .build();
                clubs.add(Club.builder()
                                .name("에코노베이션")
                                .category(Category.STUDY)
                                .location("제1학생회관 428호")
                                .shortIntroduction("경제·경영 창업 학술 동아리")
                                .introduction(intro41)
                                .recruitStart(LocalDateTime.of(2026, 3, 1, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 3, 10, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("정기 스터디와 프로젝트 참여가 필수입니다. 창의적인 아이디어를 환영합니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl("https://everytime.kr/375210/v/402118383")
                                .googleFormUrl("https://econovation.kr/")
                                .build());

                // ===== 42. UNSA =====
                ClubIntroduction intro42 = ClubIntroduction.builder()
                                .overview("UNSA는 전남대학교의 UN 모의총회 동아리입니다. 국제 모의유엔(MUN) 대회 참가를 통해 글로벌 이슈 해결 능력과 외교적 소통 역량을 기릅니다.")
                                .activities("MUN 대회 준비 및 참가, 국제 이슈 토론, 외교 문서 작성 연습, 글로벌 네트워킹을 합니다.")
                                .ideal("국제 이슈에 관심 있고 영어로 토론할 준비가 된 학생이라면 환영합니다.")
                                .build();
                intro42.addImages(List.of("https://cf-ea.everytime.kr/attach_thumbnail/814/81047065/everytime-1772189091534.jpg?Expires=1772369568&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=oUDCRY5AQV2OVvy5U7y~farwTB92d8fXI2WHwyT-FyeLR7ET5d~HtUTunff7RnCbDYSf~YCM5nKFUh4ANWsMZVyA2oQrXdCA8mnhjoWc19cooQeWUTDopKRkT1bOI7dxFHPDTi8f3tR3wUJhbeaZV1-ArvzgOY~gHefcp78UjUDaHsPBNDaIQu4A08tShYAExp8OyhuXs0lUw-nrWAyyJCeb3~c-QsLJssfDPQp1aNCkOzALqAg9NuIflqPEvomtfwm6vAxgOmVcjfGa5iBfEyIrbzag7TyfwMJjsPghFb1XD5pb4OZURHDfMV49tICDznOpSsd2ntI3jaj9aE1mLg__", "https://cf-ea.everytime.kr/attach_thumbnail/390/81047066/everytime-1772189092386.jpg?Expires=1772369568&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=LWFWQtlcRWROREgfZjanmRJOc2ID50U0CabvXirRlBmbbBAVMUiUt0zLFO3D5Unf425a3qFwZENUTIu0Qg9cnmyHlsWHmx8rJ~MbMpn2NjornEeJrqn-zErNw3GJjdgY1eOAhm1GO8ru7Ah1fAKccHs7l3Q5fJNfE6sgU0A~SmX3elpe48Arzq237Cw~J48RNgJCvaLouoMChPKV-Q~re0qHeaG3kIJMxnld1Ba3liNicnemrZdP-i--CnGCm2giP3riAzk7ymTg86zfbFQuE3Znzcd2-rsnopwChijq6YdwB6~Anzjg7kWGPMgQY5-jMW7pX1ae9A-qSoHnzxpKOg__"));
                clubs.add(Club.builder()
                                .name("UNSA")
                                .category(Category.STUDY)
                                .location("제1학생회관 419호")
                                .shortIntroduction("UN 모의총회 동아리")
                                .introduction(intro42)
                                .recruitStart(LocalDateTime.of(2026, 2, 27, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 3, 7, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("영어로 진행되는 활동이 많으므로 기본적인 영어 능력이 필요합니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl("https://everytime.kr/418923/v/402102895")
                                .googleFormUrl("https://forms.gle/DBaKeqC1TYE3tbjg6")
                                .build());

                // ===== 43. 대학희망 =====
                ClubIntroduction intro43 = ClubIntroduction.builder()
                                .overview("대학희망은 2008년부터 유기동물 봉사를 진행하는 전남대학교 중앙동아리입니다. 광주광역시 동물보호소와 전남 담양 동물보호기관에서 유기동물을 대상으로 봉사활동을 합니다.")
                                .activities("동물보호소 봉사, 유기동물 입양 캠페인, 동물 복지 교육, 펀드레이징 행사를 진행합니다.")
                                .ideal("동물을 사랑하고 봉사 정신이 있는 학생이라면 누구나 환영합니다.")
                                .build();
                intro43.addImages(List.of("https://cf-ea.everytime.kr/attach_thumbnail/105/81115643/everytime-1772340337600.jpg?Expires=1772369702&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=mzwHT0sqIyR9h4HHG8EzLsMhGqtvHNKvtmVf3VrMLUsTMJvCN3HdpwNdUWKtO80U73EgwA1yMXbt-fi1PoIvxRFORFtG59p4g1yKBFFYVUsBA8H7XS8WEPYBt9UbS3RnlKGH0buzsWPBea4YXtONuxURIxgL8pJynSUhcOAU54GSGogyGRpeG4mX6TTjBRNsL1CJXXYfdpBiKpMsu1g2ZPxrcVCTmEu73Rauvp-DOvoFgAe~Rh7o03GR77hc84NgPgkZOCxD2bbuc-MgJctVmihUTAuRWIo~OnVQ~0sNZIz--HMI0n9XyTOMMp45ifY2lCLCEEHMK5K1JyFYuStA7Q__", "https://cf-ea.everytime.kr/attach_thumbnail/624/81115644/everytime-1772340338060.jpg?Expires=1772369702&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=pseJMU5CQUEhmkskC40LqUBsZwNl6hNcwDSbmNljrR4o~R-lz0Q950-JJvDPu3kD6cc4yTnzdcDD8yiORifx3NwDscBhsIVaaB7FPZRFxnQ07gN~WILk9GHpmhJtyMZx2WqUsBGldQzWo0w3ltlTeythZCy69BsO3i5jdDtufDHySt155kunh7URo5xtN82lKiZXA5Av2A5unR4ovzP2VuXFDv5mvrET3s-vmuFD8D-u3hX2tR2kVIAae8W7lEEEjblGgzRPuckbJmS8S-2-I5mb3j68LVh3Gcy6K8gTT9h4WaYL8sPZRcu2aorxWeMDJfTfOd~wg06qlp7TY0VZiw__", "https://cf-ea.everytime.kr/attach_thumbnail/248/81115645/everytime-1772340338403.jpg?Expires=1772369702&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=lnk9-I6qztUzs8k3v3i9h1IE8Y4JY8gP0tSKA4aDyJ-L66XA6K0ZA5EU~A6xuXp2sw-gqiOa5impSbr-53T4hFJ6AXlVRSUiPsPo0vZammb2t5qbgFzgFEozHIpvpM~9cBjauicfKTfs8cIygXPBIr4QIEJ5SnIFll~LMlGfSYTacWOBX2Mhtc9205fHesK77GT3yw2ItJKufiuHFP~rwwPQJAv0uaVTTMOQTUDtD5boXhcXzyDOcylndVFK-1dfkBLK66wKTZTfYcAd53eWW9WGFb4~eQl41b4oHWjox8cLQxJR4hpqF-7yaxDdwaeXThSxt~CxiG~T7Xbdsiwjaw__", "https://cf-ea.everytime.kr/attach_thumbnail/893/81115646/everytime-1772340338703.jpg?Expires=1772369702&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=VbLYYbxMCzGyPO~1TTFOc-iqgPZwPAiivQvNcL6GWsZF~PHlNT9ysN12VcnSwcpqkwJAoOjk~fsbokpzZHorNSpyM7Cg15OwgRVy3MAFeFU7j3vZht1cgpuI50G0G~~Cg4fL1NKTuwa61uymc504q1p1EdATDCfAzxZQVPOFr9xLuXpZo7bz7X86z2h0YZAKe2J~BplztM~-SmKTbyBIp9i3xXAMdDkjg3kdYEuMk9M1yvMghWtJ1ZM4cvvrdryT2ArkszkSRkzr9IbTqSqn-mENGir-S51WDgaX9hqtwlwvJindXZP8KQyOvpTB-dd8HVe2uBvlm93s0RIneDX2tg__", "https://cf-ea.everytime.kr/attach_thumbnail/943/81115647/everytime-1772340339041.jpg?Expires=1772369702&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=jMlobv4DitHHi9eDaEuuApHVPAUuhiPTOCPVwoEo7Zx9pNMqopRFLF5YaiIzdDAHk4vUVwmA~be5mCWaQh3aGaHwa9vvTPxtutP3Lw0c8n1jhigoYCo48CzmJkzYK67IyeaUOrL-Q-T4jlxbNygpyIlwvHSCom-6gEVEigJdA5iYBnyGObIsFHP601XkFJunMk-YhJ59pjuI4ZLNJ-BB-WkYvVC81oF3hIFSyR4jcIG7CfKGs1GTEVjiA6V25Ca1Q2k78nnHYF6i-hwQnRKxD082cAWLPENY4vreKdMxWD9oer0W7g-nwxFYe0Tj6O6vj1PfS76o2qxi86eGgw7gtw__", "https://cf-ea.everytime.kr/attach_thumbnail/439/81115648/everytime-1772340339380.jpg?Expires=1772369702&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=emrpLx2IPc4M-CsGK7V3pLxFBYX5QDPNKTK4zl8GXAY2bMHK2~3YWdjvvmRzkp-8YCgVbpv77XlmGO-qX2vgg6aoMm1jjGJx6HtfaqrQUmaN9F3ZUzm9UvV2wlkQVV5YpBmFgme~FwNuOc0ASMAhZuFtG1CrPKzIo53dG8d-tBgDpfT41aNa1nf2M9VOcFoPYw~lqHzMAk7OlxtWzHbugBFL5Od-K7mvDDANPvByo8qTMqvG~I5du~jp8x62~Z95RBq~Z7nDXxTgQ~VrIx-gWNM2JX7Ineyiqy6nBsoBseB4HHlCJveF4BWsDKCJitR9G3A-m6zIYnHdkVza2DCs8A__", "https://cf-ea.everytime.kr/attach_thumbnail/155/81115649/everytime-1772340339659.jpg?Expires=1772369702&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=FRaQB~4q4vKwYNGokPYq58yo5fcL8-cWOwyZwnzqfGD-dpjVKGDcuVmVWrZBrw1NA9WzYLMCiN~-77YxJJZvfD6e2ZEtXX~myiaxdPeV2REhFSozt6uMbcjk8KtW2q0s5BPRuv1sF9ZiGJzCECv3x0HMuCw38p4q58trSso6C6-tXDlv3cbDpTWsP-iVj5VoGum5h6yBPAt9zbTzDNgp7go2KEyPldMdIkCTRehdLa6jSz~6PKerEVVndkKaIUfLp6ivUwtl0PBWaUwSIkHwN0UFd5BDYneGDRmEVqLx8-omZYfQqazLjZUmTTPLRsjkz3lu-gl6yES7iwiNLUlB8w__", "https://cf-ea.everytime.kr/attach_thumbnail/840/81115650/everytime-1772340340060.jpg?Expires=1772369702&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=aFgIciqbrnPV8B6LuCrMnA1pOAnONq0z-2OMbno0x97NMCsmrtEy8RL7xyaroru3NpyMpzN3wPPwNTyDeNBoyxdK7-K76vlO4L9UMVkEk2CrjwxuPuGH2mGYw8GG7EgXBx48YAZDkTcvS-zfLcv8GGqgQ764ATbUh-yxoXvJC9EDwICJcGoNqkL9SFX8kzA6ol3g~NTL1dgCrW5xjSA6GhjkhAvNNhLDnds1x59Qd9ThV7PMTMt7LK38QsoqlPO9QD07ecquDi7bR67xuLVQcMVksZb5Go184Ck3JM7tMEuSF6vWjsw6PKz9oLJEzsYA-M6QfLX8JE0YnUAyHwn9Uw__", "https://cf-ea.everytime.kr/attach_thumbnail/609/81115652/everytime-1772340340379.jpg?Expires=1772369702&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=ek13Afs5vsd0F0dA4jJkVAQg57zGiCcaq55LxYG9RORyp-APnhtsIlZxUXOH6Be3LM~4jDBx0dPqnjhq95RJXDbEov78zqaQuLCdBaABEZBurgQIm1gkVGA1DWEWP6LieDK8TMn9lZl3C3UKbTL2QSHnrqQ8URMnHKJ9dP2VN7OGv6oMqL4W62rxzaAq5q9UGQmDQnQyCo3iWbaziYL6SiCaMOi44fJs9MnacoWRTvlnYCSs4ZpuZY7KSFpZ6jGjCm8ZK1RgZagVF3S-wDZVPmL85xSh5hvBncAsh3ylca9SDkUck9LiOmMpTBxeZ9knElt6m9NZoAfK3wYDVvmgUw__", "https://cf-ea.everytime.kr/attach_thumbnail/495/81115654/everytime-1772340341444.jpg?Expires=1772369702&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=gi-xB5RUAO6K9yc4yuI0qQk15KchWpDOFjUwtjFkoEWjB7E12WgL-~L-SkXSikufPDDQhSUal89BAk0nr6ooAFKqR1wwO~NzUwILGQWL~EJk25d1GeRV~YLqrjlMr9hDgBxf9zFCztTPLEfgvs1~SnBZdY5qnXqwMaf2zQktzkKdeIYIqi9N3j5Gq4gSyg9mOmi0o1uZcwed~6fU2fy4Lw6kRy2WRqinKnu3fjbTZhNHFeYAtVxDLjh0r2zDKkcbAILBD~h3vYFrglUav85bS0Ipt1XBkm~NhGQtmMBkcGTFA1l1lVU3WMipAgQiX~7PYkCB-SDk6BQ-TlfKwPB1AA__"));
                clubs.add(Club.builder()
                                .name("대학희망")
                                .category(Category.VOLUNTEER)
                                .location("제1학생회관 401호")
                                .shortIntroduction("유기동물 봉사 동아리")
                                .introduction(intro43)
                                .recruitStart(LocalDateTime.of(2026, 3, 1, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 3, 6, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("봉사 활동 일정에 성실히 참석해야 합니다. 동물을 두려워하지 않아야 합니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl("https://everytime.kr/544806/v/402308422")
                                .googleFormUrl("https://naver.me/xpjx3XyJ")
                                .build());

                // ===== 44. 두드림 =====
                ClubIntroduction intro44 = ClubIntroduction.builder()
                                .overview("두드림은 전남대학교의 사회 소외계층 봉사 동아리입니다. 다양한 사회적 약자를 위한 봉사 활동을 기획하고 실천합니다.")
                                .activities("복지관 봉사, 멘토링, 소외계층 지원 프로젝트, 사회 봉사 캠페인 활동을 합니다.")
                                .ideal("나눔과 봉사를 좋아하고 사회에 기여하고 싶은 학생이라면 환영합니다.")
                                .build();
                clubs.add(Club.builder()
                                .name("두드림")
                                .category(Category.VOLUNTEER)
                                .location("재1학생회관 309호")
                                .shortIntroduction("사회 소외계층 봉사 동아리")
                                .introduction(intro44)
                                .recruitStart(LocalDateTime.of(2026, 2, 23, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 3, 8, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("정기 봉사 활동 참석이 필수입니다. 봉사에 진지하게 임하는 자세가 필요합니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl("https://everytime.kr/418923/v/402325819")
                                .googleFormUrl(null)
                                .build());

                // ===== 45. 미담장학회 =====
                ClubIntroduction intro45 = ClubIntroduction.builder()
                                .overview("미담장학회는 전남대학교의 사회취약계층 장학 봉사 동아리입니다. 경제적 어려움을 겪는 학생들을 위한 장학금 지원과 멘토링 봉사를 진행합니다.")
                                .activities("장학금 수혜자 지원, 멘토링 봉사, 펀드레이징 행사, 학습 지원 캠프를 운영합니다.")
                                .ideal("봉사 정신이 투철하고 장학 활동에 관심 있는 학생이라면 환영합니다.")
                                .build();
                intro45.addImages(List.of("https://cf-ea.everytime.kr/attach_thumbnail/383/80688424/everytime-1771591488861.jpg?Expires=1772369986&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=KrL6UfShI4W9My8NneHDwq5aKzVgjgD-Te~R1AFbVmCAyGDzT7tBVgS1jDyanpimMjoe6PLEPMleuZ38Am9sf2Pm3w-SkzbKqqKHqHP0Och-uarGPH59egQWrxc~nhg-2r2BLvKVMWqxTDkhd7HCvG2NsadVdAM06Tsn9GvTNpM9YEbKESrxv-ii6AqixxgbOwLB1WljcqqLIIe55GioNWTUp2f87vw6mH1JY0N-eoTcQloekmBP-iuNtBq7yAzHxsrjpllTQurd5~syfAlJS3~O5-31JEMBMXLYsGnGLpLSv1ByS96JiDgK6swEtjUe~RR-~DsqlV7flb-04kDYUQ__", "https://cf-ea.everytime.kr/attach_thumbnail/213/80688425/everytime-1771591489640.jpg?Expires=1772369986&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=j5Taz~b9ZhZnHAaTmPs76htTz4bhS2KsQ4hhQf6RpnH4exMOZR2829Kv4qZvOxrY9sxyZyYb3f3SQP8qVmRNe1PZ1tK173eiDbkAwJ8fWdI6wFZVxHSHSNJocCHiWXsOF4GhA6VStxSuCpY5cMRbCHQedPhzrcJrVRpkmCsddhaBOUU30Cbm6L-WHCMQ8ijzT90mPyLGz8YDWUIYjSBCBxLuNpIpwokDw384MjH6i8Du14qcNnOfpmARqsJHtoNhJhU9K4nerrGGU-4vHQhMLGZwlgCHi2wpNOVXUdA4KC4TUlAbBTJVGkEm2E-2wUFR-TJPp~~LalPLuoSRjZLlzw__", "https://cf-ea.everytime.kr/attach_thumbnail/589/80688426/everytime-1771591490339.jpg?Expires=1772369986&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=PeueuNEN~9MbuI9v9vpQ9gu1KbFNb0cNhv9cH4ZF~ZW59PBT~yTUnRP~0Uqi6wW~lhvHYhw3E7JOJ9-gTAGJP9Cuwx4rjctSup-Ko8wT3bigOXQrtGR4DZghcz15F7Db9ciZx6NEXz0DVQaT8sWl2qvwRk7bbZaxNhSVJMX7rzH7RwwgI~KYYjM-AOCgXJlHlxxDINdd34J~1vahIGj9ko1-1KQPOs2yKbwUsn6srqm5~bohXPRY7Qd4I9cpWLEcH1ufH4L7EWBfSU3NdmAhve1tX9G~QJ1aWWcFhNDOc2GlwbNmSXWFWJMms4My-rkVzwNN~IaZPTvVs8uy~cEunA__", "https://cf-ea.everytime.kr/attach_thumbnail/804/80688428/everytime-1771591490980.jpg?Expires=1772369986&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=EMoCMwaz4~-c3uRO2TAiEFDTneBXsrteTGKeZUzpEmX2Y1bbnC~MpoLleXJNsYEnBmIfB8ZPbaUkiOgDawk4D6yEVAcC4qpB2FUfoql4gL6p7MIIFCyVOZx2ZYMS-d6Sm1Wm0buR3cr3GB-m4fw84UXjjovuX~M7hXWF6Gu62TjP2Lxxva9JjHmLXHT792OUfmFeZ3tSdr1PkTWr98R10G60o9UbnKvZV9e7KoPm4l9qjglpF2g73FqXV~LR9AWJwZFvUlJA2O0ogtzzQjn2pUIz6hKWXz04f9lnCymVrY7sOKk9kRMLn9iQmNb8ra3Bpn33gZksy0cVlAj6y2YqAQ__", "https://cf-ea.everytime.kr/attach_thumbnail/267/80688429/everytime-1771591491619.jpg?Expires=1772369986&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=FZ4iXYY15Ynjhv5y1y8GnqJO~Yggrf8nv3ngk4N~HG8JOqKcxvEuWIFZEi7hlTUy7rYK4klXU9mUPvfAjUEEL5MZfpx4lD-wqJNoZpEcPd8xmg1BAtBPjxst~GHYDcqn5OXz0ayguWMGOah9HGCGF6MFt9NdgDKBKEbbRPqXx4Xt6EgRVkcVHl6GO25akUIglBPoXq-tsbxrdUCTiHok0snUOEFkFcqar3oE-X8wtG-KJdSnrIpDXI0j2DkDhbe6fSMd-FP4z6FcqJZ5nRRn5xh8ovoL8x-d1Atj917gzov1SeI0OsaeP7Izns0ADRCMucCreRnjG~80iMZwQYY0BA__", "https://cf-ea.everytime.kr/attach_thumbnail/149/80688430/everytime-1771591492253.jpg?Expires=1772369986&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=BthIhx7rQ4SJtFTu5bpq747JpJjePBy58pJSbm46PJt5Ew1tIOy6N4~jQAupp9JQWQXUuCQDNBRvtR7yWTX8M80aqyg1c19eNfnYIXun5AXzLXtR~~zWEWodhr7uRGVqMdnxPRlduq5BNEiQ9s76TceiBrEcU5AvrkczLPI~jncw39CyF5OFVdc~0NiJtYZdbW9pVY1KmWHnUyPLyhh4X2KL5rdGIki1z7y6t~bj9yQyq0JUaie6XEwjUi2On4gt1HmE24Qb9s3CaVWvWCINRywOD7ds4o5F3bt06YijHRYbmDJhpnXi59r0yGBAX3Owiq15PmzHRdTQOHye7xVeKQ__", "https://cf-ea.everytime.kr/attach_thumbnail/573/80688431/everytime-1771591492899.jpg?Expires=1772369986&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=q7Xjbh8xfg-8mah8OkT6I9iifm2WQ~AsZ8ukiWIdt3EXwX-gJh2gworq6S5FCdC3W0lwGNjl6fI01c6498Q2Vo6kbW6LE8tNILSdrup~KccpfO8pWa4eMLf7lXWBD4BLrAce6o6BOcxMxcmfL31XXC5JSSnEMn-xTHQmy~fP19XozHeSqPRTvC2Qacq4~SicbIbC-5401osldlN6DHnpl1jyj-Zu2NuGlGssdrxkt7BembD~pkXD5MzuCVQmyd2G8eWatdck4Z4Nl0vdch-Q2kdkmlqZ2Cbi0ToWi3qKCec-zMzj6lhA8uPsw8ExC9elaZMF2GfHSEdAOhJYLoHR8A__", "https://cf-ea.everytime.kr/attach_thumbnail/607/80688432/everytime-1771591493538.jpg?Expires=1772369986&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=Oys-3057jh~A0waMnUg3wmA5FbdgbnhqrfqqMoxd~YMlt1aUPwFro-jjp72yLC2YBBDG2qaK9DQgmAOxrmJXc~i50ZgHj3A-LsN3OKXflajVO2Iz~Jp4A75phpdbifqMaMuM6Op~c6RphCcGKOB2qvtnonvaWcSYradUV7P-3YwEPhP8c9JdhMiJYMPZ1MyC-893lPsCVWlX46i7ZUBfW9NVg54UE7pLLW4vRQu5GWasrQgRGk2K91ZGDt6wl9AjXbeWwt8Wu0hF7jOVNlDE3Haf5dpBYlnjH2E3dFNYBNVWTfV0EvB02WO0x5s0k0rg2q8w5KlmK8K~u05EEs~7~g__", "https://cf-ea.everytime.kr/attach_thumbnail/351/80688433/everytime-1771591494178.jpg?Expires=1772369986&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=R8~6JWZApVj~iZLSHjpCJMWcRpCRCYeP~ab2OsC~Xl4Hy8uU7nXWbN7tkw9ujzJ3Wx4sdG6OOZ7sAvQB9q7DnXuXzN80Pc~5p9dayEyipVwlWF7HqM10eakJDGO4o96WswK5dH00NKRNyI~EC3jnPlIF7diFVhbvol-f65x5qb7TM1zfI5uy9Rmc3XadqDBUaPFe4B0pUMo2n~0Xc7ktMwxpqZVVIII3iOVcI9~5~KvQNaRMuI1yYXxH5M2Wj2YM3~~lOF-N2oWVw2AtJfffczzh103tkg37Patw7VLcbPdFTl-vRtpgLA9zsxJOisKWdZqyqJ1eHuXgDClb0L89hQ__", "https://cf-ea.everytime.kr/attach_thumbnail/931/80688436/everytime-1771591494818.jpg?Expires=1772369986&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=IWQXRpatJRAP4kH32N-FZJ5suDNZ37IQb0VvyqE7lhvIgNgapM~YV5ndYIRAmfyMMToB0Qs03KLdPu4NAEPiGqovqXk-h7ODIQ0ObcsGpwacFM4Li22TbqmW76eNVrQoo0jkV~IiCkb2sQ4BDJTW~Vf7cXWh9-AJmBHZgfeV902kVe8MF4zd-HwbTE39m4tjWgkWeRA0HK~VS9icbAkn5cpTvmqs1K0839D~Qbwo3ab5FiYdkqlYeIJslkKmdat2u7EeqLXqeMky0QnnRH~nYY7f0TYoyqqelYyMqzLNF9Z5M1nfHoo3JmN-dClH0EWykWkSHlKU7Jn5IECy~RoRoQ__", "https://cf-ea.everytime.kr/attach_thumbnail/309/80688439/everytime-1771591495458.jpg?Expires=1772369986&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=U3-1gikjCXDe8XW9GHlS0ZtcyOCDwM88JDNNTSfHnLbXrfnPwXE7ic1X3QwZ9OrMQ2JSnEPdSOIEPhmV6RqNVimzFTCo1w121TVuLm7AJgFWVXWqEiTmIo-HASGPqlQXeQqZ2BEdywJhi~EFJEcEFv2v8WKopMDmdt2hkyawlweJQCHeG9mSD0bbfzzAv37mAmO5Q6TlNaYhWSTOzXIQV9-eETxTY~t7idv4IoNZaCjUF64ZWjKbPn2rJnxBtVhiZpRxrYP6fJxX5eGLd2~y8UnIm3QsM44h~45v~Mo4JWVlBFAbF2LqJeC6PzaBmhcEIwNLrQJRwxqv~cmlI8muSQ__", "https://cf-ea.everytime.kr/attach_thumbnail/286/80688442/everytime-1771591496107.jpg?Expires=1772369986&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=bWbpQ8pvITO0G0AIiWm1DpHoeWQRaTfxmTI3REOZ34U7vNnkh8BQKSWjfb9cBvyhHh10MrWJhGvN98HlCnL~a-6~a~HQ5KIinEmduUls1Sm6saH2xLO3ZKfEw5Q4rqXe~cLKypfhrOWJjx4lh3l1xcaOekmzxTGtGxRcPzMyXfyHdK0TUM7QWomxQMJy2rGqbLToGjYkAa7uu8xme2vS-FEB7l3JLiQT6X0c9r0y5Z3U4Z6lEACr~Sq5-BsDnJzkMU9JbX8I8jF-pTulhqR9CMIrZDlp8e4cLkI6P0nm5NBQIFGt4geZgi41ZZ3eIUqLvUob8aiAClYUyxBOYMgOnQ__", "https://cf-ea.everytime.kr/attach_thumbnail/190/80688445/everytime-1771591496738.jpg?Expires=1772369986&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=O1Vfx4ch29C-mGdMIVFkVZUn2CDiAhm7NzPfDDTQtkGJ81lDXszAkwgJDKuWuAIK6Ks6YA1g7jT2sdEqhcbsOVbrV3AxWov7xBE3XE2kIXuwuYCkYvWAZFc9cpfMGicO2ksl0oLu3duTWTb-KexYyqdmYiVERXa8kG3Ly23TOronbR43zAKrNMTyXBTz2FNFLK48KHh1dlZW-FEiCf-94p5H-q51x9eWt6QXRniXOq9WUcGNlPeu-E2Z83PejZlb1HyWXxXNJ2g2Im~7o3K7iUlaOw~2ywqcD82h1JffmYLfWry7xVPigAszoPyLOVljyY~FSSB3C6GbbVtLRPLatw__", "https://cf-ea.everytime.kr/attach_thumbnail/435/80688447/everytime-1771591497510.jpg?Expires=1772369986&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=fMsfuzT9Qagrea8jPb8h2v0b9snnHUSQRb6mwfZxz2B0ad8IaIhJmFgoncoEPfhyNAdYdqNGnGt76jk2FWoT1QwGLPE6okzTw6FZI1EeDOPgry3JUfsTmLOAtRV0IZksaIkCf-CMSO97WZzyx-MLiktuJlv0xdQnOeP89Rml2uIze0ts1FZUdnqUBKp2p4YdS70ECDAgEhLEgXQiZVAGSqUgqOg0nmG-Cuya125lCRBrNg0cLf0m6jj~dovsSwZDXCG-W0Cmh9EiA02hXLjFt-sm9heduMBkC3bn9-IlCnR7-hK1RdWtf1nN3iJZG11NfkUBwBt6BUWe7FDSmh6rCQ__"));
                clubs.add(Club.builder()
                                .name("미담장학회")
                                .category(Category.VOLUNTEER)
                                .location("제2학생회관 301호")
                                .shortIntroduction("취약계층 장학 봉사 동아리")
                                .introduction(intro45)
                                .recruitStart(LocalDateTime.of(2026, 2, 13, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 2, 25, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("봉사 활동에 꾸준히 참여해야 합니다. 장학 활동 관련 역할도 있습니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl("https://everytime.kr/544806/v/401059191")
                                .googleFormUrl("https://naver.me/GVV5zstA")
                                .build());

                // ===== 46. 청사 =====
                ClubIntroduction intro46 = ClubIntroduction.builder()
                                .overview("청사는 전남대학교의 환경보호 봉사 동아리입니다. 환경 캠페인, 생태계 보전 활동, 업사이클링 프로젝트 등을 통해 환경 의식을 높입니다.")
                                .activities("환경 캠페인, 생태계 조사, 업사이클링 워크숍, 환경 정화 봉사 활동을 합니다.")
                                .ideal("환경 보호에 관심 있고 지속 가능한 미래를 만들고 싶은 학생이라면 환영합니다.")
                                .build();
                clubs.add(Club.builder()
                                .name("청사")
                                .category(Category.VOLUNTEER)
                                .location("제1학생회관 414호")
                                .shortIntroduction("환경보호 봉사 동아리")
                                .introduction(intro46)
                                .recruitStart(LocalDateTime.of(2026, 1, 1, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 12, 31, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("환경 활동에 성실히 참여해야 합니다. 야외 활동이 포함될 수 있습니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl(null)
                                .googleFormUrl(null)
                                .build());

                // ===== 47. 한사랑 =====
                ClubIntroduction intro47 = ClubIntroduction.builder()
                                .overview("한사랑은 전남대학교의 사회복지 봉사 동아리입니다. 노인, 아동, 장애인 등 다양한 사회 구성원을 위한 정기 봉사 활동을 합니다.")
                                .activities("노인 복지관, 아동센터, 장애인 시설 정기 봉사 활동을 합니다.")
                                .ideal("다양한 계층을 위한 봉사에 관심 있고 따뜻한 마음을 가진 학생이라면 환영합니다.")
                                .build();
                intro47.addImages(List.of("https://cf-ea.everytime.kr/attach_thumbnail/510/81090670/everytime-1772275680444.jpg?Expires=1772369714&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=CXJ2nddwo7dxswXWmZ-MBFs~COuimZriQMHvfrIMkvQsby23vO0scREqPKx-S71SJVzJFh1vCu9kuBJBblzdqKSjm0xPOogRPvZnu51beN7ifIxmhB~rS9e6oSTucJ3trTkzGjT50seWgNYJDK-r9Gl1craFRgGQrKwZkO4~3LF-bxhUVILHif6Yjmh8HTwkI1yxziu2hmn9Q7OZg~zDAk8YP5J3b~TntF~lWALjAOa28M~-EjuE4xDmFxdLy7xpwPfKbLaKKcdehta6eGxepMJ374zoLXJsZr1d0vEF2gFIdgzyY3mwT6Ky0smoCKFJQOpUn54C-PrZJ2levCBhKQ__", "https://cf-ea.everytime.kr/attach_thumbnail/559/81090671/everytime-1772275681194.jpg?Expires=1772369714&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=KfmmgvMu3Cba8Jbal6U1rY9dvvwguFbvMkcE~Jfa6kykAAXiUoRLWGkmo0NNJAHT-PVEFn5bCOLttc~srdaQYNqmwtSLvoHJHe3hxtxAMTrqUL1TUnzfZOyQ~kKXMzbguUbc-FMrarloKp4OumN57W0l8dqMX3DPrRpOEm6dFaMFtuqESht~BXjC1j8DmKpJ~v56TYjpfX3acMGyrJiPkj0LzJCvnbTqVKX2hC9zbiC6YV9ovXa1fYrmOERLY3jGM9xIfY0zM8wV~pu3AWhfAtHC7AGXacBL1sc-7BPMvws494nXNH6VOLwzO5DZXRBHhcNz92YxmAMV-LYWdkxGUQ__", "https://cf-ea.everytime.kr/attach_thumbnail/490/81090672/everytime-1772275681605.jpg?Expires=1772369714&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=UXQkg7kvRJHoKKcKIpYY0b0PjI~XHJ7Qla0CcYjedRR2lPJUOsGVKBfpyS0W8VAHM2x3tJH0u6~clfJZ8KZQfNhY7E8f242RRnKiWwWRvnUTw2QwQCpkawCjrkYTaZr~NrcxIZqRzQ2o48bBdXUh3QN8Hojk0EyFhsAFNEWkZSEGJbd-s3Q60-pjadQ17NC4kRuKryN3hen0uv0RajwQhJxIzpmBmybFM90HJQUb1pqWtpRd~3zBspfiBVx~NL2EgCkvZqXow7Px6iRxFoLLFO9JdN0IUAxyTVSQMOJsaxoWBM5ZTA33jio7Am3XA6iFoPw5Vzq7cfN6elO4RUXNEA__", "https://cf-ea.everytime.kr/attach_thumbnail/893/81090674/everytime-1772275682322.jpg?Expires=1772369714&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=oPHiHbkraTXuMh3TLdi1aAaVBOXzj7QWVYh9UXfhlSs7junu15OmrgdrG2ADMB6HFM8mG72eBcavTZ7tUJWqctl~t52DgbaYbfeXEEoz59sa9FPR0thvH2V4aKStEbgFn5fP80s4Y4O0DaR5l1A-X5YeNzpZYxs7oATnrgTXQrhwiq3zq8CpbPMvga~kLVeGtokpMg4XRqE4KX~hVTsvuwmjj6FAdHYsS4R-8L68JNLRTAVLpzfxP145GE~pDGrU2THTxp-VBCSW4T52y36k3sbFSPzWOh3KpAJ8HINzfdd6MsgDx8FpzEwZATmZemCoBmxYWjL7kUHO~klwVL1I-g__", "https://cf-ea.everytime.kr/attach_thumbnail/449/81090675/everytime-1772275682729.jpg?Expires=1772369714&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=BLdPMTn4uUx1yqAopnxPu0kZfXFAVE7W22I6~8ZaTMlvOKIkckRL1Ji9CSolRedHNQd9JpgkPvA0VSQa5cNXZ8QyLjQyqr5CztYZj41j~ieyNOsRI-4eptSthRPyfXhftKiB~cBdYhcquUYN3kSXdmay3zF~88e8-94Y9oQaYA6aIKK7-~CM9MZTNCftjhpXX~n-962p3yCUD5~OsD1UZOc8c~IPYd8NHWciOhTSMOnB2USAN9P2FzejAtDVCwiy35kJDowx9iDDAdfx9tlOOxjWemoGWsJm-J2L6XynzlFbeb5nX1rmc8dvyRQCr2B9CzhORW1xjUaAVCmAG~lgxw__", "https://cf-ea.everytime.kr/attach_thumbnail/414/81090676/everytime-1772275683085.jpg?Expires=1772369714&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=TNgxGvPBuBLL28xZMlPsA4ZEn1fWkC2FnQ5Otfm96YX-kmsBCD5k57Ypk6128QCVkvN0wKYluB7tAeoNJPg3OOqjINnQ-PVefmDGbkHdwrMvds05I0qr1WVnxiUYfWaAjgoOW40ViGRVj5xL29d8mJBVK7CHVUkdrSnVejMf99rDzp6NgcEDJDSvzbRn4TjpaXn8LTi1R3GLdtlSA1rC438-gQc~DKMR8NeUCVDDrQ7ckVnhAy~pR99UG5X03IGP~AIY5aq0hYBW-AALBc7F3MsP1qrRDKdV5D~1wkKuZRwDi3G7lX~Oi5lcG-aDma8NX9XdyuqgH804xULlHJvYhg__"));
                clubs.add(Club.builder()
                                .name("한사랑")
                                .category(Category.VOLUNTEER)
                                .location("제2학생회관 303호")
                                .shortIntroduction("사회복지 봉사 동아리")
                                .introduction(intro47)
                                .recruitStart(LocalDateTime.of(2026, 2, 28, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 3, 5, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("정기 봉사 일정에 성실히 참석해야 합니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl("https://everytime.kr/375182/v/402225686")
                                .googleFormUrl("https://naver.me/5VxnqFgW")
                                .build());

                // ===== 48. ARC =====
                ClubIntroduction intro48 = ClubIntroduction.builder()
                                .overview("ARC는 전남대학교의 장애인 지원 및 재활 봉사 동아리입니다. 장애인 복지관 및 재활 시설에서의 봉사 활동을 통해 더 나은 사회를 만들어갑니다.")
                                .activities("장애인 복지관 봉사, 재활 보조 활동, 장애 인식 개선 캠페인을 진행합니다.")
                                .ideal("장애인 봉사와 재활 지원에 관심 있는 학생이라면 환영합니다.")
                                .build();
                intro48.addImages(List.of("https://cf-ea.everytime.kr/attach_thumbnail/885/81135201/everytime-1772364283966.jpg?Expires=1772369868&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=b-dKBsU8HYsjoKwl8r4j-Xs7vHxzp9BrZfitJqLTNbfITZdFyCyeoELZTzo2GuRO6sEhro1SNB8DsJXt29y5FTovo05s~aXz37yo-RnD-NHt-nyiW67Lhb9gx6-wn0UINVH4m4yV2Fhwd7qjjeGAi8XGqpwkEhuq7xPvZ~zBhX2xTBN2Mi-2pEhe61fgvcv5AmDpgfIqiBotY9FwRcTJ6W5~sCI2xdB4PO2cXgKY~CTMP6QadFr6okATzfvziMJYrlZO5zvRyiJalZ8OD03sUkqnnlbDCuTUrqPyoYZn7TZ-~j~IpZ9X~UjtPaOgZVeVYg7-A1TVzzllddLRh83Xmw__", "https://cf-ea.everytime.kr/attach_thumbnail/225/81135204/everytime-1772364284637.jpg?Expires=1772369868&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=kHcVQIT9pFqqlAOqu5uBo4i3Oww5ST9k1D0r59TGXhcZXMwbamXLtzZTCh2CJ3O7YIz9OMKICcKDUPpbGI0CkurElGLZTdI3pqja~ABoOC76F2M~hDUl8YO55-8XtPwh7S5xEhso29Zp0vdNndSgeZK~GmgRpvQ~CiOeqDUhNWoDgbop82lqM-Tu0OifQJPGl83uXHus0MiJ3q6c5EghyusCbyF19RGJN~Fz~urHwgFu3scboqOmKs7LNagFZCGSuVYRJjJDzdoblDduksT8ENeYb-bzlYJI7RtvTo8r5EB2DbA-0RkQGrfzedLGXAcuCbCKkgCalS2GuoM~AsCq8Q__", "https://cf-ea.everytime.kr/attach_thumbnail/803/81135206/everytime-1772364285095.jpg?Expires=1772369868&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=TRYfc0F83aZor71p1efY2-y5BZT6tNoYJN7hBu9IK7ze8G36VPeRN8tvzZVkF-ZWyHjtDJi6QmccDMtHX07zUkk32VSVEudOY0yT7UQNkB54~iSlwJ2bBd7T3CBrd5CKe3Fu0KQ5tQzv3RCzkDOFdpkvBcn3PNdYPbCwThRGUowpWBY75EvqITymOQb2e0baos-tBZ0JR~NhHJbmV4UkDVd5m5h7oQprPXqrmT7Nn4lO-k3SbThC8BMlCuaWlOGvWrVp3et-WtLzXe8slUqzx8A9Ct8TgC9qkTonnA8A4KfJ3mm-eYBUOyjSSRgY6V5vbYchTNmphRxMgLVzQp3doQ__", "https://cf-ea.everytime.kr/attach_thumbnail/608/81135209/everytime-1772364285705.jpg?Expires=1772369868&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=BhPJyWJFQzSSPLoDtaJv6Bb01xLkRtx650Q5N-uvNMUFbnGD6UpUOI0W39bRrR5RL7vF7ZpxVZTQu4WZTZtecAieSUvn83CIMnKYLgYeJAPgId2lkyucieG5erhcji6ZCkCWyuq5rWgNQEzDK4UzSsstp4Bie1k3VRUPOzPA18Rquzb1LSkoid2Bi5T0zHcOQkY7v~Cp2gBrQTQP1sxfhyfx6rhvSVhIBqrffJcDShDUbiCxaS8r1OB3qI2h8Qg2MfqJMm2YaKgVj8NyuliKfFzVi~N2sODzTcToyLi2DEGKAmk3B7~Di-3MVmKt8eP5pUdqG3dNqhEvJccUedIxMw__", "https://cf-ea.everytime.kr/attach_thumbnail/747/81135211/everytime-1772364286126.jpg?Expires=1772369868&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=nNvwZealDFBG9cWkj0PjkQUmd~8y3o8hFBXAR~FAj0SB43oTQCVtZKMv0ts1NZQjEAoWGxN2My8acQWTIFOMADXZKEIQCoF5WwN3JrIFcMnp3rQny14TxwxZX-qiIxYHFPoGaIXEt5ACUkqoiZTSs8iGSBrRHfnNivLjY~Bds4AwUxm8sWQrhxIZV2Onh-YX1nmR9xNAZ~uFYqQVr3nhJ1yHN0rUMlu6ugU45~A~o7uOKimY20H~nHJ8xEVaJutbCRmYpUSov-JUfaE2UawIhsRw3LlMOTUohMbtH1dq3EiIbu0mkUJYz9c0NWDVUOkFq2n2Ju3BPol3uebP62YLvQ__", "https://cf-ea.everytime.kr/attach_thumbnail/926/81135212/everytime-1772364286523.jpg?Expires=1772369868&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=TEOu-jQj99EoAQW-VeULybFWBhl7Rv6B2ngSTk2slLQNwG307JubeOEVPVWaWMLkRU8Wc4pI8EcTymHbAiYMpB~kxSFjHYetzHOaZvcbW-tTXHovOoiC3xXEnV8zP1shYfzqJIqkfdZeAgu6~oYbxoDlou6AnyBh-5UGf9fw2wvcv3Ee4nm~cnI--lbwvJXCw9iWzDu04UH8wbcn-CR2GOPzgfHL4sZDXqyL6qJLTjeFhC3R0fWHzXbr3vD0HzThbi2tR92wkfhBasqKYXljzMTcwMwDcKG6FCliiibvR51352qXeQSZzLm1ArxI2Zw8PwCBnq9s4-7nITuov5l4aA__"));
                clubs.add(Club.builder()
                                .name("ARC")
                                .category(Category.VOLUNTEER)
                                .location("제1학생회관 415호")
                                .shortIntroduction("장애인 지원 재활 봉사 동아리")
                                .introduction(intro48)
                                .recruitStart(LocalDateTime.of(2026, 3, 1, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 3, 11, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("정기 봉사 활동 참석이 필수입니다. 장애인에 대한 이해와 배려가 필요합니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl(null)
                                .googleFormUrl("https://naver.me/FBMdI8Pz")
                                .build());

                // ===== 49. ISF =====
                ClubIntroduction intro49 = ClubIntroduction.builder()
                                .overview("ISF는 전남대학교의 국제 교류 봉사 동아리입니다. 외국인 유학생과의 교류 및 문화 봉사 활동을 통해 글로벌 감각을 키웁니다.")
                                .activities("외국인 유학생 교류, 한국어 튜터링, 문화 교류 행사, 글로벌 파티 기획을 합니다.")
                                .ideal("다문화 환경에 관심 있고 글로벌 마인드를 가진 학생이라면 환영합니다.")
                                .build();
                clubs.add(Club.builder()
                                .name("ISF")
                                .category(Category.VOLUNTEER)
                                .location("제1학생회관 312호")
                                .shortIntroduction("국제 교류 봉사 동아리")
                                .introduction(intro49)
                                .recruitStart(LocalDateTime.of(2026, 1, 1, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 12, 31, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("정기 활동에 참석해야 합니다. 외국어 소통에 기본적인 관심이 있으면 좋습니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl(null)
                                .googleFormUrl(null)
                                .build());

                // ===== 50. KUSA =====
                ClubIntroduction intro50 = ClubIntroduction.builder()
                                .overview("KUSA는 전남대학교의 외국인 유학생 문화 교류 봉사 동아리입니다. 한국 문화를 외국인에게 소개하고 함께 즐기는 다양한 활동을 합니다.")
                                .activities("한국 문화 체험 행사, 외국인 유학생 투어, 전통 문화 교육 봉사를 합니다.")
                                .ideal("외국인 교류를 좋아하고 한국 문화를 나누고 싶은 학생이라면 환영합니다.")
                                .build();
                intro50.addImages(List.of("https://cf-ea.everytime.kr/attach_thumbnail/544/81074418/everytime-1772255678971.jpg?Expires=1772370077&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=MPERS2W8-~DExVOXA3MEm8lovvq2eju55KCstRWHxjjFe8u4DM5pgmH3sfbnOS6~yopjScgOXmbH01rRo0Ayz0l8znaCMiuz0E21oauNRWVqmWY--krC3rAv-lENX1eZJqdz3Tc0IhczBbTypXBpWqZm8emN11CfM-yRuNTWMGs-zpUYpH61mKK-BajNxWjROL5jQrV5VEFYD7R7ekMrUgaet5Cx3cGIlm6CxsfPaF1Z4-vU1vvssrFTj2E-JyGR1bTINI9L5bynI7xAh7~3WKFEc54Sxs2cTf2-Vag-nMMIWCvZEAqt0lU3AgN0KK~~M6FywXsRB9piUakcrA44vA__", "https://cf-ea.everytime.kr/attach_thumbnail/386/81074420/everytime-1772255679593.jpg?Expires=1772370077&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=A9p9YA7p4s3uYGnzNa4pbdHWE1ucwyvJk2MtR3HBMO1Mmwjee8APWAJOEwoE5ro3Vtch3FI2H33wuLvguT0No4T7cvUboS6hE8K55XDDcRGHd4kV7qLTOSIJ~g~3FlY7TwVVAsuhNX4U4NhJDivhr5fsfrT34xPJHNHSnFIjE3Mwf6kotaZOtDcrDWIiDtdOv3ZfrYrz8PfCpsjx~JIBFcSxXR-00UvuODH286X3lvhdhrieGHBFnu8ygj226ZemT2mlIrnu~iqHA3cLglFUF9hiMei05XSs-F3l-AfA1Na-RPs11W9VYO5bskNDI8ia6sehHY7GfguoEgSjv-Gvxg__", "https://cf-ea.everytime.kr/attach_thumbnail/633/81074423/everytime-1772255680154.jpg?Expires=1772370077&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=fwemmhILv-Wu2g2vcwyJbaG9k3NzFiWy4fZn8hFIf7gAJNp3OUkUBv9JlcOVT7p6SeXk5V3i6z1EGW5Gb5SfU7cvD1-P-mKvPYaf9sQtqQZiR46z18a~QsmhQbawQIzKJnkq5Myw7-RGW8sgT~orjWj209Qk1x9zgMs4rUj3nZGxsItAD49TUtSuhBandgDSMKRPct5u51S9vBfU4NgGBjzu5CpVhav3tU~NXZRZK~RJ8CClDRXMeiQ2beApnTBm~tXx48x7coiRYKiFbPBUzhPSPHs~mkbJR8M9E9T9Cjs0V9LOXLbV-tfDpmlFFbj07-Mk1gm3Er0I-9xYHQLQUw__", "https://cf-ea.everytime.kr/attach_thumbnail/105/81074425/everytime-1772255680682.jpg?Expires=1772370077&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=FmZt2c~xe5mRVoptCtjYGFiFVxG9UNLhncOUPub6ne-Av3mHkDae6tM1FyG90vE-uQbYRZHT1WGbgBYwreqoqmQOytLAgsl4odJMb-1C3OvZc32f-SFo5I5mM07EkTEHJzj1TpNOn6LJJ~SRC15biGUR6~69V36832wc5Q6~C~Z9vVAyHwjzTbhiqUFO986Gq-OFYsdBgQz9X3MFLPdlr49ckY1jt8S5ogWwUSuh47gYD1WbuDm9GKRsyIQTIJFCphsei-FD7xGiEeqdfnTZdrmcJ9QmqUIrxsGT3kfd5-OxKKrhyyCn1DEH60vIaPbM1~RjY06dNZ-eJHWSNm8ANg__"));
                clubs.add(Club.builder()
                                .name("KUSA")
                                .category(Category.VOLUNTEER)
                                .location("제1학생회관 406호")
                                .shortIntroduction("한국문화 외국인 봉사 동아리")
                                .introduction(intro50)
                                .recruitStart(LocalDateTime.of(2026, 2, 25, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 2, 28, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("정기 활동에 참석하고 외국인 학생들과의 교류에 적극적이어야 합니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl(null)
                                .googleFormUrl("https://forms.gle/RG8J7Zx8SCtR4U5W9")
                                .build());

                // ===== 51. 과실연 =====
                ClubIntroduction intro51 = ClubIntroduction.builder()
                                .overview("과실연은 전남대학교의 과학 봉사 교육 동아리입니다. 지역 청소년들을 대상으로 과학 실험 교육 봉사를 통해 과학적 호기심을 심어줍니다.")
                                .activities("초·중학생 대상 과학 실험 교육 봉사, 과학 데이 행사, 실험 키트 제작을 합니다.")
                                .ideal("과학을 좋아하고 교육 봉사에 관심 있는 학생이라면 환영합니다.")
                                .build();
                intro51.addImages(List.of("https://cf-ea.everytime.kr/attach_thumbnail/357/81116838/everytime-1772341423005.jpg?Expires=1772369924&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=qkijlW5hezw-dKAGbnPOKmjBwOkPLAIgXG11iP39WB~zrdiwwf7pJ9mWN3hw5qOgvzeiVrDPajCC9IcY4q1xUhkY4E2ZDEqsW2N3EGpnAvDqaAwy2FwspCawQLG0PDzoVko0P5~LIX9Cr6DOMTabF36Xx14euowUvRvNoz0kdZolZDs-a~o2BWa9dh4TyBUFQpMgyKyTG-R0YlaXdMSiaT8C-ZRcRNxfQCNYhO9wA4x2Mi2rty3FBuUrYGS2k3Hj6nwW2wkbFb7scd1w6BKD0Mm7~Z3-~7ZrIFxx6JYYw32yH5p1G6HqS7TVD0qhAwset3Sd8izsEq~YTg~VIulJ9Q__", "https://cf-ea.everytime.kr/attach_thumbnail/184/81116840/everytime-1772341423325.jpg?Expires=1772369924&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=bN-BsX6KWffKnOjNKiOHRpwW8Fx4wU~inixgQ6mZQzhX5ykz~VE3nZwukcDsY6VP9UQsy4N30A9g8L-lE2X2naXYfO9ZSoBpnRBBN7eSnDca91raQr0ZYOIl7vD23V0CghzsWhEwUzl7~gnRq1iZ7bvpgMn5CQM6fd7bGl3KfJiZ7PWb2xAodkB9jYtkYcHAB1qF8YCoZJVi4vm1R3F51ublPct8DijfZrDYWwZntFe01YA5zSKw1vZBIbeGEWs91mL2-PVrZpZgLTM0hfIJWxXQOw-8w9Z1LmWwW1kfCd4etLyc0DNwmfsf2HS-vlpJmg0jZNkIwxT8c3gLh91iaw__", "https://cf-ea.everytime.kr/attach_thumbnail/849/81116841/everytime-1772341423666.jpg?Expires=1772369924&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=D-SrJL~G~jQH42gI4vOXqy2ELiD8OsZI9FGk2CpJbnR9YBdqyYd7uyROdtJViBpDTZmMBsJo~H9yfjGcI0UVqh2H78hVKS3ukfK93IAurFZiKymA2SQTQgtxSTisQ~J~ivDzlZqNLNpCwmIj6M8WXZpnCmmMa~Jat3qOEeIowkyOHzU91~T37wP7pZn1SuaBQ3D5myCNaX7rBqo32ZM8cD60VI2jSochEtfqdV3FHS--abYI99S8wI2jk6k7aCdsClXMjrqBnaN73P9rWvd4o~PrmGapu33bgNY8S9LQdXu4ey-kJcaFceofeGHbiIQ~qI6rtFOKjARNx4iFdB~nGg__", "https://cf-ea.everytime.kr/attach_thumbnail/167/81116842/everytime-1772341424086.jpg?Expires=1772369924&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=mvcH~3goSQJr6EHXULPiB-izVcqIKlYaj4B7keI89oSrzr18zkGY-x6nX1abxQ9aozHWL3W2xw9meP8pf3p9ARPIVfRU5kcGwUgdQ0viao84IBp21o3cSzldCGgJHoCeCloFKevFAY5KBd3iHhyXpoLatimuXuPVyLjP9MCpdC5icXeIOVEqp92wN4msEt5aTSp~K8~2JJCIbv7hOljLELCrRSR50BRujYVHGCPZQa5Kg16wtWsTl8Zk6t2TNQ6dX3rez1RFZGsFyFwoye~yFI~Y55gILbTi8fgCQAdCj4qWLFLUyBCr8czmq~OZWhF-EP-co1pTaO4YXkvwAkxY9Q__", "https://cf-ea.everytime.kr/attach_thumbnail/485/81116843/everytime-1772341424432.jpg?Expires=1772369924&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=I~slN1zN81HNOaPQLgVkXvU8t6oIVv2MoFFr~RjOAHmR3tlZ4JRvjsf5aXArf9RL1x6zvAm1~eMsgd2ALD9LFjG8Ftr0r~fkxVT64vNn1pHSDkmTvgXO3Qu2fNdnm0l2QJw2ke78dGT2ncTSKkcVZZVJofHTmQSdLbZAN34CnmYutNYTPH9iZmwS5R0Hl9a0kRLMvkgmoCkdlwDd9H~0drtkj9v8mvDshMTBcxRZ06ZK3BQW8fyAwHAcbHJxEyVAHmy1cqFnklN1UOWcJNanj68tB5WyQloNqNW-RbOwvmFPKj16g3UQS9C-byv547L6ZgYdSPdPL~BLRaNA9Zyi2A__", "https://cf-ea.everytime.kr/attach_thumbnail/278/81116844/everytime-1772341424891.jpg?Expires=1772369924&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=PvZBzI2Yn0i1HLuxKmO-GAh9C~xoU43o9Bp3~vxfXxpCVE4poCAcGUtrT6ypOdFIAa6CzeqboC8Z5CY4vJWT-zvqX1P4VaFfayOEuZxZ~JvvZkFokRcSsw2mz0TReHPSpQdLPuZI7mLYfoYzlF~qIYsW4k3Dxi2sDgW1x6sMEz0lP0o5pm~Dw0awoIH2LhXn9Yz4Sr3sIZjH0hYxnQys3wdmtGTPKSRMBtu9uY5AFeckETyw60YjCTXhMNK~Y7b7QyMbhIjb6S229TMF9mllHZYi7OlMAx5zy4hWct~3Vg7VglJZ9XHUxWpeucPRmUPsuT8XIDmcNMrpLagGtAIp6A__", "https://cf-ea.everytime.kr/attach_thumbnail/120/81116845/everytime-1772341425233.jpg?Expires=1772369924&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=nT5fR5YOKhrhz7TCuN0NbuEjXeY1hrZ60NFLALFqsepcmCF9hk~j-QscmfRXqFrwt80XpB70Dgkg99wDdC9WDLDkrvICxnnYtJ7por-HCImnappmPb0g12rScBaZCUVC0HquqH~LH1WGAhpajjbin9eiK~CDbvjx2~h10Ljr1EsPIGyIG-Pu9eaU3ZklY3YjbVM2eG5bR5EWQGLVsDu~OreGdQ9ygsQc-9zW4wh6wpT8VGdny7gfYVAA9OdBYYwBwIFzl13-DiYYFVBbPlOdCUyMn13v5dY~kup5O6bdXl93ZzmgZhsey0aqc46y~xmqSLrQxUBdyMGHxwFg0EiAzw__", "https://cf-ea.everytime.kr/attach_thumbnail/410/81116846/everytime-1772341425539.jpg?Expires=1772369924&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=iB1XdWnhqqTweREmXcShntzCJerEtvQV7znylkNcKxAOZ~pvgpmo~1JmajAGTqGS9PNY5iWbjKsRR0kZVMKG2dvhj1LaQ2mww6qoRu6lGkTuiGlX81tsBEMikZy6W4LelpV6NJEQ6f8VV8AyO-mHi5QqAlZvuJmxtVn2FQt6nVl3sFpBOTbK2Muy-Z2N4LUD4DuJHglZbA7rJ2vm6MQyA4F1H81Rrq-pY9mPSjne7HVa0jQ6DDZ3xpHd3nBnmgWbDXklAOAN0nlIu0GgPvS9PnvdThOmRc2SMSCLJ14ZbSs1DzFCle7-akW2CGeIi2Onn8kRLmpHaDDfU6WBMLMIjQ__", "https://cf-ea.everytime.kr/attach_thumbnail/252/81116847/everytime-1772341425856.jpg?Expires=1772369924&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=S98EQcC2T8VWaVm86ZEWyVNNYmP-4XQ1vSClNTujBd9vKROjuOos7UxQ4xcV26zOQfZNjkBKHjgs0rwp34AY7GTRauYu-y9g0UNEnCfJGUh8Cl2J~4WN2O1YwQiEL-0DSwVCby3gm3JJEJzGchAtfMseEaFK~gAUZO4M7gjcVii64Aj8a0w2YNqXLkpgBKxWV0HT3NMgVsv468q1QNq7TlztkJS4qSb5fseSDdKXcfPAaaVg1IdDEAn54rCYBdjZwHgf9xKIhVk~yyEi-kne-hc-t~a9U1aICTEfDa3~FXxaPxQQ-vVffwUs1Y5D~L2m5nBXenF-HlwEye42AoV5xg__", "https://cf-ea.everytime.kr/attach_thumbnail/934/81116848/everytime-1772341426197.jpg?Expires=1772369924&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=gIcsLOSOc4ROGG-TIskNxuSc5QAzz2ImkWcp2NBJOkT76j7ylkWQEOWaP5cGI2-3FumMteg3D-dEn69~LbS19Eg1fHRSnKzW4zNfTfjOlU9XmxwUq44S0nlwI8-qcqxwJrSmnJ6hY4j8D2VE9T7PS9aM6oQRBqjxoplRhgMku7MyKbwb~z-wOB7qYyUjVsRhg1leIH64Bbr-WDkMK78yyXf2MFtUP4TJ3Q06PpzNrYrMxZAwFT0dHRfJP59pLgFVM6CPNbaNDqlE7IF9pXe3dLo-Twwn9TNJvSkz5LVZJWVsmiUPzz6YkK0e03AjczI4yZNlgXNgXPzpYbICGBBMsg__"));
                clubs.add(Club.builder()
                                .name("과실연")
                                .category(Category.VOLUNTEER)
                                .location("제1학생회관 420호")
                                .shortIntroduction("과학 봉사 교육 동아리")
                                .introduction(intro51)
                                .recruitStart(LocalDateTime.of(2026, 2, 12, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 3, 5, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("정기 봉사 활동에 성실히 참여해야 합니다. 과학 기초 지식이 있으면 좋습니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl("https://everytime.kr/544806/v/402310557")
                                .googleFormUrl("https://naver.me/5WUhwzpc")
                                .build());

                // ===== 52. 로타랙트 =====
                ClubIntroduction intro52 = ClubIntroduction.builder()
                                .overview("로타랙트는 국제 로타리클럽의 대학생 봉사 동아리입니다. 지역사회 봉사, 국제 교류, 리더십 개발을 통해 미래 사회 지도자를 양성합니다.")
                                .activities("지역사회 봉사, 국제 로타리 행사 참가, 리더십 개발 프로그램, 직업탐방 활동을 합니다.")
                                .ideal("지역 사회에 기여하고 국제적 감각을 키우고 싶은 학생이라면 환영합니다.")
                                .build();
                clubs.add(Club.builder()
                                .name("로타랙트")
                                .category(Category.VOLUNTEER)
                                .location("제1학생회관 422호")
                                .shortIntroduction("로타리 국제 봉사 동아리")
                                .introduction(intro52)
                                .recruitStart(LocalDateTime.of(2026, 2, 28, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 3, 8, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("봉사 활동에 성실히 참여해야 합니다. 로타리 정신을 이해하는 자세가 필요합니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl("https://everytime.kr/418923/v/402036238")
                                .googleFormUrl("https://docs.google.com/forms/d/e/1FAIpQLScpKNzgCHBSts_rhQLEgvomI8SokA_u0396IbJvtK8X8wuVYw/viewform")
                                .build());

                // ===== 53. 가톨릭학생회 =====
                ClubIntroduction intro53 = ClubIntroduction.builder()
                                .overview("가톨릭학생회는 전남대학교의 천주교 신앙 공동체입니다. 신앙생활, 미사, 성경 공부 등을 통해 가톨릭 신앙을 나누고 함께 성장합니다.")
                                .activities("주일 미사, 성경 공부, 가톨릭 청년 행사, 순례, 봉사 활동을 합니다.")
                                .ideal("가톨릭 신앙을 가진 학생이나 가톨릭에 관심 있는 학생이라면 환영합니다.")
                                .build();
                clubs.add(Club.builder()
                                .name("가톨릭학생회")
                                .category(Category.RELIGION)
                                .location("제1학생회관 411호")
                                .shortIntroduction("천주교 신앙 공동체")
                                .introduction(intro53)
                                .recruitStart(LocalDateTime.of(2026, 1, 1, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 12, 31, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("신앙 활동에 성실히 참여해야 합니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl(null)
                                .googleFormUrl(null)
                                .build());

                // ===== 54. 마음쉬는곳 =====
                ClubIntroduction intro54 = ClubIntroduction.builder()
                                .overview("마음쉬는곳은 전남대학교의 명상과 자기성찰 동아리입니다. 일상에서 지친 마음을 돌보고 내면의 평화를 찾는 다양한 활동을 합니다.")
                                .activities("명상 세션, 자기성찰 글쓰기, 힐링 프로그램, 마음챙김 워크숍을 합니다.")
                                .ideal("마음의 평화를 찾고 자기 성찰을 원하는 학생이라면 환영합니다.")
                                .build();
                intro54.addImages(List.of("https://cf-ea.everytime.kr/attach_thumbnail/832/81001347/everytime-1772114640876.jpg?Expires=1772370100&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=jW8w~wkLW4nXUrJL11EkDokbMQicDszw~dpgs4vf0iLCT6xfzyqtSeRhwYu~NhrlahlZ69LBaz4qWgbtXg2YR3hsVayHwd95BM5QezKutQjwwVT6ijeQx8YsTsxznvq0qM71h1cTDZ9Bvuw4lAQcsun1cDXLpViu51bWLmfQIoWlRZsxHbm-jkLwyPFAEq-N2xYbvgD5WVhFc3BSjNX2SRRS6WU23FFwiCrvPBQfxGoRN5n-ZAnsnZB~FzjtKZ1rqCwbVQEcpgll2PkPCe0cYPTlprwjjIrcXDwolKon8sggQZrGUx1smt68QzIMY0rHPsxb3v3K2z5hP1pqK3G~rQ__", "https://cf-ea.everytime.kr/attach_thumbnail/898/81001348/everytime-1772114641604.jpg?Expires=1772370100&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=lUXrNycMtJKGEq88YcHqL-ocVDKwNUdGdzu6tVmbh9LaGkb5Kk1DGCGYUPzrFsuBnX7qKK4~n1AJVEZ~Cb~XprYqojpxnr2hopuwgEHmGjNg2RERMwtuUs-JEJiUGf5PaqzAo6qOxLOh1S5zzsrZsflM0~7W1YL0jDo8HcpBmG~dVvnfSirGEIKYmJ-Ie85z3N0M40uXSuwNwDvnW1OGCW1x46QdoR1VsaYHa-T2nHx2j9GwVwZqt0KwnWuflFaXd6cXayJ628tWF7nktGmcP1LhCS255b1GNxOI1RA9T72V8Hism0KHT64agP-SjuZ0onDR~chVr0GKgDlXy-hV2Q__", "https://cf-ea.everytime.kr/attach_thumbnail/910/81001349/everytime-1772114642123.jpg?Expires=1772370100&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=E1-8GfEg3nwd5ZItkUhYF0JOVQlQXG3XuuIX87caSpvH5jnKbOeBUKffoAZfhDwkzSeVTa9mXf4shRL0F~UHXvDR9sypn7dz8vTj53wzAI5NZrb0GIlHhtBT-0cMdbwwvLLUrel0C5goX1sLYxzfZUa9deiiwRyV3Is2kkd-dXGZx-x0Y6~JPpYCYvGjU80wQFspAxWxBC40CchA-iNEnFR~qxLW-fzJnvPLIBibEHM6ZIsTYjJMljpsi6bkaXSvDmLCsfNsCSuTvmqHTv6NBVGSdT40mIXvxq6WZI4Y4orUE8ciYzhohKrka8BzjXYf00xXAOwUWXq0AnKj5jHh5A__", "https://cf-ea.everytime.kr/attach_thumbnail/70/81001350/everytime-1772114642607.jpg?Expires=1772370100&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=juuNY8NLzNHKY3WL~YDklNQLdKlY9Bd8ICkdgq7rqNP2-KQr9LrSd7Fqdy8BJIo8OTDuTXKNEQcpInCr0hA6XFvSRsryB~HZK4k2mtTklXDWpUvdWjCylEXXMAM75txZ12SPF6pnThgGwlWz5~dp2Z29zgFkYbqh0JFjYMv7rRiNWNydS3jE1vDU9qAnFd-bzM9CmtaEDAvC3zsKa3stuR1umIRaLgWZPZEVHPlYxsomt~gUpEv9m8Us5AdUV-3pYYBZVuShxBzS1jKpw1dT8kxRJgSvhnok2Cc-QrIpkNZ-e06C~vrWIVntQBtj0CmLTgwiA28a1KGyC30SAazx2Q__", "https://cf-ea.everytime.kr/attach_thumbnail/409/81001351/everytime-1772114643061.jpg?Expires=1772370100&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=cK3UvXpiw42LS1VbJ0Ah21cMDbEOHxPwA3ph3Jya8b01gZeFCeKtuQOpdv0ATgUhMwKxOTECC9Ulo7Py~f9HhLK1PToYnWCKySmDPy2fcGMzfnCoVsoL0X1A75d6VnJbkD4SyZ7wQmSmm7Q-WeggrwMA4nfIavju4T0IdIrcmS53P-PqdCCVrDYouDnHfm3ZsknQ1CgcCe~nHymxDgvxAOhvaylqbhmJzBFBkIPmTkcZrnWh5ONYxYyYNoXjSBZvWJBfvwYi5sjnBokEr96Q~bpJ6LzWrG-XW3YC2I3FtCCiFsygYplFa4QuA94JvaKW1Tbua8HL7YU5gSMjen5yfA__", "https://cf-ea.everytime.kr/attach_thumbnail/212/81001352/everytime-1772114643655.jpg?Expires=1772370100&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=O47p1BZUQDzFAdAKUIndwMaj22-QBih-o3S3cFIYNNrKFy7iDpk4XtkOGmehMbE1lHxMzsYcjlmxF1YD26kxquMlBpnj055E2RpXy2KpxdQITj~GJMCmaa0m8enkWkz9-e3DsaNvRPYYr98M44fxwv3FJRNR2ScvYlWlS9x8JYFlrwRFqja9vDR7SELAuTYA4HBgnrBvbomKTvP4sJP3goS8UO29WfvcQUvaYP4oQP5Jv8l1ec-DHbjBneZcFzHc4IXisZOViWCre7NvP2uUUvXF4jkruYgBOmtsyRYTBsYch3bpmoqn5n~YvXpHZluM0yvjchiSwph8AOO5aHnoJQ__", "https://cf-ea.everytime.kr/attach_thumbnail/197/81001353/everytime-1772114644205.jpg?Expires=1772370100&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=naq23PVsKLfwY-ClZmuMR5UA0ThNZ4kYrSLqbzcuem8FNEmlidpM6pZ4QTDG~hzPXZJugVwTBUVkmurakklMzdKjoryrGQAXWmkTpJvkx8QnGuGikKhYwfGToh0fEkMYyobwbjJyxs6eNF60W5rnrEI1--s9x~5f8QEZZZaGjo5-OBEFOSb~rDd0UvqFmKcBYfuOnS6J24kO-4EDeFoa3Xk08QpqN~K~5QL8t5JpvNIBaIiMvm7UACVKaCbA7LMWDaYjBWou31uuDUzYQfk0PuHgajOgBvErwU330~i7JjoBFNu7IuyU5XwuTwygNZJnEcv2m45Gn2gwuh6jOt47HA__", "https://cf-ea.everytime.kr/attach_thumbnail/450/81001354/everytime-1772114644702.jpg?Expires=1772370100&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=mqwhRkHiKnfBu4KO25OaDKObDlLosF~FrKk0UoCTb904aIE-V~-VEiH~UaepNhWiwKD3Z7oaFQL-05fRBA9bEAmffbf9CDfX4PBiPyz6F5L19fjEEJxwpA3gQ6UjofkQNodLVQewSRrAnwdqDyyiZlIHPbGlWQZStEWN~ejeCGftGsagSbY2say~Qjlp6KcyWKZ8IauPTSjChFMogVK~s~CCMKHM42kwziQKUAI6qytg7jIjBUr4SoG280twp2oV4~rJKtrbNS-X8kGuI8ihIgVnPy9uVsMBiCiQpXaxmQZZw6uAuAapPF3wXZzvYbvPzE2Kts4doYaqFgVLPsrm4Q__"));
                clubs.add(Club.builder()
                                .name("마음쉬는곳")
                                .category(Category.RELIGION)
                                .location("제1학생회관 316호")
                                .shortIntroduction("명상·자기성찰 동아리")
                                .introduction(intro54)
                                .recruitStart(LocalDateTime.of(2026, 3, 2, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 3, 8, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("정기 모임에 참석하고 자기성찰 활동에 진지하게 임해야 합니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl("https://everytime.kr/375208/v/401964845")
                                .googleFormUrl("https://everytime.kr/375208/v/401964845")
                                .build());

                // ===== 55. 전원회 =====
                ClubIntroduction intro55 = ClubIntroduction.builder()
                                .overview("전원회는 전남대학교의 원불교 신앙 공동체입니다. 원불교의 가르침을 바탕으로 영성을 기르고 공동체 생활을 실천합니다.")
                                .activities("예배, 원불교 경전 공부, 명상, 법회, 봉사 활동을 합니다.")
                                .ideal("원불교 신앙에 관심 있는 학생이나 영성을 키우고 싶은 학생이라면 환영합니다.")
                                .build();
                clubs.add(Club.builder()
                                .name("전원회")
                                .category(Category.RELIGION)
                                .location("제2학생회관 311호")
                                .shortIntroduction("원불교 신앙 공동체")
                                .introduction(intro55)
                                .recruitStart(LocalDateTime.of(2026, 1, 1, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 12, 31, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("신앙 활동에 성실히 참여해야 합니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl(null)
                                .googleFormUrl(null)
                                .build());

                // ===== 56. CCC =====
                ClubIntroduction intro56 = ClubIntroduction.builder()
                                .overview("CCC(Campus Crusade for Christ)는 전남대학교의 기독교 선교 동아리입니다. 성경 공부, 기도회, 선교 활동을 통해 캠퍼스 복음화를 목표로 합니다.")
                                .activities("성경 공부, 기도회, 전도 활동, 교내외 선교 캠프 참가를 합니다.")
                                .ideal("기독교 신앙을 가지고 있거나 복음에 관심 있는 학생이라면 환영합니다.")
                                .build();
                intro56.addImages(List.of("https://cf-ea.everytime.kr/attach_thumbnail/828/80340145/everytime-1770775252336.jpg?Expires=1772370244&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=slbWela1vyO1yVPKJ8t1MknrKDYH~UbtWzitL3pTmlE6jNYegMlVpot1kwg0TcffH~gUMwmdjA~XLahflCg58OAfBp3jBTTsLaLaG8IEpFI5yJ3jmhRxlp5cN1G3NX6wf5N~e97hNd~X~~VIW2Z5OOLIaKz3OVbGkMMDSzFaGMaHw7v1s-28WsG0EJRdH4dEZYfILM9fmTUmv2Q9G20K2-AldD8rcxxD9q7PBk3hKgGqblEFZuaZRxu-aTpvmb-qbLNe1I20NRs3Cl-qmhLyi079zMjmwrd7hLakyy6FVEdydTXgdDIHuYEnpnkIFThlqcNBhsgxvHLP56g9Obshjw__", "https://cf-ea.everytime.kr/attach_thumbnail/307/80340146/everytime-1770775253004.jpg?Expires=1772370244&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=G5NGuADaIK6L~BEUYRUdOsL~wnyphlO6mbWk45HYX95aq7rBThhC-rGzt6drKAY6cHmk9wTFYbyNKAEHhdlEyALPieaNi99uU-6SgRqAD8w7LZk5mkAZUcmVeQ0hIQeSqW2FvrEj72HUgN2PHkQ1eclUd~3cJWJCxTcpxGxQoB5UHhzhA2vbhEWLTuCZhM2SJspb~DAg2A0SgRT23e3EU0AUekDmu2fwC3w0gYmp~tgoBqnwRYW7v1Lbi~4vJjYe5fQCOxYGLkW~XXM1GcfIwZWQQ26vG~~GRVoTNjRGdTMAhM1HrQFsWly5~j0fFmd3QOr3KZpBKTOi4YESfIBB~w__", "https://cf-ea.everytime.kr/attach_thumbnail/57/80340147/everytime-1770775253493.jpg?Expires=1772370244&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=gp2n~nYoUOHXf2BtjV0mAuryg~4PBXznNof2Hv7Yq5bHVpy8O6AC2Mq38RY~SpK0vyjIB9c5F8rkmgpPQS9IL0ymGykmbn5VEfiqAD8Gy9GXMZgiuXOWrYT8vIKbYcYMb0AJgEF7zixub5RCgM3FQXiOGLIZSAQJloxkNlK8yGWONVuHIbkyIEU0Diy7oO47xmubvao75I2kqifnegSEZbQ-LVAatMYGcbwHKhmLv19XopCuTqDXhJ-YjIqw~j5e1RWQnWUeEHdRh6A024GwknEOcx5JxpD09b45WJgXjik84tOqteb~7JZ7mA~Z-WK3my4VRFK9Gzc2DxZcxOWfPw__", "https://cf-ea.everytime.kr/attach_thumbnail/105/80340149/everytime-1770775254067.jpg?Expires=1772370244&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=CUPsuMjfkJz9kE6j0UOM7K6KPJGcFs484mR5lMZzjRme0o4Tqhwt2mZEEzhDNcLqMQ8w0OEsNFkF-aPHD9Nx8dq0gemi4FGXYEL3YTkjaM820-USAGxNF0uB7B1Qm0UTQxYZ1rtcrPdba1TX7MHeDL1-u1IVNXkxGh5zATwqpGVnkCoPdo640sQRt12DTYwa0vw-K8W-YHMwLbskiAMikejzA9RZF4LUPRJFgGCtSFfxiX7RJ7pz5nR5drYQFQXoTsE-wwp4bDy~NGwF8CILN7Lsqyum762hfjqblVAKcSZytN2NHgdfTLONbcGEiy0HzRMR7qWzYRcwPXRioRCAaw__", "https://cf-ea.everytime.kr/attach_thumbnail/631/80340150/everytime-1770775254572.jpg?Expires=1772370244&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=VcqAjZnlB2fN8TFPG0F6uDAAPMlcAjXyjZZ2XuLnzT4eeMKN5NL61tx06c0KU9dbruP6qZs2d5RcZ1IQ3ISMO7L-Y1TaUMPzB30puEuntyvEm6VdCwTgByWyO7U2ZeBhXxP3DmLsJlIZxWmRetcEDr4YTmE28GNlaDD67XXmDBGic~Kfos-Kzu0V2HAoApOKQL5SbTTGYTKOhsczyRrLlEL4xfxxAJSucHp5o08Z1gtEuh1-gE38x8eW~yy6xZjdBYC1XL3pJVbIkv08teI0DTEXhv~5mJXrhnK4SD-BJAeolVHyQlYSMz14soYw7ysRHiwPCrzmSVwkPl3Btunjlw__", "https://cf-ea.everytime.kr/attach_thumbnail/988/80340151/everytime-1770775255041.jpg?Expires=1772370244&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=XRWVzE0dSMmnZsNsbQF-JlwUDvXn0kdQqD9sB--nmyOODtJ99c1mMxFvfhJbDIWtbpzPiNJian5SaVvtotibpMwbcHqkmTAb51pJhbzb1J5wvg3-ivjgPKioXcgTtIwo0PT32EQDDSNwOweBHjY8sLENmj47ZMnExDRtEFu1E6gzfduJujk7tIceVUYMfc1EcIYjbcM7wHTrq6OMExxpjPiYOTixdjQF6cAEdh4if6LEFx552WUJt8dvYxEpYbxh9WJp7JOaCyYkcpo2ZdIlHhOgmtigpyXZhMqxsOjna4NjIT9J3ACvQc-B4646Uit-n15djAR4McFcGjJlBVzHEA__", "https://cf-ea.everytime.kr/attach_thumbnail/108/80340152/everytime-1770775255533.jpg?Expires=1772370244&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=G8QOA3sufhsBKGmbSwoq6rAQnjLE7I1mdBGDGd~wTANjfsqFcROYpTgmha0pIJ0BPawshHnR57Y3lydb3sXTPkL9pIz-F1A3mwgF-rV1IBCLMlITWitFbQzxask6xGmx4i-fq3zz4ggWsdC4qWLyZaSpHaY-DjXgZyoC6eXQs~6LDNFAm-ljGutlUd5oNCC2Mp8qjN~DzrqRdB3qiOc858TCDFYHvYXjnK23XV9O5WB1YzWfo9yLUdqkYMS8tBEdv6MS4wb6eAnAVcFSCdD7xwnYa3o8hCjPnRZxy8~AE8NWbSOYD5Al0xTWKIt-NGL0mYiqSyyMnJNWHx-bVPiwNg__", "https://cf-ea.everytime.kr/attach_thumbnail/576/80340153/everytime-1770775255992.jpg?Expires=1772370244&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=Bcf6CnhqJ2U7a~vOMXqCkgLUtSPsTWzjzbhCIcu0bP3LOpk5AMM99YKSPsVxIpS3KuFJAqHoEsTCuBl~YzCfRKJFsskuIeh86ftk128cOS25QRW39xvEljhtFapKTEJG2mu9LGww9uSIHtsXt~a3qp22Ua3viNgOJSUU9nODqvzeUHkxPre2cmSfQ9F8VyPUZ~svS0A--1ieYdXA54TiB7eVHytGvGIaB9JLyvtYUflrt8zLzkUE2~A2CFm1BeUlR7QQh3aex7mERqULKAY0rz4pzbwdfKKU6lNJ0He2pA0eSnR-NMRL0AF7pWEm92C4sBVj94CVzAPUAqLzm4JUvg__", "https://cf-ea.everytime.kr/attach_thumbnail/930/80340154/everytime-1770775256426.jpg?Expires=1772370244&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=c8cBOW1u1xZeIxu~6khQo~bfsEz7GV3tJZlQoiod4cFSP8X0639OLS9OW7PsQ~CjKswTHtFjtx-cw0~6juaIBgNJDb3WdUH~j6d3eXqPnSH1Vu5r3Ph8lg2ShInaOme~1AOizWZzeRQVmKlf8e1VcZ0zrFSTR1FCGttTWP~wX1nBkK18DnXsY9NrN103tjrDBUR5s-7cMKDPTro-c~5k7k1Qw8cd5FqG2RD3KH8-mHEDRJr-gut0myrZAgPG4Y~BnYj6u5mVb0nLa0SdzDLN3wwCwImeq3kl2lKUotPKqvEHsTdSGROq74DXQ7p-o11hc0D-YEq9oyXrqiLI9Hn7zw__", "https://cf-ea.everytime.kr/attach_thumbnail/618/80340155/everytime-1770775256863.jpg?Expires=1772370244&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=icHs6P4GUm-XfOzynr6DDCwcahbZfK9uv8gd9wAKHqNjUNHZ1OWsDwvzeAbZJwIBztBCqVhv~hcDUcz3dNZ0tTJoDIIEZRChw8reU~G7C4SwFH4WjWflCmTbIg1qNh36TXErkvgWtw9~NsqrZ0vIE0wKCMZb9D~HIIfvbCk8cxwo5p77g4nKpXBUGIIZBah4Aoly22MvgEXtKwg3Cc9bhqTYzOZvK90wZdkGh-0AN1h3hfdsOjT2aBdywgcx1ONNkOFxDQSzjaR0GcfjnobY-DSycgvJxISCMA4dUbCD~OCcc4IzzSfEfVuID~urhp8qultzrnRIDr3NTpFrzpAo1w__"));
                clubs.add(Club.builder()
                                .name("CCC")
                                .category(Category.RELIGION)
                                .location("제1학생회관 305, 306호")
                                .shortIntroduction("기독교 선교 동아리 CCC")
                                .introduction(intro56)
                                .recruitStart(LocalDateTime.of(2026, 1, 1, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 12, 31, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("정기 성경 공부와 기도회 참석이 필수입니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl("https://everytime.kr/375210/v/400102951")
                                .googleFormUrl("https://forms.gle/aDt8aHWZajGY7FoF9")
                                .build());

                // ===== 57. COC =====
                ClubIntroduction intro57 = ClubIntroduction.builder()
                                .overview("COC는 전남대학교의 기독교 청년 공동체 동아리입니다. 예배, 성경 공부, 친교 활동을 통해 신앙 안에서 함께 성장합니다.")
                                .activities("예배, 성경 공부, 소그룹 모임, 청년 기독교 교류 활동을 합니다.")
                                .ideal("기독교 신앙을 나누고 공동체 안에서 성장하고 싶은 학생이라면 환영합니다.")
                                .build();
                clubs.add(Club.builder()
                                .name("COC")
                                .category(Category.RELIGION)
                                .location("제2학생회관 413호")
                                .shortIntroduction("기독교 청년 공동체 COC")
                                .introduction(intro57)
                                .recruitStart(LocalDateTime.of(2026, 1, 1, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 12, 31, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("정기 예배와 모임 참석이 필수입니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl(null)
                                .googleFormUrl(null)
                                .build());

                // ===== 58. DFC =====
                ClubIntroduction intro58 = ClubIntroduction.builder()
                                .overview("DFC는 전남대학교의 기독교 제자훈련 동아리입니다. 성경적 제자도를 실천하며 캠퍼스에서 그리스도의 사랑을 나눕니다.")
                                .activities("제자훈련, 예배, 성경 공부, 캠퍼스 전도 활동을 합니다.")
                                .ideal("기독교 제자로 훈련받고 싶은 학생이라면 환영합니다.")
                                .build();
                clubs.add(Club.builder()
                                .name("DFC")
                                .category(Category.RELIGION)
                                .location("제2학생회관 404호")
                                .shortIntroduction("기독교 제자훈련 동아리")
                                .introduction(intro58)
                                .recruitStart(LocalDateTime.of(2026, 1, 1, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 12, 31, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("제자훈련 과정에 성실히 참여해야 합니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl(null)
                                .googleFormUrl(null)
                                .build());

                // ===== 59. DSM =====
                ClubIntroduction intro59 = ClubIntroduction.builder()
                                .overview("DSM은 전남대학교의 기독교 선교 동아리입니다. 다양한 선교 활동과 예배를 통해 기독교 신앙을 나누고 확장합니다.")
                                .activities("선교 예배, 성경 공부, 캠퍼스 전도, 기독교 문화 행사를 합니다.")
                                .ideal("기독교 선교에 열정이 있는 학생이라면 누구나 환영합니다.")
                                .build();
                clubs.add(Club.builder()
                                .name("DSM")
                                .category(Category.RELIGION)
                                .location("제2학생회관 310호")
                                .shortIntroduction("기독교 선교 동아리 DSM")
                                .introduction(intro59)
                                .recruitStart(LocalDateTime.of(2026, 1, 1, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 12, 31, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("선교 활동에 적극적으로 임해야 합니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl(null)
                                .googleFormUrl(null)
                                .build());

                // ===== 60. ENM =====
                ClubIntroduction intro60 = ClubIntroduction.builder()
                                .overview("ENM은 전남대학교의 기독교 성가대 동아리입니다. 찬양과 예배 음악을 통해 신앙을 표현하고 캠퍼스에 감동을 전합니다.")
                                .activities("찬양 연습, 예배 반주, 교내외 찬양 공연, 기독교 음악 활동을 합니다.")
                                .ideal("찬양과 예배 음악에 관심 있는 학생이라면 환영합니다.")
                                .build();
                clubs.add(Club.builder()
                                .name("ENM")
                                .category(Category.RELIGION)
                                .location("제1학생회관 319호")
                                .shortIntroduction("기독교 성가대 동아리")
                                .introduction(intro60)
                                .recruitStart(LocalDateTime.of(2026, 1, 1, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 12, 31, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("정기 찬양 연습 참석이 필수입니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl(null)
                                .googleFormUrl(null)
                                .build());

                // ===== 61. ESF =====
                ClubIntroduction intro61 = ClubIntroduction.builder()
                                .overview("ESF는 전남대학교의 기독교 청년 선교 동아리입니다. 캠퍼스 선교와 예배를 통해 복음을 전하고 신앙 공동체를 형성합니다.")
                                .activities("예배, 성경 공부, 전도 훈련, 캠퍼스 선교 활동을 합니다.")
                                .ideal("기독교 신앙을 캠퍼스에서 나누고 싶은 학생이라면 환영합니다.")
                                .build();
                intro61.addImages(List.of("https://cf-ea.everytime.kr/attach_thumbnail/894/77863922/everytime-1756865820629.jpg?Expires=1772370337&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=XBRRyXF1hEqRNq9q0wAYyrkPELdJXQy3qh4uruvNrvnTurhlSQsYQXqMXtKTuRIM1sXhXgvADVZkDN-wltMd72DYsD-QbQenVOEqmoa07RLPRqynsE5ATJ0BjL7cnh7c9YoSk7OVpQzSN0pKsO9VkXgzZoDj-rMGqppBNCl77HkOdmymytvlcI9u8lRN4E1qFzP--KHZTH112KkRNIcxW48p5ByxvH6ml-WYSHEFLi3Wp-9cLMbCdSajEmHdurgQZ10auWAW7zKC7OddoLmuVw-OQX1oI9EIc7d17VpHSO9siiBH0ccK6mC-UBaOxvTJ48vpjvGImwcecPBdvDjRsg__", "https://cf-ea.everytime.kr/attach_thumbnail/34/77863923/everytime-1756865821345.jpg?Expires=1772370337&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=skpkp-~S2nuShS-oyE~beDvQ7xCSDjgEmeAybKJf~OaOeWJqY6tDN5VQl7I-ByrJDHnVZ0oW4aeXnV9v68YjvL1yCUXcM4YSmamJ2JK2D9Skv3hA9VGXiH8cYumYZ0mJxhpWFnlCK4SdFJIMAxgRs~ZJiAro7sbtjrKjfpQcWRIv1tr29Zvr5N9L~n-7tWIXV2zq4pS3FheDhndmGxM5d7Rq~Q92yCtzuTTM0eCyG9DmpI4PljWBA7ilv6NXDzYnTE2dCES3PaESx0uhcfvhcx6hpD5oola4BZeD3Sgv4n-QTBMq0GJuV3da1VaxDtI9E-3CLMn1PZA3VAo~FWeJtg__", "https://cf-ea.everytime.kr/attach_thumbnail/220/77863924/everytime-1756865822006.jpg?Expires=1772370337&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=E4T9WHlrANb5YWbJjEPFNwJxaCV32v9F0MPUo7MjwQDL9kOszic6P2k9Qrr8AjJagNhsrK9hkt~X-hn83da6LkBnvKj8RTGYWBWkxRJDAOfyNHktlUoJvRl2KyK-NmKoEsH-rOUbLRQDhEl3ZO2g9KvqTFROkSa5G2pk0c~5JLoXaeAlstQKrbj6vrrAT~fc6Jv06kMs1EbStjsVtbyUk~PeIEq-Uuz9XG5xB2aAZV7hJC3IOC6Y4SILklH0QVCOuE~oyQX8gzh0-lxU6ixv8b0FQD9TbqgKJivWRXVgkGsacaV0rOQ3WFAgv1yZXixcoDmz8nBcNw6qV-2gx~6xsQ__", "https://cf-ea.everytime.kr/attach_thumbnail/363/77863925/everytime-1756865822649.jpg?Expires=1772370337&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=X15y8k~l9YWmzV0xuZ4TVXQdsiYtVOYIuFDAfUR9HxbeTWoc9R7zP25KOvHgSTPAI-uz6AlWB9fvg1H7WQN2Z5f2-oDzUyUNuOQsLI~hZDWbWLBBVpRhBooSrGvT1DyTn1JIAPPAS5Oh-i5z3BP7qMcCf7IY-qPrOanbmJ0WQINa5xZBvCtChuVmHpqN68U0XH7ZNnO1Xc7rgxPqkYgaZjXN-RtZEpvHkwMNTNyXl8RM-iocDkvGUDNz2TsRCbQ1QnfQ4v59Xg1y7lfstqH9DjeH7Z-uMKuh3-okY07deEB4SZrcatzgQddV-1oGt~dQvv9jIM6XSYbHYl5NQdbX3Q__"));
                clubs.add(Club.builder()
                                .name("ESF")
                                .category(Category.RELIGION)
                                .location("제1학생회관 421호")
                                .shortIntroduction("기독교 청년 선교 동아리")
                                .introduction(intro61)
                                .recruitStart(LocalDateTime.of(2025, 9, 3, 0, 0))
                                .recruitEnd(LocalDateTime.of(2025, 9, 8, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("정기 선교 활동과 예배 참석이 필수입니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl("https://everytime.kr/418923/v/388858903")
                                .googleFormUrl(null)
                                .build());

                // ===== 62. IVF =====
                ClubIntroduction intro62 = ClubIntroduction.builder()
                                .overview("IVF(InterVarsity Fellowship)는 전남대학교의 기독교 대학생 선교회입니다. 말씀 공부, 기도, 전도 활동을 통해 하나님 나라를 이 캠퍼스에 세워갑니다.")
                                .activities("성경 공부, 기도 모임, 전도 활동, IVF 전국 대회 참가를 합니다.")
                                .ideal("기독교 신앙으로 대학 생활을 풍성하게 만들고 싶은 학생이라면 환영합니다.")
                                .build();
                clubs.add(Club.builder()
                                .name("IVF")
                                .category(Category.RELIGION)
                                .location("제1학생회관 423호")
                                .shortIntroduction("기독교 대학생 선교회")
                                .introduction(intro62)
                                .recruitStart(LocalDateTime.of(2026, 1, 1, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 12, 31, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("정기 모임과 성경 공부 참석이 필수입니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl(null)
                                .googleFormUrl("https://naver.me/FeX3uSuG")
                                .build());

                // ===== 63. JDM =====
                ClubIntroduction intro63 = ClubIntroduction.builder()
                                .overview("JDM은 전남대학교의 기독교 캠퍼스 선교 동아리입니다. 예수님의 제자로 훈련받고 복음을 전하는 사명을 감당합니다.")
                                .activities("제자훈련 모임, 예배, 전도, 기독교 신앙 나눔 활동을 합니다.")
                                .ideal("예수님의 제자로 성장하고 싶은 학생이라면 누구나 환영합니다.")
                                .build();
                intro63.addImages(List.of("https://cf-ea.everytime.kr/attach_thumbnail/593/73254684/everytime-1739939413457.jpg?Expires=1772370661&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=nNA3pqoS1obAgb3qRu7yztusWHi5Vt4BZN5smCaXgm76SQWcC7w2lagSnTnhidK4wqTsKV1wfBpFXCztTs2CGNjWNS-WHmwE4BQ000nSvsd1p19BFote3Zwm3UpCCUlnYcCnx8qc9UhmOIcvUg6Ytcf9hbiBU~GjSoa3yM8ctIhGSsB5NtzZiOLFfQ86K7ugWTYhTuqSu~z4VFNcE1W-b5UFkV8Bg2xVWVw2ANewTcqwZM-eatJNe8wgf5HiKaQCftz6Dsq8zFiUihuxsyy~bVj0xaEJFH~yzI2L0M-GaC~K1HIsLHr8hng1bGPsyOymr2Vf4pNOFy5PJSxTD3Vt-w__", "https://cf-ea.everytime.kr/attach_thumbnail/932/73254686/everytime-1739939414148.jpg?Expires=1772370661&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=HlhKG9zxn8pd4XHOCMw~TGSVHT6q3hNgbTDf3eKSZL7138hswuBPRak~xSdFI0c2cQkYw2uUTxcdyTfRenBL4~WGTsCbgXO61Fs2QVHtgVbmaxpw4KYgKP9hXnBiepnQYXxjmxX1u-ohliiQkxULIPiBhOhZHSWny4rK~x~Uxw0LyaN43WWKneeSo~VwOkl6oiosIaxEXUsQWO-wPYRCacLtMNw8sUe9eQEikidpNJuXNphDvRVTELEoM2EXBT81e9TBKCWBvOPYAGYhMcXps~BsnAekN9QHvlAfCxfBxM-Ahqu0V9MhOoZhtnEcVY1tykMuTVE-25Nt97-aLo98Dg__", "https://cf-ea.everytime.kr/attach_thumbnail/231/73254690/everytime-1739939414881.jpg?Expires=1772370661&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=AJ1Jhq~jiHHh7dw2CdEysWZhBG3KCjbh4WEZu4Tjyt0m4L3FuPH51EQnx7Tit5sbGrEy~4NE86AwlEgDZ~0jG~BMvRHlSGnSV0OCHYf8EoKs0sjgJB988h0xumQFYVrSkC4bai4002bCiAPD49sH0Hs2boBoKIf0agoq2sa9wcwNA~sG~z9jZnmPoqHlI4dSlja9cCDJ-evnBGMlMXjxudyGAjuE7O1N20rXIUSaHMsykZjbYHNY-qYpkSWrD2DMbzfcswUwK-zgjHQYgLYtp7mK~rBJ5WeWDQmT-5K5L2YbdWRU6B4d6atF59WL1pACdZAQ8v6gEIJkveUCAFtpww__"));
                clubs.add(Club.builder()
                                .name("JDM")
                                .category(Category.RELIGION)
                                .location("제1학생회관 313호")
                                .shortIntroduction("기독교 캠퍼스 선교 동아리")
                                .introduction(intro63)
                                .recruitStart(LocalDateTime.of(2026, 1, 1, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 12, 31, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("제자훈련과 전도 활동에 성실히 참여해야 합니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl("https://everytime.kr/418923/v/369217164")
                                .googleFormUrl(null)
                                .build());

                // ===== 64. SFC =====
                ClubIntroduction intro64 = ClubIntroduction.builder()
                                .overview("SFC는 전남대학교의 기독교 학생 선교 동아리입니다. 성경 말씀을 중심으로 신앙을 다지고 교내외 선교 활동을 합니다.")
                                .activities("성경 공부, 기도, 선교 활동, 교내외 기독교 행사 참가를 합니다.")
                                .ideal("기독교 신앙 안에서 함께 성장하고 싶은 학생이라면 환영합니다.")
                                .build();
                intro64.addImages(List.of("https://cf-ea.everytime.kr/attach_thumbnail/958/77684515/everytime-1756444439145.jpg?Expires=1772370787&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=XDjqIgl0VgkXTd~kW2SmkLDuPcmqSC0CAA6ZXN9PVwTNhYPAbTQk1U~d9ixtFUuTvKA5ZZiKNrSN4z-FxJD~1iItmMJx1xXnr-U6D6jbEXuo~BvRnJB-TAGQEwkqxzc4oWRpK4GbEtfEENS9dhXa~xjk1ElhH0dZXtvdnCoFP2HBWBiihlGvK7yzUg5zpcx7UBWpUrYa~i~weDjw1OP0ZoI30VjKTy8xO0m7EuMva6aU0Sev4-SQ08WfYOVUSS8NWYPB-ECY~kphvFhPwT9L4ODcGQspl-p0tWJTz4aCMThQvs80N-E2eRF6qFYxu-NJVPH0YreAVq4krg4kP4NkmA__", "https://cf-ea.everytime.kr/attach_thumbnail/429/77684518/everytime-1756444440236.jpg?Expires=1772370787&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=E3X104MmJF3fAZcvZRpDcZlDvAbETVaBiBNgF4TejroHdc-O4TJdemwYnMs-sR5c-5ZnyIR8VcBBoBnXZtlZ304g~HnjyKkTaQPyIkSFYkzA34WgSIitBunKoiPGAav0M9LMy1nm5b9b~cfVZR2vyRdEFtbrpTpjdiE~ymqqt7uDlhiAs~4OGgfwcm14~bKjax2Zq1DKAyq4hPT6-PJsPD2DtWbuxEwk~8PRj7Q7c5kjku62-XnAPRccUMilv1~IRt1xxxjGSfSZJrhVVfHoTxU0v3se00SzsAyJz2MhuQXG58EwSDU4PgKl0usC-SlOFgHT4XsbvuwLHYA90iZkFQ__", "https://cf-ea.everytime.kr/attach_thumbnail/101/77684519/everytime-1756444440819.jpg?Expires=1772370787&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=aJ7y2DjHqdzR6TT3QS0OdvycqZi-RxXelsDASusjBKk7eTROuT0S8tLLbYii9G7DwFols1uqH-HkRTa6EdKUIgcTnQHwDYo0qjUOvmob7bEL~kV3mrfBM7jzcCnbFik2gtniY1YRvGq6bq3qVCpyWWqIbu4lw2W4~I-~L39PUWJrijGbl2Gp2S9pZa-iuAitt6rU4i66fyeLdsSVXZC0etWGCmKQ3lsK0BVPQ0tr5ttGACx4NdL-5~~7Oz04V3inLxqdMRUToNU8y2-3gaH61Ddr3JHfUw~sjrCfMi-RrxOtrH8~5jS6ikA9xb9O9KHPShtRl3QvycPixqHnCBO-Sg__", "https://cf-ea.everytime.kr/attach_thumbnail/134/77684520/everytime-1756444441380.jpg?Expires=1772370787&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=kZSLqNvsNPvPQC5VNFhaU8S4Bi2uWb1uhZFJiT3iOCRp~780BOM3bV0-zZJozd~DGGQ~SlJbJZlHWPoQe~md0ox-Tc7yY4UCwFMJdO7aAOw5-CRAMMI2W2LBci7KzJI4EnIedb-NEddIL5GowcqQtxD6H5cYMhhFAUmAT3ufvlMZRyf635xIrbemdaM8IQNG7qkaBe7NA6XE-6PhPijIj2iL0w8dfgLrmB-7cYOMKk9WGewWbzAQzV8GeZZeWSmyjC~lvC3~ZZRztSjXU9osuqZxaCjDoE8up4AjIEnosYJVMdn4zdZcGTkufQfeQZfb4uZr392kLq2CqItiDDacbQ__", "https://cf-ea.everytime.kr/attach_thumbnail/468/77684521/everytime-1756444441879.jpg?Expires=1772370787&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=hlr6sv8zXZGNUI6f7lbUE7KMeUUWZ8sflprEcXBhJBiv4pDnJBa01rUZpTY4KYdOg6vpfpcQHqPYvHqqKwWVRw7BjHY1cUAyBCzhD8zKX1946696yWo4AZy4Na9EVEMbPmwDqtOr5rvvl1axZkr02T5nQqHrTMgqGYKJHawF3ZpC2jDSqzVugVcu3DQFv8aEdxltzf6HKK17x5bNT3hWZv2GVWxMw-1k9y-5ElvFJQJcMcnn9DrvSEAFgrID-pJfD36GeuzhmO0DGhCgjnXEJLdclmhCOgKCeMHUAcYFGLOojBf2qhOkBRCf45b4iSNrBw23O6NFE7eevOCD9IvL4Q__", "https://cf-ea.everytime.kr/attach_thumbnail/689/77684522/everytime-1756444442368.jpg?Expires=1772370787&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=LPEsnqqk4r41DRtBeTXNyMhQoqmdgHo78h7J6IJhkRhxTJAOgW6xTDbTsWSq-WkRj09vqpB4hKCGXVEGhukmvSiS3eg8WGtzIsinZJiZTqoG8mOizXa3XdAC9sLI93d3WcIZB25t4ndOHVQyqVgLT09z~9M3aU9nt8ecG8hgPNMBc0KYsnOAem61nbLqMP0Ud9WwdZ4TfkvOqJQAnUD9hSWAprP6UN~xhbw8NAFv2Jsm0acZeI1HDt4u3cfIbHq7FxowpCRfNRRP7XWJy~7s2Yica-JzYKuLw0an0k1U8~MhzvIwgskj7pixZQVkS1DwMgvRJXhqKNYeSGNWL4zpvQ__", "https://cf-ea.everytime.kr/attach_thumbnail/588/77684523/everytime-1756444442879.jpg?Expires=1772370787&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=F014KwAkVO4lKsf772yMv3nCZRQdwNUf7mawdnKAz7HtMT9Y5JITP7l-2FO4SvAXLMel-1wD5ia-kdSeJtJRklTx15~EIgTxtMw0vcVjnVg414gbfeuBMbcSRPgpG8T7z4LviSnUzxoe4GcRYRS~a4OSF23lES6ZEqUCIWL9xuryUlRHtrmElXfEhxrZZWlBtKx3ImumFdjyRAgTOo7CVB~ef67eqxVDScHnBsTfUIRIWUM85fdAOppkYtKW-KvHCxylcNo07lVH3p2HLqTupWcmN7zSy~CvjvTbvpK6D2fNBrK9x-2Zzrq4P0REQ-tyjlJtWwn0swGbRKGd66bY4A__"));
                clubs.add(Club.builder()
                                .name("SFC")
                                .category(Category.RELIGION)
                                .location("제1학생회관 413호")
                                .shortIntroduction("기독교 학생 선교 동아리")
                                .introduction(intro64)
                                .recruitStart(LocalDateTime.of(2026, 1, 1, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 12, 31, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("정기 선교 활동과 성경 공부 참석이 필수입니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl("https://everytime.kr/375210/v/388335975")
                                .googleFormUrl(null)
                                .build());

                // ===== 65. UBF =====
                ClubIntroduction intro65 = ClubIntroduction.builder()
                                .overview("UBF(University Bible Fellowship)는 전남대학교의 기독교 성경 연구 동아리입니다. 성경 일대일 공부와 그룹 훈련을 통해 신앙을 성장시킵니다.")
                                .activities("성경 일대일 공부, 소그룹 훈련, 전도, 말씀 나눔 활동을 합니다.")
                                .ideal("성경을 체계적으로 공부하고 신앙을 성장시키고 싶은 학생이라면 환영합니다.")
                                .build();
                clubs.add(Club.builder()
                                .name("UBF")
                                .category(Category.RELIGION)
                                .location("제1학생회관 425호")
                                .shortIntroduction("기독교 대학생 성경 동아리")
                                .introduction(intro65)
                                .recruitStart(LocalDateTime.of(2026, 1, 1, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 12, 31, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("성경 공부와 일대일 훈련에 성실히 참여해야 합니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl(null)
                                .googleFormUrl(null)
                                .build());

                // ===== 66. 기백 =====
                ClubIntroduction intro66 = ClubIntroduction.builder()
                                .overview("기백은 전남대학교의 주짓수 무술 동아리입니다. 화~목요일 저녁 정기 훈련과 매주 금요일 합동 스파링을 통해 주짓수 기술을 연마합니다.")
                                .activities("정기 주짓수 훈련, 스파링, 대회 참가, 체력 강화 훈련을 합니다.")
                                .ideal("주짓수에 관심 있거나 체력과 자기방어 능력을 키우고 싶은 학생이라면 환영합니다.")
                                .build();
                clubs.add(Club.builder()
                                .name("기백")
                                .category(Category.SPORTS)
                                .location("제2학생회관 401호")
                                .shortIntroduction("주짓수 무술 동아리")
                                .introduction(intro66)
                                .recruitStart(LocalDateTime.of(2026, 1, 1, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 12, 31, 23, 59))
                                .regularMeetingInfo("화~목요일 오후 8시부터 10시")
                                .caution("훈련 중 부상 방지를 위해 안전 수칙을 준수해야 합니다. 정기 훈련 출석이 중요합니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl("https://everytime.kr/418923/v/389039471")
                                .googleFormUrl(null)
                                .build());

                // ===== 67. 당다라당 =====
                ClubIntroduction intro67 = ClubIntroduction.builder()
                                .overview("당다라당은 전남대학교의 난타·타악 퍼포먼스 동아리입니다. 타악기를 활용한 난타 공연을 기획하고 연습하며 신명나는 무대를 선보입니다.")
                                .activities("난타 정기 연습, 타악 앙상블 공연, 행사 공연 참가, 창작 퍼포먼스를 합니다.")
                                .ideal("신나는 퍼포먼스와 타악에 관심 있는 학생이라면 환영합니다.")
                                .build();
                intro67.addImages(List.of("https://cf-ea.everytime.kr/attach_thumbnail/317/78128660/everytime-1757664363613.jpg?Expires=1772370584&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=F87T32F1OrUTyp3lNNmC7E0SCsR4w-2F1WfwhI31mSXEVF02nQmet4dNaPXYXIdbVXxvVSO922uSH~u6WLgQAemj~lTX~7r2alsorohuwNCm9bbMahwFYTVZYaWNTf~1LFgxP4~Y33XWlKhQY7f4ziUcaV0v6YxAqRaUgT7CmYNK50c8h~eXVsNWtRwLraC3FvMQ9WI09QtQ80zi52Cgr4dAzU58bmerRPwGDZZRNRaj~BY5vUnfuX3lzZGjTNFwIXKVLftndANFWW9TMQ5OjTa1ItcfF~wSfSZk1vErN9El6NC5yc5WuYH~hWWcQ23-iKfPlBGMWRVzazPtyNuXpg__", "https://cf-ea.everytime.kr/attach_thumbnail/361/78128662/everytime-1757664364332.jpg?Expires=1772370584&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=J5Lis8XJCZwnZnQ4OsQQmfQeD8BoYhGaoVsN5C7qqzBr3ZKM89lBB0HOjx84azcTaKai~Gm118mZGoh42VWEOikus14ffwZF~alw-d5x4zK4yZ-VCW7cjvSxq76i4FYBUgrcr8mZRrlCuy2FscbCHmr33poHQZ3a2KgPLqnZtgKB5MakJKWaupxQIhndwFZ1eV1ePUiNg1oC4uuhyAAdCMTZ-Qijb-VlZk0o8blyhNXkmUDxkvxmbyNfgLIcH5p4SCHs34dAq4AXt6cMv4uZpADtlMVoZPN4i-NZIQsQcoFpuyF7~IDnLdbH3Ss82hoG2Gz0AKyHK3DheSzgZJFbUw__", "https://cf-ea.everytime.kr/attach_thumbnail/455/78128663/everytime-1757664364973.jpg?Expires=1772370584&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=Y8ftZINFOs5d3LgEN4Rz3tV52QQuEva9hG6MfVc65ZhZ~sIuKfDKHkvqem0TnFqBtlI5cjPeEfB~0mwUpy8YCgG3w2ecdBQLSuWh1SqU~~61pHN9ZZj21nUb2Mq4JBIVewDEw4YXwNj7i8qpfbbvvWogMY~9A4WGvThVx1agZPHuzdJXBQMMTMdE60O9ukz2IR5S06XrViRSTkNpYTvATnkZPN7sc9CdBpAr0nRdVdIGeUcM0xlKYRIXy59Rg7u60OSDWqoKPNdJuGyTi4mkVF9YWaFErF1VUFuQxlX35AAOZTRpNtgfREszpDjY50440ZmBJ6eOXke1pDkNbowA2g__", "https://cf-ea.everytime.kr/attach_thumbnail/996/78128664/everytime-1757664365618.jpg?Expires=1772370584&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=Nx3mCZr94x9xlc5Nj5Gsu8Vv-y7clGVuqxgSv1bboSPpvAlu7h2mkCI5LB5PotLP54nrxg57XBp4MUOS2uFVuXZjDh2t21kBTnhppBrwddaBH4BYhsUw3CqjAm0fqCwROxCH3OkqjqXQgf6eIc4w5cuorNLbvjkM2wOQ6fAODN0XiNjLB0TGW07XY7o~5nPvPG-nXHoe3cJAlneHNwjnTKJVIkBPqxAM3bFzuSAaynj6mmatGcIdT2xBiVTHRbisTIXqYYG8-1Ob7aW0wtP-3L2oji3bdQZJTrrqv1KTHp89vorWpGBg6~KJprRhtnViAR6bS2Wkz~9rmVIJWsAAMw__", "https://cf-ea.everytime.kr/attach_thumbnail/134/78128665/everytime-1757664366252.jpg?Expires=1772370584&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=TJ3xEc9SBoWS6klRapm1eRlHDHyZwuc-OoCuDZfQE6jUyo4M1kSqVyhp7XhfYukqPIA0EuOt-B6AZbp981LanbiuWwkCYkrmwEC588PnDRbOzo55EmHE1YPelAh219zKTmP9e2lDpLvmPtaAdEE1wpT8q9px4My6Gx4OxN4ciN2TVvr7pONP6rpXuSxEGOW6kFnGe5mKWq0IbHu4x6M-uc1ou4XLmHcWYBxXckt2lRbfy8tVX2I6vZWbcOiARuHXuc9eWSSu1oieUxlVVhvRDJjfhGW12t~lffFnG4BCquvlv~4x551MYOTwmQy2uv1kE0dZiSj6g6W8dpiZHPxiJA__", "https://cf-ea.everytime.kr/attach_thumbnail/951/78128666/everytime-1757664366897.jpg?Expires=1772370584&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=Jk-DN3fMmEzsWiq-JrD-pgCrSn15sgvJw4OTw7S98I7~bZxBZ5L5b0XYWsR5cwG9BuZE26Fz-Onlt9lN7zqdoHAh-Yn9QuVXC7l0FwfeL3Umi1JKQzFUMbIDl5Xw9g-ci-4ku1wCcCkFzqDrastz0JCETK9BDGagimRzf62XC55~0NAb5HER9IhxmE~-ndd9wrPd~JEQgMAf68DWqm~8NPVVjbXBqHZsd-GCs2i~yQnyMUOvP8j8rpn91JugEKnuPkEN~U35KDhTY7tkxvXR7PBvnkzsYmvnvSFzpJasyn6dasr2Ub7ev8~TP8nRk3hnC-0QfmQVSuU3HYgINiMNyQ__"));
                clubs.add(Club.builder()
                                .name("당다라당")
                                .category(Category.SPORTS)
                                .location("제1학생회관 318호")
                                .shortIntroduction("난타·타악 퍼포먼스 동아리")
                                .introduction(intro67)
                                .recruitStart(LocalDateTime.of(2026, 1, 1, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 12, 31, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("정기 연습 참석이 필수입니다. 공연 일정은 사전에 공지됩니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl("https://everytime.kr/544806/v/389754236")
                                .googleFormUrl(null)
                                .build());

                // ===== 68. 블랙베어스 =====
                ClubIntroduction intro68 = ClubIntroduction.builder()
                                .overview("블랙베어스는 전남대학교의 미식축구 동아리입니다. 정기 훈련과 대회 참가를 통해 미식축구 실력을 키우고 팀워크를 다집니다.")
                                .activities("미식축구 정기 훈련, 교내외 경기, 전국 대학 미식축구 리그 참가를 합니다.")
                                .ideal("미식축구를 사랑하고 팀 스포츠를 즐기는 학생이라면 환영합니다.")
                                .build();
                clubs.add(Club.builder()
                                .name("블랙베어스")
                                .category(Category.SPORTS)
                                .location("대운동장 스탠드 1층")
                                .shortIntroduction("미식축구 동아리")
                                .introduction(intro68)
                                .recruitStart(LocalDateTime.of(2026, 1, 30, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 1, 30, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("훈련 중 안전 수칙을 반드시 준수해야 합니다. 정기 훈련 참석이 중요합니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl("https://everytime.kr/375210/v/398862678")
                                .googleFormUrl("https://open.kakao.com/o/sDAvJn7g")
                                .build());

                // ===== 69. 어푸어푸 =====
                ClubIntroduction intro69 = ClubIntroduction.builder()
                                .overview("어푸어푸는 전남대학교의 수영 동아리입니다. 초보자부터 숙련자까지 함께 수영 실력을 키우며 건강한 생활을 즐깁니다.")
                                .activities("수영 정기 연습, 수영 기술 향상 훈련, 동아리 수영 대회 참가를 합니다.")
                                .ideal("수영을 배우고 싶거나 이미 하는 학생 모두 환영합니다.")
                                .build();
                clubs.add(Club.builder()
                                .name("어푸어푸")
                                .category(Category.SPORTS)
                                .location("제2학생회관 305호")
                                .shortIntroduction("수영 동아리")
                                .introduction(intro69)
                                .recruitStart(LocalDateTime.of(2026, 2, 20, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 3, 5, 23, 59))
                                .regularMeetingInfo("한달에 최소 2회 이상 참석은 필수")
                                .caution("수영장 안전 규칙을 반드시 준수해야 합니다. 수영 기초가 없어도 됩니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl("https://everytime.kr/375208/v/401078853")
                                .googleFormUrl("https://form.naver.com/response/-rm-550f6VArzKzF5qYCKg")
                                .build());

                // ===== 70. 얼라이브 =====
                ClubIntroduction intro70 = ClubIntroduction.builder()
                                .overview("얼라이브는 전남대학교의 얼티밋 프리스비 동아리입니다. 매주 화·목요일 정기 훈련과 전국 대회 참가를 통해 스포츠맨십과 팀워크를 기릅니다.")
                                .activities("얼티밋 프리스비 정기 훈련, 전국 대회 참가, 스피릿 게임, 국제 교류 경기를 합니다.")
                                .ideal("스포츠를 사랑하고 팀 활동을 즐기는 모든 학생을 환영합니다.")
                                .build();
                intro70.addImages(List.of("https://cf-ea.everytime.kr/attach_thumbnail/472/80871996/everytime-1771926434057.jpg?Expires=1772370809&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=N1su3FEFzfcQ7thyt0I~YcTAEzJKkQWS1OmUOdGd0Oih~kVPYwLzAE5lmN1-hvU~XVfNOmfF73ORojIpA7u6CQVc70JjxbbXVaCJboqFBWg6Igan5Y2InzhAnKODQ0alh-DadRiBPnq26mzo436HXpduednAiBkIT6dvA8ghLTEIORj5Mz8xqGshujudJw5JSI-E0MAcP9YUskiIY8CKIPPp7naA82fUOlTPiYHj~cAH6~IRABj1-aTYk79upfcY5StQydf~OCOOd1HOIWZzNkOxBjrNRZVpjqHcUy3h2mFtVWf8sWIxpbiwyrJivCCvwjFkLGXvSbFt~2zftBbDHw__", "https://cf-ea.everytime.kr/attach_thumbnail/390/80871998/everytime-1771926434714.jpg?Expires=1772370809&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=UNaBisIRT7odNIDh9kUpqBI0ul42rz7ad-Pk~Hu-~XTomIWD5PFSm6c37OGPSL~w5PlbGN8BSswIeVvov8kO5KeZRvnLqeclCs4U3VPuvGpe0f7vDW9YcHmsDUHoYpycn1W7d8tyi~XeVLOibRV9P0MBYK77-HFot8eGJym2CXRuoJ7FIKzm~3qCjSVODV8xwI8N5NT8wvyllS2bZ-pyCSFqDWmB7YWKVr1gFZcrjfHyDPKBYmPTvaBicxlEV2osqxhTwjg03ZSeZih3EKBBSigp155uvZJ~Rt00-yKu4VDiT-nwug~cPAt69B3tlXVD0vWEIt-L56djEISXM64uIg__", "https://cf-ea.everytime.kr/attach_thumbnail/973/80872000/everytime-1771926435190.jpg?Expires=1772370809&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=cp-31xarFo4tN7QRSqEDAVPSBYk3fKAIWxRhw6FpLsyVS2TccIWCQxY8HoBgU585bw7VKQ89IcxPHLNJJLh0HKt0mkGDgerzn8c2ODGAaZsE-cf8A9cHYzJmVVIshKdqAc0HiA91gU37OgrO3oYqH~j6t-hpGFZ0kj204aUM6is91Sr978-~YAAR4Ajr3DHUbwZ6I5~ZVSp0QoxPjZul0qbPe1zYOss3GuETnqbdlJ5y6Crg4FB~8QLv3h1tig8edBVGWXg70CMAcgBI6EGZ40oTPvGVq8nzFDectdAIrYRf5z7QiPl7GU1k7bOuWkNMIAmFGCV1n-DLExEK0edrqQ__", "https://cf-ea.everytime.kr/attach_thumbnail/163/80872004/everytime-1771926435804.jpg?Expires=1772370809&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=n6HOQcdKzVzHbRYpt9gsKgGXJDWD6e6tOJLsmQdnjEO5x9fwpbFaMa1nv9K1Zt6ZzesmVX7loIVIzvRsOGn-n6XXFEMhULzGPQBeXywrDxD6HcMeVow47sysaWQDnP0q1jjkXPzW3nsR2IMPJxbNJ29Rn0yItPhX9x8RONsbf0CHbxoXGh42aHTsyyK141Z29PDdjf44AYNiOhxrqFFOuomYnEwBerEmnWo9FvoS0Jjbjm0s1e00YMmgYXIDGMW4kqCMp6G5pTP1ash~fMgmpzBqfYEC0qDsPZNze3W7KnCn694Qxt6j2Yq5Lg2kSigm37ySxhVtO4sNf8nd7V1PpA__", "https://cf-ea.everytime.kr/attach_thumbnail/658/80872008/everytime-1771926436288.jpg?Expires=1772370809&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=ns4x9ye3eRYQYtcLbmhYJjDZ399WDic5OWRPoVsUPHl5brZ4lC61Zdu9mMQmDI79RjDDzrrA6HYnBMT6PnHKK4J6HZF8wHtR6LkBi5CDHR3bfGDkn8tz9PWEwAXlktmKKsXO9EQZMwHoAGrxsaQcOn1mqMqH2ZRnCZ0vOdlK97ymTx48gMYP2YRhgRRfP--P5wO5WMOY-NWd1w6GK5g~mx5mbZiNJnoONrUxPShMcXUHCQQzAR2o1vi~eeeirvJEdnpQoLViIfwYcQ0t9XnVt-nVz-Zx4Zvn7G0wr5kvkEDDY0S1DdGtBKONdoNaMPmy0MmxbOI7~2lPTWrCwxCOdA__", "https://cf-ea.everytime.kr/attach_thumbnail/798/80872012/everytime-1771926436778.jpg?Expires=1772370809&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=YC1MzYgzSeNuCjyb4fd-nHn9o1UtL4nd3pfoU1pDxep4HnqW49BtBnfD4~xqUz7Axz6W0jpT2rvMwBvzdrFtRbKq~FgMFw4NkKOFk1KPPKZa1Le6n-R8XX-rGtj~2kyMkMe0N~3Yhz1FEL9sTjA0suh9bMh1h6nhiyIB2BhrmtIPMyy3pT6NgPE2Gq95Wv3EWJFgdyiH5utBYeBtHe4aGSjMvTTDgvKuadHXwuqyMZ-bLsX1wiUYwBpIua0hPAhY0eK9SXEnhB8GlsNmZPPgcYzSbhO0k79aHZ6tmit5qfyTzWxN7arydyOUUOIbAaVSKqsi66PYy4Voncnrp8Q4Mw__"));
                clubs.add(Club.builder()
                                .name("얼라이브")
                                .category(Category.SPORTS)
                                .location("제2학생회관 306호")
                                .shortIntroduction("얼티밋 프리스비 동아리")
                                .introduction(intro70)
                                .recruitStart(LocalDateTime.of(2026, 2, 16, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 3, 13, 23, 59))
                                .regularMeetingInfo("매주 화, 목 18시 30분~21시 30분")
                                .caution("정기 훈련 참석이 필수입니다. 스포츠맨십을 중요시합니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl("https://everytime.kr/375210/v/401565037")
                                .googleFormUrl("https://forms.gle/4Hz4UAkYLsfWXf2J6")
                                .build());

                // ===== 71. 전검회 =====
                ClubIntroduction intro71 = ClubIntroduction.builder()
                                .overview("전검회는 전남대학교의 검도 동아리입니다. 정기 수련을 통해 검도 기술을 연마하고 심신을 단련합니다.")
                                .activities("검도 정기 수련, 품새 및 대련 연습, 전국 대학 검도 대회 참가를 합니다.")
                                .ideal("검도에 관심 있거나 심신 단련을 원하는 학생이라면 환영합니다.")
                                .build();
                intro71.addImages(List.of("https://cf-ea.everytime.kr/attach_thumbnail/608/81004160/everytime-1772118870321.jpg?Expires=1772370513&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=jJ-HZsUK-EQUM0CgunmcoOqkxvDCJRwFyY-~Kp~5L7P8yrj91aCYSYpBpR6dfLJxtFub-qgEi4HKZWm1R5DgRkHJneamzcj4Edp499y5W-qAo-e5g9HpoHLvwJ3mYXRmII0qeWlc~4Pdfhq1lYZ~~sKzz6yklE9h9slXk1qbEGbVdICy18YEP50HjvcSOAfRKG8NpDabI3jCeojK3GmByLZvyKsX3T9S9-b5FODKfcFnf2oA7UGnrXUAkUjnv-Blq~aDyNYtgZYf41eN~qgx5oShzdcHkeWWu2ta9aj4Xs7rbJo5tcWeEiARqPRZaS4f6Li6Evkgl~YKH6QzttFAMA__", "https://cf-ea.everytime.kr/attach_thumbnail/314/81004161/everytime-1772118870867.jpg?Expires=1772370513&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=hv1jE8~4zNZMQV2sv07QqYyHqSc0dQ3ipHVhvtPUC9MvVj0jMd3WcL6D261VSt~1Wd7nqrh7jjKd2i0E0SS61D96v3v4M8ClJ7NSnZsaN7h8pgze3dOYGwLjpshHIm1Ap9g4Rjz~~q8f0l3Cr3PhfWR5lwWpzUWJ6mkz2TDNx3N-SRv3czVC78TxUquFfIQV1ltod0gjlqPhdsCwhc2kqs-vPNCTOBM4cf2zqsKEwG8yljrLs6ni4y~r9iQ5N0RHkcgPRjVp75i4TgbFf2X5hTKdtBjL7bha1LWonOXZyoAZg60mwunWM7LmVhnOkG5ziNlnv9A~B05ZGa-o58xxKg__", "https://cf-ea.everytime.kr/attach_thumbnail/968/81004162/everytime-1772118871334.jpg?Expires=1772370513&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=OhAEePJKgk5sHRFNgqkMgPEZ~JciHPb3syXnET1EqvUXoKgqop3R~pKUVoovFdEEF43lbSv1ULsP4tkJwvtNpydnlWeVQPSZ9guMOn72G8guwU8rcYJ8VoeWWRHO1Swcm6TdQwl3lzJu8ZoAvv5CTeQiKDSIANFUJWjKH3~xjqQmsjmbwg2cZqWbMsvfFii6hJ5Tbe04KNBkm0G~qxAVy~2VmTSq8GNBqCIi1RnNVIXNjgss7OsJR1T8wNcPNzPd1ztQhOpGNO1yEwOkQGgtqU~Lt8bSX2JCZgpKZslXcAiNdwDTodyl6jt6fowZZA3tZH6pmNkz07LM6iXbKY8usA__", "https://cf-ea.everytime.kr/attach_thumbnail/46/81004163/everytime-1772118871714.jpg?Expires=1772370513&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=L8pFdgVx2xl9r0ar0A7YJo9xuMl9RBzSO8eEdvIYsYlDEHP3ew8cr0rXIxqenooO~50B0iM2IUhPHLnm9Mw8UMvHThjpFUAzS10UqHfA1r9aqo-q5ORMDCbMPCejwxfJnNhKcVIIQUOXjx0EZDM2-jPFou1kbMiZP3oW~avlEKvvqgwar1qPlpuhODOg1wc32UUXM41W4oV29ee7MfKsl1saVE6~HMDijTk2Is9vBlF1OwpgA3CTg-ti4KFfDAQSW1nq4X45fzKstK2dGi9gDZzipQVDvRDI5tF5nJWm3u2J2vAGY7knvcsjAcwwkBVZsSNZqQ2~lB6FivtmVlRbqQ__", "https://cf-ea.everytime.kr/attach_thumbnail/338/81004164/everytime-1772118872049.jpg?Expires=1772370513&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=obhNUY0ALoJd3LTqfCn~lsdB5wtnjls50AqzW2SjygS5lkyXginkncShCaG0VwSUJPnRZUbSMStU-~hCq~N7YzHSII2giEt6Xy0bEDu5kBuu7J0sm9TSqOCUJZpUtqDudwdvIIJ4Xz4lNJ~jRIl009~XpC39MexmK4za4hs6XQBtW1vTrnxUFoqmLX4exXWn4PdZjx44ionpxB1yCdLG16oaqPS-icdd6y3CGdPCeSBONlObunUHNIZQaluHMrQlk8REDR6dokOjhcXaRy5IrleD4EzgfGaPQTrxjpwYHK8kHNpPWYY4AB3sWr7Jcijm--FonOE4rIrbVi~OwpGGEg__", "https://cf-ea.everytime.kr/attach_thumbnail/936/81004165/everytime-1772118872428.jpg?Expires=1772370513&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=VUjVmp19arjWrLH8qfL6tIxaKQqui2Ao0voo5dKm8wNrolmJpbIBZOAi7Ykl54dmOxJQcmTmyfwWU6tTYnfmn0igHOp~47q6mFjWNLAJjYYBIOJRjfpgRCXt1~tAmnLA5~ufiRVKl-ms15oICjWptRVrr1-4QeQZC-paKdwE86tH0~j4m-fsPWVMJT1~GvNzJ4DS1AWUw-rWfYGMDMCCbQe~Q~~DIrLDC-Q43uKu0DEvAPak9OlwKYv6a80eht3WEw5H42mH~PH27HQV7hLgMM1d8x~Vu24D8jCUl~akImjTlXTN4OWWHtIiELDk7~7COTsjg6kHy4rn5APxsoad~w__", "https://cf-ea.everytime.kr/attach_thumbnail/228/81004166/everytime-1772118872755.jpg?Expires=1772370513&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=KvrXI40Mc7bDey4-UhHj1okKKfuHzTYEH1vUPFIz6QuLeVy5SJg~xAzjXNWClP6JfJUHEz6JFMLuI5Os7w96ok0Sd3ro0QampDBaYiwZbtk7AYAeapo~5rRPA4PoYe7f7HBhMNU3TPnJX~OS2WQ-bLm8oGXb0FDzMg6j981CuzK3NBvJS~EeQnwS3NwQeMOG1tEaeTFzl0fknrt7gt~xDyAQokPNkdXIco9AA40Jm~0ztYBhtMz424s9LAftSy41uV2yKFfKX7sKnlJtygZ0wb~X9sV8M7hSSeovfGdl9-e89flSgeM5FD14o~1HAdPqy8nrW1TcCCydM8IRdb9tAw__", "https://cf-ea.everytime.kr/attach_thumbnail/970/81004167/everytime-1772118873083.jpg?Expires=1772370513&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=kLfs7hHt~RJlrM8WRt48bMKg~CFLrX4r5g-yNLLiZyaGmK4eB~jPwwuZQUjtEsc5a-kDJH8gBvvl88-6PV5zQBSDxE89CVMfXpWQeabZBidsmSLxiI4kZTVkkWAWkx4IED2XeKW0YFkUWrY2uBG-HZ167gtjwL6LzRiMpnG7DZfmEyrT1VJyaGDx7Ez3cVy~egAwLXp8Lz8oW1JkRsYTamIig84ksoxRzVueOl4G7OZUmCpigKGv9VhF~HQMXHEkVI0-N~lIhNUB8qeuHTwHHQlKyyRx-OmdrInD~9rCPoO-K-QoL3epp0zfqYuLk-Hme4~8qn2sQrld~stp2T6~Jg__", "https://cf-ea.everytime.kr/attach_thumbnail/3/81004168/everytime-1772118873545.jpg?Expires=1772370513&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=e11a7zXW2TXr4UqV~i~hRB~~RocaDsrWJFJHipNhriLimnzkU7YDGO~LZ2TkJDrAAphLMpRe6oiRk~kVpYc6HtHiraz6rvaTBePH8W-LsQe5XWWsFmPKwcK4rQzgyPE1A0JKP53s66LC6Lw12Ts0AlpNuzOKcv7HdVtXK7zxb5EpRRWl5SUILlwHi9SSP8Ab0V-VH5j9ovrxGQXYODRAV-EJU3qi9jP1AUkxFiiPJoa9CqKW8Y7p7eEYs3auWbwzwgcumiUBlVqpjqP1jrqk5bvwc0bAwNE5b0M4toEIcq-9tY8vyyZ0ba-Lx4kAzpz3QHAQ3jWFKXZZpNmdUYhtGw__", "https://cf-ea.everytime.kr/attach_thumbnail/272/81004169/everytime-1772118873875.jpg?Expires=1772370513&Key-Pair-Id=APKAICU6XZKH23IGASFA&Signature=QnddcYyQaUDuS2R8aNoEAkPuct2EzKEP3eEep28AJ3fVUBbVT8I3egpwO4XBTI8Qa7wOzYAkFvziSTK3XJ5CGzf0crdhdsGjgizioMbugczT8RUPnypBMPS4ELl5Mm6-xQV88HWCuVvlnwTU4l1sCl8QZK5QiX8suX~Yd8BRM23h0Q8Qttu0HCbdLtn6ij6kXTwz9gPUkdiTrofNz48veKmKmCiUH3MjwW9-~OZu~UfVv1Mi9xYRDAlOISzr6Ve79-pLV0AV2gfAep~I~vWcaAoe-9lNe9MgomEZ6YzTqj1qqIMecqsenlQABMh8UyxNl7xW3VJ-2Nkrj~uP68Z1kw__"));
                clubs.add(Club.builder()
                                .name("전검회")
                                .category(Category.SPORTS)
                                .location("제2학생회관 407호")
                                .shortIntroduction("검도 수련 동아리")
                                .introduction(intro71)
                                .recruitStart(LocalDateTime.of(2026, 1, 1, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 12, 31, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("검도 수련 중 안전 장비를 반드시 착용해야 합니다. 정기 훈련 참석이 필수입니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl("https://everytime.kr/375210/v/401977599")
                                .googleFormUrl("https://everytime.kr/375210/v/401977599")
                                .build());

                // ===== 72. 전설 =====
                ClubIntroduction intro72 = ClubIntroduction.builder()
                                .overview("전설은 전남대학교의 풋살·축구 동아리입니다. 정기 훈련과 교내외 리그 참가를 통해 축구 실력을 키우고 즐거운 시간을 나눕니다.")
                                .activities("풋살 정기 훈련, 교내외 리그 참가, 자체 미니 토너먼트를 진행합니다.")
                                .ideal("축구와 풋살을 좋아하는 학생이라면 실력에 상관없이 환영합니다.")
                                .build();
                clubs.add(Club.builder()
                                .name("전설")
                                .category(Category.SPORTS)
                                .location("제1학생회관 402호")
                                .shortIntroduction("풋살·축구 동아리")
                                .introduction(intro72)
                                .recruitStart(LocalDateTime.of(2026, 2, 28, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 3, 5, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("정기 훈련 참석이 필수입니다. 부상 방지를 위한 기초 준비운동이 중요합니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl("https://everytime.kr/544806/v/402175641")
                                .googleFormUrl("https://naver.me/Ge7Z6lo4")
                                .build());

                // ===== 73. 태백회 =====
                ClubIntroduction intro73 = ClubIntroduction.builder()
                                .overview("태백회는 전남대학교의 태권도 동아리입니다. 주 2회 정기 수련을 통해 태권도 기술과 정신을 기르고 심신을 단련합니다.")
                                .activities("태권도 정기 수련, 품새 및 겨루기, 승단 심사 준비, 태권도 시범 활동을 합니다.")
                                .ideal("태권도에 관심 있거나 무도 수련을 원하는 학생이라면 환영합니다.")
                                .build();
                clubs.add(Club.builder()
                                .name("태백회")
                                .category(Category.SPORTS)
                                .location("제2학생회관 402호")
                                .shortIntroduction("태권도 수련 동아리")
                                .introduction(intro73)
                                .recruitStart(LocalDateTime.of(2026, 1, 1, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 12, 31, 23, 59))
                                .regularMeetingInfo("주 2회 18:00-21:00")
                                .caution("태권도 수련 중 안전 수칙을 준수해야 합니다. 정기 수련 참석이 필수입니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl("https://everytime.kr/418923/v/394860350")
                                .googleFormUrl(null)
                                .build());

                // ===== 74. 테크니션 =====
                ClubIntroduction intro74 = ClubIntroduction.builder()
                                .overview("테크니션은 전남대학교의 배드민턴 동아리입니다. 매주 화·목요일 정기 연습과 교내외 대회를 통해 실력을 키워갑니다.")
                                .activities("배드민턴 정기 연습, 복식·단식 경기, 교내외 배드민턴 대회 참가를 합니다.")
                                .ideal("배드민턴을 좋아하거나 운동이 필요한 학생이라면 환영합니다.")
                                .build();
                clubs.add(Club.builder()
                                .name("테크니션")
                                .category(Category.SPORTS)
                                .location("제1학생회관 424호")
                                .shortIntroduction("배드민턴 동아리")
                                .introduction(intro74)
                                .recruitStart(LocalDateTime.of(2026, 1, 1, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 12, 31, 23, 59))
                                .regularMeetingInfo("매주 화, 목 18:00-19:30")
                                .caution("정기 연습 참석이 필수이며, 장비 관리에 주의해야 합니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl("https://everytime.kr/418923/v/400735401")
                                .googleFormUrl(null)
                                .build());

                // ===== 75. DOVE =====
                ClubIntroduction intro75 = ClubIntroduction.builder()
                                .overview("DOVE는 전남대학교의 사격 동아리입니다. 사격 기초부터 심화까지 전문적인 훈련을 통해 사격 실력과 집중력을 키웁니다.")
                                .activities("사격 기초 훈련, 실내 사격장 연습, 전국 대학 사격 대회 참가를 합니다.")
                                .ideal("사격에 관심 있고 집중력을 기르고 싶은 학생이라면 환영합니다.")
                                .build();
                clubs.add(Club.builder()
                                .name("DOVE")
                                .category(Category.SPORTS)
                                .location("대운동장 스탠드 1층")
                                .shortIntroduction("사격 동아리")
                                .introduction(intro75)
                                .recruitStart(LocalDateTime.of(2026, 1, 1, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 12, 31, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("사격장 내 안전 규정을 철저히 준수해야 합니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl("https://everytime.kr/418923/v/388601221")
                                .googleFormUrl(null)
                                .build());

                // ===== 76. GRIP =====
                ClubIntroduction intro76 = ClubIntroduction.builder()
                                .overview("GRIP은 전남대학교의 볼더링·클라이밍 동아리입니다. 실내 클라이밍 훈련을 통해 체력과 집중력을 기르고 암벽 등반을 즐깁니다.")
                                .activities("실내 클라이밍 정기 훈련, 야외 바위 등반, 클라이밍 대회 참가를 합니다.")
                                .ideal("클라이밍에 관심 있거나 새로운 스포츠에 도전하고 싶은 학생이라면 환영합니다.")
                                .build();
                clubs.add(Club.builder()
                                .name("GRIP")
                                .category(Category.SPORTS)
                                .location("제1학생회관 310호")
                                .shortIntroduction("볼더링·클라이밍 동아리")
                                .introduction(intro76)
                                .recruitStart(LocalDateTime.of(2026, 3, 4, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 3, 5, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("클라이밍 안전 장비 착용이 필수이며, 정기 훈련에 참석해야 합니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl("https://everytime.kr/418923/v/402350634")
                                .googleFormUrl(null)
                                .build());

                // ===== 77. PPP =====
                ClubIntroduction intro77 = ClubIntroduction.builder()
                                .overview("PPP는 전남대학교의 탁구 동아리입니다. 주 3회 정기 연습과 교내외 대회 참가를 통해 탁구 실력을 향상시킵니다.")
                                .activities("탁구 정기 연습, 단식·복식 경기, 교내외 탁구 대회 참가를 합니다.")
                                .ideal("탁구를 좋아하거나 라켓 스포츠에 관심 있는 학생이라면 환영합니다.")
                                .build();
                clubs.add(Club.builder()
                                .name("PPP")
                                .category(Category.SPORTS)
                                .location("제2학생회관 405호")
                                .shortIntroduction("탁구 동아리")
                                .introduction(intro77)
                                .recruitStart(LocalDateTime.of(2026, 2, 21, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 3, 5, 23, 59))
                                .regularMeetingInfo("주 3회 (18:00~22:00)")
                                .caution("정기 연습 참석이 필수이며, 장비 관리에 주의해야 합니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl("https://everytime.kr/418923/v/401098272")
                                .googleFormUrl("https://docs.google.com/forms/d/e/1FAIpQLSdz4CXmk0G9l2Ls8H8g9VBEG1hJnbjEJ_YMLYpmWVtJXoFwOg/viewform")
                                .build());

                // ===== 78. Sea-fox =====
                ClubIntroduction intro78 = ClubIntroduction.builder()
                                .overview("Sea-fox는 전남대학교의 수상레저 동아리입니다. 카약, 수상스키 등 다양한 수상 스포츠를 즐기며 레저 문화를 경험합니다.")
                                .activities("카약, 수상스키 등 수상 스포츠 체험, 야외 레저 캠프, 수상 안전 교육을 합니다.")
                                .ideal("수상 스포츠에 관심 있고 모험을 즐기는 학생이라면 환영합니다.")
                                .build();
                clubs.add(Club.builder()
                                .name("Sea-fox")
                                .category(Category.SPORTS)
                                .location("용지동아리관 203호")
                                .shortIntroduction("수상레저 동아리")
                                .introduction(intro78)
                                .recruitStart(LocalDateTime.of(2026, 2, 28, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 3, 5, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("수상 활동 시 안전 규정을 반드시 준수해야 합니다. 수상 레저 비용이 발생할 수 있습니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl("https://everytime.kr/418923/v/402154627")
                                .googleFormUrl("https://naver.me/GYDkioaa")
                                .build());

                // ===== 79. 별하 =====
                ClubIntroduction intro79 = ClubIntroduction.builder()
                                .overview("별하는 전남대학교의 당구 동아리입니다. 월 2회 이상 정기 대관 연습을 통해 당구 실력을 키우고 친목을 다집니다.")
                                .activities("당구 정기 연습, 교내외 당구 대회 참가, 부원 간 친선 경기를 합니다.")
                                .ideal("당구에 관심 있거나 취미로 즐기고 싶은 학생이라면 환영합니다.")
                                .build();
                clubs.add(Club.builder()
                                .name("별하")
                                .category(Category.SPORTS)
                                .location("제1학생회관 427호")
                                .shortIntroduction("당구 동아리")
                                .introduction(intro79)
                                .recruitStart(LocalDateTime.of(2026, 2, 24, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 3, 11, 23, 59))
                                .regularMeetingInfo("월 2회 우산생활체육관 정기 대관 예정(요일은 투표 및 상의를 통해 진행)")
                                .caution("당구장 이용 에티켓을 지켜야 합니다. 정기 모임 참석이 중요합니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl("https://everytime.kr/418923/v/401696136")
                                .googleFormUrl("https://forms.gle/nGEAHqBNjgZpw6UXA")
                                .build());

                // ===== 80. 산악회 =====
                ClubIntroduction intro80 = ClubIntroduction.builder()
                                .overview("산악회는 전남대학교의 산악 등반 동아리입니다. 정기 등산과 암벽 등반 활동을 통해 자연을 즐기고 체력을 기릅니다.")
                                .activities("정기 등산, 암벽 등반, 산악 안전 교육, 전국 명산 탐방 활동을 합니다.")
                                .ideal("등산을 좋아하고 자연을 즐기는 학생이라면 환영합니다.")
                                .build();
                clubs.add(Club.builder()
                                .name("산악회")
                                .category(Category.SPORTS)
                                .location("제1학생회관 311호")
                                .shortIntroduction("산악 등반 동아리")
                                .introduction(intro80)
                                .recruitStart(LocalDateTime.of(2026, 2, 25, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 3, 7, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("등산 활동 시 안전 장비를 갖추고 안전 수칙을 준수해야 합니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl("https://everytime.kr/418923/v/401734834")
                                .googleFormUrl("https://forms.gle/Uwiau5rbZf49zJnA8")
                                .build());

                // ===== 81. CNRC =====
                ClubIntroduction intro81 = ClubIntroduction.builder()
                                .overview("CNRC는 전남대학교의 사이클·자전거 동아리입니다. 정기 라이딩과 사이클 대회 참가를 통해 자전거 문화를 즐깁니다.")
                                .activities("정기 라이딩, 사이클 기술 훈련, 전국 사이클 대회 참가, 장거리 투어를 합니다.")
                                .ideal("사이클과 자전거를 좋아하는 학생이라면 누구나 환영합니다.")
                                .build();
                clubs.add(Club.builder()
                                .name("CNRC")
                                .category(Category.SPORTS)
                                .location("미정")
                                .shortIntroduction("사이클·자전거 동아리")
                                .introduction(intro81)
                                .recruitStart(LocalDateTime.of(2026, 2, 23, 0, 0))
                                .recruitEnd(LocalDateTime.of(2026, 5, 29, 23, 59))
                                .regularMeetingInfo(null)
                                .caution("자전거 안전 수칙을 반드시 준수해야 합니다. 장거리 라이딩 참가 시 체력 관리가 필요합니다.")
                                .isInterviewRequired(true)
                                .isRegistered(false)
                                .everyTimeUrl("https://everytime.kr/375210/v/401593863")
                                .googleFormUrl("https://forms.gle/5T2VSLzsKViM9fiu9")
                                .build());

                return clubRepository.saveAll(clubs);
        }

        private void seedApplyFormsAndQuestions(List<Club> clubs) {
                List<ClubApplyForm> forms = new ArrayList<>();
                List<FormQuestion> questions = new ArrayList<>();

                for (Club club : clubs) {
                        ClubApplyForm form = ClubApplyForm.builder()
                                        .club(club)
                                        .title(club.getName() + " 지원서")
                                        .description("지원서를 작성해 주세요.")
                                        .build();
                        forms.add(form);
                }

                clubApplyFormRepository.saveAll(forms);

                for (ClubApplyForm form : forms) {
                        questions.add(FormQuestion.builder()
                                        .clubApplyForm(form)
                                        .question("간단한 자기소개를 해주세요.")
                                        .fieldType(FieldType.TEXT)
                                        .isRequired(true)
                                        .displayOrder(1L)
                                        .build());
                }

                formQuestionRepository.saveAll(questions);
        }

        private void seedClubMembers(List<User> users, List<Club> clubs) {
                User[] userArr = new User[users.size() + 1];
                for (int i = 0; i < users.size(); i++) {
                        userArr[i + 1] = users.get(i);
                }

                Club[] clubArr = new Club[clubs.size() + 1];
                for (int i = 0; i < clubs.size(); i++) {
                        clubArr[i + 1] = clubs.get(i);
                }

                List<ClubMember> members = new ArrayList<>();

                // ===== 1~81번 동아리 회장(CLUB_ADMIN) =====
                for (int i = 1; i <= 81; i++) {
                        members.add(ClubMember.builder()
                                        .user(userArr[i])
                                        .club(clubArr[i])
                                        .application(null)
                                        .activeStatus(ActiveStatus.ACTIVE)
                                        .role(Role.CLUB_ADMIN)
                                        .build());
                }

                // ===== SYSTEM_ADMIN: 1번 유저를 1번 동아리에 시스템 관리자로 등록 =====
                members.add(ClubMember.builder()
                                .user(userArr[1])
                                .club(clubArr[1])
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
