package network;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

import logic.*;

public class Evaluation {

	private static final boolean DEBUG = false;
	private Node node;
	// 1st Metric Vars
	private ConcurrentHashMap<Integer, Instant> electionInit = new ConcurrentHashMap<Integer, Instant>();
	private ConcurrentHashMap<Integer, Instant> electionEnd = new ConcurrentHashMap<Integer, Instant>();
	private Duration timeElapsed;

	private ConcurrentHashMap<Integer, Integer> mapMsgOverhead = new ConcurrentHashMap<Integer, Integer>();
	private int msgSentInElection;

	public Evaluation(Node node) {
		this.node = node;
	}

	// 1st Metric - Election Time
	public void setStart(int id) {
		electionInit.put(id, Instant.now());
	}

	public void setEnd(int id) {
		electionEnd.put(id, Instant.now());
	}

	public void getTimer(int id) {

		if (!(electionInit.containsKey(id))) {
			if (DEBUG)
				System.out.println("No Election was started with that ID");
			return;
		}
		if (!(electionEnd.containsKey(id))) {
			if (DEBUG)
				System.out.println("Election wasn't finished yet Or it was left behind for a stronger leader");
			return;
		}

		this.timeElapsed = Duration.between(electionInit.get(id), electionEnd.get(id));
		System.out.println("Time taken: " + timeElapsed.toMillis() + " milliseconds");
	}

	// 2nd Metric - Overhead Messages Sent By Node
	public void resetMsgCounter() {
		msgSentInElection = 0;
	}

	public void addMsgCounter(int id) {

		resetMsgCounter();

		if (mapMsgOverhead.containsKey(id)) {
			msgSentInElection = mapMsgOverhead.get(id);
			mapMsgOverhead.remove(id, msgSentInElection);
		}
		msgSentInElection++;
		mapMsgOverhead.put(id, msgSentInElection);
	}

	public void getMsgOverhead(int id) {
		System.out.println("Msg Overhead in Election " + id + " = " + mapMsgOverhead.get(id));
	}

	// 3rd Metric - Time Without Leader

	// 4th

	// Storage Facility
	public void storeElectionTime() throws IOException {
		String textToAppend = "Election in " + Instant.now() + "  ===>  " + timeElapsed.toMillis() + " ms.";

		BufferedWriter writer = new BufferedWriter(new FileWriter("..\\Statistics\\electionTime.txt", true) // AppendMode
		);

		writer.newLine(); // Add new line
		writer.write(textToAppend);
		writer.close();
	}

	public double runningAvg() {
		return 0;
	}
}
