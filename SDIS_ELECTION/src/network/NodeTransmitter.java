package network;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

import logic.*;

public class NodeTransmitter extends Thread{
	protected Node node;
	protected int port;
	protected String ipAddress;
	
	protected byte[] dataToReceive = new byte[2048];
	
	public NodeTransmitter(Node node) {
		this.node = node;
		this.ipAddress = node.getIpAddress();
		this.port = node.getPort();
	}
	
	@Override
	public void run() {
		MulticastSocket socket = null;
		InetAddress group = null;
		Object message = null;
		HelloMessage helloMessage = null;

		ElectionMessage electionMessage = null;
		AckMessage ackMessage = null;
		LeaderMessage leaderMessage = null;

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
			
			try {
				message = deserializeMessage (datagram.getData());
			} catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
			} 
				

			//------------- Reception and logic starts here-----------------
			
			if (message instanceof HelloMessage) {
				helloMessage = (HelloMessage) message;
				//System.out.println(helloMessage.getNode().getNodeID());
				this.node.updateNeighbors (helloMessage, datagram.getAddress());
			}
			else if(message instanceof ElectionMessage) {
				electionMessage = (ElectionMessage) message;
				
				//------- Simulation -------
				if (this.node.testPacket(electionMessage.getxCoordinate(), electionMessage.getyCoordinate()) == true) {
					System.out.println("Election Message Packet Drop");
				} else {					
					// If Node is the message recipient, starts handling it
					// Separates Group messages from Individual ones
					
					if (electionMessage.isAGroup() == true) {
						if(electionMessage.getMailingList().contains(node.getNodeID())) {
							new ElectionMessageHandler(this.node, electionMessage).start(); 
						}
					} 
					else if (electionMessage.getAddresseeId() == node.getNodeID()) {
						new ElectionMessageHandler(this.node, electionMessage).start();
					}
				}
			}
			else if(message instanceof AckMessage) {
				ackMessage = (AckMessage) message;
				
				if(this.node.testPacket(ackMessage.getxCoordinate(), ackMessage.getyCoordinate())) {
					System.out.println("Ack Message Packet Drop");
				} else {
					if (ackMessage.getAddresseeId() == node.getNodeID()) {
						new AckMessageHandler(this.node, ackMessage).start();
					}	
				}
			}
			else if(message instanceof LeaderMessage) {
				leaderMessage = (LeaderMessage) message;
				
				if(this.node.testPacket(leaderMessage.getxCoordinate(), ackMessage.getyCoordinate())) {
					System.out.println("Leader Message Packet Drop");
				} else {
					if (leaderMessage.getMailingList().contains(node.getNodeID())) 		// Hope it works :D
						new LeaderMessageHandler(this.node, leaderMessage).start();
				}
			}
		}
			//---------------------------------------------------------------
	}
	
	public Object deserializeMessage (byte[] bytes) throws IOException, ClassNotFoundException {
		ByteArrayInputStream message = new ByteArrayInputStream(bytes);
        ObjectInputStream object = new ObjectInputStream(message);
        Object o = object.readObject();
        object.close();
        message.close();
        return o;
	}
}

