package software;

import java.util.HashMap;
import java.util.Map;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;

public class Main implements BaseInterface {	
	
//	Init data
	public final double doorLength, rearDoorLength, SENSOR_MAX_DISTANCE, BLIND_ZONE;
	public final Map<String, Double> weatherFrictionTable;
	
//	Controller classes
	private GpioController gpc;
	private UltrasonicController ultrac;
	private LEDController ledc;
	private static GUIController guic;
	
	public Main(double doorLength, double rearDoorLength, double SENSOR_MAX_DISTANCE, double BLIND_ZONE) {
		if (! isValidFixedDistances(doorLength, rearDoorLength, SENSOR_MAX_DISTANCE, BLIND_ZONE)) {
			throw new IllegalArgumentException("Fixed value(s) not valid");
		}
		this.doorLength = doorLength;
		this.rearDoorLength = rearDoorLength;
		this.SENSOR_MAX_DISTANCE = SENSOR_MAX_DISTANCE;
		this.BLIND_ZONE = BLIND_ZONE;
		weatherFrictionTable = new HashMap<String, Double>();
		insertWeatherFrictionValuesIntoLookUpTable();
	}
	
	/*
	 * > VG verdier:  		(Fysikk tabell)
	 * Tørr asfalt: 0,9		(0.4 - 1.0)
	 * Våt asfalt: 0,6		(0.05 - 0.9)
	 * Snø: 0,3
	 * Våt is: 0,15
	 */
	private void insertWeatherFrictionValuesIntoLookUpTable() {
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
		
//		GUI Controller
		guic = new GUIController(this);
		
//		Ultrasonic Controller
		Map<String, Ultrasonic> sensors = new HashMap<String, Ultrasonic>();
		sensors.put(FRONT, new Ultrasonic(FRONT, gpc, "5", "6"));
		sensors.put(LEFT, new Ultrasonic(LEFT, gpc, "7", "8"));
		sensors.put(RIGHT, new Ultrasonic(RIGHT, gpc, "9", "10"));
		sensors.put(REAR, new Ultrasonic(REAR, gpc, "11", "12"));
//		keyEqualsSensorName(sensors)
		ultrac = new UltrasonicController(sensors, doorLength, SENSOR_MAX_DISTANCE, BLIND_ZONE);
		
//		LED Controller
		Map<String, LED> LEDs = new HashMap<String, LED>();
		LEDs.put(RED_FRONT, new LED(RED_FRONT, gpc, "1"));
		LEDs.put(RED_LEFT, new LED(RED_LEFT, gpc, "2"));
		LEDs.put(RED_RIGHT, new LED(RED_RIGHT, gpc, "3"));
//		keyEqualsSensorName(LEDs)
		ledc = new LEDController(LEDs);
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
			if (! guic.getDrivingButton().isDisabled()) {
				brakeDistance = brakeDistance(getCarSpeed(), getFrictionValue(), getReactionTime());
				guic.updateDistanceLabelColor(guic.getDistanceFront(), brakeDistance ,ultrac.getSensorValue(FRONT, false));
				if (ultrac.isDistanceToCarInfrontOK(brakeDistance)) {
					ledc.ledOff(RED_FRONT);					
				}
				else {
					System.out.println("Test: you are too close to the vehicle ahead! Distance: " + ultrac.getSensorValue(FRONT, false));
					ledc.ledOn(RED_FRONT);
				}
				
				if (ultrac.noObjectInBlindZone(LEFT)) {
					ledc.ledOff(RED_LEFT);
				}
				else {
					System.out.println("Test: object in left blind zone!");
					ledc.ledOn(RED_LEFT);
				}
				
				if (ultrac.noObjectInBlindZone(RIGHT)) {
					ledc.ledOff(RED_RIGHT);
				}
				else {
					System.out.println("Test: object in right blind zone!");
					ledc.ledOn(RED_RIGHT);
				}
				
			}
			else if (! guic.getParkingButton().isDisabled()) {
				if (! ultrac.isParkingSpaceOK()) {
					System.out.println("Test: parking space not sufficient! Leftside: "
							+ ultrac.getSensorValue(LEFT, false) + ", Rightside: " + ultrac.getSensorValue(RIGHT, false) + ", Behind: " + ultrac.getSensorValue(REAR, false));
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
	public void setUseFrictionValue(String useFrictionValue) {
		this.useFrictionValue = useFrictionValue;
	}
	
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
	 */
	public double brakeDistance(double carSpeed, double frictionValue, double reactionTime) {
		return Math.pow(carSpeed, 2) / (2 * frictionValue * 9.81) + carSpeed * reactionTime;
	}
	
//	*** Car data ***
	
	public static void main(String[] args) {
		Main main = new Main(210.5, 155.5, 500.0, 200.0);
//		main.init();
//		main.run();
		guic.launch(args);
	}

}
