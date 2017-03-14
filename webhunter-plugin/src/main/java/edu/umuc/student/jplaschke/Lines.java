package edu.umuc.student.jplaschke;

import ij.IJ;

import java.util.ArrayList;

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
	
	public Lines(int size) {
		ListOfLines = new ArrayList<ArrayList<LinePoint>>();
		ListOfLines.ensureCapacity(size);
		EquationOfLines = new ArrayList<LineInfo>(size);
	}
	
	// Use this to add the first point (edges) on the x=0 axis
	public void addPointToLine(int lineNum, LinePoint lp) {
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
	        	IJ.log("minSep = "+minSeparation);
	    }
 	} 
	
	// Add point to closest line
	public void addPointToClosestLine(LinePoint lp) {
		double minDistance = 2000000000;
		ArrayList<LinePoint> lineNumToAddTo = null; // add to this line (minDistance)
		// change y to negative 
		// cartesian plane starts at (0,0) and y is negative
		
		// TODO - what if lines cross?
		//        what if a new line starts from x>20,y=0
		// NOTE: 10 is the slice thickness
		lp.y=-lp.y;
        LinePoint cp = null;  // for debug - closest point
        double dist = 10000;
		for (ArrayList<LinePoint> line : ListOfLines) {
			for (LinePoint point: line) {
				dist = Math.sqrt(Math.pow((point.x-lp.x),2)+Math.pow(point.y-lp.y,2));
				if ((dist < minDistance) && (dist < (minSeparation))) {
						minDistance = dist;
						lineNumToAddTo = line;  
						cp = point;
				} 
				
			}
		}
	
		if (lineNumToAddTo != null) {
	        lineNumToAddTo.add(lp);  
		} else {
			if (dist<100) IJ.log("dist ="+dist+" DID NOT ADD point add closest point x = "+lp.x+" y = "+lp.y);
		}
	
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
			IJ.log("number of points "+n);
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
		    IJ.log("line num"+lineNum+" y   = " + beta1 + " * x + " + beta0+"\n"+
		    			"R^2                 = " + R2+"\n"+
		    			"std error of slope = " + Math.sqrt(svar1)+"\n"+
		    			"std error of y-intercept = " + Math.sqrt(svar0));
		    LineInfo tmp = new LineInfo(beta1, beta0, thickness, false);
			++lineNum;
			if (EquationOfLines == null) {
				EquationOfLines = new ArrayList<LineInfo>();
			}
			// If standard error is less than 4? it is a line
			// if it is greater than the line contains a circle??? maybe
			if (Math.sqrt(svar0) < 30) {
			    EquationOfLines.add(tmp);
			}
			if (Double.isNaN(svar0)) {
				 EquationOfLines.add(tmp);		
			}
			
		}
	}

	public ArrayList<LineInfo> getEquationOfLines() {
		return EquationOfLines;
	}

	public void setEquationOfLines(ArrayList<LineInfo> equationOfLines) {
		EquationOfLines = equationOfLines;
	}
	

}
