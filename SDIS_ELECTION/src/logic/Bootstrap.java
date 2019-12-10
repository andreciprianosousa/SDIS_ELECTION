package logic;

import java.util.HashSet;
import java.util.Iterator;

public class Bootstrap extends Thread{

	protected Node node;
	protected HashSet<Integer> toSend = new HashSet<Integer>();

	public Bootstrap(Node node) {
		this.node = node;
	}

	@Override
	public void run() {
		try {
			Thread.sleep(2000); // Gives time to node set himself in the network, subject to change with network size
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// If I have no neighbours, set myself as leader immediately and it's done
		if(node.getNeighbors().isEmpty()) {
			node.setLeaderID(node.getNodeID());
			System.out.println("My current Leader: " + node.getLeaderID());
		}
		else {
			Iterator<Integer> i=node.getNeighbors().iterator();
			while(i.hasNext()) {
				Integer temp = i.next();
				toSend.add(temp);
			}		
			new Handler(this.node, logic.MessageType.ELECTION_GROUP, toSend).start();
		}


	}
}
