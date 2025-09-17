package com.kakaotech.team18.backend_server.domain.application.service;

import com.kakaotech.team18.backend_server.domain.application.dto.ApplicationDetailResponseDto;
import com.kakaotech.team18.backend_server.domain.application.dto.ApplicationStatusUpdateRequestDto;
import com.kakaotech.team18.backend_server.domain.application.entity.Application;
import com.kakaotech.team18.backend_server.domain.application.repository.ApplicationRepository;
import com.kakaotech.team18.backend_server.domain.Answer.entity.Answer;
import com.kakaotech.team18.backend_server.domain.Answer.repository.AnswerRepository;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.entity.ClubApplyForm;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.repository.ClubApplyFormRepository;
import com.kakaotech.team18.backend_server.domain.user.entity.User;
import com.kakaotech.team18.backend_server.global.dto.SuccessResponseDto;
import com.kakaotech.team18.backend_server.global.exception.exceptions.ApplicationNotFoundException;
import com.kakaotech.team18.backend_server.global.exception.exceptions.ClubApplyFormNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final AnswerRepository answerRepository;
    private final ClubApplyFormRepository clubApplyFormRepository;

    @Override
    public ApplicationDetailResponseDto getApplicationDetail(Long clubId, Long applicantId) {
        // 1. clubId와 applicantId로 지원서 조회 (없으면 ApplicationNotFoundException 예외 발생)
        ClubApplyForm clubApplyForm = clubApplyFormRepository.findByClubId(clubId)
                .orElseThrow(() -> {
                            log.warn("ClubApplyForm not found, clubId={}", clubId);
                            return new ClubApplyFormNotFoundException("clubId = " + clubId);
                        }
                );
        Application application = applicationRepository.findByClubApplyFormIdAndUserId(clubApplyForm.getId(), applicantId)
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
        List<Answer> answers = answerRepository.findByApplicationWithFormQuestion(application);
        List<ApplicationDetailResponseDto.QuestionAndAnswer> questionsAndAnswers = answers.stream()
                .map(answer -> new ApplicationDetailResponseDto.QuestionAndAnswer(
                        answer.getFormQuestion().getQuestion(),
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

    @Transactional
    @Override
    public SuccessResponseDto updateApplicationStatus(Long applicationId, ApplicationStatusUpdateRequestDto requestDto) {
        // 1. applicationId로 지원서 조회 (없으면 ApplicationNotFoundException 예외 발생)
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ApplicationNotFoundException("applicationId: " + applicationId));

        log.info("지원서 상태 변경 시작: applicationId={}, oldStatus={}, newStatus={}",
                applicationId, application.getStatus(), requestDto.status());

        // 2. Application 엔티티의 상태 변경 메소드 호출 (Dirty Checking 활용)
        application.updateStatus(requestDto.status());

        log.info("지원서 상태 변경 완료: applicationId={}, newStatus={}",
                applicationId, application.getStatus());

        // 3. 성공 응답 DTO 반환
        return new SuccessResponseDto(true);
    }
}
