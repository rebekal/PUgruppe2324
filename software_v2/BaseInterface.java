package software;

public interface BaseInterface {
	
	final String carDataFile = "D:\\Dokumenter\\GitHub\\PUgruppe2324\\tdt4140_java_v2\\src\\simulate\\carData";
	
//	Sensor
//	final String FRONT = "front", LEFT = "left", RIGHT = "right", REAR = "rear";
	final String FRONT = "/home/aproxym8/sensor_front.py", LEFT = "/home/aproxym8/sensor_left.py", RIGHT = "/home/aproxym8/sensor_right.py", REAR = "/home/aproxym8/sensor_rear.py";
	
//	LED ***
	final String RED = "red", YELLOW = "yellow", GREEN = "green";
	final String RED_FRONT = "red front", GREEN_FRONT = "green front";
	final String RED_LEFT = "red left", GREEN_LEFT = "green left";
	final String RED_RIGHT = "red right", GREEN_RIGHT = "green right";
	final String RED_REAR = "red rear", GREEN_REAR = "green rear";
	
//	Buzzer
	final String ALARM = "alarm";
	
//	Weather/road conditions
	final String DRY_ASPHALT = "dry asphalt", WET_ASPHALT = "wet asphalt", SNOW = "snow", ICE = "ice";
	
//  Modes
	final String DRIVING_MODE = "driving mode", BLIND_ZONE_MODE = "blind zone", PARKING_MODE = "parking mode";
	final String SIMULATE_MODE = "simulate mode", RUN_PROGRAM = "run program";
	
//	Car
	final String DOOR_LENGTH = "door length", REAR_DOOR_LENGTH = "rear door length", BLIND_ZONE_VALUE = "blind zone value";
	final String TOP_SPEED = "car top speed", ENGINE_MODE = "engine off";
}
