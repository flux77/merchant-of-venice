package org.mov.table;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

import org.mov.util.*;
import org.mov.ui.*;

public class AbstractTable extends SortedTable {
    
    // Default values for rendering table rows
    private static final Color backgroundColor = Color.white;
    private static final Color alternativeBackgroundColor = 
	new Color(237, 237, 237);

    // Images used for arrows representing when stock has gone up, down or is unchanged
    private String upImage = "org.mov/images/Up.gif";
    private String downImage = "org.mov/images/Down.gif";
    private String unchangedImage = "org.mov/images/Unchanged.gif";

    class StockQuoteRenderer extends JPanel implements TableCellRenderer,
						       MouseListener
    {

	private JLabel textLabel = new JLabel();
	private JLabel iconLabel = new JLabel();
	private Component glue = Box.createHorizontalGlue();

	public StockQuoteRenderer() {
	    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
	}
	
	public Component getTableCellRendererComponent(JTable table,
						       Object value,
						       boolean isSelected,
						       boolean hasFocus,
						       int row, int column) {
	    AbstractTable t = (AbstractTable) table;

	    // Set font to match default font
	    textLabel.setFont(table.getFont());

	    // Set foreground colour to match default foreground colour
	    textLabel.setForeground(table.getForeground());

	    // Make each alternate row a different colour
	    if(isSelected)
		setBackground(table.getSelectionBackground());
	    else
		setBackground(row % 2 != 0?
			      backgroundColor:
			      alternativeBackgroundColor);

	    // The change column has specific rendering
	    if(value instanceof Change) 
		renderChangeComponent(table, value, isSelected,
				      hasFocus, row, column); 
	    else if(value instanceof TradingDate) {
		TradingDate date = (TradingDate)value;

		String text = date.toString("d?/m?/yyyy");
		textLabel.setText(text);
		add(textLabel);
	    }
	    else {
		textLabel.setText(value.toString());
		add(textLabel);
	    }
	    	    
	    return this;
	}

	private void renderChangeComponent(JTable table, Object value,
					   boolean isSelected, 
					   boolean hasFocus, 
					   int row, int column) {

	    Change change = (Change)value;
	    double changePercent = change.getChange();
	    String text;

	    if(changePercent > 0)
		text = "+" + Double.toString(changePercent) + "%";
	    else
		text = Double.toString(changePercent) + "%";

	    textLabel.setText(text);

	    if(changePercent > 0) {
		// Create up arrow
		ImageIcon upImageIcon = 
		    new ImageIcon(ClassLoader.getSystemResource(upImage));
		iconLabel.setIcon(upImageIcon);
	    }

	    else if(changePercent < 0) {
		// Create down arrow
		ImageIcon downImageIcon = 
		    new ImageIcon(ClassLoader.getSystemResource(downImage));
		iconLabel.setIcon(downImageIcon);
	    }
	    else {
		// Create down arrow
		ImageIcon unchangedImageIcon = 
		    new ImageIcon(ClassLoader.getSystemResource(unchangedImage));
		iconLabel.setIcon(unchangedImageIcon);
	    }

	    add(glue);
	    add(textLabel);
	    add(iconLabel);
	}

	public void checkPopup(MouseEvent e) {
	    //	    System.out.println("pop up");

	    //    if (e.isPopupTrigger()) 
	    ///		Table.popup.show(this, e.getX(), e.getY());
	}
	
	public void mousePressed(MouseEvent e) { checkPopup(e); }
	public void mouseClicked(MouseEvent e) { checkPopup(e); }
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {} 
	public void mouseReleased(MouseEvent e) { checkPopup(e); }
    }

    public AbstractTable() {

	setShowGrid(true);

	// Our own stock quote renderer
	setDefaultRenderer(Change.class, 
			   new StockQuoteRenderer());
	setDefaultRenderer(String.class, 
			   new StockQuoteRenderer());
	setDefaultRenderer(Integer.class, 
			   new StockQuoteRenderer());
	setDefaultRenderer(Double.class, 
			   new StockQuoteRenderer());
	setDefaultRenderer(Date.class, 
			   new StockQuoteRenderer());
	setDefaultRenderer(Float.class, 
			   new StockQuoteRenderer());
	setDefaultRenderer(TradingDate.class, 
			   new StockQuoteRenderer());
    }

    public void setModel(TableModel model) {
	super.setModel(model);
    }
}


