package final_software;

import java.io.*;

public class PythonCaller {
 
	public String call(String filename) throws IOException {
		// set up the command and parameter
		String pythonScriptPath = "/home/norbert/python/helloPython.py";
		String[] cmd = new String[2];
		cmd[0] = "python"; // check version of installed python: python -V
		cmd[1] = pythonScriptPath;
		
		// create runtime to execute external command
		Runtime rt = Runtime.getRuntime();
		Process pr = rt.exec(cmd);
		
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

