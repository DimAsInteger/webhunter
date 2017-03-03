package edu.umuc.student.jplaschke;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ij.IJ;

import com.asprise.ocr.Ocr;

// Contains SEM magnification and scale information
public class SemInfo {

	// 
	private int scale;
	private int magnification;
	private int barLength;
	private Ocr ocr = null;
	
	public SemInfo() {
		Ocr.setUp(); // one time setup
		ocr = new Ocr();
		ocr.startEngine("eng", Ocr.SPEED_SLOW);
		this.magnification = -1;
		this.scale = -1;
	}

	public void readSemInfo(String fullFname, int width, int height, int bottomHeight) {

	    IJ.log("fullname = "+fullFname);
		String s = ocr.recognize(fullFname, -1, 0, height, width, bottomHeight, 
				       Ocr.RECOGNIZE_TYPE_TEXT, Ocr.OUTPUT_FORMAT_PLAINTEXT,
				       "PROP_IMG_PREPROCESS_TYPE=custom|PROP_IMG_PREPROCESS_CUSTOM_CMDS=invert()");
		s = s.replace(",", "");
		s = s.replace("o", "0");
		IJ.log("ocr = "+s);
        Pattern scalePatt = Pattern.compile(".*x(\\d+)\\s+(\\d+)pm(.*)");
        Matcher m = scalePatt.matcher(s);
        if (m.find( )) {
           IJ.log("Found mag: " + m.group(1) );
           IJ.log("Found scale: " + m.group(2) );
           this.magnification = Integer.valueOf(m.group(1));
           this.scale = Integer.valueOf(m.group(2));
        } else {
           IJ.log("NOT FOUND");
        }

	}
	
	public double getNanoMeterLength(int lenPixels) {
	    double len = 0;
	    
	    return len;
	}
	
	public int getScale() {
		return scale;
	}

	public void setScale(int scale) {
		this.scale = scale;
	}

	public int getMagnification() {
		return magnification;
	}

	public void setMagnification(int magnification) {
		this.magnification = magnification;
	}

	public int getBarLength() {
		return barLength;
	}

	public void setBarLength(int barLength) {
		this.barLength = barLength;
	}

}