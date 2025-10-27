package com.kakaotech.team18.backend_server.global.security;

import com.kakaotech.team18.backend_server.domain.clubMember.dto.ClubMembershipInfo;
import com.kakaotech.team18.backend_server.domain.clubMember.repository.ClubMemberRepository;
import com.kakaotech.team18.backend_server.domain.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtProvider {

    private final JwtProperties jwtProperties;
    private final ClubMemberRepository clubMemberRepository;

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
        // 사용자가 속한 모든 동아리의 멤버십 정보(clubId, role)를 DTO로 직접 조회합니다.
        List<ClubMembershipInfo> membershipInfos = clubMemberRepository.findClubMembershipsByUser(user);

        // 멤버십 정보를 {clubId: roleName} 형태의 Map으로 변환합니다.
        Map<String, String> structuredMemberships = membershipInfos.stream()
                .collect(Collectors.toMap(
                        info -> info.clubId().toString(),
                        info -> info.role().name()
                ));

        long now = (new Date()).getTime();
        Date validity = new Date(now + jwtProperties.accessTokenValidityInSeconds() * 1000);

        return Jwts.builder()
                .setSubject(user.getId().toString())
                .claim("tokenType", "ACCESS") // 토큰 타입: ACCESS
                .claim("memberships", structuredMemberships) // 멤버십 정보 추가
                .claim("tokenType", TokenType.ACCESS.name()) // 토큰 타입: ACCESS
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
                .claim("tokenType", TokenType.REFRESH.name()) // 토큰 타입: REFRESH
                .setIssuedAt(new Date())
                .setExpiration(validity)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    // Temporary Token 생성
    public String createTemporaryToken(Long kakaoId, String nickname) {
        long now = (new Date()).getTime();
        Date validity = new Date(now + this.temporaryTokenValidityInSeconds * 1000);
 
        return Jwts.builder()
                .setSubject(TokenType.TEMPORARY.name()) // 임시 토큰 구분
                .claim("tokenType", TokenType.TEMPORARY.name())
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
