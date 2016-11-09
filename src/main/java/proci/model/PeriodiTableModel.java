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

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PeriodiTableModel extends AbstractTableModel {

    static final Logger logger = LoggerFactory.getLogger(PeriodiTableModel.class);    
    private static final long serialVersionUID = -2432280429643470448L;

    final String[] columnNames = { "id","anno competenza","descrizione","note","quota","quota ridotta" };
    final Class[] classes = { Integer.class,String.class,String.class,String.class,BigDecimal.class,BigDecimal.class};
    ArrayList<Periodo> al;
    final Connection conn;
    
    public PeriodiTableModel(Connection conn) {
        this.conn = conn;
        load();
    }

    public void load() {
        String sql = "select * from PERIODO order by PERID desc";
        logger.debug("Executing sql: {}",sql);
        al = new ArrayList<>();        
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Periodo p = new Periodo(
                rs.getInt("PERID"),
                rs.getString("PERANNOCOMP"),
                rs.getString("PERDESCR"),
                rs.getString("PERNOTE"),
                rs.getBigDecimal("PERQUOTA"),
                rs.getBigDecimal("PERQUOTARID"));
                al.add(p);
            }        
        } catch (SQLException e) {
            logger.error("ERROR: {}",e);
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
        Periodo p = al.get(row);
        switch (column)
        {
            case 0:
                return p.getPERID();
            case 1:
                return p.getPERANNOCOMP();
            case 2:
                return p.getPERDESCR();
            case 3:
                return p.getPERNOTE();
            case 4:
                return p.getPERQUOTA();
            case 5:
                return p.getPERQUOTARID();
        }
        return "N/A";
    }

    @Override
    public int getRowCount() {
        return al.size();
    }
    
    public Periodo getItemAt(int row) {
        if (row < al.size()) {
            return al.get(row);
        }
        return null;
    }
    // cell editing
    @Override
    public boolean isCellEditable(int r, int c) {
        //return editable && c > 0;
        return false;
    }
    public ArrayList<Periodo> getValues() {
        return al;
    }
}
