package com.kakaotech.team18.backend_server.domain.clubMember.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.kakaotech.team18.backend_server.domain.club.entity.Category;
import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.AcademicStatus;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.ActiveStatus;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.ClubMember;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.ClubMemberProfile;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.Role;
import com.kakaotech.team18.backend_server.domain.user.entity.User;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
class ClubMemberProfileRepositoryTest {

        @Autowired
        private TestEntityManager entityManager;

        @Autowired
        private ClubMemberProfileRepository clubMemberProfileRepository;

        private Club club;
        private User user1;
        private User user2;
        private ClubMemberProfile profile1;
        private ClubMemberProfile profile2;

        @BeforeEach
        void setUp() {
                // Club 생성
                club = Club.builder()
                                .name("Another Club")
                                .category(Category.SPORTS)
                                .location("Busan")
                                .isInterviewRequired(false)
                                .shortIntroduction("Introduction")
                                .build();
                entityManager.persist(club);

                // User1 생성
                user1 = User.builder()
                                .name("User1")
                                .studentId("20210001")
                                .email("user1@test.com")
                                .department("CS")
                                .phoneNumber("010-1234-5678")
                                .build();
                entityManager.persist(user1);

                // ClubMember1 생성
                ClubMember member1 = ClubMember.builder()
                                .user(user1)
                                .club(club)
                                .role(Role.CLUB_ADMIN)
                                .activeStatus(ActiveStatus.ACTIVE)
                                .build();
                entityManager.persist(member1);

                // Profile1 생성
                profile1 = ClubMemberProfile.builder()
                                .clubMember(member1)
                                .name("User1")
                                .studentId("20210001")
                                .phoneNumber("010-1111-1111")
                                .college("Engineering")
                                .department("CS")
                                .academicStatus(com.kakaotech.team18.backend_server.domain.clubMember.entity.AcademicStatus.ENROLLED)
                                .joinDate(LocalDate.now())
                                .role(Role.CLUB_ADMIN)
                                .build();
                member1.setProfile(profile1); // 연관관계 설정
                entityManager.persist(profile1);

                // User2 생성
                user2 = User.builder()
                                .name("User2")
                                .studentId("20210002")
                                .email("user2@test.com")
                                .department("CS")
                                .phoneNumber("010-8765-4321")
                                .build();
                entityManager.persist(user2);

                // ClubMember2 생성
                ClubMember member2 = ClubMember.builder()
                                .user(user2)
                                .club(club)
                                .role(Role.CLUB_MEMBER)
                                .activeStatus(ActiveStatus.ACTIVE)
                                .build();
                entityManager.persist(member2);

                // Profile2 생성
                profile2 = ClubMemberProfile.builder()
                                .clubMember(member2)
                                .name("User2")
                                .studentId("20210002")
                                .phoneNumber("010-2222-2222")
                                .college("Engineering")
                                .department("CS")
                                .academicStatus(com.kakaotech.team18.backend_server.domain.clubMember.entity.AcademicStatus.ENROLLED)
                                .joinDate(LocalDate.now())
                                .role(Role.CLUB_MEMBER)
                                .build();
                member2.setProfile(profile2);
                entityManager.persist(profile2);

                entityManager.flush();
                entityManager.clear();
        }

        @Test
        @DisplayName("동아리 ID로 전체 프로필 조회")
        void findAllByClubId_Success() {
                // when
                List<ClubMemberProfile> profiles = clubMemberProfileRepository.findAllByClubId(club.getId());

                // then
                assertThat(profiles).hasSize(2);
                // 이름으로 정렬 보장은 없지만 포함 여부 확인
                assertThat(profiles).extracting("name").containsExactlyInAnyOrder("User1", "User2");
        }

        @Test
        @DisplayName("프로필 ID와 동아리 ID로 조회 - 성공")
        void findByIdAndClubId_Success() {
                // when
                Optional<ClubMemberProfile> result = clubMemberProfileRepository.findByIdAndClubId(profile1.getId(),
                                club.getId());

                // then
                assertThat(result).isPresent();
                assertThat(result.get().getName()).isEqualTo("User1");
        }

        @Test
        @DisplayName("프로필 ID와 동아리 ID로 조회 - 실패 (다른 동아리)")
        void findByIdAndClubId_Fail_WrongClub() {
                // given
                Club otherClub = Club.builder().name("Other Club").category(Category.SPORTS).location("Busan")
                                .isInterviewRequired(false).build();
                entityManager.persist(otherClub);
                entityManager.flush();

                // when
                Optional<ClubMemberProfile> result = clubMemberProfileRepository.findByIdAndClubId(profile1.getId(),
                                otherClub.getId());

                // then
                assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("동아리 ID와 학번으로 조회 - 성공")
        void findByClubMember_Club_IdAndStudentId_Success() {
                // when
                Optional<ClubMemberProfile> result = clubMemberProfileRepository
                                .findByClubMember_Club_IdAndStudentId(club.getId(), "20210001");

                // then
                assertThat(result).isPresent();
                assertThat(result.get().getId()).isEqualTo(profile1.getId());
        }

        @Test
        @DisplayName("중복 학번 체크 (본인 제외) - 중복 없음")
        void existsByClubMember_Club_IdAndStudentIdAndIdNot_False() {
                // when
                // User1이 자기 자신의 학번(20210001)을 그대로 유지하며 수정하는 경우 -> 중복 아님
                boolean exists = clubMemberProfileRepository.existsByClubMember_Club_IdAndStudentIdAndIdNot(
                                club.getId(), "20210001", profile1.getId());

                // then
                assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("중복 학번 체크 (본인 제외) - 중복 존재")
        void existsByClubMember_Club_IdAndStudentIdAndIdNot_True() {
                // when
                // User1이 User2의 학번(20210002)으로 수정하려고 하는 경우 -> 중복 발생
                boolean exists = clubMemberProfileRepository.existsByClubMember_Club_IdAndStudentIdAndIdNot(
                                club.getId(), "20210002", profile1.getId());

                // then
                assertThat(exists).isTrue();
        }
}
