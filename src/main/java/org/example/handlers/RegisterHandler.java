package org.example.handlers;

import org.example.Database;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class RegisterHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            showRegisterPage(exchange);
        } else if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            handleRegister(exchange);
        }
    }

    private void showRegisterPage(HttpExchange exchange) throws IOException {
        String response = """
                <html>
                <head>
                    <title>Register</title>
                </head>
                <body>
                    <h1>Register</h1>

                    <form method="POST" action="/register">
                        <label>Username:</label><br>
                        <input type="text" name="username"><br><br>

                        <label>Password:</label><br>
                        <input type="password" name="password"><br><br>

                        <button type="submit">Register</button>
                    </form>

                    <p><a href="/">Back to home</a></p>
                    <p><a href="/login">Already have an account? Login</a></p>
                </body>
                </html>
                """;

        sendResponse(exchange, response);
    }

    private void handleRegister(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> formData = parseFormData(body);

        String username = formData.get("username");
        String password = formData.get("password");

        try (Connection connection = Database.getConnection();
             Statement statement = connection.createStatement()) {
            String sql = "INSERT INTO users (username, password) VALUES ('"
                    + username + "', '"
                    + password + "')";

            statement.executeUpdate(sql);

            String response = """
                    <html>
                    <body>
                        <h1>Registration successful</h1>
                        <p>User registered successfully.</p>
                        <p><a href="/login">Go to login</a></p>
                    </body>
                    </html>
                    """;

            sendResponse(exchange, response);

        } catch (Exception e) {
            String response = """
                    <html>
                    <body>
                        <h1>Registration failed</h1>
                        <p>User may already exist or input is invalid.</p>
                        <p><a href="/register">Try again</a></p>
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