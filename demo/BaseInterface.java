package demo;

public interface BaseInterface {
	
//	Sensor
//	final String FRONT = "front", LEFT = "left", RIGHT = "right", REAR = "rear";
	final String FRONT = "/home/aproxym8/sensor_front.py", LEFT = "/home/aproxym8/sensor_left.py", RIGHT = "/home/aproxym8/sensor_right.py", REAR = "/home/aproxym8/sensor_rear.py";
	
//	Weather/road conditions
	final String WEATHER = "Weather", DRY_ASPHALT = "dry asphalt", WET_ASPHALT = "wet asphalt", SNOW = "snow", ICE = "ice";
	final String WINTER = "winter", SPRING = "spring", SUMMER = "summer", AUTUMN = "autumn";
	
//  Modes
	final String DRIVING_MODE = "driving mode", BLIND_ZONE_MODE = "blind zone", PARKING_MODE = "parking mode";
	final String SIMULATE_MODE = "simulate mode", RUN_PROGRAM = "run program";
	
//	Car
	final String DOOR_LENGTH = "Door", REAR_DOOR_LENGTH = "Rear door", BLIND_ZONE_VALUE = "Blind zone";
	final String CAR_SPEED = "Car speed", FRONT_PARK_DISTANCE = "Front park distance";
	final String LEFT_TURN_LIGHT = "Left turn light", RIGHT_TURN_LIGHT = "Right turn light";
	
//	User
	final String SMART_BRAKE = "Smart brake", BLINDSPOT_ALWAYS = "Blindspot always", AUDIO_ENABLED = "Audio enabled";
}