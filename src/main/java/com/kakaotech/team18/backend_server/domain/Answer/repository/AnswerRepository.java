package com.kakaotech.team18.backend_server.domain.Answer.repository;

import com.kakaotech.team18.backend_server.domain.application.entity.Application;
import com.kakaotech.team18.backend_server.domain.Answer.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AnswerRepository extends JpaRepository<Answer, Long> {
    @Query("""
           SELECT afa
           FROM Answer afa
           JOIN FETCH afa.formQuestion
           WHERE afa.application = :application
           """)
    List<Answer> findByApplicationWithFormQuestion(@Param("application") Application application);

    void deleteByApplication(Application application);
}