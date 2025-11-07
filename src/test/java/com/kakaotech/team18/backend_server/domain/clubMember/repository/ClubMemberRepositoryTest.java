package com.kakaotech.team18.backend_server.domain.clubMember.repository;

import com.kakaotech.team18.backend_server.domain.club.entity.Category;
import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import com.kakaotech.team18.backend_server.domain.clubMember.dto.ClubMembershipInfo;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.ActiveStatus;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.ClubMember;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.Role;
import com.kakaotech.team18.backend_server.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ClubMemberRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ClubMemberRepository clubMemberRepository;

    private User user;
    private Club activeClub;
    private Club inactiveClub;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .name("Test User")
                .email("test@test.com")
                .phoneNumber("010-1234-5678")
                .studentId("20201234")
                .department("Computer Science")
                .build();
        entityManager.persist(user);

        activeClub = Club.builder()
                .name("Active Club")
                .category(Category.STUDY)
                .location("Seoul")
                .shortIntroduction("Active club short intro")
                .build();
        entityManager.persist(activeClub);

        inactiveClub = Club.builder()
                .name("Inactive Club")
                .category(Category.SPORTS)
                .location("Busan")
                .shortIntroduction("Inactive club short intro")
                .build();
        entityManager.persist(inactiveClub);

        ClubMember activeMember = ClubMember.builder()
                .user(user)
                .club(activeClub)
                .role(Role.CLUB_ADMIN)
                .activeStatus(ActiveStatus.ACTIVE)
                .build();
        entityManager.persist(activeMember);

        ClubMember inactiveMember = ClubMember.builder()
                .user(user)
                .club(inactiveClub)
                .role(Role.CLUB_MEMBER)
                .activeStatus(ActiveStatus.INACTIVE)
                .build();
        entityManager.persist(inactiveMember);
    }

    @Test
    @DisplayName("사용자의 멤버십 조회 - ACTIVE 상태만 필터링")
    void findClubMembershipsByUser_filtersInactiveMembers() {
        // when
        List<ClubMembershipInfo> memberships = clubMemberRepository.findClubMembershipsByUser(user);

        // then
        // ACTIVE 상태인 멤버십만 조회되어야 하므로, 결과 리스트의 크기는 1이어야 한다.
        assertThat(memberships).hasSize(1);

        // 조회된 멤버십 정보가 ACTIVE 상태의 멤버십 정보와 일치하는지 확인한다.
        ClubMembershipInfo resultInfo = memberships.get(0);
        assertThat(resultInfo.clubId()).isEqualTo(activeClub.getId());
        assertThat(resultInfo.role()).isEqualTo(Role.CLUB_ADMIN);
    }
}
