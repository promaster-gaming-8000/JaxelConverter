module VoxconverterJava {
    requires javafx.controls;
    requires javafx.web;
	requires javafx.graphics;
    
    opens application to javafx.graphics, javafx.fxml, javafx.web;
}