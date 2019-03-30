package application;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import controllers.MainController;
import controllers.FileManagerCntrl;
import controllers.MainController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.Parent;
import javafx.scene.Scene;


public class Main extends Application { 
	
	private static Stage appStage = null;
	
    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../fxmls/main.fxml"));
           
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("../styles/application.css").toExternalForm());
            
            primaryStage.setScene(scene);
            primaryStage.setTitle( "Super Transfer" );
            primaryStage.setResizable(true);
            
            appStage = primaryStage;
            
            MainController controller = (MainController)fxmlLoader.getController();
            MainController.setStage(primaryStage);

            appStage.show();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void changeScene(Parent root) {
    	if (appStage != null) {
	    	appStage.getScene().setRoot(root);
	    	appStage.show();
    	}
    	
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
