package logic;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

public class Handler extends Thread {
	protected Node node;
	protected MessageType messageType;
	protected Set<Integer> mailingList; // message recipient
	protected int addresseeId; // message recipient
	protected AckMessage ackMessage = null;
	protected ElectionMessage electionMessage = null;
	protected LeaderMessage leaderMessage = null;
	protected InfoMessage infoMessage = null;
	byte[] messageToSend = new byte[2048];
	DatagramPacket datagram;

	private static boolean DEBUG = false;

	// Constructor
	public Handler(Node node, MessageType messageType, Set<Integer> mailingList) {
		this.node = node;
		this.messageType = messageType;
		this.mailingList = mailingList;
	}

	public Handler(Node node, MessageType messageType, int addresseeId) {
		this.node = node;
		this.messageType = messageType;
		this.addresseeId = addresseeId;
	}

	// Thread Method
	@Override
	public void run() {
		MulticastSocket socket = null;
		InetAddress group = null;
		try {
			group = InetAddress.getByName(node.getIpAddress());
		} catch (UnknownHostException e) {
			System.out.println("Handler: Error configuring InetAddress (Node: " + node.getNodeID() + ")");
		}

		try {
			socket = new MulticastSocket();
		} catch (IOException e) {
			System.out.println("Handler: Error configuring MulticastSocket (Node: " + node.getNodeID() + ")");
		}

		// Selects Type of Message and Serializes it
		// ACK and ELECTION are always sent to just one node. LEADER can be sent to
		// several nodes.
		if (messageType == MessageType.ACK) {
			ackMessage = new AckMessage(node.getNodeID(), node.getStoredId(), node.getStoredValue(),
					node.getComputationIndex(), node.getxCoordinate(), node.getyCoordinate(), addresseeId);
			messageToSend = ackMessage.toString().getBytes(StandardCharsets.UTF_8);

		} else if (messageType == MessageType.ELECTION) {
			electionMessage = new ElectionMessage(node.getNodeID(), node.getComputationIndex(), node.getxCoordinate(),
					node.getyCoordinate(), addresseeId);
			electionMessage.setAGroup(false);
			messageToSend = electionMessage.toString().getBytes(StandardCharsets.UTF_8);

			// Counter of Election Messages
			// node.getSimNode().addMsgCounter(node.getComputationIndex().getId());

		} else if (messageType == MessageType.ELECTION_GROUP) {
			electionMessage = new ElectionMessage(node.getNodeID(), node.getComputationIndex(), node.getxCoordinate(),
					node.getyCoordinate(), mailingList);
			electionMessage.setAGroup(true);
			messageToSend = electionMessage.toString().getBytes(StandardCharsets.UTF_8);

			// Counter of Election Messages
			// node.getSimNode().addMsgCounter(node.getComputationIndex().getId());

		} else if (messageType == MessageType.LEADER) {
			// Leader Message sends always messages to a group (it can be a "group of 1"),
			// so we can use just an HashSet
			leaderMessage = new LeaderMessage(node.getNodeID(), node.getLeaderID(), node.getLeaderValue(),
					node.getxCoordinate(), node.getyCoordinate(), false, mailingList);
			messageToSend = leaderMessage.toString().getBytes(StandardCharsets.UTF_8);

		} else if (messageType == MessageType.INFO) {
			infoMessage = new InfoMessage(node.getNodeID(), node.getLeaderID(), node.getLeaderValue(), addresseeId);
			messageToSend = infoMessage.toString().getBytes(StandardCharsets.UTF_8);

		} else if (messageType == MessageType.LEADER_SPECIAL) {
			leaderMessage = new LeaderMessage(node.getNodeID(), node.getLeaderID(), node.getLeaderValue(),
					node.getxCoordinate(), node.getyCoordinate(), true, mailingList);
			messageToSend = leaderMessage.toString().getBytes(StandardCharsets.UTF_8);
		}

		// Datagram Packet
		datagram = new DatagramPacket(messageToSend, messageToSend.length, group, node.getPort());

		// Sends Datagram
		try {
			// Only starts counting time when it's send an Election Msg
			// Starts Counting messages in election with Election Msg
			if ((messageType == MessageType.ELECTION) || (messageType == MessageType.ELECTION_GROUP)
					&& (node.getNetworkEvaluation().isNewElection() == false)) {

				node.networkEvaluation.setStartElectionTimer(node.getComputationIndex().getId());
				node.networkEvaluation.counterMessagesInElection(node.getComputationIndex().getId(), messageType);

				if (DEBUG)
					System.out.println(
							"I'm Node: " + node.getNodeID() + " - Init Election: " + node.getComputationIndex().getId()
									+ " = " + node.getNetworkEvaluation().getElectionInit());

				// Needed for Network Evaluation => Set New Election
				node.getNetworkEvaluation().setNewElection(true);
			}

			// Ends the message counting when Leader msg is sent
			if (messageType == MessageType.LEADER) {
				node.networkEvaluation.counterMessagesInElection(node.getComputationIndex().getId(), messageType);
			}

			socket.send(datagram);
		} catch (IOException e) {
			System.out.println(": Error sending datagram (Node: " + node.getNodeID() + ")");
		}

		socket.close();
	}
}
