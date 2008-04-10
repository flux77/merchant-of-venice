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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.prefs.BackingStoreException;

import org.mov.main.Main;
import org.mov.macro.StoredMacro;
import org.mov.quote.Symbol;
import org.mov.quote.SymbolFormatException;
import org.mov.table.WatchScreen;
import org.mov.table.WatchScreenParserException;

import org.mov.prefs.settings.ChartModuleSettings;
import org.mov.prefs.settings.GraphSettings;
import org.mov.prefs.settings.GraphSettingsWriter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import org.mov.main.ModuleFrame;
import java.util.Collection;
import java.util.Vector;

/**
 * This class represents Module data which can be saved for the purposes
 *  of restoring the modules upon restart.
 * 
 * @author Mark Hummel
 * @see PreferencesManager
 * @see ModuleSettingsWriter
 * @see ModuleSettingReader 
*/

public class ChartModuleSettingsWriter implements SettingsWriter {
    
    //Nothing to do
    public ChartModuleSettingsWriter() {	
    }

    /**
     * Write the ChartModule settings to the output stream in XML.
     *
     * @param settings   The ChartModule settings
     * @param Document   The root document
     * @param parent     The parent element to which data is appended.
     */

    public void write(Settings settings, Document document, Element parent) {
	
	ChartModuleSettings mySettings = (ChartModuleSettings)settings; 

	List symbolList = mySettings.getSymbolList();
	List levelSettingsList = mySettings.getLevelSettingsList();
	
	Element symbolListElement = (Element)document.createElement("list");
	symbolListElement.setAttribute("type","symbolList");
	parent.appendChild(symbolListElement);
	
	Iterator symbolIterator = symbolList.iterator();
	while (symbolIterator.hasNext()) {
	    Element entry = (Element)document.createElement("entry");
	    Text itemText = document.
		createTextNode((String)symbolIterator.next());
	    entry.appendChild(itemText);
	    symbolListElement.appendChild(entry);
	}
	
	Element levelList = (Element)document.createElement("list");
	levelList.setAttribute("type","levels");
		
	Iterator levelIterator = levelSettingsList.iterator();
	int index = 0;

	while (levelIterator.hasNext()) {	    
	    Element entry = (Element)document.createElement("entry");
	    Element graphListElement = (Element)document.createElement("list");
	    graphListElement.setAttribute("type","graph");	    
	    graphListElement.setAttribute("symbol", (String)symbolList.get(index));
	    levelList.appendChild(entry);
	    entry.appendChild(graphListElement);

	    List graphList = (List)levelIterator.next();
	    Iterator graphIterator = graphList.iterator();
	    while (graphIterator.hasNext()) {
		Element graphElement = (Element)document.createElement("graph");
		GraphSettings graphSettings = (GraphSettings)graphIterator.
		    next();

		graphElement.setAttribute("name", graphSettings.getTitle());
		graphElement.setAttribute("symbol", (String)symbolList.get(index++));

		graphListElement.appendChild(graphElement);
		GraphSettingsWriter graphWriter = (GraphSettingsWriter)graphSettings.getWriter();
		graphWriter.write(graphSettings, document, graphElement);		
	    }
	}
	parent.appendChild(levelList);
    }  

}