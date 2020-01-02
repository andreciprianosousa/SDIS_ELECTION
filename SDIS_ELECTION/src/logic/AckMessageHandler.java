package logic;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class AckMessageHandler extends Thread {

	protected Node node;
	protected AckMessage ackMessage;

	private static final boolean DEBUG = true;

	public AckMessageHandler(Node node, AckMessage ackMessage) {

		// Assume initialization of the node parameters was done previously
		// in the node creation or by default values
		this.node = node;
		this.ackMessage = ackMessage;
	}

	// "Send Message" for type Leader
	public void sendMessage(logic.MessageType messageType, Set<Integer> mailingList) {
		if (mailingList.isEmpty()) {
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
	public synchronized void run() { // Needs synchronization because 2+ acks may arrive at same time and alter
										// hashset simultaneously, other handlers also have this

		// Assuming this Ack was intended for me in the first place and I'm in the same
		// Computation Index
		if (!(ackMessage.getCp().getNum() == node.getComputationIndex().getNum()
				&& ackMessage.getCp().getValue() == node.getComputationIndex().getValue()
				&& ackMessage.getCp().getId() == node.getComputationIndex().getId())) {
			return; // Ignore ack from other CP that no longer matters
		}

		if (DEBUG)
			System.out.println("Receiving ack from " + ackMessage.getIncomingId());

		// When this node receives an Ack Message, updates waiting Acks first
		if ((node.getWaitingAcks().contains(ackMessage.getIncomingId()))) {

			node.getWaitingAcks().remove(ackMessage.getIncomingId());

			// Then, update this node stored value and stored id if value is bigger
			if (ackMessage.getStoredValue() > node.getStoredValue()) {
				node.setStoredValue(ackMessage.getStoredValue());
				node.setStoredId(ackMessage.getStoredID());
			}
		} else {
			return;
		}

		// If this was the last acknowledge needed, then send to parent my own ack and
		// update my parameters
		if ((node.getWaitingAcks().isEmpty()) && (node.getAckStatus() == true)) {
			if (node.getParentActive() != -1) {
				node.setAckStatus(false);
				// send ACK message to parent stored in node.getParentActive()
				if (DEBUG)
					System.out.println(
							"Sending to my parent " + node.getParentActive() + " the Leader Id " + node.getStoredId());
				sendMessage(logic.MessageType.ACK, node.getParentActive());

				// This ACK is sent only if in election
				node.networkEvaluation.counterMessagesInElection(node.getComputationIndex().getId(),
						logic.MessageType.ACK);
			}

			// or prepare to send leader message if this node is the source of the election
			// (if it has no parent)
			else {

				node.setAckStatus(true);
				node.setElectionActive(false);
				node.setLeaderID(node.getStoredId());
				node.setLeaderValue(node.getStoredValue());
				node.setStoredId(node.getNodeID());
				node.setStoredValue(node.getNodeValue());
				System.out.println("========================>   Leader agreed upon: " + node.getLeaderID());

				// Metric 1 - Election Timer
				if (node.getNodeID() == node.getComputationIndex().getId()) {
					node.networkEvaluation.setEndElectionTimer(node.getComputationIndex().getId());
					node.networkEvaluation.getElectionTimer(node.getComputationIndex().getId());
				}
				// Metric 3 - Without Leader Timer
				node.networkEvaluation.setEndWithoutLeaderTimer();
				node.networkEvaluation.getWithoutLeaderTimer();

				// send Leader message to all children
				Iterator<Integer> i = node.getNeighbors().iterator();
				node.getWaitingAcks().clear(); // clear this first just in case

				Set<Integer> toSend = Collections.synchronizedSet(new HashSet<Integer>());
				while (i.hasNext()) {
					Integer temp = i.next();
					toSend.add(temp);
				}
				// Send Election Message to all neighbours
				if (DEBUG)
					System.out.println("Sending leader to all nodes.\n-----------------------------");

				sendMessage(logic.MessageType.LEADER, toSend);
			}
		}
	}
}