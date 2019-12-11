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
	protected HashSet<Integer> waitingAcks;
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
		this.waitingAcks = new HashSet<Integer>();

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
			this.neighbors.remove(neighbor); // Remove from neighbours...
			this.getWaitingAcks().remove(neighbor);// ... but also from waiting acks, this way a node doesn't wait for a node it may not even be connected anymore
			// IMPORTANT FOR MOBILITY -> this solves the problem unless the last waiting ack is the one
			// that gets lost. Because node only checks acks remaining when it receives one
			// It will never check again on its own and will remain stuck
			// Possible solution: check here if it is last ack, if so, copy the same logic here
			// from the handler...very ugly and it doesn't work well...
			// If this was the last acknowledge needed, then send to parent my own ack and update my parameters
//			if(this.getWaitingAcks().isEmpty() && (this.getAckStatus() == true)) {
//				if(this.getParentActive() != -1) {
//					this.setAckStatus(false);
//					// send ACK message to parent stored in node.getParentActive()
//					new Handler(this, logic.MessageType.ACK, this.getParentActive()).start();
//					System.out.println("Sending to my parent " + this.getParentActive() + " the Leader Id " + this.getStoredId());
//				}
//				// or prepare to send leader message if this node is the source of the election (if it has no parent)
//				else {
//					this.setAckStatus(true); // may change
//					this.setElectionActive(false);
//					this.setLeaderID(this.getStoredId());
//					System.out.println("Leader agreed upon: " + this.getLeaderID());
//					
//					// send Leader message to all children, needs id and value of leader chosen (stored already)
//					Iterator<Integer> i=this.getNeighbors().iterator();
//					this.getWaitingAcks().clear(); // clear this first just in case
//					HashSet<Integer> toSend = new HashSet<Integer>();
//					
//					while(i.hasNext()) {
//						Integer temp = i.next();
//						toSend.add(temp);
//					}
//					// Send Election Message to all neighbours, except parent
//					System.out.println("Sending leader to all nodes.");
//					new Handler(this, logic.MessageType.LEADER, toSend).start();
//				}
//			}
			
			System.out.println("Removed neighbor " + neighbor + " from node " + this.getNodeID());
			
			printNeighbors();
			
			// If leader is no longer my neighbour, restart the election because no leader = bad
			// Special case for the leader itself, it doesn't need to check itself 	
			if(!neighbors.containsKey(leaderID) && !(nodeID == leaderID)) {
				//System.out.println("NODE ID = " + this.nodeID);
				System.out.println("Leader is gone");
				this.setStoredId(this.nodeID);
				this.setStoredValue(this.nodeID);
				this.setLeaderID(-1);
				this.setParentActive(-1);
				this.waitingAcks.remove(neighbor);
				//this.setComputationIndex(new ComputationIndex(this.getNodeID(), 0, this.getNodeValue()));
				if(this.nodeID > getMaximumIdNeighbors()) {
					System.out.println("BootStrapping => " + this.nodeID);
					new Bootstrap(this).start();
				}
			}
		}
	}
	
	public int getMaximumIdNeighbors() {
		int max=0;
		for(int neighbor : neighbors.keySet()) {
			if(neighbor > max) {
				max = neighbor;
			}
		}
		//System.out.println("MAX ID = " + max);
		return max;
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
	public void setNeighbors(HashMap<Integer, Instant> neighbors) {
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

	public HashSet<Integer> getWaitingAcks() {
		return this.waitingAcks;
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
}
