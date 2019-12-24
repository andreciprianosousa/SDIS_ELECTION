package network;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

import logic.*;

public class Evaluation {

	private static final boolean DEBUG = true;
	private static boolean toWrite = false;

	private Node node;
	// 1st Metric Vars
	private ConcurrentHashMap<Integer, Instant> electionInit = new ConcurrentHashMap<Integer, Instant>();
	private ConcurrentHashMap<Integer, Instant> electionEnd = new ConcurrentHashMap<Integer, Instant>();
	private Duration electionTimeElapsed;
	// 2nd Metric Vars
	private ConcurrentHashMap<Integer, Integer> mapMsgOverhead = new ConcurrentHashMap<Integer, Integer>();
	private int msgSentInElection;
	// 3rd Metric Vars
	private ConcurrentHashMap<Integer, Instant> withoutLeaderInit = new ConcurrentHashMap<Integer, Instant>();
	private ConcurrentHashMap<Integer, Instant> withoutLeaderEnd = new ConcurrentHashMap<Integer, Instant>();
	private Duration withoutLeaderTimeElapsed;

	public Evaluation(Node node) {
		this.node = node;
	}

	// 1st Metric - Election Time
	// Mean Time nodes are in election
	public void setStartElectionTimer(int id) {
		electionInit.put(id, Instant.now());
	}

	public void setEndElectionTimer(int id) {
		electionEnd.put(id, Instant.now());
	}

	public void getElectionTimer(int id) throws IOException {

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

		this.electionTimeElapsed = Duration.between(electionInit.get(id), electionEnd.get(id));
		System.out.println("Election _ Time taken: " + electionTimeElapsed.toMillis() + " milliseconds");

		if (toWrite)
			storeElectionTime(id);
	}

	// 2nd Metric - Overhead Messages Sent By Node in Election
	// Message Overhead (M) is the avg number of messages sent by a node in election
	public void counterMessagesInElection(int id, MessageType type) throws IOException {
		int newCounterValue;

		if (DEBUG) {
			System.out.println("Election: " + id + " | Message Type: " + type);
		}

		// If this election was not active, then reset the counter
		// Then as this is the first msg and put it in the Map
		// -> It's triggered only by election or ack msg
		// If there's already this election,
		// * and it's sent an Leader msg, election has terminated
		// * if it's other type, replace the old counter, with the updated
		if (!(mapMsgOverhead.containsKey(id))) {
			msgSentInElection = 0;

			if (DEBUG) {
				System.out.println("Setting new Counter");
			}

			synchronized (this) {
				msgSentInElection = 1;
				mapMsgOverhead.put(id, msgSentInElection);
			}
			return;

		} else {
			if (type == MessageType.LEADER) {
				if (DEBUG)
					System.out.println("Leader Message - Stop Counting. Deleting Counter.");

				synchronized (this) {
					msgSentInElection = mapMsgOverhead.get(id);
					newCounterValue = msgSentInElection + 1;
					mapMsgOverhead.replace(id, msgSentInElection, newCounterValue);
					msgSentInElection = newCounterValue;

					System.out.println("Msg Overhead in Election " + id + " = " + mapMsgOverhead.get(id));

					msgSentInElection = mapMsgOverhead.get(id);

					if (toWrite)
						storeMessageOverhead(id, msgSentInElection);

					mapMsgOverhead.remove(id, msgSentInElection);
				}
				return;
			} else {
				if (DEBUG)
					System.out.println("Updating Counter.");

				synchronized (this) {
					msgSentInElection = mapMsgOverhead.get(id);
					newCounterValue = msgSentInElection + 1;
					mapMsgOverhead.replace(id, msgSentInElection, newCounterValue);
					msgSentInElection = newCounterValue;
				}
				return;
			}
		}
	}

	// 3rd Metric - Time Without Leader
	// Frac. Time W/out Leader (F) is the fraction of sim time that a node is
	// involved in an election
	public void checkWithoutLeader() {
		if (DEBUG)
			System.out.println("Node = " + node.getNodeID() + " | Size = " + node.getNeighbors().size()
					+ " | MaxNeigh = " + node.getMaximumIdNeighbors() + " | Leader = " + node.getLeaderID());

		if ((node.getNeighbors().size() > 0) && (node.getMaximumIdNeighbors() > node.getLeaderID())) {
			if (!(node.getNetworkEvaluation().getWithoutLeaderInit().containsKey(node.getNodeID()))) {
				if (DEBUG)
					System.out.println("Starting Timer WL - Case 1");
				setStartWithoutLeaderTimer();
			}
		}
		if (((node.getNodeID() != node.getLeaderID())) && (!(node.getNeighbors().contains(node.getLeaderID())))) {
			if (!(node.getNetworkEvaluation().getWithoutLeaderInit().containsKey(node.getNodeID()))) {
				if (DEBUG)
					System.out.println("Starting Timer WL - Case 2");
				setStartWithoutLeaderTimer();
			}
		}
	}

	public void setStartWithoutLeaderTimer() {
		withoutLeaderInit.put(node.getNodeID(), Instant.now());
	}

	public void setEndWithoutLeaderTimer() {
		withoutLeaderEnd.put(node.getNodeID(), Instant.now());
	}

