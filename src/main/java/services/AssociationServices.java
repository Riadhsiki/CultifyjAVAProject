package services;

import entities.Association;
import entities.Don;
import interfaces.IServiceAssociation;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AssociationServices implements IServiceAssociation<Association> {

    private Connection con;

    public AssociationServices() {
        this.con = MyDataBase.getInstance().getConn();
    }

    @Override
    public void add(Association association) throws SQLException {
        String sql = "INSERT INTO `association`( `nom`, `description`, `contact`, `but`, `image`, `montant_desire`, `site_web`) VALUES (?,?,?,?,?,?,?)";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, association.getNom());
        ps.setString(2, association.getDescription());
        ps.setString(3, association.getContact());
        ps.setString(4, association.getBut());
        ps.setString(5, association.getImage());
        ps.setDouble(6, association.getMontantDesire());
        ps.setString(7, association.getSiteWeb());
        ps.executeUpdate();
        System.out.println("5edmt");
    }

    @Override
    public void addP(Association association) throws SQLException {
        // Implémentation à ajouter si nécessaire
    }

    // La méthode searchByName n'est plus utilisée directement pour le filtrage
    // mais nous la gardons pour la compatibilité avec le code existant
    public List<Association> searchByName(String searchText) throws SQLException {
        List<Association> associations = new ArrayList<>();
        String sql = "SELECT * FROM association WHERE nom LIKE ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, searchText + "%");
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Association association = new Association();
            association.setId(rs.getInt("id"));
            association.setNom(rs.getString("nom"));
            association.setDescription(rs.getString("description"));
            association.setContact(rs.getString("contact"));
            association.setBut(rs.getString("but"));
            association.setMontantDesire(rs.getDouble("montant_desire"));
            association.setImage(rs.getString("image"));
            association.setSiteWeb(rs.getString("site_web"));

            // Charger les dons pour calculer la progression
            loadDonsForAssociation(association);

            associations.add(association);
        }
        return associations;
    }

    // Nouvelle méthode de recherche utilisant les Streams
    public List<Association> searchAssociations(String searchText, String searchCriteria) throws SQLException {
        List<Association> allAssociations = getAll();

        if (searchText == null || searchText.isEmpty()) {
            return allAssociations;
        }

        String searchLower = searchText.toLowerCase();

        return allAssociations.stream()
                .filter(association -> {
                    switch (searchCriteria) {
                        case "nom":
                            return association.getNom() != null &&
                                    association.getNom().toLowerCase().startsWith(searchLower);
                        case "description":
                            return association.getDescription() != null &&
                                    association.getDescription().toLowerCase().contains(searchLower);
                        case "but":
                            return association.getBut() != null &&
                                    association.getBut().toLowerCase().contains(searchLower);
                        case "contact":
                            return association.getContact() != null &&
                                    association.getContact().toLowerCase().contains(searchLower);
                        case "siteWeb":
                            return association.getSiteWeb() != null &&
                                    association.getSiteWeb().toLowerCase().contains(searchLower);
                        case "montant":
                            // Convertir le montant en String et vérifier s'il contient le texte recherché
                            String montantStr = String.valueOf(association.getMontantDesire());
                            return montantStr.startsWith(searchText);
                        case "tous":
                            // Recherche dans tous les champs
                            String montantStr2 = String.valueOf(association.getMontantDesire());
                            boolean montantMatch = montantStr2.contains(searchText);

                            return (association.getNom() != null && association.getNom().toLowerCase().contains(searchLower)) ||
                                    (association.getDescription() != null && association.getDescription().toLowerCase().contains(searchLower)) ||
                                    (association.getBut() != null && association.getBut().toLowerCase().contains(searchLower)) ||
                                    (association.getContact() != null && association.getContact().toLowerCase().contains(searchLower)) ||
                                    (association.getSiteWeb() != null && association.getSiteWeb().toLowerCase().contains(searchLower)) ||
                                    montantMatch;
                        default:
                            return association.getNom() != null &&
                                    association.getNom().toLowerCase().contains(searchLower);
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public void update(Association association) throws SQLException {
        String sql = "UPDATE association SET nom =?,description = ?,contact=?,but=?,image=?,montant_desire=?,site_web=? WHERE id = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, association.getNom());
        ps.setString(2, association.getDescription());
        ps.setString(3, association.getContact());
        ps.setString(4, association.getBut());
        ps.setString(5, association.getImage());
        ps.setDouble(6, association.getMontantDesire());
        ps.setString(7, association.getSiteWeb());
        ps.setInt(8, association.getId());
        ps.executeUpdate();
    }

    @Override
    public void delete(Association association) throws SQLException {
        String sql = "DELETE FROM association WHERE id = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, association.getId());
        ps.executeUpdate();
    }

    @Override
    public List<Association> getAll() throws SQLException {
        List<Association> associations = new ArrayList<>();
        String sql = "SELECT * FROM association";
        PreparedStatement ps = con.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Association association = new Association();
            association.setId(rs.getInt("id"));
            association.setNom(rs.getString("nom"));
            association.setDescription(rs.getString("description"));
            association.setContact(rs.getString("contact"));
            association.setBut(rs.getString("but"));
            association.setMontantDesire(rs.getDouble("montant_desire"));
            association.setImage(rs.getString("image"));
            association.setSiteWeb(rs.getString("site_web"));

            // Charger les dons de l'association
            loadDonsForAssociation(association);

            associations.add(association);
        }
        return associations;
    }

    @Override
    public Association getById(int id) throws SQLException {
        String sql = "SELECT * FROM association WHERE id = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            Association association = new Association();
            association.setId(rs.getInt("id"));
            association.setNom(rs.getString("nom"));
            association.setDescription(rs.getString("description"));
            association.setContact(rs.getString("contact"));
            association.setBut(rs.getString("but"));
            association.setMontantDesire(rs.getDouble("montant_desire"));
            association.setImage(rs.getString("image"));
            association.setSiteWeb(rs.getString("site_web"));

            // Charger les dons de l'association
            loadDonsForAssociation(association);

            return association;
        } else {
            return null;
        }
    }

    // Méthode pour charger les dons d'une association
    private void loadDonsForAssociation(Association association) throws SQLException {
        String sql = "SELECT * FROM don WHERE association_id = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, association.getId());
        ResultSet rs = ps.executeQuery();

        List<Don> dons = new ArrayList<>();
        while (rs.next()) {
            Don don = new Don();
            don.setId(rs.getInt("id"));
            don.setMontant(rs.getDouble("montant"));
            don.setStatus(rs.getString("status"));

            // Ajouter d'autres champs si nécessaire

            dons.add(don);
        }

        association.setDons(dons);
    }

    public List<Association> getAllPaginated(int page, int itemsPerPage) throws SQLException {
        List<Association> associations = new ArrayList<>();
        String sql = "SELECT * FROM association LIMIT ? OFFSET ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, itemsPerPage);
        ps.setInt(2, (page - 1) * itemsPerPage);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Association association = new Association();
            association.setId(rs.getInt("id"));
            association.setNom(rs.getString("nom"));
            association.setDescription(rs.getString("description"));
            association.setContact(rs.getString("contact"));
            association.setBut(rs.getString("but"));
            association.setMontantDesire(rs.getDouble("montant_desire"));
            association.setImage(rs.getString("image"));
            association.setSiteWeb(rs.getString("site_web"));
            associations.add(association);
        }
        return associations;
    }

    public int getTotalCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM association";
        PreparedStatement ps = con.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getInt(1);
        }
        return 0;
    }
}