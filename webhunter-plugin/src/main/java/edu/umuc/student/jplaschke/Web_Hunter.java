/*

 * To the extent possible under law, the ImageJ developers have waived
 * all copyright and related or neighboring rights to this tutorial code.
 *
 * See the CC0 1.0 Universal license for details:
 *     http://creativecommons.org/publicdomain/zero/1.0/
 */

package edu.umuc.student.jplaschke;

import java.io.File;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.io.OpenDialog;
import ij.plugin.PlugIn;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import edu.umuc.student.jplaschke.Simple_Threshold;
/**
 * Web_Hunter is a Imagej plugin that detects line and circle orientation
 * The algorithm is 
 * 1) Detect the bottom of the micrograph and the scale 
 * 2) Detect edges along the x=0 column
 *    Note: May need histogram to determine constants for gradient 
 *          Edge is detected by a change in the pixel value over 3 pixels
 *          The delta for edge determination needs to be calculated from the 
 *          histogram 
 *     a) Add each point to the Lines data structure
 * 3) Iterate down columns every x pixels (optimal x to be determined)
 *    When ever an edge is detected add the Point to the closest line
 * 4) Use linear regression on the points (edges) to detect all the lines
 *
 * @author  John Plaschke
 * 
 * Adapted from Johannes Schindelin's example-legacy-plugin (imagej)
 */
public class Web_Hunter implements PlugIn {
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
	
	//@Override
	public int setup(String arg) {
		IJ.showStatus("Starting");
		if (arg.equals("about")) {
			showAbout();
		//	return DONE;
		}
		OpenDialog od = new OpenDialog("Select micrograph");

		this.image = IJ.openImage(od.getPath());
		image.show();
		    
	    simpleThreshold = new Simple_Threshold();
		readScale = new Read_Scale();
		detectFeatures = new Detect_Features();
		return 0;
	}

	@Override
	public void run(String arg) {
		
		this.setup("");
		
		SemInfo semInfo = new SemInfo();
		String dir = image.getOriginalFileInfo().directory;
	    String name = image.getOriginalFileInfo().fileName;
	    String fullFname = dir+File.separator+name;

		// get width and height
		width = image.getWidth();
		height = image.getHeight();
        int bottomHeight = height;
		// Read scale information
		//IJ.showMessage("width = "+width+" height = "+height);
		readScale.setImage(image);
		readScale.process(image);

		image = readScale.getImage();
		// Basic thresholding 
		height = readScale.getSemHeight();
	    
	    IJ.log("fullname = "+fullFname);
	    semInfo.readSemInfo(fullFname, width, height, bottomHeight);
		simpleThreshold.setImage(image);  
		simpleThreshold.setHeight(height);
		image = simpleThreshold.process(image);
	  
		// Feature detection
		detectFeatures.setImage(image);
		detectFeatures.setHeight(height);
		detectFeatures.process(image);
		
		// Display results
		//process(ip);
		image.updateAndDraw();
		
	}

	// Delaram:  Create a dialog to have the user enter a typical line 
	//      thickness and droplet diameter
	//      Add the tarantula name or ID of the spider
	//  If you cannot get the scale write a dialog to input magnification 
	//  and scale value, e.g. 10um
	private boolean showDialog() {
		GenericDialog gd = new GenericDialog("Process pixels");

		// default value is 0.00, 2 digits right of the decimal point
		gd.addNumericField("value", 0.00, 2);
		gd.addStringField("name", "test");

		gd.showDialog();
		if (gd.wasCanceled())
			return false;

		// get entered values
		value = gd.getNextNumber();
		name = gd.getNextString();

		return true;
	}

	public void showAbout() {
		IJ.showMessage("WebHunter",
			"a plugin for finding orientation of lines and circles of an image"
		);
	}

	/**
	 * Main method for DEBUGGING ONLY. ****
	 *
	 * For debugging, it is convenient to have a method that starts ImageJ, loads
	 * an image and calls the plugin, e.g. after setting breakpoints.
	 *
	 * @param args unused
	 */
	public static void main(String[] args) {
		// set the plugins.dir property to make the plugin appear in the Plugins menu
		Class<?> clazz = Web_Hunter.class;
		String url = clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class").toString();
		String pluginsDir = url.substring("file:".length(), url.length() - clazz.getName().length() - ".class".length());
		System.setProperty("plugins.dir", pluginsDir);

		// start ImageJ
		new ImageJ();

        OpenDialog od = new OpenDialog("Select micrograph");
		
		// open the chosen file
		ImagePlus image = IJ.openImage(od.getPath());
		image.show();

		// run the plugin
		IJ.runPlugIn(clazz.getName(), "");
	}

}
