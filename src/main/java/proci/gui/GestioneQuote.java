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
import com.simeosoft.form.IFormListener;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelListener;
import proci.*;
import proci.model.ComboPeriodiModel;
import proci.model.SocioQuotaTableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import proci.gui.editor.ProciDecimalEditor;
import proci.model.Periodo;

/**
 *
 * @author  simeo
 */
public class GestioneQuote extends JInternalFrame
        implements TableModelListener,ProciInternalFrame,IFormListener {
    static final Logger logger = LoggerFactory.getLogger(GestioneQuote.class);        
    private static final long serialVersionUID = 500070106665822843L;
    
    private boolean changed = false;
    private SocioQuotaTableModel qtm = null;
    private Connection conn;
    private Statement stmt;
    private PreparedStatement ps;
    private ResultSet rs;    
    private final App app = App.getInstance();  
    private JFormattedTextField jtf = new JFormattedTextField();
    
    /** Creates new form GestioneDotazioni */
    public GestioneQuote() {
        initComponents();
        try {
            conn = DBHandler.getInstance().getConnection();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(app.getMainFrame(),e,"ERRORE!",JOptionPane.ERROR_MESSAGE);
        }         
        jtaQuot.setDefaultRenderer(String.class, new ProciStringRenderer());
        jtaQuot.setDefaultRenderer(Integer.class, new ProciIntegerRenderer());
        jtaQuot.setDefaultRenderer(BigDecimal.class, new ProciBigDecimalRenderer(2));
        jtaQuot.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        qtm = new SocioQuotaTableModel(conn, 0);        
        jtaQuot.setModel(qtm);
        jScrollPane1.setViewportView(jtaQuot);
        jcbPeriodo.setModel(new ComboPeriodiModel(conn));
        jcbPeriodo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadTable();
            }
        });
    }
    
    private void loadTable() {
        Periodo p = (Periodo) jcbPeriodo.getModel().getSelectedItem();
        logger.debug("Periodo: {}",p);
        qtm = new SocioQuotaTableModel(conn, p.getPERID());
        jtaQuot.setModel(qtm);
        jtaQuot.getColumnModel().getColumn(3).setCellEditor(new ProciDecimalEditor(new JFormattedTextField()));        
        this.validate();
    }

    @Override
    public void changed(JComponent jc) {
        logger.debug("CHANGED: {}",jc);
    }
        
    @Override
    public boolean isBusy() {
        return changed;
    }
        
//    public void newActionPerformed() {
//        if (jtaDot.isEditing()) {
//            jtaDot.getCellEditor(jtaDot.getEditingRow(), jtaDot.getEditingColumn()).stopCellEditing();
//        }
//        dtm.addItem();
//        SwingUtilities.invokeLater(new Runnable() {
//            public void run() {
//                jtaDot.changeSelection(dtm.getRowCount() - 1, 2, false, false);
//            }
//        });
//    }
//    
//    private void deleteActionPerformed() {
//        if (jtaDot.isEditing()) {
//            jtaDot.getCellEditor(jtaDot.getEditingRow(), jtaDot.getEditingColumn()).stopCellEditing();
//        }
//        if (jtaDot.getSelectedRow() != -1) {
//            // controllo se la dotazione e' utilizzata
//            int id = ((Integer) dtm.getValueAt(jtaDot.getSelectedRow(), 0)).intValue();
//            String descr = (String) dtm.getValueAt(jtaDot.getSelectedRow(), 1) +
//                    (String) dtm.getValueAt(jtaDot.getSelectedRow(), 2);
//            ArrayList<String> alSoci = new ArrayList<String>();
//            try {
//                stmt = conn.createStatement();
//                rs = stmt.executeQuery("select SOCOGNOME,SONOME from SODO join SOCIO on (SODOSO = SOID) where SODODO = " + id);
//                while (rs.next()) {
//                    alSoci.add(rs.getString("SOCOGNOME") + " " + rs.getString("SONOME"));
//                }
//            } catch (SQLException e) {
//                JOptionPane.showMessageDialog(app.getMainFrame(),e,"ERRORE!",JOptionPane.ERROR_MESSAGE);
//            }
//            if (alSoci.size() > 0) {
//                String msg = "<html>";
//                msg += "La dotazione: <font color='blue'>" + descr + "</font> non può essere eliminata<br>" +
//                        "in quanto è assegnata ai seguenti soci:<br>";
//                for (String astring : alSoci) {
//                    msg += "<font color='blue'>" + astring + "</font><br>";
//                }
//                JOptionPane.showMessageDialog(app.getMainFrame(),msg,
//                        "ATTENZIONE!",JOptionPane.ERROR_MESSAGE);
//                return;
//            }
//            dtm.deleteItem(jtaDot.getSelectedRow());
//        }
//    }
    
    @Override
    public void tableChanged(javax.swing.event.TableModelEvent e) {
        changed = true;
    }
    
