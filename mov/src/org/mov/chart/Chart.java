package org.mov.chart;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.*;
import javax.swing.*;

import org.mov.chart.graph.*;
import org.mov.chart.source.*;
import org.mov.util.*;

public class Chart extends JComponent implements MouseListener {

    // The first element of this vector contains a vector of the
    // graphs for the primary graph, the second for the secondary graph etc
    // I.e. its a vector of vector of graphs.
    private Vector levels = new Vector();

    private BasicChartUI gui;
    private boolean toolTipBeenUp = false;

    // Will graph between the start date and end date here, dates is
    // a vector of all dates inbetween (inclusive)
    private TradingDate startDate;
    private TradingDate endDate;
    private Vector dates;

    // When highlighting the grap keep track of the start and end dates
    private TradingDate startHighlightedDate;
    private TradingDate endHighlightedDate;

    // Are we in a zoomed in view?
    private boolean zoomedIn = false;

    // Map of graphs to display annotations
    private HashMap annotatedGraphs = new HashMap();

    public Chart() {
	setBackground(Color.white);
	setForeground(Color.gray);
	addMouseListener(this);
	updateUI();
    }

    private void setDates(TradingDate startDate, TradingDate endDate) {

	// Dont do anything if the start and end date are
	// still the same
	if(dates == null || this.startDate != startDate ||
	   this.endDate != endDate) {
	    this.startDate = startDate;
	    this.endDate = endDate;

	    dates = Converter.dateRangeToTradingDateVector(startDate, endDate);
	}
    }

    public Vector getDates() {
	return dates;
    }

    public TradingDate getStartDate() {
	return startDate;
    }

    public TradingDate getEndDate() {
	return endDate;
    }

    private TradingDate calculateStartDate() {
	Iterator iterator = levels.iterator();
	TradingDate date;
	TradingDate startDate = null;
	
	while(iterator.hasNext()) {

	    Iterator innerIterator = ((Vector)iterator.next()).iterator();
	    
	    while(innerIterator.hasNext()) {	
		date = 
		    ((Graph)innerIterator.next()).getStartDate();
		if(startDate == null || date.compareTo(startDate) < 0)
		    startDate = date;
	    }
	}

	return startDate;
    }

    private TradingDate calculateEndDate() {
	Iterator iterator = levels.iterator();
	TradingDate date;
	TradingDate endDate = null;
	
	while(iterator.hasNext()) {

	    Iterator innerIterator = ((Vector)iterator.next()).iterator();
	    
	    while(innerIterator.hasNext()) {	

		date = 
		    ((Graph)innerIterator.next()).getEndDate();

		if(endDate == null || date.compareTo(endDate) > 0)
		    endDate = date;
	    }
	}
	
	return endDate;
    }

    // Record whether the given graph should have its annotations
    // displayed or not
    public void handleAnnotation(Graph graph, boolean enabled) {
	if(enabled)
	    annotatedGraphs.put(graph, new Boolean(true));
	else
	    annotatedGraphs.remove(graph);
    }

    // Graph's annotation is on?
    public boolean isAnnotated(Graph graph) {
	if(annotatedGraphs.get(graph) != null)
	    return true;
	else
	    return false;
    }

    public void add(Graph graph, int index) {

	// Do we already have any graphs at this index?
	if(index >= levels.size()) {
	    Vector newGraphs = new Vector();
	    newGraphs.add(graph);
	    levels.add(newGraphs);
	}

	// Otherwise add it to an existing graph vector
	else {
	    Vector oldGraphs = (Vector)levels.elementAt(index);
	    oldGraphs.add(graph);
	}

	// If we are not zoomed in, make sure we are displaying the
	// dates of the new graph (if any new dates)
	if(!zoomedIn)
	    setDates(calculateStartDate(), calculateEndDate());
    }

