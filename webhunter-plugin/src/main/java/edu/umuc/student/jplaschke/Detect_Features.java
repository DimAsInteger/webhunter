/*
 * To the extent possible under law, the ImageJ developers have waived
 * all copyright and related or neighboring rights to this tutorial code.
 *
 * See the CC0 1.0 Universal license for details:
 *     http://creativecommons.org/publicdomain/zero/1.0/
 */

package edu.umuc.student.jplaschke;

import java.awt.Color;
import java.awt.Font;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.OvalRoi;
import ij.gui.Overlay;
import ij.gui.TextRoi;
import ij.process.ImageProcessor;

/**
 * Detects lines and circles in a micrograph
 *
 * @author John Plaschke
 */
public class Detect_Features {

	private final int LOOK_FOR_TOP_BG = 0;
	private final int LOOK_FOR_TOP_EDGE = 1;
	private final int LOOK_FOR_BOTTOM_EDGE = 2;
	private final int COUNT_SEPARATION = 3;
	private final int BOTTOM_EDGE_FOUND = 4;
	
	// image property members
	private int width;
	private int height;

	// plugin parameters
	public double value;
	public String name;
	
	private Lines lines;
	private Circles circles;
	
	private SemInfo semInfo;
	private CreateHtmlReport createHtmlReport;
	private ImagePlus dropletImage;
	private ImagePlus lineImage;
	protected ImagePlus image;
	private ImagePlus origImage;
	
	// used to store end points to detect lengths
	private ArrayList<ArrayList<LinePoint>> LinesEndPoints; 

	/**
	 * <p>
	 * Please provide this method even if {@link ij.plugin.filter.PlugInFilter} does require it;
	 * the method {@link ij.plugin.filter.PlugInFilter#run(ij.process.ImageProcessor)} can only
	 * handle 2-dimensional data.
	 * </p>
	 * <p>
	 * If your plugin does not change the pixels in-place, make this method return the results and
	 * change the {@link #setup(java.lang.String, ij.ImagePlus)} method to return also the
	 * <i>DOES_NOTHING</i> flag.
	 * </p>
	 *
	 * @param image the image (possible multi-dimensional)
	 */
	public void process(ImagePlus image, SemInfo semInfo, int threshold, int startingX, int lineSep,
			             int xInc, int circleDiameter, double spindleSize, String fname) {
		this.semInfo = semInfo;
		this.image = image;
		// slice numbers start with 1 for historical reasons
		IJ.log("image.getStackSize() "+image.getStackSize()+" circleDiameter="+circleDiameter);
		for (int i = 1; i <= image.getStackSize(); i++)
			process(image.getStack().getProcessor(i), threshold, startingX, lineSep, xInc, 
					 circleDiameter, spindleSize, fname);
	}

	// Select processing method depending on image type
	public void process(ImageProcessor ip, int threshold, int startingX, int lineSep, int xInc,
			               int circleDiameterMicrons,  double spindleSize, String fname) {
		int type = image.getType();
		width = ip.getWidth();
	//	height = ip.getHeight();
		lines = new Lines(10);
		circles = new Circles(40);
		if (type == ImagePlus.GRAY8){
			byte[] pixels = process( (byte[]) ip.getPixels(), threshold, startingX, lineSep, xInc, 
					circleDiameterMicrons, spindleSize, fname );
			ip.setPixels(pixels);
		}
		else {
			throw new RuntimeException("not supported");
		}
	}

