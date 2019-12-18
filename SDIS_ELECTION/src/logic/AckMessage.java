package logic;

public class AckMessage {

	private int incomingId;
	private int leaderID;
	private float leaderValue;
	private int xCoordinate;
	private int yCoordinate;
	private int addresseeId;
	private String messageCode = "ack00";

	// ACK Message always sent to 1 node (Parent)
	public AckMessage(int incomingId, int leaderID, float leaderValue, int xCoordinate, int yCoordinate,
			int addresseeId) {
		this.incomingId = incomingId;
		this.leaderID = leaderID;
		this.leaderValue = leaderValue;
		this.addresseeId = addresseeId;
		this.xCoordinate = xCoordinate;
		this.yCoordinate = yCoordinate;
	}

	@Override
	public String toString() {
		return String.format(messageCode + "/" + incomingId + "/" + leaderID + "/" + leaderValue + "/" + xCoordinate
				+ "/" + yCoordinate + "/" + addresseeId + "/");
	}

	public int getIncomingId() {
		return incomingId;
	}

	public void setIncomingId(int incomingId) {
		this.incomingId = incomingId;
	}

	public int getStoredID() {
		return leaderID;
	}

	public void setStoredID(int storedID) {
		this.leaderID = storedID;
	}

	public float getStoredValue() {
		return leaderValue;
	}

	public void setStoredValue(float storedValue) {
		this.leaderValue = storedValue;
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
