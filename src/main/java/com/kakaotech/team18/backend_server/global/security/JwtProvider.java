package com.kakaotech.team18.backend_server.global.security;

import com.kakaotech.team18.backend_server.domain.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtProvider {

    private final JwtProperties jwtProperties;

    // 임시 토큰 유효 시간 (5분)
    private final long temporaryTokenValidityInSeconds = 300;

    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String HEADER = "Authorization";

    private Key getSigningKey() {
        byte[] keyBytes = jwtProperties.secret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // 정식 Access Token 생성
    public String createAccessToken(User user) {
        // TODO: ClubMemberRepository를 통해 사용자의 역할(Role) 정보를 조회하는 로직 추가 필요
        // Map<String, Object> claims = Map.of("memberships", structuredRoles);

        long now = (new Date()).getTime();
        Date validity = new Date(now + jwtProperties.accessTokenValidityInSeconds() * 1000);

        return Jwts.builder()
                .setSubject(user.getId().toString())
                .claim("tokenType", "ACCESS") // 토큰 타입: ACCESS
                .setIssuedAt(new Date())
                .setExpiration(validity)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    // Refresh Token 생성
    public String createRefreshToken(User user) {
        long now = (new Date()).getTime();
        Date validity = new Date(now + jwtProperties.refreshTokenValidityInSeconds() * 1000);

        return Jwts.builder()
                .setSubject(user.getId().toString())
                .claim("tokenType", "REFRESH") // 토큰 타입: REFRESH
                .setIssuedAt(new Date())
                .setExpiration(validity)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public String createTemporaryToken(Long kakaoId, String nickname) {
        long now = (new Date()).getTime();
        Date validity = new Date(now + this.temporaryTokenValidityInSeconds * 1000);
 
        return Jwts.builder()
                .setSubject("temporary") // 임시 토큰 구분
                .claim("tokenType", "TEMPORARY")
                .claim("kakaoId", kakaoId)
                .claim("nickname", nickname)
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
