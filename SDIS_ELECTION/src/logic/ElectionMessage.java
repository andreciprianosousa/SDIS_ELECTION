package logic;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class ElectionMessage implements Serializable{
	
	//This message needs to be serializable to allow its representation as a sequence of bytes
		private ComputationIndex cp;
		private int incomingId;
		private int myCoordX;
		private int myCoordY;
		private int addresseeId;

		public ElectionMessage(int incomingId, ComputationIndex cp, int myCoordX, int myCoordY, int addresseeId) {
			this.cp = cp;
			this.incomingId = incomingId;
			this.myCoordX = myCoordX;
			this.myCoordY = myCoordY;
			this.addresseeId = addresseeId;
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

		public int getAddresseeId() {
			return addresseeId;
		}

		public void setAddresseeId(int addresseeId) {
			this.addresseeId = addresseeId;
		}
}
