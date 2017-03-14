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
	private ArrayList<ArrayList<LinePoint>> allCirclePoints;
	
	public Circles(int size) {
		allCirclePoints = new ArrayList<ArrayList<LinePoint>>();
		allCirclePoints.ensureCapacity(size);
		ListofCircles = new ArrayList<CircleInfo>();
		ListofCircles.ensureCapacity(size);
	}
	
	private void printPossibleCircles() {
	    IJ.log("num circles "+allCirclePoints.size());
	    int i = 1;
	 	for (ArrayList<LinePoint> possibleCircle : allCirclePoints) {
	 		if (possibleCircle != null) {
		 		IJ.log("Circle "+i+" num points "+possibleCircle.size());
			    for (LinePoint point : possibleCircle) {
		            IJ.log("point x="+point.x+" y="+point.y);
				}
	 		}
	 		++i;
		}
	}
	
	// TODO: Chris - fill this is
	// Use this to find circles
	public void findCircles(Lines circles, byte[] pixels, int width, int height) {
		
		//this.printPossibleCircles();
		
		for (LineInfo li : circles.getEquationOfLines()) {
			//IJ.showMessage("m = "+li.slope+" y-intercept = "+li.yIntercept);
			if ((!Double.isNaN(li.slope)) && (!Double.isNaN(li.yIntercept))) {
			    for (int i=0; i<width; i++) {
			    	int y = (int) (Math.round((double)i*li.slope) + Math.round(li.yIntercept));
			    	y = -y;
			    	int positiveYintercept = (int)-li.yIntercept;
			    	
			    	try {
			    	    // Create code here to find circles 
			    		// look at lines parallel - above and below the line to 
			    		// find runs of white (value > x)?
			    		//(pixels[x + y * width]&0xFF)
			    		// Look for 45 pixels above the line
			    		searchCirclesHorizontalDir(positiveYintercept-45, positiveYintercept-5, width, 
			    				(int)Math.round(li.slope), (int)Math.round(li.yIntercept));
			    		
			    		//Look for 45 pixels below the line
			    		searchCirclesHorizontalDir(positiveYintercept+5, positiveYintercept+45, width, 
			    				(int)Math.round(li.slope), (int)Math.round(li.yIntercept));
			    		
			    	} catch (Exception e) {
			    		//IJ.log(e.getMessage());
			    		IJ.log(e.getMessage());
			    	}
			    }
			}
		}
 	}

	private void searchCirclesHorizontalDir(int top, int bottom, int width, int slope, int yIntercept) {
		for (int y=top; y>bottom; y+=5) {
			for (int x=0; x<width; x++) {
				//(pixels[x + y * width]&0xFF)
			}
			
		}
		
	}
	
	public int getCircleCount() {
		return circleCount;
	}
	
	// LinePoint cp - circle point
	public void addPointToCircleSet(LinePoint cp) {
		
		cp.y=-cp.y;
        double dist = 10000;
        ArrayList<LinePoint> cirNumToAddTo = null;
        if (allCirclePoints != null) {
			for (ArrayList<LinePoint> possibleCircle : allCirclePoints) {
			    if (possibleCircle != null) {
				    for (LinePoint point : possibleCircle) {
						dist = Math.sqrt(Math.pow((point.x-cp.x),2)+Math.pow(point.y-cp.y,2));
						if (dist < 190) { // change to configurable parm 
							cirNumToAddTo = possibleCircle;
						} 				
				   }
			    }
					
			}
        }
	    if (cirNumToAddTo != null) {
	    	cirNumToAddTo.add(cp);
	    } else {
	    	cirNumToAddTo = new ArrayList<LinePoint>(1);
	    	cirNumToAddTo.add(cp);
	    	allCirclePoints.ensureCapacity(allCirclePoints.size()+1);
	    	allCirclePoints.add(cirNumToAddTo);
	    }
		
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
