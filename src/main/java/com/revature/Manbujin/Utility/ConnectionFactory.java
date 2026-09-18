package com.revature.Manbujin.Utility;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ConnectionFactory {

    private static final String DEFAULT_URL = "jdbc:sqlite:./data/bank.db";
    private static final String url = finalUrl();

    /** 
     * Method to check if there is an environment variable for the database
     * If not, reads the database in data/bank.db
     */
    private static String finalUrl() {
        String fromEnv = System.getenv("DATABASE-PATH");
        if (fromEnv == null || fromEnv.isBlank()) {
            return DEFAULT_URL;
        }
        return fromEnv;
    }

    public static Connection getAutoCommitConnect() throws SQLException {
        Connection connection = DriverManager.getConnection(url);
        configureForeignKeyEnforcement(connection);
        return connection;
    }

    /**
     * Manual connection of the database
     */
    public static Connection getManualCommitConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(url);
        configureForeignKeyEnforcement(connection);
        connection.setAutoCommit(false);
        return connection;
    }


    public static void configureForeignKeyEnforcement(Connection connection) throws SQLException {
        try(Statement statement = connection.createStatement()){
            String sql = "PRAGMA foreign_keys = true";
            statement.execute(sql);
        }
    }

}
