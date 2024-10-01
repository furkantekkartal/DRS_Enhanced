package Controller;

import Model.Report;
import Util.DatabaseConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.io.IOException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class BaseController {

    private static final String SERVER_HOST = DatabaseConnection.getServerHost();
    private static final int SERVER_PORT = DatabaseConnection.getServerPort();
    private static final int TIMEOUT = DatabaseConnection.getTimeout();

    protected boolean isServerRunning() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT), TIMEOUT);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    protected void showAlert(String title, String message) {
        // Method to display an alert dialog
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION); // Use AlertType.ERROR if needed
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    public void handleViewMap(Report selectedReport) {
        if (selectedReport == null) {
            showAlert("No Selection", "Please select a report first.");
            return;
        }

        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/furkan/coit20258_assignment2/MapView.fxml"));
                Parent root = loader.load();

                C_MapView controller = loader.getController();
                controller.initData(selectedReport);

                Stage stage = new Stage();
                stage.setTitle("Incident Location Map");
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Error", "Error loading map view: " + e.getMessage());
            }
        });
    }
}
