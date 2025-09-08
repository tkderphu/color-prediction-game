package site.viosmash.db;

import site.viosmash.common.model.Rank;

import java.sql.Connection;
import java.util.List;

/**
 * @author Nguyen Quang Phu
 * @since 08/09/2025
 */
public class RankDao {
    private final Connection connection;

    public RankDao() {
        connection = DBConnection.getInstance().getConnection();
    }

    public int insert(Rank rank) {
        return 1;
    }
    public int updateScore(int rankId, int score) {
        return 1;
    }
    public List<Rank> findAllBySessionId(int sessionId) {
        return null;
    }
}