	// processing of GRAY8 images
	public byte[] process(byte[] pixels, int threshold, int startingX, int lineSep, int xInc, 
			                 int circleDiameterMicrons,  double spindleSize, String fname) {
		
		int state;  
		int lineNum = 0;
		int circleDiameter = 0;
		
		IJ.log("Start detect features");
		// Use a state machine to detect the top edge and bottom edge
		// a-priori knowledge of line thickness and droplet radius will be used
		// as a simplified template matching
		int x = startingX;
		boolean done = false;
		//for (int x=startingX; x <= width; x+= xInc) {
		while (!done) {
			state = LOOK_FOR_TOP_BG;
			int topY = -1;
			int bottomY = -1;
			int curRunBlack = 0; // longest run of black use to detect circle
			                     // if curRunBlack is 20 or more ignore the slice
			                     // mark as possible circle
			int prevTop = -1;
			
			for (int y=0; y < height; y++) {
				IJ.showProgress(x*y, width*height);
				switch(state) {
		
				    case LOOK_FOR_TOP_BG:
				    	if ((int)(pixels[x + y * width]&0xFF) == (int)80) {
							++curRunBlack;
							
						}
				    	if ((int)(pixels[x + y * width]&0xFF) == (int)10) {
							if (curRunBlack >= lineSep) {
								state = LOOK_FOR_TOP_EDGE;
							} else {
								curRunBlack = 0;
								state = LOOK_FOR_TOP_BG;
							}				
						}
						if (curRunBlack >= lineSep) {
							state = LOOK_FOR_TOP_EDGE;
						} 
				    	
					break;
					
					case LOOK_FOR_TOP_EDGE:
						// top edge starts with 255
						//prevTop = (int)pixels[x + (y-4) * width];
						curRunBlack = 0;
						// Need to determine thickness constant from scale
						// TODO - need to detect brightness cutoff 60 is magic number
						//        use histogram of some sort?
						if (((pixels[x + y * width])&0xFF) == (int)10) {
							topY = y-1; 
						
							if (x<=20) {
								IJ.log("topedgefound top x= "+x+" y= "+y+
										"val1 "+prevTop+" val2 "+(int)pixels[x + y * width]);
							}
							
							state = LOOK_FOR_BOTTOM_EDGE;
							prevTop = -1;
						}
					
					break;
				
					
					case LOOK_FOR_BOTTOM_EDGE:
						// bottom edge is found  change in pixel value is found
						try {
							//prevTop = (int)pixels[x + (y-4) * width];
						//	IJ.log("bottom delta "+delta);
							if (((pixels[x + y * width])&0xFF) == (int)10) {
								++curRunBlack;
							} else if ((int)(pixels[x + y * width]&0xFF) == (int)80) {
								bottomY = y+1;
								state = COUNT_SEPARATION;
								curRunBlack = 0;
							}							
							
						} catch (Exception e) {
							IJ.log("EXCEPTION found at y="+y+" x="+x+" thickness = "+(bottomY-topY));
										
							e.printStackTrace();
						}
					break;
                    // make sure there is a run of grey 
					// This means the edge is likely not in a circle
				    case COUNT_SEPARATION:
				    	if ((int)(pixels[x + y * width]&0xFF) == (int)80) {
							++curRunBlack;						
						}
				    	if ((int)(pixels[x + y * width]&0xFF) == (int)10) {
							if (curRunBlack >= lineSep) {
								state = BOTTOM_EDGE_FOUND;
							} else {
								curRunBlack = 0;
								state = LOOK_FOR_TOP_BG;
							}				
						}
						if (curRunBlack >= lineSep) {
							state = BOTTOM_EDGE_FOUND;
						} 
		
				    break;
						
					case BOTTOM_EDGE_FOUND:
						int thickness = bottomY - topY; // add eight to account for top edge
						int halfThickness = (int)Math.round(((float)thickness)/2.0);
						if (x<500) {
							IJ.log("bottomedge found bottomy= "+bottomY+" topy= "+topY+" thickness= "+thickness
							+"x= "+x+" y= "+y+" thickness= "+thickness+
							"val1 "+prevTop+" val2 "+(int)pixels[x + y * width]);
							IJ.log("thickness "+thickness);
							
						}
						// determine thickness based on scale -TODO
						int maxThickness =  (int)Math.round(semInfo.numPixelsInOneMicron()*spindleSize);
						int minThickness =  (int)Math.round(semInfo.numPixelsInOneMicron()*0.2);
						circleDiameter = (int)Math.round(semInfo.numPixelsInOneMicron()*circleDiameterMicrons);
						//IJ.log("max "+maxThickness+" min "+minThickness);
						if ((thickness >= minThickness) && (thickness <= maxThickness)) {
							//IJ.log("***###$$$ line found at y="+topY+" x="+x+" thickness = "+thickness);
							// TODO change topY to middleY?
							LinePoint lp = new LinePoint(x, topY+halfThickness, thickness, false);
							if (x==startingX) {
								lines.addPointToLine(lineNum, lp);
								lp = new LinePoint(x, topY, thickness, false);
								lines.addPointToClosestLine(lp, xInc);
								lp = new LinePoint(x, bottomY, thickness, false);
								lines.addPointToClosestLine(lp, xInc);
								
								++lineNum;
							} else {
								lines.addPointToClosestLine(lp, xInc);
								lp = new LinePoint(x, topY, thickness, false);
								lines.addPointToClosestLine(lp, xInc);
								lp = new LinePoint(x, bottomY, thickness, false);
								lines.addPointToClosestLine(lp, xInc);
							}
						} else if (thickness >= (int)Math.round((double)maxThickness*2.0)) {
							//IJ.log("*** possible CIRCLE "+" x="+x+" y="+topY+" thickness="+thickness);
							LinePoint cp = new LinePoint(x, topY, thickness, false);
							circles.addPointToCircleSet(cp, circleDiameter);
							cp = new LinePoint(x, bottomY, thickness, false);
							circles.addPointToCircleSet(cp, circleDiameter);
						}
						curRunBlack = 0;
						state = LOOK_FOR_TOP_BG;
						topY = 0;
						bottomY = 0;
				
					break;
				}
				
			}
			// Make sure that last horizontal line of the micrograph is scanned
			if (x == width-1) {
				done = true;
			}
			else if (x > width) {
				x = width-1;
			} else {
				x += xInc;		
			}
		} // while !done
		lines.CalculateLinearReqressions();
		
		// look for circles
		this.circles.findCircles(lines, pixels, width, height, circleDiameter);
		
		pixels = this.drawCircles();
		image.getProcessor().setPixels((Object)pixels);

		// draw the lines in white
		pixels = this.drawLinesInWhite(pixels, startingX);

		this.createHtmlReport = new CreateHtmlReport(origImage.getFileInfo().fileName);
		this.createHtmlReport.createWebHunterReport(origImage, this.lineImage, this.dropletImage,
				  threshold, startingX, lineSep, xInc, circleDiameterMicrons,  spindleSize,
				  circles, lines, semInfo, this.calcLineArea());
		
		return (pixels);
	}

