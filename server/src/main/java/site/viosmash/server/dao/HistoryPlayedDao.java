package site.viosmash.server.dao;

import site.viosmash.server.Db;

import java.sql.*;
import java.util.*;

public class HistoryPlayedDao {

    /**
     * Lấy danh sách match_id mà người chơi đã tham gia
     *
     * @param username Tên người chơi
     * @return Danh sách các match với match_id
     */
    public List<Map<String, Object>> getPlayerMatchHistory(String username) throws Exception {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT DISTINCT m.id as match_id, m.started_at, m.ended_at, m.room_owner " +
                             "FROM matches m " +
                             "JOIN match_players mp ON m.id = mp.match_id " +
                             "WHERE mp.username = ? " +
                             "ORDER BY m.started_at DESC")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                List<Map<String, Object>> matches = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> match = new HashMap<>();
                    match.put("match_id", rs.getLong("match_id"));
                    match.put("matchId", rs.getLong("match_id")); // Alternative key for compatibility
                    match.put("started_at", rs.getTimestamp("started_at"));
                    match.put("ended_at", rs.getTimestamp("ended_at"));
                    match.put("room_owner", rs.getString("room_owner"));
                    matches.add(match);
                }
                return matches;
            }
        }
    }

    /**
     * Lấy thông tin chi tiết về một trận đấu cụ thể
     *
     * @param matchId ID của trận đấu
     * @return Danh sách người chơi với điểm số và thời gian
     */
    public List<Map<String, Object>> getMatchDetails(long matchId) throws Exception {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT mp.username, mp.total_score, mp.total_time_ms " +
                             "FROM match_players mp " +
                             "WHERE mp.match_id = ? " +
                             "ORDER BY mp.total_score DESC, mp.total_time_ms ASC")) {
            ps.setLong(1, matchId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Map<String, Object>> players = new ArrayList<>();
                int rank = 1;
                while (rs.next()) {
                    Map<String, Object> player = new HashMap<>();
                    player.put("rank", rank++);
                    player.put("username", rs.getString("username"));
                    player.put("total_score", rs.getDouble("total_score"));
                    player.put("totalScore", rs.getDouble("total_score")); // Alternative key
                    player.put("total_time_ms", rs.getLong("total_time_ms"));
                    player.put("totalTimeMs", rs.getLong("total_time_ms")); // Alternative key
                    players.add(player);
                }
                return players;
            }
        }
    }

    /**
     * Lấy bảng xếp hạng tổng thể của tất cả người chơi
     * Tính tổng điểm, tổng thời gian và số trận đã chơi
     *
     * @return Danh sách người chơi với thống kê tổng hợp
     */
    public List<Map<String, Object>> getGlobalLeaderboard() throws Exception {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT mp.username, " +
                             "       SUM(mp.total_score) as total_score, " +
                             "       SUM(mp.total_time_ms) as total_time_ms, " +
                             "       COUNT(DISTINCT mp.match_id) as match_count " +
                             "FROM match_players mp " +
                             "GROUP BY mp.username " +
                             "ORDER BY total_score DESC, total_time_ms ASC")) {
            try (ResultSet rs = ps.executeQuery()) {
                List<Map<String, Object>> leaderboard = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> player = new HashMap<>();
                    player.put("username", rs.getString("username"));
                    player.put("total_score", rs.getDouble("total_score"));
                    player.put("total_time_ms", rs.getLong("total_time_ms"));
                    player.put("match_count", rs.getInt("match_count"));
                    leaderboard.add(player);
                }
                return leaderboard;
            }
        }
    }

    /**
     * Lấy thống kê chi tiết theo từng vòng của một người chơi trong một trận
     *
     * @param matchId  ID của trận đấu
     * @param username Tên người chơi
     * @return Danh sách các vòng với kết quả
     */
    public List<Map<String, Object>> getPlayerRoundResults(long matchId, String username) throws Exception {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT r.round_no, r.level, r.colors_json, r.show_ms, r.countdown_ms, " +
                             "       rr.selected_json, rr.score, rr.time_ms, rr.sent_at " +
                             "FROM rounds r " +
                             "LEFT JOIN round_results rr ON r.id = rr.round_id AND rr.username = ? " +
                             "WHERE r.match_id = ? " +
                             "ORDER BY r.round_no ASC")) {
            ps.setString(1, username);
            ps.setLong(2, matchId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Map<String, Object>> rounds = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> round = new HashMap<>();
                    round.put("round_no", rs.getInt("round_no"));
                    round.put("level", rs.getString("level"));
                    round.put("colors_json", rs.getString("colors_json"));
                    round.put("show_ms", rs.getInt("show_ms"));
                    round.put("countdown_ms", rs.getInt("countdown_ms"));
                    round.put("selected_json", rs.getString("selected_json"));
                    round.put("score", rs.getDouble("score"));
                    round.put("time_ms", rs.getLong("time_ms"));
                    round.put("sent_at", rs.getTimestamp("sent_at"));
                    rounds.add(round);
                }
                return rounds;
            }
        }
    }

    /**
     * Lấy top N người chơi có điểm cao nhất
     *
     * @param limit Số lượng người chơi cần lấy
     * @return Danh sách top người chơi
     */
    public List<Map<String, Object>> getTopPlayers(int limit) throws Exception {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT mp.username, " +
                             "       SUM(mp.total_score) as total_score, " +
                             "       SUM(mp.total_time_ms) as total_time_ms, " +
                             "       COUNT(DISTINCT mp.match_id) as match_count, " +
                             "       AVG(mp.total_score) as avg_score " +
                             "FROM match_players mp " +
                             "GROUP BY mp.username " +
                             "ORDER BY total_score DESC, total_time_ms ASC " +
                             "LIMIT ?")) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                List<Map<String, Object>> topPlayers = new ArrayList<>();
                int rank = 1;
                while (rs.next()) {
                    Map<String, Object> player = new HashMap<>();
                    player.put("rank", rank++);
                    player.put("username", rs.getString("username"));
                    player.put("total_score", rs.getDouble("total_score"));
                    player.put("total_time_ms", rs.getLong("total_time_ms"));
                    player.put("match_count", rs.getInt("match_count"));
                    player.put("avg_score", rs.getDouble("avg_score"));
                    topPlayers.add(player);
                }
                return topPlayers;
            }
        }
    }
}
