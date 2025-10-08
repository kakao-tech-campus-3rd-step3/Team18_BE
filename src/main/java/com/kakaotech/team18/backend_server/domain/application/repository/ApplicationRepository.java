package com.kakaotech.team18.backend_server.domain.application.repository;

import com.kakaotech.team18.backend_server.domain.application.entity.Application;
import com.kakaotech.team18.backend_server.domain.application.entity.Stage;
import com.kakaotech.team18.backend_server.domain.application.entity.Status;
import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import jakarta.persistence.LockModeType;
import java.util.List;

import com.kakaotech.team18.backend_server.domain.clubApplyForm.entity.ClubApplyForm;

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

    List<Application> findByClubApplyForm_Club_IdAndStage(Long clubId, Stage stage);
}
