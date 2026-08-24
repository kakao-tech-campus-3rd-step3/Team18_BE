package com.kakaotech.team18.backend_server.domain.notification.event;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.kakaotech.team18.backend_server.domain.notification.service.NotificationDeliveryProcessor;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResultNotificationDispatchListenerTest {

    @Mock
    private NotificationDeliveryProcessor processor;

    @Test
    void isolatesEachDeliveryFailure() {
        ResultNotificationDispatchListener listener = new ResultNotificationDispatchListener(processor);
        doThrow(new RuntimeException("first failed")).when(processor).process(1L);

        listener.onDispatchRequested(new ResultNotificationDispatchRequestedEvent(List.of(1L, 2L)));

        verify(processor).process(1L);
        verify(processor).process(2L);
    }
}
