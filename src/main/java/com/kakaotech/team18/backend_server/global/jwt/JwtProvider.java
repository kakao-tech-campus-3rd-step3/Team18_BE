package com.kakaotech.team18.backend_server.global.jwt;

import com.kakaotech.team18.backend_server.domain.user.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-validity-in-seconds}")
    private long accessTokenValidityInSeconds;

    @Value("${jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenValidityInSeconds;

    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String HEADER = "Authorization";

    private Key getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String createAccessToken(User user) {
        // TODO: ClubMemberRepository를 통해 사용자의 역할(Role) 정보를 조회하는 로직 추가 필요
        // List<Map<String, String>> structuredRoles = ...

        long now = (new Date()).getTime();
        Date validity = new Date(now + this.accessTokenValidityInSeconds * 1000);

        return Jwts.builder()
                .setSubject(user.getId().toString())
                // .claim("memberships", structuredRoles) // 역할 정보 추가
                .setIssuedAt(new Date())
                .setExpiration(validity)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

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

    // TODO: 토큰 검증 및 클레임 추출 메서드들은 다음 단계에서 구현
}
