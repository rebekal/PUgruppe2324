package software;

import java.util.HashMap;
import java.util.Map;

public class UltrasonicController implements BaseInterface {
	
	private final Map<String, Ultrasonic> sensors;
	public final double doorLength, SENSOR_MAX_DISTANCE, BLIND_ZONE;

	public UltrasonicController(Map<String, Ultrasonic> sensors, double doorLength, double SENSOR_MAX_DISTANCE, double BLIND_ZONE) {
		this.sensors = sensors;
		this.doorLength = doorLength;
		this.SENSOR_MAX_DISTANCE = SENSOR_MAX_DISTANCE;
		this.BLIND_ZONE = BLIND_ZONE;
	}
	
	public Double getSensorValue(String sensor, boolean update) {
		return sensors.get(sensor).getValue(update);
	}
	
	public Map<String, Double> getAllDistances() {
		Map<String, Double> allDistances = new HashMap<String, Double>();
		for (Map.Entry<String, Ultrasonic> entry : sensors.entrySet()) {
		    allDistances.put(entry.getKey(), entry.getValue().getValue(true));
		}
		return allDistances;
	}
	
	public void updateSensors() {
		for (Map.Entry<String, Ultrasonic> entry : sensors.entrySet()) {
		    entry.getValue().update();
		}
	}
	
	public void resetSensors() {
		for (Map.Entry<String, Ultrasonic> entry : sensors.entrySet()) {
		    entry.getValue().reset();
		}
	}
	
	public boolean isDistanceToCarInfrontOK(double currentDistance) {
		Double value = getSensorValue(FRONT, true);
		return (value != null) ? currentDistance < value : false;
	}
	
	public boolean isParkingSpaceOK() {
		Double leftValue = getSensorValue(LEFT, true), rightValue = getSensorValue(RIGHT, true);
		return (leftValue != null && rightValue != null) ? (doorLength < leftValue) && (doorLength < rightValue) : false;
	}
	
	public boolean noObjectInBlindZone(String sensor) {
		Double value = getSensorValue(sensor, true);
		return (value != null && (sensor.equals(LEFT) || sensor.equals(RIGHT))) ? (value < BLIND_ZONE) : false;
	}
	
//	Testing
	private void warning(String state, String sensor) {
		switch (state) {
		case INPUT: System.out.println("Invalid input" + System.lineSeparator()); break;
		case SENSOR: System.out.println("Warning: check " + sensor + " sensor!" + System.lineSeparator()); break;
		default: System.out.println("Warning: ?");
		}
	}
	
	public void check() {
		for (Map.Entry<String, Ultrasonic> entry : sensors.entrySet()) {
			System.out.println(entry);
		}
	}
}
