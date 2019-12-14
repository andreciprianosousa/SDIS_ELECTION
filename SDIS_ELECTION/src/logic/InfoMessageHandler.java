package logic;

import java.util.HashSet;
import java.util.Iterator;

public class InfoMessageHandler extends Thread{

	protected Node node;
	protected InfoMessage infoMessage;

	private static final boolean DEBUG = false; 
	
	public InfoMessageHandler(Node node, InfoMessage infoMessage) {
		
		// Assume initialization of the node parameters was done previously
		// in the node creation or by default values
		this.node = node;
		this.infoMessage = infoMessage;
		
	}
	
	// "Send Message" for type Info
	public void sendMessage(logic.MessageType messageType, int addresseeId) {
		new Handler(this.node, messageType, addresseeId).start();
	}
	
	// "Send Message" for type Leader Special
	public void sendMessage(logic.MessageType messageType, HashSet<Integer> mailingList) {
		if(mailingList.isEmpty()) {
			System.out.println("Mailing List is Empty");
			return;
		}
		new Handler(this.node, messageType, mailingList).start();
	}
	
	@Override
	public synchronized void run() {
		
		// If value of leader from exchanging messages is bigger, propagate that leader in "broadcast"
		
		// anterior: (infoMessage.getStoredValue() > node.getStoredValue()) || ((infoMessage.getStoredValue() == node.getStoredValue()) && (infoMessage.getLeaderId() > node.getLeaderID()))
		if((infoMessage.getStoredValue() > node.getStoredValue()) || (infoMessage.getLeaderId() > node.getLeaderID())) {
			node.setLeaderID(infoMessage.getLeaderId());
			node.setStoredValue(infoMessage.getStoredValue());
			node.setStoredId(node.getNodeID());
			System.out.println("Leader changed to: " + node.getLeaderID() + " due to exchanging messages with " + infoMessage.getIncomingId());
			
			// send "special "Leader message to all neighbours
			Iterator<Integer> i=node.getNeighbors().iterator();
			
			HashSet<Integer> toSend = new HashSet<Integer>();
			while(i.hasNext()) { 
				Integer temp = i.next();
				if(temp != infoMessage.getIncomingId()) {
					toSend.add(temp);
				}
			}
			// Send Election Message to all neighbours, except myself
			// If I have no neighbours except node I exchanged info messages with, no need to send leader messages
			if(!toSend.isEmpty()) {
				
				if(DEBUG)
					System.out.println("Sending special leader to all nodes.");
				
				sendMessage(logic.MessageType.LEADER_SPECIAL, toSend);
			}

		}
		// If value is the same but their leader ID is bigger, also send message
		// If not, send a message back saying that the other node should send the leader message instead with my leader
		else {
			
			if(DEBUG)
				System.out.println("Sending back stronger leader.\n-----------------------------");
			
			sendMessage(logic.MessageType.INFO, infoMessage.getIncomingId());
		}
	}
}