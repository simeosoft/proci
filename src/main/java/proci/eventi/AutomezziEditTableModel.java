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

package proci.eventi;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;
import proci.App;


public class AutomezziEditTableModel extends AbstractTableModel {
    private static final long serialVersionUID = 6571686172832943961L;

    private final String[] columnNames = { "descrizione","targa", "responsabile", "note"};
    private final Class[] classes = { String.class,String.class,String.class,String.class };
    private ArrayList<Automezzo> al = new ArrayList<>();
    private int maximum_id = 0;
    private ResultSet rs;
    private int gruppo = 0;
    
    public AutomezziEditTableModel(Connection conn,int gruppo) {
        this.gruppo = gruppo;
        try {
            Statement stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
            String sql = "select * from AUTOMEZZO where AUTGRUPPO = " + gruppo +
                    " order by AUTID";
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Automezzo aut = new Automezzo(rs);
                if (aut.getId() > maximum_id) {
                    maximum_id = aut.getId();
                }
                al.add(aut);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(App.getInstance().getMainFrame(),e,"ERRORE!",JOptionPane.ERROR_MESSAGE);
        } 
    }
   
    public void deleteAllItems() {
        al.clear();
        maximum_id = 0;
        fireTableDataChanged();
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

    public Automezzo getPartecipanteAt(int row) {
        return al.get(row);
    }
    
    @Override
    public Object getValueAt(int row, int column) {
        Automezzo aut = al.get(row);
        switch (column)
        {
            case 0:
                return aut.getDescrizione();
            case 1:
                return aut.getTarga();
            case 2:
                return aut.getResponsabile();
            case 3:
                return aut.getNote();
        }
        return "???";
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
        if (r >= al.size()) {
            return;
        }
        Automezzo aut = al.get(r);
        switch (c) {
            case 0:
                aut.setDescrizione((String) val);
                break;
            case 1:
                aut.setTarga((String) val);
                break;
            case 2:
                aut.setResponsabile((String) val);
                break;
            case 3:
                aut.setNote((String) val);
                break;
        }
        fireTableDataChanged();
    }
    
    public void addItem() {
        maximum_id++;
        Automezzo aut = new Automezzo(maximum_id,gruppo);
        al.add(aut);
        fireTableDataChanged();
    }
    // cell editing
    @Override
    public boolean isCellEditable(int r, int c) {
        return true;
    }
    public ArrayList<Automezzo> getValues() {
        return al;
    }
}
