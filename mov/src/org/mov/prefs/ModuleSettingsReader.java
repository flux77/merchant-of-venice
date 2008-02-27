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

import org.mov.quote.Symbol;
import org.mov.main.ModuleFrame;
import org.mov.util.Locale;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
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
import org.mov.prefs.ModuleSettingsParserException;
import org.mov.prefs.ModuleSettings;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;
import org.xml.sax.SAXException;

/**
 * This class writes watch screens in XML format.
 *
 * @author Mark Hummel
 * @see DesktopManager
 * @see PreferencesManager
 * @see ModuleSettingsWriter
 */
public class ModuleSettingsReader {

    private ModuleSettingsReader() {
        // Nothing to do
    }
   
        public static ModuleSettings read(InputStream stream) throws IOException, ModuleSettingsParserException {
	DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();

	ModuleSettings settings = null;

        try {
	    settings = new ModuleSettings();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document document = builder.parse(stream);
            Element moduleSettingsElement = (Element)document.getDocumentElement();

	    
	    NodeList childNodes = moduleSettingsElement.getChildNodes();
	    boolean foundDataElement = false;
	    boolean foundListItems = false;
	   	    
	    
	    for (int i = 0; i < childNodes.getLength() && !foundDataElement; i++) {
		if (childNodes.item(i).getNodeName().equals("data")) {
		    foundDataElement = true;

		    NamedNodeMap dataAttributes = childNodes.item(i).getAttributes();
		    Node dataKeyNode = dataAttributes.getNamedItem("key");
		    Node dataTypeNode = dataAttributes.getNamedItem("type");
		    
		    
		    if (dataTypeNode.getNodeValue().endsWith("ChartModule")) {
			settings.setType(ModuleSettings.CHARTMODULE);
		    } else if (dataTypeNode.getNodeValue().endsWith("TableModule")) {
			settings.setType(ModuleSettings.TABLEMODULE);
		    } else if (dataTypeNode.getNodeValue().endsWith("AnalyserModule")) {
			settings.setType(ModuleSettings.ANALYSERMODULE); 
		    } else if (dataTypeNode.getNodeValue().endsWith("PortfolioModule")) {
			settings.setType(ModuleSettings.PORTFOLIOMODULE); 
		    } else if (dataTypeNode.getNodeValue().endsWith("PreferencesModule")) {
			settings.setType(ModuleSettings.PORTFOLIOMODULE);
		    } else {
			throw new ModuleSettingsParserException(Locale.getString("MISSING_MODULE_TYPE_MODULE_SETTINGS"));
		    }
		    
		    settings.setKey(dataKeyNode.getNodeValue());
		    NodeList dataNodes = childNodes.item(i).getChildNodes();
		    		    
		    for (int j = 0; j < dataNodes.getLength() && !foundListItems; j++) {
			
			NodeList listNodes = dataNodes.item(i).getChildNodes();

			if (listNodes.item(0).getNodeName().equals("item")) {
			    foundListItems = true;
			    Node symbolNode = listNodes.item(0).getFirstChild();
			    String symbol = symbolNode.getNodeValue();
			    settings.addData(symbol);			    
			}
		    }
		    if (!foundListItems) 
			throw new ModuleSettingsParserException(Locale.getString("MISSING_LIST_ITEM_DATA_MODULE_SETTINGS"));
					
		}
		if (!foundDataElement) 
		    throw new ModuleSettingsParserException(Locale.getString("MISSING_DATA_ELEMENT_MODULE_SETTINGS"));
	    }	       
        } catch (SAXException e) {
            throw new ModuleSettingsParserException(e.getMessage());
        } catch(ParserConfigurationException e) {
            throw new ModuleSettingsParserException(e.getMessage());
        }
	
	return settings;
    } 

}