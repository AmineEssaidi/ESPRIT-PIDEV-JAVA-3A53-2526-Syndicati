package com.syndicati.services.security;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.syndicati.services.observability.HCaptchaService;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Random;

/**
 * A tiny local HTTP server to serve hCaptcha with a real localhost origin.
 * This satisfies hCaptcha's domain/origin security requirements.
 */
public class HCaptchaLocalServer {
    private HttpServer server;
    private int port;
    private final String htmlContent;

    public HCaptchaLocalServer(String htmlContent) {
        this.htmlContent = htmlContent;
    }

    public void start() throws IOException {
        // Try random ports until one works
        Random random = new Random();
        int attempts = 0;
        while (attempts < 20) {
            try {
                this.port = 8000 + random.nextInt(2000);
                this.server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
                break;
            } catch (IOException e) {
                attempts++;
            }
        }

        if (server == null) {
            throw new IOException("Could not find an available port for HCaptchaLocalServer");
        }

        server.createContext("/", exchange -> {
            byte[] response = htmlContent.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        });

        server.setExecutor(java.util.concurrent.Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        }));
        server.start();
        System.out.println("[HCaptchaServer] Started on http://localhost:" + port);
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            System.out.println("[HCaptchaServer] Stopped");
        }
    }

    public String getUrl() {
        return "http://127.0.0.1:" + port + "/";
    }
}
