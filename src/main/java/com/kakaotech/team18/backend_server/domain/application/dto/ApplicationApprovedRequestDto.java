package com.kakaotech.team18.backend_server.domain.application.dto;

import com.kakaotech.team18.backend_server.domain.notification.type.NotificationChannel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.Set;

import static com.kakaotech.team18.backend_server.domain.notification.type.NotificationChannel.EMAIL;

@Schema(description = "지원 결과 처리 및 알림 전송 요청 데이터")
public record ApplicationApprovedRequestDto(
        @Schema(description = "합격자에게 추가로 전달할 안내 메시지", example = "자세한 일정은 추후 안내드리겠습니다.")
        @Size(max = 800, message = "추가 안내 메시지는 800자를 초과할 수 없습니다.")
        String message,

        @Schema(
                description = "알림 전송 채널. 여러 채널을 선택할 수 있으며, 생략하거나 null이면 EMAIL로 처리됩니다.",
                example = "[\"EMAIL\", \"SMS\"]",
                defaultValue = "[\"EMAIL\"]"
        )
        @NotEmpty(message = "알림 채널은 하나 이상 선택해야 합니다.")
        Set<NotificationChannel> channels
) {

    public ApplicationApprovedRequestDto {
        channels = channels == null ? Set.of(EMAIL) : Set.copyOf(channels);
    }

    public ApplicationApprovedRequestDto(String message) {
        this(message, Set.of(EMAIL));
    }
}