	public void showAbout() {
		IJ.showMessage("Detect Features",
			"detects spindles and droplets"
		);
	}

	public ImagePlus getImage() {
		return image;
	}

	public void setImage(ImagePlus image) {
		this.image = image;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}
	
	public ImagePlus getOrigImage() {
		return origImage;
	}

	public void setOrigImage(ImagePlus origImage) {
		this.origImage = origImage;
	}

	private byte[] drawCircles() {
		Overlay overlay = new Overlay();
		for (CircleInfo ci : circles.getListofCircles()) {
			OvalRoi ovalRoi = new OvalRoi(ci.getX(), ci.getY(), ci.getRadius()*2, ci.getRadius()*2);
			ovalRoi.setStrokeColor(Color.white); 
			ovalRoi.setStrokeWidth(4.0);
			ovalRoi.drawPixels(this.image.getProcessor());
			image.setOverlay(overlay);
			overlay.add(ovalRoi);
			
			Font font = new Font("SansSerif", Font.PLAIN, 86);
			TextRoi roi = new TextRoi(ci.getX()-ci.getRadius()*2, ci.getY()-ci.getRadius()*2, 
					              String.valueOf(ci.getCircleNum()), font); 
		    roi.setStrokeColor(Color.white); 
			roi.setNonScalable(true); 
			
			image.setOverlay(overlay);
			overlay.add(roi);
			
	
		}
		this.dropletImage = image.flatten();
		this.dropletImage.show();
		return (byte[])image.getProcessor().getPixels();
	}
	
