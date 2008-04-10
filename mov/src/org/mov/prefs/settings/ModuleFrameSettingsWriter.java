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

import java.io.OutputStream;
import java.util.Iterator;
import java.util.Vector;
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
import org.mov.prefs.settings.Settings;
import org.mov.prefs.settings.ModuleFrameSettings;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * This class writes watch screens in XML format.
 *
 * @author Mark Hummel
 * @see DesktopManager
 * @see PreferencesManager
 * @see WatchScreenWriter
 */
public class ModuleFrameSettingsWriter implements SettingsWriter {

    public ModuleFrameSettingsWriter() {
        // Nothing to do
    }

    /**
     * Write the window settings to the output stream in XML format.
     *
     * @param frame the module frame data to write
     * @param stream      the output stream to write the window settings.
     */
    public void write(Settings settings, Document document, Element parent) {

	Element frameSettingsElement = (Element)document.createElement("frame");
	parent.appendChild(frameSettingsElement);
	
	String key = settings.getKey();
	
	frameSettingsElement.setAttribute("name", "FrameDataFor" + settings.getTitle() + "_" + settings.getKey());
	
	frameSettingsElement.setAttribute("module",key);
	
	
	Element geometryElement = (Element)document.createElement("geometry");
	frameSettingsElement.appendChild(geometryElement);
	
	Element sizeElement = (Element)document.createElement("size");
	Element positionElement = (Element)document.createElement("position");
	geometryElement.appendChild(sizeElement);
	geometryElement.appendChild(positionElement);
	
	ModuleFrameSettings mySettings = (ModuleFrameSettings)settings;
	Rectangle bounds = mySettings.getBounds();

	addElement("height", bounds.getHeight(), sizeElement, document);
	addElement("width", bounds.getWidth(), sizeElement, document);
	addElement("xpos", bounds.getX(), positionElement, document);
	addElement("ypos", bounds.getY(), positionElement, document);
	
	Settings moduleSettings = mySettings.getModuleSettings();
	Element moduleSettingsElement = (Element)document.createElement("module");
	moduleSettingsElement.setAttribute("group", 
					   String.
					   valueOf(moduleSettings.getGroup()));
	
	moduleSettingsElement.setAttribute("type", 
					   String.
					   valueOf(moduleSettings.getType()));
	
	moduleSettingsElement.setAttribute("name", moduleSettings.getTitle());

	frameSettingsElement.appendChild(moduleSettingsElement);
	
       
	SettingsWriter moduleSettingsWriter = moduleSettings.getWriter();
	moduleSettingsWriter.write(moduleSettings, document, moduleSettingsElement);

    }


    private  void addElement(String name, double value, Element parent,
				   Document doc) {
	Element newElement = (Element)doc.createElement(name);
	String str = String.valueOf(value);
	Text textNode = doc.createTextNode(str);
	newElement.appendChild(textNode);
	parent.appendChild(newElement);
	
    }

}