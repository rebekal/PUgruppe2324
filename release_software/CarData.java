package release;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class CarData implements BaseInterface {
	
	/*
	 * CarData collects continous data from the car (simulated)
	 */
	
	private double doorLength, rearDoorLength, blindZoneValue, frontDistParking;
	private int topSpeed;
	
//	Simulated variables used by the private SimulateCar class
	private int currentSpeed;
	private boolean rightTurnLight, leftTurnLight;
	
	public CarData(double doorLength, double rearDoorLength, double blindZoneValue, double topSpeed, double frontDistParking) {
		this.doorLength = doorLength;
		this.rearDoorLength = rearDoorLength;
		this.blindZoneValue = blindZoneValue;
		this.topSpeed = (int) topSpeed;
		this.frontDistParking = frontDistParking;
		
//		Simulate data
		random = new Random();
		timer = 0;
		speedValues = createSpeedValues(this.topSpeed);
		speedLimits = getSpeedLimits();
		currentSpeedLimit = getSpeedLimit(0);
	}
	
	public CarData() {
		random = new Random();
		timer = 0;
		speedLimits = getSpeedLimits();
		currentSpeedLimit = getSpeedLimit(0);
	}

	private ArrayList<Integer> getSpeedLimits() {
		return new ArrayList<Integer>(Arrays.asList(30, 40, 50, 60, 70, 80, 90, 100, 110));
	}
	
	public boolean isReady() {
		return 0 <= doorLength && 0 <= rearDoorLength && 0 <= blindZoneValue && 0 < topSpeed && 0 <= frontDistParking;
	}
	
	public double getDoorLength() {
		return doorLength;
	}

	public void setDoorLength(double doorLength) {
		this.doorLength = doorLength;
	}

	public double getRearDoorLength() {
		return rearDoorLength;
	}

	public void setRearDoorLength(double rearDoorLength) {
		this.rearDoorLength = rearDoorLength;
	}

	public double getBlindZoneValue() {
		return blindZoneValue;
	}

	public void setBlindZoneValue(double blindZoneValue) {
		this.blindZoneValue = blindZoneValue;
	}

	public int getTopSpeed() {
		return topSpeed;
	}

	public void setTopSpeed(int topSpeed) {
		this.topSpeed = topSpeed;
	}
	
	public double getFrontDistParking() {
		return frontDistParking;
	}
	
	public void setFrontDistParking(double frontDistParking) {
		this.frontDistParking = frontDistParking;
	}
	
	
	//	Returns a double value in km/h
	public double getCarSpeed() {
		return currentSpeed;
	}
	
	public boolean isLeftTurnLightOn() {
		return leftTurnLight;
	}
	
	public boolean isRightTurnLightOn() {
		return rightTurnLight;
	}

	
//	*** Simulate Car Data ***
	
	private List<Integer> speedValues;
	private final List<Integer> speedLimits;
	private Random random;
	public final int TIME_OUT = 120;
	
	private int currentSpeedLimit, nextSpeedLimit;
	private boolean newSpeedLimit;
	
	private boolean redTrafficLight;
	private int redTrafficLightCountDown, redTrafficLightCD;

	private int leftCount, rightCount, turnLightMaxCount;
	private boolean turnLightJustUsed;
	
	private int index, timer, acceleration, accelerationCount;
	private boolean brake = false;
	
	public boolean getRedTrafficLight() {
		return redTrafficLight;
	}
	
	public int getRedTrafficLightCount() {
		return redTrafficLightCountDown;
	}
	
	public void setBrake(boolean brake) {
		this.brake = brake;
	}
	
	private List<Integer> createSpeedValues(int topSpeed) {
		List<Integer> values = new ArrayList<Integer>();
		for (int x=0; x <= topSpeed; x++) {
			values.add(x);
		}
		return values;
	}

	public void simulateOneStep() {
		if (TIME_OUT - timer == TIME_OUT / 6) {
			nextSpeedLimit = getSpeedLimit(currentSpeedLimit);
			newSpeedLimit = true;
		}
		if (timer == TIME_OUT) {
			currentSpeedLimit = nextSpeedLimit;
			timer = 0;
			newSpeedLimit = false;
		}
		
		if (! redTrafficLight && redTrafficLightCD == 0) {
			redTrafficLight = trafficLight();
		}
		else {
			redTrafficLightCD--;
		}
		if (0 < redTrafficLightCountDown) {
			redTrafficLightCountDown--;
			if (redTrafficLightCountDown == 0) {
				redTrafficLight = false;						
			}
		}
		
		changeCarSpeed();
		
		setLeftTurnLight();
		setRightTurnLight();				
		
		if (accelerationCount == 0) {
			acceleration = getCurrentAcceleration(redTrafficLight);					
		}
		try {
			Thread.sleep(acceleration);
			if (0 < accelerationCount) {
				accelerationCount--;						
			}
		} catch (InterruptedException e) {
		}
		currentSpeed = speedValues.get(index);
//		System.out.println(toString());
		timer++;
	}
	
	public void resetSimulation() {
		index = 0;
		timer = 0;
		acceleration = 0;
		accelerationCount = 0;
		brake = false;
		
		currentSpeed = 0;
		currentSpeedLimit = getSpeedLimit(0);
		nextSpeedLimit = 0;
		newSpeedLimit = false;
		
		redTrafficLight = false;
		redTrafficLightCountDown = 0;
		redTrafficLightCD = 0;
		
		leftTurnLight = false;
		leftCount = 0;
		rightTurnLight = false;
		rightCount = 0;
		turnLightMaxCount = 0;
		turnLightJustUsed = false;
	}
	
	private boolean trafficLight() {
		if (randomAction(25) && randomAction(85)) {
			redTrafficLightCountDown = random.nextInt(30);
			redTrafficLightCD = TIME_OUT * 2;
			return true;
		}
		return false;
	}

	private int getSpeedLimit(int currentSpeedLimit) {
		if (currentSpeedLimit == 0) {
			return speedLimits.get(random.nextInt(speedLimits.size()));
		}
		int result = 0, temp;
		for (int x=0; x<10; x++) {
			temp = random.nextInt(15);
			if (temp < 2) {
				temp = -10;
			}
			result += temp;
		}
		result = Math.abs(result);
		
		for (int x=0; x < speedLimits.size(); x++) {
			if (speedLimits.get(x) > result) {
				return speedLimits.get(x);
			}
		}
		return speedLimits.get(random.nextInt(speedLimits.size()));
	}
	
	public int getSpeedLimit() {
		return currentSpeedLimit;
	}

	/*
	 * Moving index pointer in the list of car speed values continuously
	 * - the car's max speed have been reached -> stay at current speed or reduce speed
	 * - either of the turn lights have been used (in the end phase of turning) -> reduce speed
	 * - current speed != current speed limit -> increase / decrease to obtain correct speed
	 * - "random" -> increase / decrease / no change
	 * - red traffic light -> reduce speed and stop, wait untill ready to go again
	 */
	private void changeCarSpeed() {
		if (0 < currentSpeed && (0 < redTrafficLightCountDown || brake)) {
			index--;
		}
		else if (redTrafficLight) {
			return;
		}
		else if (index == 0) {
			if (randomAction(50)) {
				return;
			}
			index++;
		}
		else if (index == speedValues.size() - 1) {
			if (randomAction(50)) {
				return;
			}
			index--;
		}
		else if (isLeftTurnLightOn() && (turnLightMaxCount - leftCount < turnLightMaxCount / 2.5)) {
			index -= currentSpeed/7;
//				index--;
		}
		else if(isRightTurnLightOn() && (turnLightMaxCount - rightCount < turnLightMaxCount / 2.5)) {
			index -= currentSpeed/7;
//				index--;
		}
		else if (newSpeedLimit && currentSpeed > nextSpeedLimit) {
			index--;
		}
		else if (currentSpeed < currentSpeedLimit) {
			index++;
		}
		else if (currentSpeed > currentSpeedLimit) {
			index--;
		}
		else {
			if (randomAction(50)) {
				index++;
			}
			else if (currentSpeed != 0 && randomAction(90)) {
				index--;
			}
		}
	}
	
	/*
	 * Set the acceleration of the car
	 */
	private int getCurrentAcceleration(boolean trafficLightIsRed) {
		if (trafficLightIsRed || brake) {
			return 10;
		}
		else if (rightAfterTurnLight()) {
			turnLightJustUsed = false;						// ***
			accelerationCount = turnLightMaxCount;
			return 20;
		}
		else if (atEndOfTurnLight()) {
			turnLightJustUsed = true;
			accelerationCount = turnLightMaxCount;
			return 40;
		}
		else if (currentlyUsingTurnLight()) {
			accelerationCount = turnLightMaxCount;
			return 90;
		}
		else if (currentSpeed < currentSpeedLimit || currentSpeed > currentSpeedLimit) {
			accelerationCount = Math.abs(currentSpeedLimit - currentSpeed);
			return 50;
		}
		return 100; // *
	}

	private boolean currentlyUsingTurnLight() {
		return (isLeftTurnLightOn() && leftCount < turnLightMaxCount) || (isRightTurnLightOn() && rightCount < turnLightMaxCount);
	}
	
	private boolean atEndOfTurnLight() {
		return (isLeftTurnLightOn() && leftCount == turnLightMaxCount) || (isRightTurnLightOn() && rightCount == turnLightMaxCount);
	}
	
	private boolean rightAfterTurnLight() {
		return turnLightJustUsed;
	}

	public void setLeftTurnLight() {
		if (rightTurnLight) {
			leftTurnLight = false;
		}
		else if (leftTurnLight && leftCount < turnLightMaxCount) {
			leftCount++;
		}
		else {
			leftTurnLight = randomAction(99);
			leftCount = 0;
			if (leftTurnLight) {
				turnLightMaxCount = calculateTurnLightTime();
			}
		}
	}

	public void setRightTurnLight() {
		if (leftTurnLight) {
			rightTurnLight = false;
		}
		else if (rightTurnLight && rightCount < turnLightMaxCount) {
			rightCount++;
		}
		else {
			rightTurnLight = randomAction(99);
			rightCount = 0;
			if (rightTurnLight) {
				turnLightMaxCount = calculateTurnLightTime();
			}
		}
	}
	
	private int calculateTurnLightTime() {
		if (currentSpeed < 10) {
			return 3;
		}
		else if (currentSpeed < 50) {
			return 4;
		}
		return currentSpeed / 10;
	}
	
	public boolean randomAction(int minValue) {
		return minValue < random.nextInt(101);
	}
	
	private String convertToLight(boolean trafficLightRed) {
		return trafficLightRed ? "Red" : "Green";
	}
	
	@Override
	public String toString() {
		String result = "";
		result += "Timer: " + timer + System.lineSeparator()
				+ "Speed = " + getCarSpeed() + "km/h || SpeedLimit = " + currentSpeedLimit + "km/h";
		if (newSpeedLimit) {
			result += " || NextSpeedLimit= " + nextSpeedLimit + "km/h";	
		}
		result += System.lineSeparator()
				+ "AccelerationCount = " + accelerationCount + System.lineSeparator()
				+ "Left turn light   = " + isLeftTurnLightOn() + System.lineSeparator()
				+ "Right turn light  = " + isRightTurnLightOn() + System.lineSeparator()
				+ "LeftCount  = " + leftCount + "/" + turnLightMaxCount + System.lineSeparator()
				+ "RightCount = " + rightCount + "/" + turnLightMaxCount + System.lineSeparator()
				+ "Turn light just used = " + rightAfterTurnLight() + System.lineSeparator()
				+ "TrafficLight         = " + convertToLight(redTrafficLight) + System.lineSeparator()
				+ "TrafficLightRedCount = " + redTrafficLightCountDown + System.lineSeparator()
				+ "TrafficLightCooldown = " + redTrafficLightCD + "/" + (TIME_OUT * 2) + System.lineSeparator();
		return result;
	}

	public void updateValue(String carValue, double value) {
		switch (carValue) {
		case DOOR_LENGTH: setDoorLength(value); break;
		case REAR_DOOR_LENGTH: setRearDoorLength(value); break;
		case BLIND_ZONE_VALUE: setBlindZoneValue(value); break;
		case TOP_SPEED: setTopSpeed((int) value); speedValues = createSpeedValues((int) value); break;
		case FRONT_PARK_DISTANCE: setFrontDistParking(value); break;
		}
		
	}
}