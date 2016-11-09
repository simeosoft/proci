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

package proci.reports;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.JOptionPane;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import proci.App;

/**
 *
 * @author simeo
 */
public class DiesDataSource implements JRDataSource {
    
    private ResultSet rs = null;    
    private String tipo;
    
    public DiesDataSource(Connection conn,String tipo) {
        this.tipo = tipo;
        try {
            Statement stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
            String sql = "";
            if (tipo.equals("D")) {
                sql = "select * from DOTAZIONE order by DOTDESCR";
            } else if (tipo.equals("I")) {
                sql = "select * from INTERVENTO order by INTDESCR";
            } else if (tipo.equals("E")) {
                sql = "select * from ESERCITAZIONE order by ESEDESCR";
            } else if (tipo.equals("S")) {
                sql = "select * from SPECIALIZZAZIONE order by SPEDESCR";
            }
            rs = stmt.executeQuery(sql);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(App.getInstance().getMainFrame(),e,"ERRORE!",JOptionPane.ERROR_MESSAGE);
        } 
    }
    
    public Object getFieldValue(JRField field) throws JRException {
        Object value = null;
        String columnName = field.getName();
        try {
            if (columnName.equals("TIPO")) {
                value = tipo;
            } else if (columnName.equals("DESTIPO")) {
                if (tipo.equals("D")) {
                    value = "dotazioni";
                } else if (tipo.equals("I")) {
                    value = "interventi";
                } else if (tipo.equals("E")) {
                    value = "esercitazioni";
                } else if (tipo.equals("S")) {
                    value = "specializzazioni";
                }
            } else if (columnName.equals("DESCR")) {
                if (tipo.equals("D")) {
                    value = rs.getString("DOTDESCR");
                } else if (tipo.equals("I")) {
                    value = rs.getString("INTDESCR");
                } else if (tipo.equals("E")) {
                    value = rs.getString("ESEDESCR");
                } else if (tipo.equals("S")) {
                    value = rs.getString("SPEDESCR");
                }
            } else if (columnName.equals("TAGLIA")) {
                if (tipo.equals("D")) {
                    value = rs.getString("DOTTAGLIA");
                } else {
                    value = null;
                }
            } else if (columnName.equals("ID")) {
                if (tipo.equals("D")) {
                    value = rs.getString("DOTID");
                } else if (tipo.equals("I")) {
                    value = rs.getString("INTID");
                } else if (tipo.equals("E")) {
                    value = rs.getString("ESEID");
                } else if (tipo.equals("S")) {
                    value = rs.getString("SPEID");
                }
            } else {
                value = "????? " + field.getName();
            }
        } catch (SQLException e) {
            value = "????? " + e;
        }         
        return value;
    }
    
    public boolean next() throws JRException {
        try {
            return rs.next();
        } catch (SQLException e) {
            return false;
        } 
    }
}
