package software;

import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.RaspiPin;

public class Ultrasonic extends Sensor {
	
	private GpioPinDigitalOutput trigPin;
	private GpioPinDigitalInput ecPin;
	
	private Double value;
	
	public Ultrasonic(String sensorName, String triggerPin, String echoPin) {
		super(sensorName, triggerPin, echoPin);
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

	public Double getValue() {
		update();
		return value;
	}
	
	public void reset() {
		value = null;
	}
	
	public void update() {
		sendActivePulse();
		
		double readValue = 0; // ecPin ***
		
		double signalOffStart = System.currentTimeMillis();
		double signalOff = signalOffStart;
		
		while (readValue == 0 && ((signalOff - signalOffStart) < 0.5)) {
//			readValue
			signalOff = System.currentTimeMillis();
		}
		double signalOn = signalOff;
		
		while (readValue == 1) {
//			readValue
			signalOn = System.currentTimeMillis();
		}
		value = calculateDistance(signalOn, signalOff);
	}

	private void sendActivePulse() {
		try {
			trigPin.low();
			Thread.sleep(300);
			
			trigPin.high();
			Thread.sleep(10);
			
			trigPin.low();
			
		} catch (InterruptedException e) {
			System.out.println("Interrupted exception");
		}
	}
	
	private Double calculateDistance(double signalOn, double signalOff) {
		return (344 * (signalOn - signalOff) * 100 ) / 2;
	}


	

	
	
}
