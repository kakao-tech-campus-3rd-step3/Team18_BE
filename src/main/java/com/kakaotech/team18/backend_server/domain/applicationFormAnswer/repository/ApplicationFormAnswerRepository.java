package com.kakaotech.team18.backend_server.domain.applicationFormAnswer.repository;

import com.kakaotech.team18.backend_server.domain.application.entity.Application;
import com.kakaotech.team18.backend_server.domain.applicationFormAnswer.entity.ApplicationFormAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ApplicationFormAnswerRepository extends JpaRepository<ApplicationFormAnswer, Long> {
    @Query("""
           SELECT afa 
           FROM ApplicationFormAnswer afa 
           JOIN FETCH afa.applicationFormField 
           WHERE afa.application = :application
           """)
    List<ApplicationFormAnswer> findByApplicationWithFormField(@Param("application") Application application);
}