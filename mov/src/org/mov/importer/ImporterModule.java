package org.mov.importer;

import org.mov.main.ModuleFrame;
import org.mov.main.Module;
import org.mov.util.*;
import org.mov.quote.*;

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.prefs.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

/**
 * The importer module for venice which allows importing of quotes from
 * files or the internet. It provides an interface to allow the user
 * to perform a variety of quote imports. Currently quotes can be
 * imported from the internet, a range of quote file formats and can
 * be imported to a database or another quote file.
 */
public class ImporterModule extends JPanel 
    implements Module, ActionListener {

    private JDesktopPane desktop;
    private PropertyChangeSupport propertySupport;

    // Import From
    private JRadioButton fromDatabase;
    private JRadioButton fromFiles;
    private JComboBox formatComboBox;
    private JRadioButton fromInternet;
    private JComboBox yearComboBox;

    // Import To
    private JCheckBox toFiles;
    private JCheckBox toDatabase;
    private JTextField toFileName;

    // Buttons
    private JButton importButton;
    private JButton cancelButton;

    private DatabaseQuoteSource databaseSource = null;

    /**
     * Create a new Importer Module.
     *
     * @param	desktop	the parent desktop
     */
    public ImporterModule(JDesktopPane desktop) {

	this.desktop = desktop;
	propertySupport = new PropertyChangeSupport(this);       

	setLayout(new BorderLayout());

	Preferences p = Preferences.userRoot().node("/import_quotes");
	String importFromSource = p.get("from", "internet");

	Box importOptions = Box.createVerticalBox();

	// Import From
	{
	    TitledBorder titled = new TitledBorder("Import From");
	    JPanel importFromPanel = new JPanel();
	    importFromPanel.setBorder(titled);

	    GridBagLayout gridbag = new GridBagLayout();
	    GridBagConstraints c = new GridBagConstraints();
	    importFromPanel.setLayout(gridbag);

	    c.weightx = 1.0;
	    c.ipadx = 5;
	    c.anchor = GridBagConstraints.WEST;

	    // From Database
	    fromDatabase = new JRadioButton("Database");
	    fromDatabase.addActionListener(this);
	    if(importFromSource.equals("database"))
		fromDatabase.setSelected(true);
	    c.gridwidth = GridBagConstraints.REMAINDER;
	    gridbag.setConstraints(fromDatabase, c);
	    importFromPanel.add(fromDatabase);

	    // From Files
	    fromFiles = new JRadioButton("Files");
	    fromFiles.addActionListener(this);
	    if(importFromSource.equals("files"))
		fromFiles.setSelected(true);
	    c.gridwidth = 1;
	    gridbag.setConstraints(fromFiles, c);
	    importFromPanel.add(fromFiles);

	    formatComboBox = new JComboBox();
	    formatComboBox.addActionListener(this);
	    formatComboBox.addItem("MetaStock");
	    Vector formats = QuoteFilterList.getInstance().getList();
	    Iterator iterator = formats.iterator();
	    QuoteFilter filter;
	    String selectedFilter = p.get("fileFilter", "MetaStock");

	    while(iterator.hasNext()) {
		filter = (QuoteFilter)iterator.next();
		formatComboBox.addItem(filter.getName());
		if(filter.getName().equals(selectedFilter))
		    formatComboBox.setSelectedItem((Object)filter.getName());
	    }

	    c.gridwidth = GridBagConstraints.REMAINDER;
	    gridbag.setConstraints(formatComboBox, c);
	    importFromPanel.add(formatComboBox);

	    // From Internet
	    fromInternet = new JRadioButton("Internet");
	    fromInternet.addActionListener(this);
	    if(importFromSource.equals("internet"))
		fromInternet.setSelected(true);
	    c.gridwidth = 1;
	    gridbag.setConstraints(fromInternet, c);
	    importFromPanel.add(fromInternet);

	    yearComboBox = new JComboBox();
	    yearComboBox.addActionListener(this);
	    // Add years from 1901 to preset
	    TradingDate today = new TradingDate();
	    int thisYear = today.getYear();
	    for(int year = 1901; year <= thisYear; year++) {
		yearComboBox.addItem("Quotes From " + Integer.toString(year));
	    }
	    yearComboBox.addItem("Latest Quotes");
	    yearComboBox.setSelectedItem(p.get("internetYear", 
					       "Latest Quotes"));

	    c.gridwidth = GridBagConstraints.REMAINDER;
	    gridbag.setConstraints(yearComboBox, c);
	    importFromPanel.add(yearComboBox);

	    // Put all "import from" radio buttons into group
	    ButtonGroup group = new ButtonGroup();
	    group.add(fromDatabase);
	    group.add(fromFiles);
	    group.add(fromInternet);

	    importOptions.add(importFromPanel);
	}

	// Import to
	{
	    TitledBorder titled = new TitledBorder("Import To");
	    JPanel importToPanel = new JPanel();
	    importToPanel.setBorder(titled);

	    GridBagLayout gridbag = new GridBagLayout();
	    GridBagConstraints c = new GridBagConstraints();
	    importToPanel.setLayout(gridbag);

	    c.weightx = 1.0;
	    c.ipadx = 5;
	    c.anchor = GridBagConstraints.WEST;

	    // To Database
	    toDatabase = new JCheckBox("Database");
	    toDatabase.addActionListener(this);
	    if(p.getBoolean("toDatabase", false))
		toDatabase.setSelected(true);
	    c.gridwidth = GridBagConstraints.REMAINDER;
	    gridbag.setConstraints(toDatabase, c);
	    importToPanel.add(toDatabase);

	    // To Files
	    toFiles = new JCheckBox("Files");
	    toFiles.addActionListener(this);
	    if(p.getBoolean("toFiles", false))
		toFiles.setSelected(true);
	    c.gridwidth = 1;
	    gridbag.setConstraints(toFiles, c);
	    importToPanel.add(toFiles);

	    toFileName = new JTextField(p.get("toFileName", ""), 15);
	    c.gridwidth = GridBagConstraints.REMAINDER;
	    gridbag.setConstraints(toFileName, c);
	    importToPanel.add(toFileName);

	    importOptions.add(importToPanel);	    
	}

	add(importOptions, BorderLayout.CENTER);

	// Import, Cancel buttons
	JPanel buttonPanel = new JPanel();
	importButton = new JButton("Import");
	importButton.addActionListener(this);
	cancelButton = new JButton("Cancel");
	cancelButton.addActionListener(this);
	buttonPanel.add(importButton);
	buttonPanel.add(cancelButton);

	add(buttonPanel, BorderLayout.SOUTH);

	// Make sure the appropriate buttons are enabled and the others
	// are disabled
	checkDisabledStatus();
    }

    // Enable/disable the appropriate widgets depending on which widgets
    // are checked.
    private void checkDisabledStatus() {

	// Cant import from database to database
	toDatabase.setEnabled(!fromDatabase.isSelected());

	// File format is only applicable if importing from files and not
	// to files (where the format is fixed)
	formatComboBox.setEnabled(fromFiles.isSelected() &&
				  !toFiles.isSelected());

	// Year is only specified if importing from the internet
	yearComboBox.setEnabled(fromInternet.isSelected());

	// Destination file name is only specified if importing to files
	// and not importing from files
	toFileName.setEnabled(toFiles.isSelected() &&
			      !fromFiles.isSelected());

	// Import button is only enabled if the user has selected at least
	// one destination
	importButton.setEnabled(toFiles.isSelected() || 
				(toDatabase.isSelected() &&
				 toDatabase.isEnabled()));
    }

    /**
     *  This is called when one of the buttons is pressed
     */
    public void actionPerformed(ActionEvent e) {

	// Make sure the appropriate widgets are disabled if they are
	// not in use
	if(e.getSource() == fromFiles ||
	   e.getSource() == fromInternet ||
	   e.getSource() == fromDatabase ||	
	   e.getSource() == toDatabase ||
	   e.getSource() == toFiles) {	    
	    checkDisabledStatus();
	} 
	else if(e.getSource() == cancelButton) {
	    // Tell frame we want to close
	    propertySupport.
		firePropertyChange(ModuleFrame.WINDOW_CLOSE_PROPERTY, 0, 1);
	}
	else if(e.getSource() == importButton) {

	    saveConfiguration();
	    importQuotes();
	}
    }

    // Import quotes
    private void importQuotes() {

	//
	// Step one: Open import data source and/or get list of files to import
	//
 
	QuoteSource source = null;
	String fileNames[] = {};
	Vector dates = new Vector();
	int numberDays = 0;

	// If we are importing from files we'll need to open a dialog
	if(fromFiles.isSelected()) {
	    // Get files user wants to import
	    JFileChooser chooser = new JFileChooser();
	    chooser.setMultiSelectionEnabled(true);
	    int action = chooser.showOpenDialog(desktop);

	    if(action == JFileChooser.APPROVE_OPTION) {
		File files[] = chooser.getSelectedFiles();
		fileNames = Converter.toFileNames(files);

		numberDays = files.length; 

		// Cancel if no files were selected (one day = one file)
		if(numberDays == 0)
		    return;

		// If we are importing to the database we'll need a quote
		// source - if we are importing only to files we dont need
		// this source as we'll just add the file names to the list
		if(toDatabase.isSelected()) {
		    source = 
			new FileQuoteSource((String)formatComboBox.getSelectedItem(), 
					    fileNames);
		    dates = source.getDates();

		    // Update number of days to reflect REAL number of days
		    // in cache
		    numberDays = dates.size();
		}
	    }
	    else
		return; // if the user cancelled then dont import
	}

	// Importing from the net
	else if(fromInternet.isSelected()) {
	    // Get username and password from preferences
	    Preferences p = 
		Preferences.userRoot().node("/quote_source/internet");
	    source = new SanfordQuoteSource(p.get("username", ""),
					    p.get("password", ""));
	}

	// Or database
	else 
	    source = new DatabaseQuoteSource();

	//
	// Step two: Import
	//

	// Close frame before doing import - we just want the progress bar
	// visible
	propertySupport.
	    firePropertyChange(ModuleFrame.WINDOW_CLOSE_PROPERTY, 0, 1);

	if(numberDays > 0)
	    performImport(source, numberDays, fileNames, dates); 
    }

    // Perform actual import given source and/or file list
    private void performImport(final QuoteSource source, 
			       final int numberDays,
			       final String[] fileNames,
			       final Vector dates) {
	
	Thread importQuotes = new Thread() {		
		public void run() {
		    
		    TradingDate date;
		    boolean owner = 
			Progress.getInstance().open("Importing", numberDays);

		    // Import a day at a time
		    for(int i = 0; i < numberDays; i++) {
			date = (TradingDate)dates.get(i);
			
			Progress.getInstance().setText("Importing: " +
						       date.toString("d?/m?/yyyy"),
						       owner);

			// file -> file 
			if(fromFiles.isSelected() && toFiles.isSelected())
			    importFileToFile(fileNames[i]);
			
			// anything -> database
			if(toDatabase.isSelected())
			    importToDatabase(source, date);

			Progress.getInstance().next();
		    }

		    // This makes sure the next query uses the new imported 
		    // quotes
		    Quote.flush();	

		    Progress.getInstance().close(owner);	
		}
	    };

	importQuotes.start();
		
    }

    // Import a single file into the file list
    private void importFileToFile(String fileName) {
	// Get list of files
	Preferences p = Preferences.userRoot().node("/quote_source/files");
	String fileList = p.get("list", "");
	String[] fileNames = fileList.split(", ");

	// Add file if its not already there
	for(int i = 0; i < fileNames.length; i++) {
	    if(fileNames[i].equals(fileName))
	       return; // exit its already there
	}
       
	// If we got here its not so add it
	p.put("list", fileList.concat(", ").concat(fileName));	
    }

    // Import a file into the database
    private void importToDatabase(QuoteSource source, TradingDate date) {
	Preferences p = Preferences.userRoot().node("/quote_source/database");
	String databaseName = p.get("dbname", "shares");

	if(databaseSource == null)
	    databaseSource = new DatabaseQuoteSource();

	databaseSource.importQuotes(databaseName, source, date);
    }

    // Save the configuration on screen to the preferences file
    private void saveConfiguration() {
	Preferences p = Preferences.userRoot().node("/import_quotes");

	// Import From
	if(fromDatabase.isSelected())
	    p.put("from", "database");
	else if(fromFiles.isSelected())
	    p.put("from", "files");
	else
	    p.put("from", "internet");
	p.put("internetYear", (String)yearComboBox.getSelectedItem());
	p.put("fileFilter", (String)formatComboBox.getSelectedItem());

	// Import To
	p.putBoolean("toDatabase", toDatabase.isSelected());
	p.putBoolean("toFiles", toFiles.isSelected());
	p.put("toFileName", toFileName.getText());
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
	return "Import Quotes";
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


