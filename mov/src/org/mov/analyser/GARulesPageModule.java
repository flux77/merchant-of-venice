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

/**
 *
 * @author  Alberto Nacher
 */
package org.mov.analyser;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.mov.main.Module;
import org.mov.ui.AbstractTable;
import org.mov.ui.AbstractTableModel;
import org.mov.ui.Column;
import org.mov.ui.ConfirmDialog;
import org.mov.ui.ExpressionEditorDialog;
import org.mov.ui.MenuHelper;
import org.mov.util.Locale;

public class GARulesPageModule extends AbstractTable implements Module {
    private PropertyChangeSupport propertySupport;
    
    public static final int PARAMETER_COLUMN = 0;
    public static final int MIN_PARAMETER_COLUMN = 1;
    public static final int MAX_PARAMETER_COLUMN = 2;
    public static final int NUMBER_COLUMN = 3;

    private final static String separatorString = GPModuleConstants.separatorString;
    private final static String nullString = GPModuleConstants.nullString;
    // empty string
    private final static String emptyString = nullString + separatorString + nullString  + separatorString + nullString;
    
    private final static String format = GPModuleConstants.format;

    private JDesktopPane desktop;
    private Model model;
    private double[] min = new double[0];
    private double[] max = new double[0];
    
    // Menus
    private JMenuBar menuBar;
    private JMenuItem editParameterMenuItem;
    private JMenuItem editMinParameterMenuItem;
    private JMenuItem editMaxParameterMenuItem;
    private JMenuItem removeMenuItem;
    private JMenuItem removeAllMenuItem;
    
    private class Model extends AbstractTableModel {
        private List results;
        
        public Model(List columns) {
            super(columns);
            results = new ArrayList();
        }
        
        public String[] getResult(int row) {
            return (String[])results.get(row);
        }
        
        public void removeAllResults() {
            results.clear();
            
            // Notify table that the whole data has changed
            fireTableDataChanged();
        }
        
        public List getResults() {
            return results;
        }
        
        public void setResults(List results) {
            this.results = results;
            
            // Notify table that the whole data has changed
            fireTableDataChanged();
        }
        
        public void addResults(List results) {
            this.results.addAll(results);
            
            // Notify table that the whole data has changed
            fireTableDataChanged();
        }
        
        public void addEmptyIfNot() {
            // If there is neither a row empty,
            // add an empty row so the user can add a new buy/sell rules
            boolean isAlreadyEmpty = false;
            for (int i=0; i<getRowCount(); i++) {
                String[] result = (String[])results.get(i);
                if (result[PARAMETER_COLUMN].equals("") &&
                    result[MIN_PARAMETER_COLUMN].equals("")) {
                    isAlreadyEmpty = true;
                }
                result = null;
            }
            if (!isAlreadyEmpty) {
                this.addEmpty();
            }
        }
        
        private void addEmpty() {
            String values[] = new String[NUMBER_COLUMN];
            for (int i=0; i<values.length; i++) {
                values[i] = new String("");
            }
            this.results.add(values);
        }
        
        public int getRowCount() {
            return results.size();
        }
        
        public Object getValueAt(int row, int column) {
            if(row >= getRowCount())
                return "";
            
            String[] result =
                (String[])results.get(row);
            
            if(column == PARAMETER_COLUMN)
                return result[PARAMETER_COLUMN];
            
            else if(column == MIN_PARAMETER_COLUMN)
                return result[MIN_PARAMETER_COLUMN];
            
            else if(column == MAX_PARAMETER_COLUMN)
                return result[MAX_PARAMETER_COLUMN];
            
            else {
                assert false;
                return "";
            }
        }
        
        public boolean setValueAt(int row, int column, String value) {
            if(row >= getRowCount())
                return false;
            
            String[] result = (String[])results.get(row);
            String[] object = new String[NUMBER_COLUMN];
            object[PARAMETER_COLUMN] = new String(result[PARAMETER_COLUMN]);
            object[MIN_PARAMETER_COLUMN] = new String(result[MIN_PARAMETER_COLUMN]);
            object[MAX_PARAMETER_COLUMN] = new String(result[MAX_PARAMETER_COLUMN]);
            object[column] = new String(value);
            
            result = (String[])results.set(row, object);
            result = null;
            
            return true;
        }
    }
    
