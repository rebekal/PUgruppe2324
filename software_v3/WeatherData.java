package final_software;

import java.util.HashMap;

public class WeatherData implements BaseInterface {
	
	public final HashMap<String, Double> weatherFrictionTable;
	
	private String frictionValue, location;

	public WeatherData() {
		weatherFrictionTable = new HashMap<String, Double>();
		insertWeatherFrictionValuesIntoLookUpTable();
		setUseFrictionValue(DRY_ASPHALT); // testing
	}
	
//	if not implementation for getting data from weatherservers aka. yr.no
	private void insertWeatherFrictionValuesIntoLookUpTable() {
		weatherFrictionTable.put(DRY_ASPHALT, 0.9);
		weatherFrictionTable.put(WET_ASPHALT, 0.05);
		weatherFrictionTable.put(SNOW, 0.3);
		weatherFrictionTable.put(ICE, 0.02);
	}
	
	public void setUseFrictionValue(String frictionValue) {
		this.frictionValue = frictionValue;
	}
	
	public double getFrictionValue() {
		return weatherFrictionTable.get(frictionValue);
	}
	
	
//	Weatherserver methods ***
	
	public void setLocation(String location) {
		this.location = location;
	}
	
	public String getLocation() {
		return location;
	}
	
	public double getWeatherCondition() {
		return 0;
	}
	
	public double getOutdoorTemperature() {
		return 0;
	}
	
	
	
	
}
