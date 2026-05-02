package com.syndicati.services.user.messaging.socket;

import com.google.gson.*;
import com.syndicati.models.user.Participant;
import com.syndicati.services.user.messaging.MessagingService;

import java.io.*;
import java.lang.reflect.Type;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MessagingSocketServer {
    private static final int PORT = 8888;
    private static MessagingSocketServer instance;
    private final Map<Integer, ClientHandler> clients = new ConcurrentHashMap<>();
    private final ExecutorService threadPool = Executors.newVirtualThreadPerTaskExecutor();
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();
    private boolean running = false;

    private MessagingSocketServer() {}

    public static MessagingSocketServer getInstance() {
        if (instance == null) {
            instance = new MessagingSocketServer();
        }
        return instance;
    }

    public void start() {
        if (running) return;
        running = true;
        
        // Kill any zombie process on port 8888 before starting
        cleanupPort8888();
        
        Thread serverThread = new Thread(() -> {
            try {
                ServerSocket serverSocket = new ServerSocket();
                serverSocket.setReuseAddress(true);
                serverSocket.bind(new java.net.InetSocketAddress(PORT));
                System.out.println("[MessagingServer] Started on port " + PORT);
                while (running) {
                    Socket clientSocket = serverSocket.accept();
                    threadPool.execute(new ClientHandler(clientSocket));
                }
            } catch (IOException e) {
                if (running) {
                    System.err.println("[MessagingServer] Error: " + e.getMessage());
                }
            }
        });
        serverThread.setDaemon(true);
        serverThread.setName("MessagingSocketServer");
        serverThread.start();
    }

    public void stop() {
        running = false;
        clients.values().forEach(ClientHandler::close);
        clients.clear();
        threadPool.shutdownNow();
    }

    private void cleanupPort8888() {
        try {
            // Windows specific port cleanup
            ProcessBuilder pb = new ProcessBuilder("cmd", "/c", 
                "for /f \"tokens=5\" %a in ('netstat -aon ^| findstr 8888') do taskkill /f /pid %a");
            pb.start().waitFor();
            Thread.sleep(500); // Give it a moment to release the port
        } catch (Exception ignored) {}
    }

    private class ClientHandler implements Runnable {
        private final Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private int userId = -1;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            try {
                socket.setTcpNoDelay(true); // Reduce latency for messaging
                socket.setSendBufferSize(64 * 1024);
                socket.setReceiveBufferSize(64 * 1024);
            } catch (Exception ignored) {}
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                String line;
                while ((line = in.readLine()) != null) {
                    SocketPayload payload = gson.fromJson(line, SocketPayload.class);
                    handlePayload(payload);
                }
            } catch (IOException e) {
                // Connection closed
            } finally {
                close();
            }
        }

        private void handlePayload(SocketPayload payload) {
            if (payload.getType() == SocketPayload.Type.AUTH) {
                this.userId = payload.getSenderId();
                clients.put(userId, this);
                System.out.println("[MessagingServer] User " + userId + " authenticated.");
            } else if (payload.getType() == SocketPayload.Type.MESSAGE) {
                broadcastMessage(payload);
            }
        }

        private void broadcastMessage(SocketPayload payload) {
            // If it's a conversation, find all participants
            if (payload.getConversationId() != null) {
                List<Participant> participants = MessagingService.getInstance().findConversationParticipants(payload.getConversationId());
                for (Participant p : participants) {
                    if (p.getUserId() == userId) continue; // Don't send back to sender
                    ClientHandler target = clients.get(p.getUserId());
                    if (target != null) {
                        target.send(payload);
                    }
                }
            } else if (payload.getRecipientId() != null) {
                // Direct message to a specific user (fallback if conversationId not yet known)
                ClientHandler target = clients.get(payload.getRecipientId());
                if (target != null) {
                    target.send(payload);
                }
            }
        }

        public void send(SocketPayload payload) {
            if (out != null) {
                out.println(gson.toJson(payload));
            }
        }

        public void close() {
            try {
                if (userId != -1) clients.remove(userId);
                if (in != null) in.close();
                if (out != null) out.close();
                if (!socket.isClosed()) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
