package org.example.namesaver;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.*;

public class SharedDocumentsServlet extends HttpServlet {

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:4200");
        response.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Allow CORS for actual requests
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:4200");
        response.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");

        String username = request.getPathInfo() != null ? request.getPathInfo().substring(1) : null;

        if (username == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Username is required");
            return;
        }

        try (Connection connection = DatabaseConnectionServlet.getConnection()) {

            // Query to get user_id based on the username
            String getUserIdSql = "SELECT user_id FROM users WHERE username = ?";
            int userId = -1;

            try (PreparedStatement stmt = connection.prepareStatement(getUserIdSql)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    userId = rs.getInt("user_id");
                    System.out.println("Found user_id: " + userId);
                } else {
                    response.getWriter().write("{\"message\": \"User not found\"}");
                    return;
                }
            }

            String getSharedDocumentsSql = "SELECT d.title, d.content, u.username AS owner_username, sd.access " +
                    "FROM documents d " +
                    "JOIN shared_documents sd ON d.document_id = sd.document_id " +
                    "JOIN users u ON d.owner_id = u.user_id " +
                    "WHERE sd.shared_user_id = ?";

            try (PreparedStatement stmt = connection.prepareStatement(getSharedDocumentsSql)) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();

                StringBuilder jsonResponse = new StringBuilder();
                jsonResponse.append("[");

                boolean firstDocument = true;

                while (rs.next()) {
                    String title = rs.getString("title");
                    String content = rs.getString("content");
                    String ownerUsername = rs.getString("owner_username");
                    String accessType = rs.getString("access");


                    title = escapeJson(title);
                    content = escapeJson(content);

                    if (!firstDocument) {
                        jsonResponse.append(",");
                    }

                    jsonResponse.append("{");
                    jsonResponse.append("\"title\":\"").append(title).append("\",");
                    jsonResponse.append("\"content\":\"").append(content).append("\",");
                    jsonResponse.append("\"owner\":\"").append(ownerUsername).append("\",");
                    jsonResponse.append("\"accessType\":\"").append(accessType).append("\"");
                    jsonResponse.append("}");

                    firstDocument = false;
                }

                if (jsonResponse.length() == 1) {
                    jsonResponse.append("{\"message\":\"No shared documents found\"}");
                }

                jsonResponse.append("]");
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(jsonResponse.toString());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        }
    }


    private String escapeJson(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
