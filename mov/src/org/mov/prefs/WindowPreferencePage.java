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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;


import org.mov.util.Locale;

/**
 * Provides a preference page to let the user
 * loading or saving the preferences of MoV windows.
 *
 * @author Alberto Nacher
 */
public class WindowPreferencePage extends JPanel implements PreferencesPage {
    
    private JDesktopPane desktop = null;
    private PreferencesManager.WindowPreferencePreferences windowPreferencePreferences = null;
    
    private JLabel informationLabel = null;
    private JLabel blankLabel = new JLabel(" ");
    private JLabel pathLabel = null;
    private JTextField pathTextField = null;
    private JButton loadButton = null;
    private JButton saveButton = null;
    private JButton cancelButton = null;
    
    /* XML Filter File. */
    public class XMLFilter extends FileFilter {

        public static final String xml = "xml";
        
        //Accept all directories and xml files.
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }

            String extension = this.getExtension(f);
            if (extension != null) {
                if (extension.equals(XMLFilter.xml)) {
                        return true;
                } else {
                    return false;
                }
            }

            return false;
        }

        //The description of this filter
        public String getDescription() {
            return Locale.getString("XML_ONLY");
        }
        
        /*
         * Get the extension of a file.
         */  
        private String getExtension(File f) {
            String ext = null;
            String s = f.getName();
            int i = s.lastIndexOf(".");

            if (i > 0 &&  i < s.length() - 1) {
                ext = s.substring(i+1).toLowerCase();
            }
            return ext;
        }
    }

    /**
     * Create a new windowPreference preferences page.
     *
     * @param	desktop	the parent desktop.
     */
    public WindowPreferencePage(JDesktopPane desktop, JButton cancelButton) {
	this.desktop = desktop;
        this.cancelButton = cancelButton;
	
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	
	add(createWindowPreferencePanel());
    }
    
    private JPanel createWindowPreferencePanel() {
	JPanel windowPreferencePanel = new JPanel();
	windowPreferencePanel.setLayout(new BorderLayout());
	JPanel borderPanel = new JPanel();
        borderPanel.setLayout(new BoxLayout(borderPanel, BoxLayout.PAGE_AXIS));
	
	windowPreferencePreferences = 
	    PreferencesManager.loadWindowPreferenceSettings();
        
        informationLabel = new JLabel(Locale.getString("WINDOW_PREFERENCE_TEXT"));
        
        pathLabel = new JLabel(Locale.getString("WINDOW_PREFERENCE_CURRENT"));
        
        pathTextField = new JTextField();
        pathTextField.setText(windowPreferencePreferences.XMLfile);

        loadButton = new JButton(Locale.getString("LOAD"));
        loadButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    // Get the path preferences file that the user wants to import
                    JFileChooser chooser;
                    String lastDirectory = windowPreferencePreferences.path;

                    if(lastDirectory != null)
                        chooser = new JFileChooser(lastDirectory);
                    else
                        chooser = new JFileChooser();

                    chooser.setMultiSelectionEnabled(false);
                    chooser.setFileFilter(new XMLFilter());
                    int action = chooser.showOpenDialog(desktop);
                    
                    if(action == JFileChooser.APPROVE_OPTION) {
                        // Remember directory
                        lastDirectory = chooser.getCurrentDirectory().getAbsolutePath();
                        windowPreferencePreferences.path = lastDirectory;

                        // Add file to text box
                        File file = chooser.getSelectedFile();
                        windowPreferencePreferences.XMLfile = file.getAbsolutePath();
                        pathTextField.setText(windowPreferencePreferences.XMLfile);
                        
                        // Save new preferences from the file
                        try {
                            InputStream inputStream = new BufferedInputStream(
                                new FileInputStream(file));
                            PreferencesManager.importPreferences(inputStream);
                            inputStream.close();
                            // 'Cancel' button is clicked, so that the new preferences
                            // cannot be overwritten by the old ones.
                            clickCancel();
                        } catch (IOException ex) {
                            JOptionPane.showInternalMessageDialog(desktop,
                                                                  Locale.getString("ERROR_READING_FROM_FILE"),
                                                                  Locale.getString("INVALID_PREFERENCES_ERROR"),
                                                                  JOptionPane.ERROR_MESSAGE);
                        } catch (Exception ex) {
                            JOptionPane.showInternalMessageDialog(desktop,
                                                                  Locale.getString("INVALID_PREFERENCES_FORMAT_ERROR"),
                                                                  Locale.getString("INVALID_PREFERENCES_ERROR"),
                                                                  JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            });
	
        saveButton = new JButton(Locale.getString("SAVE"));
        saveButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    // Set the path preferences file that the user wants to export
                    JFileChooser chooser;
                    String lastDirectory = windowPreferencePreferences.path;

                    if(lastDirectory != null)
                        chooser = new JFileChooser(lastDirectory);
                    else
                        chooser = new JFileChooser();

                    chooser.setMultiSelectionEnabled(false);
                    chooser.setFileFilter(new XMLFilter());
                    int action = chooser.showSaveDialog(desktop);
                    
                    if(action == JFileChooser.APPROVE_OPTION) {
                        // Remember directory
                        lastDirectory = chooser.getCurrentDirectory().getAbsolutePath();
                        windowPreferencePreferences.path = lastDirectory;

                        // Add file to text box
                        File file = chooser.getSelectedFile();
                        windowPreferencePreferences.XMLfile = file.getAbsolutePath();
                        pathTextField.setText(windowPreferencePreferences.XMLfile);
                        
                        // Save file in the system
                        try {
                            OutputStream outputStream = new BufferedOutputStream(
                                new FileOutputStream(file));
                            PreferencesManager.exportPreferences(outputStream);
                            outputStream.close();
                        } catch (IOException ex) {
                            JOptionPane.showInternalMessageDialog(desktop,
                                                                  Locale.getString("ERROR_WRITING_TO_FILE"),
                                                                  Locale.getString("INVALID_PREFERENCES_ERROR"),
                                                                  JOptionPane.ERROR_MESSAGE);
                        } catch (Exception ex) {
                            JOptionPane.showInternalMessageDialog(desktop,
                                                                  Locale.getString("INVALID_PREFERENCES_FORMAT_ERROR"),
                                                                  Locale.getString("INVALID_PREFERENCES_ERROR"),
                                                                  JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            });

        borderPanel.add(informationLabel);
        borderPanel.add(blankLabel);
        borderPanel.add(pathLabel);
        borderPanel.add(pathTextField);
        borderPanel.add(loadButton);
        borderPanel.add(saveButton);
        
        windowPreferencePanel.add(borderPanel, BorderLayout.NORTH);

	return windowPreferencePanel;
    }
    
    private void clickCancel() {
	this.cancelButton.doClick();
    }                               
 
    public String getTitle() {
	return Locale.getString("WINDOW_PREFERENCE_PAGE_TITLE");
    }
    
    public void save() {
	PreferencesManager.saveWindowPreferenceSettings(windowPreferencePreferences);
    }
    
    public JComponent getComponent() {
	return this;
    }
}
