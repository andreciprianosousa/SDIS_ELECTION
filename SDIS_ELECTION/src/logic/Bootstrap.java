package logic;

import java.util.HashSet;
import java.util.Iterator;

public class Bootstrap extends Thread{

	protected Node node;
	
	private static final int NetworkSet_Delay = 3000;
	private static final int Election_Delay = 4000;

	private static final boolean DEBUG = false; 
	
	public Bootstrap(Node node) {
		this.node = node;
	}

	@Override
	public void run() {
		try {
			Thread.sleep(NetworkSet_Delay); // Gives time to node set himself in the network, subject to change with network size
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
		// If not, start an election with neighbours or join current network
		else {
			
			// Only biggest ID node should bootstrap election, should many nodes instantiate at once, given
			// time to setup network beforehand
			if(!(node.getNodeID() > node.getMaximumIdNeighbors())) {
				try {
					Thread.sleep(NetworkSet_Delay+Election_Delay); // Gives time to Higher Value node to finish the election, subject to change with network size
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				if(node.getLeaderID() == node.getNodeID()) { // Either I'm a new node (No interaction with Elections) or i'm leader
					
					if(DEBUG)
						System.out.println("Node is not strong enough to initiate election. Exchanging leader info with one neighbour.");
					
					Iterator<Integer> i=node.getNeighbors().iterator();
					new Handler(this.node, logic.MessageType.INFO, i.next()).start();
				}
				
			} else {
			
				node.setAckStatus(true); // true means it has not sent ack to parent, in ack handler we will put this to false again
	
				// For every neighbour except parent, put them in waitingAck 
				synchronized (this) {
					Iterator<Integer> i=node.getNeighbors().iterator();
					while(i.hasNext()) {
						Integer temp = i.next();
						if((!(node.getWaitingAcks().contains(temp))) && (!(temp.toString().equals("")))) {
							node.getWaitingAcks().add(temp);
							//System.out.println("ADD " + temp);
						}	
					}
				}
								
				System.out.println("Node " + node.getNodeID() + " bootstrapped election group message.");
				
				// -----------CP Tests-----------
				node.getComputationIndex().setNum(node.getComputationIndex().getNum()+1);
				node.getComputationIndex().setId(node.getNodeID());
				node.getComputationIndex().setValue(node.getNodeValue());
				//------------------------------
				new Handler(this.node, logic.MessageType.ELECTION_GROUP, node.getWaitingAcks()).start();
			}	
		}
	}
}

