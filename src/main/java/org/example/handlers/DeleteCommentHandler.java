package org.example.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.Database;
import org.example.SessionManager;
import org.example.util.HttpUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Map;

public class DeleteCommentHandler implements HttpHandler {

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
        String id = form.get("id");

        try (Connection conn = Database.getConnection()) {
            String sql = "DELETE FROM comments WHERE id = ? AND username = ?";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, Integer.parseInt(id));
            stmt.setString(2, username);

            stmt.executeUpdate();

            HttpUtils.redirect(exchange, "/");

        } catch (Exception e) {
            HttpUtils.sendHtml(exchange, "<h1>Delete error</h1>");
        }
    }
}