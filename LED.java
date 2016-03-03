package software;

import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.RaspiPin;

public class LED extends Sensor {
	
	private GpioPinDigitalOutput anode;
	
	public LED(String LEDName, String pinNumber) {
		super(LEDName, pinNumber, null);
		setup();
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
	
	public void toggle() {
		anode.toggle();
	}
	
	public void setLow() {
		anode.low();
	}
	
	public void setHigh() {
		anode.high();
	}

}
