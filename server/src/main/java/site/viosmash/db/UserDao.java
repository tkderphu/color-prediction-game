package site.viosmash.db;

import site.viosmash.common.User;

import java.sql.Connection;

public class UserDao {

    private final Connection connection;

    public UserDao(Connection connection) {
        this.connection = connection;
    }

    public User findByUsernameAndPassword(String username, String password) {
        return null;
    }
    public void save(User user) {

    }
}
