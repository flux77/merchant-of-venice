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

package org.mov.util;

import junit.framework.TestCase;

import org.mov.util.TradingTime;
import org.mov.util.TradingTimeFormatException;

public class TradingTimeTest extends TestCase {
    public void testParser() {
        TradingTime time = null;

        try {
            time = new TradingTime("4:12PM");
            assertTrue(time.getHour() == 16 &&
                       time.getMinute() == 12 &&
                       time.getSecond() == 0);

            time = new TradingTime("11:59AM");
            assertTrue(time.getHour() == 11 &&
                       time.getMinute() == 59 &&
                       time.getSecond() == 0);

            time = new TradingTime("12:00PM");
            assertTrue(time.getHour() == 12 &&
                       time.getMinute() == 0 &&
                       time.getSecond() == 0);

            time = new TradingTime("11:59pm");
            assertTrue(time.getHour() == 23 &&
                       time.getMinute() == 59 &&
                       time.getSecond() == 0);

            time = new TradingTime("12:00AM");
            assertTrue(time.getHour() == 0 &&
                       time.getMinute() == 0 &&
                       time.getSecond() == 0);

            time = new TradingTime("1:00am");
            assertTrue(time.getHour() == 1 &&
                       time.getMinute() == 0 &&
                       time.getSecond() == 0);


        }
        catch(TradingTimeFormatException e) {
            fail(e.toString());
        }
    }
}