package org.example.namesaver;
import jakarta.servlet.http.*;
import java.io.*;


public class UpdateDocumentServletFile extends HttpServlet {

    private static final String MAIN_FOLDER_PATH = "/home/madhu-pt7750/Users/";

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:4200");
        response.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setHeader("Access-Control-Allow-Origin", "http://localhost:4200");
        response.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");

        response.setContentType("text/html");
        try (PrintWriter out = response.getWriter()) {
            String title = request.getParameter("title");
            String content = request.getParameter("content");
            String username = request.getParameter("username");

            try {
                File userFolder = new File(MAIN_FOLDER_PATH + username);
                File documentFile = new File(userFolder, title + ".txt");
                FileWriter writer = new FileWriter(documentFile);
                writer.write(content);
                writer.close();

                out.println("success");
            } catch (IOException e) {
                e.printStackTrace();
                out.println("error in database");
            }
        }
    }
}
