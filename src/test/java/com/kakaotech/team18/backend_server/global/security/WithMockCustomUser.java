package com.kakaotech.team18.backend_server.global.security;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.springframework.security.test.context.support.WithSecurityContext;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory.class)
public @interface WithMockCustomUser {

    long userId() default 1L;

    String nickname() default "testuser";

    /**
     * 사용자의 동아리 멤버십 정보를 "clubId:ROLE" 형식의 문자열 배열로 설정합니다.
     * <p>
     * 예: {"1:CLUB_ADMIN", "3:CLUB_MEMBER"}
     */
    String[] memberships() default {};
}