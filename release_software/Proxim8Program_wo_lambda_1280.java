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
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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

public class Proxim8Program_wo_lambda_1280 extends Application implements BaseInterface {

	private UltrasonicController ultraController;
	private BackUpData backUpData;
	private CarData carData;
	private WeatherData weatherData;
	
	public Proxim8Program_wo_lambda_1280() {
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
				case SMART_BRAKE: smartBrakeActivated = entry.getValue(); break;
				case BLINDSPOT_ALWAYS: blindspotAlwaysActivated = entry.getValue(); break;
				case AUDIO_ENABLED: audioWarningActivated = entry.getValue(); break;
				default: System.out.println("No case for " + entry.getKey());
				}
			}
		}
//		*** Initialize weather simulation ***
		weatherData = new WeatherData();
	}


	final static int WIDTH = 1280;
	final static int HEIGHT = 768;
	final Font customFont = new Font("Aria", 40);
	final Font simulateInfoFont = new Font("Aria", 26);
	private Label mainRuleLabel = new Label("Main rule");
	
	final static Image BACKGROUND = new Image(Proxim8Program_wo_lambda_1280.class.getResource("background.png").toString());
	final static Image CAR_1 = new Image(Proxim8Program_wo_lambda_1280.class.getResource("car1.png").toString());
	final static Image CAR_2 = new Image(Proxim8Program_wo_lambda_1280.class.getResource("car2.png").toString());
	final static Image TURN_LIGHT = new Image(Proxim8Program_wo_lambda_1280.class.getResource("light1.png").toString());
	final static Image RED_TRAFFIC_LIGHT = new Image(Proxim8Program_wo_lambda_1280.class.getResource("light2.png").toString());
	
	private int carSpeed, speedLimit, brakeDistance;
	private Double frontDistance, leftDistance, rightDistance, rearDistance;
	private boolean drivingMode, blindZoneMode, parkingMode;
	private boolean simulateActive, leftCarVisible, rightCarVisible, usingLeftTurnLight, usingRightTurnLight, redTrafficLightVisible;
	private boolean settingsVisible, smartBrakeActivated, blindspotAlwaysActivated, audioWarningActivated;
	
	@FXML
	private final Button driveModeButton = new Button("Drive");
	@FXML
	private final Button blindZoneModeButton = new Button("Blindspot");
	@FXML
	private final Button parkingModeButton = new Button("Parking");
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		final Label programTitle = new Label("Proxim8");
		programTitle.setFont(new Font("Aria", 60));
		programTitle.setTextFill(Color.WHITE);
		programTitle.setLayoutX(985);
		programTitle.setLayoutY(20);
		
		mainRuleLabel.setLayoutX(915);
		mainRuleLabel.setLayoutY(220);
		mainRuleLabel.setFont(customFont);
		
		final ImageView background = new ImageView(BACKGROUND);
		background.setFitHeight(HEIGHT);
		background.setFitWidth(WIDTH);
		
		int modeButtonWidth = 100, modeButtonHeight = 60;
		final Rectangle mode1Enabled = new Rectangle();
		mode1Enabled.setFill(Color.GREEN);
		mode1Enabled.setWidth(modeButtonWidth + 20);
		mode1Enabled.setHeight(modeButtonHeight + 20);
		mode1Enabled.setVisible(false);
		
		final Rectangle mode2Enabled = new Rectangle();
		mode2Enabled.setFill(Color.GREEN);
		mode2Enabled.setWidth(modeButtonWidth + 20);
		mode2Enabled.setHeight(modeButtonHeight + 20);
		mode2Enabled.setVisible(false);
		
		Font modeButtonFont = new Font("Aria", 18);
		driveModeButton.setPrefWidth(modeButtonWidth);
		driveModeButton.setPrefHeight(modeButtonHeight);
		driveModeButton.setLayoutX(920);
		driveModeButton.setLayoutY(120);
		driveModeButton.setFont(modeButtonFont);
		
		blindZoneModeButton.setPrefWidth(modeButtonWidth);
		blindZoneModeButton.setPrefHeight(modeButtonHeight);
		blindZoneModeButton.setLayoutX(1040);
		blindZoneModeButton.setLayoutY(120);
		blindZoneModeButton.setFont(modeButtonFont);

		parkingModeButton.setPrefWidth(modeButtonWidth);
		parkingModeButton.setPrefHeight(modeButtonHeight);
		parkingModeButton.setLayoutX(1160);
		parkingModeButton.setLayoutY(120);
		parkingModeButton.setFont(modeButtonFont);
		final Button simulateButton = new Button("Start simulation");
		simulateButton.setPrefWidth(130);
		simulateButton.setPrefHeight(45);
		simulateButton.setLayoutX(1025);
		simulateButton.setLayoutY(300);
		final Button settingsButton = new Button("Show settings");
		settingsButton.setPrefWidth(130);
		settingsButton.setPrefHeight(45);
		settingsButton.setLayoutX(1025);
		settingsButton.setLayoutY(400);
		
		final ImageView myCar = new ImageView(CAR_1);
		myCar.setFitHeight(473);
		myCar.setFitWidth(216);
		myCar.setLayoutX(324);
		myCar.setLayoutY(168);
		
		final ImageView leftCar = new ImageView(CAR_2);
		leftCar.setVisible(leftCarVisible);
		leftCar.setFitHeight(473);
		leftCar.setFitWidth(216);
		leftCar.setLayoutX(36);
		leftCar.setLayoutY(315);
		
		final ImageView rightCar = new ImageView(CAR_2);
		rightCar.setVisible(rightCarVisible);
		rightCar.setFitHeight(473);
		rightCar.setFitWidth(216);
		rightCar.setLayoutX(615);
		rightCar.setLayoutY(315);
		
		final ImageView leftTurnLight = new ImageView(TURN_LIGHT);
		leftTurnLight.setVisible(usingLeftTurnLight);
		leftTurnLight.setFitHeight(42);
		leftTurnLight.setFitWidth(38);
		leftTurnLight.setLayoutX(320);
		leftTurnLight.setLayoutY(237);
		
		final ImageView rightTurnLight = new ImageView(TURN_LIGHT);
		rightTurnLight.setVisible(usingRightTurnLight);
		rightTurnLight.setFitHeight(42);
		rightTurnLight.setFitWidth(38);
		rightTurnLight.setLayoutX(509);
		rightTurnLight.setLayoutY(237);
		
		final ImageView redTrafficLight = new ImageView(RED_TRAFFIC_LIGHT);
		redTrafficLight.setFitHeight(85);
		redTrafficLight.setFitWidth(109);
		
		Text redTrafficLightCountLabel = new Text();
		VBox redLight = new VBox(redTrafficLight, redTrafficLightCountLabel);
		redLight.setVisible(redTrafficLightVisible);
		redLight.setLayoutX(690);
		redLight.setLayoutY(99);
		
		
		Text frontDistLabel = new Text();
		frontDistLabel.setVisible(false);
		frontDistLabel.setFont(customFont);
		frontDistLabel.setLayoutX(375);
		frontDistLabel.setLayoutY(130);
		Text leftDistLabel = new Text();
		leftDistLabel.setVisible(false);
		leftDistLabel.setFont(customFont);
		leftDistLabel.setLayoutX(175);
		leftDistLabel.setLayoutY(400);
		Text rightDistLabel = new Text();
		rightDistLabel.setVisible(false);
		rightDistLabel.setFont(customFont);
		rightDistLabel.setLayoutX(575);
		rightDistLabel.setLayoutY(400);
		Text rearDistLabel = new Text();
		rearDistLabel.setVisible(false);
		rearDistLabel.setFont(customFont);
		rearDistLabel.setLayoutX(375);
		rearDistLabel.setLayoutY(700);
		
		Text carSpeedLabel = new Text("Car speed:");
		carSpeedLabel.setFont(simulateInfoFont);
		carSpeedLabel.setLayoutY(24);
		Text speedLimitLabel = new Text("Speed limit:");
		speedLimitLabel.setFont(simulateInfoFont);
		speedLimitLabel.setLayoutX(300);
		speedLimitLabel.setLayoutY(24);
		Text brakeDistanceLabel = new Text("Brake distance:");
		brakeDistanceLabel.setFont(simulateInfoFont);
		brakeDistanceLabel.setLayoutX(600);
		brakeDistanceLabel.setLayoutY(24);
		Text weatherLabel = new Text("Weather : ");
		weatherLabel.setFont(simulateInfoFont);
		weatherLabel.setLayoutY(60);
		
		driveModeButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				boolean activate = ! drivingMode;
				drivingMode = activate;
				frontDistLabel.setVisible(activate);
				mode1Enabled.setLayoutX(910);
				mode1Enabled.setLayoutY(110);
				mode1Enabled.setVisible(activate);
				if (activate) {
					parkingModeButton.setDisable(activate);
				}
				else if (!blindZoneMode) {
					parkingModeButton.setDisable(activate);
				}
			}
		});
		
		blindZoneModeButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				boolean activate = ! blindZoneMode;
				blindZoneMode = activate;
				mode2Enabled.setLayoutX(1030);
				mode2Enabled.setLayoutY(110);
				mode2Enabled.setVisible(activate);
				if (activate) {
					parkingModeButton.setDisable(activate);
				}
				else if (!drivingMode) {
					parkingModeButton.setDisable(activate);
				}
			}
		});
		
		parkingModeButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				boolean activate = ! parkingMode;
				parkingMode = activate;
				frontDistLabel.setVisible(activate);
				leftDistLabel.setVisible(activate);
				rightDistLabel.setVisible(activate);
				rearDistLabel.setVisible(activate);
				mode2Enabled.setLayoutX(1150);
				mode2Enabled.setLayoutY(110);
				mode2Enabled.setVisible(activate);
				driveModeButton.setDisable(activate);
				blindZoneModeButton.setDisable(activate);
			}
		});
		
