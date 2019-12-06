package logic;

import java.io.IOException;
import java.net.DatagramPacket;

public class Handler implements Thread { 
	
	public Handler() {
	// TODO Auto-generated constructor stub
	}
	
	@Override
	public void run() {
		
		// Datagram Packet
		//datagram = new DatagramPacket(messageToSend, messageToSend.length, group, port);
		
		
		// Datagram => Serialize
		try {
		//	socket.send(datagram);
		} catch (IOException e) {
		//	System.out.println(": Error sending datagram (Node: " + node.getNodeID()+ ")");
		}
	}
	
}
