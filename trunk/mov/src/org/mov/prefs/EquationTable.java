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

package org.mov.prefs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.mov.ui.AbstractTable;
import org.mov.ui.AbstractTableModel;
import org.mov.ui.Column;
import org.mov.ui.EquationComboBox;
import org.mov.ui.ExpressionEditorDialog;
import org.mov.util.Locale;

/**
 * The table in the Equation Preferences page that lists stored equations.
 */
public class EquationTable extends AbstractTable {
    private static final int NAME_COLUMN = 0;
    private static final int EQUATION_COLUMN = 1;

    private Model model;

    // Current list of stored equations in the table
    private List storedEquations;

    private class Model extends AbstractTableModel {

	private List storedEquations;

	public Model(List columns, List storedEquations) {
	    super(columns);
	    this.storedEquations = storedEquations;
	}
	
	public int getRowCount() {
	    return storedEquations.size();
	}

	public Object getValueAt(int row, int column) {
	    assert row < storedEquations.size();
	    StoredEquation storedEquation = 
		(StoredEquation)storedEquations.get(row);

	    if(column == NAME_COLUMN) 
		return storedEquation.name;
	    else
		return storedEquation.equation;
	}
        
    }

    /**
     * Create a new equation table. The equation table will be initially populated from
     * the current stored equations.
     */
    public EquationTable() {
	List columns = new ArrayList();
	columns.add(new Column(NAME_COLUMN, 
			       Locale.getString("NAME"), 
			       Locale.getString("NAME_COLUMN_HEADER"), 
			       String.class, Column.VISIBLE));
	columns.add(new Column(EQUATION_COLUMN, 
			       Locale.getString("EQUATION"), 
			       Locale.getString("FULL_EQUATION_COLUMN_HEADER"), 
			       String.class, Column.VISIBLE));

	storedEquations = PreferencesManager.loadStoredEquations();

	model = new Model(columns, storedEquations);
	setModel(model);
	showColumns(model);
    }

    /** 
     * Display a dialog asking the user to enter a new stored equation.
     */
    public void add() {
        Thread thread = new Thread(new Runnable() {
                public void run() {
		    StoredEquation storedEquation = 
			ExpressionEditorDialog.showAddDialog(storedEquations, 
							     Locale.getString("ADD_EQUATION"));

		    if(storedEquation != null) {
                        storedEquations.add(storedEquation);
                        setModel(model);
                        model.fireTableDataChanged();
			repaint();
		    }
		}
	    });
    
	thread.start();
    }

    /** 
     * Display a dialog allowing the user to edit the stored equation.
     *
     * @param row the row of the stored equation to edit.
     */
    public void edit(int row) {
	if(row >= 0 && row < storedEquations.size()) {
	    final StoredEquation storedEquation = (StoredEquation)storedEquations.get(row);

	    Thread thread = new Thread(new Runnable() {
		    public void run() {
			ExpressionEditorDialog.showEditDialog(storedEquations,
							      Locale.getString("EDIT_EQUATION"), 
							      storedEquation);
                        model.fireTableDataChanged();
			repaint();
		    }
		});
    
	    thread.start();
	}
    }

    /**
     * Delete the stored equations in the given rows.
     */
    public void delete(int[] rows) {
	// Remove the last row first, then the second to last, etc...
	Arrays.sort(rows);

	for(int i = rows.length - 1; i >= 0; i--) {
	    int row = rows[i];
	    if(row >= 0 && row < storedEquations.size())
		storedEquations.remove(row);
	}

	// For some reason we need to do an explicit repaint call
	// here to get the table to redraw itself.
        model.fireTableDataChanged();
	repaint();
    }
    
    /**
     * Replace the stored equations in preferences with the stored equations in
     * this table. Make sure everything is in-sync with the new equations.
     */
    public void save() {
	PreferencesManager.saveStoredEquations(storedEquations);
	EquationComboBox.updateEquations();
    }
}