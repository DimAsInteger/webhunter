package edu.umuc.student.jplaschke;

import ij.IJ;
import ij.ImagePlus;
import ij.io.FileSaver;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CreateHtmlReport {

	private File resultsDir = null;
	private File imageDir = null;
	private File tiffDir = null;
	
	public CreateHtmlReport(String imageFName) {
		final String dir = System.getProperty("user.dir");
		resultsDir = new File(dir+File.separator+"results");

		// if the results directory does not exist, create it
		if (!resultsDir.exists()) {
		    try{
		        resultsDir.mkdir();
		    } catch (Exception e) {
		    	e.printStackTrace();
		    }
		}
		//String[] imageName = imageFName.split(".");
		// create image directory 
		IJ.log("resultsDir = "+resultsDir);
	}
	
	public void createWebHunterReport(ImagePlus orig, ImagePlus line, ImagePlus droplet
			                            ) {
		
		IJ.log(orig.toString());
		IJ.log(line.toString());
		IJ.log(droplet.toString());
		
		
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyMMddHHmmss");//dd/MM/yyyy
		Date now = new Date();
		String strDate = sdfDate.format(now);
		    
		FileSaver fileSaverOrig = new FileSaver(orig);
		FileSaver fileSaverLine = new FileSaver(line);
		FileSaver fileSaverDroplet = new FileSaver(droplet);
	
		String[] fName = orig.getOriginalFileInfo().fileName.split(".");
		IJ.log(fName.toString());
		String fileName = orig.getOriginalFileInfo().fileName;
		imageDir = new File(this.resultsDir.getPath()+File.separator+fileName+File.separator);
		if (!imageDir.exists()) {
		    try{
		    	imageDir.mkdir();
		    } catch (Exception e) {
		    	
		    }
		}
		tiffDir = new File(this.imageDir.getPath()+File.separator+"tiffs");
		if (!tiffDir.exists()) {
		    try{
		    	tiffDir.mkdir();
		    } catch (Exception e) {
		    	
		    }
		}
	    IJ.log("tiffDir "+tiffDir);
	
		String origFname = tiffDir.getPath()+File.separator+strDate+fileName;
		IJ.log("save file name = "+origFname);	
		fileSaverOrig.saveAsTiff(origFname);
		String lineFname = tiffDir.getPath()+File.separator+strDate+"line_"+fileName;
		fileSaverLine.saveAsTiff(lineFname);
		String dropFname = tiffDir.getPath()+File.separator+strDate+"drop_"+fileName;
		fileSaverDroplet.saveAsTiff(dropFname);
		
	}
	
}
