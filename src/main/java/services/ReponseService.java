package Services;

import Entities.Reponse;
import Entities.Reclamation;
import Interfaces.IServiceReponse;
import Utils.MyDataBase;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

public class ReponseService implements IServiceReponse<Reponse> {
    private Connection con;

    public ReponseService() {
        this.con = MyDataBase.getInstance().getConn();
    }

    @Override
    public void add(Reponse reponse) throws SQLException {
        String query = "INSERT INTO `reponse`(`reponsedate`, `titre`, `contenu`, `offre`, `id_reclamation`) VALUES (?,?,?,?,?)";
        PreparedStatement ps = con.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
        ps.setDate(1, reponse.getReponsedate() != null ? new java.sql.Date(reponse.getReponsedate().getTime()) : null);
        ps.setString(2, reponse.getTitre());
        ps.setString(3, reponse.getContenu());
        ps.setString(4, reponse.getOffre());
        ps.setInt(5, reponse.getReclamation().getId_reclamation());
        ps.executeUpdate();

        // Récupérer l'ID généré
        ResultSet generatedKeys = ps.getGeneratedKeys();
        if (generatedKeys.next()) {
            reponse.setId_reponse(generatedKeys.getInt(1));
        }
    }

    @Override
    public void update(Reponse reponse) throws SQLException {
        String query = "UPDATE `reponse` SET `reponsedate`=?, `titre`=?, `contenu`=?, `offre`=?, `id_reclamation`=? WHERE id_reponse=?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setDate(1, reponse.getReponsedate() != null ? new java.sql.Date(reponse.getReponsedate().getTime()) : null);
        ps.setString(2, reponse.getTitre());
        ps.setString(3, reponse.getContenu());
        ps.setString(4, reponse.getOffre());
        ps.setInt(5, reponse.getReclamation().getId_reclamation());
        ps.setInt(6, reponse.getId_reponse());
        ps.executeUpdate();
    }

    @Override
    public void delete(Reponse reponse) throws SQLException {
        // Juste supprimer la réponse, la réclamation reste
        String query = "DELETE FROM `reponse` WHERE id_reponse=?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, reponse.getId_reponse());
        ps.executeUpdate();
    }

    @Override
    public List<Reponse> getAll() throws SQLException {
        String query = "SELECT p.*, r.id_reclamation, r.titre as reclamation_titre " +
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
            if (rs.getInt("id_reclamation") != 0) {
                Reclamation reclamation = new Reclamation();
                reclamation.setId_reclamation(rs.getInt("id_reclamation"));
                reclamation.setTitre(rs.getString("reclamation_titre"));
                reponse.setReclamation(reclamation);
            }

            reponses.add(reponse);
        }
        return reponses;
    }

    @Override
    public Reponse getById(int id) throws SQLException {
        String query = "SELECT p.*, r.id_reclamation, r.titre as reclamation_titre " +
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

            if (rs.getInt("id_reclamation") != 0) {
                Reclamation reclamation = new Reclamation();
                reclamation.setId_reclamation(rs.getInt("id_reclamation"));
                reclamation.setTitre(rs.getString("reclamation_titre"));
                reponse.setReclamation(reclamation);
            }

            return reponse;
        }
        return null;
    }

    public Reponse getByReclamationId(int reclamationId) throws SQLException {
        String query = "SELECT * FROM `reponse` WHERE id_reclamation = ?";
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
            return reponse;
        }
        return null;
    }
}