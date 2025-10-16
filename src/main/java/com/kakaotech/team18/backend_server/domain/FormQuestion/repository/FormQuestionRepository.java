package com.kakaotech.team18.backend_server.domain.FormQuestion.repository;

import com.kakaotech.team18.backend_server.domain.FormQuestion.entity.FormQuestion;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.entity.ClubApplyForm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FormQuestionRepository extends JpaRepository<FormQuestion,Long> {
    List<FormQuestion> findByClubApplyFormIdOrderByDisplayOrderAsc(Long clubApplyFormId);

    List<FormQuestion> findByClubApplyFormIdOrderByIdAsc(Long formId);

    List<FormQuestion> findByClubApplyForm(ClubApplyForm findClubApplyForm);
}
