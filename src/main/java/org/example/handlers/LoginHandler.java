package org.example.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.Database;
import org.example.SessionManager;
import org.example.util.HttpUtils;
import org.example.util.TemplateUtils;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

public class LoginHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws java.io.IOException {
        if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            String html = TemplateUtils.loadHtml("login.html");
            HttpUtils.sendHtml(exchange, html);
            return;
        }

        if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            Map<String, String> form = HttpUtils.parseForm(exchange);

            String username = form.get("username");
            String password = form.get("password");

            try (Connection conn = Database.getConnection()) {
                String sql = "SELECT * FROM users WHERE username = ?";

                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, username);

                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    String hashedPassword = rs.getString("password");

                    if (BCrypt.checkpw(password, hashedPassword)) {
                        String sessionId = SessionManager.createSession(username);

                        exchange.getResponseHeaders().add(
                                "Set-Cookie",
                                "SESSIONID=" + sessionId + "; Path=/; HttpOnly; SameSite=Strict"
                        );

                        HttpUtils.redirect(exchange, "/");
                        return;
                    }
                }

                HttpUtils.sendHtml(exchange, "<h1>Invalid username or password</h1><a href='/login'>Back</a>");

            } catch (Exception e) {
                HttpUtils.sendHtml(exchange, "<h1>Login error</h1>");
            }
        }
    }
}