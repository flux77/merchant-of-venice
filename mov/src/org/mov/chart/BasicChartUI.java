package org.mov.chart;

import java.awt.*;
import java.awt.image.*;
import java.lang.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.plaf.*;

import org.mov.chart.graph.*;
import org.mov.chart.source.*;
import org.mov.util.*;
import org.mov.quote.*;

/**
 * Chart Implementation.
 */
public class BasicChartUI extends ComponentUI implements ImageObserver  {

    //
    // Constants that define the look and feel of the graph
    //

    // Buffer space between start of pimary graph and first line. Multiply
    // this number by the height of the font to get the space
    private static final double TOP_GRAPH_BUFFER_MULTIPLIER = 1.5;

    // Space assigned for X label
    private static final int X_LABEL_HEIGHT = 40;

    // Space assigned for Y label text
    private static final int Y_LABEL_WIDTH = 60;

    // Minimum height of graph level in pixels (not including X lables)
    private static final int MINIMUM_LEVEL_HEIGHT = 50;

    // Horizontal margin in annotated text pop-up boxes (in pixels)
    private static final int ANNOTATED_TEXT_MARGIN = 5;

    //
    // Other constants
    //

    // Trading month has a minimum of this many trading days (roughly :)
    private static final int MINIMUM_TRADING_DAYS_IN_MONTH = 20;
    
    // When buffering an image, we create a buffer area around (in pixels)
    private static final int BUFFER_BUFFER_SIZE = 200;

    // Ratio of primary level to secondary level size (secondary graph
    // levels such as volume graphs try to be 1/6 of the size of the primary
    // e.g. day close graphs)
    private static final int PRIMARY_HEIGHT_UNITS = 6;
    private static final int SECONDARY_HEIGHT_UNITS = 1;

    // These variables are the same for each graph we draw
    private float horizontalScale;
    private int firstHorizontalLine;

    // We buffer the graph image for speed so we dont have to recalculate
    // it each time the user moves the scroll bar
    private BufferedImage image = null;
    private int bufferWidth = 0;
    private int bufferHeight = 0;
    private Vector levelHeights;

    // Also precompute data that doesnt change on resizing
    private HorizontalAxis quartersHorizontalAxis = null;
    private HorizontalAxis monthsHorizontalAxis = null;
    private HorizontalAxis majorHorizontalAxis = null;
    private HorizontalAxis minorHorizontalAxis = null;
    private Vector verticalAxes = null;
    private HashMap colourMap = null;

    // Remember these variables last values as they are needed for
    // tool tip generation
    private int xoffset, yoffset;

    /**
     * Return the minimum width needed by this component.
     * 
     * @param	chart	the chart component
     */
    public static int getMinimumWidth(Chart chart) {
	// The minimum width is merely the length of this plus some
	// space for the Y axis labels. The minimum height is fixed but
	// may or may not include the X axis labels height.
	if(chart.getXRange() != null)
	    return chart.getXRange().size() + Y_LABEL_WIDTH;
	else
	    return Y_LABEL_WIDTH;
    }

    /**
     * Return the minimum height needed by this component.
     * 
     * @param	chart	the chart component
     */
    public static int getMinimumHeight(Chart chart) {
	// Levels of graphs
	int levels = chart.getLevels().size();
	
	return MINIMUM_LEVEL_HEIGHT * levels + X_LABEL_HEIGHT;
    }

    public static ComponentUI createUI(JComponent c) {
	return new BasicChartUI();
    }

    /**
     * Return the X value at the given x coordinate.
     *
     * @param	chart	the chart component
     * @param	xCoordinate	an x coordinate on the screen
     * @return	the X value at the x coordinate
     */
    public Comparable getXAtPoint(Chart chart, int xCoordinate) {

	Comparable x = null;

       	// First find the X at this point
	int xOffset = (int)(xCoordinate / horizontalScale);

	if(xOffset >= chart.getXRange().size())
	    xOffset = chart.getXRange().size()-1;
	if(xOffset < 0)
	    xOffset = 0;

	if(chart.getXRange() != null && xOffset < chart.getXRange().size())
	    x = (Comparable)chart.getXRange().elementAt(xOffset);
	
	return x;
    }
    
