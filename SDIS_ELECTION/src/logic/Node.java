package logic;

import java.io.IOException;
import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import mobility.Mobility;
import network.*;

import simulation.Simulation;

public class Node implements Serializable {

	private static final boolean DEBUG = true;

	protected int nodeID;
	protected ComputationIndex computationIndex;
	protected boolean electionActive;
	protected int parentActive;
	protected boolean ackSent;
	protected int leaderID;
	protected float leaderValue;
	protected ConcurrentHashMap<Integer, Instant> neighbors;
	protected Set<Integer> waitingAcks;
	protected float nodeValue;
	protected float storedValue;
	protected int storedId;

	// Used in mobile implementation
	private float xMax;
	private float yMax;

	private int xCoordinate;
	private int yCoordinate;
	private int nodeRange;
	private int timeOut;

	private boolean networkSet;
	private static final int nodeTimeout = 2;
	private static final int networkTimeout = 10;

	// Simulation Purpose
	protected Simulation simNode;
	private int medianFailure;
	private boolean isToTestSimulation;
	private int medianDeath;
	private boolean isToTestDeath;
	private boolean isKilled;

	// Mobility Purpose
	protected Mobility automovel;
	protected int finalX;
	protected int finalY;
	protected int direction;
	protected boolean hasMobility;
	protected boolean testMobility;

	// Network+Algorithm Evaluation Purpose
	protected Evaluation networkEvaluation;

	protected int port;
	protected String ipAddress;

	protected Instant init;

	public Node(int nodeID, int port, String ipAddress, int[] dimensions, int refreshRate, int timeOut,
			int medianFailure, int medianDeath, int[] finalDestination) throws InterruptedException {
		this.nodeID = nodeID;
		this.port = port;
		this.ipAddress = ipAddress;
		this.nodeValue = nodeID;
		this.leaderValue = this.nodeValue;
		this.storedValue = this.nodeValue;
		this.storedId = this.nodeID;

		this.computationIndex = new ComputationIndex(this.getNodeID(), 0, this.getNodeValue());
		this.parentActive = -1;
		this.electionActive = false;
		this.leaderID = this.nodeID; // leader is itself if nothing is said otherwise
		this.ackSent = true; // true means no ack sent yet, which technically is correct here
		this.waitingAcks = Collections.synchronizedSet(new HashSet<Integer>());

		this.xMax = dimensions[0];
		this.yMax = dimensions[1];
		this.nodeRange = dimensions[2];
		this.neighbors = new ConcurrentHashMap<Integer, Instant>();
		this.timeOut = timeOut;

		// Simulation Purpose
		this.medianFailure = medianFailure;
		this.medianDeath = medianDeath;
		this.isKilled = false;
		this.networkSet = false;
		this.init = Instant.now();

		// Mobility Purpose
		this.finalX = finalDestination[0];
		this.finalY = finalDestination[1];
		this.direction = finalDestination[2];

		// 0 in mFailure && mDeath means no Drop Packets & no node Kills
		if ((this.medianFailure <= 0) && (this.medianDeath <= 0)) {
			this.simNode = new Simulation();
			setToTestSimulation(false);
			setToTestDeath(false);
		} else {
			this.simNode = new Simulation(medianFailure, medianDeath, init);
			setToTestSimulation(true);
			setToTestDeath(true);
			if (this.medianFailure <= 0)
				setToTestSimulation(false);
			if (this.medianDeath <= 0)
				setToTestDeath(false);
		}

		// -1 = no movement
		// has Mobility = TRUE => nodes move randomly following "Randow Waypoint Model"
		// test Mobility = TRUE => nodes move accordingly to user input (has only 1
		// move) and stops
		if (((finalX < 0) && (finalY < 0) && (direction < 0)) || (direction > 1)) {
			hasMobility = false;
			testMobility = false;
		} else if ((finalX < 0) || (finalY < 0) || (direction < 0)) {
			hasMobility = true;
			testMobility = false;
		} else {
			testMobility = true;
			hasMobility = true;
		}

		// Initial coordinates
		xCoordinate = (int) xMax; // (int) ((Math.random() * ((xMax - 0) + 1)) + 0);
		yCoordinate = (int) yMax; // (int) ((Math.random() * ((yMax - 0) + 1)) + 0);

		if (DEBUG) {

			System.out.println("Simulation(MF, MD): " + this.medianFailure + " - " + this.medianDeath);
			System.out.println("Simulation(S,D): " + this.isToTestSimulation + " - " + this.isToTestDeath);
			System.out.println("Mobility(M,T): " + this.hasMobility + " - " + this.testMobility);
			// System.out.println(
			// "Mobility(xF,yF,Direction): " + this.finalX + " - " + this.finalY + " - " +
			// this.direction);
			// System.out.println();
		}

		new NodeListener(this, refreshRate).start();
		new NodeTransmitter(this, timeOut).start();

		this.networkEvaluation = new Evaluation(this);

		new Bootstrap(this).start(); // New node, so set network and act accordingly

		this.automovel = new Mobility(this, hasMobility, testMobility);
		automovel.start();

		// Arg 1: 1 = x final coordenate | 2 = y final coordenate |
		// 3 - direction [0 - Horizontal, 1 - Vertical] | 4 - Sleep time
		automovel.testMobility(finalX, finalY, direction, 1);

	}

