/*
 * Preferences.java
 *
 * Created on 29 January 2002, 20:04
 *
 * This class provides a convenient way for all parts of the Venice system to
 * obtain preference information without violating Preferences namespace convention
 */

package org.mov.prefs;

import java.util.prefs.Preferences;
/**
 *
 * @author  Dan
 * @version 1.0
 */
public class PreferencesManager {

    /** The base in the prefs tree where all Venice settings are stored */
    private final static String base = "org.mov";
    
    /** The user root from Venice's point of view */
    private static Preferences user_root = Preferences.userRoot().node(base);
  
    /** Fetches the root user node that parts of Venice may access */
    public static java.util.prefs.Preferences userRoot() {
        return user_root;
    }

    /** Fetches the desired user node, based at the <code>base</code> branch
     * @param node the path to the node to be fetched
     */
    public static java.util.prefs.Preferences getUserNode(String node) {
        if (node.charAt(0) == '/') node = node.substring(1);
        return user_root.node(node);
    }
    
}