    /**
     * Return which graph level contains the given y coordinate.
     *
     * @param	yCoordinate	the y coordinate to query
     * @return	the level which contains the y coordinate or <code>0</code>
     *		if none do
     */
    private int getLevelAtPoint(int yCoordinate) {
	
	Iterator iterator = levelHeights.iterator();
	int level = 0;
	int yoffset = 0;
	int levelHeight;

	while(iterator.hasNext()) {
	    
	    yoffset += ((Integer)iterator.next()).intValue();
	    if(yCoordinate <= yoffset)
		return level;
	    
	    level++;
	}
       	return 0;
    }

    /**
     * Return the start y offset for the given level.
     *
     * @param	level	the level
     * @return	the start y offset
     */
    private int getStartOfLevel(int level) {
	int yoffset = 0;

	if(level > 0)
	    for(int i = 0; i < level-1; i++) {
		yoffset += ((Integer)levelHeights.elementAt(i)).intValue();
	    }
	
	return yoffset;
    }

    /**
     * Return the tool tip text for the given x, y coordinates.
     *
     * @param	chart	the chart component
     * @param	x	the x coordinate
     * @param	y	the y coordinate
     */
    public String getToolTipText(Chart chart, int xCoordinate, 
				 int yCoordinate) {

	// Abort if some of our variables are set yet
	if(verticalAxes == null)
	    return null;

	Insets insets = chart.getInsets();
	int height = chart.getHeight() - insets.top - insets.bottom;

	// Get x at this point
	Comparable x = getXAtPoint(chart, xCoordinate);
	// Get graph level at this point
	int level = getLevelAtPoint(yCoordinate);
	// Get vertical axis of graph level
	VerticalAxis verticalAxis = 
	    (VerticalAxis)verticalAxes.elementAt(level);
	int yoffset = getStartOfLevel(level) + firstHorizontalLine;

	String toolTipText = null;

	if(yCoordinate < (height - X_LABEL_HEIGHT) && verticalAxis != null &&
	   x != null) {

	    // Iterate through all graphs until one of the graph gives us
	    // a tooltip
	    Iterator iterator = 
		((Vector)chart.getLevels().elementAt(level)).iterator();
	    Graph graph;
    
	    while(iterator.hasNext() && toolTipText == null) {
		graph = (Graph)iterator.next();
		
		toolTipText = 
		    graph.getToolTipText(x, yCoordinate, yoffset + 
					 verticalAxis.getHeightOfGraph(),
					 verticalAxis.getScale(), 
					 verticalAxis.getBottomLineValue());
	    }
	}

	return toolTipText;
    }

    /**
     * Paint the component. 
     *
     * @param	g the graphics to paint to
     * @param	c the chart component
     */
    public void paint(Graphics g, JComponent c) {

	// Get size
	Insets insets = c.getInsets();

	int width = c.getWidth() - insets.left - insets.right;
	int height = c.getHeight() - insets.top - insets.bottom;
	
	// Do we need to allocate a new image buffer? We do if:
	// 1) There isnt one
	// 2) Its not big enough to fit the image
	// 3) Its bigger than the image by more than twice the buffer size
	if(image == null || 
	   image.getWidth() < width || image.getHeight() < height ||
	   image.getWidth() > (width + 2*BUFFER_BUFFER_SIZE) ||
	   image.getHeight() > (height + 2*BUFFER_BUFFER_SIZE)) {
	    
	    // Allocate a little more space than we need 
	    image = 
		new BufferedImage(width + BUFFER_BUFFER_SIZE, 
				  height + BUFFER_BUFFER_SIZE, 
				  BufferedImage.TYPE_3BYTE_BGR);	
	}
	
	// Draw it iff the size has changed
	if(width != bufferWidth || height != bufferHeight) {
	    bufferedPaint(image.getGraphics(), (Chart)c, width, height);
	    
	    bufferWidth = width;
	    bufferHeight = height;
	}
	
	// Copy buffer to screen
	g.drawImage(image, 0, 0, this);
	
	// Finally highlight region
	highlightRegion(g, (Chart)c, height);
    }

    // Repaint the component and recalculate everything
    private void bufferedPaint(Graphics g, Chart chart, 
			       int width, int height) {

	// Calculate horizontal axis
	calculateHorizontalAxes(g, chart, width, height);

	// Draw everything
	drawBackground(g, chart, width, height);
	drawLevels(g, chart, width, height);
	drawHorizontalLabels(g, height);
	drawAnnotations(g, chart);
    }

