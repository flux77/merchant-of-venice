package org.mov.prefs;

import javax.swing.*;

/** 
 * Common interface for all preference pages displayed by the
 * Preferences Module.
 *
 * @see PreferencesModule
 */
public interface PreferencesPage {

    /**
     * Return the window title.
     *
     * @return	the window title.
     */
    public String getTitle();

    /**
     * Update the preferences file.
     */
    public void save();

    /**
     * Return displayed component for this page.
     *
     * @return the component to display.
     */
    public JComponent getComponent();

    /**
     * Return menu bar for quote source preferences module.
     *
     * @return	the menu bar.
     */
    public JMenuBar getJMenuBar();
}
