package edu.umuc.student.jplaschke;

import ij.IJ;

import java.util.ArrayList;
import java.util.Random;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 * Data structure for lines
 * 
 * @author John Plaschke
 * 
 */
public class Lines {
	
	private ArrayList<ArrayList<LinePoint>> ListOfLines;

	private int lineCount = 0;
	private ArrayList<LineInfo> EquationOfLines;
	private int prevY = 0;
	private int minSeparation = 5000;
	private int lineNum;
	
	public Lines(int size) {
		ListOfLines = new ArrayList<ArrayList<LinePoint>>();
		ListOfLines.ensureCapacity(size);
	
		EquationOfLines = new ArrayList<LineInfo>(size);
	}
	
	// Use this to add the first point (edges) on the x=0 axis
	public void addPointToLine(int lineNum, LinePoint lp) {
		this.lineNum = lineNum;
		// change y to negative 
		// Cartesian plane starts at (0,0) and y is negative		
		lp.y=-lp.y;
		ArrayList<LinePoint> tmp = null;
		tmp = new ArrayList<LinePoint>(1);
		tmp.add(lp);
		if (ListOfLines == null) {
			ListOfLines = new ArrayList<ArrayList<LinePoint>>();
		}
		
		ListOfLines.ensureCapacity(lineNum+1);
		if (lp.x == 0) {
			IJ.log("add first point x = "+lp.x+" y = "+lp.y);
		}
		ListOfLines.add(tmp);
		if (lineNum > lineCount) {
		   lineCount = lineNum+1;
		}
		int diff = Math.abs(this.prevY - lp.y);
		if (diff < this.minSeparation) {
	        	minSeparation = (int)Math.round((double)diff/4.0*3.0);
	        	this.prevY = lp.y;
	        	//IJ.log("minSep = "+minSeparation);
	    }
 	} 
	
	// Add point to closest line
	public void addPointToClosestLine(LinePoint lp, int xInc) {
		double minDistance = 2000000000;
		ArrayList<LinePoint> lineNumToAddTo = null; // add to this line (minDistance)
		// change y to negative 
		// cartesian plane starts at (0,0) and y is negative
		
		// TODO - what if lines cross?
		//        what if a new line starts from x>20,y=0
		lp.y=-lp.y;
        LinePoint cp = null;  // for debug - closest point
        double dist = 10000;
		for (ArrayList<LinePoint> line : ListOfLines) {
			for (LinePoint point: line) {
				dist = Math.sqrt(Math.pow((point.x-lp.x),2)+Math.pow(point.y-lp.y,2));
				// TODO: rethink use of minSeparation
			//	if ((dist < minDistance)) { // && (dist < (minSeparation))) {
				if (dist < xInc*2) {
					    minDistance = dist;
						lineNumToAddTo = line;  
						cp = point;
				} 
				
			}
		}
	
		if (lineNumToAddTo != null) {
	        lineNumToAddTo.add(lp);  
		} else {
			//IJ.log("dist ="+dist+" DID NOT ADD point add closest point x = "+lp.x+" y = "+lp.y);
			this.addPointToLine(lineNum+1, lp);
		}
	
 	}
	
	double[] calcThicknessStats() {
		double[] thickness = new double[this.EquationOfLines.size()];
		int i=0;
		for (LineInfo li : this.EquationOfLines) {
			if ((!Double.isNaN(li.slope)) && (!Double.isNaN(li.yIntercept))) {
				thickness[i] = (double)li.getThickness();
				//IJ.log("thickness = "+thickness[i]);
				i++;
			}
		}
			
		return StatsFunctions.calcStatistics(thickness);
	}
	
	double[] calcMinMaxDistance(int width) {
		double[] minMax = new double[this.EquationOfLines.size()*2];
		int i=0;
		LineInfo prevLine = null;
		double min = 100000;
		double max = -1000;
		
		for (LineInfo li : this.EquationOfLines) {
			min = 100000;
			max = -10000;
			if ((!Double.isNaN(li.slope)) && (!Double.isNaN(li.yIntercept))) {
				if (prevLine == null) {
					prevLine = li;
				} else {
					for (int x=0; x<width; x++) {
						double dist = (prevLine.slope-li.slope)*x + (prevLine.yIntercept-li.yIntercept);
					    if (dist > max) {
					    	max = dist;
					    } 
					    if (dist < min) {
					        min = dist;
					    }
					
					}
					minMax[i] = max;
					++i;
					minMax[i] = min;
					++i;
					prevLine = li;
				}
			}
		}
			
		return minMax;
		
	}
	
	
		
