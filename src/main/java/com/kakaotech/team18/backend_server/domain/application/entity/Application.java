package com.kakaotech.team18.backend_server.domain.application.entity;

import com.kakaotech.team18.backend_server.domain.BaseEntity;
import com.kakaotech.team18.backend_server.domain.answer.entity.Answer;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.entity.ClubApplyForm;
import com.kakaotech.team18.backend_server.domain.comment.entity.Comment;
import com.kakaotech.team18.backend_server.domain.user.entity.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Application extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "application_id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_apply_form_id", nullable = false)
    private ClubApplyForm clubApplyForm;

    @Enumerated(EnumType.STRING)
    @Column(name = "status",  nullable = false)
    private Status status = Status.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "stage", nullable = false)
    private Stage stage;

    @Column(nullable = false)
    private Double averageRating = 0.0;

    @OneToMany(mappedBy = "application", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Answer> answers = new ArrayList<>();

    @OneToMany(mappedBy = "application", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    private LocalDate interviewDate;

    private LocalTime interviewTime;

    @ElementCollection
    @CollectionTable(
            name = "application_interview_preference",
            joinColumns = @JoinColumn(name = "application_id")
    )
    @OrderColumn(name = "pref_idx")
    private List<InterviewPreference> interviewPreferences = new ArrayList<>();

    @Builder
    private Application(User user, ClubApplyForm clubApplyForm, Status status, Stage stage, LocalDate interviewDate, LocalTime interviewTime) {
        this.user = user;
        this.clubApplyForm = clubApplyForm;
        this.status = (status != null) ? status : Status.PENDING; // 기본값 보존
        this.stage = (stage != null) ? stage : Stage.INTERVIEW;
        this.averageRating = 0.0;
        this.interviewDate = interviewDate;
        this.interviewTime = interviewTime;
    }

    /**
     * 지원서의 상태를 변경합니다.
     * 이 메소드는 서비스 계층에서 트랜잭션 내에서 호출되어야 합니다.
     * @param newStatus 새로운 지원서 상태
     */
    public void updateStatus(Status newStatus) {
        this.status = newStatus;
    }

    public void updateStage(Stage newStage) {
        this.stage = newStage;
    }

    public void updateAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public void updatePreferInterviewInfo(Map<LocalDate, List<LocalTime>> preferInterviewInfo) {
        log.info("{}지원자 인터뷰 선호 시간 정보 업데이트", this.id);
        interviewPreferences.clear();
        for (Map.Entry<LocalDate, List<LocalTime>> entry : preferInterviewInfo.entrySet()){
            LocalDate date = entry.getKey();
            List<LocalTime> timeSlots = entry.getValue();
            interviewPreferences.add(new InterviewPreference(date, timeSlots));
        }
        log.info("{}지원자 인터뷰 선호 시간 정보 업데이트 완료", this.id);
    }

    public void updateInterviewInfo(LocalDateTime interviewSchedule) {
        log.info("지원자의 인터뷰 일정 업데이트 시작 applicationId={}, interviewSchedule={}", this.id, interviewSchedule);
        this.interviewDate = interviewSchedule.toLocalDate();
        this.interviewTime = interviewSchedule.toLocalTime();
        log.info("지원자의 인터뷰 일정 업데이트 완료 applicationId={}, interviewSchedule={}", this.id, interviewSchedule);
    }
}