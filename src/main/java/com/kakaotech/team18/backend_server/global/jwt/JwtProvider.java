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
import java.util.Map;

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

    /**
     * 추가 정보 입력이 필요한 신규 회원을 위한 임시 토큰을 생성합니다.
     * @param kakaoId 카카오로부터 받은 고유 ID
     * @param nickname 카카오로부터 받은 닉네임
     * @return 임시 토큰 문자열
     */
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

    // TODO: 토큰 검증 및 클레임 추출 메서드들은 다음 단계에서 구현
}
