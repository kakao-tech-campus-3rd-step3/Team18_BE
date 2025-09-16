package com.kakaotech.team18.backend_server.domain.clubMember.entity;

import com.kakaotech.team18.backend_server.domain.BaseEntity;
import com.kakaotech.team18.backend_server.domain.application.entity.Application;
import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import com.kakaotech.team18.backend_server.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClubMember extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "club_member_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id",  nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY,  optional = false)
    @JoinColumn(name = "club_id",  nullable = false)
    private Club club;

    @Enumerated(EnumType.STRING)
    @Column(name = "active_status", nullable = false)
    private ActiveStatus activeStatus;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id")
    private Application application;

    @Enumerated(EnumType.STRING)
    @Column(name = "club_role", nullable = false, length = 20) // 길이는 적당히
    private Role role;
}
