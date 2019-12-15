package network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;

import logic.*;

public class NodeTransmitter extends Thread{
	protected Node node;
	protected int port;
	protected int timeOut;
	protected String ipAddress;

	protected byte[] dataToReceive = new byte[2048];

	public NodeTransmitter(Node node, int timeOut) {
		this.node = node;
		this.ipAddress = node.getIpAddress();
		this.port = node.getPort();
		this.timeOut = timeOut;
	}

	@SuppressWarnings("resource")
	@Override
	public void run() {
		MulticastSocket socket = null;
		InetAddress group = null;
		String message = null;

		ElectionMessage electionMessage = null;
		AckMessage ackMessage = null;
		LeaderMessage leaderMessage = null;
		InfoMessage infoMessage = null;

		DatagramPacket datagram; 

		try {
			group = InetAddress.getByName(ipAddress);
		} catch (UnknownHostException e) {
			System.out.println ("Transmitter: Error configuring InetAddress (Node: " + node.getNodeID()+ ")");
		}

		try {
			socket = new MulticastSocket(port);
		} catch (IOException e) {
			System.out.println("Transmitter: Error configuring MulticastSocket (Node: " + node.getNodeID()+ ")");
		}


		try {
			socket.joinGroup(group);
			socket.setTimeToLive(1); //each datagram only does one hop 
		} catch (IOException e) {

			System.out.println("Transmitter: Error configuring Group (Node: " + node.getNodeID()+ ")");
		}

		while (true) {
			datagram = new DatagramPacket (dataToReceive, dataToReceive.length);
			//System.out.println(dataToReceive.length);
			try {
				socket.receive(datagram);	
			} catch (IOException e) {
				System.out.println("Transmitter: Error receiving datagram (Node: " + node.getNodeID()+ ")");
			}

			message = new String (datagram.getData(), 0,datagram.getData().length ,StandardCharsets.UTF_8);
			//System.out.println("Node " + node.getNodeID() + ". Message " + message);

			//------------- Reception and logic starts here-----------------
			String[] fields = message.split("/");
			if(message.contains("hello")) {
				this.node.updateNeighbors(Integer.parseInt(fields[1]), Integer.parseInt(fields[2]), Integer.parseInt(fields[3]));
			}
			
			else if(message.contains("elec")) {

				// If Node is the message recipient, starts handling it
				// Separates Group messages from Individual ones
				if (message.contains("elecG") == true) {
					electionMessage = convertToElectionMessageGroup(message);
					if(electionMessage.getMailingList().contains(node.getNodeID())) {
						//System.out.println("Node " + node.getNodeID() + " received election group message from " + electionMessage.getIncomingId());
						new ElectionMessageHandler(this.node, electionMessage).start(); 
					}
				} 
				else {
					electionMessage = convertToElectionMessageIndividual(message);
					if (electionMessage.getAddresseeId() == node.getNodeID()) {
						//System.out.println("Node " + node.getNodeID() + " received election message from " + electionMessage.getIncomingId());
						new ElectionMessageHandler(this.node, electionMessage).start();
					}
				}
			}
			else if(message.contains("ack")) {
				ackMessage = convertToAckMessage(message);

				if (ackMessage.getAddresseeId() == node.getNodeID()) {
					//System.out.println("Node " + node.getNodeID() + " received ack message from "+ ackMessage.getIncomingId());
					new AckMessageHandler(this.node, ackMessage).start();
				}
			}
			
			else if(message.contains("leadr")) {
//				System.out.println("leader has " + fields.length);
//				for(int i=0; i<fields.length; i++) {
//					System.out.println(fields[i]);
//				}
				// We may put here a mailing list check, because node that started election doesn't need leader message's information
				leaderMessage = convertToLeaderMessage(message);
				if(leaderMessage.getMailingList().contains(node.getNodeID())) {
					//System.out.println("Node " + node.getNodeID() + " received leader message from " + leaderMessage.getIncomingId());
					new LeaderMessageHandler(this.node, leaderMessage).start();
				}
			}
			
			else if(message.contains("info")) {
				infoMessage = convertToInfoMessage(message);
				if(infoMessage.getAddresseeId() == node.getNodeID()) {
					//System.out.println("Node " + node.getNodeID() + " received info message from "+ infoMessage.getIncomingId());
					new InfoMessageHandler(this.node, infoMessage).start();
				}
				
			}
		}
	}

	public ElectionMessage convertToElectionMessageGroup (String message) {
		String[] fields = message.split("/");
		int id = Integer.parseInt(fields[1]);
		ComputationIndex cIndex = stringToCP(fields[2]);
		int x = Integer.parseInt(fields[3]);
		int y = Integer.parseInt(fields[4]);
		HashSet <Integer> mList= stringToMalingList(fields[5]);
		return new ElectionMessage (id, cIndex, x, y, mList);
	}
	
	public ElectionMessage convertToElectionMessageIndividual (String message) {
		String[] fields = message.split("/");
		int id = Integer.parseInt(fields[1]);
		ComputationIndex cIndex = stringToCP(fields[2]);
		int x = Integer.parseInt(fields[3]);
		int y = Integer.parseInt(fields[4]);
		int addresseeId = Integer.parseInt(fields[5]);
		
		return new ElectionMessage (id, cIndex, x, y, addresseeId);
	}
	
	public AckMessage convertToAckMessage(String message) {
		String[] fields = message.split("/");
		int incomingId = Integer.parseInt(fields[1]);
		int leaderID = Integer.parseInt(fields[2]);
		float leaderValue = Float.valueOf(fields[3]);
		int xCoordinate = Integer.parseInt(fields[4]);
		int yCoordinate = Integer.parseInt(fields[5]);
		int addresseeId= Integer.parseInt(fields[6]);
		
		return new AckMessage(incomingId, leaderID, leaderValue, xCoordinate, yCoordinate, addresseeId);
	}
	
	public LeaderMessage convertToLeaderMessage (String message) {
		String[] fields = message.split("/");
		int incomingId = Integer.parseInt(fields[1]);
		int leaderID = Integer.parseInt(fields[2]);
		float leaderValue = Float.valueOf(fields[3]);
		int xCoordinate = Integer.parseInt(fields[4]);
		int yCoordinate = Integer.parseInt(fields[5]);
		boolean special = Boolean.parseBoolean(fields[6]);
		HashSet <Integer> mList= stringToMalingList(fields[7]);

		
		return new LeaderMessage(incomingId, leaderID, leaderValue, xCoordinate, yCoordinate, special, mList);
	}
	
	public InfoMessage convertToInfoMessage(String message) {
		String[] fields = message.split("/");
		int incomingId = Integer.parseInt(fields[1]);
		int leaderID = Integer.parseInt(fields[2]);
		float leaderValue = Float.valueOf(fields[3]);
		int addresseeId= Integer.parseInt(fields[4]);
		
		return new InfoMessage(incomingId, leaderID, leaderValue, addresseeId);
	}
	
	
	public ComputationIndex stringToCP(String cpString) {
		String[] fields = cpString.split(",");
		return new ComputationIndex(Integer.parseInt(fields[0]), Integer.parseInt(fields[1]), Float.valueOf((fields[2])));
	}
	
	public HashSet<Integer> stringToMalingList (String mailingListString) {
		String[] fields = mailingListString.split(",");
		HashSet <Integer> mailingListConverted = new HashSet<Integer>();
		for(String id : fields) {
			mailingListConverted.add(Integer.parseInt(id));
		}
		
		return mailingListConverted;
	}
}

