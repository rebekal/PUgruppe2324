package working;

public interface BaseInterface {
	
	final String carDataFile = "D:\\Programfiler (x86)\\Eclipse\\Workspace\\JavaFX\\src\\working\\carData";
	
//	Sensor
//	final String FRONT = "front", LEFT = "left", RIGHT = "right", REAR = "rear";
	final String FRONT = "/home/aproxym8/sensor_front.py", LEFT = "/home/aproxym8/sensor_left.py", RIGHT = "/home/aproxym8/sensor_right.py", REAR = "/home/aproxym8/sensor_rear.py";
	
//	Weather/road conditions
	final String DRY_ASPHALT = "dry asphalt", WET_ASPHALT = "wet asphalt", SNOW = "snow", ICE = "ice";
	
//  Modes
	final String DRIVING_MODE = "driving mode", BLIND_ZONE_MODE = "blind zone", PARKING_MODE = "parking mode";
	final String SIMULATE_MODE = "simulate mode", RUN_PROGRAM = "run program";
	
//	Car
	final String DOOR_LENGTH = "Door", REAR_DOOR_LENGTH = "Rear door", BLIND_ZONE_VALUE = "Blind zone";
	final String TOP_SPEED = "Top speed";
}