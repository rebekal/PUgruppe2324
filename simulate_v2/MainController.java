package simulate;

import java.util.HashMap;
import java.util.Map;

public class MainController implements BaseInterface {	
	
//	Controller classes
	private UltrasonicController ultracontroller;
	private GUIController gui;
	
//	Continuous data from external sources
	private CarData carData;
	private WeatherData weatherData;
	
//	Filedata
	private BackUpData backUpData;
	
//	Variables
	private boolean runProgram, drivingMode, blindZoneMode, parkingMode, simulateMode;
	
	public MainController(GUIController guiController) {
		this.gui = guiController;
		init();
	}
	
	public void init() {
//		CarData
		initCarData();
		
//		WeatherData
		weatherData = new WeatherData();
		
//		Ultrasonic Controller
		Map<String, Ultrasonic> ultrasonicSensors = new HashMap<String, Ultrasonic>();
		ultrasonicSensors.put(FRONT, new Ultrasonic(FRONT, "5", "6"));
		ultrasonicSensors.put(LEFT, new Ultrasonic(LEFT, "7", "8"));
		ultrasonicSensors.put(RIGHT, new Ultrasonic(RIGHT, "9", "10"));
		ultrasonicSensors.put(REAR, new Ultrasonic(REAR, "11", "12"));
		ultracontroller = new UltrasonicController(ultrasonicSensors);
	}

	public void run() {
		boolean usingLeftTurnLight = carData.isLeftTurnLightOn();
		boolean usingRightTurnLight = carData.isRightTurnLightOn();
		boolean redTrafficLight = carData.getRedTrafficLight();
		
		if (drivingMode || blindZoneMode) {
			carData.simulateOneStep();
//			Car speed, speed limit, brake distance
			gui.setLabelText(gui.getCarSpeedLabel(), String.valueOf(carData.getCarSpeed()));
			gui.setLabelText(gui.getSpeedLimitLabel(), String.valueOf(carData.getSpeedLimit()));
			gui.setLabelText(gui.getBrakeDistanceLabel(), String.valueOf((int) getBrakeDistance()));
//			Left turn light / right turn light
			gui.setImageVisible(gui.getLeftTurnLight(), usingLeftTurnLight);
			gui.setImageVisible(gui.getRightTurnLight(), usingRightTurnLight);			
//			Traffic light (red)
			gui.setImageVisible(gui.getRedTrafficLight(), redTrafficLight);
			gui.setLabelVisible(gui.getRedTrafficLightCountLabel(), redTrafficLight);
			gui.setLabelText(gui.getRedTrafficLightCountLabel(), String.valueOf(carData.getRedTrafficLightCount()));
			gui.setLabelVisible(gui.getRedTrafficLightCountLabel(), redTrafficLight);
		}
		
		
		if (drivingMode) {
			Double frontDistance = ultracontroller.getSensorValue(FRONT, true);
			gui.updateDistance(gui.getFrontDistanceLabel(), frontDistance, getBrakeDistance());
		}
		
		if (blindZoneMode) {
			boolean leftBlind = isObjectInBlindZone(LEFT);
			boolean rightBlind = isObjectInBlindZone(RIGHT);
			if (usingLeftTurnLight) {
				gui.setImageVisible(gui.getLeftCar(), leftBlind);				
			}
			else {
				gui.setImageVisible(gui.getLeftCar(), false);
			}
			
			if (usingRightTurnLight) {
				gui.setImageVisible(gui.getRightCar(), rightBlind);				
			}
			else {
				gui.setImageVisible(gui.getRightCar(), false);
			}
		}
		
		if (parkingMode) {
			double frontDistance = ultracontroller.getSensorValue(FRONT, true);
			double leftDistance = ultracontroller.getSensorValue(LEFT, true);
			double rightDistance = ultracontroller.getSensorValue(RIGHT, true);
			double rearDistance = ultracontroller.getSensorValue(REAR, true);
			gui.updateDistance(gui.getFrontDistanceLabel(), frontDistance, 0.5);
			gui.updateDistance(gui.getLeftDistanceLabel(), leftDistance, carData.doorLength);
			gui.updateDistance(gui.getRightDistanceLabel(), rightDistance, carData.doorLength);
			gui.updateDistance(gui.getRearDistanceLabel(), rearDistance, carData.rearDoorLength);
		}
	}
	
