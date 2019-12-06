package network;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

import logic.Node;
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			
			// Test Packet
			if (this.node.testPacket() == true) {
				System.out.println("Packet Drop!");
				message = "Dropped";
			}
			
			if (message instanceof HelloMessage) {
				helloMessage = (HelloMessage) message;
				//System.out.println(helloMessage.getNode().getNodeID());
				this.node.updateNeighbors (helloMessage, datagram.getAddress());
			}
			
			//System.out.println("Message received by Node " + node.getNodeID() + ": " + message);
		}
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
