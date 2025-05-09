package controllers.reservation;

import models.Reservation;
import services.eventreservation.ReservationService;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

public class DetailReservation {

    @FXML private TableView<Reservation> tableView;
    @FXML private TableColumn<Reservation, String> etatCol;
    @FXML private TableColumn<Reservation, String> dateCol;
    @FXML private TableColumn<Reservation, String> themeCol;
    @FXML private TableColumn<Reservation, String> urlCol;
    @FXML private TableColumn<Reservation, Integer> nbTicketsCol;
    @FXML private TableColumn<Reservation, String> eventCol;
    @FXML private TableColumn<Reservation, Void> actionCol;
    @FXML private Button btnRetour;

    private ReservationService reservationService = new ReservationService();

    @FXML
    public void initialize() {
        configureTable();
        configureSorting();
        loadData();
    }

    private void configureTable() {
        etatCol.setCellValueFactory(new PropertyValueFactory<>("etat"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("dateR"));
        themeCol.setCellValueFactory(new PropertyValueFactory<>("theme"));
        urlCol.setCellValueFactory(new PropertyValueFactory<>("url"));
        nbTicketsCol.setCellValueFactory(new PropertyValueFactory<>("nbTickets"));
        eventCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getEvent() != null ?
                                cellData.getValue().getEvent().getTitre() : ""));

        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Modifier");
            private final Button deleteBtn = new Button("Supprimer");
            private final Button pdfBtn = new Button("PDF");

