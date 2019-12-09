package logic;

import java.util.HashSet;
import java.util.Iterator;

public class Test extends Thread{




	protected Node node;
	protected HashSet<Integer> test = new HashSet<Integer>();

	public Test(Node node) {
		this.node = node;
	}

	@Override
	public void run() {
		while(true) {
			if(node.getNeighbors().size() == 2) {
				Iterator<Integer> i=node.getNeighbors().iterator();
				while(i.hasNext()) {
					Integer temp = i.next();
					test.add(temp);
				}		
				new Handler(this.node, logic.MessageType.ELECTION_GROUP, test).start();
				break;
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
