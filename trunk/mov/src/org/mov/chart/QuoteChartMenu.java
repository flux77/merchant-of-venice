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

package org.mov.chart;

import java.awt.event.*;
import java.text.*;
import java.util.*;
import javax.swing.*;

import org.mov.chart.graph.*;
import org.mov.chart.source.*;
import org.mov.util.Locale;
import org.mov.util.TradingDate;
import org.mov.quote.*;
import org.mov.ui.*;


/**
 * Provides a menu which is associated with a stock symbol being graphed.
 * The menu provides a series of options which allow the user
 * to graph related charts and indicators.
 */
public class QuoteChartMenu extends JMenu implements ActionListener {

    // Graphs
    private static final String BOLLINGER      = Locale.getString("BOLLINGER_BANDS");
    private static final String DAY_HIGH       = Locale.getString("DAY_HIGH");
    private static final String DAY_LOW        = Locale.getString("DAY_LOW");
    private static final String DAY_OPEN       = Locale.getString("DAY_OPEN");
    private static final String HIGH_LOW_BAR   = Locale.getString("HIGH_LOW_BAR");
    private static final String MACD           = Locale.getString("MACD");
    private static final String MOMENTUM       = Locale.getString("MOMENTUM");
    private static final String MOVING_AVERAGE = Locale.getString("MOVING_AVERAGE");
    private static final String EXP_MOVING_AVERAGE = Locale.getString("EXP_MOVING_AVERAGE");
    private static final String OBV	       = Locale.getString("OBV");
    private static final String RSI	       = Locale.getString("RSI");
    private static final String STANDARD_DEVIATION = Locale.getString("STANDARD_DEVIATION");
    private static final String DAY_VOLUME     = Locale.getString("VOLUME");
    private static final String POINTANDFIGURE = Locale.getString("POINTANDFIGURE");
    private static final String PRICE_THRESHOLD = Locale.getString("PRICE_THRESHOLD");
    private static final String SMOOTHING_CONSTANT = Locale.getString("SMOOTHING_CONSTANT");
        
    JMenu graphMenu;
    JMenu annotateMenu;
    JMenu graphConstantsMenu; // Not convinced this is the best way to allow
                              // the constants to change
    
    private JMenuItem removeMenu;
    private JMenuItem smoothingConstantMenuItem;
    private JMenuItem priceThresholdMenuItem;

    private QuoteBundle quoteBundle;
    private Graph graph;
    private ChartModule listener;
    private HashMap map = new HashMap();
    private HashMap annotateMap = new HashMap();

    //Constants
    private double smoothingConstant = 0.1;
    private double priceThreshold;

    GraphConstants graphConstants;

    /**
     * Create a new menu allowing the user to graph related graphs
     * for the given graph. The symbol we are associated with will be
     * extracted from the graph.
     *
     * @param	listener	the chart module associated with the menu
     * @param	graph		the graph we are associated with
     */
    public QuoteChartMenu(ChartModule listener, QuoteBundle quoteBundle,
			  Graph graph) {


	super(graph.getName());

	this.quoteBundle = quoteBundle;
	this.graph = graph;
	this.listener = listener;
	
	// Create graph + annotation menus
	graphMenu = new JMenu(Locale.getString("GRAPH"));
	annotateMenu = new JMenu(Locale.getString("ANNOTATE"));
	graphConstantsMenu = new JMenu(Locale.getString("GRAPH_CONSTANTS"));
	graphConstants = new GraphConstants();

	// heuristic to guess a starting value for the box reversal value

	Symbol symbol = quoteBundle.getFirstSymbol();
	TradingDate td = quoteBundle.getFirstDate();
	Double guess;

	try {
	    
	    Double close = new Double(quoteBundle.getQuote(symbol, 1, td));
	    guess = new Double(close.toString());
	    
	    graphConstants.setPriceReversalThreshold(guess.doubleValue());
	}
	
	
	catch(MissingQuoteException e) {
	    
	}
	
	this.add(graphMenu);
	this.add(annotateMenu);
	this.add(graphConstantsMenu);

	// Add graph menu items
	addMenuItem(DAY_OPEN);
	addMenuItem(DAY_HIGH);
	addMenuItem(DAY_LOW);
	addMenuItem(DAY_VOLUME);
	graphMenu.addSeparator();
	
	addMenuItem(BOLLINGER);
	addMenuItem(HIGH_LOW_BAR);
	addMenuItem(MACD);
	addMenuItem(MOMENTUM);
	addMenuItem(MOVING_AVERAGE);
	addMenuItem(EXP_MOVING_AVERAGE);
	addMenuItem(OBV);
        addMenuItem(RSI);
	addMenuItem(STANDARD_DEVIATION);
	addMenuItem(POINTANDFIGURE);

	// Add annotation menu items
	addAnnotateMenuItem(MACD, Locale.getString("BUY_SELL"));
	addAnnotateMenuItem(MOVING_AVERAGE, Locale.getString("BUY_SELL"));
		
	// Add graph constant menu items
	// Used to change the view of a graph where the constants
	// relate to the data and thus are independent of the scale
	// (e.g smoothing constant)
	// FIXME maybe

	smoothingConstantMenuItem = new JMenuItem(SMOOTHING_CONSTANT);
	smoothingConstantMenuItem.addActionListener(this);
	graphConstantsMenu.add(smoothingConstantMenuItem);
	
	priceThresholdMenuItem = new JMenuItem(PRICE_THRESHOLD);
	priceThresholdMenuItem.addActionListener(this);
	graphConstantsMenu.add(priceThresholdMenuItem);

	// Add all static menus
	this.addSeparator();
	removeMenu = new JMenuItem(Locale.getString("REMOVE"));
	removeMenu.addActionListener(this);
	this.add(removeMenu);	
    }

