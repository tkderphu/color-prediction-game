package site.viosmash.server.dao;

import site.viosmash.common.Json;
import site.viosmash.common.Round;

import java.sql.*;

/**
 * @author Nguyen Quang Phu
 * @since 05/11/2025
 */
public class RoundDao extends Dao{
    public int createRound(Round round) throws Exception {
        try (
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO rounds(match_id, round_no, level, colors, show_ms, countdown_ms, sent_at) " +
                             "VALUES (?,?,?,?,?,?,?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, round.getMatch().getId());
            ps.setInt(2, round.getRoundNo());
            ps.setString(3, round.getLevel());
            ps.setString(4, Json.to(round.getColors()));
            ps.setInt(5, round.getShowMs());
            ps.setInt(6, round.getCountDownMs());
            ps.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { rs.next(); return rs.getInt(1); }
        }
    }
}
