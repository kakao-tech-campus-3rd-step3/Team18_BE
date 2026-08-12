package com.kakaotech.team18.backend_server.domain.notification.sender;

import com.kakaotech.team18.backend_server.domain.notification.type.NotificationChannel;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class NotificationSenderRegistry {

    private final Map<NotificationChannel, NotificationSender> senders;

    public NotificationSenderRegistry(List<NotificationSender> senderList) {
        EnumMap<NotificationChannel, NotificationSender> registered =
                new EnumMap<>(NotificationChannel.class);
        for (NotificationSender sender : senderList) {
            NotificationSender duplicate = registered.put(sender.channel(), sender);
            if (duplicate != null) {
                throw new IllegalStateException("알림 채널 발송기가 중복 등록되었습니다: " + sender.channel());
            }
        }
        this.senders = Map.copyOf(registered);
    }

    public NotificationSender get(NotificationChannel channel) {
        NotificationSender sender = senders.get(channel);
        if (sender == null) {
            throw new IllegalStateException("등록된 알림 채널 발송기가 없습니다: " + channel);
        }
        return sender;
    }
}
