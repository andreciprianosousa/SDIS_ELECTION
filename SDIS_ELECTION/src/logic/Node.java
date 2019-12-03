package logic;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.HashSet;

import network.*;

public class Node implements Serializable{
	
	protected int nodeID;
	protected ComputationIndex computationIndex;
	protected boolean electionActive;
	protected int parentActive;
	protected boolean ackSent;
	protected int leaderID;
	protected HashSet<Node> neighbors;
	protected HashSet<Node> waitingACK;
	protected float nodeValue;
	protected float storedValue;
	protected int storedId;
	
	// Used in mobile implementation
	private float xMax;
	private float yMax;
	private float xCoordinate;
	private float yCoordinate;
	private float nodeRange;
	protected int port;
	protected String ipAddress;
	
	public Node (int nodeID, int port, String ipAddress, int[] dimensions) throws InterruptedException {
		this.nodeID = nodeID;
		this.port = port;
		this.ipAddress = ipAddress;
		this.nodeValue = nodeID;
		this.storedValue = this.nodeValue;
		this.storedId = this.nodeID;
		// don't forget to increase num when starting election
		this.computationIndex = new ComputationIndex(this.getNodeID(), 0, this.getNodeValue()); 
		
		this.xMax=dimensions[0];
		this.yMax=dimensions[1];
		this.nodeRange = dimensions[2];
		this.neighbors = new HashSet <Node>();
		
		//Initial coordinates 
		xCoordinate = (float) ((Math.random() * ((xMax - 0) + 1)) + 0);
		yCoordinate = (float) ((Math.random() * ((yMax - 0) + 1)) + 0);
		
		new NodeListener(this).start();
		new NodeTransmitter(this).start();
		
	}
	
	public void updateNeighbors(HelloMessage message, InetAddress ipaddress) {
		int nodeMessageID = message.getNode().getNodeID();
		//if message node is not this node

		if(nodeMessageID != nodeID) {
			//if this node does not contain the name in the message
			if(!(neighbors.contains( message.getNode()))) {
				//if message node is inside neighborhood
				if(message.getNode().isInsideNeighborhood(this)) {
					neighbors.add(message.getNode());
					
					System.out.print("Node " + this.getNodeID() + " is neighbor of: [");
					for(Node neighbor : neighbors) {
						System.out.print(neighbor.getNodeID()+ " ");
					}
					System.out.println("]");
				}
			}	
		}
	}
	
	public boolean isInsideNeighborhood(Node node) {
		float distanceBetweenNodes;
		
		distanceBetweenNodes = (float) Math.sqrt(Math.pow((node.xCoordinate-xCoordinate),2) + Math.pow((node.yCoordinate-yCoordinate),2));
		
		if(distanceBetweenNodes <= node.nodeRange) {
			return true;
		}
		else {
			return false;
		}
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
	public float getNodeValue() {
		return nodeValue;
	}
	public void setNodeValue(float nodeValue) {
		this.nodeValue = nodeValue;
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
	public HashSet<Node> getNeighbors() {
		return neighbors;
	}
	public void setNeighbors(HashSet<Node> neighbors) {
		this.neighbors = neighbors;
	}
	public HashSet<Node> getWaitingAcks() {
		return this.waitingACK;
	}
	public void setWaitingAck(HashSet<Node> waitingACK) {
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
	public ComputationIndex getCP() {
		return this.computationIndex;
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

	public float getxCoordinate() {
		return xCoordinate;
	}

	public void setxCoordinate(float xCoordinate) {
		this.xCoordinate = xCoordinate;
	}

	public float getyCoordinate() {
		return yCoordinate;
	}

	public void setyCoordinate(float yCoordinate) {
		this.yCoordinate = yCoordinate;
	}

	public float getNodeRange() {
		return nodeRange;
	}

	public void setNodeRange(float nodeRange) {
		this.nodeRange = nodeRange;
	}

}
