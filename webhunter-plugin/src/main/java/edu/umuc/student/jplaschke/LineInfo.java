package edu.umuc.student.jplaschke;

public class LineInfo {
	
	double slope;
	double yIntercept;
	private int thickness;
	private boolean aggregate;
	
	public LineInfo(double x, double y, int thickness, boolean aggregate) {
	   this.slope = x;
	   this.yIntercept = y;
	   this.thickness = thickness;
	   this.aggregate = aggregate;
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

	public double getSlope() {
		return slope;
	}

	public void setSlope(double slope) {
		this.slope = slope;
	}

	public double getyIntercept() {
		return yIntercept;
	}

	public void setyIntercept(double yIntercept) {
		this.yIntercept = yIntercept;
	}
	

}
