package demo;

import java.io.*;

public class PythonCaller {
 
	public String call(String filename) throws IOException {
		// create runtime to execute external command
		Runtime rt = Runtime.getRuntime();
		Process pr = rt.exec(filename);
		
		// retrieve output from python script
		BufferedReader bfr = new BufferedReader(new InputStreamReader(pr.getInputStream()));
		String line = "";
		while((line = bfr.readLine()) != null) {
			// display each output line form python script
			return line;
		}
		return line;
	}
}