    // Create a new horizontal axis which is sized for the component
    private void calculateHorizontalAxes(Graphics g, Chart chart, 
					 int width, int height) {
	// Minor horizontal axis can either be MONTHS or QUARTERS. Test
	// to see if twice the width of "Feb" can fit within the pixel
	// space given if we use MONTHS, if not - use QUARTERS.

	// Calculate horizontal scale
	horizontalScale =
	    HorizontalAxis.calculateScale(width - Y_LABEL_WIDTH, 
					  chart.getXRange().size());

	if(horizontalScale * MINIMUM_TRADING_DAYS_IN_MONTH <
	   2 * g.getFontMetrics().stringWidth(new String("Feb"))) {
	    
	    if(quartersHorizontalAxis == null)
		quartersHorizontalAxis = 
		    new HorizontalAxis(chart.getXRange(), 
				       HorizontalAxis.QUARTERS,
				       HorizontalAxis.MINOR);

	    minorHorizontalAxis = quartersHorizontalAxis;
	}
	else {
	    if(monthsHorizontalAxis == null)		
		monthsHorizontalAxis = 
		    new HorizontalAxis(chart.getXRange(), 
				       HorizontalAxis.MONTHS,
				       HorizontalAxis.MINOR);

	    minorHorizontalAxis = monthsHorizontalAxis;
	}


	// Major horizontal axis is always years
	if(majorHorizontalAxis == null)
	    majorHorizontalAxis = 
		new HorizontalAxis(chart.getXRange(), HorizontalAxis.YEARS,
				   HorizontalAxis.MAJOR);

    }

    // Highlight the component's highlighted region
    private void highlightRegion(Graphics g, Chart chart, int height) {
	if(chart.getHighlightedStart() != null && 
	   chart.getHighlightedEnd() != null) {

	    // Convert X value to X coordinate
	    int start = getXCoordinate(chart, chart.getHighlightedStart());
	    int end = getXCoordinate(chart, chart.getHighlightedEnd());

	    g.setXORMode(Color.pink);

	    g.fillRect(start < end? start: end, 0, 
		       1+Math.abs(end-start), height);

	    g.setPaintMode();
	}
    }

    // For the given X value, return the X coordinate
    private int getXCoordinate(Chart chart, Comparable x) {

	int i = 0;
	
	if(chart.getXRange() != null) {
	    Iterator iterator = chart.getXRange().iterator();
	    Comparable thisX = null;

	    while(iterator.hasNext()) {
		thisX = (Comparable)iterator.next();

		if(x.compareTo(thisX) <= 0)
		    return (int)(i * horizontalScale);
		
		i++;
	    }
	}
	return (int)(i * horizontalScale);
    }

    /**
     * Reset the double buffer and force the chart to redraw.
     */
    public void resetBuffer() {
	// Recalculate everything
	quartersHorizontalAxis = null;
	monthsHorizontalAxis = null;
	majorHorizontalAxis = null;
	verticalAxes = null;
	colourMap = null;
	image = null;
	bufferWidth = 0;
	bufferHeight = 0;
    }

    // Set the background colour 
    private void drawBackground(Graphics g, Chart chart, int width,
				int height) {
	g.setColor(chart.getBackground());
	g.fillRect(0, 0, width, height);
    }

    // Draw the vertical grid and the vertical labels onto the chart
    private void drawVerticalGridAndLabels(Graphics g, 
					   Graph firstGraph,
					   String title,
					   VerticalAxis verticalAxis,
					   int yoffset, int width) {
	g.setColor(Color.lightGray);
	verticalAxis.drawGridAndLabels(g, firstGraph, title, 0, yoffset,
				       width - Y_LABEL_WIDTH);

    }

    // Draw all the graph levels onto the chart
    private void drawLevels(Graphics g, Chart chart, int width, int height) {
	// Calculate space between top of graph and first horizontalLine
	firstHorizontalLine = getFirstHorizontalLine(g);

	// Calculate height of each level
	calculateLevelHeights(chart, height);

	// Draw each graph level
	Iterator iterator = chart.getLevels().iterator();

	int yoffset = firstHorizontalLine;
	int level = 0;

	while(iterator.hasNext()) {
	    Vector graphs = (Vector)iterator.next();

	    drawLevel(g, graphs, chart, yoffset, width, 
		      ((Integer)levelHeights.elementAt(level)).intValue() - 
		      firstHorizontalLine, level);
		      
	    yoffset += ((Integer)levelHeights.elementAt(level++)).intValue();
	}
    }

