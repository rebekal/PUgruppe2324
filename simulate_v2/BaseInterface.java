package simulate;

public interface BaseInterface {
	
	public final String carDataFile = "D:\\Programfiler (x86)\\Eclipse\\Workspace\\tdt4140\\src\\software_v2_simulate_v2\\carData";
	
//	Sensor
	public final String FRONT = "front", LEFT = "left", RIGHT = "right", REAR = "rear";
	
//	LED ***
	public final String RED = "red", YELLOW = "yellow", GREEN = "green";
	public final String RED_FRONT = "red front", GREEN_FRONT = "green front";
	public final String RED_LEFT = "red left", GREEN_LEFT = "green left";
	public final String RED_RIGHT = "red right", GREEN_RIGHT = "green right";
	public final String RED_REAR = "red rear", GREEN_REAR = "green rear";
	
//	Buzzer
	public final String ALARM = "alarm";
	
//	Weather/road conditions
	public final String DRY_ASPHALT = "dry asphalt", WET_ASPHALT = "wet asphalt", SNOW = "snow", ICE = "ice";
	
//  Modes
	public final String DRIVING_MODE = "driving mode", BLIND_ZONE_MODE = "blind zone", PARKING_MODE = "parking mode";
	public final String SIMULATE_MODE = "simulate mode", RUN_PROGRAM = "run program";
	
//	Car
	public final String DOOR_LENGTH = "door length", REAR_DOOR_LENGTH = "rear door length", BLIND_ZONE_VALUE = "blind zone value";
	public final String TOP_SPEED = "car top speed", ENGINE_MODE = "engine off";
}
