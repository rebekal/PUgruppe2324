package software;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

public class Ultrasonic extends Sensor {
	
	private GpioPinDigitalOutput trigPin;
	private GpioPinDigitalInput ecPin;
	
	private Double value;
	
	public Ultrasonic(String sensorName, GpioController gpc, String triggerPin, String echoPin) {
		super(sensorName, gpc, triggerPin, echoPin);
		setup();
	}
	
	@Override
	protected void setup() {
		trigPin = gpc.provisionDigitalOutputPin(RaspiPin.getPinByName(pin1));
		ecPin = gpc.provisionDigitalInputPin(RaspiPin.getPinByName(pin2));
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
//			Sending a active pulse
			trigPin.high();
			Thread.sleep(20);
		} catch (InterruptedException e) {
			System.out.println("InterruptedExeption - ultrasonic update");
		}
		
		trigPin.low();
//		Waiting for the result
		
		double startTime = System.currentTimeMillis(), stopTime = 0;
		do {
			stopTime = System.currentTimeMillis();
			if ((System.currentTimeMillis() - startTime) >= 40) {
				break;
			}
		} while (ecPin.getState() != PinState.HIGH);
		
		/*
		 * Calculates the distance.
		 * If the loop stopped after 38ms set the result to null,
		 * indicating that the update times out
		 */
		if ((stopTime - startTime) <= 38) {
			value = (stopTime - startTime) * 165.7;
		}
		else {
			System.out.println("Timed out");
			value = null;
		}
	}
	
}
