package org.example.namesaver;

import java.io.*;
import java.sql.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

public class LoginServlet extends HttpServlet {

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

        response.setContentType("text/plain");

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        try (PrintWriter out = response.getWriter()) {
            try (Connection conn = DatabaseConnectionServlet.getConnection()) {
                String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, username);
                    stmt.setString(2, password);
                    ResultSet rs = stmt.executeQuery();

                    if (rs.next()) {
                        String s = Base64Util.encode(username);
                        Cookie usernameCookie = new Cookie("username", s);
                        usernameCookie.setMaxAge(24 * 60 * 60);
                        usernameCookie.setPath("/");
                        usernameCookie.setHttpOnly(true);
                        usernameCookie.setSecure(false);

                        response.addCookie(usernameCookie);

                    } else {
                        out.println("Error: Invalid username or password.");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                out.println("Error: Database error occurred.");
            }
            out.println("success");
        }
    }
}
