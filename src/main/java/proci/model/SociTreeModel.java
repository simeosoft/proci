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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import proci.model.TreeItem.TreeItemType;

/**
 *
 * @author simeo
 */
public class SociTreeModel extends DefaultTreeModel {
    
    private DefaultMutableTreeNode nroot;
    private Statement stmt;
    private Statement stmt2;
    
    /**
     * Creates a new instance of SociTreeModel 
     */
    public SociTreeModel(Connection conn,DefaultMutableTreeNode nroot) throws SQLException {
        super(nroot);
        this.nroot = nroot;
        nroot = (DefaultMutableTreeNode) getRoot();
        nroot.setUserObject(new TreeItem(TreeItemType.ROOT, 0, "Soci"));
        conn.setAutoCommit(false);
        stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
        stmt2 = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
        ResultSet rs = stmt.executeQuery("select * from SOCIO order by SOCOGNOME,SONOME");
        while (rs.next()) {
            int soid = rs.getInt("SOID");
            TreeItem ti = new TreeItem(TreeItemType.SOCIO, soid, 
                    rs.getString("SOCOGNOME") + " " + rs.getString("SONOME"));
            ti.setImgFile(rs.getString("SOFOTO"));
            addSocio(ti, false);
        }
        
    }
    
    public void addSocio(TreeItem ti, boolean insert) throws SQLException {
        DefaultMutableTreeNode socionode = new DefaultMutableTreeNode(ti);
        if (insert) {
            insertNodeInto(socionode,nroot,nroot.getChildCount());
        } else {
            nroot.add(socionode);
        }
        TreeItem tiTemp = null;
        // dotazioni
        ResultSet rs2 = stmt2.executeQuery("select DOTDESCR,DOTID,DOTTAGLIA,SODOQUANT from SODO join DOTAZIONE on (SODODO = DOTID) " +
                "where SODOSO = " + ti.getId());
        tiTemp = new TreeItem(TreeItemType.DOTNODE, 0, "Dotazioni");
        DefaultMutableTreeNode dotnode = new DefaultMutableTreeNode(tiTemp);
        socionode.add(dotnode);
        while (rs2.next()) {
            tiTemp = new TreeItem(TreeItemType.DOT, rs2.getInt("DOTID"), 
                    rs2.getString("DOTDESCR") + " tg.: " + rs2.getString("DOTTAGLIA") + 
                            " q.: " + rs2.getInt("SODOQUANT"));
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(tiTemp);
            dotnode.add(node);
        }
        // interventi
        rs2 = stmt2.executeQuery("select INTDESCR,INTID from SOIN join INTERVENTO on (SOININ = INTID) " +
                "where SOINSO = " + ti.getId());
        tiTemp = new TreeItem(TreeItemType.INTNODE, 0, "Interventi");
        DefaultMutableTreeNode intnode = new DefaultMutableTreeNode(tiTemp);
        socionode.add(intnode);
        while (rs2.next()) {
            tiTemp = new TreeItem(TreeItemType.INT, rs2.getInt("INTID"), 
                    rs2.getString("INTDESCR"));
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(tiTemp);
            intnode.add(node);
        }
        // esercitazioni
        rs2 = stmt2.executeQuery("select ESEDESCR,ESEID from SOES join ESERCITAZIONE on (SOESES = ESEID) " +
                "where SOESSO = " + ti.getId());
        tiTemp = new TreeItem(TreeItemType.ESENODE, 0, "Esercitazioni");
        DefaultMutableTreeNode esenode = new DefaultMutableTreeNode(tiTemp);
        socionode.add(esenode);
        while (rs2.next()) {
            tiTemp = new TreeItem(TreeItemType.ESE, rs2.getInt("ESEID"), 
                    rs2.getString("ESEDESCR"));
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(tiTemp);
            esenode.add(node);
        }
        // specializzazioni
        rs2 = stmt2.executeQuery("select SPEDESCR,SPEID from SOSP join SPECIALIZZAZIONE on (SOSPSP = SPEID) " +
                "where SOSPSO = " + ti.getId());
        tiTemp = new TreeItem(TreeItemType.SPENODE, 0, "Specializzazioni");
        DefaultMutableTreeNode spenode = new DefaultMutableTreeNode(tiTemp);
        socionode.add(spenode);
        while (rs2.next()) {
            tiTemp = new TreeItem(TreeItemType.SPE, rs2.getInt("SPEID"), 
                    rs2.getString("SPEDESCR"));
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(tiTemp);
            spenode.add(node);
        }
    }
}
