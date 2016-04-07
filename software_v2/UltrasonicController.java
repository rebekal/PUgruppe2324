package software;

import java.util.HashMap;
import java.util.Map;

public class UltrasonicController implements BaseInterface {
	
	private final Map<String, Ultrasonic> sensors; 

	public UltrasonicController(Map<String, Ultrasonic> sensors) {
		this.sensors = sensors;
	}
	
	public Ultrasonic getSensor(String sensor) {
		return sensors.get(sensor);
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
}
