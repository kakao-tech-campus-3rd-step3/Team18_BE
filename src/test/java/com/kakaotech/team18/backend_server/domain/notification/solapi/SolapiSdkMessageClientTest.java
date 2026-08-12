package com.kakaotech.team18.backend_server.domain.notification.solapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.solapi.sdk.message.dto.response.MultipleDetailMessageSentResponse;
import com.solapi.sdk.message.exception.SolapiInvalidApiKeyException;
import com.solapi.sdk.message.exception.SolapiMessageNotReceivedException;
import com.solapi.sdk.message.exception.SolapiUnknownException;
import com.solapi.sdk.message.model.FailedMessage;
import com.solapi.sdk.message.model.Message;
import com.solapi.sdk.message.model.group.GroupInfo;
import com.solapi.sdk.message.service.DefaultMessageService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SolapiSdkMessageClientTest {

    @Mock
    private DefaultMessageService messageService;

    private SolapiSdkMessageClient client;

    @BeforeEach
    void setUp() {
        client = new SolapiSdkMessageClient(messageService, "01012345678");
    }

    @Test
    void sendsSmsAndMapsAcceptanceIdentifiers() throws Exception {
        MultipleDetailMessageSentResponse sdkResponse = mockAcceptedResponse();
        when(messageService.send(any(Message.class))).thenReturn(sdkResponse);

        SolapiSendResponse response = client.send(new SolapiSmsRequest(
                "01098765432",
                "결과 안내 메시지",
                "delivery-100"
        ));

        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(messageService).send(messageCaptor.capture());
        Message sent = messageCaptor.getValue();
        assertThat(sent.getFrom()).isEqualTo("01012345678");
        assertThat(sent.getTo()).isEqualTo("01098765432");
        assertThat(sent.getText()).isEqualTo("결과 안내 메시지");
        assertThat(sent.getCustomFields())
                .containsEntry("notificationDeliveryId", "delivery-100");
        assertThat(response).isEqualTo(new SolapiSendResponse(
                "group-id",
                "message-id",
                "2000"
        ));
    }

    @Test
    void mapsRejectedMessageToPermanentFailure() throws Exception {
        MultipleDetailMessageSentResponse response = new MultipleDetailMessageSentResponse();
        FailedMessage failure = new FailedMessage();
        failure.setStatusCode("InvalidReceiver");
        failure.setStatusMessage("invalid receiver");
        response.setFailedMessageList(List.of(failure));
        when(messageService.send(any(Message.class))).thenReturn(response);

        assertThatThrownBy(() -> client.send(request()))
                .isInstanceOfSatisfying(SolapiClientException.class, exception -> {
                    assertThat(exception.getFailureType())
                            .isEqualTo(SolapiClientException.FailureType.PERMANENT);
                    assertThat(exception.getErrorCode()).isEqualTo("InvalidReceiver");
                });
    }

    @Test
    void mapsInvalidApiKeyWithoutExposingCredential() throws Exception {
        doAnswer(invocation -> {
            throw new SolapiInvalidApiKeyException("invalid key");
        }).when(messageService).send(any(Message.class));

        assertThatThrownBy(() -> client.send(request()))
                .isInstanceOfSatisfying(SolapiClientException.class, exception -> {
                    assertThat(exception.getFailureType())
                            .isEqualTo(SolapiClientException.FailureType.PERMANENT);
                    assertThat(exception.getErrorCode())
                            .isEqualTo("SOLAPI_AUTHENTICATION_FAILED");
                    assertThat(exception.getMessage()).doesNotContain("invalid key");
                });
    }

    @Test
    void mapsUnknownSdkResponseToUnknownFailure() throws Exception {
        doAnswer(invocation -> {
            throw new SolapiUnknownException("network response unknown");
        }).when(messageService).send(any(Message.class));

        assertThatThrownBy(() -> client.send(request()))
                .isInstanceOfSatisfying(SolapiClientException.class, exception -> {
                    assertThat(exception.getFailureType())
                            .isEqualTo(SolapiClientException.FailureType.UNKNOWN);
                    assertThat(exception.getErrorCode())
                            .isEqualTo("SOLAPI_AMBIGUOUS_RESPONSE");
                });
    }

    @Test
    void treatsMessageNotReceivedWithoutFailureDetailAsUnknown() throws Exception {
        doAnswer(invocation -> {
            throw new SolapiMessageNotReceivedException("response was not received");
        }).when(messageService).send(any(Message.class));

        assertThatThrownBy(() -> client.send(request()))
                .isInstanceOfSatisfying(SolapiClientException.class, exception -> {
                    assertThat(exception.getFailureType())
                            .isEqualTo(SolapiClientException.FailureType.UNKNOWN);
                    assertThat(exception.getErrorCode())
                            .isEqualTo("SOLAPI_MESSAGE_RESULT_UNKNOWN");
                });
    }

    @Test
    void treatsMissingMessageIdAsUnknownToAvoidDuplicateSend() throws Exception {
        MultipleDetailMessageSentResponse response = new MultipleDetailMessageSentResponse();
        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setGroupId("group-id");
        response.setGroupInfo(groupInfo);
        response.setMessageList(List.of(new MultipleDetailMessageSentResponse.MessageList()));
        when(messageService.send(any(Message.class))).thenReturn(response);

        assertThatThrownBy(() -> client.send(request()))
                .isInstanceOfSatisfying(SolapiClientException.class, exception -> {
                    assertThat(exception.getFailureType())
                            .isEqualTo(SolapiClientException.FailureType.UNKNOWN);
                    assertThat(exception.getErrorCode())
                            .isEqualTo("SOLAPI_INCOMPLETE_RESPONSE");
                });
    }

    private MultipleDetailMessageSentResponse mockAcceptedResponse() {
        MultipleDetailMessageSentResponse response = new MultipleDetailMessageSentResponse();
        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setGroupId("group-id");
        MultipleDetailMessageSentResponse.MessageList message =
                new MultipleDetailMessageSentResponse.MessageList();
        message.setMessageId("message-id");
        message.setStatusCode("2000");
        response.setGroupInfo(groupInfo);
        response.setMessageList(List.of(message));
        return response;
    }

    private SolapiSmsRequest request() {
        return new SolapiSmsRequest("01098765432", "결과 안내 메시지", "delivery-100");
    }
}
