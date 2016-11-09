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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JTable;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import proci.App;
import proci.EDirectories;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author simeo
 */
public class TaDataSource implements JRDataSource {

    static final Logger logger = LoggerFactory.getLogger(TaDataSource.class);    
    
    ArrayList<Partecipante> al = new ArrayList<>();
    int current = 0;
    Partecipante par = null;
    JTable table = null;
    String evento = null;
    String dataEvento = null;

    private final App app = App.getInstance();
    
    public TaDataSource(ArrayList<Partecipante> al, String evento,String dataEvento) {
        this.al = al;
        this.evento = evento;
        this.dataEvento = dataEvento;
    }
    @Override
    public Object getFieldValue(JRField field) throws JRException {
        Object value = null;
        String columnName = field.getName();
        switch (columnName) {
            case "COGNOME":
                value = par.getCognome();
                break;
            case "NOME":
                value = par.getNome();
                break;
            case "COGNOMENOME":
                value = par.getCognome() + " " + par.getNome();
                break;
            case "GRUPPO":
                value = par.getGruppoDescr();
                break;
            case "EVENTO":
                value = evento;
                break;
            case "DATAEVENTO":
                value = dataEvento;
                break;
            case "ID":
                value = par.getId();
                break;
            case "IMG_TESSERARIC":
                try {
                    FileInputStream fis = new FileInputStream(app.getAppPath(EDirectories.IMAGES) + "tesseraric.png");
                    value = fis;
                } catch (IOException ioe) {
                    logger.error("Errore IMG_TESSERARIC: {}",ioe);
                }   
                break;
            case "IMG_ATTESTATO":
                try {
                    FileInputStream fis = new FileInputStream(app.getAppPath(EDirectories.IMAGES) + "attestato.png");
                    value = fis;
                } catch (IOException ioe) {
                    logger.error("Errore IMG_ATTESTATO: {}",ioe);
            }   break;
            default:
                value = "????? " + field.getName();
                break;
        }
        return value;
    }
    
    @Override
    public boolean next() throws JRException {
        if (current >= al.size()) {
            return false;
        }
        par = al.get(current);
        current++;
        return true;
    }
}
