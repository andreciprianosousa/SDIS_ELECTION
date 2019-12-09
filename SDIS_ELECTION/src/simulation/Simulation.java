package simulation;

import java.util.Random;

public class Simulation {
	
	private static final float v = (float) 3.5;					// Urban microcells 
	
	boolean nodeKilled;
	

	public Simulation() {
		System.out.println("Hello from simulation");
		nodeKilled = false;
	}
	
	public boolean dropPacket(float range, float distance) {
		float Pdropped=0, decisionN=0;
		Random decisionMaker = new Random();
		
		// Probability calculation 
		Pdropped = distance/range * 100;
		
		// Random Packet Dropout
		decisionN = decisionMaker.nextInt(100);					// Numbers between 0 and 99
		if(decisionN < Pdropped) {
			return true;										// Packet Dropped
		}
		else {
			return false;
		}
	}
	
	public void nodeKill(int battery) {
		Random decisionMaker = new Random();
		int decisionN;
		
		// Random Kill
		decisionN = decisionMaker.nextInt(100);					// Numbers between 0 and 99
		if(decisionN < battery) {
			setNodeKilled(true);							
		}
		else {
			setNodeKilled(false);	
		}
			
	}
	
	public boolean isNodeKilled() {
		return nodeKilled;
	}

	public void setNodeKilled(boolean nodeKilled) {
		this.nodeKilled = nodeKilled;
	}
}
