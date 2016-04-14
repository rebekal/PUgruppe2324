package software;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainRunner extends Application {
	@Override
	public void start(Stage primaryStage) throws IOException {
		Parent root = FXMLLoader.load(Main.class.getResource("GUI3.fxml"));
		primaryStage.setScene(new Scene(root,1250,700));
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
