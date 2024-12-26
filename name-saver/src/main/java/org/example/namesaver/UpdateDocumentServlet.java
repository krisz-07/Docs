package org.example.namesaver;

import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

public class UpdateDocumentServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setHeader("Access-Control-Allow-Origin", "http://localhost:4200");
        response.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setHeader("Access-Control-Allow-Credentials", "true");


        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        response.setContentType("application/json");
        try (PrintWriter out = response.getWriter()) {
            // Retrieve request parameters
            String title = request.getParameter("title");
            String content = request.getParameter("content");
            String username = request.getParameter("username");

            if (title == null || title.isEmpty() || content == null || content.isEmpty() || username == null || username.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"message\": \"Missing required parameters.\"}");
                return;
            }

            try (Connection connection = DatabaseConnectionServlet.getConnection()) {
                String sql = "UPDATE documents SET content = ? WHERE title = ? AND owner_id = (SELECT user_id FROM users WHERE username = ?)";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, content);
                    statement.setString(2, title);
                    statement.setString(3, username);

                    int rowsUpdated = statement.executeUpdate();

                    if (rowsUpdated > 0) {
                        response.setStatus(HttpServletResponse.SC_OK);
                        out.write("{\"message\": \"Document updated successfully.\"}");
                        System.out.println("Document updated successfully.");
                    } else {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        out.write("{\"message\": \"Document not found or user does not have access.\"}");
                        System.out.println("Document not found or user does not have access.");
                    }
                }
            } catch (SQLException e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.write("{\"message\": \"An error occurred while updating the document.\"}");
                System.err.println("Error while updating document:");
                e.printStackTrace();
            }
        }
    }
}
