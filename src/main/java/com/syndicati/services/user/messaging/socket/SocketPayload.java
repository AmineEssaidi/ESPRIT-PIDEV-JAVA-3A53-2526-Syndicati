package com.syndicati.services.user.messaging.socket;

import com.syndicati.models.user.Message;

public class SocketPayload {
    public enum Type {
        AUTH,      // Identify user to server
        MESSAGE,   // Chat message
        TYPING,    // User is typing (optional enhancement)
        ERROR
    }

    private Type type;
    private int senderId;
    private Integer recipientId; // For 1-to-1
    private Integer conversationId; // For group or existing 1-to-1
    private Message message;
    private String content;

    public SocketPayload() {}

    public SocketPayload(Type type) {
        this.type = type;
    }

    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }

    public int getSenderId() { return senderId; }
    public void setSenderId(int senderId) { this.senderId = senderId; }

    public Integer getRecipientId() { return recipientId; }
    public void setRecipientId(Integer recipientId) { this.recipientId = recipientId; }

    public Integer getConversationId() { return conversationId; }
    public void setConversationId(Integer conversationId) { this.conversationId = conversationId; }

    public Message getMessage() { return message; }
    public void setMessage(Message message) { this.message = message; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
