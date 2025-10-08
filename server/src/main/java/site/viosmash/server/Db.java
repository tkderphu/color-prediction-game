// server/src/main/java/com/cgo/server/Db.java
package site.viosmash.server;

import java.sql.*;

public class Db {
    private static String url = "jdbc:mysql://localhost:3306/cgo?useSSL=false&serverTimezone=UTC";
    private static String user = "root";
    private static String pass = "sbqpro2004";

    public static Connection get() throws SQLException {
        return DriverManager.getConnection(url, user, pass);
    }
}