	public synchronized void updateNeighbors(int nodeMessageID, int xNeighbor, int yNeighbor) {
		if (nodeMessageID != nodeID) {
			// if message node is inside neighborhood
			if (this.isInsideNeighborhood(nodeMessageID, xNeighbor, yNeighbor)) {

				// If I'm in an election, ignores new nodes entering range of neighbourhood!
				// This not only prevents wrong leader passing with or without mobility, but
				// also cuts down on number of inconsequential messages and network passivity
				// This only works because every node updates itself (connects all nodes around)
				// at every call of this method and thus doesn't lose new nodes.
				// ERROR: conflicts with bootstrapping kinda
				if (this.isElectionActive())
					return;

				// This check makes sure than, in mobility, if a node recognizes a new node
				// connecting and not in an election,
				// it exchanges info messages with it to establish leader in the new overall
				// network
				if (!neighbors.containsKey(nodeMessageID))
					new Handler(this, logic.MessageType.INFO, nodeMessageID).start();

				neighbors.put(nodeMessageID, Instant.now());
				// System.out.println("Updated to: " + Instant.now());

			}
		}
		updateNetworkSet();
	}

	// Used in update removed nodes
	// "Send Message" for type Leader
	public void sendMessage(logic.MessageType messageType, Set<Integer> mailingList) {
		if (mailingList.isEmpty()) {
			System.out.println("Mailing List is Empty");
			return;
		}
		new Handler(this, messageType, mailingList).start();
	}

	// "Send Message" for type Ack
	public void sendMessage(logic.MessageType messageType, int addresseeId) {
		new Handler(this, messageType, addresseeId).start();
	}

