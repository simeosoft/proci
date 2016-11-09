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


public class DotazioniTableModel extends AbstractTableModel {

    final String[] columnNames = { "id","descrizione","taglia"};
    final Class[] classes = { Integer.class,String.class,String.class};
    ArrayList<Dotazione> al = new ArrayList<Dotazione>();
    boolean editable = false;
    int maximum_id = 0;
    
    public DotazioniTableModel(ArrayList<Dotazione> al, boolean editable) {
        this.al = al;
        this.editable = editable;
        // calcola il max id..
        for (Dotazione dot : al) {
            if (dot.getDOTID() > maximum_id) {
                maximum_id = dot.getDOTID();
            }
        }
    }
   
    public String getColumnName(int column) {
        return columnNames[column];
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public Class getColumnClass(int column) {
        return classes[column];
    }

    public Object getValueAt(int row, int column) {
        Dotazione dot = al.get(row);
        switch (column)
        {
            case 0:
                return dot.getDOTID();
            case 1:
                return dot.getDOTDESCR();
            case 2:
                return dot.getDOTTAGLIA();
        }
        return null;
    }

    public int getRowCount() {
        return al.size();
    }
    public void deleteItem(int row) {
        al.remove(row);
        fireTableDataChanged();
    }

    public void setValueAt(Object val,int r, int c) {
        if (r >= al.size()) {
            return;
        }
        Dotazione dot = al.get(r);
        if (c == 1) {
            dot.setDOTDESCR((String) val);
        } else {
            dot.setDOTTAGLIA((String) val);
        }
        fireTableDataChanged();
    }
    
    public void addItem() {
        maximum_id++;
        Dotazione dot = new Dotazione(maximum_id,"","");
        al.add(dot);
        fireTableDataChanged();
    }
    // cell editing
    public boolean isCellEditable(int r, int c) {
        if (editable && c > 0) {
            return true;
        }
        return false;
    }
    public void setEditable(boolean editable) {
        this.editable = editable;
    }
    public ArrayList<Dotazione> getValues() {
        return al;
    }
}
