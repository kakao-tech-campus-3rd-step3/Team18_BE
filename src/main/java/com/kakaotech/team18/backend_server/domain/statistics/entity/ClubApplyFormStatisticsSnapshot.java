package com.kakaotech.team18.backend_server.domain.statistics.entity;

import com.kakaotech.team18.backend_server.domain.BaseEntity;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.entity.ClubApplyForm;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 지원폼의 확정 통계 스냅샷.
 * <p>
 * <strong>불합격 지원서가 삭제되기 전에 저장한다.</strong> 지원폼과 지원자를 잇는 유일한 연결이
 * {@code Application}이라, 불합격 지원서가 지워지면 그 지원자의 성별·학과는 {@code User}에 남아 있어도
 * 어느 지원폼에 지원했는지 알 수 없게 된다. 삭제 후에 집계하면 합격자만 남아 성비와 학과 분포가 완전히
 * 왜곡된다.
 * <p>
 * <strong>집계 수치만 저장하고 개별 지원자 정보는 저장하지 않는다.</strong> 저장 시점에 이미 마스킹까지 끝난
 * 공개용 응답을 그대로 담는다.
 */
@Getter
@Entity
@Table(name = "club_apply_form_statistics_snapshot")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClubApplyFormStatisticsSnapshot extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "club_apply_form_statistics_snapshot_id")
    private Long id;

    /**
     * 지원폼당 스냅샷은 하나다. 첫 단계 전환 시점의 전체 지원자 분포가 확정본이며, 이후 단계 전환에서
     * 덮어쓰지 않는다.
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "club_apply_form_id", nullable = false, unique = true)
    private ClubApplyForm clubApplyForm;

    @Column(name = "total_applicants", nullable = false)
    private long totalApplicants;

    /** 집계 규칙 버전. 규칙이 바뀌었을 때 재계산 대상을 골라내기 위해 저장한다. */
    @Column(name = "rules_version", nullable = false)
    private String rulesVersion;

    /** 공개용 응답(JSON) 그대로. 개별 지원자 정보는 포함되지 않는다. */
    @Lob
    @Column(name = "payload", nullable = false)
    private String payload;

    @Builder
    private ClubApplyFormStatisticsSnapshot(
            ClubApplyForm clubApplyForm,
            long totalApplicants,
            String rulesVersion,
            String payload
    ) {
        this.clubApplyForm = clubApplyForm;
        this.totalApplicants = totalApplicants;
        this.rulesVersion = rulesVersion;
        this.payload = payload;
    }
}
