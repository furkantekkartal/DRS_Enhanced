module coit20258assignment3 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.web; 
    requires java.base;
    requires java.sql;
    requires org.apache.pdfbox;
    exports Controller;
    opens Controller to javafx.fxml;

    opens coit20258assignment3 to javafx.fxml;
    exports coit20258assignment3;
    
    opens Model to javafx.base;
}