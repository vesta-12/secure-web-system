package org.example;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import org.example.handlers.RegisterHandler;

public class Main {
    public static void main(String[] args) throws IOException {
        Database.init();

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/register", new RegisterHandler());
        server.createContext("/", exchange -> {
            String response = """
                    <html>
                    <body>
                        <ul>
                            <li><a href="/register">Register</a></li>
                            <li><a href="/login">Login</a></li>
                            <li><a href="/comments">Comments</a></li>
                        </ul>
                    </body>
                    </html>
                    """;

            exchange.sendResponseHeaders(200, response.getBytes().length);
            exchange.getResponseBody().write(response.getBytes());
            exchange.close();
        });

        server.start();

        System.out.println("Server started at http://localhost:8080");
    }
}