package org.example.namesaver;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.util.Scanner;

public class DocumentContentServlet extends HttpServlet {

    private static final String MAIN_FOLDER_PATH = "/home/madhu-pt7750/Users/";

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Allow CORS preflight requests
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

        String p = request.getPathInfo();
        String[] a = p.split("/");
        String username = a[1];
        String title = a[2];

        File file = new File(MAIN_FOLDER_PATH + username + "/" + title + ".txt");

        // Check if the 'download' parameter is present in the URL
        String downloadParam = request.getParameter("download");

        try (Scanner scanner = new Scanner(file)) {
            StringBuilder content = new StringBuilder();
            while (scanner.hasNextLine()) {
                content.append(scanner.nextLine()).append("\n");
            }

            // If download=true, trigger the download behavior
            if ("true".equalsIgnoreCase(downloadParam)) {
                response.setHeader("Content-Disposition", "attachment; filename=\"" + title + ".txt\"");
                response.setContentType("application/octet-stream"); // For downloading the file
            } else {
                response.setContentType("text/plain"); // For viewing the content
            }

            // Write the content to the response (either to view or to download)
            response.getWriter().write(content.toString());

        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("An error occurred while reading the document");
            e.printStackTrace();
        }
    }
}
