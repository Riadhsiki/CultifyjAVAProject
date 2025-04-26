package models;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

public class CultureSketch {
    private int id;
    private int userId;
    private String title;
    private String description;
    private List<String> colors;
    private List<Map<String, Object>> shapeData;
    private boolean isPublic;
    private Timestamp createdAt;

    // Default constructor
    public CultureSketch() {
    }

    // Constructor with all fields except id and createdAt
    public CultureSketch(int userId, String title, String description, List<String> colors,
                         List<Map<String, Object>> shapeData, boolean isPublic) {
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.colors = colors;
        this.shapeData = shapeData;
        this.isPublic = isPublic;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getColors() {
        return colors;
    }

    public void setColors(List<String> colors) {
        this.colors = colors;
    }

    public List<Map<String, Object>> getShapeData() {
        return shapeData;
    }

    public void setShapeData(List<Map<String, Object>> shapeData) {
        this.shapeData = shapeData;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "CultureSketch{" +
                "id=" + id +
                ", userId=" + userId +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", colors=" + colors +
                ", shapeData=" + shapeData +
                ", isPublic=" + isPublic +
                ", createdAt=" + createdAt +
                '}';
    }
}