    public void remove(Graph graph) {

	// Find and remove first (only) matching graph
	boolean found = false;
	Graph traverse;
	Iterator iterator = levels.iterator();
	Vector innerVector;
	Iterator innerIterator;

	while(iterator.hasNext()) {
	    innerVector = (Vector)iterator.next();
	    innerIterator = innerVector.iterator();

	    while(innerIterator.hasNext()) {
		traverse = (Graph)innerIterator.next();

		// Found one to remove?
		if(traverse == graph) {
		    // Remove it
		    innerVector.removeElement((Object)graph);

		    // Is the vector now empty?
		    if(innerVector.size() == 0) {
			// Yes so remove it from the outer vector
			levels.remove(innerVector);

			// If we are not zoomed in a graph has disappeared,
			// make sure we arent displaying dates for a graph
			// that doesnt exist yet - but dont do this if
			// we are zoomed in.
			if(!zoomedIn)
			    setDates(calculateStartDate(), calculateEndDate());

		    }
		    return;
		}
	    }
	}

	// If we got here we couldnt find it
    }

    public Vector getLevels() {
	return levels;
    }

    public Color getGraphColour(Graph graph) {
	return gui.getGraphColour(graph, this);
    }

    public void setUI(BasicChartUI ui) {
	super.setUI(ui);
    }

    public void updateUI() {
	gui = new BasicChartUI();

	setUI(gui);
	resetBuffer();
    }

    public TradingDate getDateAtPoint(int x) {
	return gui.getDateAtPoint(this, x);
    }

    public boolean contains(int x, int y) {

	String text = gui.getToolTipText(this, x, y);

	// If the user leaves the cursor over the chart but not near
	// the graph (i.e. the line) we'll tell them how to get a day
	// quote. This is really to fix a problem where tool tips dont
	// pop up IFF another gadget is listening for mouse events AND
	// the first few tool tips set are NULL when the cursor enters
	// the gadget.
	if(text == null && !toolTipBeenUp)
	    text = "Hover cursor near graph for day quote";
	else if(!toolTipBeenUp)
	    toolTipBeenUp = true;

	setToolTipText(text);

	return super.contains(x, y);
    }

    private void setSize() {
	setMinimumSize(new Dimension(BasicChartUI.getMinimumWidth(this),
				     BasicChartUI.getMinimumHeight(this)));
	setPreferredSize(new Dimension(BasicChartUI.getMinimumWidth(this),
				       BasicChartUI.getMinimumHeight(this)));
    }

    public String getUIClassID() {
	return "ChartUI";
    }

    public void setHighlightedRegionStart(TradingDate start) {
	startHighlightedDate = start;
	endHighlightedDate = start;
	repaint();
    }

    public void setHighlightedRegionEnd(TradingDate end) {
	endHighlightedDate = end;
	repaint();
    }

    public TradingDate getHighlightedStart() {
	return startHighlightedDate;
    }

    public TradingDate getHighlightedEnd() {
	return endHighlightedDate;
    }

    public void clearHighlightedRegion() {
	startHighlightedDate = null;
	endHighlightedDate = null;
	repaint();
    }

    public void zoomToHighlightedRegion() {
	// Order [startHighlightedDate, endHighlightedDate]
	if(startHighlightedDate.after(endHighlightedDate)) {
	    TradingDate temp = startHighlightedDate;
	    startHighlightedDate = endHighlightedDate;
	    endHighlightedDate = temp;
	}

	// Recalculate dates 
	setDates(startHighlightedDate, endHighlightedDate);
	clearHighlightedRegion();
	zoomedIn = true;
	resetBuffer();
    }

    public void zoomToDefaultRegion() {

	// Recalculate dates and lowest, highest values
	setDates(calculateStartDate(), calculateEndDate());
	zoomedIn = false;
	resetBuffer();
    }

    public void resetBuffer() {
	if(gui != null)
	    gui.resetBuffer();
	setSize();
    }

    public String getTitle() {

	Iterator levelIterator = levels.iterator();
	Iterator graphIterator;
	Vector graphs;
	String title = "Graph of ";

	while(levelIterator.hasNext()) {
	    graphs = (Vector)levelIterator.next();
	    graphIterator = graphs.iterator();

	    while(graphIterator.hasNext()) {
		title += 
		    ((Graph)graphIterator.next()).getName().toUpperCase();

		if(graphIterator.hasNext())
		    title += ", ";
	    }
	    
	    if(levelIterator.hasNext())
		title += " + ";
	}

	return title;
    }

    public void mouseClicked(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {
	toolTipBeenUp = false;
    }
    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}

}




