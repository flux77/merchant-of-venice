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


package org.mov.ui;

import java.util.HashMap;
import java.util.Vector;
import org.mov.main.ModuleFrame;


/**
 *
 * FrameRegister is a collection of ModuleFrames. 
 * It is an implementation of a simple monitor and it's purpose is to
 * serialise the creation of new ModuleFrames. 
 *
 * FrameRegister was written initially for the purpose of restoring saved windows
 * as a way of accessing the new ModuleFrame objects but also as a way
 * of serialising their creation. The use of some non thread safe objects were 
 * discovered when trying to create multiple ChartModules and the decision was
 * made to serialise their creation under restoration rather than attempt to modify code that otherwise works perfectly well.

 * @author Mark Humel

 */

/* 
  FrameRegister is a HashMap rather than a Vector because although 
  Main.restoreSavedWindows() creates and access frames serially via an index,
  that's not sufficient reason to apply that restriction for other uses.

  
 */

public class FrameRegister extends HashMap {

    public FrameRegister() {
	super();
    }

    /**
     *
     * Add new ModuleFrame to register, using size as key
     * 
     * @param frame A new ModuleFrame 
     */
    
    public synchronized void add(ModuleFrame frame) {
	put(String.valueOf(size()),frame);
    }

    /**
     *
     * Register a new ModuleFrame identified by key
     * 
     * @param key The Identifier for a ModuleFrame
     * @param frame The ModuleFrame to associate with key
     */

    public synchronized void put(String key, ModuleFrame frame) {
	super.put(key, frame);
	notifyAll();

    }

    /**
     *
     * Return the ModuleFrame identified by key
     * 
     * The method will block if there is no ModuleFrame associated with
     * the key.
     * 
     * @param key The identifier for the ModuleFrame
     *     
     */

    public synchronized ModuleFrame get(String key) {

	ModuleFrame frame = (ModuleFrame)super.get(key);
	while (frame == null) {
	    try {
		wait();
	    } catch (InterruptedException ie) {
		return null;
	    }
	    frame = (ModuleFrame)super.get(key);
	}
	notifyAll();
	return frame;    
    }

}