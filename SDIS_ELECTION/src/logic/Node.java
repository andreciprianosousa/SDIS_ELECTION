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
	private int dropPacketProbability;
	private boolean isKilled;
	private boolean networkSet;
	private static final int nodeTimeout = 2;
	private static final int networkTimeout = 10;
	protected Mobility automovel;

	protected int port;
	protected String ipAddress;
	protected Simulation simNode;

	protected Instant init;

	public Node(int nodeID, int port, String ipAddress, int[] dimensions, int refreshRate, int timeOut,
			int dropPacketProbability) throws InterruptedException {
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
		this.dropPacketProbability = dropPacketProbability;
		this.isKilled = false;
		this.networkSet = false;
		this.init = Instant.now();

		// 0 in dropPacketProbability means no Drop Packets & no node Kills
		if (this.dropPacketProbability == 0) {
			this.simNode = new Simulation();
		} else {
			this.simNode = new Simulation(dropPacketProbability);
		}

		// Initial coordinates
		xCoordinate = (int) xMax; // (int) ((Math.random() * ((xMax - 0) + 1)) + 0);
		yCoordinate = (int) yMax; // (int) ((Math.random() * ((yMax - 0) + 1)) + 0);

		new NodeListener(this, refreshRate).start();
		new NodeTransmitter(this, timeOut).start();

		new Bootstrap(this).start(); // New node, so set network and act accordingly

		this.automovel = new Mobility(this, true, true);
		automovel.start();
		// Arg 1: Mover sim ou nao | 2 = x final coordenate | 3 = y final coordenate |
		// 4 - direction [0 - Horizontal, 1 - Vertical] | 5 - Sleep time
		automovel.testMobility(true, 5, 0, 0, 1);

	}

	public synchronized void updateNeighbors(int nodeMessageID, int xNeighbor, int yNeighbor) {
		if (nodeMessageID != nodeID) {
			// if message node is inside neighborhood
			if (this.isInsideNeighborhood(nodeMessageID, xNeighbor, yNeighbor)) {
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
			this.getWaitingAcks().remove(neighbor);// ... but also from waiting acks, this way a node doesn't wait for a
													// node it may not even be connected anymore

			// If ack removed was the last needed we need to check here
			if ((this.getWaitingAcks().isEmpty()) && (this.getAckStatus() == true)) {
				if (this.getParentActive() != -1) {
					this.setAckStatus(false);
					// send ACK message to parent stored in node.getParentActive()
					if (DEBUG)
						System.out.println("Sending to my parent " + this.getParentActive() + " the Leader Id "
								+ this.getStoredId());
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
					System.out.println("========================>   Leader agreed upon: " + this.getLeaderID());

					this.simNode.setEnd();
					this.simNode.getTimer();

					try {
						this.simNode.storeElectionTime();
					} catch (IOException e) {
						e.printStackTrace();
					}

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

			if (DEBUG)
				System.out.println("Removed neighbor " + neighbor + " from node " + this.getNodeID());

			printNeighbors();

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

				if (this.getNodeID() > this.getMaximumIdNeighbors()) {
					new Bootstrap(this).start();
				} else {
					// Start election fresh just in case
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

					this.simNode.setStart();

					// -----------CP Tests-----------
					this.getComputationIndex().setNum(this.getComputationIndex().getNum() + 1);
					this.getComputationIndex().setId(this.getNodeID());
					this.getComputationIndex().setValue(this.getNodeValue());
					// ------------------------------
					new Handler(this, logic.MessageType.ELECTION_GROUP, this.getWaitingAcks()).start();
				}
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

	public boolean testLiveliness(boolean isToTest) {

		if (!isToTest)
			return false;

		simNode.testNodeKill();

		if ((simNode.isNodeKilled())) {
			return true;
		}
		return false;
	}

	public boolean testPacket(boolean isToTest) {
		float distanceBetweenNodes = 0;

		if (!isToTest)
			return false;

		if (((simNode.dropPacketRange(this.nodeRange, distanceBetweenNodes)) || (simNode.dropPacketRandom())) == true) {
			return true;
		}
		return false;
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
		System.out.println("Node: " + nodeID + " || Leader: " + leaderID + " || LeaderValue: " + leaderValue
				+ " || Stored Id: " + storedId + " || Stored Value: " + storedValue);
		System.out.println("............................................");
	}

	public int getDropPacketProbability() {
		return dropPacketProbability;
	}

	public void setDropPacketProbability(int dropPacketProbability) {
		this.dropPacketProbability = dropPacketProbability;
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
}