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

public class EquationTable extends AbstractTable {
    public static final int NAME_COLUMN = 0;
    public static final int EQUATION_COLUMN = 1;

    private Model model;
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

    public EquationTable() {
	List columns = new ArrayList();
	columns.add(new Column(NAME_COLUMN, "Name", "Name", String.class, Column.VISIBLE));
	columns.add(new Column(EQUATION_COLUMN, "Equation", "Equation", String.class, 
			       Column.VISIBLE));

	storedEquations = PreferencesManager.loadStoredEquations();

	model = new Model(columns, storedEquations);
	setModel(model);
	showColumns(model);
    }

    public void add() {
        Thread thread = new Thread(new Runnable() {
                public void run() {
		    StoredEquation storedEquation = 
			ExpressionEditorDialog.showAddDialog(storedEquations, "Add Equation");

		    if(storedEquation != null) {
			storedEquations.add(storedEquation);
			model.fireTableDataChanged();
			repaint();
		    }
		}
	    });
    
	thread.start();
    }

    public void edit(int row) {
	if(row >= 0 && row < storedEquations.size()) {
	    final StoredEquation storedEquation = (StoredEquation)storedEquations.get(row);

	    Thread thread = new Thread(new Runnable() {
		    public void run() {
			ExpressionEditorDialog.showEditDialog(storedEquations,"Edit Equation", 
							      storedEquation);
			model.fireTableDataChanged();
			repaint();
		    }
		});
    
	    thread.start();
	}
    }

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
    
    public void save() {
	PreferencesManager.saveStoredEquations(storedEquations);
	EquationComboBox.updateEquations();
    }
}