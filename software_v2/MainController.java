package software;

import java.util.HashMap;
import java.util.Map;

public class MainController implements BaseInterface {	
	
//	Controller classes
	private UltrasonicController ultraController;
	private GUIController guiController;
	
//	Continuous data from external sources
	private CarData carData;
	private WeatherData weatherData;
	
//	Filedata
	private BackUpData backUpData;
	private Map<String, Double> carDataValues;
	
//	Variables
	private boolean runProgram, drivingMode, blindZoneMode, parkingMode;
	
	public MainController(GUIController guiController) {
		this.guiController = guiController;
		init();
	}
	
	
	public static void main(String[] args) {
		System.out.println("Brake distance or doorlength < sensor value -> " + MainController.isSensorValueLargerThan(null, 25.0));
		System.out.println("blind zone value < sensor value -> " + MainController.isObjectInBlindZone(null, 300.0));
	}
	
	public void init() {
//		Back up data
		backUpData = new BackUpData(carDataFile);
		
//		CarData
		initCarData();
		
//		WeatherData
		weatherData = new WeatherData();
		
		
//		Ultrasonic Controller
		PythonCaller pyCaller = new PythonCaller();
		Map<String, Ultrasonic> ultrasonicSensors = new HashMap<String, Ultrasonic>();
		ultrasonicSensors.put(FRONT, new Ultrasonic(FRONT, pyCaller));
		ultrasonicSensors.put(LEFT, new Ultrasonic(LEFT, pyCaller));
		ultrasonicSensors.put(RIGHT, new Ultrasonic(RIGHT, pyCaller));
		ultraController = new UltrasonicController(ultrasonicSensors);
	}
	
	
	private double carSpeed, brakeDistance;
	public void run() {
		if (drivingMode || blindZoneMode) {
			carData.simulateOneStep();
			
			carSpeed = carData.getCarSpeed();
			guiController.setLabelText(guiController.getCarSpeedLabel(), String.valueOf(carSpeed));
			double speedLimit = carData.getSpeedLimit();
			guiController.setLabelText(guiController.getSpeedLimitLabel(), String.valueOf(speedLimit));
			brakeDistance = getBrakeDistance(carSpeed, weatherData.getFrictionValue());
			guiController.setLabelText(guiController.getBrakeDistanceLabel(), String.valueOf(brakeDistance));			
		}
		
		if (drivingMode) {
			Double frontDistance = ultraController.getSensorValue(FRONT, true);
			guiController.updateDistance(guiController.getFrontDistanceLabel(), frontDistance, brakeDistance);
			
			if (isSensorValueLargerThan(frontDistance, brakeDistance)) {
//				TODO LED?
			}
		}
		
		if (blindZoneMode) {
			boolean leftBlind = isObjectInBlindZone(ultraController.getSensorValue(LEFT, true), carData.blindZoneValue);
			boolean rightBlind = isObjectInBlindZone(ultraController.getSensorValue(RIGHT, true), carData.blindZoneValue);
			if (carData.isLeftTurnLightOn() && leftBlind) {
				guiController.setImageVisible(guiController.getLeftCar(), true);
			}
			else {
				guiController.setImageVisible(guiController.getLeftCar(), false);
			}
			if (carData.isRightTurnLightOn() && rightBlind) {
				guiController.setImageVisible(guiController.getRightCar(), true);
			}
			else {
				guiController.setImageVisible(guiController.getRightCar(), false);
			}
		}
		
		if (parkingMode) {
			double frontDistance = ultraController.getSensorValue(FRONT, true);
			double leftDistance = ultraController.getSensorValue(LEFT, true);
			double rightDistance = ultraController.getSensorValue(RIGHT, true);
			
			guiController.updateDistance(guiController.getFrontDistanceLabel(), frontDistance, 0.5);
			guiController.updateDistance(guiController.getLeftDistanceLabel(), leftDistance, carData.doorLength);
			guiController.updateDistance(guiController.getRightDistanceLabel(), rightDistance, carData.doorLength);
			if (isSensorValueLargerThan(leftDistance, carData.doorLength)) {
//				TODO ?
			}
			
			if (isSensorValueLargerThan(rightDistance, carData.doorLength)) {
//				TODO ?
			}
			
		}
	}
	
	
	public void setMode(String mode, boolean value) {
		switch (mode) {
		case RUN_PROGRAM: runProgram = value; break;
		case DRIVING_MODE: drivingMode = value; break;
		case BLIND_ZONE_MODE: blindZoneMode = value; break;
		case PARKING_MODE: parkingMode = value; break;
		case ENGINE_MODE: carData.setEngineMode(value); break;
		default: System.out.println("No mode for '" + mode + "'.");
		}
	}
	
