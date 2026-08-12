package com.kakaotech.team18.backend_server.domain.notification.sender;

import com.kakaotech.team18.backend_server.domain.notification.dto.NotificationMessage;
import com.kakaotech.team18.backend_server.domain.notification.dto.NotificationSendResult;
import com.kakaotech.team18.backend_server.domain.notification.exception.NotificationSendException;
import com.kakaotech.team18.backend_server.domain.notification.quota.SolapiQuotaExceededException;
import com.kakaotech.team18.backend_server.domain.notification.quota.SolapiSendQuota;
import com.kakaotech.team18.backend_server.domain.notification.sms.InvalidSmsMessageException;
import com.kakaotech.team18.backend_server.domain.notification.sms.PreparedSmsMessage;
import com.kakaotech.team18.backend_server.domain.notification.sms.SmsMessagePolicy;
import com.kakaotech.team18.backend_server.domain.notification.solapi.SolapiClientException;
import com.kakaotech.team18.backend_server.domain.notification.solapi.SolapiMessageClient;
import com.kakaotech.team18.backend_server.domain.notification.solapi.SolapiSendResponse;
import com.kakaotech.team18.backend_server.domain.notification.solapi.SolapiSmsRequest;
import com.kakaotech.team18.backend_server.domain.notification.type.NotificationChannel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SmsNotificationSender implements NotificationSender {

    private final SolapiMessageClient solapiMessageClient;
    private final SmsMessagePolicy messagePolicy;
    private final SolapiSendQuota sendQuota;

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.SMS;
    }

    @Override
    public NotificationSendResult send(NotificationMessage message) {
        try {
            PreparedSmsMessage prepared = messagePolicy.prepare(
                    message.recipientAddress(),
                    message.body()
            );
            sendQuota.reserve();
            SolapiSendResponse response = solapiMessageClient.send(new SolapiSmsRequest(
                    prepared.recipient(),
                    prepared.text(),
                    prepared.subject(),
                    String.valueOf(message.deliveryId())
            ));
            return NotificationSendResult.accepted(
                    response.groupId(),
                    response.messageId(),
                    response.statusCode()
            );
        } catch (SolapiQuotaExceededException exception) {
            throw NotificationSendException.retryableAt(
                    "SOLAPI_HOURLY_QUOTA_EXCEEDED",
                    exception.getMessage(),
                    exception.getRetryAt(),
                    exception
            );
        } catch (InvalidSmsMessageException exception) {
            throw NotificationSendException.permanent(
                    exception.getErrorCode(),
                    exception.getMessage(),
                    exception
            );
        } catch (SolapiClientException exception) {
            if (exception.getFailureType() == SolapiClientException.FailureType.UNKNOWN) {
                throw NotificationSendException.unknown(
                        exception.getErrorCode(),
                        exception.getMessage(),
                        exception
                );
            }
            throw NotificationSendException.permanent(
                    exception.getErrorCode(),
                    exception.getMessage(),
                    exception
            );
        }
    }
}
