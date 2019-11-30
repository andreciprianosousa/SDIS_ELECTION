package network;

import java.net.MulticastSocket;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import logic.*;

public class NodeListener extends Thread{
	protected Node node;
	protected int port;
	protected String ipAddress;
	
	byte[] messageToSend;
	DatagramPacket datagram;
	
	public NodeListener(Node node) {
		this.node = node;
		this.port = node.getPort();
		this.ipAddress = node.getIpAddress();
	}
	
	@Override
	public void run() {
		MulticastSocket socket = null;
		InetAddress group = null;
		try {
			group = InetAddress.getByName(ipAddress);
		} catch (UnknownHostException e) {
			System.out.println ("Listener: Error configuring InetAddress (Node: " + node.getNodeID()+ ")");
		}
	
		try {
			socket = new MulticastSocket();
		} catch (IOException e) {
			System.out.println("Listener: Error configuring MulticastSocket (Node: " + node.getNodeID()+ ")");
		}
		

//		try {
//			socket.joinGroup(group);
//		} catch (IOException e) {
//
//			System.out.println("Listener: Error configuring Group (Node: " + node.getNodeID()+ ")");
//		}
		
		String message = "Hello, I'm node " + node.getNodeID();
		
		while (true) {
			messageToSend = message.getBytes(StandardCharsets.UTF_8);
			datagram = new DatagramPacket(messageToSend, messageToSend.length, group, port);
			
			try {
				socket.send(datagram);
			} catch (IOException e) {
				System.out.println("Listener: Error sending datagram (Node: " + node.getNodeID()+ ")");
			}
			
			System.out.println("Message sent by Node: " + node.getNodeID());
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				System.out.println("Listener: Error putting thread to sleep (Node: " + node.getNodeID()+ ")");
			}
		}
	}
	
	
}
