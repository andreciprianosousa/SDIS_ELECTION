package logic;

import java.util.Iterator;

public class LeaderMessageHandler extends Thread{
	
	protected Node node;
	protected Node incomingNode;
	
	public LeaderMessageHandler(Node node, Node incomingNode) {
		
		// Assume initialization of the node parameters was done previously
		// in the node creation or by default values
		this.node = node;
		this.incomingNode = incomingNode;
	}
	
	@Override
	public synchronized void run() {
		
		// If this node receives leader message, update parameters accordingly
		node.setElectionActive(false);
		node.setLeaderID(incomingNode.getLeaderID());
		node.setParentActive(-1);
		node.setAckStatus(true); // may change...
		node.setStoredValue(incomingNode.getStoredValue());
		node.setStoredId(incomingNode.getStoredId());
		
		// Then send election messages to neighbours except to the message sender's id
		Iterator<Node> i=node.getNeighbors().iterator();
		while(i.hasNext()) {
			Node temp = i.next();
			if(!(temp.getNodeID() == incomingNode.getNodeID())) {
				node.getWaitingAcks().add(temp);
				// Send Election Message to current selected neighbour
			}
		}
		
	}
}
