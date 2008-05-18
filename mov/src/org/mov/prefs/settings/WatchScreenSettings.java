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
 * This class  represents WatchScreen Module data  which can restore modules upon restart. 
 * 
 * @author Mark Hummel
 * @see PreferencesManager
 * @see ModuleSettingsWriter
 * @see ModuleSettingReader 
*/

import javax.swing.JDesktopPane;
import java.util.*;
import org.mov.main.Module;
import org.mov.table.WatchScreenModule;
import org.mov.quote.MixedQuoteBundle;
import org.mov.quote.QuoteSourceManager;
import org.mov.table.WatchScreen;
import org.mov.util.TradingDate;

import org.mov.prefs.PreferencesManager;
import org.mov.prefs.PreferencesException;
import org.mov.prefs.settings.SettingsWriter;

public class WatchScreenSettings extends AbstractSettings {
    
    public WatchScreenSettings() {
	super(Settings.TABLE, Settings.WATCHSCREENMODULE);
    }

    public WatchScreenSettings(String title) {
	super(Settings.TABLE, Settings.WATCHSCREENMODULE);
	super.setTitle(title);
    }

    public Module getModule(JDesktopPane desktop) {
	
	try {
	    WatchScreen watchScreen = PreferencesManager.getWatchScreen(getTitle());
	    
	    TradingDate lastDate = QuoteSourceManager.getSource().getLastDate();
	    
	    if (lastDate != null) {	
		MixedQuoteBundle quoteBundle = new MixedQuoteBundle(watchScreen.getSymbols(),
								    lastDate.previous(1),
								    lastDate);
		
		return new WatchScreenModule(watchScreen, quoteBundle);
	    }	    	    
	} catch (PreferencesException pfe) {
	}	
	return null;	    
    }
}