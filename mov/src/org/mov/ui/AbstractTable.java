package org.mov.table;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

import org.liquid.misc.*;
import org.liquid.table.*;
import org.mov.util.*;

public class AbstractTable extends SortedTable {
    
    // Default values for rendering table rows
    private static final Color backgroundColor = Color.white;
    private static final Color alternativeBackgroundColor = 
	new Color(237, 237, 237);

    private int redColumn = -1;
    private int greenColumn = -1;

    class ChangeComparator implements Comparator {

	public int compare(Object firstObject, Object secondObject)
	{
	    Change first = (Change)firstObject;
	    Change second = (Change)secondObject;

	    if(first.getChange() < second.getChange())
		return -1;
	    if(first.getChange() > second.getChange())
		return 1;
	    else
		return 0;
	}
    }

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
	    // or set it to red/green if its the red/green colum
	    if(t.getRedColumn() == column)
		textLabel.setForeground(Color.red.darker());
	    else if(t.getGreenColumn() == column)
		textLabel.setForeground(Color.green.darker().darker());
	    else
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
	    else if(value instanceof Date) {
		String text = 
		    DateFormat.getDateInstance().format((Date)value);
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

	    if(changePercent > 0)
		iconLabel.setIcon(Loader.loadImage("images/up.gif"));
	    else if(changePercent < 0)
		iconLabel.setIcon(Loader.loadImage("images/down.gif"));
	    else
		iconLabel.setIcon(Loader.loadImage("images/unch.gif"));

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
    }

    public void setModel(TableModel model) {

	super.setModel(model);
	setDefaultSortComparator(Change.class,
				 new ChangeComparator());
    }

    public void setRedColumn(int column) {
	redColumn = column;
    }

    public int getRedColumn() {
	return redColumn;
    }

    public void setGreenColumn(int column) {
	greenColumn = column;
    }

    public int getGreenColumn() {
	return greenColumn;
    }
}