	public void getWithoutLeaderTimer() throws IOException {

		if (!(withoutLeaderInit.containsKey(node.getNodeID()))) {
			if (DEBUG)
				System.out.println("No Timer was started with that Node ID");

			withoutLeaderEnd.remove(node.getNodeID());
			return;
		}
		if (!(withoutLeaderEnd.containsKey(node.getNodeID()))) {
			if (DEBUG)
				System.out.println("Timer wasn't finished yet Or it was left behind");

			// Fail Safe Case... In case that it's initiated and never finished
			if (Duration.between(withoutLeaderInit.get(node.getNodeID()), Instant.now()).toMillis() > 5000)
				withoutLeaderEnd.remove(node.getNodeID());
			return;
		}

		this.withoutLeaderTimeElapsed = Duration.between(withoutLeaderInit.get(node.getNodeID()),
				withoutLeaderEnd.get(node.getNodeID()));
		System.out.println("Without Leader _ Time taken: " + withoutLeaderTimeElapsed.toMillis() + " milliseconds");

		withoutLeaderInit.remove(node.getNodeID());
		withoutLeaderEnd.remove(node.getNodeID());

		if (toWrite)
			storeWithoutLeaderTimer();
	}

	// 4th Metric - Time spent in exchanging leaders
	// Related to Leader Special and Info Messages

	// 5th Metric - Election-Rate (R) is defined as the avg number of elections that
	// a node participates in per unit time
	// It depends on so many things ...

	// Storage Facility
	public void storeElectionTime(int id) throws IOException {
		String textToAppend = "Time" + "," + Instant.now() + "," + "Election" + "," + id + "," + "Node" + ","
				+ node.getNodeID() + "," + "Msg_Overhead" + "," + electionTimeElapsed.toMillis() + "," + "ms" + "\n";

		BufferedWriter writer = new BufferedWriter(new FileWriter("..\\Statistics\\electionTime.txt", true) // AppendMode
		);

		writer.newLine(); // Add new line
		writer.write(textToAppend);
		writer.close();
	}

	public void storeMessageOverhead(int id, int msgOverhead) throws IOException {
		String textToAppend = "Time" + "," + Instant.now() + "," + "Election" + "," + id + "," + "Node" + ","
				+ node.getNodeID() + "," + "Msg_Overhead" + "," + msgOverhead + "\n";

		BufferedWriter writer = new BufferedWriter(new FileWriter("..\\Statistics\\messageOverhead.txt", true) // AppendMode
		);

		writer.newLine(); // Add new line
		writer.write(textToAppend);
		writer.close();
	}

	public void storeWithoutLeaderTimer() throws IOException {
		String textToAppend = "Time" + "," + Instant.now() + "," + "W/out Leader" + "," + "-" + "," + "Node" + ","
				+ node.getNodeID() + "," + "Time" + "," + withoutLeaderTimeElapsed + "\n";

		BufferedWriter writer = new BufferedWriter(new FileWriter("..\\Statistics\\withoutLeader.txt", true) // AppendMode
		);

		writer.newLine(); // Add new line
		writer.write(textToAppend);
		writer.close();
	}

	public double runningAvg() {
		return 0;
	}

	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}

	public ConcurrentHashMap<Integer, Instant> getElectionInit() {
		return electionInit;
	}

	public void setElectionInit(ConcurrentHashMap<Integer, Instant> electionInit) {
		this.electionInit = electionInit;
	}

	public ConcurrentHashMap<Integer, Instant> getElectionEnd() {
		return electionEnd;
	}

	public void setElectionEnd(ConcurrentHashMap<Integer, Instant> electionEnd) {
		this.electionEnd = electionEnd;
	}

	public Duration getElectionTimeElapsed() {
		return electionTimeElapsed;
	}

	public void setElectionTimeElapsed(Duration electionTimeElapsed) {
		this.electionTimeElapsed = electionTimeElapsed;
	}

	public ConcurrentHashMap<Integer, Integer> getMapMsgOverhead() {
		return mapMsgOverhead;
	}

	public void setMapMsgOverhead(ConcurrentHashMap<Integer, Integer> mapMsgOverhead) {
		this.mapMsgOverhead = mapMsgOverhead;
	}

	public int getMsgSentInElection() {
		return msgSentInElection;
	}

	public void setMsgSentInElection(int msgSentInElection) {
		this.msgSentInElection = msgSentInElection;
	}

	public ConcurrentHashMap<Integer, Instant> getWithoutLeaderInit() {
		return withoutLeaderInit;
	}

	public void setWithoutLeaderInit(ConcurrentHashMap<Integer, Instant> withoutLeaderInit) {
		this.withoutLeaderInit = withoutLeaderInit;
	}

	public ConcurrentHashMap<Integer, Instant> getWithoutLeaderEnd() {
		return withoutLeaderEnd;
	}

	public void setWithoutLeaderEnd(ConcurrentHashMap<Integer, Instant> withoutLeaderEnd) {
		this.withoutLeaderEnd = withoutLeaderEnd;
	}

	public Duration getWithoutLeaderTimeElapsed() {
		return withoutLeaderTimeElapsed;
	}

	public void setWithoutLeaderTimeElapsed(Duration withoutLeaderTimeElapsed) {
		this.withoutLeaderTimeElapsed = withoutLeaderTimeElapsed;
	}

}
