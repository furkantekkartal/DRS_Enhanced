// File: src/main/java/Controller/C_MapView.java

package Controller;

import javafx.fxml.FXML;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
import javafx.stage.Stage;
import Model.Report;

public class C_MapView {

    @FXML
    private WebView mapWebView; // Updated to match fx:id in FXML

    private Report report;

    public void initData(Report report) {
        this.report = report;
        loadMap(); // Updated method name
    }

    private void loadMap() {
        double latitude = report.getLatitude();
        double longitude = report.getLongitude();

        if (latitude == 0 && longitude == 0) {
            String htmlContent = "<html><body><h2>No Location Available</h2></body></html>";
            mapWebView.getEngine().loadContent(htmlContent);
            return;
        }

        // Construct the OpenStreetMap URL
        String mapUrl = "https://www.openstreetmap.org/?mlat=" + latitude + "&mlon=" + longitude
                + "#map=13/" + latitude + "/" + longitude;

        WebEngine webEngine = mapWebView.getEngine();
        webEngine.load(mapUrl);
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) mapWebView.getScene().getWindow();
        stage.close();
    }
}