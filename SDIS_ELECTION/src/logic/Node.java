package logic;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import network.*;
import simulation.Simulation;
import java.time.Instant; 
import java.time.Duration;

public class Node implements Serializable{
	
	protected int nodeID;
	protected ComputationIndex computationIndex;
	protected boolean electionActive;
	protected int parentActive;
	protected boolean ackSent;
	protected int leaderID;
	protected HashMap<Integer, Instant> neighbors;
	protected HashSet<Integer> waitingACK;
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

	protected int port;
	protected String ipAddress;
	protected Simulation simNode;
	
	public Node (int nodeID, int port, String ipAddress, int[] dimensions, int refreshRate, int timeOut) throws InterruptedException {
		this.nodeID = nodeID;
		this.port = port;
		this.ipAddress = ipAddress;
		this.nodeValue = nodeID;

		this.storedValue = this.nodeValue;
		this.storedId = this.nodeID;
		// don't forget to increase num when starting election
		this.computationIndex = new ComputationIndex(this.getNodeID(), 0, this.getNodeValue()); 
		this.parentActive = -1;
		this.electionActive = false;
		this.leaderID = -1; // -1 is no leader set
		this.ackSent = true; // true means no ack sent yet, which technically is correct here
		this.waitingACK = new HashSet<Integer>();

		this.xMax=dimensions[0];
		this.yMax=dimensions[1];
		this.nodeRange = dimensions[2];
		this.neighbors = new HashMap <Integer, Instant>();
		this.timeOut = timeOut;
		
		//Initial coordinates 
		xCoordinate = (int) ((Math.random() * ((xMax - 0) + 1)) + 0);
		yCoordinate = (int) ((Math.random() * ((yMax - 0) + 1)) + 0);

		
		new NodeListener(this, refreshRate).start();
		new NodeTransmitter(this, timeOut).start();
		
	
		new Bootstrap(this).start(); // New node, so set network and start election	

	}
	
	public synchronized void updateNeighbors(HelloMessage message, InetAddress ipaddress) {
		int nodeMessageID = message.getNodeID();
		//if message node is not this node

		if(nodeMessageID != nodeID) {
			//if message node is inside neighborhood
			if(this.isInsideNeighborhood(nodeMessageID, message.getxCoordinate(), message.getyCoordinate())) {
				//does node exist? update time, otherwise add it and update time
				if(!neighbors.containsKey(nodeMessageID)) {
					System.out.print("Node " + this.getNodeID() + " is neighbor of: [");
					for(int neighbor : neighbors.keySet()) {
						System.out.print(neighbor+ " ");
					}
					System.out.print(nodeMessageID+ " ");
					System.out.println("]");
				}
				neighbors.put(message.getNodeID(), Instant.now());
			}		
		}
		
	}
	
	public void updateRemovedNodes() {

		ArrayList<Integer> toRemove = new ArrayList<Integer>();
		
		
		// Check if neighbors are connected, if not put them in a "blacklist"
		for(int neighbor : neighbors.keySet()) { 
			//System.out.println(Duration.between(neighbors.get(neighbor), Instant.now()).toMillis());
			if(Duration.between(neighbors.get(neighbor), Instant.now()).toMillis() > (timeOut*1000)) {
				toRemove.add(neighbor);
			}		
		}
		
		// Actually remove the gone neighbours
		for(int neighbor : toRemove) {
			neighbors.remove(neighbor);
			System.out.println("Removed neighbor " + neighbor + " from node " + this.getNodeID());
			
			printNeighbors();
			
			// If leader is no longer my neighbour, restart the election because no leader = bad
			if(!(neighbors.containsKey(leaderID))) {
				System.out.println("Leader is gone");
				new Bootstrap(this).start();
			}
		}
	}
	
	public void printNeighbors() {
		
		System.out.print("Node " + this.getNodeID() + " is neighbor of: [");
		for(int neighbor : neighbors.keySet()) {
			System.out.print(neighbor+ " ");
		}
		System.out.println("]");
	}

	public boolean isInsideNeighborhood(int neighborID, int xNeighbor, int yNeighbor) {
		
		float distanceBetweenNodes;
		
		distanceBetweenNodes = (float) Math.sqrt(Math.pow((xNeighbor-xCoordinate),2) + Math.pow((yNeighbor-yCoordinate),2));
		
		if(distanceBetweenNodes <= this.nodeRange) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public boolean testPacket () {
		boolean isPacketDropped;
		float distanceBetweenNodes = 0;
		
		//distanceBetweenNodes = distanceBetweenNodes();
		
		if ((isPacketDropped = simNode.dropPacket(this.nodeRange, distanceBetweenNodes)) == true)
			return true;
		else 
			return false;
	}


	
	@Override
    public String toString() {
        return Integer.toString(nodeID);
    }

    @Override
    public boolean equals(Object obj) {
        Node node = (Node) obj;
        if(node.getNodeID()==this.getNodeID()) {
        	return true;
        }
        else {
        	return false;
        }

    }
   
    public float distanceBetweenNodes(Node node) {
    	float distanceBetweenNodes;
    	
    	distanceBetweenNodes = (float) Math.sqrt(Math.pow((node.xCoordinate-xCoordinate),2) + Math.pow((node.yCoordinate-yCoordinate),2));
    	return distanceBetweenNodes;
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
		this.storedId = storedId;;
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
	public void setNeighbors(HashMap<Integer, Instant> neighbors) {
		this.neighbors = neighbors;
	}
	public HashSet<Integer> getWaitingAcks() {
		return this.waitingACK;
	}
	public void setWaitingAck(HashSet<Integer> waitingACK) {
		this.waitingACK = waitingACK;
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

	public HashSet<Integer> getWaitingACK() {
		return waitingACK;
	}

	public void setWaitingACK(HashSet<Integer> waitingACK) {
		this.waitingACK = waitingACK;
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

	public int getParentActive() {
		return parentActive;
	}
	
}
