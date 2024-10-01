package Controller;

import Server.DRSServer;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.application.Platform;

public class C_DRSServer {

    @FXML
    private Button startButton;

    @FXML
    private Button stopButton;

    @FXML
    private Label statusLabel;

    private DRSServer server;
    private Thread serverThread; // Declared serverThread
    private String currentUser;

    @FXML
    private void initialize() {
        stopButton.setDisable(true);
        updateStatus("Server is stopped");
    }

    @FXML
    private void handleStartServer() {
        server = new DRSServer();
        serverThread = new Thread(() -> {
            server.start();
            Platform.runLater(() -> {
                // Update the UI when the server stops
                startButton.setDisable(false);
                stopButton.setDisable(true);
                statusLabel.setText("Server Status: Stopped");
            });
        });
        serverThread.setDaemon(true);
        serverThread.start();
        
        // Immediately update the UI after starting the server
        startButton.setDisable(true);
        stopButton.setDisable(false);
        statusLabel.setText("Server Status: Running on port " + DRSServer.getPORT()); // Use getter
    }

    @FXML
    private void handleStopServer() {
        if (server != null && server.isRunning()) {
            new Thread(() -> {
                server.stop();
                Platform.runLater(() -> {
                    updateStatus("Server is stopped");
                    startButton.setDisable(false);
                    stopButton.setDisable(true);
                });
            }).start();
        }
    }

    private void updateStatus(String status) {
        statusLabel.setText(status);
    }

    public void setCurrentUser(String username) {
        this.currentUser = username;
    }
}