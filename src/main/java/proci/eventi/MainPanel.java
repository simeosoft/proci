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

import com.simeosoft.form.FormController;
import com.simeosoft.form.IFormListener;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import proci.App;
import proci.EDirectories;

/**
 *
 * @author  simeo
 */
public class MainPanel extends javax.swing.JPanel implements IFormListener {
    
    private GestioneEvento ge;
    private Connection conn;
    private ResultSet rs;
    private Statement stmt;
    private PreparedStatement ps;
    private FormController fc;
    //
    private int parSelectedColumn = -1;
    private int parOldSelectedColumn = -1;
    private boolean parDesc = false;
    private String parCurrOrderby = "GRUDESCR,PARID";
    private PartecipantiMainTableModel pmtm = null;
    //
    private int autSelectedColumn = -1;
    private int autOldSelectedColumn = -1;
    private boolean autDesc = false;
    private String autCurrOrderby = "GRUDESCR,AUTID";
    private AutomezziMainTableModel amtm = null;

    private final App app = App.getInstance();
    
    /** Creates new form MainPanel */
    public MainPanel(GestioneEvento ge,Connection conn) {
        initComponents();
        this.ge = ge;
        this.conn = conn;
        jTabbedPane1.setIconAt(0,new ImageIcon(app.getAppPath(EDirectories.IMAGES_COMMON) + "user.png"));
        jTabbedPane1.setIconAt(1,new ImageIcon(app.getAppPath(EDirectories.IMAGES_COMMON) + "auto.png"));
        fc = new FormController(this,jlMessage);
        fc.addTextField(jtEvento,"Evento",100,false);
        fc.addTextField(jtData,"Data",100,false);
        jbSalva.setEnabled(false);
        jbSalva.setIcon(new ImageIcon(app.getAppPath(EDirectories.IMAGES_COMMON) + "salva.png"));
        jbAnnulla.setEnabled(false);
        jbAnnulla.setIcon(new ImageIcon(app.getAppPath(EDirectories.IMAGES_COMMON) + "annulla.png"));
        jbStampaAttestati.setIcon(new ImageIcon(app.getAppPath(EDirectories.IMAGES_COMMON) + "print.png"));
        jbStampaTessere.setIcon(new ImageIcon(app.getAppPath(EDirectories.IMAGES_COMMON) + "print.png"));
        
        jtaMainParte.getTableHeader().setDefaultRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                // label campo selezionato per l'ordinamento
                JLabel jl = new JLabel((String) value);
                jl.setToolTipText((String) value);
                jl.setBorder(BorderFactory.createEtchedBorder());
                if (column == parSelectedColumn) {
                    if (parDesc) {
                        jl.setIcon(new javax.swing.ImageIcon(getClass().getResource("/" + EDirectories.IMAGES_COMMON + "arrow_down.png")));
                    } else {
                        jl.setIcon(new javax.swing.ImageIcon(getClass().getResource("/" + EDirectories.IMAGES_COMMON + "arrow_up.png")));
                    }
                }
                return jl;
            }
        });        
        // installa un listener sulla tabella per l'ordinamento
        jtaMainParte.getTableHeader().addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                parSelectedColumn = jtaMainParte.columnAtPoint(e.getPoint());
                if (parSelectedColumn == -1 || e.getClickCount() ==0) {
                    return;
                }
                // se ho ricliccato sulla colonna, cambio ordinamento
                // e ricreo il modello di tabella (current_orderby rimane ovviamente lo stesso)
                if (parSelectedColumn == parOldSelectedColumn) {
                    parDesc = ! parDesc;
                } else {
                    parOldSelectedColumn = parSelectedColumn;
                    switch (parSelectedColumn + 1) {
                        case 1:
                            parCurrOrderby = "GRUDESCR";
                            break;
                        case 2:
                            parCurrOrderby = "PARCOGNOME";
                            break;
                        case 3:
                            parCurrOrderby = "PARNOME";
                            break;
                        case 4:
                            parCurrOrderby = "PARCELL";
                            break;
                        case 5:
                            parCurrOrderby = "PARSPEC";
                            break;
                        case 6:
                            parCurrOrderby = "PARNOTE";
                            break;
                    }
                }
                parSetDbModel();
                //overAdjustTable();
            }
        });
        //
        jtaMainAuto.getTableHeader().setDefaultRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                // label campo selezionato per l'ordinamento
                JLabel jl = new JLabel((String) value);
                jl.setToolTipText((String) value);
                jl.setBorder(BorderFactory.createEtchedBorder());
                if (column == autSelectedColumn) {
                    if (autDesc) {
                        jl.setIcon(new javax.swing.ImageIcon(getClass().getResource("/" + EDirectories.IMAGES_COMMON + "arrow_down.png")));
                    } else {
                        jl.setIcon(new javax.swing.ImageIcon(getClass().getResource("/" + EDirectories.IMAGES_COMMON + "arrow_up.png")));
                    }
                }
                return jl;
            }
        });        
        // installa un listener sulla tabella per l'ordinamento
        jtaMainAuto.getTableHeader().addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                autSelectedColumn = jtaMainAuto.columnAtPoint(e.getPoint());
                if (autSelectedColumn == -1 || e.getClickCount() ==0) {
                    return;
                }
                // se ho ricliccato sulla colonna, cambio ordinamento
                // e ricreo il modello di tabella (current_orderby rimane ovviamente lo stesso)
                if (autSelectedColumn == autOldSelectedColumn) {
                    autDesc = ! autDesc;
                } else {
                    autOldSelectedColumn = autSelectedColumn;
                    switch (autSelectedColumn + 1) {
                        case 1:
                            autCurrOrderby = "GRUDESCR";
                            break;
                        case 2:
                            autCurrOrderby = "AUTDESCR";
                            break;
                        case 3:
                            autCurrOrderby = "AUTTARGA";
                            break;
                        case 4:
                            autCurrOrderby = "AUTRESP";
                            break;
                        case 5:
                            autCurrOrderby = "AUTNOTE";
                            break;
                    }
                }
                autSetDbModel();
                //overAdjustTable();
            }
        });
        
        caricaForm();
    }
    
    private void caricaForm() {
        parSetDbModel();
        autSetDbModel();
        //
        try {
            stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
            String sql = "select UTILDATA from UTIL where UTILKEY = 'EVEN'";
            rs = stmt.executeQuery(sql);
            if (rs.next()) {
                fc.setString(jtEvento,rs.getString("UTILDATA"));
            }
            sql = "select UTILDATA from UTIL where UTILKEY = 'DTEV'";
            rs = stmt.executeQuery(sql);
            if (rs.next()) {
                fc.setString(jtData,rs.getString("UTILDATA"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(app.getMainFrame(),e,"ERRORE!",JOptionPane.ERROR_MESSAGE);
        } 
    }
    
    public void changed(JComponent comp) {
        setChanged(true);
    }
    
    private void setChanged(boolean change) {
        if (change) {
            jbSalva.setEnabled(true);
            jbAnnulla.setEnabled(true);
            ge.setChanged(true);
            jbStampaAttestati.setEnabled(false);
            jbStampaTessere.setEnabled(false);
        } else {
            jbSalva.setEnabled(false);
            jbAnnulla.setEnabled(false);
            ge.setChanged(false);
            jbStampaAttestati.setEnabled(true);
            jbStampaTessere.setEnabled(true);
        }
    }
    
    private void parSetDbModel() {
        pmtm = new PartecipantiMainTableModel(conn,parCurrOrderby,parDesc);
        jtaMainParte.setModel(pmtm);
    }
    
    private void autSetDbModel() {
        amtm = new AutomezziMainTableModel(conn,autCurrOrderby,autDesc);
        jtaMainAuto.setModel(amtm);
    }
    
    private void salva() {
        String sql = "";
        try {
            conn.setAutoCommit(false);
            // aggiorna evento e data evento
            sql = "delete from UTIL where UTILKEY = 'EVEN' or UTILKEY='DTEV'";
            stmt.execute(sql);
            sql = "insert into UTIL values('DTEV','" + fc.getString(jtData) + "')";
            stmt.execute(sql);
            sql = "insert into UTIL values('EVEN','" + fc.getString(jtEvento) + "')";
            stmt.execute(sql);
            conn.commit();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(app.getMainFrame(),e,"ERRORE!",JOptionPane.ERROR_MESSAGE);
        }         
        setChanged(false);
    }
    
    private void annulla() {
        caricaForm();
        setChanged(false);
    }

    private void stampaTessere() {
        Report3 r3 = new Report3(app.getMainFrame(), true, "T", jtaMainParte, fc.getString(jtEvento), fc.getString(jtData));
        r3.setVisible(true);
        if (r3.deveAggiornare()) {
            aggiornaPar("T",r3.stampaSoloSelezione(),true);
        }
    }

    private void stampaAttestati() {
        Report3 r3 = new Report3(app.getMainFrame(), true, "A", jtaMainParte, fc.getString(jtEvento), fc.getString(jtData));
        r3.setVisible(true);
        if (r3.deveAggiornare()) {
            aggiornaPar("A",r3.stampaSoloSelezione(),true);
        }
    }

    /**
     * richiamata da GestioneEvento
     */
    public void fleggaTabella(String tipo, boolean flag) {
        if (jtaMainParte.getSelectedRows().length > 0) {
            aggiornaPar(tipo,true,flag);
        } else {
            JOptionPane.showMessageDialog(app.getMainFrame(),"Devi selezionare i partecipanti!","INFO!",
                    JOptionPane.WARNING_MESSAGE);            
        }
    }
    
    private void aggiornaPar(String tipo,boolean soloSel,boolean flag) {
        String campo = "";
        if (tipo.equalsIgnoreCase("A")) {
            campo = "PARATTESTATOSTAMPATO";
        } else {
            campo = "PARTESSERASTAMPATA";
        }
        String sflag = flag ? "Y" : "N";
        String sql;
        try {
            if (soloSel) {
                if (jtaMainParte.getSelectedRows().length > 0) {
                    sql = "update PARTECIPANTE set " + campo + " = '" + sflag + "' where PARGRUPPO = ? and PARID = ?";
                    ps = conn.prepareStatement(sql);
                    int[] arsel = jtaMainParte.getSelectedRows();
                    for (int i=0; i<arsel.length; i++) {
                        int gruppo = ((Integer) pmtm.getValueAt(arsel[i],8)).intValue();
                        int id = ((Integer) pmtm.getValueAt(arsel[i],9)).intValue();
                        ps.setInt(1,gruppo);
                        ps.setInt(2,id);
                        ps.execute();
                    }
                    caricaForm();
                } else {
                    JOptionPane.showMessageDialog(app.getMainFrame(),"Devi selezionare i partecipanti!","INFO!",
                            JOptionPane.WARNING_MESSAGE);            
                }
            } else {
                sql = "update PARTECIPANTE set " + campo + " = '" + sflag + "'";    
                stmt = conn.createStatement();
                stmt.executeUpdate(sql);
                caricaForm();                
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(app.getMainFrame(),e,"ERRORE!",JOptionPane.ERROR_MESSAGE);
        }         
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jbStampaAttestati = new javax.swing.JButton();
        jbStampaTessere = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jtEvento = new javax.swing.JFormattedTextField();
        jtData = new javax.swing.JFormattedTextField();
        jLabel3 = new javax.swing.JLabel();
        jbSalva = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jtaMainParte = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jtaMainAuto = new javax.swing.JTable();
        jlMessage = new javax.swing.JLabel();
        jbAnnulla = new javax.swing.JButton();

        jbStampaAttestati.setText("stampa attestati");
        jbStampaAttestati.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbStampaAttestatiActionPerformed(evt);
            }
        });

        jbStampaTessere.setText("stampa tessere");
        jbStampaTessere.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbStampaTessereActionPerformed(evt);
            }
        });

        jLabel1.setText("evento:");

        jtEvento.setMinimumSize(new java.awt.Dimension(128, 19));
        jtEvento.setPreferredSize(new java.awt.Dimension(128, 19));

        jtData.setMinimumSize(new java.awt.Dimension(128, 19));
        jtData.setPreferredSize(new java.awt.Dimension(128, 19));

        jLabel3.setText("data:");

        jbSalva.setText("salva");
        jbSalva.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbSalvaActionPerformed(evt);
            }
        });

        jPanel1.setLayout(new java.awt.BorderLayout());

        jtaMainParte.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane2.setViewportView(jtaMainParte);

        jPanel1.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        jTabbedPane1.addTab("partecipanti", jPanel1);

        jPanel2.setLayout(new java.awt.BorderLayout());

        jtaMainAuto.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(jtaMainAuto);

        jPanel2.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jTabbedPane1.addTab("automezzi", jPanel2);

        jlMessage.setForeground(new java.awt.Color(255, 0, 0));
        jlMessage.setToolTipText("");

        jbAnnulla.setText("annulla");
        jbAnnulla.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbAnnullaActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jlMessage, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 578, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(jLabel1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jtEvento, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 237, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel3)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jtData, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 230, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(jbStampaTessere)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jbStampaAttestati)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 155, Short.MAX_VALUE)
                        .add(jbSalva)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jbAnnulla)))
                .add(12, 12, 12))
            .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 602, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(jtEvento, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel3)
                    .add(jtData, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(15, 15, 15)
                .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 382, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jlMessage, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 11, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jbStampaTessere)
                    .add(jbStampaAttestati)
                    .add(jbAnnulla)
                    .add(jbSalva))
                .add(12, 12, 12))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jbAnnullaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbAnnullaActionPerformed
        annulla();
    }//GEN-LAST:event_jbAnnullaActionPerformed

    private void jbSalvaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbSalvaActionPerformed
        salva();
    }//GEN-LAST:event_jbSalvaActionPerformed

    private void jbStampaAttestatiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbStampaAttestatiActionPerformed
        stampaAttestati();
    }//GEN-LAST:event_jbStampaAttestatiActionPerformed

    private void jbStampaTessereActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbStampaTessereActionPerformed
        stampaTessere();
    }//GEN-LAST:event_jbStampaTessereActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JButton jbAnnulla;
    private javax.swing.JButton jbSalva;
    private javax.swing.JButton jbStampaAttestati;
    private javax.swing.JButton jbStampaTessere;
    private javax.swing.JLabel jlMessage;
    private javax.swing.JFormattedTextField jtData;
    private javax.swing.JFormattedTextField jtEvento;
    private javax.swing.JTable jtaMainAuto;
    private javax.swing.JTable jtaMainParte;
    // End of variables declaration//GEN-END:variables
    
}
