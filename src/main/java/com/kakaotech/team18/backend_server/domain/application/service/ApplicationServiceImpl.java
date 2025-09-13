package com.kakaotech.team18.backend_server.domain.application.service;

import com.kakaotech.team18.backend_server.domain.application.dto.ApplicationDetailResponseDto;
import com.kakaotech.team18.backend_server.domain.application.entity.Application;
import com.kakaotech.team18.backend_server.domain.application.repository.ApplicationRepository;
import com.kakaotech.team18.backend_server.domain.applicationFormAnswer.entity.ApplicationFormAnswer;
import com.kakaotech.team18.backend_server.domain.applicationFormAnswer.repository.ApplicationFormAnswerRepository;
import com.kakaotech.team18.backend_server.domain.user.entity.User;
import com.kakaotech.team18.backend_server.global.exception.exceptions.ApplicationNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationFormAnswerRepository applicationFormAnswerRepository;

    @Override
    public ApplicationDetailResponseDto getApplicationDetail(Long clubId, Long applicantId) {
        // 1. clubId와 applicantId로 지원서 조회 (없으면 ApplicationNotFoundException 예외 발생)
        Application application = applicationRepository.findByClub_IdAndUser_Id(clubId, applicantId)
                .orElseThrow(() -> new ApplicationNotFoundException("clubId=" + clubId + ", applicantId=" + applicantId));

        // 2. 지원자 정보(ApplicantInfo) DTO 생성
        User applicant = application.getUser();
        ApplicationDetailResponseDto.ApplicantInfo applicantInfo = new ApplicationDetailResponseDto.ApplicantInfo(
                applicant.getId(),
                applicant.getName(),
                applicant.getDepartment(),
                applicant.getStudentId(),
                applicant.getEmail(),
                applicant.getPhoneNumber()
        );

        // 3. 질문 및 답변(QuestionAndAnswer) DTO 리스트 생성
        List<ApplicationFormAnswer> answers = applicationFormAnswerRepository.findByApplicationWithFormField(application);
        List<ApplicationDetailResponseDto.QuestionAndAnswer> questionsAndAnswers = answers.stream()
                .map(answer -> new ApplicationDetailResponseDto.QuestionAndAnswer(
                        answer.getApplicationFormField().getQuestion(),
                        answer.getAnswer()
                ))
                .collect(Collectors.toList());

        // 4. 최종 응답 DTO 조립 및 반환
        return new ApplicationDetailResponseDto(
                application.getId(),
                application.getStatus().name(),
                applicantInfo,
                questionsAndAnswers
        );
    }
}
