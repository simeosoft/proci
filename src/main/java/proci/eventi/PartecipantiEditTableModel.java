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


public class PartecipantiEditTableModel extends AbstractTableModel {

    private final String[] columnNames = { "cognome","nome", "cellulare", "specializzazione", "note"};
    private final Class[] classes = { String.class,String.class,String.class,String.class,String.class };
    private ArrayList<Partecipante> al = new ArrayList<Partecipante>();
    private int maximum_id = 1;
    private ResultSet rs;
    private int gruppo = 0;
    
    public PartecipantiEditTableModel(Connection conn,int gruppo) {
        this.gruppo = gruppo;
        try {
            Statement stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
            String sql = "select * from PARTECIPANTE where PARGRUPPO = " + gruppo +
                    " and PARID > 1 order by PARID";
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Partecipante par = new Partecipante(rs);
                if (par.getId() > maximum_id) {
                    maximum_id = par.getId();
                }
                al.add(par);
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

    public Partecipante getPartecipanteAt(int row) {
        return al.get(row);
    }
    
    @Override
    public Object getValueAt(int row, int column) {
        Partecipante par = al.get(row);
        switch (column)
        {
            case 0:
                return par.getCognome();
            case 1:
                return par.getNome();
            case 2:
                return par.getCellulare();
            case 3:
                return par.getSpecializzazione();
            case 4:
                return par.getNote();
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
        Partecipante par = al.get(r);
        switch (c) {
            case 0:
                par.setCognome((String) val);
                break;
            case 1:
                par.setNome((String) val);
                break;
            case 2:
                par.setCellulare((String) val);
                break;
            case 3:
                par.setSpecializzazione((String) val);
                break;
            case 4:
                par.setNote((String) val);
                break;
        }
        fireTableDataChanged();
    }
    
    public void addItem() {
        maximum_id++;
        Partecipante par = new Partecipante(maximum_id,gruppo);
        al.add(par);
        fireTableDataChanged();
    }
    public void addItem(String cognome, String nome) {
        maximum_id++;
        Partecipante par = new Partecipante(maximum_id,gruppo);
        par.setCognome(cognome);
        par.setNome(nome);
        par.setGruppo(gruppo);
        al.add(par);
        fireTableDataChanged();
    }
    // cell editing
    @Override
    public boolean isCellEditable(int r, int c) {
        return true;
    }
    public ArrayList<Partecipante> getValues() {
        return al;
    }
}
