package org.mov.chart;

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.beans.*;
import java.text.*;
import java.util.*;
import javax.swing.*;

import org.mov.chart.graph.*;
import org.mov.chart.source.*;
import org.mov.main.*;
import org.mov.util.*;
import org.mov.parser.*;
import org.mov.quote.*;

/**
 * The charting module for venice. This class provides the user interface
 * used to draw any of the required charts.
 * Example:
 * <pre>
 *	QuoteCache cache = new QuoteCache(symbol);
 *	GraphSource dayClose = new OHLCVQuoteGraphSource(cache, Token.DAY_CLOSE_TOKEN);
 *	Graph graph = new LineGraph(dayClose);
 *
 *	ChartModule chart = new ChartModule(desktop);
 *	chart.add(graph, 0); 
 *	chart.redraw(); 
 *
 *	// Create a frame around the module and add to the desktop
 *	ModuleFrame frame = new ModuleFrame(chart, 0, 0, 400, 300);
 *	desktop.add(frame);
 * </pre>
 *
 * <h2>Structure</h2>
 *
 * The chart module is made up of three core classes. These core classes
 * are: <code>ChartModule</chart>, <code>Chart</code> & 
 * <code>BasicChartUI</code>.
 *
 * <p>
 * <ul>
 *
 * <li>
 * <code>BasicChartUI</chart>
 * <p>
 * This class provides the sizing and drawing code to draw graphs.
 * Given a set of graph levels it will arrange the graphs in the display,
 * calculate the size of each graph and for each graph level draw each
 * graph at that level. It will also create and manage the graph axes.
 * </li>

 * <li>
 * <code>Chart</chart>
 * <p>
 * This class is a new Swing widget which allows charting of graphs.
 * The actual code in this class is responsible for extending java swing's
 * <code>JComponent</code> class. It provides the code to allow the
 * user to select and unselect a portion of the chart and also
 * manages graph annotations via tooltips.
 * </li>
 *
 * <li>
 * <code>ChartModule</chart>
 * <p>
 * This class handles the integration of the chart module with <i>Venice</i>.
 * It is the container class of the actual chart, and is responsible for 
 * laying out the chart widget and the toolbar in a frame. It also
 * provides the menu.
 * </li>
 *
 * </ul>
 *
 * <p>
 *
 * <h2>Glossary</h2>
 *
 * The charting module uses a variety of phrases which have special meaning:
 * <p>
 * <dl compact>
 *
 * <dt><i>Annotations</i>
 * <dd>These appear on the graph as little yellow notes indicating
 * <i>Graph</i> specific information to the user. This information may
 * include buy/sell recommendations or any other data.
 *
 * <dt><i>Chart</i>
 * <dd>A chart represents the entire graphable area. The chart can 
 * consist of several <i>Graph Levels</i>, each graph level may contain
 * several <i>Graphs</i>.
 *
 * <dt><i>Graph</i>
 * <dd>A graph represents a specific type of graph to display, for example
 * a <i>Line Graph</i>, a <i>Bar Graph</i>, a <i>Moving Average Graph</i>
 * etc. These graphs can then be used to display different things to the
 * user, for example the <i>Bar Graph</i> can be used to graph a stock's
 * volume. A <i>Line Graph</i> can be used to graph a stock's day close.
 *
 * <dt><i>Graph Level</i>
 * <dd>For each chart there can be several <i>levels</i> of graph, these
 * levels are displayed vertical one on top of the other. The top level
 * may contain several stock's day close graphs. The bottom levels typically
 * contain indicators such as volume or RSI.
 *
 * <dt><i>Graph Source</i>
 * <dd>Contains useful information that <i>Graphs</i> need to know so they
 * can graph particular data (such as quote data). This information includes 
 * the values to be graphed, a title, the axis types to use and any 
 * <i>Annotations</i> to display for the graph.
 * </dl>
 *
 * @see Graph
 */

