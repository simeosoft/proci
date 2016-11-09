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

import proci.gui.render.ProciStringRenderer;
import proci.gui.render.ProciIntegerRenderer;
import java.awt.AWTEvent;
import java.awt.EventQueue;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelListener;
import proci.model.Dotazione;
import proci.model.DotazioniTableModel;
import proci.*;

/**
 *
 * @author  simeo
 */
public class GestioneDotazioni extends JInternalFrame
        implements TableModelListener,ProciInternalFrame {
    
    private boolean changed = false;
    private DotazioniTableModel dtm = null;
    private ArrayList<Dotazione> al;
    private Connection conn;
    private Statement stmt;
    private PreparedStatement ps;
    private ResultSet rs;    
    private final App app = App.getInstance();  
    
    /** Creates new form GestioneDotazioni */
    public GestioneDotazioni() {
        initComponents();
        al = new ArrayList<Dotazione>();
        try {
            conn = DBHandler.getInstance().getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("select * from DOTAZIONE order by DOTID");
            while (rs.next()) {
                Dotazione dot = new Dotazione(rs.getInt("DOTID"),rs.getString("DOTDESCR"), rs.getString("DOTTAGLIA"));
                al.add(dot);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(app.getMainFrame(),e,"ERRORE!",JOptionPane.ERROR_MESSAGE);
        } 
        dtm = new DotazioniTableModel(al,true);
        jtaDot = new JTable(dtm) {
            private final KeyStroke tabKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
            public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend) {
                AWTEvent currentEvent = EventQueue.getCurrentEvent();
                if(currentEvent instanceof KeyEvent){
                    KeyEvent ke = (KeyEvent)currentEvent;
                    if(ke.getSource()!=this) {
                        return;
                    }
                    if(rowIndex==0 && columnIndex==0 && KeyStroke.getKeyStrokeForEvent(ke).equals(tabKeyStroke)) {
                        dtm.addItem();
                        rowIndex = getRowCount()-1;
                    }
                }
                super.changeSelection(rowIndex, columnIndex == 0 ? 1 : columnIndex, toggle, extend);
            }
        };
        jtaDot.setDefaultRenderer(String.class, new ProciStringRenderer());
        jtaDot.setDefaultRenderer(Integer.class, new ProciIntegerRenderer());
        jtaDot.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(jtaDot);
        dtm.addTableModelListener(this);
        if (dtm.getRowCount() > 0) {
            jtaDot.changeSelection(0, 1, false, false);
        }
    }
    
    public boolean isBusy() {
        return changed;
    }
        
    public void newActionPerformed() {
        if (jtaDot.isEditing()) {
            jtaDot.getCellEditor(jtaDot.getEditingRow(), jtaDot.getEditingColumn()).stopCellEditing();
        }
        dtm.addItem();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                jtaDot.changeSelection(dtm.getRowCount() - 1, 2, false, false);
            }
        });
    }
    
    private void deleteActionPerformed() {
        if (jtaDot.isEditing()) {
            jtaDot.getCellEditor(jtaDot.getEditingRow(), jtaDot.getEditingColumn()).stopCellEditing();
        }
        if (jtaDot.getSelectedRow() != -1) {
            // controllo se la dotazione e' utilizzata
            int id = ((Integer) dtm.getValueAt(jtaDot.getSelectedRow(), 0)).intValue();
            String descr = (String) dtm.getValueAt(jtaDot.getSelectedRow(), 1) +
                    (String) dtm.getValueAt(jtaDot.getSelectedRow(), 2);
            ArrayList<String> alSoci = new ArrayList<String>();
            try {
                stmt = conn.createStatement();
                rs = stmt.executeQuery("select SOCOGNOME,SONOME from SODO join SOCIO on (SODOSO = SOID) where SODODO = " + id);
                while (rs.next()) {
                    alSoci.add(rs.getString("SOCOGNOME") + " " + rs.getString("SONOME"));
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(app.getMainFrame(),e,"ERRORE!",JOptionPane.ERROR_MESSAGE);
            }
            if (alSoci.size() > 0) {
                String msg = "<html>";
                msg += "La dotazione: <font color='blue'>" + descr + "</font> non può essere eliminata<br>" +
                        "in quanto è assegnata ai seguenti soci:<br>";
                for (String astring : alSoci) {
                    msg += "<font color='blue'>" + astring + "</font><br>";
                }
                JOptionPane.showMessageDialog(app.getMainFrame(),msg,
                        "ATTENZIONE!",JOptionPane.ERROR_MESSAGE);
                return;
            }
            dtm.deleteItem(jtaDot.getSelectedRow());
        }
    }
    
    public void tableChanged(javax.swing.event.TableModelEvent e) {
        changed = true;
    }
    
    private void okActionPerformed() {
        if (jtaDot.isEditing()) {
            jtaDot.getCellEditor(jtaDot.getEditingRow(), jtaDot.getEditingColumn()).stopCellEditing();
        }
        if (! changed) {
            dispose();
            return;
        }
        jbOk.setEnabled(false);
        jbCancel.setEnabled(false);
        try {
            conn.setAutoCommit(false);
            stmt = conn.createStatement();
            stmt.execute("delete from DOTAZIONE");
            ps = conn.prepareStatement("insert into DOTAZIONE values (?,?,?)");
            for (Dotazione dot : al) {
                ps.setInt(1, dot.getDOTID());
                ps.setString(2, dot.getDOTDESCR());
                ps.setString(3, dot.getDOTTAGLIA());
                ps.execute();
            }
            conn.commit();
            conn.close();
            this.dispose();
            return;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(app.getMainFrame(),e,"ERRORE!",JOptionPane.ERROR_MESSAGE);
        }
        try {
            conn.rollback();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(app.getMainFrame(),e,"ERRORE!",JOptionPane.ERROR_MESSAGE);
        }
        this.dispose();
    }
    
    private void closeFrame() {
        if (jtaDot.isEditing()) {
            jtaDot.getCellEditor(jtaDot.getEditingRow(), jtaDot.getEditingColumn()).stopCellEditing();
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
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jtaDot = new javax.swing.JTable();
        jbNew = new javax.swing.JButton();
        jbDelete = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jbOk = new javax.swing.JButton();
        jbCancel = new javax.swing.JButton();

        setClosable(true);
        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("Gestione Dotazioni");
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

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jPanel1.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0)));
        jtaDot.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(jtaDot);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 80.0;
        gridBagConstraints.weighty = 100.0;
        jPanel1.add(jScrollPane1, gridBagConstraints);

        jbNew.setText("Nuovo");
        jbNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbNewActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 0, 5);
        jPanel1.add(jbNew, gridBagConstraints);

        jbDelete.setText("Elimina");
        jbDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbDeleteActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 0, 5);
        jPanel1.add(jbDelete, gridBagConstraints);

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        jbOk.setMnemonic('O');
        jbOk.setText("ok");
        jbOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbOkActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 33.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 40, 5, 40);
        jPanel2.add(jbOk, gridBagConstraints);

        jbCancel.setMnemonic('C');
        jbCancel.setText("annulla");
        jbCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbCancelActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 33.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 40, 5, 40);
        jPanel2.add(jbCancel, gridBagConstraints);

        getContentPane().add(jPanel2, java.awt.BorderLayout.SOUTH);

        pack();
    }
    // </editor-fold>//GEN-END:initComponents
    
    private void jbCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbCancelActionPerformed
        closeFrame();
    }//GEN-LAST:event_jbCancelActionPerformed
    
    private void jbOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbOkActionPerformed
        okActionPerformed();
    }//GEN-LAST:event_jbOkActionPerformed
    
    private void jbDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbDeleteActionPerformed
        deleteActionPerformed();
    }//GEN-LAST:event_jbDeleteActionPerformed
    
    private void jbNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbNewActionPerformed
        newActionPerformed();
    }//GEN-LAST:event_jbNewActionPerformed
    
    private void formInternalFrameClosing(javax.swing.event.InternalFrameEvent evt) {//GEN-FIRST:event_formInternalFrameClosing
        closeFrame();
    }//GEN-LAST:event_formInternalFrameClosing
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton jbCancel;
    private javax.swing.JButton jbDelete;
    private javax.swing.JButton jbNew;
    private javax.swing.JButton jbOk;
    private javax.swing.JTable jtaDot;
    // End of variables declaration//GEN-END:variables
    
}
