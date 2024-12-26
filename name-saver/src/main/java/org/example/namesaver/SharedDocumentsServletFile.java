package org.example.namesaver;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.*;

public class SharedDocumentsServletFile extends HttpServlet {

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:4200");
        response.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Allow CORS for actual requests
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:4200");
        response.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String username = request.getPathInfo();
        username = username.substring(1);

        try (Connection connection = DatabaseConnectionServlet.getConnection()) {
            String getSharedDocumentsSql = "SELECT title, ownername, access FROM shared_user_documents WHERE username=?";
            try (PreparedStatement stmt = connection.prepareStatement(getSharedDocumentsSql)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();

                StringBuilder responseJson = new StringBuilder("[");
                boolean first = true;

                while (rs.next()) {
                    if (!first) {
                        responseJson.append(",");
                    } else {
                        first = false;
                    }

                    String title = rs.getString("title");
                    String ownerUsername = rs.getString("ownername");
                    String accessType = rs.getString("access");

                    responseJson.append("{");
                    responseJson.append("\"title\":\"").append(title).append("\",");
                    responseJson.append("\"owner\":\"").append(ownerUsername).append("\",");
                    responseJson.append("\"accessType\":\"").append(accessType).append("\"");
                    responseJson.append("}");
                }

                responseJson.append("]");
                response.getWriter().write(responseJson.toString());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().write("{\"message\":\"An error occurred while fetching shared documents\"}");
        }
    }

}
