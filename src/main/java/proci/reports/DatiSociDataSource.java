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
import java.util.ArrayList;
import javax.swing.JOptionPane;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import proci.ETipoSocio;
import proci.App;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Data source per report dati socio.<br>
 * Per ragioni di compatibilità con il vecchio report 
 * viene mantenuta la gestione tramite oggetti Detail.
 * @author simeo
 */
public class DatiSociDataSource implements JRDataSource {
    static final Logger logger = LoggerFactory.getLogger(DatiSociDataSource.class);        
    ArrayList<Detail> aldett = new ArrayList<Detail>();
    int current_dett = 0;
    int tot_dett = 0;    
    boolean inDetail = false;
    Detail dett = null;
    private ResultSet rs = null;
    private Statement stmt = null;
    private Statement stmt2 = null;
    
    public DatiSociDataSource(Connection conn, int from, int to) {
        try {
            stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
            // aggiunto per conversione Derby
            stmt2 = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);            
            String sql = "select * from SOCIO where 1=1 ";
            if (from != 0) {
                sql = sql + " and SOID >= " + from;
            }
            if (to != 0) {
                sql = sql + " and SOID <= " + to;
            }
            sql = sql + " and SOTIPO = '" + ETipoSocio.OPERATIVO.getVal() + "'";
            sql = sql + " order by SOCOGNOME,SONOME ";
            rs = stmt.executeQuery(sql);
            logger.debug("Query sql {}",sql);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(App.getInstance().getMainFrame(),e,"ERRORE!",JOptionPane.ERROR_MESSAGE);
        }
    }
    public Object getFieldValue(JRField field) throws JRException {
        Object value = null;
        String columnName = field.getName();
        logger.debug("getFieldValue reading field: {}...",columnName);
        try {
            switch (columnName) {
                case "SOCOGNOME":
                case "SONOME":
                case "SOTELABI":
                case "SOTELCELL":
                    value = rs.getString(columnName);
                    logger.debug("getFieldValue... String value: {}",value);
                    break;
                case "SOID":
                    value = rs.getInt(columnName);
                    logger.debug("getFieldValue... int value: {}",value);
                    break;
                case "TIPO":
                    if (aldett.size() > 0) {            
                        value = dett.getTipo();
                    } else {
                        value = "";
                    }
                    logger.debug("getFieldValue... value: {}",value);
                    break;
                case "DESTIPO":
                    if (aldett.size() > 0) {
                        value = dett.getDestipo();
                    } else {
                        value = "";
                    }
                    logger.debug("getFieldValue... value: {}",value);
                    break;
                case "DESCR":
                    if (aldett.size() > 0) {
                        value = dett.getDescr();
                    } else {
                        value = "";
                    }
                    logger.debug("getFieldValue... value: {}",value);
                    break;
                case "TAGLIA":
                    if (aldett.size() > 0) {
                        value = dett.getTaglia();
                    } else {
                        value = "";
                    }
                    logger.debug("getFieldValue... value: {}",value);
                    break;
                case "QUANT":
                    if (aldett.size() > 0) {
                        value = dett.getQuant();
                    } else {
                        value = "";
                    }
                    logger.debug("getFieldValue... value: {}",value);
                    break;
                case "ID":
                    if (aldett.size() > 0) {
                        value = dett.getId();
                    } else {
                        value = "";
                    }
                    logger.debug("getFieldValue... value: {}",value);
                    break;
                default:
                    value = "????? " + field.getName();
                    logger.debug("ERRORE: getFieldValue {}",value);
            }
            logger.debug("getFieldValue: RETURNING: {}",value);            
            return value;
        } catch (SQLException e) {
            return e;
        }             
    }
    
    public boolean next() throws JRException {
        logger.debug("NEXT");        
        try {        
            if (current_dett >= tot_dett) {
                inDetail = false;
            }
            if (! inDetail && ! rs.next()) {
                return false;
            }
            if (! inDetail) {
                current_dett = 0;
                tot_dett = 0;
                buildDetail(rs.getInt("SOID"));
                if (aldett.size() > 0) {
                    dett = aldett.get(current_dett);
                    current_dett++;                
                    tot_dett = aldett.size();
                    inDetail = true;
                }
            } else {        
                dett = aldett.get(current_dett);
                current_dett++;            
            }        
            return true;
        } catch (SQLException e) {
            return false;
        }             
    }
    
    private void buildDetail(int id) {
        aldett = new ArrayList<Detail>();
        current_dett = 0;
        try {
            // dotazioni        
            ResultSet rs2 = stmt2.executeQuery("select DOTDESCR,DOTID,DOTTAGLIA,SODOQUANT from SODO join DOTAZIONE on (SODODO = DOTID) " +
                    "where SODOSO = " + id + "order by DOTDESCR,DOTTAGLIA");
            while (rs2.next()) {
                Detail det = new Detail();
                det.setTipo("D");
                det.setId(rs2.getInt("DOTID"));
                det.setDescr(rs2.getString("DOTDESCR"));
                det.setDestipo("dotazioni:");
                det.setTaglia(rs2.getString("DOTTAGLIA"));
                det.setQuant(rs2.getInt("SODOQUANT") + "");
                aldett.add(det);            
            }
            // interventi
            rs2 = stmt2.executeQuery("select INTDESCR,INTID from SOIN join INTERVENTO on (SOININ = INTID) " +
                    "where SOINSO = " + id + "order by INTDESCR");
            while (rs2.next()) {
                Detail det = new Detail();
                det.setTipo("I");
                det.setId(rs2.getInt("INTID"));
                det.setDescr(rs2.getString("INTDESCR"));
                det.setDestipo("interventi:");
                aldett.add(det);            
            }
            // esercitazioni
            rs2 = stmt2.executeQuery("select ESEDESCR,ESEID from SOES join ESERCITAZIONE on (SOESES = ESEID) " +
                    "where SOESSO = " + id  + "order by ESEDESCR");
            while (rs2.next()) {
                Detail det = new Detail();
                det.setTipo("E");
                det.setId(rs2.getInt("ESEID"));
                det.setDescr(rs2.getString("ESEDESCR"));
                det.setDestipo("esercitazioni:");
                aldett.add(det);            
            }
            // specializzazioni
            rs2 = stmt2.executeQuery("select SPEDESCR,SPEID from SOSP join SPECIALIZZAZIONE on (SOSPSP = SPEID) " +
                    "where SOSPSO = " + id + "order by SPEDESCR");
            while (rs2.next()) {
                Detail det = new Detail();
                det.setTipo("S");
                det.setId(rs2.getInt("SPEID"));
                det.setDescr(rs2.getString("SPEDESCR"));
                det.setDestipo("specializzazioni:");
                aldett.add(det);            
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(App.getInstance().getMainFrame(),e,"ERRORE id: " + id + "!",JOptionPane.ERROR_MESSAGE);
        }         
    }
}
