package services.reclamation;

import models.Reclamation;
import models.Reponse;
import services.reponse.ReponseService;
import utils.MyDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReclamationService implements IServiceReclamation<Reclamation> {
    private Connection con;
    private ReponseService reponseService;

    public ReclamationService() {
        this.con = MyDataBase.getInstance().getConn();
        // Initialiser le ReponseService ici pour éviter NullPointerException
        this.reponseService = new ReponseService();
    }

    // Méthode pour définir le ReponseService manuellement (si nécessaire)
    public void setReponseService(ReponseService reponseService) {
        this.reponseService = reponseService;
    }

    @Override
    public void add(Reclamation reclamation) throws SQLException {
        String query = "INSERT INTO `reclamation`(`type`, `titre`, `description`, `statut`, `priorite`, `email`) VALUES (?,?,?,?,?,?)";
        PreparedStatement ps = con.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
        ps.setString(1, reclamation.getType());
        ps.setString(2, reclamation.getTitre());
        ps.setString(3, reclamation.getDescription());
        ps.setString(4, reclamation.getStatut());
        ps.setString(5, reclamation.getPriorite());
        ps.setString(6, reclamation.getEmail());
        ps.executeUpdate();

        // Récupérer l'ID généré
        ResultSet generatedKeys = ps.getGeneratedKeys();
        if (generatedKeys.next()) {
            reclamation.setId_reclamation(generatedKeys.getInt(1));
        }

        // Si la réclamation a une réponse associée, l'ajouter
        if (reclamation.getReponse() != null && reponseService != null) {
            reclamation.getReponse().setReclamation(reclamation);
            reponseService.add(reclamation.getReponse());
        }

        System.out.println("Reclamation added");
    }

    @Override
    public void update(Reclamation reclamation) throws SQLException {
        String query = "UPDATE `reclamation` SET `type`=?, `titre`=?, `description`=?, `statut`=?, `priorite`=?, `email`=? WHERE id_reclamation=?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setString(1, reclamation.getType());
        ps.setString(2, reclamation.getTitre());
        ps.setString(3, reclamation.getDescription());
        ps.setString(4, reclamation.getStatut());
        ps.setString(5, reclamation.getPriorite());
        ps.setString(6, reclamation.getEmail());
        ps.setInt(7, reclamation.getId_reclamation());
        ps.executeUpdate();

        // Mettre à jour la réponse associée si elle existe
        if (reclamation.getReponse() != null && reponseService != null) {
            // S'assurer que la réponse est correctement liée à la réclamation
            reclamation.getReponse().setReclamation(reclamation);

            // Vérifier si la réponse existe déjà dans la base de données
            Reponse existingReponse = reponseService.getByReclamationId(reclamation.getId_reclamation());

            if (existingReponse != null) {
                // Si elle existe, mettre à jour son ID pour que la mise à jour fonctionne
                reclamation.getReponse().setId_reponse(existingReponse.getId_reponse());
                reponseService.update(reclamation.getReponse());
            } else {
                // Si elle n'existe pas, l'ajouter
                reponseService.add(reclamation.getReponse());
            }
        }

        System.out.println("Reclamation updated");
    }

    @Override
    public void delete(Reclamation reclamation) throws SQLException {
        // Vérifier si reponseService est initialisé
        if (reponseService == null) {
            reponseService = new ReponseService();
        }

        // Vérifier d'abord si la réclamation a une réponse associée
        Reponse existingReponse = reponseService.getByReclamationId(reclamation.getId_reclamation());

        // Si une réponse existe, la supprimer avant de supprimer la réclamation
        if (existingReponse != null) {
            reponseService.delete(existingReponse);
        }

        // Ensuite supprimer la réclamation
        String query = "DELETE FROM `reclamation` WHERE `id_reclamation` = ?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, reclamation.getId_reclamation());
        ps.executeUpdate();
        System.out.println("Reclamation and associated Reponse deleted");
    }

    @Override
    public List<Reclamation> getAll() throws SQLException {
        List<Reclamation> reclamations = new ArrayList<>();
        String query = "SELECT r.*, p.id_reponse, p.reponsedate, p.titre as reponse_titre, p.contenu, p.offre " +
                "FROM `reclamation` r LEFT JOIN `reponse` p ON r.id_reclamation = p.id_reclamation";
        PreparedStatement ps = con.prepareStatement(query);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            Reclamation reclamation = new Reclamation();
            reclamation.setId_reclamation(rs.getInt("id_reclamation"));
            reclamation.setType(rs.getString("type"));
            reclamation.setTitre(rs.getString("titre"));
            reclamation.setDescription(rs.getString("description"));
            reclamation.setStatut(rs.getString("statut"));
            reclamation.setPriorite(rs.getString("priorite"));
            reclamation.setEmail(rs.getString("email"));

            // Si une réponse existe, la créer et l'associer
            if (rs.getObject("id_reponse") != null) {
                Reponse reponse = new Reponse();
                reponse.setId_reponse(rs.getInt("id_reponse"));
                reponse.setReponsedate(rs.getDate("reponsedate"));
                reponse.setTitre(rs.getString("reponse_titre"));
                reponse.setContenu(rs.getString("contenu"));
                reponse.setOffre(rs.getString("offre"));
                reponse.setReclamation(reclamation);
                reclamation.setReponse(reponse);
            }

            reclamations.add(reclamation);
        }
        return reclamations;
    }

    @Override
    public Reclamation getById(int id) throws SQLException {
        String query = "SELECT r.*, p.id_reponse, p.reponsedate, p.titre as reponse_titre, p.contenu, p.offre " +
                "FROM `reclamation` r LEFT JOIN `reponse` p ON r.id_reclamation = p.id_reclamation " +
                "WHERE r.id_reclamation = ?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            Reclamation reclamation = new Reclamation();
            reclamation.setId_reclamation(rs.getInt("id_reclamation"));
            reclamation.setType(rs.getString("type"));
            reclamation.setTitre(rs.getString("titre"));
            reclamation.setDescription(rs.getString("description"));
            reclamation.setStatut(rs.getString("statut"));
            reclamation.setPriorite(rs.getString("priorite"));
            reclamation.setEmail(rs.getString("email"));

            if (rs.getObject("id_reponse") != null) {
                Reponse reponse = new Reponse();
                reponse.setId_reponse(rs.getInt("id_reponse"));
                reponse.setReponsedate(rs.getDate("reponsedate"));
                reponse.setTitre(rs.getString("reponse_titre"));
                reponse.setContenu(rs.getString("contenu"));
                reponse.setOffre(rs.getString("offre"));
                reponse.setReclamation(reclamation);
                reclamation.setReponse(reponse);
            }

            return reclamation;
        }
        return null;
    }

    // Méthode pour récupérer toutes les réclamations non traitées
    public List<Reclamation> getAllNonTraitees() throws SQLException {
        List<Reclamation> reclamationsNonTraitees = new ArrayList<>();
        String query = "SELECT r.*, p.id_reponse, p.reponsedate, p.titre as reponse_titre, p.contenu, p.offre " +
                "FROM `reclamation` r LEFT JOIN `reponse` p ON r.id_reclamation = p.id_reclamation " +
                "WHERE r.statut != 'Traité'";
        PreparedStatement ps = con.prepareStatement(query);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            Reclamation reclamation = new Reclamation();
            reclamation.setId_reclamation(rs.getInt("id_reclamation"));
            reclamation.setType(rs.getString("type"));
            reclamation.setTitre(rs.getString("titre"));
            reclamation.setDescription(rs.getString("description"));
            reclamation.setStatut(rs.getString("statut"));
            reclamation.setPriorite(rs.getString("priorite"));
            reclamation.setEmail(rs.getString("email"));

            // Si une réponse existe, la créer et l'associer
            if (rs.getObject("id_reponse") != null) {
                Reponse reponse = new Reponse();
                reponse.setId_reponse(rs.getInt("id_reponse"));
                reponse.setReponsedate(rs.getDate("reponsedate"));
                reponse.setTitre(rs.getString("reponse_titre"));
                reponse.setContenu(rs.getString("contenu"));
                reponse.setOffre(rs.getString("offre"));
                reponse.setReclamation(reclamation);
                reclamation.setReponse(reponse);
            }

            reclamationsNonTraitees.add(reclamation);
        }
        return reclamationsNonTraitees;
    }
}