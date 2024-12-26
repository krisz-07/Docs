package org.example.namesaver;

import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.OnClose;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@ServerEndpoint("/document-websocket")
public class DocumentWebSocketServlet {
    private static final Map<String, Session> sessions = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("New WebSocket connection opened: " + session.getId());
    }

    @OnClose
    public void onClose(Session session) {
        System.out.println("WebSocket connection closed: " + session.getId());
        sessions.values().remove(session);
    }

    @OnMessage
    public String onMessage(String message, Session session) {
        System.out.println("Received message: " + message);

        try {
            if (message.contains("cursorPosition")) {
                handleCursorPosition(message, session);
            } else if (message.contains("startIndex")) {
                handleContentUpdate(message, session);
            } else if (message.contains("id")) {
                handleColorChange(message, session);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return message;
    }

    private void handleCursorPosition(String message, Session session) {
        Map<String, String> parsedMessage = parseJsonMessage(message);
        if (parsedMessage != null && parsedMessage.size() >= 4) {
            String username = parsedMessage.get("username");
            String owner = parsedMessage.get("owner");
            String documentTitle = parsedMessage.get("title");
            int cursorPosition = Integer.parseInt(parsedMessage.get("cursorPosition"));

            String broadcastMessage = buildCursorMessage(username, owner, documentTitle, cursorPosition);
            broadcastToAllSessions(session, broadcastMessage);
        } else {
            System.err.println("Invalid cursor position message: " + message);
        }
    }

    private void handleContentUpdate(String message, Session session) {
        Map<String, String> parsedMessage = parseJsonMessage(message);

        if (parsedMessage != null && parsedMessage.size() == 6) {
            String senderUsername = parsedMessage.get("username");
            String documentOwner = parsedMessage.get("owner");
            String documentTitle = parsedMessage.get("title");
            int startPosition = Integer.parseInt(parsedMessage.get("startIndex"));
            int endPosition = Integer.parseInt(parsedMessage.get("endIndex"));
            String contentUpdate = escapeSpecialCharacters(parsedMessage.get("changedContent"));

            System.out.println("Content Update: " + contentUpdate);
            sessions.put(senderUsername, session);

            String broadcastMessage = buildJsonMessage(senderUsername, documentOwner, documentTitle, startPosition, endPosition, contentUpdate);

            broadcastToAllSessions(session, broadcastMessage);
        } else {
            System.err.println("Invalid content update message: " + message);
        }
    }

    private void handleColorChange(String message, Session session) {
        Map<String, String> parsedMessage = parseJsonMessage(message);

        if (parsedMessage != null && parsedMessage.containsKey("id")) {
            String username = parsedMessage.get("username");
            String owner = parsedMessage.get("owner");
            String documentTitle = parsedMessage.get("title");
            String keyId = parsedMessage.get("id");

            System.out.println("Color Change: Key ID - " + keyId);

            String broadcastMessage = buildColorChangeMessage(username, owner, documentTitle, keyId);
            broadcastToAllSessions(session, broadcastMessage);
        } else {
            System.err.println("Invalid color change message: " + message);
        }
    }

    private String buildCursorMessage(String username, String owner, String documentTitle, int cursorPosition) {
        return String.format("{\"username\": \"%s\", \"owner\": \"%s\", \"title\": \"%s\", \"cursorPosition\": %d}",
                username, owner, documentTitle, cursorPosition);
    }

    private String buildJsonMessage(String senderUsername, String documentOwner, String documentTitle, int startPosition, int endPosition, String contentUpdate) {
        return String.format("{\"username\": \"%s\", \"owner\": \"%s\", \"title\": \"%s\", \"startIndex\": %d, \"endIndex\": %d, \"changedContent\": \"%s\"}",
                senderUsername, documentOwner, documentTitle, startPosition, endPosition, contentUpdate);
    }

    private String buildColorChangeMessage(String username, String owner, String documentTitle, String keyId) {
        return String.format("{\"username\": \"%s\", \"owner\": \"%s\", \"title\": \"%s\", \"id\": \"%s\"}",
                username, owner, documentTitle, keyId);
    }

    private void broadcastToAllSessions(Session currentSession, String message) {
        for (Map.Entry<String, Session> entry : sessions.entrySet()) {
            Session s = entry.getValue();
            if (!s.equals(currentSession) && s.isOpen()) {
                try {
                    s.getBasicRemote().sendText(message);
                    System.out.println("Broadcasted to session: " + s.getId());
                } catch (IOException e) {
                    System.err.println("Error sending message to session " + s.getId() + ": " + e.getMessage());
                }
            }
        }
    }

    private Map<String, String> parseJsonMessage(String message) {
        Map<String, String> parsedData = new ConcurrentHashMap<>();

        try {
            message = message.trim().replaceAll("^\\{", "").replaceAll("\\}$", "");

            String[] pairs = message.split(",");
            for (String pair : pairs) {
                String[] keyValue = pair.split(":");

                if (keyValue.length == 2) {
                    String key = keyValue[0].trim().replaceAll("^\"|\"$", "");
                    String value = keyValue[1].trim().replaceAll("^\"|\"$", "");

                    parsedData.put(key, value);
                }
            }

            System.out.println("Parsed message: " + parsedData);
        } catch (Exception e) {
            System.err.println("Error parsing message: " + e.getMessage());
        }

        return parsedData;
    }

    private String escapeSpecialCharacters(String content) {
        if (content == null) return "";
        return content.replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
