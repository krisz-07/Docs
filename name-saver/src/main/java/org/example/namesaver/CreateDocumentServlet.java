package org.example.namesaver;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.*;

public class CreateDocumentServlet extends HttpServlet {

    private static final String MAIN_FOLDER_PATH = "/home/madhu-pt7750/Users/";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.setHeader("Access-Control-Allow-Origin", "http://localhost:4200");
        response.setHeader("Access-Control-Allow-Methods", "POST");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setHeader("Access-Control-Allow-Credentials", "true");

        String title = request.getParameter("title");
        String content = request.getParameter("content");
        String username = request.getParameter("username");

        try (PrintWriter out = response.getWriter()) {
            if (title == null || title.trim().isEmpty() || content == null || content.trim().isEmpty() || username == null || username.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println("All fields (title, content, username) are required.");
                return;
            }

            try (Connection conn = DatabaseConnectionServlet.getConnection()) {
                if (conn == null) {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.println("Error: Unable to establish database connection.");
                    return;
                }

                String checkAndInsertQuery = """
                        INSERT INTO documents (title, content, owner_id)
                        SELECT ?, ?, user_id
                        FROM users
                        WHERE username = ?
                          AND NOT EXISTS (
                              SELECT 1
                              FROM documents
                              WHERE title = ?
                                AND owner_id = (SELECT user_id FROM users WHERE username = ?)
                          )
                        """;

                try (PreparedStatement stmt = conn.prepareStatement(checkAndInsertQuery)) {
                    stmt.setString(1, title);
                    stmt.setString(2, content);
                    stmt.setString(3, username);
                    stmt.setString(4, title);
                    stmt.setString(5, username);

                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {

                        saveDocumentToFile(username, title, content);

                        response.setStatus(HttpServletResponse.SC_CREATED);
                        out.println("Document creation successful.");
                    } else {
                        response.setStatus(HttpServletResponse.SC_CONFLICT);
                        out.println("Document with the same title already exists for this user " + username);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.println("Error connecting to the database.");
            }
        }
    }

    private void saveDocumentToFile(String username, String title, String content) throws IOException {
        File userFolder = new File(MAIN_FOLDER_PATH + username);
        if (!userFolder.exists() && !userFolder.mkdirs()) {
            throw new IOException("Unable to create user folder: " + userFolder.getAbsolutePath());
        }

        File documentFile = new File(userFolder, title + ".txt");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(documentFile))) {
            writer.write(content);
            System.out.println("Document saved to file: " + documentFile.getAbsolutePath());
        }
    }
}
