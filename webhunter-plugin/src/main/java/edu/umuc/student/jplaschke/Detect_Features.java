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
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

/**
 * A template for processing each pixel of either
 * GRAY8, GRAY16, GRAY32 or COLOR_RGB images.
 *
 * @author Johannes Schindelin 
 * modified by John Plaschke
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
	

	/**
	 * Process an image.
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
		IJ.showMessage("image.getStackSize() "+image.getStackSize());
		for (int i = 1; i <= image.getStackSize(); i++)
			process(image.getStack().getProcessor(i));
	}

	// Select processing method depending on image type
	public void process(ImageProcessor ip) {
		int type = image.getType();
		width = ip.getWidth();
		height = ip.getHeight();
		lines = new Lines(10);
		if (type == ImagePlus.GRAY8)
			process( (byte[]) ip.getPixels() );
		else if (type == ImagePlus.GRAY16)
			process( (short[]) ip.getPixels() );
		else if (type == ImagePlus.GRAY32)
			process( (float[]) ip.getPixels() );
		else if (type == ImagePlus.COLOR_RGB)
			process( (int[]) ip.getPixels() );
		else {
			throw new RuntimeException("not supported");
		}
	}

	// processing of GRAY8 images
	public void process(byte[] pixels) {
		
		int state;  
		int lineNum = 0;
		
		IJ.showMessage("start detect features");
		// Use a state machine to detect the top endge and bottom edge
		// a-priori knowledge of line thickness and droplet radius will be used
		// as a simplified template matching
		for (int x=0; x < width; x+=20) {
			state = LOOK_FOR_TOP_EDGE;
			int topY = -1;
			int bottomY = -1;
			int curRunWhite = 0; // longest run of white
			int prevTop = -1;
			int delta = 0;
			for (int y=2; y < height; y++) {
				IJ.showProgress(x*y, width*height);
				switch(state) {
					case LOOK_FOR_TOP_EDGE:
						// top edge starts with 255
						prevTop = (int)pixels[x + (y-2) * width];
						delta = (int)pixels[x + y * width] - prevTop;
						// Need to determine 60 constant from scale
						// TODO - need to detect brightness cutoff 100 is magic number
						//        use histogram of some sort?
						if ((delta >= 50) && ((int)pixels[x + y * width]>100)) {
							topY = y; 
							curRunWhite = 0;

							if (x==0) {
								IJ.showMessage("topedgefound top x= "+x+" y= "+y+
										"val1 "+prevTop+" val2 "+(int)pixels[x + y * width]);
							}
							
							state = LOOK_FOR_BOTTOM_EDGE;
							prevTop = -1;
							
						}
					
					break;
				
					case LOOK_FOR_BOTTOM_EDGE:
						// bottom edge is found when 5 white pixels are found
						try {
							prevTop = (int)pixels[x + (y-2) * width];
							delta = prevTop - (int)pixels[x + y * width];
							if (delta >= (byte)50) {
								state = BOTTOM_EDGE_FOUND;
								bottomY = y;
							}
						} catch (Exception e) {
							IJ.showMessage("EXCEPTION found at y="+y+" x="+x+" thickness = "+(bottomY-topY));
										
							e.printStackTrace();
						}
					break;
					
					case BOTTOM_EDGE_FOUND:
						int thickness = bottomY - topY + 2; // add six to account for top edge
						
						if (x==0) {
							IJ.showMessage("bottomedge found bottomy= "+bottomY+" topy= "+topY+" thickness= "+thickness
							+"x= "+x+" y= "+y+" thickness= "+thickness+
							"val1 "+prevTop+" val2 "+(int)pixels[x + y * width]);
						}
						// determine thickness based on scale -TODO
						if (thickness >= 6) {
							//IJ.showMessage("line found at y="+topY+" x="+x+" thickness = "+thickness);
							LinePoint lp = new LinePoint(x, topY, thickness, false);
							if (x==0) {
								lines.addPointToLine(lineNum, lp);
								++lineNum;
							} else {
								lines.addPointToClosestLine(lp);
							}
						}
						if (thickness > 14) {
							//IJ.showMessage("Possible circle");
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
	}

	// processing of GRAY16 images
	public void process(short[] pixels) {
		for (int y=0; y < height; y++) {
			for (int x=0; x < width; x++) {
				// process each pixel of the line
				// example: add 'number' to each pixel
				pixels[x + y * width] += (short)value;
			}
		}
	}

	// processing of GRAY32 images
	public void process(float[] pixels) {
		for (int y=0; y < height; y++) {
			for (int x=0; x < width; x++) {
				// process each pixel of the line
				// example: add 'number' to each pixel
				pixels[x + y * width] += (float)value;
			}
		}
	}

	// processing of COLOR_RGB images
	public void process(int[] pixels) {
		for (int y=0; y < height; y++) {
			for (int x=0; x < width; x++) {
				// process each pixel of the line
				// example: add 'number' to each pixel
				pixels[x + y * width] += (int)value;
			}
		}
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
}
