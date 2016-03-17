package software;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.RaspiPin;

public class LED extends Sensor {
	
	private GpioPinDigitalOutput anode;
	private boolean lightOn;
	
	public LED(String LEDName, GpioController gpc, String pinNumber) {
		super(LEDName, gpc, pinNumber, null);
	}

	@Override
	protected void setup() {
		anode = gpc.provisionDigitalOutputPin(RaspiPin.getPinByName(pin1));
	}

	@Override
	protected String getModel() {
		return "LED";
	}

	@Override
	protected String getPin1Name() {
		return "Anode";
	}

	@Override
	protected String getPin2Name() {
		return "Cathode (ground)";
	}
	
	public boolean lightOn() {
		return lightOn;
	}
	
	public void toggle() {
		anode.toggle();
		lightOn = ! lightOn;
	}
	
	public void setLow() {
		anode.low();
		lightOn = false;
	}
	
	public void setHigh() {
		anode.high();
		lightOn = true;
	}

}
