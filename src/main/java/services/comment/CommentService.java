package services.comment;

import models.Comment;
import utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentService implements Service<Comment> {
    private Connection con;

    public CommentService() {
        con = DataSource.getInstance().getConnection();
    }

    @Override
    public void add(Comment comment) throws SQLException {
        String query = "INSERT INTO comment (sketch_id, user_id, content, is_edited) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pst = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pst.setInt(1, comment.getSketchId());
            pst.setInt(2, comment.getUserId());
            pst.setString(3, comment.getContent());
            pst.setBoolean(4, comment.getIsEdited() != null ? comment.getIsEdited() : false);

            int affectedRows = pst.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = pst.getGeneratedKeys()) {
                    if (rs.next()) {
                        comment.setId(rs.getInt(1));
                    }
                }
            }
        }
    }

    @Override
    public boolean update(Comment comment) throws SQLException {
        String query = "UPDATE comment SET sketch_id=?, user_id=?, content=?, is_edited=? WHERE id=?";

        try (PreparedStatement pst = con.prepareStatement(query)) {
            pst.setInt(1, comment.getSketchId());
            pst.setInt(2, comment.getUserId());
            pst.setString(3, comment.getContent());
            pst.setBoolean(4, comment.getIsEdited());
            pst.setInt(5, comment.getId());

            return pst.executeUpdate() > 0;
        }
    }

    @Override
    public void delete(Comment comment) throws SQLException {
        String query = "DELETE FROM comment WHERE id=?";
        try (PreparedStatement pst = con.prepareStatement(query)) {
            pst.setInt(1, comment.getId());
            pst.executeUpdate();
        }
    }

    @Override
    public List<Comment> getAll() throws SQLException {
        List<Comment> comments = new ArrayList<>();
        String query = "SELECT * FROM comment ORDER BY created_at DESC";

        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                comments.add(mapResultSetToComment(rs));
            }
        }
        return comments;
    }

    public List<Comment> getCommentsBySketchId(int sketchId) throws SQLException {
        List<Comment> comments = new ArrayList<>();
        String query = "SELECT * FROM comment WHERE sketch_id=? ORDER BY created_at DESC";

        try (PreparedStatement pst = con.prepareStatement(query)) {
            pst.setInt(1, sketchId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    comments.add(mapResultSetToComment(rs));
                }
            }
        }
        return comments;
    }

    public List<Comment> getCommentsByUserId(int userId) throws SQLException {
        List<Comment> comments = new ArrayList<>();
        String query = "SELECT * FROM comment WHERE user_id=? ORDER BY created_at DESC";

        try (PreparedStatement pst = con.prepareStatement(query)) {
            pst.setInt(1, userId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    comments.add(mapResultSetToComment(rs));
                }
            }
        }
        return comments;
    }

    public Comment getById(int id) throws SQLException {
        String query = "SELECT * FROM comment WHERE id=?";
        try (PreparedStatement pst = con.prepareStatement(query)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToComment(rs);
                }
            }
        }
        return null;
    }

    public List<Object[]> getCommentsWithUserInfo(int sketchId) throws SQLException {
        List<Object[]> results = new ArrayList<>();
        String query = "SELECT c.*, u.username FROM comment c " +
                "JOIN user u ON c.user_id = u.id " +
                "WHERE c.sketch_id = ? " +
                "ORDER BY c.created_at DESC";

        try (PreparedStatement pst = con.prepareStatement(query)) {
            pst.setInt(1, sketchId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Comment comment = mapResultSetToComment(rs);
                    String username = rs.getString("username");
                    results.add(new Object[]{comment, username, null}); // profilePicture set to null
                }
            }
        }
        return results;
    }

    public int countCommentsBySketchId(int sketchId) throws SQLException {
        String query = "SELECT COUNT(*) FROM comment WHERE sketch_id = ?";
        try (PreparedStatement pst = con.prepareStatement(query)) {
            pst.setInt(1, sketchId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    private Comment mapResultSetToComment(ResultSet rs) throws SQLException {
        Comment comment = new Comment();
        comment.setId(rs.getInt("id"));
        comment.setSketchId(rs.getInt("sketch_id"));
        comment.setUserId(rs.getInt("user_id"));
        comment.setContent(rs.getString("content"));
        comment.setCreatedAt(rs.getTimestamp("created_at"));
        comment.setIsEdited(rs.getBoolean("is_edited"));
        return comment;
    }
}