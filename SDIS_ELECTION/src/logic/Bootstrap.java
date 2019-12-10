package logic;

import java.util.HashSet;
import java.util.Iterator;

public class Bootstrap extends Thread{

	protected Node node;

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
			System.out.println("I'm alone, my current Leader is me: " + node.getLeaderID());
			System.out.println("-----------------------------");
		}
		// If not, start an election with neighbours
		else {
			node.setAckStatus(true); // true means it has not sent ack to parent, in ack handler we will put this to false again

			// For every neighbour except parent, put them in waitingAck 

			Iterator<Integer> i=node.getNeighbors().iterator();
			while(i.hasNext()) {
				Integer temp = i.next();
				if(!(temp == node.getParentActive())) {
					node.getWaitingAcks().add(temp);
					// Send Election Message to current selected neighbour	
				}
			}		
			System.out.println("Node " + node.getNodeID() + " bootstrapped election group message.");
			// -----------CP Tests-----------
			node.getComputationIndex().setNum(node.getComputationIndex().getNum()+1);
			//------------------------------
			new Handler(this.node, logic.MessageType.ELECTION_GROUP, node.getWaitingAcks()).start();
		}


	}
}
