package site.viosmash.db;

import site.viosmash.common.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    // ... (giữ nguyên code cũ)

    // THÊM: Update last_login
    public void updateLastLogin(int userId) {
        String sql = "UPDATE users SET last_login = CURRENT_TIMESTAMP WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
            System.out.println("Updated last_login for user ID " + userId);
        } catch (SQLException e) {
            System.err.println("Update last_login error: " + e.getMessage());
        }
    }

    // SỬA: findAllOnline() với filter real-time (last_login trong 1 phút)
    public List<User> findAllOnline() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE is_online = 1 AND status = 'free' AND last_login > NOW() - INTERVAL 1 MINUTE";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                User user = mapFromResultSet(rs);
                users.add(user);
            }
            System.out.println("UserDao: Found " + users.size() + " real-time online users.");
        } catch (SQLException e) {
            System.err.println("UserDao query error: " + e.getMessage());
        }
        return users;
    }

    private User mapFromResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setStatus(rs.getString("status"));  // Lấy enum 'free' từ DB
        // Set các field khác nếu cần: email, full_name, etc.
        return user;
    }


    // Set online status
    public void setOnline(int userId, boolean online) {
        String sql = "UPDATE users SET is_online = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setBoolean(1, online);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
