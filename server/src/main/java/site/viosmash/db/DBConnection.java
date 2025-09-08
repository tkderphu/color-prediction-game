package site.viosmash.db;

import java.sql.Connection;

public class DBConnection {
    private static volatile DBConnection dbConn;
    private Connection connection;

    private final String DB_USERNAME = "";
    private final String DB_PASSWORD = "";
    private final String DB_URL = "";


    private DBConnection() {
        //connect to mysql
    }

    public static DBConnection getInstance() {
        if(dbConn == null) {
            dbConn = new DBConnection();
        }
        return dbConn;
    }

    public Connection getConnection() {
        return connection;
    }

}
