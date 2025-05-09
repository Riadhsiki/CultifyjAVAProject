package services.eventreservation;

import models.Event;
import models.Reservation;
import utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservationService implements IService<Reservation> {
    private Connection connection;

    public ReservationService() {
        connection = DataSource.getInstance().getConnection();
    }

    @Override
    public void add(Reservation reservation) throws SQLException {
        String sql = "INSERT INTO reservation (etat, dateR, theme, url, nbTickets, idE) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, reservation.getEtat());
            preparedStatement.setDate(2, Date.valueOf(reservation.getDateR()));
            preparedStatement.setString(3, reservation.getTheme());
            preparedStatement.setString(4, reservation.getUrl());
            preparedStatement.setInt(5, reservation.getNbTickets());
            preparedStatement.setInt(6, reservation.getEvent().getIdE());

            preparedStatement.executeUpdate();

            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    reservation.setIdR(generatedKeys.getInt(1));
                }
            }
        }
    }

    @Override
    public void update(Reservation reservation) throws SQLException {
        String sql = "UPDATE reservation SET etat = ?, dateR = ?, theme = ?, url = ?, " +
                "nbTickets = ?, idE = ? WHERE idR = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, reservation.getEtat());
            preparedStatement.setDate(2, Date.valueOf(reservation.getDateR()));
            preparedStatement.setString(3, reservation.getTheme());
            preparedStatement.setString(4, reservation.getUrl());
            preparedStatement.setInt(5, reservation.getNbTickets());
            preparedStatement.setInt(6, reservation.getEvent().getIdE());
            preparedStatement.setInt(7, reservation.getIdR());

            preparedStatement.executeUpdate();
        }
    }

    @Override
    public void delete(Reservation reservation) throws SQLException {
        String sql = "DELETE FROM reservation WHERE idR = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, reservation.getIdR());
            preparedStatement.executeUpdate();
        }
    }

    @Override
    public List<Reservation> getAll() throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT r.*, e.titre as event_titre FROM reservation r LEFT JOIN event e ON r.idE = e.idE";

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                Reservation reservation = new Reservation();
                reservation.setIdR(resultSet.getInt("idR"));
                reservation.setEtat(resultSet.getString("etat"));
                reservation.setDateR(resultSet.getDate("dateR").toLocalDate());
                reservation.setTheme(resultSet.getString("theme"));
                reservation.setUrl(resultSet.getString("url"));
                reservation.setNbTickets(resultSet.getInt("nbTickets"));

                Event event = new Event();
                event.setIdE(resultSet.getInt("idE"));
                event.setTitre(resultSet.getString("event_titre"));
                reservation.setEvent(event);

                reservations.add(reservation);
            }
        }
        return reservations;
    }

    @Override
    public List<Reservation> select() throws SQLException {
        return getAll();
    }

    public Reservation getById(int id) throws SQLException {
        String sql = "SELECT r.*, e.titre as event_titre FROM reservation r LEFT JOIN event e ON r.idE = e.idE WHERE idR = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    Reservation reservation = new Reservation();
                    reservation.setIdR(resultSet.getInt("idR"));
                    reservation.setEtat(resultSet.getString("etat"));
                    reservation.setDateR(resultSet.getDate("dateR").toLocalDate());
                    reservation.setTheme(resultSet.getString("theme"));
                    reservation.setUrl(resultSet.getString("url"));
                    reservation.setNbTickets(resultSet.getInt("nbTickets"));

                    Event event = new Event();
                    event.setIdE(resultSet.getInt("idE"));
                    event.setTitre(resultSet.getString("event_titre"));
                    reservation.setEvent(event);

                    return reservation;
                }
            }
        }
        return null;
    }

}