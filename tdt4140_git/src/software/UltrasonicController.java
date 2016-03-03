package software;

import java.util.HashMap;
import java.util.Map;

public class UltrasonicController implements BaseInterface {
	
	private final Map<String, Ultrasonic> sensors;
	public final double doorLength, SENSOR_MAX_DISTANCE, BLIND_ZONE;

	public UltrasonicController(Map<String, Ultrasonic> sensors, double doorLength, Double SENSOR_MAX_DISTANCE, Double BLIND_ZONE) {
		this.sensors = sensors;
		this.doorLength = doorLength;
		this.SENSOR_MAX_DISTANCE = SENSOR_MAX_DISTANCE;
		this.BLIND_ZONE = BLIND_ZONE;
	}

	public Double getDistance(String sensor) {
		return sensors.get(sensor).getValue();
	}
	
	public Map<String, Double> getAllDistances() {
		Map<String, Double> allDistances = new HashMap<String, Double>();
		for (Map.Entry<String, Ultrasonic> entry : sensors.entrySet()) {
		    allDistances.put(entry.getKey(), entry.getValue().getValue());
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
	
//	(hastighet * hastighet) / (2 * friksjon * gravitasjon)
	public boolean isThreeSecondRuleOK(double carSpeed, double weatherValue, double temperature) {
		Double value = sensors.get(FRONT).getValue();
		return (value != null) ? ((Math.pow(carSpeed, 2)) / (2 * weatherValue * 9.81) < value) : false;
	}
	
	public boolean isParkingSpaceOK() {
		Double leftValue = sensors.get(LEFT).getValue(), rightValue = sensors.get(RIGHT).getValue();
		return (leftValue != null && rightValue != null) ? (doorLength < leftValue) && (doorLength < rightValue) : false;
	}
	
	public boolean noObjectInBlindZone(String sensor) {
		if (!sensor.equals(LEFT) && !sensor.equals(RIGHT)) {
			warning(INPUT, null);
		}
		Double value = sensors.get(sensor).getValue();
		return (value != null) ? (value < BLIND_ZONE) : false;
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
