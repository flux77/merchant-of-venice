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

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JDesktopPane;
import javax.swing.BoxLayout;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;
import javax.swing.JTextField;

import org.mov.ui.GridBagHelper;
import org.mov.util.Locale;

/**
 * Provides a preference page to let the user specify their web proxy.
 *
 * @author Matthias Stockel
 */
public class ProxyPage extends JPanel implements PreferencesPage {
    
    private JDesktopPane desktop = null;
    private PreferencesManager.ProxyPreferences proxyPreferences = null;
    private JCheckBox useProxyCheckBox = null;
    private JTextField hostTextField = null;
    private JTextField portTextField = null;
    
    /**
     * Create a new proxy preferences page.
     *
     * @param	desktop	the parent desktop.
     */
    public ProxyPage(JDesktopPane desktop) {
	this.desktop = desktop;
	
	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	
	add(createProxyPanel());
    }
    
    private JPanel createProxyPanel() {
	JPanel proxyPanel = new JPanel();
	proxyPanel.setLayout(new BorderLayout());
	JPanel borderPanel = new JPanel();
	
	GridBagLayout gridbag = new GridBagLayout();
	GridBagConstraints c = new GridBagConstraints();
	borderPanel.setLayout(gridbag);
	
	c.weightx = 1.0;
	c.ipadx = 5;
	c.anchor = GridBagConstraints.WEST;

	proxyPreferences = 
	    PreferencesManager.loadProxySettings();

	useProxyCheckBox = 
	    GridBagHelper.addCheckBoxRow(borderPanel, 
					 Locale.getString("USE_PROXY"),
					 proxyPreferences.isEnabled, gridbag, c);
	useProxyCheckBox.addActionListener(new ActionListener() {
		public void actionPerformed(final ActionEvent e) {
		    checkDisabledStatus();
		}
	    });

	hostTextField = GridBagHelper.addTextRow(borderPanel,
						 Locale.getString("PROXY_HOST"),
						 proxyPreferences.host,
						 gridbag, c, 20);
	
	portTextField = GridBagHelper.addTextRow(borderPanel,
						 Locale.getString("PROXY_PORT"),
						 proxyPreferences.port,
						 gridbag, c, 5);
	
	proxyPanel.add(borderPanel, BorderLayout.NORTH);

	checkDisabledStatus();
					   
	return proxyPanel;
    }
    
    public String getTitle() {
	return Locale.getString("PROXY_PAGE_TITLE");
    }
    
    public void save() {
	proxyPreferences.host = hostTextField.getText();
	proxyPreferences.port = portTextField.getText();
	proxyPreferences.isEnabled = useProxyCheckBox.isSelected();
	PreferencesManager.saveProxySettings(proxyPreferences);
    }
    
    public JComponent getComponent() {
	return this;
    }

    private void checkDisabledStatus() {
        boolean useProxy = useProxyCheckBox.isSelected();
	hostTextField.setEnabled(useProxy);
	portTextField.setEnabled(useProxy);
    }
}
