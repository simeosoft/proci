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

/**
 * Classe supporto per DatiSociDataSource e DatiSociDataSource2
 * @author simeo
 */
public class Detail {
    private String tipo;
    private String id;
    private String destipo;
    private String descr;
    private String taglia;
    private String quant;    
    private int idsocio;         // solo per DatiSociDataSource2
    private String nomesocio;       // solo per DatiSociDataSource2
    
    public Detail() {
        tipo = "";
        id ="";
        destipo = "";
        descr = "";
        taglia = "";
        quant = "";
    }
    public String getTipo() {
        return tipo;
    }
    
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
    
    public String getDestipo() {
        return destipo;
    }
    
    public void setDestipo(String destipo) {
        this.destipo = destipo;
    }
    
    public String getDescr() {
        return descr;
    }
    
    public void setDescr(String descr) {
        this.descr = descr;
    }
    
    public String getTaglia() {
        return taglia;
    }
    
    public void setTaglia(String taglia) {
        this.taglia = taglia;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id.toString();
    }

    public String getQuant() {
        return quant;
    }

    public void setQuant(String quant) {
        this.quant = quant;
    }

    public int getIdSocio() {
        return idsocio;
    }

    public void setIdSocio(int idsocio) {
        this.idsocio = idsocio;
    }

    public String getNomeSocio() {
        return nomesocio;
    }

    public void setNomeSocio(String nomesocio) {
        this.nomesocio = nomesocio;
    }
}