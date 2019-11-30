package launcher;

import logic.Node;

public class NodeLauncher {
	protected static int nodeID;
	protected static int port;
	protected static String ipAddress;
	
	
	public static void main(String[] args) {
		nodeID = Integer.parseInt(args[0]);
		System.out.println(nodeID);
		port = 5000;
		ipAddress = "225.4.5.6"; //between 224.0.0.0 and 239.255.255.255
		new Node(nodeID, port, ipAddress);
	}
}
