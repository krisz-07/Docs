package org.example.namesaver;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.*;

public class DocumentServlet extends HttpServlet {

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Allow CORS for preflight requests
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

        // Get the username from the request URL
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
                    userId = rs.getInt("user_id");  // Use 'user_id' as the column name
                    System.out.println("Found user_id: " + userId);  // Debugging output
                } else {
                    response.getWriter().write("{\"message\": \"User not found\"}");
                    return;
                }
            }

            String getDocumentsSql = "SELECT title, content FROM documents WHERE owner_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(getDocumentsSql)) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();

                StringBuilder jsonResponse = new StringBuilder();
                jsonResponse.append("[");

                boolean firstDocument = true;

                while (rs.next()) {
                    String title = rs.getString("title");
                    String content = rs.getString("content");

                    // Escape special characters to ensure the response is valid JSON
                    title = escapeJson(title);
                    content = escapeJson(content);

                    if (!firstDocument) {
                        jsonResponse.append(",");
                    }

                    jsonResponse.append("{");
                    jsonResponse.append("\"title\":\"").append(title).append("\",");
                    jsonResponse.append("\"content\":\"").append(content).append("\"");
                    jsonResponse.append("}");

                    firstDocument = false;
                }

                if (jsonResponse.length() == 1) {
                    jsonResponse.append("{\"message\":\"No documents found\"}");
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

    /**
     * Escapes special characters in a JSON string to ensure it is valid.
     */
    private String escapeJson(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\", "\\\\")  // Escape backslashes
                .replace("\"", "\\\"")  // Escape double quotes
                .replace("\n", "\\n")   // Escape newline
                .replace("\r", "\\r")   // Escape carriage return
                .replace("\t", "\\t");  // Escape tab
    }
}
