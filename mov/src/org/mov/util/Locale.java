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

/**
 * This class implements internationalisation support for Venice. Each text message
 * displayed by Venice is represented by a string symbol (e.g. "OPEN") which describes
 * the error message being displayed. This symbol is then matched to a
 * internationalistaion file which will then retrieve the local language version of
 * that error message.
 * <p>
 * If a match could not be found in the current language, it will try to match the
 * symbol in the english (default) language. If it could not find a match there,
 * it will display the string <code>Locale.UNKNOWN</code>.
 * <p>
 * The internationalisation files are kept in src/org/mov/util/locale/.
 * <p>
 * @see java.util.ResourceBundle
 */
public class Locale {
    // This is the string we use if we can't find a matching entry in
    // any of the resource bundles.
    private final static String UNKNOWN = "???";

    private static ResourceBundle primaryResourceBundle = null;
    private static ResourceBundle secondaryResourceBundle = null;

    private static boolean resourceBundlesLoaded = false;

    private Locale() {
        // This class is never instantiated
        assert false;
    }

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

    // In the given source string replace all occurences of patternText with
    // text. This function appears elsewhere, it needs to be refactored.
    private static String replace(String source, String patternText, String text) {
	Pattern pattern = Pattern.compile(patternText);
	Matcher matcher = pattern.matcher(source);
	return matcher.replaceAll(text);
    }

    /**
     * Return the current langauge translation of the text associated
     * with the given key.
     *
     * @param key a key which represents a line of text
     * @return the text
     */
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

    /**
     * Return the current langauge translation of the text associated
     * with the given key. Insert the given argument into the text
     * translation. For example if the text object in the internationalisation 
     * file looks like:
     *
     * <pre>Generation %1</pre>
     *
     * The first argument will replace <code>%1</code>.
     *
     * @param key a key which represents a line of text
     * @param arg1 the first argument
     * @return the text
     */
    public static String getString(String key, String arg1) {
	String string = getString(key);
	
	return replace(string, "%1", arg1);	
    }

    /**
     * Return the current langauge translation of the text associated
     * with the given key. Insert the given arguments into the text
     * translation. For example if the text object in the internationalisation 
     * file looks like:
     *
     * <pre>Generation %1 of %2</pre>
     *
     * The first argument will replace <code>%1</code> and the second
     * argument will replace <code>%2</code>.
     *
     * @param key a key which represents a line of text
     * @param arg1 the first argument
     * @param arg2 the second argument
     * @return the text
     */
    public static String getString(String key, String arg1, String arg2) {
	String string = getString(key);
	
	string = replace(string, "%1", arg1);	
	return replace(string, "%2", arg2);
    }

    /**
     * Return the current langauge translation of the text associated
     * with the given key. Insert the given arguments into the text
     * translation. For example if the text object in the internationalisation 
     * file looks like:
     *
     * <pre>%1 of %2 (%3%)</pre>
     *
     * The first argument will replace <code>%1</code> and the second
     * argument will replace <code>%2</code> and the third argument
     * will replace <code>%3</code>.
     *
     * @param key a key which represents a line of text
     * @param arg1 the first argument
     * @param arg2 the second argument
     * @param arg3 the third argument
     * @return the text
     */
    public static String getString(String key, String arg1, String arg2, String arg3) {
	String string = getString(key);
	
	string = replace(string, "%1", arg1);	
	string = replace(string, "%2", arg2);	
	return replace(string, "%3", arg3);	
    }

    /**
     * Return the current langauge translation of the text associated
     * with the given key. Insert the given argument into the text
     * translation. For example if the text object in the internationalisation 
     * file looks like:
     *
     * <pre>Generation %1</pre>
     *
     * The first argument will replace <code>%1</code>.
     *
     * @param key a key which represents a line of text
     * @param arg1 the first argument
     * @return the text
     */
    public static String getString(String key, int arg1) {
	return getString(key, Integer.toString(arg1));
    }

    /**
     * Return the current langauge translation of the text associated
     * with the given key. Insert the given arguments into the text
     * translation. For example if the text object in the internationalisation 
     * file looks like:
     *
     * <pre>Generation %1 of %2</pre>
     *
     * The first argument will replace <code>%1</code> and the second
     * argument will replace <code>%2</code>.
     *
     * @param key a key which represents a line of text
     * @param arg1 the first argument
     * @param arg2 the second argument
     * @return the text
     */
    public static String getString(String key, int arg1, int arg2) {
	return getString(key, Integer.toString(arg1), Integer.toString(arg2));
    }
}