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
import java.util.Collection;
import java.util.Vector;
import javax.swing.JDesktopPane;

import org.mov.main.Main;
import org.mov.macro.StoredMacro;
import org.mov.util.Locale;
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


import org.mov.chart.graph.LineGraph;
import org.mov.chart.graph.BarChartGraph;
import org.mov.chart.graph.HighLowBarGraph;
import org.mov.chart.graph.CandleStickGraph;
import org.mov.chart.graph.PointAndFigureGraph;
import org.mov.chart.graph.BollingerBandsGraph;
import org.mov.chart.graph.MACDGraph;
import org.mov.chart.graph.MovingAverageGraph;
import org.mov.chart.graph.ExpMovingAverageGraph;
import org.mov.chart.graph.MultipleMovingAverageGraph;
import org.mov.chart.graph.MomentumGraph;
import org.mov.chart.graph.OBVGraph;
import org.mov.chart.graph.StandardDeviationGraph;
import org.mov.chart.graph.RSIGraph;
import org.mov.chart.graph.CustomGraph;


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
    Symbol symbol;

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
     * @param   title The graph title  
     */

    public void setTitle(String title) {
	this.title = title;
    }

    /**
     *
     * Set the symbol of the graph
     * 
     * @param symbol  A symbol
     */

    public void setSymbol(Symbol symbol) {
	this.symbol = symbol;
    }

    /**
     *
     * Return the symbol assigned to this graph
     * @return The quote symbol
     */

    public Symbol getSymbol() {
	return symbol;
    }

    /**
     *
     * Set the symbol of the graph
     * 
     * @param symbol  A string representing a Symbol
     */

    public void setSymbol(String s) {
	try {
	    symbol = Symbol.find(s);
	} catch (SymbolFormatException sfe) {

	}
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

       
	if (title.equals(Locale.getString("HIGH_LOW_BAR"))) {
		newGraph = 
		    new HighLowBarGraph(getSource(bundle, Quote.DAY_LOW),
					getSource(bundle, Quote.DAY_HIGH),
					getSource(bundle, Quote.DAY_CLOSE));
						
	    }

	if (title.equals("Line Chart") ||
	    title.equals("Day Close")) {
	    newGraph = new LineGraph(getSource(bundle, Quote.DAY_CLOSE),
				     title,
				     true);
					       
	    
	}

	
	if (title.equals("Day Open")) {
	    newGraph = new LineGraph(getSource(bundle, Quote.DAY_OPEN),
				     title,
				     true);
					       	    
	}
	
	if (title.equals("Day High")) {
	    newGraph = new LineGraph(getSource(bundle, Quote.DAY_HIGH),
				     title,
				     true);
					       	    
	}

	if (title.equals("Day Low")) {
	    newGraph = new LineGraph(getSource(bundle, Quote.DAY_LOW),
				     title,
				     true);
					       	    
	}

	if (title.equals("Volume")) {
	    newGraph = new LineGraph(getSource(bundle, Quote.DAY_VOLUME),
				     title,
				     true);
					       	    
	}

		
	if (title.equals("Simple Moving Average")) {
	    newGraph = new 
		MovingAverageGraph(getSource(bundle, Quote.DAY_CLOSE), settings);
	}


	if (title.equals("Exponential Moving Average")) {
	    newGraph = new 
		MovingAverageGraph(getSource(bundle, Quote.DAY_CLOSE), settings);
	}

	
	

	if (title.equals("Bollinger Bands")) {
	    newGraph = new 
		BollingerBandsGraph(getSource(bundle, Quote.DAY_CLOSE),
				    settings);
	}

	if (title.equals(Locale.getString("MOMENTUM"))) {	    	    
	    newGraph = new
		MomentumGraph(getSource(bundle, Quote.DAY_CLOSE),
			       settings);
	    
	}

	if (title.equals("Multiple Moving Average")) {
	    newGraph = new
		MultipleMovingAverageGraph(getSource(bundle, Quote.DAY_CLOSE));
	}

	
	if (title.equals("OBV")) {
	    
	    newGraph = new
		OBVGraph(getSource(bundle, Quote.DAY_OPEN),
			 getSource(bundle, Quote.DAY_CLOSE),
			 getSource(bundle, Quote.DAY_VOLUME));	    
	}

	if (title.equals("Standard Deviation")) {
	    newGraph = new 
		StandardDeviationGraph(getSource(bundle, Quote.DAY_CLOSE),
				       settings);
		
	}
	
	if (title.equals("MACD")) {
	    newGraph = new
		MACDGraph(getSource(bundle, Quote.DAY_CLOSE),
			  settings);
	}

	if (title.equals("RSI")) {
	    newGraph = new
		RSIGraph(getSource(bundle, Quote.DAY_CLOSE),
			 settings);
	}


	if (title.equals("Custom")) {
	    newGraph = new
		CustomGraph(getSource(bundle, Quote.DAY_CLOSE),
			    symbol,
			    bundle,
			    settings);
	}

	return newGraph;

    }

    
    
    private GraphSource getSource(EODQuoteBundle bundle, int type) {

	assert (type >= Quote.DAY_CLOSE ||
		type <= Quote.DAY_VOLUME);

	return new OHLCVQuoteGraphSource(bundle, type);
    }
}
