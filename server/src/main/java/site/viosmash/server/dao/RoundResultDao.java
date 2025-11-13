package site.viosmash.server.dao;

import com.fasterxml.jackson.core.type.TypeReference;
import site.viosmash.common.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Nguyen Quang Phu
 * @since 05/11/2025
 */
public class RoundResultDao extends Dao{

    public void save(RoundResult roundResult) throws Exception {
        try (
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO round_results(round_id, user_id, selected_colors, score, time_ms, sent_at) " +
                             "VALUES (?,?,?,?,?,?)")) {
            ps.setLong(1, roundResult.getRound().getId());
            ps.setInt(2, roundResult.getUser().getId());
            ps.setString(3, Json.to(roundResult.getSelectedColors()));
            ps.setDouble(4, roundResult.getScore());
            ps.setLong(5, roundResult.getTimeMs());
            if(roundResult.getSentAt() != null) {
                ps.setTimestamp(6, Timestamp.valueOf(roundResult.getSentAt()));

            }
            ps.executeUpdate();
        }
    }

    public List<RoundResult> getListByMatchId(int matchId, int userId) {
        List<RoundResult> results = new ArrayList<>();

        String sql = "SELECT " +
                "rr.id AS rr_id, rr.score, rr.time_ms, rr.sent_at AS rr_sent_at, rr.selected_colors," +
                "u.id AS u_id, u.username, u.password, " +
                "r.id AS r_id, r.round_no, r.colors, r.level, r.show_ms, r.countdown_ms, r.sent_at AS r_sent_at, " +
                "m.id AS m_id, m.started_at, m.ended_at " +
                "FROM round_results rr " +
                "JOIN rounds r ON rr.round_id = r.id " +
                "JOIN matches m ON r.match_id = m.id " +
                "JOIN users u ON rr.user_id = u.id " +
                "WHERE m.id = ? AND u.id = ? " +
                "ORDER BY r.round_no ASC, rr.score DESC";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, matchId);  // match ID
            ps.setInt(2, userId);   // user ID
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                // User
                User user = new User();
                user.setId(rs.getInt("u_id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));

                // Match
                Match match = new Match();
                match.setId(rs.getInt("m_id"));
                if (rs.getTimestamp("started_at") != null)
                    match.setStartedAt(rs.getTimestamp("started_at").toLocalDateTime());
                if (rs.getTimestamp("ended_at") != null)
                    match.setEndedAt(rs.getTimestamp("ended_at").toLocalDateTime());

                // Round
                Round round = new Round();
                round.setId(rs.getInt("r_id"));
                round.setMatch(match);
                round.setColors(Json.from(rs.getString("colors"), new TypeReference<List<String>>() {
                }));
                round.setRoundNo(rs.getInt("round_no"));
                round.setLevel(rs.getString("level"));
                round.setShowMs(rs.getInt("show_ms"));
                round.setCountDownMs(rs.getInt("countdown_ms"));
                if (rs.getTimestamp("r_sent_at") != null)
                    round.setSentAt(rs.getTimestamp("r_sent_at").toLocalDateTime());

                // RoundResult
                RoundResult rr = new RoundResult();
                rr.setId(rs.getInt("rr_id"));
                rr.setRound(round);
                rr.setSelectedColors(Json.from(rs.getString("selected_colors"), new TypeReference<List<String>>() {
                }));
                rr.setUser(user);
                rr.setScore(rs.getFloat("score"));
                rr.setTimeMs(rs.getLong("time_ms"));
                if (rs.getTimestamp("rr_sent_at") != null)
                    rr.setSentAt(rs.getTimestamp("rr_sent_at").toLocalDateTime());

                results.add(rr);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }


        return results;
    }
}
