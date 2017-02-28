package edu.umuc.student.jplaschke;

import ij.IJ;

import java.util.ArrayList;

/**
 * Data structure for Circles
 * 
 * @author John Plaschke, Chris McFall
 * 
 */
public class Circles {
	
	private int circleCount = 0;
	private ArrayList<CircleInfo> ListofCircles;
	
	public Circles(int size) {
		ListofCircles = new ArrayList<CircleInfo>();
		ListofCircles.ensureCapacity(size);
	}
	
	// TODO: Chris - fill this is
	// Use this to find circles
	public void findCircles(Lines lines, byte[] pixels, int width, int height) {
		for (LineInfo li : lines.getEquationOfLines()) {
			IJ.showMessage("m = "+li.slope+" y-intercept = "+li.yIntercept);
			if ((!Double.isNaN(li.slope)) && (!Double.isNaN(li.yIntercept))) {
			    for (int i=0; i<width; i++) {
			    	int y = (int) (Math.round((double)i*li.slope) + Math.round(li.yIntercept));
			    	y = -y;
			    	
			    	try {
			    	    // Create code here to find circles 
			    		// look at lines parallel - above and below the line to 
			    		// find runs of white (value > x)?
			    		
			    	} catch (Exception e) {
			    		//IJ.log(e.getMessage());
			    		IJ.log(e.getMessage());
			    	}
			    }
			}
		}
 	}

	public int getCircleCount() {
		return circleCount;
	}

	public void setCircleCount(int circleCount) {
		this.circleCount = circleCount;
	}

	public ArrayList<CircleInfo> getListofCircles() {
		return ListofCircles;
	}

	public void setListofCircles(ArrayList<CircleInfo> listofCircles) {
		ListofCircles = listofCircles;
	} 
		

}
