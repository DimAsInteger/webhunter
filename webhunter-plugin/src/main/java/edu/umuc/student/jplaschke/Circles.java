package edu.umuc.student.jplaschke;

import ij.IJ;

import java.awt.Color;
import java.awt.Paint;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;

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
	private ArrayList<ArrayList<LinePoint>> allCirclePoints;
	private ArrayList<CircleInfo> ListofCircles;
	
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
	    	     new DecimalFormat("000.000",
	    	                       new DecimalFormatSymbols(Locale.US));

	    	   // fit a circle to the test points
	    	   CircleFitter fitter = new CircleFitter();
	    	   try {
	    		   fitter.initialize(points);
	    		   // minimize the residuals
	    		   int iter = fitter.minimize(100, 0.1, 1.0e-5);
	    		   IJ.log("converged circle: x="
 	                      + format.format(fitter.getCenter().x)
 	                      + " x="     + format.format(fitter.getCenter().y)
 	                      + " r="     + format.format(fitter.getRadius()));
	    		   int x = (int)Math.round(fitter.getCenter().x);
	    		   int y = (int)Math.round(fitter.getCenter().y);
	    		   int r = (int)Math.round(fitter.getRadius());
	    		   
	    		   if (r < diam) {
	    			   CircleInfo ci = new CircleInfo(x, y, r, false);
	    			   ci.setCircleNum(ListofCircles.size()+1);
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
	public void findCircles(Lines lines, byte[] pixels, int width, int height, int circleDiameter) {
				
		for (LineInfo li : lines.getEquationOfLines()) {
			//IJ.showMessage("m = "+li.slope+" y-intercept = "+li.yIntercept);
			if ((!Double.isNaN(li.slope)) && (!Double.isNaN(li.yIntercept))) {
			    for (int i=0; i<width; i++) {
			    	//int y = (int) (Math.round((double)i*li.slope) + Math.round(li.yIntercept));
			    	//y = -y;
			    	int positiveYintercept = (int)-li.yIntercept;
			    	
			    	try {
			    	    // Create code here to find circles 
			    		// look at lines parallel - above and below the line to 
			    		// find runs of white (value > x)?
			    		//(pixels[x + y * width]&0xFF)
			    		// Look for halfDiam pixels above the line
			    		int maxCirDiam = circleDiameter;
			    		int halfDiam = (int)Math.round((double)maxCirDiam); // / 2.0);
			    		searchCirclesHorizontalDir(pixels, positiveYintercept-halfDiam, positiveYintercept-5, width, 
			    				           li.slope, maxCirDiam);
			    		
			    		//Look for halfDiam pixels below the line
			    		searchCirclesHorizontalDir(pixels, positiveYintercept+5, positiveYintercept+halfDiam, width, 
			    				                    li.slope,maxCirDiam);
			    		
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
	
	private boolean checkRight(int start, double slope, double i,
			                        byte[] pixels, int width, int val, int n) {
		
		boolean test = true;
		for (int x=start; x<n; x++) {
		    int y = (int) (Math.round((double)x*slope) + Math.round(i));
    	    y = -y;
    	    if (((pixels[x + y * width])&0xFF) != (int)val) {
    	    	test = true;
    	    }
		}
		return test;
	}
	
	private void searchCirclesHorizontalDir(byte[] pixels, int top, int bottom, int width, 
			                     double slope, int circleDiameter) {
		int state = SEARCH_LEFT_EDGE;
		for (int i=top; i>=bottom; i++) {
			state = SEARCH_LEFT_EDGE;
			for (int x=0; x<width; x++) {
				int y = (int) (Math.round((double)x*slope) + Math.round(i));
		    	y = -y;
		    
				if (state == SEARCH_LEFT_EDGE) {
					if (((pixels[x + y * width])&0xFF) == (int)10) {
						if (checkRight(x, slope, i,
		                        pixels, width, 10, 2)) {
							state = SEARCH_RIGHT_EDGE;
							LinePoint cp = new LinePoint(x, y, -1, false);
							addPointToCircleSet(cp, circleDiameter);
						}
					}
				} else {
					if (((pixels[x + y * width])&0xFF) == (int)80) {
						if (checkRight(x, slope, i,
		                        pixels, width, 80, 2)) {
							state = SEARCH_LEFT_EDGE;
							LinePoint cp = new LinePoint(x, y, -1, false);
							addPointToCircleSet(cp, circleDiameter);
						}
					}
				}
			}
			
		}
		
	}
	
	public int getCircleCount() {
		return circleCount;
	}
	
	public void createHistogram(double[] values, double min, double max, String name) {
		HistogramDataset dataset = new HistogramDataset();
		dataset.setType(HistogramType.FREQUENCY);
       	dataset.addSeries("Histogram",values,values.length);
       	String plotTitle = "Droplet Area Histogram"; 
       	String xaxis = "Droplet Area (microns squared)";
       	String yaxis = "Count"; 
       	PlotOrientation orientation = PlotOrientation.VERTICAL; 
       	boolean show = false; 
       	boolean toolTips = false;
       	boolean urls = false; 
       	JFreeChart chart = ChartFactory.createHistogram( plotTitle, xaxis, yaxis, 
                dataset, orientation, show, toolTips, urls);
       	XYPlot plot = (XYPlot) chart.getPlot();
        XYBarRenderer renderer = (XYBarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardXYBarPainter());
        Paint[] paintArray = new Paint[1];
        paintArray[0] = new Color(0x800000ff, true);
        plot.setDrawingSupplier(new DefaultDrawingSupplier(
                paintArray,
                DefaultDrawingSupplier.DEFAULT_FILL_PAINT_SEQUENCE,
                DefaultDrawingSupplier.DEFAULT_OUTLINE_PAINT_SEQUENCE,
                DefaultDrawingSupplier.DEFAULT_STROKE_SEQUENCE,
                DefaultDrawingSupplier.DEFAULT_OUTLINE_STROKE_SEQUENCE,
                DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE));
       	int width = 500;
       	int height = 300; 
        try {
        ChartUtilities.saveChartAsJPEG(new File(name), chart, width, height);
        } catch (IOException e) {}
            
	}
	

	// LinePoint cp - circle point
	public void addPointToCircleSet(LinePoint cp, int circleDiameter) {
		
		cp.y=-cp.y;
        double dist = 10000;
        double minDist = 100000;
        ArrayList<LinePoint> cirNumToAddTo = null;
        if (allCirclePoints != null) {
			for (ArrayList<LinePoint> possibleCircle : allCirclePoints) {
			    if (possibleCircle != null) {
				    for (LinePoint point : possibleCircle) {
						dist = Math.sqrt(Math.pow((point.x-cp.x),2)+Math.pow(point.y-cp.y,2));
						//if (dist <= circleDiameter) {
						if (dist <= 159) {
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

		
	// TODO: William fill this out
    public void createCircleReport() {
    	
    	// Write report header
    	// write number of circles
    	// write circle info
    	for (CircleInfo ci : ListofCircles) {
    		
    	}
    }
	
	
	public ArrayList<CircleInfo> getListofCircles() {
		return ListofCircles;
	}

	public void setListofCircles(ArrayList<CircleInfo> listofCircles) {
		ListofCircles = listofCircles;
	}

	public static void main(String[] args) {
         // Create a number of fake circles
         int width = 5000;
         int height = 4000;
         int numCircles;
         Random rand = new Random();
         numCircles = rand.nextInt(40) + 10;
         Circles circle = new Circles(numCircles);
         
         // create random circles
         ArrayList<CircleInfo> ListOfCircles = circle.ListofCircles;
         for (int i=0; i<numCircles; i++) {
        	 CircleInfo circleInfo = new CircleInfo(0, 0, 0, false);
        	 circleInfo.setRadius(rand.nextInt(150)+30);
        	 circleInfo.setX(rand.nextInt(width));
        	 circleInfo.setY(rand.nextInt(height));
        	 circleInfo.setAggregate(rand.nextInt(10)<9 ? false : true);
        	 ListOfCircles.add(circleInfo);
         }
         circle.setCircleCount(numCircles);
         
         // Create report
         circle.createCircleReport();
         
	}
}
