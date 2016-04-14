package software;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.event.ActionEvent;
import javafx.scene.image.ImageView;

public class GUIController2 implements BaseInterface{

	private Main model;
	
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
		model = new Main(1,1,2,3,this);
		model.start();
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
		model.setMode(PARKING);
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
		model.setMode(DRIVING);
	}


	@FXML public void sunButtonPressed(ActionEvent event) {
		model.setWeather(DRY_ASPHALT);
	}


	@FXML public void snowButtonPressed(ActionEvent event) {
		model.setWeather(SNOW);
	}


	@FXML public void rainButtonPressed(ActionEvent event) {
		model.setWeather(WET_ASPHALT);
	}


	@FXML public void iceButtonPressed(ActionEvent event) {
		model.setWeather(ICE);
	}

	public void setLeftBlindZone(Boolean leftBlindZone) {
		this.leftCar.setVisible(leftBlindZone);
	}

	public void setRightBlindZone(Boolean rightBlindZone) {
		this.rightCar.setVisible(rightBlindZone);
	}

	public void setCurrentFrontDistance(Double frontDist) {
		this.currentFrontDistance.setText(String.valueOf(frontDist));
		this.updateDistanceLabelColor(currentFrontDistance, frontDist);
	}

	public void setCurrentLeftDistance(Double leftDistance) {
		this.currentLeftDistance.setText(String.valueOf(leftDistance));
		this.updateDistanceLabelColor(currentLeftDistance, leftDistance);
	}

	public void setCurrentRightDistance(Double rightDistance) {
		this.currentRightDistance.setText(String.valueOf(rightDistance));
		this.updateDistanceLabelColor(currentRightDistance, rightDistance);
	}
	
	public void setCurrentDistanceBehind(Double behindDistance) {
		this.currentDistanceBehind.setText(String.valueOf(behindDistance));
		this.updateDistanceLabelColor(currentDistanceBehind, behindDistance);
	}
	
	
	public void updateDistanceLabelColor(Label label, double currentDistance) {
		double validDistance = model.brakeDistance();
		System.out.println(validDistance);
		if (currentDistance < validDistance) {
			label.setTextFill(Color.RED);
		}
		else if (currentDistance <= (validDistance * 1.25)) {
			label.setTextFill(Color.YELLOW);
		}
		else {
			label.setTextFill(Color.GREEN);
		}
	}

	
}
