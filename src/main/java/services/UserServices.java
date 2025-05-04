package services;

import entities.User;
import utils.MyDataBase;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserServices {
    private Connection con;

    public UserServices() {
        this.con = MyDataBase.getInstance().getConn();
    }

    /**
     * Get a user by their ID
     */
    public User getUserById(int userId) throws SQLException {
        String sql = "SELECT id, nom, email, role FROM user WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setName(rs.getString("nom"));
                    user.setEmail(rs.getString("email"));
                    user.setRole(rs.getString("role"));
                    return user;
                }
            }
        }
        return null;
    }

    /**
     * Method to get current logged-in user (placeholder)
     * This can be implemented based on how your application manages sessions
     */
    public User getCurrentUser() {
        // This should be replaced with actual session management
        // For now, return a dummy user for testing
        User dummyUser = new User();
        dummyUser.setId(1);
        dummyUser.setName("test_user");
        dummyUser.setEmail("test@example.com");
        dummyUser.setRole("user");
        return dummyUser;
    }
}