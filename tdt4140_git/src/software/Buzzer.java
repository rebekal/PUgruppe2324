package software;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.RaspiPin;

public class Buzzer extends Sensor {
	
	private GpioPinDigitalOutput outPin;
	private boolean makingSound;
	
	public Buzzer(String sensorName, GpioController gpc, String pinNumber) {
		super(sensorName, gpc, pinNumber, null);
	}
	
	@Override
	protected void setup() {
		outPin = gpc.provisionDigitalOutputPin(RaspiPin.getPinByName(pin1));
	}

	@Override
	protected String getModel() {
		return "Buzzer";
	}

	@Override
	protected String getPin1Name() {
		return "Output pin";
	}

	@Override
	protected String getPin2Name() {
		return "Ground";
	}
	
	public boolean makingSound() {
		return makingSound;
	}
	
	public void setLow() {
		outPin.low();
	}
	
	public void setHigh() {
		outPin.high();
	}
	
	public void toggle() {
		outPin.toggle();
	}
	
}
