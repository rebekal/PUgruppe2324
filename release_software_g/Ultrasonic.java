package release;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Random;

public class Ultrasonic extends Sensor {
	
	private PythonCaller pyCaller;

	private Double value;
	private String valueFromPy;
	private Random random = new Random();
	
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

//	To use/not use sensors: uncomment/comment marked lines
	public Double getValue(boolean update) {
		if (update) {
			update();	// ***
		}
//		value = random.nextDouble() + random.nextInt(50); // ***
		return Math.floor(value*1e2)/1e2;
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