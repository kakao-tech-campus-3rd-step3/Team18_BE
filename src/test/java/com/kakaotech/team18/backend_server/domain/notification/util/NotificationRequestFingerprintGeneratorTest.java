package com.kakaotech.team18.backend_server.domain.notification.util;

import com.kakaotech.team18.backend_server.domain.application.dto.ApplicationApprovedRequestDto;
import com.kakaotech.team18.backend_server.domain.application.entity.Stage;
import com.kakaotech.team18.backend_server.domain.notification.type.NotificationChannel;
import java.util.LinkedHashSet;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationRequestFingerprintGeneratorTest {

    @Test
    @DisplayName("채널 순서가 달라도 동일한 요청 지문을 생성한다")
    void sameFingerprintRegardlessOfChannelOrder() {
        ApplicationApprovedRequestDto first = new ApplicationApprovedRequestDto(
                "결과 안내",
                new LinkedHashSet<>(Set.of(NotificationChannel.EMAIL, NotificationChannel.SMS))
        );
        LinkedHashSet<NotificationChannel> reversedChannels = new LinkedHashSet<>();
        reversedChannels.add(NotificationChannel.SMS);
        reversedChannels.add(NotificationChannel.EMAIL);
        ApplicationApprovedRequestDto second = new ApplicationApprovedRequestDto("결과 안내", reversedChannels);

        String firstFingerprint = NotificationRequestFingerprintGenerator.generate(1L, Stage.INTERVIEW, first);
        String secondFingerprint = NotificationRequestFingerprintGenerator.generate(1L, Stage.INTERVIEW, second);

        assertThat(firstFingerprint).isEqualTo(secondFingerprint).hasSize(64);
    }

    @Test
    @DisplayName("메시지, 채널 또는 단계가 다르면 요청 지문도 달라진다")
    void differentFingerprintWhenRequestChanges() {
        ApplicationApprovedRequestDto original = new ApplicationApprovedRequestDto(
                "결과 안내",
                Set.of(NotificationChannel.EMAIL)
        );
        String originalFingerprint = NotificationRequestFingerprintGenerator.generate(
                1L,
                Stage.INTERVIEW,
                original
        );

        assertThat(NotificationRequestFingerprintGenerator.generate(
                1L,
                Stage.INTERVIEW,
                new ApplicationApprovedRequestDto("변경된 안내", Set.of(NotificationChannel.EMAIL))
        )).isNotEqualTo(originalFingerprint);
        assertThat(NotificationRequestFingerprintGenerator.generate(
                1L,
                Stage.INTERVIEW,
                new ApplicationApprovedRequestDto("결과 안내", Set.of(NotificationChannel.SMS))
        )).isNotEqualTo(originalFingerprint);
        assertThat(NotificationRequestFingerprintGenerator.generate(
                1L,
                Stage.FINAL,
                original
        )).isNotEqualTo(originalFingerprint);
    }
}
