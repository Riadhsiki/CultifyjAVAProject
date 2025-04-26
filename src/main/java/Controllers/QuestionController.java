package Controllers;

import Models.Question;
import Models.Quiz;
import Services.QuestionService;
import Services.QuizService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class QuestionController implements Initializable {
    private static final String TEXT_REGEX = "^[a-zA-Z0-9 ,.?-]+$";
    private static final String RESPONSE_REGEX = "^[a-zA-Z0-9 ,.?-]+$";

    @FXML private ListView<Question> listView;
    @FXML private TextField  txtReponse1, txtReponse2, txtReponse3, txtReponseCorrecte;
    @FXML private TextArea txtQuestion;
    @FXML private ComboBox<Quiz> cbQuiz;
    @FXML private Label questionError, reponse1Error, reponse2Error, reponse3Error, correctError, quizError;

    private QuestionService questionService = new QuestionService();
    private QuizService quizService = new QuizService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupListView();
        loadData();
        loadQuizzes();
        setupValidation();
    }
    private void setupValidation() {
        addValidation(txtQuestion, questionError, TEXT_REGEX, "Caractères autorisés : lettres, chiffres, ponctuation");
        addValidation(txtReponse1, reponse1Error, RESPONSE_REGEX, "Réponse invalide");
        addValidation(txtReponse2, reponse2Error, RESPONSE_REGEX, "Réponse invalide");
        addValidation(txtReponse3, reponse3Error, RESPONSE_REGEX, "Réponse invalide");
        addValidation(txtReponseCorrecte, correctError, RESPONSE_REGEX, "Réponse invalide");
    }

    private boolean validateForm() {
        boolean isValid = true;

        if (txtQuestion.getText().trim().isEmpty()) {
            questionError.setText("Champ obligatoire");
            isValid = false;
        }

        if (cbQuiz.getSelectionModel().isEmpty()) {
            quizError.setText("Sélectionnez un quiz");
            isValid = false;
        }

        String correct = txtReponseCorrecte.getText();
        if (!correct.equals(txtReponse1.getText()) &&
                !correct.equals(txtReponse2.getText()) &&
                !correct.equals(txtReponse3.getText())) {
            correctError.setText("La réponse doit correspondre à une proposition");
            isValid = false;
        }

        return isValid;
    }

    private void addValidation(TextInputControl field, Label errorLabel, String regex, String message) {
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches(regex)) {
                errorLabel.setText(message);
                field.getStyleClass().add("error");
            } else {
                errorLabel.setText("");
                field.getStyleClass().remove("error");
            }
        });
    }    private void setupListView() {
        listView.setCellFactory(param -> new ListCell<Question>() {
            @Override
            protected void updateItem(Question item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getText_question());
            }
        });

        listView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) populateFields(newVal);
        });
    }

    @FXML
    private void loadData() {
        try {
            listView.getItems().setAll(questionService.getAll());
        } catch (SQLException e) {
            showAlert("Erreur", "Échec du chargement des questions: " + e.getMessage());
        }
    }

    private void loadQuizzes() {
        try {
            cbQuiz.setItems(FXCollections.observableArrayList(quizService.getAll()));
        } catch (SQLException e) {
            showAlert("Erreur", e.getMessage());
        }
    }

    private void populateFields(Question question) {
        txtQuestion.setText(question.getText_question());
        txtReponse1.setText(question.getResponse_prop1());
        txtReponse2.setText(question.getResponse_prop2());
        txtReponse3.setText(question.getResponse_prop3());
        txtReponseCorrecte.setText(question.getResponse_correct());

        cbQuiz.getItems().stream()
                .filter(q -> q.getId_quiz() == question.getQuiz_id())
                .findFirst()
                .ifPresent(q -> cbQuiz.getSelectionModel().select(q));
    }

    @FXML
    private void handleAdd() {
        if (!validateForm()) {
            showAlert("Erreur", "Veuillez corriger les champs invalides");
            return;
        }
        try {
            Quiz quiz = cbQuiz.getSelectionModel().getSelectedItem();
            if (quiz == null) {
                showAlert("Erreur", "Veuillez sélectionner un quiz");
                return;
            }

            Question nouvelle = new Question(
                    quiz.getId_quiz(),
                    txtQuestion.getText(),
                    txtReponse1.getText(),
                    txtReponse2.getText(),
                    txtReponse3.getText(),
                    txtReponseCorrecte.getText()
            );
            questionService.add(nouvelle);
            loadData();
            clearFields();
        } catch (Exception e) {
            showAlert("Erreur", e.getMessage());
        }
    }

    @FXML
    private void handleUpdate() {
        if (!validateForm()) {
            showAlert("Erreur", "Veuillez corriger les champs invalides");
            return;
        }
        Question selected = listView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                Quiz quiz = cbQuiz.getSelectionModel().getSelectedItem();
                if (quiz == null) {
                    showAlert("Erreur", "Veuillez sélectionner un quiz");
                    return;
                }

                selected.setQuiz_id(quiz.getId_quiz());
                selected.setText_question(txtQuestion.getText());
                selected.setResponse_prop1(txtReponse1.getText());
                selected.setResponse_prop2(txtReponse2.getText());
                selected.setResponse_prop3(txtReponse3.getText());
                selected.setResponse_correct(txtReponseCorrecte.getText());

                questionService.update(selected);
                loadData();
            } catch (Exception e) {
                showAlert("Erreur", e.getMessage());
            }
        }
    }


    @FXML
    private void handleDelete() {
        Question selected = listView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                questionService.delete(selected);
                loadData();
                clearFields();
            } catch (SQLException e) {
                showAlert("Erreur", e.getMessage());
            }
        }
    }

    @FXML
    private void handleClear() {
        clearFields();
    }

    private void clearFields() {
        txtQuestion.clear();
        txtReponse1.clear();
        txtReponse2.clear();
        txtReponse3.clear();
        txtReponseCorrecte.clear();
        cbQuiz.getSelectionModel().clearSelection();
    }

    private void showAlert(String title, String message) {
        new Alert(Alert.AlertType.ERROR, message).showAndWait();
    }
}

