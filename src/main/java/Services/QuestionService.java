package Services;

import Models.Question;
import Utils.MyConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuestionService implements IService<Question> {
    private Connection conn = MyConnection.getMyConnection().getConnection();

    // Méthode helper pour paramétrer les champs communs
    private PreparedStatement setCommonParameters(PreparedStatement pstmt, Question question) throws SQLException {
        pstmt.setInt(1, question.getQuiz_id());
        pstmt.setString(2, question.getText_question());
        pstmt.setString(3, question.getResponse_prop1());
        pstmt.setString(4, question.getResponse_prop2());
        pstmt.setString(5, question.getResponse_prop3());
        pstmt.setString(6, question.getResponse_correct());
        return pstmt;
    }

    @Override
    public void add(Question question) throws SQLException {
        String query = "INSERT INTO question (quiz_id, text_question, response_prop1, response_prop2, response_prop3, response_correct) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            setCommonParameters(pstmt, question);
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) question.setId_question(rs.getInt(1));
            }
        }
    }

    @Override
    public void update(Question question) throws SQLException {
        String query = "UPDATE question SET quiz_id=?, text_question=?, response_prop1=?, response_prop2=?, response_prop3=?, response_correct=? WHERE id_question=?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            setCommonParameters(pstmt, question);
            pstmt.setInt(7, question.getId_question());
            pstmt.executeUpdate();
        }
    }

    @Override
    public void delete(Question question) throws SQLException {
        String query = "DELETE FROM question WHERE id_question=?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, question.getId_question());
            pstmt.executeUpdate();
        }
    }

    @Override
    public List<Question> getAll() throws SQLException {
        List<Question> questions = new ArrayList<>();
        String query = "SELECT * FROM question";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                questions.add(new Question(
                        rs.getInt("id_question"),
                        rs.getInt("quiz_id"),
                        rs.getString("text_question"),
                        rs.getString("response_prop1"),
                        rs.getString("response_prop2"),
                        rs.getString("response_prop3"),
                        rs.getString("response_correct")
                ));
            }
        }
        return questions;
    }
}