package com.kakaotech.team18.backend_server.domain.applicationForm.repository;

import com.kakaotech.team18.backend_server.domain.applicationForm.entity.ApplicationForm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationFormRepository extends JpaRepository<ApplicationForm,Long> {

}
