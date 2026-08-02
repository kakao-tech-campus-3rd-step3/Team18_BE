package com.kakaotech.team18.backend_server.domain.clubPopularity.dto;

import java.util.List;

public record ClubPopularityResponse(List<PopularClubResponse> clubs) {

    public ClubPopularityResponse {
        clubs = List.copyOf(clubs);
    }

    public static ClubPopularityResponse empty() {
        return new ClubPopularityResponse(List.of());
    }
}
