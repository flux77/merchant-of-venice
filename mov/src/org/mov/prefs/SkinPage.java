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

import org.mov.ui.SkinManager;

/** 
 * Provides a preferences page to let the user modify display skin settings
 */
public class SkinPage extends JPanel 
    implements ActionListener, PreferencesPage
{

    private JDesktopPane desktop;

    private JRadioButton no_skintype;
    private JRadioButton pack_skintype;
    private JRadioButton x11_skintype;

    private JTextField pack_file_text;
    private JTextField x11_file_1_text;
    private JTextField x11_file_2_text;

    private JButton pack_browse_button;
    private JButton x11_browse_1_button;
    private JButton x11_browse_2_button;

    private String skin_type;
    private String pack_file;
    private String x11_file_1;
    private String x11_file_2;

    /**
     * Create a new Skins Preferences page.
     *
     * @param	desktop	the parent desktop.
     */
    public SkinPage(JDesktopPane desktop) {

	this.desktop = desktop;
	GridBagLayout gb = new GridBagLayout();
	GridBagConstraints c = new GridBagConstraints();
	GridBagConstraints big_c = new GridBagConstraints();

	setLayout(gb);

	Preferences p = Preferences.userRoot().node("/display/skin");
	skin_type = p.get("skin_type", "none");

	ButtonGroup group = new ButtonGroup();
	group.add(no_skintype = new JRadioButton("None", 
						 skin_type.equals("none") ? true : false));
	group.add(pack_skintype = new JRadioButton("Skin theme pack (see www.l2fprod.com)", 
						   skin_type.equals("pack") ? true : false));
	group.add(x11_skintype = new JRadioButton("KDE or GTK theme",
						  skin_type.equals("x11") ? true: false));
	
	no_skintype.addActionListener(this);
	pack_skintype.addActionListener(this);
	x11_skintype.addActionListener(this);

	TitledBorder titled = new TitledBorder("Skins Preferences");
	this.setBorder(titled);

	// No skins
	big_c.gridx = 0;
	big_c.gridy = 0;
	big_c.gridwidth = GridBagConstraints.REMAINDER;
	big_c.anchor = GridBagConstraints.NORTHWEST;
	big_c.fill = GridBagConstraints.BOTH;
	add(no_skintype, big_c);

	// Theme Pack
	JPanel pack_panel;
	big_c.gridy = 1;
	add(pack_panel = new JPanel(),big_c);

	gb = new GridBagLayout();
	c = new GridBagConstraints();
	pack_panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
	pack_panel.setLayout(gb);
	    
	c.weightx = 1.0;
	c.ipadx = 5;
	c.anchor = GridBagConstraints.WEST;
	c.gridx = 0;
	c.gridy = 0;
	c.gridwidth = 3;

	pack_panel.add(pack_skintype, c);

	c.gridy = 1;
	c.gridwidth = 1;
	pack_panel.add(new JLabel("Theme file"), c);
	
	c.gridx = 1;
	c.gridwidth = GridBagConstraints.REMAINDER;
	pack_file = p.get("pack_file", "");
	pack_panel.add(pack_file_text = new JTextField(pack_file,15), c);
	
	c.gridy = 2;
	pack_panel.add(pack_browse_button = new JButton("Browse..."),c);
	pack_browse_button.addActionListener(this);
	
	// X11 Themes
	JPanel x11_panel;
	big_c.gridy = 2;
	add(x11_panel = new JPanel(),big_c);

	gb = new GridBagLayout();
	GridBagConstraints c2 = new GridBagConstraints();
	x11_panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
	x11_panel.setLayout(gb);
	    
	//	c2.weightx = 1.0;
	c2.ipadx = 5;
	c2.anchor = GridBagConstraints.WEST;
	c2.gridx = 0;
	c2.gridy = 0;
	c2.gridwidth = 4;

	x11_panel.add(x11_skintype, c2);
	c2.gridy = 1;
	c2.gridwidth = 1;
	x11_panel.add(new JLabel("Primary theme file"), c2);
	
	c2.gridx = 1;
	c2.gridwidth = GridBagConstraints.REMAINDER;
	x11_file_1 = p.get("x11_file_1", "");
	x11_panel.add(x11_file_1_text = new JTextField(x11_file_1,15), c2);
	
	c2.gridy = 2;
	x11_panel.add(x11_browse_1_button = new JButton("Browse..."),c2);
	x11_browse_1_button.addActionListener(this);

	c2.gridy = 3;
 	c2.gridx = 0;
	c2.gridwidth = 1;
	x11_panel.add(new JLabel("Secondary theme file"), c2);
	
	c2.gridx = 1;
	c2.gridwidth = GridBagConstraints.REMAINDER;
	x11_file_2 = p.get("x11_file_2", "");
	x11_panel.add(x11_file_2_text = new JTextField(x11_file_2,15), c2);

	
       	c2.gridy = 4;
	c2.gridwidth = 1;
	x11_panel.add(x11_browse_2_button = new JButton("Browse..."), c2);
	x11_browse_2_button.addActionListener(this);
	
    }

    /**
     *  This is called when one of the buttons is pressed
     */
    public void actionPerformed(ActionEvent e) {
	
	if(e.getSource() == pack_browse_button ||
	   e.getSource() == x11_browse_1_button ||
	   e.getSource() == x11_browse_2_button) {

	    // Get theme file
	    JFileChooser chooser = new JFileChooser();
	    chooser.setMultiSelectionEnabled(false);
	    int action = chooser.showOpenDialog(desktop);

	    if(action == JFileChooser.APPROVE_OPTION) {
		// Add files to file list
		String filename = null;
		try {
		    filename = chooser.getSelectedFile().toURL().toString();
		} catch (Exception ex) {}
		if (e.getSource() == pack_browse_button) {
		    pack_file = filename;
		    pack_file_text.setText(pack_file);
		} else if (e.getSource() == x11_browse_1_button) {
		    x11_file_1 = filename;
		    x11_file_1_text.setText(x11_file_1);
		}
		if (e.getSource() == x11_browse_2_button) {
		    x11_file_2 = filename;
		    x11_file_2_text.setText(x11_file_2);
		}

	    }
	    
	    
	} else if (e.getSource() == no_skintype)
	    skin_type = "none";
	else if (e.getSource() == pack_skintype)
	    skin_type = "pack";
	else if (e.getSource() == x11_skintype)
	    skin_type = "x11";
	    

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
     * Returns the window title.
     *
     * @return	the window title.
     */
    public String getTitle() {
	return "Quote Source Preferences";
    }

    /**
     * Update the preferences file.
     */
    public void save() {
	
	Preferences p = 
	    Preferences.userRoot().node("/display/skin");
	p.put("skin_type", skin_type);
	p.put("pack_file", pack_file);
	p.put("x11_file_1", x11_file_1);
	p.put("x11_file_2", x11_file_2);
	SkinManager.loadSkin();
    }

}
