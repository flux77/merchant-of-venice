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
import org.mov.quote.SymbolFormatException;
import org.mov.util.Locale;

import java.io.InputStream;
import java.io.IOException;
import java.awt.Dimension;
import java.awt.Rectangle;

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

import org.mov.prefs.ModuleSettingsParserException;
import org.mov.main.ModuleFrame;
import org.mov.prefs.ModuleFrameSettings;
import org.mov.prefs.ModuleSettings;


import org.xml.sax.SAXException;

/**
 * This class parses watch screens written in XML format.
 *
 * @author Mark Hummel
 * @see ModuleFrame
 * @see ModuleSettingsWriter
 */
public class ModuleFrameSettingsReader {

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
	
	ModuleFrameSettings settings = new ModuleFrameSettings();
        Rectangle rectangle = null;
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document document = builder.parse(stream);
            Element ModuleFrameSettings = (Element)document.getDocumentElement();
            NamedNodeMap moduleFrameSettingsAttributes = ModuleFrameSettings.getAttributes();
            Node moduleFrameNameNode = moduleFrameSettingsAttributes.getNamedItem("name");

	    Node moduleKeyNode = moduleFrameSettingsAttributes.getNamedItem("module");



            if(moduleFrameNameNode == null)
                throw new ModuleSettingsParserException(Locale.getString("MISSING_WINDOW_FRAME_NAME_ATTRIBUTE"));

	    if(moduleKeyNode == null)
                throw new ModuleSettingsParserException(Locale.getString("MISSING_MODULE_KEY_ATTRIBUTE"));
	    
            String frameName = moduleFrameNameNode.getNodeValue();
	    String moduleKey = moduleKeyNode.getNodeValue();

	    
	    settings.setModuleKey(moduleKey);

            NodeList childNodes = ModuleFrameSettings.getChildNodes();
	    boolean readGeometry = false;

	    for (int i = 0; i < childNodes.getLength() && !readGeometry; i++) {
		

		if (childNodes.item(i).getNodeName().equals("data")) {
		    NodeList dataNodes = childNodes.item(i).getChildNodes();

		    int height = 0, width = 0;
		    int xpos = 0, ypos = 0;		    

		    if (dataNodes.item(0).getNodeName().equals("size")) {
			NodeList sizeNodes = dataNodes.item(0).getChildNodes();
			
			for (int j = 0; j < sizeNodes.getLength(); j++) {
			    if (sizeNodes.item(j).getNodeName().equals("height")) {
				Node heightNode = sizeNodes.item(j).getFirstChild();
				height = new Double(heightNode.getNodeValue()).intValue();		    
			    }
			}
			for (int j = 0; j < sizeNodes.getLength(); j++) {
			    if (sizeNodes.item(j).getNodeName().equals("width")) {
				Node heightNode = sizeNodes.item(j).getFirstChild();
				width = new Double(heightNode.getNodeValue()).intValue();		    
			    }
			}
			
		    }		    
		    if (dataNodes.item(1).getNodeName().equals("position")) {
			NodeList positionNodes = dataNodes.item(1).getChildNodes();

			for (int j = 0; j < positionNodes.getLength(); j++) {
			    if (positionNodes.item(j).getNodeName().equals("xpos")) {
				
				Node xposNode = positionNodes.item(j).getFirstChild();
				xpos = new Double(xposNode.getNodeValue()).intValue();
			    }
			    if (positionNodes.item(j).getNodeName().equals("ypos")) {
				
				Node yposNode = positionNodes.item(j).getFirstChild();
				ypos = new Double(yposNode.getNodeValue()).intValue();
			    }
			}
		    }
		    
		    rectangle = new Rectangle();				
		    rectangle.setBounds(xpos, ypos, width, height);
		    readGeometry = true;
		    settings.setBounds(rectangle);
		}
	    }
	    if (!readGeometry) {
		throw new ModuleSettingsParserException(Locale.getString("MODULE_SETTINGS_TOP_LEVEL_ERROR"));		
	    }
        } catch (SAXException e) {
            throw new ModuleSettingsParserException(e.getMessage());
        } catch(ParserConfigurationException e) {
            throw new ModuleSettingsParserException(e.getMessage());
        }

        return settings;
    }
    
}

