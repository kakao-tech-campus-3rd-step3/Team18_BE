package com.kakaotech.team18.backend_server.domain.notices.entity;

import com.kakaotech.team18.backend_server.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_id")
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "isAlive", nullable = false )
    private boolean isAlive;

    @Builder
    private Notice(Long  id, String title, String content, boolean isAlive) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.isAlive = isAlive;
    }
}
