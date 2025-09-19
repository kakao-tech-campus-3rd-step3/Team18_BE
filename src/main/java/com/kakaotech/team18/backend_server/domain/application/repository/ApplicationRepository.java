package com.kakaotech.team18.backend_server.domain.application.repository;

import com.kakaotech.team18.backend_server.domain.application.entity.Application;
import com.kakaotech.team18.backend_server.domain.application.entity.Status;
import java.util.List;

import com.kakaotech.team18.backend_server.domain.clubApplyForm.entity.ClubApplyForm;
import com.kakaotech.team18.backend_server.domain.user.entity.User;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;

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
    Optional<Application> findByUserAndClubApplyForm(String studentId, ClubApplyForm form);
}
