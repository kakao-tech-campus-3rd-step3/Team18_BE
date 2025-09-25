package com.kakaotech.team18.backend_server.domain.clubApplyForm.entity;

import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ClubApplyFormTest {


    @DisplayName("update 메서드를 통해 title과 description을 수정할 수 있다.")
    @Test
    void update() {
        //given
        ClubApplyForm clubApplyForm = ClubApplyForm.builder()
                .club(Club.builder().name("테스트 동아리").build())
                .title("기존 제목")
                .description("기존 설명")
                .build();
        //when
        clubApplyForm.update("수정된 제목", "수정된 설명");

        //then
        Assertions.assertThat(clubApplyForm).extracting("title", "description").containsExactly("수정된 제목", "수정된 설명");
    }
}