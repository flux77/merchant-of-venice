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

import org.mov.util.Locale;

/**
 * An exception which is thrown when there is a problem executing an
 * expression. Since this exception can be thrown a lot by the GP,
 * and a stack trace is not used, common compile time exceptions
 * have been made static to avoid generating unused stack traces.
 * See Java Performance Tuning for more information.
 *
 * @author Andrew Leppard
 */
public class EvaluationException extends ExpressionException {

    /** An exception which is thrown when per performa a divide by zero operation. */
    public static EvaluationException DIVIDE_BY_ZERO_EXCEPTION =
        new EvaluationException(Locale.getString("DIVIDE_BY_ZERO_ERROR"));

    // The next two errors should never appear to the user so they aren't
    // localised.

    /** An exception which is thrown when the GP tries to access a date that is
        too far into the future. */
    public static EvaluationException FUTURE_DATE_EXCEPTION =
        new EvaluationException("Future date");

    /** An exception which is thrown when the GP tries to access a date that is
        too distant in the past. */
    public static EvaluationException PAST_DATE_EXCEPTION =
        new EvaluationException("Date too far into past");

    /** An exception which is thrown on an invalid sum() range. */
    public static EvaluationException SUM_RANGE_EXCEPTION =
        new EvaluationException(Locale.getString("SUM_RANGE_ERROR"));

    /** An exception which is thrown on an invalid avg() range. */
    public static EvaluationException AVG_RANGE_EXCEPTION =
        new EvaluationException(Locale.getString("AVG_RANGE_ERROR"));

    /** An exception which is thrown on an invalid max() range. */
    public static EvaluationException MAX_RANGE_EXCEPTION =
        new EvaluationException(Locale.getString("MAX_RANGE_ERROR"));

    /** An exception which is thrown on an invalid min() range. */
    public static EvaluationException MIN_RANGE_EXCEPTION =
        new EvaluationException(Locale.getString("MIN_RANGE_ERROR"));

    /** An exception which is thrown on an invalid rsi() range. */
    public static EvaluationException RSI_RANGE_EXCEPTION =
        new EvaluationException(Locale.getString("RSI_RANGE_ERROR"));

    /** An exception which is thrown on an invalid lag() range. */
    public static EvaluationException LAG_RANGE_EXCEPTION =
        new EvaluationException(Locale.getString("LAG_RANGE_ERROR"));

    /** An exception which is thrown when trying to calculate the square
        root of a negative number. */
    public static EvaluationException SQUARE_ROOT_NEGATIVE_EXCEPTION =
        new EvaluationException(Locale.getString("SQUARE_ROOT_NEGATIVE_ERROR"));

    /** An exception which is thrown on an invalid sum() offset. */
    public static EvaluationException SUM_OFFSET_EXCEPTION =
        new EvaluationException(Locale.getString("SUM_OFFSET_ERROR"));

    /** An exception which is thrown on an invalid avg() offset. */
    public static EvaluationException AVG_OFFSET_EXCEPTION =
        new EvaluationException(Locale.getString("AVG_OFFSET_ERROR"));

    /** An exception which is thrown on an invalid lag() offset. */
    public static EvaluationException LAG_OFFSET_EXCEPTION =
        new EvaluationException(Locale.getString("LAG_OFFSET_ERROR"));

    /** An exception which is thrown on an invalid max() offset. */
    public static EvaluationException MAX_OFFSET_EXCEPTION =
        new EvaluationException(Locale.getString("MAX_OFFSET_ERROR"));

    /** An exception which is thrown on an invalid min() offset. */
    public static EvaluationException MIN_OFFSET_EXCEPTION =
        new EvaluationException(Locale.getString("MIN_OFFSET_ERROR"));

    /** An exception which is thrown on an invalid rsi() offset. */
    public static EvaluationException RSI_OFFSET_EXCEPTION =
        new EvaluationException(Locale.getString("RSI_OFFSET_ERROR"));

    /** An exception which is thrown on an invalid corr() offset. */
    public static EvaluationException CORR_OFFSET_EXCEPTION =
        new EvaluationException(Locale.getString("CORR_OFFSET_ERROR"));

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
}
