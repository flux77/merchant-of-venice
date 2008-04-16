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

import org.mov.main.Main;
import org.mov.macro.StoredMacro;
import org.mov.quote.Symbol;
import org.mov.quote.SymbolFormatException;
import org.mov.quote.EODQuoteBundle;
import org.mov.quote.Quote;
import org.mov.chart.source.OHLCVQuoteGraphSource;
import org.mov.table.WatchScreen;
import org.mov.table.WatchScreenParserException;
import org.mov.table.WatchScreenReader;
import org.mov.table.WatchScreenWriter;

import org.mov.chart.graph.Graph;
import org.mov.chart.source.GraphSource;
import org.mov.main.Module;
import org.mov.prefs.settings.GraphSettingsWriter;
import org.mov.main.ModuleFrame;
import java.util.Collection;
import java.util.Vector;

import org.mov.chart.graph.BarChartGraph;
import org.mov.chart.graph.MovingAverageGraph;
import org.mov.chart.graph.CandleStickGraph;
import org.mov.chart.graph.PointAndFigureGraph;


/**
 * This class represents Graph data which can be saved for the purposes
 *  of restoring the modules upon restart.
 * 
 * @author Mark Hummel
 * @see PreferencesManager
 * @see GraphSettingsWriter
 * @see GraphSettingReader 
*/

public class GraphSettings extends AbstractSettings {
    
    
    private HashMap settings;
    String title;

    /**
     *
     * Create new GraphSettings. 
     * 
     * @param   key     The graph Settings Identifier
     * @param   parent  The chart settings identifier
     * @param   title   The title of the graph.
       
     */

    public GraphSettings(String key, String parent, String title) {
	super(Settings.CHART,Settings.GRAPHS, key);
	this.title = title;
    }

    /**
     *
     * Create new GraphSettings. 
     * 
     * @param   key     The graph Settings Identifier
     * @param   parent  The chart settings identifier
     */

    public GraphSettings() {
	super(Settings.CHART, Settings.GRAPHS);
	
    }
    
    /**
     *
     * Return the internal graph settings 
     * 
     * @return A hashMap representing a set of key-value pairs
     */
    public HashMap get() {
	return settings;
    }

    /**
     * 
     * Set the internal graph settings. 
     * 
     * @param settings A hashmap representing the settings data  

     */

    public void put(HashMap settings) {
	
	this.settings = settings;

    }


    /**
     * Return the graph title
     * 
     * @return  The graph title
     */
    public String getTitle() {
	return title;
    }

    /**
     * 
     * Set the graph title
     * 
     @ param   title The graph title  
     */

    public void setTitle(String title) {
	this.title = title;
    }

    /**
     * 
     * Return a writer object which can write this classes data
     * 
     * @return  A graphSettingsWriter 
     */
    public SettingsWriter getWriter() {
	return new GraphSettingsWriter();
    }



    //Graph settings are data of the chart module, so nothing is returned here
    public Module getModule(JDesktopPane desktop) {
	return null;
    }

    /**
     * This is factory method which returns a graph represented by this 
     * object's settings.
     * 
     * @return  A graph with these settings.
     */

    public Graph getGraph(EODQuoteBundle bundle) {
	Graph newGraph = null;
	
	if (title.equals("Bar Chart")) {
	    newGraph = new BarChartGraph(getSource(bundle, Quote.DAY_OPEN),
					 getSource(bundle, Quote.DAY_CLOSE),
					 getSource(bundle, Quote.DAY_HIGH),
					 getSource(bundle, Quote.DAY_LOW));
	    
	}
	
	if (title.equals("Candle Stick")) {
	    newGraph = new CandleStickGraph(getSource(bundle, Quote.DAY_OPEN),
					    getSource(bundle, Quote.DAY_LOW),
					    getSource(bundle, Quote.DAY_HIGH),
					    getSource(bundle, Quote.DAY_CLOSE));
					    	    
	}

	if (title.equals("Point and Figure")) {
	    newGraph = new PointAndFigureGraph(getSource(bundle, Quote.DAY_CLOSE),
					  settings);
	}


	if (title.equals("Line Chart")) {
	    newGraph = null;
	}

	if (title.equals("Simple Moving Average")) {
	    newGraph = new 
		MovingAverageGraph(getSource(bundle, Quote.DAY_CLOSE), settings);
	}

	return newGraph;
    }
    
    private GraphSource getSource(EODQuoteBundle bundle, int type) {

	assert (type >= Quote.DAY_CLOSE ||
		type <= Quote.DAY_VOLUME);

	return new OHLCVQuoteGraphSource(bundle, type);
    }
}

