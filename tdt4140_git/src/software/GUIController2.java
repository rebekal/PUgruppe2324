package software;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import software.Car;
import javafx.event.ActionEvent;
import javafx.scene.image.ImageView;

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
	@FXML Label distanceBehind;
	@FXML Label currentDistanceBehind;
	@FXML ImageView leftCar;
	@FXML ImageView rightCar;

	
	public void initialize() {
		model = new Car(this);
	}


	@FXML public void parkingButtonPressed(ActionEvent event) {
		distanceLeft.setVisible(true);
		distanceFront.setVisible(true);
		distanceRight.setVisible(true);
		distanceBehind.setVisible(true);
		currentDistanceBehind.setVisible(true);
		currentLeftDistance.setVisible(true);
		currentRightDistance.setVisible(true);
		currentFrontDistance.setVisible(false);
		leftCar.setVisible(false);
		rightCar.setVisible(false);
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
		distanceBehind.setVisible(false);
		currentDistanceBehind.setVisible(false);
		currentLeftDistance.setVisible(false);
		currentRightDistance.setVisible(false);
		currentFrontDistance.setVisible(true);
		leftCar.setVisible(true);
		rightCar.setVisible(true);
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
		this.leftCar.setVisible(leftBlindZone);
	}

	public void setRightBlindZone(Boolean rightBlindZone) {
		this.rightCar.setVisible(rightBlindZone);
	}

	public void setCurrentFrontDistance(Double frontDistance) {
		this.currentFrontDistance.setText(String.valueOf(frontDistance));
		this.updateDistanceLabelColor(currentFrontDistance, frontDistance);
	}

	public void setCurrentLeftDistance(Double leftDistance) {
		this.currentLeftDistance.setText(String.valueOf(leftDistance));
		this.updateDistanceLabelColor(currentLeftDistance, leftDistance);
	}

	public void setCurrentRightDistance(Double rightDistance) {
		this.currentRightDistance.setText(String.valueOf(rightDistance));
		this.updateDistanceLabelColor(currentRightDistance, rightDistance);
	}
	
	
	public void updateDistanceLabelColor(Label label, double currentDistance) {
		validDistance = model.getValidDistance();
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
