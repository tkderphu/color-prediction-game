// server/src/main/java/com/cgo/server/dao/UserDao.java
package site.viosmash.server.dao;
import java.sql.*;
import org.mindrot.jbcrypt.BCrypt;
import site.viosmash.server.Db;

public class UserDao {
    public boolean verifyLogin(String username, String password) {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement("SELECT password_hash FROM users WHERE username=?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return false;
                String hash = rs.getString(1);
                return BCrypt.checkpw(password, hash);
            }
        } catch (Exception e) {
            e.printStackTrace(); return false;
        }
    }
    public void createUser(String username, String rawPassword) throws Exception {
        String hash = BCrypt.hashpw(rawPassword, BCrypt.gensalt(10));
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO users(username,password_hash) VALUES (?,?)")) {
            ps.setString(1, username);
            ps.setString(2, hash);
            ps.executeUpdate();
        }
    }
}
