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

/*
 * ProgressDialogManager.java
 *
 * Created on 5 February 2002, 21:48
 */

package org.mov.ui;
import java.util.Hashtable;

/**
 *
 * @author  Dan
 * @version 1.0
 *
 * This class maintains progress dialogs for the application on a thread by thread
 * basis.  Each thread has one ProgressDialog associated with it, and by using the 
 * static methods defined in this class, the user can access the specific dialog
 * to be used with the current thread from any point within the application.
 *
 * This approach allows multiple ProgressDialog objects to be used simultaneously
 * by multiple concurrent threads, while providing a single access point for users
 * to reference them from any point within their associated thread.
 */
public class ProgressDialogManager {
    /** List of all the currently defined Progress Dialogs available */
    private static Hashtable progress_dialogs = null;

    /** Prevent instantiation of ProgressDialogManager */
    private ProgressDialogManager() {
    }

    /**
     * Fetches the progress dialog associated with the current thread, creating
     * it on the fly if it doesn't exist as yet
     *
     * @return a reference to the progress dialog associated with the given thread
     */
    public static ProgressDialog getProgressDialog() {
        if (progress_dialogs == null)
            progress_dialogs = new Hashtable();
        
        ProgressDialog p = (ProgressDialog)progress_dialogs.get(Thread.currentThread());
        if (p == null) {
            progress_dialogs.put(Thread.currentThread(), (p = new ProgressDialog()));
        }
        return p;
    }
    
    /** Closes and removes the progress dialog associated with the current thread */
    public static void closeProgressDialog() {
        ProgressDialog p = (ProgressDialog)progress_dialogs.remove(Thread.currentThread());
        if (p != null) {
            p.dispose();
        }
    }
}
