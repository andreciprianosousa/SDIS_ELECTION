package logic;

import java.util.HashSet;
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
	
	public void sendMessage(logic.MessageType messageType, HashSet<Integer> mailingList) {
		new Handler(this.node, messageType, mailingList).start();
	}
	
	@Override
	public synchronized void run() {
		
			// If leader is special, don't care about parent and child stuff
			if(!leaderMessage.isSpecial()) {
				if(!(leaderMessage.getIncomingId()==node.getParentActive())) {
					System.out.println("Ignoring leader message from " + leaderMessage.getIncomingId()+ "\n-----------------------------");
					return;
				}
			}
			
			if(leaderMessage.getStoredID() == node.getLeaderID()) {
				System.out.println("Do nothing.");
				return;
			}
			
			node.setLeaderID(leaderMessage.getStoredID());
			
			// If this node receives leader message, update parameters accordingly if necessary
			// And has a different leader, broadcasts the leader msgs
			//if(node.isElectionActive()) {
				node.setElectionActive(false);
				node.setLeaderID(leaderMessage.getStoredID());
				node.setParentActive(-1);
				node.setAckStatus(true);
				node.setStoredValue(leaderMessage.getStoredValue());
				node.setStoredId(node.getNodeID());
				
				System.out.println("Node " + node.getNodeID() + "'s leader is " + node.getLeaderID());
				System.out.println("CP(num/value/id): " + node.getComputationIndex().getNum()+ " - " + node.getComputationIndex().getValue()+ " - " + node.getComputationIndex().getId());
				System.out.println("-----------------------------");
				
				// If my only neighbour is my parent, don't propagate leader message and just return
				if(node.getNeighbors().size() == 1) {
					return;
				}
				
				// If not, send leader messages to neighbours except to the message sender's id
				HashSet<Integer> mailingList = new HashSet<Integer>();
				Iterator<Integer> i=node.getNeighbors().iterator();
				while(i.hasNext()) {
					int temp = i.next();
					if(!(temp == leaderMessage.getIncomingId())) {
						mailingList.add(temp);
					}
				}
				
				// If leader message is special, send leader special message instead of normal
				if(!leaderMessage.isSpecial()) {
					sendMessage(MessageType.LEADER, mailingList);
				}
				else {
					sendMessage(MessageType.LEADER_SPECIAL, mailingList);
				}
			}
	//}
}
