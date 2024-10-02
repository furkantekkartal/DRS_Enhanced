package coit20258assignment3;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX App This class serves as the main entry point for the JavaFX
 * application. It handles the initialization and launching of the application's
 * GUI.
 *
 * @author 12223508
 */
public class App extends Application {

    private static Scene scene;

    /**
     * The main entry point for the application. This method calls the launch()
     * method to start the JavaFX application.
     *
     * @param args Command line arguments passed to the application
     */
    public static void main(String[] args) {
        launch();
    }

    /**
     * This method is called when the application is started. It sets up the
     * initial scene and displays the primary stage.
     *
     * @param stage The primary stage for this application
     * @throws IOException If there's an error loading the FXML file
     */
    @Override
    public void start(Stage stage) throws IOException {
        // Set up the initial scene with the Login FXML
        scene = new Scene(loadFXML("/FXML/Login"), 400, 750);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Loads an FXML file and returns the root parent node.
     *
     * @param fxml The name of the FXML file to load (without the .fxml
     * extension)
     * @return The root parent node of the loaded FXML file
     * @throws IOException If there's an error loading the FXML file
     */
    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }
}
