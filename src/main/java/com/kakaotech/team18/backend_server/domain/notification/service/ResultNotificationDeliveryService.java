package com.kakaotech.team18.backend_server.domain.notification.service;

import com.kakaotech.team18.backend_server.domain.application.entity.Application;
import com.kakaotech.team18.backend_server.domain.application.entity.Stage;
import com.kakaotech.team18.backend_server.domain.application.entity.Status;
import com.kakaotech.team18.backend_server.domain.notification.entity.NotificationDelivery;
import com.kakaotech.team18.backend_server.domain.notification.repository.NotificationDeliveryRepository;
import com.kakaotech.team18.backend_server.domain.notification.sms.SmsMessagePolicy;
import com.kakaotech.team18.backend_server.domain.notification.type.NotificationChannel;
import com.kakaotech.team18.backend_server.domain.notification.type.NotificationResultType;
import com.kakaotech.team18.backend_server.global.exception.exceptions.NotificationSmsLimitExceededException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ResultNotificationDeliveryService {

    private static final ZoneId SERVICE_ZONE = ZoneId.of("Asia/Seoul");
    private final NotificationDeliveryRepository notificationDeliveryRepository;
    private final ResultNotificationTemplate notificationTemplate;
    private final SmsMessagePolicy smsMessagePolicy;
    private final int maxSmsPerRequest;
    private final Clock clock;

    @Autowired
    public ResultNotificationDeliveryService(
            NotificationDeliveryRepository notificationDeliveryRepository,
            ResultNotificationTemplate notificationTemplate,
            SmsMessagePolicy smsMessagePolicy,
            @Value("${notification.result.max-sms-per-request:50}") int maxSmsPerRequest
    ) {
        this(
                notificationDeliveryRepository,
                notificationTemplate,
                smsMessagePolicy,
                maxSmsPerRequest,
                Clock.system(SERVICE_ZONE)
        );
    }

    ResultNotificationDeliveryService(
            NotificationDeliveryRepository notificationDeliveryRepository,
            int maxSmsPerRequest,
            Clock clock
    ) {
        this(
                notificationDeliveryRepository,
                new ResultNotificationTemplate(),
                new SmsMessagePolicy(),
                maxSmsPerRequest,
                clock
        );
    }

    ResultNotificationDeliveryService(
            NotificationDeliveryRepository notificationDeliveryRepository,
            ResultNotificationTemplate notificationTemplate,
            SmsMessagePolicy smsMessagePolicy,
            int maxSmsPerRequest,
            Clock clock
    ) {
        this.notificationDeliveryRepository = notificationDeliveryRepository;
        this.notificationTemplate = notificationTemplate;
        this.smsMessagePolicy = smsMessagePolicy;
        this.maxSmsPerRequest = maxSmsPerRequest;
        this.clock = clock;
    }

    public List<NotificationDelivery> createPendingDeliveries(
            Long clubId,
            String idempotencyKey,
            Stage stage,
            String customMessage,
            String replyToAddress,
            Set<NotificationChannel> channels,
            List<Application> applications
    ) {
        validateSmsLimit(channels, applications.size());

        LocalDateTime requestedAt = LocalDateTime.now(clock);
        List<NotificationDelivery> deliveries = new ArrayList<>(applications.size() * channels.size());

        for (Application application : applications) {
            NotificationResultType resultType = resolveResultType(stage, application.getStatus());
            String clubName = application.getClubApplyForm().getClub().getName();
            String applicantName = application.getUser().getName();
            String messageBody = notificationTemplate.render(
                    resultType,
                    clubName,
                    applicantName,
                    customMessage,
                    application
            );

            for (NotificationChannel channel : channels) {
                String recipient = resolveRecipient(channel, application);
                if (channel == NotificationChannel.SMS) {
                    smsMessagePolicy.prepare(recipient, messageBody);
                }
                deliveries.add(NotificationDelivery.pending(
                        clubId,
                        application.getUser().getId(),
                        application.getId(),
                        idempotencyKey,
                        channel,
                        resultType,
                        recipient,
                        channel == NotificationChannel.EMAIL ? replyToAddress : null,
                        channel == NotificationChannel.EMAIL
                                ? "[동아리 지원] " + clubName + " - " + applicantName
                                : null,
                        messageBody,
                        requestedAt
                ));
            }
        }

        return notificationDeliveryRepository.saveAll(deliveries);
    }

    private void validateSmsLimit(Set<NotificationChannel> channels, int recipientCount) {
        if (channels.contains(NotificationChannel.SMS) && recipientCount > maxSmsPerRequest) {
            throw new NotificationSmsLimitExceededException(recipientCount, maxSmsPerRequest);
        }
    }

    private NotificationResultType resolveResultType(Stage stage, Status status) {
        return switch (stage) {
            case INTERVIEW -> switch (status) {
                case APPROVED -> NotificationResultType.INTERVIEW_APPROVED;
                case REJECTED -> NotificationResultType.INTERVIEW_REJECTED;
                default -> throw new IllegalStateException("처리할 수 없는 지원서 상태입니다: " + status);
            };
            case FINAL -> switch (status) {
                case APPROVED -> NotificationResultType.FINAL_APPROVED;
                case REJECTED -> NotificationResultType.FINAL_REJECTED;
                default -> throw new IllegalStateException("처리할 수 없는 지원서 상태입니다: " + status);
            };
            case RESULT -> throw new IllegalArgumentException("RESULT 단계는 결과 알림을 생성할 수 없습니다.");
        };
    }

    private String resolveRecipient(NotificationChannel channel, Application application) {
        return switch (channel) {
            case EMAIL -> application.getUser().getEmail();
            case SMS -> application.getUser().getPhoneNumber();
        };
    }

}