	public Boolean getCurrentModeStatus(String mode) {
		switch (mode) {
		case RUN_PROGRAM: return runProgram;
		case DRIVING_MODE: return drivingMode;
		case BLIND_ZONE_MODE: return blindZoneMode;
		case PARKING_MODE: return parkingMode;
		case ENGINE_MODE: return carData.getEngineMode();
		default: System.out.println("No mode for '" + mode + "'."); return null;
		}
	}
	
//	*** Car Data Backup ***
	
	/*
	 * Loading and using data that have already been registered before
	 * If no data is found or is invalid: get data from user
	 */
	private void initCarData() {
		carDataValues = backUpData.readCarDataFromFile();
		if (carDataValues != null) {
			carData = new CarData(carDataValues.get(DOOR_LENGTH), carDataValues.get(BLIND_ZONE_VALUE), carDataValues.get(TOP_SPEED));			
		}
		else {
			carDataValues = guiController.createAndUpdateCarDataValues();
			backUpData.writeCarDataToFile(carDataValues.get(DOOR_LENGTH), carDataValues.get(BLIND_ZONE_VALUE), carDataValues.get(TOP_SPEED));
			carData = new CarData(carDataValues.get(DOOR_LENGTH), carDataValues.get(BLIND_ZONE_VALUE), carDataValues.get(TOP_SPEED));
		}
	}
	
	public Map<String, Double> getCurrentCarDataValues() {
		return carDataValues;
	}
	
	public void clearCarData() {
		backUpData.clearCarData();
	}
	
//	*** Methods for GUI ***
	
	public void resetCarSimulation() {
		carData.resetSimulation();
	}
	
	public void prepareParkingMode() {
		guiController.setImageVisible(guiController.getRedTrafficLight(), false);
		guiController.setLabelVisible(guiController.getRedTrafficLightCountLabel(), false);
	}
	
	/*
	 *  Formula brake distance : velocity^2 / (2 * friction * gravity) + (velocity * reactiontime)
	 *  - where velocity is measured in meter/second
	 *  - gravity = 9.81m/s^2
	 *  - reactiontime = 1
	 */	
	public static double getBrakeDistance(double carSpeed, double frictionValue) {
		double meterPerSecond = carSpeed / 3.6;
		return Math.pow(meterPerSecond, 2) / (2 * frictionValue * 9.81) + meterPerSecond;
	}
	
	/*
	 * Used for:
	 * - brake distance < front sensor value
	 * - door length < left/right sensor value
	 */
	public static boolean isSensorValueLargerThan(Double sensorValue, double value) {
		return (sensorValue != null) ? value < sensorValue : true;
	}
	
	/*
	 * Checking left or right ultrasonic sensor if there's a object (car) in blind zone
	 * If timeout (value == null): blind zone clear
	 * If value < car's blind zone value -> blind zone not clear
	 */
	public static boolean isObjectInBlindZone(Double sensorValue, double blindZoneValue) {
		return (sensorValue != null) ? (sensorValue < blindZoneValue) : false;
	}

	public String getCarDataString() {
		return DOOR_LENGTH + ": " + carData.doorLength + System.lineSeparator()
				+ BLIND_ZONE_VALUE + ": " + carData.blindZoneValue + System.lineSeparator()
				+ TOP_SPEED + ": " + carData.topSpeed + System.lineSeparator();
	}
	
	
	
}
