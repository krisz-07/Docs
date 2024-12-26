package org.example.namesaver;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.*;
import java.io.FileWriter;
import java.io.IOException;
public class CreateDocumentServletFile extends HttpServlet {

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
            try (Connection con = DatabaseConnectionServlet.getConnection()) {
                String checkAndInsertQuery = "INSERT INTO user_documents (title, username) " +
                        "SELECT ?, ? " +
                        "WHERE NOT EXISTS (SELECT 1 FROM user_documents WHERE title = ? AND username = ?)";

                try (PreparedStatement stmt = con.prepareStatement(checkAndInsertQuery)) {
                    stmt.setString(1, title);
                    stmt.setString(2, username);
                    stmt.setString(3, title);
                    stmt.setString(4, username);

                    int r = stmt.executeUpdate();
                    if (r > 0) {
                        create(username, title, content);
                        out.println("Success");
                    } else {
                        out.println("Document with the same title already exists for this user: " + username);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                out.println("Error connecting to the database.");
            }
        }
    }

    private void create(String username, String title, String content) throws IOException {
        File x = new File(MAIN_FOLDER_PATH + username);
        File a = new File(x, title + ".txt");
        FileWriter writer = new FileWriter(a);
        writer.write(content);
        writer.close();
    }
}
