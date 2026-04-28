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

        if (username == null) {
            html = html.replace("{{username}}", "Guest");
        } else {
            html = html.replace("{{username}}", username);
        }

        html = html.replace("{{comments}}", loadComments());

        HttpUtils.sendHtml(exchange, html);
    }

    private String loadComments() {
        StringBuilder result = new StringBuilder();

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery(
                    "SELECT username, text, created_at FROM comments ORDER BY id DESC"
            );

            while (rs.next()) {
                result.append("<div style='border:1px solid #ccc; padding:10px; margin:10px 0;'>")
                        .append("<b>")
                        .append(rs.getString("username"))
                        .append("</b>")
                        .append("<p>")
                        .append(rs.getString("text"))
                        .append("</p>")
                        .append("<small>")
                        .append(rs.getString("created_at"))
                        .append("</small>")
                        .append("</div>");
            }

        } catch (Exception e) {
            result.append("<p>Error loading comments</p>");
        }

        return result.toString();
    }
}