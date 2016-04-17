package working;

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
	private String input;
	
	private BufferedReader reader;
	private BufferedWriter writer;
	
	public BackUpData(String carDataFile) {
		this.carDataFile = carDataFile;
	}
	
	public void writeCarDataToFile(double doorLength, double rearDoorLength, double blindZoneValue, double topSpeed) {
		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(carDataFile)));
			try {
				writer.write(DOOR_LENGTH + ":" + doorLength + System.lineSeparator()
							+ REAR_DOOR_LENGTH + ":" + rearDoorLength + System.lineSeparator()
							+ BLIND_ZONE_VALUE + ":" + blindZoneValue + System.lineSeparator()
							+ TOP_SPEED + ":" + topSpeed);
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
					input = reader.readLine();
					String inputSplit[] = input.split(":");
					try {
						switch (inputSplit[0]) {
						case DOOR_LENGTH: backUpCarData.put(DOOR_LENGTH, Double.valueOf(inputSplit[1])); break;
						case REAR_DOOR_LENGTH: backUpCarData.put(REAR_DOOR_LENGTH, Double.valueOf(inputSplit[1])); break;
						case BLIND_ZONE_VALUE: backUpCarData.put(BLIND_ZONE_VALUE, Double.valueOf(inputSplit[1])); break;
						case TOP_SPEED: backUpCarData.put(TOP_SPEED, Double.valueOf(inputSplit[1])); break;
						default: System.out.println("No case found for: " + inputSplit[0]);
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
		return (backUpCarData.size() == 4) ? backUpCarData : null;
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