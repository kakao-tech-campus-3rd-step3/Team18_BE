package com.kakaotech.team18.backend_server.domain.formQuestion.service;

import com.kakaotech.team18.backend_server.domain.formQuestion.entity.FormQuestion;
import com.kakaotech.team18.backend_server.domain.formQuestion.repository.FormQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class FormQuestionServiceImpl implements FormQuestionService {
        private final FormQuestionRepository formQuestionRepository;

        @Override
        @Transactional(readOnly = true)
        public List<FormQuestion> getApplicationFormFieldsById(Long applicationFormId) {
                return formQuestionRepository.findByClubApplyFormIdOrderByDisplayOrderAsc(applicationFormId);
        }
}
