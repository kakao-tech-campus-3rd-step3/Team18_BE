package com.kakaotech.team18.backend_server.domain.application.repository;

import com.kakaotech.team18.backend_server.domain.application.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    Optional<Application> findByClub_IdAndUser_Id(Long clubId, Long userId);
}
