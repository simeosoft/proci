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
import proci.DBHandler;
import proci.App;
import proci.model.*;


public class PartecipantiMainTableModel extends AbstractTableModel {

    final String[] columnNames = { "gruppo", "cognome", "nome", "cellulare", "specializzazione", 
                                "note", "tessera?" , "attestato?"};
    final Class[] classes = { String.class,String.class,String.class,String.class,String.class,
                            String.class,Boolean.class,Boolean.class};
    ResultSet rs;
    int rowCount = 0;
    
    public PartecipantiMainTableModel(Connection conn,String orderby, boolean desc) {
        String sql = "";
        try {
            Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
            sql = "select * from PARTECIPANTE " +
                    " join GRUPPO on (GRUID=PARGRUPPO) " + 
                    " order by " + orderby;
            String ascdesc = desc == true ? " desc " : "";
            sql = sql + ascdesc;
            rs = stmt.executeQuery(sql);
            if (rs != null) {
                rs.last();
                rowCount = rs.getRow();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(App.getInstance().getMainFrame(),e + "(" + sql + ")","ERRORE!",JOptionPane.ERROR_MESSAGE);
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
    public Object getValueAt(int r, int c) {
       try {
            rs.absolute(r + 1);
            switch (c) {
                case 0:
                    return rs.getString("GRUDESCR");
                case 1:
                    return rs.getString("PARCOGNOME");
                case 2:
                    return rs.getString("PARNOME");
                case 3:
                    return rs.getString("PARCELL");
                case 4:
                    return rs.getString("PARSPEC");
                case 5:
                    return rs.getString("PARNOTE");
                case 6:
                    return rs.getString("PARTESSERASTAMPATA").equals("Y") ? true : false;
                case 7:
                    return rs.getString("PARATTESTATOSTAMPATO").equals("Y") ? true : false;
                // helpers per aggiornamenti 
                case 8:
                    return rs.getInt("PARGRUPPO");
                case 9:
                    return rs.getInt("PARID");
                default:
                    return "???";
            }
        } catch (SQLException e) {
            return "SQL Exc: getValueAt: row: " + (r + 1)  + 
                    "  col: " + (c + 1) + "  exc: " + e.getLocalizedMessage();
        } catch (Exception e) {
            return "Exc: col: " + c + "  exc: " + e;
        }
    }        

    @Override
    public int getRowCount() {
        return rowCount;
    }
    // cell editing
    @Override
    public boolean isCellEditable(int r, int c) {
        return false;
    }
    
    public Partecipante getPartecipanteAt(int row) {
        Partecipante par;
        try {
            rs.absolute(row + 1);
            par = new Partecipante(rs);
            par.setGruppoDescr(rs.getString("GRUDESCR"));
            return par;
        } catch (SQLException e) {
            par = new Partecipante(0,0);
            par.setCognome(e.getLocalizedMessage());
        }
        return par;
    }
    
    public ArrayList<Partecipante> getValues() {
        ArrayList<Partecipante> alPar = new ArrayList<>();
        Partecipante par;
        for (int i=0; i < rowCount; i++) {
            try {
                rs.absolute(i + 1);
                par = new Partecipante(rs);
                par.setGruppoDescr(rs.getString("GRUDESCR"));
                alPar.add(par);
            } catch (SQLException e) {
                par = new Partecipante(0,0);
                par.setCognome(e.getLocalizedMessage());
                alPar.add(par);
            }
        }
        return alPar;
    }
}
