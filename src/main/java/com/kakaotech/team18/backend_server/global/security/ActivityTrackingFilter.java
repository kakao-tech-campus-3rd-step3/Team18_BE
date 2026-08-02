package com.kakaotech.team18.backend_server.global.security;

import com.kakaotech.team18.backend_server.domain.activity.service.ActivityTrackingService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
@Slf4j
public class ActivityTrackingFilter extends OncePerRequestFilter {

    public static final String ANONYMOUS_ID_REQUEST_ATTRIBUTE = "clubPopularity.anonymousId";

    static final String ANONYMOUS_COOKIE_NAME = "anonymous_id";
    private static final int ANONYMOUS_COOKIE_MAX_AGE_SECONDS = 60 * 60 * 24 * 365;
    private static final Pattern POPULARITY_RECORDING_PATH =
            Pattern.compile("^/api/clubs/[^/]+/(views|heartbeat)$");
    private static final String POPULARITY_QUERY_PATH = "/api/clubs/popular";

    private final ActivityTrackingService activityTrackingService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestUri = request.getRequestURI();
        String method = request.getMethod();
        if (!shouldProvideAnonymousIdentity(requestUri, method)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 쿠키는 브라우저의 식별자, 요청 속성은 이번 요청 내부 전달용이다.
        String anonymousId = getCookieValue(request, ANONYMOUS_COOKIE_NAME);
        if (anonymousId == null || anonymousId.isBlank()) {
            anonymousId = UUID.randomUUID().toString();
            Cookie cookie = new Cookie(ANONYMOUS_COOKIE_NAME, anonymousId);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            cookie.setMaxAge(ANONYMOUS_COOKIE_MAX_AGE_SECONDS);
            cookie.setSecure(request.isSecure());
            response.addCookie(cookie);
        }

        request.setAttribute(ANONYMOUS_ID_REQUEST_ATTRIBUTE, anonymousId);

        // 인기 기록 API는 식별자만 필요하므로 기존 ActivityDaily를 오염시키지 않는다.
        if (isActivityTrackTarget(requestUri, method)) {
            try {
                Long userId = extractUserId();
                activityTrackingService.track(anonymousId, userId);
            } catch (Exception e) {
                // MAU 추적 실패가 비즈니스 요청 실패로 이어지지 않도록 보호
                log.warn("Failed to track activity for uri={}: {}", requestUri, e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean shouldProvideAnonymousIdentity(String uri, String method) {
        return isActivityTrackTarget(uri, method) || isPopularityRecordingRequest(uri, method);
    }

    private boolean isActivityTrackTarget(String uri, String method) {
        if (uri == null || !uri.startsWith("/api/")) {
            return false;
        }
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return false;
        }
        return !isPopularityPath(uri);
    }

    private boolean isPopularityRecordingRequest(String uri, String method) {
        // 두 POST 경로만 인기 기록 API로 취급한다.
        return "POST".equalsIgnoreCase(method)
                && uri != null
                && POPULARITY_RECORDING_PATH.matcher(uri).matches();
    }

    private boolean isPopularityPath(String uri) {
        return POPULARITY_QUERY_PATH.equals(uri)
                || POPULARITY_RECORDING_PATH.matcher(uri).matches();
    }

    private String getCookieValue(HttpServletRequest request, String cookieName) {
        if (request.getCookies() == null) {
            return null;
        }
        return Arrays.stream(request.getCookies())
                .filter(c -> cookieName.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    private Long extractUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof PrincipalDetails principalDetails)) {
            return null;
        }
        return principalDetails.getUserId();
    }
}
