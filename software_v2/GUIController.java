package software;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;


import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class GUIController implements BaseInterface, Initializable {

	private MainController mainController;
	
	@FXML
	private Button drivingButton, blindZoneButton, parkingButton;
	@FXML
	private Button startButton, stopButton, settingsButton;
	
	@FXML
	private Label frontDistanceLabel, leftDistanceLabel, rightDistanceLabel;
	@FXML
	private Label carSpeedLabel, speedLimitLabel, brakeDistanceLabel, simulateModeLabel;
	@FXML
	private Label redTrafficLightCountLabel;

	@FXML
	private ImageView myCar, leftCar, rightCar, redTrafficLight, leftTurnLight, rightTurnLight;
	
	@FXML
	private Rectangle screenBackground, menuBackground, driveEnabled, blindZoneEnabled, parkingEnabled, startEnabled, stopEnabled;
	
	public Label getFrontDistanceLabel() {
		return frontDistanceLabel;
	}

	public Label getLeftDistanceLabel() {
		return leftDistanceLabel;
	}

	public Label getRightDistanceLabel() {
		return rightDistanceLabel;
	}

	public Label getCarSpeedLabel() {
		return carSpeedLabel;
	}

	public Label getSpeedLimitLabel() {
		return speedLimitLabel;
	}

	public Label getBrakeDistanceLabel() {
		return brakeDistanceLabel;
	}

	public Label getSimulateModeLabel() {
		return simulateModeLabel;
	}

	public ImageView getLeftCar() {
		return leftCar;
	}
	
	public ImageView getRightCar() {
		return rightCar;
	}
	
	public ImageView getRedTrafficLight() {
		return redTrafficLight;
	}
	
	public Label getRedTrafficLightCountLabel() {
		return redTrafficLightCountLabel;
	}
	
	public ImageView getLeftTurnLight() {
		return leftTurnLight;
	}
	
	public ImageView getRightTurnLight() {
		return rightTurnLight;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		mainController = new MainController(this);
//		Screen
		leftCar.setVisible(false);
		rightCar.setVisible(false);
		redTrafficLight.setVisible(false);;
		redTrafficLightCountLabel.setVisible(false);
		leftTurnLight.setVisible(false);
		rightTurnLight.setVisible(false);

		frontDistanceLabel.setVisible(false);
		leftDistanceLabel.setVisible(false);
		rightDistanceLabel.setVisible(false);
		emptyDistanceLabels();
		// TODO
	}
	
	private boolean activation(String mode) {
		return ! mainController.getCurrentModeStatus(mode);
	}

	@FXML
	public void drivingButtonClicked(ActionEvent event) {
		boolean activate = activation(DRIVING_MODE);
		frontDistanceLabel.setVisible(activate);
		
		mainController.setMode(DRIVING_MODE, activate);
		
		boolean blindZoneActive = mainController.getCurrentModeStatus(BLIND_ZONE_MODE);
		if (activate) {
			driveEnabled.setFill(Color.GREEN);
			parkingButton.setDisable(activate);
		}
		else {
			driveEnabled.setFill(Color.BLACK);
		}
		if (!activate && !blindZoneActive) {
			parkingButton.setDisable(activate);
		}
	}
	
	@FXML
	public void blindZoneButtonClicked(ActionEvent event) {
		boolean activate = activation(BLIND_ZONE_MODE);
		mainController.setMode(BLIND_ZONE_MODE, activate);
		
		boolean drivingActive = mainController.getCurrentModeStatus(DRIVING_MODE);
		if (activate) {
			blindZoneEnabled.setFill(Color.GREEN);
			parkingButton.setDisable(activate);
		}
		else {
			blindZoneEnabled.setFill(Color.BLACK);
			leftCar.setVisible(activate);
			rightCar.setVisible(activate);
		}
		if (!activate && !drivingActive) {
			parkingButton.setDisable(activate);
		}
	}
	
	@FXML
	public void parkingButtonClicked(ActionEvent event) {
		boolean activate = activation(PARKING_MODE);
		leftDistanceLabel.setVisible(activate);
		frontDistanceLabel.setVisible(activate);
		rightDistanceLabel.setVisible(activate);
		
		mainController.setMode(PARKING_MODE, activate);
		drivingButton.setDisable(activate);
		blindZoneButton.setDisable(activate);
		
		if (activate) {
			parkingEnabled.setFill(Color.GREEN);
			mainController.prepareParkingMode();
		}
		else {
			parkingEnabled.setFill(Color.BLACK);
		}
	}
	
	@FXML
	public void startButtonClicked(ActionEvent event) {
		mainController.setMode(ENGINE_MODE, true);
		setLabelText(simulateModeLabel, String.valueOf(true));
		
		startEnabled.setFill(Color.GREEN);
		stopEnabled.setFill(Color.BLACK);
//		run();
	}
	
	@FXML
	public void stopButtonClicked(ActionEvent event) {
		mainController.setMode(ENGINE_MODE, false);
		setLabelText(simulateModeLabel, String.valueOf(false));
		
		startEnabled.setFill(Color.BLACK);
		stopEnabled.setFill(Color.RED);
		
		emptyDistanceLabels();
		mainController.resetCarSimulation();
	}

	private void emptyDistanceLabels() {
		frontDistanceLabel.setText("");
		leftDistanceLabel.setText("");
		rightDistanceLabel.setText("");
	}
	
	@FXML
	public void settingsButtonClicked(ActionEvent event) {
		Stage settingsWindow = new Stage();
		settingsWindow.initModality(Modality.APPLICATION_MODAL);
		settingsWindow.setTitle("Settings");
		
		Label infoSettingsLabel = new Label("...");
		
		
		VBox layout = new VBox(infoSettingsLabel);
		Scene scene = new Scene(layout);
		settingsWindow.setScene(scene);
		settingsWindow.showAndWait();
	}
	
	public void setImageVisible(ImageView image, boolean visible) {
		image.setVisible(visible);
	}
	
	public void setLabelVisible(Label label, boolean visible) {
		label.setVisible(visible);
	}
	
	public void updateDistance(Label label, Double currentDistance, double validDistance) {
		label.setText(String.valueOf(currentDistance) + "cm");
		updateDistanceLabelColor(label, currentDistance, validDistance);
	}
	
	private void updateDistanceLabelColor(Label label, double currentDistance, double validDistance) {
		if (currentDistance < validDistance) {
			label.setTextFill(Color.RED);
		}
		else if (currentDistance <= (validDistance * 1.05)) {
			label.setTextFill(Color.YELLOW);
		}
		else {
			label.setTextFill(Color.GREEN);
		}
	}
	
	public void setLabelText(Label label, String text) {
		if (label.equals(carSpeedLabel) || label.equals(speedLimitLabel)) {
			text = "Car speed: " + text + "km/h";
		}
		else if (label.equals(brakeDistanceLabel)) {
			text = "Brake distance: " + text + "meter(s)";
		}
		else if (label.equals(simulateModeLabel)) {
			text = "Simulate mode: " + convertBoolean(Boolean.valueOf(text));
		}
		label.setText(text);
	}
	
	private String convertBoolean(boolean mode) {
		return mode ? "ON" : "OFF";
	}
	
	
//	*** init car window ****
	
	private Label infoLabel = new Label("Insert values for your car:");
	private Label doorLenghtLabel = new Label("Door length:");
	private Label blindZoneValueLabel = new Label("Blind zone value:");
	private Label topSpeedLabel = new Label("Top speed:");
	
	private TextField doorLengthField = new TextField();
	private TextField blindZoneValueField = new TextField();
	private TextField topSpeedField = new TextField();

	private void editInitCarDataLabels(String font, int fontSize) {
		infoLabel.setFont(new Font(font, fontSize + 4));
		doorLenghtLabel.setFont(new Font(font, fontSize));
		blindZoneValueLabel.setFont(new Font(font, fontSize));
		topSpeedLabel.setFont(new Font(font, fontSize));
	}
	
	public Map<String, Double> createAndUpdateCarDataValues() {
		Map<String, Double> result = new HashMap<String, Double>();
		Stage initCarDataWindow = new Stage();
		initCarDataWindow.initModality(Modality.APPLICATION_MODAL);
		initCarDataWindow.setTitle("Car Data");
		
		doorLengthField.setPromptText("Meter(s)");
		blindZoneValueField.setPromptText("Meter(s)");
		topSpeedField.setPromptText("Km/h");
		
		GridPane textFields = new GridPane();
		textFields.add(doorLenghtLabel, 0, 0);
		textFields.add(doorLengthField, 1, 0);

		textFields.add(blindZoneValueLabel, 0, 1);
		textFields.add(blindZoneValueField, 1, 1);
		
		textFields.add(topSpeedLabel, 0, 2);
		textFields.add(topSpeedField, 1, 2);
		
		Button confirmButton = new Button("Confirm");
		confirmButton.setOnAction(e -> {
			result.put(DOOR_LENGTH, Double.valueOf(doorLengthField.getText()));
			result.put(BLIND_ZONE_VALUE, Double.valueOf(blindZoneValueField.getText()));
			result.put(TOP_SPEED, Double.valueOf(topSpeedField.getText()));
			initCarDataWindow.close();
		});
		
		VBox frame = new VBox(infoLabel, textFields, confirmButton);
		Scene scene = new Scene(frame);
		initCarDataWindow.setScene(scene);
		initCarDataWindow.showAndWait();
		return result;
	}
	

}
