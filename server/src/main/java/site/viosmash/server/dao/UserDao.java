// server/src/main/java/com/cgo/server/dao/UserDao.java
package site.viosmash.server.dao;
import java.sql.*;
import org.mindrot.jbcrypt.BCrypt;
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
                if(BCrypt.checkpw(user.getPassword(), password)) {
                    return null;
                }

                User user1 =new User();
                user1.setPassword(password);
                user1.setUsername(username);
                user1.setId(id);

                return user1;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
