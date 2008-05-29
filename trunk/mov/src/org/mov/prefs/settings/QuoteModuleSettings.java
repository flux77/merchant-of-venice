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



package org.mov.prefs.settings;


/**
 * This class represents quote module data which can restore Quote modules upon restart. 
 * 
 * @author Mark Hummel
 * @see PreferencesManager
 * @see SettingsWriter
 * @see SettingsReader 
 * @see Settings
*/

import javax.swing.JDesktopPane;
import java.util.*;
import org.mov.main.Module;
import org.mov.table.QuoteModule;
import org.mov.quote.SettingsEODQuoteBundle;
import org.mov.quote.EODQuoteBundle;
import org.mov.quote.EODQuoteRange;
import org.mov.quote.QuoteSourceManager;
import org.mov.table.WatchScreen;
import org.mov.util.TradingDate;

import org.mov.prefs.PreferencesManager;
import org.mov.prefs.PreferencesException;
import org.mov.prefs.settings.SettingsWriter;

public class QuoteModuleSettings extends AbstractSettings {
    
    private EODQuoteBundle quoteBundle;
    private boolean singleDate;

    /**
     *
     * QuoteModuleSettings default constructor
     */
    
    public QuoteModuleSettings() {
	super(Settings.TABLE, Settings.QUOTEMODULE);	
    }

    /**
     * 
     * Set the quoteBundle for these Settings
     * 
     * @param quoteBundle  The quoteBundle of a QuoteModule
     */
    
    public void setQuoteBundle(EODQuoteBundle quoteBundle) {
	this.quoteBundle = quoteBundle;
    }

    /**
     * 
     * Return the quoteBundle of these Settings
     * 
     * @return A quoteBundle of a QuoteModule
     */
    public EODQuoteBundle getQuoteBundle() {
	return quoteBundle;
    }

    /**
     * 
     * Return the singleDate flag
     * 
     * @return The singleDate flag
     */
   
    public boolean getSingleDate() {
	return singleDate;
    }
    

    /**
     * 
     * Set the singleDate flag
     * 
     * @param singleDate  Wether the quoteModule consists of a single date
     */
    public void setSingleDate(boolean singleDate) {
	this.singleDate = singleDate;
    }    
    
    /**
     * 
     * Return a QuoteModule based on these settings
     * 
     * @return A QuoteModule
     */
    public Module getModule(JDesktopPane desktop) {	
	return new QuoteModule(quoteBundle, singleDate);		
    }

}