	private byte[] drawLinesInWhite(byte[] pixels, int startingX) {
		int LineNum = 1;
	    Overlay overlay = new Overlay();
	    TextRoi roi = null;
	    int separateY = 0;
        // used to calculate spindle area
	    LinesEndPoints = new ArrayList<ArrayList<LinePoint>>(40);
	    LinesEndPoints.ensureCapacity(lines.getEquationOfLines().size());
	
	    for (LineInfo li : lines.getEquationOfLines()) {
		        
			if ((!Double.isNaN(li.slope)) && (!Double.isNaN(li.yIntercept))) {
				IJ.log("m = "+li.slope+" y-intercept = "+li.yIntercept);
				int y = (int) (Math.round((double)startingX*li.slope) + Math.round(li.yIntercept));
		    	y = -y;

			    NumberFormat formatter = new DecimalFormat("#0.000");     
			    String text = "Line "+LineNum+" thickness = "
			        +formatter.format(semInfo.getMicronLength(li.getThickness()))+" "+IJ.micronSymbol+"m";
				Font font = new Font("SansSerif", Font.PLAIN, 86);
				if (LineNum/2 == 0) {
					separateY = 50;
				} else {
					separateY = -50;
				}
				
			    roi = new TextRoi(startingX, y-separateY, text, font); 
			    roi.setStrokeColor(Color.white); 
				roi.setNonScalable(true); 
				
				image.setOverlay(overlay);
				overlay.add(roi);
				boolean firstPointFound = false;
				boolean lastPointFound = false;
				for (int i=0; i<width; i++) {
			    	y = (int) (Math.round((double)i*li.slope) + Math.round(li.yIntercept));
			    	y = -y;
			    
			    	if ((y < height) && (y > 0))  {
			    		if (!firstPointFound) {
			    			firstPointFound = true;
			    			LinePoint lp = new LinePoint(i,y,li.getThickness(),li.isAggregate());
			    			ArrayList<LinePoint> tmp = new ArrayList<LinePoint>(2);
			    			tmp.ensureCapacity(2);
			    			tmp.add(lp);
			    			LinesEndPoints.add(tmp);
			    		}
			    		if (true) { //((int)(pixels[i + y * width]&0xFF) == (int)10) {
					    	try {
					    	   pixels[i + (y-1) * width] = (byte)255;	 	
					    	   pixels[i + y * width] = (byte)255;				     
					    	   pixels[i + (y+1) * width] = (byte)255;	
					    	   pixels[i + (y+2) * width] = (byte)255;			   
					    	} catch (Exception e) {
					    		//IJ.log(e.getMessage());
					    		IJ.log("exception i = "+i+" y="+y);
					    	}
			    		} else {
			    			//IJ.log("LINE ERROR line is not black at x="+i+" y="+y);
			    		}
				    } else {
				    	if (!firstPointFound) {
			    			firstPointFound = true;
			    			LinePoint lp = new LinePoint(i,y,li.getThickness(),li.isAggregate());
			    			ArrayList<LinePoint> tmp = new ArrayList<LinePoint>(2);
			    			tmp.ensureCapacity(2);
			    			tmp.add(lp);
			    			LinesEndPoints.add(tmp);
			    		} else if ((firstPointFound) &&(!lastPointFound)) {
			    			lastPointFound = true;
			    			LinePoint lp = new LinePoint(i,y,li.getThickness(),li.isAggregate());
			    			ArrayList<LinePoint> tmp = LinesEndPoints.get(LineNum-1);
			    			tmp.add(lp);
			    		}
				    }
				}
				if ((firstPointFound) &&(!lastPointFound)) {
	    			lastPointFound = true;
	    			LinePoint lp = new LinePoint(width,y,li.getThickness(),li.isAggregate());
	    			ArrayList<LinePoint> tmp = LinesEndPoints.get(LineNum-1);
	    			tmp.add(lp);
	    		}
				++LineNum;
			}
		}
	    this.lineImage = image.flatten();
		this.lineImage.show();
		image.show();
		 	
		return pixels;
	}
	
    double[] calcLineArea() {
    	double[] areas = new double[50];
    	int i=0;
    	for (ArrayList<LinePoint> lp : this.LinesEndPoints) {
    		LinePoint p1 = lp.get(0);
    		LinePoint p2 = lp.get(1);
    		double spindleLen = Math.sqrt(Math.pow((p1.x-p2.x),2)+Math.pow(p1.y-p2.y,2));   		
    		LineInfo li = lines.getEquationOfLines().get(i);
    	    areas[i] = semInfo.getMicronLength(spindleLen) * semInfo.getMicronLength(li.getThickness());
    		++i;
    	}
    
    	return areas;
    }
    
	
}
