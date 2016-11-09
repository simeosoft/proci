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

package proci.gui;
import proci.gui.render.SociTreeCellRenderer;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingWorker;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import proci.model.SociTreeModel;
import proci.*;
import proci.model.TreeItem;
import proci.model.TreeItem.TreeItemType;
import proci.util.ProciUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author  simeo
 */
public class GestioneSoci extends javax.swing.JInternalFrame 
                            implements TreeSelectionListener, ProciInternalFrame {
    
    static final Logger logger = LoggerFactory.getLogger(GestioneSoci.class);    
    private SociTreeModel dtm;
    
    private JMenuItem jmpAddSocio = new JMenuItem();
    private JMenuItem jmpDeleteSocio = new JMenuItem();
    
    private boolean editing = false;
    
    private DefaultMutableTreeNode nroot;
    private Connection conn = null;
    private PreparedStatement ps;

    private final App app = App.getInstance();
    
    /** Creates new form GestioneSoci */
    public GestioneSoci() {
        initComponents();
        jbNuovo.setIcon(new ImageIcon(app.getAppPath(EDirectories.IMAGES_COMMON) + "user_add.png"));
        jlStatus.setText(" ");
        jlMessage.setText(" ");
        nroot = new DefaultMutableTreeNode("Soci");
        //
        jmpAddSocio.setText("Inserisci nuovo socio");
        jmpAddSocio.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addSocio();
            }
        });
        jmpDeleteSocio.setText("Elimina socio");
        jmpDeleteSocio.addActionListener(new java.awt.event.ActionListener() {
            @Override
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                popupDeleteSocio();
            }
        });
        MouseListener popupListener = new PopupListener(jpopTree);
        jtrSoci.addMouseListener(popupListener);
        //
        try {
            conn = DBHandler.getInstance().getConnection();
            jlMessage.setText("Caricamento in corso...");
            TreeLoader tloader = new TreeLoader();
            tloader.execute();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(app.getMainFrame(),e,"ERRORE!",JOptionPane.ERROR_MESSAGE);
        } 
    }
    
    private class TreeLoader extends SwingWorker<SociTreeModel, Object> {
        
        @Override
        protected SociTreeModel doInBackground() throws Exception {
            // aggiorna le miniature (era un metodo nel costruttore)
            File imgDir = new File(app.getAppPath(EDirectories.IMAGES_PHOTOS));
            File[] imgNormFiles = imgDir.listFiles();
            for (File imgNorm : imgNormFiles) {
                 File imgMini = new File(app.getAppPath(EDirectories.IMAGES_THUMBS) + imgNorm.getName());
                 if (! imgMini.exists() || imgMini.lastModified() < imgNorm.lastModified()) {
                     ProciUtils.creaMiniatura(imgNorm);
                 }
            }            
            // modello dell'albero dei soci
            dtm = new SociTreeModel(conn,nroot);
            return dtm;
        }
        
        @Override
        protected void done() {
            jtrSoci.setModel(dtm);
            jtrSoci.addTreeSelectionListener(GestioneSoci.this);
            jtrSoci.setCellRenderer(new SociTreeCellRenderer());
            jtrSoci.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
            if(nroot.getChildCount()>0) {
                jtrSoci.setSelectionRow(0);
                TreePath tp = jtrSoci.getSelectionPath();
                jtrSoci.expandPath(tp); 
            }        
            jlMessage.setText("Caricati " + dtm.getChildCount(dtm.getRoot()) + " soci.");
        }
    }
    
    /**
     * Restituisce lo stato corrente
     * @return 
     */
    @Override
    public boolean isBusy() {
        return editing;
    }
    
    /**
     * Richiamata dal pannello SocioPanel in caso
     * di inserimento nuovo socio
     * @param ti TreeItem da inserire (tipo socio)
     */
    public void insertSocio(TreeItem ti) {
        try {
            dtm.addSocio(ti,true);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(app.getMainFrame(),e,"ERRORE insert socio!",JOptionPane.ERROR_MESSAGE);
        }   
        jtrSoci.repaint();
        validate();
    }    
    
    private void addSocio() {
        SocioPanel sp = new SocioPanel(null,this,jlMessage);
        jpData.removeAll();
        jpData.add(sp);
        jtrSoci.setEnabled(false);
        jbNuovo.setEnabled(false);
        validate();
    }
    
    private void popupDeleteSocio() {
        if (JOptionPane.showConfirmDialog(this,"Confermi l'eliminazione?","Conferma",JOptionPane.YES_NO_OPTION)
                    == JOptionPane.CANCEL_OPTION) {
            return;
        }
        TreePath tp = jtrSoci.getSelectionPath();
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tp.getLastPathComponent();
        TreeItem ti = (TreeItem)  node.getUserObject();
        try {
            ps = conn.prepareStatement("delete from SOCIO where SOID = ?");
            ps.setInt(1,ti.getId());
            ps.execute();            
            ps = conn.prepareStatement("delete from SODO where SODOSO = ?");
            ps.setInt(1,ti.getId());
            ps.execute();
            ps = conn.prepareStatement("delete from SOIN where SOINSO = ?");
            ps.setInt(1,ti.getId());
            ps.execute();
            ps = conn.prepareStatement("delete from SOES where SOESSO = ?");
            ps.setInt(1,ti.getId());
            ps.execute();
            ps = conn.prepareStatement("delete from SOSP where SOSPSO = ?");
            ps.setInt(1,ti.getId());
            ps.execute();
            if (ti.getImgFile() != null) {
                File oldImage = new File(app.getAppPath(EDirectories.IMAGES_PHOTOS) + ti.getImgFile());
                oldImage.delete();
                oldImage = new File(app.getAppPath(EDirectories.IMAGES_THUMBS) + ti.getImgFile());
                oldImage.delete();
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(app.getMainFrame(),e,"ERRORE cancellazione socio!",JOptionPane.ERROR_MESSAGE);
            logger.error("ERRORE cancellazione socio!: {}",e);
        }            
        dtm.removeNodeFromParent(node);
        jtrSoci.setSelectionRow(0);
        validate();
    }
    
    public void setEditing(boolean editing) {
        this.editing = editing;
        if (editing) {
            jlStatus.setText("MODIFICA");
            jlStatus.setForeground(Color.RED);
            jtrSoci.setEnabled(false);
            jbNuovo.setEnabled(false);
        } else {
            jlStatus.setText(" ");
            jlStatus.setForeground(Color.BLACK);
            jtrSoci.setEnabled(true);
            jbNuovo.setEnabled(true);
        }
    }
    
    @Override
    public void valueChanged(TreeSelectionEvent event) {
        TreePath tp = jtrSoci.getSelectionPath();
        if (tp == null) {
            return;
        }
        if (tp.getPathCount() == 1) {   // apro tabella soci
            ElencoSociPanel esp = new ElencoSociPanel();
            jpData.removeAll();
            jpData.add(esp);
            validate();
            return;
        }
        Object o = ((DefaultMutableTreeNode) tp.getLastPathComponent()).getUserObject();
        TreeItem ti = (TreeItem) o;
        if (ti.getType() == TreeItemType.SOCIO) {
            SocioPanel sp = new SocioPanel(ti,this,jlMessage);
            jpData.removeAll();
            jpData.add(sp);
            validate();
        }
    }
    
    private class PopupListener extends MouseAdapter {
        JPopupMenu popup;
        PopupListener(JPopupMenu popupMenu) {
            popup = popupMenu;
        }
        @Override
        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }
        @Override
        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }
        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                if (jtrSoci.getSelectionCount() > 0) {
                    TreePath tp = jtrSoci.getSelectionPath();
                    if (tp.getPathCount() == 0) {
                        return;
                    }
                    if (tp.getPathCount() == 1) {
                        popup.removeAll();
                        popup.add(jmpAddSocio);
                    }
                    if (tp.getPathCount() == 2) {
                        popup.removeAll();
                        popup.add(jmpDeleteSocio);
                    }
                    popup.show(e.getComponent(),e.getX(), e.getY());
                }
            }
        }
    }
    
    public void closeFrame() {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {}
        }
        this.dispose();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jpopTree = new javax.swing.JPopupMenu();
        jSplitPane1 = new javax.swing.JSplitPane();
        jpData = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jtrSoci = new javax.swing.JTree();
        jPanel2 = new javax.swing.JPanel();
        jbNuovo = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jlStatus = new javax.swing.JLabel();
        jlMessage = new javax.swing.JLabel();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("Gestione Soci");
        addInternalFrameListener(new javax.swing.event.InternalFrameListener() {
            public void internalFrameActivated(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameClosed(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameClosing(javax.swing.event.InternalFrameEvent evt) {
                formInternalFrameClosing(evt);
            }
            public void internalFrameDeactivated(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameDeiconified(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameIconified(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameOpened(javax.swing.event.InternalFrameEvent evt) {
            }
        });

        jSplitPane1.setDividerLocation(150);
        jSplitPane1.setMinimumSize(new java.awt.Dimension(100, 100));

        jpData.setLayout(new java.awt.BorderLayout());
        jSplitPane1.setRightComponent(jpData);

        jPanel3.setLayout(new java.awt.BorderLayout());

        jScrollPane1.setMinimumSize(new java.awt.Dimension(100, 22));

        jtrSoci.setModel(null);
        jtrSoci.setMinimumSize(new java.awt.Dimension(79, 72));
        jScrollPane1.setViewportView(jtrSoci);

        jPanel3.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jbNuovo.setText("nuovo socio");
        jbNuovo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbNuovoActionPerformed(evt);
            }
        });
        jPanel2.add(jbNuovo);

        jPanel3.add(jPanel2, java.awt.BorderLayout.SOUTH);

        jSplitPane1.setLeftComponent(jPanel3);

        getContentPane().add(jSplitPane1, java.awt.BorderLayout.CENTER);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jlStatus.setText("jLabel1");
        jlStatus.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 30.0;
        jPanel1.add(jlStatus, gridBagConstraints);

        jlMessage.setText("jLabel2");
        jlMessage.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 70.0;
        jPanel1.add(jlMessage, gridBagConstraints);

        getContentPane().add(jPanel1, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jbNuovoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbNuovoActionPerformed
        addSocio();
    }//GEN-LAST:event_jbNuovoActionPerformed
        
    private void formInternalFrameClosing(javax.swing.event.InternalFrameEvent evt) {//GEN-FIRST:event_formInternalFrameClosing
        closeFrame();
    }//GEN-LAST:event_formInternalFrameClosing
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JButton jbNuovo;
    private javax.swing.JLabel jlMessage;
    private javax.swing.JLabel jlStatus;
    private javax.swing.JPanel jpData;
    private javax.swing.JPopupMenu jpopTree;
    private javax.swing.JTree jtrSoci;
    // End of variables declaration//GEN-END:variables
    
}
