package com.kakaotech.team18.backend_server.global.config;

import com.kakaotech.team18.backend_server.global.exception.exceptions.ForbiddenAccessException;
import com.kakaotech.team18.backend_server.global.exception.exceptions.UnauthenticatedUserException;
import com.kakaotech.team18.backend_server.global.security.JwtAuthenticationFilter;
import com.kakaotech.team18.backend_server.global.security.JwtProperties;
import com.kakaotech.team18.backend_server.global.security.JwtProvider;
import com.kakaotech.team18.backend_server.global.security.PrincipalDetailsService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    private final HandlerExceptionResolver resolver;
    private final JwtProvider jwtProvider;
    private final PrincipalDetailsService principalDetailsService;

    public SecurityConfig(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver,
            JwtProvider jwtProvider,
            PrincipalDetailsService principalDetailsService) {
        this.resolver = resolver;
        this.jwtProvider = jwtProvider;
        this.principalDetailsService = principalDetailsService;
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

        // =================== 개발 단계에서 모든 API 허용 ===================
        // TODO: 배포 시, 아래 주석을 풀고 원래의 보안 설정을 적용해야 합니다.
        http.authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll());
        // ===============================================================

        /*
        // =================== 원래의 보안 설정 (배포 시 활성화) ===================
        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/api/auth/**", "/swagger-ui.html", "/v3/api-docs/**", "/swagger-ui/**").permitAll()
                .anyRequest().authenticated()
        );
        // ====================================================================
        */

        http.exceptionHandling(exceptionHandling -> exceptionHandling
                .authenticationEntryPoint(authenticationEntryPoint())
                .accessDeniedHandler(accessDeniedHandler())
        );

        // JWT 인증 필터를 UsernamePasswordAuthenticationFilter 앞에 추가
        http.addFilterBefore(
                new JwtAuthenticationFilter(jwtProvider, principalDetailsService, resolver),
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
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
