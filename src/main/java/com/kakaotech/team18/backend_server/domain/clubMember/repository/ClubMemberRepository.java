package com.kakaotech.team18.backend_server.domain.clubMember.repository;

import com.kakaotech.team18.backend_server.domain.application.entity.Stage;
import com.kakaotech.team18.backend_server.domain.application.entity.Status;
import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import com.kakaotech.team18.backend_server.domain.clubMember.dto.ClubMembershipInfo;
import com.kakaotech.team18.backend_server.domain.clubMember.dto.ClubListInfoDto;
import com.kakaotech.team18.backend_server.domain.clubMember.dto.ClubMembershipInfo;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.ActiveStatus;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.ClubMember;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.Role;
import com.kakaotech.team18.backend_server.domain.user.entity.User;
import java.util.List;
import java.util.Optional;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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
            join fetch cm.application a
            join fetch cm.user
            where cm.club.id = :clubId and cm.role = :role and a.stage = :stage
            """)
    List<ClubMember> findByClubIdAndRoleAndStage(Long clubId, Role role, Stage stage);

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
            join fetch cm.user
            where cm.club.id = :clubId and cm.role = :role and a.status = :status
            """)
    List<ClubMember> findByClubIdAndRoleAndApplicationStatus(Long clubId, Role role, Status status);

    @Query("""
            select cm
            from ClubMember cm
            join fetch cm.application a
            join fetch cm.user
            where cm.club.id = :clubId and cm.role = :role and a.status = :status and a.stage = :stage
            """)
    List<ClubMember> findByClubIdAndRoleAndApplicationStatusAndStage(Long clubId, Role role, Status status, Stage stage);

    @Query("""
        select cm.user
        from ClubMember cm
        where cm.club.id = :clubId
          and cm.role = :role
          and cm.activeStatus = :status
        """)
    Optional<User> findUserByClubIdAndRoleAndStatus(Long clubId, Role role, ActiveStatus status);

    @Query("""            
            select new com.kakaotech.team18.backend_server.domain.clubMember.dto.ClubMembershipInfo(cm.club.id, cm.role)
            from ClubMember cm
            where cm.user = :user and cm.activeStatus = com.kakaotech.team18.backend_server.domain.clubMember.entity.ActiveStatus.ACTIVE
            """)
    List<ClubMembershipInfo> findClubMembershipsByUser(User user);

    Optional<ClubMember> findFirstByRole(Role role);


    @Query("""
        select new com.kakaotech.team18.backend_server.domain.clubMember.dto.ClubListInfoDto(cm.club.id, cm.club.name,cm.role)
        from ClubMember cm
        join cm.club
        where cm.user = :user
        """)
    List<ClubListInfoDto> findClubListInfoByUser(User user);

    List<ClubMember> findByUser(User user);

    Optional<ClubMember> findByClubIdAndUserStudentId(Long clubId, String studentId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update ClubMember cm set cm.application = null where cm.application.id = :applicationId")
    int clearApplicationByApplicationId(Long applicationId);

    boolean existsByUserAndClubAndClubRoleAndActiveStatus(User user, Club club, @NotNull(message = "직책은 필수 값입니다.") Role role, ActiveStatus activeStatus);

    boolean existsByClubAndRole(Club club, @NotNull(message = "직책은 필수 값입니다.") Role role);
}
