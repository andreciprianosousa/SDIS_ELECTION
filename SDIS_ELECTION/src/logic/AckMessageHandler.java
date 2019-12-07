package logic;

import java.awt.TrayIcon.MessageType;
import java.util.Iterator;

public class AckMessageHandler extends Thread{

	protected Node node;
	protected AckMessage ackMessage;
	
	
	public AckMessageHandler(Node node, AckMessage ackMessage) {
		
		// Assume initialization of the node parameters was done previously
		// in the node creation or by default values
		this.node = node;
		this.ackMessage = ackMessage;
	}
	
	public void sendMessage(logic.MessageType messageType, int addresseeId) {
		new Handler(this.node, messageType, addresseeId).start();
	}
	
	@Override
	public synchronized void run() { // Needs synchronization because 2+ acks may arrive at same time and alter hashset simultaneously, other handlers also have this
		
		// Assuming this Ack was intended for me in the first place
		// When this node receives an Ack Message, updates waiting Acks first
		
		if(!(node.getWaitingAcks().isEmpty())) {
			node.getWaitingAcks().remove(ackMessage.getIncomingId());
		}
		// caso das computations simultaneas: este nó pode entrar nouta eleição e já ter recebido aluns acks 
		// nesse caso manda de novo election para dar update dos CP values dos vizinhos, mas o valor dos
		// acks seriam os mesmos, assumindo vizinhos estáticos: se vizinho muito forte que deu ack saiu da network
		// e este nó começa nova eleição, este valor pode não vir a ser superado e a propagar um nó fantasma!!
		
		// Then, update this node stored value and stored id if value is bigger
		if(ackMessage.getStoredValue() > node.getStoredValue()) {
			node.setStoredValue(ackMessage.getStoredValue() );
			node.setStoredId(ackMessage.getStoredID());
		}
		
		// If this was the last acknowledge needed, then send to parent my own ack and update my parameters
		// or prepare to send leader message if this node is the source of the election (if it has no parent, for now)
		if(node.getWaitingAcks().isEmpty()) {
			if(node.getParentActive() != -1) {
				node.setAckStatus(false);
				// send ACK message to parent stored in node.getParentActive()
				sendMessage(logic.MessageType.ACK, node.getParentActive());
			}
			else {
				node.setAckStatus(true); // may change
				node.setElectionActive(false);
				node.setLeaderID(node.getStoredId());
				// send Leader message to all children, needs id and value of leader chosen (stored already)
				Iterator<Integer> i=node.getNeighbors().iterator();
				while(i.hasNext()) {
					Integer temp = i.next();
					if(!(temp == node.getParentActive())) {
						node.getWaitingAcks().add(temp);
						// Send Election Message to current selected neighbour
						sendMessage(logic.MessageType.LEADER, temp);	
					}
				}
			}
		}		
	}
}
