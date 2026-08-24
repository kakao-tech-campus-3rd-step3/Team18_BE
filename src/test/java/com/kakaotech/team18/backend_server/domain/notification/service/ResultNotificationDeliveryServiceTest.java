package com.kakaotech.team18.backend_server.domain.notification.service;

import com.kakaotech.team18.backend_server.domain.application.entity.Application;
import com.kakaotech.team18.backend_server.domain.application.entity.Stage;
import com.kakaotech.team18.backend_server.domain.application.entity.Status;
import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.entity.ClubApplyForm;
import com.kakaotech.team18.backend_server.domain.notification.entity.NotificationDelivery;
import com.kakaotech.team18.backend_server.domain.notification.repository.NotificationDeliveryRepository;
import com.kakaotech.team18.backend_server.domain.notification.type.NotificationChannel;
import com.kakaotech.team18.backend_server.domain.notification.type.NotificationDeliveryStatus;
import com.kakaotech.team18.backend_server.domain.notification.type.NotificationResultType;
import com.kakaotech.team18.backend_server.domain.user.entity.User;
import com.kakaotech.team18.backend_server.global.exception.exceptions.NotificationSmsLimitExceededException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResultNotificationDeliveryServiceTest {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final Instant NOW = Instant.parse("2026-08-06T01:00:00Z");

    @Mock
    private NotificationDeliveryRepository notificationDeliveryRepository;

    private ResultNotificationDeliveryService service;

    @BeforeEach
    void setUp() {
        service = new ResultNotificationDeliveryService(
                notificationDeliveryRepository,
                50,
                Clock.fixed(NOW, SEOUL)
        );
        lenient().when(notificationDeliveryRepository.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @DisplayName("지원자와 선택 채널의 조합마다 PENDING 발송 작업을 생성한다")
    void createDeliveryForEveryApplicantAndChannel() {
        Application approved = application(
                101L,
                201L,
                "김합격",
                "approved@example.com",
                "010-1111-2222",
                Status.APPROVED
        );
        when(approved.getInterviewDate()).thenReturn(LocalDate.of(2026, 8, 10));
        when(approved.getInterviewTime()).thenReturn(LocalTime.of(14, 30));
        Application rejected = application(
                102L,
                202L,
                "이불합격",
                "rejected@example.com",
                "010-3333-4444",
                Status.REJECTED
        );

        service.createPendingDeliveries(
                1L,
                "announcement-key",
                Stage.INTERVIEW,
                "면접 장소는 학생회관입니다.",
                "president@example.com",
                Set.of(NotificationChannel.EMAIL, NotificationChannel.SMS),
                List.of(approved, rejected)
        );

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<NotificationDelivery>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationDeliveryRepository).saveAll(captor.capture());
        List<NotificationDelivery> deliveries = captor.getValue();

        assertThat(deliveries).hasSize(4);
        assertThat(deliveries).allSatisfy(delivery -> {
            assertThat(delivery.getStatus()).isEqualTo(NotificationDeliveryStatus.PENDING);
            assertThat(delivery.getIdempotencyKey()).isEqualTo("announcement-key");
            assertThat(delivery.getNextAttemptAt())
                    .isEqualTo(LocalDateTime.ofInstant(NOW, SEOUL));
        });
        assertThat(deliveries)
                .filteredOn(delivery -> delivery.getUserId().equals(201L))
                .extracting(NotificationDelivery::getResultType)
                .containsOnly(NotificationResultType.INTERVIEW_APPROVED);
        assertThat(deliveries)
                .filteredOn(delivery -> delivery.getUserId().equals(202L))
                .extracting(NotificationDelivery::getResultType)
                .containsOnly(NotificationResultType.INTERVIEW_REJECTED);
        assertThat(deliveries)
                .filteredOn(delivery -> delivery.getChannel() == NotificationChannel.EMAIL)
                .extracting(NotificationDelivery::getRecipientAddress)
                .containsExactlyInAnyOrder("approved@example.com", "rejected@example.com");
        assertThat(deliveries)
                .filteredOn(delivery -> delivery.getChannel() == NotificationChannel.EMAIL)
                .extracting(NotificationDelivery::getReplyToAddress)
                .containsOnly("president@example.com");
        assertThat(deliveries)
                .filteredOn(delivery -> delivery.getChannel() == NotificationChannel.SMS)
                .extracting(NotificationDelivery::getRecipientAddress)
                .containsExactlyInAnyOrder("010-1111-2222", "010-3333-4444");
        assertThat(deliveries)
                .filteredOn(delivery -> delivery.getResultType() == NotificationResultType.INTERVIEW_APPROVED)
                .allSatisfy(delivery -> assertThat(delivery.getMessageBody())
                        .contains("2026-08-10 14:30", "면접 장소는 학생회관입니다."));
    }

    @Test
    @DisplayName("문자 수신자가 발표 1회 한도를 초과하면 저장하지 않는다")
    void rejectSmsOverPerRequestLimit() {
        service = new ResultNotificationDeliveryService(
                notificationDeliveryRepository,
                2,
                Clock.fixed(NOW, SEOUL)
        );
        List<Application> applications = List.of(mock(Application.class), mock(Application.class), mock(Application.class));

        assertThatThrownBy(() -> service.createPendingDeliveries(
                1L,
                "over-limit-key",
                Stage.FINAL,
                "결과 안내",
                "president@example.com",
                Set.of(NotificationChannel.SMS),
                applications
        )).isInstanceOf(NotificationSmsLimitExceededException.class);

        verify(notificationDeliveryRepository, never()).saveAll(anyList());
    }

    private Application application(
            Long applicationId,
            Long userId,
            String name,
            String email,
            String phoneNumber,
            Status status
    ) {
        Club club = mock(Club.class);
        when(club.getName()).thenReturn("테스트 동아리");
        ClubApplyForm form = mock(ClubApplyForm.class);
        when(form.getClub()).thenReturn(club);
        User user = mock(User.class);
        when(user.getId()).thenReturn(userId);
        when(user.getName()).thenReturn(name);
        when(user.getEmail()).thenReturn(email);
        when(user.getPhoneNumber()).thenReturn(phoneNumber);
        Application application = mock(Application.class);
        when(application.getId()).thenReturn(applicationId);
        when(application.getStatus()).thenReturn(status);
        when(application.getClubApplyForm()).thenReturn(form);
        when(application.getUser()).thenReturn(user);
        return application;
    }
}
