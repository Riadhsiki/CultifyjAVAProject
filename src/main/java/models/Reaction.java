package models;

import java.sql.Timestamp;

public class Reaction {
    private Integer id;
    private Integer sketchId;
    private Integer userId;
    private String reactionType; // e.g., "LIKE", "LOVE", "IDEA"
    private Timestamp createdAt;

    // Default constructor
    public Reaction() {
    }

    // Constructor with all fields except id and createdAt
    public Reaction(Integer sketchId, Integer userId, String reactionType) {
        this.sketchId = sketchId;
        this.userId = userId;
        this.reactionType = reactionType;
    }

    // Constructor with all fields
    public Reaction(Integer id, Integer sketchId, Integer userId, String reactionType, Timestamp createdAt) {
        this.id = id;
        this.sketchId = sketchId;
        this.userId = userId;
        this.reactionType = reactionType;
        this.createdAt = createdAt;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getSketchId() {
        return sketchId;
    }

    public void setSketchId(Integer sketchId) {
        this.sketchId = sketchId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getReactionType() {
        return reactionType;
    }

    public void setReactionType(String reactionType) {
        this.reactionType = reactionType;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Reaction{" +
                "id=" + id +
                ", sketchId=" + sketchId +
                ", userId=" + userId +
                ", reactionType='" + reactionType + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}