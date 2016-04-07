package software;

import java.io.IOException;

public class Ultrasonic extends Sensor {
	
	private Double value;
	private PythonCaller pyCaller;
	private String valueFromPy;
	
	public Ultrasonic(String sensorName, PythonCaller pyCaller) {
		super(sensorName, "Trigger pin", "Echo pin");
		this.pyCaller = pyCaller;
	}
	
	@Override
	protected String getModel() {
		return "HC-SR04 Ultrasonic Device";
	}
	
	@Override
	protected String getPin1Name() {
		return "TriggerPin";
	}

	@Override
	protected String getPin2Name() {
		return "EchoPin";
	}

	public Double getValue(boolean update) {
		if (update) {
			update();			
		}
		return value;
	}
	
	public void reset() {
		value = null;
	}
	
	public void update() {
		try {
			valueFromPy = pyCaller.call(sensorName);
			if (valueFromPy.equals("None")) {
				value = null;
			}
			else {
				value = Double.valueOf(valueFromPy);				
			}
		} catch (IOException e) {
			value = null;
		} catch (NumberFormatException e) {
			value = null;
		}
	}
	
}
