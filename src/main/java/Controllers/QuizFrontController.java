package Controllers;

import Models.Question;
import Models.Quiz;
import Services.QuestionService;
import Services.QuizService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.net.URL;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

public class QuizFrontController implements Initializable {

    @FXML private ComboBox<Quiz> quizComboBox;
    @FXML private ListView<Question> questionOrderList;
    @FXML private Button startQuizButton;
    @FXML private Button moveUpButton;
    @FXML private Button moveDownButton;
    @FXML private VBox quizPlayArea;
    @FXML private Label questionLabel;
    @FXML private RadioButton option1, option2, option3;
    @FXML private Button submitAnswerButton;
    @FXML private Label scoreLabel;

    private QuizService quizService = new QuizService();
    private QuestionService questionService = new QuestionService();
    private ObservableList<Question> questions = FXCollections.observableArrayList();
    private int currentQuestionIndex = 0;
    private int score = 0;
    private Quiz selectedQuiz;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadQuizzes();
        setupQuestionOrderList();
        setupQuizPlayArea();
    }

    private void loadQuizzes() {
        try {
            quizComboBox.setItems(FXCollections.observableArrayList(quizService.getAll()));
            quizComboBox.setOnAction(event -> loadQuestionsForQuiz());
        } catch (SQLException e) {
            showAlert("Erreur", "Échec du chargement des quiz: " + e.getMessage());
        }
    }

    private void setupQuestionOrderList() {
        questionOrderList.setCellFactory(param -> new ListCell<Question>() {
            @Override
            protected void updateItem(Question item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getText_question());
            }
        });

        // Enable reordering by double-click
        questionOrderList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Question selected = questionOrderList.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    int index = questionOrderList.getSelectionModel().getSelectedIndex();
                    if (index > 0) {
                        Collections.swap(questions, index, index - 1);
                        questionOrderList.setItems(questions);
                        questionOrderList.getSelectionModel().select(index - 1);
                    } else if (index < questions.size() - 1) {
                        Collections.swap(questions, index, index + 1);
                        questionOrderList.setItems(questions);
                        questionOrderList.getSelectionModel().select(index + 1);
                    }
                }
            }
        });
    }

    private void setupQuizPlayArea() {
        quizPlayArea.setVisible(false);
        submitAnswerButton.setOnAction(event -> checkAnswer());
        startQuizButton.setOnAction(event -> startQuiz());
        moveUpButton.setOnAction(event -> moveUp());
        moveDownButton.setOnAction(event -> moveDown());
    }

    private void loadQuestionsForQuiz() {
        selectedQuiz = quizComboBox.getSelectionModel().getSelectedItem();
        if (selectedQuiz != null) {
            try {
                questions.setAll(questionService.getAll().stream()
                        .filter(q -> q.getQuiz_id() == selectedQuiz.getId_quiz())
                        .toList());
                questionOrderList.setItems(questions);
                quizPlayArea.setVisible(false);
                startQuizButton.setDisable(questions.isEmpty());
            } catch (SQLException e) {
                showAlert("Erreur", "Échec du chargement des questions: " + e.getMessage());
            }
        }
    }

    private void startQuiz() {
        if (questions.isEmpty()) {
            showAlert("Erreur", "Aucune question disponible pour ce quiz.");
            return;
        }
        currentQuestionIndex = 0;
        score = 0;
        scoreLabel.setText("Score: 0");
        quizPlayArea.setVisible(true);
        questionOrderList.setDisable(true);
        quizComboBox.setDisable(true);
        startQuizButton.setDisable(true);
        moveUpButton.setDisable(true);
        moveDownButton.setDisable(true);
        displayQuestion();
    }

    private void displayQuestion() {
        if (currentQuestionIndex < questions.size()) {
            Question question = questions.get(currentQuestionIndex);
            questionLabel.setText(question.getText_question());
            option1.setText(question.getResponse_prop1());
            option2.setText(question.getResponse_prop2());
            option3.setText(question.getResponse_prop3());
            option1.setSelected(false);
            option2.setSelected(false);
            option3.setSelected(false);
        } else {
            endQuiz();
        }
    }

    private void checkAnswer() {
        Question question = questions.get(currentQuestionIndex);
        String selectedAnswer = null;
        if (option1.isSelected()) selectedAnswer = option1.getText();
        else if (option2.isSelected()) selectedAnswer = option2.getText();
        else if (option3.isSelected()) selectedAnswer = option3.getText();

        if (selectedAnswer != null && selectedAnswer.equals(question.getResponse_correct())) {
            score += 10; // Arbitrary points per correct answer
            scoreLabel.setText("Score: " + score);
        }

        currentQuestionIndex++;
        displayQuestion();
    }

    private void endQuiz() {
        quizPlayArea.setVisible(false);
        questionOrderList.setDisable(false);
        quizComboBox.setDisable(false);
        startQuizButton.setDisable(false);
        moveUpButton.setDisable(false);
        moveDownButton.setDisable(false);
        showAlert("Quiz Terminé", "Votre score final est: " + score);
    }

    @FXML
    private void moveUp() {
        Question selected = questionOrderList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            int index = questionOrderList.getSelectionModel().getSelectedIndex();
            if (index > 0) {
                Collections.swap(questions, index, index - 1);
                questionOrderList.setItems(questions);
                questionOrderList.getSelectionModel().select(index - 1);
            }
        }
    }

    @FXML
    private void moveDown() {
        Question selected = questionOrderList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            int index = questionOrderList.getSelectionModel().getSelectedIndex();
            if (index < questions.size() - 1) {
                Collections.swap(questions, index, index + 1);
                questionOrderList.setItems(questions);
                questionOrderList.getSelectionModel().select(index + 1);
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}