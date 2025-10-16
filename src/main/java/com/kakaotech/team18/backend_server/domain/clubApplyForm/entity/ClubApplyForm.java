package com.kakaotech.team18.backend_server.domain.clubApplyForm.entity;

import com.kakaotech.team18.backend_server.domain.BaseEntity;
import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClubApplyForm extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "club_apply_form_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "club_id", nullable = false)
    private Club club;

    @Column(name = "title", nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    private boolean isActive;

    private String interviewMessage;
    private String finalMessage;

    @Builder
    private ClubApplyForm(Club club, String title, String description, boolean isActive) {
        this.club = club;
        this.title = title;
        this.description = description;
        this.isActive = isActive;
    }

    public void update(String title, String description) {
        this.title = title;
        this.description = description;
    }
    public void updateInterviewMessage(String message){
        this.interviewMessage = message;
    }
    public void updateFinalMessage(String message){
        this.finalMessage = message;
    }
}
