package logic;

import java.util.HashSet;
import java.util.Iterator;

import javax.swing.DebugGraphics;

public class LeaderMessageHandler extends Thread{
	
	protected Node node;
	protected LeaderMessage leaderMessage;

	private static final boolean DEBUG = true; 
	
	public LeaderMessageHandler(Node node, LeaderMessage lm) {
		
		// Assume initialization of the node parameters was done previously
		// in the node creation or by default values
		this.node = node;
		this.leaderMessage = lm;
	}
	
	public void sendMessage(logic.MessageType messageType, HashSet<Integer> mailingList) {
		if(mailingList.isEmpty()) {
			System.out.println("Mailing List is Empty");
			return;
		}
		new Handler(this.node, messageType, mailingList).start();
	}
	
	
	public void sendLeaderMessage() {
		
		HashSet<Integer> mailingList = new HashSet<Integer>();
		Iterator<Integer> i=node.getNeighbors().iterator();
		while(i.hasNext()) {
			int temp = i.next();
			if(!(temp == leaderMessage.getIncomingId())) {
				mailingList.add(temp);
			}
		}
		
		if(!(leaderMessage.isSpecial())) {
			sendMessage(MessageType.LEADER, mailingList);
		}
		else {
			sendMessage(MessageType.LEADER_SPECIAL, mailingList);
		}
	}
	
	@Override
	public synchronized void run() {
		
			// If leader is special, don't care about parent and child stuff
			if(!(leaderMessage.isSpecial())) {
				if(!(leaderMessage.getIncomingId() == node.getParentActive())) {
					
					if(DEBUG)
						System.out.println("Ignoring leader message from " + leaderMessage.getIncomingId()+ "\n-----------------------------");
					
					return;
				}
			}
			
			if(leaderMessage.getStoredID() == node.getLeaderID()) {
				
				if(DEBUG)
					System.out.println("Do nothing.");
				
				return;
			} else { // Either leaderMessage ID > node Leader Ou other way around
				
			
			// If this node receives leader message, update parameters accordingly if necessary
			// And has a different leader, broadcasts the leader msgs
			if(node.isElectionActive() && (!(leaderMessage.isSpecial()))) {
				
				if(leaderMessage.getStoredID() < node.getLeaderID()) {
					System.out.println("Hey! I think you're wrong! Maybe it's time to set a new election");
					System.out.println("--------!!!!!!!!!!!---------!!!!!!!!!---------!!!!!!!!----------");
					
					//sendLeaderMessage();

					return;
				}
			
				node.setElectionActive(false);
				node.setLeaderID(leaderMessage.getStoredID());
				node.setStoredValue(leaderMessage.getStoredValue());
				node.setParentActive(-1);
				node.setAckStatus(true);
				node.setStoredId(node.getNodeID());

				
				System.out.println("Node " + node.getNodeID() + "'s leader is " + node.getLeaderID());
				System.out.println("CP(num/value/id): " + node.getComputationIndex().getNum()+ " - " + node.getComputationIndex().getValue()+ " - " + node.getComputationIndex().getId());
				System.out.println("-----------------------------");
				
			
				// If my only neighbour is my parent, don't propagate leader message and just return
				if(node.getNeighbors().size() == 1) {
					return;
				}
				
				// If not, send leader messages to neighbours except to the message sender's id
				// If leader message is special, send leader special message instead of normal
				sendLeaderMessage();
			}
			else {
				
				if(leaderMessage.getStoredID() <= node.getLeaderID()) {
					System.out.println("Men, that guy is weak. Do nothing!");
					
					return;
				}
				
				//node.setElectionActive(false);
				node.setLeaderID(leaderMessage.getStoredID());
				node.setStoredValue(leaderMessage.getStoredValue());
				//node.setParentActive(-1);
				//node.setAckStatus(false);
				node.setStoredId(node.getNodeID());
				
				
				// If my only neighbour is my parent, don't propagate leader message and just return 
				if(node.getNeighbors().size() == 1) {
					
					return;
				}
				
				// If not, send leader messages to neighbours except to the message sender's id
				sendLeaderMessage();
			}
		}
	}
}
