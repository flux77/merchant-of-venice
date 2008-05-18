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
 * This class  represents WatchScreen Module data  which can restore modules upon restart. 
 * 
 * @author Mark Hummel
 * @see PreferencesManager
 * @see ModuleSettingsWriter
 * @see ModuleSettingReader 
*/

import javax.swing.JDesktopPane;
import java.util.*;

import org.mov.main.Module;
import org.mov.help.HelpModule;
import org.mov.prefs.PreferencesManager;
import org.mov.prefs.PreferencesException;

public class HelpModuleSettings extends AbstractSettings {
    

    private int positionInStack;
    private Stack visitedPages;

    public HelpModuleSettings() {
	super(Settings.TABLE, Settings.HELPMODULE);
    }

    public HelpModuleSettings(String title) {
	super(Settings.TABLE, Settings.HELPMODULE);
	super.setTitle(title);
    }

    public Stack getVisitedPages() {
	return visitedPages;
    }

    public void setVisitedPages(Stack visitedPages) {
	this.visitedPages = visitedPages;
    }

    public int getPositionInStack() {
	return positionInStack;
    }

    public void setPositionInStack(int positionInStack) {
	this.positionInStack = positionInStack;
    }

    public Module getModule(JDesktopPane desktop) {	
	return new HelpModule(desktop, this);
    }
}