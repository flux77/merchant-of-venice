package org.mov.ui;

import java.util.Vector;
import java.util.Comparator;
import java.awt.Dimension;
import java.awt.Dimension;
import java.awt.Component;
import java.awt.event.*;
import java.awt.BorderLayout;
import java.beans.PropertyVetoException;
import javax.swing.*;

import org.mov.main.*;

/**
 * This class manages activities to do with internal frames on the desktop
 */
public class DesktopManager 
    extends javax.swing.DefaultDesktopManager
    implements java.io.Serializable {  

    static private int DEFAULT_FRAME_WIDTH = 450;
    static private int DEFAULT_FRAME_HEIGHT = 375;

    public static final int HORIZONTAL          = 0,
	VERTICAL            = 1,                          
	CASCADE             = 2, 
	ARRANGE             = 3;

    private static JDesktopPane desktop_instance = null;

    public static void setDesktop(JDesktopPane desktop) {
	desktop_instance = desktop;
    }

    public static JDesktopPane getDesktop() {
	return desktop_instance;
    }

    public DesktopManager(JDesktopPane desktop) {
	super();
	setDesktop(desktop);
    }

    /**
     * Show a simple error message to the user.
     *
     * @param	message	the error message to display
     */
    public static void showErrorMessage(String message) {
	JOptionPane.showInternalMessageDialog(desktop_instance,
					      message, 
					      "Venice problem!",
					      JOptionPane.ERROR_MESSAGE);
    }

    // method to tile open windows in various styles:  
    //    HORIZONTAL = horizontal tiling  
    //    VERTICAL = vertical tiling  
    //    CASCADE = cascade windows, resizing based on desktop size  
    //    ARRANGE = arranges windows in a grid  

    public static void tileFrames(int style){    
	Dimension deskDim = desktop_instance.getSize();
	int deskWidth = deskDim.width;
	int deskHeight = deskDim.height;
	JInternalFrame[] frames = desktop_instance.getAllFrames();
	int frameCount = frames.length;
	int frameWidth=0;
	int frameHeight=0;
	int xpos=0;
	int ypos=0;
	double scale = 0.6;
	int spacer=30;
	int frameCounter=0;
	Vector frameVec=new Vector(1,1);
	boolean areIcons=false;
	int tempy=0,tempx=0;
	for (int i =0; i< frameCount; i++) {
	    if (frames[i].isVisible() && !frames[i].isIcon()) {
		frameVec.addElement(frames[i]);
		frameCounter++;
	    }      
	    else if(frames[i].isIcon()) 
		areIcons=true;
	}    
	if(areIcons) 
	    deskHeight = deskHeight - 50;
	switch(style){
	case(HORIZONTAL):
	    for (int i=0; i<frameCounter; i++){
		JInternalFrame temp = (JInternalFrame) frameVec.elementAt(i);
		frameWidth = deskWidth;
		frameHeight = (int)(deskHeight/frameCounter);
		if (temp.isResizable()) 
		    temp.reshape(xpos, ypos, frameWidth, frameHeight);
		else 
		    temp.setLocation(xpos,ypos);
		ypos = ypos+frameHeight;
		temp.moveToFront();
	    }        
	    break;

	case(VERTICAL): 
	    for (int i=0; i<frameCounter; i++){
		JInternalFrame temp = (JInternalFrame) frameVec.elementAt(i);
		frameWidth = (int)(deskWidth/frameCounter);
		frameHeight = deskHeight;
		if (temp.isResizable()) 
		    temp.reshape(xpos, ypos, frameWidth, frameHeight);
		else 
		    temp.setLocation(xpos,ypos);
		xpos = xpos+frameWidth;
		temp.moveToFront();
	    }        
	    break;
	case(CASCADE):
	    for (int i=0; i<frameCounter; i++){
		JInternalFrame temp = (JInternalFrame) frameVec.elementAt(i);
		frameWidth =  (int)(deskWidth*scale);
		frameHeight = (int)(deskHeight*scale);
		if (temp.isResizable()) 
		    temp.reshape(xpos, ypos, frameWidth, frameHeight);
		else 
		    temp.setLocation(xpos,ypos);
		temp.moveToFront();
		xpos=xpos+spacer;
		ypos=ypos+spacer;
		if((xpos+frameWidth>deskWidth)||(ypos+frameHeight>deskHeight-50)){ 
		    xpos=0;
		    ypos=0;
		}        
	    }
	    break;
	case(ARRANGE):
	    int row=new Long(Math.round(Math.sqrt(new Integer(frameCounter).doubleValue()))).intValue();
	    if(row==0) 
		break;
	    int col=frameCounter/row;
	    if (col ==0) 
		break;
	    int rem=frameCounter%row;
	    int rowCount=1;
	    frameWidth = (int) deskWidth/col;
	    frameHeight = (int) deskHeight/row;
	    for (int i=0; i<frameCounter; i++){
		JInternalFrame temp = (JInternalFrame) frameVec.elementAt(i);
		if(rowCount<=row-rem) {
		    if (temp.isResizable()) 
			temp.reshape(xpos,ypos,frameWidth,frameHeight);
		    else 
			temp.setLocation(xpos,ypos);
		    if(xpos+10<deskWidth-frameWidth) 
			xpos=xpos+frameWidth;
		    else { 
			ypos=ypos+frameHeight;
			xpos=0;
			rowCount++;
		    }          
		}          
		else 
		    {
			frameWidth = (int)deskWidth/(col+1);
			if (temp.isResizable()) 
			    temp.reshape(xpos,ypos,frameWidth,frameHeight);
			else 
			    temp.setLocation(xpos,ypos);
			if(xpos+10<deskWidth-frameWidth) 
			    xpos=xpos+frameWidth;
			else { 
			    ypos=ypos+frameHeight;
			    xpos=0;
			}          
		    }
	    }
	    break;
	default:
	    break;
	}  
    }  

    // method to minimize all windows that are iconifiable  
    public static void minimizeWindows() {
	JInternalFrame[] openWindows = desktop_instance.getAllFrames();
	for (int i=0; i<openWindows.length; i++)
	    if(openWindows[i].isIconifiable()) {
		try { 
		    openWindows[i].setIcon(true);
		}          
		catch (java.beans.PropertyVetoException pve) { 
		    pve.printStackTrace();
		}        
	    } 
    }
    
    // method to restore all minimized windows  
    public static void restoreAll() {
	JInternalFrame[] openWindows = desktop_instance.getAllFrames();
	for(int i=0; i<openWindows.length; i++) {
	    if(openWindows[i].isIcon())
		try {
		    openWindows[i].setIcon(false);
		}      
		catch (java.beans.PropertyVetoException pve) { 
		    pve.printStackTrace();
		}    
	} 
    }  
    
    // method to close all open windows  
    public static void closeAllWindows() {
	JInternalFrame[] openWindows = desktop_instance.getAllFrames();
	for (int i=0; i<openWindows.length; i++) { 
	    openWindows[i].dispose();
	}  
    }  
    
    private static void _windowSelected(ActionEvent e, JInternalFrame f) { 
	try {
	    if(f.isIcon())
		f.setIcon(false);
	    f.moveToFront();
	}   
	catch (Exception ex) { 
	    ex.printStackTrace();
	}  
    }    
    
    //Comparator dedicated to sorting objects (File or JInternalFrame) by name  
    //used to sort files in windowMenu so as to display dynamicaly files currently contained in desktop.  
    public static final Comparator FILE_NAME = new Comparator() {    
	    public int compare(Object one, Object two) {
		String name1="", name2="";
		if ((one instanceof JInternalFrame) && (two instanceof JInternalFrame)){
		    name1=((JInternalFrame)one).getTitle();
		    name2=((JInternalFrame)two).getTitle();
		}      
		int index=0;
		int test=0;
		while(test==0){     
		    if (name1.charAt(index)==(name2.charAt(index))) { 
			//System.out.println(name1+" "+name1.charAt(index)+"    "+name2+" "+name2.charAt(index)+"  ("+index+")");
			test=0;
			index++;
			if((index>=name1.length())|(index>=name2.length())) 
			    break;
		    }       
		    else if (name1.charAt(index)>name2.charAt(index))  
			test= 1;
		    else test = -1;
		}//while      
		return test;
	    }   
	    
	    public boolean equals(Object object) { 
		return object.equals(this);
	    }    
	    
	    public String toString(){ return "FILE_NAME";
	    }  
	};

    /**
     * Display a new frame upon the current desktop. Frame will be
     * displayed at (0,0) and not centred.
     * 
     * @param module the module to render in the frame
     */
    public void newFrame(Module module) {
	newFrame(module, false, false);
    }

    /**
     * Display a new frame upon the current desktop
     *
     * @param module the module to render in the frame
     * @param centre should the frame be centred?
     * @param honourSize should we honour the frame's preferred size?
     */
    public void newFrame(Module module, boolean centre, boolean honourSize) {

	ModuleFrame frame = new ModuleFrame(module, centre, honourSize);
	desktop_instance.add(frame);
	
	try {
	    frame.setSelected(true);
	}
	catch(PropertyVetoException v) {
	    // ignore
	}
	
	frame.moveToFront();		    
    }

    /**
     * Call save() on every open module. This will save all the modules'
     * preferences data.
     */
    public void save() {
	// Get all frames open
	JInternalFrame[] frames = desktop_instance.getAllFrames();

	for(int i = 0; i < frames.length; i++) {
	    // Only call save() on modules - these are identified by
	    // being module frames.
	    JInternalFrame frame = frames[i];

	    if(frame instanceof ModuleFrame) {
		ModuleFrame moduleFrame = (ModuleFrame)frame;
		moduleFrame.getModule().save();
	    }
	}
    }
}


