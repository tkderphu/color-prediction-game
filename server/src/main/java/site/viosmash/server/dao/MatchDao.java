// server/src/main/java/com/cgo/server/dao/MatchDao.java
package site.viosmash.server.dao;
import site.viosmash.server.Db;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class MatchDao {
    public long createMatch(String roomOwner) throws Exception {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO matches(room_owner, started_at) VALUES (?,?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, roomOwner);
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { rs.next(); return rs.getLong(1); }
        }
    }
    public void endMatch(long matchId) throws Exception {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE matches SET ended_at=? WHERE id=?")) {
            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(2, matchId);
            ps.executeUpdate();
        }
    }
    public void addPlayer(long matchId, String username) throws Exception {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO match_players(match_id, username) VALUES (?,?)")) {
            ps.setLong(1, matchId);
            ps.setString(2, username);
            ps.executeUpdate();
        }
    }
    public long createRound(long matchId, int roundNo, String level,
                            String colorsJson, int showMs, int countdownMs) throws Exception {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO rounds(match_id, round_no, level, colors_json, show_ms, countdown_ms, sent_at) " +
                             "VALUES (?,?,?,?,?,?,?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, matchId);
            ps.setInt(2, roundNo);
            ps.setString(3, level);
            ps.setString(4, colorsJson);
            ps.setInt(5, showMs);
            ps.setInt(6, countdownMs);
            ps.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { rs.next(); return rs.getLong(1); }
        }
    }
    public void saveRoundResult(long roundId, String username, String selectedJson,
                                double score, long timeMs, Timestamp sentAt) throws Exception {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO round_results(round_id, username, selected_json, score, time_ms, sent_at) " +
                             "VALUES (?,?,?,?,?,?)")) {
            ps.setLong(1, roundId);
            ps.setString(2, username);
            ps.setString(3, selectedJson);
            ps.setDouble(4, score);
            ps.setLong(5, timeMs);
            ps.setTimestamp(6, sentAt);
            ps.executeUpdate();
        }
    }
    public void updatePlayerTotals(long matchId, String username,
                                   double totalScoreDelta, long timeDeltaMs) throws Exception {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE match_players SET total_score = total_score + ?, " +
                             "total_time_ms = total_time_ms + ? WHERE match_id=? AND username=?")) {
            ps.setDouble(1, totalScoreDelta);
            ps.setLong(2, timeDeltaMs);
            ps.setLong(3, matchId);
            ps.setString(4, username);
            ps.executeUpdate();
        }
    }
    public List<Map<String,Object>> finalRanking(long matchId) throws Exception {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT username, total_score, total_time_ms FROM match_players " +
                             "WHERE match_id=? ORDER BY total_score DESC, total_time_ms ASC")) {
            ps.setLong(1, matchId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Map<String,Object>> out = new ArrayList<>();
                while (rs.next()) {
                    Map<String,Object> m = new HashMap<>();
                    m.put("username", rs.getString(1));
                    m.put("totalScore", rs.getDouble(2));
                    m.put("totalTimeMs", rs.getLong(3));
                    out.add(m);
                }
                return out;
            }
        }
    }
}
