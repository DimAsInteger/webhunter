package edu.umuc.student.jplaschke;

import ij.IJ;
import ij.ImagePlus;
import ij.io.FileSaver;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
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
	
	public void createWebHunterReport(ImagePlus orig, ImagePlus line, ImagePlus droplet,
			int threshold, int startingX, int lineSep, int xInc, 
            int circleDiameterMicrons,  double spindleSize, Circles circles, Lines lines,
            SemInfo semInfo, double[] areas) {
		
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

			fw = new FileWriter(imageDir+File.separator+strDate+fName[0]+".html");
			bw = new BufferedWriter(fw);
			bw.write("<html>");
			bw.write("<head><style>table { border-collapse: collapse; width: 60%;  border: 1px solid black}"+
			  "th, td { text-align: left; padding: 8px; border: 1px solid black}"+ "ol { padding-bottom: 8px; }"
			  + " tr:nth-child(even) {background-color: #f2f2f2}</style><meta charset=\"UTF-8\"></head>");
			bw.write("<body>");
			bw.write("<h1>Web Hunter Report</h1>");

			bw.write("<h2>Parameters</h2>");
            bw.write("<table style='width:100%'><tr><th>Parameter Name</th><th>Value</th></tr>");
            bw.write("<tr><td>threshold pixel value</td><td>"+threshold+"</td></tr>");
            bw.write("<tr><td>startingX</td><td>"+startingX+"</td></tr>");
            bw.write("<tr><td>lineSep</td><td>"+lineSep+"</td></tr>");
            bw.write("<tr><td>xInc</td><td>"+xInc+"</td></tr>");
            bw.write("<tr><td>max droplet diameter</td><td>"+circleDiameterMicrons+"</td></tr>");
            bw.write("<tr><td>max spindle thickness</td><td>"+spindleSize+"</td></tr>");
            bw.write("</table>");
        	NumberFormat formatter = new DecimalFormat("#0.000");  
    		
			bw.write("<h2>Original Image</h2>");
			bw.write("<p>micrograph width: "+formatter.format(semInfo.getMicronLength(line.getWidth()))+IJ.micronSymbol+"m");
	        bw.write("</p><p>micrograph height: "+formatter.format(semInfo.getMicronLength(line.getHeight()))+IJ.micronSymbol+"m");
	        bw.write("</p><p>micrograph area: "+formatter.format(semInfo.getMicronLength(line.getWidth())*semInfo.getMicronLength(line.getHeight()))+IJ.micronSymbol+"m&sup2;");
	            
			bw.write("</p><div style=\"position:relative; height: 100%; width: 100%; top:0;left 0;\">");
			bw.write("<img src=\"file:///"+origFname + ".jpg\" style='height: 100%'></div>");
			
			bw.write("<h2>Line Image</h2>");
			bw.write("<div style=\"position:relative; height: 100%; width: 100%; top:0;left 0;\">");
			bw.write("<img src=\"file:///"+lineFname + ".jpg\" style='height: 100%'></div>");
			
			bw.write("<h2>Line Analysis</h2>");
			bw.write("<p>Number of lines = "+lines.getEquationOfLines().size()+"</p>");
			bw.write("<h3>Equations of lines</h3>");
			bw.write("<ol>");
		    for (LineInfo li : lines.getEquationOfLines()) {	
		    	bw.write("<li>"+"y = "+formatter.format(li.slope)+"x +"+
		    			formatter.format(li.yIntercept)+" thickness = "+
		    			formatter.format(semInfo.getMicronLength(li.getThickness()))+" "+IJ.micronSymbol+"m</li>");
		    }
			bw.write("</ol>");			
			bw.write("<h3>Thickness statistics</h3>");
			double[] stats = lines.calcThicknessStats();
			bw.write("<table style='width:100%'>");
            bw.write("<tr><td>Minimum</td><td>"+formatter.format(semInfo.getMicronLength(stats[0]))+" "+IJ.micronSymbol+"m"+"</td></tr>");
            bw.write("<tr><td>Maximum</td><td>"+formatter.format(semInfo.getMicronLength(stats[1]))+" "+IJ.micronSymbol+"m"+"</td></tr>");
            bw.write("<tr><td>Mean</td><td>"+formatter.format(semInfo.getMicronLength(stats[2]))+" "+IJ.micronSymbol+"m"+"</td></tr>");
            bw.write("<tr><td>Standard Deviation</td><td>"+formatter.format(semInfo.getMicronLength(stats[3]))+" "+IJ.micronSymbol+"m"+"</td></tr>");
            bw.write("</table>");
            bw.write("<h3>Spindle Area Information</h3>");
			bw.write("<table style='width:100%'><tr><th>Line Number</th><th>Spindle Area</th></tr>");
            double total = 0;
			for (int i=0; i<lines.getEquationOfLines().size(); i++) {
			    bw.write("<tr><td>Line "+i+"</td><td>"+formatter.format(areas[i])+" "+IJ.micronSymbol+"m&sup2;"+"</td></tr>");
			    total += areas[i];
			}
			double percentCov = total/(semInfo.getMicronLength(line.getWidth())*semInfo.getMicronLength(line.getHeight()))*100.0;			
		    bw.write("<tr><td>Total </td><td>"+formatter.format(total)+" "+IJ.micronSymbol+"m&sup2;"+"</td></tr>");
		    bw.write("<tr><td>Percent Coverage </td><td>"+formatter.format(percentCov)+"%</td></tr>");
	        bw.write("</table>");            
	        
	        bw.write("<h3>Spindle Separation Information</h3>");
	    	bw.write("<table style='width:100%'><tr><th>Distance between</th><th>Max distance</th><th>Min distance</th></tr>");
	          
	        double[] tmp = lines.calcMinMaxDistance(line.getWidth());
			for (int i=0;i<lines.getEquationOfLines().size()-1; i++) {
				//IJ.log("minmax ="+tmp[i]);
			    bw.write("<tr><td>"+(i+1)+" to "+(i+2)+"</td><td>"+formatter.format(tmp[i])+"</td><td>"+
			    		               formatter.format(tmp[i+1])+"</tr>");
			}
	        bw.write("</table>");            
			
			bw.write("<h2>Droplet Image</h2>");
			bw.write("<div style=\"position:relative; height: 100%; width: 100%; top:0;left 0;\">");
			bw.write("<img src=\"file:///"+dropFname + ".jpg\" style='height: 100%'></div>");
			
			bw.write("<h2>Droplet Analysis</h2>");

			bw.write("<h2>Algorithm Accuracy</h2>");
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
