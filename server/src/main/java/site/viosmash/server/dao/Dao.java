// server/src/main/java/com/cgo/server/Dao.java
package site.viosmash.server.dao;
import java.sql.*;

public class Dao {
    private static String url = "jdbc:mysql://localhost:3306/cgo?useSSL=false&serverTimezone=UTC";
    private static String user = "root";
    private static String pass = "root";

    protected Connection conn;

    public Dao() {
        try {
            conn = DriverManager.getConnection(url, user, pass);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


}
