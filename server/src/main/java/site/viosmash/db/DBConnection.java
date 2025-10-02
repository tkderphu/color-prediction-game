package site.viosmash.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static volatile DBConnection dbConn;
    private Connection connection;

    // Thông tin DB
    private final String DB_USERNAME = "root";       // thay bằng user của bạn
    private final String DB_PASSWORD = "";     // thay bằng password của bạn
    private final String DB_URL = "jdbc:mysql://localhost:3306/game?useSSL=false&serverTimezone=UTC";

    private DBConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
            System.out.println("Database connected successfully!");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to connect to database", e);
        }
    }

    public static DBConnection getInstance() {
        if (dbConn == null) {
            synchronized (DBConnection.class) {
                if (dbConn == null) {
                    dbConn = new DBConnection();
                }
            }
        }
        return dbConn;
    }

    public Connection getConnection() {
        return connection;
    }
}
