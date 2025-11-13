// server/src/main/java/com/cgo/server/dao/MatchDao.java
package site.viosmash.server.dao;

import site.viosmash.common.Match;
import site.viosmash.common.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class MatchDao extends Dao{
    public int createMatch(Match match) throws Exception {
        try (
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO matches(room_owner_id, started_at) VALUES (?,?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, match.getRoomOwner().getId());
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { rs.next(); return rs.getInt(1); }
        }
    }
    public void endMatch(Match match) throws Exception {
        try (
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE matches SET ended_at=? WHERE id=?")) {
            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(2, match.getId());
            ps.executeUpdate();
        }
    }


    
    public List<Match> getListMatchPlayed(int userId) throws SQLException {
        try (
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT m.id as id, m.started_at as started_at, m.ended_at as ended_at, u.username as username FROM users u INNER JOIN match_players mp ON u.id = mp.user_id INNER JOIN matches m " +
                             "ON mp.match_id = m.id " +
                             "WHERE u.id = ? ORDER BY started_at DESC")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Match> out = new ArrayList<>();
                while (rs.next()) {
                    Match match = new Match();
                    match.setId(rs.getInt(1));
                    match.setStartedAt(rs.getTimestamp(2).toLocalDateTime());
                    match.setEndedAt(rs.getTimestamp(3) != null ? rs.getTimestamp(3).toLocalDateTime() : null);
                    User owner = new User();
                    owner.setUsername(rs.getString("username"));
                    match.setRoomOwner(owner);
                    out.add(match);
                }
                return out;
            }
        }
    }
}
