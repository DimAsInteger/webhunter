package edu.umuc.student.jplaschke;

public class CircleInfo {

	// Center
	private int x;
	private int y;
	private int radius;
	private boolean aggregate;
	
	public CircleInfo(int x, int y, int radius, boolean aggregate) {
		this.x = x;
		this.y = y;
		this.radius = radius;
		this.aggregate = aggregate;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getRadius() {
		return radius;
	}

	public void setRadius(int radius) {
		this.radius = radius;
	}

	public boolean isAggregate() {
		return aggregate;
	}

	public void setAggregate(boolean aggregate) {
		this.aggregate = aggregate;
	}
}
