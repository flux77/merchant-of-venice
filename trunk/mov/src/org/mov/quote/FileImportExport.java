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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.mov.util.Locale;
import org.mov.util.Report;

/**
 * Import quotes from files into Venice or export them to files.
 *
 * @author Andrew Leppard
 * @see InternetImport, ImportQuoteModule, ExportQuoteModule
 */
public class FileImportExport {

    // This class is not instantiated.
    private FileImportExport() {
        assert false;
    }

    /** 
     * Import quotes from a file into Venice.
     *
     * @param report report to log warnings and errors
     * @param filter format of quote file
     * @param file quote file to import
     * @return list of quotes
     */
    public static List importFile(Report report, QuoteFilter filter, File file) {
        List quotes = new ArrayList();
        String fileName = file.getName();

	try {
            FileInputStream fileStream = new FileInputStream(file);
            InputStreamReader inputStream = new InputStreamReader(fileStream);
	    BufferedReader fileReader = new BufferedReader(inputStream);
            int lineNumber = 1;
	    String line = fileReader.readLine();

	    while(line != null) {
                try {
                    Quote quote = filter.toQuote(line);
                    quotes.add(quote);
                    verify(report, fileName, lineNumber, quote);
                }
                catch(QuoteFormatException e) {
                    report.addError(fileName + ":" +
                                    Integer.toString(lineNumber) + ":" +
                                    Locale.getString("ERROR") + ": " +
                                    e.getMessage());
                }

                line = fileReader.readLine();
                lineNumber++;
	    }
		
	    fileReader.close();

	} catch (IOException e) {
            report.addError(fileName + ":" +
                            Locale.getString("ERROR") + ": " +
                            Locale.getString("ERROR_READING_FROM_FILE", fileName));
        }

        return quotes;
    }

    /**
     * Export a single day of quotes from Venice into a file
     *
     * @param filter format of quote file
     * @param file quote file to export
     * @param quotes list of quotes to export
     * @exception IOException if there was an error writing the file
     */
    public static void exportFile(QuoteFilter filter, File file, List quotes)
        throws IOException {

        // Don't bother creating empty files
        if(quotes.size() > 0) {
            FileWriter fileWriter = new FileWriter(file);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            PrintWriter printWriter = new PrintWriter(bufferedWriter);
            
            // Iterate through stocks printing them to file
            for(Iterator iterator = quotes.iterator(); iterator.hasNext();) {
                Quote quote = (Quote)iterator.next();
                printWriter.println(filter.toString(quote));
            }
            
            printWriter.close();
        }
    }

    /**
     * Verify the quote is valid. Log any problems to the report and try to clean
     * it up the best we can.
     *
     * @param report the report
     * @param fileName the name of the quote file
     * @param lineNumber the line number in the quote file or the quote
     * @param quote the quote
     */
    private static void verify(Report report, String fileName, int lineNumber, Quote quote) {
        try {
            quote.verify();
        }
        catch(QuoteFormatException e) {
            List messages = e.getMessages();

            for(Iterator iterator = messages.iterator(); iterator.hasNext();) {
                String message = (String)iterator.next();

                report.addWarning(fileName + ":" + 
                                  Integer.toString(lineNumber) + ":" +
                                  Locale.getString("WARNING") + ": " +
                                  message);
            }
        }
    }
}
