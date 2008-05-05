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
import java.util.Stack;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.mov.prefs.settings.Settings;
import org.mov.prefs.settings.ChartModuleSettings;
import org.mov.prefs.settings.XMLHelper;
import org.mov.help.HelpPage;

public class HelpSettingsReader {

    private HelpSettingsReader() {

    }

    public static HelpModuleSettings read(Element root) {
	HelpModuleSettings settings = new HelpModuleSettings();       
	Vector pagesList = XMLHelper.readList(root, 
					       "list", 
					       "visitedPages",
					      "entry");
	
	pagesList = XMLHelper.nodeToStringList(pagesList);

	Stack visitedPages = new Stack();
	Vector positionList = 
	    XMLHelper.nodeToStringList(XMLHelper.
				       getChildrenByTagName(root, "position")
				       );
	
	String positionStr = (String)positionList.get(0);
   
	try {
	    settings.setPositionInStack(new Integer(positionStr).intValue());
	} catch (NumberFormatException e) {

	}

	Iterator iterator = pagesList.iterator();
	while (iterator.hasNext()) {
	    String name = (String)iterator.next();
	    HelpPage page = new HelpPage(name);
	    visitedPages.push(page);
	    
	}
	
	settings.setVisitedPages(visitedPages);

	return settings;
    }
    
}