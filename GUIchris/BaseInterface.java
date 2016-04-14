package software;

public interface BaseInterface {
	
//	Sensor
	public final String FRONT = "front", LEFT = "left", RIGHT = "right", REAR = "rear";
	
//	LED	
	public final String RED = "red", YELLOW = "yellow", GREEN = "green";
	public final String RED_FRONT = "red front", GREEN_FRONT = "green front";
	public final String RED_LEFT = "red left", GREEN_LEFT = "green left";
	public final String RED_RIGHT = "red right", GREEN_RIGHT = "green right";
	public final String RED_REAR = "red rear", GREEN_REAR = "green rear";
	
//	Info
	public final String INPUT = "input", SENSOR = "sensor";
	
//	Weather, road conditions
	public final String DRY_ASPHALT = "dry asphalt", WET_ASPHALT = "wet asphalt", SNOW = "snow", ICE = "ice";
	
// Modes
	public final String PARKING = "Parking", DRIVING = "Driving";
	
}
