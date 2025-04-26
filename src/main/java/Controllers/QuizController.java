package Controllers;

import Models.Quiz;
import Models.ContenuMultiMedia;
import Services.QuizService;
import Services.ContenuMultiMediaService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.sql.SQLException;
import java.util.Date;
import java.util.ResourceBundle;

public class QuizController implements Initializable {
    private static final String TITLE_REGEX = "^[a-zA-Z0-9 ]{3,50}$";
    private static final String RESPONSE_REGEX = "^[a-zA-Z0-9 ,.?-]+$";

    @FXML private ListView<Quiz> listView;
    @FXML private TextField txtTitre, txtScore, txtReponse;
    @FXML private ComboBox<ContenuMultiMedia> cbContenu;
    @FXML private Label titreError, scoreError, reponseError, contenuError;
    private QuizService quizService = new QuizService();
    private ContenuMultiMediaService contenuService = new ContenuMultiMediaService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupValidation(); // Ajout de la validation
        setupListView();
        loadData();
        loadContenus();
    }
    // Ajout de la méthode de validation
    private void setupValidation() {
        // Validation du titre
        txtTitre.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches(TITLE_REGEX)) {
                titreError.setText("3 à 50 caractères alphanumériques");
            } else {
                titreError.setText("");
            }
        });

        // Validation du score
        txtScore.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                scoreError.setText("Nombre uniquement");
            } else {
                scoreError.setText("");
            }
        });

        // Validation de la réponse
        txtReponse.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches(RESPONSE_REGEX)) {
                reponseError.setText("Caractères spéciaux non autorisés");
            } else {
                reponseError.setText("");
            }
        });
    }

    private void setupListView() {
        listView.setCellFactory(param -> new ListCell<Quiz>() {
            @Override
            protected void updateItem(Quiz item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getTitre_quiz() + " - Score: " + item.getScore_quiz());
            }
        });

        listView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) populateFields(newVal);
        });
    }

    @FXML
    private void loadData() {
        try {
            listView.getItems().setAll(quizService.getAll());
        } catch (SQLException e) {
            showAlert("Erreur", "Échec du chargement des quiz: " + e.getMessage());
        }
    }
    private void loadContenus() {
        try {
            cbContenu.setItems(FXCollections.observableArrayList(contenuService.getAll()));
        } catch (SQLException e) {
            showAlert("Erreur", e.getMessage());
        }
    }

    private void populateFields(Quiz quiz) {
        txtTitre.setText(quiz.getTitre_quiz());
        txtScore.setText(String.valueOf(quiz.getScore_quiz()));
        txtReponse.setText(quiz.getReponse_choisit());

        cbContenu.getItems().stream()
                .filter(c -> c.getId_contenu() == quiz.getContenu_id())
                .findFirst()
                .ifPresent(c -> cbContenu.getSelectionModel().select(c));
    }

    @FXML
    private void handleAdd() {
        // Vérification avant ajout
        if (txtTitre.getText().isEmpty() ||
                txtScore.getText().isEmpty() ||
                txtReponse.getText().isEmpty() ||
                cbContenu.getValue() == null) {

            showAlert("Erreur", "Tous les champs sont obligatoires");
            return;
        }

        try {
            // Vérification numérique du score
            Integer.parseInt(txtScore.getText());
        } catch (NumberFormatException e) {
            scoreError.setText("Nombre invalide");
            return;
        }

        // ... reste du code existant
        try {
            ContenuMultiMedia contenu = cbContenu.getSelectionModel().getSelectedItem();

            Quiz nouveau = new Quiz(
                    contenu.getId_contenu(),
                    txtTitre.getText(),
                    new Date(),
                    Integer.parseInt(txtScore.getText()),
                    txtReponse.getText()
            );

            quizService.add(nouveau);
            loadData();
            clearFields();
        } catch (Exception e) {
            showAlert("Erreur", e.getMessage());
        }
    }



    @FXML
    private void handleUpdate() {
        Quiz selected = listView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                ContenuMultiMedia contenu = cbContenu.getSelectionModel().getSelectedItem();
                if (contenu == null) {
                    showAlert("Erreur", "Veuillez sélectionner un contenu");
                    return;
                }
                if (txtTitre.getText().isEmpty() ||
                        txtScore.getText().isEmpty() ||
                        txtReponse.getText().isEmpty() ||
                        cbContenu.getValue() == null) {

                    showAlert("Erreur", "Tous les champs sont obligatoires");
                    return;
                }

                selected.setContenu_id(contenu.getId_contenu());
                selected.setTitre_quiz(txtTitre.getText());
                selected.setScore_quiz(Integer.parseInt(txtScore.getText()));
                selected.setReponse_choisit(txtReponse.getText());

                quizService.update(selected);
                loadData();
            } catch (Exception e) {
                showAlert("Erreur", e.getMessage());
            }
        }
    }

    @FXML
    private void handleDelete() {
        Quiz selected = listView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                quizService.delete(selected);
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
        txtTitre.clear();
        txtScore.clear();
        txtReponse.clear();
        cbContenu.getSelectionModel().clearSelection();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}