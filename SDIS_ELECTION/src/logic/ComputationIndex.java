package logic;

import java.io.Serializable;

public class ComputationIndex implements Serializable{

	protected int id;
	protected int num;
	protected float value;
	
	public ComputationIndex(int id, int num, float value) {
		this.id = id;
		// Assuming it will be value of node and id (in paper they use a pair, 
		// because they assume the id=value, but in our case that may not hold true, 
		// and as such, the value should be the primary tie breaker and then the id)
		this.num = num;
		this.value = value;
	}
	
	public int getId() {
		return this.id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getNum() {
		return this.num;
	}
	public void setNum(int num) {
		this.num = num;
	}
	public float getValue() {
		return this.value;
	}
	public void setValue(float value) {
		this.value = value;
	}
}
