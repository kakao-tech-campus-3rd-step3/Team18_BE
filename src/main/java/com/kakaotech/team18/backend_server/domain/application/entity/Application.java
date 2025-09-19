package com.kakaotech.team18.backend_server.domain.application.entity;

import com.kakaotech.team18.backend_server.domain.BaseEntity;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.entity.ClubApplyForm;
import com.kakaotech.team18.backend_server.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @Column(nullable = false)
    private Double averageRating = 0.0;

    @Builder
    private Application(User user, ClubApplyForm clubApplyForm, Status status) {
        this.user = user;
        this.clubApplyForm = clubApplyForm;
        this.status = status;
        this.averageRating = 0.0;
    }

    public Application(User user, ClubApplyForm form) {
        this.user = user;
        this.clubApplyForm = form;
        this.averageRating = 0.0;
    }


    /**
     * 지원서의 상태를 변경합니다.
     * 이 메소드는 서비스 계층에서 트랜잭션 내에서 호출되어야 합니다.
     * @param newStatus 새로운 지원서 상태
     */
    public void updateStatus(Status newStatus) {
        this.status = newStatus;
    }

    public void updateAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }
}
