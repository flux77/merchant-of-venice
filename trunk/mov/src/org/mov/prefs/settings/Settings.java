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
 * This provides an interface for Module settings which can used to restore modules upon restart. 
 * 
 * @author Mark Hummel
 * @see PreferencesManager
 * @see ModuleSettingsWriter
 * @see ModuleSettingReader 
*/

import javax.swing.JDesktopPane;
import java.util.*;
import org.mov.main.Module;

import org.mov.prefs.settings.SettingsWriter;

public interface Settings {
    

    //Groups
    public static final int FRAME    = -2;
    public static final int ROOTMODULE = -1;

    //Module Groups
    public static final int CHART    = 0;
    public static final int ANALYSER = 1;
    public static final int TABLE    = 2;
    public static final int PREFS    = 3;
    public static final int HELP     = 4;
    public static final int PORTFOLIO = 5;
    public static final int PREFERENCES = 6;

    //Module Types
    public static final int MODULE = -1;
    public static final int CHARTMODULE = 0;
    public static final int GRAPHS = 1;
    
    public static final int QUOTEMODULE = 2;
    public static final int WATCHSCREENMODULE = 3;
    public static final int HELPMODULE = 4; 
    public static final int PORTFOLIOMODULE = 5;
    public static final int PREFERENCESMODULE = 6;
    /**
     * Return the Module group as defined by the Settings constants
     * 
     * @return THe module group
     */
    public int getGroup();

    /**
     * Set the Module group as defined by the Settings constants
     * 
     * @param  group  The module group
     */
    public void setGroup(int group);

    /**
     * Return the module type as defined by the Setting constants.
     *
     * @return The module type
     */
    public int getType();

    /**
     * Set the module type.
     * 
     * @param type  a type constant
     */
    public void setType(int type);

    /**
     * Return the Module title. Is the same the screen title in the Module interface. 
     * 
     * @return  The screen title
     */
    public String getTitle();
    
    /**
     * Set the Module screen title
     * 
     * @param title   The screen title
     */
    public void setTitle(String title);


    
    /**
     * Return the module identifier
     * 
     * @return  The module identifier
     */
    
    public String getKey();

    /**
     * Set the module identifier. 
     *
     * @param  key   The module identifier
     * 
     */
   
    public void setKey(String key);
    
    /**
     *
     * Return a Module based on the settings.
     */
    public Module getModule(JDesktopPane desktop);


}