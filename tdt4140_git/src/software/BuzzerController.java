package software;

import java.util.Map;

public class BuzzerController {

	private final Map<String, Buzzer> buzzers;
	
	public BuzzerController(Map<String, Buzzer> buzzers) {
		this.buzzers = buzzers;
	}
	
	public void toggleLED(String led) {
		buzzers.get(led).toggle();
	}
	
	public void ledOff(String buzzer) {
		if (buzzers.get(buzzer).makingSound()) {
			buzzers.get(buzzer).setLow();		
		}
	}
	
	public void ledOn(String led) {
		if (! buzzers.get(led).makingSound()) {
			buzzers.get(led).setHigh();			
		}
	}
	
	public void alarmSound() {
		
	}
}
