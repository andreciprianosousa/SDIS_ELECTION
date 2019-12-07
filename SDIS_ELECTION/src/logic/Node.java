package logic;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import network.*;
import simulation.Simulation;

public class Node implements Serializable{
	
	protected int nodeID;
	protected ComputationIndex computationIndex;
	protected boolean electionActive;
	protected int parentActive;
	protected boolean ackSent;
	protected int leaderID;
	protected HashSet<Integer> neighbors;
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
	
	private boolean isKilled;

	protected int port;
	protected String ipAddress;
	protected Simulation simNode;
	
	public Node (int nodeID, int port, String ipAddress, int[] dimensions) throws InterruptedException {
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
		this.ackSent = true; // true means no ack sent yet, which technically is correct he
		this.waitingACK = new HashSet<Integer>();

		this.xMax=dimensions[0];
		this.yMax=dimensions[1];
		this.nodeRange = dimensions[2];
		this.neighbors = new HashSet <Integer>();
		
		//Initial coordinates 
		xCoordinate = (int) ((Math.random() * ((xMax - 0) + 1)) + 0);
		yCoordinate = (int) ((Math.random() * ((yMax - 0) + 1)) + 0);

		
		new NodeListener(this).start();
		new NodeTransmitter(this).start();

	}
	
	public void updateNeighbors(HelloMessage message, InetAddress ipaddress) {
		int nodeMessageID = message.getNodeID();
		//if message node is not this node

		if(nodeMessageID != nodeID) {
			//if this node does not contain the name in the message
			if(!(neighbors.contains( message.getNodeID()))) {
				//if message node is inside neighborhood
				if(this.isInsideNeighborhood(message.getNodeID(), message.getxCoordinate(), message.getyCoordinate())) {
					neighbors.add(message.getNodeID());
					
					System.out.print("Node " + this.getNodeID() + " is neighbor of: [");
					for(int neighbor : neighbors) {
						System.out.print(neighbor+ " ");
					}
					System.out.println("]");
				}
			}	
		}
	}
	

	public boolean isInsideNeighborhood(int neighborID, int xNeighbor, int yNeighbor) {
		int range;
		float distanceBetweenNodes;
		
		distanceBetweenNodes = distanceBetweenNodes(xNeighbor, yNeighbor);
		
		if(distanceBetweenNodes <= this.nodeRange) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public boolean testPacket (int xNeighbor, int yNeighbor) {
		
		float distanceBetweenNodes = distanceBetweenNodes(xNeighbor, yNeighbor);
		
		if (simNode.dropPacket(this.nodeRange, distanceBetweenNodes) == true)
			return true;
		else 
			return false;
	}

	public float distanceBetweenNodes(int xNeighbor, int yNeighbor) {
		
    	float distanceBetweenNodes = (float) Math.sqrt(Math.pow((xNeighbor-xCoordinate),2) + Math.pow((yNeighbor-yCoordinate),2));
    	
    	return distanceBetweenNodes;
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
	public HashSet<Integer> getNeighbors() {
		return neighbors;
	}
	public void setNeighbors(HashSet<Integer> neighbors) {
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

	public boolean isKilled() {
		return isKilled;
	}

	public void setKilled(boolean isKilled) {
		this.isKilled = isKilled;
	}
}
