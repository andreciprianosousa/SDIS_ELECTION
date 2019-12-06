package logic;

import java.util.Iterator;

public class LeaderMessageHandler extends Thread{
	
	protected Node node;
	protected LeaderMessage leaderMessage;
	
	public LeaderMessageHandler(Node node, LeaderMessage lm) {
		
		// Assume initialization of the node parameters was done previously
		// in the node creation or by default values
		this.node = node;
		this.leaderMessage = lm;
	}
	
	@Override
	public synchronized void run() {
		
		// If this node receives leader message, update parameters accordingly
		node.setElectionActive(false);
		node.setLeaderID(leaderMessage.getStoredID());
		node.setParentActive(-1);
		node.setAckStatus(true); // may change...
		node.setStoredValue(leaderMessage.getStoredValue());
		node.setStoredId(leaderMessage.getStoredID());
		
		// If my only neighbour is my parent, don't send message and just return
		if(node.getNeighbors().size() == 1) {
			return;
		}
		
		// If not send election messages to neighbours except to the message sender's id
		Iterator<Node> i=node.getNeighbors().iterator();
		while(i.hasNext()) {
			Node temp = i.next();
			if(!(temp.getNodeID() == leaderMessage.getIncomingId())) {
				// Send Election Message to current selected neighbour
			}
		}
		
		
	}
}