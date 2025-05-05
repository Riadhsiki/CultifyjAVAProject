package Services;

import Models.Quiz;
import Utils.MyConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javafx.concurrent.Task;
import javafx.fxml.FXML;

public class QuizService implements IService<Quiz> {
    private Connection conn = MyConnection.getMyConnection().getConnection();

    @Override
    public void add(Quiz quiz) throws SQLException {
        String query = "INSERT INTO quiz (contenu_id, titre_quiz, date_quiz, score_quiz, reponse_choisit) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, quiz.getContenu_id());
            pstmt.setString(2, quiz.getTitre_quiz());
            pstmt.setTimestamp(3, new Timestamp(quiz.getDate_quiz().getTime()));
            pstmt.setInt(4, quiz.getScore_quiz());
            pstmt.setString(5, quiz.getReponse_choisit());
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) quiz.setId_quiz(rs.getInt(1));
            }
        }
    }

    @Override
    public void update(Quiz quiz) throws SQLException {
        String query = "UPDATE quiz SET contenu_id=?, titre_quiz=?, score_quiz=?, reponse_choisit=? WHERE id_quiz=?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, quiz.getContenu_id());
            pstmt.setString(2, quiz.getTitre_quiz());
            pstmt.setInt(3, quiz.getScore_quiz());
            pstmt.setString(4, quiz.getReponse_choisit());
            pstmt.setInt(5, quiz.getId_quiz());
            pstmt.executeUpdate();
        }
    }

    @Override
    public void delete(Quiz quiz) throws SQLException {
        String query = "DELETE FROM quiz WHERE id_quiz=?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, quiz.getId_quiz());
            pstmt.executeUpdate();
        }
    }

    @Override
    public List<Quiz> getAll() throws SQLException {
        List<Quiz> quizzes = new ArrayList<>();
        String query = "SELECT * FROM quiz";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                quizzes.add(new Quiz(
                        rs.getInt("id_quiz"),
                        rs.getInt("contenu_id"),
                        rs.getString("titre_quiz"),
                        rs.getTimestamp("date_quiz"),
                        rs.getInt("score_quiz"),
                        rs.getString("reponse_choisit")
                ));
            }
        }
        return quizzes;
    }
}