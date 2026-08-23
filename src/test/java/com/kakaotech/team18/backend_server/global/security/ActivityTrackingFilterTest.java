package com.kakaotech.team18.backend_server.global.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.kakaotech.team18.backend_server.domain.activity.service.ActivityTrackingService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@ExtendWith(MockitoExtension.class)
class ActivityTrackingFilterTest {

    @Mock
    private ActivityTrackingService activityTrackingService;

    private ActivityTrackingFilter filter;

    @BeforeEach
    void setUp() {
        filter = new ActivityTrackingFilter(activityTrackingService);
    }

    @Test
    @DisplayName("기존 API는 익명 쿠키를 발급하고 ActivityDaily 활동을 기록한다")
    void existingApiIssuesCookieAndTracksActivity() throws Exception {
        MockHttpServletRequest request = request("GET", "/api/clubs");
        request.setSecure(true);
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> requestIdentity = new AtomicReference<>();

        filter.doFilter(request, response, captureIdentity(requestIdentity));

        Cookie cookie = response.getCookie(ActivityTrackingFilter.ANONYMOUS_COOKIE_NAME);
        assertThat(cookie).isNotNull();
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.getSecure()).isTrue();
        assertThat(cookie.getPath()).isEqualTo("/");
        assertThat(cookie.getMaxAge()).isEqualTo(365 * 24 * 60 * 60);
        assertThat(requestIdentity.get()).isEqualTo(cookie.getValue());
        verify(activityTrackingService).track(cookie.getValue(), null);
    }

    @Test
    @DisplayName("기존 익명 쿠키는 요청 속성으로 그대로 전달한다")
    void reusesExistingCookieAsRequestAttribute() throws Exception {
        MockHttpServletRequest request = request("GET", "/api/notices");
        request.setCookies(new Cookie(ActivityTrackingFilter.ANONYMOUS_COOKIE_NAME, "existing-anonymous-id"));
        AtomicReference<String> requestIdentity = new AtomicReference<>();

        filter.doFilter(request, new MockHttpServletResponse(), captureIdentity(requestIdentity));

        assertThat(requestIdentity.get()).isEqualTo("existing-anonymous-id");
        verify(activityTrackingService).track("existing-anonymous-id", null);
    }

    @Test
    @DisplayName("views API는 익명 요청 속성을 제공하지만 ActivityDaily를 기록하지 않는다")
    void viewsProvidesIdentityWithoutActivityTracking() throws Exception {
        MockHttpServletRequest request = request("POST", "/api/clubs/1/views");
        request.setCookies(new Cookie(ActivityTrackingFilter.ANONYMOUS_COOKIE_NAME, "viewer-1"));
        AtomicReference<String> requestIdentity = new AtomicReference<>();

        filter.doFilter(request, new MockHttpServletResponse(), captureIdentity(requestIdentity));

        assertThat(requestIdentity.get()).isEqualTo("viewer-1");
        verify(activityTrackingService, never()).track("viewer-1", null);
    }

    @Test
    @DisplayName("heartbeat API는 익명 요청 속성을 제공하지만 ActivityDaily를 기록하지 않는다")
    void heartbeatProvidesIdentityWithoutActivityTracking() throws Exception {
        MockHttpServletRequest request = request("POST", "/api/clubs/1/heartbeat");
        AtomicReference<String> requestIdentity = new AtomicReference<>();

        filter.doFilter(request, new MockHttpServletResponse(), captureIdentity(requestIdentity));

        assertThat(requestIdentity.get()).isNotBlank();
        verify(activityTrackingService, never()).track(requestIdentity.get(), null);
    }

    @Test
    @DisplayName("popular API는 쿠키와 요청 속성 및 ActivityDaily 기록을 모두 생성하지 않는다")
    void popularDoesNotIdentifyOrTrackViewer() throws Exception {
        MockHttpServletRequest request = request("GET", "/api/clubs/popular");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> requestIdentity = new AtomicReference<>();

        filter.doFilter(request, response, captureIdentity(requestIdentity));

        assertThat(response.getCookies()).isEmpty();
        assertThat(requestIdentity.get()).isNull();
        verify(activityTrackingService, never()).track(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("OPTIONS 요청은 쿠키와 활동 기록을 생성하지 않는다")
    void optionsDoesNotIdentifyOrTrackViewer() throws Exception {
        MockHttpServletRequest request = request("OPTIONS", "/api/clubs/1/views");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> { });

        assertThat(response.getCookies()).isEmpty();
        assertThat(request.getAttribute(ActivityTrackingFilter.ANONYMOUS_ID_REQUEST_ATTRIBUTE)).isNull();
        verify(activityTrackingService, never()).track(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
    }

    private MockHttpServletRequest request(String method, String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
        request.setRequestURI(uri);
        return request;
    }

    private FilterChain captureIdentity(AtomicReference<String> identity) {
        return (request, response) -> identity.set((String) request.getAttribute(
                ActivityTrackingFilter.ANONYMOUS_ID_REQUEST_ATTRIBUTE));
    }
}
