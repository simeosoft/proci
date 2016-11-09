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

/**
 *
 * @author simeo
 */
public class TreeItem {
    
    public enum TreeItemType {
        ROOT (false),
        SOCIO (true),
        DOTNODE (false),
        DOT (true),
        INTNODE (false),
        INT (true),
        ESENODE (false),
        ESE (true),
        SPENODE (false),
        SPE (true);
        private boolean dispid;
        TreeItemType(boolean dispid) {
            this.dispid = dispid;
        }
        public boolean getIdDisplay() {
            return dispid;
        }
    }

    private int id;
    private String descr;
    private TreeItemType type;
    private String imgFile = null;
    
    /** Creates a new instance of TreeItem */
    public TreeItem(TreeItemType type,int id, String descr) {
        this.type = type;
        this.id = id;
        this.descr = descr;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescr() {
        return descr;
    }

    public void setDescr(String descr) {
        this.descr = descr;
    }
    
    public String toString() {
        if (type.getIdDisplay()) {
            return descr + " (" + id + ")";
        }
        return descr;
    }
    
    public TreeItemType getType() {
        return type;
    }

    public String getImgFile() {
        return imgFile;
    }

    public void setImgFile(String imgFile) {
        this.imgFile = imgFile;
    }
}
