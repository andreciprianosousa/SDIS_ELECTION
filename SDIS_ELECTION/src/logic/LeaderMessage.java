package logic;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class LeaderMessage{

	private int incomingId;
	private int storedID;
	private float storedValue;
	private int xCoordinate;
	private int yCoordinate;
	private boolean special;

	private Set<Integer> mailingList;
	private String messageCode = "leadr";

	public LeaderMessage(int incomingId, int leaderID, float leaderValue, int xCoordinate, int yCoordinate, boolean special, Set<Integer> mailingList) {
		this.incomingId = incomingId;
		this.storedID = leaderID;
		this.storedValue = leaderValue;
		this.xCoordinate = xCoordinate;
		this.yCoordinate = yCoordinate;
		this.mailingList = mailingList;
		this.special = special;
	}

	@Override
	public String toString() {
		int[] mailingListInt = mailingList.stream().mapToInt(Integer::intValue).toArray();
		String mailingListString = IntStream.of(mailingListInt).mapToObj(Integer::toString).collect(Collectors.joining(","));
		return String.format(messageCode + "/" + incomingId + "/" + storedID + "/" + storedValue+  "/" + xCoordinate + "/" + yCoordinate + "/" + special + "/" + mailingListString + "/"); 
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

	public boolean isSpecial() {
		return special;
	}

	public void setSpecial(boolean special) {
		this.special = special;
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

	public Set<Integer> getMailingList() {
		return mailingList;
	}
	public void setMailingList(HashSet<Integer> mailingList) {
		this.mailingList = mailingList;
	}
}
