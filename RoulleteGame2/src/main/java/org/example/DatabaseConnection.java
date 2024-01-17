package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    public static Connection connect() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/roulette_game_db";
        String user = "root";
        String password = "admin";

        return DriverManager.getConnection(url, user, password);
    }
}
