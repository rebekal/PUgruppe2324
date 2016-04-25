package release;

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
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Proxim8Program extends Application implements BaseInterface {

	private UltrasonicController ultraController;
	private BackUpData backUpData;
	private CarData carData;
	private WeatherData weatherData;
	
	public Proxim8Program() {
//		*** Initialize ultrasonic sensors ***
		PythonCaller pyCaller = new PythonCaller();
		Map<String, Ultrasonic> ultrasonicSensors = new HashMap<String, Ultrasonic>();
		ultrasonicSensors.put(FRONT, new Ultrasonic(FRONT, pyCaller));
		ultrasonicSensors.put(LEFT, new Ultrasonic(LEFT, pyCaller));
		ultrasonicSensors.put(RIGHT, new Ultrasonic(RIGHT, pyCaller));
		ultrasonicSensors.put(REAR, new Ultrasonic(REAR, pyCaller));
		ultraController = new UltrasonicController(ultrasonicSensors);
		
//		*** Getting loading saved data and initialize car simulation ***
		backUpData = new BackUpData();
		
		Map<String, Double> carDataValues = backUpData.readCarDataFromFile();
		if (carDataValues != null) {
			carData = new CarData(carDataValues.get(DOOR_LENGTH), carDataValues.get(REAR_DOOR_LENGTH),
								  carDataValues.get(BLIND_ZONE_VALUE), carDataValues.get(TOP_SPEED), carDataValues.get(FRONT_PARK_DISTANCE));			
			mainRuleLabel.setVisible(false);
		}
		else {
			carData = new CarData();
			validateAndUpdate(null, false, mainRuleLabel, "Update vehicle data");
			disableModeButtons(driveModeButton, blindZoneModeButton, parkingModeButton, true);
		}
		
		Map<String, Boolean> userDataValues = backUpData.readUserDataFromFile();
		if (userDataValues != null) {
			for (Map.Entry<String, Boolean> entry : userDataValues.entrySet()) {
				switch (entry.getKey()) {
				case SMART_BRAKE: smartBrakeActivated.setValue(entry.getValue()); break;
				case BLINDSPOT_ALWAYS: blindspotAlwaysActivated.setValue(entry.getValue()); break;
				case AUDIO_ENABLED: audioWarningActivated.setValue(entry.getValue()); break;
				default: System.out.println("No case for " + entry.getKey());
				}
			}
		}
//		*** Initialize weather simulation ***
		weatherData = new WeatherData();
	}


	final static int WIDTH = 1920; //1280;
	final static int HEIGHT = 1000; //768;
	final Font customFont = new Font("Aria", 40);
	private Label mainRuleLabel = new Label("Main rule");
	
	final static Image BACKGROUND = new Image(Proxim8Program.class.getResource("background_hd.png").toString());
	final static Image CAR_1 = new Image(Proxim8Program.class.getResource("car1.png").toString());
	final static Image CAR_2 = new Image(Proxim8Program.class.getResource("car2.png").toString());
	final static Image TURN_LIGHT = new Image(Proxim8Program.class.getResource("light1.png").toString());
	final static Image RED_TRAFFIC_LIGHT = new Image(Proxim8Program.class.getResource("light2.png").toString());
	
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
	
	private BooleanProperty settingsVisible = new SimpleBooleanProperty(this, "Settings window");
	private BooleanProperty smartBrakeActivated = new SimpleBooleanProperty(this, "Smart brake");
	private BooleanProperty blindspotAlwaysActivated = new SimpleBooleanProperty(this, "Blindspot always");
	private BooleanProperty audioWarningActivated = new SimpleBooleanProperty(this, "Audio warning");
	
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
		
		mainRuleLabel.setLayoutX(1470); //1020
		mainRuleLabel.setLayoutY(400); //250
		mainRuleLabel.setFont(customFont);
		
		final ImageView background = new ImageView(BACKGROUND);
//		background.setFitHeight(HEIGHT);
//		background.setFitWidth(WIDTH);
		
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
		final Button simulateButton = new Button("Start simulation");
		simulateButton.setPrefWidth(140); //85
		simulateButton.setPrefHeight(60); //45
		simulateButton.setLayoutX(1565); //1050
		simulateButton.setLayoutY(450); //300
		final Button settingsButton = new Button("Show settings");
		settingsButton.setPrefWidth(140); //85
		settingsButton.setPrefHeight(60);//45
		settingsButton.setLayoutX(1565); //1050
		settingsButton.setLayoutY(550); //400
		
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
		
//		*** Media warning *** TODO
//		Media warningAudioClip = new Media("file:///nedlastninger/warning.mp3");
//		MediaPlayer player = new MediaPlayer(warningAudioClip); 
//		player.stopTimeProperty().setValue(new Duration(300));
//		player.setVolume(0.1);
		
		simulateButton.setOnAction(e -> {
			if (simulateActive.getValue()) {
				simulateButton.setText("Start simulation");
				carData.resetSimulation();
				weatherData.resetCount();
			}
			else {
				simulateButton.setText("Stop simulation");
				
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
		                    				carData.setBrake(false);
		                    			}
		                    			else {
		                    				if (smartBrakeActivated.getValue()) {
		                    					carData.setBrake(true);
		                    				}
		                    				else {
		                    					carData.setBrake(false);
		                    				}
		                    				if (audioWarningActivated.getValue()) {
//		                    			        player.play();
//		                    			        player.stop();
		                    			        // TODO
		                    				}
		                    			}
		                    		}
		                    		
		                    		if (blindZoneMode.getValue()) {
		                    			boolean leftBlind = isObjectInBlindZone(ultraController.getSensorValue(LEFT, true), carData.getBlindZoneValue());
		                    			boolean rightBlind = isObjectInBlindZone(ultraController.getSensorValue(RIGHT, true), carData.getBlindZoneValue());
		                    			
		                    			if (leftBlind && (blindspotAlwaysActivated.getValue() || usingLeftTurnLight.getValue())) {
		                    				leftCar.setVisible(true);
		                    			}
		                    			else {
		                    				leftCar.setVisible(false);
		                    			}
		                    			if (rightBlind && (blindspotAlwaysActivated.getValue() || usingRightTurnLight.getValue())) {
		                    				rightCar.setVisible(true);
		                    			}
		                    			else {
		                    				rightCar.setVisible(false);
		                    			}
		                    			
		                    		}
		                    		
		                    		if (parkingMode.getValue()) {
		                    			frontDistance.setValue(ultraController.getSensorValue(FRONT, true));
		                    			leftDistance.setValue(ultraController.getSensorValue(LEFT, true));
		                    			rightDistance.setValue(ultraController.getSensorValue(RIGHT, true));
		                    			rearDistance.setValue(ultraController.getSensorValue(REAR, true));
		                    			
		                    			updateDistance(frontDistLabel, frontDistance.getValue(), carData.getFrontDistParking());
		                    			updateDistance(leftDistLabel, leftDistance.getValue(), carData.getDoorLength());
		                    			updateDistance(rightDistLabel, rightDistance.getValue(), carData.getDoorLength());
		                    			updateDistance(rearDistLabel, rearDistance.getValue(), carData.getRearDoorLength());
		                    			
		                    			if (isSensorValueLargerThan(leftDistance.getValue(), carData.getDoorLength())) {
//		                    				TODO alarm / LED OFF ?
		                    			}
		                    			else {
//		                    				TODO alarm / LED ON ?
		                    			}
		                    			if (isSensorValueLargerThan(rightDistance.getValue(), carData.getDoorLength())) {
//		                    				TODO alarm / LED OFF ?
		                    			}
		                    			else {
//		                    				TODO alarm / LED ON ?
		                    			}
		                    		}
		                        }
		                    });
		                }
		                interrupt();
		            }
		        }.start();
			}
			simulateActive.setValue(! simulateActive.getValue());
		});
		
		final TabPane settings = createSettingsWindow();
		settings.setLayoutX(1370); //1450
		settings.setLayoutY(620); //500
		settingsButton.setOnAction(e -> {
			if (settingsVisible.getValue()) {
				settingsButton.setText("Show settings");
			}
			else {
				settingsButton.setText("Hide settings");
			}
			settingsVisible.setValue(! settingsVisible.getValue());
			settings.setVisible(settingsVisible.getValue());
			
			feedbackSimulationLabel.setVisible(! settingsVisible.getValue());
			feedbackSettingsLabel.setVisible(! settingsVisible.getValue());
			checkAfterSettings();
		});
		
		final Group root = new Group(background, programTitle, mode1Enabled, mode2Enabled, settings, 
									myCar, leftCar, rightCar, leftTurnLight, rightTurnLight, carSpeedLabel,
									speedLimitLabel, brakeDistanceLabel, weatherLabel, frontDistLabel, leftDistLabel,
									rightDistLabel, rearDistLabel, driveModeButton, blindZoneModeButton,
									parkingModeButton, simulateButton, settingsButton,
									mainRuleLabel, redLight);
		Scene scene = new Scene(root); //, WIDTH, HEIGHT);		
		primaryStage.setTitle("Proxim8");
		primaryStage.setFullScreen(true);
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	
	/*
	 * Method checks if all required data is initialized so that Proxim8's features can be used
	 * - Door length, rear door length, blind zone value
	 */
	private void checkAfterSettings() {
		if (mainRuleLabel.isVisible()) {
			disableModeButtons(driveModeButton, blindZoneModeButton, parkingModeButton, true);
		}
		else if (drivingMode.getValue() || blindZoneMode.getValue() || parkingMode.getValue()) {
			// avoid changing mode after closing settings
		}
		else {
			disableModeButtons(driveModeButton, blindZoneModeButton, parkingModeButton, false);
		}
	}
	
