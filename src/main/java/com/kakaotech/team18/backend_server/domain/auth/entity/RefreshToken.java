package com.kakaotech.team18.backend_server.domain.auth.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@Getter
@AllArgsConstructor
@RedisHash(value = "refreshToken")
public class RefreshToken {

    @Id
    private Long id; // User ID

    private String refreshToken;

    @TimeToLive
    private Long ttl;
}
