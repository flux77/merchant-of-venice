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

package org.mov.parser;

/**
 * An exception which is thrown when there is a problem executing an
 * expression. Since this exception can be thrown a lot by the GP,
 * and a stack trace is not used, common compile time exceptions
 * have been made static to avoid generating unused stack traces.
 * See Java Performance Tuning for more information.
 */
public class EvaluationException extends ExpressionException {

    private static EvaluationException divideByZeroException = 
        new EvaluationException("Divide by zero error");

    private static EvaluationException futureDateException = 
        new EvaluationException("Future date");

    private static EvaluationException pastDateException = 
        new EvaluationException("Date too far into past");

    private static EvaluationException rangeForSumException = 
        new EvaluationException("Range for sum() needs to be >0");

    private static EvaluationException rangeForAvgException = 
        new EvaluationException("Range for avg() needs to be >0");

    private static EvaluationException rangeForMaxException = 
        new EvaluationException("Range for max() needs to be >0");

    private static EvaluationException rangeForMinException = 
        new EvaluationException("Range for min() needs to be >0");

    private static EvaluationException squareRootNegativeNumberException = 
        new EvaluationException("Square root of a negative number");

    /**
     * Create a new evaluation exception with the given error reason.
     * Make any exception without a run-time error message static so the
     * GP doesn't waste time building stack traces.
     *
     * @param	reason	the reason the execution failed
     */
    public EvaluationException(String reason) {
	super(reason);
    }

    /**
     * Returns an instance of the divide by zero exception. This error
     * represents when you divide a number by zero, which is undefined.
     *
     * @return divide by zero exception
     */
    public static EvaluationException divideByZero() {
        return divideByZeroException;
    }

    /**
     * Returns an instance where the GP has accessed a date in
     * the future.
     *
     * @return future date exception
     */
    public static EvaluationException futureDate() {
        return futureDateException;
    }

    /**
     * Returns an instance where the GP has accessed a date that
     * is too far into the past.
     *
     * @return past date exception
     */
    public static EvaluationException pastDate() {
        return pastDateException;
    }

    /**
     * Returns an instance where the range in the sum() expression
     * is less than or equal to zero.
     *
     * @return range for sum exception
     */
    public static EvaluationException rangeForSum() {
        return rangeForSumException;
    }

    /**
     * Returns an instance where the range in the avg() expression
     * is less than or equal to zero.
     *
     * @return range for avg exception
     */
    public static EvaluationException rangeForAvg() {
        return rangeForAvgException;
    }

    /**
     * Returns an instance where the range in the max() expression
     * is less than or equal to zero.
     *
     * @return range for max exception
     */
    public static EvaluationException rangeForMax() {
        return rangeForMaxException;
    }

    /**
     * Returns an instance where the range in the min() expression
     * is less than or equal to zero.
     *
     * @return range for min exception
     */
    public static EvaluationException rangeForMin() {
        return rangeForMinException;
    }

    /**
     * Returns an instance where the caller tried to take the
     * square root of a negative number.
     *
     * @return square root of negative number exception.
     */
    public static EvaluationException squareRootNegativeNumber() {
        return squareRootNegativeNumberException;
    }

}
