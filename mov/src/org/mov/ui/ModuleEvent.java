package org.mov.ui;

import org.mov.main.*;

import java.util.*;

/**
 * Representation of an event indicating that a module has been added,
 * removed or renamed.
 */
public class ModuleEvent extends EventObject {
   
    /**
     * Create a new module event based on the given module.
     *
     * @param	module	the module
     */
    public ModuleEvent(Module module) {
	super((Object)module);
    }
}
