package org.mov.ui;

import org.mov.main.*;

import java.util.*;

/**
 * Interface for classes that are listening for module events. Module
 * events are triggered when modules are added, removed or renamed.
 */
public interface ModuleListener extends EventListener {
   
    /**
     * Called when a module has been added
     *
     * @param	moduleEvent	the module event
     */
    public void moduleAdded(ModuleEvent moduleEvent);

    /**
     * Called when a module has been renamed
     *
     * @param	moduleEvent	the module event
     */
    public void moduleRenamed(ModuleEvent moduleEvent);

    /**
     * Called when a module has been removed
     *
     * @param	moduleEvent	the module event
     */
    public void moduleRemoved(ModuleEvent moduleEvent);
}
