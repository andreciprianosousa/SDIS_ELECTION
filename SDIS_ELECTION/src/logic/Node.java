package logic;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import network.*;
import simulation.Simulation;

public class Node implements Serializable{
	
	protected int nodeID;
	protected int computationIndex;
	protected boolean electionActive;
	protected int parentActive;
	protected boolean ackSent;
	protected int leaderID;
	protected HashSet<Node> neighbors;
	protected HashSet<Node> waitingACK;
	protected float nodeValue;
	
	// Used in mobile implementation
	private float xMax;
	private float yMax;
	private float xCoordinate;
	private float yCoordinate;
	private float nodeRange;
	protected int port;
	protected String ipAddress;
	protected Simulation simNode;
	
	public Node (int nodeID, int port, String ipAddress, int[] dimensions) throws InterruptedException {
		this.nodeID = nodeID;
		this.port = port;
		this.ipAddress = ipAddress;
		this.nodeValue = nodeID;
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
			if(!(neighbors.contains(message.getNode()))) {
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
		
		distanceBetweenNodes = distanceBetweenNodes(node);
		
		if(distanceBetweenNodes <= node.nodeRange) {
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
	public boolean isElectionActive() {
		return electionActive;
	}
	public void setElectionActive(boolean electionActive) {
		this.electionActive = electionActive;
	}
	public int isParentActive() {
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

	public int getComputationIndex() {
		return computationIndex;
	}

	public void setComputationIndex(int computationIndex) {
		this.computationIndex = computationIndex;
	}

	public boolean isAckSent() {
		return ackSent;
	}

	public void setAckSent(boolean ackSent) {
		this.ackSent = ackSent;
	}

	public HashSet<Node> getWaitingACK() {
		return waitingACK;
	}

	public void setWaitingACK(HashSet<Node> waitingACK) {
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

	public int getParentActive() {
		return parentActive;
	}
	
}
