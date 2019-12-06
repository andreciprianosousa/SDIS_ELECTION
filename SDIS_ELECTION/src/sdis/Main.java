package sdis;

import java.util.Iterator;

public class Main {
	
	public static void main(String[] args) {
		
		NodeTest n1 = new NodeTest(1,2);
		NodeTest n2 = new NodeTest(2,-1);
		NodeTest n3 = new NodeTest(3, 1);
		NodeTest n4 = new NodeTest(4, 2);
		
		n1.getNeighbors().add(n2);
		n1.getNeighbors().add(n3);
		n1.getNeighbors().add(n4);
		
		Iterator<NodeTest> i=n1.getNeighbors().iterator();
		while(i.hasNext()) {
			// This will not work most likely...
			NodeTest temp = i.next();
			if(!(temp.getId() == n1.getParent())) {
				n1.getWaitingAcks().add(temp);
				// Send Election Message to current selected neighbour
			}
		}
		
		Iterator<NodeTest> ii=n1.getWaitingAcks().iterator();
		while(ii.hasNext()) {
			System.out.println(ii.next().getId());
		}
	}
}
