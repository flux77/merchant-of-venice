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
import org.mov.quote.SymbolFormatException;
import org.mov.util.Locale;

import java.io.InputStream;
import java.io.IOException;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.Vector;
import java.util.HashMap;
import java.util.Iterator;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.mov.prefs.settings.ModuleSettingsParserException;
import org.mov.main.ModuleFrame;
import org.mov.prefs.settings.ModuleFrameSettings;
import org.mov.prefs.settings.GraphSettings;



import org.xml.sax.SAXException;

/**
 * This class parses watch screens written in XML format.
 *
 * @author Mark Hummel
 * @see ModuleFrame
 * @see ModuleSettingsWriter
 */
public class ModuleFrameSettingsReader {

    static Settings moduleSettings;
    static ModuleFrameSettings settings;

    /**
     * This class cannot be instantiated.
     */
    private ModuleFrameSettingsReader() {
        // Nothing to do
    }

    /**
     * Read and parse the watch screen in XML format from the input stream and return
     * the watch screen object.
     *
     * @param stream the input stream containing the watch screen in XML format
     * @return the watch screen
     * @exception IOException if there was an I/O error reading from the stream.
     * @exception ModuleSettingsParserException if there was an error parsing the watch screen.
     */
    public static ModuleFrameSettings read(InputStream stream) throws IOException, ModuleSettingsParserException {

	if (settings == null) {
	    settings = new ModuleFrameSettings();
	}

	
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
	
        try {
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document document = builder.parse(stream);
            Element moduleFrameSettings = (Element)document.getDocumentElement();
            NamedNodeMap moduleFrameSettingsAttributes = moduleFrameSettings.getAttributes();
            Node moduleFrameNameNode = moduleFrameSettingsAttributes.getNamedItem("name");
	    
            NodeList childNodes = moduleFrameSettings.getChildNodes();
	    boolean readGeometry = false;

	    NodeList geometryNodes = moduleFrameSettings.getElementsByTagName("geometry");
	    
	    NodeList moduleNodes = moduleFrameSettings.getElementsByTagName("module");
	    

	    if (moduleNodes.getLength() <= 0 || moduleNodes.getLength() > 1) 
		throw new ModuleSettingsParserException(Locale.getString("MODULE_SETTINGS_INCORRECT_MODULES_NUM"));				
	    
	    if (geometryNodes.getLength() <= 0 || geometryNodes.getLength() > 1) 
		throw new ModuleSettingsParserException(Locale.getString("MODULE_SETTINGS_INCORRECT_GEOMETRY_NUM"));				
	    
	    if (!readGeometryData((Element)geometryNodes.item(0))) {
		throw new ModuleSettingsParserException(Locale.getString("MODULE_SETTINGS_TOP_LEVEL_ERROR"));		

	    }
	    
	    if (!readModuleData((Element)moduleNodes.item(0)))  {
		throw new ModuleSettingsParserException(Locale.getString("MODULE_SETTINGS_TOP_LEVEL_ERROR"));		
	    } else {
		settings.setModuleSettings(moduleSettings);
	    }
	    
	} catch (SAXException e) {
	    throw new ModuleSettingsParserException(e.getMessage());
	} catch(ParserConfigurationException e) {
	    throw new ModuleSettingsParserException(e.getMessage());
	}
	
	return settings;    
    }
    
    static boolean readGeometryData(Element data) throws ModuleSettingsParserException {

	int height = 0, width = 0;
	int xpos = 0, ypos = 0;		    

	NodeList sizeElement = data.getElementsByTagName("size");
	NodeList positionElement = data.getElementsByTagName("position");
	
	if (sizeElement.getLength() <= 0 || sizeElement.getLength() > 1) 
	    throw new ModuleSettingsParserException("Wrong Number of size elements");
	
	if (positionElement.getLength() <= 0 || positionElement.getLength() > 1) 
	    throw new ModuleSettingsParserException("Wrong Number of position elements");
	
	ModuleSettingsParserException mspe = new ModuleSettingsParserException("MODULE_SETTINGS_GEOMETRY_NOT_DEF");

	String heightStr = XMLHelper.getValue(data, "size/height");
	String widthStr = XMLHelper.getValue(data, "size/width");
	String readXPos = XMLHelper.getValue(data, "position/xpos");
	String readYPos = XMLHelper.getValue(data, "position/ypos");
				  
	Double tmpy = new Double(heightStr);
	Double tmpx = new Double(widthStr);
		
	width = tmpx.intValue();
	height = tmpy.intValue();

	tmpx = new Double(readXPos);
	tmpy = new Double(readYPos);
	
	xpos = tmpx.intValue();
	ypos = tmpy.intValue();
	

	Rectangle rectangle = new Rectangle();				
	rectangle.setBounds(xpos, ypos, width, height);
	settings.setBounds(rectangle);
	
	return true;
    }
    
    static boolean readModuleData(Element data) {
	int type;
	int group;
		
	String groupStr = XMLHelper.getAttribute(data, "group");
	String typeStr = XMLHelper.getAttribute(data, "type");

	Integer tmp = new Integer(groupStr);
	group = tmp.intValue();
	tmp = new Integer(typeStr);
	type = tmp.intValue();

	switch (type) {
	case Settings.CHARTMODULE:
	    moduleSettings = ChartModuleSettingsReader.read(data);
	    break;
	case Settings.WATCHSCREENMODULE:
	    moduleSettings = WatchScreenSettingsReader.read(data);
	    break;
	default:	   
	    return false;
	}
		
	return true;
    }
    
}

