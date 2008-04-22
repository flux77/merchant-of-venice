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


/**
 * This is an interface for which Module settings writers must conform. It exists so that settings can be written at a top level without needing to know the exact module type. 
 * 
 * @author Mark Hummel
 * @see PreferencesManager
 * @see ModuleSettingsWriter
 * @see ModuleSettingReader 
*/

import java.util.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import org.mov.quote.Symbol;

public class WatchScreenSettingsWriter implements SettingsWriter {
    
    public WatchScreenSettingsWriter() {
	
    }
    
    public void write(Settings settings, Document document, Element parent) {
	WatchScreenSettings mySettings = (WatchScreenSettings)settings;
	
	List symbolList = mySettings.getSymbolList();
	Iterator iterator = symbolList.iterator();

	Element symbolListElement = (Element)document.createElement("list");
	symbolListElement.setAttribute("type","symbolList");
	parent.appendChild(symbolListElement);

	while (iterator.hasNext()) {
	    Symbol s = (Symbol)iterator.next();
	    Element entry = (Element)document.createElement("entry");
	    Text itemText = document.createTextNode(s.toString());
	    entry.appendChild(itemText);
	    symbolListElement.appendChild(entry);
	}
    }
}