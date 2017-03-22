package edu.umuc.student.jplaschke;

import ij.IJ;
import ij.ImagePlus;
import ij.io.FileSaver;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
		BufferedWriter bw = null;
		FileWriter fw = null;
		
		FileSaver fileSaverOrig = new FileSaver(orig);
		FileSaver fileSaverLine = new FileSaver(line);
		FileSaver fileSaverDroplet = new FileSaver(droplet);
	
		String[] fName = orig.getOriginalFileInfo().fileName.split("\\.");
		IJ.log(fName[0]);
		String fileName = fName[0];
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
		fileSaverOrig.saveAsJpeg(origFname+".jpg");
		String lineFname = tiffDir.getPath()+File.separator+strDate+"line_"+fileName;
		fileSaverLine.saveAsJpeg(lineFname+".jpg");
		String dropFname = tiffDir.getPath()+File.separator+strDate+"drop_"+fileName;
		fileSaverDroplet.saveAsJpeg(dropFname+".jpg");
		
		// Create HTML report
		try {

			String content = "This is the content to write into file\n";

			fw = new FileWriter(imageDir+File.separator+fName[0]+".html");
			bw = new BufferedWriter(fw);
			bw.write("<html>");

			bw.write("<body>");
			bw.write("<h1>Web Hunter Report</h1>");

			bw.write("<h2>Parameters</h2>");

			bw.write("<h2>Original Image</h2>");
			bw.write("<div style=\"position:relative; height: 100%; width: 100%; top:0;left 0;\">");
			bw.write("<img src=\"file:///"+origFname + ".jpg\" style='height: 100%'></div>");
			
			bw.write("<h2>Line Image</h2>");
			bw.write("<div style=\"position:relative; height: 100%; width: 100%; top:0;left 0;\">");
			bw.write("<img src=\"file:///"+lineFname + ".jpg\" style='height: 100%'></div>");
			
			bw.write("<h2>Line Analysis</h2>");
			
			bw.write("<h2>Droplet Image</h2>");
			bw.write("<div style=\"position:relative; height: 100%; width: 100%; top:0;left 0;\">");
			bw.write("<img src=\"file:///"+dropFname + ".jpg\" style='height: 100%'></div>");
			
			bw.write("<h2>Droplet Analysis</h2>");

			bw.write("</body>");

			bw.write("</html>");

		} catch (IOException e) {

			e.printStackTrace();

		} finally {

			try {

				if (bw != null)
					bw.close();

				if (fw != null)
					fw.close();

			} catch (IOException ex) {

				ex.printStackTrace();

			}

		}
	}
	
}
