package com.kakaotech.team18.backend_server.domain.clubPopularity.redis;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.io.InputStream;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ClubPopularityRedisScriptsTest {

    private static final List<String> SCRIPT_NAMES = List.of(
            "club-popularity-record-views.lua",
            "club-popularity-record-heartbeat.lua",
            "club-popularity-aggregate.lua",
            "club-popularity-remove-pending.lua");

    @Test
    @DisplayName("기록·집계·조건부 삭제 Lua 스크립트를 리소스로 제공한다")
    void scriptsArePackaged() {
        for (String scriptName : SCRIPT_NAMES) {
            try (InputStream stream = getClass().getResourceAsStream("/lua/" + scriptName)) {
                assertThat(stream).as(scriptName).isNotNull();
                String source = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
                assertThat(source).isNotBlank();
                assertThat(source).contains("KEYS");
            } catch (Exception exception) {
                throw new AssertionError("Unable to read " + scriptName, exception);
            }
        }
    }
}
