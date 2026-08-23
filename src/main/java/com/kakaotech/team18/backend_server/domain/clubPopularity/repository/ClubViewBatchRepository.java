package com.kakaotech.team18.backend_server.domain.clubPopularity.repository;

import java.time.Instant;
import java.util.List;

public interface ClubViewBatchRepository {

    void upsertAll(List<Upsert> records);

    void upsertUser(long clubId, long userId, Instant lastViewedAt);

    void upsertAnonymous(long clubId, byte[] anonymousIdentity, Instant lastViewedAt);

    int deleteOlderThan(Instant cutoff, int batchSize);

    void deleteAllByClubIds(List<Long> clubIds);

    record Upsert(long clubId, Long userId, byte[] anonymousIdentity, Instant lastViewedAt) {
        public static Upsert user(long clubId, long userId, Instant lastViewedAt) {
            return new Upsert(clubId, userId, null, lastViewedAt);
        }

        public static Upsert anonymous(long clubId, byte[] anonymousIdentity, Instant lastViewedAt) {
            return new Upsert(clubId, null, anonymousIdentity, lastViewedAt);
        }
    }
}
