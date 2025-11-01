package com.kakaotech.team18.backend_server.domain.club.entity;

import com.kakaotech.team18.backend_server.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class ClubIntroduction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "clubIntroduction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClubImage> images = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String overview;

    @Column(columnDefinition = "TEXT")
    private String activities;

    @Column(columnDefinition = "TEXT")
    private String ideal;

    @Builder
    private ClubIntroduction(String overview, String activities, String ideal, List<ClubImage> images) {
        this.overview = overview;
        this.activities = activities;
        this.ideal = ideal;
    }

    public void addImage(ClubImage image) {
        images.add(image);
        image.setClubIntroductionInternal(this); // 내부 전용 세터
    }

    public void removeImage(ClubImage image) {
        if (images.remove(image)) {
            image.setClubIntroductionInternal(null);
        }
    }

    public void updateImages(List<String> imageUrl) {
        this.images.clear();
        for (String url : imageUrl) {
            ClubImage image = ClubImage.builder()
                    .imageUrl(url)
                    .clubIntroduction(this)
                    .build();
            this.addImage(image);
        }
    }
}
