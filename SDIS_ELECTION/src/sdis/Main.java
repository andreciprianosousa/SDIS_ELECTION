package sdis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Main {
	
	// ----------------------------------ONLY CHANGE HERE - SETUP ---------------------------------------
	/**
	 * Provide absolute JAVA file path, change name of file accordingly
	 */
	private static final String JAVAC_FILE_LOCATION = System.getProperty("user.home")
			+ "/SDIS_ELECTION/SDIS_ELECTION/src/sdis/Test.java";
	
	private static final String JAVA_FILE_LOCATION = System.getProperty("user.home")
			+ "/SDIS_ELECTION/SDIS_ELECTION/src/";
	
	private static final String PACKAGE_CLASS = "sdis.Test";
	

	// ----------------------------------END SETUP-------------------------------------------
	private static void print(String status, InputStream input) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(input));
		System.out.println("************* " + status + "***********************");
		String line = null;
		while ((line = in.readLine()) != null) {
			System.out.println(line);
		}
		in.close();
	}

	private static void newProcess(String argument) throws IOException, InterruptedException {
		// Compile Program first
		String command[] = { "javac", JAVAC_FILE_LOCATION };
		ProcessBuilder processBuilder = new ProcessBuilder(command);

		Process process = processBuilder.start();
		process.waitFor(); // Wait for compilation

		/**
		 * Check if any errors or compilation errors encounter then print on Console.
		 */

		if (process.getErrorStream().read() != -1) {
			print("Compilation Errors", process.getErrorStream());
		}
		/**
		 * Check if javac process execute successfully or Not 0 - successful
		 */
		if (process.exitValue() == 0) {
			process = new ProcessBuilder(
					new String[] { "java", "-cp", JAVA_FILE_LOCATION, PACKAGE_CLASS, argument })
							.start();
			/**
			 * Check if RuntimeException or Errors encounter during execution then print
			 * errors on console Otherwise print Output
			 */
			if (process.getErrorStream().read() != -1) {
				print("Errors ", process.getErrorStream());
			} else {
				print("Output ", process.getInputStream());
			}
		}
	}
	
	public static void main(String args[]) throws IOException, InterruptedException {
		
		String argument = "2";
		
		newProcess(argument);
	}

}
