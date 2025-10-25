package com.kakaotech.team18.backend_server.global.security;

import com.kakaotech.team18.backend_server.domain.application.repository.ApplicationRepository;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.Role;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomSecurityService {

    private final ApplicationRepository applicationRepository;

    /**
     * 특정 지원서(applicationId)에 대한 접근 권한을 검사합니다.
     * <p>
     * 현재 로그인한 사용자가 해당 지원서가 속한 동아리의 CLUB_ADMIN 또는 CLUB_EXECUTIVE인지 확인합니다.
     *
     * @param applicationId 검사할 지원서의 ID
     * @return 권한이 있으면 true, 없으면 false
     */
    @Transactional(readOnly = true)
    public boolean isClubAdminOrExecutiveForApplication(Long applicationId) {
        // 1. 현재 인증된 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof PrincipalDetails principalDetails)) {
            return false; // 인증되지 않았거나, Principal 타입이 다르면 거부
        }

        // 2. applicationId로 clubId를 효율적으로 조회 (Projection)
        Long clubId = applicationRepository.findClubIdByApplicationId(applicationId)
                .orElse(null); // 지원서가 없으면 clubId는 null

        if (clubId == null) {
            // 지원서 자체가 존재하지 않는 경우, 서비스 계층에서 404 처리를 하겠지만,
            // 인가 계층에서는 일단 접근을 거부하는 것이 안전합니다.
            return false;
        }

        // 3. 사용자의 역할 정보(memberships) 가져오기
        Map<String, String> memberships = principalDetails.getMemberships();
        if (memberships == null) {
            return false;
        }

        // 4. 해당 clubId에 대한 사용자의 역할(Role) 가져오기
        String userRoleForClub = memberships.get(clubId.toString());

        // 5. 역할이 CLUB_ADMIN 또는 CLUB_EXECUTIVE인지 확인
        return Objects.equals(userRoleForClub, Role.CLUB_ADMIN.name()) ||
               Objects.equals(userRoleForClub, Role.CLUB_EXECUTIVE.name());
    }
}