    // Draw a single graph level onto the chart
    private void drawLevel(Graphics g, Vector graphs, Chart chart, int yoffset,
			   int width, int height, int level) {

	VerticalAxis verticalAxis = calculateVerticalAxis(chart, graphs,
							  height, level);
		       
	drawVerticalGridAndLabels(g, (Graph)graphs.firstElement(),
				  getLevelTitle(graphs), 
				  verticalAxis, yoffset, width); 
	drawGraphs(g, graphs, chart, verticalAxis, yoffset, width, height);
	drawHorizontalGrid(g, chart, verticalAxis, yoffset);
    }

    // Calculates the title to put on the graph level
    private String getLevelTitle(Vector graphs) {

	Vector symbols = new Vector();
	String symbol;
	boolean found;

	Iterator symbolsIterator;
	Iterator iterator = graphs.iterator();

	while(iterator.hasNext()) {
	    symbol = ((Graph)iterator.next()).getName();

	    // add it if its not already in our list of symbols
	    symbolsIterator = symbols.iterator();
	   
	    found = false;

	    while(symbolsIterator.hasNext()) {

		// the same?
		if(symbol.compareTo((String)symbolsIterator.next()) == 0) {
		    found = true;
		    break;
		}
	    }

	    // Add to list
	    if(!found)
		symbols.add(symbol);

	}

	// Now convert list of symbols to string of comma separated
	// company names
	String title = new String("");
	String companyName = null;

	symbolsIterator = symbols.iterator();

	while(symbolsIterator.hasNext()) {
	    symbol = (String)symbolsIterator.next();

	    if(title.length() != 0) 
		title = title.concat(", ");

	    companyName = Quote.getSource().getCompanyName(symbol);

	    if(companyName != null)
		title = 
		    title.concat(companyName);
	}

	return title;
    }

    // Create a new vertical axis which is sized for the component
    private VerticalAxis calculateVerticalAxis(Chart chart, Vector graphs,
					       int height, int level) {

	Graph firstGraph = (Graph)graphs.firstElement();

	// Do we need to recalculate vertical axis vector?
	if(verticalAxes == null)
	    verticalAxes = new Vector();

	// Ensure vector is large enough to hold 'level' axes
	while(verticalAxes.size() <= level)
	    verticalAxes.add(null);

	// Recreate vertical axis if its null otherwise buffer
	VerticalAxis verticalAxis = 
	    (VerticalAxis)verticalAxes.elementAt(level);

	if(verticalAxis == null) {
	    verticalAxis = 
		new VerticalAxis(getLowestY(chart.getXRange(), graphs), 
				 getHighestY(chart.getXRange(), graphs),
				 firstGraph.getAcceptableMinorDeltas(),
				 firstGraph.getAcceptableMajorDeltas());
	    verticalAxes.setElementAt(verticalAxis, level);
	}

	// Fix height of vertical axis
	verticalAxis.setHeight(height);

	return verticalAxis;
    }

    // Draw all of the given graphs at this level onto the graph
    private void drawGraphs(Graphics g, Vector graphs, Chart chart,
			    VerticalAxis verticalAxis,
			    int yoffset, int width, int height) {

	Graph graph;
	Iterator iterator = graphs.iterator();

	// Draw vector of overlapping graphs
	while(iterator.hasNext()) {

	    graph = (Graph)iterator.next();

	    graph.render(g, getGraphColour(graph, chart), 0, yoffset + 
			 verticalAxis.getHeightOfGraph() +
			 (height-verticalAxis.getHeightOfGraph())/2,
			 horizontalScale, 
			 verticalAxis.getScale(),
			 verticalAxis.getBottomLineValue(), 
			 chart.getXRange());
	}
    }

    // Draw the horizontal grid onto the chart
    private void drawHorizontalGrid(Graphics g, Chart chart, 
				   VerticalAxis verticalAxis, int yoffset) {

	g.setColor(Color.lightGray);
 
	minorHorizontalAxis.drawGrid(g, yoffset + 
				     verticalAxis.getHeightOfGraph(),
				     horizontalScale,
				     verticalAxis.getHeightOfGraph());
	majorHorizontalAxis.drawGrid(g, yoffset + 
				     verticalAxis.getHeightOfGraph(),
				     horizontalScale,
				     verticalAxis.getHeightOfGraph());
    }

