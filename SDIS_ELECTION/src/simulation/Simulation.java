package simulation;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Random;

public class Simulation {

	private static final int nodeKillProb = 1;
	private static final boolean DEBUG = false;

	private boolean nodeKilled;
	private int dropPacketProbability;
	private int nodeKillProbability;

	// THERE'S AN ERROR IN INSTANTS
	protected Instant start = Instant.now();
	protected Instant end = Instant.now();
	protected Duration timeElapsed;

	public Simulation() {
		System.out.println("Hello from simulation");
		nodeKilled = false;
		this.dropPacketProbability = -1;
		this.nodeKillProbability = -1;
	}

	public Simulation(int dropPacketProbability) {
		System.out.println("Hello from simulation w/ drop Packet Probability");
		nodeKilled = false;
		this.dropPacketProbability = dropPacketProbability;
		this.nodeKillProbability = nodeKillProb; // %
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

	public boolean dropPacketRange(float range, float distance) {
		float Pdropped = 0, decisionN = 0;

		if (Pdropped == 0)
			return false;

		// No Drop Packet
		if ((dropPacketProbability == -1) && (nodeKillProbability == -1))
			return false;

		Random decisionMaker = new Random();

		// Probability calculation - Raw Method
		Pdropped = distance / range * 100;

		// Random Packet Dropout
		decisionN = decisionMaker.nextInt(100); // Numbers between 0 and 99

		if (DEBUG)
			System.out.println("Range>>> decisionN =  " + decisionN + " | Pdropped = " + Pdropped);

		if (decisionN < Pdropped) {
			return true; // Packet Dropped
		} else {
			return false;
		}
	}

	public boolean dropPacketRandom() {
		float decisionN = 0;

		// No Drop Packet
		if ((dropPacketProbability == -1) && (nodeKillProbability == -1))
			return false;

		Random decisionMaker = new Random();

		// Random Packet Dropout
		decisionN = decisionMaker.nextInt(100); // Numbers between 0 and 99

		if (DEBUG)
			System.out.println(
					"Random>>> decisionN =  " + decisionN + " | dropPacketProbability = " + dropPacketProbability);

		if (decisionN < dropPacketProbability) {
			return true; // Packet Dropped
		} else {
			return false;
		}
	}

	public void testNodeKill() {

		// No Kill
		if ((dropPacketProbability == -1) && (nodeKillProbability == -1)) {
			setNodeKilled(false);
			return;
		}

		Random decisionMaker = new Random();
		int decisionN;

		// Random Kill
		decisionN = decisionMaker.nextInt(100); // Numbers between 0 and 99

		if (DEBUG)
			System.out.println("Kill>>> decisionN = " + decisionN + " | nodeKillProbability = " + nodeKillProbability);

		if (decisionN < this.nodeKillProbability) {
			setNodeKilled(true);
		} else {
			setNodeKilled(false);

		}
	}

	public boolean isNodeKilled() {
		return nodeKilled;
	}

	public void setNodeKilled(boolean nodeKilled) {
		this.nodeKilled = nodeKilled;
	}
}
