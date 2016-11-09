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

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author simeo
 */
public class Automezzo {

    private int gruppo = 0;    
    private int id = 0;
    private String descrizione = "";
    private String responsabile = "";
    private String targa = "";
    private String note = "";

    /** Creates a new instance of Automezzo */
    public Automezzo(int id,int gruppo) {
        this.id = id;
        this.gruppo = gruppo;
    }
    public Automezzo(ResultSet rs) {
        try {
            gruppo = rs.getInt("AUTGRUPPO");
            id = rs.getInt("AUTID");
            descrizione = rs.getString("AUTDESCR");
            responsabile = rs.getString("AUTRESP");
            targa = rs.getString("AUTTARGA");
            note = rs.getString("AUTNOTE");
        } catch (SQLException e) {
            descrizione = e.getLocalizedMessage();
        }
    }

    public int getGruppo() {
        return gruppo;
    }

    public void setGruppo(int gruppo) {
        this.gruppo = gruppo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getResponsabile() {
        return responsabile;
    }

    public void setResponsabile(String responsabile) {
        this.responsabile = responsabile;
    }

    public String getTarga() {
        return targa;
    }

    public void setTarga(String targa) {
        this.targa = targa;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
    
}
