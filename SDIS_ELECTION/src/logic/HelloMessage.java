package logic;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import java.util.HashSet;

public class HelloMessage implements Serializable{
	//This message needs to be serializable to allow its representation as a sequence of bytes
	private int nodeID;
	private int xCoordinate;
	private int yCoordinate;
	private String messageCode; //5 chars code
	public HelloMessage(Node node) {
		this.nodeID = node.getNodeID();
		this.xCoordinate = node.getxCoordinate();
		this.yCoordinate = node.getyCoordinate();
		messageCode = "hello";
	}

	@Override
	public String toString() { 
		return String.format(messageCode + "/" + nodeID + "/" + xCoordinate + "/" + yCoordinate + "/"); 
	} 

	public int getNodeID() {
		return nodeID;
	}

	public void setNodeID(int nodeID) {
		this.nodeID = nodeID;
	}

	public int getxCoordinate() {
		return xCoordinate;
	}

	public void setxCoordinate(int xCoordinate) {
		this.xCoordinate = xCoordinate;
	}

	public int getyCoordinate() {
		return yCoordinate;
	}

	public void setyCoordinate(int yCoordinate) {
		this.yCoordinate = yCoordinate;
	}



}
