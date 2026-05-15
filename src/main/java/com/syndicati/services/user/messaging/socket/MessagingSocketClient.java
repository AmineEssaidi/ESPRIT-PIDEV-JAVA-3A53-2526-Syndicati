package com.syndicati.services.user.messaging.socket;

import com.google.gson.*;
import javafx.application.Platform;

import java.io.*;
import java.lang.reflect.Type;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MessagingSocketClient {
    private static final String HOST = "localhost";
    private static final int PORT = 8888;
    private static MessagingSocketClient instance;
    
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();
    private boolean connected = false;
    private Integer authenticatedUserId;
    private final List<Consumer<SocketPayload>> messageListeners = new ArrayList<>();

    private MessagingSocketClient() {}

    public static MessagingSocketClient getInstance() {
        if (instance == null) {
            instance = new MessagingSocketClient();
        }
        return instance;
    }

    public void connect(int userId) {
        if (connected && authenticatedUserId != null && authenticatedUserId == userId) return;
        if (connected) {
            disconnect();
        }

        Thread clientThread = new Thread(() -> {
            try {
                socket = new Socket(HOST, PORT);
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                connected = true;

                // Authenticate
                SocketPayload auth = new SocketPayload(SocketPayload.Type.AUTH);
                auth.setSenderId(userId);
                send(auth);
                authenticatedUserId = userId;

                System.out.println("[MessagingClient] Connected and authenticated as " + userId);

                String line;
                while (connected && (line = in.readLine()) != null) {
                    SocketPayload payload = gson.fromJson(line, SocketPayload.class);
                    notifyListeners(payload);
                }
            } catch (IOException e) {
                System.err.println("[MessagingClient] Connection failed or lost: " + e.getMessage());
                connected = false;
            } finally {
                disconnect();
            }
        });
        clientThread.setDaemon(true);
        clientThread.setName("MessagingSocketClient");
        clientThread.start();
    }

    public void disconnect() {
        connected = false;
        authenticatedUserId = null;
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        messageListeners.clear();
    }

    public void send(SocketPayload payload) {
        if (connected && out != null) {
            out.println(gson.toJson(payload));
        }
    }

    public void addMessageListener(Consumer<SocketPayload> listener) {
        messageListeners.add(listener);
    }

    public void removeMessageListener(Consumer<SocketPayload> listener) {
        messageListeners.remove(listener);
    }

    private void notifyListeners(SocketPayload payload) {
        for (Consumer<SocketPayload> listener : messageListeners) {
            listener.accept(payload);
        }
    }

    public boolean isConnected() {
        return connected;
    }

    private static class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
        private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        @Override
        public JsonElement serialize(LocalDateTime localDateTime, Type type, JsonSerializationContext context) {
            return new JsonPrimitive(formatter.format(localDateTime));
        }

        @Override
        public LocalDateTime deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
            return LocalDateTime.parse(json.getAsString(), formatter);
        }
    }
}
