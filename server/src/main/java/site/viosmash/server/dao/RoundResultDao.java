package site.viosmash.server.dao;

import site.viosmash.common.Json;
import site.viosmash.common.RoundResult;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;

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

}
