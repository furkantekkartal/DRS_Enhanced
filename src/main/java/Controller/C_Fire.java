package Controller;

import ENUM.*;
import Model.*;
import java.io.IOException;
import java.sql.SQLException;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Controller class for the Fire Department interface. Handles the interaction
 * between the user interface and the Fire Department model.
 *
 * @author 12223508
 */
public class C_Fire extends BaseController {

    // FXML annotated fields
    @FXML
    private TableView<Report> reportTable;
    @FXML
    private TableColumn<Report, Integer> idColumn;
    @FXML
    private TableColumn<Report, String> typeColumn;
    @FXML
    private TableColumn<Report, String> locationColumn;
    @FXML
    private TableColumn<Report, Double> latitudeColumn;
    @FXML
    private TableColumn<Report, Double> longitudeColumn;
    @FXML
    private TableColumn<Report, String> dateTimeColumn;
    @FXML
    private TableColumn<Report, String> statusColumn;
    @FXML
    private TableColumn<Report, String> priorityColumn;
    @FXML
    private TableColumn<Report, String> fireDeptStatusColumn;
    @FXML
    private ComboBox<ResponseStatus> statusComboBox;
    @FXML
    private TextArea reportDetailsArea;
    @FXML
    private TextArea communicationLogArea;
    @FXML
    private TextArea resourcesNeededArea;
    @FXML
    private TextField newLogEntryField;
    @FXML
    private Label userLabel;
    @FXML
    private Button createPdfButton;

    // Class fields
    private String currentUser;
    private ObservableList<Report> activeReports = FXCollections.observableArrayList();
    private Report selectedReport;
    private Fire fireDepartment;

    /**
     * Initializes the controller. This method is automatically called after the
     * FXML file has been loaded.
     */
    @FXML
    public void initialize() throws SQLException {
        fireDepartment = new Fire();

        setupTableColumns();
        setupTableData();
        setupStatusComboBox();
        loadActiveReports();
        setupTableSelectionListener();
    }

