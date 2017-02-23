package edu.umuc.student.jplaschke;

public class LinePoint {
	
	int x;
	int y;
	private int thickness;
	private boolean aggregate;
	
	public LinePoint(int x, int y, int thickness, boolean aggregate) {
	   this.x = x;
	   this.y = y;
	   this.thickness = thickness;
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
	public int getThickness() {
		return thickness;
	}
	public void setThickness(int thickness) {
		this.thickness = thickness;
	}
	public boolean isAggregate() {
		return aggregate;
	}
	public void setAggregate(boolean aggregate) {
		this.aggregate = aggregate;
	}
	

}
