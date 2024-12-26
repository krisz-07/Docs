package org.example.namesaver;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.*;

public class ShareDocumentServletFile extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/plain");

        response.setHeader("Access-Control-Allow-Origin", "http://localhost:4200");
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
        String ownerUsername = request.getParameter("currentUsername");

        PrintWriter out = response.getWriter();

        try (Connection conn = DatabaseConnectionServlet.getConnection()) {
            String checkUserQuery = "SELECT 1 FROM users WHERE username = ?";
            try (PreparedStatement stmt = conn.prepareStatement(checkUserQuery)) {
                stmt.setString(1, usernameToShare);
                ResultSet rs = stmt.executeQuery();
                if (!rs.next()) {
                    out.println("User not found.");
                    return;
                }
            }

            String getDocIdQuery = "SELECT doc_id FROM user_documents WHERE title = ? AND username = ?";
            int docId = -1;
            try (PreparedStatement stmt = conn.prepareStatement(getDocIdQuery)) {
                stmt.setString(1, title);
                stmt.setString(2, ownerUsername);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    docId = rs.getInt("doc_id");
                }
            }

            String checkShareQuery = "SELECT 1 FROM shared_user_documents WHERE doc_id = ? AND username = ? AND ownername = ?";
            try (PreparedStatement stmt = conn.prepareStatement(checkShareQuery)) {
                stmt.setInt(1, docId);
                stmt.setString(2, usernameToShare);
                stmt.setString(3, ownerUsername);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    String updateAccessQuery = "UPDATE shared_user_documents SET access = ? WHERE doc_id = ? AND username = ? AND ownername = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateAccessQuery)) {
                        updateStmt.setString(1, accessType);
                        updateStmt.setInt(2, docId);
                        updateStmt.setString(3, usernameToShare);
                        updateStmt.setString(4, ownerUsername);
                        int rowsAffected = updateStmt.executeUpdate();

                        if (rowsAffected > 0) {
                            out.println("Document access updated successfully.");
                        } else {
                            out.println("Failed to update document access.");
                        }
                    }
                } else {

                    String insertShareQuery = "INSERT INTO shared_user_documents (doc_id, title, username, ownername, access) VALUES (?, ?, ?, ?, ?) ";
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertShareQuery)) {
                        insertStmt.setInt(1, docId);
                        insertStmt.setString(2, title);
                        insertStmt.setString(3, usernameToShare);
                        insertStmt.setString(4, ownerUsername);
                        insertStmt.setString(5, accessType);
                        int rowsAffected = insertStmt.executeUpdate();

                        if (rowsAffected > 0) {
                            out.println("Document shared successfully.");
                        } else {
                            out.println("Failed to share the document.");
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            out.println("An error occurred while sharing the document.");
        }
    }
}
