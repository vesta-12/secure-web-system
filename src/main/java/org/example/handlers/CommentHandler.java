package org.example.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.Database;
import org.example.SessionManager;
import org.example.util.HttpUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;

public class CommentHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws java.io.IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            HttpUtils.redirect(exchange, "/");
            return;
        }

        String sessionId = HttpUtils.getCookie(exchange, "SESSIONID");
        String username = sessionId == null ? null : SessionManager.getUsername(sessionId);

        if (username == null) {
            HttpUtils.sendHtml(exchange, "<h1>You must login first</h1><a href='/login'>Login</a>");
            return;
        }

        Map<String, String> form = HttpUtils.parseForm(exchange);
        String text = form.get("text");

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement()) {

            ResultSet userRs = stmt.executeQuery(
                    "SELECT id FROM users WHERE username = '" + username + "'"
            );

            if (userRs.next()) {
                int userId = userRs.getInt("id");

                String sql = "INSERT INTO comments(user_id, username, text) VALUES ("
                        + userId + ", '" + username + "', '" + text + "')";

                stmt.executeUpdate(sql);
            }

            HttpUtils.redirect(exchange, "/");

        } catch (Exception e) {
            HttpUtils.sendHtml(exchange, "<h1>Comment error</h1><p>" + e.getMessage() + "</p>");
        }
    }
}