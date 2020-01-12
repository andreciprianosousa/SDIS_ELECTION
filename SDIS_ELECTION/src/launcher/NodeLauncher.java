package launcher;

import logic.Node;

public class NodeLauncher {
	protected static int nodeID;
	protected static int port;
	protected static String ipAddress;
	protected static int[] dimensions = new int[3];
	protected static int[] finalDestination = new int[3];
	protected static int medianFailure;
	protected static int medianDeath;
	protected static int mode;

	/* Network configurations */
	protected static int refreshRate = 1; // Every refreshRate seconds, each node sends a HelloMessage
	protected static int timeOut = 10 * refreshRate; // If a node does not react for timeOut seconds, it's dead
	// 20 Nodes - 10 , 40 Nodes - 20, 60/80 Nodes - 30
	protected static boolean DEBUG = false;

	public static void main(String[] args) throws InterruptedException {

		if (args.length != 10) {
			System.out.println("Incorrect usage! To run the program use:");
			System.out.println(
					"Java -jar <nodeID> <xDimension> <yDimension> <nodeRange> <medianFailure> <medianDeath> <FinalX> <FinalY> <MovementDirection[0=H,1=V]> <Mode[0=Static,1=Dynamic]>");
		}

		nodeID = Integer.parseInt(args[0]);
		dimensions[0] = Integer.parseInt(args[1]);
		dimensions[1] = Integer.parseInt(args[2]);
		dimensions[2] = Integer.parseInt(args[3]);
		medianFailure = Integer.parseInt(args[4]);
		medianDeath = Integer.parseInt(args[5]);
		finalDestination[0] = Integer.parseInt(args[6]);
		finalDestination[1] = Integer.parseInt(args[7]);
		finalDestination[2] = Integer.parseInt(args[8]);
		mode = Integer.parseInt(args[9]);

		if (DEBUG) {
			System.out.println("### Welcome ### \n" + "# nodeID = " + nodeID + "\n# Xi = " + dimensions[0] + " | Yi = "
					+ dimensions[1] + "\n# Range = " + dimensions[2] + "\n# MedianFailure = " + medianFailure
					+ "\n# MedianDeath = " + medianDeath + "\n# Xf = " + finalDestination[0] + " | Yf = "
					+ finalDestination[1] + "\n# Direction[0=H,1=V] = " + finalDestination[2]
					+ "\n# Mode[0=Static,1=Dynamic] =" + mode + "\n\n\n");
		}

		port = 5000;
		ipAddress = "225.225.221.6"; // between 224.0.0.0 and 239.255.255.255
		System.out.println("Starting Node " + nodeID);
		new Node(nodeID, port, ipAddress, dimensions, refreshRate, timeOut, medianFailure, medianDeath,
				finalDestination, mode);
	}
}