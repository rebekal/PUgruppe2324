package software;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;

public abstract class Sensor {
	
	public final String sensorName, pin1, pin2;
	
	protected GpioController gpc;
	
	public Sensor(String sensorName, GpioController gpc, String pin1, String pin2) {
		this.sensorName = sensorName;
		this.pin1 = pin1;
		this.pin2 = pin2;
		this.gpc = gpc;
		setup();
	}
	
	protected abstract void setup();
	
	protected abstract String getModel();
	
	protected abstract String getPin1Name();
	
	protected abstract String getPin2Name();
	
	@Override
	public String toString() {
		String result = getModel() + ": " + sensorName + ", " + getPin1Name() + " = " + pin1;
		if (pin2 != null) {
			result += ", " + getPin2Name() + " = " + pin2;
		}
		return result;
	}
	
}