	// Calculate linear regression for a line to get y=mx+b
	public void CalculateLinearReqressions() {
		IJ.showMessage("Num Lines in list "+ListOfLines.size());
		int lineNum = 1;
		for (ArrayList<LinePoint> line : ListOfLines) {
			int MAXN = line.size()+1;
	        int n = 0;
	        double[] x = new double[MAXN];
	        double[] y = new double[MAXN];
	        double sumx = 0.0, sumy = 0.0, sumx2 = 0.0;
	        int thickness = 0;
			for (LinePoint point: line) {
			
		        // first pass: read in data, compute xbar and ybar
		        x[n] = (double)point.x;
	            y[n] = (double)point.y;
	            sumx  += x[n];
	            sumx2 += x[n] * x[n];
	            sumy  += y[n];
	            thickness += point.getThickness();
	            n++;
	           
	        }
			// Average thickness
			thickness = thickness/n;
			//IJ.log("number of points "+n);
		    double xbar = sumx / n;
		    double ybar = sumy / n;

		   // second pass: compute summary statistics
		    double xxbar = 0.0, yybar = 0.0, xybar = 0.0;
		    for (int i = 0; i < n; i++) {
		        xxbar += (x[i] - xbar) * (x[i] - xbar);
		        yybar += (y[i] - ybar) * (y[i] - ybar);
		        xybar += (x[i] - xbar) * (y[i] - ybar);
		    }
		    double beta1 = xybar / xxbar;
		    double beta0 = ybar - beta1 * xbar;

		    // print results
		    
		    // analyze results
		    int df = n - 2;
		    double rss = 0.0;      // residual sum of squares
		    double ssr = 0.0;      // regression sum of squares
		    for (int i = 0; i < n; i++) {
		        double fit = beta1*x[i] + beta0;
		        rss += (fit - y[i]) * (fit - y[i]);
		        ssr += (fit - ybar) * (fit - ybar);
		    }
		    double R2    = ssr / yybar;
		    double svar  = rss / df;
		    double svar1 = svar / xxbar;
		    double svar0 = svar/n + xbar*xbar*svar1;
		    //IJ.log("line num"+lineNum+" y   = " + beta1 + " * x + " + beta0+"\n"+
		    //			"R^2                 = " + R2+"\n"+
		    //			"std error of slope = " + Math.sqrt(svar1)+"\n"+
		    //			"std error of y-intercept = " + Math.sqrt(svar0));
		    LineInfo tmp = new LineInfo(beta1, beta0, thickness, false);
			++lineNum;
			if (EquationOfLines == null) {
				EquationOfLines = new ArrayList<LineInfo>();
			}
			// If standard error is less than 4? it is a line
			// if it is greater than the line contains a circle??? maybe
			if (!Double.isNaN(svar0)) {		
				if (Math.sqrt(svar0) < 30) {   //TODO think this over
			       EquationOfLines.add(tmp);
				}
			}
			
		}
	}

	public ArrayList<LineInfo> getEquationOfLines() {
		return EquationOfLines;
	}

	public void setEquationOfLines(ArrayList<LineInfo> equationOfLines) {
		EquationOfLines = equationOfLines;
	}
	
	
	// TODO: Delaram fill this out
    public void createLineReport() {
    	
    	// Write report header
    	// write number of lines
    	// write line info
    	for (LineInfo li : EquationOfLines) {
    		
    	}
    }

	public static void main(String[] args) {
         // Create a number of fake lines
         int width = 5000;
         int height = 4000;
         int numLines;
         Random rand = new Random();
         numLines = rand.nextInt(5) + 10;
         ArrayList<LineInfo> equationOfLines = new ArrayList<LineInfo>(numLines);
         Lines lines = new Lines(numLines);
         
         for (int i=0; i<numLines; i++) {
        	 LineInfo lineInfo = new LineInfo(0, 0, 0, false);
        	 lineInfo.setSlope(1.0 - (double)(rand.nextInt(2)/rand.nextInt(4)));
        	 lineInfo.setyIntercept(0-rand.nextInt(height));
        	 lineInfo.setThickness(16-rand.nextInt(8));
        	 lineInfo.setAggregate(rand.nextInt(10)<9 ? false : true);
        	 equationOfLines.add(lineInfo);
         }
         lines.setEquationOfLines(equationOfLines); 
         
         // Line report
         lines.createLineReport();
         
	}
	
}
