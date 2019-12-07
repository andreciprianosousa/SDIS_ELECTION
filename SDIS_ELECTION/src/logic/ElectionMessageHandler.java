package logic;

import java.util.Iterator;

import network.*;

public class ElectionMessageHandler extends Thread {

	protected Node node;
	protected ElectionMessage electionMessage;

	public ElectionMessageHandler(Node node, ElectionMessage em) {

		// Assume initialization of the node parameters was done previously
		// in the node creation or by default values
		this.node = node;
		this.electionMessage = em;
	}
	
	public void sendAckMessage() {
		new Handler(this.node, MessageType.ACK).start();
	}
	public void sendElectionMessage() {
		new Handler(this.node, MessageType.ELECTION).start();
	}

	@Override
	public synchronized void run() {

		// If I'm already in the process of electing:
		// If it's the same election we're talking about, send immediate ack to the sender id of the message
		// If not, check computation index and act accordingly 
		// Need id of message sender
		 
		if(node.isElectionActive()) { //true means node is in an ongoing election
			
			//If the election sent to me is the same as my current election
			if((electionMessage.getComputationIndex().getNum() == node.getComputationIndex().getNum()) && (node.getComputationIndex().getValue()== electionMessage.getComputationIndex().getValue()) && (node.getComputationIndex().getId()==electionMessage.getComputationIndex().getId())) {
			// send ACK Message to the same id of the message, also passing storedValue and storedId
			sendAckMessage();	
			}
			else{
				// If I have priority in Computation Index, send to sender of message new Election in my terms
				if( (electionMessage.getComputationIndex().getValue() < node.getComputationIndex().getValue() ) || ( (electionMessage.getComputationIndex().getValue() == node.getComputationIndex().getValue()) && (electionMessage.getComputationIndex().getId() < node.getComputationIndex().getId()) )) {
					// send election message to sender with my stored id, value and CP stuff
					sendElectionMessage();
				}
				else {
					// If the sender has priority, I clean myself and propagate its message
				    node.getComputationIndex().setNum(electionMessage.getComputationIndex().getNum());
				    node.getComputationIndex().setId(electionMessage.getComputationIndex().getId()); 
				    node.getComputationIndex().setValue(electionMessage.getComputationIndex().getValue());
				    
				    node.setParentActive(electionMessage.getNodeID());
					
					// If it this node has no neighbours, send ack to parent and set ackSent to false right away
					if(node.getNeighbors().isEmpty()) {
						node.setAckStatus(false);
						// send Ack message to sender/parent with my stored id and value
						sendAckMessage();
					}
					else {	
						node.setAckStatus(true); // true means it has not sent ack to parent, in ack handler we will put this to false again
						
						// For every neighbour except parent, put them in waitingAck 
						Iterator<Integer> i=node.getNeighbors().iterator();
						while(i.hasNext()) {
							int temp = i.next();
							if(!(temp == node.getParentActive())) {
								node.getWaitingAcks().add(temp);
								// Send Election Message to current selected neighbour
								sendElectionMessage();
							}
						}
					}
				}
			}
				
		}
		// If I'm not in an election, setup myself and send ACKs to neighbours
		else {
			node.setElectionActive(true);
			node.setParentActive(electionMessage.getNodeID()); 
			
			// IMPORTANT -> if this node starts an election after other elections in the past, 
			// don't forget to update these values to a bigger num but with id equal to this node's
			// because id is just a tie breaker
		    node.getComputationIndex().setNum(electionMessage.getComputationIndex().getNum());
		    node.getComputationIndex().setId(electionMessage.getComputationIndex().getId()); 
		    node.getComputationIndex().setValue(electionMessage.getComputationIndex().getValue());
			
			// If this node has no neighbours except parent, send ack to parent and set ackSent to false right away
			if(node.getNeighbors().size() == 1) {
				node.setAckStatus(false);
				// send Ack message to sender/parent with my stored id and value
				sendAckMessage();
			}
			else {	
				node.setAckStatus(true); // true means it has not sent ack to parent, in ack handler we will put this to false again
				
				// For every neighbour except parent, put them in waitingAck 
				Iterator<Integer> i=node.getNeighbors().iterator();
				while(i.hasNext()) {
					Integer temp = i.next();
					if(!(temp == node.getParentActive())) {
						node.getWaitingAcks().add(temp);
						// Send Election Message to current selected neighbour
						sendElectionMessage();
					}
				}
			}
		}
	}
}
