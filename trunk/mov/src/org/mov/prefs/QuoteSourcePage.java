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

package org.mov.prefs;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.border.TitledBorder;

import org.mov.ui.GridBagHelper;
import org.mov.prefs.PreferencesManager;
import org.mov.quote.DatabaseQuoteSource;
import org.mov.quote.QuoteSourceManager;
import org.mov.util.Locale;

/** 
 * Provides a preferences page to let the user modify the quote source.
 * The quote source can be from a database (internal or external) or
 * sample quotes.
 *
 * @author Andrew Leppard
 */
public class QuoteSourcePage extends JPanel implements PreferencesPage
{
    private JDesktopPane desktop;

    // Widgets from database pane
    private JRadioButton useDatabase;
    private JComboBox databaseSoftware;
    private JTextField databaseHost;

    // Wigets from internal pane
    private JRadioButton useInternal;
    private JTextField internalFileNameTextField;

    // This field needs to be initialised as it may be referenced
    // before the widget is created.
    private JTextField databasePort = null;
    private JTextField databaseUsername;
    private JPasswordField databasePassword;
    private JTextField databaseName;

    // Widgets from internet pane
    private JRadioButton useInternet;
    private JComboBox internetHost;
    private JTextField internetUsername;
    private JPasswordField internetPassword;

    // Widgets from the samples pane
    private JRadioButton useSamples;

    // Preferences
    private PreferencesManager.DatabasePreferences databasePreferences = null;

    // Quote source enumeration
    private final static int SAMPLES = 0;
    private final static int INTERNAL = 1;
    private final static int DATABASE = 2;

    // Database default ports
    private final static int MYSQL_DEFAULT_PORT = 3306;
    private final static int POSTGRESQL_DEFAULT_PORT = 5432;
    private final static int HSQLDB_DEFAULT_PORT = 9001;

    /**
     * Create a new Quote Source Preferences page.
     *
     * @param	desktop	the parent desktop.
     */
    public QuoteSourcePage(JDesktopPane desktop) {

	this.desktop = desktop;

	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

	// Load quote source preferences
	int quoteSource = PreferencesManager.getQuoteSource();

	// Tab Pane
	JTabbedPane pane = new JTabbedPane(JTabbedPane.TOP);
	// Put all "use this option" radio buttons into group
	ButtonGroup buttonGroup = new ButtonGroup();
	
        // Add a pane for each quote source the user can select.
        // These should be added in the same order as DATABASE, INTERNAL, etc.
        pane.addTab(Locale.getString("SAMPLES"), 
		    createSamplesPanel(quoteSource, buttonGroup));
        pane.addTab(Locale.getString("INTERNAL"),
                    createInternalPanel(quoteSource, buttonGroup));
        pane.addTab(Locale.getString("DATABASE"), 
		    createDatabasePanel(quoteSource, buttonGroup));

	// Raise the select source's pane
        if(quoteSource == PreferencesManager.INTERNAL)
	    pane.setSelectedIndex(INTERNAL);
        else if(quoteSource == PreferencesManager.DATABASE)
	    pane.setSelectedIndex(DATABASE);
	else
	    pane.setSelectedIndex(SAMPLES);

	add(pane);
    }

    private JPanel createInternalPanel(int quoteSource, ButtonGroup buttonGroup) {
        String internalFileName = PreferencesManager.loadInternalFileName();

        useInternal = new JRadioButton(Locale.getString("USE_INTERNAL"), true);
	buttonGroup.add(useInternal);

	useInternal.setSelected(quoteSource == PreferencesManager.INTERNAL);
        
        TitledBorder titled = new TitledBorder(Locale.getString("INTERNAL_PREFERENCES"));
        JPanel preferencesPanel = new JPanel();
        preferencesPanel.setBorder(titled);
        preferencesPanel.setLayout(new BorderLayout()); 
        JPanel borderPanel = new JPanel();
        
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        borderPanel.setLayout(gridbag);
        
        c.weightx = 1.0;
        c.ipadx = 5;
        c.anchor = GridBagConstraints.WEST;
        
        // File to store database
        internalFileNameTextField = GridBagHelper.addTextRow(borderPanel, 
                                                             Locale.getString("FILE"), 
                                                             internalFileName,
                                                             gridbag, c, 15);
        
        preferencesPanel.add(borderPanel, BorderLayout.NORTH);
        
        JPanel outerPanel = new JPanel();
        outerPanel.setLayout(new BorderLayout());
        outerPanel.add(useInternal, BorderLayout.NORTH);
        outerPanel.add(preferencesPanel, BorderLayout.CENTER);
        
        return outerPanel;
    }

