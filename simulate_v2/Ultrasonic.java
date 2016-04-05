package simulate;

public class Ultrasonic extends Sensor {
	
	private Double value;
	
	public Ultrasonic(String sensorName, String triggerPin, String echoPin) {
		super(sensorName, triggerPin, echoPin);
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
		value = Double.valueOf(random.nextInt(50));
	}
	
}
