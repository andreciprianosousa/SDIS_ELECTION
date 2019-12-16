package network;

import java.net.MulticastSocket;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

import logic.*;

public class NodeListener extends Thread{
	protected Node node;
	protected int port;
	protected String ipAddress;
	protected int refreshRate;
	protected String helloMessage;
	byte[] messageToSend = new byte[2048];
	DatagramPacket datagram;
	private int print = 0;
	
	public NodeListener(Node node, int refreshRate) {
		this.node = node;
		this.port = node.getPort();
		this.ipAddress = node.getIpAddress();
		this.refreshRate = refreshRate;
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
		

		try {
			socket.joinGroup(group);
		} catch (IOException e) {

			System.out.println("Listener: Error configuring Group (Node: " + node.getNodeID()+ ")");
		}
		
		while (true) {
//			
//			node.updateRemovedNodes();
//					
//			if(print % 2 == 0) {
//				node.printLeader();
//				System.out.println("From Node Listener, NODE " + node.getNodeID() + " , Size WA: " + node.getWaitingAcks().size());
//				//System.out.println("WA_NODE" + node.getNodeID() + "_: " + node.getWaitingAcks().toString() + " ==> Empty - " + node.getWaitingAcks().isEmpty());
//			}
//			if(print % 3 == 0) {
//				node.printNeighbors();
//			}
			print++;
						
			helloMessage = new HelloMessage(node).toString();
			//System.out.println(helloMessage);
			messageToSend = helloMessage.getBytes(StandardCharsets.UTF_8);
			
			datagram = new DatagramPacket(messageToSend, messageToSend.length, group, port);
			
			try {
				socket.send(datagram);
			} catch (IOException e) {
				System.out.println("Listener: Error sending datagram (Node: " + node.getNodeID()+ ")");
			}
			
			//System.out.println("Message sent by Node: " + node.getNodeID());
			
			try {
				Thread.sleep(refreshRate*1000);
				//Thread.sleep(refreshRate*1);
			} catch (InterruptedException e) {
				System.out.println("Listener: Error putting thread to sleep (Node: " + node.getNodeID()+ ")");
			}
		}
	}
	
	
}