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
import javax.swing.JDesktopPane;

import org.mov.main.Main;
import org.mov.main.Module;
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
 * This class can save the toplevel Module data which is common to all modules.
 * 
 * @author Mark Hummel
 * @see Settings
 * @see SettingsWriter
*/

public abstract class AbstractSettings implements Settings {

        
    private int group;
    private int type;
    private String name;

    private String key;
    private String title;

    public AbstractSettings(int group, int type) {
	this.group = group;
	this.type = type;

	name = this.getClass().getName();
    }

    public AbstractSettings(int group, int type, String key) {
	this.group = group;
	this.type = type;
	this.key = key;	

	name = this.getClass().getName();
	

    }

    /**
     * Set the title of the Module.
     * 
     * @param title The module title
     */

    public void setTitle(String title) {
	this.title = title;
    }

    /**
     *
     * Get the title of the module
     * 
     * @return The module title
     */

    public String getTitle() {
	return title;
    }

    public int getType() {
	return type;
    }

    public int getGroup() {
	return group;
    }

    public void setGroup(int group) {
	this.group = group;
    }

    public void setType(int type) {
	this.type = type;
    }

    public void setKey(String key) {
	this.key = key;
    }

    public String getKey() {
	return key;
    }
    
    public Module getModule(JDesktopPane desktop) {
	return null;
    }

    public String toString() {
	String rv = "Group: " + String.valueOf(group) + 
	    "Type: " + String.valueOf(type) +
	    "Title: " + title;

	return rv;
    }

}