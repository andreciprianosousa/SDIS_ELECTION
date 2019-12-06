package sdis;

import java.util.HashSet;


public class NodeTest {

	protected HashSet<NodeTest> neighbors;
	protected HashSet<NodeTest> waitingACK;
	protected int id, parent;
	
	public NodeTest(int id, int parent) {
		this.neighbors = new HashSet <NodeTest>();
		this.waitingACK = new HashSet<NodeTest>();
		this.id = id;
		this.parent = parent;
	}
	
	public HashSet<NodeTest> getNeighbors() {
		return neighbors;
	}
	public void setNeighbors(HashSet<NodeTest> neighbors) {
		this.neighbors = neighbors;
	}
	public HashSet<NodeTest> getWaitingAcks() {
		return this.waitingACK;
	}
	public void setWaitingAck(HashSet<NodeTest> waitingACK) {
		this.waitingACK = waitingACK;
	}
	public int getId() {
		return this.id;
	}
	public int getParent() {
		return this.parent;
	}
	
	
}
