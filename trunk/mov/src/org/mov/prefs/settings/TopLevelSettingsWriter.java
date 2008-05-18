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

package org.mov.prefs.settings;

import org.mov.quote.Symbol;
import org.mov.main.ModuleFrame;
import org.mov.main.Module;

import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.beans.XMLEncoder;
import java.util.Iterator;
import java.util.Vector;
import java.util.List;
import java.util.Collection;
import java.awt.Rectangle;
import java.awt.Dimension;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.mov.quote.Symbol;
import org.mov.chart.graph.Graph;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;


/**
 * This class writes settings in XML format.
 *
 * @author Mark Hummel
 * @see DesktopManager
 * @see PreferencesManager
 * @see WatchScreenWriter
 */
public class TopLevelSettingsWriter implements SettingsWriter {

    public TopLevelSettingsWriter() {
        // Nothing to do
    }


    /**
     * Write the module settings to the output stream in XML format.
     *
     * @param frame the module frame data to write
     * @param stream      the output stream to write the window settings.
     */
    public  void write(ModuleFrame frame, OutputStream stream) {

	DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
	
	ModuleFrameSettings settings = new ModuleFrameSettings();
	Settings moduleSettings = frame.getModule().getSettings();
	settings.setModuleSettings(moduleSettings);
	settings.setBounds(frame.getBounds());

	BufferedOutputStream buffStream = new BufferedOutputStream(stream);
	XMLEncoder xStream = new XMLEncoder(buffStream);
	
	try {
	    xStream.writeObject(settings);
	    xStream.close();
	} catch (java.lang.Exception e) {
	    System.out.println("Error writeing object; " + e);
	}


    }

    public void write(Settings settings, Document document, Element parent) {
	
    }
}