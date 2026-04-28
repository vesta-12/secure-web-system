package org.example.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.Database;
import org.example.util.HttpUtils;
import org.example.util.TemplateUtils;

import java.sql.Connection;
import java.sql.Statement;
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

            try (Connection conn = Database.getConnection();
                 Statement stmt = conn.createStatement()) {

                String sql = "INSERT INTO users(username, password) VALUES ('"
                        + username + "', '" + password + "')";

                stmt.executeUpdate(sql);

                HttpUtils.redirect(exchange, "/login");

            } catch (Exception e) {
                HttpUtils.sendHtml(exchange, "<h1>Registration error</h1><p>" + e.getMessage() + "</p>");
            }
        }
    }
}