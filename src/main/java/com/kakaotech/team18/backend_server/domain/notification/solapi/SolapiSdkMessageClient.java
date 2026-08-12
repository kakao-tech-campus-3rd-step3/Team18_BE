package com.kakaotech.team18.backend_server.domain.notification.solapi;

import com.solapi.sdk.message.dto.response.MultipleDetailMessageSentResponse;
import com.solapi.sdk.message.dto.request.MessageListRequest;
import com.solapi.sdk.message.dto.response.MessageListResponse;
import com.solapi.sdk.message.exception.SolapiApiKeyException;
import com.solapi.sdk.message.exception.SolapiBadRequestException;
import com.solapi.sdk.message.exception.SolapiEmptyResponseException;
import com.solapi.sdk.message.exception.SolapiInvalidApiKeyException;
import com.solapi.sdk.message.exception.SolapiMessageNotReceivedException;
import com.solapi.sdk.message.exception.SolapiUnknownException;
import com.solapi.sdk.message.model.FailedMessage;
import com.solapi.sdk.message.model.Message;
import com.solapi.sdk.message.service.DefaultMessageService;
import java.util.List;
import java.util.Map;

public class SolapiSdkMessageClient implements SolapiMessageClient {

    private final DefaultMessageService messageService;
    private final String senderNumber;

    public SolapiSdkMessageClient(DefaultMessageService messageService, String senderNumber) {
        this.messageService = messageService;
        this.senderNumber = senderNumber;
    }

    @Override
    public SolapiSendResponse send(SolapiSmsRequest request) {
        Message message = new Message();
        message.setFrom(senderNumber);
        message.setTo(request.recipient());
        message.setText(request.text());
        if (request.subject() != null && !request.subject().isBlank()) {
            message.setSubject(request.subject());
        }
        if (request.clientReference() != null && !request.clientReference().isBlank()) {
            message.setCustomFields(Map.of("notificationDeliveryId", request.clientReference()));
        }

        try {
            MultipleDetailMessageSentResponse response = messageService.send(message);
            return toResponse(response);
        } catch (Exception exception) {
            throw mapException(exception);
        }
    }

    @Override
    public SolapiStatusResponse getStatus(String messageId) {
        MessageListRequest request = new MessageListRequest();
        request.setMessageId(messageId);
        request.setLimit(1);
        try {
            MessageListResponse response = messageService.getMessageList(request);
            if (response == null || response.getMessageList() == null
                    || response.getMessageList().isEmpty()) {
                return new SolapiStatusResponse(SolapiMessageStatus.NOT_FOUND, null);
            }
            Message message = response.getMessageList().values().stream()
                    .filter(candidate -> messageId.equals(candidate.getMessageId()))
                    .findFirst()
                    .orElseGet(() -> response.getMessageList().values().iterator().next());
            String status = message.getStatus() == null
                    ? ""
                    : message.getStatus().trim().toUpperCase();
            SolapiMessageStatus mapped = switch (status) {
                case "COMPLETE" -> SolapiMessageStatus.SENT;
                case "FAILED" -> SolapiMessageStatus.FAILED;
                default -> SolapiMessageStatus.PENDING;
            };
            return new SolapiStatusResponse(mapped, message.getStatusCode());
        } catch (Exception exception) {
            throw mapException(exception);
        }
    }

    private SolapiSendResponse toResponse(MultipleDetailMessageSentResponse response) {
        if (response == null) {
            throw SolapiClientException.unknown(
                    "SOLAPI_EMPTY_RESPONSE",
                    "SOLAPI에서 빈 발송 응답을 반환했습니다.",
                    null
            );
        }

        List<FailedMessage> failedMessages = response.getFailedMessageList();
        if (failedMessages != null && !failedMessages.isEmpty()) {
            FailedMessage failure = failedMessages.getFirst();
            throw SolapiClientException.permanent(
                    safeCode(failure.getStatusCode(), "SOLAPI_MESSAGE_REJECTED"),
                    safeMessage(failure.getStatusMessage(), "SOLAPI가 메시지 발송 요청을 거부했습니다."),
                    null
            );
        }

        String groupId = response.getGroupInfo() == null
                ? null
                : response.getGroupInfo().getGroupId();
        List<MultipleDetailMessageSentResponse.MessageList> messages = response.getMessageList();
        MultipleDetailMessageSentResponse.MessageList acceptedMessage =
                messages == null || messages.isEmpty() ? null : messages.getFirst();
        if (groupId == null || groupId.isBlank()
                || acceptedMessage == null
                || acceptedMessage.getMessageId() == null
                || acceptedMessage.getMessageId().isBlank()) {
            throw SolapiClientException.unknown(
                    "SOLAPI_INCOMPLETE_RESPONSE",
                    "SOLAPI 발송 응답에 그룹 ID 또는 메시지 ID가 없습니다.",
                    null
            );
        }

        return new SolapiSendResponse(
                groupId,
                acceptedMessage.getMessageId(),
                safeCode(acceptedMessage.getStatusCode(), "SOLAPI_ACCEPTED")
        );
    }

    private SolapiClientException mapException(Exception exception) {
        if (exception instanceof SolapiClientException clientException) {
            return clientException;
        }
        if (exception instanceof SolapiInvalidApiKeyException
                || exception instanceof SolapiApiKeyException) {
            return SolapiClientException.permanent(
                    "SOLAPI_AUTHENTICATION_FAILED",
                    "SOLAPI API 인증에 실패했습니다.",
                    exception
            );
        }
        if (exception instanceof SolapiBadRequestException) {
            return SolapiClientException.permanent(
                    "SOLAPI_BAD_REQUEST",
                    safeMessage(exception.getMessage(), "SOLAPI 발송 요청 값이 올바르지 않습니다."),
                    exception
            );
        }
        if (exception instanceof SolapiMessageNotReceivedException notReceived) {
            FailedMessage failure = firstFailure(notReceived.getFailedMessageList());
            if (failure == null) {
                return SolapiClientException.unknown(
                        "SOLAPI_MESSAGE_RESULT_UNKNOWN",
                        safeMessage(exception.getMessage(), "SOLAPI 발송 결과를 확인할 수 없습니다."),
                        exception
                );
            }
            return SolapiClientException.permanent(
                    safeCode(failure.getStatusCode(), "SOLAPI_MESSAGE_REJECTED"),
                    safeMessage(failure.getStatusMessage(), "SOLAPI가 메시지 발송 요청을 거부했습니다."),
                    exception
            );
        }
        if (exception instanceof SolapiEmptyResponseException
                || exception instanceof SolapiUnknownException) {
            return SolapiClientException.unknown(
                    "SOLAPI_AMBIGUOUS_RESPONSE",
                    safeMessage(exception.getMessage(), "SOLAPI 발송 결과를 확인할 수 없습니다."),
                    exception
            );
        }
        return SolapiClientException.unknown(
                "SOLAPI_UNEXPECTED_ERROR",
                safeMessage(exception.getMessage(), "SOLAPI 호출 중 알 수 없는 오류가 발생했습니다."),
                exception
        );
    }

    private FailedMessage firstFailure(List<FailedMessage> failures) {
        return failures == null || failures.isEmpty() ? null : failures.getFirst();
    }

    private String safeCode(String code, String fallback) {
        return code == null || code.isBlank() ? fallback : code;
    }

    private String safeMessage(String message, String fallback) {
        return message == null || message.isBlank() ? fallback : message;
    }
}
