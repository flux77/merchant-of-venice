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


/**
 * This class represents Transaction Module data  which can restore modules upon restart. 
 * 
 * @author Mark Hummel
 * @see PreferencesManager
 * @see SettingsWriter
 * @see SettingsReader 
 * @see Settings 
*/

import javax.swing.JDesktopPane;
import java.util.*;

import org.mov.main.Module;
import org.mov.main.ModuleFrame;
import org.mov.main.CommandManager;
import org.mov.portfolio.PortfolioModule;
import org.mov.portfolio.Portfolio;
import org.mov.portfolio.TransactionModule;
import org.mov.portfolio.Transaction;

import org.mov.quote.QuoteBundle;
import org.mov.quote.EODQuoteRange;
import org.mov.quote.EODQuoteBundle;
import org.mov.quote.QuoteSourceManager;
import org.mov.util.TradingDate;
import org.mov.util.Money;

import org.mov.ui.AbstractTableModel;
import org.mov.ui.Column;
import org.mov.ui.FrameRegister;

import org.mov.prefs.PreferencesManager;
import org.mov.prefs.PreferencesException;

public class TransactionModuleSettings extends AbstractSettings {
    
    private String portfolioName;

    /**
     * 
     * TransactioModuleSettings default constructor
     */

    public TransactionModuleSettings() {
	super(Settings.PORTFOLIO, Settings.TRANSACTIONMODULE);
    }

    /**
     * 
     * Construct a TransactionModuleSettings with title as key
     *
     * @param title  The title of a TransactionModule
     */

    public TransactionModuleSettings(String title) {
	super(Settings.PORTFOLIO, Settings.TRANSACTIONMODULE);
	super.setTitle(title);
	portfolioName = title;
    }

    /**
     * 
     * Set the name of the Portfolio that the Transactions operated on
     * 
     * @param portfolioName  The name of the portfolio
     */

    public void setPortfolioName(String portfolioName) {
	this.portfolioName = portfolioName;
    }

    /**
     * 
     * Get the name of the portfolio that the TransactionModule is attached to
     * 
     * @return  The name of the portfolio
     */

    public String getPortfolioName() {
	return portfolioName;
    }
    
    /**
     * 
     * Return a TransactionModule based on these settings
     * 
     * @param  desktop  The Venice desktop
     * @return  A TransactionModule
     */

    public Module getModule(JDesktopPane desktop) {	
	PortfolioModule portfolioModule;
	
	//Determine if there is currently a portfolioModule frame for 
	//this transaction window
	FrameRegister register = CommandManager.getInstance().getDesktopManager().getFrameRegister();
	
	ModuleFrame frame = register.getFrameOfType("org.mov.portfolio.PortfolioModule");
	if (frame != null) {	    
	    portfolioModule = (PortfolioModule)frame.getModule();	    
	    return portfolioModule;
	} 
	
	try {
	    Portfolio portfolio = PreferencesManager.getPortfolio(portfolioName);
	    
	    TradingDate lastDate = QuoteSourceManager.getSource().getLastDate();
	    
	    if (lastDate != null) {	
		
		EODQuoteRange quoteRange = 
		    new EODQuoteRange(portfolio.getStocksHeld(),
				      lastDate.previous(1),
				      lastDate);
		
		EODQuoteBundle quoteBundle = new EODQuoteBundle(quoteRange);
		
		PortfolioModuleSettings pms = new PortfolioModuleSettings();
				
		pms.setQuoteBundle(quoteBundle);
		pms.setPortfolio(portfolio);
		
		portfolioModule = new PortfolioModule(desktop, pms);

		return new TransactionModule(portfolioModule, portfolio);
	    }	    	    
	} catch (PreferencesException pfe) {
	}	
	return null;
    }
	


}