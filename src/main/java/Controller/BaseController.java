package Controller;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.io.IOException;
import javafx.application.Platform;
import javafx.scene.control.Alert;

public class BaseController {

    private static final String SERVER_HOST = "localhost"; // Replace with your server's host if different
    private static final int SERVER_PORT = 5000;           // Replace with your server's port
    private static final int TIMEOUT = 2000;               // Timeout in milliseconds

    protected boolean isServerRunning() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT), TIMEOUT);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    protected void showAlert(String title, String content) {
        // Ensure alerts are displayed on the JavaFX Application Thread
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
}