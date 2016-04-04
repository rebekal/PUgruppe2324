package software_v2;

import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

public class UltrasonicTesting extends Sensor {
	
	private GpioPinDigitalOutput triggerPinOutput;
	private GpioPinDigitalInput echoPinInput;
	
	private Double value;
	
	public UltrasonicTesting(String sensorName, String triggerPin, String echoPin) {
		super(sensorName, triggerPin, echoPin);
	}
	
	@Override
	protected void setup() {
		triggerPinOutput = gpc.provisionDigitalOutputPin(RaspiPin.getPinByName(pin1));
		echoPinInput = gpc.provisionDigitalInputPin(RaspiPin.getPinByName(pin2));
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
			triggerPinOutput.high();
			Thread.sleep(20);
		} catch (InterruptedException e) {
			System.out.println("InterruptedExeption - ultrasonic update");
		}
		
		triggerPinOutput.low();
//		Waiting for the result
		
		double startTime = System.currentTimeMillis(), stopTime = 0;
		do {
			stopTime = System.currentTimeMillis();
			if ((System.currentTimeMillis() - startTime) >= 40) {
				break;
			}
		} while (echoPinInput.getState() != PinState.HIGH);
		
		/*
		 * Calculates the distance.
		 * If the loop stopped after 38ms set the result to null,
		 * indicating that the update times out
		 */
		if ((stopTime - startTime) <= 38) {
			value = (stopTime - startTime) * 165.7;
		}
		else {
			System.out.println("Ultrasonic sensor: Timed out");
			value = null;
		}
	}
	
	public static void main(String[] args) {
		UltrasonicTesting ucFront = new UltrasonicTesting("Front", "23", "24");
		UltrasonicTesting ucLeft = new UltrasonicTesting("Left", "16", "26");
		UltrasonicTesting ucRight = new UltrasonicTesting("Right", "17", "27");
		while (true) {
			System.out.println("Front = " + ucFront.getValue(true));
			System.out.println("Left = " + ucLeft.getValue(true));
			System.out.println("Right = " + ucRight.getValue(true));
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
			}
		}
	}
	
}
