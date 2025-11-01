package com.kakaotech.team18.backend_server.domain.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.kakaotech.team18.backend_server.domain.answer.entity.Answer;
import com.kakaotech.team18.backend_server.domain.answer.repository.AnswerRepository;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.ActiveStatus;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.Role;
import com.kakaotech.team18.backend_server.domain.clubMember.repository.ClubMemberRepository;
import com.kakaotech.team18.backend_server.domain.email.dto.ApplicationInfoDto;
import com.kakaotech.team18.backend_server.domain.application.dto.ApplicationApprovedRequestDto;
import com.kakaotech.team18.backend_server.domain.application.entity.Stage;
import com.kakaotech.team18.backend_server.domain.application.entity.Status;
import com.kakaotech.team18.backend_server.domain.email.dto.FinalApprovedEvent;
import com.kakaotech.team18.backend_server.domain.email.dto.FinalRejectedEvent;
import com.kakaotech.team18.backend_server.domain.email.dto.InterviewApprovedEvent;
import com.kakaotech.team18.backend_server.domain.email.dto.InterviewRejectedEvent;
import com.kakaotech.team18.backend_server.domain.formQuestion.entity.FormQuestion;
import com.kakaotech.team18.backend_server.domain.formQuestion.repository.FormQuestionRepository;
import com.kakaotech.team18.backend_server.domain.application.dto.ApplicationApplyRequestDto;
import com.kakaotech.team18.backend_server.domain.application.dto.ApplicationApplyRequestDto.AnswerDto;
import com.kakaotech.team18.backend_server.domain.application.dto.ApplicationApplyResponseDto;
import com.kakaotech.team18.backend_server.domain.application.dto.ApplicationDetailResponseDto;
import com.kakaotech.team18.backend_server.domain.application.dto.ApplicationStatusUpdateRequestDto;
import com.kakaotech.team18.backend_server.domain.application.entity.Application;
import com.kakaotech.team18.backend_server.domain.application.repository.ApplicationRepository;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.entity.ClubApplyForm;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.repository.ClubApplyFormRepository;
import com.kakaotech.team18.backend_server.domain.email.dto.AnswerEmailLine;
import com.kakaotech.team18.backend_server.domain.email.dto.ApplicationSubmittedEvent;
import com.kakaotech.team18.backend_server.domain.user.entity.User;
import com.kakaotech.team18.backend_server.domain.user.repository.UserRepository;
import com.kakaotech.team18.backend_server.global.dto.SuccessResponseDto;
import com.kakaotech.team18.backend_server.global.exception.exceptions.ApplicationNotFoundException;
import com.kakaotech.team18.backend_server.global.exception.exceptions.ClubApplyFormNotFoundException;
import com.kakaotech.team18.backend_server.global.exception.exceptions.InvalidAnswerException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.kakaotech.team18.backend_server.global.exception.exceptions.PresidentNotFoundException;
import com.kakaotech.team18.backend_server.global.exception.exceptions.PendingApplicationsExistException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Objects.nonNull;

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
    private final ClubMemberRepository clubMemberRepository;
    private static final int MAX_READ_LIMIT = 100;

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
        ClubApplyForm form = clubApplyFormRepository.findByClubId(clubId)
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

        User president = clubMemberRepository
                .findUserByClubIdAndRoleAndStatus(application.getClubApplyForm().getClub().getId(), Role.CLUB_ADMIN, ActiveStatus.ACTIVE)
                .orElseThrow(() -> new PresidentNotFoundException("clubId:" + application.getClubApplyForm().getClub().getId()));

        List<AnswerEmailLine> emailLines = saveApplicationAnswers(application, request.answers());
        ApplicationInfoDto applicationInfoDto = buildApplicationInfo(application,  president);
        publisher.publishEvent(new ApplicationSubmittedEvent(applicationInfoDto, application.getId(), emailLines));

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

        User president = clubMemberRepository
                .findUserByClubIdAndRoleAndStatus(newApplication.getClubApplyForm().getClub().getId(), Role.CLUB_ADMIN, ActiveStatus.ACTIVE)
                .orElseThrow(() -> new PresidentNotFoundException("clubId:" + newApplication.getClubApplyForm().getClub().getId()));

        List<AnswerEmailLine> emailLines = saveApplicationAnswers(newApplication, request.answers());
        ApplicationInfoDto applicationInfoDto = buildApplicationInfo(newApplication, president);
        publisher.publishEvent(new ApplicationSubmittedEvent(applicationInfoDto, newApplication.getId(), emailLines));

        return new ApplicationApplyResponseDto(
                newApplication.getUser().getStudentId(),
                newApplication.getLastModifiedAt(),
                false
        );
    }

    public List<AnswerEmailLine> saveApplicationAnswers(Application application, List<AnswerDto> answers) {
        if (answers == null) answers = List.of();

        final Long formId = application.getClubApplyForm().getId();

        Map<Long, JsonNode> byQuestionNum = answers.stream()
                .filter(Objects::nonNull)
                .filter(a -> a.questionNum() != null)
                .collect(Collectors.toMap(
                        AnswerDto::questionNum,
                        AnswerDto::answer
                ));

        // 1) 폼의 질문을 표시순서대로 조회
        List<FormQuestion> questions = formQuestionRepository.findByClubApplyFormIdOrderByDisplayOrderAsc(formId);//해당 formId에 있는 질문만 조회

        // 2) 문항-답변 매칭(displayOrder 기반)
        List<Answer> toSave = new ArrayList<>(questions.size());
        List<AnswerEmailLine> emailLines = new ArrayList<>(questions.size());

        log.info("payload keys={}", byQuestionNum.keySet());

        for (FormQuestion q : questions) {
            Long disp = q.getDisplayOrder();
            JsonNode raw = byQuestionNum.get(disp-1);

            List<String> rawValues = extractTextValues(raw);
            String normalized = coerceForFieldType(q, rawValues);

            log.info("Q(disp={}, id={}, type={}, req={}): raw={}, rawValues={}, normalized='{}'",disp, q.getId(), q.getFieldType(), q.getIsRequired(), raw, rawValues, normalized);

            // 필수 문항 검사
            if (q.getIsRequired() && isBlank(normalized)) {
                throw new InvalidAnswerException("필수 문항 미응답: displayOrder=%d, questionId=%d, question=\"%s\""
                        .formatted(q.getDisplayOrder(), q.getId(), q.getQuestion()));
            }

            // 타입별 검증/정규화
            switch (q.getFieldType()) {
                case TEXT -> {
                }

                case RADIO -> {
                    if (q.getIsRequired() && isBlank(normalized)) {
                        throw new InvalidAnswerException("단일 선택 값이 필요합니다: displayOrder=%d, questionId=%d, question=\"%s\""
                                .formatted(q.getDisplayOrder(), q.getId(), q.getQuestion()));
                    }
                }

                case CHECKBOX -> {
                    List<String> options = splitAndTrim(normalized);
                    if (q.getIsRequired() && options.isEmpty()) {
                        throw new InvalidAnswerException("최소 1개 선택 필요: displayOrder=%d, questionId=%d, question=\"%s\""
                                .formatted(q.getDisplayOrder(), q.getId(), q.getQuestion()));
                    }
                    normalized = String.join(",", options);
                }

                case TIME_SLOT -> {
                    List<String> options = splitAndTrim(normalized);
                    if (q.getIsRequired() && options.isEmpty()) {
                        throw new InvalidAnswerException("최소 1칸 필요: displayOrder=%d, questionId=%d, question=\"%s\""
                                .formatted(q.getDisplayOrder(), q.getId(), q.getQuestion()));
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

            emailLines.add(new AnswerEmailLine(
                    q.getId(),
                    q.getDisplayOrder(),
                    q.getQuestion(),
                    isBlank(normalized) ? "(미입력)" : normalized)
            );
        }

        // 4) 일괄 저장
        answerRepository.saveAll(toSave);
        return emailLines;
    }

    @Transactional
    @Override
    public SuccessResponseDto sendPassFailMessage(Long clubId, ApplicationApprovedRequestDto requestDto, Stage stage) {

        ClubApplyForm form = clubApplyFormRepository.findByClubId(clubId)
                .orElseThrow(() -> {
                            log.warn("ClubApplyForm not found, clubId={}", clubId);
                            return new ClubApplyFormNotFoundException("clubId = " + clubId);
                        }
                );
        User president = clubMemberRepository
                .findUserByClubIdAndRoleAndStatus(clubId, Role.CLUB_ADMIN, ActiveStatus.ACTIVE)
                .orElseThrow(() -> new PresidentNotFoundException("clubId:" + clubId));

        if(stage == Stage.INTERVIEW) {
            List<Application> apps = applicationRepository.findAllByClubIdAndStage(clubId, stage);
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
                ApplicationInfoDto applicationInfoDto = buildApplicationInfo(a,president);
                Stage originalStage = a.getStage();
                a.updateStage(Stage.FINAL);
                a.updateStatus(Status.PENDING);
                publisher.publishEvent(new InterviewApprovedEvent(
                        applicationInfoDto,
                        a.getId(),
                        a.getUser().getEmail(),
                        requestDto.message(),
                        originalStage));
            }
            for(Application a : rejected) {
                ApplicationInfoDto applicationInfoDto = buildApplicationInfo(a,president);
                publisher.publishEvent(new InterviewRejectedEvent(applicationInfoDto));
                clubMemberRepository.clearApplicationByApplicationId(a.getId());
                applicationRepository.delete(a);
            }
        }
        if(stage == Stage.FINAL) {
            List<Application> apps = applicationRepository.findAllByClubIdAndStage(clubId, stage);
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
                ApplicationInfoDto applicationInfoDto = buildApplicationInfo(a,president);
                publisher.publishEvent(new FinalApprovedEvent(
                        applicationInfoDto,
                        a.getId(),
                        a.getUser().getEmail(),
                        requestDto.message(),
                        a.getStage()));
            }
            for(Application a : rejected) {
                ApplicationInfoDto applicationInfoDto = buildApplicationInfo(a,president);
                publisher.publishEvent(new FinalRejectedEvent(applicationInfoDto));
                clubMemberRepository.clearApplicationByApplicationId(a.getId());
                applicationRepository.delete(a);
            }
        }
        if(stage == null) {
            List<Application> apps = applicationRepository.findAllByClubId(clubId);
            boolean hasPending = apps.stream()
                    .anyMatch(a -> a.getStatus() == Status.PENDING);
            if (hasPending) {
                throw new PendingApplicationsExistException();
            }
            List<Application> approved = apps.stream()
                    .filter(a -> a.getStatus() == Status.APPROVED)
                    .toList();
            List<Application> rejected = apps.stream()
                    .filter(a -> a.getStatus() == Status.REJECTED)
                    .toList();
            for(Application a : approved) {
                ApplicationInfoDto applicationInfoDto = buildApplicationInfo(a,president);
                publisher.publishEvent(new FinalApprovedEvent(
                        applicationInfoDto,
                        a.getId(),
                        a.getUser().getEmail(),
                        requestDto.message(),
                        a.getStage()));
            }
            for(Application a : rejected) {
                ApplicationInfoDto applicationInfoDto = buildApplicationInfo(a,president);
                publisher.publishEvent(new FinalRejectedEvent(applicationInfoDto));
                clubMemberRepository.clearApplicationByApplicationId(a.getId());
                applicationRepository.delete(a);
            }
        }
        return new SuccessResponseDto(true);
    }

    //helper methods

    private ApplicationInfoDto buildApplicationInfo(Application a, User president) {
        return new ApplicationInfoDto(
                a.getClubApplyForm().getClub().getName(),
                a.getUser().getName(),
                a.getClubApplyForm().getClub().getId(),
                president.getEmail(),
                a.getUser().getStudentId(),
                a.getUser().getDepartment(),
                a.getUser().getPhoneNumber(),
                a.getUser().getEmail(),
                a.getLastModifiedAt()
        );
    }

    private List<String> extractTextValues(JsonNode node) {
        if (node == null || node.isNull()) return List.of();

        if (node.isTextual() || node.isNumber() || node.isBoolean()) {
            return List.of(node.asText());
        }

        if (node.isArray()) {
            List<String> out = new ArrayList<>();
            for (JsonNode item : node) {
                if (item.isTextual() || item.isNumber() || item.isBoolean()) {
                    out.add(item.asText());
                } else if (item.isObject()) {
                    JsonNode v = firstByCommonKeys(item);
                    if (v != null) {
                        if (v.isArray()) out.addAll(extractTextValues(v));
                        else if (v.isTextual() || v.isNumber() || v.isBoolean()) out.add(v.asText());
                        else {
                            collectStringLeaves(v, out, MAX_READ_LIMIT);
                        }
                    } else {
                        collectStringLeaves(item, out, MAX_READ_LIMIT);
                    }
                } else {
                    out.addAll(extractTextValues(item));
                }
            }
            return out;
        }

        if (node.isObject()) {
            JsonNode v = firstByCommonKeys(node);
            if (nonNull(v)) {
                if (v.isArray()) return extractTextValues(v);
                if (v.isTextual() || v.isNumber() || v.isBoolean()) return List.of(v.asText());
            }
            List<String> leaves = new ArrayList<>();
            collectStringLeaves(node, leaves, MAX_READ_LIMIT);
            return leaves;
        }

        return List.of(node.asText(""));
    }

    private JsonNode firstByCommonKeys(JsonNode obj) {
        String[] keys = {
                "interviewDateAnswer"
        };
        for (String k : keys) {
            JsonNode v = obj.get(k);
            if (nonNull(v) && !v.isNull()) return v;
        }
        return null;
    }

    private void collectStringLeaves(JsonNode node, List<String> out, int limit) {
        if (out.size() >= limit) return;
        if (node.isTextual() || node.isNumber() || node.isBoolean()) {
            out.add(node.asText());
            return;
        }
        if (node.isArray()) {
            for (JsonNode n : node) {
                if (out.size() >= limit) break;
                collectStringLeaves(n, out, limit);
            }
            return;
        }
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> it = node.fields();
            while (it.hasNext() && out.size() < limit) {
                Map.Entry<String, JsonNode> entry = it.next();
                JsonNode child = entry.getValue();
                collectStringLeaves(child, out, limit);
            }
        }
    }

    private static final Pattern DATE = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
    private static final Pattern TR = Pattern.compile("\\d{2}:\\d{2}-\\d{2}:\\d{2}");

    private String reassembleTimeSlots(List<String> vals) {
        if (vals == null || vals.isEmpty()) return "";

        boolean alreadyCombined = vals.stream().anyMatch(s -> s.contains(" ") && TR.matcher(s).find());
        if (alreadyCombined) return String.join(",", vals);

        List<String> out = new ArrayList<>();
        String currentDate = null;

        for (String raw : vals) {
            String s = normalize(raw);
            if (s.isEmpty()) continue;

            boolean looksDate = DATE.matcher(s).matches();
            boolean looksTimeRange = TR.matcher(s).matches();

            if (looksDate) {
                currentDate = s;
                continue;
            }
            if (looksTimeRange) {
                out.add(currentDate != null ? (currentDate + " " + s) : s);
                continue;
            }
            out.add(s);
        }
        return String.join(",", out);
    }


    private String coerceForFieldType(FormQuestion q, List<String> rawValues) {
        List<String> vals = rawValues.stream()
                .map(this::normalize)
                .filter(s -> !isBlank(s))
                .toList();

        switch (q.getFieldType()) {
            case TEXT -> {
                return vals.isEmpty() ? "" : vals.get(0);
            }
            case RADIO -> {
                return vals.isEmpty() ? "" : vals.get(0);
            }
            case CHECKBOX -> {
                return String.join(",", vals);
            }
            case TIME_SLOT -> {
                return reassembleTimeSlots(vals);
            }
            default -> throw new InvalidAnswerException("지원하지 않는 타입: " + q.getFieldType());
        }
    }

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
