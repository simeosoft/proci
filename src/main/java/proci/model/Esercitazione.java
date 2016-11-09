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

public class Esercitazione {
    private int ESEID;
    private String ESEDESCR;
    public Esercitazione(int ESEID,String ESEDESCR) {
        this.ESEID = ESEID;
        this.ESEDESCR = ESEDESCR;
    }
    public int getESEID() {
        return ESEID;
    }
    public void setESEID(int param) {
        this.ESEID = param;
    }
    public String getESEDESCR() {
        return ESEDESCR;
    }
    public void setESEDESCR(String param) {
        this.ESEDESCR = param;
    }
    public String toString() {
        return "(" + ESEID + ") " + ESEDESCR;
    }
}
