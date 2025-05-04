package services.eventreservation;

import models.Event;
import utils.DataSource;

import java.sql.*;
import java.util.*;

public class EventService implements IService<Event> {
    private Connection connection;

    public EventService() {
        connection = DataSource.getInstance().getConnection();
    }


    @Override
    public void add(Event event) throws SQLException {
        String sql = "INSERT INTO event (titre, description, dateE, organisation, capacite, nbplaces, categorie, prix, image) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setEventParameters(ps, event);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    event.setIdE(rs.getInt(1));
                }
            }
        }
    }

    @Override
    public void update(Event event) throws SQLException {
        String sql = "UPDATE event SET titre = ?, description = ?, dateE = ?, organisation = ?, " +
                "capacite = ?, nbplaces = ?, categorie = ?, prix = ?, image = ? WHERE idE = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setEventParameters(ps, event);
            ps.setInt(10, event.getIdE());
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(Event event) throws SQLException {
        String sql = "DELETE FROM event WHERE idE = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, event.getIdE());
            ps.executeUpdate();
        }
    }

    @Override
    public List<Event> getAll() throws SQLException {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT * FROM event";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                events.add(extractEventFromResultSet(rs));
            }
        }
        return events;
    }

    // Nouvelle méthode pour la recherche par titre
    public List<Event> searchByTitle(String title) throws SQLException {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT * FROM event WHERE LOWER(titre) LIKE LOWER(?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, "%" + title + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    events.add(extractEventFromResultSet(rs));
                }
            }
        }
        return events;
    }

    // Nouvelle méthode pour le tri par date
    public List<Event> getAllSortedByDate() throws SQLException {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT * FROM event ORDER BY dateE";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                events.add(extractEventFromResultSet(rs));
            }
        }
        return events;
    }

    // Méthodes statistiques
    public Map<String, Integer> countEventsByCategory() throws SQLException {
        Map<String, Integer> stats = new HashMap<>();
        String sql = "SELECT categorie, COUNT(*) as count FROM event GROUP BY categorie";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                stats.put(rs.getString("categorie"), rs.getInt("count"));
            }
        }
        return stats;
    }

    public Map<String, Double> averagePriceByCategory() throws SQLException {
        Map<String, Double> stats = new HashMap<>();
        String sql = "SELECT categorie, AVG(prix) as avg_price FROM event GROUP BY categorie";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                stats.put(rs.getString("categorie"), rs.getDouble("avg_price"));
            }
        }
        return stats;
    }

    public Map<String, Double> averageCapacityByCategory() throws SQLException {
        Map<String, Double> stats = new HashMap<>();
        String sql = "SELECT categorie, AVG(capacite) as avg_capacity FROM event GROUP BY categorie";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                stats.put(rs.getString("categorie"), rs.getDouble("avg_capacity"));
            }
        }
        return stats;
    }

    public Map<String, Double> occupancyRateByCategory() throws SQLException {
        Map<String, Double> stats = new HashMap<>();
        String sql = "SELECT categorie, AVG((capacite - nbplaces) * 100.0 / capacite) as rate FROM event WHERE capacite > 0 GROUP BY categorie";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                stats.put(rs.getString("categorie"), rs.getDouble("rate"));
            }
        }
        return stats;
    }

    public int getTotalEvents() throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM event";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt("total") : 0;
        }
    }

    private void setEventParameters(PreparedStatement ps, Event event) throws SQLException {
        ps.setString(1, event.getTitre());
        ps.setString(2, event.getDescription());
        ps.setDate(3, new java.sql.Date(event.getDateE().getTime()));
        ps.setString(4, event.getOrganisation());
        ps.setInt(5, event.getCapacite());
        ps.setInt(6, event.getNbplaces());
        ps.setString(7, event.getCategorie());
        ps.setFloat(8, event.getPrix());
        ps.setString(9, event.getImage());
    }

    private Event extractEventFromResultSet(ResultSet rs) throws SQLException {
        Event event = new Event();
        event.setIdE(rs.getInt("idE"));
        event.setTitre(rs.getString("titre"));
        event.setDescription(rs.getString("description"));
        event.setDateE(rs.getDate("dateE"));
        event.setOrganisation(rs.getString("organisation"));
        event.setCapacite(rs.getInt("capacite"));
        event.setNbplaces(rs.getInt("nbplaces"));
        event.setCategorie(rs.getString("categorie"));
        event.setPrix(rs.getFloat("prix"));
        event.setImage(rs.getString("image"));
        return event;
    }

    @Override
    public List<Event> select() throws SQLException {
        return getAll();
    }

    public Event getById(int id) throws SQLException {
        String sql = "SELECT * FROM event WHERE idE = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? extractEventFromResultSet(rs) : null;
            }
        }
    }
    public List<Event> getAllSortedByPrice() throws SQLException {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT * FROM event ORDER BY prix";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                events.add(extractEventFromResultSet(rs));
            }
        }
        return events;
    }

    public List<Event> getAllSortedByPopularity() throws SQLException {
        List<Event> events = getAll(); // Appelle la méthode existante qui récupère tous les événements
        events.sort(Comparator.comparingInt(Event::getNbplaces).reversed()); // Trie du plus populaire au moins populaire
        return events;
    }

    public List<Event> getEventsByOrganisation(String organisation) throws SQLException {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT * FROM event WHERE organisation = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, organisation);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    events.add(extractEventFromResultSet(rs));
                }
            }
        }
        return events;
    }
}