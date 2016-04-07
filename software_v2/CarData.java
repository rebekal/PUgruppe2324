package software;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class CarData implements BaseInterface {
	
	/*
	 * CarData collects continous data from the car (simulated)
	 */
	
	public final double doorLength, blindZoneValue;
	public final int topSpeed;
	
	private boolean engineMode;
	
	
//	Simulated variables used by the private SimulateCar class
	private int currentSpeed;
	private boolean rightTurnLight, leftTurnLight;
	
	public CarData(double doorLength, double blindZoneValue, double topSpeed) {
		if (! isValidFixedDistances(doorLength, blindZoneValue, topSpeed)) {
			throw new IllegalArgumentException("Fixed value(s) not valid");
		}
		this.doorLength = doorLength;
		this.blindZoneValue = blindZoneValue;
		this.topSpeed = (int) topSpeed;
		
//		Simulate data
		random = new Random();
		timer = 0;
		speedValues = createSpeedValues(this.topSpeed);
		speedLimits = new ArrayList<Integer>(Arrays.asList(30, 40, 50, 60, 70, 80, 90, 100, 110));
		currentSpeedLimit = getSpeedLimit(0);
	}
	
	private static boolean isValidFixedDistances(double doorLength, double blindZoneValue, double topSpeed) {
		return 0 <= doorLength && 0 <= blindZoneValue && 0 <= topSpeed;
	}
	
	public boolean getEngineMode() {
		return engineMode;
	}
	
	public void setEngineMode(boolean engineMode) {
		this.engineMode = engineMode;
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
	
	private final List<Integer> speedValues, speedLimits;
	private Random random;
	public final int TIME_OUT = 120;
	
	private int currentSpeedLimit, nextSpeedLimit;
	private boolean newSpeedLimit;
	
	private boolean redTrafficLight;
	private int redTrafficLightCountDown, redTrafficLightCD;

	private int leftCount, rightCount, turnLightMaxCount;
	private boolean turnLightJustUsed;
	
	private int index, timer, acceleration, accelerationCount;
	
	public boolean getRedTrafficLight() {
		return redTrafficLight;
	}
	
	public int getRedTrafficLightCount() {
		return redTrafficLightCountDown;
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
		
		changeCarSpeed(redTrafficLight);
		
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
	private void changeCarSpeed(boolean trafficLightIsRed) {
		if (0 < redTrafficLightCountDown && 0 < currentSpeed) {
			index--;
		}
		else if (trafficLightIsRed) {
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
		if (trafficLightIsRed) {
			return 50;
		}
		else if (rightAfterTurnLight()) {
			turnLightJustUsed = false;						// ***
			accelerationCount = turnLightMaxCount;
			return 85;
		}
		else if (atEndOfTurnLight()) {
			turnLightJustUsed = true;
			accelerationCount = turnLightMaxCount;
			return 85;
		}
		else if (currentlyUsingTurnLight()) {
			accelerationCount = turnLightMaxCount;
			return 750;
		}
		else if (currentSpeed < currentSpeedLimit || currentSpeed > currentSpeedLimit) {
			accelerationCount = Math.abs(currentSpeedLimit - currentSpeed);
			return 100;
		}
		return 500; // *
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

	

}
