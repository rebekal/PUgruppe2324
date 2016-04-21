package release;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

public class BackUpData implements BaseInterface {
	
	public final String carDataFile;
	
	private BufferedReader reader;
	private BufferedWriter writer;
	
	public BackUpData(String carDataFile) {
		this.carDataFile = carDataFile;
	}
	
	public void writeCarDataToFile(double doorLength, double rearDoorLength, double blindZoneValue, double topSpeed, double frontDistParking) {
		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(carDataFile)));
			try {
				writer.write(DOOR_LENGTH + ":" + doorLength + System.lineSeparator()
							+ REAR_DOOR_LENGTH + ":" + rearDoorLength + System.lineSeparator()
							+ BLIND_ZONE_VALUE + ":" + blindZoneValue + System.lineSeparator()
							+ TOP_SPEED + ":" + topSpeed + System.lineSeparator()
							+ FRONT_PARK_DISTANCE + ":" + frontDistParking);
			} catch (IOException e) {
				System.out.println("An error occurred when writing to file: " + carDataFile);
			} finally {
				writer.close();
				System.out.println("Data was successfully written to file: " + carDataFile);
			}
		} catch (FileNotFoundException e) {
			System.out.println("File not found.");
		} catch (IOException e) {
			System.out.println("An IO exception occurred.");
		}
	}
	
	public Map<String, Double> readCarDataFromFile() {
		Map<String, Double> backUpCarData = new HashMap<String, Double>();
		try {
			try {
				reader = new BufferedReader(new InputStreamReader(new FileInputStream(carDataFile)));
				
				while (reader.ready()) {
					String input = reader.readLine();
					String inputSplit[] = input.split(":");
					String carValue = inputSplit[0];
					String value = inputSplit[1];
					try {
						switch (carValue) {
						case DOOR_LENGTH: backUpCarData.put(DOOR_LENGTH, Double.valueOf(value)); break;
						case REAR_DOOR_LENGTH: backUpCarData.put(REAR_DOOR_LENGTH, Double.valueOf(value)); break;
						case BLIND_ZONE_VALUE: backUpCarData.put(BLIND_ZONE_VALUE, Double.valueOf(value)); break;
						case TOP_SPEED: backUpCarData.put(TOP_SPEED, Double.valueOf(value)); break;
						case FRONT_PARK_DISTANCE: backUpCarData.put(FRONT_PARK_DISTANCE, Double.valueOf(value)); break;
						default: System.out.println("No case found for: " + carValue);
						}
					} catch (IndexOutOfBoundsException e) {
						System.out.println("Invalid line.");
						continue;
					}
				}
			} finally {
				reader.close();
				System.out.println("Data was successfully read from file: " + carDataFile);
			}
		} catch (IOException e) {
			System.out.println("An error occurred while reading from file: " + carDataFile);
		}
		return (backUpCarData.size() == 5) ? backUpCarData : null;
	}
	
	public void clearCarData() {
		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(carDataFile)));
			try {
				writer.write("");
			} catch (IOException e) {
				System.out.println("An error occurred when writing to file: " + carDataFile);
			} finally {
				writer.close();
				System.out.println("Data was successfully written to file: " + carDataFile);
			}
		} catch (FileNotFoundException e) {
			System.out.println("File not found.");
		} catch (IOException e) {
			System.out.println("An IO exception occurred.");
		}
	}
}