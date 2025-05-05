package Services;

import Models.ContenuMultiMedia;
import Utils.MyConnection;
import java.sql.*;
import java.sql.Date;
import java.util.*;

public class ContenuMultiMediaService implements IService<ContenuMultiMedia> {
    private Connection conn = MyConnection.getMyConnection().getConnection();

    @Override
    public void add(ContenuMultiMedia contenu) throws SQLException {
        String query = "INSERT INTO contenu_multi_media (titre_media, text_media, photo_media, categorie_media, date_media) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, contenu.getTitre_media());
            pstmt.setString(2, contenu.getText_media());
            pstmt.setString(3, contenu.getPhoto_media());
            pstmt.setString(4, contenu.getCategorie_media());
            pstmt.setTimestamp(5, new Timestamp(contenu.getDate_media().getTime()));
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) contenu.setId_contenu(rs.getInt(1));
            }
        }
    }

    @Override
    public void update(ContenuMultiMedia contenu) throws SQLException {
        String query = "UPDATE contenu_multi_media SET titre_media=?, text_media=?, photo_media=?, categorie_media=? WHERE id_contenu=?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, contenu.getTitre_media());
            pstmt.setString(2, contenu.getText_media());
            pstmt.setString(3, contenu.getPhoto_media());
            pstmt.setString(4, contenu.getCategorie_media());
            pstmt.setInt(5, contenu.getId_contenu());
            pstmt.executeUpdate();
        }
    }

    @Override
    public void delete(ContenuMultiMedia contenu) throws SQLException {
        String query = "DELETE FROM contenu_multi_media WHERE id_contenu=?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, contenu.getId_contenu());
            pstmt.executeUpdate();
        }
    }

    @Override
    public List<ContenuMultiMedia> getAll() throws SQLException {
        List<ContenuMultiMedia> contenus = new ArrayList<>();
        String query = "SELECT * FROM contenu_multi_media";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                contenus.add(new ContenuMultiMedia(
                        rs.getInt("id_contenu"),
                        rs.getString("titre_media"),
                        rs.getString("text_media"),
                        rs.getString("photo_media"),
                        rs.getString("categorie_media"),
                        rs.getTimestamp("date_media")
                ));
            }
        }
        return contenus;
    }
    public Map<String, Integer> getStatsByCategory() throws SQLException {
        Map<String, Integer> stats = new HashMap<>();
        String query = "SELECT categorie_media, COUNT(*) as count FROM contenu_multi_media GROUP BY categorie_media";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                stats.put(rs.getString("categorie_media"), rs.getInt("count"));
            }
        }
        return stats;
    }

    public Map<java.util.Date, Integer> getCreationTrend() throws SQLException {
        Map<java.util.Date, Integer> trends = new LinkedHashMap<>();
        String query = "SELECT DATE(date_media) as creation_date, COUNT(*) as count "
                + "FROM contenu_multi_media GROUP BY DATE(date_media) ORDER BY creation_date";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                java.sql.Date sqlDate = rs.getDate("creation_date");
                trends.put(new java.util.Date(sqlDate.getTime()), rs.getInt("count"));
            }
        }
        return trends;
    }
}