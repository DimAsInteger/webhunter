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
import ij.gui.GenericDialog;
import ij.io.OpenDialog;
import ij.plugin.PlugIn;

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
	public String filename;
	
	//private classes
	private Simple_Threshold simpleThreshold;
	private Read_Scale readScale;
	private Detect_Features detectFeatures;

	// plugin parameters
	public double threshold;
	public String name;
	public double startingX;
	public double lineSep;
	public double xInc;
	public int circleDiameter;  //pixels
	
	public double spindle;
	
	private boolean showDialog() {
		GenericDialog gd = new GenericDialog("Web Hunter Parameters");

		// default value is 0.00, 2 digits right of the decimal point
		gd.addStringField("name", "Spider1");
		gd.addNumericField("threshold", 135, 3);
		gd.addNumericField("startingX", 130, 0);
		gd.addNumericField("line separation", 5, 0);
		gd.addNumericField("X increment", 10, 0);
		gd.addNumericField("circle diameter", 190, 0);
		gd.addNumericField("spindle thickness", 0.8, 2);

		gd.showDialog();
		if (gd.wasCanceled())
			return false;

		// get entered values
		name = gd.getNextString();
		threshold = gd.getNextNumber();
		startingX = gd.getNextNumber();
		lineSep = gd.getNextNumber();
		xInc = gd.getNextNumber();
		circleDiameter = (int)gd.getNextNumber();
		spindle = gd.getNextNumber();
		
		return true;
	}

	
	//@Override
	public int setup(String arg) {
		int retVal = -1;
		IJ.showStatus("Starting");
		if (arg.equals("about")) {
			showAbout();
		//	return DONE;
		}
		OpenDialog od = new OpenDialog("Select micrograph");

		if (od.getPath() != null) {
			this.image = IJ.openImage(od.getPath());
			image.show();
			    
		    simpleThreshold = new Simple_Threshold();
			readScale = new Read_Scale();
			detectFeatures = new Detect_Features();
			retVal = 0;
		}
		return retVal;
	}

	@Override
	public void run(String arg) {
		
		if (this.showDialog()) {
			if (this.setup("") >= 0) {
			
				SemInfo semInfo = new SemInfo();
				String dir = image.getOriginalFileInfo().directory;
			    String filename = image.getOriginalFileInfo().fileName;
			    String fullFname = dir+File.separator+filename;
		
				// get width and height
				width = image.getWidth();
				height = image.getHeight();
		        int bottomHeight = height;
				// Read scale information
				//IJ.showMessage("width = "+width+" height = "+height);
				readScale.setImage(image);
				IJ.showStatus("Read Scale");
				readScale.process(image);
				// set the scale length
				semInfo.setBarLength(readScale.getScaleWidth());
				
				image = readScale.getImage();
				// Basic thresholding 
				height = readScale.getSemHeight();
			    
			    IJ.log("fullname = "+fullFname);
			    semInfo.readSemInfo(fullFname, width, height, bottomHeight);
				IJ.showStatus("Threshold image");
			    simpleThreshold.setImage(image);  
				simpleThreshold.setHeight(height);
				image = simpleThreshold.process(image, (int)Math.round(threshold));
		
				IJ.showStatus("Detecting features");
				// Feature detection
				detectFeatures.setImage(image);
				detectFeatures.setHeight(height);
				detectFeatures.process(image, semInfo, (int)startingX,
						(int)lineSep, (int)xInc,
						circleDiameter, spindle, filename);
				
				// Display results
				//process(ip);
				image.updateAndDraw();
			}
		}		
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

		// run the plugin
		IJ.runPlugIn(clazz.getName(), "");
	}

}
