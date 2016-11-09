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

/**
 *
 * @author simeo
 */
public class Gruppo {
    private int id = 0;
    private String descr = "";
    private String resp = "";   // combinazione cognome/nome/cellulare per tooltip
    private boolean isNew = false;
    
    /** Creates a new instance of Gruppo */
    public Gruppo(int id,String descr,String resp) {
        this.id = id;
        this.setDescr(descr);
        this.resp = resp;
    }
    
    public String toString() {
        return getDescr();
    }

    public int getId() {
        return id;
    }

    public String getDescr() {
        return descr;
    }

    public void setDescr(String descr) {
        this.descr = descr;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }
}
