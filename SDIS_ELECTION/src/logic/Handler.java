package logic;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.HashSet;

public class Handler extends Thread { 
	protected Node node;	
	protected MessageType messageType;
	protected HashSet<Integer> mailingList;							   // message recipient
	protected int addresseeId;							  			   // message recipient
	protected AckMessage      ackMessage      = null;
	protected ElectionMessage electionMessage = null;
	protected LeaderMessage   leaderMessage   = null;
	byte[] messageToSend = new byte[2048];					// I would say that this can be much lower. I need confirmation!
	DatagramPacket datagram;
	
	// Constructor
	public Handler(Node node, MessageType messageType, HashSet<Integer> mailingList) {
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
			System.out.println("Handler: Error configuring InetAddress (Node: " + node.getNodeID()+ ")");
		}

		try {
			socket = new MulticastSocket();
		} catch (IOException e) {
			System.out.println("Handler: Error configuring MulticastSocket (Node: " + node.getNodeID()+ ")");
		}
		
		// Selects Type of Message and Serializes it
		// ACK and ELECTION are always sent to just one node. LEADER can be sent to several nodes.
		if (messageType == MessageType.ACK) {
			
			try {
				ackMessage = new AckMessage(node.getNodeID(), node.getStoredId(), node.getStoredValue(), node.getxCoordinate(), node.getyCoordinate(), addresseeId);
				messageToSend = ackMessage.serializeAckMessage();
			} catch (IOException e1) {
				e1.printStackTrace();
				System.out.println("Handler: Error Serializing AckMessage (Node: " + node.getNodeID()+ ")");
			}		
			
		} else if (messageType == MessageType.ELECTION) {
			try {
				electionMessage = new ElectionMessage(node.getNodeID(), node.getComputationIndex(), node.getxCoordinate(), node.getyCoordinate(), addresseeId);
				electionMessage.setAGroup(false);
				messageToSend = electionMessage.serializeElectionMessage();
			} catch (IOException e2) {
				e2.printStackTrace();
				System.out.println("Handler: Error Serializing ElectionMessage (Node: " + node.getNodeID()+ ")");
			}
			
		} else if (messageType == MessageType.ELECTION_GROUP) {
			try {
				electionMessage = new ElectionMessage(node.getNodeID(), node.getComputationIndex(), node.getxCoordinate(), node.getyCoordinate(), mailingList);
				electionMessage.setAGroup(true);
				messageToSend = electionMessage.serializeElectionMessage();
			} catch (IOException e2) {
				e2.printStackTrace();
				System.out.println("Handler: Error Serializing ElectionMessage (Node: " + node.getNodeID()+ ")");
			}
		
			
		} else if (messageType == MessageType.LEADER) {
			
			// Leader Message sends always messages to a group (it can be a "group of 1"), so we can use just an HashSet
			try {
				leaderMessage = new LeaderMessage(node.getNodeID(), node.getStoredId() , node.getStoredValue(), node.getxCoordinate(), node.getyCoordinate(), mailingList);
				messageToSend = leaderMessage.serializeLeaderMessage();
			} catch (IOException e3) {
				e3.printStackTrace();
				System.out.println("Handler: Error Serializing LeaderMessage (Node: " + node.getNodeID()+ ")");
			}
		}
		
		// Datagram Packet
		datagram = new DatagramPacket(messageToSend, messageToSend.length, group, node.getPort());
		
		// Sends Datagram		
		try {
			socket.send(datagram);
		} catch (IOException e) {
			System.out.println(": Error sending datagram (Node: " + node.getNodeID()+ ")");
		}
		
		socket.close();
	}
}
