package org.example.namesaver;

import java.io.*;
import java.sql.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

public class SignupServlet extends HttpServlet {

    private static final String MAIN_FOLDER_PATH = "/home/madhu-pt7750/Users/";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:4200");
        response.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setHeader("Access-Control-Allow-Credentials", "true");

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        try (PrintWriter out = response.getWriter()) {

            if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
                out.println("Error: Username and password are required.");
                return;
            }

            try (Connection conn = DatabaseConnectionServlet.getConnection()) {
                String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, username);
                    stmt.setString(2, password);
                    stmt.executeUpdate();


                    File userFolder = new File(MAIN_FOLDER_PATH + username);
                    userFolder.mkdirs();
                }
            } catch (SQLIntegrityConstraintViolationException e) {
                out.println("Error: Username already exists.");
                return;
            } catch (SQLException e) {
                out.println("Error: Unable to save user.");
                return;
            }

            response.setStatus(HttpServletResponse.SC_OK);
            out.println("Success");
        }
    }
}
