package org.mov.main;

import java.beans.*;
import javax.swing.*;

public interface Module
{
    public String getTitle();
    public void addModuleChangeListener(PropertyChangeListener listener);
    public void removeModuleChangeListener(PropertyChangeListener listener);
    public ImageIcon getFrameIcon();
    public JComponent getComponent();
    public JMenuBar getJMenuBar();
    public boolean encloseInScrollPane();
    public void save();
}

