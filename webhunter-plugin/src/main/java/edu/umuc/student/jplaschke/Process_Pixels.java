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
import edu.umuc.student.jplaschke.Simple_Threshold;
/**
 * A template for processing each pixel of either
 * GRAY8, GRAY16, GRAY32 or COLOR_RGB images.
 *
 * @author Johannes Schindelin 
 * 
 * Adapted by John Plaschke
 */
public class Process_Pixels implements PlugInFilter {
	protected ImagePlus image;

	// image property members
	private int width;
	private int height;


	// plugin parameters
	public double value;
	public String name;
	
	//private classes
	private Simple_Threshold simpleThreshold;
	private Read_Scale readScale;
	private Detect_Features detectFeatures;
	
	@Override
	public int setup(String arg, ImagePlus imp) {
		IJ.showMessage("setup before showAbout");
		if (arg.equals("about")) {
			showAbout();
			return DONE;
		}

		image = imp;
		simpleThreshold = new Simple_Threshold();
		readScale = new Read_Scale();
		detectFeatures = new Detect_Features();
		
		return DOES_8G | DOES_16 | DOES_32 | DOES_RGB;
	}

	@Override
	public void run(ImageProcessor ip) {
		// get width and height
		width = ip.getWidth();
		height = ip.getHeight();

		// Read scale information
		IJ.showMessage("width = "+width+" height = "+height);
		readScale.setImage(image);
		readScale.process(ip);
		image = readScale.getImage();
		// Basic thresholding 
		height = readScale.getSemHeight();
		//simpleThreshold.setImage(image);
		//simpleThreshold.setHeight(height);
		//simpleThreshold.process(ip);
		
		// Feature detection
		detectFeatures.setImage(image);
		detectFeatures.setHeight(height);
		detectFeatures.process(ip);
		
		// Display results
		//process(ip);
		image.updateAndDraw();
		
	}

	private boolean showDialog() {
		GenericDialog gd = new GenericDialog("Process pixels");

		// default value is 0.00, 2 digits right of the decimal point
		gd.addNumericField("value", 0.00, 2);
		gd.addStringField("name", "John");

		gd.showDialog();
		if (gd.wasCanceled())
			return false;

		// get entered values
		value = gd.getNextNumber();
		name = gd.getNextString();

		return true;
	}

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
		int blackTest = 0;
		int upperX = -1;
		int upperY = -1;
		int lowerX = -1;
		int lowerY = -1;
		int maxLength = -1;
		int firstBlackLine = -1;
		
		IJ.showMessage("height = "+height+" width = "+width);
		int longestRunWhite = 0; // longest run of white
		for (int y=0; y < height; y++) {
			blackTest = 0; // check for black line
			int currentX = -1;
			int curRunWhite = 0; // longest run of white
			for (int x=0; x < width; x++) {
				// process each pixel of the line
				// example: add 'number' to each pixel
				blackTest += (int)pixels[x + y * width];
			}
			if ((blackTest == 0) && (firstBlackLine == -1)) {
				IJ.showMessage("Found black line at y = "+y);
				firstBlackLine = y;
			}
		}
		
		// Look for biggest white rectangle
		for (int y=firstBlackLine; y < height; y++) {
			int currentX = -1;
			int curRunWhite = 0; // longest run of white
			for (int x=0; x < width; x++) {
				// Look for white rectangle
				if (pixels[x + y * width] == (byte)245) {
					if (currentX == -1) {
						currentX = x;
					}
					++curRunWhite;
					IJ.showStatus("curRunWhite = "+curRunWhite);
				} else {
					if (curRunWhite > longestRunWhite) {
						longestRunWhite = curRunWhite;
						IJ.showStatus("longestRunWhite = "+longestRunWhite);
						upperX = currentX;
						upperY = y;
					} else if (curRunWhite == longestRunWhite) {
						if (longestRunWhite > 380) {
							lowerX = x;
							lowerY = y;
						}
					}
					curRunWhite = -1;
					currentX = -1;
				}
			}
		
		}
		
		IJ.showMessage("Final upperX = "+upperX+" upperY = "+upperY+" lowerX = "+lowerX+" lowerY = "+lowerY);
		for (int y=upperY; y < lowerY; y++) {
			for (int x=upperX; x < lowerX; x++) {
				pixels[x + y * width] += (byte)value;		
			}
		}
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
		Class<?> clazz = Process_Pixels.class;
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
