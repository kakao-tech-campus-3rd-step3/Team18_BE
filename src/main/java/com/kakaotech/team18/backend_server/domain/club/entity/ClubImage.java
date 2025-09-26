package com.kakaotech.team18.backend_server.domain.club.entity;

import com.kakaotech.team18.backend_server.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClubImage extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 500, nullable = false)
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_introduction_id", nullable = false)
    private ClubIntroduction clubIntroduction;

    @Builder
    private ClubImage(String imageUrl, ClubIntroduction clubIntroduction) {
        this.imageUrl = imageUrl;
        this.clubIntroduction = clubIntroduction;
    }

    // 연관관계 편의 메서드
    void setClubIntroductionInternal(ClubIntroduction clubIntroduction) {
        this.clubIntroduction = clubIntroduction;
    }
}


