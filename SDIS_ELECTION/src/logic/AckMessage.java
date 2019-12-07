package logic;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class AckMessage implements Serializable  {
	
	    //This message needs to be serializable to allow its representation as a sequence of bytes
		private int incomingId;
		private int storedID;
		private float storedValue;

		private int myCoordX;
		private int myCoordY;
		private int addresseeId;
		

		public AckMessage(int incomingId, int leaderID, float leaderValue, int myCoordX, int myCoordY, int addresseeId) {
			this.incomingId = incomingId;
			this.storedID = leaderID;
			this.storedValue = leaderValue;
			this.addresseeId = addresseeId;
			this.myCoordX = myCoordX;
			this.myCoordY = myCoordY;
		}
		
		public byte[] serializeAckMessage () throws IOException {
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
		
		public int getAddresseeId() {
			return addresseeId;
		}

		public void setAddresseeId(int addresseeId) {
			this.addresseeId = addresseeId;
		}

		public int getMyCoordX() {
			return myCoordX;
		}

		public void setMyCoordX(int myCoordX) {
			this.myCoordX = myCoordX;
		}

		public int getMyCoordY() {
			return myCoordY;
		}

		public void setMyCoordY(int myCoordY) {
			this.myCoordY = myCoordY;
		}

		// Pai destinario - manda quando recebe de todos os filhos
}