    private JPanel createDatabasePanel(int quoteSource, ButtonGroup buttonGroup) {
	databasePreferences = PreferencesManager.loadDatabaseSettings();
        useDatabase = new JRadioButton(Locale.getString("USE_DATABASE"), true);
	buttonGroup.add(useDatabase);

	useDatabase.setSelected(quoteSource == PreferencesManager.DATABASE);
        
        TitledBorder titled = new TitledBorder(Locale.getString("DATABASE_PREFERENCES"));
        JPanel preferencesPanel = new JPanel();
        preferencesPanel.setBorder(titled);
        preferencesPanel.setLayout(new BorderLayout()); 
        JPanel borderPanel = new JPanel();
        
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        borderPanel.setLayout(gridbag);
        
        c.weightx = 1.0;
        c.ipadx = 5;
        c.anchor = GridBagConstraints.WEST;
        
        // Database 
        JLabel label = new JLabel(Locale.getString("DATABASE"));
        gridbag.setConstraints(label, c);
        borderPanel.add(label);
        
        databaseSoftware = new JComboBox();
        databaseSoftware.addItem(Locale.getString("MYSQL"));
        databaseSoftware.addItem(Locale.getString("POSTGRESQL"));
        databaseSoftware.addItem(Locale.getString("HSQLDB"));
	if(databasePreferences.software.equals("mysql"))
	    databaseSoftware.setSelectedIndex(DatabaseQuoteSource.MYSQL);
	else if(databasePreferences.software.equals("postgresql"))
	    databaseSoftware.setSelectedIndex(DatabaseQuoteSource.POSTGRESQL);
        else
	    databaseSoftware.setSelectedIndex(DatabaseQuoteSource.HSQLDB);

        // If the user changes the database....
        // field to reflect the default port of the database.
        databaseSoftware.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if(databasePort != null && databaseUsername != null &&
                       databasePassword != null) {

                        // ... then update the port
                        if(databaseSoftware.getSelectedIndex() == DatabaseQuoteSource.MYSQL)
                            databasePort.setText(Integer.toString(MYSQL_DEFAULT_PORT));
                        else if(databaseSoftware.getSelectedIndex() == DatabaseQuoteSource.POSTGRESQL)
                            databasePort.setText(Integer.toString(POSTGRESQL_DEFAULT_PORT));
                        else
                            databasePort.setText(Integer.toString(HSQLDB_DEFAULT_PORT));

                        // And enable/disable the username and password fields if applicable
                        if(databaseSoftware.getSelectedIndex() == DatabaseQuoteSource.HSQLDB) {
                            // Hypesonic SQL does not accept the username password fields
                            databaseUsername.setEnabled(false);
                            databasePassword.setEnabled(false);
                        }
                        else {
                            databaseUsername.setEnabled(true);
                            databasePassword.setEnabled(true);                            
                        }
                    }
                }
            });

        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(databaseSoftware, c);
        borderPanel.add(databaseSoftware);
        
        // Host
        databaseHost = GridBagHelper.addTextRow(borderPanel, 
						Locale.getString("HOST"), 
						databasePreferences.host,
                                                gridbag, c, 15);
        
        // Port
        databasePort = GridBagHelper.addTextRow(borderPanel, 
						Locale.getString("PORT"), 
                                                databasePreferences.port,
                                                gridbag, c, 15);
        
        // Username
        databaseUsername = GridBagHelper.addTextRow(borderPanel, 
						    Locale.getString("USERNAME"), 
                                                    databasePreferences.username, gridbag, c, 15);
        
        // Password
        databasePassword = GridBagHelper.addPasswordRow(borderPanel, 
							Locale.getString("PASSWORD"), 
                                                        databasePreferences.password, 
                                                        gridbag, c, 15);
        
        // Hypesonic SQL does not accept the username password fields
        if(databaseSoftware.getSelectedIndex() == DatabaseQuoteSource.HSQLDB) {
            databaseUsername.setEnabled(false);
            databasePassword.setEnabled(false);
        }

        // Database Name
        databaseName = GridBagHelper.addTextRow(borderPanel, 
						Locale.getString("DATABASE_NAME"), 
                                                databasePreferences.database, gridbag, c, 15);
        
        preferencesPanel.add(borderPanel, BorderLayout.NORTH);
        
        JPanel database = new JPanel();
        database.setLayout(new BorderLayout());
        database.add(useDatabase, BorderLayout.NORTH);
        database.add(preferencesPanel, BorderLayout.CENTER);
        
        return database;
    }

    private JPanel createSamplesPanel(int quoteSource, ButtonGroup buttonGroup) {
        useSamples = new JRadioButton(Locale.getString("USE_SAMPLES"), true);
        buttonGroup.add(useSamples);

	useSamples.setSelected(quoteSource == PreferencesManager.SAMPLES);
        
        JPanel samples = new JPanel();
        samples.setLayout(new BorderLayout());
        samples.add(useSamples, BorderLayout.NORTH);
        
        return samples;
    }

    public JComponent getComponent() {
	return this;
    }

    public String getTitle() {
	return Locale.getString("QUOTE_SOURCE_PAGE_TITLE");
    }

    public void save() {
	// Save quote source preferences
	int quoteSource;

	if(useInternal.isSelected())
	    quoteSource = PreferencesManager.INTERNAL;
	else if(useDatabase.isSelected())
	    quoteSource = PreferencesManager.DATABASE;
	else
	    quoteSource = PreferencesManager.SAMPLES;
	PreferencesManager.setQuoteSource(quoteSource);

        // Save internal preferences
        {
            PreferencesManager.saveInternalFileName(internalFileNameTextField.getText());
        }

	// Save database preferences
	{
	    String software = (String)databaseSoftware.getSelectedItem();
	    if(software.equals(Locale.getString("MYSQL")))
		databasePreferences.software = "mysql";
	    else if(software.equals(Locale.getString("POSTGRESQL")))
		databasePreferences.software = "postgresql";
            else
		databasePreferences.software = "hsql";

	    databasePreferences.host = databaseHost.getText();
	    databasePreferences.port = databasePort.getText();
	    databasePreferences.username = databaseUsername.getText();
	    databasePreferences.password = new String(databasePassword.getPassword());
	    databasePreferences.database = databaseName.getText();

	    PreferencesManager.saveDatabaseSettings(databasePreferences);
	}

	// This makes the next query use our new settings
	QuoteSourceManager.flush();
    }
}
