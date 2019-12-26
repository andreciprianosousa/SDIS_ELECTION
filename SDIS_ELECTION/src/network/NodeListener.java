package network;

import java.net.MulticastSocket;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;

import logic.*;

public class NodeListener extends Thread {
	protected Node node;
	protected int port;
	protected String ipAddress;
	protected int refreshRate;
	protected String helloMessage;
	byte[] messageToSend = new byte[2048];
	DatagramPacket datagram;
	private int print = 0;

	private boolean oldState = false;
	private Instant lastLivenessTest = Instant.now();
	private Instant deathNode = Instant.now();

	private static final int refreshTestLiveliness = 1000;
	private static final boolean DEBUG = false;
	private static final String GoingToSleep = null;

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
			System.out.println("Listener: Error configuring InetAddress (Node: " + node.getNodeID() + ")");
		}

		try {
			socket = new MulticastSocket();
		} catch (IOException e) {
			System.out.println("Listener: Error configuring MulticastSocket (Node: " + node.getNodeID() + ")");
		}

		try {
			socket.joinGroup(group);
		} catch (IOException e) {

			System.out.println("Listener: Error configuring Group (Node: " + node.getNodeID() + ")");
		}

		while (true) {

			node.updateRemovedNodes();

//			if (print % 4 == 0 && (node.isKilled() == false)) {
//				node.printLeader();
//				System.out.println("From Node Listener, NODE " + node.getNodeID());
//				node.printNeighbors();
//			}

			print++;

			node.getNetworkEvaluation().setStartElectionRateTimer();
			if (node.isElectionActive()) {
				try {
					node.getNetworkEvaluation().counterElectionRate(node.getComputationIndex().getId());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (!(node.isKilled())) {

				// If we want to test the death of a node, then
				// - Test it, within cycles of refreshTestLiveliness sec
				// - If death, sleep for 5 * refreshTestLiveliness sec ==> Has a check to
				// perceive Resurrection
				if (node.isToTestDeath()) {
					if (oldState == true) {
						System.out
								.println(">>> Ressurection!  " + Duration.between(deathNode, Instant.now()).toMillis());

						node.resetCharge(); // Charge restored to test new possible death

						// Cipriano - This is for you
						new Bootstrap(node).start(); // New node, so set network and act accordingly
					}
					oldState = false;

					if (Duration.between(lastLivenessTest, Instant.now()).toMillis() > refreshTestLiveliness) {
						boolean setToKill = node.testLiveliness();
						lastLivenessTest = Instant.now();
						node.setKilled(setToKill);
						if (setToKill == true) {
							deathNode = Instant.now();
							oldState = true;
							if (DEBUG)
								System.out.println("(-, - )� zzzZZZ");
						}
					}
				}

				helloMessage = new HelloMessage(node).toString();
				// System.out.println(helloMessage);
				messageToSend = helloMessage.getBytes(StandardCharsets.UTF_8);

				datagram = new DatagramPacket(messageToSend, messageToSend.length, group, port);

				try {
					socket.send(datagram);
				} catch (IOException e) {
					System.out.println("Listener: Error sending datagram (Node: " + node.getNodeID() + ")");
				}

				// System.out.println("Message sent by Node: " + node.getNodeID());

				try {
					Thread.sleep(refreshRate * 1000);
				} catch (InterruptedException e) {
					System.out.println("Listener: Error putting thread to sleep (Node: " + node.getNodeID() + ")");
				}

			} else {
				if (DEBUG) {
					System.out.println("                             (-, - )� zzzZZZ");
				}

				if ((Duration.between(deathNode, Instant.now()).toMillis()) > (5 * refreshTestLiveliness)) {
					node.setKilled(false);
				}

			}
		}
	}
}