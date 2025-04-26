package services.user;

import models.User;
import utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService implements Service<User> {
    private Connection con;

    public UserService() {
        con = DataSource.getInstance().getConnection();
    }

    @Override
    public void add(User user) throws SQLException {
        String query = "INSERT INTO user (nom, prenom, username, numTel, email, gender, " +
                "datedenaissance, roles, profilePicture, password, montantAPayer) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pst = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, user.getNom());
            pst.setString(2, user.getPrenom());
            pst.setString(3, user.getUsername());
            pst.setString(4, user.getNumTel());
            pst.setString(5, user.getEmail());
            pst.setString(6, user.getGender());
            pst.setDate(7, user.getDatedenaissance());
            pst.setString(8, user.getRoles());
            pst.setString(9, user.getProfilePicture());
            pst.setString(10, user.getPassword());
            if (user.getMontantAPayer() != null) {
                pst.setFloat(11, user.getMontantAPayer());
            } else {
                pst.setNull(11, Types.FLOAT);
            }

            int affectedRows = pst.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = pst.getGeneratedKeys()) {
                    if (rs.next()) {
                        user.setId(rs.getInt(1));
                    }
                }
            }
        }
    }

    @Override
    public boolean update(User user) throws SQLException {
        String query = "UPDATE user SET nom=?, prenom=?, username=?, numTel=?, email=?, " +
                "gender=?, datedenaissance=?, roles=?, profilePicture=?, password=?, montantAPayer=? WHERE id=?";

        try (PreparedStatement pst = con.prepareStatement(query)) {
            pst.setString(1, user.getNom());
            pst.setString(2, user.getPrenom());
            pst.setString(3, user.getUsername());
            pst.setString(4, user.getNumTel());
            pst.setString(5, user.getEmail());
            pst.setString(6, user.getGender());
            pst.setDate(7, user.getDatedenaissance());
            pst.setString(8, user.getRoles());
            pst.setString(9, user.getProfilePicture());
            pst.setString(10, user.getPassword());
            if (user.getMontantAPayer() != null) {
                pst.setFloat(11, user.getMontantAPayer());
            } else {
                pst.setNull(11, Types.FLOAT);
            }
            pst.setInt(12, user.getId());

            return pst.executeUpdate() > 0;
        }
    }

    @Override
    public void delete(User user) throws SQLException {
        String query = "DELETE FROM user WHERE id=?";
        try (PreparedStatement pst = con.prepareStatement(query)) {
            pst.setInt(1, user.getId());
            pst.executeUpdate();
        }
    }

    @Override
    public List<User> getAll() throws SQLException {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM user";

        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        }
        return users;
    }

    public User getByEmail(String email) throws SQLException {
        String query = "SELECT * FROM user WHERE email=?";
        try (PreparedStatement pst = con.prepareStatement(query)) {
            pst.setString(1, email);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        return null;
    }

    public boolean usernameExists(String username) throws SQLException {
        return getByUsername(username) != null;
    }

    public boolean emailExists(String email) throws SQLException {
        return getByEmail(email) != null;
    }

    public User getByUsername(String username) throws SQLException {
        String query = "SELECT * FROM user WHERE username=?";
        try (PreparedStatement pst = con.prepareStatement(query)) {
            pst.setString(1, username);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        return null;
    }

    public User getById(int id) throws SQLException {
        String query = "SELECT * FROM user WHERE id=?";
        try (PreparedStatement pst = con.prepareStatement(query)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        return null;
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        Float montantAPayer = rs.getFloat("montantAPayer");
        if (rs.wasNull()) {
            montantAPayer = null;
        }

        User user = new User(
                rs.getString("nom"),
                rs.getString("prenom"),
                rs.getString("username"),
                rs.getString("numTel"),
                rs.getString("email"),
                rs.getString("gender"),
                rs.getDate("datedenaissance"),
                rs.getString("profilePicture"),
                rs.getString("password"),
                rs.getString("roles"),
                montantAPayer
        );
        user.setId(rs.getInt("id"));
        return user;
    }

    // Corrected create method to use the class's connection
    public boolean create(User user) throws SQLException {
        String query = "INSERT INTO user (nom, prenom, username, numTel, email, gender, datedenaissance, " +
                "profilePicture, password, roles, montantAPayer) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, user.getNom());
            stmt.setString(2, user.getPrenom());
            stmt.setString(3, user.getUsername());
            stmt.setString(4, user.getNumTel());
            stmt.setString(5, user.getEmail());
            stmt.setString(6, user.getGender());
            stmt.setDate(7, user.getDatedenaissance());
            stmt.setString(8, user.getProfilePicture());
            stmt.setString(9, user.getPassword());
            stmt.setString(10, user.getRoles());

            if (user.getMontantAPayer() != null) {
                stmt.setFloat(11, user.getMontantAPayer());
            } else {
                stmt.setNull(11, java.sql.Types.FLOAT);
            }

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    public User getUserByUsername(String username) {
        String query = "SELECT * FROM user WHERE username = ?";
        try (PreparedStatement pst = con.prepareStatement(query)) {
            pst.setString(1, username);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();

        }
        return null;
    }

    public User getByVerificationToken(String token) throws SQLException {
        String query = "SELECT * FROM user WHERE verification_token = ?";
        try (PreparedStatement pst = con.prepareStatement(query)) {
            pst.setString(1, token);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        return null;
    }

}