    public GARulesPageModule(JDesktopPane desktop) {
        this.desktop = desktop;
        List columns = new ArrayList();
        columns.add(new Column(PARAMETER_COLUMN,
            Locale.getString("PARAMETER"),
            Locale.getString("PARAMETER_COLUMN_HEADER"),
            String.class,
            Column.VISIBLE));
        columns.add(new Column(MIN_PARAMETER_COLUMN,
            Locale.getString("MIN_PARAMETER"),
            Locale.getString("MIN_PARAMETER_COLUMN_HEADER"),
            String.class,
            Column.VISIBLE));
        columns.add(new Column(MAX_PARAMETER_COLUMN,
            Locale.getString("MAX_PARAMETER"),
            Locale.getString("MAX_PARAMETER_COLUMN_HEADER"),
            String.class,
            Column.VISIBLE));
        
        model = new Model(columns);
        setModel(model);
        
        model.addTableModelListener(this);
        
        propertySupport = new PropertyChangeSupport(this);
        
        addMenu();
        
        // If the user clicks on the table trap it.
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                handleMouseClicked(evt);
            }
        });
        
        showColumns(model);
    }
    
    // If the user double clicks on a result with the LMB, graph the portfolio.
    // If the user right clicks over the table, open up a popup menu.
    private void handleMouseClicked(MouseEvent event) {
        Point point = event.getPoint();
        
        // Right click on the table - raise menu
        if(event.getButton() == MouseEvent.BUTTON3) {
            JPopupMenu menu = new JPopupMenu();
            
            JMenuItem popupeditParameterMenuItem =
            new JMenuItem(Locale.getString("EDIT_PARAMETER"));
            popupeditParameterMenuItem.setEnabled(getSelectedRowCount() == 1);
            popupeditParameterMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    editParameter();
                }});
            menu.add(popupeditParameterMenuItem);

            JMenuItem popupeditMinParameterMenuItem =
            new JMenuItem(Locale.getString("EDIT_MIN_PARAMETER"));
            popupeditMinParameterMenuItem.setEnabled(getSelectedRowCount() == 1);
            popupeditMinParameterMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    editMinParameter();
                }});
            menu.add(popupeditMinParameterMenuItem);
            
            JMenuItem popupeditMaxParameterMenuItem =
            new JMenuItem(Locale.getString("EDIT_MAX_PARAMETER"));
            popupeditMaxParameterMenuItem.setEnabled(getSelectedRowCount() == 1);
            popupeditMaxParameterMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    editMaxParameter();
                }});
            menu.add(popupeditMaxParameterMenuItem);

            menu.addSeparator();

            JMenuItem popupRemoveMenuItem =
            new JMenuItem(Locale.getString("REMOVE"));
            popupRemoveMenuItem.setEnabled(getSelectedRowCount() >= 1);
            popupRemoveMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    removeSelectedResults();
                    checkMenuDisabledStatus();
                }});
            menu.add(popupRemoveMenuItem);

            menu.show(this, point.x, point.y);
        }
        
    }
    
    // Display a dialog allowing the user to edit the selected parameter.
    private void editParameter() {
        // Get result at row
        final int row = getSelectedRow();
        
        // Don't do anything if we couldn't retrieve the selected row
        if(row != -1) {
            final String[] result = model.getResult(row);
            
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    String newBuyRule = ExpressionEditorDialog.showEditDialog(Locale.getString("EDIT_EQUATION"),
                    result[PARAMETER_COLUMN]);
                    model.setValueAt(row, PARAMETER_COLUMN, newBuyRule);
                    model.addEmptyIfNot();
                    model.fireTableDataChanged();
                    repaint();
                }});
                
                thread.start();
        }
    }
    
    // Display a dialog allowing the user to edit the selected minimum parameter.
    private void editMinParameter() {
        // Get result at row
        final int row = getSelectedRow();
        
        // Don't do anything if we couldn't retrieve the selected row
        if(row != -1) {
            final String[] result = model.getResult(row);
            
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    String newSellRule = ExpressionEditorDialog.showEditDialog(Locale.getString("EDIT_EQUATION"),
                    result[MIN_PARAMETER_COLUMN]);
                    model.setValueAt(row, MIN_PARAMETER_COLUMN, newSellRule);
                    model.addEmptyIfNot();
                    model.fireTableDataChanged();
                    repaint();
                }});
                
                thread.start();
        }
    }
    
    
    // Display a dialog allowing the user to edit the selected maximum parameter.
    private void editMaxParameter() {
        // Get result at row
        final int row = getSelectedRow();
        
        // Don't do anything if we couldn't retrieve the selected row
        if(row != -1) {
            final String[] result = model.getResult(row);
            
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    String newPerc = ExpressionEditorDialog.showEditDialog(Locale.getString("EDIT_PERC"),
                    result[MAX_PARAMETER_COLUMN]);
                    model.setValueAt(row, MAX_PARAMETER_COLUMN, newPerc);
                    model.fireTableDataChanged();
                    repaint();
                }});
                
                thread.start();
        }
    }
    
    // Removes all the selected results from the table
    private void removeSelectedResults() {
        
        // Get selected rows and put them in order from highest to lowest
        int[] rows = getSelectedRows();
        List rowIntegers = new ArrayList();
        for(int i = 0; i < rows.length; i++)
            rowIntegers.add(new Integer(rows[i]));
        
        List sortedRows = new ArrayList(rowIntegers);
        Collections.sort(sortedRows);
        Collections.reverse(sortedRows);
        
        // Now remove them from the results list starting from the highest row
        // to the lowest
        List results = model.getResults();
        Iterator iterator = sortedRows.iterator();
        
        while(iterator.hasNext()) {
            Integer rowToRemove = (Integer)iterator.next();
            
            results.remove(rowToRemove.intValue());
        }
        
        model.setResults(results);
        model.addEmptyIfNot();
    }
    
    // Some menu items are only enabled/disabled depending on what is
    // selected in the table or by the size of the table
    private void checkMenuDisabledStatus() {
        int numberOfSelectedRows = getSelectedRowCount();
        
        editParameterMenuItem.setEnabled(numberOfSelectedRows == 1);
        editMinParameterMenuItem.setEnabled(numberOfSelectedRows == 1);
        editMaxParameterMenuItem.setEnabled(numberOfSelectedRows == 1);
        removeMenuItem.setEnabled(numberOfSelectedRows >= 1);
        removeAllMenuItem.setEnabled(model.getRowCount() > 0);
    }
    
    // Add a menu
    private void addMenu() {
        menuBar = new JMenuBar();
        
        JMenu resultMenu = MenuHelper.addMenu(menuBar, Locale.getString("RESULT"));
        
        editParameterMenuItem = new JMenuItem(Locale.getString("EDIT_PARAMETER"));
        editParameterMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                editParameter();
            }});
        resultMenu.add(editParameterMenuItem);

        editMinParameterMenuItem = new JMenuItem(Locale.getString("EDIT_MIN_PARAMETER"));
        editMinParameterMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                editMinParameter();
            }});
        resultMenu.add(editMinParameterMenuItem);

        editMaxParameterMenuItem = new JMenuItem(Locale.getString("EDIT_MAX_PARAMETER"));
        editMaxParameterMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                editMaxParameter();
            }});
        resultMenu.add(editMaxParameterMenuItem);

        resultMenu.addSeparator();

        removeMenuItem = new JMenuItem(Locale.getString("REMOVE"));
        removeMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                removeSelectedResults();
                checkMenuDisabledStatus();
            }});
        resultMenu.add(removeMenuItem);

        removeAllMenuItem = new JMenuItem(Locale.getString("REMOVE_ALL"));
        removeAllMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                model.removeAllResults();
                checkMenuDisabledStatus();
            }});
        resultMenu.add(removeAllMenuItem);

        resultMenu.addSeparator();

        JMenu columnMenu = createShowColumnMenu(model);
        resultMenu.add(columnMenu);

        resultMenu.addSeparator();

        // Listen for changes in selection so we can update the menus
        getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                checkMenuDisabledStatus();
            }

        });

        checkMenuDisabledStatus();
    }
    
    public void addResults(List results) {
        model.addResults(results);
        checkMenuDisabledStatus();
        validate();
        repaint();
    }
    
    public void addRowTable(String buyRule, String sellRule, String perc) {
        List results = new ArrayList();
        String[] object = new String[NUMBER_COLUMN];
        object[PARAMETER_COLUMN] = new String(buyRule);
        object[MIN_PARAMETER_COLUMN] = new String(sellRule);
        object[MAX_PARAMETER_COLUMN] = new String(perc);
        results.add(object);
        model.addResults(results);
        checkMenuDisabledStatus();
        validate();
        repaint();
    }

    private boolean isAllValuesAcceptable() {
        try {
            setNumericalValues();
        } catch(ParseException e) {
            JOptionPane.showInternalMessageDialog(desktop,
                                                  Locale.getString("ERROR_PARSING_NUMBER",
                                                                   e.getMessage()),
                                                  Locale.getString("INVALID_GA_ERROR"),
                                                  JOptionPane.ERROR_MESSAGE);
	    return false;
	}

        return true;
    }
    
    private void setNumericalValues() throws ParseException {
        min = new double[model.getRowCount()];
        max = new double[model.getRowCount()];
    
        // decimalFormat manage the localization.
        DecimalFormat decimalFormat = new DecimalFormat(format);
        for (int i=0; i<min.length; i++) {
            final String[] result = model.getResult(i);
            if((!result[MIN_PARAMETER_COLUMN].equals("")) && (!result[MIN_PARAMETER_COLUMN].equals(nullString))) {
                min[i] = decimalFormat.parse(result[MIN_PARAMETER_COLUMN]).doubleValue();
            } else {
                min[i] = 0;
            }
        }
        for (int i=0; i<max.length; i++) {
            final String[] result = model.getResult(i);
            if((!result[MAX_PARAMETER_COLUMN].equals("")) && (!result[MAX_PARAMETER_COLUMN].equals(nullString))) {
                max[i] = decimalFormat.parse(result[MAX_PARAMETER_COLUMN]).doubleValue();
            } else {
                max[i] = 0;
            }
        }
    }
        
    public String getTitle() {
        return Locale.getString("GA_PAGE_PARAMETERS_LONG");
    }
    
    public void save() {
        // Free up precious memory
        model.removeAllResults();
    }
    
    public void save(HashMap settings, String idStr) {
        List results = model.getResults();
        // Clean old settings
        for (int i=0; i<settings.size(); i++) {
            settings.put(idStr + (new Integer(i)).toString(), emptyString);
        }
        // Save the new settings
        int counterNew = 0;
        for (int i=0; i<results.size(); i++) {
            String[] values = new String[NUMBER_COLUMN];
            values = (String[])results.get(i);
            // Put a wild char (nullString) to manage the unlucky
            // situation when there is a null field.
            // In that case we can't manage the split operation
            // without using the wild char
            for (int j=0; j<values.length; j++) {
                if (values[j].equals("")) {
                    values[j] = new String(nullString);
                }
            }
            if (!((values[PARAMETER_COLUMN].compareTo(nullString)==0) &&
                (values[MIN_PARAMETER_COLUMN].compareTo(nullString)==0))) {
                String value = values[PARAMETER_COLUMN] +
                    separatorString + values[MIN_PARAMETER_COLUMN] +
                    separatorString + values[MAX_PARAMETER_COLUMN];
                settings.put(idStr + (new Integer(counterNew)).toString(), value);
                counterNew++;
            }
            values = null;
        }
    }
    
    public void load(String value) {
        String values[] = value.split(separatorString);
        if (values.length==NUMBER_COLUMN) {
            // If the string is a wild char (nullString)
            // change it to null string
            for (int j=0; j<values.length; j++) {
                if (values[j].equals(nullString))
                    values[j] = new String("");
            }
            if (!((values[PARAMETER_COLUMN].compareTo("")==0) &&
                (values[MIN_PARAMETER_COLUMN].compareTo("")==0))) {
                List newModel = new ArrayList();
                newModel.add(values);
                model.addResults(newModel);
            }
        }
    }
    
    public void loadEmpty() {
        model.addEmptyIfNot();
    }
        
    public boolean parse() {
        if(!isAllValuesAcceptable()) {
            return false;
        }
        return true;
    }
    
    public String getBuyRule(int row) {
        return (String)model.getValueAt(row, PARAMETER_COLUMN);
    }

    public String getSellRule(int row) {
        return (String)model.getValueAt(row, MIN_PARAMETER_COLUMN);
    }

    public void addModuleChangeListener(PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(listener);
    }
    
    public void removeModuleChangeListener(PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener(listener);
    }
    
    public ImageIcon getFrameIcon() {
        return null;
    }
    
    public JComponent getComponent() {
        return this;
    }
    
    public JMenuBar getJMenuBar() {
        return menuBar;
    }
    
    public boolean encloseInScrollPane() {
        return true;
    }
    
}
