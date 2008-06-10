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



package nz.org.venice.prefs.settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.prefs.BackingStoreException;
import javax.swing.JDesktopPane;

import nz.org.venice.main.Main;
import nz.org.venice.macro.StoredMacro;
import nz.org.venice.quote.Symbol;
import nz.org.venice.quote.SymbolFormatException;
import nz.org.venice.table.WatchScreen;
import nz.org.venice.table.WatchScreenParserException;
import nz.org.venice.table.WatchScreenReader;
import nz.org.venice.table.WatchScreenWriter;
import nz.org.venice.chart.ChartModule;


import nz.org.venice.main.Module;
import nz.org.venice.main.ModuleFrame;
import java.util.Collection;
import java.util.Vector;

/**
 * This class represents ChartModule data which can be saved for the purposes
 *  of restoring the modules upon restart.
 * 
 * @author Mark Hummel
 * @see PreferencesManager
 * @see ChartModuleSettingsWriter
 * @see ChartModuleSettingReader 
*/

public class ChartModuleSettings extends AbstractSettings {
    

    private List symbolList;
    private List levelSettingsList;
        
    /**
     *
     * Create new ChartModule settings. 
     *        
     */

    public ChartModuleSettings() {
	super(Settings.CHART, Settings.CHARTMODULE);
    }


    /**
     *
     * Create new ChartModule settings. 
     * 
     * @param   key     The graph Settings Identifier
     * 
       
     */

    public ChartModuleSettings(String key) {
	super(Settings.CHART, Settings.CHARTMODULE, key);
    }

    /**
     *
     * Set the symbolList of this chart.
     * 
     * @param symbolList  A list of symbols
     */
    public void setSymbolList(List symbolList) {
	this.symbolList = symbolList;
    }

    /**
     *
     * Return the list of symbols for this chart.
     * 
     * @return A List of symbols.
     */

    public List getSymbolList() {
	return symbolList;
    }

    /**
     *
     * Return the Level settings to reproduce the chart levels
     * 
     * @return  A List of levels.
     */

    public List getLevelSettingsList() {
	return levelSettingsList;
    }

    /**
     * Put the level settings for the chart levels
     * 
     * @param levelSettingsList  A list of level settings
     */
    
    public void setLevelSettingsList(List levelSettingsList) {
	this.levelSettingsList = levelSettingsList;
    }

    /**
     * 
     * Set the Chart title
     * 
     * @param  title  The chart title
     */

    public void setTitle(String title) {
	super.setTitle(title);
    }

 
    /**
     * 
     * Creates a ChartModule based on this ChartModuleSettings.
     * 
     */

    public Module getModule(JDesktopPane desktop) {
	return new ChartModule(desktop, this);
    }

    public String toString() {
	String rv = super.toString() + "symbols: " + symbolList.toString() + 
	    " #levels: " + levelSettingsList.size() + " levels: " + levelSettingsList.toString();

	return rv;
    }

}