public class ChartModule extends JPanel implements Module,
						   MouseListener,
						   MouseMotionListener,
						   ActionListener
{
    // Constants
    private static int TOOLBAR_GRAPHIC_SIZE = 12;

    private PropertyChangeSupport propertySupport;
    private Chart chart;
    private JScrollPane scrollPane;
    private JMenuBar menuBar = new JMenuBar();

    // Function Toolbar 
    private JButton defaultZoom = null;
    private JButton zoomIn = null;    
    //    private JButton zoomOut = null;

    // Menus
    private JMenuItem addMenuItem = null;
    private JMenuItem closeMenuItem = null;

    // Vector containing menus for each symbol
    Vector menus = new Vector();

    // Enabled?
    private boolean defaultZoomEnabled = false;
    private boolean zoomInEnabled = false;

    private JDesktopPane desktop;

    /**
     * Create a new Chart.
     *
     * @param	desktop	the parent desktop.
     */
    public ChartModule(JDesktopPane desktop) {

	this.desktop = desktop;

	propertySupport = new PropertyChangeSupport(this);       

	chart = new Chart();
	chart.addMouseListener(this);
	chart.addMouseMotionListener(this);

	setLayout(new BorderLayout());

	addFunctionToolBar();

	// Add non-company specific menu for graph
	JMenu menu = new JMenu("Graph");
	addMenuItem = new JMenuItem("Add");
	addMenuItem.setAccelerator(KeyStroke.getKeyStroke('A',
				   java.awt.Event.CTRL_MASK, false));
	addMenuItem.addActionListener(this);
	menu.add(addMenuItem);
	menu.addSeparator();

	closeMenuItem = new JMenuItem("Close");
	closeMenuItem.setAccelerator(KeyStroke.getKeyStroke('C',
		  		     java.awt.Event.CTRL_MASK, false));
	closeMenuItem.addActionListener(this);
	menu.add(closeMenuItem);

	menuBar.add(menu);

	scrollPane = new JScrollPane(chart);
	
	add(scrollPane, BorderLayout.CENTER);
    }

    /*
    public void show() {
	System.out.println("show!");

	JScrollBar horizontal = scrollPane.getHorizontalScrollBar();
	System.out.println("maximum is " + horizontal.getMaximum());
	horizontal.setValue(horizontal.getMaximum());

	super.show();
    }
    */

    // Adds the toolbar that gives the user the options to zoom in and out
    // of the chart
    private void addFunctionToolBar() {
	JToolBar toolBar = new JToolBar(SwingConstants.VERTICAL);

	defaultZoom = new JButton(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("images/Zoom24.gif")));
	zoomIn = new JButton(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("images/ZoomIn24.gif")));
	//	zoomOut = new JButton(new ImageIcon("ZoomOut24.gif"));

	//	zoomIn.addActionListener(this);
	defaultZoom.addActionListener(this);
	zoomIn.addActionListener(this);

	defaultZoom.setEnabled(defaultZoomEnabled);
	zoomIn.setEnabled(zoomInEnabled);

	toolBar.add(defaultZoom);
	toolBar.add(zoomIn);
	//	toolBar.add(zoomOut);

	add(toolBar, BorderLayout.WEST);
    }

    /**
     * Redraw the current display.
     */
    public void redraw() {
	chart.resetBuffer();
	chart.repaint();       
    }

    /**
     * Add a new graph to the specified level. Add new menu for graph.
     *
     * @param	graph	the new graph to add
     * @param	level	graph level to add the new graph
     */
    public void add(Graph graph, QuoteCache cache, int level) {

	// Add graph to chart
	chart.add(graph, level);

	// Currently only support quotes - so add menu for quote
	QuoteChartMenu menu = new QuoteChartMenu(this, cache, graph);
	menus.add(menu);
	menuBar.add(menu);
    }

    /** 
     * Add the graph to the specified level. This is identical to
     * the add method except that it does not add a new menu for the
     * graph.
     *
     * @param	graph	The new graph to add
     * @param	level	graph level to add the new graph
     * @see	#add
     */
    public void append(Graph graph, int level) {
	// Add graph to chart to given level, redraw chart but dont add it 
	// to menu as it is already there
	chart.add(graph, level);
    }

    /**
     * Create a new level and add the graph. This is identical to the
     * add method except that it does not add a new menu for the
     * graph.
     *
     * @param	graph	the new graph to add.
     * @see	#add
     */
    public void append(Graph graph) {
	// Add graph at a new graph level, redraw chart but dont add graph to 
	// menu as it is already there
	append(graph, chart.getLevels().size());
    }

    /**
     * Remove the graph from the chart. Currently does not remove the
     * menu for the appropriate symbol. Probably should.
     *
     * @param	graph	the graph to remove.
     */
    public void remove(Graph graph) {
	// Remove graph from chart, redraw chart and dont remove any
	// menus
	chart.remove(graph);
    }

    /**
     * Remove all graphs with the given symbol from the chart. Or will
     * do when its implemented.
     *
     * @param symbol	The symbol of the graphs to remove
     */
    public void removeAll(String symbol) {

	/*
	// Remove graph
	chart.remove(graph);

	// Remove from menu bar
	Iterator iterator = menus.iterator();
	Menu menu;

	while(iterator.hasNext()) {
	    menu = (Menu)iterator.next();

	    if(menu.getSymbol().equals(graph.getSource().getSymbol())) {
		menuBar.remove(menu);
		break;
	    }		
	}
	*/

	redraw();
    }

    /** 
     * Record whether the given graph should have its annotations
     * displayed or not. Annotations are little popup text notes that
     * contain information about the graph, such as buy/sell suggestions. 
     *
     * @param	graph	the graph to change annotations for.
     * @param	enabled	set to true if the graph should handle annotations
     *			false otherwise.	
     */
    public void handleAnnotation(Graph graph, boolean enabled) {
	chart.handleAnnotation(graph, enabled);
    }

    /**
     * Return the window title.
     *
     * @return	the window title
     */
    public String getTitle() {
	return chart.getTitle();
    }

    /**
     * Add a property change listener for module change events.
     *
     * @param	listener	listener
     */
    public void addModuleChangeListener(PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(listener);
    }

    /**
     * Remove a property change listener for module change events.
     *
     * @param	listener	listener
     */
    public void removeModuleChangeListener(PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener(listener);
    }

    /**
     * Return displayed component for this module.
     *
     * @return the component to display.
     */
    public JComponent getComponent() {
	return this;
    }

    /**
     * Called when a mouse click event is received. 
     *
     * @param	e	mouse event
     */
    public void mouseClicked(MouseEvent e) { 
	chart.clearHighlightedRegion();
	zoomIn.setEnabled(zoomInEnabled = false);
    }

    /**
     * Called when a mouse enter event is received. 
     *
     * @param	e	mouse event
     */
    public void mouseEntered(MouseEvent e) {}

    /**
     * Called when a mouse exit event is received. 
     *
     * @param	e	mouse event
     */
    public void mouseExited(MouseEvent e) {}

    /**
     * Called when a mouse press event is received. 
     *
     * @param	e	mouse event
     */
    public void mousePressed(MouseEvent e) {
	TradingDate date = chart.getDateAtPoint(e.getX());

	if(date != null) 
	    chart.setHighlightedRegionStart(date);
    }

    /**
     * Called when a mouse release event is received. 
     *
     * @param	e	mouse event
     */
    public void mouseReleased(MouseEvent e) { }

    /**
     * Called when a mouse drag event is received. 
     *
     * @param	e	mouse event
     */
    public void mouseDragged(MouseEvent e) {
	TradingDate date = chart.getDateAtPoint(e.getX());

	if(date != null) 
	    chart.setHighlightedRegionEnd(date);

	// can now zoom in!
	zoomIn.setEnabled(zoomInEnabled = true);
    }

    /**
     * Called when a mouse move event is received. 
     *
     * @param	e	mouse event
     */
    public void mouseMoved(MouseEvent e) {}

    /**
     * Handle widget events.
     *
     * @param	e	action event
     */
    public void actionPerformed(ActionEvent e) {

	if(e.getSource() == zoomIn) {
	    chart.zoomToHighlightedRegion();
	    zoomIn.setEnabled(zoomInEnabled = false);
	    defaultZoom.setEnabled(defaultZoomEnabled = true);
	    // This tells the scrollpane to re-asses whether it needs
	    // the horizontal scrollbar now
	    scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	    repaint();
	}
	else if(e.getSource() == defaultZoom) {
	    chart.zoomToDefaultRegion();
	    defaultZoom.setEnabled(defaultZoomEnabled = false);
	    // This tells the scrollpane to re-asses whether it needs
	    // the horizontal scrollbar now
	    scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	    repaint();
	}
	else if(e.getSource() == closeMenuItem) {
	    propertySupport.
		firePropertyChange(ModuleFrame.WINDOW_CLOSE_PROPERTY, 0, 1);
	}
	else if(e.getSource() == addMenuItem) {
	    SortedSet companies = 
		CommodityListQuery.getCommoditiesByCode(desktop, "Add Graph");
								
	}
	

    }

    /**
     * Return menu bar for chart module.
     *
     * @return	the menu bar.
     */
    public JMenuBar getJMenuBar() {
	return menuBar;
    }

    /**
     * Return frame icon for chart module.
     *
     * @return	the frame icon
     */
    public ImageIcon getFrameIcon() {
	return new ImageIcon(ClassLoader.getSystemClassLoader().getResource("images/GraphIcon.gif"));
    }

    /**
     * Return whether the module should be enclosed in a scroll pane.
     *
     * @return	enclose module in scroll bar
     */
    public boolean encloseInScrollPane() {
	return false;
    }

    /**
     * Tell module to save any current state data / preferences data because
     * the window is being closed.
     */
    public void save() { }
}


