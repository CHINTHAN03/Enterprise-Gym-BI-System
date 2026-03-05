package core;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/GymEnterpriseDB";
    private static final String USER = "root";
    private static final String PASS = "c1u2t3e4"; //Your password

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (SQLException e) {
            System.err.println("Fatal Error: Could not connect to GymEnterpriseDB.");
            e.printStackTrace();
            return null;
        }
    }
}