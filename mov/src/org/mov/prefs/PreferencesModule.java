package org.mov.prefs;

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.prefs.*;
import javax.swing.*;
import javax.swing.event.*;

import org.mov.main.ModuleFrame;
import org.mov.main.Module;

/**
 * The preferences module for venice. This class provides the user
 * interface to change any of the preferences settings. Preferences are
 * organised as a set of pages, each page is responsible for one group
 * of settings.
 * Example:
 * <pre>
 *      // Open a new preferences window displaying the quote source page
 *      PreferencesModule prefs = new PreferencesModule(desktop,
 *				      PreferencesModule.QUOTE_SOURCE_PAGE);
 * 
 *	// Create a frame around the module and add to the desktop
 *	ModuleFrame frame = new ModuleFrame(chart, 0, 0, 400, 300);
 *	desktop.add(frame);
 * </pre>
 *
 * @see PreferencesPage
 */

public class PreferencesModule extends JPanel
    implements Module, ActionListener {

    /**
     * Preferences page for retrieving stock quotes.
     */
    public static int QUOTE_SOURCE_PAGE = 0;

    private JDesktopPane desktop;
    private PropertyChangeSupport propertySupport;
    private PreferencesPage activePage;

    private JButton okButton;
    private JButton cancelButton;
    
    /**
     * Create a new Preference Module.
     *
     * @param	desktop	the parent desktop.
     * @param	initialPage	the initial page to display, so far we only
     *				support 
     */
    public PreferencesModule(JDesktopPane desktop, int initialPage) {

	this.desktop = desktop;
	propertySupport = new PropertyChangeSupport(this);       

	DefaultListModel fileListModel = new DefaultListModel();
	JList pageList = new JList();
	pageList.setModel(fileListModel);
	fileListModel.addElement((Object)new String("Quote Source"));
	pageList.setSelectedIndex(0);

	activePage = new QuoteSourcePage(desktop);

	//setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	setLayout(new BorderLayout());
	add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
			   pageList,
			   activePage.getComponent()),
	    BorderLayout.CENTER);

	JPanel buttonPanel = new JPanel();
	okButton = new JButton("OK");
	okButton.addActionListener(this);
	cancelButton = new JButton("Cancel");
	cancelButton.addActionListener(this);
	buttonPanel.add(okButton);
	buttonPanel.add(cancelButton);
  
	add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Called when the user clicks on the save or cancel button.
     *
     * @param	e	The event.
     */
    public void actionPerformed(ActionEvent e) {

	if(e.getSource() == okButton) {
	    // Save preference data - currently theres only one and its
	    // the active page - so save it.
	    activePage.save();

	    // flush changes to backing store
	    try {
		Preferences.userRoot().flush();
	    }
	    catch(BackingStoreException be) {
		// ignore it
	    }
	}

	// ok or cancel button closes window
	propertySupport.
	    firePropertyChange(ModuleFrame.WINDOW_CLOSE_PROPERTY, 0, 1);
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
     * Return menu bar for quote source preferences module.
     *
     * @return	the menu bar.
     */
    public JMenuBar getJMenuBar() {
	return null;
    }

    /**
     * Return frame icon for quote source preferences module.
     *
     * @return	the frame icon.
     */
    public ImageIcon getFrameIcon() {
	//	return new ImageIcon(ClassLoader.getSystemClassLoader().getResource("images/GraphIcon.gif"));
	return null;
    }

    /**
     * Returns the window title.
     *
     * @return	the window title.
     */
    public String getTitle() {
	return activePage.getTitle();
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
     * Called when window is closing. We handle the saving explicitly so
     * this is only called when the user clicks on the close button in the
     * top right hand of the window. Dont trigger a save event for this.
     *
     * @return	enclose module in scroll bar
     */
    public void save() {
	// Same as hitting cancel - do not save anything
    }
}
