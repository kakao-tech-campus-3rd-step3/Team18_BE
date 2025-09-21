package com.kakaotech.team18.backend_server.domain.application.entity;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.kakaotech.team18.backend_server.global.exception.exceptions.StatusNotFoundException;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class StatusTest {

    @DisplayName("fromText를 통해 Status 값을 정상적으로 찾는다.")
    @ParameterizedTest
    @MethodSource("provideTextAndStatus")
    void fromText(String text, Status expected) {
        assertThat(Status.fromText(text)).isEqualTo(expected);
    }
    private static Stream<Arguments> provideTextAndStatus() {
        return Stream.of(
                Arguments.of("미정", Status.PENDING),
                Arguments.of("합격", Status.APPROVED),
                Arguments.of("불합격", Status.REJECTED)
        );
    }

    @DisplayName("fromText의 파라미터가 매칭되지 않아 StatusNotFoundException이 일어난다.")
    @ParameterizedTest
    @MethodSource("provideTextAndStatusWithError")
    void fromTextWithError(String text) {
        assertThatThrownBy(() -> Status.fromText(text)).isInstanceOf(StatusNotFoundException.class);
    }
    private static Stream<Arguments> provideTextAndStatusWithError() {
        return Stream.of(
                Arguments.of("pending"),
                Arguments.of("approve"),
                Arguments.of("rejected")
        );
    }




}