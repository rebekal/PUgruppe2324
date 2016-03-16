package software;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class Main extends Application implements BaseInterface, Initializable {	
	
//	Init data
	public final double doorLength, rearDoorLength, SENSOR_MAX_DISTANCE, BLIND_ZONE;
	
	private Map<String, Double> weatherFrictionTable;

	private GpioController gpc;
	private UltrasonicController uc;
	private LEDController lc;
	
	public Main(double doorLength, double rearDoorLength, double SENSOR_MAX_DISTANCE, double BLIND_ZONE) {
		if (! isValidFixedDistances(doorLength, rearDoorLength, SENSOR_MAX_DISTANCE, BLIND_ZONE)) {
			throw new IllegalArgumentException("Fixed value(s) not valid");
		}
		this.doorLength = doorLength;
		this.rearDoorLength = rearDoorLength;
		this.SENSOR_MAX_DISTANCE = SENSOR_MAX_DISTANCE;
		this.BLIND_ZONE = BLIND_ZONE;
		createWeatherFrictionLookUpTable();
	}
	
	/*
	 * > VG verdier:  		(Fysikk tabell)
	 * Tørr asfalt: 0,9		(0.4 - 1.0)
	 * Våt asfalt: 0,6		(0.05 - 0.9)
	 * Snø: 0,3
	 * Våt is: 0,15
	 */
	private void createWeatherFrictionLookUpTable() {
		weatherFrictionTable = new HashMap<String, Double>();
		weatherFrictionTable.put(DRY_ASPHALT, 0.9);
		weatherFrictionTable.put(WET_ASPHALT, 0.05);
		weatherFrictionTable.put(SNOW, 0.3);
		weatherFrictionTable.put(ICE, 0.02);
	}

	private static boolean isValidFixedDistances(double doorLength, double rearDoorLength, double MAX_DISTANCE, double BLIND_ZONE) {
		return 0 <= doorLength && 0 <= rearDoorLength && 0 <= MAX_DISTANCE && 0 <= BLIND_ZONE;
	}
	
	public void init() {
//		Raspberry pi, GpioController
		gpc = GpioFactory.getInstance();
		
//		Ultrasonic
		Map<String, Ultrasonic> sensors = new HashMap<String, Ultrasonic>();
		sensors.put(FRONT, new Ultrasonic(FRONT, gpc, "5", "6"));
		sensors.put(LEFT, new Ultrasonic(LEFT, gpc, "7", "8"));
		sensors.put(RIGHT, new Ultrasonic(RIGHT, gpc, "9", "10"));
		sensors.put(REAR, new Ultrasonic(REAR, gpc, "11", "12"));
//		keyEqualsSensorName(sensors)
		uc = new UltrasonicController(sensors, doorLength, SENSOR_MAX_DISTANCE, BLIND_ZONE);
		
//		LED
		Map<String, LED> LEDs = new HashMap<String, LED>();
		LEDs.put(RED, new LED(RED, gpc, "1"));
		LEDs.put(YELLOW, new LED(YELLOW, gpc, "2"));
		LEDs.put(GREEN, new LED(GREEN, gpc, "3"));
//		keyEqualsSensorName(LEDs)
		lc = new LEDController(LEDs);
	}
	
	private static void keyEqualsSensorName(Map<String, Sensor> dictionary) {
		for (Map.Entry<String, Sensor> entry : dictionary.entrySet()) {
			if (! entry.getKey().equals(entry.getValue().sensorName)) {
				throw new IllegalArgumentException("Key != sensorname");
			}
		}
	}

	public void run() {
		System.out.println("Program start");
		double brakeDistance;
		
		while (engineOn()) {
			if (! drivingButton.isDisabled()) {
				brakeDistance = brakeDistance(getCarSpeed(), getFrictionValue(), getReactionTime());
				if (! uc.isDistanceToCarInfrontOK(brakeDistance)) {
					System.out.println("Test: you are too close to the vehicle ahead! Distance: " + uc.getSensorValue(FRONT, false));
					updateDistanceLabelColor(distanceFront, brakeDistance ,uc.getSensorValue(FRONT, false));
				}
				
				if (! uc.noObjectInBlindZone(LEFT)) {
					System.out.println("Test: object in left blind zone!");
					
				}
				
				if (! uc.noObjectInBlindZone(RIGHT)) {
					System.out.println("Test: object in right blind zone!");
				}
				
			}
			else if (! parkingButton.isDisabled()) {
				if (! uc.isParkingSpaceOK()) {
					System.out.println("Test: parking space not sufficient! Leftside: "
							+ uc.getSensorValue(LEFT, false) + ", Rightside: " + uc.getSensorValue(RIGHT, false) + ", Behind: " + uc.getSensorValue(REAR, false));
				}
				
			}
		}
		System.out.println("Program shutdown");
	}

//	*** Car data ***
	
	
	private boolean engineOn() {
		return true;
	}
	
	private double getCarSpeed() {
		return 0;
	}
	
	private String useFrictionValue;
	private double getFrictionValue() {
		return weatherFrictionTable.get(useFrictionValue);
	}
	
	private double getReactionTime() {
		return 0;
	}
	
	private double getOutdoorTemperature() {
		return 0;
	}
	
	/*
	 *  Formula brake distance : velocity^2 / (2 * friction * gravity)
	 *  w/reaction time -> + velocity * reactiontime
	 *  - where velocity is measured in meter/second
	 *  - gravity = 9.81m/s^2
	 *  - outdoortemperature (?)
	 */
	public double brakeDistance(double carSpeed, double frictionValue, double reactionTime) {
		return Math.pow(carSpeed, 2) / (2 * frictionValue * 9.81) + carSpeed * reactionTime;
	}
	
//	*** Car data ***
//	*** FXML ***

	@FXML
	private ToggleButton drivingButton, parkingButton, stopButton, dryAsphaltButton, wetAsphaltButton, snowButton, iceButton;
	
	@FXML
	private Label distanceFront, distanceLeft, distanceRight, distanceBehind;
	
	@FXML
	private Rectangle screenBackground, menuBackground;
	
	@FXML
	private ImageView leftCar, myCar, rightCar;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// Main screen
		myCar.setVisible(true);
		leftCar.setVisible(false);
		rightCar.setVisible(false);
		screenBackground.setFill(Color.web("#d6f1ff"));
		distanceFront.setVisible(false);
		distanceLeft.setVisible(false);
		distanceRight.setVisible(false);
		distanceBehind.setVisible(false);
		
		// Menu
		menuBackground.setFill(Color.BLACK);
		drivingButton.setDisable(false);
		parkingButton.setDisable(false);
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
		distanceBehind.setVisible(false);
		// Disables parking mode button
		parkingButton.setDisable(true);
	}
	
	@FXML
	public void dryAsphaltButtonClicked() {
		useFrictionValue = DRY_ASPHALT;
	}
	
	@FXML
	public void wetAsphaltButtonClicked() {
		useFrictionValue = WET_ASPHALT;
	}
	
	@FXML
	public void snowButtonClicked() {
		useFrictionValue = SNOW;
	}
	
	@FXML
	public void iceButtonClicked() {
		useFrictionValue = ICE;
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
		distanceBehind.setVisible(true);
		
		drivingButton.setDisable(true);
	}
	
	@FXML
	public void objectDetectedInLeftBlindZone() {
		screenBackground.setFill(new LinearGradient(125, 0, 225, 0, false, CycleMethod.NO_CYCLE, new Stop[] { new Stop(0, Color.BLACK), new Stop(1, Color.RED)}));
	}
	
	@FXML
	public void objectDetectedInRightBlindZone() {
		
	}
	
	@FXML
	public void frontDistance() {
		
	}
	
	@FXML
	public void behindDistance() {
		
	}
	
	private void updateDistanceLabelColor(Label label, double validDistance, double currentDistance) {
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
	
	@Override
	public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(Main.class.getResource("GUI2.fxml"));
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
	}
		
//	*** FXML ***
	
	public static void main(String[] args) {
		Main main = new Main(210.5, 155.5, 500.0, 200.0);
//		main.init();
//		main.run();
		launch(args);
	}

}
