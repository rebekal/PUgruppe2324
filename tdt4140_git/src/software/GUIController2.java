package software;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import software.Car;
import javafx.event.ActionEvent;

public class GUIController2 {

	private Car model;
	
	@FXML Label leftBlindZone;
	@FXML Label rightBlindZone;
	@FXML Label currentFrontDistance;
	@FXML Label currentLeftDistance;
	@FXML Label currentRightDistance;
	@FXML Button sunButton;
	@FXML Button rainButton;
	@FXML Button snowButton;
	@FXML Button iceButton;
	@FXML Button parkingButton;
	@FXML Button drivingButton;
	@FXML Label distanceFront;
	@FXML Label distanceLeft;
	@FXML Label distanceRight;

	
	public void initialize() {
		model = new Car(this);
	}

	

	@FXML public void parkingButtonPressed(ActionEvent event) {
		distanceLeft.setVisible(true);
		distanceFront.setVisible(false);
		distanceRight.setVisible(true);
		currentLeftDistance.setVisible(true);
		currentRightDistance.setVisible(true);
		currentFrontDistance.setVisible(false);
		leftBlindZone.setVisible(false);
		rightBlindZone.setVisible(false);
		sunButton.setVisible(false);
		rainButton.setVisible(false);
		snowButton.setVisible(false);
		iceButton.setVisible(false);
		model.setMode("Parking");
	}


	@FXML public void drivingButtonPressed(ActionEvent event) {
		distanceLeft.setVisible(false);
		distanceFront.setVisible(true);
		distanceRight.setVisible(false);
		currentLeftDistance.setVisible(false);
		currentRightDistance.setVisible(false);
		currentFrontDistance.setVisible(true);
		leftBlindZone.setVisible(true);
		rightBlindZone.setVisible(true);
		sunButton.setVisible(true);
		rainButton.setVisible(true);
		snowButton.setVisible(true);
		iceButton.setVisible(true);
		model.setMode("Driving");
	}


	@FXML public void sunButtonPressed(ActionEvent event) {
		model.setWeather("Sun");
	}


	@FXML public void snowButtonPressed(ActionEvent event) {
		model.setWeather("Snow");
	}


	@FXML public void rainButtonPressed(ActionEvent event) {
		model.setWeather("Rain");
	}


	@FXML public void iceButtonPressed(ActionEvent event) {
		model.setWeather("Ice");
	}

	public void setLeftBlindZone(Boolean leftBlindZone) {
		this.leftBlindZone.setVisible(leftBlindZone);
	}

	public void setRightBlindZone(Boolean rightBlindZone) {
		this.rightBlindZone.setVisible(rightBlindZone);
	}

	public void setCurrentFrontDistance(String frontDistance) {
		this.currentFrontDistance.setText(frontDistance);
		
	}

	public void setCurrentLeftDistance(String leftDistance) {
		this.currentLeftDistance.setText(leftDistance);
	}

	public void setCurrentRightDistance(String rightDistance) {
		this.currentRightDistance.setText(rightDistance);
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
