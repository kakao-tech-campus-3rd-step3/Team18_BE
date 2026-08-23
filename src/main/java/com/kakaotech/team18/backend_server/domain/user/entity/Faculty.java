package com.kakaotech.team18.backend_server.domain.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 지원자 학부(단과대학).
 * <p>
 * 통계 집계에 사용된다. 자유 입력 학과({@link User#getDepartment()})와 달리 학부는 목록이 한정적이라 enum으로 고정해
 * 표기 파편화 없이 정확히 집계한다. 목록에 없는 경우를 위해 {@link #ETC}(기타)를 둔다.
 * <p>
 * <strong>반드시 {@code @Enumerated(EnumType.STRING)}으로 저장한다.</strong> 순서값으로 저장하면 상수를 추가하거나
 * 순서를 바꾸는 순간 기존 데이터의 의미가 바뀐다.
 */
@Getter
@RequiredArgsConstructor
public enum Faculty {
    NURSING("간호대학"),
    BUSINESS("경영대학"),
    ENGINEERING("공과대학"),
    AGRICULTURE_AND_LIFE_SCIENCES("농업생명과학대학"),
    EDUCATION("사범대학"),
    SOCIAL_SCIENCES("사회과학대학"),
    HUMAN_ECOLOGY("생활과학대학"),
    VETERINARY_MEDICINE("수의과대학"),
    PHARMACY("약학대학"),
    ARTS("예술대학"),
    MEDICINE("의과대학"),
    HUMANITIES("인문대학"),
    NATURAL_SCIENCES("자연과학대학"),
    AI_CONVERGENCE("AI융합대학"),
    SELF_DESIGNED_MAJOR("자율전공학부"),
    ETC("기타");

    private final String label;
}
