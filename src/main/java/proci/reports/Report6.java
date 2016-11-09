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

package proci.reports;

import com.simeosoft.form.AddFieldException;
import com.simeosoft.form.FormController;
import com.simeosoft.form.IFormListener;
import com.simeosoft.string.StringUtils;
import javax.swing.SwingWorker;
import java.awt.Cursor;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.view.JRViewer;
import proci.DBHandler;
import proci.ETipoSocio;
import proci.App;
import proci.gui.ProciInternalFrame;
import proci.model.ComboSociModel;

/**
 *
 * @author  simeo
 */
public class Report6 extends javax.swing.JInternalFrame 
        implements ChangeListener, IFormListener, ProciInternalFrame {

    private Map<String,Object> parameters = new HashMap<String,Object>();
    private JRViewer jw = null;
    private String REPORT_NAME = "RIndirizzi.jasper";
    FormController fc;
    private Connection conn;
    private ComboSociModel cbm = null; 
    private final App app = App.getInstance();    
    
    public Report6() {
        initComponents();
        fc = new FormController(this,null);
        try {
            fc.addIntegerField(jtDa, "Da socio", 5, false, false);
            fc.addIntegerField(jtA, "A socio", 5, false, false);
        } catch (AddFieldException afe) {
        }
        jtDa.setEnabled(false);
        jtA.setEnabled(false);
        try {
            conn = DBHandler.getInstance().getConnection();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(app.getMainFrame(),e,"ERRORE!",JOptionPane.ERROR_MESSAGE);
        }          
        cbm = new ComboSociModel(conn, null);
        jcSocio.setModel(cbm);
        jcSocio.setEnabled(false);
        jrTutto.addChangeListener(this);
        jrRange.addChangeListener(this);
        jrCombo.addChangeListener(this);
    }
    
    public void stateChanged(ChangeEvent ce) {
        if (ce.getSource() == jrTutto) {
            jtDa.setEnabled(false);
            jtA.setEnabled(false);
            jcSocio.setEnabled(false);
        } else if (ce.getSource() == jrRange) {
            jtDa.setEnabled(true);
            jtDa.setCaretPosition(0);
            jtDa.requestFocus();
            jtA.setEnabled(true);
            jtA.setCaretPosition(0);
            jcSocio.setEnabled(false);
        } else {
            jcSocio.setEnabled(true);
            jtDa.setEnabled(false);
            jtA.setEnabled(false);
        }
    }
    
    public boolean isBusy() {
        return false;
    }
    
    public void eseguiReport() {
        jbEsegui.setEnabled(false);
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        String opString = "Data: " + StringUtils.formatDate(null,StringUtils.FORMAT_DD_MM_YYYY_HH_MM_SS);
        int start_socio = 0;
        int end_socio = Integer.MAX_VALUE;
        if (jrRange.isSelected()) {
            start_socio = fc.getInt(jtDa);
            end_socio = fc.getInt(jtA);
        } else if (jrTutto.isSelected()) {
        } else {
            // selezionato un socio
            int id = cbm.getIdSocioAt(jcSocio.getSelectedIndex());
            start_socio = id;
            end_socio = id;
        }
        String stipo = ((String) jcTipo.getSelectedItem()).substring(0,1);
        ETipoSocio tipo = ETipoSocio.OPERATIVO;
        if (stipo.equalsIgnoreCase("t")) {
            tipo = null;
        }
        final SociDataSource ds = new SociDataSource(conn,start_socio,end_socio,tipo,null,null);
        jbEsegui.setEnabled(false);
        SwingWorker worker = new SwingWorker() {
            @Override
            protected Object doInBackground() throws Exception {
                return new EseguiReportTask(ds);
            }
        };
        worker.run();
    }
  
    private class EseguiReportTask {
        public EseguiReportTask(JRDataSource ds) {
            try {
                String reportFile = app.getAppPath() + System.getProperty("file.separator") + 
                        "reports" + System.getProperty("file.separator") + 
                        REPORT_NAME;
                JasperPrint jp = JasperFillManager.fillReport(reportFile,parameters, ds);
                if (jw != null) {
                    jpReport.removeAll();
                }
                jw = new JRViewer(jp);
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        jpReport.add(jw);
                        jpReport.validate();
                        jbEsegui.setEnabled(true);
                        setCursor(Cursor.getDefaultCursor());
                    }
                });
            } catch (JRException jre) {
                setCursor(Cursor.getDefaultCursor());
                JOptionPane.showMessageDialog(app.getMainFrame(),"<html>Errore esecuzione report: <b>" + jre + "</b>","ERRORE",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    public void changed(JComponent jc) {
    }
            
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jbEsegui = new javax.swing.JButton();
        jrTutto = new javax.swing.JRadioButton();
        jrRange = new javax.swing.JRadioButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jtA = new javax.swing.JFormattedTextField();
        jtDa = new javax.swing.JFormattedTextField();
        jcSocio = new javax.swing.JComboBox();
        jrCombo = new javax.swing.JRadioButton();
        jcTipo = new javax.swing.JComboBox();
        jpReport = new javax.swing.JPanel();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("Indirizzi");
        jPanel1.setLayout(new java.awt.GridBagLayout());

        jbEsegui.setText("esegui");
        jbEsegui.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbEseguiActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 100.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        jPanel1.add(jbEsegui, gridBagConstraints);

        buttonGroup1.add(jrTutto);
        jrTutto.setSelected(true);
        jrTutto.setText("tutto");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel1.add(jrTutto, gridBagConstraints);

        buttonGroup1.add(jrRange);
        jrRange.setText("soci");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel1.add(jrRange, gridBagConstraints);

        jLabel1.setText("a");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        jPanel1.add(jLabel1, gridBagConstraints);

        jLabel2.setText("da");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        jPanel1.add(jLabel2, gridBagConstraints);

        jtA.setMaximumSize(new java.awt.Dimension(40, 19));
        jtA.setMinimumSize(new java.awt.Dimension(40, 19));
        jtA.setPreferredSize(new java.awt.Dimension(40, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 8);
        jPanel1.add(jtA, gridBagConstraints);

        jtDa.setMaximumSize(new java.awt.Dimension(40, 19));
        jtDa.setMinimumSize(new java.awt.Dimension(40, 19));
        jtDa.setPreferredSize(new java.awt.Dimension(40, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 8);
        jPanel1.add(jtDa, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 5, 2, 0);
        jPanel1.add(jcSocio, gridBagConstraints);

        buttonGroup1.add(jrCombo);
        jrCombo.setText("socio");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel1.add(jrCombo, gridBagConstraints);

        jcTipo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "tutti i soci", "solo soci operativi" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 30, 0, 10);
        jPanel1.add(jcTipo, gridBagConstraints);

        getContentPane().add(jPanel1, java.awt.BorderLayout.NORTH);

        jpReport.setLayout(new java.awt.BorderLayout());

        jpReport.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        getContentPane().add(jpReport, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jbEseguiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbEseguiActionPerformed
        eseguiReport();
    }//GEN-LAST:event_jbEseguiActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton jbEsegui;
    private javax.swing.JComboBox jcSocio;
    private javax.swing.JComboBox jcTipo;
    private javax.swing.JPanel jpReport;
    private javax.swing.JRadioButton jrCombo;
    private javax.swing.JRadioButton jrRange;
    private javax.swing.JRadioButton jrTutto;
    private javax.swing.JFormattedTextField jtA;
    private javax.swing.JFormattedTextField jtDa;
    // End of variables declaration//GEN-END:variables
    
}
