/* Merchant of Venice - technical analysis software for the stock market.
   Copyright (C) 2002 Andrew Leppard (aleppard@picknowl.com.au)

   This program is free software; you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation; either version 2 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software
   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package org.mov.quote;

/**
 * Interim class containing statistical functions to be performed on
 * stock quotes. This class will eventually be
 * broken up so that each stat gets put into its own class, e.g.
 * average.java, RSI.java etc. They will then be put into the stats package.
 * The gondola language will be made so that it can turn any stat
 * class into a function.
 */
public class QuoteFunctions {

    /** This is the default/recommended period for the RSI. */
    final public static int DEFAULT_RSI_PERIOD = 45;

    /**
     * Find the standard deviation of the given values. This
     * algorthim will calculate the standard deviation on the values in
     * the given array in the range [start, end]. Start inclusive, end exclusive.
     *
     * @param values array of values to analyse
     * @param start  analyse values from start
     * @param end    to end
     * @return the standard deviation
     */
    static public double sd(double[] values, int start, int end) {
	double average = avg(values, start, end);
	int period = end - start;

	double deviationSum = 0.0D;
	for(int i = start; i < end; i++) {
	    deviationSum += (values[i] - average)*(values[i] - average);
	}

        if(period > 2)
            deviationSum /= (period - 1);

	return Math.sqrt(deviationSum);
    }

    /**
     * Find the average of the given values. This
     * algorthim will calculate the average on the values in
     * the given array in the range [start, end]. Start inclusive, end exclusive.
     *
     * @param values array of values to analyse
     * @param start  analyse values from start
     * @param end    to end
     * @return the average
     */
    static public double avg(double[] values, int start, int end) {
	double avg = 0.0D;
	int period = end - start;

	// Sum quotes
	for(int i = start; i < end; i++) {
	    avg += values[i];
	}

	// Average
        if(period > 1)
            avg /= period;

	return avg;
    }

    /**
     * Calculate the Pearson product-moment correlation between the two
     * variables. This will return a correlation co-efficient which is in the range of
     * -1 (negative correlation) through to (no correlation) through to 1 (perfect
     * correlation.
     *
     * The correlation co-efficient is calculated as follows:
     *
     * r = sum(Zx * Zy)
     *     ------------
     *         N - 1
     *
     * Where Zx = X - E(X)
     *            --------
     *              Sx
     *
     * Where E(X) is the mean of X and Sx is the standard deviation of X.
     *
     * Simillarly for Zy.
     *
     * @param x     array of values to test against
     * @param y     array of values to detect correlation against x
     * @param start analyse values from start
     * @param end   to end
     * @return the correlation co-efficient
     */
    static public double corr(double[] x, double y[], int start, int end) {
        double r = 0.0D;
        double ex = avg(x, start, end);
        double sx = sd(x, start, end);
        double ey = avg(y, start, end);
        double sy = sd(y, start, end);

        if(sx != 0.0D && sy != 0.0D) {
            for(int i = start; i < end; i++) {
                double zx = (x[i] - ex) / sx;
                double zy = (y[i] - ey) / sy;
                
                r += zx * zy;
            }

            if((end - start) > 1)
                r /= (end - start - 1);
        }

        return r;
    }

    /**
     * Calculate the Relative Strength Indicator (RSI) value. Technical Analysis
     * by Martin J. Pring describes the RSI as:
     *
     * "It is a momentum indicator, or oscillator, that measures the relative internal
     *  strength of a security against <i>itself</i>....".
     *
     * The formula for the RSI is as follows:
     *
     *               100
     * RSI = 100 - ------
     *             1 + RS
     *
     *       average of x days' up closes
     * RS = ------------------------------
     *      average of x days' down closes
     *
     * To calculate an X day RSI you need X + 1 quote values. So try and make
     * sure that <code>start</code> is always greater than zero.
     *
     * @param values array of values to analyse
     * @param start  analyse values from start
     * @param end    to end
     * @return RSI
     */
    static public double rsi(double[] values, int start, int end) {
        double sumGain = 0.0D;
        double sumLoss = 0.0D;
        int numberGains = 0;
        int numberLosses = 0;
        double previous;

        if(start > 0)
            previous = values[start - 1];
        else
            previous = values[start];

        // Calculate average day up and down closes
        for(int i = start; i < end; i++) {
            if(values[i] > previous) {
                sumGain += (values[i] - previous);
                numberGains++;
            }

            else if(values[i] < previous) {
                sumLoss += (previous - values[i]);
                numberLosses++;
            }

            previous = values[i];
        }

        // If the period is too small, return a neutral result
        if(numberLosses == 0 && numberGains == 0)
            return 50.0D;

        // If avg loss is 0, then RSI returns 100 by definition.
        else if(numberLosses == 0 || (sumLoss / numberLosses == 0.0D))
            return 100.0D;

        else {
            double avgGain;
            double avgLoss = sumLoss / numberLosses;

            if (numberGains == 0)
                avgGain = 0;
            else
                avgGain = sumGain / numberGains;

            double RS = avgGain / avgLoss;
            return 100.0D - 100.D / (1.0D + RS);
        }
    }

    static public double ema(double values[], int start, int end, double smoothingConstant) {
	double avg = 0.0D;
	double prev = 0.0;
	int period = end - start;

	// Formula: AVGcurrent = AVGprev + k(DATAcurrent - AVGprev) where:
	// AVGcurrent is the avg over values, DATAcurrent is values[i], and k is the smoothing constant.
	for(int i = start; i < end; i++) {	    
	    if (i > start) {
		avg = prev + (smoothingConstant) * (values[i] - prev);
		prev = avg;
	    } else {
		prev = values[i];
		avg = prev;
	    }
	}	
	return avg;
    }

    public static final double roundDouble(double d, int places) {
        return Math.round(d * Math.pow(10, (double) places)) / Math.pow(10,
            (double) places);
    }
    

}