    // Draw the horizontal axis labels onto the chart
    private void drawHorizontalLabels(Graphics g, int height) {

	g.setColor(Color.lightGray);

	minorHorizontalAxis.drawLabels(g, horizontalScale, 
				       0, height - X_LABEL_HEIGHT +
				       g.getFontMetrics().getHeight());
	majorHorizontalAxis.drawLabels(g, horizontalScale, 
				       0, height - X_LABEL_HEIGHT +
				       g.getFontMetrics().getHeight()*2);

    }

    // Draws onto graph annotations such as "16/9 Buy" etc. These
    // can be from any graph on the chart.
    private void drawAnnotations(Graphics g, Chart chart) {

	// Iterate through all graphs and draw all their annotations
	Iterator iterator = chart.getLevels().iterator();
	Iterator innerIterator;
	Graph graph;
	HashMap annotations;
	VerticalAxis verticalAxis;
	int level = 0;
	int yoffset;

	while(iterator.hasNext()) {
	    innerIterator = ((Vector)iterator.next()).iterator();
	    yoffset = getStartOfLevel(level) + firstHorizontalLine;
	    verticalAxis = (VerticalAxis)verticalAxes.elementAt(level++); 

	    while(innerIterator.hasNext()) {
		graph = (Graph)innerIterator.next();
		annotations = graph.getAnnotations();

		// Draw this graph's annotations if it has any and 
		// its turned on
		if(annotations != null && chart.isAnnotated(graph))
		    drawGraphAnnotations(g, chart, graph, verticalAxis,
					 yoffset, annotations);

	    }
	}
    }	     

    // Draw all annotations in the given hashmap
    private void drawGraphAnnotations(Graphics g, Chart chart, Graph graph,
				      VerticalAxis verticalAxis, 
				      int yoffset, HashMap annotations) {

	Set xRange = annotations.keySet();
	Iterator iterator = xRange.iterator();
	Comparable x;
	String text;

	while(iterator.hasNext()) {

	    x = (Comparable)iterator.next();

	    // Only display annotation if its within the range displayed by the
	    // chart
	    if(x.compareTo(chart.getStartX()) >= 0 &&
	       x.compareTo(chart.getEndX()) <= 0) {
	  
		text = (String)annotations.get(x); // associated annotation

		// Insert X value into text field
		text = x.toString() + ": " + text;
		Float y = graph.getY(x);

		// Ignore annotations where the data source has no y value
		if(y != null) {

		    // Put y position near graph (assumes y graph is a line)
		    int yCoordinate = yoffset + 
			verticalAxis.getHeightOfGraph() - 
			GraphTools.
			scaleAndFitPoint(y.floatValue(),
					 verticalAxis.getBottomLineValue(), 
					 verticalAxis.getScale());

		    drawAnnotation(g, text, getXCoordinate(chart, x), 
				   yCoordinate);
		}
	    }
	}
    }

    // Draws at the given point a single annotation with the given text.
    private void drawAnnotation(Graphics g, String text, int x, int y) {

	int width = g.getFontMetrics().stringWidth(text) + 
	    2 * ANNOTATED_TEXT_MARGIN;
	int height = g.getFontMetrics().getHeight();

	g.setColor(Color.yellow);
	g.fillRect(x, y, width, height); 

	g.setColor(Color.black);
	g.drawString(text, x + ANNOTATED_TEXT_MARGIN, 
		     y + height - g.getFontMetrics().getDescent());
    }

    public boolean imageUpdate(Image image, int infofloags, int x, int y,
			       int width, int height) {
	return true;
    }

    // Find the lowest Y value for all the graphs in the given vector
    // over the given X range.
    private float getLowestY(Vector x, Vector graphs) {
	Iterator iterator = graphs.iterator();
	float y;
	float lowestY = Integer.MAX_VALUE;

	while(iterator.hasNext()) {
		
	    y = ((Graph)iterator.next()).getLowestY(x);

	    if(y < lowestY)
		lowestY = y;
	}
	return lowestY;
    }

