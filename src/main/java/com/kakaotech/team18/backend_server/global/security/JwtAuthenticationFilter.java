package com.kakaotech.team18.backend_server.global.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final PrincipalDetailsService principalDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. 헤더에서 "Authorization" 값을 가져온다.
        String bearerToken = request.getHeader("Authorization");

        // 2. 토큰이 없거나, "Bearer "로 시작하지 않으면, 다음 필터로 넘어간다.
        // (이후 다른 필터, 예를 들어 익명 사용자 필터 등에서 처리될 수 있다)
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 3. "Bearer " 접두사를 제거하고 순수한 토큰을 추출한다.
            String token = jwtProvider.extractToken(bearerToken);

            // 4. 토큰 유효성 검증 및 클레임 추출
            Claims claims = jwtProvider.verify(token);

            // 5. 토큰의 subject(userId)로 UserDetails 객체를 조회한다.
            UserDetails userDetails = principalDetailsService.loadUserByUsername(claims.getSubject());

            // 6. Authentication 객체 생성 (Principal, Credentials, Authorities)
            // 우리는 JWT를 사용하므로, 비밀번호(Credentials)는 null로 설정한다.
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );

            // 7. SecurityContextHolder에 Authentication 객체를 저장한다.
            // 이 작업이 완료되면, 해당 요청은 '인증된' 것으로 간주된다.
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e) {
            // 토큰 검증 과정에서 예외 발생 시 (만료, 위변조 등), SecurityContext를 비운다.
            // 이렇게 하면, 해당 요청은 '인증되지 않은' 것으로 처리된다.
            SecurityContextHolder.clearContext();
        }

        // 8. 다음 필터 체인을 실행한다.
        filterChain.doFilter(request, response);
    }
}
