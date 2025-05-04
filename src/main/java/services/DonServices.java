package services;

import entities.Association;
import entities.Don;
import entities.User;
import interfaces.IServiceDon;
import utils.MyDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DonServices implements IServiceDon<Don> {

    private Connection con;

    public DonServices() {
        this.con = MyDataBase.getInstance().getConn();
    }

    @Override
    public void Add(int userId, Don don) throws SQLException {
        String sql = "INSERT INTO `don`(`id_user_id`, `association_id`, `montant`, `donor_type`, `status`, `type`) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, don.getAssociation().getId());
            ps.setDouble(3, don.getMontant());
            ps.setString(4, don.getDonorType());
            ps.setString(5, don.getStatus());
            ps.setString(6, don.getType());
            ps.executeUpdate();
        }
    }

    @Override
    public void update(Don don, int newAssociationId) throws SQLException {
        String sql = "UPDATE `don` SET `montant`=?, `donor_type`=?, `status`=?, `type`=?, `association_id`=?, `id_user_id`=? WHERE id=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, don.getMontant());
            ps.setString(2, don.getDonorType());
            ps.setString(3, don.getStatus());
            ps.setString(4, don.getType());
            ps.setInt(5, newAssociationId);
            ps.setInt(6, don.getUser().getId());
            ps.setInt(7, don.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(Don don) throws SQLException {
        String sql = "DELETE FROM `don` WHERE id=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, don.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public List<Don> getDonsByUser(int userId) throws SQLException {
        List<Don> dons = new ArrayList<>();
        String sql = "SELECT d.*, a.nom AS association_nom FROM don d " +
                "LEFT JOIN association a ON d.association_id = a.id " +
                "WHERE d.id_user_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    dons.add(mapDonFromResultSet(rs));
                }
            }
        }
        return dons;
    }

    @Override
    public List<Don> getDonsByAssociation(int associationId) throws SQLException {
        List<Don> dons = new ArrayList<>();
        String sql = "SELECT d.*, a.nom AS association_nom FROM don d " +
                "LEFT JOIN association a ON d.association_id = a.id " +
                "WHERE d.association_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, associationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    dons.add(mapDonFromResultSet(rs));
                }
            }
        }
        return dons;
    }

    @Override
    public List<Don> getAll() throws SQLException {
        List<Don> dons = new ArrayList<>();
        String sql = "SELECT d.*, a.nom AS association_nom FROM don d " +
                "LEFT JOIN association a ON d.association_id = a.id";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                dons.add(mapDonFromResultSet(rs));
            }
        }
        return dons;
    }

    private Don mapDonFromResultSet(ResultSet rs) throws SQLException {
        Don don = new Don();
        don.setId(rs.getInt("id"));

        don.setMontant(rs.getDouble("montant"));
        don.setDonorType(rs.getString("donor_type"));
        don.setStatus(rs.getString("status"));
        don.setType(rs.getString("type"));

        User user = new User();
        user.setId(rs.getInt("id_user_id"));
        //user.setName(rs.getString("nom"));
        don.setUser(user);

        Association association = new Association();
        association.setId(rs.getInt("association_id"));
        association.setNom(rs.getString("association_nom"));
        don.setAssociation(association);

        return don;
    }

    @Override
    public Don getDonDetails(int donId) throws SQLException {
        String sql = "SELECT d.*, a.nom AS association_nom FROM don d " +
                "LEFT JOIN association a ON d.association_id = a.id WHERE d.id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, donId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapDonFromResultSet(rs);
                }
            }
        }
        return null;
    }

    public List<Don> searchByAssociationName(String name) throws SQLException {
        List<Don> dons = new ArrayList<>();
        String sql = "SELECT d.*, a.nom AS association_nom FROM don d " +
                "LEFT JOIN association a ON d.association_id = a.id " +
                "WHERE a.nom LIKE ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + name + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    dons.add(mapDonFromResultSet(rs));
                }
            }
        }
        return dons;
    }

    public List<Don> searchByStatus(String status) throws SQLException {
        List<Don> dons = new ArrayList<>();
        String sql = "SELECT d.*, a.nom AS association_nom FROM don d " +
                "LEFT JOIN association a ON d.association_id = a.id " +
                "WHERE d.status = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    dons.add(mapDonFromResultSet(rs));
                }
            }
        }
        return dons;
    }
    public  List<Don> getByUSer(int userId) throws SQLException {
        List<Don> dons = new ArrayList<>();
        String sql ="SELECT * FROM DON WHERE id_user_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    dons.add(mapDonFromResultSet(rs));
                }
            }
        }
        return dons;
    }
    public List<Don> searchDons(String searchText, String searchCriteria, int userId) throws SQLException {
        // Récupérer uniquement les dons de l'utilisateur spécifié
        List<Don> userDons = getDonsByUser(userId);

        if (searchText == null || searchText.isEmpty()) {
            return userDons;
        }

        String searchLower = searchText.toLowerCase();

        return userDons.stream()
                .filter(don -> {
                    switch (searchCriteria) {
                        case "association":
                            return don.getAssociation() != null &&
                                    don.getAssociation().getNom() != null &&
                                    don.getAssociation().getNom().toLowerCase().startsWith(searchLower);
                        case "montant":
                            try {
                                return String.valueOf(don.getMontant()).startsWith(searchText);
                            } catch (NumberFormatException e) {
                                return false;
                            }
                        case "status":
                            return don.getStatus() != null &&
                                    don.getStatus().toLowerCase().startsWith(searchLower);
                        default:
                            return false;
                    }
                })
                .collect(Collectors.toList());
    }

}