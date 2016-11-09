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
import java.sql.Timestamp;

/**
 *
 * @author simeo
 */
public class Partecipante {
    
    private int id = 0;
    private String cognome = "";
    private String nome = "";
    private String cellulare = "";
    private String specializzazione = "";
    private String note = "";
    private Timestamp syncdate = null;
    private String syncip = "";
    private int gruppo = 0;
    private boolean tesseraStampata = false;
    private boolean attestatoStampato = false;
    // usato per i report in caso di query con join a gruppo
    private String gruppoDescr = "";    
    
    /** Creates a new instance of Partecipante */
    public Partecipante(int id,int gruppo) {
        this.id = id;
        this.gruppo = gruppo;
    }
    public Partecipante(ResultSet rs) {
        try {
            gruppo = rs.getInt("PARGRUPPO");
            id = rs.getInt("PARID");
            cognome = rs.getString("PARCOGNOME");
            nome = rs.getString("PARNOME");
            cellulare = rs.getString("PARCELL");
            specializzazione = rs.getString("PARSPEC");
            note = rs.getString("PARNOTE");
            syncdate = rs.getTimestamp("PARSYNCDATE");
            syncip = rs.getString("PARSYNCIP");
            tesseraStampata = rs.getString("PARTESSERASTAMPATA").equals("Y") ? true : false;
            attestatoStampato = rs.getString("PARATTESTATOSTAMPATO").equals("Y") ? true : false;
        } catch (SQLException e) {
            cognome = e.getLocalizedMessage();
        }
    }

    public String getCognome() {
        return cognome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getGruppo() {
        return gruppo;
    }

    public void setGruppo(int gruppo) {
        this.gruppo = gruppo;
    }

    public boolean isTesseraStampata() {
        return tesseraStampata;
    }

    public void setTesseraStampata(boolean tessera) {
        tesseraStampata = tessera;
    }

    public boolean isAttestatoStampato() {
        return attestatoStampato;
    }

    public void setAttestatoStampato(boolean attestato) {
        attestatoStampato = attestato;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCellulare() {
        return cellulare;
    }

    public void setCellulare(String cellulare) {
        this.cellulare = cellulare;
    }

    public String getSpecializzazione() {
        return specializzazione;
    }

    public void setSpecializzazione(String specializzazione) {
        this.specializzazione = specializzazione;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Timestamp getSyncdate() {
        return syncdate;
    }

    public String getSyncip() {
        return syncip;
    }

    public String getGruppoDescr() {
        return gruppoDescr;
    }

    public void setGruppoDescr(String gruppoDescr) {
        this.gruppoDescr = gruppoDescr;
    }
}