//	Enables / disables Proxim8's features
	private void disableModeButtons(Button driveModeButton, Button blindZoneModeButton, Button parkingModeButton, boolean disable) {
		driveModeButton.setDisable(disable);
		blindZoneModeButton.setDisable(disable);
		parkingModeButton.setDisable(disable);
	}
	
	
//	*** Settings tab/window ***
	private Label feedbackSettingsLabel = new Label("Settings feedback label");
	private Label feedbackSimulationLabel = new Label("Simulation feedback label");
	
	/*
	 * Creates the setting tabs:
	 * - Information: Gives the user information about the different modes
	 * - Settings: Shows current values, and have update fields so that the user can edit the values
	 * - Simulation: Only for simulation of car data (demo)
	 * - Extra features: Gives the user the option to enable/disable extra features
	 */
	private TabPane createSettingsWindow() {
		int tabHeight = 800, tabWidth = 540;
		Color settingsTitleColor = Color.DODGERBLUE;
		Color settingsTextColor = Color.ORANGE;
		Font settingsFont = new Font("Aria", 18);
		Font settingsTitleFont = new Font("Aria", 21);
		Insets settingsInsets = new Insets(0, 0, 5, 0);
		Insets topSettingsInsets = new Insets(5, 0, 5, 0);
		Insets paddingAllAround = new Insets(5, 5, 5, 5);
		Insets separatorInsets = new Insets(10, 0, 10, 0);
		feedbackSettingsLabel.setFont(settingsFont);
		feedbackSimulationLabel.setFont(settingsFont);

//		*** Settings>informationTab ***
		Tab infoTab = new Tab("Information");
		infoTab.setClosable(false);
		
		VBox infoContent = new VBox();
		infoContent.setPrefSize(tabWidth, tabHeight);
		
		Label proxim8Version = new Label("Proxim8 v3.3");
		proxim8Version.setTextFill(settingsTitleColor);
		proxim8Version.setFont(customFont);
		Label driveModeLabel = new Label("Drive mode:");
		driveModeLabel.setTextFill(settingsTitleColor);
		driveModeLabel.setFont(settingsTitleFont);
		Text driveModeInfo = new Text("- measures the distance to the car infront of you" + System.lineSeparator()
									  + "- checks if your brakedistance < current distance");
		driveModeInfo.setFill(settingsTextColor);
		driveModeInfo.setFont(settingsFont);
		Label blindspotLabel = new Label("Blindspot mode:");
		blindspotLabel.setTextFill(settingsTitleColor);
		blindspotLabel.setFont(settingsTitleFont);
		Text blindspotModeInfo = new Text("- checks if there's a car in your blindzone");
		blindspotModeInfo.setFill(settingsTextColor);
		blindspotModeInfo.setFont(settingsFont);
		Label parkingModeLabel = new Label("Parking mode:");
		parkingModeLabel.setTextFill(settingsTitleColor);
		parkingModeLabel.setFont(settingsTitleFont);
		Text parkingModeInfo = new Text("- measures the distances around the car" + System.lineSeparator()
										+ "- gives a warning incase the distance < door length");
		parkingModeInfo.setFill(settingsTextColor);
		parkingModeInfo.setFont(settingsFont);
		
		infoContent.getChildren().addAll(proxim8Version, driveModeLabel, driveModeInfo, blindspotLabel, blindspotModeInfo, parkingModeLabel, parkingModeInfo);
		infoTab.setContent(infoContent);
		
//		*** Settings>settingsTab ***
		Tab settingsTab = new Tab("Settings");
		settingsTab.setClosable(false);
		
		VBox settingsContent = new VBox();
		settingsContent.setPrefSize(tabWidth, tabHeight);
		
		HBox getAndSetValues = new HBox();
		getAndSetValues.setPadding(paddingAllAround);
//		Settings>settingsTab>currentValues
		GridPane currentValues = new GridPane();
		Label currentValuesLabel = new Label("Current values:");
		currentValuesLabel.setTextFill(settingsTitleColor);
		currentValuesLabel.setFont(settingsTitleFont);
		
		Label door = new Label(DOOR_LENGTH + ": ");
		door.setTextFill(settingsTextColor);
		door.setFont(settingsFont);
		door.setPadding(topSettingsInsets);
		Label doorValue = new Label(String.valueOf(carData.getDoorLength()) + "m");
		doorValue.setTextFill(settingsTextColor);
		doorValue.setFont(settingsFont);
		Label rearDoor = new Label(REAR_DOOR_LENGTH + ": ");
		rearDoor.setTextFill(settingsTextColor);
		rearDoor.setFont(settingsFont);
		rearDoor.setPadding(settingsInsets);
		Label rearDoorValue = new Label(String.valueOf(carData.getRearDoorLength()) + "m");
		rearDoorValue.setTextFill(settingsTextColor);
		rearDoorValue.setFont(settingsFont);
		Label blindZone = new Label(BLIND_ZONE_VALUE + ": ");
		blindZone.setTextFill(settingsTextColor);
		blindZone.setFont(settingsFont);
		blindZone.setPadding(settingsInsets);
		Label blindZoneValue = new Label(String.valueOf(carData.getBlindZoneValue()) + "m");
		blindZoneValue.setTextFill(settingsTextColor);
		blindZoneValue.setFont(settingsFont);
		Label frontParkDist = new Label(FRONT_PARK_DISTANCE + ": ");
		frontParkDist.setTextFill(settingsTextColor);
		frontParkDist.setFont(settingsFont);
		frontParkDist.setPadding(settingsInsets);
		Label frontParkDistValue = new Label(String.valueOf(carData.getFrontDistParking()) + "m");
		frontParkDistValue.setTextFill(settingsTextColor);
		frontParkDistValue.setFont(settingsFont);
		
		currentValues.add(currentValuesLabel, 0, 0);
		currentValues.add(door, 0, 1);
		currentValues.add(doorValue, 1, 1);
		
		currentValues.add(rearDoor, 0, 2);
		currentValues.add(rearDoorValue, 1, 2);
		
		currentValues.add(blindZone, 0, 3);
		currentValues.add(blindZoneValue, 1, 3);
		
		currentValues.add(frontParkDist, 0, 4);
		currentValues.add(frontParkDistValue, 1, 4);
		
//		Settings>settingTab>updateFields
		VBox updateFields = new VBox();
		updateFields.setPadding(paddingAllAround);
		Label updateLabel = new Label("Set new value:");
		updateLabel.setTextFill(settingsTitleColor);
		updateLabel.setFont(settingsTitleFont);
		
		TextField doorLengthField = new TextField();
		doorLengthField.setPromptText("meter");
		doorLengthField.setMaxWidth(180);
		doorLengthField.setOnAction(e -> {
			validateInput(doorValue, feedbackSettingsLabel, doorLengthField, DOOR_LENGTH, 0, 10);
		});
		
		TextField rearDoorLengthField = new TextField();
		rearDoorLengthField.setPromptText("meter");
		rearDoorLengthField.setMaxWidth(180);
		rearDoorLengthField.setOnAction(e -> {
			validateInput(rearDoorValue, feedbackSettingsLabel, rearDoorLengthField, REAR_DOOR_LENGTH, 0, 10);
		});
		
		TextField blindZoneValueField = new TextField();
		blindZoneValueField.setMaxWidth(180);
		blindZoneValueField.setPromptText("meter");
		blindZoneValueField.setOnAction(e -> {
			validateInput(blindZoneValue, feedbackSettingsLabel, blindZoneValueField, BLIND_ZONE_VALUE, 0, 10);
		});
		
		TextField frontParkDistField = new TextField();
		frontParkDistField.setMaxWidth(180);
		frontParkDistField.setPromptText("meter");
		frontParkDistField.setOnAction(e -> {
			validateInput(frontParkDistValue, feedbackSettingsLabel, frontParkDistField, FRONT_PARK_DISTANCE, 0, 10);
		});
		
		updateFields.getChildren().addAll(updateLabel, doorLengthField, rearDoorLengthField, blindZoneValueField, frontParkDistField);
		
		Region regionSettings = new Region();
		regionSettings.setPrefWidth(50);
		getAndSetValues.getChildren().addAll(currentValues, regionSettings, updateFields);
		
		Separator settingsSeparator = new Separator();
		settingsSeparator.setPadding(separatorInsets);
		
		Text howToSave = new Text("Enter new values into the textfields and click [enter]" + System.lineSeparator() + "to update current values.");
		howToSave.setFill(settingsTextColor);
		howToSave.setFont(settingsFont);
		
		Separator settingsSeparator2 = new Separator();
		settingsSeparator2.setPadding(separatorInsets);
		
		
		settingsContent.getChildren().addAll(getAndSetValues, settingsSeparator, howToSave, settingsSeparator2, feedbackSettingsLabel);
		settingsTab.setContent(settingsContent);
		
//		*** Settings>simulate ***
		Tab simulateTab = new Tab("Simulation (demo)");
		simulateTab.setClosable(false);
		
		VBox simulateContent = new VBox();
		simulateContent.setPrefSize(tabWidth, tabHeight);
		
		
		HBox getAndSetSim = new HBox();
		getAndSetSim.setPadding(paddingAllAround);
//		Settings>simulate>currentValues
		GridPane currentValuesSim = new GridPane();
		Label currentValuesSimLabel = new Label("Current values:");
		currentValuesSimLabel.setTextFill(settingsTitleColor);
		currentValuesSimLabel.setFont(settingsTitleFont);
		
		Label topSpeed = new Label(TOP_SPEED + ": ");
		topSpeed.setTextFill(settingsTextColor);
		topSpeed.setFont(settingsFont);
		topSpeed.setPadding(topSettingsInsets);
		Label topSpeedValue = new Label(String.valueOf(carData.getTopSpeed()) + "km/h");
		topSpeedValue.setTextFill(settingsTextColor);
		topSpeedValue.setFont(settingsFont);
		
		currentValuesSim.add(currentValuesSimLabel, 0, 0);
		currentValuesSim.add(topSpeed, 0, 1);
		currentValuesSim.add(topSpeedValue, 1, 1);
		
//		Settings>simulate>updateFields
		VBox updateFieldsSim = new VBox();
		Label updateSimLabel = new Label("Set new value:");
		updateSimLabel.setTextFill(settingsTitleColor);
		updateSimLabel.setFont(settingsTitleFont);
		
		TextField topSpeedField = new TextField();
		topSpeedField.setMaxWidth(180);
		topSpeedField.setPromptText("km/h");
		topSpeedField.setOnAction(e -> {
			validateInput(topSpeedValue, feedbackSimulationLabel, topSpeedField, TOP_SPEED, Double.valueOf(carSpeed.getValue()), 999.0);
		});
		
		updateFieldsSim.getChildren().addAll(updateSimLabel, topSpeedField);
		
		Region simulateRegion = new Region();
		simulateRegion.setPrefWidth(50);
		getAndSetSim.getChildren().addAll(currentValuesSim, simulateRegion, updateFieldsSim);
		
		Separator simulationSeparator = new Separator();
		simulationSeparator.setPadding(separatorInsets);
		
		Text simulateInfo = new Text("Settings for simulate data." + System.lineSeparator() + "Enter new values into the textfields and click [enter]");
		simulateInfo.setFill(settingsTextColor);
		simulateInfo.setFont(settingsFont);
		
		Separator simulationSeparator2 = new Separator();
		simulationSeparator2.setPadding(separatorInsets);
		
		
		simulateContent.getChildren().addAll(getAndSetSim, simulationSeparator, simulateInfo, simulationSeparator2, feedbackSimulationLabel);
		simulateTab.setContent(simulateContent);
		
//		*** Settings>checkBoxTab ***
		Tab extraFeaturesTab = new Tab("Extra features");
		extraFeaturesTab.setClosable(false);
		
		VBox extraFeaturesContent = new VBox();
		extraFeaturesContent.setPrefSize(tabWidth, tabHeight);
		extraFeaturesContent.setPadding(paddingAllAround);
		
		Label extraFeaturesLabel = new Label("Extra features");
		extraFeaturesLabel.setTextFill(settingsTitleColor);
		extraFeaturesLabel.setFont(settingsTitleFont);
		extraFeaturesLabel.setPadding(topSettingsInsets);
		
		Separator separatorExtraFeatures = new Separator();
		separatorExtraFeatures.setPadding(separatorInsets);
		
		Insets checkInsets = new Insets(5, 0, 5, 5);
		CheckBox smartBrake = new CheckBox("Smart brake");
		smartBrake.setSelected(smartBrakeActivated.getValue());
		smartBrake.setFont(settingsFont);
		smartBrake.setTextFill(settingsTextColor);
		smartBrake.setPadding(checkInsets);
		smartBrake.setOnAction(e -> {
			smartBrakeActivated.setValue(! smartBrakeActivated.getValue());
			validateAndUpdate(null, true, feedbackSettingsLabel, "Successfully updated");
		});
		CheckBox blindspotAlways = new CheckBox("Blindspot always");
		blindspotAlways.setSelected(blindspotAlwaysActivated.getValue());
		blindspotAlways.setFont(settingsFont);
		blindspotAlways.setTextFill(settingsTextColor);
		blindspotAlways.setPadding(checkInsets);
		blindspotAlways.setOnAction(e -> {
			blindspotAlwaysActivated.setValue(! blindspotAlwaysActivated.getValue());
			validateAndUpdate(null, true, feedbackSettingsLabel, "Successfully updated");
		});
		CheckBox audioWarning = new CheckBox("Audio warning");
		audioWarning.setSelected(audioWarningActivated.getValue());
		audioWarning.setFont(settingsFont);
		audioWarning.setTextFill(settingsTextColor);
		audioWarning.setPadding(checkInsets);
		audioWarning.setOnAction(e -> {
			audioWarningActivated.setValue(! audioWarningActivated.getValue());
			validateAndUpdate(null, true, feedbackSettingsLabel, "Successfully updated");
		});
		
		extraFeaturesContent.getChildren().addAll(extraFeaturesLabel, separatorExtraFeatures, smartBrake, blindspotAlways, audioWarning);
		extraFeaturesTab.setContent(extraFeaturesContent);
		
		
		TabPane settingsWindow = new TabPane();
		settingsWindow.setVisible(false);
		settingsWindow.getTabs().addAll(infoTab, settingsTab, simulateTab, extraFeaturesTab);
		return settingsWindow;
	}
	
	/*
	 * Validates all user input in the settings menu
	 * If the input is valid:
	 * - show positive feedback
	 * - update values in the carData class
	 * - update visible values in the settings menu
	 * 
	 * If the input is invalid:
	 * - show rules as feedback
	 * - keep old values
	 */
	private void validateInput(Label info, Label updateLabel, TextField field, String carValue, double min, double max) {
		if (! isValidDouble(field.getText())) {
			validateAndUpdate(field, false, updateLabel, "Invalid number");
		}
		else if (! isInsideBounderies(Double.valueOf(field.getText()), min, max)) {
			validateAndUpdate(field, false, updateLabel, min + " <= value <= " + max);
		}
		else {
			double valueR = formatDoubleWithTwoDeci(Double.valueOf(field.getText()));
			carData.updateValue(carValue, valueR);
			
			validateAndUpdate(field, true, updateLabel, "Successfully updated");
			switch (carValue) {
			case DOOR_LENGTH: case REAR_DOOR_LENGTH: case BLIND_ZONE_VALUE: case FRONT_PARK_DISTANCE: info.setText(valueR + "m"); break;
			case TOP_SPEED: info.setText(valueR + "km/h"); break;
			}
		}
	}

