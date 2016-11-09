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

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.logging.Level;
import javax.swing.JOptionPane;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import proci.ETipoSocio;
import proci.App;
import proci.EDirectories;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author simeo
 */
public class SociDataSource implements JRDataSource {

    static final Logger logger = LoggerFactory.getLogger(SociDataSource.class);    

    private ResultSet rs = null;
    private String immagine = null;
    private ETipoSocio tipo_socio = null;
    private final App app = App.getInstance();  
    
    public SociDataSource(Connection conn, int from, int to,  ETipoSocio tipo, String immagine, String[] order_by) {
        this.immagine = immagine;
        this.tipo_socio = tipo;
        try {
            Statement stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
            String sql = "select * from SOCIO where 1=1 ";
            if (from != 0) {
                sql = sql + " and SOID >= " + from;
            }
            if (to != 0) {
                sql = sql + " and SOID <= " + to;
            }
            if (tipo != null) {
                sql = sql + " and SOTIPO = '" + tipo.getVal() + "'";
            }
            if (order_by != null && order_by.length > 0) {
                sql = sql + " order by ";
                for (String s : order_by) {
                    sql = sql + s + ",";
                }
                sql = sql.substring(0, sql.length() -1);
            }
            rs = stmt.executeQuery(sql);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(app.getMainFrame(),e,"ERRORE!",JOptionPane.ERROR_MESSAGE);
        } 
    }
    
    @Override
    public Object getFieldValue(JRField field) throws JRException {
        Object value = null;
        String columnName = field.getName();
        try {
            // campi non standard
            if (columnName.equals("INIZIALE")) {         // usato nella rubrica
                if (rs.getString("SOCOGNOME").length() > 0) {
                    value = rs.getString("SOCOGNOME").substring(0,1);
                } else {
                    value = "";
                }
            } else if (columnName.equals("COGNOMENOME")) {
                value = rs.getString("SOCOGNOME") + " " + rs.getString("SONOME");
            } else if (columnName.equals("IMG_SFONDO")) {
                try {
                    FileInputStream fis = new FileInputStream(app.getAppPath() + "/" + EDirectories.IMAGES.getDir() + "/" + immagine);
                    value = fis;
                } catch (IOException ioe) {
                    logger.error("Errore immagine sfondo: {}",ioe);    
                }
            } else if (columnName.equals("SOFOTO")) {
                try {
                    if (rs.getString("SOFOTO") == null) {
                        FileInputStream fis = new FileInputStream(app.getAppPath(EDirectories.IMAGES_COMMON) + "noimage.png");
                        value = fis;
                    } else {
                        FileInputStream fis = new FileInputStream(app.getAppPath(EDirectories.IMAGES_PHOTOS) + rs.getString("SOFOTO"));
                        value = fis;
                    }
                } catch (IOException ioe) {
                    logger.error("Errore immagine foto: {}",ioe);
                }
            } else if (field.getValueClass().equals(Integer.class)) {
                value = rs.getInt(columnName);
            } else if (field.getValueClass().equals(String.class)) {
                value = rs.getString(columnName);
            } else if (field.getValueClass().equals(Date.class)) {
                value = rs.getDate(columnName);
            } else {
                return "ERRORE: tipo campo non previsto: " + columnName;
            }
        } catch (SQLException e) {
            return "ERRORE: " + e;
        } 
        return value;
    }
    
    @Override
    public boolean next() throws JRException {
        try {
            return rs.next();
        } catch (SQLException e) {
            return false;
        } 
    }
}
