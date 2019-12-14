package logic;

public class AckMessage {
	
	    //This message needs to be serializable to allow its representation as a sequence of bytes
		private int incomingId;
		private int storedID;
		private float storedValue;

		private int xCoordinate;
		private int yCoordinate;
		private int addresseeId;
		private String messageCode = "ack00";
		// ACK Message always sent to 1 node (Parent)
		public AckMessage(int incomingId, int leaderID, float leaderValue, int xCoordinate, int yCoordinate, int addresseeId) {
			this.incomingId = incomingId;
			this.storedID = leaderID;
			this.storedValue = leaderValue;
			this.addresseeId = addresseeId;
			this.xCoordinate = xCoordinate;
			this.yCoordinate = yCoordinate;
		}
		
		@Override
	    public String toString() {
			return String.format(messageCode + "/" + incomingId + "/" + storedID + "/" + storedValue + "/" + xCoordinate + "/" + yCoordinate + "/" + addresseeId +"/" ); 
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
