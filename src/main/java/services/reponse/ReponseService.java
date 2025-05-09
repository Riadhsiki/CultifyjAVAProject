package services.reponse;

import models.Reclamation;
import models.Reponse;
import utils.DataSource;

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
        this.con = DataSource.getInstance().getConnection();
    }

    @Override
    public void add(Reponse reponse) throws SQLException {
        if (reponse.getReclamation() == null || reponse.getReclamation().getId_reclamation() <= 0) {
            throw new SQLException("La réponse doit être associée à une réclamation valide");
        }

        String query = "INSERT INTO `reponse`(`reponsedate`, `titre`, `contenu`, `offre`, `id_reclamation`) VALUES (?,?,?,?,?)";
        PreparedStatement ps = con.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);

        if (reponse.getReponsedate() == null) {
            reponse.setReponsedate(new Date());
        }

        ps.setDate(1, new java.sql.Date(reponse.getReponsedate().getTime()));
        ps.setString(2, reponse.getTitre());
        ps.setString(3, reponse.getContenu());
        ps.setString(4, reponse.getOffre() != null ? reponse.getOffre() : "");
        ps.setInt(5, reponse.getReclamation().getId_reclamation());
        ps.executeUpdate();

        ResultSet generatedKeys = ps.getGeneratedKeys();
        if (generatedKeys.next()) {
            reponse.setId_reponse(generatedKeys.getInt(1));
        }

        updateReclamationStatus(reponse.getReclamation().getId_reclamation(), "Traité");

        System.out.println("Réponse ajoutée avec succès");
    }

    @Override
    public void update(Reponse reponse) throws SQLException {
        if (reponse.getReclamation() == null || reponse.getReclamation().getId_reclamation() <= 0) {
            throw new SQLException("La réponse doit être associée à une réclamation valide");
        }

        String query = "UPDATE `reponse` SET `reponsedate`=?, `titre`=?, `contenu`=?, `offre`=?, `id_reclamation`=? WHERE id_reponse=?";
        PreparedStatement ps = con.prepareStatement(query);

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
        int reclamationId = reponse.getReclamation() != null ?
                reponse.getReclamation().getId_reclamation() :
                getReclamationIdForReponse(reponse.getId_reponse());

        String query = "DELETE FROM `reponse` WHERE id_reponse=?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, reponse.getId_reponse());
        ps.executeUpdate();

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

    private void updateReclamationStatus(int reclamationId, String status) throws SQLException {
        String query = "UPDATE `reclamation` SET `statut` = ? WHERE `id_reclamation` = ?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setString(1, status);
        ps.setInt(2, reclamationId);
        ps.executeUpdate();
    }

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