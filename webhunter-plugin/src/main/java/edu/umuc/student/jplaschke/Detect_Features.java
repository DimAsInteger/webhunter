/*
 * To the extent possible under law, the ImageJ developers have waived
 * all copyright and related or neighboring rights to this tutorial code.
 *
 * See the CC0 1.0 Universal license for details:
 *     http://creativecommons.org/publicdomain/zero/1.0/
 */

package edu.umuc.student.jplaschke;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;

/**
 * Detects lines and circles in a micrograph
 *
 * @author John Plaschke
 */
public class Detect_Features {
	
	private final int LOOK_FOR_TOP_EDGE = 0;
	private final int LOOK_FOR_BOTTOM_EDGE = 1;
	private final int BOTTOM_EDGE_FOUND = 2;
	
	protected ImagePlus image;

	// image property members
	private int width;
	private int height;

	// plugin parameters
	public double value;
	public String name;
	
	private Lines lines;
	private Circles circles;

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
	public void process(ImagePlus image) {
		// slice numbers start with 1 for historical reasons
		IJ.log("image.getStackSize() "+image.getStackSize());
		for (int i = 1; i <= image.getStackSize(); i++)
			process(image.getStack().getProcessor(i));
	}

	// Select processing method depending on image type
	public void process(ImageProcessor ip) {
		int type = image.getType();
		width = ip.getWidth();
		height = ip.getHeight();
		lines = new Lines(10);
		circles = new Circles(40);
		if (type == ImagePlus.GRAY8){
			byte[] pixels = process( (byte[]) ip.getPixels() );
			ip.setPixels(pixels);
		}
		else {
			throw new RuntimeException("not supported");
		}
	}

	// processing of GRAY8 images
	public byte[] process(byte[] pixels) {
		
		int state;  
		int lineNum = 0;
		
		IJ.showMessage("Start detect features");
		// Use a state machine to detect the top endge and bottom edge
		// a-priori knowledge of line thickness and droplet radius will be used
		// as a simplified template matching
		// NOTE: magic number 20 should match add point (distance to closest line
		for (int x=0; x < width; x+=20) {
			state = LOOK_FOR_TOP_EDGE;
			int topY = -1;
			int bottomY = -1;
			int curRunWhite = 0; // longest run of white use to detect circle
			                     // if curRunWhite is 5 or more ignore the slice
			                     // mark as possible circle
			int maxRunWhite = -1;
			int prevTop = -1;
			int delta = 0;
			for (int y=4; y < height; y+=4) {
				IJ.showProgress(x*y, width*height);
				switch(state) {
					case LOOK_FOR_TOP_EDGE:
						// top edge starts with 255
						prevTop = (int)pixels[x + (y-4) * width];
						delta = (int)pixels[x + y * width] - prevTop;
						// Need to determine thickness constant from scale
						// TODO - need to detect brightness cutoff 60 is magic number
						//        use histogram of some sort?
						if ((int)pixels[x + y * width] == 255) {
							++curRunWhite;
						}
						if (curRunWhite > 10) {
							continue;
						}
						// TODO: make calculation of 60 
						if ((delta >= 60)) {
							topY = y-4; 
						
							if (x==0) {
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
							prevTop = (int)pixels[x + (y-4) * width];
							delta = prevTop - (int)pixels[x + y * width];
						//	IJ.log("bottom delta "+delta);
				
							if (delta >= (byte)150) {
								state = BOTTOM_EDGE_FOUND;
								bottomY = y;
							}
						} catch (Exception e) {
							IJ.log("EXCEPTION found at y="+y+" x="+x+" thickness = "+(bottomY-topY));
										
							e.printStackTrace();
						}
					break;
					
					case BOTTOM_EDGE_FOUND:
						int thickness = bottomY - topY; // add eight to account for top edge
				//		int halfThickness = 
						if (x==0) {
							IJ.log("bottomedge found bottomy= "+bottomY+" topy= "+topY+" thickness= "+thickness
							+"x= "+x+" y= "+y+" thickness= "+thickness+
							"val1 "+prevTop+" val2 "+(int)pixels[x + y * width]);
							IJ.log("thickness "+thickness);
							
						}
						// determine thickness based on scale -TODO
						if ((thickness >= 6) && (thickness <= 10)) {
							//IJ.log("line found at y="+topY+" x="+x+" thickness = "+thickness);
							// TODO change topY to middleY?
							LinePoint lp = new LinePoint(x, topY, thickness, false);
							if (x==0) {
								lines.addPointToLine(lineNum, lp);
								++lineNum;
							} else {
								lines.addPointToClosestLine(lp);
							}
						}
						curRunWhite = 0;
						state = LOOK_FOR_TOP_EDGE;
						topY = 0;
						bottomY = 0;
					break;
				}
				
			}
			
		}
		lines.CalculateLinearReqressions();
		// draw the lines in black
		pixels = this.drawLinesInBlack(pixels);
		
		// look for cirles
		this.circles.findCircles(lines, pixels, width, height);
		
		return (pixels);
	}

	public void showAbout() {
		IJ.showMessage("ProcessPixels",
			"a template for processing each pixel of an image"
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

	/**
	 * Main method for debugging.
	 *
	 * For debugging, it is convenient to have a method that starts ImageJ, loads
	 * an image and calls the plugin, e.g. after setting breakpoints.
	 *
	 * @param args unused
	 */
	public static void main(String[] args) {
		// set the plugins.dir property to make the plugin appear in the Plugins menu
		Class<?> clazz = Detect_Features.class;
		String url = clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class").toString();
		String pluginsDir = url.substring("file:".length(), url.length() - clazz.getName().length() - ".class".length());
		System.setProperty("plugins.dir", pluginsDir);

		// start ImageJ
		new ImageJ();

		// open the Clown sample
		ImagePlus image = IJ.openImage("http://imagej.net/images/clown.jpg");
		image.show();

		// run the plugin
		IJ.runPlugIn(clazz.getName(), "");
	}
	
	private byte[] drawLinesInBlack(byte[] pixels) {
		for (LineInfo li : lines.getEquationOfLines()) {
			IJ.log("m = "+li.slope+" y-intercept = "+li.yIntercept);
			if ((!Double.isNaN(li.slope)) && (!Double.isNaN(li.yIntercept))) {
			    for (int i=0; i<width; i++) {
			    	int y = (int) (Math.round((double)i*li.slope) + Math.round(li.yIntercept));
			    	y = -y;
			    	
			    	try {
			    	   pixels[i + y * width] = (byte)0;				     
			    	   pixels[i + (y+1) * width] = (byte)0;				     
			    	   pixels[i + (y-1) * width] = (byte)0;				     
				    	   
			    	} catch (Exception e) {
			    		//IJ.log(e.getMessage());
			    		IJ.log(e.getMessage());
			    	}
			    }
			}
		}
		return pixels;
	}
}
