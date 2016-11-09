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

import proci.gui.render.ProciBigDecimalRenderer;
import proci.gui.render.ProciStringRenderer;
import proci.gui.render.ProciIntegerRenderer;
import com.simeosoft.form.AddFieldException;
import com.simeosoft.form.FieldException;
import com.simeosoft.form.FormController;
import com.simeosoft.form.IFormListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import proci.*;
import proci.model.PeriodiTableModel;
import proci.model.Periodo;

/**
 *
 * @author  simeo
 */
public class GestionePeriodi extends JInternalFrame implements ProciInternalFrame,IFormListener {
    private static final long serialVersionUID = 7490116808676787538L;
    static final Logger logger = LoggerFactory.getLogger(MainMenu.class);        
    
    private boolean changed = false;
    private Periodo currentPeriod = null;     // null = insert, object = edit
    private PeriodiTableModel ptm = null;
    private Connection conn;
    private Statement stmt;
    private PreparedStatement ps;
    private ResultSet rs;    
    private final App app = App.getInstance();  
    private FormController fc;
    
    /** Creates new form GestionePeriodi */
    public GestionePeriodi() {
        initComponents();
        try {
            conn = DBHandler.getInstance().getConnection();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(app.getMainFrame(),e,"ERRORE!",JOptionPane.ERROR_MESSAGE);
        }
        ptm = new PeriodiTableModel(conn);
        jtaPer = new JTable(ptm) {
            private static final long serialVersionUID = -673980102687745913L;
            private final KeyStroke tabKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
            @Override
            public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend) {
                changeSel(rowIndex);
                super.changeSelection(rowIndex, columnIndex, toggle, extend);
            }
        };
        jtaPer.setDefaultRenderer(String.class, new ProciStringRenderer());
        jtaPer.setDefaultRenderer(Integer.class, new ProciIntegerRenderer());
        jtaPer.setDefaultRenderer(BigDecimal.class, new ProciBigDecimalRenderer(2));
        jtaPer.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(jtaPer);
        jbEdit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                edit();
            }
        });
        jbNew.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                newItem();
            }
        });        
        // form
        fc = new FormController(this, jlMsg);
        fc.addTextField(jtAnnocomp, "anno competenza", 4, true);
        fc.addTextField(jtDescr, "descrizione", 100, false);
        fc.addTextField(jtaNote, "note", 255, false);
        try {
            fc.addDecimalField(jtQuota, "quota intera", 10, 2, true, false);
            fc.addDecimalField(jtQuotarid, "quota intera", 10, 2, false, false);
        } catch (AddFieldException afe) {
            logger.error("Add field exc: {}",afe);
        }
        jbSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                save();
            }
        });
        jbCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancel();
            }
        });
    }

    @Override
    public void changed(JComponent jc) {
        changed = true;
        jbCancel.setEnabled(true);
        jbSave.setEnabled(true);        
    }
    
    private void changeSel(int rowIndex) {
        logger.debug("Change sel: {}",rowIndex);
        jbDelete.setEnabled(true);
        jbEdit.setEnabled(true);
    }
    
    private void edit() {
        if (jtaPer.getSelectedRow() == -1) {
            return;
        }
        jtaPer.setEnabled(false);
        jtAnnocomp.setEnabled(true);
        jtDescr.setEnabled(true);
        jtaNote.setEnabled(true);
        jtQuota.setEnabled(true);
        jtQuotarid.setEnabled(true);
        Periodo p = ((PeriodiTableModel) jtaPer.getModel()).getItemAt(jtaPer.getSelectedRow());
        currentPeriod = p;
        fc.setString(jtAnnocomp,p.getPERANNOCOMP());
        fc.setString(jtDescr,p.getPERDESCR());
        fc.setString(jtaNote,p.getPERNOTE());
        fc.setDecimal(jtQuota,p.getPERQUOTA());
        fc.setDecimal(jtQuotarid,p.getPERQUOTARID());
        jbNew.setEnabled(false);
        jbDelete.setEnabled(false);
        jbEdit.setEnabled(false);
        jbCancel.setEnabled(true);
        jbSave.setEnabled(false);
        jtAnnocomp.requestFocus();
    }
    
    private void newItem() {
        jtaPer.setEnabled(false);
        jtAnnocomp.setEnabled(true);
        jtDescr.setEnabled(true);
        jtaNote.setEnabled(true);
        jtQuota.setEnabled(true);
        jtQuotarid.setEnabled(true);
        fc.clear();
        jbNew.setEnabled(false);
        jbDelete.setEnabled(false);
        jbEdit.setEnabled(false);
        jbCancel.setEnabled(true);
        jbSave.setEnabled(false);
        jtAnnocomp.requestFocus();
        currentPeriod = null;
    }
    
    private void save() {
        try {
            fc.check();
        } catch (FieldException fe) {
            logger.debug("FE exception: {}",fe);
            return;
        }
        Periodo p;
        if (currentPeriod == null) {
            p = new Periodo();
        } else {
            p = currentPeriod;
        }
        p.setPERANNOCOMP(fc.getString(jtAnnocomp));
        p.setPERDESCR(fc.getString(jtDescr));
        p.setPERNOTE(fc.getString(jtaNote));
        p.setPERQUOTA(fc.getDecimal(jtQuota));
        p.setPERQUOTARID(fc.getDecimal(jtQuotarid));
        persist(p);
        ptm.fireTableDataChanged();
        jtaPer.setEnabled(true);
        jtAnnocomp.setEnabled(false);
        jtDescr.setEnabled(false);
        jtaNote.setEnabled(false);
        jtQuota.setEnabled(false);
        jtQuotarid.setEnabled(false);
        fc.clear();
        jbNew.setEnabled(true);
        jbDelete.setEnabled(false);
        jbEdit.setEnabled(false);
        jbCancel.setEnabled(false);
        jbSave.setEnabled(false);        
    }
    
    private void cancel() {
        jtaPer.setEnabled(true);
        jtAnnocomp.setEnabled(false);
        jtDescr.setEnabled(false);
        jtaNote.setEnabled(false);
        jtQuota.setEnabled(false);
        jtQuotarid.setEnabled(false);
        jbNew.setEnabled(true);
        jbDelete.setEnabled(false);
        jbEdit.setEnabled(false);
        jbCancel.setEnabled(false);
        jbSave.setEnabled(false);        
        changed = false;
    }    
    @Override
    public boolean isBusy() {
        return changed;
    }
        
    private void persist(Periodo p) {
        String sql;
        if (p.getPERID() != 0) {
            sql = "update PERIODO set PERANNOCOMP=?,PERDESCR=?,PERNOTE=?,PERQUOTA=?,PERQUOTARID=? where PERID=?";
        } else {
            sql = "insert into PERIODO (PERANNOCOMP,PERDESCR,PERNOTE,PERQUOTA,PERQUOTARID) values (?,?,?,?,?)";
        }
        logger.debug("Persisting: sql: {}",sql);
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, p.getPERANNOCOMP());
            ps.setString(2, p.getPERDESCR());
            ps.setString(3, p.getPERNOTE());
            ps.setBigDecimal(4, p.getPERQUOTA());
            ps.setBigDecimal(5, p.getPERQUOTARID());
            if (p.getPERID() != 0) {
                ps.setInt(6, p.getPERID());
            }
            ps.execute();
        } catch (SQLException e) {
            String err = "ERRORE SQL PERIODO: (" + sql + ") " + e.getLocalizedMessage();
            logger.error(err);
                JOptionPane.showMessageDialog(app.getMainFrame(),err,"ATTENZIONE!",JOptionPane.ERROR_MESSAGE);
        }
        jtAnnocomp.setEnabled(false);
        jtDescr.setEnabled(false);
        jtaNote.setEnabled(false);
        jtQuota.setEnabled(false);
        jtQuotarid.setEnabled(false);
        jbNew.setEnabled(true);
        jbDelete.setEnabled(false);
        jbEdit.setEnabled(false);
        jbCancel.setEnabled(false);
        jbSave.setEnabled(false);
        changed = false;
    }
    
    private void closeFrame() {
        if (jtaPer.isEditing()) {
            jtaPer.getCellEditor(jtaPer.getEditingRow(), jtaPer.getEditingColumn()).stopCellEditing();
        }
        if (changed) {
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
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jtaPer = new javax.swing.JTable();
        jbNew = new javax.swing.JButton();
        jbDelete = new javax.swing.JButton();
        jbEdit = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jtaNote = new javax.swing.JTextArea();
        jPanel3 = new javax.swing.JPanel();
        jbSave = new javax.swing.JButton();
        jbCancel = new javax.swing.JButton();
        jlMsg = new javax.swing.JLabel();
        jtAnnocomp = new javax.swing.JFormattedTextField();
        jtQuota = new javax.swing.JFormattedTextField();
        jtQuotarid = new javax.swing.JFormattedTextField();
        jtDescr = new javax.swing.JFormattedTextField();

        setClosable(true);
        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("Gestione Periodi");
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

        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel1.setLayout(new java.awt.GridBagLayout());

        jtaPer.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(jtaPer);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 80.0;
        gridBagConstraints.weighty = 100.0;
        jPanel1.add(jScrollPane1, gridBagConstraints);

        jbNew.setText("Nuovo");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 0, 5);
        jPanel1.add(jbNew, gridBagConstraints);

        jbDelete.setText("Elimina");
        jbDelete.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 0, 5);
        jPanel1.add(jbDelete, gridBagConstraints);

        jbEdit.setText("Modifica");
        jbEdit.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 0, 5);
        jPanel1.add(jbEdit, gridBagConstraints);

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        jLabel1.setText("anno competenza:");

        jLabel2.setText("descrizione:");

        jLabel3.setText("note:");

        jLabel4.setText("quota ridotta:");

        jLabel5.setText("quota intera:");

        jtaNote.setColumns(20);
        jtaNote.setRows(5);
        jScrollPane2.setViewportView(jtaNote);

        jbSave.setMnemonic('O');
        jbSave.setText("salva");
        jbSave.setEnabled(false);

        jbCancel.setMnemonic('C');
        jbCancel.setText("annulla");
        jbCancel.setEnabled(false);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jlMsg, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jbSave)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jbCancel))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jbSave)
                    .addComponent(jbCancel))
                .addContainerGap(13, Short.MAX_VALUE))
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addComponent(jlMsg, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 439, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jtAnnocomp, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jtQuotarid, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jtQuota, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jtDescr)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap(17, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jtAnnocomp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jtDescr, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(15, 15, 15)
                        .addComponent(jLabel3)))
                .addGap(9, 9, 9)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jtQuota, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jtQuotarid, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        getContentPane().add(jPanel2, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents
                    
    private void formInternalFrameClosing(javax.swing.event.InternalFrameEvent evt) {//GEN-FIRST:event_formInternalFrameClosing
        closeFrame();
    }//GEN-LAST:event_formInternalFrameClosing
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JButton jbCancel;
    private javax.swing.JButton jbDelete;
    private javax.swing.JButton jbEdit;
    private javax.swing.JButton jbNew;
    private javax.swing.JButton jbSave;
    private javax.swing.JLabel jlMsg;
    private javax.swing.JFormattedTextField jtAnnocomp;
    private javax.swing.JFormattedTextField jtDescr;
    private javax.swing.JFormattedTextField jtQuota;
    private javax.swing.JFormattedTextField jtQuotarid;
    private javax.swing.JTextArea jtaNote;
    private javax.swing.JTable jtaPer;
    // End of variables declaration//GEN-END:variables
    
}