    // Add a graph menu item, e.g. "Day Close", "Bollinger Bands"
    private void addMenuItem(String label) {

	// Add graph menu
	JMenuItem item = new JCheckBoxMenuItem(label);
	// Grey out menu item for when quoteBundle is null since
	// there is no data and would just cause a Null Pointer Exception
	// E.g with the Advance/Decline graph
	if (quoteBundle == null) {
	    item.setEnabled(false);
	}
	
	item.addActionListener(this);
	graphMenu.add(item);
    }

    // Add an annotate menu item, e.g. "Moving Average Buy/Sell"
    private void addAnnotateMenuItem(String graphName, String annotation) {
	JMenuItem item = new JCheckBoxMenuItem(graphName + " " + annotation);
	item.addActionListener(this);
	item.setEnabled(false);
	annotateMenu.add(item);
		
	// Save reference to annotation
	annotateMap.put((Object)graphName, item);		
    }

    /**
     * Return the graph name we are associated with
     *
     * @return	the graph name
     */
    public String getName() {
        // Under Java 1.5 beta if I don't check against graph being NULL I get
        // a NULL pointer exception. I don't understand why. This whole module
        // will be upgraded soon so I am not too concerned.
        if(graph != null)
            return graph.getName();
        else
            return "";
    }

    /**
     * This function is called when the user selects one of the menu
     * items.
     *
     * @param	e	the action performed
     */
    public void actionPerformed(ActionEvent e) {
	
	// Check static menus first
	if(e.getSource() == removeMenu) {
	    listener.removeAll(getName());

            // Only redraw if there are only graphs left
            if(listener.count() > 0)
                listener.redraw();
	} 
	
	else if (e.getSource() == smoothingConstantMenuItem) {	    
	    
	    //Put up a dialog requesting input

	    //This doesn't work and I don't know why.  
	    //The dialog doesn't appear and the thread hangs
	    //TextDialog dialog = new TextDialog(listener.getDesktop(), "Smoothing Constant", "Enter Value");
	    //String val = dialog.showDialog();

	}

	else if (e.getSource() == priceThresholdMenuItem) {

	}
	
	// Otherwise check dynamic menus
	else {

	    JCheckBoxMenuItem menu = (JCheckBoxMenuItem)e.getSource();
	    String text = menu.getText();
	
	    // Check annotation menus first
	    if(handleAnnotationMenu(text, menu));
	
	    // Handle removing graphs next
	    else if(!menu.getState())
		removeGraph(text);
	
	    // Ok looks like its adding a graph
	    else if(text == BOLLINGER)
		addGraph(new BollingerBandsGraph(getDayClose(), 20),
			 BOLLINGER, 0);

	    else if(text == DAY_HIGH)
		addGraph(new LineGraph(getDayHigh()), DAY_HIGH, 0);
	
	    else if(text == DAY_LOW)
		addGraph(new LineGraph(getDayLow()), DAY_LOW, 0);
	
	    else if(text == DAY_OPEN)
		addGraph(new LineGraph(getDayOpen()), DAY_OPEN, 0);
	
	    else if(text == HIGH_LOW_BAR)
		addGraph(new HighLowBarGraph(getDayLow(), getDayHigh(),
					     getDayClose()),
			 HIGH_LOW_BAR, 0);
	    else if(text == MACD)
		// 1 1 2 3 5 8 13 21 34 55
		addGraph(new MACDGraph(getDayClose(), 13, 34), MACD, 0);

	    else if(text == MOMENTUM)
		addGraph(new MomentumGraph(getDayClose(), 10), MOMENTUM);
	
	    else if(text == MOVING_AVERAGE)
		addGraph(new MovingAverageGraph(getDayClose(), 40),
			 MOVING_AVERAGE, 0);

	    else if(text == EXP_MOVING_AVERAGE)
		addGraph(new ExpMovingAverageGraph(getDayClose(), 40, graphConstants.getSmoothingConstant()),
			 EXP_MOVING_AVERAGE, 0);	    

	    else if(text == OBV)
		addGraph(new OBVGraph(getDayOpen(), getDayClose(),
				      getDayVolume(),
				      50000.0D), OBV);
	    else if(text == DAY_VOLUME)
		addGraph(new BarGraph(getDayVolume()), DAY_VOLUME);

            else if(text == RSI)
                addGraph(new RSIGraph(getDayClose(), 14), RSI);

	    else if(text == STANDARD_DEVIATION)
		addGraph(new StandardDeviationGraph(getDayClose(), 20),
			 STANDARD_DEVIATION);

	    else if(text == POINTANDFIGURE) 
		addGraph(new PointAndFigureGraph(getDayClose(), 20,graphConstants.getPriceReversalThreshold()),
			 POINTANDFIGURE);
	    
	    else if(text == SMOOTHING_CONSTANT) {
		
	    }

	    else if(text == PRICE_THRESHOLD) {
		
	    }
	    
	}
    }

