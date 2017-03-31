package edu.umuc.student.jplaschke;

import JSci.maths.ArrayMath;
import org.apache.commons.collections.primitives.ArrayDoubleList;

/**
 *
 * @author juha
 * from https://github.com/molgenis/systemsgenetics/blob/master/genetica-libraries/src/main/java/umcg/genetica/math/stats/TheilSen.java
 */
public class TheilSen {

    public TheilSen() {
    }
    
    
   
    /**
     * 
     * Calculates Theil-Sen estimation for given arrays.
     * 
     * @param v1
     * @param v2
     * @return [y intercept, slope, 2.5 percentile, 97.5 percentile, number of pairs]
     */
    public static double[] getDescriptives(double[] v1, double[] v2) {

        if (v1.length != v2.length) {
            throw new IllegalArgumentException("Arrays must be of the same length! " + v1.length + ", " + v2.length);
        }

        ArrayDoubleList slopesList = new ArrayDoubleList();
        int cnt = 0;
        for (int i = 0; i < v1.length; i++) {
            double x = v1[i];
            double y = v2[i];
            for (int j = i + 1; j < v1.length; j++) {
                if (x != v1[j]) { // x must be different, otherwise slope becomes infinite
                    double slope = (v2[j] - y) / (v1[j] - x);
                    slopesList.add(slope);
                    ++cnt;
                }
            }
        }

        double[] slopes = slopesList.toArray();
        double median1 = ArrayMath.median(v1);
        double median2 = ArrayMath.median(v2);
        double slope = ArrayMath.median(slopes);
        double yI = median2 - slope * median1;
        double p1 = ArrayMath.percentile(slopes, 0.025d);
        double p2 = ArrayMath.percentile(slopes, 0.975d);

        return new double[]{yI, slope, p1, p2, cnt};

    }
}
