package org.mov.util;

import java.awt.*;
import java.text.*;
import java.util.*;
import javax.swing.*;

import org.mov.quote.*;
import org.mov.ui.TextDialog;

/**
 * A set of dialogs used for querying the user for commodities either
 * by name or symbol.
 */
public class CommodityListQuery {

    private CommodityListQuery() {
	// Cannot instantiate this class
    }

    /**
     * Open a new <code>CommodityListQuery</code> dialog. Ask the user
     * to enter a partial name of a company and return the appropriate
     * symbol.
     *
     * @param	parent	the parent desktop
     * @param	title	the title of the dialog
     * @return	a sorted set containing a single commodity string or
     * <code>null</code> if the user cancelled the dialog
     */
    public static SortedSet getCommodityByName(JDesktopPane parent, 
					       String title) {
	SortedSet companySet;
	String company;
	boolean invalidResponse;

	do {
	    companySet = null;
	    company = "";
	    invalidResponse = false; // assume user does OK

	    // First prompt user for list
	    TextDialog dlg = new TextDialog(parent, 
					    "Please enter commodity name",
					    "Graphing company share price");
	    company = dlg.showDialog();
	    
	    // Parse what the user inputed
	    if(company != null) {
		String symbol = 
		    Quote.getSource().getCompanySymbol(company);
		
		// Not recognised?
		if(symbol == null) {
		    String noData = 
			"No match for '" + company + "'";

		    JOptionPane.
			showInternalMessageDialog(parent, noData, 
						  "Unknown company",
						  JOptionPane.ERROR_MESSAGE);
		    invalidResponse = true;
		}

		// Recognised! Build company set
		else {
		    companySet = new TreeSet();
		    companySet.add(symbol);
		}
	    }

	    // Keep going while user hasnt entered a valid company and
	    // is selecting "ok"
	} while(invalidResponse); 

	// Return either null for no company selected or a set of one
	if(companySet != null && companySet.size() == 0)
	    return null;
	
	return companySet;
    }

    /**
     * Open a new <code>CommodityListQuery</code> dialog. Ask the user
     * to enter a list of company symbols. It will test to make each is
     * a valid symbol.
     *
     * @param	parent	the parent desktop
     * @param	title	the title of the dialog
     * @return	a sorted set containing at least one company symbol string or
     * <code>null</code> if the user cancelled the dialog
     */
    public static SortedSet getCommoditiesByCode(JDesktopPane parent, 
						 String title) {
	SortedSet companySet;
	String symbol;
	String companies;
	String unknownCompanies;
	boolean invalidResponse;

	do {
	    companySet = null;
	    companies = "";
	    unknownCompanies = "";
	    invalidResponse = false; // assume user does OK

	    // First prompt user for list
	    TextDialog dlg = new TextDialog(parent, 
					    "Please enter commodity symbols",
					    "Graphing symbol(s)");
	    companies = dlg.showDialog();
					    

	    // Parse what the user inputed
	    if(companies != null) {
		
		// Convert string to sorted set
		companySet = stringToSortedSet(companies);
		Iterator iterator = companySet.iterator();
		
		while(iterator.hasNext()) {
		    symbol = (String)iterator.next();
		    
		    // See if company exists
		    if(!Quote.getSource().symbolExists(symbol)) {
			
			// Add to list of companies we don't know
			if(unknownCompanies.length() > 0)
			    unknownCompanies = unknownCompanies.concat(" ");
			
			unknownCompanies = unknownCompanies.concat(symbol);
			
			// Remove company from set of valid companies the user
			// has entered
			iterator.remove();
		    }
		}
	    	
		// If there was any unknowno companies put up a message dialog
		// telling the user which ones were unknown
		if(unknownCompanies.length() > 0) {
		    String noData = 
			"No data available for companies '" + 
			unknownCompanies + 
			"'";
		    
		    JOptionPane.
			showInternalMessageDialog(parent, noData, 
						  "Unknown companies",
						  JOptionPane.ERROR_MESSAGE);
		    // Invalid if user entered illegal company(s) but no
		    // legal ones
		    if(companySet.size() == 0)
			invalidResponse = true;
		}
	    }

	    // Keep going while user hasnt entered a valid company and
	    // is selecting "ok"
	} while(invalidResponse); 

	// If the set is empty return a null pointer
	if(companySet != null && companySet.size() == 0)
	    return null;

	return companySet;
    }

    // Convert space separated list into vector of compay symbols
    // e.g "CBA WBC TLS" -> [CBA, TLS, WBC]. This should probably be
    // in the Converter class.
    private static SortedSet stringToSortedSet(String string) {
	int space;
	Vector vector = new Vector();
	boolean endOfString = false;
	
	while(!endOfString) {
	    space = string.indexOf(" ");
	    
	    if(space == -1) {
		vector.add(string);
		endOfString = true;
	    }
	    else {
		vector.add(new String(string.substring(0, space)));
		string = string.substring(space+1);
	    }
	}
	
	// Sort vector
	TreeSet sortedSet = new TreeSet(Collator.getInstance());
	sortedSet.addAll(vector);
	
	return sortedSet;
    }
}

