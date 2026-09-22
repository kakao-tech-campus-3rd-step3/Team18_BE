package com.kakaotech.team18.backend_server.global.security;

import com.kakaotech.team18.backend_server.global.exception.code.ErrorCode;
import com.kakaotech.team18.backend_server.global.exception.exceptions.ExpiredAccessTokenException;
import com.kakaotech.team18.backend_server.global.exception.exceptions.InvalidJwtException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Collections;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final PrincipalDetailsService principalDetailsService;
    private final HandlerExceptionResolver resolver;
    private final RedisTemplate<String, String> redisTemplate;

    // 필터링을 건너뛸 경로 목록을 미리 정의합니다.
    private final List<AntPathRequestMatcher> permitAllMatchers;

    // Qualifier 를 이용하기 위해서 RequiredArgsConstructor 사용 X
    public JwtAuthenticationFilter(JwtProvider jwtProvider, PrincipalDetailsService principalDetailsService, @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver, RedisTemplate<String, String> redisTemplate) {
        this.jwtProvider = jwtProvider;
        this.principalDetailsService = principalDetailsService;
        this.resolver = resolver;
        this.redisTemplate = redisTemplate;
        // SecurityConfig의 permitAll 규칙들을 여기에 명시적으로 정의합니다.
        this.permitAllMatchers = List.of(
                new AntPathRequestMatcher("/api/auth/**"),
                new AntPathRequestMatcher("/swagger-ui.html"),
                new AntPathRequestMatcher("/v3/api-docs/**"),
                new AntPathRequestMatcher("/swagger-ui/**"),
                new AntPathRequestMatcher("/api/notices", HttpMethod.GET.name()),
                new AntPathRequestMatcher("/api/notices/*", HttpMethod.GET.name()),
                new AntPathRequestMatcher("/api/clubs", HttpMethod.GET.name()),
                new AntPathRequestMatcher("/api/clubs/*", HttpMethod.GET.name()),
                new AntPathRequestMatcher("/api/clubs/*/apply", HttpMethod.GET.name()),
                new AntPathRequestMatcher("/api/clubs/*/apply-submit", HttpMethod.POST.name()),
                new AntPathRequestMatcher("/api/clubs/*/reviews")
        );
    }

    /**
     * 이 필터가 동작하지 않아야 할 경로인지 확인합니다.
     * 생성자에서 정의된 permitAllMatchers 리스트와 현재 요청을 비교합니다.
     * @return 필터를 건너뛰려면 true, 필터를 실행하려면 false
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return permitAllMatchers.stream()
                .anyMatch(matcher -> matcher.matches(request));
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

        String token = null; // try-catch 블록 밖에서 token 변수 선언
        try {
            // 3. "Bearer " 접두사를 제거하고 순수한 토큰을 추출한다.
            token = jwtProvider.extractToken(bearerToken);

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

            // 5. 토큰에서 "memberships" 클레임을 추출한다.
            // get(key, Class)를 사용하여 타입 안전하게 추출하고, 없을 경우를 대비해 기본값(빈 맵)을 설정한다.
            Map<String, String> memberships = claims.get("memberships", Map.class);
            if (memberships == null) {
                memberships = Collections.emptyMap();
            }

            // 6. 토큰의 subject(userId)와 memberships 정보로 UserDetails 객체를 조회한다.
            UserDetails userDetails = principalDetailsService.loadUserByUsername(claims.getSubject(), memberships);

            // 7. Authentication 객체 생성 (Principal, Credentials, Authorities)
            // 우리는 JWT를 사용하므로, 비밀번호(Credentials)는 null로 설정한다.
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );

            // 8. SecurityContextHolder에 Authentication 객체를 저장한다.
            // 이 작업이 완료되면, 해당 요청은 '인증된' 것으로 간주된다.
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("인증 성공: userId='{}', uri='{}'", userDetails.getUsername(), request.getRequestURI()); // 인증 성공 로그

        } catch (SignatureException e) {
            log.warn("유효하지 않은 JWT 서명입니다. token={}, uri={}", token, request.getRequestURI()); // 예외 로그
            resolver.resolveException(request, response, null, new InvalidJwtException(ErrorCode.INVALID_JWT_SIGNATURE));
            return;
        } catch (MalformedJwtException e) {
            log.warn("손상된 JWT 토큰입니다. token={}, uri={}", token, request.getRequestURI()); // 예외 로그
            resolver.resolveException(request, response, null, new InvalidJwtException(ErrorCode.MALFORMED_JWT));
            return;
        } catch (ExpiredJwtException e) {
            log.warn("만료된 JWT 토큰입니다. token={}, uri={}", token, request.getRequestURI()); // 예외 로그
            // Access Token이 만료된 경우, ExpiredAccessTokenException 커스텀 예외를 발생시켜 401 에러를 응답합니다.
            resolver.resolveException(request, response, null, new ExpiredAccessTokenException());
            return;
        } catch (UnsupportedJwtException e) {
            log.warn("지원하지 않는 JWT 토큰입니다. token={}, uri={}", token, request.getRequestURI()); // 예외 로그
            resolver.resolveException(request, response, null, new InvalidJwtException(ErrorCode.UNSUPPORTED_JWT));
            return;
        } catch (IllegalArgumentException e) {
            log.warn("JWT 클레임이 비어있습니다. token={}, uri={}", token, request.getRequestURI()); // 예외 로그
            resolver.resolveException(request, response, null, new InvalidJwtException(ErrorCode.ILLEGAL_ARGUMENT_JWT));
            return;
        }

        // 9. 다음 필터 체인을 실행한다.
        filterChain.doFilter(request, response);
    }
}
