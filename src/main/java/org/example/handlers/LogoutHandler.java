package org.example.handlers;

import org.example.SessionManager;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class LogoutHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        SessionManager.deleteSession(exchange);

        exchange.getResponseHeaders().add(
                "Set-Cookie",
                "SESSION_ID=deleted; Path=/; Max-Age=0"
        );

        String response = """
                <html>
                <body>
                    <h1>Logged out</h1>
                    <p>You have been logged out.</p>
                    <p><a href="/">Back to home</a></p>
                </body>
                </html>
                """;

        sendResponse(exchange, response);
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