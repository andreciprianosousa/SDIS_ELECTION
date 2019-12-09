package launcher;

import logic.Node;
//New Branch Test

public class NodeLauncher {
	protected static int nodeID;
	protected static int port;
	protected static String ipAddress;
	protected static int[] dimensions = new int[3];
	
	/*Network configurations*/
	protected static int refreshRate = 2;			// Every refreshRate seconds, each node sends a HelloMessage
	protected static int timeOut = 2 * refreshRate; // If a node does not react for timeOut seconds, it's dead
	
	public static void main(String[] args) throws InterruptedException {
		
		if(args.length!=4) {
			System.out.println("Incorrect usage! To run the program use:");
			System.out.println("Java -jar <nodeID> <xDimension> <yDimension> <nodeRange>");
		}

		nodeID = Integer.parseInt(args[0]);
		dimensions[0] = Integer.parseInt(args[1]);
		dimensions[1] = Integer.parseInt(args[2]);
		dimensions[2] = Integer.parseInt(args[3]);
		
		port = 5000;
		ipAddress = "225.225.221.6"; //between 224.0.0.0 and 239.255.255.255
		System.out.println("Starting Node " + nodeID);
		new Node(nodeID, port, ipAddress, dimensions, refreshRate, timeOut);
		
	}
}