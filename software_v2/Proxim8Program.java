package software;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Proxim8Program extends Application {
	
	@Override
	public void start(Stage primaryStage) throws IOException {
		Parent root = FXMLLoader.load(MainController.class.getResource("GUI_carData.fxml"));
		primaryStage.setScene(new Scene(root,1280,768));
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
