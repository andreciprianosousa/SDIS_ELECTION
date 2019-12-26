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
	private static final boolean DEBUG_ElectionTimer = false;
	private static final boolean DEBUG_MsgOverhead = false;
	private static final boolean DEBUG_WithoutLeaderTimer = false;
	private static final boolean DEBUG_ExchangingLeader = false;
	private static final boolean DEBUG_ElectionRate = true;
	private static final int timeoutLeaderExchange = 300000;

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
	// 4th Metric Vars
	private ConcurrentHashMap<Integer, Instant> leaderExchangeInit = new ConcurrentHashMap<Integer, Instant>();
	private ConcurrentHashMap<Integer, Instant> leaderExchangeEnd = new ConcurrentHashMap<Integer, Instant>();
	private Duration leaderExchangeElapsed;
	// 5th Metric Vars
	private ConcurrentHashMap<Integer, Integer> nodeElectionRate = new ConcurrentHashMap<Integer, Integer>();
	private int electionRate;
	private Instant electionRateInit;
	private int unitTime; // in seconds
	private int totalNumberOfElectionRates;
	private int currentNumberOfElectionRates;
	private boolean isElectionRateDone;
	private boolean newTestElectionRate;
	private boolean newElection;

	public Evaluation(Node node) {
		this.node = node;
		this.unitTime = 60;
		this.totalNumberOfElectionRates = 2;
		this.currentNumberOfElectionRates = 0;
		this.isElectionRateDone = false;
		this.newTestElectionRate = true;
		this.newElection = false;
	}

	// 1st Metric - Election Time
	// Mean Time nodes are in election
	public void setStartElectionTimer(int id) {
		electionInit.put(id, Instant.now());
	}

	public void setEndElectionTimer(int id) {
		electionEnd.put(id, Instant.now());
	}

	public void getElectionTimer(int id) {

		if (!(electionInit.containsKey(id))) {
			if (DEBUG_ElectionTimer)
				System.out.println("No Election was started with that ID");
			return;
		}
		if (!(electionEnd.containsKey(id))) {
			if (DEBUG_ElectionTimer)
				System.out.println("Election wasn't finished yet Or it was left behind for a stronger leader");
			return;
		}

		this.electionTimeElapsed = Duration.between(electionInit.get(id), electionEnd.get(id));
		System.out.println("Election _ Time taken: " + electionTimeElapsed.toMillis() + " milliseconds");

		if (toWrite) {
			try {
				storeElectionTime(id);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// 2nd Metric - Overhead Messages Sent By Node in Election
	// Message Overhead (M) is the avg number of messages sent by a node in election
	public void counterMessagesInElection(int id, MessageType type) {
		int newCounterValue;

		if (DEBUG_MsgOverhead) {
			// System.out.println("Election: " + id + " | Message Type: " + type);
		}

		// If this election was not active, then reset the counter
		// Then as this is the first msg and put it in the Map
		// -> It's triggered only by election or ack msg
		// If there's already this electiohn,
		// * and it's sent an Leader msg, election has terminated
		// * if it's other type, replace the old counter, with the updated
		if (!(mapMsgOverhead.containsKey(id))) {
			msgSentInElection = 0;

			if (DEBUG_MsgOverhead) {
				System.out.println("Setting new Counter");
			}

			synchronized (this) {
				msgSentInElection = 1;
				mapMsgOverhead.put(id, msgSentInElection);
			}
			return;

		} else {
			if (type == MessageType.LEADER) {
				if (DEBUG_MsgOverhead)
					System.out.println("Leader Message - Stop Counting. Deleting Counter.");

				synchronized (this) {
					msgSentInElection = mapMsgOverhead.get(id);
					newCounterValue = msgSentInElection + 1;
					mapMsgOverhead.replace(id, msgSentInElection, newCounterValue);
					msgSentInElection = newCounterValue;

					// System.out.println("Msg Overhead in Election " + id + " = " +
					// mapMsgOverhead.get(id));

					msgSentInElection = mapMsgOverhead.get(id);

					if (toWrite) {
						try {
							storeMessageOverhead(id, msgSentInElection);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}

					mapMsgOverhead.remove(id, msgSentInElection);
				}
				return;
			} else {
				if (DEBUG_MsgOverhead)
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
		if (DEBUG_WithoutLeaderTimer)
			System.out.println("Node = " + node.getNodeID() + " | Size = " + node.getNeighbors().size()
					+ " | MaxNeigh = " + node.getMaximumIdNeighbors() + " | Leader = " + node.getLeaderID());

		if ((node.getNeighbors().size() > 0) && (node.getMaximumIdNeighbors() > node.getLeaderID())) {
			if (!(node.getNetworkEvaluation().getWithoutLeaderInit().containsKey(node.getNodeID()))) {
				if (DEBUG_WithoutLeaderTimer)
					System.out.println("Starting Timer WL - Case 1");
				setStartWithoutLeaderTimer();
			}
		}
		if (((node.getNodeID() != node.getLeaderID())) && (!(node.getNeighbors().contains(node.getLeaderID())))) {
			if (!(node.getNetworkEvaluation().getWithoutLeaderInit().containsKey(node.getNodeID()))) {
				if (DEBUG_WithoutLeaderTimer)
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

	public void getWithoutLeaderTimer() {

		if (!(withoutLeaderInit.containsKey(node.getNodeID()))) {
			if (DEBUG_WithoutLeaderTimer)
				System.out.println("No Timer was started with that Node ID");

			withoutLeaderEnd.remove(node.getNodeID());
			return;
		}
		if (!(withoutLeaderEnd.containsKey(node.getNodeID()))) {
			if (DEBUG_WithoutLeaderTimer)
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

		if (toWrite) {
			try {
				storeWithoutLeaderTimer();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// 4th Metric - Time spent in exchanging leaders
	// Related to Info Messages
	// There is 3 cases in dispute:
	// 1) Info Sender has LEADER bigger than Receiver => Sends INFO and gets no
	// __ reply, because receiver switch it's leader and it's done.
	// 2) Info Sender has same receiver LEADER => Sends INFO and gets no reply
	// 3) Info Sender has LEADER smaller than Receiver => Sends INFO and gets reply
	// __ with new leader
	// This function works only for case 3
	public void setStartExchangingLeadersTimer(int addresseeId) {
		leaderExchangeInit.put(addresseeId, Instant.now());
	}

	public void setEndExchangingLeadersTimer(int addresseeId) {
		leaderExchangeEnd.put(addresseeId, Instant.now());
	}

	public void getExchangingLeaderTimer(int addresseeId) {

		if (!(leaderExchangeInit.containsKey(addresseeId))) {
			if (DEBUG_ExchangingLeader)
				System.out.println("No Leader Exchange not contains Init [Case 1 or 2]");
			leaderExchangeEnd.remove(addresseeId);
			return;
		}
		if (!(leaderExchangeEnd.containsKey(addresseeId))) {
			if (DEBUG_ExchangingLeader)
				System.out.println("Leader Election wasn't finished yet Or it was left behind");

			if (Duration.between(leaderExchangeInit.get(addresseeId), Instant.now()).toMillis() > timeoutLeaderExchange)
				leaderExchangeInit.remove(addresseeId);
			return;
		}

		this.leaderExchangeElapsed = Duration.between(leaderExchangeInit.get(addresseeId),
				leaderExchangeEnd.get(addresseeId));
		System.out.println("Leader _ Exchange [" + addresseeId + "," + node.getNodeID() + "] Time taken: "
				+ leaderExchangeElapsed.toMillis() + " milliseconds");

		if (toWrite) {
			try {
				storeExchangingLeaderTimer(addresseeId);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		leaderExchangeInit.remove(addresseeId);
		leaderExchangeEnd.remove(addresseeId);
	}

	// 5th Metric - Election-Rate (R) is defined as the avg number of elections that
	// a node participates in per unit time
	// It depends on so many things ...
	public void setStartElectionRateTimer() {

		if (isElectionRateDone() == true) {
			return;
		}

		if (isNewTestElectionRate() == true) {
			if (DEBUG_ElectionRate)
				System.out.println("<<5>> Set New Election rates <<5>>");
			electionRateInit = Instant.now();
			setNewTestElectionRate(false);
		}
	}

	public void counterElectionRate(int id) {

		if (isElectionRateDone() == true) {
			if (DEBUG_ElectionRate)
				System.out.println("<<5>> Election Rate done " + totalNumberOfElectionRates + "x. It's enough! <<5>>");
			return;
		}

		setNewElection(false);

		if (Duration.between(electionRateInit, Instant.now()).toMillis() >= unitTime * 1000) {
			synchronized (this) {
				electionRate = nodeElectionRate.size();
				currentNumberOfElectionRates++;
				if (DEBUG_ElectionRate)
					System.out.println(
							"<<5>> In the last " + unitTime + " s, Election Rate = " + electionRate + ".<<5>>");

				if (currentNumberOfElectionRates == totalNumberOfElectionRates) {
					setElectionRateDone(true);
					if (DEBUG_ElectionRate)
						System.out.println("<<5>> All Election Rates were done.<<5>>");
				}

				if (toWrite) {
					try {
						storeElectionRate(electionRate);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				nodeElectionRate.clear();
				if (isElectionRateDone() == false) {
					setNewTestElectionRate(true);
				}
			}
		} else {
			synchronized (this) {
				if (!(nodeElectionRate.containsKey(id))) {
					nodeElectionRate.put(node.getNodeID(), 1);
					if (DEBUG_ElectionRate)
						System.out.println("<<5>> New Election to Count!<<5>>");
				} else {
					electionRate = nodeElectionRate.get(id);
					electionRate++;
					nodeElectionRate.replace(id, electionRate);
					if (DEBUG_ElectionRate)
						System.out.println("<<5>> Election with Same ID ++ <<5>>");
				}
			}
		}
	}

	// Storage Facility
	public void storeElectionTime(int id) throws IOException {
		String textToAppend = "Time" + "," + Instant.now() + "," + "Election" + "," + id + "," + "Node" + ","
				+ node.getNodeID() + "," + "ElectionTime" + "," + electionTimeElapsed.toMillis() + "," + "ms" + "\n";

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

	public void storeExchangingLeaderTimer(int id) throws IOException {
		String textToAppend = "Time" + "," + Instant.now() + "," + "Leader_Exchange" + "," + id + "," + "Node" + ","
				+ node.getNodeID() + "," + "ExchangingLeader" + "," + leaderExchangeElapsed + "\n";

		BufferedWriter writer = new BufferedWriter(new FileWriter("..\\Statistics\\exchangingLeader.txt", true) // AppendMode
		);

		writer.newLine(); // Add new line
		writer.write(textToAppend);
		writer.close();
	}

	public void storeElectionRate(int electionRatetoStore) throws IOException {
		String textToAppend = "Time" + "," + Instant.now() + "," + "Election_Rate" + "," + "Node" + ","
				+ node.getNodeID() + "," + "Election_Rate" + "," + electionRatetoStore + "," + "UnitTime" + ","
				+ unitTime + "," + "s" + "\n";

		BufferedWriter writer = new BufferedWriter(new FileWriter("..\\Statistics\\electionRate.txt", true) // AppendMode
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

	public static boolean isToWrite() {
		return toWrite;
	}

	public static void setToWrite(boolean toWrite) {
		Evaluation.toWrite = toWrite;
	}

	public ConcurrentHashMap<Integer, Integer> getNodeElectionRate() {
		return nodeElectionRate;
	}

	public void setNodeElectionRate(ConcurrentHashMap<Integer, Integer> nodeElectionRate) {
		this.nodeElectionRate = nodeElectionRate;
	}

	public int getUnitTime() {
		return unitTime;
	}

	public void setUnitTime(int unitTime) {
		this.unitTime = unitTime;
	}

	public int getTotalNumberOfElectionRates() {
		return totalNumberOfElectionRates;
	}

	public void setTotalNumberOfElectionRates(int totalNumberOfElectionRates) {
		this.totalNumberOfElectionRates = totalNumberOfElectionRates;
	}

	public int getCurrentNumberOfElectionRates() {
		return currentNumberOfElectionRates;
	}

	public void setCurrentNumberOfElectionRates(int currentNumberOfElectionRates) {
		this.currentNumberOfElectionRates = currentNumberOfElectionRates;
	}

	public boolean isElectionRateDone() {
		return isElectionRateDone;
	}

	public void setElectionRateDone(boolean isElectionRateDone) {
		this.isElectionRateDone = isElectionRateDone;
	}

	public boolean isNewTestElectionRate() {
		return newTestElectionRate;
	}

	public void setNewTestElectionRate(boolean newTestElectionRate) {
		this.newTestElectionRate = newTestElectionRate;
	}

	public boolean isNewElection() {
		return newElection;
	}

	public void setNewElection(boolean newElection) {
		this.newElection = newElection;
	}

}
