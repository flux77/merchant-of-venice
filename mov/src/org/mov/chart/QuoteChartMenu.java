package org.mov.chart;

import java.awt.event.*;
import java.text.*;
import java.util.*;
import javax.swing.*;

import org.mov.chart.graph.*;
import org.mov.chart.source.*;
import org.mov.util.*;
import org.mov.quote.*;

/**
 * Provides a menu which is associated with a stock symbol being graphed. 
 * The menu provides a series of options which allow the user
 * to graph related charts and indicators. 
 */
public class QuoteChartMenu extends JMenu implements ActionListener {

    // Graphs
    private static final String BOLLINGER      = "Bollinger Bands";
    private static final String DAY_HIGH       = "Day High";
    private static final String DAY_LOW        = "Day Low";
    private static final String DAY_OPEN       = "Day Open";
    private static final String HIGH_LOW_BAR   = "High Low Bar";
    private static final String MACD           = "MACD";
    private static final String MOMENTUM       = "Momentum";
    private static final String MOVING_AVERAGE = "Moving Average";
    private static final String OBV	       = "OBV";
    private static final String STANDARD_DEVIATION = "Standard Deviation";
    private static final String VOLUME         = "Volume";
    
    // HashMap of graphs to their annotations if any
    private HashMap graphMap = new HashMap();
    
    private JMenuItem removeMenu;
    
    private QuoteCache cache;
    private Graph graph;
    private ChartModule listener;
    private HashMap map = new HashMap();
    private HashMap annotateMap = new HashMap();
  
    /**
     * Create a new menu allowing the user to graph related graphs
     * for the given graph. The symbol we are associated with will be
     * extracted from the graph.
     *
     * @param	listener	the chart module associated with the menu
     * @param	graph		the graph we are associated with
     */
    public QuoteChartMenu(ChartModule listener, QuoteCache cache,
			  Graph graph) {

	super(graph.getName().toUpperCase());
	
	// Order not important - will be added to menu in
	// alphabetical order
	graphMap.put((Object)BOLLINGER,	null);
	graphMap.put((Object)DAY_HIGH,	null);
	graphMap.put((Object)DAY_LOW,	null);
	graphMap.put((Object)DAY_OPEN,	null);
	graphMap.put((Object)HIGH_LOW_BAR,	null);
	graphMap.put((Object)MACD,		"Buy/Sell");
	graphMap.put((Object)MOMENTUM,		null);
	graphMap.put((Object)MOVING_AVERAGE, "Buy/Sell");
	graphMap.put((Object)OBV,		null);
	graphMap.put((Object)STANDARD_DEVIATION,	null);
	graphMap.put((Object)VOLUME,	null);
	
	this.cache = cache; 
	this.graph = graph;
	this.listener = listener;
	
	// Create graph + annotation menus
	JMenu graphMenu = new JMenu("Graph");
	JMenu annotateMenu = new JMenu("Annotate");
	this.add(graphMenu);
	this.add(annotateMenu);
	
	// Get list of graphs in alphabetical order
	TreeSet set = new TreeSet(Collator.getInstance());
	set.addAll(graphMap.keySet());
	
	Iterator iterator = set.iterator();
	String graphName;
	JCheckBoxMenuItem item;
	Object object;
	
	while(iterator.hasNext()) {
	    graphName = (String)iterator.next();
	    
	    // Add graph menu
	    item = new JCheckBoxMenuItem(graphName);
	    item.addActionListener(this);
	    graphMenu.add(item);
	    
	    // Add annotation menu
	    object = graphMap.get(graphName);
	    
	    if(object != null) {
		item = new JCheckBoxMenuItem(graphName + " " + 
					     (String)object);
		item.addActionListener(this);
		item.setEnabled(false);
		annotateMenu.add(item);
		
		// Save reference to annotation
		annotateMap.put((Object)graphName, item);		    
	    }
	}
	
	// Add all static menus
	this.addSeparator();
	removeMenu = new JMenuItem("Remove");
	removeMenu.addActionListener(this);
	this.add(removeMenu);	    
    }

    /**
     * Return the graph name we are associated with
     *
     * @return	the graph name
     */
    public String getName() {
	return graph.getName();
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
	    listener.redraw();
	}
	
	// Otherwise check dynamic menus
	else {
	    JCheckBoxMenuItem menu = (JCheckBoxMenuItem)e.getSource();
	    String text = menu.getText();
	    
	    // Create these now so we dont have to create them in 
	    // 10 different places. We might not use them but hey.
	    GraphSource dayOpen = 
		new OHLCVQuoteGraphSource(cache, Quote.DAY_OPEN);
	    GraphSource dayHigh = 
		new OHLCVQuoteGraphSource(cache, Quote.DAY_HIGH);
	    GraphSource dayLow = 
		new OHLCVQuoteGraphSource(cache, Quote.DAY_LOW);
	    GraphSource dayClose = 
		new OHLCVQuoteGraphSource(cache, Quote.DAY_CLOSE);
	    GraphSource dayVolume = 
		new OHLCVQuoteGraphSource(cache, Quote.DAY_VOLUME);
	    
	    // Check annotation menus first
	    if(handleAnnotationMenu(text, menu.getState()));
	    
	    // Handle removing graphs next
	    else if(!menu.getState())
		removeGraph(text);
	    
	    // Ok looks like its adding a graph
	    else if(text == BOLLINGER) 
		addGraph(new BollingerBandsGraph(dayClose, 20), 
			 BOLLINGER, 0);

	    else if(text == DAY_HIGH) 
		addGraph(new LineGraph(dayHigh), DAY_HIGH, 0);
	    
	    else if(text == DAY_LOW) 
		addGraph(new LineGraph(dayLow), DAY_LOW, 0);
	    
	    else if(text == DAY_OPEN) 
		addGraph(new LineGraph(dayOpen), DAY_OPEN, 0);
	    
	    else if(text == HIGH_LOW_BAR) 
		addGraph(new HighLowBarGraph(dayLow, dayHigh, dayClose),
			 HIGH_LOW_BAR, 0);
	    else if(text == MACD)
		// 1 1 2 3 5 8 13 21 34 55
		addGraph(new MACDGraph(dayClose, 13, 34), MACD, 0);

	    else if(text == MOMENTUM) 
		addGraph(new MomentumGraph(dayClose, 10), MOMENTUM);
	    
	    else if(text == MOVING_AVERAGE) 
		addGraph(new MovingAverageGraph(dayClose, 40), 
			 MOVING_AVERAGE, 0);

	    else if(text == OBV) 
		addGraph(new OBVGraph(dayOpen, dayClose, dayVolume, 
				      50000.0F), OBV);
	    else if(text == VOLUME) 
		addGraph(new BarGraph(dayVolume), VOLUME);

	    else if(text == STANDARD_DEVIATION) 
		addGraph(new StandardDeviationGraph(dayClose, 20), 
			 STANDARD_DEVIATION);
	}
    }
    
    // Is annotation menu?
    private boolean handleAnnotationMenu(String text, boolean state) {
	Set set = graphMap.keySet();
	Iterator iterator = set.iterator();
	String graphName;
	String annotationName;
	
	while(iterator.hasNext()) {
	    graphName = (String)iterator.next();
	    annotationName = graphName + " " +  graphMap.get(graphName);
	    
	    // is it an annotation menu?
	    if(annotationName.equals(text)) {
		
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
	    item.setEnabled(false);	// disable check box	   
	    item.setSelected(false); // remove tick
	}
    }
}
