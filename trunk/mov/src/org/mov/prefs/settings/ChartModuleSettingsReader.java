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

import java.util.Vector;
import java.util.HashMap;
import java.util.Iterator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.mov.prefs.settings.Settings;
import org.mov.prefs.settings.ChartModuleSettings;
import org.mov.prefs.settings.XMLHelper;

public class ChartModuleSettingsReader {

    private ChartModuleSettingsReader() {

    }

    public static ChartModuleSettings read(Element root) {
	ChartModuleSettings settings = new ChartModuleSettings();       
	Vector symbolList = XMLHelper.readList(root, 
					       "list", 
					       "symbolList",
					       "entry");

	Vector levelList = XMLHelper.readList(root, 
					      "list", 
					      "levels",
					      "entry");

	Vector graphList = null;
	Iterator levelIterator = levelList.iterator();
	Vector levelSettingsList = new Vector();
	Vector graphSettingsList = null;
	
	int i = 0;
	while (levelIterator.hasNext()) {
	    Node list = (Node)levelIterator.next();

	    Vector graphEntries = XMLHelper.readList((Element)list,
						     "list",
						     "graph",
						     "graph");

	    Iterator graphEntriesIterator = graphEntries.iterator();	    
	    graphList = new Vector();

	    while (graphEntriesIterator.hasNext()) {



		Node graphNode = (Node)graphEntriesIterator.next();
		
		String graphTitle = XMLHelper.getAttribute((Element)graphNode,
							   "name");

		String graphSymbol = XMLHelper.getAttribute((Element)graphNode,
							    "symbol");

		Vector chartSettings = XMLHelper.readList((Element)graphNode,
						      "set",
						      "entry");

		graphSettingsList = new Vector();
		Iterator chartSettingsIterator = chartSettings.iterator();

		HashMap setSettings = new HashMap();
		GraphSettings graphSettings = new 
		    GraphSettings(graphSymbol, graphTitle, graphTitle);
		
		while (chartSettingsIterator.hasNext()) {

		    Node hash = (Node)chartSettingsIterator.next();
		    String key = XMLHelper.getAttribute((Element)hash, "key");
		    String value = XMLHelper.getAttribute((Element)hash, 
							  "value");	           		    
		    setSettings.put(key, value);		    		    
		}
		graphSettings.put(setSettings);
		graphSettingsList.add(graphSettings);

		//Graph has no settings, but we want an entry for the
		//graph anyway so ChartModule can recreate it.
		if (graphSettingsList.size() == 0) {
		    GraphSettings empty = new GraphSettings("empty",
							    graphSymbol,
							    graphTitle);
		    HashMap emptyMap = new HashMap();
		    empty.put(emptyMap);
		    graphSettingsList.add(empty);
		}
		
		graphList.add(graphSettingsList);
	    }
	    levelSettingsList.add(graphList);	    
	    i++;		
	}
	settings.putLevelSettingsList(levelSettingsList);
	settings.putSymbolList(XMLHelper.nodeToStringList(symbolList));

	return settings;
    }
    
    
}