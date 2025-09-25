package com.kakaotech.team18.backend_server.global.config;

import com.kakaotech.team18.backend_server.global.exception.exceptions.ForbiddenAccessException;
import com.kakaotech.team18.backend_server.global.exception.exceptions.UnauthenticatedUserException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final HandlerExceptionResolver resolver;

    public SecurityConfig(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.resolver = resolver;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable());
        http.formLogin(formLogin -> formLogin.disable());
        http.httpBasic(httpBasic -> httpBasic.disable());

        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/api/auth/**", "/swagger-ui.html", "/v3/api-docs/**", "/swagger-ui/**").permitAll()
                .anyRequest().authenticated()
        );

        http.exceptionHandling(exceptionHandling -> exceptionHandling
                .authenticationEntryPoint(authenticationEntryPoint())
                .accessDeniedHandler(accessDeniedHandler())
        );

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
