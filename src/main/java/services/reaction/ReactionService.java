package services.reaction;

import models.Reaction;
import utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReactionService implements Service<Reaction> {
    private Connection con;

    public ReactionService() {
        con = DataSource.getInstance().getConnection();
    }

    @Override
    public void add(Reaction reaction) throws SQLException {
        String query = "INSERT INTO reaction (sketch_id, user_id, reaction_type) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE created_at = CURRENT_TIMESTAMP";

        try (PreparedStatement pst = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pst.setInt(1, reaction.getSketchId());
            pst.setInt(2, reaction.getUserId());
            pst.setString(3, reaction.getReactionType());

            int affectedRows = pst.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = pst.getGeneratedKeys()) {
                    if (rs.next()) {
                        reaction.setId(rs.getInt(1));
                    }
                }
            }
        }
    }

    @Override
    public boolean update(Reaction reaction) throws SQLException {
        String query = "UPDATE reaction SET sketch_id=?, user_id=?, reaction_type=? WHERE id=?";

        try (PreparedStatement pst = con.prepareStatement(query)) {
            pst.setInt(1, reaction.getSketchId());
            pst.setInt(2, reaction.getUserId());
            pst.setString(3, reaction.getReactionType());
            pst.setInt(4, reaction.getId());

            return pst.executeUpdate() > 0;
        }
    }

    @Override
    public void delete(Reaction reaction) throws SQLException {
        String query = "DELETE FROM reaction WHERE id=?";
        try (PreparedStatement pst = con.prepareStatement(query)) {
            pst.setInt(1, reaction.getId());
            pst.executeUpdate();
        }
    }

    public void deleteUserReaction(int userId, int sketchId, String reactionType) throws SQLException {
        String query = "DELETE FROM reaction WHERE user_id=? AND sketch_id=? AND reaction_type=?";
        try (PreparedStatement pst = con.prepareStatement(query)) {
            pst.setInt(1, userId);
            pst.setInt(2, sketchId);
            pst.setString(3, reactionType);
            pst.executeUpdate();
        }
    }

    @Override
    public List<Reaction> getAll() throws SQLException {
        List<Reaction> reactions = new ArrayList<>();
        String query = "SELECT * FROM reaction ORDER BY created_at DESC";

        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                reactions.add(mapResultSetToReaction(rs));
            }
        }
        return reactions;
    }

    public List<Reaction> getReactionsBySketchId(int sketchId) throws SQLException {
        List<Reaction> reactions = new ArrayList<>();
        String query = "SELECT * FROM reaction WHERE sketch_id=? ORDER BY created_at DESC";

        try (PreparedStatement pst = con.prepareStatement(query)) {
            pst.setInt(1, sketchId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    reactions.add(mapResultSetToReaction(rs));
                }
            }
        }
        return reactions;
    }

    public List<Reaction> getUserReactionsForSketch(int userId, int sketchId) throws SQLException {
        List<Reaction> reactions = new ArrayList<>();
        String query = "SELECT * FROM reaction WHERE user_id=? AND sketch_id=?";

        try (PreparedStatement pst = con.prepareStatement(query)) {
            pst.setInt(1, userId);
            pst.setInt(2, sketchId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    reactions.add(mapResultSetToReaction(rs));
                }
            }
        }
        return reactions;
    }

    public Map<String, Integer> countReactionsByType(int sketchId) throws SQLException {
        Map<String, Integer> counts = new HashMap<>();
        String query = "SELECT reaction_type, COUNT(*) as count FROM reaction WHERE sketch_id=? GROUP BY reaction_type";

        try (PreparedStatement pst = con.prepareStatement(query)) {
            pst.setInt(1, sketchId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    counts.put(rs.getString("reaction_type"), rs.getInt("count"));
                }
            }
        }
        return counts;
    }

    public int getTotalReactionsCount(int sketchId) throws SQLException {
        String query = "SELECT COUNT(*) FROM reaction WHERE sketch_id=?";
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

    public List<Object[]> getRecentReactionsWithUsers(int sketchId, int limit) throws SQLException {
        List<Object[]> results = new ArrayList<>();
        String query = "SELECT r.*, u.username FROM reaction r " +
                "JOIN user u ON r.user_id = u.id " +
                "WHERE r.sketch_id = ? " +
                "ORDER BY r.created_at DESC LIMIT ?";

        try (PreparedStatement pst = con.prepareStatement(query)) {
            pst.setInt(1, sketchId);
            pst.setInt(2, limit);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Reaction reaction = mapResultSetToReaction(rs);
                    String username = rs.getString("username");
                    results.add(new Object[]{reaction, username});
                }
            }
        }
        return results;
    }

    private Reaction mapResultSetToReaction(ResultSet rs) throws SQLException {
        Reaction reaction = new Reaction();
        reaction.setId(rs.getInt("id"));
        reaction.setSketchId(rs.getInt("sketch_id"));
        reaction.setUserId(rs.getInt("user_id"));
        reaction.setReactionType(rs.getString("reaction_type"));
        reaction.setCreatedAt(rs.getTimestamp("created_at"));
        return reaction;
    }
}