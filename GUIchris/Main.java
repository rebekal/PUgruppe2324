package software;


import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

import javafx.application.Platform;
import software.GUIController2;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;

public class Main extends Thread implements BaseInterface{	
	
//	Init data
	public final double doorLength, rearDoorLength, SENSOR_MAX_DISTANCE, BLIND_ZONE;
	public final Map<String, Double> weatherFrictionTable;
	
//	Controller classes
	private GpioController gpc;
	private UltrasonicController ultrac;
	private LEDController ledc;
	
	private String mode;
	private String weather = DRY_ASPHALT;
	private GUIController2 guicont;
	
	public void setMode(String mode){
		this.mode = mode;
	}
	
	public void setWeather(String weather){
		this.weather = weather;
	}
	
	public Main(double doorLength, double rearDoorLength, double SENSOR_MAX_DISTANCE, double BLIND_ZONE, GUIController2 guic) {
		if (! isValidFixedDistances(doorLength, rearDoorLength, SENSOR_MAX_DISTANCE, BLIND_ZONE)) {
			throw new IllegalArgumentException("Fixed value(s) not valid");
		}
		this.doorLength = doorLength;
		this.rearDoorLength = rearDoorLength;
		this.SENSOR_MAX_DISTANCE = SENSOR_MAX_DISTANCE;
		this.BLIND_ZONE = BLIND_ZONE;
		weatherFrictionTable = new HashMap<String, Double>();
		insertWeatherFrictionValuesIntoLookUpTable();
		this.guicont = guic;
	}
	
	/*
	 * > VG verdier:  		(Fysikk tabell)
	 * T�rr asfalt: 0,9		(0.4 - 1.0)
	 * V�t asfalt: 0,6		(0.05 - 0.9)
	 * Sn�: 0,3
	 * V�t is: 0,15
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
		while (engineOn()) {
			System.out.println(this.mode);
			if (this.mode == "Driving") {
				String frontDist = String.valueOf((int) (Math.random() * 100));
				//boolean leftBlind= ! ultrac.noObjectInBlindZone(LEFT);
				//boolean rightBlind = ! ultrac.noObjectInBlindZone(RIGHT);
				Platform.runLater(new Runnable() {
					   @Override
					   public void run() {
						   guicont.setCurrentFrontDistance(Double.valueOf(frontDist));
						   //guicont.setLeftBlindZone(leftBlind);
						   //guicont.setRightBlindZone(rightBlind);
					   }
					});
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		
			}
			else if (this.mode == "Parking") {
				double leftDist = ultrac.getSensorValue(LEFT, true);
				double rightDist = ultrac.getSensorValue(RIGHT, true);
				double behindDist = ultrac.getSensorValue(REAR, true);
				guicont.setCurrentLeftDistance(leftDist);
				guicont.setCurrentRightDistance(rightDist);
				guicont.setCurrentDistanceBehind(behindDist);
			}
		}
	}

//	*** Car data ***
	
	private boolean engineOn() {
		return true;
	}
	
	private double getCarSpeed() {
		return 15;
	}
	
	private String useFrictionValue = DRY_ASPHALT;
	public void setUseFrictionValue(String useFrictionValue) {
		this.useFrictionValue = useFrictionValue;
	}
	
	private double getFrictionValue() {
		return weatherFrictionTable.get(weather);
	}
	
	private double getReactionTime() {
		return 1;
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
	public double brakeDistance() {
		return Math.pow(getCarSpeed(), 2) / (2 * getFrictionValue() * 9.81) + getCarSpeed() * getReactionTime();
	}
	
//	*** Car data ***


}
