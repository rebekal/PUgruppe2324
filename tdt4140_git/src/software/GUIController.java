package software;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class GUIController extends Application implements BaseInterface, Initializable {
	
	private Main main;

	@FXML
	private ToggleButton drivingButton, parkingButton, stopButton, dryAsphaltButton, wetAsphaltButton, snowButton, iceButton;
	
	@FXML
	private Label distanceFront, distanceLeft, distanceRight, distanceRear;
	
	@FXML
	private Rectangle screenBackground, menuBackground;
	
	@FXML
	private ImageView leftCar, myCar, rightCar;
	
	public GUIController(Main main) {
		this.main = main;
	}

	public ToggleButton getDrivingButton() {
		return drivingButton;
	}

	public ToggleButton getParkingButton() {
		return parkingButton;
	}

	public ToggleButton getStopButton() {
		return stopButton;
	}

	public ToggleButton getDryAsphaltButton() {
		return dryAsphaltButton;
	}

	public ToggleButton getWetAsphaltButton() {
		return wetAsphaltButton;
	}

	public ToggleButton getSnowButton() {
		return snowButton;
	}

	public ToggleButton getIceButton() {
		return iceButton;
	}

	public Label getDistanceFront() {
		return distanceFront;
	}

	public void setDistanceFront(String distanceFront) {
		this.distanceFront.setText(distanceFront);
	}

	public Label getDistanceLeft() {
		return distanceLeft;
	}

	public void setDistanceLeft(String distanceLeft) {
		this.distanceLeft.setText(distanceLeft);
	}

	public Label getDistanceRight() {
		return distanceRight;
	}

	public void setDistanceRight(String distanceRight) {
		this.distanceRight.setText(distanceRight);
	}

	public Label getDistanceRear() {
		return distanceRear;
	}

	public void setDistanceRear(String distanceRear) {
		this.distanceRear.setText(distanceRear);
	}

	public Rectangle getScreenBackground() {
		return screenBackground;
	}

	public void setScreenBackground(Color screenBackground) {
		this.screenBackground.setFill(screenBackground);
	}

	public Rectangle getMenuBackground() {
		return menuBackground;
	}

	public void setMenuBackground(Color menuBackground) {
		this.menuBackground.setFill(menuBackground);
	}

	public ImageView getLeftCar() {
		return leftCar;
	}

	public ImageView getMyCar() {
		return myCar;
	}

	public ImageView getRightCar() {
		return rightCar;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// Main screen
		myCar.setVisible(true);
		leftCar.setVisible(false);
		rightCar.setVisible(false);
		setScreenBackground(Color.web("#d6f1ff"));
		distanceFront.setVisible(false);
		distanceLeft.setVisible(false);
		distanceRight.setVisible(false);
		distanceRear.setVisible(false);
		
		// Menu
		setMenuBackground(Color.BLACK);
		drivingButton.setDisable(false);
		parkingButton.setDisable(false);
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(Main.class.getResource("GUI2.fxml"));
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
	}
	
	@FXML
	public void drivingModeButtonClicked() {
		// Sets all required FXML items visible for driving mode
		distanceFront.setVisible(true);
		dryAsphaltButton.setVisible(true);
		wetAsphaltButton.setVisible(true);
		snowButton.setVisible(true);
		iceButton.setVisible(true);
		
		// Sets all unnecessary labels invisible
		distanceLeft.setVisible(false);
		distanceRight.setVisible(false);
		distanceRear.setVisible(false);
		// Disables parking mode button
		parkingButton.setDisable(true);
	}
	
	@FXML
	public void dryAsphaltButtonClicked() {
		main.setUseFrictionValue(DRY_ASPHALT);
	}
	
	@FXML
	public void wetAsphaltButtonClicked() {
		main.setUseFrictionValue(WET_ASPHALT);
	}
	
	@FXML
	public void snowButtonClicked() {
		main.setUseFrictionValue(SNOW);
	}
	
	@FXML
	public void iceButtonClicked() {
		main.setUseFrictionValue(ICE);
	}
	
	@FXML
	public void parkingModeButtonClicked() {
		distanceFront.setVisible(false);
		dryAsphaltButton.setVisible(false);
		wetAsphaltButton.setVisible(false);
		snowButton.setVisible(false);
		iceButton.setVisible(false);
		
		distanceLeft.setVisible(true);
		distanceRight.setVisible(true);
		distanceRear.setVisible(true);
		
		drivingButton.setDisable(true);
	}
	
	@FXML
	public void objectDetectedInLeftBlindZone() {
		screenBackground.setFill(new LinearGradient(125, 0, 225, 0, false, CycleMethod.NO_CYCLE, new Stop[] { new Stop(0, Color.BLACK), new Stop(1, Color.RED)}));
	}
	
	@FXML
	public void blindZoneScreenUpdate(String side, boolean visible) {
		switch (side) {
		case LEFT: leftCar.setVisible(visible); break;
		case RIGHT: rightCar.setVisible(visible); break;
		default: System.out.println("No case was found for '" + side + "'.");
		}
	}
	
	@FXML
	public void frontDistance() {
		
	}
	
	@FXML
	public void rearDistance() {
		
	}

	public void updateDistanceLabelColor(Label label, double validDistance, double currentDistance) {
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

}