//	*** GUI methods ***
	
//	Update label text and color according to the relation of current distance and valid distance
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
	
	/*
	 * Changes textfield color according to if the user input was valid or not, also makes the rulelabel visible
	 * Auto-saves new valid input
	 */
    private void validateAndUpdate(TextField textField, boolean isValid, Label ruleField, String ruleMessage) {
    	if (textField != null) {
    		if (textField.getText().equals("")) {
    			return;
    		}
    		String color = isValid ? "white" : "red";
    		textField.setStyle("-fx-background-color: " + color);
    		if (isValid) {
    			backUpData.writeCarDataToFile(carData.getDoorLength(), carData.getRearDoorLength(), carData.getBlindZoneValue(),
    										  carData.getTopSpeed(), carData.getFrontDistParking());    			
    		}
    	}
    	else {
    		backUpData.writeUserDataToFile(smartBrakeActivated.getValue(), blindspotAlwaysActivated.getValue(), audioWarningActivated.getValue());
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
    
//  Checks if input is inside the given bounderies
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
    
//  Checks if the given value actually is a number
    private boolean isValidDouble(String text) {
    	try {
    		Double value = Double.valueOf(text);
    	} catch (NumberFormatException e) {
    		return false;
    	}
    	return true;
    }
    
//  Reduces double value to a number with two decimal places
    private double formatDoubleWithTwoDeci(double value) {
    	return Math.floor(value*1e2)/1e2;
    }


//	*** Main methods ***
    
	/*
	 *  Formula brake distance : velocity^2 / (2 * friction * gravity) + (velocity * reaction_time)
	 *  - where velocity is measured in meter/second
	 *  - gravity = 9.81m/s^2
	 *  - reaction_time = 1 second
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