    // Find the highest Y value for all the graphs in the given vector
    // over the given X range.
    private float getHighestY(Vector x, Vector graphs) {
	Iterator iterator = graphs.iterator();
	float y;
	float highestY = Integer.MIN_VALUE;
	
	while(iterator.hasNext()) {

	    y = ((Graph)iterator.next()).getHighestY(x);

	    if(y > highestY)
		highestY = y;

	}

	return highestY;
    }

    // Calculate the height of each graph level.
    private void calculateLevelHeights(Chart chart, int height) {

	// At the moment the top level is the primary level, whilst
	// the remaining are secondary - later on the user will be able
	// to change this
	height -= X_LABEL_HEIGHT;

	int levels = chart.getLevels().size();
	int primaryLevelHeight;
	int secondaryLevelHeight;
	int units = PRIMARY_HEIGHT_UNITS + (levels-1) * SECONDARY_HEIGHT_UNITS;
	int unitHeight = height / units;

	// 1. If the height of the secondary level is too small, make it
	// the minimum size
	if(unitHeight * SECONDARY_HEIGHT_UNITS < MINIMUM_LEVEL_HEIGHT)
	    secondaryLevelHeight = MINIMUM_LEVEL_HEIGHT;
	else
	    secondaryLevelHeight = unitHeight * SECONDARY_HEIGHT_UNITS;

	// 2. Primary height is the space left over
	primaryLevelHeight = height - secondaryLevelHeight * (levels-1);

	// 3. Create vector of height for each level
	levelHeights = new Vector();

	levelHeights.add(new Integer(primaryLevelHeight));

	while(--levels > 0)
	    levelHeights.add(new Integer(secondaryLevelHeight));
    }

    // Return the y value of the first horizontal line from the top of
    // this component.
    private int getFirstHorizontalLine(Graphics g) {
	return(firstHorizontalLine = (int)(TOP_GRAPH_BUFFER_MULTIPLIER *
					   g.getFontMetrics().getHeight()));
    }

    // Create a mapping between stock symbol and the colour we should
    // graph it. E.g. if CBA is charted in two different levels
    // they will both be in the same colour.
    private void calculateColourMap(Chart chart) {

	if(colourMap == null) {

	    // Create map between colour and each graph symbol, e.g so
	    // CBA would be one colour and WBC another
	    
	    // Colours of graphs in order of use 
	    Color[] colours = {Color.cyan.darker(), 
			       Color.blue.darker(),  
			       Color.magenta.darker(), Color.orange.darker(),
			       Color.pink.darker(),
			       						       
			       Color.blue, Color.magenta,
			       Color.orange, Color.pink, 

			       Color.cyan.darker().darker(), 
			       Color.blue.darker().darker(), 
			       Color.magenta.darker().darker(),
			       Color.orange.darker().darker(),
			       Color.pink.darker().darker(),

			       Color.cyan.brighter(), 
			       Color.blue.brighter(), 
			       Color.magenta.brighter(), 
			       Color.orange.brighter(),
			       Color.pink.brighter()};
	    
	    colourMap = new HashMap();
	    
	    // Iterate through all graphs and grab all sources
	    Iterator levelsIterator = chart.getLevels().iterator();
	    Iterator graphsIterator;
	    Graph graph;
	    String symbol;
	    int coloursUsed = 0;
	    
	    while(levelsIterator.hasNext()) {
		
		graphsIterator = ((Vector)levelsIterator.next()).iterator();
		
		while(graphsIterator.hasNext()) {
		    // Get symbol
		    graph = (Graph)graphsIterator.next();
		    symbol = graph.getName();
		    
		    // Add mapping between symbol and colour if it doesnt
		    // exist yet
		    if(colourMap.get(symbol) == null) {
			colourMap.put(symbol, colours[coloursUsed++]);
			
			// Re-use colours if necessary
			if(coloursUsed >= colours.length)
			    coloursUsed = 0;
		    }
		}
	    }
	}
    }

    /**
     * Retuen the colour we will draw the given graph. Note that the graph
     * may choose to override this colour.
     *
     * @param	graph	the graph to query
     * @param	chart	the chart component
     * @return	the colour the graph might be
     */
    public Color getGraphColour(Graph graph, Chart chart) {

	if(colourMap == null)
	    calculateColourMap(chart);

	Color colour = (Color)colourMap.get(graph.getName());
	    
	if(colour != null)
	    return colour;

	// If the colour map is missing a symbol then default
	// to dark grey (shouldnt happen)
	return Color.darkGray;
    }
}



