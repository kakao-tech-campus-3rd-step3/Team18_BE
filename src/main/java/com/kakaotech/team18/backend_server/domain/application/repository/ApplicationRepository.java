package com.kakaotech.team18.backend_server.domain.application.repository;

import com.kakaotech.team18.backend_server.domain.application.entity.Application;
import com.kakaotech.team18.backend_server.domain.application.entity.Stage;
import com.kakaotech.team18.backend_server.domain.application.entity.Status;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.entity.ClubApplyForm;
import jakarta.persistence.LockModeType;
import java.util.List;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    Optional<Application> findByClubApplyFormIdAndUserId(Long clubApplyFormId, Long userId);

    @Query("""
            select app
            from Application app
            join fetch app.clubApplyForm
            where app.clubApplyForm.id = :clubApplyFormId and app.status = :status
            """)
    List<Application> findByClubApplyFormIdAndStatus(Long clubApplyFormId, Status status);

    @Query("""
            select a 
            from Application a
            join a.user u 
            where u.studentId = :studentId and a.clubApplyForm = :form
            """)
    Optional<Application> findByStudentIdAndClubApplyForm(String studentId, ClubApplyForm form);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
             SELECT a
             FROM Application a
             WHERE a.id = :id""")
    Optional<Application> findByIdWithPessimisticLock(@Param("id") Long id);

    /**
     * applicationId를 사용하여, 해당 지원서가 속한 동아리의 ID(clubId)를 조회합니다.
     * <p>
     * 엔티티 전체를 로딩하지 않고 필요한 clubId 값만 직접 조회(Projection)하여 성능을 최적화합니다.
     * CustomSecurityService에서 특정 지원서에 대한 권한을 검사할 때 사용됩니다.
     *
     * @param applicationId 조회할 지원서의 ID
     * @return 해당 지원서가 속한 Club의 ID
     */
    @Query("""
            SELECT a.clubApplyForm.club.id
            FROM Application a
            WHERE a.id = :applicationId
            """)
    Optional<Long> findClubIdByApplicationId(@Param("applicationId") Long applicationId);

    @Query("""
            SELECT a
            FROM Application a
            WHERE a.clubApplyForm.club.id = :clubId AND a.stage = :stage""")
    List<Application> findAllByClubIdAndStage(Long clubId, Stage stage);

    @Query("""
            SELECT a
            FROM Application a
            WHERE a.clubApplyForm.club.id = :clubId""")
    List<Application> findAllByClubId(Long clubId);
}
