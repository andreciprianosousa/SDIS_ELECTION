package logic;

import java.util.Iterator;
import java.util.Set;

public class ElectionMessageHandler extends Thread {

	protected Node node;
	protected ElectionMessage electionMessage;

	private static final boolean DEBUG = true;

	public ElectionMessageHandler(Node node, ElectionMessage em) {

		// Assume initialization of the node parameters was done previously
		// in the node creation or by default values
		this.node = node;
		this.electionMessage = em;
	}

	public void sendMessage(logic.MessageType messageType, Set<Integer> mailingList) {
		if (mailingList.isEmpty()) {
			System.out.println("Mailing List is Empty");
			return;
		}
		new Handler(this.node, messageType, mailingList).start();
	}

	public void sendMessage(logic.MessageType messageType, int addresseId) {
		new Handler(this.node, messageType, addresseId).start();
	}

	@Override
	public synchronized void run() {

		// If I'm already in the process of electing:
		// If it's the same election we're talking about, send immediate ack to the
		// sender id of the message
		// If not, check computation index and act accordingly
		if (node.isElectionActive()) { // true means node is in an ongoing election

			// If the election sent to me is the same as my current election
			if ((electionMessage.getComputationIndex().getNum() == node.getComputationIndex().getNum())
					&& (node.getComputationIndex().getValue() == electionMessage.getComputationIndex().getValue())
					&& (node.getComputationIndex().getId() == electionMessage.getComputationIndex().getId())) {
				// send ACK Message to the same id of the message, also passing storedValue and
				// storedId

				if (DEBUG)
					System.out.println(
							"Already in same election! Sending immediate ack to " + electionMessage.getIncomingId());

				sendMessage(logic.MessageType.ACK, electionMessage.getIncomingId());
			} else {
				// If I have priority in Computation Index, send to sender of message new
				// Election in my terms
				if ((electionMessage.getComputationIndex().getValue() < node.getComputationIndex().getValue())
						|| ((electionMessage.getComputationIndex().getValue() == node.getComputationIndex().getValue())
								&& (electionMessage.getComputationIndex().getId() < node.getComputationIndex()
										.getId()))) {
					// send election message to sender with my stored id, value and CP stuff
					electionMessage.setAGroup(false);

					if (DEBUG)
						System.out
								.println("My CP is higher, sending my election to " + electionMessage.getIncomingId());

					sendMessage(logic.MessageType.ELECTION, electionMessage.getIncomingId());
				} else {

					if (DEBUG)
						System.out.println("Incoming CP is higher, propagating that election instead...");

					// If the sender has priority, I clean myself and propagate its message
					node.getComputationIndex().setNum(electionMessage.getComputationIndex().getNum());
					node.getComputationIndex().setId(electionMessage.getComputationIndex().getId());
					node.getComputationIndex().setValue(electionMessage.getComputationIndex().getValue());

					node.setParentActive(electionMessage.getIncomingId());

					// If it this node has no neighbours, send ack to parent and set ackSent to
					// false right away
					if (node.getNeighbors().isEmpty()) {
						node.setAckStatus(false);
						// send Ack message to sender/parent with my stored id and value
						sendMessage(logic.MessageType.ACK, node.getParentActive());
					} else {
						node.setAckStatus(true); // true means it has not sent ack to parent, in ack handler we will put
													// this to false again
						node.getWaitingAcks().clear();
						// For every neighbour except parent, put them in waitingAck
						Iterator<Integer> i = node.getNeighbors().iterator();
						while (i.hasNext()) {
							Integer temp = i.next();
							if (!(temp == node.getParentActive())) {
								if ((!(node.getWaitingAcks().contains(temp))) && (!(temp.toString().equals("")))) {
									node.getWaitingAcks().add(temp);
								}
							}
						}
						// Sends messages to all possible nodes
						if (!(node.getWaitingAcks().isEmpty())) {
							sendMessage(logic.MessageType.ELECTION_GROUP, node.getWaitingAcks());
						}
					}
				}
			}
		}
		// If I'm not in an election, setup myself and send ACKs to neighbours, if I
		// have any
		else {
			node.setElectionActive(true);
			node.setParentActive(electionMessage.getIncomingId());

			node.getComputationIndex().setNum(electionMessage.getComputationIndex().getNum());
			node.getComputationIndex().setId(electionMessage.getComputationIndex().getId());
			node.getComputationIndex().setValue(electionMessage.getComputationIndex().getValue());

			// If this node has no neighbours except parent, send ack to parent and set
			// ackSent to false right away
			if (node.getNeighbors().size() == 1) {
				node.setAckStatus(false);

				if (DEBUG)
					System.out.println("Sending immediate ack since I only have parent.");

				sendMessage(logic.MessageType.ACK, node.getParentActive());
			} else {
				node.setAckStatus(true); // true means it has not sent ack to parent, in ack handler we will put this to
											// false again

				// For every neighbour except parent, put them in waitingAck
				Iterator<Integer> i = node.getNeighbors().iterator();
				while (i.hasNext()) {
					Integer temp = i.next();
					if (!(temp == node.getParentActive())) {
						if (!(node.getWaitingAcks().contains(temp)) && (!(temp.toString().equals("")))) {
							node.getWaitingAcks().add(temp);
						}
					}
				}
				if (!(node.getWaitingAcks().isEmpty())) {
					sendMessage(logic.MessageType.ELECTION_GROUP, node.getWaitingAcks());
				}
			}
		}
	}
}
