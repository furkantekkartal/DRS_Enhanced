package Controller;

import Model.Report;
import Util.DatabaseConnection;
import java.io.File;
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

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import javafx.stage.FileChooser;

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
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/MapView.fxml"));
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

    public void createPDF(Report report) {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.LETTER);
        document.addPage(page);

        try {
            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            // Set starting position
            float yPosition = page.getMediaBox().getHeight() - 50;
            float leading = 14.5f;

            // Title
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 20);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, yPosition);
            contentStream.showText("Disaster Report");
            contentStream.endText();

            yPosition -= leading + 20;

            // Report details
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, yPosition);

            contentStream.showText("Report ID: " + report.getId());
            contentStream.newLineAtOffset(0, -leading);
            contentStream.showText("Disaster Type: " + report.getDisasterType());
            contentStream.newLineAtOffset(0, -leading);
            contentStream.showText("Location: " + report.getLocation());
            contentStream.newLineAtOffset(0, -leading);
            contentStream.showText("Date/Time: " + report.getDateTime());
            contentStream.newLineAtOffset(0, -leading);
            contentStream.showText("Reporter Name: " + report.getReporterName());
            contentStream.newLineAtOffset(0, -leading);
            contentStream.showText("Contact Info: " + report.getContactInfo());
            contentStream.newLineAtOffset(0, -leading);

            // Add disaster-specific details
            addDisasterSpecificDetails(contentStream, report);

            // Close content stream
            contentStream.endText();
            contentStream.close();

            // Save the document to a chosen file
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Report PDF");
            fileChooser.setInitialFileName("Report_" + report.getId() + ".pdf");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            File file = fileChooser.showSaveDialog(null);

            if (file != null) {
                document.save(file);
                showAlert("Success", "PDF saved successfully.");
            }

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Error creating PDF: " + e.getMessage());
        } finally {
            try {
                document.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void addDisasterSpecificDetails(PDPageContentStream contentStream, Report report) throws IOException {
        float leading = 14.5f;
        contentStream.setFont(PDType1Font.HELVETICA, 12);

        switch (report.getDisasterType()) {
            case "Wildfire":
                contentStream.showText("Fire Intensity: " + report.getFireIntensity());
                contentStream.newLineAtOffset(0, -leading);
                contentStream.showText("Affected Area Size: " + report.getAffectedAreaSize());
                contentStream.newLineAtOffset(0, -leading);
                contentStream.showText("Nearby Infrastructure: " + report.getNearbyInfrastructure());
                contentStream.newLineAtOffset(0, -leading);
                break;
            case "Hurricane":
                contentStream.showText("Wind Speed: " + report.getWindSpeed());
                contentStream.newLineAtOffset(0, -leading);
                contentStream.showText("Flood Risk: " + report.getFloodRisk());
                contentStream.newLineAtOffset(0, -leading);
                contentStream.showText("Evacuation Status: " + report.getEvacuationStatus());
                contentStream.newLineAtOffset(0, -leading);
                break;
            case "Earthquake":
                contentStream.showText("Magnitude: " + report.getMagnitude());
                contentStream.newLineAtOffset(0, -leading);
                contentStream.showText("Depth: " + report.getDepth());
                contentStream.newLineAtOffset(0, -leading);
                contentStream.showText("Aftershocks Expected: " + report.getAftershocksExpected());
                contentStream.newLineAtOffset(0, -leading);
                break;
            case "Flood":
                contentStream.showText("Water Level: " + report.getWaterLevel());
                contentStream.newLineAtOffset(0, -leading);
                contentStream.showText("Flood Evacuation Status: " + report.getFloodEvacuationStatus());
                contentStream.newLineAtOffset(0, -leading);
                contentStream.showText("Infrastructure Damage: " + report.getInfrastructureDamage());
                contentStream.newLineAtOffset(0, -leading);
                break;
            case "Landslide":
                contentStream.showText("Slope Stability: " + report.getSlopeStability());
                contentStream.newLineAtOffset(0, -leading);
                contentStream.showText("Blocked Roads: " + report.getBlockedRoads());
                contentStream.newLineAtOffset(0, -leading);
                contentStream.showText("Casualties/Injuries: " + report.getCasualtiesInjuries());
                contentStream.newLineAtOffset(0, -leading);
                break;
            default:
                contentStream.showText("Disaster Description: " + report.getDisasterDescription());
                contentStream.newLineAtOffset(0, -leading);
                contentStream.showText("Estimated Impact: " + report.getEstimatedImpact());
                contentStream.newLineAtOffset(0, -leading);
                break;
        }

        // Add additional report details like response status, priority level, etc.
        contentStream.showText("Response Status: " + report.getResponseStatus());
        contentStream.newLineAtOffset(0, -leading);
        contentStream.showText("Priority Level: " + report.getPriorityLevel());
        contentStream.newLineAtOffset(0, -leading);

        contentStream.showText("Communication Log:");
        contentStream.newLineAtOffset(0, -leading);
        String[] logLines = report.getCommunicationLog().split("\n");
        for (String line : logLines) {
            contentStream.showText(line);
            contentStream.newLineAtOffset(0, -leading);
        }
    }
}
