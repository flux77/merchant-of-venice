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

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellRenderer;

import org.mov.parser.EvaluationException;
import org.mov.quote.QuoteBundle;
import org.mov.quote.Symbol;
import org.mov.util.Locale;
import org.mov.util.Money;
import org.mov.util.TradingDate;

public class AbstractTable extends SortedTable {

    // Default values for rendering table rows
    private static final Color backgroundColor = Color.white;
    private static final Color alternativeBackgroundColor = new Color(237, 237, 237);   
    private static final Color selectedBackgroundColor = Color.blue;   
    private static final Color selectedForegroundColor = Color.white;

    // Images used for arrows representing when stock has gone up, down or is unchanged
    private String upImage = "org/mov/images/Up.png";
    private String downImage = "org/mov/images/Down.png";
    private String unchangedImage = "org/mov/images/Unchanged.png";

    // Keep a single instance of the following so we don't have to instantiate
    // for each cell that is drawn
    private NumberFormat format;
    private ImageIcon upImageIcon;
    private ImageIcon downImageIcon;
    private ImageIcon unchangedImageIcon;

    // List of show equation column menu items
    private List showEquationColumnMenuItems;

    class StockQuoteRenderer extends JPanel implements TableCellRenderer
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
	    if(isSelected) {
		setBackground(selectedBackgroundColor);
		textLabel.setForeground(selectedForegroundColor);
	    } else {
		setBackground(row % 2 != 0?
			      backgroundColor:
			      alternativeBackgroundColor);
            }

	    // The change column has specific rendering
	    if(value instanceof ChangeFormat)
		renderChangeComponent(table, value, isSelected,
				      hasFocus, row, column);
	    else if(value instanceof TradingDate) {
	    	TradingDate date = (TradingDate)value;

	    	String text = date.toString("d?/m?/yyyy");
	    	textLabel.setText(text);
	    	add(textLabel);
	    } else {
		textLabel.setText(value.toString());
		add(textLabel);
	    }
	    return this;
	}

	private void renderChangeComponent(JTable table, Object value,
					   boolean isSelected,
					   boolean hasFocus,
					   int row, int column) {

	    ChangeFormat change = (ChangeFormat)value;
	    double changePercent = change.getChange();
	    String text = new String();

	    if(changePercent > 0)
		text = "+";

            text = text.concat(format.format(changePercent));
            text = text.concat("%");
	    textLabel.setText(text);

	    if(changePercent > 0 && upImageIcon != null)
		iconLabel.setIcon(upImageIcon);

	    else if(changePercent < 0 && downImageIcon != null)
		iconLabel.setIcon(downImageIcon);

	    else if(changePercent == 0 && unchangedImageIcon != null)
		iconLabel.setIcon(unchangedImageIcon);

	    add(glue);
	    add(textLabel);
	    add(iconLabel);
	}
    }

    public AbstractTable() {

	setShowGrid(true);

	// Our own stock quote renderer
	setDefaultRenderer(AccountNameFormat.class, new StockQuoteRenderer());
	setDefaultRenderer(ChangeFormat.class, new StockQuoteRenderer());
	setDefaultRenderer(Double.class, new StockQuoteRenderer());
	setDefaultRenderer(Float.class, new StockQuoteRenderer());
	setDefaultRenderer(Integer.class, new StockQuoteRenderer());
	setDefaultRenderer(Money.class, new StockQuoteRenderer());
	setDefaultRenderer(QuoteFormat.class, new StockQuoteRenderer());
	setDefaultRenderer(String.class, new StockQuoteRenderer());
	setDefaultRenderer(TradingDate.class, new StockQuoteRenderer());
        setDefaultRenderer(EquationResult.class, new StockQuoteRenderer());
        setDefaultRenderer(PointChangeFormat.class, new StockQuoteRenderer());
        setDefaultRenderer(Symbol.class, new StockQuoteRenderer());

        // Set up number formatter for rendering ChangeFormat.java
        format = NumberFormat.getInstance();
        format.setMinimumIntegerDigits(1);
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);

        // Add create the image icons for the up, down & unchanged images
        URL upImageResource = ClassLoader.getSystemResource(upImage);
        upImageIcon = (upImageResource != null? new ImageIcon(upImageResource) : null);

        URL downImageResource = ClassLoader.getSystemResource(downImage);
        downImageIcon = (downImageResource != null? new ImageIcon(downImageResource) : null);

        URL unchangedImageResource = ClassLoader.getSystemResource(unchangedImage);
        unchangedImageIcon = (unchangedImageResource != null?
                              new ImageIcon(unchangedImageResource) : null);
    }

    protected void showColumns(AbstractTableModel model) {
        for(int i = 0; i < model.getColumnCount(); i++) {
            Column column = model.getColumn(i);

            showColumn(column.getNumber(), column.getVisible() == Column.VISIBLE);
        }
    }

    protected JMenu createShowColumnMenu(AbstractTableModel model) {
        boolean foundEquationColumn = false;

        JMenu showColumnsMenu = new JMenu(Locale.getString("SHOW_COLUMNS"));
        showEquationColumnMenuItems = new ArrayList();

        for(int i = 0; i < model.getColumnCount(); i++) {
            final Column column = model.getColumn(i);

            if(column.getVisible() != Column.ALWAYS_HIDDEN) {
                boolean isEquationColumn = (column instanceof EquationColumn);

                // Insert a bar between the ordinary columns and the equation
                // columns
                if(!foundEquationColumn && isEquationColumn) {
                    foundEquationColumn = true;
                    showColumnsMenu.addSeparator();
                }

                JCheckBoxMenuItem showMenuItem =
                    MenuHelper.addCheckBoxMenuItem(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem)e.getSource();
                                showColumn(column.getNumber(), menuItem.getState());
                            }
                        }, showColumnsMenu, column.getFullName());

                showMenuItem.setState(column.getVisible() == Column.VISIBLE);
                
                if(isEquationColumn)
                    showEquationColumnMenuItems.add(showMenuItem);
            }
        }
        return showColumnsMenu;
    }

    protected void applyEquations(final QuoteBundle quoteBundle,
                                  final QuoteModel model) {
	// Handle all action in a separate thread so we dont
	// hold up the dispatch thread. See O'Reilley Swing pg 1138-9.
	Thread thread = new Thread() {

		public void run() {
		    final EquationColumnDialog dialog =
                        new EquationColumnDialog(model.getEquationColumns().length);

		    // Did the user modify the equation columns?
		    if(dialog.showDialog(model.getEquationColumns())) {
                        final EquationColumn[] equationColumns = dialog.getEquationColumns();

                        // Load equation columns with data
                        model.setEquationColumns(quoteBundle, equationColumns);

                        SwingUtilities.invokeLater(new Runnable() {
				public void run() {
				    // Make sure all columns with an equation
				    // are visible and all without are not.
				    // Also update check box menus
				    for(int i = 0; i < equationColumns.length; i++) {
                                        boolean containsEquation =
                                            equationColumns[i].getEquation().length() > 0;
                                        JCheckBoxMenuItem menuItem =
                                            (JCheckBoxMenuItem)showEquationColumnMenuItems.get(i);
			
                                        showColumn(equationColumns[i].getNumber(),
                                                   containsEquation);
                                        menuItem.setState(containsEquation);
                                    }
                                }});
		    }
		}
	    };
	thread.start();
    }
}
