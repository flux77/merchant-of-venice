package org.mov.prefs;

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.beans.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

import org.mov.main.AnalyserModule;
import org.mov.util.DatabaseLookup;

public class QuoteSourcePreferences extends JPanel implements AnalyserModule
{

    private JDesktopPane desktop;
    private PropertyChangeSupport propertySupport;
    private DatabaseLookup db = DatabaseLookup.getInstance();

    /**
     * Create a new Quote Source Preferences view.
     *
     * @param	desktop	the parent desktop.
     */
    public QuoteSourcePreferences(JDesktopPane desktop) {

	this.desktop = desktop;

	propertySupport = new PropertyChangeSupport(this);       

	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

	// Tab Pane
	JTabbedPane pane = new JTabbedPane(JTabbedPane.TOP);

	// Database Pane
	{
	    JCheckBox useDatabase = new JCheckBox("Use Database", true);
	    
	    TitledBorder titled = new TitledBorder("Database Preferences");
	    JPanel databasePreferences = new JPanel();
	    databasePreferences.setBorder(titled);
	    databasePreferences.setLayout(new BorderLayout()); 
	    JPanel borderPanel = new JPanel();

	    GridBagLayout gridbag = new GridBagLayout();
	    GridBagConstraints c = new GridBagConstraints();
	    borderPanel.setLayout(gridbag);
	    
	    c.weightx = 1.0;
	    c.ipadx = 5;
	    c.anchor = GridBagConstraints.WEST;
	    
	    // Database 
	    JLabel label = new JLabel("Database");
	    gridbag.setConstraints(label, c);
	    borderPanel.add(label);
	    
	    JComboBox databaseType = new JComboBox();
	    databaseType.addItem("Mysql");
	    c.gridwidth = GridBagConstraints.REMAINDER;
	    gridbag.setConstraints(databaseType, c);
	    borderPanel.add(databaseType);
	    
	    // Host
	    addTextRow(borderPanel, "Host", db.get("host"), 
		       gridbag, c);
	    
	    // Port
	    addTextRow(borderPanel, "Port", db.get("port"), 
		       gridbag, c);
	    
	    // Username
	    addTextRow(borderPanel, "Username", db.get("username"),
		       gridbag, c);
	    
	    // Password
	    label = new JLabel("Password");
	    c.gridwidth = 1;
	    gridbag.setConstraints(label, c);
	    borderPanel.add(label);
	    
	    JPasswordField password = new JPasswordField(db.get("password"),
							 15);
	    c.gridwidth = GridBagConstraints.REMAINDER;
	    gridbag.setConstraints(password, c);
	    borderPanel.add(password);
	    
	    // Database Name
	    addTextRow(borderPanel, "Database Name", 
		       db.get("dbname"), gridbag, c);

	    databasePreferences.add(borderPanel, BorderLayout.NORTH);

	    // Import data
	    JButton importButton = new JButton("Import Quotes");
	    	    
	    JPanel database = new JPanel();
	    database.setLayout(new BorderLayout());
	    database.add(useDatabase, BorderLayout.NORTH);
	    database.add(databasePreferences, BorderLayout.CENTER);
	    database.add(importButton, BorderLayout.SOUTH);

	    pane.addTab("Database", database);
	}

	// File Pane
	{
	    JCheckBox useFiles = new JCheckBox("Use Files", true);

	    TitledBorder titled = new TitledBorder("File Preferences");
	    JPanel filePreferences = new JPanel();
	    filePreferences.setBorder(titled);
	    filePreferences.setLayout(new BorderLayout());

	    // Directory tree
	    Box horizontalBox = Box.createHorizontalBox();
	    horizontalBox.add(new JLabel("File Directory"));
	    horizontalBox.add(Box.createHorizontalStrut(5));
	    horizontalBox.add(new JTextField(15));
	    horizontalBox.add(Box.createHorizontalStrut(5));
	    horizontalBox.add(new JButton("Browse"));

	    horizontalBox.add(Box.createHorizontalGlue());

	    filePreferences.add(horizontalBox, BorderLayout.NORTH);

	    JPanel files = new JPanel();
	    files.setLayout(new BorderLayout());
	    files.add(useFiles, BorderLayout.NORTH);
	    files.add(filePreferences, BorderLayout.CENTER);

	    pane.addTab("Files", files);
	}

	// Internet Pane
	{
	    pane.addTab("Internet", new JLabel("Unsupported"));
	}

	add(pane);
    }

    private void addTextRow(JPanel panel, String field, String value,
			    GridBagLayout gridbag,
			    GridBagConstraints c) {
	JLabel label = new JLabel(field);
	c.gridwidth = 1;
	gridbag.setConstraints(label, c);
	panel.add(label);

	JTextField text = new JTextField(value, 15);
	c.gridwidth = GridBagConstraints.REMAINDER;
	gridbag.setConstraints(text, c);
	panel.add(text);
    }

    /**
     * Add a property change listener for module change events.
     *
     * @param	listener	listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(listener);
    }

    /**
     * Remove a property change listener for module change events.
     *
     * @param	listener	listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
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
     * Return menu bar for chart module.
     *
     * @return	the menu bar.
     */
    public JMenuBar getJMenuBar() {
	return null;
    }

    /**
     * Return frame icon for chart module.
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
	return "Quote Source Preferences";
    }

    /**
     * Return whether the module should be enclosed in a scroll pane.
     *
     * @return	enclose module in scroll bar
     */
    public boolean encloseInScrollPane() {
	return true;
    }

}
