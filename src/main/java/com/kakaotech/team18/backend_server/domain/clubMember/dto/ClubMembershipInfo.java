package com.kakaotech.team18.backend_server.domain.clubMember.dto;

import com.kakaotech.team18.backend_server.domain.clubMember.entity.Role;

public record ClubMembershipInfo(Long clubId, Role role) {
}
