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
import javax.swing.border.*;
import javax.swing.event.*;

import org.mov.main.AnalyserModule;
import org.mov.quote.*;

public class QuoteSourcePreferences extends JPanel 
    implements AnalyserModule, ActionListener, ListSelectionListener
{

    private JDesktopPane desktop;
    private PropertyChangeSupport propertySupport;
    private DatabaseLookup db = DatabaseLookup.getInstance();

    // Widgets from database pane
    JRadioButton useDatabase;
    JButton databaseImport;
    JTextField databaseHost;
    JTextField databasePort;
    JTextField databaseUsername;
    JPasswordField databasePassword;
    JTextField databaseName;

    // Widgets from file pane
    JRadioButton useFiles;
    JList fileList;
    DefaultListModel fileListModel;
    JButton addFiles;
    JButton deleteFiles;

    /**
     * Create a new Quote Source Preferences view.
     *
     * @param	desktop	the parent desktop.
     */
    public QuoteSourcePreferences(JDesktopPane desktop) {

	this.desktop = desktop;

	propertySupport = new PropertyChangeSupport(this);       

	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

	Preferences p = Preferences.userRoot().node("/quote_source");
	String quoteSource = p.get("source", "database");

	// Tab Pane
	JTabbedPane pane = new JTabbedPane(JTabbedPane.TOP);

	// Database Pane
	{
	    useDatabase = new JRadioButton("Use Database", true);
	    useDatabase.addActionListener(this);
	    if(quoteSource.equals("database"))
		useDatabase.setSelected(true);
	    else
		useDatabase.setSelected(false);

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
	    databaseType.addItem("MySQL");
	    c.gridwidth = GridBagConstraints.REMAINDER;
	    gridbag.setConstraints(databaseType, c);
	    borderPanel.add(databaseType);
	    
	    // Host
	    databaseHost = addTextRow(borderPanel, "Host", db.get("host"), 
				      gridbag, c);
	    
	    // Port
	    databasePort = addTextRow(borderPanel, "Port", db.get("port"), 
				      gridbag, c);
	    
	    // Username
	    databaseUsername = addTextRow(borderPanel, "Username", 
					  db.get("username"), gridbag, c);
	    
	    // Password
	    label = new JLabel("Password");
	    c.gridwidth = 1;
	    gridbag.setConstraints(label, c);
	    borderPanel.add(label);
	    
	    databasePassword = new JPasswordField(db.get("password"),
						  15);
	    c.gridwidth = GridBagConstraints.REMAINDER;
	    gridbag.setConstraints(databasePassword, c);
	    borderPanel.add(databasePassword);
	    
	    // Database Name
	    databaseName = addTextRow(borderPanel, "Database Name", 
				      db.get("dbname"), gridbag, c);

	    databasePreferences.add(borderPanel, BorderLayout.NORTH);

	    // Import data
	    databaseImport = new JButton("Import Quotes");
	    databaseImport.addActionListener(this);
	    JPanel buttonPanel = new JPanel();
	    buttonPanel.add(databaseImport);
	    	    
	    JPanel database = new JPanel();
	    database.setLayout(new BorderLayout());
	    database.add(useDatabase, BorderLayout.NORTH);
	    database.add(databasePreferences, BorderLayout.CENTER);
	    database.add(buttonPanel, BorderLayout.SOUTH);

	    pane.addTab("Database", database);
	}

	// File Pane
	{
	    p = Preferences.userRoot().node("/quote_source/files");

	    useFiles = new JRadioButton("Use Files", true);
	    useFiles.addActionListener(this);
	    if(quoteSource.equals("files"))
		useFiles.setSelected(true);
	    else
		useFiles.setSelected(false);

	    TitledBorder titled = new TitledBorder("Files");
	    JPanel filePreferences = new JPanel();
	    filePreferences.setBorder(titled);
	    filePreferences.setLayout(new BoxLayout(filePreferences,
						    BoxLayout.Y_AXIS));

	    fileList = new JList();
	    fileListModel = new DefaultListModel();
	    fileList.setModel(fileListModel);
	    fileList.addListSelectionListener(this);
	    filePreferences.add(new JScrollPane(fileList));

	    // Add files from prefs
	    String fileList = p.get("list", "");
	    String[] fileListArray = fileList.split(", ");
	    for(int i = 0; i < fileListArray.length; i++) 
		fileListModel.addElement(fileListArray[i]);

	    // Add, Delete buttons
	    JPanel buttonPanel = new JPanel();
	    addFiles = new JButton("Add");
	    addFiles.addActionListener(this);
	    deleteFiles = new JButton("Delete");
	    deleteFiles.setEnabled(false);
	    deleteFiles.addActionListener(this);
	    
	    buttonPanel.add(addFiles);
	    buttonPanel.add(deleteFiles);

	    JPanel files = new JPanel();
	    files.setLayout(new BorderLayout());
	    files.add(useFiles, BorderLayout.NORTH);
	    files.add(filePreferences, BorderLayout.CENTER);
	    files.add(buttonPanel, BorderLayout.SOUTH);

	    pane.addTab("Files", files);
	}

	// Internet Pane
	{
	    pane.addTab("Internet", new JLabel("Not yet implemented"));
	}

	// Put all "use this option" radio buttons into group
	ButtonGroup group = new ButtonGroup();
	group.add(useDatabase);
	group.add(useFiles);

	// Raise the select source's pane
	if(quoteSource.equals("files")) 
	    pane.setSelectedIndex(1);
	else 
	    pane.setSelectedIndex(0);

	add(pane);
    }

    private JTextField addTextRow(JPanel panel, String field, String value,
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

	return text;
    }

    public void valueChanged(ListSelectionEvent e) {
	// If the user has selected an element in the file list
	// then enable the delete button
	deleteFiles.setEnabled(true);
    }

    public void actionPerformed(ActionEvent e) {

	if(e.getSource() == databaseImport) {

	    // Get files user wants to import
	    JFileChooser chooser = new JFileChooser();
	    chooser.setMultiSelectionEnabled(true);
	    int action = chooser.showOpenDialog(desktop);

	    if(action == JFileChooser.APPROVE_OPTION) {
		// Add files to file list
		File files[] = chooser.getSelectedFiles();

		for(int i = 0; i < files.length; i++) {
		    System.out.println(files[i].getPath() + 
				       files[i].getName());
		}

	    }
	}
	else if(e.getSource() == addFiles) {
	    // Get files user wants to import
	    JFileChooser chooser = new JFileChooser();
	    chooser.setMultiSelectionEnabled(true);
	    int action = chooser.showOpenDialog(desktop);

	    if(action == JFileChooser.APPROVE_OPTION) {
		// Add files to file list
		File files[] = chooser.getSelectedFiles();
		String fileName;

		for(int i = 0; i < files.length; i++) {
		    fileName = files[i].getPath();

		    if(!fileListModel.contains((Object)fileName))
			fileListModel.addElement((Object)fileName);
		}
	    }
	}
	else if(e.getSource() == deleteFiles) {
	    // Get selected files from list
	    Object[] selected = fileList.getSelectedValues();

	    // Remove all elements from list
	    for(int i = 0; i < selected.length; i++) {
		fileListModel.removeElement(selected[i]);
	    }
	    
	    // Disable delete button after delete since nothing will be
	    // highlighted
	    deleteFiles.setEnabled(false);
	}
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

    public void save() {
	// Type
	Preferences p = 
	    Preferences.userRoot().node("/quote_source");
	if(useFiles.isSelected())
	    p.put("source", "files");
	else
	    p.put("source", "database");

	// Save database preferences
	{
	    p = Preferences.userRoot().node("/quote_source/database");
	    p.put("host", databaseHost.getText());
	    p.put("port", databasePort.getText());
	    p.put("username", databaseUsername.getText());
	    p.put("password", new String(databasePassword.getPassword()));
	    p.put("dbname", databaseName.getText());
	}

	// Save file preferences
	{
	    // Files
	    p = Preferences.userRoot().node("/quote_source/files");
	    String fileList = "";
	    for(int i = 0; i < fileListModel.getSize(); i++) {
		if(!fileList.equals(""))
		    fileList = fileList.concat(", ");
		fileList = fileList.concat((String)fileListModel.elementAt(i));
	    }

	    p.put("list", fileList);
	}

	// This makes the next query use our new settings
	Quote.flush();
    }
}
