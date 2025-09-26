package com.kakaotech.team18.backend_server.global.security;

import com.kakaotech.team18.backend_server.domain.user.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;

@Slf4j
@Component
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-validity-in-seconds}")
    private long accessTokenValidityInSeconds;

    @Value("${jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenValidityInSeconds;

    // 임시 토큰 유효 시간 (5분)
    private final long temporaryTokenValidityInSeconds = 300;

    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String HEADER = "Authorization";

    private Key getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // 정식 Access Token 생성
    public String createAccessToken(User user) {
        // TODO: ClubMemberRepository를 통해 사용자의 역할(Role) 정보를 조회하는 로직 추가 필요
        // Map<String, Object> claims = Map.of("memberships", structuredRoles);

        long now = (new Date()).getTime();
        Date validity = new Date(now + this.accessTokenValidityInSeconds * 1000);

        return Jwts.builder()
                .setSubject(user.getId().toString())
                // .setClaims(claims) // 역할 정보 추가
                .setIssuedAt(new Date())
                .setExpiration(validity)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    // Refresh Token 생성
    public String createRefreshToken(User user) {
        long now = (new Date()).getTime();
        Date validity = new Date(now + this.refreshTokenValidityInSeconds * 1000);

        return Jwts.builder()
                .setSubject(user.getId().toString())
                .setIssuedAt(new Date())
                .setExpiration(validity)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public String createTemporaryToken(Long kakaoId, String nickname) {
        long now = (new Date()).getTime();
        Date validity = new Date(now + this.temporaryTokenValidityInSeconds * 1000);

        // 임시 토큰에는 회원가입 완료에 필요한 최소한의 정보만 담습니다.
        Map<String, Object> claims = Map.of(
                "kakaoId", kakaoId,
                "nickname", nickname
        );

        return Jwts.builder()
                .setSubject("temporary") // 토큰의 주체를 'temporary'로 설정하여 구분
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(validity)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 토큰의 유효성을 검증하고, 파싱하여 클레임(Payload)을 반환합니다.
     * @param token 검증할 JWT
     * @return 토큰의 클레임
     * @throws ExpiredJwtException 토큰이 만료된 경우
     * @throws MalformedJwtException 토큰이 유효하지 않은 형식인 경우
     * @throws SignatureException 서명이 유효하지 않은 경우
     */
    public Claims verify(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 토큰 타입이 "Bearer"인지 확인하고, 실제 토큰 문자열만 추출합니다.
     * @param bearerToken "Bearer " 접두사가 포함된 토큰
     * @return 실제 토큰 문자열
     * @throws IllegalArgumentException 토큰이 "Bearer "로 시작하지 않는 경우
     */
    public String extractToken(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(TOKEN_PREFIX.length());
        }
        throw new IllegalArgumentException("Invalid token type");
    }
}
