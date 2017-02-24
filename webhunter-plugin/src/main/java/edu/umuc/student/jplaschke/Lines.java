package edu.umuc.student.jplaschke;

import ij.IJ;

import java.util.ArrayList;


public class Lines {
	
	private ArrayList<ArrayList<LinePoint>> ListOfLines;
	private int lineCount = 0;
	private ArrayList<LineInfo> EquationOfLines;
	
	public Lines(int size) {
		ListOfLines = new ArrayList<ArrayList<LinePoint>>();
		ListOfLines.ensureCapacity(size);
		EquationOfLines = new ArrayList<LineInfo>(size);
	}
	
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
		ListOfLines.add(tmp);
		if (lineNum > lineCount) {
		   lineCount = lineNum;
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
		for (ArrayList<LinePoint> line : ListOfLines) {
			for (LinePoint point: line) {
				double dist = Math.sqrt(Math.pow((point.x-lp.x),2)+Math.pow(point.y-lp.y,2));
				if ((dist < minDistance) && (dist > 10)) {
					minDistance = dist;
					lineNumToAddTo = line;
				}
			}
		}
	    lineNumToAddTo.add(lp);
	
 	}
	
		
	// Calculate linear regression for a line to get y=mx+b
	public void CalculateLinearReqressions() {
		IJ.showMessage("Found "+lineCount+" lines.");
		for (ArrayList<LinePoint> line : ListOfLines) {
			int MAXN = 3000;
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
		    IJ.log("y   = " + beta1 + " * x + " + beta0+"\n"+
		    			"R^2                 = " + R2+"\n"+
		    			"std error of slope = " + Math.sqrt(svar1)+"\n"+
		    			"std error of y-intercept = " + Math.sqrt(svar0));
		    LineInfo tmp = new LineInfo(beta1, beta0, thickness, false);
			
			if (EquationOfLines == null) {
				EquationOfLines = new ArrayList<LineInfo>();
			}
			
			EquationOfLines.add(tmp);
			
		}
	}

	public ArrayList<LineInfo> getEquationOfLines() {
		return EquationOfLines;
	}

	public void setEquationOfLines(ArrayList<LineInfo> equationOfLines) {
		EquationOfLines = equationOfLines;
	}
	

}
