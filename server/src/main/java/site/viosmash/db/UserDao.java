package site.viosmash.db;

import site.viosmash.common.model.User;

import java.sql.Connection;
import java.util.List;

public class UserDao {

    private final Connection connection;

    public UserDao() {
        this.connection = DBConnection.getInstance().getConnection();
    }

    public User findById(int id) {
        return null;
    }

    public User findByUsernameAndPassword(String username, String password) {
        return null;
    }

    public void insert(User user) {

    }

    public List<User> findAll() {
        return null;
    }

    public int updateStatusOnline(int userId, Boolean online) {
        return 1;
    }

}
