// server/src/main/java/com/cgo/server/dao/UserDao.java
package site.viosmash.server.dao;

import java.sql.*;
import site.viosmash.common.User;

public class UserDao extends Dao{
    public User verifyLogin(User user) {
        try (PreparedStatement ps = conn.prepareStatement("SELECT id, username, password FROM users WHERE username=?")) {
            ps.setString(1, user.getUsername());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                String password = rs.getString(3);
                int id = rs.getInt(1);
                String username = rs.getString(2);

                // Kiểm tra password trực tiếp (không mã hóa)
                if (user.getPassword().equals(password)) {
                    User user1 =new User();
                    user1.setPassword(password);
                    user1.setUsername(username);
                    user1.setId(id);

                    return user1;
                }
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean isUsernameExists(String username) {
        try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM users WHERE username=?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    public boolean register(User user) {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO users (username, password) VALUES (?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {

            // Lưu password trực tiếp (không mã hóa)
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}