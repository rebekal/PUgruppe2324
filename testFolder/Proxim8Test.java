package testing;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import software.MainController;
import software.PythonCaller;
import software.Ultrasonic;
import software.UltrasonicController;


public class Proxim8Test {
	
	final String FRONT_SENSOR = "/home/proxim8/sensor1.py";
	final String LEFT_SENSOR = "/home/proxim8/sensor2.py";
	final String RIGHT_SENSOR = "/home/proxim8/sensor3.py";
	final double blindZoneValue = 150;
	final double doorLength = 120;
	
	private UltrasonicController uc;
	
	@Before
	public void setUp() {
		PythonCaller pyCaller = new PythonCaller();
		Ultrasonic frontSensor = new Ultrasonic(FRONT_SENSOR, pyCaller);
		Ultrasonic leftSensor = new Ultrasonic(LEFT_SENSOR, pyCaller);
		Ultrasonic rightSensor = new Ultrasonic(RIGHT_SENSOR, pyCaller);
		Map<String, Ultrasonic> sensors = new HashMap<String, Ultrasonic>();
		sensors.put(FRONT_SENSOR, frontSensor);
		sensors.put(LEFT_SENSOR, leftSensor);
		sensors.put(RIGHT_SENSOR, rightSensor);
		uc = new UltrasonicController(sensors);
	}
	
	@Test
	public void testFrontDistance() {
//		Update sensor value while having a object ("car") 25cm away from the sensor
		Double frontDist = uc.getSensorValue(FRONT_SENSOR, true);
		assertEquals(Double.valueOf(25), frontDist);
//		Do not update sensor value
		frontDist = uc.getSensorValue(FRONT_SENSOR, false);
		assertEquals(Double.valueOf(25), frontDist);
//		Reset sensor value (sets value == null), get sensor value w/o updating
		uc.getSensor(FRONT_SENSOR).reset();
		assertEquals(null, uc.getSensorValue(FRONT_SENSOR, false));
	}
	
	@Test
	public void testFrontDistanceOutsideRange() {
//		if the sensor times out it'll return null -> distance OK
//		testing sensor outside its range
		Double frontDistance = uc.getSensorValue(FRONT_SENSOR, true);
		assertEquals(null, frontDistance);
	}
	
	@Test
	public void testLeftDistance() {
		Double leftDist = uc.getSensorValue(LEFT_SENSOR, true);
		assertEquals(Double.valueOf(10), leftDist);
	}
	
	@Test
	public void testRightDistance() {
		Double rightDist = uc.getSensorValue(RIGHT_SENSOR, true);
		assertEquals(Double.valueOf(5), rightDist);
	}
	
	@Test
	public void testBrakeDistanceMethod() {
//		Calculate brake distance with carspeed == 80 km/h and friction value == 0.9 (dry asphalt)
		double brakeDist = MainController.getBrakeDistance(80, 0.9);
		assertEquals(Double.valueOf(50.18842227284104), Double.valueOf(brakeDist));
	}
	
	@Test
	public void testDriveAndParkingMethod() {
		Double leftDist = uc.getSensorValue(LEFT_SENSOR, true);
		boolean leftParkingSpaceOK = MainController.isSensorValueLargerThan(leftDist, doorLength);
		assertEquals(true, leftParkingSpaceOK);
	}
	
	@Test
	public void testBlindZoneMethod() {
//		Testing while a object ("car") is in the left blind zone
		Double leftSensor = uc.getSensorValue(LEFT_SENSOR, true);
		boolean isObjectInLeftBlindZone = MainController.isObjectInBlindZone(leftSensor, blindZoneValue);
		assertEquals(true, isObjectInLeftBlindZone);
		
//		Testing while all objects are outside the blind zone
		Double rightSensor = uc.getSensorValue(RIGHT_SENSOR, true);
		boolean isObjectInRightBlindZone = MainController.isObjectInBlindZone(rightSensor, blindZoneValue);
		assertEquals(false, isObjectInRightBlindZone);
	}
	
	@Test
	public void testBlindZoneMethodOutSideSensorRange() {
//		Testing while no objects are in the sensors range -> return null -> false
		Double rightSensor = uc.getSensorValue(RIGHT_SENSOR, true);
		boolean isObjectInRightBlindZone = MainController.isObjectInBlindZone(rightSensor, blindZoneValue);
		assertEquals(false, isObjectInRightBlindZone);
	}
}
