package org.example.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.SessionManager;
import org.example.util.HttpUtils;

public class LogoutHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws java.io.IOException {
        String sessionId = HttpUtils.getCookie(exchange, "SESSIONID");

        if (sessionId != null) {
            SessionManager.removeSession(sessionId);
        }

        exchange.getResponseHeaders().add(
                "Set-Cookie",
                "SESSIONID=deleted; Path=/; Max-Age=0"
        );

        HttpUtils.redirect(exchange, "/");
    }
}