package demo;


public class CarData2 implements BaseInterface {
	
	/*
	 * CarData collects continous data from the car (simulated)
	 */
	
	private double doorLength, rearDoorLength, blindZoneValue, frontDistParking;
	
//	Simulated variables used by the private SimulateCar class
	private int currentSpeed;
	private boolean rightTurnLight, leftTurnLight;
	
	public CarData2(double doorLength, double rearDoorLength, double blindZoneValue, double frontDistParking) {
		this.doorLength = doorLength;
		this.rearDoorLength = rearDoorLength;
		this.blindZoneValue = blindZoneValue;
		this.frontDistParking = frontDistParking;
	}
	
	public CarData2() {
		
	}
	
	public boolean isReady() {
		return 0 <= doorLength && 0 <= rearDoorLength && 0 <= blindZoneValue && 0 <= frontDistParking;
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
	
	public double getFrontDistParking() {
		return frontDistParking;
	}
	
	public void setFrontDistParking(double frontDistParking) {
		this.frontDistParking = frontDistParking;
	}
	
	
	//	Returns a double value in km/h
	public int getCarSpeed() {
		return currentSpeed;
	}
	
	public boolean isLeftTurnLightOn() {
		return leftTurnLight;
	}
	
	public boolean isRightTurnLightOn() {
		return rightTurnLight;
	}

	public void updateValue(String carValue, double value) {
		switch (carValue) {
		case DOOR_LENGTH: setDoorLength(value); break;
		case REAR_DOOR_LENGTH: setRearDoorLength(value); break;
		case BLIND_ZONE_VALUE: setBlindZoneValue(value); break;
		case FRONT_PARK_DISTANCE: setFrontDistParking(value); break;
		}
		
	}
}