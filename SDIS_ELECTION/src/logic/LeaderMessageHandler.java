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
	
	public void sendMessage(logic.MessageType messageType, int addresseeId) {
		new Handler(this.node, messageType, addresseeId).start();
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
		
		// If not send leader messages to neighbours except to the message sender's id
		Iterator<Integer> i=node.getNeighbors().iterator();
		while(i.hasNext()) {
			int temp = i.next();
			if(!(temp == leaderMessage.getIncomingId())) {
				// Send Leader Message to current selected neighbour
				sendMessage(MessageType.LEADER, temp);
				System.out.println("Leader Message from " + node.getNodeID() + " to " + temp);
			}
		}
	}
}
