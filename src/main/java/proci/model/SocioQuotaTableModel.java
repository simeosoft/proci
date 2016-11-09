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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.table.AbstractTableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SocioQuotaTableModel extends AbstractTableModel {

    static final Logger logger = LoggerFactory.getLogger(SocioQuotaTableModel.class);    
    private static final long serialVersionUID = -2432280429643470449L;

    private static final String[] columnNames = { "socio","flag","note","quota pagata","data gadget" };
    private static final Class[] classes = { String.class,String.class,String.class,BigDecimal.class,Date.class};
    private ArrayList<SocioQuota> al;
    private final Connection conn;
    private int current_period = 0;
    
    public SocioQuotaTableModel(Connection conn,int periodo) {
        this.conn = conn;
        if (periodo != 0) {
            load(periodo);
        } else {
            al = new ArrayList<>();            
        }
    }

    public void load(int periodo) {
        String sql = "select SOID,SONOME,SOCOGNOME,QUOTA.* from SOCIO left join QUOTA on (QUOSOID=SOID)"
                + " where (QUOPERID=? or QUOPERID is null)";
        logger.debug("Executing sql: {}, periodo: ",sql,periodo);
        al = new ArrayList<>();
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, periodo);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                SocioQuota q = new SocioQuota();
                q.setSOID(rs.getInt("SOID"));
                q.setSOCOGNOME(rs.getString("SOCOGNOME"));                
                q.setSONOME(rs.getString("SONOME"));                
                q.setQUOID(rs.getInt("QUOID"));
                q.setQUOPERID(periodo);
                q.setQUOSOID(rs.getInt("SOID"));
                q.setQUOFLAG(rs.getString("QUOFLAG"));
                q.setQUONOTE(rs.getString("QUONOTE"));
                q.setQUOPAG(rs.getBigDecimal("QUOPAG"));
                q.setQUODATAGADGET(rs.getDate("QUODATAGADGET") != null ? new Date(rs.getDate("QUODATAGADGET").getTime()) : null);
                al.add(q);
                current_period = periodo;
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
        SocioQuota q = al.get(row);
        switch (column)
        {
            case 0:
                return q.getSOCOGNOME() + " " + q.getSONOME();
            case 1:
                return q.getQUOFLAG();
            case 2:
                return q.getQUONOTE();
            case 3:
                return q.getQUOPAG();
            case 4:
                return q.getQUODATAGADGET();
        }
        return "N/A";
    }

    @Override
    public int getRowCount() {
        return al.size();
    }
    
    public SocioQuota getItemAt(int row) {
        if (row < al.size()) {
            return al.get(row);
        }
        return null;
    }
    // cell editing
    @Override
    public boolean isCellEditable(int r, int c) {
        return c > 0;
    }
    public ArrayList<SocioQuota> getValues() {
        return al;
    }
 
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        logger.debug("setValueAt: Obj: {}, r: {}, c: {}",aValue,rowIndex,columnIndex);
        SocioQuota sq = al.get(rowIndex);
        switch (columnIndex) {
            case 1:
                sq.setQUOFLAG((String) aValue);
                break;
            case 2:
                sq.setQUONOTE((String) aValue);
                break;
            case 3:
                sq.setQUOPAG((BigDecimal) aValue);
                break;
            case 5:
                sq.setQUODATAGADGET((Date) aValue);
                break;
        }
        fireTableCellUpdated(rowIndex, columnIndex);
        persist(sq);
    }    
    
    private String  persist(SocioQuota sq) {
        logger.debug("Persist: {}",sq);
        String sql = "";
        String msg = null;
        PreparedStatement ps;
        try {
            if (sq.getQUOID() != 0) {
                sql = "update QUOTA set QUOFLAG=?,QUONOTE=?,QUOPAG=?,QUODATAGADGET=? where QUOID=?";
                logger.debug("Persisting update: sql: {}",sql);
                ps = conn.prepareStatement(sql);
                ps.setString(1, sq.getQUOFLAG());
                ps.setString(2, sq.getQUONOTE());
                ps.setBigDecimal(3, sq.getQUOPAG() != null ? sq.getQUOPAG() : BigDecimal.ZERO);
                if (sq.getQUODATAGADGET() != null) {                
                    ps.setDate(4, new java.sql.Date(sq.getQUODATAGADGET().getTime()));
                } else {
                    ps.setNull(4, java.sql.Types.DATE);
                }
                ps.setInt(5, sq.getQUOID());
                ps.execute();
            } else {
                sql = "insert into QUOTA (QUOSOID,QUOPERID,QUOFLAG,QUONOTE,QUOPAG,QUODATAGADGET) values (?,?,?,?,?,?)";
                logger.debug("Persisting: insert sql: {}",sql);
                ps = conn.prepareStatement(sql);
                ps.setInt(1, sq.getQUOSOID());
                ps.setInt(2, sq.getQUOPERID());
                ps.setString(3, sq.getQUOFLAG());
                ps.setString(4, sq.getQUONOTE());
                ps.setBigDecimal(5, sq.getQUOPAG() != null ? sq.getQUOPAG() : BigDecimal.ZERO);
                if (sq.getQUODATAGADGET() != null) {
                    ps.setDate(6, new java.sql.Date(sq.getQUODATAGADGET().getTime()));
                } else {
                    ps.setNull(6, java.sql.Types.DATE);
                }
                ps.execute();
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    logger.debug("Persisting: generated key: {}",rs.getInt(0));
                    sq.setQUOID(rs.getInt(0));
                }
            }
        } catch (SQLException e) {
            msg = "ERRORE SQL QUOTA: (" + sql + ") " + e.getLocalizedMessage();
            logger.error(msg);
        }
        return msg;
    }
    
    
}
