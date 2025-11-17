package site.viosmash.server.dao;

import site.viosmash.common.MatchPlayer;
import site.viosmash.common.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Nguyen Quang Phu
 * @since 05/11/2025
 */
public class MatchPlayerDao extends Dao{

    public void savePlayer(MatchPlayer matchPlayer) throws Exception {
        try (
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO match_players(match_id, user_id, total_score, total_time_ms) VALUES (?,?, 0, 0)")) {
            ps.setInt(1, matchPlayer.getMatch().getId());
            ps.setInt(2, matchPlayer.getUser().getId());
            ps.executeUpdate();
        }
    }

    public void updatePlayerTotals(MatchPlayer matchPlayer) throws Exception {
        try (
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE match_players SET total_score = total_score + ?, " +
                             "total_time_ms = total_time_ms + ? WHERE match_id=? AND user_id=?")) {
            ps.setDouble(1, matchPlayer.getTotalScore());
            ps.setLong(2, matchPlayer.getTotalTimeMs());
            ps.setInt(3, matchPlayer.getMatch().getId());
            ps.setInt(4, matchPlayer.getUser().getId());
            ps.executeUpdate();
        }
    }


    public List<MatchPlayer> finalRanking(int matchId) throws Exception {
        try (
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT u.username, mp.total_score, mp.total_time_ms " +
                             "FROM match_players mp " +
                             "JOIN users u ON mp.user_id = u.id " +
                             "WHERE mp.match_id = ? " +
                             "ORDER BY mp.total_score DESC, mp.total_time_ms ASC")) {
            ps.setInt(1, matchId);
            try (ResultSet rs = ps.executeQuery()) {
                List<MatchPlayer> out = new ArrayList<>();
                while (rs.next()) {
                    MatchPlayer player = new MatchPlayer();
                    player.setTotalScore( rs.getFloat(2));
                    player.setTotalTimeMs( rs.getLong(3));

                    User user = new User();
                    user.setUsername(rs.getString(1));
                    player.setUser(user);

                    out.add(player);
                }
                return out;
            }
        }
    }
}
