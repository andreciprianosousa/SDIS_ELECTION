package logic;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashSet;

public class LeaderMessage implements Serializable{
	
		private int incomingId;
		private int storedID;
		private float storedValue;
		private int xCoordinate;
		private int yCoordinate;
		private HashSet<Integer> mailingList;

		public LeaderMessage(int incomingId, int leaderID, float leaderValue, int xCoordinate, int yCoordinate, HashSet<Integer> mailingList) {
			this.incomingId = incomingId;
			this.storedID = leaderID;
			this.storedValue = leaderValue;
			this.xCoordinate = xCoordinate;
			this.yCoordinate = yCoordinate;
			this.mailingList = mailingList;
		}
		
		public byte[] serializeLeaderMessage () throws IOException {
			ByteArrayOutputStream message = new ByteArrayOutputStream();
	        ObjectOutputStream object = new ObjectOutputStream(message);
	        object.writeObject((Object)this);
	        object.flush();
	        object.close();
	        message.close();
	        return message.toByteArray();
		}

		public int getIncomingId() {
			return incomingId;
		}

		public void setIncomingId(int incomingId) {
			this.incomingId = incomingId;
		}

		public int getStoredID() {
			return storedID;
		}

		public void setStoredID(int storedID) {
			this.storedID = storedID;
		}

		public float getStoredValue() {
			return storedValue;
		}

		public void setStoredValue(float storedValue) {
			this.storedValue = storedValue;
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

		public HashSet<Integer> getMailingList() {
			return mailingList;
		}
		public void setMailingList(HashSet<Integer> mailingList) {
			this.mailingList = mailingList;
		}
}
