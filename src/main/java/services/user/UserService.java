package services.user;

import services.eventreservation.EventService;
import models.Don;
import models.Event;
import models.User;
import utils.DataSource;
import utils.PasswordHasher;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService implements Service<User> {
    private Connection con;
    private EventService eventService;

    public UserService() {
        con = DataSource.getInstance().getConnection();
        eventService = new EventService();
    }

    @Override
    public void add(User user) throws SQLException {
        String query = "INSERT INTO user (nom, prenom, username, numTel, email, gender, " +
                "datedenaissance, roles, profilePicture, password, montantAPayer) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

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

    public void changePassword(String username, String newPassword) throws SQLException {
        User user = getByUsername(username);
        if (user == null) {
            throw new SQLException("User not found.");
        }
        user.setPassword(PasswordHasher.hash(newPassword));
        update(user);
    }

    public void updateUserRoles(User user, boolean grantAdmin) throws SQLException {
        String currentRoles = user.getRoles();
        String newRoles;
        if (grantAdmin) {
            // Add admin role if not already present
            if (currentRoles == null || currentRoles.isEmpty()) {
                newRoles = "Admin";
            } else if (!currentRoles.contains("Admin")) {
                newRoles = currentRoles + ",Admin";
            } else {
                return; // Already admin, no update needed
            }
        } else {
            // Remove admin role
            if (currentRoles == null || !currentRoles.contains("Admin")) {
                return; // Not admin, no update needed
            }
            newRoles = currentRoles.replace(",Admin", "").replace("Admin,", "").replace("Admin", "");
            if (newRoles.isEmpty()) {
                newRoles = "User"; // Default to User if no roles remain
            }
        }
        user.setRoles(newRoles);
        update(user);
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

    public boolean create(User user) throws SQLException {
        String query = "INSERT INTO user (nom, prenom, username, numTel, email, gender, datedenaissance, " +
                "profilePicture, password, roles, montantAPayer) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

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
                stmt.setNull(11, Types.FLOAT);
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
            System.err.println("Error fetching user by username: " + e.getMessage());
        }
        return null;
    }

    public boolean verifyPassword(String username, String password) {
        try {
            User user = getByUsername(username);
            if (user != null && user.getPassword() != null) {
                return PasswordHasher.verify(password, user.getPassword());
            }
        } catch (SQLException e) {
            System.err.println("Error verifying password: " + e.getMessage());
        }
        return false;
    }

    public void updateUser(User currentUser) {
        try {
            // Hash the password if it's being updated
            String password = currentUser.getPassword();
            if (password != null && !password.contains(":")) { // Check if not already hashed
                currentUser.setPassword(PasswordHasher.hash(password));
            }
            update(currentUser);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update user: " + e.getMessage(), e);
        }
    }

    public void calculateMontantAPayer(User user) throws SQLException {
        // Step 1: Fetch events where organisation matches the user's username
        List<Event> events = eventService.getEventsByOrganisation(user.getUsername());

        // Step 2: Calculate total event cost (prix * nbplaces)
        double totalEventCost = 0.0;
        for (Event event : events) {
            totalEventCost += event.getPrix() * event.getNbplaces();
        }

        // Step 3: Calculate total confirmed contributions from dons
        double totalContributions = 0.0;
        for (Don don : user.getDons()) {
            totalContributions += don.calculateTotalContributions();
        }

        // Step 4: Compute montantAPayer
        double montantAPayer = totalEventCost - totalContributions;
        user.setMontantAPayer(montantAPayer > 0 ? (float) montantAPayer : 0.0f);

        // Step 5: Update the user in the database
        update(user);
    }

    public String getRoleByUsername(String username) throws SQLException {
        String query = "SELECT roles FROM user WHERE username = ?";
        try (PreparedStatement pst = con.prepareStatement(query)) {
            pst.setString(1, username);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("roles");
                }
            }
        }
        return null;
    }
}