            {
                editBtn.setOnAction(event -> {
                    Reservation res = getTableView().getItems().get(getIndex());
                    handleEdit(res);
                });

                deleteBtn.setOnAction(event -> {
                    Reservation res = getTableView().getItems().get(getIndex());
                    handleDelete(res);
                });

                pdfBtn.setOnAction(event -> {
                    Reservation res = getTableView().getItems().get(getIndex());
                    exportSingleToPDF(res);
                });

                editBtn.getStyleClass().add("btn-primary");
                deleteBtn.getStyleClass().add("btn-primary");
                pdfBtn.getStyleClass().add("btn-primary");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(new HBox(5, editBtn, deleteBtn, pdfBtn));
                }
            }
        });
    }

    private void configureSorting() {
        dateCol.setSortable(true);
        dateCol.setSortType(TableColumn.SortType.DESCENDING);
        tableView.getSortOrder().add(dateCol);

        dateCol.setComparator((date1, date2) -> {
            if (date1 == null && date2 == null) return 0;
            if (date1 == null) return -1;
            if (date2 == null) return 1;
            return date1.compareTo(date2);
        });
    }

    private void loadData() {
        try {
            TableColumn.SortType sortType = dateCol.getSortType();
            ObservableList<TableColumn<Reservation, ?>> sortOrder = tableView.getSortOrder();

            tableView.getItems().setAll(reservationService.getAll());

            tableView.getSortOrder().setAll(sortOrder);
            dateCol.setSortType(sortType);
            tableView.sort();
        } catch (SQLException e) {
            showAlert("Erreur", e.getMessage());
        }
    }

    @FXML
    private void sortByDate(ActionEvent event) {
        if (dateCol.getSortType() == TableColumn.SortType.ASCENDING) {
            dateCol.setSortType(TableColumn.SortType.DESCENDING);
        } else {
            dateCol.setSortType(TableColumn.SortType.ASCENDING);
        }

        tableView.getSortOrder().setAll(dateCol);
        tableView.sort();
    }

    @FXML
    private void exportToPDF() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le PDF");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
        fileChooser.setInitialFileName("reservations.pdf");

        File file = fileChooser.showSaveDialog(tableView.getScene().getWindow());
        if (file != null) {
            try {
                createPDF(file.getAbsolutePath(), tableView.getItems());
                showAlert("Succès", "Export PDF réussi !");
            } catch (Exception e) {
                showAlert("Erreur", "Erreur lors de l'export PDF: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void exportSingleToPDF(Reservation reservation) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le PDF");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
        fileChooser.setInitialFileName("reservation_" + reservation.getIdR() + ".pdf");

        File file = fileChooser.showSaveDialog(tableView.getScene().getWindow());
        if (file != null) {
            try {
                createSingleReservationPDF(file.getAbsolutePath(), reservation);
                showAlert("Succès", "Export PDF réussi !");
            } catch (Exception e) {
                showAlert("Erreur", "Erreur lors de l'export PDF: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void createPDF(String filePath, ObservableList<Reservation> reservations) throws Exception {
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(filePath));
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph title = new Paragraph("Liste des Réservations", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20f);
        document.add(title);

        Font dateFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
        Paragraph date = new Paragraph("Généré le: " + java.time.LocalDate.now(), dateFont);
        date.setAlignment(Element.ALIGN_RIGHT);
        date.setSpacingAfter(20f);
        document.add(date);

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);

        String[] headers = {"État", "Date", "Thème", "URL", "Nb Tickets", "Événement"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header));
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (Reservation res : reservations) {
            table.addCell(res.getEtat());
            table.addCell(res.getDateR() != null ? res.getDateR().format(dateFormatter) : "");
            table.addCell(res.getTheme());
            table.addCell(res.getUrl());
            table.addCell(String.valueOf(res.getNbTickets()));
            table.addCell(res.getEvent() != null ? res.getEvent().getTitre() : "");
        }

        document.add(table);
        document.close();
    }

    private void createSingleReservationPDF(String filePath, Reservation reservation) throws Exception {
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(filePath));
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph title = new Paragraph("Détails de la Réservation", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20f);
        document.add(title);

        Font dateFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
        Paragraph date = new Paragraph("Généré le: " + java.time.LocalDate.now(), dateFont);
        date.setAlignment(Element.ALIGN_RIGHT);
        date.setSpacingAfter(20f);
        document.add(date);

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);

        addTableRow(table, "ID", String.valueOf(reservation.getIdR()));
        addTableRow(table, "État", reservation.getEtat());
        addTableRow(table, "Date", reservation.getDateR() != null ? reservation.getDateR().format(dateFormatter) : "");
        addTableRow(table, "Thème", reservation.getTheme());
        addTableRow(table, "URL", reservation.getUrl());
        addTableRow(table, "Nombre de Tickets", String.valueOf(reservation.getNbTickets()));
        addTableRow(table, "Événement", reservation.getEvent() != null ? reservation.getEvent().getTitre() : "");

        document.add(table);
        document.close();
    }

    private void addTableRow(PdfPTable table, String label, String value) {
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);

        PdfPCell labelCell = new PdfPCell(new Phrase(label, boldFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value));
        valueCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(valueCell);
    }

    private void handleEdit(Reservation reservation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterReservation.fxml"));
            Parent root = loader.load();
            AjouterReservation controller = loader.getController();
            controller.initEditData(reservation);
            navigateTo("/AjouterReservation.fxml", "Modifier Réservation");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir l'éditeur de réservation");
        }
    }

    private void handleDelete(Reservation reservation) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText("Supprimer la réservation");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer cette réservation ?");

        if (confirmAlert.showAndWait().get() == ButtonType.OK) {
            try {
                reservationService.delete(reservation);
                loadData();
                showAlert("Succès", "Réservation supprimée !");
            } catch (SQLException e) {
                showAlert("Erreur", e.getMessage());
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

    @FXML
    public void redirectToAddForm(ActionEvent actionEvent) {
        navigateTo("/AjouterReservation.fxml", "Ajouter Réservation");
    }

    @FXML
    public void redirectToEventList(ActionEvent actionEvent) {
        navigateTo("/DetailEvent.fxml", "Détails de l'Événement");
    }

    @FXML
    public void handleRetour(ActionEvent actionEvent) {
        navigateTo("/DetailEvent.fxml", "Détails de l'Événement");
    }

    private void navigateTo(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            if (loader.getLocation() == null) {
                showAlert("Navigation Error", "Failed to load view: " + fxmlPath);
                return;
            }
            Parent root = loader.load();
            Stage stage = (Stage) tableView.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.centerOnScreen();
            stage.show();
            System.out.println("Navigated to " + title);
        } catch (IOException e) {
            showAlert("Navigation Error", "Failed to load view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void redirectToReservation(ActionEvent actionEvent) {
    }
}