    // Returns a graph of the day open prices
    private GraphSource getDayOpen() {
	return new OHLCVQuoteGraphSource(quoteBundle, Quote.DAY_OPEN);
    }

    // Returns a graph of the day high prices
    private GraphSource getDayHigh() {
	return new OHLCVQuoteGraphSource(quoteBundle, Quote.DAY_HIGH);
    }

    // Returns a graph of the day low prices
    private GraphSource getDayLow() {
	return new OHLCVQuoteGraphSource(quoteBundle, Quote.DAY_LOW);
    }

    // Returns a graph of the day close prices
    private GraphSource getDayClose() {
	return new OHLCVQuoteGraphSource(quoteBundle, Quote.DAY_CLOSE);
    }

    // Returns a graph of the day volume prices
    private GraphSource getDayVolume() {
	return new OHLCVQuoteGraphSource(quoteBundle, Quote.DAY_VOLUME);
    }

    // Is annotation menu?
    private boolean handleAnnotationMenu(String text,
					 JCheckBoxMenuItem menu) {
	boolean state = menu.getState();
	Set set = annotateMap.keySet();
	Iterator iterator = set.iterator();
	String graphName;

	while(iterator.hasNext()) {
	    graphName = (String)iterator.next();
	
	    // is it an annotation menu?
	    Object object = annotateMap.get(graphName);
	
	    if(object == menu) {
		// Turn on annotation for this graph
		listener.handleAnnotation((Graph)map.get(graphName),
					  state);		
		listener.redraw();
		return true;
	    }
	}	

	return false;
    }

    // Adds graph to chart
    private void addGraph(Graph graph, String mapIdentifier) {
	map.put(mapIdentifier, graph);
	listener.append(graph);
	listener.redraw();
    }

    // Same as above but add at specific index
    private void addGraph(Graph graph, String mapIdentifier, int index) {

	map.put(mapIdentifier, graph);
	listener.append(graph, index);
	listener.redraw();
	
	// Enable annotation menu (if there is one)
	Object object = annotateMap.get(mapIdentifier);
	
	if(object != null) {
	    JCheckBoxMenuItem item = (JCheckBoxMenuItem)object;
	    item.setEnabled(true);		
	}
    }

    // Removes graph from chart
    private void removeGraph(String mapIdentifier) {
	Graph graph = (Graph)map.get(mapIdentifier);
	map.remove(mapIdentifier);
	
	// Remove graph and annotation
	listener.remove(graph);
	listener.handleAnnotation(graph, false);
	listener.redraw();
	
	// Disable annotation menu (if there is one)
	Object object = annotateMap.get(mapIdentifier);
	
	if(object != null) {
	    JCheckBoxMenuItem item = (JCheckBoxMenuItem)object;
	    item.setEnabled(false); // disable check box	
	    item.setSelected(false); // remove tick
	}
    }
}
