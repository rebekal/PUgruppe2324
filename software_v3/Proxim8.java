package final_software;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Proxim8 extends Application implements BaseInterface {

	private UltrasonicController ultraController;
	private BackUpData backUpData;
	private CarData carData;
	private WeatherData weatherData;
	
	private Map<String, Double> carDataValues;
	
	public Proxim8() {
//		Init ultrasonic sensors
		PythonCaller pyCaller = new PythonCaller();
		Map<String, Ultrasonic> ultrasonicSensors = new HashMap<String, Ultrasonic>();
		ultrasonicSensors.put(FRONT, new Ultrasonic(FRONT, pyCaller));
		ultrasonicSensors.put(LEFT, new Ultrasonic(LEFT, pyCaller));
		ultrasonicSensors.put(RIGHT, new Ultrasonic(RIGHT, pyCaller));
		ultrasonicSensors.put(REAR, new Ultrasonic(REAR, pyCaller));
		ultraController = new UltrasonicController(ultrasonicSensors);
		
		backUpData = new BackUpData(carDataFile);
		carDataValues = backUpData.readCarDataFromFile();

		if (carDataValues != null) {
			carData = new CarData(carDataValues.get(DOOR_LENGTH), carDataValues.get(REAR_DOOR_LENGTH), carDataValues.get(BLIND_ZONE_VALUE), carDataValues.get(TOP_SPEED));			
			mainRuleLabel.setVisible(false);
		}
		else {
			carData = new CarData();
			validate(null, false, mainRuleLabel, "Update car data");
			disableModeButtons(driveModeButton, blindZoneModeButton, parkingModeButton, true);
		}
		
		weatherData = new WeatherData();
		weatherData.setUseFrictionValue(DRY_ASPHALT);
	}
	
	final static int WIDTH = 1280;
	final static int HEIGHT = 768;
	final Font customFont = new Font("aria", 30);
	
	final static Image BACKGROUND = new Image(Proxim8.class.getResource("background.png").toString());
	final static Image CAR_1 = new Image(Proxim8.class.getResource("car1.png").toString());
	final static Image CAR_2 = new Image(Proxim8.class.getResource("car2.png").toString());
	final static Image TURN_LIGHT = new Image(Proxim8.class.getResource("light1.png").toString());
	final static Image RED_TRAFFIC_LIGHT = new Image(Proxim8.class.getResource("light2.png").toString());
	
	private IntegerProperty carSpeed = new SimpleIntegerProperty(this, "Car speed");
	private IntegerProperty speedLimit = new SimpleIntegerProperty(this, "Speed limit");
	
	private DoubleProperty frontDistance = new SimpleDoubleProperty(this, "Front distance");
	private DoubleProperty leftDistance = new SimpleDoubleProperty(this, "Left distance");
	private DoubleProperty rightDistance = new SimpleDoubleProperty(this, "Right distance");
	private DoubleProperty rearDistance = new SimpleDoubleProperty(this, "Rear distance");
	private DoubleProperty brakeDistance = new SimpleDoubleProperty(this, "Brake distance");
	
//	Modes
	private BooleanProperty drivingMode = new SimpleBooleanProperty(this, DRIVING_MODE);
	private BooleanProperty blindZoneMode = new SimpleBooleanProperty(this, BLIND_ZONE_MODE);
	private BooleanProperty parkingMode = new SimpleBooleanProperty(this, PARKING_MODE);
	
//	Simulate
	private BooleanProperty simulateActive = new SimpleBooleanProperty(this, "Simulate");
	private BooleanProperty leftCarVisible = new SimpleBooleanProperty(this, "Left blind zone car");
	private BooleanProperty rightCarVisible = new SimpleBooleanProperty(this, "Right blind zone car");
	private BooleanProperty usingLeftTurnLight = new SimpleBooleanProperty(this, "Left turn light");
	private BooleanProperty usingRightTurnLight = new SimpleBooleanProperty(this, "Right turn light");
	private BooleanProperty redTrafficLightVisible = new SimpleBooleanProperty(this, "Red traffic light");
	
	private BooleanProperty settingsWindowVisible = new SimpleBooleanProperty(this, "Settings window");
	
	@FXML
	private final Button driveModeButton = new Button("Drive");
	@FXML
	private final Button blindZoneModeButton = new Button("Blind Zone");
	@FXML
	private final Button parkingModeButton = new Button("Parking");
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		final Label programTitle = new Label("Proxim8");
		programTitle.setFont(new Font("aria", 60));
		programTitle.setTextFill(Color.WHITE);
		programTitle.setLayoutX(985);
		programTitle.setLayoutY(20);
		
		mainRuleLabel.setFont(new Font("aria", 20));
		mainRuleLabel.setLayoutX(1020);
		mainRuleLabel.setLayoutY(250);
		
		final ImageView background = new ImageView(BACKGROUND);
		
		final Rectangle mode1Enabled = new Rectangle();
		mode1Enabled.setFill(Color.GREEN);
		mode1Enabled.setWidth(120);
		mode1Enabled.setHeight(80);
		mode1Enabled.setVisible(false);
		
		final Rectangle mode2Enabled = new Rectangle();
		mode2Enabled.setFill(Color.GREEN);
		mode2Enabled.setWidth(120);
		mode2Enabled.setHeight(80);
		mode2Enabled.setVisible(false);
		
		driveModeButton.setPrefWidth(100);
		driveModeButton.setPrefHeight(60);
		driveModeButton.setLayoutX(920);
		driveModeButton.setLayoutY(120);
		
		blindZoneModeButton.setPrefWidth(100);
		blindZoneModeButton.setPrefHeight(60);
		blindZoneModeButton.setLayoutX(1040);
		blindZoneModeButton.setLayoutY(120);

		parkingModeButton.setPrefWidth(100);
		parkingModeButton.setPrefHeight(60);
		parkingModeButton.setLayoutX(1160);
		parkingModeButton.setLayoutY(120);	
		final Button simulateButton = new Button("Start");
		simulateButton.setPrefWidth(85);
		simulateButton.setPrefHeight(45);
		simulateButton.setLayoutX(1050);
		simulateButton.setLayoutY(300);
		final Button settingsButton = new Button("Settings");
		settingsButton.setPrefWidth(85);
		settingsButton.setPrefHeight(45);
		settingsButton.setLayoutX(1050);
		settingsButton.setLayoutY(400);
		
		final ImageView myCar = new ImageView(CAR_1);
		myCar.setFitHeight(473);
		myCar.setFitWidth(216);
		myCar.setLayoutX(324);
		myCar.setLayoutY(168);
		
		final ImageView leftCar = new ImageView(CAR_2);
		leftCar.setVisible(leftCarVisible.getValue());
		leftCar.setFitHeight(473);
		leftCar.setFitWidth(216);
		leftCar.setLayoutX(36);
		leftCar.setLayoutY(315);
		
		final ImageView rightCar = new ImageView(CAR_2);
		rightCar.setVisible(rightCarVisible.getValue());
		rightCar.setFitHeight(473);
		rightCar.setFitWidth(216);
		rightCar.setLayoutX(615);
		rightCar.setLayoutY(315);
		
		final ImageView leftTurnLight = new ImageView(TURN_LIGHT);
		leftTurnLight.setVisible(usingLeftTurnLight.getValue());
		leftTurnLight.setFitHeight(42);
		leftTurnLight.setFitWidth(38);
		leftTurnLight.setLayoutX(320);
		leftTurnLight.setLayoutY(237);
		
		final ImageView rightTurnLight = new ImageView(TURN_LIGHT);
		rightTurnLight.setVisible(usingRightTurnLight.getValue());
		rightTurnLight.setFitHeight(42);
		rightTurnLight.setFitWidth(38);
		rightTurnLight.setLayoutX(509);
		rightTurnLight.setLayoutY(237);
		
		final ImageView redTrafficLight = new ImageView(RED_TRAFFIC_LIGHT);
		redTrafficLight.setFitHeight(85);
		redTrafficLight.setFitWidth(109);
		
		Text redTrafficLightCountLabel = new Text();
		VBox redLight = new VBox(redTrafficLight, redTrafficLightCountLabel);
		redLight.setVisible(redTrafficLightVisible.getValue());
		redLight.setLayoutX(690);
		redLight.setLayoutY(99);
		
		
		Text frontDistLabel = new Text();
		frontDistLabel.setVisible(false);
		frontDistLabel.setFont(customFont);
		frontDistLabel.setLayoutX(370);
		frontDistLabel.setLayoutY(130);
		Text leftDistLabel = new Text();
		leftDistLabel.setFont(customFont);
		leftDistLabel.setVisible(false);
		leftDistLabel.setLayoutX(175);
		leftDistLabel.setLayoutY(400);
		Text rightDistLabel = new Text();
		rightDistLabel.setFont(customFont);
		rightDistLabel.setVisible(false);
		rightDistLabel.setLayoutX(575);
		rightDistLabel.setLayoutY(400);
		Text rearDistLabel = new Text();
		rearDistLabel.setFont(customFont);
		rearDistLabel.setVisible(false);
		rearDistLabel.setLayoutX(375);
		rearDistLabel.setLayoutY(700);
		
		
		
		Text carSpeedLabel = new Text("Car speed:");
		carSpeedLabel.setFont(customFont);
		carSpeedLabel.setLayoutY(40);
		Text speedLimitLabel = new Text("Speed limit:");
		speedLimitLabel.setFont(customFont);
		speedLimitLabel.setLayoutX(300);
		speedLimitLabel.setLayoutY(40);
		Text brakeDistanceLabel = new Text("Brake distance:");
		brakeDistanceLabel.setFont(customFont);
		brakeDistanceLabel.setLayoutX(600);
		brakeDistanceLabel.setLayoutY(40);
		
		driveModeButton.setOnAction(e -> {
			boolean activate = ! drivingMode.get();
			drivingMode.set(activate);
			frontDistLabel.setVisible(activate);
			mode1Enabled.setLayoutX(910);
			mode1Enabled.setLayoutY(110);
			mode1Enabled.setVisible(activate);
			if (activate) {
				parkingModeButton.setDisable(activate);
			}
			else if (!blindZoneMode.get()) {
				parkingModeButton.setDisable(activate);
			}
		});
		
		blindZoneModeButton.setOnAction(e -> {
			boolean activate = ! blindZoneMode.get();
			blindZoneMode.set(activate);
			mode2Enabled.setLayoutX(1030);
			mode2Enabled.setLayoutY(110);
			mode2Enabled.setVisible(activate);
			if (activate) {
				parkingModeButton.setDisable(activate);
			}
			else if (!drivingMode.get()) {
				parkingModeButton.setDisable(activate);
			}
		});
		
		parkingModeButton.setOnAction(e -> {
			boolean activate = ! parkingMode.get();
			parkingMode.set(activate);
			frontDistLabel.setVisible(activate);
			leftDistLabel.setVisible(activate);
			rightDistLabel.setVisible(activate);
			rearDistLabel.setVisible(activate);
			mode2Enabled.setLayoutX(1150);
			mode2Enabled.setLayoutY(110);
			mode2Enabled.setVisible(activate);
			driveModeButton.setDisable(activate);
			blindZoneModeButton.setDisable(activate);
		});
		
		simulateButton.setOnAction(e -> {
			if (simulateActive.getValue()) {
				simulateButton.setText("Start");
				backUpData.writeCarDataToFile(carData.getDoorLength(), carData.getRearDoorLength(), carData.getBlindZoneValue(), carData.getTopSpeed());
			}
			else {
				simulateButton.setText("Stop");
		        new Thread() {
		            // runnable for that thread
		            public void run() {
		                while (simulateActive.getValue()) {
		                    try {
		                        Thread.sleep(100);
		                    } catch (InterruptedException e) {
		                    }
		                    // update ProgressIndicator on FX thread
		                    Platform.runLater(new Runnable() {
		                        public void run() {
		                        	/*
		                        	if (0 == carData.getCarSpeed()) {
		                        		settingsButton.setDisable(false);
		                        	}
		                        	else {
		                        		settingsButton.setDisable(true);
		                        	}
		                        	*/
		                        	
		                    		if (drivingMode.getValue() || blindZoneMode.getValue()) {
		                    			carData.simulateOneStep();
		                    			
//		                    			Simulted car data
		                    			carSpeed.setValue(carData.getCarSpeed());
		                    			speedLimit.setValue(carData.getSpeedLimit());
		                    			brakeDistance.setValue(getBrakeDistance(carSpeed.getValue(), weatherData.getFrictionValue()));
		                    			
		                    			carSpeedLabel.textProperty().setValue("Car speed: " + carSpeed.getValue() + "km/h");
		                    			speedLimitLabel.textProperty().setValue("Speed limit: " + speedLimit.getValue() + "km/h");
		                    			brakeDistanceLabel.textProperty().setValue("Brake distance: " + String.valueOf(brakeDistance.getValue()) + "m");
		                    			
//		                				Left turn light / right turn light
		                    			usingLeftTurnLight.setValue(carData.isLeftTurnLightOn());
		                    			usingRightTurnLight.setValue(carData.isRightTurnLightOn());
		                    			leftTurnLight.setVisible(usingLeftTurnLight.getValue());
		                    			rightTurnLight.setVisible(usingRightTurnLight.getValue());
		                    			
		                    			
//		                				Traffic light (red only)
		                    			redLight.setVisible(carData.getRedTrafficLight());
		                    			redTrafficLightCountLabel.textProperty().setValue(String.valueOf(carData.getRedTrafficLightCount()));
		                    		}
		                    		
		                    		if (drivingMode.getValue()) {
		                    			Double frontDistance = ultraController.getSensorValue(FRONT, true);
		                    			updateDistance(frontDistLabel, frontDistance, brakeDistance.getValue());
		                    			
		                    			if (isSensorValueLargerThan(frontDistance, brakeDistance.getValue())) {
//		                    				TODO alarm / LED ON ?
		                    			}
		                    			else {
//		                    				TODO alarm / LED OFF ?
		                    			}
		                    		}
		                    		
		                    		if (blindZoneMode.getValue()) {
		                    			boolean leftBlind = isObjectInBlindZone(ultraController.getSensorValue(LEFT, true), carData.getBlindZoneValue());
		                    			boolean rightBlind = isObjectInBlindZone(ultraController.getSensorValue(RIGHT, true), carData.getBlindZoneValue());
		                    			if (usingLeftTurnLight.getValue() && leftBlind) {
		                    				leftCar.setVisible(true);
//		                    				TODO alarm / LED ON ?
		                    			}
		                    			else {
		                    				leftCar.setVisible(false);
//		                    				TODO alarm / LED OFF ?
		                    			}
		                    			if (usingRightTurnLight.getValue() && rightBlind) {
		                    				rightCar.setVisible(true);
//		                    				TODO alarm / LED OFF ?
		                    			}
		                    			else {
		                    				rightCar.setVisible(false);
//		                    				TODO alarm / LED OFF ?
		                    			}
		                    			
		                    		}
		                    		
		                    		if (parkingMode.getValue()) {
		                    			double frontDistance = ultraController.getSensorValue(FRONT, true);
		                    			double leftDistance = ultraController.getSensorValue(LEFT, true);
		                    			double rightDistance = ultraController.getSensorValue(RIGHT, true);
		                    			double rearDistance = ultraController.getSensorValue(REAR, true);
		                    			double currentDoorLength = carData.getDoorLength();
		                    			double currentRearDoorLength = carData.getRearDoorLength();
		                    			
		                    			updateDistance(frontDistLabel, frontDistance, 0.5);
		                    			updateDistance(leftDistLabel, leftDistance, currentDoorLength);
		                    			updateDistance(rightDistLabel, rightDistance, currentDoorLength);
		                    			updateDistance(rearDistLabel, rearDistance, currentRearDoorLength);
		                    			
		                    			if (isSensorValueLargerThan(leftDistance, currentDoorLength)) {
//		                    				TODO alarm / LED OFF ?
		                    			}
		                    			else {
//		                    				TODO alarm / LED ON ?
		                    			}
		                    			if (isSensorValueLargerThan(rightDistance, currentDoorLength)) {
//		                    				TODO alarm / LED OFF ?
		                    			}
		                    			else {
//		                    				TODO alarm / LED ON ?
		                    			}
		                    		}
		                        }
		                    });
		                }
		                interrupt(); // TODO optimalisere
		                
		            }
		        }.start();
			}
			simulateActive.setValue(! simulateActive.getValue());
		});
		
		final HBox settingsWindow = createSettingsWindow();
		settingsWindow.setLayoutX(900);
		settingsWindow.setLayoutY(500);
		settingsButton.setOnAction(e -> {
			settingsWindowVisible.setValue(! settingsWindowVisible.getValue());
			settingsWindow.setVisible(settingsWindowVisible.getValue());
			if (mainRuleLabel.isVisible()) {
				disableModeButtons(driveModeButton, blindZoneModeButton, parkingModeButton, true);
			}
			else if (drivingMode.getValue() || blindZoneMode.getValue() || parkingMode.getValue()) {
				// avoid changing mode after closing settings
			}
			else {
				disableModeButtons(driveModeButton, blindZoneModeButton, parkingModeButton, false);
			}
		});
		
		final Group root = new Group(background, programTitle, mode1Enabled, mode2Enabled, settingsWindow, 
									myCar, leftCar, rightCar, leftTurnLight, rightTurnLight, carSpeedLabel,
									speedLimitLabel, brakeDistanceLabel, frontDistLabel, leftDistLabel,
									rightDistLabel, rearDistLabel, driveModeButton, blindZoneModeButton,
									parkingModeButton, simulateButton, settingsButton, mainRuleLabel, redLight);
		Scene scene = new Scene(root, WIDTH, HEIGHT);		
		primaryStage.setTitle("Aproxym8");
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	private void disableModeButtons(Button driveModeButton, Button blindZoneModeButton, Button parkingModeButton, boolean disable) {
		driveModeButton.setDisable(disable);
		blindZoneModeButton.setDisable(disable);
		parkingModeButton.setDisable(disable);
	}
	
	private Label mainRuleLabel = new Label("Rule");
	private Label updateRuleLabel = new Label("Update rule");
	
	private HBox createSettingsWindow() {
		updateRuleLabel.setVisible(false);
		TextField doorLengthField = new TextField();
		doorLengthField.setPromptText("meter");
		doorLengthField.setMaxWidth(75);
		doorLengthField.setOnAction(e -> {
			validateInput(doorLengthField, DOOR_LENGTH, 0.0, null);
		});
		
		TextField rearDoorLengthField = new TextField();
		rearDoorLengthField.setPromptText("meter");
		rearDoorLengthField.setMaxWidth(75);
		rearDoorLengthField.setOnAction(e -> {
			validateInput(rearDoorLengthField, REAR_DOOR_LENGTH, 0.0, null);
		});
		
		TextField blindZoneValueField = new TextField();
		blindZoneValueField.setMaxWidth(75);
		blindZoneValueField.setPromptText("meter");
		blindZoneValueField.setOnAction(e -> {
			validateInput(blindZoneValueField, BLIND_ZONE_VALUE, 0.0, null);
		});
		TextField topSpeedField = new TextField();
		topSpeedField.setMaxWidth(75);
		topSpeedField.setPromptText("km/h");
		topSpeedField.setOnAction(e -> {
			validateInput(topSpeedField, TOP_SPEED, 0.0, null);
		});
		
		Label doorLengthLabel = new Label("Door length");
		doorLengthLabel.setTextFill(Color.ORANGE);
		Label rearDoorLengthLabel = new Label("Rear door length   ");
		rearDoorLengthLabel.setTextFill(Color.ORANGE);
		Label blindZoneValueLabel = new Label("Blind zone value");
		blindZoneValueLabel.setTextFill(Color.ORANGE);
		Label topSpeedLabel = new Label("Top speed");
		topSpeedLabel.setTextFill(Color.ORANGE);
		
		VBox infoFields = new VBox();
		Text door = new Text(DOOR_LENGTH + ": " + carData.getDoorLength() + "m");
		door.setFill(Color.ORANGE);
		Text rearDoor = new Text(REAR_DOOR_LENGTH + ": " + carData.getRearDoorLength() + "m");
		rearDoor.setFill(Color.ORANGE);
		Text blindZone = new Text(BLIND_ZONE_VALUE + ": " + carData.getBlindZoneValue() + "m");
		blindZone.setFill(Color.ORANGE);
		Text topSpeed = new Text(TOP_SPEED + ": " + carData.getTopSpeed() + "km/h");
		topSpeed.setFill(Color.ORANGE);
		infoFields.getChildren().add(door);
		infoFields.getChildren().add(rearDoor);
		infoFields.getChildren().add(blindZone);
		infoFields.getChildren().add(topSpeed);
		infoFields.setVisible(false);
		
		GridPane updateFields = new GridPane();
		updateFields.add(doorLengthLabel, 0, 0);
		updateFields.add(doorLengthField, 1, 0);
		
		updateFields.add(rearDoorLengthLabel, 0, 1);
		updateFields.add(rearDoorLengthField, 1, 1);
		
		updateFields.add(blindZoneValueLabel, 0, 2);
		updateFields.add(blindZoneValueField, 1, 2);
		
		updateFields.add(topSpeedLabel, 0, 3);
		updateFields.add(topSpeedField, 1, 3);
		
		updateFields.setVisible(false);
		Button infoButton = new Button("Info");
		infoButton.setPrefWidth(125);
		infoButton.setPrefHeight(40);
		infoButton.setOnAction(e -> {
			boolean isVisible = ! infoFields.isVisible();
			infoFields.setVisible(isVisible);
			/*
			if (isVisible) {
				updateFields.setVisible(! isVisible);				
			}
			*/
		});
		
		Button updateButton = new Button("Update");
		updateButton.setPrefWidth(125);
		updateButton.setPrefHeight(40);
		updateButton.setOnAction(e -> {
			boolean isVisible = ! updateFields.isVisible();
			updateFields.setVisible(isVisible);
			/*
			if (isVisible) {
				infoFields.setVisible(! isVisible);				
			}
			*/
		});
		
		VBox info = new VBox(infoButton, infoFields);
		VBox update = new VBox(updateButton, updateFields, updateRuleLabel);
		HBox settingsWindow = new HBox(info, update);
		settingsWindow.setLayoutX(1050);
		settingsWindow.setLayoutY(500);
		settingsWindow.setVisible(settingsWindowVisible.getValue());
		return settingsWindow;
	}
	
	private void validateInput(TextField field, String carValue, Double min, Double max) {
		if (! isValidDouble(field.getText())) {
			validate(field, false, updateRuleLabel, "Invalid number");			
		}
		else if (! isInsideBounderies(Double.valueOf(field.getText()), min, max)) {
			validate(field, false, updateRuleLabel, convert(min, max));
		}
		else {
			validate(field, true, updateRuleLabel, "OK");
			carData.updateValue(carValue, Double.valueOf(field.getText()));
		}
	}

	private String convert(Double min, Double max) {
    	if (min != null && max == null) {
    		return min + " <= value <= inf";
    	}
    	else if (min == null && max != null) {
    		return "-inf <= value <= " + max;
    	}
    	return min + " <= value <= " + max;
	}

//	*** GUI methods ***
	
	private void updateDistance(Text label, Double currentDistance, double validDistance) {
		label.setText(String.valueOf(currentDistance) + "m");
		
		if (currentDistance < validDistance) {
			label.setFill(Color.RED);
		}
		else if (currentDistance <= (validDistance * 1.05)) {
			label.setFill(Color.YELLOW);
		}
		else {
			label.setFill(Color.GREEN);
		}
	}
	
    private void validate(TextField textField, boolean isValid, Label ruleField, String ruleMessage) {
    	if (textField != null) {
        	if (textField.getText().equals("")) {
        		return;
        	}
    		String color = isValid ? "green" : "red";
    		textField.setStyle("-fx-background-color: " + color);
    	}
        if (ruleField != null) {
        	ruleField.setText(ruleMessage);
        	ruleField.setTextFill(Color.RED);
        	ruleField.setVisible(! isValid);
        }
    }
    
    private boolean isInsideBounderies(double value, Double min, Double max) {
    	if (min == null && max == null) {
    		return true;
    	}
    	else if (min != null && max == null) {
    		return min <= value;
    	}
    	else if (min == null && max != null) {
    		return value <= max;
    	}
    	return min <= value && value <= max;
    }
    
    private boolean isValidDouble(String text) {
    	try {
    		Double value = Double.valueOf(text);
    	} catch (NumberFormatException e) {
    		return false;
    	}
    	return true;
    }


//	*** Main methods ***
	
	/*
	 *  Formula brake distance : velocity^2 / (2 * friction * gravity) + (velocity * reactiontime)
	 *  - where velocity is measured in meter/second
	 *  - gravity = 9.81m/s^2
	 *  - reactiontime = 1
	 */	
	public int getBrakeDistance(double carSpeed, double frictionValue) {
		double meterPerSecond = carSpeed / 3.6;
		return (int) (Math.pow(meterPerSecond, 2) / (2 * frictionValue * 9.81) + meterPerSecond);
	}
	
	/*
	 * Used for:
	 * - brake distance < front sensor value
	 * - door length < left/right sensor value
	 */
	public boolean isSensorValueLargerThan(Double sensorValue, double value) {
		return (sensorValue != null) ? value < sensorValue : true;
	}
	
	/*
	 * Checking left or right ultrasonic sensor if there's a object (car) in blind zone
	 * If timeout (value == null): blind zone clear
	 * If value < car's blind zone value -> blind zone not clear
	 */
	public boolean isObjectInBlindZone(Double sensorValue, double blindZoneValue) {
		return (sensorValue != null) ? (sensorValue < blindZoneValue) : false;
	}
	
	
	
	public static void main(String[] args) {
		launch(args);
		
	}
}
