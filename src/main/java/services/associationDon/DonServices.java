package services.associationDon;

import models.Association;
import models.Don;
import models.User;
import services.user.UserService;
import utils.DataSource;
import utils.SessionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DonServices {
    private Connection con;
    private UserService userService;

    public DonServices() {
        con = DataSource.getInstance().getConnection();
        userService = new UserService();
    }

    public void Add(Don don) throws SQLException {
        // Get the current user from the session
        String currentUsername = SessionManager.getInstance().getCurrentUsername();
        if (currentUsername == null || currentUsername.isEmpty()) {
            throw new SQLException("No user is currently logged in");
        }

        User currentUser = userService.getUserByUsername(currentUsername);
        if (currentUser == null) {
            throw new SQLException("Failed to retrieve user information");
        }

        // Set the user to the donation
        don.setUser(currentUser);

        // Insert the donation into the database
        String query = "INSERT INTO don (montant, donor_type, status, type, association_id, id_user_id) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pst = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pst.setDouble(1, don.getMontant());
            pst.setString(2, don.getDonorType());
            pst.setString(3, don.getStatus());
            pst.setString(4, don.getType());

            if (don.getAssociation() != null) {
                pst.setInt(5, don.getAssociation().getId());
            } else {
                pst.setNull(5, Types.INTEGER);
            }

            pst.setInt(6, currentUser.getId());

            int affectedRows = pst.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = pst.getGeneratedKeys()) {
                    if (rs.next()) {
                        don.setId(rs.getInt(1));
                    }
                }
            }
        }
    }

    // Overloaded method that accepts user ID explicitly (for backward compatibility)
    public void Add(int userId, Don don) throws SQLException {
        // If a specific user ID is provided, get that user
        User user = userService.getById(userId);
        if (user == null) {
            throw new SQLException("User with ID " + userId + " not found");
        }

        // Set the user to the donation
        don.setUser(user);

        // Insert the donation into the database
        String query = "INSERT INTO don (montant, donor_type, status, type, association_id, id_user_id) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pst = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pst.setDouble(1, don.getMontant());
            pst.setString(2, don.getDonorType());
            pst.setString(3, don.getStatus());
            pst.setString(4, don.getType());

            if (don.getAssociation() != null) {
                pst.setInt(5, don.getAssociation().getId());
            } else {
                pst.setNull(5, Types.INTEGER);
            }

            pst.setInt(6, userId);

            int affectedRows = pst.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = pst.getGeneratedKeys()) {
                    if (rs.next()) {
                        don.setId(rs.getInt(1));
                    }
                }
            }
        }
    }

    public boolean update(Don don, int associationId) throws SQLException {
        String query = "UPDATE don SET montant=?, donor_type=?, status=?, type=?, association_id=?, id_user_id=? WHERE id=?";

        try (PreparedStatement pst = con.prepareStatement(query)) {
            pst.setDouble(1, don.getMontant());
            pst.setString(2, don.getDonorType());
            pst.setString(3, don.getStatus());
            pst.setString(4, don.getType());
            pst.setInt(5, associationId);

            if (don.getUser() != null) {
                pst.setInt(6, don.getUser().getId());
            } else {
                pst.setNull(6, Types.INTEGER);
            }

            pst.setInt(7, don.getId());

            return pst.executeUpdate() > 0;
        }
    }

    public boolean delete(Don don) throws SQLException {
        String query = "DELETE FROM don WHERE id=?";
        try (PreparedStatement pst = con.prepareStatement(query)) {
            pst.setInt(1, don.getId());
            return pst.executeUpdate() > 0;
        }
    }

    public List<Don> getAll() throws SQLException {
        List<Don> donations = new ArrayList<>();
        String query = "SELECT d.*, a.id as association_id, a.nom as association_nom, " +
                "u.id as user_id, u.username as user_username, u.email as user_email " +
                "FROM don d " +
                "LEFT JOIN association a ON d.association_id = a.id " +
                "LEFT JOIN user u ON d.id_user_id = u.id";

        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                donations.add(mapResultSetToDon(rs));
            }
        }
        return donations;
    }

    public Don getById(int id) throws SQLException {
        String query = "SELECT d.*, a.id as association_id, a.nom as association_nom, " +
                "u.id as user_id, u.username as user_username, u.email as user_email " +
                "FROM don d " +
                "LEFT JOIN association a ON d.association_id = a.id " +
                "LEFT JOIN user u ON d.id_user_id = u.id " +
                "WHERE d.id=?";

        try (PreparedStatement pst = con.prepareStatement(query)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDon(rs);
                }
            }
        }
        return null;
    }

    // Method needed by ListDon class
    public List<Don> getDonsByUser(int userId) throws SQLException {
        return getByUserId(userId);
    }

    public List<Don> getByUserId(int userId) throws SQLException {
        List<Don> donations = new ArrayList<>();
        String query = "SELECT d.*, a.id as association_id, a.nom as association_nom, " +
                "u.id as user_id, u.username as user_username, u.email as user_email " +
                "FROM don d " +
                "LEFT JOIN association a ON d.association_id = a.id " +
                "LEFT JOIN user u ON d.id_user_id = u.id " +
                "WHERE d.id_user_id=?";

        try (PreparedStatement pst = con.prepareStatement(query)) {
            pst.setInt(1, userId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    donations.add(mapResultSetToDon(rs));
                }
            }
        }
        return donations;
    }

    public List<Don> getByAssociationId(int associationId) throws SQLException {
        List<Don> donations = new ArrayList<>();
        String query = "SELECT d.*, a.id as association_id, a.nom as association_nom, " +
                "u.id as user_id, u.username as user_username, u.email as user_email " +
                "FROM don d " +
                "LEFT JOIN association a ON d.association_id = a.id " +
                "LEFT JOIN user u ON d.id_user_id = u.id " +
                "WHERE d.association_id=?";

        try (PreparedStatement pst = con.prepareStatement(query)) {
            pst.setInt(1, associationId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    donations.add(mapResultSetToDon(rs));
                }
            }
        }
        return donations;
    }

    // Get donation details with full association and user information
    public Don getDonDetails(int donId) throws SQLException {
        String query = "SELECT d.*, " +
                "a.id as association_id, a.nom as association_nom, " +
                "u.id as user_id, u.nom as user_nom, u.prenom as user_prenom, u.username as user_username, u.email as user_email " +
                "FROM don d " +
                "LEFT JOIN association a ON d.association_id = a.id " +
                "LEFT JOIN user u ON d.id_user_id = u.id " +
                "WHERE d.id=?";

        try (PreparedStatement pst = con.prepareStatement(query)) {
            pst.setInt(1, donId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDetailedDon(rs);
                }
            }
        }
        return null;
    }

    private Don mapResultSetToDetailedDon(ResultSet rs) throws SQLException {
        Don don = new Don();
        don.setId(rs.getInt("id"));
        don.setMontant(rs.getDouble("montant"));
        don.setDonorType(rs.getString("donor_type"));
        don.setStatus(rs.getString("status"));
        don.setType(rs.getString("type"));

        // Map Association with more details if available
        int associationId = rs.getInt("association_id");
        if (!rs.wasNull()) {
            Association association = new Association();
            association.setId(associationId);
            association.setNom(rs.getString("association_nom"));
            don.setAssociation(association);
        }

        // Map User with more details if available
        int userId = rs.getInt("user_id");
        if (!rs.wasNull()) {
            User user = new User();
            user.setId(userId);
            user.setNom(rs.getString("user_nom"));
            user.setPrenom(rs.getString("user_prenom"));
            user.setUsername(rs.getString("user_username"));
            user.setEmail(rs.getString("user_email"));
            don.setUser(user);
        }

        return don;
    }

    private Don mapResultSetToDon(ResultSet rs) throws SQLException {
        Don don = new Don();
        don.setId(rs.getInt("id"));
        don.setMontant(rs.getDouble("montant"));
        don.setDonorType(rs.getString("donor_type"));
        don.setStatus(rs.getString("status"));
        don.setType(rs.getString("type"));

        // Map Association if available
        int associationId = rs.getInt("association_id");
        if (!rs.wasNull()) {
            Association association = new Association();
            association.setId(associationId);
            association.setNom(rs.getString("association_nom"));
            don.setAssociation(association);
        }

        // Map User if available
        int userId = rs.getInt("user_id");
        if (!rs.wasNull()) {
            User user = new User();
            user.setId(userId);
            user.setUsername(rs.getString("user_username"));
            user.setEmail(rs.getString("user_email"));
            don.setUser(user);
        }

        return don;
    }

    // Get donations for the currently logged in user
    public List<Don> getCurrentUserDonations() throws SQLException {
        String currentUsername = SessionManager.getInstance().getCurrentUsername();
        if (currentUsername == null || currentUsername.isEmpty()) {
            throw new SQLException("No user is currently logged in");
        }

        User currentUser = userService.getUserByUsername(currentUsername);
        if (currentUser == null) {
            throw new SQLException("Failed to retrieve user information");
        }

        return getByUserId(currentUser.getId());
    }

    // Search function for the ListDon interface
    public List<Don> searchDons(String searchText, String criteria, int userId) throws SQLException {
        List<Don> donations = new ArrayList<>();

        // Base query for searching donations with joins
        String query = "SELECT d.*, a.id as association_id, a.nom as association_nom, " +
                "u.id as user_id, u.username as user_username, u.email as user_email " +
                "FROM don d " +
                "LEFT JOIN association a ON d.association_id = a.id " +
                "LEFT JOIN user u ON d.id_user_id = u.id " +
                "WHERE d.id_user_id=? AND ";

        // Add criteria-specific condition
        switch (criteria.toLowerCase()) {
            case "association":
                query += "LOWER(a.nom) LIKE ?";
                break;
            case "montant":
                query += "CAST(d.montant AS CHAR) LIKE ?";
                break;
            case "status":
                query += "LOWER(d.status) LIKE ?";
                break;
            default:
                query += "LOWER(a.nom) LIKE ?";
        }

        try (PreparedStatement pst = con.prepareStatement(query)) {
            pst.setInt(1, userId);
            pst.setString(2, "%" + searchText.toLowerCase() + "%");

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    donations.add(mapResultSetToDon(rs));
                }
            }
        }

        return donations;
    }
    public List<Don> getConfirmedContributionsByUser(int userId) throws SQLException {
        List<Don> confirmedContributions = new ArrayList<>();
        String query = "SELECT * FROM don WHERE id_user_id = ? AND type = 'contribution' AND status = 'confirme'";
        try (PreparedStatement pst = con.prepareStatement(query)) {
            pst.setInt(1, userId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Don don = new Don(
                            rs.getInt("id"),
                            rs.getDouble("montant"),
                            rs.getString("donorType"),
                            rs.getString("status"),
                            rs.getString("type")
                    );
                    confirmedContributions.add(don);
                }
            }
        }
        return confirmedContributions;
    }
}