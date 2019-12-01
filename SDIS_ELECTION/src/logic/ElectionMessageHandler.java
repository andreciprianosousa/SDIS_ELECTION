package logic;

import java.util.Iterator;

import network.*;

public class ElectionMessageHandler extends Thread {

	protected Node node;
	// protected Message message;

	public ElectionMessageHandler(Node node /* , Message message */) {

		// Assume initialization of the node parameters was done previously
		// in the node creation or by default values
		this.node = node;

		// this.message = message;
	}

	@Override
	public void run() {

		// If I'm already in the process of electing:
		// If it's the same election we're talking about, send immediate ack to the sender id of the message
		// If not, check computation index and act accordingly 
		// Need id of message sender
		 
		if(node.isElectionActive()) { //true means node is in an ongoing election
			
			if((/*message CP num */ == node.getCP().getNum()) && (node.getCP().getValue()==/*message CP value */) && (node.getCP().getId()==/*message CP id */)) {
			// send ACK Message using NodeListener to the same id of the message, also passing storedValue and storedId
			// Using multicast will send to every neighbour! No issue unless it's parent, which it cannot be sent to
			// Possible solution on my part: in message sent here also send the desired recipient(s) so the receiver knows if it is addressed directly		
			}
			else{
				// If I have priority in Computation Index, send to sender of message new Election in my terms
				if( (/*message CP value*/ < node.getCP().getValue() ) || ( (/*message CP value*/ == node.getCP().getValue()) && (/*message CP id*/ < node.getCP().getId()) )) {
					// send election message to sender with my stored id, value and CP stuff
				}
				else {
					// If the sender has priority, I clean myself and propagate its message
					node.getCP().setNum(/*message CP num*/);
				    node.getCP().setId(/*message CP id*/); 
				    node.getCP().setValue(/*message CP value*/);
				    
				    node.setParentActive(/*message sender id*/);
					
					// If it this node has no neighbours, send ack to parent and set ackSent to false right away
					if(node.getNeighbors().isEmpty()) {
						node.setAckStatus(false);
						// send Ack message to sender/parent with my stored id and value
					}
					else {	
						node.setAckStatus(true); // true means it has not sent ack to parent, in ack handler we will put this to false again
						
						// For every neighbour except parent, put them in waitingAck 
						// Iterator has flaws! If hash set of neighbours was of Integers, I could do this way more reliably
						Iterator<Node> i=node.getNeighbors().iterator();
						while(i.hasNext()) {
							// TODO CHECK PARENT, with this hash set it's way more difficult than it need to be
							node.getWaitingAcks().add(i.next());
							// Send Election Message to current selected neighbour
							}
						}
				}
			}
				
		}
		// If I'm not in an election, setup myself and send ACKs to neighbours
		else {
			node.setElectionActive(true);
			node.setParentActive(/*message sender id */);
			
			// IMPORTANT -> if this node starts an election after other elections in the past, 
			// don't forget to update these values to a bigger num but with id equal to this node's
			// because id is just a tie breaker
		    node.getCP().setNum(/*message CP num*/);
		    node.getCP().setId(/*message CP id*/); 
		    node.getCP().setValue(/*message CP value*/);
			
			// If it this node has no neighbours, send ack to parent and set ackSent to false right away
			if(node.getNeighbors().isEmpty()) {
				node.setAckStatus(false);
				// send Ack message to sender/parent with my stored id and value
			}
			else {	
				node.setAckStatus(true); // true means it has not sent ack to parent, in ack handler we will put this to false again
				
				// For every neighbour except parent, put them in waitingAck 
				// Iterator has flaws! If hash set of neighbours was of Integers, I could do this way more reliably
				Iterator<Node> i=node.getNeighbors().iterator();
				while(i.hasNext()) {
					// TODO CHECK PARENT, with this hash set it's way more difficult than it need to be
					node.getWaitingAcks().add(i.next());
					// Send Election Message to current selected neighbour
					}
				}
			}
		}
}}
