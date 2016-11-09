/*
    Gestione Soci Protezione Civile
    Copyright (C) Simeosoft di Carlo Simeone
    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package proci.model;

import java.util.*;
import javax.swing.table.AbstractTableModel;


public class InterventiTableModel extends AbstractTableModel {
    private static final long serialVersionUID = 2587323537457470688L;

    final String[] columnNames = { "id","descrizione"};
    final Class[] classes = { Integer.class,String.class};
    ArrayList<Intervento> al = new ArrayList<>();
    boolean editable = false;
    int maximum_id = 0;
    
    public InterventiTableModel(ArrayList<Intervento> al, boolean editable) {
        this.al = al;
        this.editable = editable;
        // calcola il max id..
        for (Intervento inte : al) {
            if (inte.getINTID() > maximum_id) {
                maximum_id = inte.getINTID();
            }
        }
    }
   
    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public Class getColumnClass(int column) {
        return classes[column];
    }

    @Override
    public Object getValueAt(int row, int column) {
        Intervento inte = al.get(row);
        switch (column)
        {
            case 0:
                return inte.getINTID();
            case 1:
                return inte.getINTDESCR();
        }
        return null;
    }

    @Override
    public int getRowCount() {
        return al.size();
    }
    public void deleteItem(int row) {
        al.remove(row);
        fireTableDataChanged();
    }

    @Override
    public void setValueAt(Object val,int r, int c) {
        Intervento inte = al.get(r);
        if (c == 1) {
            inte.setINTDESCR((String) val);
        }
        fireTableDataChanged();
    }
    
    public void addItem() {
        maximum_id++;
        Intervento inte = new Intervento(maximum_id,"");
        al.add(inte);
        fireTableDataChanged();
    }
    // cell editing
    @Override
    public boolean isCellEditable(int r, int c) {
        return editable && c > 0;
    }
    public void setEditable(boolean editable) {
        this.editable = editable;
    }
    public ArrayList<Intervento> getValues() {
        return al;
    }
}
