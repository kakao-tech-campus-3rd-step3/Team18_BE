package com.kakaotech.team18.backend_server.club;

import com.kakaotech.team18.backend_server.user.Users;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Club {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "club_id")
    private long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "president_id")
    private Users users;

    @Column(name = "club_name")
    private String name;

    @Enumerated(EnumType.STRING)
    private Category category;

    private String location;

    private String shortIntroduction;

    private String introductionImage;

    private String introductionIntroduce;

    private String introductionActivity;

    private String introductionWannabe;

    private LocalDateTime recruitStart;

    private LocalDateTime recruitEnd;

}
