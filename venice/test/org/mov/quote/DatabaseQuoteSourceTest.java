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

import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mov.quote.DatabaseQuoteSource;
import org.mov.quote.EODQuote;
import org.mov.quote.Symbol;
import org.mov.quote.SymbolFormatException;
import org.mov.util.TradingDate;

public class DatabaseQuoteSourceTest extends TestCase
{
    public void testAll() {
        //
        // Setup environment
        //

        // Delete old database
        try {
            File oldDatabase = new File(".database.log");
            oldDatabase.delete();
            oldDatabase = new File(".database.properties");
            oldDatabase.delete();
            oldDatabase = new File(".database.script");
            oldDatabase.delete();
        }
        catch(SecurityException e) {
            // Don't care
        }

        String databaseFile = null;
        File tempFile;

        try {
            tempFile = new File(".database");
            databaseFile = tempFile.getCanonicalPath();
        }
        catch(IOException e) {
            fail(e.getMessage());
        }

        Symbol CBA = null;
        Symbol WBC = null;
        Symbol ANZ = null;

        try {
            CBA = Symbol.find("CBA");
            WBC = Symbol.find("WBC");
            ANZ = Symbol.find("ANZ");
        }
        catch(SymbolFormatException e) {
            fail(e.getMessage());
        }

        DatabaseQuoteSource database = new DatabaseQuoteSource(databaseFile);

        //
        // Test import
        //

        // The database is empty so these symbols should not be found
        assertFalse(database.symbolExists(CBA));
        assertFalse(database.symbolExists(WBC));
        assertFalse(database.symbolExists(ANZ));

        List importedQuotes = new ArrayList();
        importedQuotes.add(new EODQuote(CBA, new TradingDate(2005, 9, 16),
                                        1000, 12.0D, 12.0D, 12.0D, 12.0D));
        importedQuotes.add(new EODQuote(CBA, new TradingDate(2005, 9, 15),
                                        1000, 12.0D, 12.0D, 12.0D, 12.0D));
        importedQuotes.add(new EODQuote(ANZ, new TradingDate(2005, 9, 16),
                                        1000, 12.0D, 12.0D, 12.0D, 12.0D));
        importedQuotes.add(new EODQuote(WBC, new TradingDate(2005, 9, 16),
                                        1000, 12.0D, 12.0D, 12.0D, 12.0D));
        importedQuotes.add(new EODQuote(CBA, new TradingDate(2005, 9, 14),
                                        1000, 12.0D, 12.0D, 12.0D, 12.0D));

        int importCount = database.importQuotes(importedQuotes);
        assertEquals(importCount, 5);

        assertTrue(database.symbolExists(CBA));
        assertTrue(database.symbolExists(WBC));
        assertTrue(database.symbolExists(ANZ));

        // Re-import existing quotes with one new quote
        importedQuotes.add(new EODQuote(CBA, new TradingDate(2005, 9, 13),
                                        1000, 12.0D, 12.0D, 12.0D, 12.0D));
        importCount = database.importQuotes(importedQuotes);
        assertEquals(importCount, 1);

        //
        // Test date ranges
        //

        assertEquals(database.getFirstDate(), new TradingDate(2005, 9, 13));
        assertEquals(database.getLastDate(), new TradingDate(2005, 9, 16));
        assertFalse(database.containsDate(new TradingDate(2005, 9, 12)));
        assertTrue(database.containsDate(new TradingDate(2005, 9, 13)));
        assertTrue(database.containsDate(new TradingDate(2005, 9, 14)));
        assertTrue(database.containsDate(new TradingDate(2005, 9, 15)));
        assertTrue(database.containsDate(new TradingDate(2005, 9, 16)));
        assertFalse(database.containsDate(new TradingDate(2005, 9, 17)));

        List dates = database.getDates();
        assertEquals(dates.size(), 4);
        Collections.sort(dates);
        assertEquals(dates.get(0), new TradingDate(2005, 9, 13));
        assertEquals(dates.get(1), new TradingDate(2005, 9, 14));
        assertEquals(dates.get(2), new TradingDate(2005, 9, 15));
        assertEquals(dates.get(3), new TradingDate(2005, 9, 16));
    }

}
