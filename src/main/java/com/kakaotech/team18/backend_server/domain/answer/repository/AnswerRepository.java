package com.kakaotech.team18.backend_server.domain.answer.repository;

import com.kakaotech.team18.backend_server.domain.application.entity.Application;
import com.kakaotech.team18.backend_server.domain.answer.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    long deleteByApplication(Application application);


    @Modifying
    @Query("DELETE FROM Answer a WHERE a.formQuestion.id IN :formQuestionIds")
    void deleteAllByFormQuestionIds(@Param("formQuestionIds") List<Long> formQuestionIds);
}