package release;

import java.util.Date;
import java.util.HashMap;
import java.util.Random;

public class WeatherData implements BaseInterface {
	
	public final HashMap<String, Double> weatherFrictionTable;
	private Random random;
	private Date date;
	
	private String frictionValue;
	private int TIME_OUT = 200;
	private int month, count;
	private boolean winter, spring, summer, autumn;

	public WeatherData() {
		random = new Random();
		date = new Date();
		weatherFrictionTable = new HashMap<String, Double>();
		insertWeatherFrictionValuesIntoLookUpTable();
		startUpUpdate();
	}

	private void insertWeatherFrictionValuesIntoLookUpTable() {
		weatherFrictionTable.put(DRY_ASPHALT, 0.9);
		weatherFrictionTable.put(WET_ASPHALT, 0.6);
		weatherFrictionTable.put(SNOW, 0.3);
		weatherFrictionTable.put(ICE, 0.15);
	}
	
	public void setUseFrictionValue(String frictionValue) {
		this.frictionValue = frictionValue;
	}
	
	public String getFrictionString() {
		return frictionValue;
	}
	
	public double getFrictionValue() {
		return weatherFrictionTable.get(frictionValue);
	}
	
	private void startUpUpdate() {
		month = date.getMonth();
		switch (month) {
		case 2: case 3: case 4: setSeasons(SPRING); break;
		case 5: case 6: case 7: setSeasons(SUMMER); break;
		case 8: case 9: case 10: setSeasons(AUTUMN); break;
		case 11: case 0: case 1: setSeasons(WINTER); break;
		}
		update();
	}

	public void update() {
		if (count == 0) {
			int weatherValue = random.nextInt(100);
			count = TIME_OUT;
			
			if (winter) {
				if (weatherValue < 5) {
					setUseFrictionValue(ICE);
				}
				else {
					setUseFrictionValue(SNOW);
				}
			}
			else if (spring || autumn) {
				if (weatherValue < 5) {
					setUseFrictionValue(ICE);
				}
				else if (weatherValue < 20) {
					setUseFrictionValue(SNOW);
				}
				else if (weatherValue < 95) {
					setUseFrictionValue(WET_ASPHALT);
				}
				else {
					setUseFrictionValue(DRY_ASPHALT);
				}
			}
			else if (summer) {
				if (weatherValue < 8) {
					setUseFrictionValue(WET_ASPHALT);
				}
				else {
					setUseFrictionValue(DRY_ASPHALT);
				}
			}
		}
		else {
			count--;
		}
	}
	
	private void setSeasons(String season) {
		switch (season) {
		case SPRING: autumn = false; summer = false; spring = true; winter = false; break;
		case SUMMER: autumn = false; summer = true; spring = false; winter = false; break;
		case AUTUMN: autumn = true; summer = false; spring = false; winter = false; break;
		case WINTER: autumn = false; summer = false; spring = false; winter = true; break;
		}
	}
	
	public void resetCount() {
		count = 0;
	}
	
	@Override
	public String toString() {
		return "Month: " + month + ", Value: " + frictionValue + ", Count: " + count;
	}
}
