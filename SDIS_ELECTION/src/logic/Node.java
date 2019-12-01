package logic;

import java.util.HashSet;

import network.*;

public class Node {
	
	protected int nodeID;
	protected int computationIndex;
	protected boolean electionActive;
	protected int parentActive;
	protected boolean ackSent;
	protected int leaderID;
	protected HashSet<Node> neighbors;
	protected HashSet<Node> waitingACK;
	protected float nodeValue;
	
	protected int port;
	protected String ipAddress;
	
	public Node (int nodeID, int port, String ipAddress) {
		this.nodeID = nodeID;
		this.port = port;
		this.ipAddress = ipAddress;
		this.nodeValue = nodeID;
		new NodeListener(this).start();
		new NodeTransmitter(this).start();
		
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


}
