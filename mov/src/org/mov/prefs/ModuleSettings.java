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
import org.mov.table.WatchScreenReader;
import org.mov.table.WatchScreenWriter;


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

public class ModuleSettings {
    int type;
    String key; //An identifier string
    Vector dataList; //A list of symbols etc

    
    public static final int CHARTMODULE = 0;
    public static final int TABLEMODULE = 1;
    public static final int ANALYSERMODULE = 2;
    public static final int PORTFOLIOMODULE = 3;
    public static final int PREFERENCESMODULE = 4;
    
    
    public ModuleSettings(String type, String key) {
	this.key = key;

	dataList = new Vector();
	
    }

    public ModuleSettings() {
	dataList = new Vector();
	
    }

    public void setKey(String key) {
	this.key = key;
    }

    public String getKey() {
	return key;
    }

    public void addData(String element) {
	dataList.add(element);
    }


    public Vector getDataList() {
	return dataList;
    }

    public int getType() {
	return type; 
    }

    public void setType(int type) {
	this.type = type;

	assert (type >= 0 && type <= PREFERENCESMODULE);
    }

    
}