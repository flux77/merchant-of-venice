package org.mov.analyser;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

import org.mov.main.*;
import org.mov.util.*;
import org.mov.parser.*;
import org.mov.prefs.*;
import org.mov.portfolio.*;
import org.mov.quote.*;
import org.mov.ui.*;

public class PaperTradeModule extends JPanel implements Module,
							ActionListener {

    private PropertyChangeSupport propertySupport;
    
    private JDesktopPane desktop;

    private JTextField fromDateTextField;
    private JTextField toDateTextField;
    private JTextField symbolsTextField;
    private JTextField buyRuleTextField;
    private JTextField sellRuleTextField;
    private JTextField initialCapitalTextField;
    private JTextField tradeCostTextField;

    private JButton tradeButton;
    private JButton closeButton;

    // Single result table for entire application
    private static ModuleFrame resultsFrame = null;

    /**
     * Create a new paper trade module.
     *
     * @param	desktop	the current desktop
     */
    public PaperTradeModule(JDesktopPane desktop) {

	this.desktop = desktop;

	propertySupport = new PropertyChangeSupport(this);

	//	createMenu();
	layoutPaperTrade();

	// Load GUI settings from preferences
	load();
    }

    private void layoutPaperTrade() {

	setLayout(new BorderLayout());

	Box paperTradeOptions = Box.createVerticalBox();

	// Date panel
	{
	    TitledBorder dateTitled = new TitledBorder("Date Range");
	    JPanel datePanel = new JPanel();
	    datePanel.setBorder(dateTitled);

	    GridBagLayout gridbag = new GridBagLayout();
	    GridBagConstraints c = new GridBagConstraints();
	    datePanel.setLayout(gridbag);

	    c.weightx = 1.0;
	    c.ipadx = 5;
	    c.anchor = GridBagConstraints.WEST;

	    fromDateTextField = 
	    	addTextRow(datePanel, "From Date", "", gridbag, c, 15);
	    toDateTextField = 
		addTextRow(datePanel, "To Date", "", gridbag, c, 15);

	    paperTradeOptions.add(datePanel);
	}

	// Symbols Panel
	{
	    TitledBorder symbolTitled = new TitledBorder("Symbol");
	    JPanel symbolPanel = new JPanel();
	    symbolPanel.setBorder(symbolTitled);

	    GridBagLayout gridbag = new GridBagLayout();
	    GridBagConstraints c = new GridBagConstraints();
	    symbolPanel.setLayout(gridbag);

	    c.weightx = 1.0;
	    c.ipadx = 5;
	    c.anchor = GridBagConstraints.WEST;

	    symbolsTextField = 
		addTextRow(symbolPanel, "Symbols", "", gridbag, c, 15);

	    paperTradeOptions.add(symbolPanel);

	}

	// Equations Panel
	{
	    TitledBorder equationTitled = new TitledBorder("Buy/Sell Rules");
	    JPanel equationPanel = new JPanel();
	    equationPanel.setBorder(equationTitled);

	    GridBagLayout gridbag = new GridBagLayout();
	    GridBagConstraints c = new GridBagConstraints();
	    equationPanel.setLayout(gridbag);

	    c.weightx = 1.0;
	    c.ipadx = 5;
	    c.anchor = GridBagConstraints.WEST;

	    buyRuleTextField = 
		addTextRow(equationPanel, "Buy Rule", "", gridbag, c, 18);
	    sellRuleTextField = 
		addTextRow(equationPanel, "Sell Rule", "", gridbag, c, 18);
	    
	    paperTradeOptions.add(equationPanel);

	}

	// Portfolio Panel
	{
	    TitledBorder portfolioTitled = new TitledBorder("Portfolio");
	    JPanel portfolioPanel = new JPanel();
	    portfolioPanel.setBorder(portfolioTitled);

	    GridBagLayout gridbag = new GridBagLayout();
	    GridBagConstraints c = new GridBagConstraints();
	    portfolioPanel.setLayout(gridbag);

	    c.weightx = 1.0;
	    c.ipadx = 5;
	    c.anchor = GridBagConstraints.WEST;

	    initialCapitalTextField = 
		addTextRow(portfolioPanel, "Initial Capital", "", gridbag, c, 
			   15);
	    tradeCostTextField =
		addTextRow(portfolioPanel, "Trade Cost", "", gridbag, c, 15);
	    
	    paperTradeOptions.add(portfolioPanel);
	}

	add(paperTradeOptions, BorderLayout.CENTER);

	// Paper trade, close buttons
	JPanel buttonPanel = new JPanel();
	tradeButton = new JButton("Paper Trade");
	tradeButton.addActionListener(this);
	closeButton = new JButton("Close");
	closeButton.addActionListener(this);
	buttonPanel.add(tradeButton);
	buttonPanel.add(closeButton);

	add(buttonPanel, BorderLayout.SOUTH);
    }

    // Helper method which adds a new text field in a new row to the given 
    // grid bag layout.
    private JTextField addTextRow(JPanel panel, String field, String value,
				  GridBagLayout gridbag,
				  GridBagConstraints c,
				  int length) {
	JLabel label = new JLabel(field);
	c.gridwidth = 1;
	gridbag.setConstraints(label, c);
	panel.add(label);

	JTextField text = new JTextField(value, length);
	c.gridwidth = GridBagConstraints.REMAINDER;
	gridbag.setConstraints(text, c);
	panel.add(text);

	return text;
    }

    public void load() {
	// Load last GUI settings from preferences
	HashMap settings = PreferencesManager.loadLastPaperTradeSettings();

	Iterator iterator = settings.keySet().iterator();

	while(iterator.hasNext()) {
	    String setting = (String)iterator.next();
	    String value = (String)settings.get((Object)setting);

	    if(setting.equals("from_date"))
		fromDateTextField.setText(value);
	    else if(setting.equals("to_date"))
		toDateTextField.setText(value);

	    else if(setting.equals("symbols"))
		symbolsTextField.setText(value);
	    else if(setting.equals("buy_rule"))
		buyRuleTextField.setText(value);
	    else if(setting.equals("sell_rule"))
		sellRuleTextField.setText(value);
	    else if(setting.equals("initial_capital"))
		initialCapitalTextField.setText(value);
	    else if(setting.equals("trade_cost"))
		tradeCostTextField.setText(value);
	}
    }

    public void save() {
	// Save last GUI settings to preferences
	HashMap settings = new HashMap();

	settings.put("from_date", fromDateTextField.getText());
	settings.put("to_date", toDateTextField.getText());
	settings.put("symbols", symbolsTextField.getText());
	settings.put("buy_rule", buyRuleTextField.getText());
	settings.put("sell_rule", sellRuleTextField.getText());
	settings.put("initial_capital", initialCapitalTextField.getText());
	settings.put("trade_cost", tradeCostTextField.getText());

	PreferencesManager.saveLastPaperTradeSettings(settings);
    }

    public String getTitle() {
	return "Paper Trade";
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
     * Return frame icon for table module.
     *
     * @return	the frame icon.
     */
    public ImageIcon getFrameIcon() {
	return new ImageIcon(ClassLoader.getSystemClassLoader().getResource("images/TableIcon.gif"));
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
     * Return menu bar for chart module.
     *
     * @return	the menu bar.
     */
    public JMenuBar getJMenuBar() {
	//	return menuBar;
	return null;
    }

    /**
     * Return whether the module should be enclosed in a scroll pane.
     *
     * @return	enclose module in scroll bar
     */
    public boolean encloseInScrollPane() {
	return true;
    }

    /**
     * Handle widget events.
     *
     * @param	e	action event
     */
    public void actionPerformed(final ActionEvent e) {

	if(e.getSource() == closeButton) {
	    // Tell frame we want to close
	    propertySupport.
		firePropertyChange(ModuleFrame.WINDOW_CLOSE_PROPERTY, 0, 1);
	}
	else if(e.getSource() == tradeButton) {
	    Thread t = new Thread(new Runnable() {
		    public void run() {
			paperTrade();
		    }
		});

	    t.start();
	}

    }
    
    private void paperTrade() {

	// 
	// Extract data from GUI
	//

	TradingDate fromDate = new TradingDate(fromDateTextField.getText(),
					       TradingDate.BRITISH);
	TradingDate toDate = new TradingDate(toDateTextField.getText(),
					     TradingDate.BRITISH);
	SortedSet symbols = 
	    Converter.stringToSortedSet(symbolsTextField.getText());

	Parser parser = new Parser();
	Expression buyRule = null;
	Expression sellRule = null;

	try {
	    buyRule = parser.parse(buyRuleTextField.getText());
	}
	catch(ExpressionException e) {	   
	    buildPaperTradeError("Error parsing buy rule: " +
				 e.getReason());
	    return;
	}

	try {
	    sellRule = parser.parse(sellRuleTextField.getText());
	}
	catch(ExpressionException e) {
	    buildPaperTradeError("Error parsing sell rule: " +
				 e.getReason());
	    return;
	}

	float initialCapital = 0.0F;
	float tradeCost = 0.0F;

	try {
	    if(!initialCapitalTextField.getText().equals(""))
		initialCapital = 
		    Float.parseFloat(initialCapitalTextField.getText());
	    	   
	    if(!tradeCostTextField.getText().equals(""))
		tradeCost = 
		    Float.parseFloat(tradeCostTextField.getText());
	}

	//
	// Validate data
	//

	catch(NumberFormatException e) {
	    buildPaperTradeError("Can't parse number '" +
				 e.getMessage() + "'");
	    return;
	}

	if(initialCapital <= 0) {
	    buildPaperTradeError("Cannot trade without some initial capital");
	    return;
	}

	if(fromDate.getYear() == 0 || toDate.getYear() == 0 ||
	   fromDate.after(toDate)) {
	    buildPaperTradeError("Invalid date range");
	    return;
	}

	if(symbols.size() == 0) {
	    buildPaperTradeError("Need to specify a commodity to trade");
	    return;
	}
	else {
	    String symbol = (String)symbols.first();

	    // Check company exists
	    if(!QuoteSourceManager.getSource().symbolExists(symbol)) {
		buildPaperTradeError("No data available for commodity: " +
				     symbol);
				     
		return;
	    }

	}

	//
	// Trade
	//

	final Thread thread = Thread.currentThread();
	ProgressDialog progress = 
	    ProgressDialogManager.getProgressDialog();
	progress.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    thread.interrupt();
		}
	    });

	Portfolio portfolio = null;
	    
	try {
	    progress.setTitle("Loading quotes for paper trade");
	    progress.show();
		
	    QuoteCache cache = new QuoteCache((String)symbols.first());

            if (!thread.isInterrupted()) {	       

		// Skip non trading days. Non trading days wont be in
		// the cache and will have positive offsets.
		while(cache.dateToOffset(fromDate) > 0 &&
		      fromDate.before(toDate))
		    fromDate = fromDate.next(1);

		while(cache.dateToOffset(toDate) > 0 &&
		      toDate.after(fromDate))
		    toDate = toDate.previous(1);

		// This error will happen if the entire date range does
		// not cover a single trading day
		if(cache.dateToOffset(fromDate) > 0 ||
		   cache.dateToOffset(toDate) > 0) {
		    buildPaperTradeError("Invalid date range");
		    return;
		}

		String symbol = (String)symbols.first();
		symbol = symbol.toLowerCase();

		portfolio = 
		    PaperTrade.paperTrade("Paper Trade of " + 
					  symbol.toUpperCase(),
					  cache,
					  symbol,
					  fromDate,
					  toDate,
					  buyRule,
					  sellRule,
					  initialCapital,
					  tradeCost);
	    }

	    if (!Thread.currentThread().interrupted()) {
		ProgressDialogManager.closeProgressDialog();

		// Dispaly results table if its not already up (or if it
		// was closed we need to create a new one)
		if(resultsFrame == null || resultsFrame.isClosed()) {
		    resultsFrame = 
			CommandManager.getInstance().newPaperTradeResultTable();
		}

		// Send result to result table for display
		PaperTradeResultModule resultsModule = 
		    (PaperTradeResultModule)resultsFrame.getModule();

		resultsModule.addResult(portfolio, cache, initialCapital,
					tradeCost,
					buyRule.toString(), 
					sellRule.toString(), 
					fromDate, toDate);
	    }

	} catch (Exception e) {
	    ProgressDialogManager.closeProgressDialog();
	}
    }


    private void buildPaperTradeError(String message) {
	JOptionPane.showInternalMessageDialog(desktop, 
					      message,
					      "Error building Paper Trade",
					      JOptionPane.ERROR_MESSAGE);
    }

}
