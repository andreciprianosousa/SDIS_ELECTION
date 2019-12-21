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

			if (print % 4 == 0 && (node.isKilled() == false)) {
				node.printLeader();
				System.out.println("From Node Listener, NODE " + node.getNodeID());
				node.printNeighbors();
			}

			print++;

			if (!(node.isKilled())) {

				// If we want to test the death of a node, then
				// - Test it, within cycles of refreshTestLiveliness sec
				// - If death, sleep for 5 * refreshTestLiveliness sec ==> Has a check to
				// perceive Resurrection
				if (node.isToTestDeath()) {
					if (oldState == true) {
						System.out.println(">>> Ressurection!");
						// SEND ELECTION MESSAGE? Yet, it's needed that network has connection and
						// neighbors, jezz
						// new Handler(this.node, logic.MessageType.ELECTION_GROUP,
						// node.getWaitingAcks()).start();

						// FALAR COM CIPRIANO
						node.resetCharge(); // Charge restored to new test
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
							System.out.println("(-, - )� zzzZZZ");
							if (node.getNeighbors().size() > 0) {
								synchronized (GoingToSleep) {
									try {
										this.wait();
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							}
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