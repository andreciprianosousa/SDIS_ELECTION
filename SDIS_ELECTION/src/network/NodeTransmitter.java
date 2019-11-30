package network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

import logic.Node;

public class NodeTransmitter extends Thread{
	protected Node node;
	protected int port;
	protected String ipAddress;
	
	protected DatagramPacket datagram; 
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
			socket.setTimeToLive(1); //each datagram only does one hope 
		} catch (IOException e) {

			System.out.println("Transmitter: Error configuring Group (Node: " + node.getNodeID()+ ")");
		}
		
		String message;
		while (true) {
			datagram = new DatagramPacket (dataToReceive, dataToReceive.length);
			
			try {
				socket.receive(datagram);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			message = new String (datagram.getData(), StandardCharsets.UTF_8);
			
			System.out.println("Message received by Node " + node.getNodeID() + ": " + message);
		}
	}
}
