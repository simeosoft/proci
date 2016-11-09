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

package proci;

public enum ETipoSocio {
        OPERATIVO ("Operativo","O"),
        NON_OPERATIVO ("Non operativo","N"),                
        ONORARIO ("Onorario","X");
        
    private final String desc;
    private final String val;
    ETipoSocio(String desc,String val) {
        this.desc = desc;
        this.val = val;
    }
    public String getDescr() {
        return desc;
    }
    public String getVal() {
        return val;
    }
    @Override
    public String toString() {
        return desc;
    }
}                
