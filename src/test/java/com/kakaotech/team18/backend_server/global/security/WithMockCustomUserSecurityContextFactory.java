package com.kakaotech.team18.backend_server.global.security;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithMockCustomUserSecurityContextFactory
    implements WithSecurityContextFactory<WithMockCustomUser> {

  @Override
  public SecurityContext createSecurityContext(WithMockCustomUser customUser) {
    // 1. SecurityContext를 새로 생성합니다.
    SecurityContext context = SecurityContextHolder.createEmptyContext();

    // 2. memberships 문자열 배열을 Map<String, String>으로 변환합니다.
    Map<String, String> memberships =
        Arrays.stream(customUser.memberships())
            .map(s -> s.split(":"))
            .collect(Collectors.toMap(a -> a[0], a -> a[1]));

    // 3. 어노테이션의 정보를 바탕으로 PrincipalDetails 객체를 직접 생성합니다.
    PrincipalDetails principalDetails = new PrincipalDetails(customUser.userId(), memberships);

    // 4. Authentication 객체를 생성합니다.
    Authentication authentication =
        new UsernamePasswordAuthenticationToken(
            principalDetails, null, principalDetails.getAuthorities());

    // 5. SecurityContext에 Authentication 객체를 설정합니다.
    context.setAuthentication(authentication);

    // 6. 완성된 SecurityContext를 반환합니다.
    return context;
  }
}