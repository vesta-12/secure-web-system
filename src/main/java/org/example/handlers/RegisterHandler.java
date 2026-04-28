package org.example.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.Database;
import org.example.util.HttpUtils;
import org.example.util.TemplateUtils;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Map;

public class RegisterHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws java.io.IOException {
        if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            String html = TemplateUtils.loadHtml("register.html");
            HttpUtils.sendHtml(exchange, html);
            return;
        }

        if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            Map<String, String> form = HttpUtils.parseForm(exchange);

            String username = form.get("username");
            String password = form.get("password");

            if (username == null || username.isBlank() || password == null || password.length() < 6) {
                HttpUtils.sendHtml(exchange, "<h1>Invalid input</h1><p>Password must be at least 6 characters.</p><a href='/register'>Back</a>");
                return;
            }

            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

            try (Connection conn = Database.getConnection()) {
                String sql = "INSERT INTO users(username, password) VALUES (?, ?)";

                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, username);
                stmt.setString(2, hashedPassword);

                stmt.executeUpdate();

                HttpUtils.redirect(exchange, "/login");

            } catch (Exception e) {
                HttpUtils.sendHtml(exchange, "<h1>Registration error</h1><p>User may already exist.</p>");
            }
        }
    }
}