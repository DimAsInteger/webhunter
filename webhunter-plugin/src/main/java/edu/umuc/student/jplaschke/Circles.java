package edu.umuc.student.jplaschke;

import ij.IJ;

import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Locale;

import edu.umuc.student.jplaschke.CircleFitter.LocalException;

/**
 * Data structure for Circles
 * 
 * @author John Plaschke, Chris McFall
 * 
 */
public class Circles {
	
	private final int SEARCH_LEFT_EDGE = 0;
	private final int SEARCH_RIGHT_EDGE = 1;
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
	
	
	private void performCircleRegression(int diam) {
	    IJ.log("num circles "+allCirclePoints.size()+" max diam ="+diam);
	    int i = 1;
	 	for (ArrayList<LinePoint> possibleCircle : allCirclePoints) {
	 		if ((possibleCircle != null) && (possibleCircle.size() > 1))  {
		 		IJ.log("Circle "+i+" num points "+possibleCircle.size());
		 		ArrayList list = new ArrayList();
			    for (LinePoint point : possibleCircle) {
		            IJ.log("point x="+point.x+" y="+point.y);
		            list.add(new Point2D.Double(point.x,-point.y));
				}
			    Point2D.Double[] points =
			    	     (Point2D.Double[]) list.toArray(new Point2D.Double[list.size()]);

	    	   DecimalFormat format =
	    	     new DecimalFormat("000.00000000",
	    	                       new DecimalFormatSymbols(Locale.US));

	    	   // fit a circle to the test points
	    	   CircleFitter fitter = new CircleFitter();
	    	   try {
	    		   fitter.initialize(points);
	    		   // minimize the residuals
	    		   //int iter = fitter.minimize(100, 0.1, 1.0e-12);
	    		   IJ.log("converged circle: x="
 	                      + format.format(fitter.getCenter().x)
 	                      + " x="     + format.format(fitter.getCenter().y)
 	                      + " r="     + format.format(fitter.getRadius()));
	    		   int x = (int)Math.round(fitter.getCenter().x);
	    		   int y = (int)Math.round(fitter.getCenter().y);
	    		   int r = (int)Math.round(fitter.getRadius());
	    		   
	    		   if (r < diam) {
	    			   CircleInfo ci = new CircleInfo(x, y, r, false);
	    			   ListofCircles.add(ci);
	    		   }
	    	   } catch (LocalException e) {
	    		   // TODO Auto-generated catch block
	    		   //e.printStackTrace();
	    	   }
	    	   
	 		}
	 		++i;
		}
	 	
	 	
	}
	// TODO: Chris - fill this is
	// Use this to find circles
	public void findCircles(Lines circles, byte[] pixels, int width, int height, int circleDiameter) {
		
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
			    		int halfDiam = (int)Math.round((double)circleDiameter / 2.0);
			    		searchCirclesHorizontalDir(pixels, positiveYintercept-halfDiam, positiveYintercept-2, width, 
			    				(int)Math.round(li.slope), circleDiameter);
			    		
			    		//Look for 45 pixels below the line
			    		searchCirclesHorizontalDir(pixels, positiveYintercept+2, positiveYintercept+halfDiam, width, 
			    				                     (int)Math.round(li.slope),circleDiameter);
			    		
			    	} catch (Exception e) {
			    		//IJ.log(e.getMessage());
			    		IJ.log(e.getMessage());
			    	}
			    }
			}
		}
		//this.printPossibleCircles();
		IJ.log("circle diameter = "+circleDiameter);
		this.performCircleRegression(circleDiameter);
 	}

	private void searchCirclesHorizontalDir(byte[] pixels, int top, int bottom, int width, 
			                     int slope, int circleDiameter) {
		int state = SEARCH_LEFT_EDGE;
		for (int i=top; i>bottom; i++) {
			state = SEARCH_LEFT_EDGE;
			for (int x=0; x<width; x++) {
				int y = (int) (Math.round((double)x*slope) + Math.round(i));
		    	y = -y;
		    
				if (state == SEARCH_LEFT_EDGE) {
					if (((pixels[x + y * width])&0xFF) == (int)10) {
						state = SEARCH_RIGHT_EDGE;
						LinePoint cp = new LinePoint(x, y, -1, false);
						addPointToCircleSet(cp, circleDiameter);
					}
				} else {
					if (((pixels[x + y * width])&0xFF) == (int)80) {
						state = SEARCH_LEFT_EDGE;
						LinePoint cp = new LinePoint(x, y, -1, false);
						addPointToCircleSet(cp, circleDiameter);
					}
				}
			}
			
		}
		
	}
	
	public int getCircleCount() {
		return circleCount;
	}
	
	// LinePoint cp - circle point
	public void addPointToCircleSet(LinePoint cp, int circleDiameter) {
		
		cp.y=-cp.y;
        double dist = 10000;
        ArrayList<LinePoint> cirNumToAddTo = null;
        if (allCirclePoints != null) {
			for (ArrayList<LinePoint> possibleCircle : allCirclePoints) {
			    if (possibleCircle != null) {
				    for (LinePoint point : possibleCircle) {
						dist = Math.sqrt(Math.pow((point.x-cp.x),2)+Math.pow(point.y-cp.y,2));
						if (dist < circleDiameter) { // change to configurable parm 
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