//    private void okActionPerformed() {
//        if (jtaDot.isEditing()) {
//            jtaDot.getCellEditor(jtaDot.getEditingRow(), jtaDot.getEditingColumn()).stopCellEditing();
//        }
//        if (! changed) {
//            dispose();
//            return;
//        }
//        jbOk.setEnabled(false);
//        jbCancel.setEnabled(false);
//        try {
//            conn.setAutoCommit(false);
//            stmt = conn.createStatement();
//            stmt.execute("delete from DOTAZIONE");
//            ps = conn.prepareStatement("insert into DOTAZIONE values (?,?,?)");
//            for (Dotazione dot : al) {
//                ps.setInt(1, dot.getDOTID());
//                ps.setString(2, dot.getDOTDESCR());
//                ps.setString(3, dot.getDOTTAGLIA());
//                ps.execute();
//            }
//            conn.commit();
//            conn.close();
//            this.dispose();
//            return;
//        } catch (SQLException e) {
//            JOptionPane.showMessageDialog(app.getMainFrame(),e,"ERRORE!",JOptionPane.ERROR_MESSAGE);
//        }
//        try {
//            conn.rollback();
//        } catch (SQLException e) {
//            JOptionPane.showMessageDialog(app.getMainFrame(),e,"ERRORE!",JOptionPane.ERROR_MESSAGE);
//        }
//        this.dispose();
//    }
    
    private void closeFrame() {
        if (jtaQuot.isEditing()) {
            jtaQuot.getCellEditor(jtaQuot.getEditingRow(), jtaQuot.getEditingColumn()).stopCellEditing();
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
        jtaQuot = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jcbPeriodo = new javax.swing.JComboBox();
        jPanel3 = new javax.swing.JPanel();
        jlMessage = new javax.swing.JLabel();

        setClosable(true);
        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("Gestione Quote");
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

        jtaQuot.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(jtaQuot);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 80.0;
        gridBagConstraints.weighty = 100.0;
        jPanel1.add(jScrollPane1, gridBagConstraints);

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        jLabel1.setText("periodo:");

        jcbPeriodo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jcbPeriodo, 0, 260, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jcbPeriodo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        getContentPane().add(jPanel2, java.awt.BorderLayout.NORTH);

        jlMessage.setText("jLabel2");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jlMessage, javax.swing.GroupLayout.DEFAULT_SIZE, 389, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jlMessage)
        );

        getContentPane().add(jPanel3, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents
                    
    private void formInternalFrameClosing(javax.swing.event.InternalFrameEvent evt) {//GEN-FIRST:event_formInternalFrameClosing
        closeFrame();
    }//GEN-LAST:event_formInternalFrameClosing
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JComboBox jcbPeriodo;
    private javax.swing.JLabel jlMessage;
    private javax.swing.JTable jtaQuot;
    // End of variables declaration//GEN-END:variables

}
