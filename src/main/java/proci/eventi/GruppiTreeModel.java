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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/**
 *
 * @author simeo
 */
public class GruppiTreeModel extends DefaultTreeModel {
    
    private DefaultMutableTreeNode nroot;
    private PreparedStatement ps1;
    private PreparedStatement ps2;
    private Statement st;
    private Connection conn;
    
    /**
     * Creates a new instance of SociTreeModel 
     */
    public GruppiTreeModel(Connection conn,DefaultMutableTreeNode nroot) throws SQLException {
        super(nroot);
        this.conn = conn;
        this.nroot = nroot;
        // FIXME
        //nroot = (DefaultMutableTreeNode) getRoot();
        //nroot.setUserObject(new TreeItem(TreeItemType.ROOT, 0, "Soci"));
        ps1 = conn.prepareStatement("select * from GRUPPO order by GRUID");
        ps2 = conn.prepareStatement("select PARCOGNOME,PARNOME,PARCELL from PARTECIPANTE " +
                " where PARGRUPPO = ? and PARID = 1");      //responsabile
        ResultSet rs1 = ps1.executeQuery();
        while (rs1.next()) {
            ps2.setInt(1,rs1.getInt("GRUID"));
            ResultSet rs2 = ps2.executeQuery();
            String resp = "";
            if (rs2.next()) {
                resp = rs2.getString("PARCOGNOME") + " " +
                       rs2.getString("PARNOME") + " " +
                       rs2.getString("PARCELL");
            }
            Gruppo gruppo = new Gruppo(rs1.getInt("GRUID"),rs1.getString("GRUDESCR"),resp);
            nroot.add(new DefaultMutableTreeNode(gruppo));
        }
        
    }
    
    public void nuovoGruppo() {
        Gruppo gruppo = new Gruppo(0,"Nuovo gruppo","");
        gruppo.setNew(true);
        DefaultMutableTreeNode grnode = new DefaultMutableTreeNode(gruppo);
        insertNodeInto(grnode,nroot,nroot.getChildCount());
    }
    
    public void eliminaGruppo(DefaultMutableTreeNode node) throws SQLException {
        conn.setAutoCommit(false);
        Gruppo gruppo = (Gruppo) node.getUserObject();
        ps1 = conn.prepareStatement("delete from PARTECIPANTE where PARGRUPPO = ?");
        ps1.setInt(1,gruppo.getId());
        ps1.executeUpdate();
        ps1 = conn.prepareStatement("delete from GRUPPO where GRUID = ?");
        ps1.setInt(1,gruppo.getId());
        ps1.executeUpdate();
        conn.commit();
        removeNodeFromParent(node);
    }
    
    public void aggiungiGruppoSoci() throws SQLException {
        Gruppo gruppo = new Gruppo(0,"Gruppo soci","");
        DefaultMutableTreeNode grnode = new DefaultMutableTreeNode(gruppo);
        insertNodeInto(grnode,nroot,nroot.getChildCount());
        //
        conn.setAutoCommit(false);
        int maxgruppo = 1;
        int ctrsocio = 1;   // parte da uno perchè devo indicare il responsabile
        st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
        ResultSet rs = st.executeQuery("select max(GRUID) as max_id from GRUPPO");
        if (rs.next()) {
            maxgruppo = rs.getInt("max_id");
            maxgruppo++;
        }
        gruppo.setId(maxgruppo);
        //
        String sql = "insert into GRUPPO values (?,?)";
        ps1 = conn.prepareStatement(sql);
        ps1.setInt(1, gruppo.getId());
        ps1.setString(2, gruppo.getDescr());
        ps1.execute();
        sql = "insert into PARTECIPANTE values (?,?,?,?,?,?,?,?,?,?,?)";
        ps1 = conn.prepareStatement(sql);
        rs = st.executeQuery("select SOCOGNOME,SONOME,SOTELCELL from SOCIO where SOTIPO = 'O'");
        while (rs.next()) {
            ctrsocio++;
            ps1.setInt(1, maxgruppo);
            ps1.setInt(2, ctrsocio);
            ps1.setString(3, rs.getString("SOCOGNOME"));
            ps1.setString(4, rs.getString("SONOME"));
            ps1.setString(5, rs.getString("SOTELCELL"));
            ps1.setString(6, "");
            ps1.setString(7, "");
            ps1.setTimestamp(8, null);
            ps1.setString(9, null);
            ps1.setString(10,"N");
            ps1.setString(11,"N");
            ps1.execute();
        }
        conn.commit();
    }

}
