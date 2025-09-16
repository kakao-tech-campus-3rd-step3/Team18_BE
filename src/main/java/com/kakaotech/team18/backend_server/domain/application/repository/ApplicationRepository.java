package com.kakaotech.team18.backend_server.domain.application.repository;

import com.kakaotech.team18.backend_server.domain.application.entity.Application;
import com.kakaotech.team18.backend_server.domain.application.entity.Status;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    Optional<Application> findByClub_IdAndUser_Id(Long clubId, Long userId);

    @Query("""
            select app
            from Application app
            join fetch app.club
            where app.club.id = :clubId and app.status = :status
            """)
    List<Application> findByClubIdAndStatus(Long clubId, Status status);
}
