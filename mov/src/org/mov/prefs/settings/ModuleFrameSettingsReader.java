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
import java.io.BufferedInputStream;
import java.io.IOException;
import java.beans.XMLDecoder;
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
	
	BufferedInputStream buffStream = new BufferedInputStream(stream);
	XMLDecoder xStream = new XMLDecoder(buffStream);
	Object result = xStream.readObject();
	xStream.close();

	return (ModuleFrameSettings)result;
    }
}
