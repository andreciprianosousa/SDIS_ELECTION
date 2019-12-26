package logic;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class LeaderMessageHandler extends Thread {

	protected Node node;
	protected LeaderMessage leaderMessage;

	private static final boolean DEBUG = false;

	public LeaderMessageHandler(Node node, LeaderMessage lm) {

		// Assume initialization of the node parameters was done previously
		// in the node creation or by default values
		this.node = node;
		this.leaderMessage = lm;
	}

	public void sendMessage(logic.MessageType messageType, Set<Integer> mailingList) {
		if (mailingList.isEmpty()) {
			System.out.println("Mailing List is Empty");
			return;
		}
		new Handler(this.node, messageType, mailingList).start();
	}

	public void sendLeaderMessage() {

		Set<Integer> mailingList = Collections.synchronizedSet(new HashSet<Integer>());
		Iterator<Integer> i = node.getNeighbors().iterator();
		while (i.hasNext()) {
			int temp = i.next();
			if (!(temp == leaderMessage.getIncomingId())) {
				mailingList.add(temp);
			}
		}

		if (!(leaderMessage.isSpecial())) {
			sendMessage(MessageType.LEADER, mailingList);
		} else {
			sendMessage(MessageType.LEADER_SPECIAL, mailingList);
		}
	}

	@Override
	public synchronized void run() {

		// If leader is special, don't care about parent and child stuff
		// It it isn't, if it's not my parent who's sending the leader message, then
		// reject that info.
		if (!(leaderMessage.isSpecial())) {
			if (DEBUG)
				System.out.println("Leader incoming/current parent " + leaderMessage.getIncomingId() + "/"
						+ node.getParentActive());
			if (!(leaderMessage.getIncomingId() == node.getParentActive())) {

				if (DEBUG)
					System.out.println("Ignoring leader message from " + leaderMessage.getIncomingId()
							+ "\n-----------------------------");

				return;
			}
		}

		// If this node receives leader message from parent DUE TO AN ELECTION, update
		// parameters
		// accordingly if necessary
		// And if has a different leader, broadcasts the leader msgs
		if (node.isElectionActive() && (!(leaderMessage.isSpecial()))) {

			if (DEBUG)
				System.out.println("Receiving leader assurance from " + leaderMessage.getIncomingId());

			node.setElectionActive(false);
			node.setLeaderID(leaderMessage.getStoredID());
			node.setLeaderValue(leaderMessage.getStoredValue());
			node.setParentActive(-1);
			node.setAckStatus(true);
			// Ready for new election
			node.setStoredId(node.getNodeID());
			node.setStoredValue(node.getNodeValue());

			// Set End To Timer Without Leader
			node.networkEvaluation.setEndWithoutLeaderTimer();
			node.networkEvaluation.getWithoutLeaderTimer();

			if (DEBUG) {
				System.out.println("Node " + node.getNodeID() + "'s leader is " + node.getLeaderID());
				System.out.println("CP(num/value/id): " + node.getComputationIndex().getNum() + " - "
						+ node.getComputationIndex().getValue() + " - " + node.getComputationIndex().getId());
				System.out.println("-----------------------------");
			}
			// If my only neighbour is my parent, don't propagate leader message and just
			// return
			if (node.getNeighbors().size() == 1) {
				return;
			}
			// If not, send leader message to neighbours
			sendLeaderMessage();

			// If im not in an election and message isn't from exchanging leaders, ignore
			// everything
		} else if (!(node.isElectionActive()) && !(leaderMessage.isSpecial())) {
			if (DEBUG)
				System.out.println("Not election and not special, end.");
			return;

			// If message is special and I'm not in election
		} else if (leaderMessage.isSpecial() && !(node.isElectionActive())) {

			if (leaderMessage.getStoredID() <= node.getLeaderID()) {
				System.out.println("Men, that guy is weak. Do nothing!");

				return;
			}

			node.setLeaderID(leaderMessage.getStoredID());
			node.setLeaderValue(leaderMessage.getStoredValue());
			node.setStoredId(node.getNodeID());
			node.setStoredValue(node.getNodeValue());

			// Set End To Timer Without Leader
			node.networkEvaluation.setEndWithoutLeaderTimer();
			node.networkEvaluation.getWithoutLeaderTimer();

			// If my only neighbour is my parent, don't propagate leader message and just
			// return
			if (node.getNeighbors().size() == 1) {
				return;
			}

			// If not, send leader messages to neighbours except to the message sender's id
			sendLeaderMessage();
			// If message is special and I'm in election, reject leader special message
		} else if (leaderMessage.isSpecial() && node.isElectionActive()) {
			if (DEBUG)
				System.out.println("Rejection special message while on election!");
			return;
		}
	}
}