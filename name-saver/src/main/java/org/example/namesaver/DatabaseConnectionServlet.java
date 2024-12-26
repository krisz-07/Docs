package org.example.namesaver;

import java.io.*;
import java.sql.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

public class DatabaseConnectionServlet extends HttpServlet {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/doc_sharing";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "password";


    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("Connecting to database...");
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            throw new SQLException("Failed to establish connection", e);
        }
    }

}
