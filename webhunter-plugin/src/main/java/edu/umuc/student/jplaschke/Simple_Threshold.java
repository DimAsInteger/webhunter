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
import ij.process.ImageProcessor;

/**
 * Converts the image to black (pixel = 0) and white (pixel value = 255) based
 * on a constant
 * TODO: Change this to use a histogram to determine the background threshold
 *
 * @author John Plaschke
 * 
 */
public class Simple_Threshold  {
	protected ImagePlus image;

	// image property members
	private int width;
	private int height;

	// plugin parameters
	public double threshold;
	public String name;
	public double startingX;
	public double lineSep;
	public double xInc;
	
	private boolean showDialog() {
		GenericDialog gd = new GenericDialog("Web Hunter Parameters");

		// default value is 0.00, 2 digits right of the decimal point
		gd.addStringField("name", "Spider1");
		gd.addNumericField("threshold", 135, 3);
		gd.addNumericField("startingX", 130, 0);
		gd.addNumericField("line separation", 5, 0);
		gd.addNumericField("X increment", 10, 0);

		gd.showDialog();
		if (gd.wasCanceled())
			return false;

		// get entered values
		name = gd.getNextString();
		threshold = gd.getNextNumber();
		startingX = gd.getNextNumber();
		xInc = gd.getNextNumber();
				
		return true;
	}

	public ImagePlus getImage() {
		return image;
	}

	public void setImage(ImagePlus image) {
		this.image = image;
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
	public ImagePlus process(ImagePlus image) {
		if (showDialog()) {
		// slice numbers start with 1 for historical reasons
			for (int i = 1; i <= image.getStackSize(); i++) {
				ImageProcessor ip = process(image.getStack().getProcessor(i));
				image.getStack().setProcessor(ip, i);
			}
		}
		return image;
	}

	// Select processing method depending on image type
	public ImageProcessor process(ImageProcessor ip) {
		width = ip.getWidth();
		
		int type = image.getType();
		if (type == ImagePlus.GRAY8) {
			byte[] pixels =  process( (byte[]) ip.getPixels() );
			ip.setPixels(pixels);
		}
		else {
			throw new RuntimeException("not supported");
		}
		return ip;
	}

	// processing of GRAY8 images
	public byte[] process(byte[] pixels) {
		
		IJ.log("height = "+height+" width = "+width);
		for (int x=0; x < width; x++) {
			
			for (int y=0;y < height; y++) {
				// process each pixel of the line
				// Set pixel to 10 if the value is greater than threshold
				if ((int)(pixels[x + y * width]&0xFF) > threshold) {
					pixels[x + y * width] = (byte)10;
			    } else {
				   pixels[x + y * width] = (byte)80;
				}
			}
		}
		return pixels;
	}

	public void showAbout() {
		IJ.showMessage("ProcessPixels",
			"a template for processing each pixel of an image"
		);
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
		Class<?> clazz = Simple_Threshold.class;
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
