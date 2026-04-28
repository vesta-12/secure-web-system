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

            try (Connection conn = Database.getConnection();
                 Statement stmt = conn.createStatement()) {

                String sql = "SELECT * FROM users WHERE username = '"
                        + username + "' AND password = '" + password + "'";

                ResultSet rs = stmt.executeQuery(sql);

                if (rs.next()) {
                    String sessionId = SessionManager.createSession(username);

                    exchange.getResponseHeaders().add(
                            "Set-Cookie",
                            "SESSIONID=" + sessionId + "; Path=/"
                    );

                    HttpUtils.redirect(exchange, "/");
                } else {
                    HttpUtils.sendHtml(exchange, "<h1>Invalid username or password</h1><a href='/login'>Back</a>");
                }

            } catch (Exception e) {
                HttpUtils.sendHtml(exchange, "<h1>Login error</h1><p>" + e.getMessage() + "</p>");
            }
        }
    }
}