	public void updateRemovedNodes() {

		ArrayList<Integer> toRemove = new ArrayList<Integer>();

		// Check if neighbors are connected, if not put them in a "blacklist"
		for (int neighbor : neighbors.keySet()) {
			// System.out.println(Duration.between(neighbors.get(neighbor),
			// Instant.now()).toMillis());
			if (Duration.between(neighbors.get(neighbor), Instant.now()).toMillis() > (timeOut * 1000)) {
				if (DEBUG)
					System.out.println("Duration = "
							+ Duration.between(neighbors.get(neighbor), Instant.now()).toMillis() + "ms.");
				toRemove.add(neighbor);
			}
		}

		// Actually remove the gone neighbours
		for (int neighbor : toRemove) {
			this.neighbors.remove(neighbor); // Remove from neighbours...

			if (DEBUG)
				System.out.println("Removed neighbor " + neighbor + " from node " + this.getNodeID());

			printNeighbors();

			if (this.isElectionActive()) {
				this.getWaitingAcks().remove(neighbor);// ... but also from waiting acks, this way a node doesn't wait
														// for a
														// node it may not even be connected anymore

				// If ack removed was the last needed we need to check here
				if ((this.getWaitingAcks().isEmpty()) && (this.getAckStatus() == true)) {
					if (this.getParentActive() != -1) {
						this.setAckStatus(false);
						// send ACK message to parent stored in node.getParentActive()
						if (DEBUG)
							System.out.println("Sending to my parent " + this.getParentActive() + " the Leader Id "
									+ this.getStoredId() + " from removed last ack.");
						sendMessage(logic.MessageType.ACK, this.getParentActive());

					}
					// or prepare to send leader message if this node is the source of the election
					// (if it has no parent)
					else {

						this.setAckStatus(true);
						this.setElectionActive(false);
						this.setLeaderID(this.getStoredId());
						this.setLeaderValue(this.getStoredValue());
						this.setStoredId(this.getNodeID());
						this.setStoredValue(this.getNodeValue());
						if (DEBUG)
							System.out.println("========================>   Leader agreed upon: " + this.getLeaderID());

						this.networkEvaluation.setEnd(this.getComputationIndex().getId());
						this.networkEvaluation.getTimer(this.getComputationIndex().getId());
						// this.networkEvaluation.getMsgOverhead(this.getComputationIndex().getId());

//						try {
//							this.simNode.storeElectionTime();
//						} catch (IOException e) {
//							e.printStackTrace();
//						}

						// send Leader message to all children
						Iterator<Integer> i = this.getNeighbors().iterator();
						this.getWaitingAcks().clear(); // clear this first just in case

						Set<Integer> toSend = Collections.synchronizedSet(new HashSet<Integer>());
						while (i.hasNext()) {
							Integer temp = i.next();
							toSend.add(temp);
						}
						// Send Election Message to all neighbours
						if (DEBUG)
							System.out.println("Sending leader to all nodes.\n-----------------------------");

						sendMessage(logic.MessageType.LEADER, toSend);
					}
				}
			}

			// Auto check to prevent multiple leader elections
			if (this.isElectionActive()) {
				if (DEBUG)
					System.out.println("...but already in election so no need to start another.");
				return;
			}

			// If leader is no longer my neighbour, restart the election because no leader =
			// bad
			// Special case for the leader itself, it doesn't need to check itself
			if (!neighbors.containsKey(leaderID) && !(nodeID == leaderID)) {
				this.setStoredId(this.nodeID);
				this.setStoredValue(this.nodeValue);
				this.setLeaderID(this.nodeID);
				this.setLeaderValue(this.nodeValue);
				this.setParentActive(-1);
				this.waitingAcks.remove(neighbor);
				if (DEBUG)
					System.out.println(
							"Leader gone or one of my neighbours gone but I don't have direct connection to leader.");

				synchronized (this) {
					Iterator<Integer> i = this.getNeighbors().iterator();
					while (i.hasNext()) {
						Integer temp = i.next();
						if ((!(this.getWaitingAcks().contains(temp))) && (!(temp.toString().equals("")))) {
							this.getWaitingAcks().add(temp);
						}
					}
				}

				if (DEBUG)
					System.out.println("Node " + this.getNodeID() + " bootstrapped election on leader removal.");

				this.networkEvaluation.setStart(this.getComputationIndex().getId());

				// -----------CP Tests-----------
				this.getComputationIndex().setNum(this.getComputationIndex().getNum() + 1);
				this.getComputationIndex().setId(this.getNodeID());
				this.getComputationIndex().setValue(this.getNodeValue());
				// ------------------------------
				new Handler(this, logic.MessageType.ELECTION_GROUP, this.getWaitingAcks()).start();
			}
		}
	}

	public int getMaximumIdNeighbors() {
		int max = 0;
		for (int neighbor : neighbors.keySet()) {
			if (neighbor > max) {
				max = neighbor;
			}
		}
		return max;
	}

	private void updateNetworkSet() {
		if (Duration.between(init, Instant.now()).toMillis() > (timeOut * 1000)) {
			timeOut = nodeTimeout;
		} else {
			timeOut = networkTimeout;
		}
	}

	public void printNeighbors() {

		System.out.print("Node " + this.getNodeID() + " is neighbor of: [");
		for (int neighbor : neighbors.keySet()) {
			System.out.print(neighbor + " ");
		}
		System.out.println("]");
	}

	public boolean isInsideNeighborhood(int neighborID, int xNeighbor, int yNeighbor) {

		float distanceBetweenNodes;

		distanceBetweenNodes = (float) Math
				.sqrt(Math.pow((xNeighbor - xCoordinate), 2) + Math.pow((yNeighbor - yCoordinate), 2));

		if (distanceBetweenNodes <= this.nodeRange) {
			return true;
		} else {
			return false;
		}
	}

	public boolean testPacket(int messageCounter) {

		if (!isToTestSimulation)
			return false;

		if (simNode.meanTimeToHappenFailure(messageCounter) == true) {
			return true;
		}
		return false;
	}

	public boolean testLiveliness() {

		if (!isToTestDeath)
			return false;

		// simNode.testNodeKill();
		simNode.meanTimeToDie(Instant.now());

		if ((simNode.isNodeKilled())) {
			simNode.resetCharge(Instant.now());
			return true;
		}
		return false;
	}

	public void resetCharge() {
		simNode.resetCharge(Instant.now());
	}

	@Override
	public String toString() {
		return Integer.toString(nodeID);
	}

	@Override
	public boolean equals(Object obj) {
		Node node = (Node) obj;
		if (node.getNodeID() == this.getNodeID()) {
			return true;
		} else {
			return false;
		}

	}

	public float distanceBetweenNodes(Node node) {
		float distanceBetweenNodes;

		distanceBetweenNodes = (float) Math
				.sqrt(Math.pow((node.xCoordinate - xCoordinate), 2) + Math.pow((node.yCoordinate - yCoordinate), 2));
		return distanceBetweenNodes;
	}

