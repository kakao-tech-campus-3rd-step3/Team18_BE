package com.kakaotech.team18.backend_server.domain.clubPopularity.repository;

import java.time.Instant;
import java.util.List;

public interface ClubViewBatchRepository {

    void upsertUser(long clubId, long userId, Instant lastViewedAt);

    void upsertAnonymous(long clubId, byte[] anonymousIdentity, Instant lastViewedAt);

    int deleteOlderThan(Instant cutoff, int batchSize);

    void deleteAllByClubIds(List<Long> clubIds);
}
