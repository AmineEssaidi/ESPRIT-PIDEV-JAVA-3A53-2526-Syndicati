package com.syndicati.models.user;

import java.time.LocalDateTime;

public class Conversation {
    private int id;
    private LocalDateTime createdAt;
    private String name;
    private boolean isGroup;
    private int creatorId;

    public Conversation() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public boolean isGroup() { return isGroup; }
    public void setGroup(boolean group) { isGroup = group; }

    public int getCreatorId() { return creatorId; }
    public void setCreatorId(int creatorId) { this.creatorId = creatorId; }
}
