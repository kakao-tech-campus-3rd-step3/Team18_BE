package com.kakaotech.team18.backend_server.domain.clubPopularity.dto;

public record PopularClubResponse(
        long clubId,
        int recentViewerCount,
        int activeViewerCount,
        boolean recentViewerBadge,
        boolean activeViewerBadge) {
}
