package sdis;

import java.util.ArrayList;

public class Node {
	
	// Used in all implementations
	private int value; // Desirability of this node as the leader of the network
	private int id; // Used to identify participants during election process and to break ties between nodes with same value
	private ArrayList<Integer> neighbours; // Immediate neighbours found during network topology creation
	private int parentNode; // Used to know paresnt's node in spawning tree
	private boolean inElection; // Indicates if this node is currently in election or not
	private ArrayList<Integer> waitingReceptionFrom; // Set of nodes from which this node has yet to hear an Ack from
	
	
	// Used in mobile implementation
	private float xCoordinate;
	private float yCoordinate;
	
	Node(int id){
		this.id = id;
		this.value = id; // First approach, might change later based on coordinates or other future parameters
		this.inElection = false;
		this.neighbours = new ArrayList<Integer>();
		this.parentNode = -1; // Assume node id starts in 1 or 0
		this.waitingReceptionFrom = new ArrayList<Integer>();
	}
	
	public int getId() {
		return this.id;
	}
	
	
	
	
	
}