//		*** Media warning *** TODO
//		Media warningAudioClip = new Media("file:///nedlastninger/warning.mp3");
//		MediaPlayer player = new MediaPlayer(warningAudioClip); 
//		player.stopTimeProperty().setValue(new Duration(300));
//		player.setVolume(0.1);
		
		simulateButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (simulateActive) {
					simulateButton.setText("Start simulation");
					carData.resetSimulation();
					weatherData.resetCount();
				}
				else {
					simulateButton.setText("Stop simulation");
					
					new Thread() {
						// runnable for that thread
						public void run() {
							while (simulateActive) {
								// increase sleeptimer incase GUI becomes slow
								try {
									Thread.sleep(100);
								} catch (InterruptedException e) {
								}
								// update ProgressIndicator on FX thread
								Platform.runLater(new Runnable() {
									public void run() {
										
										if (drivingMode || blindZoneMode) {
											carData.simulateOneStep();
											weatherData.update();
											
//		                    				Simulted car data
											carSpeed = carData.getCarSpeed();
											speedLimit = carData.getSpeedLimit();
											brakeDistance = getBrakeDistance(carSpeed, weatherData.getFrictionValue());
											
											carSpeedLabel.textProperty().setValue("Car speed: " + carSpeed + "km/h");
											speedLimitLabel.textProperty().setValue("Speed limit: " + speedLimit + "km/h");
											brakeDistanceLabel.textProperty().setValue("Brake distance: " + String.valueOf(brakeDistance) + "m");
											weatherLabel.textProperty().setValue("Weather: " + weatherData.getFrictionString());
											
//		                					Left turn light / right turn light
											usingLeftTurnLight = carData.isLeftTurnLightOn();
											usingRightTurnLight = carData.isRightTurnLightOn();
											leftTurnLight.setVisible(usingLeftTurnLight);
											rightTurnLight.setVisible(usingRightTurnLight);
											
//		                					Traffic light (red only)
											redLight.setVisible(carData.getRedTrafficLight());
											redTrafficLightCountLabel.textProperty().setValue(String.valueOf(carData.getRedTrafficLightCount()));
										}
										
										if (drivingMode) {
											frontDistance = ultraController.getSensorValue(FRONT, true);
											updateDistance(frontDistLabel, frontDistance, brakeDistance);
											
											if (isSensorValueLargerThan(frontDistance, brakeDistance)) {
												carData.setBrake(false);
											}
											else {
												if (smartBrakeActivated) {
													carData.setBrake(true);
												}
												else {
													carData.setBrake(false);
												}
												if (audioWarningActivated) {
//		                    			        player.play();
//		                    			        player.stop();
													// TODO
												}
											}
										}
										
										if (blindZoneMode) {
											boolean leftBlind = isObjectInBlindZone(ultraController.getSensorValue(LEFT, true), carData.getBlindZoneValue());
											boolean rightBlind = isObjectInBlindZone(ultraController.getSensorValue(RIGHT, true), carData.getBlindZoneValue());
											
											if (leftBlind && (blindspotAlwaysActivated || usingLeftTurnLight)) {
												leftCar.setVisible(true);
											}
											else {
												leftCar.setVisible(false);
											}
											if (rightBlind && (blindspotAlwaysActivated || usingRightTurnLight)) {
												rightCar.setVisible(true);
											}
											else {
												rightCar.setVisible(false);
											}
											
										}
										
										if (parkingMode) {
											frontDistance = ultraController.getSensorValue(FRONT, true);
											leftDistance = ultraController.getSensorValue(LEFT, true);
											rightDistance = ultraController.getSensorValue(RIGHT, true);
											rearDistance = ultraController.getSensorValue(REAR, true);
											
											updateDistance(frontDistLabel, frontDistance, carData.getFrontDistParking());
											updateDistance(leftDistLabel, leftDistance, carData.getDoorLength());
											updateDistance(rightDistLabel, rightDistance, carData.getDoorLength());
											updateDistance(rearDistLabel, rearDistance, carData.getRearDoorLength());
											
											if (isSensorValueLargerThan(leftDistance, carData.getDoorLength())) {
//		                    				TODO alarm / LED OFF ?
											}
											else {
//		                    				TODO alarm / LED ON ?
											}
											if (isSensorValueLargerThan(rightDistance, carData.getDoorLength())) {
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
				simulateActive = ! simulateActive;	
			}
		});
		
		final TabPane settings = createSettingsWindow();
		settings.setLayoutX(900);
		settings.setLayoutY(450);
		settingsButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (settingsVisible) {
					settingsButton.setText("Show settings");
				}
				else {
					settingsButton.setText("Hide settings");
				}
				settingsVisible = ! settingsVisible;
				settings.setVisible(settingsVisible);
				
				feedbackSimulationLabel.setVisible(! settingsVisible);
				feedbackSettingsLabel.setVisible(! settingsVisible);
				checkAfterSettings();
			}
		});
		
		final Group root = new Group(background, programTitle, mode1Enabled, mode2Enabled, settings, 
									myCar, leftCar, rightCar, leftTurnLight, rightTurnLight, carSpeedLabel,
									speedLimitLabel, brakeDistanceLabel, weatherLabel, frontDistLabel, leftDistLabel,
									rightDistLabel, rearDistLabel, driveModeButton, blindZoneModeButton,
									parkingModeButton, simulateButton, settingsButton,
									mainRuleLabel, redLight);
		Scene scene = new Scene(root, WIDTH, HEIGHT);		
		primaryStage.setTitle("Proxim8");
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
		else if (drivingMode || blindZoneMode || parkingMode) {
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
		int tabHeight = 380, tabWidth = 318, textfieldWidth = 120;
		Color settingsTitleColor = Color.DODGERBLUE;
		Color settingsTextColor = Color.ORANGE;
		Font settingsTitleFont = new Font("Aria", 20);
		Font settingsFont = new Font("Aria", 18);
		Font infoFont = new Font("Aria", 16);
		Insets settingsInsets = new Insets(0, 0, 5, 0);
		Insets topSettingsInsets = new Insets(5, 0, 5, 0);
		Insets paddingAllAround = new Insets(5, 5, 5, 5);
		Insets separatorInsets = new Insets(5, 0, 5, 0);
		feedbackSettingsLabel.setFont(settingsFont);
		feedbackSimulationLabel.setFont(settingsFont);

//		*** Settings>informationTab ***
		Tab infoTab = new Tab("Information");
		infoTab.setClosable(false);
		
		VBox infoContent = new VBox();
		infoContent.setPrefSize(tabWidth, tabHeight);
		
		final Label proxim8Version = new Label("Proxim8 v3.3");
		proxim8Version.setTextFill(settingsTitleColor);
		proxim8Version.setFont(new Font("Aria", 24));
		final Label driveModeLabel = new Label("Drive mode:");
		driveModeLabel.setTextFill(settingsTitleColor);
		driveModeLabel.setFont(settingsTitleFont);
		final Text driveModeInfo = new Text("- measures the distance to the car infront of you" + System.lineSeparator()
									  	  + "- checks if your brakedistance < current distance");
		driveModeInfo.setFill(settingsTextColor);
		driveModeInfo.setFont(infoFont);
		final Label blindspotLabel = new Label("Blindspot mode:");
		blindspotLabel.setTextFill(settingsTitleColor);
		blindspotLabel.setFont(settingsTitleFont);
		final Text blindspotModeInfo = new Text("- checks if there's a car in your blindzone");
		blindspotModeInfo.setFill(settingsTextColor);
		blindspotModeInfo.setFont(infoFont);
		final Label parkingModeLabel = new Label("Parking mode:");
		parkingModeLabel.setTextFill(settingsTitleColor);
		parkingModeLabel.setFont(settingsTitleFont);
		final Text parkingModeInfo = new Text("- measures the distances around the car" + System.lineSeparator()
											+ "- gives a warning incase the distance < door length");
		parkingModeInfo.setFill(settingsTextColor);
		parkingModeInfo.setFont(infoFont);
		
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
		final Label currentValuesLabel = new Label("Current values:");
		currentValuesLabel.setTextFill(settingsTitleColor);
		currentValuesLabel.setFont(settingsTitleFont);
		
		final Label door = new Label(DOOR_LENGTH + ": ");
		door.setTextFill(settingsTextColor);
		door.setFont(settingsFont);
		door.setPadding(topSettingsInsets);
		final Label doorValue = new Label(String.valueOf(carData.getDoorLength()) + "m");
		doorValue.setTextFill(settingsTextColor);
		doorValue.setFont(settingsFont);
		final Label rearDoor = new Label(REAR_DOOR_LENGTH + ": ");
		rearDoor.setTextFill(settingsTextColor);
		rearDoor.setFont(settingsFont);
		rearDoor.setPadding(settingsInsets);
		final Label rearDoorValue = new Label(String.valueOf(carData.getRearDoorLength()) + "m");
		rearDoorValue.setTextFill(settingsTextColor);
		rearDoorValue.setFont(settingsFont);
		final Label blindZone = new Label(BLIND_ZONE_VALUE + ": ");
		blindZone.setTextFill(settingsTextColor);
		blindZone.setFont(settingsFont);
		blindZone.setPadding(settingsInsets);
		final Label blindZoneValue = new Label(String.valueOf(carData.getBlindZoneValue()) + "m");
		blindZoneValue.setTextFill(settingsTextColor);
		blindZoneValue.setFont(settingsFont);
		final Label frontParkDist = new Label(FRONT_PARK_DISTANCE + ": ");
		frontParkDist.setTextFill(settingsTextColor);
		frontParkDist.setFont(settingsFont);
		frontParkDist.setPadding(settingsInsets);
		final Label frontParkDistValue = new Label(String.valueOf(carData.getFrontDistParking()) + "m");
		frontParkDistValue.setTextFill(settingsTextColor);
		frontParkDistValue.setFont(settingsFont);
		frontParkDistValue.setPadding(settingsInsets);
		
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
		final Label updateLabel = new Label("Set new value:");
		updateLabel.setTextFill(settingsTitleColor);
		updateLabel.setFont(settingsTitleFont);
		
		final TextField doorLengthField = new TextField();
		doorLengthField.setPromptText("meter");
		doorLengthField.setMaxWidth(textfieldWidth);
		doorLengthField.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				validateInput(doorValue, feedbackSettingsLabel, doorLengthField, DOOR_LENGTH, 0, 10);
			}
		});
		
		final TextField rearDoorLengthField = new TextField();
		rearDoorLengthField.setPromptText("meter");
		rearDoorLengthField.setMaxWidth(textfieldWidth);
		rearDoorLengthField.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				validateInput(rearDoorValue, feedbackSettingsLabel, rearDoorLengthField, REAR_DOOR_LENGTH, 0, 10);
			}
		});
		
		final TextField blindZoneValueField = new TextField();
		blindZoneValueField.setMaxWidth(textfieldWidth);
		blindZoneValueField.setPromptText("meter");
		blindZoneValueField.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				validateInput(blindZoneValue, feedbackSettingsLabel, blindZoneValueField, BLIND_ZONE_VALUE, 0, 10);
			}
		});
		
		final TextField frontParkDistField = new TextField();
		frontParkDistField.setMaxWidth(textfieldWidth);
		frontParkDistField.setPromptText("meter");
		frontParkDistField.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				validateInput(frontParkDistValue, feedbackSettingsLabel, frontParkDistField, FRONT_PARK_DISTANCE, 0, 10);
			}
		});
		
		updateFields.getChildren().addAll(updateLabel, doorLengthField, rearDoorLengthField, blindZoneValueField, frontParkDistField);
		
		Region regionSettings = new Region();
		regionSettings.setPrefWidth(25);
		getAndSetValues.getChildren().addAll(currentValues, regionSettings, updateFields);
		
		Separator settingsSeparator = new Separator();
		settingsSeparator.setPadding(separatorInsets);
		
		final Text howToSave = new Text("Enter new values into the textfields" + System.lineSeparator() + "and click [enter] to update current values.");
		howToSave.setFill(settingsTextColor);
		howToSave.setFont(settingsFont);
		
		Separator settingsSeparator2 = new Separator();
		settingsSeparator2.setPadding(separatorInsets);
		
		
		settingsContent.getChildren().addAll(getAndSetValues, settingsSeparator, howToSave, settingsSeparator2, feedbackSettingsLabel);
		settingsTab.setContent(settingsContent);
		
