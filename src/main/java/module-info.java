module furkan.coit20258_assignment2 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.web; 
    requires java.base;
    requires java.sql;
    requires org.apache.pdfbox;
    exports Controller;
    opens Controller to javafx.fxml;

    opens furkan.coit20258_assignment2 to javafx.fxml;
    exports furkan.coit20258_assignment2;
    
    opens Model to javafx.base;
}