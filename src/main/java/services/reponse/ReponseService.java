package services.reponse;

import models.Reclamation;
import models.Reponse;
import utils.MyDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReponseService implements IServiceReponse<Reponse> {
    private Connection con;

    public ReponseService() {
        this.con = MyDataBase.getInstance().getConn();
    }

    @Override
    public void add(Reponse reponse) throws SQLException {
        // Vérifier que la réclamation associée existe
        if (reponse.getReclamation() == null || reponse.getReclamation().getId_reclamation() <= 0) {
            throw new SQLException("La réponse doit être associée à une réclamation valide");
        }

        String query = "INSERT INTO `reponse`(`reponsedate`, `titre`, `contenu`, `offre`, `id_reclamation`) VALUES (?,?,?,?,?)";
        PreparedStatement ps = con.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);

        // Si reponsedate est null, utiliser la date actuelle
        if (reponse.getReponsedate() == null) {
            reponse.setReponsedate(new Date());
        }

        ps.setDate(1, new java.sql.Date(reponse.getReponsedate().getTime()));
        ps.setString(2, reponse.getTitre());
        ps.setString(3, reponse.getContenu());
        ps.setString(4, reponse.getOffre() != null ? reponse.getOffre() : "");
        ps.setInt(5, reponse.getReclamation().getId_reclamation());
        ps.executeUpdate();

        // Récupérer l'ID généré
        ResultSet generatedKeys = ps.getGeneratedKeys();
        if (generatedKeys.next()) {
            reponse.setId_reponse(generatedKeys.getInt(1));
        }

        // Mettre à jour le statut de la réclamation à "Traité"
        updateReclamationStatus(reponse.getReclamation().getId_reclamation(), "Traité");

        System.out.println("Réponse ajoutée avec succès");
    }

    @Override
    public void update(Reponse reponse) throws SQLException {
        // Vérifier que la réclamation associée existe
        if (reponse.getReclamation() == null || reponse.getReclamation().getId_reclamation() <= 0) {
            throw new SQLException("La réponse doit être associée à une réclamation valide");
        }

        String query = "UPDATE `reponse` SET `reponsedate`=?, `titre`=?, `contenu`=?, `offre`=?, `id_reclamation`=? WHERE id_reponse=?";
        PreparedStatement ps = con.prepareStatement(query);

        // Si reponsedate est null, utiliser la date actuelle
        if (reponse.getReponsedate() == null) {
            reponse.setReponsedate(new Date());
        }

        ps.setDate(1, new java.sql.Date(reponse.getReponsedate().getTime()));
        ps.setString(2, reponse.getTitre());
        ps.setString(3, reponse.getContenu());
        ps.setString(4, reponse.getOffre() != null ? reponse.getOffre() : "");
        ps.setInt(5, reponse.getReclamation().getId_reclamation());
        ps.setInt(6, reponse.getId_reponse());
        ps.executeUpdate();

        System.out.println("Réponse mise à jour avec succès");
    }

    @Override
    public void delete(Reponse reponse) throws SQLException {
        // Récupérer l'ID de la réclamation avant de supprimer la réponse
        int reclamationId = reponse.getReclamation() != null ?
                reponse.getReclamation().getId_reclamation() :
                getReclamationIdForReponse(reponse.getId_reponse());

        // Supprimer la réponse
        String query = "DELETE FROM `reponse` WHERE id_reponse=?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, reponse.getId_reponse());
        ps.executeUpdate();

        // Mettre à jour le statut de la réclamation à "Non traité" si un ID de réclamation a été trouvé
        if (reclamationId > 0) {
            updateReclamationStatus(reclamationId, "Non traité");
        }

        System.out.println("Réponse supprimée avec succès");
    }

    @Override
    public List<Reponse> getAll() throws SQLException {
        String query = "SELECT p.*, r.id_reclamation, r.titre as reclamation_titre, r.type, r.statut, r.priorite " +
                "FROM `reponse` p LEFT JOIN `reclamation` r ON p.id_reclamation = r.id_reclamation";
        PreparedStatement ps = con.prepareStatement(query);
        ResultSet rs = ps.executeQuery();
        List<Reponse> reponses = new ArrayList<>();

        while (rs.next()) {
            Reponse reponse = new Reponse();
            reponse.setId_reponse(rs.getInt("id_reponse"));
            java.sql.Date sqlDate = rs.getDate("reponsedate");
            reponse.setReponsedate(sqlDate != null ? new Date(sqlDate.getTime()) : null);
            reponse.setTitre(rs.getString("titre"));
            reponse.setContenu(rs.getString("contenu"));
            reponse.setOffre(rs.getString("offre"));

            // Associer la réclamation si elle existe
            if (rs.getObject("id_reclamation") != null) {
                Reclamation reclamation = new Reclamation();
                reclamation.setId_reclamation(rs.getInt("id_reclamation"));
                reclamation.setTitre(rs.getString("reclamation_titre"));
                reclamation.setType(rs.getString("type"));
                reclamation.setStatut(rs.getString("statut"));
                reclamation.setPriorite(rs.getString("priorite"));
                reponse.setReclamation(reclamation);
            }

            reponses.add(reponse);
        }
        return reponses;
    }

    @Override
    public Reponse getById(int id) throws SQLException {
        String query = "SELECT p.*, r.id_reclamation, r.titre as reclamation_titre, r.type, r.statut, r.priorite " +
                "FROM `reponse` p LEFT JOIN `reclamation` r ON p.id_reclamation = r.id_reclamation " +
                "WHERE p.id_reponse = ?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            Reponse reponse = new Reponse();
            reponse.setId_reponse(rs.getInt("id_reponse"));
            java.sql.Date sqlDate = rs.getDate("reponsedate");
            reponse.setReponsedate(sqlDate != null ? new Date(sqlDate.getTime()) : null);
            reponse.setTitre(rs.getString("titre"));
            reponse.setContenu(rs.getString("contenu"));
            reponse.setOffre(rs.getString("offre"));

            if (rs.getObject("id_reclamation") != null) {
                Reclamation reclamation = new Reclamation();
                reclamation.setId_reclamation(rs.getInt("id_reclamation"));
                reclamation.setTitre(rs.getString("reclamation_titre"));
                reclamation.setType(rs.getString("type"));
                reclamation.setStatut(rs.getString("statut"));
                reclamation.setPriorite(rs.getString("priorite"));
                reponse.setReclamation(reclamation);
            }

            return reponse;
        }
        return null;
    }

    public Reponse getByReclamationId(int reclamationId) throws SQLException {
        String query = "SELECT p.*, r.id_reclamation, r.titre as reclamation_titre, r.type, r.statut, r.priorite " +
                "FROM `reponse` p LEFT JOIN `reclamation` r ON p.id_reclamation = r.id_reclamation " +
                "WHERE p.id_reclamation = ?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, reclamationId);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            Reponse reponse = new Reponse();
            reponse.setId_reponse(rs.getInt("id_reponse"));
            java.sql.Date sqlDate = rs.getDate("reponsedate");
            reponse.setReponsedate(sqlDate != null ? new Date(sqlDate.getTime()) : null);
            reponse.setTitre(rs.getString("titre"));
            reponse.setContenu(rs.getString("contenu"));
            reponse.setOffre(rs.getString("offre"));

            // Créer et associer la réclamation
            Reclamation reclamation = new Reclamation();
            reclamation.setId_reclamation(rs.getInt("id_reclamation"));
            reclamation.setTitre(rs.getString("reclamation_titre"));
            reclamation.setType(rs.getString("type"));
            reclamation.setStatut(rs.getString("statut"));
            reclamation.setPriorite(rs.getString("priorite"));
            reponse.setReclamation(reclamation);

            return reponse;
        }
        return null;
    }

    // Méthode pour mettre à jour le statut d'une réclamation
    private void updateReclamationStatus(int reclamationId, String status) throws SQLException {
        String query = "UPDATE `reclamation` SET `statut` = ? WHERE `id_reclamation` = ?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setString(1, status);
        ps.setInt(2, reclamationId);
        ps.executeUpdate();
    }

    // Méthode pour trouver l'ID de la réclamation associée à une réponse
    private int getReclamationIdForReponse(int reponseId) throws SQLException {
        String query = "SELECT `id_reclamation` FROM `reponse` WHERE id_reponse = ?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, reponseId);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return rs.getInt("id_reclamation");
        }
        return -1;
    }
}