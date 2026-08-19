package com.kakaotech.team18.backend_server.domain.clubPopularity.repository;

import java.sql.Timestamp;
import java.sql.Connection;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;

@RequiredArgsConstructor
public class ClubViewBatchRepositoryImpl implements ClubViewBatchRepository {

    private final JdbcTemplate jdbcTemplate;
    private final AtomicReference<Boolean> mySqlCache = new AtomicReference<>();

    @Override
    public void upsertAll(List<Upsert> records) {
        if (records.isEmpty()) return;
        if (!isMySql()) {
            records.forEach(record -> {
                if (record.userId() != null) upsertUser(record.clubId(), record.userId(), record.lastViewedAt());
                else upsertAnonymous(record.clubId(), record.anonymousIdentity(), record.lastViewedAt());
            });
            return;
        }
        batchUpsert(records.stream().filter(record -> record.userId() != null).toList(), true);
        batchUpsert(records.stream().filter(record -> record.userId() == null).toList(), false);
    }

    private void batchUpsert(List<Upsert> records, boolean user) {
        if (records.isEmpty()) return;
        String sql = user ? """
                INSERT INTO club_view (club_id, user_id, anonymous_identity, last_viewed_at) VALUES (?, ?, NULL, ?)
                ON DUPLICATE KEY UPDATE last_viewed_at = GREATEST(last_viewed_at, VALUES(last_viewed_at))
                """ : """
                INSERT INTO club_view (club_id, user_id, anonymous_identity, last_viewed_at) VALUES (?, NULL, ?, ?)
                ON DUPLICATE KEY UPDATE last_viewed_at = GREATEST(last_viewed_at, VALUES(last_viewed_at))
                """;
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            public void setValues(java.sql.PreparedStatement statement, int index) throws java.sql.SQLException {
                Upsert record = records.get(index);
                statement.setLong(1, record.clubId());
                if (user) statement.setLong(2, record.userId()); else statement.setBytes(2, record.anonymousIdentity());
                statement.setTimestamp(3, Timestamp.from(record.lastViewedAt()));
            }
            public int getBatchSize() { return records.size(); }
        });
    }

    @Override
    public void upsertUser(long clubId, long userId, Instant lastViewedAt) {
        if (!isMySql()) {
            upsertPortable("SELECT id, last_viewed_at FROM club_view WHERE club_id = ? AND user_id = ?",
                    clubId, userId, null, lastViewedAt);
            return;
        }
        jdbcTemplate.update("""
                INSERT INTO club_view (club_id, user_id, anonymous_identity, last_viewed_at)
                VALUES (?, ?, NULL, ?)
                ON DUPLICATE KEY UPDATE last_viewed_at = GREATEST(last_viewed_at, VALUES(last_viewed_at))
                """, clubId, userId, Timestamp.from(lastViewedAt));
    }

    @Override
    public void upsertAnonymous(long clubId, byte[] anonymousIdentity, Instant lastViewedAt) {
        if (!isMySql()) {
            upsertPortable("SELECT id, last_viewed_at FROM club_view WHERE club_id = ? AND anonymous_identity = ?",
                    clubId, null, anonymousIdentity, lastViewedAt);
            return;
        }
        jdbcTemplate.update("""
                INSERT INTO club_view (club_id, user_id, anonymous_identity, last_viewed_at)
                VALUES (?, NULL, ?, ?)
                ON DUPLICATE KEY UPDATE last_viewed_at = GREATEST(last_viewed_at, VALUES(last_viewed_at))
                """, clubId, anonymousIdentity, Timestamp.from(lastViewedAt));
    }

    @Override
    public int deleteOlderThan(Instant cutoff, int batchSize) {
        return jdbcTemplate.update("""
                DELETE FROM club_view
                WHERE id IN (SELECT id FROM (SELECT id FROM club_view WHERE last_viewed_at <= ? ORDER BY id LIMIT ?) doomed)
                """, Timestamp.from(cutoff), batchSize);
    }

    @Override
    public void deleteAllByClubIds(List<Long> clubIds) {
        if (clubIds.isEmpty()) {
            return;
        }
        String placeholders = String.join(",", java.util.Collections.nCopies(clubIds.size(), "?"));
        jdbcTemplate.update("DELETE FROM club_view WHERE club_id IN (" + placeholders + ")", clubIds.toArray());
    }

    private void upsertPortable(String lookupSql, long clubId, Long userId, byte[] anonymousIdentity,
            Instant lastViewedAt) {
        List<Long> ids = userId != null
                ? jdbcTemplate.query(lookupSql, (rs, rowNum) -> rs.getLong("id"), clubId, userId)
                : jdbcTemplate.query(lookupSql, (rs, rowNum) -> rs.getLong("id"), clubId, anonymousIdentity);
        if (ids.isEmpty()) {
            try {
                jdbcTemplate.update("INSERT INTO club_view (club_id, user_id, anonymous_identity, last_viewed_at) VALUES (?, ?, ?, ?)",
                        clubId, userId, anonymousIdentity, Timestamp.from(lastViewedAt));
            } catch (DuplicateKeyException exception) {
                // 다른 flush 작업이 먼저 삽입한 경우 UPDATE로 최신 시각만 반영한다.
                ids = jdbcTemplate.query(lookupSql, (rs, rowNum) -> rs.getLong("id"), clubId,
                        userId != null ? userId : anonymousIdentity);
                if (ids.isEmpty()) {
                    throw exception;
                }
                jdbcTemplate.update("UPDATE club_view SET last_viewed_at = CASE WHEN last_viewed_at < ? THEN ? ELSE last_viewed_at END WHERE id = ?",
                        Timestamp.from(lastViewedAt), Timestamp.from(lastViewedAt), ids.get(0));
            }
            return;
        }
        jdbcTemplate.update("UPDATE club_view SET last_viewed_at = CASE WHEN last_viewed_at < ? THEN ? ELSE last_viewed_at END WHERE id = ?",
                Timestamp.from(lastViewedAt), Timestamp.from(lastViewedAt), ids.get(0));
    }

    private boolean isMySql() {
        Boolean cached = mySqlCache.get();
        if (cached != null) {
            return cached;
        }
        boolean resolved = Boolean.TRUE.equals(jdbcTemplate.execute((Connection connection) ->
                connection.getMetaData().getDatabaseProductName().toLowerCase(Locale.ROOT).contains("mysql")));
        mySqlCache.compareAndSet(null, resolved);
        return resolved;
    }
}
