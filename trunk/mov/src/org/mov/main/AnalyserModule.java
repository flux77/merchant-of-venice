package org.mov.main;

import java.beans.*;
import javax.swing.*;

public interface AnalyserModule
{
    public String getTitle();
    public void addPropertyChangeListener(PropertyChangeListener listener);
    public void removePropertyChangeListener(PropertyChangeListener listener);
    public ImageIcon getFrameIcon();
    public JComponent getComponent();
    public JMenuBar getJMenuBar();
    public boolean encloseInScrollPane();
}