    /**
     * Sets up the table columns and their respective cell value factories.
     */
    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("disasterType"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        latitudeColumn.setCellValueFactory(new PropertyValueFactory<>("latitude"));
        longitudeColumn.setCellValueFactory(new PropertyValueFactory<>("longitude"));
        dateTimeColumn.setCellValueFactory(new PropertyValueFactory<>("dateTime"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("responseStatus"));
        priorityColumn.setCellValueFactory(new PropertyValueFactory<>("priorityLevel"));
        fireDeptStatusColumn.setCellValueFactory(cellData
                -> new SimpleStringProperty(cellData.getValue().getDepartmentStatus(Department.FIRE_DEPARTMENT).getDisplayName())
        );

        // Set column widths
        idColumn.prefWidthProperty().bind(reportTable.widthProperty().multiply(0.05));
        typeColumn.prefWidthProperty().bind(reportTable.widthProperty().multiply(0.1));
        latitudeColumn.prefWidthProperty().bind(reportTable.widthProperty().multiply(0.1));
        longitudeColumn.prefWidthProperty().bind(reportTable.widthProperty().multiply(0.1));
        locationColumn.prefWidthProperty().bind(reportTable.widthProperty().multiply(0.15));
        dateTimeColumn.prefWidthProperty().bind(reportTable.widthProperty().multiply(0.15));
        statusColumn.prefWidthProperty().bind(reportTable.widthProperty().multiply(0.1));
        priorityColumn.prefWidthProperty().bind(reportTable.widthProperty().multiply(0.1));
        fireDeptStatusColumn.prefWidthProperty().bind(reportTable.widthProperty().multiply(0.15));
    }

    /**
     * Sets up the table data and binds it to the observable list of active
     * reports.
     */
    private void setupTableData() {
        reportTable.setItems(activeReports);
    }

    /**
     * Sets up the status combo box with all possible response statuses.
     */
    private void setupStatusComboBox() {
        statusComboBox.getItems().addAll(ResponseStatus.values());
    }

    /**
     * Sets up the listener for table row selection.
     */
    private void setupTableSelectionListener() {
        reportTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedReport = newSelection;
                try {
                    updateReportDetails(newSelection);
                } catch (SQLException ex) {
                    Logger.getLogger(C_Fire.class.getName()).log(Level.SEVERE, null, ex);
                }
                updateCommunicationLog(newSelection);
                updateResourcesNeeded(newSelection);
            }
        });
    }

    /**
     * Loads active reports from the database and populates the table.
     */
    private void loadActiveReports() {
        try {
            activeReports.clear();
            activeReports.addAll(fireDepartment.getActiveReports());
        } catch (SQLException e) {
            activeReports.clear();
            reportTable.setItems(FXCollections.observableArrayList());
            System.err.println("Cannot load data: " + e.getMessage());
        }
    }

    /**
     * Updates the report details area with information from the selected
     * report.
     *
     * @param report The selected report
     */
    private void updateReportDetails(Report report) throws SQLException {
        StringBuilder details = new StringBuilder();
        details.append("Disaster Type: ").append(report.getDisasterType()).append("\n");
        details.append("Location: ").append(report.getLocation()).append("\n");
        details.append("Date/Time: ").append(report.getDateTime()).append("\n");
        details.append("Reporter: ").append(report.getReporterName()).append("\n");
        details.append("Contact: ").append(report.getContactInfo()).append("\n\n");

        appendDisasterSpecificDetails(details, report);

        details.append("\n");
        details.append("Latitude: ").append(report.getLatitude()).append("\n");
        details.append("Longitude: ").append(report.getLongitude()).append("\n");

        reportDetailsArea.setText(details.toString());
    }

    /**
     * Appends disaster-specific details to the StringBuilder.
     *
     * @param details The StringBuilder to append to
     * @param report The report containing the disaster information
     */
    private void appendDisasterSpecificDetails(StringBuilder details, Report report) throws SQLException {
        fireDepartment.appendDisasterSpecificDetails(details, report);
    }

    /**
     * Handles the submission of a new status for the selected report.
     */
    @FXML
    private void handleSubmitStatus() throws SQLException {
        if (!isServerRunning()) {
            showAlert("Server Connection Error", "Server connection is not established.");
            System.out.println("Cannot submit status: Server is not running.");
            return;
        }
        if (selectedReport == null) {
            showAlert("Error", "Please select a report first.");
            return;
        }

        ResponseStatus selectedStatus = statusComboBox.getValue();
        if (selectedStatus == null) {
            showAlert("Error", "Please select a status.");
            return;
        }

        fireDepartment.updateStatus(selectedReport, selectedStatus);
        showAlert("Success", "Status updated successfully.");
        loadActiveReports(); // Refresh the table
        System.out.println("Status submitted successfully.");
    }

    /**
     * Handles the addition of a new log entry to the communication log.
     */
    @FXML
    private void handleAddLogEntry() throws SQLException {
        if (!isServerRunning()) {
            showAlert("Server Connection Error", "Server connection is not established.");
            System.out.println("Cannot submit status: Server is not running.");
            return;
        }
        if (selectedReport == null) {
            showAlert("Error", "Please select a report first.");
            return;
        }

        String newEntry = newLogEntryField.getText().trim();
        if (newEntry.isEmpty()) {
            showAlert("Error", "Please enter a log entry.");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String timestamp = now.format(formatter);

        String logEntry = String.format("[%s] Fire Department: %s", timestamp, newEntry);

        fireDepartment.addCommunicationLogEntry(selectedReport, logEntry);
        updateCommunicationLog(selectedReport);
        System.out.println("Log added successfully.");
        newLogEntryField.clear();
    }

    /**
     * Updates the communication log area with the latest log entries.
     *
     * @param report The report whose communication log should be displayed
     */
    private void updateCommunicationLog(Report report) {
        communicationLogArea.setText(report.getCommunicationLog());
    }

    /**
     * Updates the resources needed area with the latest information.
     *
     * @param report The report whose resources needed should be displayed
     */
    private void updateResourcesNeeded(Report report) {
        resourcesNeededArea.setText(report.getResourcesNeeded());
    }

    /**
     * Handles the refresh of the reports table.
     */
    @FXML
    private void handleRefreshReports() {
        if (!isServerRunning()) {
            showAlert("Server Connection Error", "Server connection is not established.");
            // Clear the table to show an empty view
            activeReports.clear();
            reportTable.setItems(FXCollections.observableArrayList());
            System.out.println("Cannot refresh reports: Server is not running.");
            clearAllFields();
            return;
        }
        loadActiveReports();
        reportTable.refresh();
        System.out.println("Reports refreshed successfully.");
    }

    /**
     * Sets the current user and updates the user label.
     *
     * @param username The username of the current user
     */
    public void setCurrentUser(String username) {
        this.currentUser = username;
        userLabel.setText("Logged in as: " + username);
    }

    /**
     * Clears all input fields and resets checkboxes.
     */
    private void clearAllFields() {
        // Clear text areas
        reportDetailsArea.clear();
        communicationLogArea.clear();
        resourcesNeededArea.clear();

        // Clear input fields
        newLogEntryField.clear();

        // Reset combo boxes
        statusComboBox.getSelectionModel().clearSelection();

        // Clear table selection
        reportTable.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleViewMap() {
        Report selectedReport = reportTable.getSelectionModel().getSelectedItem();
        handleViewMap(selectedReport); // Calls the method in BaseController
    }

    @FXML
    private void handleCreatePdf(ActionEvent event) {
        Report selectedReport = reportTable.getSelectionModel().getSelectedItem();
        if (selectedReport != null) {
            createPDF(selectedReport);
        } else {
            showAlert("No Selection", "Please select a report to create PDF.");
        }
    }
}
