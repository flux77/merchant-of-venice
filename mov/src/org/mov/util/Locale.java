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

package org.mov.util;

import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Locale {
    // This is the string we use if we can't find a matching entry in
    // any of the resource bundles.
    private final static String UNKNOWN = "???";

    private static ResourceBundle primaryResourceBundle = null;
    private static ResourceBundle secondaryResourceBundle = null;

    private static boolean resourceBundlesLoaded = false;

    private static synchronized void loadResourceBundles() {
	if(!resourceBundlesLoaded) {
	    // First get the user's preferred language
	    try {
		primaryResourceBundle = ResourceBundle.getBundle("org.mov.util.locale.venice");
	    }
	    catch(Exception e) {
		// It's OK if we couldn't load the user's preferred locale
	    }

	    // Also load English as a fallback if the preferred language hasn't
	    // been fully transalted.
	    try {
		secondaryResourceBundle = ResourceBundle.getBundle("org.mov.util.locale.venice",
								   java.util.Locale.ENGLISH);
	    }
	    catch(Exception e) {
		// This should have worked.
		System.err.println(e);
		assert false;
	    }

	    resourceBundlesLoaded = true;
	}
    }

    public static String getString(String key) {
	String string = null;

	loadResourceBundles();

	if(primaryResourceBundle != null)
	    try {
		string = primaryResourceBundle.getString(key);
	    }
	    catch(MissingResourceException e) {
		// try secondary (english) text
	    }

	if(string == null && secondaryResourceBundle != null)
	    try {
		string = secondaryResourceBundle.getString(key);
	    }
	    catch(MissingResourceException e) {
		// use ???
	    }

	if(string == null)
	    string = UNKNOWN;

	return string;
    }

    public static String getString(String key, String arg1) {
	String string = getString(key);
	
	return replace(string, "%1", arg1);	
    }

    public static String getString(String key, String arg1, String arg2, String arg3) {
	String string = getString(key);
	
	string = replace(string, "%1", arg1);	
	string = replace(string, "%2", arg2);	
	return replace(string, "%3", arg3);	
    }

    public static String getString(String key, int arg1) {
	return getString(key, Integer.toString(arg1));
    }

    // In the given source string replace all occurences of patternText with
    // text.
    private static String replace(String source, String patternText, String text) {
	Pattern pattern = Pattern.compile(patternText);
	Matcher matcher = pattern.matcher(source);
	return matcher.replaceAll(text);
    }
}