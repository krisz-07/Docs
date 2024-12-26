package org.example.namesaver;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DocumentServletFile extends HttpServlet {

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:4200");
        response.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:4200");
        response.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String username = request.getPathInfo();
        username = username.substring(1);

        StringBuilder json = new StringBuilder("[");
        boolean firstDocument = true;

        try (Connection con = DatabaseConnectionServlet.getConnection()) {
            PreparedStatement stmt = con.prepareStatement("SELECT title FROM user_documents WHERE username = ?");
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                if (!firstDocument) {
                    json.append(",");
                }

                String title = rs.getString("title");
                json.append("{");
                json.append("\"title\":\"").append(title).append("\"");
                json.append("}");
                firstDocument = false;
            }
        } catch (Exception e) {
            response.getWriter().write("{\"message\":\"An error occurred while fetching documents\"}");
            return;
        }

        json.append("]");
        response.getWriter().write(json.toString());
    }

}
