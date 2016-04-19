package working;

import java.util.HashMap;
import java.util.Map;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
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
			validate(null, false, mainRuleLabel, "Update vehicle data");
			disableModeButtons(driveModeButton, blindZoneModeButton, parkingModeButton, true);
		}
		weatherData = new WeatherData(); // TODO fix weather simulation
	}
	
	final static int WIDTH = 1920; //1280;
	final static int HEIGHT = 1000; //768;
	final Font customFont = new Font("Aria", 40);
	private Label mainRuleLabel = new Label("Main rule");
	
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
	private DoubleProperty currentDoorLength = new SimpleDoubleProperty(this, "Current door length");
	private DoubleProperty currentRearDoorLength = new SimpleDoubleProperty(this, "Current rear door length");
	
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
	private final Button blindZoneModeButton = new Button("Blindspot");
	@FXML
	private final Button parkingModeButton = new Button("Parking");
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		final Label programTitle = new Label("Proxim8");
		programTitle.setFont(new Font("Aria", 90));
		programTitle.setTextFill(Color.WHITE);
		programTitle.setLayoutX(1480); //985
		programTitle.setLayoutY(20); //20
		
		mainRuleLabel.setFont(new Font("Aria", 40));
		mainRuleLabel.setLayoutX(1470); //1020
		mainRuleLabel.setLayoutY(400); //250
		
		final ImageView background = new ImageView(BACKGROUND);
		background.setFitHeight(HEIGHT);
		background.setFitWidth(WIDTH);
		
		int modeButtonWidth = 150, modeButtonHeight = 90;
		final Rectangle mode1Enabled = new Rectangle();
		mode1Enabled.setFill(Color.GREEN);
		mode1Enabled.setWidth(modeButtonWidth + 20); //120
		mode1Enabled.setHeight(modeButtonHeight + 20); //80
		mode1Enabled.setVisible(false);
		
		final Rectangle mode2Enabled = new Rectangle();
		mode2Enabled.setFill(Color.GREEN);
		mode2Enabled.setWidth(modeButtonWidth + 20);
		mode2Enabled.setHeight(modeButtonHeight + 20);
		mode2Enabled.setVisible(false);
		
		Font modeButtonFont = new Font("Aria", 24);
		driveModeButton.setPrefWidth(modeButtonWidth); //100
		driveModeButton.setPrefHeight(modeButtonHeight); //60
		driveModeButton.setLayoutX(1380); //920
		driveModeButton.setLayoutY(180); //120
		driveModeButton.setFont(modeButtonFont);
		
		blindZoneModeButton.setPrefWidth(modeButtonWidth);
		blindZoneModeButton.setPrefHeight(modeButtonHeight);
		blindZoneModeButton.setLayoutX(1560); //1040
		blindZoneModeButton.setLayoutY(180); //120
		blindZoneModeButton.setFont(modeButtonFont);

		parkingModeButton.setPrefWidth(modeButtonWidth);
		parkingModeButton.setPrefHeight(modeButtonHeight);
		parkingModeButton.setLayoutX(1740);	//1160
		parkingModeButton.setLayoutY(180); //120
		parkingModeButton.setFont(modeButtonFont);
		final Button simulateButton = new Button("Start");
		simulateButton.setPrefWidth(140); //85
		simulateButton.setPrefHeight(60); //45
		simulateButton.setLayoutX(1565); //1050
		simulateButton.setLayoutY(500); //300
		final Button settingsButton = new Button("Settings");
		settingsButton.setPrefWidth(140); //85
		settingsButton.setPrefHeight(60);//45
		settingsButton.setLayoutX(1565); //1050
		settingsButton.setLayoutY(600); //400
		
		final ImageView myCar = new ImageView(CAR_1);
		myCar.setFitHeight(650); //473
		myCar.setFitWidth(300); //216
		myCar.setLayoutX(500); //324
		myCar.setLayoutY(200); //168
		
		final ImageView leftCar = new ImageView(CAR_2);
		leftCar.setVisible(leftCarVisible.getValue());
		leftCar.setFitHeight(650); //473
		leftCar.setFitWidth(300); //216
		leftCar.setLayoutX(100); //36
		leftCar.setLayoutY(450);//315
		
		final ImageView rightCar = new ImageView(CAR_2);
		rightCar.setVisible(rightCarVisible.getValue());
		rightCar.setFitHeight(650);
		rightCar.setFitWidth(300);
		rightCar.setLayoutX(900); //615
		rightCar.setLayoutY(450); //315
		
		final ImageView leftTurnLight = new ImageView(TURN_LIGHT);
		leftTurnLight.setVisible(usingLeftTurnLight.getValue());
		leftTurnLight.setFitHeight(50); //42
		leftTurnLight.setFitWidth(46); //38
		leftTurnLight.setLayoutX(495); //320
		leftTurnLight.setLayoutY(300); //237
		
		final ImageView rightTurnLight = new ImageView(TURN_LIGHT);
		rightTurnLight.setVisible(usingRightTurnLight.getValue());
		rightTurnLight.setFitHeight(50); //42
		rightTurnLight.setFitWidth(46); //38
		rightTurnLight.setLayoutX(758); //509
		rightTurnLight.setLayoutY(300); //237
		
		final ImageView redTrafficLight = new ImageView(RED_TRAFFIC_LIGHT);
		redTrafficLight.setFitHeight(85);
		redTrafficLight.setFitWidth(109);
		
		Text redTrafficLightCountLabel = new Text();
		VBox redLight = new VBox(redTrafficLight, redTrafficLightCountLabel);
		redLight.setVisible(redTrafficLightVisible.getValue());
		redLight.setLayoutX(1250); //690, Y = 99
		
		
		Text frontDistLabel = new Text();
		frontDistLabel.setVisible(false);
		frontDistLabel.setFont(customFont);
		frontDistLabel.setLayoutX(605); //370
		frontDistLabel.setLayoutY(150); //130
		Text leftDistLabel = new Text();
		leftDistLabel.setVisible(false);
		leftDistLabel.setFont(customFont);
		leftDistLabel.setLayoutX(300); //175
		leftDistLabel.setLayoutY(500); //400
		Text rightDistLabel = new Text();
		rightDistLabel.setVisible(false);
		rightDistLabel.setFont(customFont);
		rightDistLabel.setLayoutX(860); //575
		rightDistLabel.setLayoutY(500); //400
		Text rearDistLabel = new Text();
		rearDistLabel.setVisible(false);
		rearDistLabel.setFont(customFont);
		rearDistLabel.setLayoutX(600); //375
		rearDistLabel.setLayoutY(925); //700
		
		
		
		Text carSpeedLabel = new Text("Car speed:");
		carSpeedLabel.setFont(customFont);
		carSpeedLabel.setLayoutY(40);
		Text speedLimitLabel = new Text("Speed limit:");
		speedLimitLabel.setFont(customFont);
		speedLimitLabel.setLayoutX(400); //300
		speedLimitLabel.setLayoutY(40);
		Text brakeDistanceLabel = new Text("Brake distance:");
		brakeDistanceLabel.setFont(customFont);
		brakeDistanceLabel.setLayoutX(900); //600
		brakeDistanceLabel.setLayoutY(40);
		Text weatherLabel = new Text("Weather : ");
		weatherLabel.setFont(customFont);
