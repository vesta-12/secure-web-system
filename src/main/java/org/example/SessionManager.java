package org.example;
import com.sun.net.httpserver.HttpExchange;
import java.net.HttpCookie;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;

public class SessionManager {

    public static String createSession(int userId) {
        String sessionId = UUID.randomUUID().toString();

        try (Connection connection = Database.getConnection();
             Statement statement = connection.createStatement()) {

            String sql = "INSERT INTO sessions (session_id, user_id) VALUES ('"
                    + sessionId + "', "
                    + userId + ")";

            statement.executeUpdate(sql);

            return sessionId;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Integer getUserIdFromSession(HttpExchange exchange) {
        String sessionId = getCookie(exchange, "SESSION_ID");

        if (sessionId == null) {
            return null;
        }

        try (Connection connection = Database.getConnection();
             Statement statement = connection.createStatement()) {

            String sql = "SELECT user_id FROM sessions WHERE session_id = '" + sessionId + "'";

            ResultSet resultSet = statement.executeQuery(sql);

            if (resultSet.next()) {
                return resultSet.getInt("user_id");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void deleteSession(HttpExchange exchange) {
        String sessionId = getCookie(exchange, "SESSION_ID");

        if (sessionId == null) {
            return;
        }

        try (Connection connection = Database.getConnection();
             Statement statement = connection.createStatement()) {

            String sql = "DELETE FROM sessions WHERE session_id = '" + sessionId + "'";
            statement.executeUpdate(sql);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getCookie(HttpExchange exchange, String cookieName) {
        List<String> cookies = exchange.getRequestHeaders().get("Cookie");

        if (cookies == null) {
            return null;
        }

        for (String cookieHeader : cookies) {
            List<HttpCookie> parsedCookies = HttpCookie.parse(cookieHeader);

            for (HttpCookie cookie : parsedCookies) {
                if (cookie.getName().equals(cookieName)) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }
}