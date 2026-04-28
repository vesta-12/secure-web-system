package org.example.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.Database;
import org.example.SessionManager;
import org.example.util.HttpUtils;
import org.example.util.TemplateUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class HomeHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws java.io.IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            HttpUtils.sendHtml(exchange, "Method not allowed");
            return;
        }

        String sessionId = HttpUtils.getCookie(exchange, "SESSIONID");
        String username = sessionId == null ? null : SessionManager.getUsername(sessionId);

        String html = TemplateUtils.loadHtml("index.html");

        html = html.replace("{{username}}", username == null ? "Guest" : HttpUtils.escapeHtml(username));
        html = html.replace("{{comments}}", loadComments(username));

        HttpUtils.sendHtml(exchange, html);
    }

    private String loadComments(String currentUsername) {
        StringBuilder result = new StringBuilder();

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery(
                    "SELECT id, username, text, created_at FROM comments ORDER BY id DESC"
            );

            while (rs.next()) {
                int commentId = rs.getInt("id");
                String commentUsername = rs.getString("username");

                result.append("<div style='border:1px solid #ccc; padding:10px; margin:10px 0;'>")
                        .append("<b>")
                        .append(HttpUtils.escapeHtml(commentUsername))
                        .append("</b>")
                        .append("<p>")
                        .append(HttpUtils.escapeHtml(rs.getString("text")))
                        .append("</p>")
                        .append("<small>")
                        .append(HttpUtils.escapeHtml(rs.getString("created_at")))
                        .append("</small>");

                if (currentUsername != null && currentUsername.equals(commentUsername)) {
                    result.append("""
                            <form action="/delete-comment" method="post" style="margin-top:10px;">
                                <input type="hidden" name="id" value="%d">
                                <button type="submit">Delete</button>
                            </form>
                            """.formatted(commentId));
                }

                result.append("</div>");
            }

        } catch (Exception e) {
            result.append("<p>Error loading comments</p>");
        }

        return result.toString();
    }
}