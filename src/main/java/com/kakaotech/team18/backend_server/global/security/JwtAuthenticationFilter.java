package com.kakaotech.team18.backend_server.global.security;

import com.kakaotech.team18.backend_server.global.exception.code.ErrorCode;
import com.kakaotech.team18.backend_server.global.exception.exceptions.InvalidJwtException;
import com.kakaotech.team18.backend_server.global.exception.exceptions.LoggedOutUserException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final PrincipalDetailsService principalDetailsService;
    private final HandlerExceptionResolver resolver;
    private final RedisTemplate<String, String> redisTemplate;

    // Qualifier 를 이용하기 위해서 RequiredArgsConstructor 사용 X
    public JwtAuthenticationFilter(JwtProvider jwtProvider, PrincipalDetailsService principalDetailsService, @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver, RedisTemplate<String, String> redisTemplate) {
        this.jwtProvider = jwtProvider;
        this.principalDetailsService = principalDetailsService;
        this.resolver = resolver;
        this.redisTemplate = redisTemplate;
    }

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

            // 블랙리스트 확인
            ValueOperations<String, String> values = redisTemplate.opsForValue();
            if (values.get("blacklist:" + token) != null) {
                // 블랙리스트에 존재하면, 로그아웃된 토큰으로 간주하고 예외를 발생시켜 요청을 차단합니다.
                resolver.resolveException(request, response, null, new InvalidJwtException(ErrorCode.BLACKLISTED_TOKEN));
                return; // 필터 체인 진행을 중단합니다.
            }

            // 4. 토큰 유효성 검증 및 클레임 추출
            Claims claims = jwtProvider.verify(token);

            // 4-1. 토큰 타입 검증: "ACCESS" 타입의 토큰만 인증 처리
            String tokenType = claims.get("tokenType", String.class);
            if (!TokenType.ACCESS.name().equals(tokenType)) {
                // ACCESS 토큰이 아니면 (예: REFRESH 토큰이면), 인증 처리하지 않고 다음 필터로 넘어간다.
                filterChain.doFilter(request, response);
                return;
            }

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

        } catch (SignatureException e) {
            resolver.resolveException(request, response, null, new InvalidJwtException(ErrorCode.INVALID_JWT_SIGNATURE));
            return;
        } catch (MalformedJwtException e) {
            resolver.resolveException(request, response, null, new InvalidJwtException(ErrorCode.MALFORMED_JWT));
            return;
        } catch (ExpiredJwtException e) {
            resolver.resolveException(request, response, null, new InvalidJwtException(ErrorCode.EXPIRED_JWT_TOKEN));
            return;
        } catch (UnsupportedJwtException e) {
            resolver.resolveException(request, response, null, new InvalidJwtException(ErrorCode.UNSUPPORTED_JWT));
            return;
        } catch (IllegalArgumentException e) {
            resolver.resolveException(request, response, null, new InvalidJwtException(ErrorCode.ILLEGAL_ARGUMENT_JWT));
            return;
        }

        // 8. 다음 필터 체인을 실행한다.
        filterChain.doFilter(request, response);
    }
}
