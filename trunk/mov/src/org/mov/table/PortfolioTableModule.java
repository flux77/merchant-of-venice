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

package org.mov.table;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

import org.mov.main.*;
import org.mov.util.ExpressionQuery;
import org.mov.util.Locale;
import org.mov.util.TradingDate;
import org.mov.parser.*;
import org.mov.portfolio.Portfolio;
import org.mov.quote.*;
import org.mov.ui.*;


public class PortfolioTableModule extends AbstractTable
    implements Module, ActionListener {

    // Menu
    private JMenuBar menuBar;
    private JMenuItem tableClose;

    private PropertyChangeSupport propertySupport;
    private QuoteBundle quoteBundle;
    private Portfolio portfolio;
    private PortfolioModel model;

    // Frame Icon
    private String frameIcon = "org/mov/images/TableIcon.gif";

    public PortfolioTableModule(Portfolio portfolio, QuoteBundle quoteBundle) {
	
	this.quoteBundle = quoteBundle;
        this.portfolio = portfolio;

	propertySupport = new PropertyChangeSupport(this);

        model = new PortfolioModel(portfolio, quoteBundle);
	setModel(model, PortfolioModel.DATE_COLUMN, SORT_UP);
	model.addTableModelListener(this);
        showColumns(model);
        resort();

	addMenu();
    }

    // Create a menu
    private void addMenu() {
	menuBar = new JMenuBar();

        // Table Menu
        {
            JMenu tableMenu = MenuHelper.addMenu(menuBar, Locale.getString("TABLE"));

            // Show columns menu
            tableMenu.add(createShowColumnMenu(model));

            tableMenu.addSeparator();

            tableClose = MenuHelper.addMenuItem(this, tableMenu,
                                                Locale.getString("CLOSE"));	
        }
    }

    /**
     * Tell module to save any current state data / preferences data because
     * the window is being closed.
     */
    public void save() {
        // nothing to save to preferences
    }

    /**
     * Return the window title.
     *
     * @return	the window title
     */
    public String getTitle() {
	return Locale.getString("TABLE_OF", portfolio.getName());
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
	return new ImageIcon(ClassLoader.getSystemClassLoader().getResource(frameIcon));
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
	if(e.getSource() == tableClose) {
	    propertySupport.
		firePropertyChange(ModuleFrame.WINDOW_CLOSE_PROPERTY, 0, 1);
	}
	else
            assert false;
    }

}
