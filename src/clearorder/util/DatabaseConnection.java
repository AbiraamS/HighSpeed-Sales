package clearorder.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // MySQL Configuration
    private static final String MYSQL_URL = "jdbc:mysql://3.69.96.96:80/db1";
    private static final String MYSQL_USER = "db1";
    private static final String MYSQL_PASSWORD = "!db1.wip25?SS6";

    static {
        // Load the MySQL database driver
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("MySQL Driver loaded successfully");
        } catch (ClassNotFoundException e) {
            System.err.println("ERROR: MySQL Driver not found. Make sure mysql-connector-j is in your classpath.");
            e.printStackTrace();
        }
    }

    public static Connection getConnection() {
        try {
            Connection conn = DriverManager.getConnection(MYSQL_URL, MYSQL_USER, MYSQL_PASSWORD);
            System.out.println("Connected to MySQL database successfully");
            return conn;
        } catch (SQLException e) {
            System.err.println("Warning: Database connection failed.");
            System.err.println("Database error: " + e.getMessage());
            System.err.println("Connection URL: " + MYSQL_URL);
            return null;
        }
    }

    // Optionally keep this for explicit closing, but not needed anymore
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}