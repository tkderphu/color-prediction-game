package site.viosmash.db;

import site.viosmash.common.model.Session;

import java.sql.Connection;

/**
 * @author Nguyen Quang Phu
 * @since 08/09/2025
 */
public class SessionDao {
    private final Connection connection;


    public SessionDao() {
        connection = DBConnection.getInstance().getConnection();
    }

    public int insert(Session session) {
        return 1;
    }


}