//		*** Settings>simulate ***
		Tab simulateTab = new Tab("Simulation");
		simulateTab.setClosable(false);
		
		VBox simulateContent = new VBox();
		simulateContent.setPrefSize(tabWidth, tabHeight);
		
		
		HBox getAndSetSim = new HBox();
		getAndSetSim.setPadding(paddingAllAround);
//		Settings>simulate>currentValues
		GridPane currentValuesSim = new GridPane();
		final Label currentValuesSimLabel = new Label("Current values:");
		currentValuesSimLabel.setTextFill(settingsTitleColor);
		currentValuesSimLabel.setFont(settingsTitleFont);
		
		final Label topSpeed = new Label(TOP_SPEED + ": ");
		topSpeed.setTextFill(settingsTextColor);
		topSpeed.setFont(settingsFont);
		topSpeed.setPadding(topSettingsInsets);
		final Label topSpeedValue = new Label(String.valueOf(carData.getTopSpeed()) + "km/h");
		topSpeedValue.setTextFill(settingsTextColor);
		topSpeedValue.setFont(settingsFont);
		
		currentValuesSim.add(currentValuesSimLabel, 0, 0);
		currentValuesSim.add(topSpeed, 0, 1);
		currentValuesSim.add(topSpeedValue, 1, 1);
		
