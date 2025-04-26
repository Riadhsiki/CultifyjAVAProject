package services.sketch;

import models.CultureSketch;
import utils.DataSource;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CultureSketchService {
    private final Gson gson = new Gson();
    private final DataSource dataSource;
    private static final int DEFAULT_USER_ID = 1; // Hardcoded user_id for testing

    // Constructor to initialize with DataSource
    public CultureSketchService() {
        this.dataSource = DataSource.getInstance();
        initDatabase();
    }

    private void initDatabase() {
        Statement stmt = null;
        try {
            Connection conn = dataSource.getConnection();
            stmt = conn.createStatement();

            // Create the user table if it doesn't exist
            String createUserTableSQL =
                    "CREATE TABLE IF NOT EXISTS user (" +
                            "id INT PRIMARY KEY AUTO_INCREMENT, " +
                            "username VARCHAR(255) NOT NULL, " +
                            "email VARCHAR(255), " +
                            "password VARCHAR(255)" +
                            ") ENGINE=InnoDB";
            stmt.execute(createUserTableSQL);

            // Ensure a test user with DEFAULT_USER_ID exists
            String insertTestUserSQL =
                    "INSERT INTO user (id, username, email, password) " +
                            "SELECT * FROM (SELECT 1, 'testuser', 'test@example.com', 'password') AS tmp " +
                            "WHERE NOT EXISTS (SELECT id FROM user WHERE id = 1) LIMIT 1";
            stmt.execute(insertTestUserSQL);

            // Create the culture_sketches table if it doesn't exist
            String createTableSQL =
                    "CREATE TABLE IF NOT EXISTS culture_sketches (" +
                            "id INT PRIMARY KEY AUTO_INCREMENT, " +
                            "user_id INT NOT NULL, " +
                            "title VARCHAR(255) NOT NULL, " +
                            "description TEXT, " +
                            "colors JSON, " +
                            "shape_data JSON, " +
                            "is_public BOOLEAN DEFAULT FALSE, " +
                            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                            "FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE" +
                            ") ENGINE=InnoDB";
            stmt.execute(createTableSQL);
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResourcesExceptConnection(stmt);
        }
    }

    // Add a new sketch, returns the new ID
    public Integer add(CultureSketch sketch) {
        String sql = "INSERT INTO culture_sketches " +
                "(user_id, title, description, colors, shape_data, is_public) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        PreparedStatement pstmt = null;
        ResultSet generatedKeys = null;

        try {
            Connection conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            // Use hardcoded user_id for testing
            pstmt.setInt(1, DEFAULT_USER_ID);
            pstmt.setString(2, sketch.getTitle());
            pstmt.setString(3, sketch.getDescription());
            pstmt.setString(4, gson.toJson(sketch.getColors()));
            pstmt.setString(5, gson.toJson(sketch.getShapeData()));
            pstmt.setBoolean(6, sketch.isPublic());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error adding sketch: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResourcesExceptConnection(generatedKeys, pstmt);
        }
        return null; // Indicate failure
    }

    // Update an existing sketch
    public boolean update(CultureSketch sketch) {
        String sql = "UPDATE culture_sketches SET " +
                "title = ?, description = ?, colors = ?, shape_data = ?, is_public = ? " +
                "WHERE id = ? AND user_id = ?";

        PreparedStatement pstmt = null;

        try {
            Connection conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, sketch.getTitle());
            pstmt.setString(2, sketch.getDescription());
            pstmt.setString(3, gson.toJson(sketch.getColors()));
            pstmt.setString(4, gson.toJson(sketch.getShapeData()));
            pstmt.setBoolean(5, sketch.isPublic());
            pstmt.setInt(6, sketch.getId());
            pstmt.setInt(7, DEFAULT_USER_ID); // Use hardcoded user_id

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating sketch: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            closeResourcesExceptConnection(pstmt);
        }
    }

    // Delete a sketch
    public boolean delete(int id) {
        String sql = "DELETE FROM culture_sketches WHERE id = ?";

        PreparedStatement pstmt = null;

        try {
            Connection conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting sketch: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            closeResourcesExceptConnection(pstmt);
        }
    }

    // Get sketch by ID
    public CultureSketch getById(int id) {
        String sql = "SELECT * FROM culture_sketches WHERE id = ?";

        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            Connection conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);

            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractSketchFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving sketch by ID: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResourcesExceptConnection(rs, pstmt);
        }
        return null;
    }

    // Get sketches by user ID
    public List<CultureSketch> getByUserId(int userId) {
        List<CultureSketch> sketches = new ArrayList<>();
        String sql = "SELECT * FROM culture_sketches WHERE user_id = ? ORDER BY created_at DESC";

        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            Connection conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);

            pstmt.setInt(1, DEFAULT_USER_ID); // Use hardcoded user_id
            rs = pstmt.executeQuery();

            while (rs.next()) {
                sketches.add(extractSketchFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving sketches by user ID: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResourcesExceptConnection(rs, pstmt);
        }
        return sketches;
    }

    // Get public sketches
    public List<CultureSketch> getPublicSketches() {
        List<CultureSketch> sketches = new ArrayList<>();
        String sql = "SELECT * FROM culture_sketches WHERE is_public = TRUE ORDER BY created_at DESC";

        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            Connection conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);

            rs = pstmt.executeQuery();
            while (rs.next()) {
                sketches.add(extractSketchFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving public sketches: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResourcesExceptConnection(rs, pstmt);
        }
        return sketches;
    }

    // Helper method to extract sketch from result set
    private CultureSketch extractSketchFromResultSet(ResultSet rs) throws SQLException {
        CultureSketch sketch = new CultureSketch();
        sketch.setId(rs.getInt("id"));
        sketch.setUserId(rs.getInt("user_id"));
        sketch.setTitle(rs.getString("title"));
        sketch.setDescription(rs.getString("description"));

        String colorsJson = rs.getString("colors");
        List<String> colors = colorsJson != null ?
                gson.fromJson(colorsJson, new TypeToken<List<String>>(){}.getType()) :
                new ArrayList<>();
        sketch.setColors(colors);

        String shapeDataJson = rs.getString("shape_data");
        List<Map<String, Object>> shapeData = shapeDataJson != null ?
                gson.fromJson(shapeDataJson, new TypeToken<List<Map<String, Object>>>(){}.getType()) :
                new ArrayList<>();
        sketch.setShapeData(shapeData);

        sketch.setPublic(rs.getBoolean("is_public"));
        sketch.setCreatedAt(rs.getTimestamp("created_at"));

        return sketch;
    }

    // Helper method to close database resources except Connection
    private void closeResourcesExceptConnection(AutoCloseable... resources) {
        for (AutoCloseable resource : resources) {
            if (resource != null && !(resource instanceof Connection)) {
                try {
                    resource.close();
                } catch (Exception e) {
                    System.err.println("Error closing resource: " + e.getMessage());
                }
            }
        }
    }
}