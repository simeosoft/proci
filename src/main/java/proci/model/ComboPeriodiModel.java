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
import java.util.ArrayList;
import javax.swing.DefaultComboBoxModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Modello per selezione periodi
 * @author simeo
 */
public class ComboPeriodiModel extends DefaultComboBoxModel {
    private static final long serialVersionUID = -7730956163402067519L;
    private ArrayList<Periodo> al;
    static final Logger logger = LoggerFactory.getLogger(ComboPeriodiModel.class);    
    
    public ComboPeriodiModel(Connection conn) {
        try {
            Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
            String sql = "select * from PERIODO order by PERID desc";
            logger.debug("Executing sql: {}",sql);
            al = new ArrayList<>();        
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
    public Periodo getElementAt(int i) {
        if (i < al.size()) {
            Periodo p = al.get(i);
            return p;
        }
        return null;
    }        
    
    @Override
    public int getSize() {
        return al.size();
    }
    
    public Periodo getPeriodoAt(int i) {
        return al.get(i);
    }
}
