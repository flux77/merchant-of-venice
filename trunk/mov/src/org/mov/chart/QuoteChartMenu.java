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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;

import org.mov.chart.graph.*;
import org.mov.chart.source.GraphSource;
import org.mov.chart.source.OHLCVQuoteGraphSource;
import org.mov.util.Locale;
import org.mov.util.TradingDate;
import org.mov.quote.Quote;
import org.mov.quote.QuoteBundle;
import org.mov.quote.QuoteFunctions;

/**
 * Provides a menu which is associated with a stock symbol being graphed.
 * The menu provides a series of options which allow the user
 * to graph related charts and indicators.
 *
 * @author Andrew Leppard
 */
public class QuoteChartMenu extends JMenu implements ActionListener {

    // Graphs
    private static final String BAR_CHART          = Locale.getString("BAR_CHART");
    private static final String BOLLINGER          = Locale.getString("BOLLINGER_BANDS");
    private static final String CANDLE_STICK       = Locale.getString("CANDLE_STICK");
    private static final String DAY_HIGH           = Locale.getString("DAY_HIGH");
    private static final String DAY_LOW            = Locale.getString("DAY_LOW");
    private static final String DAY_OPEN           = Locale.getString("DAY_OPEN");
    private static final String DAY_VOLUME         = Locale.getString("VOLUME");
    private static final String EXP_MOVING_AVERAGE = Locale.getString("EXP_MOVING_AVERAGE");
    private static final String HIGH_LOW_BAR       = Locale.getString("HIGH_LOW_BAR");
    private static final String LINE_CHART         = Locale.getString("LINE_CHART");
    private static final String MOMENTUM           = Locale.getString("MOMENTUM");
    private static final String MOVING_AVERAGE     = Locale.getString("MOVING_AVERAGE");
    private static final String OBV	           = Locale.getString("OBV");
    private static final String POINT_AND_FIGURE   = Locale.getString("POINT_AND_FIGURE");
    private static final String RSI	           = Locale.getString("RSI");
    private static final String STANDARD_DEVIATION = Locale.getString("STANDARD_DEVIATION");

    private static final String PRICE_THRESHOLD    = Locale.getString("PRICE_THRESHOLD");
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


    private ButtonGroup viewButtonGroup = new ButtonGroup();
    private Graph lastViewGraph = null;

    //Constants
    private double smoothingConstant = 0.1;
    private double priceThreshold;

    private GraphConstants graphConstants;

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
	GraphSource source = getDayClose();
	Graphable graphable = source.getGraphable();
	double[] values = graphable.toArray();
	
	double guess  = QuoteFunctions.sd(values,
					  1,
					  values.length) / 2;

	guess = QuoteFunctions.roundDouble(guess, 2);
	
	graphConstants.setPriceReversalThreshold(guess);
	
		
	this.add(graphMenu);
	this.add(annotateMenu);
	this.add(graphConstantsMenu);

        // Add the view menu items. Usually the user will only want to display
        // one of these at a time. So if they do, unslect the other members of
        // the group.
	addViewMenuItem(LINE_CHART, true); // Initially selected
        addViewMenuItem(BAR_CHART, false);
	addViewMenuItem(CANDLE_STICK, false);
	addViewMenuItem(HIGH_LOW_BAR, false);
	addViewMenuItem(POINT_AND_FIGURE, false);
        lastViewGraph = graph;

        graphMenu.addSeparator();

	// Add the indicator menu items. These indicators can be "stacked" anyway
        // the user wishes. For example, the user can display bollinger bands and
        // Moving Average in the same graph.
	addMenuItem(BOLLINGER);
	addMenuItem(DAY_OPEN);
	addMenuItem(DAY_HIGH);
	addMenuItem(DAY_LOW);
	addMenuItem(DAY_VOLUME);
	addMenuItem(MOMENTUM);
	addMenuItem(MOVING_AVERAGE);
	addMenuItem(EXP_MOVING_AVERAGE);
	addMenuItem(OBV);
        addMenuItem(RSI);
	addMenuItem(STANDARD_DEVIATION);

	// Add annotation menu items
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

    private void addViewMenuItem(String label, boolean isSelected) {
        JRadioButtonMenuItem item = new JRadioButtonMenuItem(label);
        viewButtonGroup.add(item);
        item.setSelected(isSelected);
        item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Graph graph;
                    JRadioButtonMenuItem item = (JRadioButtonMenuItem)e.getSource();
                    String label = item.getText();

                    // First remove last view graph displayed
                    if(lastViewGraph != null)
                        listener.remove(lastViewGraph);

                    // Now add the new one
                    if(label == POINT_AND_FIGURE)
                        graph = new PointAndFigureGraph(getDayClose(), 20,
                                                        graphConstants.getPriceReversalThreshold());
                    else if(label == BAR_CHART)
                        graph = new BarChartGraph(getDayOpen(), getDayLow(),
                                                  getDayHigh(), getDayClose());
                    else if(label == CANDLE_STICK)
                        graph = new CandleStickGraph(getDayOpen(), getDayLow(),
                                                     getDayHigh(), getDayClose());
                    else if(label == HIGH_LOW_BAR)
                        graph = new HighLowBarGraph(getDayLow(), getDayHigh(),
                                                    getDayClose());
                    else {
                        assert(label == LINE_CHART);
                        graph = new LineGraph(getDayClose());
                    }

                    listener.append(graph, 0);
                    listener.redraw();

                    lastViewGraph = graph;
                }});

        graphMenu.add(item);
    }

    // Add a graph menu item, e.g. "Day Close", "Bollinger Bands"
    private void addMenuItem(String label) {

	// Add graph menu
	JMenuItem item = new JCheckBoxMenuItem(label);
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

	    // Replace this with TextDialog
	
	    String constantStr = (new Double(graphConstants.getSmoothingConstant())).toString();
	    String val = JOptionPane.showInputDialog(null, "Enter Value", constantStr);
	
	    if (val != null) {
		graphConstants.setSmoothingConstant( (new Double(val)).doubleValue());
	    }


	    // FIXME: replace this kludge with proper redraw code
	
	    if (graphExists(EXP_MOVING_AVERAGE)) {
		removeGraph(EXP_MOVING_AVERAGE);
		addGraph(new ExpMovingAverageGraph(getDayClose(), 40, graphConstants.getSmoothingConstant()),
			 EXP_MOVING_AVERAGE, 0);	
		
		
	    }

	}
	
	else if (e.getSource() == priceThresholdMenuItem) {

	    String constantStr = (new Double(graphConstants.getPriceReversalThreshold())).toString();
	    String val = JOptionPane.showInputDialog(null, "Enter Value", constantStr);
	
	    if (val != null) {
		graphConstants.setPriceReversalThreshold( (new Double(val)).doubleValue());
	    }

	    // FIXME: replace this kludge with proper redraw code
	    if (graphExists(POINT_AND_FIGURE)) {
		removeGraph(POINT_AND_FIGURE);
		addGraph(new PointAndFigureGraph(getDayClose(), 20,graphConstants.getPriceReversalThreshold()),
			 POINT_AND_FIGURE);
	    }
	
	}
	

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

    // Return whether or not the graph has been drawn

    private boolean graphExists(String mapIdentifier) {
	Graph graph = (Graph)map.get(mapIdentifier);
	
	return true ? graph != null : false ;
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
