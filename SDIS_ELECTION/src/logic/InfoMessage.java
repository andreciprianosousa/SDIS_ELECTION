package logic;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class InfoMessage {

	private int incomingId;
	private float leaderValue;
	private int leaderId;
	private int addresseeId = 0;
	private String messageCode = "info0";
	
	
	public InfoMessage(int id, int leader, float sv, int addressee) {
		this.incomingId = id;
		this.leaderId = leader;
		this.leaderValue = sv;
		this.addresseeId = addressee;
	}
	
	@Override
    public String toString() {
		return String.format(messageCode + "/" + incomingId + "/" + leaderId + "/" + leaderValue + "/" + addresseeId +"/" ); 
	}
	
	public int getIncomingId() {
		return incomingId;
	}


	public void setIncomingId(int incomingId) {
		this.incomingId = incomingId;
	}


	public int getLeaderId() {
		return leaderId;
	}


	public void setLeaderId(int leaderId) {
		this.leaderId = leaderId;
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

}
