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
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;
import proci.App;


public class AutomezziMainTableModel extends AbstractTableModel {

    final String[] columnNames = { "gruppo", "descrizione", "targa", "responsabile", "note",};
    final Class[] classes = { String.class,String.class,String.class,String.class,String.class };
    ResultSet rs;
    int rowCount = 0;
    
    public AutomezziMainTableModel(Connection conn,String orderby, boolean desc) {
        String sql = "";
        try {
            Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
            sql = "select * from AUTOMEZZO " +
                    " join GRUPPO on (GRUID=AUTGRUPPO) " + 
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
                    return rs.getString("AUTDESCR");
                case 2:
                    return rs.getString("AUTTARGA");
                case 3:
                    return rs.getString("AUTRESP");
                case 4:
                    return rs.getString("AUTNOTE");
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
}