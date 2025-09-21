package com.kakaotech.team18.backend_server.domain.clubMember.repository;

import com.kakaotech.team18.backend_server.domain.clubMember.entity.ActiveStatus;
import com.kakaotech.team18.backend_server.domain.application.entity.Status;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.ClubMember;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.Role;
import com.kakaotech.team18.backend_server.domain.user.entity.User;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClubMemberRepository extends JpaRepository<ClubMember, Long> {

    @Query("""
            select cm
            from ClubMember cm
            join fetch cm.club
            where cm.club.id = :clubId and cm.role = :role
            """)
    List<ClubMember> findByClubIdAndRole(Long clubId, Role role);

    @Query("""
            select cm
            from ClubMember cm
            join fetch cm.club
            where cm.club.id = :clubId and cm.role = :role
            """)
    Optional<ClubMember> findClubAdminByClubIdAndRole(Long clubId, Role role);

    @Query("""
            select cm
            from ClubMember cm
            join fetch cm.application a
            where cm.club.id = :clubId and cm.role = :role and a.status = :status

            """)
    List<ClubMember> findByClubIdAndRoleAndApplicationStatus(Long clubId, Role role, Status status);

    @Query("""
        select cm.user
        from ClubMember cm
        where cm.club.id = :clubId
          and cm.role = :role
          and cm.activeStatus = :status
        """)
    Optional<User> findUserByClubIdAndRoleAndStatus(Long clubId, Role role, ActiveStatus status);
}
