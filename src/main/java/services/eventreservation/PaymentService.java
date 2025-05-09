package services.eventreservation;

import models.Event;
import models.Payment;
import utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaymentService {
    private Connection connection;

    public PaymentService() {
        connection = DataSource.getInstance().getConnection();
    }

    public boolean processPayment(Payment payment) {
        String sql = "INSERT INTO payment (idE, idU, amount, payment_date, payment_method, transaction_reference, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, payment.getIdE());
            ps.setInt(2, payment.getIdU());
            ps.setFloat(3, payment.getAmount());
            ps.setTimestamp(4, payment.getPaymentDate());
            ps.setString(5, payment.getPaymentMethod());
            ps.setString(6, payment.getTransactionReference());
            ps.setString(7, payment.getStatus());

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        payment.setIdP(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void updateEventPlaces(int eventId, int nbTickets) throws SQLException {
        String sql = "UPDATE event SET nbplaces = nbplaces - ? WHERE idE = ? AND nbplaces >= ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, nbTickets);
            ps.setInt(2, eventId);
            ps.setInt(3, nbTickets);
            ps.executeUpdate();
        }
    }

    public List<Payment> getPaymentsByUser(int userId) {
        List<Payment> payments = new ArrayList<>();
        String sql = "SELECT p.*, e.* FROM payment p JOIN event e ON p.idE = e.idE WHERE p.idU = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Payment payment = new Payment();
                    payment.setIdP(rs.getInt("idP"));
                    payment.setIdE(rs.getInt("idE"));
                    payment.setIdU(rs.getInt("idU"));
                    payment.setAmount(rs.getFloat("amount"));
                    payment.setPaymentDate(rs.getTimestamp("payment_date"));
                    payment.setPaymentMethod(rs.getString("payment_method"));
                    payment.setTransactionReference(rs.getString("transaction_reference"));
                    payment.setStatus(rs.getString("status"));

                    Event event = new Event();
                    event.setIdE(rs.getInt("idE"));
                    event.setTitre(rs.getString("titre"));
                    event.setPrix(rs.getFloat("prix"));
                    // Ajouter d'autres propriétés de l'événement si nécessaire

                    payment.setEvent(event);
                    payments.add(payment);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return payments;
    }
}