//		Settings>simulate>updateFields
		VBox updateFieldsSim = new VBox();
		final Label updateSimLabel = new Label("Set new value:");
		updateSimLabel.setTextFill(settingsTitleColor);
		updateSimLabel.setFont(settingsTitleFont);
		
		final TextField topSpeedField = new TextField();
		topSpeedField.setMaxWidth(textfieldWidth);
		topSpeedField.setPromptText("km/h");
		topSpeedField.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				validateInput(topSpeedValue, feedbackSimulationLabel, topSpeedField, TOP_SPEED, Double.valueOf(carSpeed), 999.0);				
			}
		});
		
		updateFieldsSim.getChildren().addAll(updateSimLabel, topSpeedField);
		
		Region simulateRegion = new Region();
		simulateRegion.setPrefWidth(25);
		getAndSetSim.getChildren().addAll(currentValuesSim, simulateRegion, updateFieldsSim);
		
		Separator simulationSeparator = new Separator();
		simulationSeparator.setPadding(separatorInsets);
		
		final Text simulateInfo = new Text("Settings for simulate data." + System.lineSeparator()
										 + "Enter new values into the textfields and click [enter]");
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
		
		final Label extraFeaturesLabel = new Label("Extra features");
		extraFeaturesLabel.setTextFill(settingsTitleColor);
		extraFeaturesLabel.setFont(settingsTitleFont);
		extraFeaturesLabel.setPadding(topSettingsInsets);
		
		Separator separatorExtraFeatures = new Separator();
		separatorExtraFeatures.setPadding(separatorInsets);
		
		Insets checkInsets = new Insets(5, 0, 5, 5);
		final CheckBox smartBrake = new CheckBox("Smart brake");
		smartBrake.setSelected(smartBrakeActivated);
		smartBrake.setFont(settingsFont);
		smartBrake.setTextFill(settingsTextColor);
		smartBrake.setPadding(checkInsets);
		smartBrake.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				smartBrakeActivated = ! smartBrakeActivated;
				validateAndUpdate(null, true, feedbackSettingsLabel, "Successfully updated");
			}
		});
		final CheckBox blindspotAlways = new CheckBox("Blindspot always");
		blindspotAlways.setSelected(blindspotAlwaysActivated);
		blindspotAlways.setFont(settingsFont);
		blindspotAlways.setTextFill(settingsTextColor);
		blindspotAlways.setPadding(checkInsets);
		blindspotAlways.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				blindspotAlwaysActivated = ! blindspotAlwaysActivated;
				validateAndUpdate(null, true, feedbackSettingsLabel, "Successfully updated");
			}
		});
		final CheckBox audioWarning = new CheckBox("Audio warning");
		audioWarning.setSelected(audioWarningActivated);
		audioWarning.setFont(settingsFont);
		audioWarning.setTextFill(settingsTextColor);
		audioWarning.setPadding(checkInsets);
		audioWarning.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				audioWarningActivated = ! audioWarningActivated;
				validateAndUpdate(null, true, feedbackSettingsLabel, "Successfully updated");				
			}
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
    			boolean notReady = ! carData.isReady();
    			mainRuleLabel.setVisible(notReady);
    			disableModeButtons(driveModeButton, blindZoneModeButton, parkingModeButton, notReady);
    			if (notReady) {
    				validateAndUpdate(null, false, mainRuleLabel, "Update vehicle data");    				
    			}
    		}
    	}
    	else {
    		backUpData.writeUserDataToFile(smartBrakeActivated, blindspotAlwaysActivated, audioWarningActivated);
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