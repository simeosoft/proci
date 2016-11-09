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


/**
 * Data source per report dati socio.<br>
 * Per ragioni di compatibilità con il vecchio report 
 * viene mantenuta la gestione tramite oggetti Detail.
 * @author simeo
 */
public class DatiSociDataSource2 implements JRDataSource {
    
    ArrayList<Detail> aldett = new ArrayList<>();
    int current_dett = 0;
    Detail dett = null;
    
    public DatiSociDataSource2(Connection conn, int from, int to, String tipo) {
        Statement stmt;
        ResultSet rs;
        String criteria = " where SOTIPO = '" + ETipoSocio.OPERATIVO.getVal() + "'";
        if (from != 0) {
            criteria = criteria + " and SOID >= " + from;
        }
        if (to != 0) {
            criteria = criteria + " and SOID <= " + to;
        }
        try {
            stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
            if (tipo.equals("tutti ") || tipo.equals("solo d")) {
                // dotazioni        
                rs = stmt.executeQuery("select DOTID,DOTDESCR,DOTTAGLIA,SOCOGNOME,SONOME,SOID,SODOQUANT from SODO " +
                                        "join DOTAZIONE on (SODODO = DOTID) join socio on (SODOSO = SOID) " +
                                        criteria + 
                                        "order by SODODO,DOTTAGLIA,SOID");
                while (rs.next()) {
                    Detail det = new Detail();
                    det.setTipo("D");
                    det.setId(rs.getInt("DOTID"));
                    det.setDescr(rs.getString("DOTDESCR"));
                    det.setDestipo("dotazioni:");
                    det.setTaglia(rs.getString("DOTTAGLIA"));
                    det.setQuant(rs.getInt("SODOQUANT") + "");
                    det.setIdSocio(rs.getInt("SOID"));
                    det.setNomeSocio(rs.getString("SOCOGNOME") + " " + rs.getString("SONOME"));
                    aldett.add(det);       
                }
            }
            if (tipo.equals("tutti ") || tipo.equals("solo i")) {
                // interventi
                rs = stmt.executeQuery("select INTDESCR,INTID,SOCOGNOME,SONOME,SOID from SOIN " +
                                        "join INTERVENTO on (SOININ = INTID) join socio on (SOINSO = SOID) " +
                                        criteria + 
                                        "order by SOININ,SOID");
                while (rs.next()) {
                    Detail det = new Detail();
                    det.setTipo("I");
                    det.setId(rs.getInt("INTID"));
                    det.setDescr(rs.getString("INTDESCR"));
                    det.setDestipo("interventi:");
                    det.setTaglia("");
                    det.setQuant("");
                    det.setIdSocio(rs.getInt("SOID"));
                    det.setNomeSocio(rs.getString("SOCOGNOME") + " " + rs.getString("SONOME"));
                    aldett.add(det);            
                }
            }
            if (tipo.equals("tutti ") || tipo.equals("solo e")) {
                // esercitazioni
                rs = stmt.executeQuery("select ESEDESCR,ESEID,SOCOGNOME,SONOME,SOID from SOES " +
                                        "join ESERCITAZIONE on (SOESES = ESEID) join socio on (SOESSO = SOID) " +
                                        criteria + 
                                        "order by SOESES,SOID");
                while (rs.next()) {
                    Detail det = new Detail();
                    det.setTipo("E");
                    det.setId(rs.getInt("ESEID"));
                    det.setDescr(rs.getString("ESEDESCR"));
                    det.setDestipo("esercitazioni:");
                    det.setTaglia("");
                    det.setQuant("");
                    det.setIdSocio(rs.getInt("SOID"));
                    det.setNomeSocio(rs.getString("SOCOGNOME") + " " + rs.getString("SONOME"));
                    aldett.add(det);            
                }
            }
            if (tipo.equals("tutti ") || tipo.equals("solo s")) {
                // specializzazioni
                rs = stmt.executeQuery("select SPEDESCR,SPEID,SOCOGNOME,SONOME,SOID from SOSP " +
                                        "join SPECIALIZZAZIONE on (SOSPSP = SPEID) join socio on (SOSPSO = SOID) " +
                                        criteria + 
                                        "order by SOSPSP,SOID");
                while (rs.next()) {
                    Detail det = new Detail();
                    det.setTipo("S");
                    det.setId(rs.getInt("SPEID"));
                    det.setDescr(rs.getString("SPEDESCR"));
                    det.setDestipo("specializzazioni:");
                    det.setTaglia("");
                    det.setQuant("");
                    det.setIdSocio(rs.getInt("SOID"));
                    det.setNomeSocio(rs.getString("SOCOGNOME") + " " + rs.getString("SONOME"));
                    aldett.add(det);            
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(App.getInstance().getMainFrame(),e,"ERRORE!",JOptionPane.ERROR_MESSAGE);
        }         
        
    }
    @Override
    public Object getFieldValue(JRField field) throws JRException {
        Object value = null;
        String columnName = field.getName();
        switch (columnName) {
            case "NOME":
                value = dett.getNomeSocio();
                break;
            case "SOID":
                value = dett.getIdSocio();
                break;
            case "TIPO":
                value = dett.getTipo();
                break;
            case "DESTIPO":
                value = dett.getDestipo();
                break;
            case "DESCR":
                value = dett.getDescr();
                break;
            case "TAGLIA":
                value = dett.getTaglia();
                break;
            case "QUANT":
                value = dett.getQuant();
                break;
            case "ID":
                value = dett.getId();
                break;
            default:
                value = "????? " + field.getName();
                break;
        }
        return value;
    }
    
    @Override
    public boolean next() throws JRException {
        if (current_dett >= aldett.size() || aldett.isEmpty()) {
            return false;
        }
        dett = aldett.get(current_dett);
        current_dett++;            
        return true;
    }
}
