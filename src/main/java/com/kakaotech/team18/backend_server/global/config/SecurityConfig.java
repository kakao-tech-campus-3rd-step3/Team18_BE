package com.kakaotech.team18.backend_server.global.config;

import com.kakaotech.team18.backend_server.domain.activity.service.ActivityTrackingService;
import com.kakaotech.team18.backend_server.global.exception.exceptions.ForbiddenAccessException;
import com.kakaotech.team18.backend_server.global.exception.exceptions.UnauthenticatedUserException;
import com.kakaotech.team18.backend_server.global.security.ActivityTrackingFilter;
import com.kakaotech.team18.backend_server.global.security.JwtAuthenticationFilter;
import com.kakaotech.team18.backend_server.global.security.JwtProperties;
import com.kakaotech.team18.backend_server.global.security.JwtProvider;
import com.kakaotech.team18.backend_server.global.security.PrincipalDetailsService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    private final HandlerExceptionResolver resolver;
    private final JwtProvider jwtProvider;
    private final PrincipalDetailsService principalDetailsService;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectProvider<ActivityTrackingFilter> activityTrackingFilterProvider;

    public SecurityConfig(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver,
            JwtProvider jwtProvider,
            PrincipalDetailsService principalDetailsService,
            RedisTemplate<String, String> redisTemplate,
            ObjectProvider<ActivityTrackingFilter> activityTrackingFilterProvider) {
        this.resolver = resolver;
        this.jwtProvider = jwtProvider;
        this.principalDetailsService = principalDetailsService;
        this.redisTemplate = redisTemplate;
        this.activityTrackingFilterProvider = activityTrackingFilterProvider;
    }

    /**
     * 정적 리소스나 인증이 전혀 필요 없는 경로들을 Spring Security 필터 체인에서 제외합니다.
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers("/h2-console/**", "/swagger-ui.html", "/v3/api-docs/**", "/swagger-ui/**");
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable());
        http.formLogin(formLogin -> formLogin.disable());
        http.httpBasic(httpBasic -> httpBasic.disable());

        http.headers(headers -> headers.frameOptions(FrameOptionsConfig::disable));

        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/api/auth/**", "/swagger-ui.html", "/v3/api-docs/**", "/swagger-ui/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/webhooks/solapi").permitAll()
                // Actuator 엔드포인트 (모니터링용)
                .requestMatchers("/actuator/health", "/actuator/prometheus").permitAll()
                // 공지사항 조회 관련 API (공개)
                .requestMatchers(HttpMethod.GET, "/api/notices", "/api/notices/*").permitAll()
                // 동아리 정보 조회 관련 API (공개)
                .requestMatchers(HttpMethod.GET, "/api/clubs", "/api/clubs/*").permitAll()
                // 지원서 양식 조회 API (공개)
                .requestMatchers(HttpMethod.GET, "/api/clubs/*/apply").permitAll()
                // 동아리원 일괄 등록 양식 다운로드 API (공개)
                .requestMatchers(HttpMethod.GET, "/api/clubs/members/registration-form").permitAll()
                // 지원서 제출 API (공개)
                .requestMatchers(HttpMethod.POST, "/api/clubs/*/apply-submit").permitAll()
                // 동아리 후기 조회 및 등록 API (공개)
                .requestMatchers("/api/clubs/*/reviews").permitAll()
                // 헬스체크 (공개)
                .requestMatchers("/actuator/health","/actuator/health/**").permitAll()
                .anyRequest().authenticated()
        );

        http.exceptionHandling(exceptionHandling -> exceptionHandling
                .authenticationEntryPoint(authenticationEntryPoint())
                .accessDeniedHandler(accessDeniedHandler())
        );

        // JWT 인증 필터를 UsernamePasswordAuthenticationFilter 앞에 추가
        http.addFilterBefore(
                new JwtAuthenticationFilter(jwtProvider, principalDetailsService, resolver, redisTemplate),
                UsernamePasswordAuthenticationFilter.class);
        ActivityTrackingFilter activityTrackingFilter = activityTrackingFilterProvider.getIfAvailable();
        if (activityTrackingFilter != null) {
            http.addFilterAfter(activityTrackingFilter, JwtAuthenticationFilter.class);
        }

        return http.build();
    }

    @Bean
    @ConditionalOnBean(ActivityTrackingService.class)
    public ActivityTrackingFilter activityTrackingFilter(ActivityTrackingService activityTrackingService) {
        return new ActivityTrackingFilter(activityTrackingService);
    }

    private AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            resolver.resolveException(request, response, null, new UnauthenticatedUserException());
        };
    }

    private AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            resolver.resolveException(request, response, null, new ForbiddenAccessException());
        };
    }
}
