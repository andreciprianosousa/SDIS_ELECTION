package logic;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

public class Handler extends Thread { 
	protected Node node;	
	protected MessageType messageType;
	protected AckMessage      ackMessage      = null;
	protected ElectionMessage electionMessage = null;
	protected LeaderMessage   leaderMessage   = null;
	byte[] messageToSend = new byte[2048];					// I would say that this can be much lower. I need confirmation!
	DatagramPacket datagram;
	
	// Constructor
	public Handler(Node node, MessageType messageType) {
		this.node = node;
		this.messageType = messageType; 
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
		if (messageType == MessageType.ACK) {
			try {
				ackMessage = new AckMessage(node.getNodeID(), node.getStoredId(), node.getStoredValue());
				messageToSend = ackMessage.serializeAckMessage();
			} catch (IOException e1) {
				e1.printStackTrace();
				System.out.println("Handler: Error Serializing AckMessage (Node: " + node.getNodeID()+ ")");
			}
			
		} else if (messageType == MessageType.ELECTION) {
			try {
				electionMessage = new ElectionMessage(node.getNodeID(), node.getComputationIndex());
				messageToSend = electionMessage.serializeElectionMessage();
			} catch (IOException e2) {
				e2.printStackTrace();
				System.out.println("Handler: Error Serializing ElectionMessage (Node: " + node.getNodeID()+ ")");
			}
			
		} else if (messageType == MessageType.LEADER) {
			try {
				leaderMessage = new LeaderMessage(node.getNodeID(), node.getStoredId() , node.getStoredValue());
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
		
		// Thread Sends and Dies - As we spoke in the class
		// If you prefer and if it gives you more power, I can try to change it to a thread that just sleeps
		// Cipriano's response - I think we will not need to have sleeping threads consuming space, since these messages
		// will be quick and the partitions may not be that intense xD, also it's easier to just finish the socket
		// on that note, you did not close the socket created, is it intended?
	}
}
