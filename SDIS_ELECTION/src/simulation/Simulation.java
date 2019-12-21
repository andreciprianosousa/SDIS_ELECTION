package simulation;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Random;

public class Simulation {

	private static final boolean DEBUG = false;

	private int medianFailure = 100; // 100 Messages
	private int medianDeath = 5; // 5 minute
	private Instant nodeInit;
	private Instant nodeCharge;

	private boolean isToTestPacket;
	private boolean isToTestDeath;
	private boolean nodeKilled;

	// THERE'S AN ERROR IN INSTANTS
	protected Instant start = Instant.now();
	protected Instant end = Instant.now();
	protected Duration timeElapsed;

	protected Random decisionMaker = new Random();

	public Simulation() {
		if (DEBUG)
			System.out.println("Hello from simulation. No packet drop, No death!.");
		isToTestPacket = false;
		isToTestDeath = false;
	}

	public Simulation(int medianFailure, int medianDeath, Instant nodeInit) {
		if (DEBUG)
			System.out.println("Have Fun with the Simulation");

		if (medianFailure <= 0) {
			isToTestPacket = false;
		} else {
			isToTestPacket = true;
			this.medianFailure = medianFailure;
		}

		if (medianDeath <= 0) {
			isToTestDeath = false;
		} else {
			isToTestDeath = true;
			this.medianDeath = medianDeath;
		}

		this.nodeInit = nodeInit;
		this.nodeCharge = nodeInit;
	}

	public void setStart() {
		this.start = Instant.now();
	}

	public void setEnd() {
		this.end = Instant.now();
	}

	public void getTimer() {
		this.timeElapsed = Duration.between(start, end);
		System.out.println("Time taken: " + timeElapsed.toMillis() + " milliseconds");
	}

	public void storeElectionTime() throws IOException {
		String textToAppend = "Election in " + Instant.now() + "  ===>  " + timeElapsed.toMillis() + " ms.";

		BufferedWriter writer = new BufferedWriter(new FileWriter("..\\Statistics\\electionTime.txt", true) // AppendMode
		);

		writer.newLine(); // Add new line
		writer.write(textToAppend);
		writer.close();
	}

	public boolean meanTimeToHappenFailure(int messageCount) {
		double Probability = 0, aux = 0, decisionN = 0;

		// DecimalFormat df5 = new DecimalFormat("#.#####");

		if (!isToTestPacket()) {
			if (DEBUG)
				System.out.println("No Test Packet!");
			return false;
		}

		// mean Time to Happen => P("event") = 1 - 2^[-(tc/tmedian)]
		aux = -(1.0) * ((double) messageCount / (double) medianFailure);
		Probability = (double) (1 - Math.pow(2, aux)) * 100;

		decisionN = decisionMaker.nextInt(100); // Numbers between 0 and 99

		if (DEBUG)
			System.out.println(
					"meanTimeToHappenFailure >>> decisionN =  " + decisionN + " | Probability = " + Probability);

		if (decisionN < Probability) {
			return true; // Packet Dropped
		} else {
			return false;
		}
	}

	public boolean meanTimeToDie(Instant newInstant) {
		double Probability = 0, decisionN = 0, aux = 0, timeSpent = 0;

		if (!isToTestDeath) {
			if (DEBUG)
				System.out.println("No Death for Nodes!");
			return false;
		}

		timeSpent = Duration.between(nodeCharge, newInstant).toMillis();
		aux = -(1.0) * ((double) timeSpent / (double) (medianDeath * 60 * 1000));
		Probability = (double) (1 - Math.pow(2, aux)) * 100;

		decisionN = decisionMaker.nextInt(100); // Numbers between 0 and 99

		if (DEBUG)
			System.out.println("meanTimeToDie >>> decisionN =  " + decisionN + " | Probability = " + Probability);

		if (decisionN < Probability) {
			setNodeKilled(true);
			return true; // Node death
		} else {
			setNodeKilled(false);
			return false;
		}
	}

	public void resetCharge(Instant upTo100) {
		this.nodeCharge = upTo100;
	}

	public boolean isNodeKilled() {
		return nodeKilled;
	}

	public void setNodeKilled(boolean nodeKilled) {
		this.nodeKilled = nodeKilled;
	}

	public boolean isToTestPacket() {
		return isToTestPacket;
	}

	public void setToTestPacket(boolean isToTestPacket) {
		this.isToTestPacket = isToTestPacket;
	}

	public boolean isToTestDeath() {
		return isToTestDeath;
	}

	public void setToTestDeath(boolean isToTestDeath) {
		this.isToTestDeath = isToTestDeath;
	}

	public int getMedianfailure() {
		return medianFailure;
	}

	public int getMediandeath() {
		return medianDeath;
	}

}