package logic;

import java.util.HashSet;

import network.*;

public class Node {
	
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
	
	protected int port;
	protected String ipAddress;
	
	public Node (int nodeID, int port, String ipAddress) {
		this.nodeID = nodeID;
		this.port = port;
		this.ipAddress = ipAddress;
		this.nodeValue = nodeID;
		this.storedValue = this.nodeValue;
		this.storedId = this.nodeID;
		// don't forget to increase num when starting election
		this.computationIndex = new ComputationIndex(this.getNodeID(), 0, this.getNodeValue()); 
		
		
		new NodeListener(this).start();
		new NodeTransmitter(this).start();
		
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

}
