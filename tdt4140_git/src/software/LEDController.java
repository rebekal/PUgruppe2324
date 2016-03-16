package software;

import java.util.Map;

public class LEDController implements BaseInterface {
	
	private final Map<String, LED> LEDs;
	
	public LEDController(Map<String, LED> LEDs) {
		this.LEDs = LEDs;
	}
	
	public void toggleLED(String led) {
		LEDs.get(led).toggle();
	}
	
	public void ledOff(String led) {
		if (LEDs.get(led).isHigh()) {
			LEDs.get(led).setLow();			
		}
	}
	
	public void ledOn(String led) {
		if (! LEDs.get(led).isHigh()) {
			LEDs.get(led).setHigh();			
		}
	}

}
