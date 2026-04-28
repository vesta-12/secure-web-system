package org.example.handlers;

import org.example.Database;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Statement;

public class DeleteCommentHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        String query = exchange.getRequestURI().getQuery();

        String id = null;

        if (query != null && query.startsWith("id=")) {
            id = query.substring(3);
        }

        try (Connection connection = Database.getConnection();
             Statement statement = connection.createStatement()) {

            // INSECURE: любой пользователь может удалить любой комментарий
            String sql = "DELETE FROM comments WHERE id = " + id;

            statement.executeUpdate(sql);

        } catch (Exception e) {
            e.printStackTrace();
        }

        exchange.getResponseHeaders().add("Location", "/comments");
        exchange.sendResponseHeaders(302, -1);
    }
}