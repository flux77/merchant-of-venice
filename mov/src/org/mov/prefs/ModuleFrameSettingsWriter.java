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
public class ModuleFrameSettingsWriter {

    private ModuleFrameSettingsWriter() {
        // Nothing to do
    }

    /**
     * Write the window settings to the output stream in XML format.
     *
     * @param frame the module frame data to write
     * @param stream      the output stream to write the window settings.
     */
    public static void write(ModuleFrame frame, OutputStream stream) {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document document = builder.newDocument();

            Element frameSettingsElement = (Element)document.createElement("frame");
            frameSettingsElement.setAttribute("name", "FrameDataFor" + frame.getTitle() + "_" + frame.hashCode());

	    String key = String.valueOf(frame.getModule().hashCode());

	    frameSettingsElement.setAttribute("module",key);
            document.appendChild(frameSettingsElement);


	    Element dataElement = (Element)document.createElement("data");
	    frameSettingsElement.appendChild(dataElement);

            Element sizeElement = (Element)document.createElement("size");
	    Element positionElement = (Element)document.createElement("position");
	    dataElement.appendChild(sizeElement);
	    dataElement.appendChild(positionElement);

	    Rectangle bounds = frame.getBounds();
	    
	    addElement("height", bounds.getHeight(), sizeElement, document);
	    addElement("width", bounds.getWidth(), sizeElement, document);
	    addElement("xpos", bounds.getX(), positionElement, document);
	    addElement("ypos", bounds.getY(), positionElement, document);
	    			           	    
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();

            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(stream);
            transformer.transform(source, result);
        }
        catch(ParserConfigurationException e) {
            // This should not occur
            assert false;
        }
        catch(TransformerException e) {
            // This should not occur
            assert false;
        }
    }

    private static void addElement(String name, double value, Element parent,
			    Document doc) {
	Element newElement = (Element)doc.createElement(name);
	String str = String.valueOf(value);
	Text textNode = doc.createTextNode(str);
	newElement.appendChild(textNode);
	parent.appendChild(newElement);
	
    }

}