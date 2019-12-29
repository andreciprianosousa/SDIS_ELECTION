package simulation;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Simulation {

	private static final boolean DEBUG = false;

	private int medianFailure = 100; // 100 Messages
	private int medianDeath = 5; // 5 minute
	private Instant nodeInit;
	private Instant nodeCharge;

	private boolean isToTestPacket;
	private boolean takeDecisionFailure;
	private double decisionFailure;
	private boolean isToTestDeath;
	private boolean nodeKilled;
	private boolean takeDecisionDeath;
	private double decisionDeath;

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

		this.decisionFailure = 0;
		this.decisionDeath = 0;
		this.takeDecisionFailure = true;
		this.takeDecisionDeath = true;
		this.nodeInit = nodeInit;
		this.nodeCharge = nodeInit;
	}

	// Simulation - Drop Packets following Mean Time To Happen
	public boolean meanTimeToHappenFailure(int messageCount) {
		double Probability, aux = 0;

		// DecimalFormat df5 = new DecimalFormat("#.#####");
		if (!isToTestPacket()) {
			if (DEBUG)
				System.out.println("No Test Packet!");
			return false;
		}

		if (takeDecisionFailure == true) {
			decisionFailure = decisionMaker.nextInt(100); // Numbers between 0 and 99
			takeDecisionFailure = false;
		}

		// mean Time to Happen => P("event") = 1 - 2^[-(tc/tmedian)]
		aux = -(1.0) * ((double) messageCount / (double) medianFailure);
		Probability = (double) (1 - Math.pow(2, aux)) * 100;

		if (DEBUG)
			System.out.println(
					"meanTimeToHappenFailure >>> decisionN =  " + decisionFailure + " | Probability = " + Probability);

		if (decisionFailure < Probability) {
			takeDecisionFailure = true;
			return true; // Packet Dropped
		} else {
			return false;
		}
	}

	// Simulation - Node Death following Mean Time To Happen
	public boolean meanTimeToDie(Instant newInstant) {
		double Probability, aux = 0, timeSpent = 0;

		if (!isToTestDeath) {
			if (DEBUG)
				System.out.println("No Death for Nodes!");
			return false;
		}

		if (takeDecisionDeath == true) {
			decisionDeath = decisionMaker.nextInt(100); // Numbers between 0 and 99
			takeDecisionDeath = false;
		}

		timeSpent = Duration.between(nodeCharge, newInstant).toMillis();
		aux = -(1.0) * ((double) timeSpent / (double) (medianDeath * 60 * 1000));
		Probability = (double) (1 - Math.pow(2, aux)) * 100;

		if (DEBUG)
			System.out.println("meanTimeToDie >>> decisionN =  " + decisionDeath + " | Probability = " + Probability);

		if (decisionDeath < Probability) {
			setNodeKilled(true);
			takeDecisionDeath = true;
			return true; // Node death
		} else {
			setNodeKilled(false);
			return false;
		}
	}

	public void resetCharge(Instant upTo100) {
		this.nodeCharge = upTo100;
	}

	// Getters and Setters
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