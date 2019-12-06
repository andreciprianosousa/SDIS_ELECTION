package logic;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class AckMessage extends Message implements Serializable  {
	
	//This message needs to be serializable to allow its representation as a sequence of bytes
		private int incomingId;
		private int storedID;
		private float storedValue;

		public AckMessage(int incomingId, int leaderID, float leaderValue) {
			this.incomingId = incomingId;
			this.storedID = leaderID;
			this.storedValue = leaderValue;
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
		

}
