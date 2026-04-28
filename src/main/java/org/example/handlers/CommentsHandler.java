package org.example.handlers;

import org.example.Database;
import org.example.SessionManager;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class CommentsHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            showCommentsPage(exchange);
        } else if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            handleAddComment(exchange);
        }
    }

    private void showCommentsPage(HttpExchange exchange) throws IOException {
        Integer userId = SessionManager.getUserIdFromSession(exchange);

        StringBuilder commentsHtml = new StringBuilder();

        try (Connection connection = Database.getConnection();
             Statement statement = connection.createStatement()) {

            ResultSet rs = statement.executeQuery("SELECT * FROM comments");

            while (rs.next()) {
                int id = rs.getInt("id");
                int uid = rs.getInt("user_id");
                String content = rs.getString("content");

                // INSECURE: нет escaping → XSS
                commentsHtml.append("<p>")
                        .append("User ").append(uid)
                        .append(": ")
                        .append(content)
                        .append(" ")
                        .append("<a href='/delete?id=").append(id).append("'>Delete</a>")
                        .append("</p>");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        String form = "";

        if (userId != null) {
            form = """
                    <form method="POST" action="/comments">
                        <textarea name="content"></textarea><br>
                        <button type="submit">Add Comment</button>
                    </form>
                    """;
        } else {
            form = "<p>You must <a href='/login'>login</a> to post comments.</p>";
        }

        String response = """
                <html>
                <body>
                    <h1>Comments</h1>
                """
                + commentsHtml +
                form +
                """
                    <p><a href="/">Home</a></p>
                </body>
                </html>
                """;

        sendResponse(exchange, response);
    }

    private void handleAddComment(HttpExchange exchange) throws IOException {
        Integer userId = SessionManager.getUserIdFromSession(exchange);

        if (userId == null) {
            sendResponse(exchange, "<h1>Unauthorized</h1>");
            return;
        }

        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> formData = parseFormData(body);

        String content = formData.get("content");

        try (Connection connection = Database.getConnection();
             Statement statement = connection.createStatement()) {

            // INSECURE: SQL Injection possible
            String sql = "INSERT INTO comments (user_id, content) VALUES ("
                    + userId + ", '"
                    + content + "')";

            statement.executeUpdate(sql);

        } catch (Exception e) {
            e.printStackTrace();
        }

        // redirect
        exchange.getResponseHeaders().add("Location", "/comments");
        exchange.sendResponseHeaders(302, -1);
    }

    private Map<String, String> parseFormData(String body) {
        Map<String, String> result = new HashMap<>();

        String[] pairs = body.split("&");

        for (String pair : pairs) {
            String[] keyValue = pair.split("=");

            if (keyValue.length == 2) {
                String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
                String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                result.put(key, value);
            }
        }

        return result;
    }

    private void sendResponse(HttpExchange exchange, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);

        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }
}