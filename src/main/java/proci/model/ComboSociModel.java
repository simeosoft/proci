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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.DefaultComboBoxModel;
import proci.ETipoSocio;

/**
 * Modello per selezione soci
 * @author simeo
 */
public class ComboSociModel extends DefaultComboBoxModel {
    private static final long serialVersionUID = -7730956163402067518L;
    
    private ResultSet rs = null;
    private int cachedSize = 0;
    /** Creates a new instance of ComboSociModel
     * @param conn
     * @param tipo */
    public ComboSociModel(Connection conn, ETipoSocio tipo) {
        try {
            Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
            String sql = "select SOID,SOCOGNOME,SONOME from SOCIO where 1=1 ";
            if (tipo != null) {
                sql = sql + " and SOTIPO = '" + tipo.getVal() + "'";
            }
            sql = sql + " order by SOCOGNOME,SONOME";
            rs = stmt.executeQuery(sql);
            rs.last();
            cachedSize = rs.getRow();
        } catch (SQLException e) {
        }
    }
    
    @Override
    public String getElementAt(int i) {
        try {
            rs.absolute(i + 1);
            return rs.getString("SOCOGNOME") + "," + rs.getString("SONOME") + " (" + rs.getString("SOID") + ")";
        } catch (SQLException e) {
            return "ERRORE: " + e;
        }
    }
    
    public int getIdSocioAt(int i) {
        try {
            rs.absolute(i + 1);
            return rs.getInt("SOID");
        } catch (SQLException e) {
            return 1;
        }
    }
    
    @Override
    public int getSize() {
        return cachedSize;
    }
}
