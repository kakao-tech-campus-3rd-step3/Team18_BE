package com.kakaotech.team18.backend_server.domain.club.entity;

import com.kakaotech.team18.backend_server.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
public class ClubIntroduction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 500)
    private String imageUrl;

    @Column(columnDefinition = "TEXT")
    private String overview;

    @Column(columnDefinition = "TEXT")
    private String activities;

    @Column(columnDefinition = "TEXT")
    private String ideal;

    private ClubIntroduction(String imageUrl, String overview, String activities, String ideal) {
        this.imageUrl = imageUrl;
        this.overview = overview;
        this.activities = activities;
        this.ideal = ideal;
    }

    public static ClubIntroduction of(String imageUrl, String overview, String activities, String ideal) {
        return new ClubIntroduction(imageUrl, overview, activities, ideal);
    }


}
