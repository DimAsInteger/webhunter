package edu.umuc.student.jplaschke;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class StatsFunctions {
	
	public static double[] calcStatistics(double[] values) {
		double[] stats = {0,0,0,0};
		DescriptiveStatistics statslib = new DescriptiveStatistics();

		// Add the data from the array
		for( int i = 0; i < values.length; i++) {
			 if (values[i] > 0) {
		        statslib.addValue(values[i]);
			 }
		}

		// Compute some statistics
		stats[0] = statslib.getMin();
		stats[1] = statslib.getMax();
		stats[2] = statslib.getMean();
		stats[3] = statslib.getStandardDeviation();

		return stats; // min, max, mean, standard deviation
	}
}
