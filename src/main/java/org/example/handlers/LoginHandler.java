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

public class LoginHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            showLoginPage(exchange);
        } else if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            handleLogin(exchange);
        }
    }

    private void showLoginPage(HttpExchange exchange) throws IOException {
        String response = """
                <html>
                <head>
                    <title>Login</title>
                </head>
                <body>
                    <h1>Login</h1>

                    <form method="POST" action="/login">
                        <label>Username:</label><br>
                        <input type="text" name="username"><br><br>

                        <label>Password:</label><br>
                        <input type="password" name="password"><br><br>

                        <button type="submit">Login</button>
                    </form>

                    <p><a href="/">Back to home</a></p>
                    <p><a href="/register">Create account</a></p>
                </body>
                </html>
                """;

        sendResponse(exchange, response);
    }

    private void handleLogin(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> formData = parseFormData(body);

        String username = formData.get("username");
        String password = formData.get("password");

        try (Connection connection = Database.getConnection();
             Statement statement = connection.createStatement()) {

            String sql = "SELECT * FROM users WHERE username = '"
                    + username
                    + "' AND password = '"
                    + password
                    + "'";

            ResultSet resultSet = statement.executeQuery(sql);

            if (resultSet.next()) {
                int userId = resultSet.getInt("id");
                String sessionId = SessionManager.createSession(userId);

                exchange.getResponseHeaders().add(
                        "Set-Cookie",
                        "SESSION_ID=" + sessionId + "; Path=/"
                );

                String response = """
                        <html>
                        <body>
                            <h1>Login successful</h1>
                            <p>You are now logged in.</p>
                            <p><a href="/comments">Go to comments</a></p>
                        </body>
                        </html>
                        """;

                sendResponse(exchange, response);
            } else {
                String response = """
                        <html>
                        <body>
                            <h1>Login failed</h1>
                            <p>Invalid username or password.</p>
                            <p><a href="/login">Try again</a></p>
                        </body>
                        </html>
                        """;

                sendResponse(exchange, response);
            }

        } catch (Exception e) {
            e.printStackTrace();

            String response = """
                    <html>
                    <body>
                        <h1>Error</h1>
                        <p>Something went wrong.</p>
                        <p><a href="/login">Back to login</a></p>
                    </body>
                    </html>
                    """;

            sendResponse(exchange, response);
        }
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