	public float getLeaderValue() {
		return leaderValue;
	}

	public void setLeaderValue(float leaderValue) {
		this.leaderValue = leaderValue;
	}

	public int hashCode() {
		return toString().hashCode();
	}

	public int getNodeID() {
		return nodeID;
	}

	public void setNodeID(int nodeID) {
		this.nodeID = nodeID;
	}

	public int getStoredId() {
		return storedId;
	}

	public void setStoredId(int storedId) {
		this.storedId = storedId;
		;
	}

	public float getStoredValue() {
		return storedValue;
	}

	public void setStoredValue(float storedValue) {
		this.storedValue = storedValue;
	}

	public boolean isElectionActive() {
		return electionActive;
	}

	public void setElectionActive(boolean electionActive) {
		this.electionActive = electionActive;
	}

	public int getParentActive() {
		return parentActive;
	}

	public void setParentActive(int parentActive) {
		this.parentActive = parentActive;
	}

	public int getLeaderID() {
		return leaderID;
	}

	public void setLeaderID(int leaderID) {
		this.leaderID = leaderID;
	}

	public Set<Integer> getNeighbors() {
		return neighbors.keySet();
	}

	public void setNeighbors(ConcurrentHashMap<Integer, Instant> neighbors) {
		this.neighbors = neighbors;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public boolean getAckStatus() {
		return this.ackSent;
	}

	public void setAckStatus(boolean ackSent) {
		this.ackSent = ackSent;
	}

	public ComputationIndex getComputationIndex() {
		return this.computationIndex;
	}

	public void setComputationIndex(ComputationIndex computationIndex) {
		this.computationIndex = computationIndex;
	}

	public boolean isAckSent() {
		return ackSent;
	}

	public void setAckSent(boolean ackSent) {
		this.ackSent = ackSent;
	}

	public Set<Integer> getWaitingAcks() {
		return this.waitingAcks;
	}

	public void setWaitingAcks(Set<Integer> waitingAcks) {
		this.waitingAcks = waitingAcks;
	}

	public void setWaitingAcks(HashSet<Integer> waitingACK) {
		this.waitingAcks = waitingACK;
	}

	public float getNodeValue() {
		return nodeValue;
	}

	public void setNodeValue(float nodeValue) {
		this.nodeValue = nodeValue;
	}

	public float getxMax() {
		return xMax;
	}

	public void setxMax(float xMax) {
		this.xMax = xMax;
	}

	public float getyMax() {
		return yMax;
	}

	public void setyMax(float yMax) {
		this.yMax = yMax;
	}

	public int getxCoordinate() {
		return xCoordinate;
	}

	public void setxCoordinate(int xCoordinate) {
		this.xCoordinate = xCoordinate;
	}

	public int getyCoordinate() {
		return yCoordinate;
	}

	public void setyCoordinate(int yCoordinate) {
		this.yCoordinate = yCoordinate;
	}

	public int getNodeRange() {
		return nodeRange;
	}

	public void setNodeRange(int nodeRange) {
		this.nodeRange = nodeRange;
	}

	public void printLeader() {
		System.out.println(
				"Node: " + nodeID + " || Leader: " + leaderID + " || LeaderValue: " + leaderValue + " || Stored Id: "
						+ storedId + " || Stored Value: " + storedValue + " || CP: " + computationIndex.getNum() + "/"
						+ computationIndex.getValue() + "/" + computationIndex.getId() + this.isElectionActive());
		System.out.println("............................................");
	}

	public int getMedianFailure() {
		return medianFailure;
	}

	public void setMedianFailure(int medianFailure) {
		this.medianFailure = medianFailure;
	}

	public int getMedianDeath() {
		return medianDeath;
	}

	public void setMedianDeath(int medianDeath) {
		this.medianDeath = medianDeath;
	}

	public boolean isKilled() {
		return isKilled;
	}

	public void setKilled(boolean isKilled) {
		this.isKilled = isKilled;
	}

	public Simulation getSimNode() {
		return simNode;
	}

	public void setSimNode(Simulation simNode) {
		this.simNode = simNode;
	}

	public boolean isToTestSimulation() {
		return isToTestSimulation;
	}

	public void setToTestSimulation(boolean isToTestSimulation) {
		this.isToTestSimulation = isToTestSimulation;
	}

	public boolean isToTestDeath() {
		return isToTestDeath;
	}

	public void setToTestDeath(boolean isToTestDeath) {
		this.isToTestDeath = isToTestDeath;
	}
}