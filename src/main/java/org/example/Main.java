package org.example;

import com.sun.net.httpserver.HttpServer;
import org.example.handlers.*;

import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) throws Exception {
        Database.init();

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/", new HomeHandler());
        server.createContext("/register", new RegisterHandler());
        server.createContext("/login", new LoginHandler());
        server.createContext("/logout", new LogoutHandler());
        server.createContext("/comment", new CommentHandler());

        server.setExecutor(null);
        server.start();

        System.out.println("Server started: http://localhost:8080");
    }
}