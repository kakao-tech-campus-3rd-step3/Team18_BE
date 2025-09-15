package com.kakaotech.team18.backend_server.domain.applicant.repository;

import com.kakaotech.team18.backend_server.domain.applicant.entity.Applicant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicantRepository extends JpaRepository<Applicant, Long> {

    List<Applicant> findByClubId(long clubId);
}
