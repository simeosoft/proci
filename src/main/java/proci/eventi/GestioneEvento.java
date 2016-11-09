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

import com.simeosoft.swing.SaveFileChooser;
import com.simeosoft.util.XlsUtils;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import proci.*;
import proci.gui.ProciInternalFrame;

/**
 * Gestione eventi - form principale
 * @author  simeo
 */
public class GestioneEvento extends JInternalFrame implements ProciInternalFrame,TreeSelectionListener {
    
    private boolean changed = false;
    private boolean soci_caricati = false;
    private Connection conn;
    private ResultSet rs;
    private Statement stmt;
    private PreparedStatement ps;
    private String gruppoDefault = "";
    private GruppiTreeModel gtm;
    private DefaultMutableTreeNode nroot;    
    private MainPanel map = null;

    private final App app = App.getInstance();    
    
    private Action actNuovoGruppo = new NuovoGruppoAction();
    private Action actEliminaTutto = new EliminaTuttoAction();
    private Action actInsSoci = new InsSociAction();
    private Action actEliminaGruppo = new EliminaGruppoAction();

    /** Creates new form GestioneEvento */
    public GestioneEvento() {
        initComponents();
        jbNuovo.setAction(actNuovoGruppo);
        try {
            conn = DBHandler.getInstance().getConnection();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(app.getMainFrame(),e,"ERRORE!",JOptionPane.ERROR_MESSAGE);
        }
        nroot = new DefaultMutableTreeNode("Evento");
        try {
            conn = DBHandler.getInstance().getConnection();
            gtm = new GruppiTreeModel(conn,nroot);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(app.getMainFrame(),e,"ERRORE!",JOptionPane.ERROR_MESSAGE);
        } 
        MouseListener popupListener = new PopupListener(new javax.swing.JPopupMenu());
        jtrGruppi.addMouseListener(popupListener);
        jtrGruppi.setModel(gtm);
        jtrGruppi.addTreeSelectionListener(this);
        jtrGruppi.setCellRenderer(new GruppiTreeCellRenderer());
        jtrGruppi.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        if(nroot.getChildCount()>0) {
            jtrGruppi.setSelectionRow(0);
            TreePath tp = jtrGruppi.getSelectionPath();
            jtrGruppi.expandPath(tp); 
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
                if (jtrGruppi.getSelectionCount() > 0) {
                    TreePath tp = jtrGruppi.getSelectionPath();
                    if (tp.getPathCount() == 0) {
                        return;
                    }
                    if (tp.getPathCount() == 1) {
                        popup.removeAll();
                        popup.add(actNuovoGruppo);
                        popup.add(actEliminaTutto);
                        popup.add(actInsSoci);
                    }
                    if (tp.getPathCount() == 2) {
                        popup.removeAll();
                        popup.add(actEliminaGruppo);
                    }
                    popup.show(e.getComponent(),e.getX(), e.getY());
                }
            }
        }
    }    
    
    private class NuovoGruppoAction extends AbstractAction {
        private static final long serialVersionUID = 386873338104212496L;
        public NuovoGruppoAction() {
            putValue(Action.NAME, "Nuovo Gruppo");
            putValue(Action.SMALL_ICON, new ImageIcon(GestioneEvento.this.app.getAppPath(EDirectories.IMAGES_COMMON) + "gruppo_add.png"));
        }
        @Override
        public void actionPerformed(ActionEvent ae) {
            gtm.nuovoGruppo();
            java.awt.EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    if (jtrGruppi.getRowCount() == 1) {
                        jtrGruppi.expandRow(0); 
                        jtrGruppi.setSelectionRow(1);
                    } else {
                        jtrGruppi.setSelectionRow(jtrGruppi.getRowCount() - 1);
                    }
                }
            });
        }
    }

    private class EliminaTuttoAction extends AbstractAction {
        public EliminaTuttoAction() {
            putValue(Action.NAME, "Elimina Tutto");
            putValue(Action.SMALL_ICON, new ImageIcon(GestioneEvento.this.app.getAppPath(EDirectories.IMAGES_COMMON) + "gruppo_del.png"));
        }
        @Override
        public void actionPerformed(ActionEvent ae) {
            if (JOptionPane.showConfirmDialog(GestioneEvento.this,"Vuoi veramente eliminare tutto?",
                    "Conferma",JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
                return;
            }
            // Andrebbe incapsulato nel modello
            try {
                conn.setAutoCommit(false);
                String sql = "delete from PARTECIPANTE";
                stmt = conn.createStatement();
                stmt.executeUpdate(sql);
                sql = "delete from GRUPPO";
                stmt = conn.createStatement();
                stmt.executeUpdate(sql);
                conn.commit();
                nroot = new DefaultMutableTreeNode("Evento");
                gtm = new GruppiTreeModel(conn,nroot);                
                jtrGruppi.setModel(gtm);
                jtrGruppi.repaint();
                java.awt.EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        jtrGruppi.setSelectionRow(0);
                    }
                });
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(GestioneEvento.this.app.getMainFrame(),e,"ERRORE!",JOptionPane.ERROR_MESSAGE);
            }         
        }
    }

    private class InsSociAction extends AbstractAction {
        public InsSociAction() {
            putValue(Action.NAME, "Inserisci Soci");
            putValue(Action.SMALL_ICON, new ImageIcon(GestioneEvento.this.app.getAppPath(EDirectories.IMAGES_COMMON) + "gruppo_add.png"));
        }
        @Override
        public void actionPerformed(ActionEvent ae) {
            try {
                gtm.aggiungiGruppoSoci();
                java.awt.EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (jtrGruppi.getRowCount() == 1) {
                            jtrGruppi.expandRow(0); 
                            jtrGruppi.setSelectionRow(1);
                        } else {
                            jtrGruppi.setSelectionRow(jtrGruppi.getRowCount() - 1);
                        }
                    }
                });
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(GestioneEvento.this.app.getMainFrame(),e,"ERRORE!",JOptionPane.ERROR_MESSAGE);
            }         
        }
    }

    private class EliminaGruppoAction extends AbstractAction {
        public EliminaGruppoAction() {
            putValue(Action.NAME, "Elimina Gruppo");
            putValue(Action.SMALL_ICON, new ImageIcon(GestioneEvento.this.app.getAppPath(EDirectories.IMAGES_COMMON) + "gruppo_del.png"));
        }
        @Override
        public void actionPerformed(ActionEvent ae) {
            if (JOptionPane.showConfirmDialog(GestioneEvento.this,"Vuoi veramente eliminare il gruppo?",
                    "Conferma",JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
                return;
            }
            int [] ar = jtrGruppi.getSelectionRows();
            final int selected = ar[0];
            try {
                gtm.eliminaGruppo((DefaultMutableTreeNode) jtrGruppi.getSelectionPath().getLastPathComponent());
                java.awt.EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        jtrGruppi.setSelectionRow(selected - 1);
                    }
                });
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(GestioneEvento.this.app.getMainFrame(),e,"ERRORE!",JOptionPane.ERROR_MESSAGE);
            }         
        }
    }    
    @Override
    public boolean isBusy() {
        return changed;
    }
    
    @Override
    public void valueChanged(TreeSelectionEvent event) {
        TreePath tp = jtrGruppi.getSelectionPath();
        if (tp == null) {
            return;
        }
        if (tp.getPathCount() == 1) {   // apro tabella soci
            map = new MainPanel(this,conn);
            jpData.removeAll();
            jpData.add(map);
            jmOpzioni.setEnabled(true);
            validate();
            return;
        }
        Object o = ((DefaultMutableTreeNode) tp.getLastPathComponent()).getUserObject();
        Gruppo gruppo = (Gruppo) o;
        EditPanel ep = new EditPanel(gruppo,this,conn);
        jpData.removeAll();
        jpData.add(ep);
        jmOpzioni.setEnabled(false);
        validate();
    }
    
    
    private void caricaSoci() {
        if (soci_caricati) {
            if (JOptionPane.showConfirmDialog(this,"E' stato già effettuato il caricamento dei soci! Vuoi caricare di nuovo?",
                    "Conferma",JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
                return;
            }
        }
        try {
            Statement stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
            String sql = "select SOCOGNOME,SONOME from SOCIO where SOTIPO = 'O' order by SOCOGNOME,SONOME ";
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                // FIXME dtm.addItem(rs.getString("SOCOGNOME"),rs.getString("SONOME"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(app.getMainFrame(),e,"ERRORE!",JOptionPane.ERROR_MESSAGE);
        }         
        soci_caricati = true;
    }
    
    /**
     * Richiamata dai pannelli in caso di editing
     */
    public void setChanged(boolean changed) {
        this.changed = changed;
        if (changed) {
            jtrGruppi.setEnabled(false);
            jbNuovo.setEnabled(false);
        } else {
            jtrGruppi.setEnabled(true);
            jbNuovo.setEnabled(true);
        }
        
    }
    
    private void fleggaTabella(String tipo, boolean flag) {
        map.fleggaTabella(tipo,flag);
    }
    
    private void esportaPart() {
        SaveFileChooser cfc = new SaveFileChooser(app.getAppPath(), ".xls", "File XLS (.xls)");
        int rc = cfc.showSaveDialog(this);
        if (rc == JFileChooser.CANCEL_OPTION) {
            return;
        }
        String filename = cfc.getSelectedFile().getPath();
        ArrayList<ArrayList<Object>> al = new ArrayList<>();
        ArrayList<Object> al2 = new ArrayList<>(3);
        al2.add("Cognome");
        al2.add("Nome");
        al2.add("Gruppo");
        al.add(al2);
        try {
            Statement stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
            String sql = "select GRUDESCR,PARCOGNOME,PARNOME,PARCELL,PARSPEC,PARNOTE from PARTECIPANTE " +
                    "join GRUPPO on (GRUID=PARGRUPPO) " +
                    "order by PARGRUPPO,PARCOGNOME,PARNOME ";
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                al2 = new ArrayList<Object>(3);
                al2.add(rs.getString("GRUDESCR"));
                al2.add(rs.getString("PARCOGNOME"));
                al2.add(rs.getString("PARNOME"));
                al2.add(rs.getString("PARCELL"));
                al2.add(rs.getString("PARSPEC"));
                al2.add(rs.getString("PARNOTE"));
                al.add(al2);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(app.getMainFrame(),e,"ERRORE!",JOptionPane.ERROR_MESSAGE);
        }         
        try {
            if (!filename.endsWith(".xls")) {
                filename = filename + ".xls";
            }
            FileOutputStream os = new FileOutputStream(filename);
            HSSFWorkbook wb = XlsUtils.initXls();
            XlsUtils.addXlsWorksheet(wb, "Partecipanti",al);
            wb.write(os);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,"<html><font color='red'>ERRORE!</font>" + e,
                    "ERRORE",
                    JOptionPane.ERROR_MESSAGE);
        } 
    }
    
    private void closeFrame() {
        if (isBusy()) {
            if (JOptionPane.showConfirmDialog(this,"Sono state fatte delle modifiche! Vuoi uscire veramente??",
                    "Conferma",JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
                return;
            }
        }
        this.dispose();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jSplitPane1 = new javax.swing.JSplitPane();
        jpData = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jtrGruppi = new javax.swing.JTree();
        jbNuovo = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jmEsporta = new javax.swing.JMenu();
        jmEsportaPart = new javax.swing.JMenuItem();
        jmOpzioni = new javax.swing.JMenu();
        jmFlaggaTessere = new javax.swing.JMenuItem();
        jmFlaggaAttestati = new javax.swing.JMenuItem();
        jmSflaggaTessere = new javax.swing.JMenuItem();
        jmSflaggaAttestati = new javax.swing.JMenuItem();

        setClosable(true);
        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("Gestione evento");
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

        jpData.setLayout(new java.awt.BorderLayout());

        jSplitPane1.setRightComponent(jpData);

        jScrollPane1.setViewportView(jtrGruppi);

        jbNuovo.setText("jButton1");

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jbNuovo)
                .addContainerGap(147, Short.MAX_VALUE))
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 247, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 497, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jbNuovo))
        );
        jSplitPane1.setLeftComponent(jPanel1);

        jmEsporta.setText("Esporta");
        jmEsportaPart.setText("Esporta partecipanti in formato XLS");
        jmEsportaPart.setToolTipText("Esporta partecipanti in formato XLS");
        jmEsportaPart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jmEsportaPartActionPerformed(evt);
            }
        });

        jmEsporta.add(jmEsportaPart);

        jMenuBar1.add(jmEsporta);

        jmOpzioni.setText("Opzioni");
        jmFlaggaTessere.setText("Imposta Flag Tessera Stampata");
        jmFlaggaTessere.setToolTipText("Imposta il flag Tessera Stampata per i partecipanti selezionati");
        jmFlaggaTessere.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jmFlaggaTessereActionPerformed(evt);
            }
        });

        jmOpzioni.add(jmFlaggaTessere);

        jmFlaggaAttestati.setText("Imposta Flag Attestato Stampato");
        jmFlaggaAttestati.setToolTipText("Imposta il flag Attestato Stampato per i partecipanti selezionati");
        jmFlaggaAttestati.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jmFlaggaAttestatiActionPerformed(evt);
            }
        });

        jmOpzioni.add(jmFlaggaAttestati);

        jmSflaggaTessere.setText("Elimina Flag Tessera Stampata");
        jmSflaggaTessere.setToolTipText("Elimina il flag Tessera Stampata per i partecipanti selezionati");
        jmSflaggaTessere.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jmSflaggaTessereActionPerformed(evt);
            }
        });

        jmOpzioni.add(jmSflaggaTessere);

        jmSflaggaAttestati.setText("Elimina Flag Attestato Stampato");
        jmSflaggaAttestati.setToolTipText("Elimina il flag Attestato stampato per i partecipanti selezionati");
        jmSflaggaAttestati.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jmSflaggaAttestatiActionPerformed(evt);
            }
        });

        jmOpzioni.add(jmSflaggaAttestati);

        jMenuBar1.add(jmOpzioni);

        setJMenuBar(jMenuBar1);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jSplitPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 707, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jSplitPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 530, Short.MAX_VALUE)
        );
        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jmEsportaPartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jmEsportaPartActionPerformed
        esportaPart();
    }//GEN-LAST:event_jmEsportaPartActionPerformed

    private void jmFlaggaAttestatiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jmFlaggaAttestatiActionPerformed
        fleggaTabella("A", true);
    }//GEN-LAST:event_jmFlaggaAttestatiActionPerformed

    private void jmFlaggaTessereActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jmFlaggaTessereActionPerformed
        fleggaTabella("T", true);
    }//GEN-LAST:event_jmFlaggaTessereActionPerformed

    private void jmSflaggaAttestatiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jmSflaggaAttestatiActionPerformed
        fleggaTabella("A", false);
    }//GEN-LAST:event_jmSflaggaAttestatiActionPerformed

    private void jmSflaggaTessereActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jmSflaggaTessereActionPerformed
        fleggaTabella("T", false);
    }//GEN-LAST:event_jmSflaggaTessereActionPerformed

    private void formInternalFrameClosing(javax.swing.event.InternalFrameEvent evt) {//GEN-FIRST:event_formInternalFrameClosing
        closeFrame();
    }//GEN-LAST:event_formInternalFrameClosing
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JButton jbNuovo;
    private javax.swing.JMenu jmEsporta;
    private javax.swing.JMenuItem jmEsportaPart;
    private javax.swing.JMenuItem jmFlaggaAttestati;
    private javax.swing.JMenuItem jmFlaggaTessere;
    private javax.swing.JMenu jmOpzioni;
    private javax.swing.JMenuItem jmSflaggaAttestati;
    private javax.swing.JMenuItem jmSflaggaTessere;
    private javax.swing.JPanel jpData;
    private javax.swing.JTree jtrGruppi;
    // End of variables declaration//GEN-END:variables
    
}
