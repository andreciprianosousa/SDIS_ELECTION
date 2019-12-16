package simulation;

import java.time.Duration;
import java.time.Instant;
import java.util.Random;

public class Simulation {
	
	private static final float v = (float) 3.5;					// Urban microcells 
	
	boolean nodeKilled;
	
	protected Instant start;
	protected Instant end;
	protected Duration timeElapsed;
	

	public Simulation() {
		System.out.println("Hello from simulation");
		nodeKilled = false;
	}
	
	public void setStart() {
		this.start = Instant.now();
	}
	
	public void setEnd() {
		this.end = Instant.now();
	}
	
	public void getTimer() {
		this.timeElapsed = Duration.between(start, end);
		System.out.println("Time taken: "+ timeElapsed.toMillis() +" milliseconds");
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