//		weatherLabel.setLayoutX(value);
		weatherLabel.setLayoutY(80);
		
		driveModeButton.setOnAction(e -> {
			boolean activate = ! drivingMode.get();
			drivingMode.set(activate);
			frontDistLabel.setVisible(activate);
			mode1Enabled.setLayoutX(1370); //910
			mode1Enabled.setLayoutY(170); //110
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
			mode2Enabled.setLayoutX(1550); //1030
			mode2Enabled.setLayoutY(170); //110
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
			mode2Enabled.setLayoutX(1730); //1150
			mode2Enabled.setLayoutY(170); //110
			mode2Enabled.setVisible(activate);
			driveModeButton.setDisable(activate);
			blindZoneModeButton.setDisable(activate);
		});
		
		simulateButton.setOnAction(e -> {
			if (simulateActive.getValue()) {
				simulateButton.setText("Start");
				carData.resetSimulation();
				weatherData.resetCount();
			}
			else {
				simulateButton.setText("Stop");
				currentDoorLength.setValue(carData.getDoorLength());
				currentRearDoorLength.setValue(carData.getRearDoorLength());
				
		        new Thread() {
		            // runnable for that thread
		            public void run() {
		                while (simulateActive.getValue()) {
		                    // increase sleeptimer incase GUI becomes slow
		                	try {
		                        Thread.sleep(100);
		                    } catch (InterruptedException e) {
		                    }
		                    // update ProgressIndicator on FX thread
		                    Platform.runLater(new Runnable() {
		                        public void run() {
		                        	
		                    		if (drivingMode.getValue() || blindZoneMode.getValue()) {
		                    			carData.simulateOneStep();
		                    			weatherData.update();
		                    			
//		                    			Simulted car data
		                    			carSpeed.setValue(carData.getCarSpeed());
		                    			speedLimit.setValue(carData.getSpeedLimit());
		                    			brakeDistance.setValue(getBrakeDistance(carSpeed.getValue(), weatherData.getFrictionValue()));
		                    			
		                    			carSpeedLabel.textProperty().setValue("Car speed: " + carSpeed.getValue() + "km/h");
		                    			speedLimitLabel.textProperty().setValue("Speed limit: " + speedLimit.getValue() + "km/h");
		                    			brakeDistanceLabel.textProperty().setValue("Brake distance: " + String.valueOf(brakeDistance.getValue()) + "m");
		                    			weatherLabel.textProperty().setValue("Weather: " + weatherData.getFrictionString());
		                    			
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
		                    			frontDistance.setValue(ultraController.getSensorValue(FRONT, true));
		                    			updateDistance(frontDistLabel, frontDistance.getValue(), brakeDistance.getValue());
		                    			
		                    			if (isSensorValueLargerThan(frontDistance.getValue(), brakeDistance.getValue())) {
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
		                    			frontDistance.setValue(ultraController.getSensorValue(FRONT, true));
		                    			leftDistance.setValue(ultraController.getSensorValue(LEFT, true));
		                    			rightDistance.setValue(ultraController.getSensorValue(RIGHT, true));
		                    			rearDistance.setValue(ultraController.getSensorValue(REAR, true));
		                    			
		                    			updateDistance(frontDistLabel, frontDistance.getValue(), 0.5);
		                    			updateDistance(leftDistLabel, leftDistance.getValue(), currentDoorLength.getValue());
		                    			updateDistance(rightDistLabel, rightDistance.getValue(), currentDoorLength.getValue());
		                    			updateDistance(rearDistLabel, rearDistance.getValue(), currentRearDoorLength.getValue());
		                    			
		                    			if (isSensorValueLargerThan(leftDistance.getValue(), currentDoorLength.getValue())) {
//		                    				TODO alarm / LED OFF ?
		                    			}
		                    			else {
//		                    				TODO alarm / LED ON ?
		                    			}
		                    			if (isSensorValueLargerThan(rightDistance.getValue(), currentDoorLength.getValue())) {
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
		
		final GridPane settingsWindow = createSettingsWindow();
		settingsWindow.setLayoutX(1370); //1450
		settingsWindow.setLayoutY(700); //500
		settingsButton.setOnAction(e -> {
			settingsWindowVisible.setValue(! settingsWindowVisible.getValue());
			settingsWindow.setVisible(settingsWindowVisible.getValue());
			updateRuleLabel.setVisible(! settingsWindowVisible.getValue());
			saveLabel.setVisible(! settingsWindowVisible.getValue());
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
									speedLimitLabel, brakeDistanceLabel, weatherLabel, frontDistLabel, leftDistLabel,
									rightDistLabel, rearDistLabel, driveModeButton, blindZoneModeButton,
									parkingModeButton, simulateButton, settingsButton, mainRuleLabel, redLight);
		Scene scene = new Scene(root, WIDTH, HEIGHT);		
		primaryStage.setTitle("Proxim8");
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	private void disableModeButtons(Button driveModeButton, Button blindZoneModeButton, Button parkingModeButton, boolean disable) {
		driveModeButton.setDisable(disable);
		blindZoneModeButton.setDisable(disable);
		parkingModeButton.setDisable(disable);
	}
	
	
	
	private Label updateRuleLabel = new Label("Update rule");
	private	Label saveLabel = new Label("Succuessfully saved");
	private GridPane createSettingsWindow() {
		Color settingsTextColor = Color.ORANGE;
		updateRuleLabel.setVisible(false);
		saveLabel.setVisible(false);
		saveLabel.setFont(new Font("Aria", 20));
		saveLabel.setTextFill(Color.GREEN);
		saveLabel.setPadding(new Insets(0, 0, 0, 5));
		
//		>Settings>info
		Font infoFont = new Font("Aria", 18);
		Insets infoInsets = new Insets(0, 0, 5, 0);
		Label door = new Label(DOOR_LENGTH + ": ");
		door.setTextFill(settingsTextColor);
		door.setFont(infoFont);
		door.setPadding(new Insets(5, 0, 5, 0));
		Label doorValue = new Label(String.valueOf(carData.getDoorLength()) + "m");
		doorValue.setTextFill(settingsTextColor);
		doorValue.setFont(infoFont);
		Label rearDoor = new Label(REAR_DOOR_LENGTH + ": ");
		rearDoor.setTextFill(settingsTextColor);
		rearDoor.setFont(infoFont);
		rearDoor.setPadding(infoInsets);
		Label rearDoorValue = new Label(String.valueOf(carData.getRearDoorLength()) + "m");
		rearDoorValue.setTextFill(settingsTextColor);
		rearDoorValue.setFont(infoFont);
		Label blindZone = new Label(BLIND_ZONE_VALUE + ": ");
		blindZone.setTextFill(settingsTextColor);
		blindZone.setFont(infoFont);
		blindZone.setPadding(infoInsets);
		Label blindZoneValue = new Label(String.valueOf(carData.getBlindZoneValue()) + "m");
		blindZoneValue.setTextFill(settingsTextColor);
		blindZoneValue.setFont(infoFont);
		Label topSpeed = new Label(TOP_SPEED + ": ");
		topSpeed.setTextFill(settingsTextColor);
		topSpeed.setFont(infoFont);
		topSpeed.setPadding(infoInsets);
		Label topSpeedValue = new Label(String.valueOf(carData.getTopSpeed()) + "km/h");
		topSpeedValue.setTextFill(settingsTextColor);
		topSpeedValue.setFont(infoFont);
		
		GridPane infoFields = new GridPane();
		infoFields.setMaxWidth(180);
		infoFields.add(door, 0, 0);
		infoFields.add(doorValue, 1, 0);
		
		infoFields.add(rearDoor, 0, 1);
		infoFields.add(rearDoorValue, 1, 1);
		
		infoFields.add(blindZone, 0, 2);
		infoFields.add(blindZoneValue, 1, 2);
		
		infoFields.add(topSpeed, 0, 3);
		infoFields.add(topSpeedValue, 1, 3);

		infoFields.setVisible(false);
		
//		>Settings>update	
		TextField doorLengthField = new TextField();
		doorLengthField.setPromptText("meter");
		doorLengthField.setMaxWidth(180);
		doorLengthField.setOnAction(e -> {
			validateInput(doorValue, doorLengthField, DOOR_LENGTH, 0.0, 10.0);
		});
		
		TextField rearDoorLengthField = new TextField();
		rearDoorLengthField.setPromptText("meter");
		rearDoorLengthField.setMaxWidth(180);
		rearDoorLengthField.setOnAction(e -> {
			validateInput(rearDoorValue, rearDoorLengthField, REAR_DOOR_LENGTH, 0.0, 10.0);
		});
		
		TextField blindZoneValueField = new TextField();
		blindZoneValueField.setMaxWidth(180);
		blindZoneValueField.setPromptText("meter");
		blindZoneValueField.setOnAction(e -> {
			validateInput(blindZoneValue, blindZoneValueField, BLIND_ZONE_VALUE, 0.0, 10.0);
		});
		TextField topSpeedField = new TextField();
		topSpeedField.setMaxWidth(180);
		topSpeedField.setPromptText("km/h");
		topSpeedField.setOnAction(e -> {
			validateInput(topSpeedValue, topSpeedField, TOP_SPEED, Double.valueOf(carSpeed.getValue()), 999.0);
		});
		
		VBox updateFields = new VBox();
		updateFields.getChildren().add(doorLengthField);
		updateFields.getChildren().add(rearDoorLengthField);
		updateFields.getChildren().add(blindZoneValueField);
		updateFields.getChildren().add(topSpeedField);
		updateFields.setVisible(false);
		
		Button infoButton = new Button("Info");
		infoButton.setPrefWidth(180); //125
		infoButton.setPrefHeight(40); //40
		infoButton.setOnAction(e -> {
			boolean isVisible = ! infoFields.isVisible();
			infoFields.setVisible(isVisible);
		});
		
		Button updateButton = new Button("Update");
		updateButton.setPrefWidth(180); //125
		updateButton.setPrefHeight(40); //40
		updateButton.setOnAction(e -> {
			boolean isVisible = ! updateFields.isVisible();
			updateFields.setVisible(isVisible);
			infoFields.setVisible(isVisible);
		});
		
		Button saveButton = new Button("Save settings");
		saveButton.setPrefWidth(180); //125
		saveButton.setPrefHeight(40); //40
		saveButton.setOnAction(e -> {
			backUpData.writeCarDataToFile(carData.getDoorLength(), carData.getRearDoorLength(), carData.getBlindZoneValue(), carData.getTopSpeed());
			boolean readyForAction = ! carData.isReady();
			mainRuleLabel.setVisible(readyForAction);
			saveLabel.setVisible(true);
		});
		
		GridPane settingsWindow = new GridPane();
		settingsWindow.setVisible(settingsWindowVisible.getValue());
		
		settingsWindow.add(infoButton, 0, 0);
		settingsWindow.add(infoFields, 0, 1);

		settingsWindow.add(updateButton, 1, 0);
		settingsWindow.add(updateFields, 1, 1);
		settingsWindow.add(updateRuleLabel, 1, 2);
		
		settingsWindow.add(saveButton, 2, 0);
		settingsWindow.add(saveLabel, 2, 1);
		return settingsWindow;
	}

	
	private void validateInput(Label info, TextField field, String carValue, double min, double max) {
		if (! isValidDouble(field.getText())) {
			validate(field, false, updateRuleLabel, "Invalid number");			
		}
		else if (! isInsideBounderies(Double.valueOf(field.getText()), min, max)) {
			validate(field, false, updateRuleLabel, min + " <= value <= " + max);
		}
		else {
			validate(field, true, updateRuleLabel, "Successfully updated");
			double valueR = Math.floor(Double.valueOf(field.getText())*1e2)/1e2;
			carData.updateValue(carValue, Double.valueOf(valueR));
			switch (carValue) {
			case DOOR_LENGTH: case REAR_DOOR_LENGTH: case BLIND_ZONE_VALUE: info.setText(valueR+ "m"); break;
			case TOP_SPEED: info.setText(valueR + "km/h"); break;
//			case UPDATE_PARKING_SENSORS: info.setText(field.getText() + "ms"); break;
			}
		}
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
    		String color = isValid ? "white" : "red";
    		textField.setStyle("-fx-background-color: " + color);
    	}
        if (ruleField != null) {
        	ruleField.setText(ruleMessage);
        	if (isValid) {
        		ruleField.setTextFill(Color.GREEN);
        	}
        	else {
        		ruleField.setTextFill(Color.RED);        		
        	}
        	ruleField.setVisible(true);
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