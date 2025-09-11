package com.kakaotech.team18.backend_server.domain.applicationFormField.repository;

import com.kakaotech.team18.backend_server.domain.applicationFormField.entity.ApplicationFormField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationFormFieldRepository extends JpaRepository<ApplicationFormField,Long> {
    List<ApplicationFormField> findByApplicationFormIdOrderByOrderAsc(Long applicationFormId);
}
