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

package org.mov.chart.graph;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.HashMap;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.mov.ui.GridBagHelper;
import org.mov.util.Locale;

/**
 * The Point and Figure graph user interface.
 *
 * @author Andrew Leppard
 * @see PointAndFigureGraph
 */
public class PointAndFigureGraphUI implements GraphUI {

    // The graph's user interface
    private JPanel panel;
    private JTextField priceScaleTextField;

    // String name of settings
    private final static String PRICE_SCALE = "price_scale";

    // Limits
    private final static double MINIMUM_PRICE_SCALE = 0.0001D;

    // Default values - this is not a constant because the default
    // value will be set depending on the graph
    private double defaultPriceScale;

    /**
     * Create a new Point and Figure user interface with the initial settings.
     *
     * @param settings the initial settings
     * @param defaultPriceScale default price scale based on data
     */
    public PointAndFigureGraphUI(HashMap settings, double defaultPriceScale) {
        this.defaultPriceScale = defaultPriceScale;
        buildPanel();
        setSettings(settings);
    }

    /**
     * Build the user interface JPanel.
     */
    private void buildPanel() {
        panel = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        panel.setLayout(layout);

        c.weightx = 1.0;
        c.ipadx = 5;
        c.anchor = GridBagConstraints.WEST;

        priceScaleTextField =
            GridBagHelper.addTextRow(panel, Locale.getString("PRICE_SCALE"), "",
                                     layout, c, 8);
    }

    public String checkSettings() {
        HashMap settings = getSettings();

        // Check price scale
        String priceScaleString = (String)settings.get(PRICE_SCALE);
        double priceScale;

        try {
            priceScale = Double.parseDouble(priceScaleString);
        }
        catch(NumberFormatException e) {
            return Locale.getString("ERROR_PARSING_NUMBER", priceScaleString);
        }

        if (priceScale < MINIMUM_PRICE_SCALE)
            return Locale.getString("ERROR_PRICE_SCALE_TOO_SMALL");

        // Settings are OK
        return null;
    }

    public HashMap getSettings() {
        HashMap settings = new HashMap();
        settings.put(PRICE_SCALE, priceScaleTextField.getText());
        return settings;
    }

    public void setSettings(HashMap settings) {
        priceScaleTextField.setText(Double.toString(getPriceScale(settings,
                                                                  defaultPriceScale)));
    }

    public JPanel getPanel() {
        return panel;
    }

    /**
     * Retrieve the price scale from the settings hashmap. If the hashmap
     * is empty, then return the default price scale.
     *
     * @param settings the settings
     * @param defaultPriceScale the default price scale
     * @return the price scale
     */
    public static double getPriceScale(HashMap settings, double defaultPriceScale) {
        double priceScale = defaultPriceScale;
        String priceScaleString = (String)settings.get(PRICE_SCALE);

        if(priceScaleString != null) {
            try {
                priceScale = Double.parseDouble(priceScaleString);
            }
            catch(NumberFormatException e) {
                // Value should already be checked
                assert false;
            }
        }

        return priceScale;
    }
}