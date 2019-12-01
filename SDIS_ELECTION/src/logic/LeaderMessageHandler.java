package logic;

public class LeaderMessageHandler extends Thread{
	
	protected Node node;
	//protected Message message;
	
	public LeaderMessageHandler(Node node /*, Message message */) {
		
		// Assume initialization of the node parameters was done previously
		// in the node creation or by default values
		this.node = node;
		
		//this.message = message;
		
	}
	
	@Override
	public void run() {
		
		// If this node receives leader message, update parameters accordingly
		node.setElectionActive(false);
		node.setLeaderID(/*message leader id*/);
		node.setParentActive(-1);
		node.setAckStatus(true); // may change...
		node.setStoredValue(/*message leader value*/);
		node.setStoredId(/*message leader id*/);
		
		// Then send election messages to neighbours except to the message sender's id
		// Use iterator
		
	}
}
