package mobility;

import java.util.Random;

import logic.Node;

public class Mobility extends Thread{
	protected int xCoordinate;
	protected int yCoordinate;
	protected int xRange;
	protected int yRange;
	protected Node node;
	protected boolean mobility;
	
	protected int nodeSpeed;
	protected int nodeDirection;
	protected int nextX;
	protected int nextY;
	protected int sleepTime;
	protected boolean isMoving;
	protected boolean newMove;
	protected boolean print = false;
	
	public Mobility(Node node, boolean mobility) {
		this.xCoordinate = node.getxCoordinate();
		this.yCoordinate = node.getyCoordinate();
		this.xRange = (int) node.getxMax();
		this.yRange = (int) node.getyMax();
		this.node = node;
		this.mobility = mobility;
	}
	
	public void newDestiny() {
		Random decisionMaker; 
		
		// random point
		if(nodeDirection == 0) {						// X axis
			decisionMaker = new Random();
			nextX = decisionMaker.nextInt(this.xRange);
			nextY = yCoordinate;
			if(print)
				System.out.println("New X: " + nextX + " || New Y: " + nextY);
			
		} else {										// Y axis
			decisionMaker = new Random();
			nextX = xCoordinate;
			nextY = decisionMaker.nextInt(this.yRange);
			if(print)
				System.out.println("New X: " + nextX + " || New Y: " + nextY);
		}
	}
	
	public void newDirection() {
		Random decisionMaker = new Random();
		
		// Random Direction
		nodeDirection = decisionMaker.nextInt(2);
		if(print)
			System.out.println("New Direction: " + nodeDirection + "[0 - Horizontal, 1 - Vertical]");
	}
	
	public void randomSpeed() {
		Random decisionMaker = new Random();
		nodeSpeed = decisionMaker.nextInt(10);						// Max Speed = 10 moves/second
		nodeSpeed = 1;												// Same Speed for all, for now
	}
	
	public void randomSleepTime() {
		Random decisionMaker = new Random();
		sleepTime = decisionMaker.nextInt(20) * 1000; 				//max 20 seconds
	}
	
	@Override
	public void run() {
		while(mobility) {
			if (xCoordinate == nextX && yCoordinate == nextY && newMove == false) {
				System.out.println("Arrived!");
				xCoordinate = nextX;
				yCoordinate = nextY;
				node.setxCoordinate(xCoordinate);
				node.setyCoordinate(yCoordinate);
				randomSleepTime();
				setMoving(false);
				
				if(print)
					System.out.println("Node will not move for " + sleepTime + " s.");
								
				try {
					setnewMove(true);
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					System.out.println("Mobility: Error putting thread to sleep (Node: " + node.getNodeID()+ ")");
				}
				
			} else if (isMoving == true) {
				if(nodeDirection == 0) {
					if (xCoordinate > nextX) {
						xCoordinate--;
					} else if (xCoordinate < nextX){
						xCoordinate++;
					}
				
				} else {
					if (yCoordinate > nextY) {
						yCoordinate--;
					} else if (yCoordinate < nextY){
						yCoordinate++;;
					}
				};
				
				if(print)
					System.out.println("X = " + xCoordinate + " || Y = " + yCoordinate);
				sleepTime = 1/nodeSpeed * 1000;
				
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					System.out.println("Mobility: Error putting thread to sleep (Node: " + node.getNodeID()+ ")");
				}
			} else if (isMoving == false){
				newDirection();
				newDestiny();
				randomSpeed();
				setMoving(true);
				setnewMove(false);
			}
		}
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

	public int getxRange() {
		return xRange;
	}

	public void setxRange(int xRange) {
		this.xRange = xRange;
	}

	public int getyRange() {
		return yRange;
	}

	public void setyRange(int yRange) {
		this.yRange = yRange;
	}

	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}

	public int getNodeSpeed() {
		return nodeSpeed;
	}

	public void setNodeSpeed(int nodeSpeed) {
		this.nodeSpeed = nodeSpeed;
	}

	public int getNodeDirection() {
		return nodeDirection;
	}

	public void setNodeDirection(int nodeDirection) {
		this.nodeDirection = nodeDirection;
	}

	public int getNextX() {
		return nextX;
	}

	public void setNextX(int nextX) {
		this.nextX = nextX;
	}

	public int getNextY() {
		return nextY;
	}

	public void setNextY(int nextY) {
		this.nextY = nextY;
	}

	public int getSleepTime() {
		return sleepTime;
	}

	public void setSleepTime(int sleepTime) {
		this.sleepTime = sleepTime;
	}

	public boolean isMoving() {
		return isMoving;
	}

	public void setMoving(boolean isMoving) {
		this.isMoving = isMoving;
	}

	public boolean isnewMove() {
		return newMove;
	}

	public void setnewMove(boolean newMove) {
		this.newMove = newMove;
	}
}
