package models;

import java.sql.Timestamp;

public class Comment {
    private Integer id;
    private Integer sketchId;
    private Integer userId;
    private String content;
    private Timestamp createdAt;
    private Boolean isEdited;

    // Default constructor
    public Comment() {
    }

    // Constructor with all fields except id and createdAt
    public Comment(Integer sketchId, Integer userId, String content, Boolean isEdited) {
        this.sketchId = sketchId;
        this.userId = userId;
        this.content = content;
        this.isEdited = isEdited;
    }

    // Constructor with all fields
    public Comment(Integer id, Integer sketchId, Integer userId, String content,
                   Timestamp createdAt, Boolean isEdited) {
        this.id = id;
        this.sketchId = sketchId;
        this.userId = userId;
        this.content = content;
        this.createdAt = createdAt;
        this.isEdited = isEdited;
    }

    // Getters and setters
    public Integer getId() {
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getIsEdited() {
        return isEdited;
    }

    public void setIsEdited(Boolean isEdited) {
        this.isEdited = isEdited;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", sketchId=" + sketchId +
                ", userId=" + userId +
                ", content='" + content + '\'' +
                ", createdAt=" + createdAt +
                ", isEdited=" + isEdited +
                '}';
    }
}