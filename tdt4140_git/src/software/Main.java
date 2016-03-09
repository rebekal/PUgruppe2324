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
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class Main extends Application implements BaseInterface, Initializable {	
	
//	Init data
	public final double doorLength, SENSOR_MAX_DISTANCE, BLIND_ZONE;
	
	private Map<String, Double> weatherFrictionTable;

	private GpioController gpc;
	private UltrasonicController uc;
	private LEDController lc;
	
	public Main(double doorLength, double SENSOR_MAX_DISTANCE, double BLIND_ZONE) {
		if (! isValidFixedDistances(doorLength, SENSOR_MAX_DISTANCE, BLIND_ZONE)) {
			throw new IllegalArgumentException("Fixed value(s) not valid");
		}
		this.doorLength = doorLength;
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

	private static boolean isValidFixedDistances(double doorLength, Double MAX_DISTANCE, Double BLIND_ZONE) {
		return 0 <= doorLength && 0 <= MAX_DISTANCE && 0 <= BLIND_ZONE;
	}
	
	public void init() {
//		Raspberry pi, GpioController
		gpc = GpioFactory.getInstance();
		
//		Ultrasonic
		Map<String, Ultrasonic> sensors = new HashMap<String, Ultrasonic>();
		sensors.put(FRONT, new Ultrasonic(FRONT, gpc, "5", "6"));
		sensors.put(LEFT, new Ultrasonic(LEFT, gpc, "7", "8"));
		sensors.put(RIGHT, new Ultrasonic(RIGHT, gpc, "9", "10"));
		sensors.put(BEHIND, new Ultrasonic(BEHIND, gpc, "11", "12"));
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
//		uc.noObjectInBlindZone(LEFT);
//		uc.noObjectInBlindZone(RIGHT);
//		uc.isParkingSpaceOK();
//		uc.isThreeSecondRuleOK(getCarSpeed(), getWeatherValue(), getTemperature());
//		uc.getDistance(FRONT);
//		uc.getAllDistances();

	}

//	*** Car data ***
	
	private double getCarSpeed() {
		return 0;
	}
	
	private double getFrictionValue(String weatherRoadCondition) {
		return weatherFrictionTable.get(weatherRoadCondition);
	}
	
	private double getTemperature() {
		return 0;
	}
	
//	*** Car data ***
//	*** FXML ***

	@FXML
	private Button drivingButton, parkingButton, stopButton, sunButton, rainButton, snowButton, iceButton;
	
	@FXML
	private Label distanceFront, currentFrontDistance, distanceLeft, currentLeftDistance, distanceRight, currentRightDistance;
	@FXML
	private Label distanceBehind, currentDistanceBehind, leftBlindZone, rightBlindZone;
	
	@FXML
	private ImageView leftCar, myCar, rightCar;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		System.out.println("Init");
	}
	
	@FXML
	public void drivingModeEnabled() {
		distanceFront.setVisible(true);
		currentFrontDistance.setVisible(true);
		sunButton.setVisible(true);
		rainButton.setVisible(true);
		snowButton.setVisible(true);
		iceButton.setVisible(true);
		
		distanceLeft.setVisible(false);
		currentLeftDistance.setVisible(false);
		distanceRight.setVisible(false);
		currentRightDistance.setVisible(false);
		distanceBehind.setVisible(false);
		currentDistanceBehind.setVisible(false);	
	}
	
	@FXML
	public void parkingModeEnabled() {
		distanceFront.setVisible(false);
		currentFrontDistance.setVisible(false);
		sunButton.setVisible(false);
		rainButton.setVisible(false);
		snowButton.setVisible(false);
		iceButton.setVisible(false);
		
		distanceLeft.setVisible(true);
		currentLeftDistance.setVisible(true);
		distanceRight.setVisible(true);
		currentRightDistance.setVisible(true);
		distanceBehind.setVisible(true);
		currentDistanceBehind.setVisible(true);
	}
	
	
	@Override
	public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(Main.class.getResource("GUI.fxml"));
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
	}
		
//	*** FXML ***
	
	public static void main(String[] args) {
		Main main = new Main(210.5, 500.0, 200.0);
//		main.init();
//		main.run();
		launch(args);
	}

}
