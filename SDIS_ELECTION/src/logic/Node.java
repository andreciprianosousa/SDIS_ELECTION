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
	protected HashSet<Integer> neighbors;
	protected HashSet<Node> waitingACK;
	protected float nodeValue;
	
	// Used in mobile implementation
	private float xMax;
	private float yMax;
	private int xCoordinate;
	private int yCoordinate;
	private int nodeRange;
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
<<<<<<< Upstream, based on branch 'network' of https://github.com/andreciprianosousa/SDIS_ELECTION.git
			if(!(neighbors.contains(message.getNode()))) {
=======
			if(!(neighbors.contains( message.getNodeID()))) {
>>>>>>> 6c22e4e simplified messages
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
	
<<<<<<< Upstream, based on branch 'network' of https://github.com/andreciprianosousa/SDIS_ELECTION.git
	public boolean isInsideNeighborhood(Node node) {
=======
	public boolean isInsideNeighborhood(int neighborID, int xNeighbor, int yNeighbor) {
		int range;
>>>>>>> 6c22e4e simplified messages
		float distanceBetweenNodes;
		
<<<<<<< Upstream, based on branch 'network' of https://github.com/andreciprianosousa/SDIS_ELECTION.git
		distanceBetweenNodes = distanceBetweenNodes(node);
=======
		distanceBetweenNodes = (float) Math.sqrt(Math.pow((xNeighbor-xCoordinate),2) + Math.pow((yNeighbor-yCoordinate),2));
>>>>>>> 6c22e4e simplified messages
		
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
	public HashSet<Integer> getNeighbors() {
		return neighbors;
	}
	public void setNeighbors(HashSet<Integer> neighbors) {
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
