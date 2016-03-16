package software;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.RaspiPin;

public class LED extends Sensor {
	
	private GpioPinDigitalOutput anode;
	private boolean isHigh;
	
	public LED(String LEDName, GpioController gpc, String pinNumber) {
		super(LEDName, gpc, pinNumber, null);
		setup();
	}

	@Override
	protected void setup() {
		anode = gpc.provisionDigitalOutputPin(RaspiPin.getPinByName(pin1));
		setLow();
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
	
	public boolean isHigh() {
		return isHigh;
	}
	
	public void toggle() {
		anode.toggle();
		isHigh = ! isHigh;
	}
	
	public void setLow() {
		anode.low();
		isHigh = false;
	}
	
	public void setHigh() {
		anode.high();
		isHigh = true;
	}

}
