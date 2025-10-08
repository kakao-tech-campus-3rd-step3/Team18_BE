package com.kakaotech.team18.backend_server.domain.application.service;

import com.kakaotech.team18.backend_server.domain.answer.entity.Answer;
import com.kakaotech.team18.backend_server.domain.answer.repository.AnswerRepository;
import com.kakaotech.team18.backend_server.domain.FormQuestion.entity.FormQuestion;
import com.kakaotech.team18.backend_server.domain.FormQuestion.repository.FormQuestionRepository;
import com.kakaotech.team18.backend_server.domain.application.dto.ApplicationApplyRequestDto;
import com.kakaotech.team18.backend_server.domain.application.dto.ApplicationApplyRequestDto.AnswerDto;
import com.kakaotech.team18.backend_server.domain.application.dto.ApplicationApplyResponseDto;
import com.kakaotech.team18.backend_server.domain.application.dto.ApplicationApprovedRequestDto;
import com.kakaotech.team18.backend_server.domain.application.dto.ApplicationDetailResponseDto;
import com.kakaotech.team18.backend_server.domain.application.dto.ApplicationStatusUpdateRequestDto;
import com.kakaotech.team18.backend_server.domain.application.entity.Application;
import com.kakaotech.team18.backend_server.domain.application.entity.Stage;
import com.kakaotech.team18.backend_server.domain.application.entity.Status;
import com.kakaotech.team18.backend_server.domain.application.repository.ApplicationRepository;
import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import com.kakaotech.team18.backend_server.domain.club.repository.ClubRepository;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.entity.ClubApplyForm;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.repository.ClubApplyFormRepository;
import com.kakaotech.team18.backend_server.domain.email.dto.AnswerEmailLine;
import com.kakaotech.team18.backend_server.domain.email.dto.ApplicationSubmittedEvent;
import com.kakaotech.team18.backend_server.domain.email.dto.FinalApprovedEvent;
import com.kakaotech.team18.backend_server.domain.email.dto.FinalRejectedEvent;
import com.kakaotech.team18.backend_server.domain.email.dto.InterviewApprovedEvent;
import com.kakaotech.team18.backend_server.domain.email.dto.InterviewRejectedEvent;
import com.kakaotech.team18.backend_server.domain.user.entity.User;
import com.kakaotech.team18.backend_server.domain.user.repository.UserRepository;
import com.kakaotech.team18.backend_server.global.dto.SuccessResponseDto;
import com.kakaotech.team18.backend_server.global.exception.exceptions.ApplicationNotFoundException;
import com.kakaotech.team18.backend_server.global.exception.exceptions.ClubApplyFormNotFoundException;
import com.kakaotech.team18.backend_server.global.exception.exceptions.InvalidAnswerException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.kakaotech.team18.backend_server.global.exception.exceptions.PendingApplicationsExistException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final AnswerRepository answerRepository;
    private final ClubApplyFormRepository clubApplyFormRepository;
    private final FormQuestionRepository formQuestionRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher publisher;
    private final ClubRepository clubRepository;

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

    @Transactional
    @Override
    public ApplicationApplyResponseDto submitApplication(
            Long clubId,
            ApplicationApplyRequestDto request,
            boolean overwrite
    ) {

        //1. applicationForm 찾기
        ClubApplyForm form = clubApplyFormRepository.findByClubIdAndIsActiveTrue(clubId)
                .orElseThrow(() -> new ClubApplyFormNotFoundException("clubId:"+clubId));

        //2. 유저 정보생성(없으면 생성)
        User user = userRepository.findByStudentId(request.studentId())
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .studentId(request.studentId())
                            .email(request.email())
                            .name(request.name())
                            .phoneNumber(request.phoneNumber())
                            .department(request.department())
                            .build();
                    return userRepository.save(newUser);
                });

        //3. (폼+학번)으로 지원내역이 있는지 찾기
        Optional<Application> existingApplicationOptional = applicationRepository.findByStudentIdAndClubApplyForm(request.studentId(), form);

        //3.1 제출내역이 있고
        if (existingApplicationOptional.isPresent()) {
            Application existingApplication = existingApplicationOptional.get();

            //3.1.1 덮어쓰기가 true 이면
            if (overwrite) {
                return updateApplication(existingApplication, request);
            }

            //3.1.2 덮어쓰기가 false(=default)
            return new ApplicationApplyResponseDto(
                    existingApplication.getUser().getStudentId(),
                    existingApplication.getLastModifiedAt(),
                    true
            );
        } else {
            //3.2 제출내역이 없는경우
            return createApplication(user, form, request);
        }
    }

    private ApplicationApplyResponseDto updateApplication(
            Application application,
            ApplicationApplyRequestDto request
    ) {

        long deleted = answerRepository.deleteByApplication(application);
        log.info("기존 답변 삭제됨 applicationId={}, 삭제된문항수={}", application.getId(), deleted);


        List<AnswerEmailLine> emailLines = saveApplicationAnswers(application, request.answerList());
        publisher.publishEvent(new ApplicationSubmittedEvent(application.getId(), emailLines));

        return new ApplicationApplyResponseDto(
                application.getUser().getStudentId(),
                application.getLastModifiedAt(),
                false
        );
    }

    private ApplicationApplyResponseDto createApplication(
            User user,
            ClubApplyForm form,
            ApplicationApplyRequestDto request
    ) {

        Application newApplication = Application.builder().user(user).clubApplyForm(form).build();
        applicationRepository.save(newApplication);
        log.info("새로운 답변 기록됨 applicationId={}", newApplication.getId());


        List<AnswerEmailLine> emailLines = saveApplicationAnswers(newApplication, request.answerList());
        publisher.publishEvent(new ApplicationSubmittedEvent(newApplication.getId(), emailLines));

        return new ApplicationApplyResponseDto(
                newApplication.getUser().getStudentId(),
                newApplication.getLastModifiedAt(),
                false
        );
    }

    public List<AnswerEmailLine> saveApplicationAnswers(Application application, List<AnswerDto> answerList) {
        if (answerList == null) answerList = List.of();

        final Long formId = application.getClubApplyForm().getId();

        Map<Long, String> byQuestionId = answerList.stream()
                .filter(Objects::nonNull)
                .filter(a -> a.questionId() != null)
                .collect(Collectors.toMap(
                        AnswerDto::questionId,
                        a -> normalize(a.answerContent()),
                        (prev, next) -> next
                ));

        // 1) 폼의 질문을 표시순서대로 조회
        List<FormQuestion> questions = formQuestionRepository.findByClubApplyFormIdOrderByDisplayOrderAsc(formId);

        // 2) 문항-답변 매칭(displayOrder 기반)
        List<Answer> toSave = new ArrayList<>(questions.size());
        List<AnswerEmailLine> emailLines = new ArrayList<>(questions.size());

        for (FormQuestion q : questions) {
            String normalized = byQuestionId.getOrDefault(q.getId(), "");
            normalized = normalize(normalized);

            // 필수 문항 검사
            if (q.getIsRequired() && isBlank(normalized)) {
                throw new InvalidAnswerException("필수 문항 미응답: questionId=" + q.getId());
            }

            // 타입별 검증/정규화
            switch (q.getFieldType()) {
                case TEXT -> {
                }

                case RADIO -> {
                    if (q.getIsRequired() && isBlank(normalized)) {
                        throw new InvalidAnswerException("단일 선택 값이 필요합니다. questionId=" + q.getId());
                    }
                }

                case CHECKBOX -> {
                    List<String> options = splitAndTrim(normalized);
                    if (q.getIsRequired() && options.isEmpty()) {
                        throw new InvalidAnswerException("최소 1개 필요. questionId=" + q.getId());
                    }
                    normalized = String.join(",", options);
                }

                case TIME_SLOT -> {
                    List<String> options = splitAndTrim(normalized);
                    if (q.getIsRequired() && options.isEmpty()) {
                        throw new InvalidAnswerException("최소 1칸 필요. questionId=" + q.getId());
                    }
                    normalized = String.join(",", options);
                }
                default -> throw new InvalidAnswerException("지원하지 않는 타입: " + q.getFieldType());
            }

            toSave.add(
                    Answer.builder()
                            .application(application)
                            .formQuestion(q)
                            .answer(isBlank(normalized) ? null : normalized)
                            .build()
            );

            emailLines.add(new AnswerEmailLine(q.getId(), q.getDisplayOrder(), q.getQuestion(), isBlank(normalized) ? "(미입력)" : normalized));
        }

        // 4) 일괄 저장
        answerRepository.saveAll(toSave);
        return emailLines;
    }

    @Transactional
    @Override
    public SuccessResponseDto sendPassFailMessage(Long clubId, ApplicationApprovedRequestDto requestDto, Stage stage) {

        List<Application> apps = applicationRepository.findByClubApplyForm_Club_IdAndStage(clubId, stage);

        ClubApplyForm form = clubApplyFormRepository.findByClubId(clubId)
                .orElseThrow(() -> {
                            log.warn("ClubApplyForm not found, clubId={}", clubId);
                            return new ClubApplyFormNotFoundException("clubId = " + clubId);
                        }
                );

        if(stage == Stage.INTERVIEW) {
            boolean hasPending = apps.stream()
                    .filter(a -> a.getStage() == stage)
                    .anyMatch(a -> a.getStatus() == Status.PENDING);
            if (hasPending) {
                throw new PendingApplicationsExistException();
            }
            form.updateInterviewMessage(requestDto.message());
            List<Application> approved = apps.stream()
                    .filter(a -> a.getStage() == stage)
                    .filter(a -> a.getStatus() == Status.APPROVED)
                    .toList();
            List<Application> rejected = apps.stream()
                    .filter(a -> a.getStage() == stage)
                    .filter(a -> a.getStatus() == Status.REJECTED)
                    .toList();
            for(Application a : approved) {
                a.updateStage(Stage.FINAL);
                a.updateStatus(Status.PENDING);
                publisher.publishEvent(new InterviewApprovedEvent(
                        a.getId(),
                        a.getUser().getEmail(),
                        requestDto.message(),
                        a.getStage()));
            }
            for(Application a : rejected) {
                publisher.publishEvent(new InterviewRejectedEvent(
                        a.getClubApplyForm().getClub(),
                        a.getUser()));
            }
            applicationRepository.deleteAllInBatch(rejected);
        }
        if(stage == Stage.FINAL) {
            boolean hasPending = apps.stream()
                    .filter(a -> a.getStage() == stage)
                    .anyMatch(a -> a.getStatus() == Status.PENDING);
            if (hasPending) {
                throw new PendingApplicationsExistException();
            }
            form.updateFinalMessage(requestDto.message());
            List<Application> approved = apps.stream()
                    .filter(a -> a.getStage() == stage)
                    .filter(a -> a.getStatus() == Status.APPROVED)
                    .toList();
            List<Application> rejected = apps.stream()
                    .filter(a -> a.getStage() == stage)
                    .filter(a -> a.getStatus() == Status.REJECTED)
                    .toList();
            for(Application a : approved) {
                publisher.publishEvent(new FinalApprovedEvent(
                        a.getId(),
                        a.getUser().getEmail(),
                        requestDto.message(),
                        a.getStage()));
            }
            for(Application a : rejected) {
                publisher.publishEvent(new FinalRejectedEvent(
                        a.getClubApplyForm().getClub(),
                        a.getUser()));
            }
            applicationRepository.deleteAllInBatch(rejected);
        }
        if(stage == null) {
            boolean hasPending = apps.stream()
                    .filter(a -> a.getStage() == stage)
                    .anyMatch(a -> a.getStatus() == Status.PENDING);
            if (hasPending) {
                throw new PendingApplicationsExistException();
            }
            List<Application> approved = apps.stream()
                    .filter(a -> a.getStage() == stage)
                    .filter(a -> a.getStatus() == Status.APPROVED)
                    .toList();
            List<Application> rejected = apps.stream()
                    .filter(a -> a.getStage() == stage)
                    .filter(a -> a.getStatus() == Status.REJECTED)
                    .toList();
            for(Application a : approved) {
                publisher.publishEvent(new FinalApprovedEvent(
                        a.getId(),
                        a.getUser().getEmail(),
                        requestDto.message(),
                        a.getStage()));
            }
            for(Application a : rejected) {
                publisher.publishEvent(new FinalRejectedEvent(
                        a.getClubApplyForm().getClub(),
                        a.getUser()));
            }
            applicationRepository.deleteAllInBatch(rejected);
        }
        return new SuccessResponseDto(true);
    }

    //helper methods

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private String normalize(String s) {
        return (s == null) ? "" : s.trim();
    }

    private List<String> splitAndTrim(String raw) {
        if (isBlank(raw)) return List.of();
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(str -> !str.isEmpty())
                .distinct()
                .toList();
    }
}
