package simulation;

import java.util.Random;

public class Simulation {
	
	private static final float v = (float) 3.5;					// Urban microcells 
	
	
	public Simulation() {
		System.out.println("Hello from simulation");
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
	
}
