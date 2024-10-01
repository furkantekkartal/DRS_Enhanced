package Controller;

import Model.User;
import Util.DatabaseConnection;
import ENUM.UserRole;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class C_Registration implements Initializable {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField fullNameField;

    @FXML
    private ComboBox<String> roleComboBox;

    @FXML
    private Button registerButton;

    @FXML
    private Button cancelButton;

    private Stage stage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        roleComboBox.getItems().addAll(
            UserRole.Coordinator.toString(),
            UserRole.FireDepartment.toString(),
            UserRole.HealthDepartment.toString(),
            UserRole.LawEnforcement.toString(),
            UserRole.Meteorology.toString(),
            UserRole.Geoscience.toString(),
            UserRole.UtilityCompanies.toString()
        );
    }

    @FXML
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String email = emailField.getText().trim();
        String fullName = fullNameField.getText().trim();
        String role = roleComboBox.getValue();

        if (username.isEmpty() || password.isEmpty() || email.isEmpty() || fullName.isEmpty() || role == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill in all fields.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Error", "Passwords do not match.");
            return;
        }

        if (DatabaseConnection.usernameExists(username)) {
            showAlert(Alert.AlertType.ERROR, "Error", "Username already exists.");
            return;
        }

        if (DatabaseConnection.emailExists(email)) {
            showAlert(Alert.AlertType.ERROR, "Error", "Email already registered.");
            return;
        }

        User newUser = new User(username, password, role, email, fullName);
        boolean success = DatabaseConnection.registerUser(newUser);

        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Registration successful. You can now log in.");
            closeWindow();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Registration failed. Please try again.");
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        if (stage == null) {
            stage = (Stage) registerButton.getScene().getWindow();
        }
        stage.close();
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}