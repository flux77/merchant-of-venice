package org.mov.chart;

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.beans.*;
import java.text.*;
import java.util.*;
import javax.swing.*;

import org.mov.main.*;
import org.mov.util.*;

public class AnalyserChart extends JPanel implements AnalyserModule,
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

    public AnalyserChart(JDesktopPane desktop) {

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

    public void redraw() {
	chart.resetBuffer();
	chart.repaint();       
    }

    public void add(Graph graph, int index) {

	// Add graph to chart
	chart.add(graph, index);

	// Add menu for company
	Menu menu = new Menu(this, graph);
	menus.add(menu);
	menuBar.add(menu);
    }

    public void append(Graph graph, int index) {
	// Add graph to chart at given index, redraw chart but dont add it 
	// to menu as it is already there
	chart.add(graph, index);
    }

    public void append(Graph graph) {
	// Add graph to chart at new index, redraw chart but dont add it to 
	// menu as it is already there
	append(graph, chart.getLevels().size());
    }

    public void remove(Graph graph) {
	// Remove graph from chart, redraw chart and dont remove any
	// menus
	chart.remove(graph);
    }

    // Removes all graphs with the given symbol
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

    // Record whether the given graph should have its annotations
    // displayed or not
    public void handleAnnotation(Graph graph, boolean enabled) {
	chart.handleAnnotation(graph, enabled);
    }

    public String getTitle() {
	return chart.getTitle();
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener(listener);
    }

    public JComponent getComponent() {
	return this;
    }

    public void mouseClicked(MouseEvent e) { 
	chart.clearHighlightedRegion();
	zoomIn.setEnabled(zoomInEnabled = false);
    }
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}

    public void mousePressed(MouseEvent e) {
	TradingDate date = chart.getDateAtPoint(e.getX());

	if(date != null) 
	    chart.setHighlightedRegionStart(date);
    }

    public void mouseReleased(MouseEvent e) { }

    public void mouseDragged(MouseEvent e) {
	TradingDate date = chart.getDateAtPoint(e.getX());

	if(date != null) 
	    chart.setHighlightedRegionEnd(date);

	// can now zoom in!
	zoomIn.setEnabled(zoomInEnabled = true);
    }

    public void mouseMoved(MouseEvent e) {}

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
		firePropertyChange(AnalyserFrame.WINDOW_CLOSE_PROPERTY, 0, 1);
	}
	else if(e.getSource() == addMenuItem) {
	    SortedSet companies = 
		CommodityListQuery.getCommoditiesByCode(desktop, "Add Graph");
								
	}
	

    }

    public JMenuBar getJMenuBar() {
	return menuBar;
    }

    public ImageIcon getFrameIcon() {
	return new ImageIcon(ClassLoader.getSystemClassLoader().getResource("images/GraphIcon.gif"));
    }

    public boolean encloseInScrollPane() {
	return false;
    }

    public class Menu extends JMenu implements ActionListener {

	// Graphs
	private static final String DAY_HIGH       = "Day High";
	private static final String DAY_LOW        = "Day Low";
	private static final String DAY_OPEN       = "Day Open";
	private static final String HIGH_LOW_BAR   = "High Low Bar";
	private static final String MACD           = "MACD";
	private static final String MOVING_AVERAGE = "Moving Average";
	private static final String VOLUME         = "Volume";

	// HashMap of graphs to their annotations if any
	private HashMap graphMap = new HashMap();

	private JMenuItem removeMenu;
	
	private QuoteCache cache;
	private Graph graph;
	private AnalyserChart listener;
	private HashMap map = new HashMap();
	private HashMap annotateMap = new HashMap();

	public Menu(AnalyserChart listener, Graph graph) {
	    super(graph.getSymbol().toUpperCase());

	    // Order not important - will be added to menu in
	    // alphabetical order
	    graphMap.put((Object)DAY_HIGH,	null);
	    graphMap.put((Object)DAY_LOW,	null);
	    graphMap.put((Object)DAY_OPEN,	null);
	    graphMap.put((Object)HIGH_LOW_BAR,	null);
	    graphMap.put((Object)MACD,		"Buy/Sell");
	    graphMap.put((Object)MOVING_AVERAGE, "Buy/Sell");
	    graphMap.put((Object)VOLUME,	null);

	    cache = graph.getCache();    
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

	public String getSymbol() {
	    return graph.getSymbol();
	}

	public void actionPerformed(ActionEvent e) {

	    // Check static menus first
	    if(e.getSource() == removeMenu) {
		listener.removeAll(getSymbol());
		listener.redraw();
	    }

	    // Otherwise check dynamic menus
	    else {
		JCheckBoxMenuItem menu = (JCheckBoxMenuItem)e.getSource();
		String text = menu.getText();
		
		// Check annotation menus first
		if(handleAnnotationMenu(text, menu.getState()));
		    
		// Handle removing graphs next
		else if(!menu.getState())
		    removeGraph(text);

		// Ok looks like its adding a graph
		else if(text == DAY_HIGH)
		    addGraph(new LineGraph(new DayHighGraphDataSource(cache)),
			     DAY_HIGH, 0);

		else if(text == DAY_LOW)
		    addGraph(new LineGraph(new DayLowGraphDataSource(cache)),
			     DAY_LOW, 0);

		else if(text == DAY_OPEN)
		    addGraph(new LineGraph(new DayOpenGraphDataSource(cache)),
			     DAY_OPEN, 0);

		else if(text == HIGH_LOW_BAR)
		    addGraph(new HighLowBarGraph
			(
			 new DayLowGraphDataSource(cache),
			 new DayHighGraphDataSource(cache),
			 new DayCloseGraphDataSource(cache)
			     ),	HIGH_LOW_BAR, 0);
		
		else if(text == MACD)
		    // 1 1 2 3 5 8 13 21 34 55
		    addGraph(new MACDGraph(new DayCloseGraphDataSource(cache),
					   13, 34), MACD, 0);

		else if(text == MOVING_AVERAGE)
		    addGraph(new MovingAverageGraph(new 
			DayCloseGraphDataSource(cache), 40), 
			     MOVING_AVERAGE, 0);

		else if(text == VOLUME)
		    addGraph(new BarGraph(new DayVolumeGraphDataSource(cache)),
			     VOLUME);

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
}


