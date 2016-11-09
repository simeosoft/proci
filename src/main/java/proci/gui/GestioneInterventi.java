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
import proci.*;
import proci.model.InterventiTableModel;
import proci.model.Intervento;

/**
 *
 * @author  simeo
 */
public class GestioneInterventi extends JInternalFrame
                                 implements TableModelListener,ProciInternalFrame {
    
    private boolean changed = false;
    private InterventiTableModel dtm = null;
    private ArrayList<Intervento> al;
    private Connection conn;
    private Statement stmt;
    private PreparedStatement ps;
    private ResultSet rs;
    private final App app = App.getInstance();      
    
    /** Creates new form GestioneInterventi */
    public GestioneInterventi() {
        initComponents();
        al = new ArrayList<>();
        try {
            conn = DBHandler.getInstance().getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("select * from INTERVENTO order by INTID");
            while (rs.next()) {
                Intervento inte = new Intervento(rs.getInt("INTID"),rs.getString("INTDESCR"));
                al.add(inte);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(app.getMainFrame(),e,"ERRORE!",JOptionPane.ERROR_MESSAGE);
        }      
        dtm = new InterventiTableModel(al,true);
        jtaInt = new JTable(dtm) {
            private static final long serialVersionUID = -673980102687745913L;
            private final KeyStroke tabKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
            @Override
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
        jtaInt.setDefaultRenderer(String.class, new ProciStringRenderer());
        jtaInt.setDefaultRenderer(Integer.class, new ProciIntegerRenderer());
        jtaInt.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(jtaInt);
        dtm.addTableModelListener(this);
        if (dtm.getRowCount() > 0) {
            jtaInt.changeSelection(0, 1, false, false);
        }        
    }
    
    @Override
    public boolean isBusy() {
        return changed;
    }
    
    public void newActionPerformed() {
        if (jtaInt.isEditing()) {
            jtaInt.getCellEditor(jtaInt.getEditingRow(), jtaInt.getEditingColumn()).stopCellEditing();        
        }
        dtm.addItem();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                jtaInt.changeSelection(dtm.getRowCount() - 1, 2, false, false);
            }
        });
    }
    
    private void deleteActionPerformed() {
        if (jtaInt.isEditing()) {
            jtaInt.getCellEditor(jtaInt.getEditingRow(), jtaInt.getEditingColumn()).stopCellEditing();        
        }
        if (jtaInt.getSelectedRow() != -1) {
            // controllo se l'intervento e' utilizzato
            int id = ((Integer) dtm.getValueAt(jtaInt.getSelectedRow(), 0));
            String descr = (String) dtm.getValueAt(jtaInt.getSelectedRow(), 1);
            ArrayList<String> alSoci = new ArrayList<>();
            try {
                stmt = conn.createStatement();
                rs = stmt.executeQuery("select SOCOGNOME,SONOME from SOIN join SOCIO on (SOINSO = SOID) where SOININ = " + id);
                while (rs.next()) {
                    alSoci.add(rs.getString("SOCOGNOME") + " " + rs.getString("SONOME"));
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(app.getMainFrame(),e,"ERRORE!",JOptionPane.ERROR_MESSAGE);
            }
            if (alSoci.size() > 0) {
                String msg = "<html>";
                msg += "L'intervento: <font color='blue'>" + descr + "</font> non può essere eliminato<br>" +
                        "in quanto è assegnato ai seguenti soci:<br>";
                for (String astring : alSoci) {
                    msg += "<font color='blue'>" + astring + "</font><br>";
                }
                JOptionPane.showMessageDialog(app.getMainFrame(),msg,
                        "ATTENZIONE!",JOptionPane.ERROR_MESSAGE);
                return;
            }
            dtm.deleteItem(jtaInt.getSelectedRow());
        }
    }    
            
    @Override
    public void tableChanged(javax.swing.event.TableModelEvent e) {
        changed = true;
    }   
    
    private void okActionPerformed() {
        if (jtaInt.isEditing()) {
            jtaInt.getCellEditor(jtaInt.getEditingRow(), jtaInt.getEditingColumn()).stopCellEditing();        
        }
        if (! changed) {
            this.dispose();
            return;
        }
        jbOk.setEnabled(false);
        jbCancel.setEnabled(false);
        try {
            conn.setAutoCommit(false);
            stmt = conn.createStatement();
            stmt.execute("delete from INTERVENTO");
            ps = conn.prepareStatement("insert into INTERVENTO values (?,?)");
            for (Intervento inte : al) {
                ps.setInt(1, inte.getINTID());
                ps.setString(2, inte.getINTDESCR());
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
        if (jtaInt.isEditing()) {
            jtaInt.getCellEditor(jtaInt.getEditingRow(), jtaInt.getEditingColumn()).stopCellEditing();        
        }
        if (changed) {
            if (JOptionPane.showConfirmDialog(this,"Sono state fatte delle modifiche! Vuoi uscire veramente??",
                    "Conferma",JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
                return;
            }
        }
        try {
            conn.close();
        } catch (SQLException e) {}
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
        jtaInt = new javax.swing.JTable();
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
        setTitle("Gestione Interventi");
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
        jtaInt.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(jtaInt);

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
    private javax.swing.JTable jtaInt;
    // End of variables declaration//GEN-END:variables
    
}
