package logic;

import java.util.HashSet;
import java.util.Iterator;

import org.w3c.dom.traversal.NodeIterator;

public class AckMessageHandler extends Thread{

	protected Node node;
	protected AckMessage ackMessage;

	private static final boolean DEBUG = false; 


	public AckMessageHandler(Node node, AckMessage ackMessage) {

		// Assume initialization of the node parameters was done previously
		// in the node creation or by default values
		this.node = node;
		this.ackMessage = ackMessage;
	}


	// "Send Message" for type Leader - Needs HashSet to send to a group of Nodes
	public void sendMessage(logic.MessageType messageType, HashSet <Integer> mailingList) {
		if(mailingList.isEmpty()) {
			System.out.println("Mailing List is Empty");
			return;
		}
		new Handler(this.node, messageType, mailingList).start();
	}

	// "Send Message" for type Ack
	public void sendMessage(logic.MessageType messageType, int addresseeId) {
		new Handler(this.node, messageType, addresseeId).start();
	}


	@Override
	public synchronized void run() { // Needs synchronization because 2+ acks may arrive at same time and alter hashset simultaneously, other handlers also have this

		// Assuming this Ack was intended for me in the first place
		// When this node receives an Ack Message, updates waiting Acks first
		if((node.getWaitingAcks().contains(ackMessage.getIncomingId()))) {
			node.getWaitingAcks().remove(ackMessage.getIncomingId());
			
			//System.out.println("Node: " + node.getNodeID() + " // => " + node.getWaitingAcks().toString());
			//System.out.println("ACKS " + node.getNodeID() + ": " + node.getWaitingAcks().size() + " || Retirado = " + ackMessage.getIncomingId());

			// Then, update this node stored value and stored id if value is bigger
			if(ackMessage.getStoredValue() > node.getStoredValue()) {
				node.setStoredValue(ackMessage.getStoredValue());
				node.setStoredId(ackMessage.getStoredID());
			} 			
		} else {
			return;
		}
		
		// If this was the last acknowledge needed, then send to parent my own ack and update my parameters
		// 	if(node.getWaitingAcks().isEmpty() && (node.getAckStatus() == true)) {
		if((node.getWaitingAcks().isEmpty()) && (node.getAckStatus() == true)) {
			if(node.getParentActive() != -1) {
				node.setAckStatus(false);
				// send ACK message to parent stored in node.getParentActive()
				sendMessage(logic.MessageType.ACK, node.getParentActive());
				
				if(DEBUG)
					System.out.println("Sending to my parent " + node.getParentActive() + " the Leader Id " + node.getStoredId());
			}
			// or prepare to send leader message if this node is the source of the election (if it has no parent)
			else {
				if(node.getStoredValue() < node.getLeaderID()) {
					node.setStoredValue(node.getNodeValue());
					node.setStoredId(node.getLeaderID());
				}
				
				node.setAckStatus(true);
				node.setElectionActive(false);
				node.setLeaderID(node.getStoredId());
				System.out.println("========================>   Leader agreed upon: " + node.getLeaderID());
				
				// send Leader message to all children
				Iterator<Integer> i=node.getNeighbors().iterator();
				node.getWaitingAcks().clear(); // clear this first just in case
				
				HashSet<Integer> toSend = new HashSet<Integer>();
				while(i.hasNext()) { 
					Integer temp = i.next();
					toSend.add(temp);
				}
				// Send Election Message to all neighbours, except myself
				
				if(DEBUG)
					System.out.println("Sending leader to all nodes.\n-----------------------------");
				
				sendMessage(logic.MessageType.LEADER, toSend);
			}
		}
	}
}
