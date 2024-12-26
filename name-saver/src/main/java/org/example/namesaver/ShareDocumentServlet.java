package org.example.namesaver;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.*;

public class ShareDocumentServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        response.setHeader("Access-Control-Allow-Origin", "http://localhost:4200"); // Adjust this to your frontend URL
        response.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setHeader("Access-Control-Allow-Credentials", "true");


        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }


        String title = request.getParameter("title");
        String usernameToShare = request.getParameter("usernameToShare");
        String accessType = request.getParameter("accessType");
        String currentUsername = request.getParameter("currentUsername");
        System.out.println("Access Type received: " + accessType);

        if (title == null || usernameToShare == null || accessType == null || currentUsername == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"message\":\"Missing required parameters\"}");
            return;
        }


        try (Connection conn = DatabaseConnectionServlet.getConnection()) {

            String checkUserQuery = "SELECT user_id FROM users WHERE username = ?";
            try (PreparedStatement stmt = conn.prepareStatement(checkUserQuery)) {
                stmt.setString(1, usernameToShare);
                ResultSet rs = stmt.executeQuery();
                if (!rs.next()) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    response.getWriter().write("{\"message\":\"User not found\"}");
                    return;
                }
            }

            String getDocumentIdQuery = "SELECT document_id, owner_id FROM documents WHERE title = ? AND owner_id = (SELECT user_id FROM users WHERE username = ?)";
            int documentId = -1;
            int ownerId = -1;
            try (PreparedStatement stmt = conn.prepareStatement(getDocumentIdQuery)) {
                stmt.setString(1, title);
                stmt.setString(2, currentUsername);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    documentId = rs.getInt("document_id");
                    ownerId = rs.getInt("owner_id");
                }
            }


            if (documentId == -1) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"message\":\"Document not found or you are not the owner\"}");
                return;
            }

            // Check if the document is already shared with the user
            String checkShareQuery = "SELECT id FROM shared_documents WHERE document_id = ? AND shared_user_id = (SELECT user_id FROM users WHERE username = ?)";
            try (PreparedStatement stmt = conn.prepareStatement(checkShareQuery)) {
                stmt.setInt(1, documentId);
                stmt.setString(2, usernameToShare);
                ResultSet rs = stmt.executeQuery();

                // If a record exists, update the access level
                if (rs.next()) {
                    int sharedDocId = rs.getInt("id");
                    String updateQuery = "UPDATE shared_documents SET access = ? WHERE id = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                        updateStmt.setString(1, accessType);
                        updateStmt.setInt(2, sharedDocId);
                        int rowsAffected = updateStmt.executeUpdate();

                        if (rowsAffected > 0) {
                            response.getWriter().write("{\"message\":\"Document access updated successfully!\"}");
                        } else {
                            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                            response.getWriter().write("{\"message\":\"Failed to update the document access\"}");
                        }
                    }
                } else {
                    // If the document is not already shared, insert a new record
                    String insertShareQuery = "INSERT INTO shared_documents (document_id, shared_user_id, access, owner_id) VALUES (?, (SELECT user_id FROM users WHERE username = ?), ?, ?)";
                    try (PreparedStatement stmtInsert = conn.prepareStatement(insertShareQuery)) {
                        stmtInsert.setInt(1, documentId);
                        stmtInsert.setString(2, usernameToShare);
                        stmtInsert.setString(3, accessType);
                        stmtInsert.setInt(4, ownerId);
                        int rowsAffected = stmtInsert.executeUpdate();

                        if (rowsAffected > 0) {
                            response.getWriter().write("{\"message\":\"Document shared successfully!\"}");
                        } else {
                            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                            response.getWriter().write("{\"message\":\"Failed to share the document\"}");
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"message\":\"Error occurred while sharing the document\"}");
        }
    }
}
