package org.mov.analyser;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

import org.mov.main.*;
import org.mov.util.*;
import org.mov.parser.*;
import org.mov.portfolio.*;
import org.mov.quote.*;
import org.mov.table.*;
import org.mov.ui.*;

public class PaperTradeResultModule extends AbstractTable 
    implements Module,
	       ActionListener {
    
    private PropertyChangeSupport propertySupport;

    private static final int START_DATE_COLUMN = 0;
    private static final int END_DATE_COLUMN = 1;
    private static final int SYMBOLS_COLUMN = 2;
    private static final int BUY_RULE_COLUMN = 3;
    private static final int SELL_RULE_COLUMN = 4;
    private static final int TRADE_COST_COLUMN = 5;
    private static final int INITIAL_CAPITAL_COLUMN = 6;
    private static final int FINAL_CAPITAL_COLUMN = 7;
    private static final int PERCENT_RETURN_COLUMN = 8;

    private Model model;

    // Menu
    private JMenuBar menuBar;
    private JCheckBoxMenuItem showStartDateColumn;
    private JCheckBoxMenuItem showEndDateColumn;
    private JCheckBoxMenuItem showSymbolsColumn;
    private JCheckBoxMenuItem showBuyRuleColumn;
    private JCheckBoxMenuItem showSellRuleColumn;
    private JCheckBoxMenuItem showTradeCostColumn;
    private JCheckBoxMenuItem showInitialCapitalColumn;
    private JCheckBoxMenuItem showFinalCapitalColumn;
    private JCheckBoxMenuItem showReturnColumn;

    private JMenuItem resultClose;

    class PaperTradeResult {
	public Portfolio portfolio;
	public QuoteCache cache;
	public float initialCapital;
	public float tradeCost;
	public String buyRule;
	public String sellRule;
	public TradingDate startDate;	
	public TradingDate endDate;

	public PaperTradeResult(Portfolio portfolio, QuoteCache cache,
				float initialCapital, float tradeCost,
				String buyRule, String sellRule,
				TradingDate startDate,
				TradingDate endDate) {
	    this.portfolio = portfolio;
	    this.cache = cache;
	    this.initialCapital = initialCapital;
	    this.tradeCost = tradeCost;
	    this.buyRule = buyRule;
	    this.sellRule = sellRule;
	    this.startDate = startDate;
	    this.endDate = endDate;
	}
    }

    class Model extends AbstractTableModel {
	private String[] headers = {
	    "Start Date", "End Date", "Symbols", "Buy Rule", "Sell Rule",
	    "Trade Cost", "Initial Capital", "Final Capital", "Return"};
	    
	private Class[] columnClasses = {
	    TradingDate.class, TradingDate.class, String.class, String.class,
	    String.class, Float.class, Float.class, Float.class, Change.class};
	
	private Vector results;

	public Model() {
	    results = new Vector();
	}

	public PaperTradeResult getPaperTradeResult(int row) {
	    return (PaperTradeResult)results.elementAt(row);
	}

	public void addResult(Portfolio portfolio, QuoteCache cache,
			      float initialCapital, float tradeCost,
			      String buyRule, String sellRule,
			      TradingDate startDate,
			      TradingDate endDate) {
	    results.add((Object)new PaperTradeResult(portfolio, cache,
						     initialCapital, tradeCost,
						     buyRule, sellRule,
						     startDate, endDate));

	    // Notify table that we've appended a row at the end
	    fireTableRowsInserted(results.size() - 1,
	    			  results.size() - 1);
	}
	
	public int getRowCount() {
	    return results.size();
	}

	public int getColumnCount() {
	    return headers.length;
	}
	
	public String getColumnName(int c) {
	    return headers[c];
	}

	public Class getColumnClass(int c) {
	    return columnClasses[c];
	}

	// Calculate the final value of the portfolio 
	private float finalPortfolioValue(Portfolio portfolio, 
					  QuoteCache cache,
					  TradingDate startDate,
					  TradingDate endDate) {
	    boolean calculatedEndValue = false;
	    float endValue = 0.0F;

	    // We have to do a loop here because the last day in the
	    // paper trade may be a public holiday where we have no
	    // data for and thus can't calculate the end value...
	    while(!calculatedEndValue && endDate.after(startDate)) {
		try {
		    endValue = 
			portfolio.getValue(cache, 
					   endDate);
		    calculatedEndValue = true;
		}
		catch(EvaluationException e) {
		    endDate = endDate.previous(1);
		}
	    }
	    
	    return endValue;
	}

	public Object getValueAt(int row, int column) {
	    if(row >= getRowCount()) 
		return "";

	    PaperTradeResult result = 
		(PaperTradeResult)results.elementAt(row);

	    if(column == START_DATE_COLUMN) {
		return result.startDate;
	    }

	    else if(column == END_DATE_COLUMN) {
		return result.endDate;
	    }

	    else if(column == SYMBOLS_COLUMN) {
		Vector symbolsTraded = result.portfolio.getSymbolsTraded();

		String string = null;
		Iterator iterator = symbolsTraded.iterator();
		while(iterator.hasNext()) {
		    String symbol = (String)iterator.next();
		    symbol = symbol.toUpperCase();

		    if(string == null) {
			string = new String(symbol);
		    }
		    else {
			string = string.concat(", " + symbol);
		    }
		}

		return string;
	    }
	    
	    else if(column == BUY_RULE_COLUMN) {
		return result.buyRule;
	    }

	    else if(column == SELL_RULE_COLUMN) {
		return result.sellRule;
	    }
	    
	    else if(column == TRADE_COST_COLUMN) {
		return new Float(result.tradeCost);
	    }

	    else if(column == FINAL_CAPITAL_COLUMN) {
		return new Float(finalPortfolioValue(result.portfolio,
						     result.cache,
						     result.startDate,
						     result.endDate));
	    }

	    else if(column == INITIAL_CAPITAL_COLUMN) {
		return new Float(result.initialCapital);
	    }

	    else if(column == PERCENT_RETURN_COLUMN) {
		float startValue = result.initialCapital;
		float endValue = finalPortfolioValue(result.portfolio,
						     result.cache,
						     result.startDate,
						     result.endDate);

		return Converter.changeToChange(startValue, endValue);
	    }

	    return "";
	}
    }

    public PaperTradeResultModule() {
	model = new Model();
	setModel(model);

	model.addTableModelListener(this);

	propertySupport = new PropertyChangeSupport(this);

	addMenu();

	// If the user double clicks on a cell then graph the portfolio
	addMouseListener(new MouseAdapter() {
		
		public void mouseClicked(MouseEvent evt) {
		    
		    Point point = evt.getPoint();
		    if (evt.getClickCount() == 2) {
			
			// This will take care of the issue of if the table
			// is sorted by a different column. It'll return
			// the row number as so it wasnt sorted
			int row = getSelectedRow();
			
			// Get portfolio at row
			PaperTradeResult result = 
			    model.getPaperTradeResult(row);
			    
			CommandManager.getInstance().graphPortfolio(result.portfolio,
								    result.cache,
								    result.startDate,
								    result.endDate);
		    }
		}
	    });
    }

    // Construct menu for this frame
    private void addMenu() {
	menuBar = new JMenuBar();

	JMenu resultMenu = MenuHelper.addMenu(menuBar, "Result");

	JMenu columnMenu = 
	    MenuHelper.addMenu(resultMenu, "Show Columns");
	{
	    showStartDateColumn = 
		MenuHelper.addCheckBoxMenuItem(this, columnMenu,
					       "Start Date");
	    showEndDateColumn = 
		MenuHelper.addCheckBoxMenuItem(this, columnMenu,
					       "End Date");
	    showSymbolsColumn = 
		MenuHelper.addCheckBoxMenuItem(this, columnMenu,
					       "Symbols");
	    showBuyRuleColumn = 
		MenuHelper.addCheckBoxMenuItem(this, columnMenu,
					       "Buy Rule");
	    showSellRuleColumn = 
		MenuHelper.addCheckBoxMenuItem(this, columnMenu,
					       "Sell Rule");
	    showTradeCostColumn = 
		MenuHelper.addCheckBoxMenuItem(this, columnMenu,
					       "Trade Cost");
	    showInitialCapitalColumn = 
		MenuHelper.addCheckBoxMenuItem(this, columnMenu,
					       "Initial Capital");
	    showFinalCapitalColumn = 
		MenuHelper.addCheckBoxMenuItem(this, columnMenu,
					       "Final Capital");
	    showReturnColumn = 
		MenuHelper.addCheckBoxMenuItem(this, columnMenu,
					       "Return");

	    // Set menu items to default configuration
	    showStartDateColumn.setState(true);
	    showEndDateColumn.setState(true);
	    showSymbolsColumn.setState(true);
	    showBuyRuleColumn.setState(true);
	    showSellRuleColumn.setState(true);
	    showInitialCapitalColumn.setState(true);
	    showReturnColumn.setState(true);

	    // Tell table model not to show these columns - by default
	    // they will all be visible
	    showColumn(TRADE_COST_COLUMN, false);
	    showColumn(FINAL_CAPITAL_COLUMN, false);
	}

	resultMenu.addSeparator();

	resultClose = MenuHelper.addMenuItem(this, resultMenu,
					     "Close");
    }

    public void addResult(Portfolio portfolio, QuoteCache cache,
			  float initialCapital, float tradeCost, 
			  String buyRule, String sellRule,
			  TradingDate startDate, TradingDate endDate) {
	model.addResult(portfolio, cache, initialCapital, tradeCost, 
			buyRule, sellRule, startDate, endDate);

	validate();
	repaint();
    }

    public void save() {

    }

    public String getTitle() {
	return "Paper Trade Results";
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
	return menuBar;
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

	if(e.getSource() == resultClose) {
	    propertySupport.
		firePropertyChange(ModuleFrame.WINDOW_CLOSE_PROPERTY, 0, 1);
	}

	else {
	    // Otherwise its a checkbox menu item
	    JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem)e.getSource();
	    boolean state = menuItem.getState();
	    int column = START_DATE_COLUMN;

	    if(menuItem == showStartDateColumn) 
		column = START_DATE_COLUMN;

	    else if(menuItem == showEndDateColumn) 
		column = END_DATE_COLUMN;

	    else if(menuItem == showSymbolsColumn) 
		column = SYMBOLS_COLUMN;

	    else if(menuItem == showBuyRuleColumn) 
		column = BUY_RULE_COLUMN;

	    else if(menuItem == showSellRuleColumn) 
		column = SELL_RULE_COLUMN;

	    else if(menuItem == showTradeCostColumn) 
		column = TRADE_COST_COLUMN;

	    else if(menuItem == showInitialCapitalColumn) 
		column = INITIAL_CAPITAL_COLUMN;

	    else if(menuItem == showFinalCapitalColumn) 
		column = FINAL_CAPITAL_COLUMN;

	    else if(menuItem == showReturnColumn) 
		column = PERCENT_RETURN_COLUMN;

	    showColumn(column, state);
	}

    }
}
