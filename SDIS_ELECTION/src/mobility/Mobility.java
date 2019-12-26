package mobility;

import java.util.Random;

// PARA COLOCAR EM TESTE; USAR CONSTRUTOR

import logic.Node;

public class Mobility extends Thread {
	protected int xCoordinate;
	protected int yCoordinate;
	protected int xRange;
	protected int yRange;
	protected Node node;
	protected boolean mobility;
	protected boolean test;

	protected int nodeSpeed;
	protected int constantToSleep = 1; // when =1 doesn't interfere with speed
	protected int nodeDirection;
	protected int nextX;
	protected int nextY;
	protected int sleepTime;
	protected boolean isMoving;
	protected boolean newMove;
	protected boolean print = true;

	private static final int speed = 1;

	public Mobility(Node node, boolean mobility, boolean test) {
		this.xCoordinate = node.getxCoordinate();
		this.yCoordinate = node.getyCoordinate();
		// Uncomment
		this.xRange = (int) node.getxMax();
		// this.xRange = 100;
		this.yRange = (int) node.getyMax();
		// this.yRange = 100;
		this.node = node;
		this.mobility = mobility;
		this.test = test;
	}

	public void newDestiny() {
		Random decisionMaker;

		if (isTest())
			return;

		// random point
		if (nodeDirection == 0) { // X axis
			decisionMaker = new Random();
			if (this.xRange == 0) {
				nextX = 0;
			} else {
				nextX = decisionMaker.nextInt(this.xRange);
			}

			nextY = yCoordinate;
			if (print)
				System.out.println("New X: " + nextX + " || New Y: " + nextY);

		} else { // Y axis
			decisionMaker = new Random();
			nextX = xCoordinate;
			if (this.yRange == 0) {
				nextY = 0;
			} else {
				nextY = decisionMaker.nextInt(this.yRange);
			}

			if (print)
				System.out.println("New X: " + nextX + " || New Y: " + nextY);
		}
	}

	public void newDirection() {
		Random decisionMaker = new Random();

		if (isTest())
			return;

		// Random Direction
		nodeDirection = decisionMaker.nextInt(2);
		if (print)
			System.out.println("New Direction: " + nodeDirection + "[0 - Horizontal, 1 - Vertical]");
	}

	public void randomSpeed() {
		Random decisionMaker = new Random();
		nodeSpeed = decisionMaker.nextInt(10); // Max Speed = 10 moves/second
		nodeSpeed = speed; // Same Speed for all, for now
	}

	public void randomSleepTime() {

		if (isTest())
			return;

		Random decisionMaker = new Random();
		sleepTime = decisionMaker.nextInt(20) * 1000; // max 20 seconds
	}

	public void testMobility(int xf, int yf, int direction, int sleep) {
		if (!test) {
			return;
		}

		this.nextX = xf;
		this.nextY = yf;
		this.nodeDirection = direction;
		this.constantToSleep = sleep;
		this.nodeSpeed = 1;
		setMoving(true);

		if ((this.yCoordinate == this.nextY) && (this.nodeDirection == 1)) {
			System.out.println("You set the same vertical position to move. Maybe you should change that");
		} else if ((this.xCoordinate == this.nextX) && (this.nodeDirection == 0)) {
			System.out.println("You set the same horizontal position to move. Maybe you should change that");
		}
	}

	@Override
	public void run() {
		while (mobility) {
			if (xCoordinate == nextX && yCoordinate == nextY && newMove == false) {
				System.out.println("Arrived!");
				xCoordinate = nextX;
				yCoordinate = nextY;
				node.setxCoordinate(xCoordinate);
				node.setyCoordinate(yCoordinate);

				if (isTest()) {
					System.out.println("___Node arrived!___");
					return;
				}

				randomSleepTime();
				setMoving(false);

				if (print)
					System.out.println("Node will not move for " + sleepTime + " ms.");

				try {
					setnewMove(true);
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					System.out.println("Mobility: Error putting thread to sleep (Node: " + node.getNodeID() + ")");
				}

			} else if (isMoving == true) {
				if (nodeDirection == 0) {
					System.out.println("Horizontal");
					if (xCoordinate > nextX) {
						xCoordinate--;
					} else if (xCoordinate < nextX) {
						xCoordinate++;
					} else if (xCoordinate == nextX) {
						if (isTest()) {
							System.out.println("You have arrived.");
							setMoving(false);
							return;
						}
					}

				} else if (nodeDirection == 1) {
					if (yCoordinate > nextY) {
						yCoordinate--;
					} else if (yCoordinate < nextY) {
						yCoordinate++;
					} else if (yCoordinate == nextY) {
						if (isTest()) {
							System.out.println("You have arrived.");
							setMoving(false);
							return;
						}
					}
				}

				if (print)
					System.out.println("X = " + xCoordinate + " || Y = " + yCoordinate);

				// if (!isTest())
				sleepTime = (1 / nodeSpeed) * constantToSleep * 1000;

				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					System.out.println("Mobility: Error putting thread to sleep (Node: " + node.getNodeID() + ")");
				}
			} else if (isMoving == false) {
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

	public boolean isTest() {
		return test;
	}

	public void setTest(boolean test) {
		this.test = test;
	}
}