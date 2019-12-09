package logic;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;

public class ElectionMessage implements Serializable{
	
	//This message needs to be serializable to allow its representation as a sequence of bytes
		private ComputationIndex cp;
		private int incomingId;
		private int xCoordinate;
		private int yCoordinate;
		private int addresseeId = 0;
		private HashSet<Integer> mailingList = new HashSet<Integer>();
		private boolean isAGroup;

		public ElectionMessage(int incomingId, ComputationIndex cp, int xCoordinate, int yCoordinate, int addresseeId) {
			this.cp = cp;
			this.incomingId = incomingId;
			this.xCoordinate = xCoordinate;
			this.yCoordinate = yCoordinate;
			this.addresseeId = addresseeId;
		}
		
		public ElectionMessage(int incomingId, ComputationIndex cp, int xCoordinate, int yCoordinate, HashSet<Integer> mailingList) {
			this.cp = cp;
			this.incomingId = incomingId;
			this.xCoordinate = xCoordinate;
			this.yCoordinate = yCoordinate;
			this.mailingList = mailingList;
			
		}
		
		public byte[] serializeElectionMessage () throws IOException {
			ByteArrayOutputStream message = new ByteArrayOutputStream();
	        ObjectOutputStream object = new ObjectOutputStream(message);
	        object.writeObject((Object)this);
	        object.flush();
	        object.close();
	        message.close();
	        return message.toByteArray();
		}
		
		public int getNodeID() {
			return this.incomingId;
		}

		public void setincomingId(int incomingId) {
			this.incomingId = incomingId;
		}
		public ComputationIndex getComputationIndex() {
			return this.cp;
		}

		public ComputationIndex getCp() {
			return cp;
		}

		public void setCp(ComputationIndex cp) {
			this.cp = cp;
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

		public int getAddresseeId() {
			return addresseeId;
		}

		public void setAddresseeId(int addresseeId) {
			this.addresseeId = addresseeId;
		}

		public HashSet<Integer> getMailingList() {
			return mailingList;
		}

		public void setMailingList(HashSet<Integer> mailingList) {
			this.mailingList = mailingList;
		}

		public boolean isAGroup() {
			return isAGroup;
		}

		public void setAGroup(boolean isAGroup) {
			this.isAGroup = isAGroup;
		}
}