	public void setMode(String mode, boolean value) {
		switch (mode) {
		case RUN_PROGRAM: runProgram = value; break;
		case DRIVING_MODE: drivingMode = value; break;
		case BLIND_ZONE_MODE: blindZoneMode = value; break;
		case PARKING_MODE: parkingMode = value; break;
		case ENGINE_MODE: carData.setEngineMode(value); break;
		case SIMULATE_MODE: simulateMode = value; break;
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
		case SIMULATE_MODE: return simulateMode;
		default: System.out.println("No mode for '" + mode + "'."); return null;
		}
	}

	/*
	 * Loading and using data that have already been registered before
	 * If no data is found or is invalid: get data from user
	 */
	private void initCarData() {
		backUpData = new BackUpData(carDataFile);
		Map<String, Double> carDataValues = backUpData.readCarDataFromFile();
		if (carDataValues != null) {
			carData = new CarData(carDataValues.get(DOOR_LENGTH), carDataValues.get(REAR_DOOR_LENGTH), carDataValues.get(BLIND_ZONE_VALUE), carDataValues.get(TOP_SPEED));			
		}
		else {
			carDataValues = gui.initCarValues();
			backUpData.writeCarDataToFile(carDataValues.get(DOOR_LENGTH), carDataValues.get(REAR_DOOR_LENGTH), carDataValues.get(BLIND_ZONE_VALUE), carDataValues.get(TOP_SPEED));
			carData = new CarData(carDataValues.get(DOOR_LENGTH), carDataValues.get(REAR_DOOR_LENGTH), carDataValues.get(BLIND_ZONE_VALUE), carDataValues.get(TOP_SPEED));
		}
	}
	
//	Methods for GUI
	public void resetCarSimulation() {
		carData.resetSimulation();
	}
	
	public void prepareParkingMode() {
		gui.setImageVisible(gui.getRedTrafficLight(), false);
		gui.setLabelVisible(gui.getRedTrafficLightCountLabel(), false);
	}
	
	/*
	 *  Formula brake distance : velocity^2 / (2 * friction * gravity) + (velocity * reactiontime)
	 *  - where velocity is measured in meter/second
	 *  - gravity = 9.81m/s^2
	 *  - reactiontime = 1
	 */	
	public double getBrakeDistance() {
		double meterPerSecond = carData.getCarSpeed() / 3.6;
		return Math.pow(meterPerSecond, 2) / (2 * weatherData.getFrictionValue() * 9.81) + meterPerSecond;
	}
	
	public boolean isDistanceToCarInfrontOK(Double distance) {
		return (distance != null) ? getBrakeDistance() < distance : true;
	}
	
	/*
	 * Checking left and right ultrasonic sensor if there's enough space to open doors
	 * Only checking value < doorLenght if sensor did not timeout
	 */
	public boolean isParkingSpaceOK() {
		Double leftValue = ultracontroller.getSensorValue(LEFT, true), rightValue = ultracontroller.getSensorValue(RIGHT, true);
		if (leftValue == null && rightValue == null) {
			return true;
		}
		else if (leftValue != null && rightValue == null) {
			return carData.doorLength < leftValue;
		}
		else if (leftValue == null && rightValue != null) {
			return carData.doorLength < rightValue;
		}
		return (carData.doorLength < leftValue) && (carData.doorLength < rightValue);
	}
	
	/*
	 * Checking left or right ultrasonic sensor if there's a object (car) in blind zone
	 * If timeout (value == null): blind zone clear
	 * If value < car's blind zone value -> blind zone not clear
	 */
	public boolean isObjectInBlindZone(String sensor) {
		Double value = ultracontroller.getSensorValue(sensor, true);
		return (value != null && (sensor.equals(LEFT) || sensor.equals(RIGHT))) ? (value < carData.blindZoneValue) : false;
	}

	public String getCarData() {
		return DOOR_LENGTH + ": " + carData.doorLength + System.lineSeparator()
				+ REAR_DOOR_LENGTH + ": " + carData.rearDoorLength + System.lineSeparator()
				+ BLIND_ZONE_VALUE + ": " + carData.blindZoneValue + System.lineSeparator()
				+ TOP_SPEED + ": " + carData.topSpeed + System.lineSeparator();
	}
	
	
	
}
