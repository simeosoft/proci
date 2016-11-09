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

import com.simeosoft.string.StringUtils;
import javax.swing.SwingWorker;
import java.awt.Cursor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.view.JRViewer;
import proci.App;
import proci.EDirectories;
import proci.gui.ProciInternalFrame;

/**
 *
 * @author  simeo
 */
public class Report3 extends java.awt.Dialog implements ProciInternalFrame {
    
    private String tipo = null;
    private PartecipantiMainTableModel dtm = null;
    private JTable table = null;
    private String evento = null;
    private String dataEvento = null;
    private String report_name = null;
    private final Map<String,Object> parameters = new HashMap<>();
    private JRViewer jw = null;
    ArrayList<Partecipante> aldati = new ArrayList<>();
    private boolean deveAggiornare = false;
    private boolean stampaSelezione = false;

    private final App app = App.getInstance();
    
    /**
     * Creates new form Report3 
     */
    public Report3(java.awt.Frame parent, boolean modal, String tipo,
            JTable table, String evento, String dataEvento) {
        super(parent, modal);
        initComponents();
        setName("TessereAttestati");
        app.loadPrefs(this);
        this.tipo = tipo;
        this.dtm = (PartecipantiMainTableModel) table.getModel();
        this.table = table;
        this.evento = evento;
        this.dataEvento = dataEvento;
        if (tipo.equals("T")) {
            setTitle("Stampa Tessere Riconoscimento");
            report_name = "RTesric.jasper";
        } else {
            setTitle("Stampa Attestati");
            report_name = "RAttestati.jasper";
        }
    }
    
    @Override
    public boolean isBusy() {
        return false;
    }    
    
    public boolean deveAggiornare() {
        return deveAggiornare;
    }
    public boolean stampaSoloSelezione() {
        return stampaSelezione;
    }
    
    private void eseguiReport() {
        jbEsegui.setEnabled(false);
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        String opString = "Data: " + StringUtils.formatDate(null,StringUtils.FORMAT_DD_MM_YYYY_HH_MM_SS);
        parameters.put("SOTTOTITOLO",opString);
        parameters.put("LOGO1",app.getAppPath(EDirectories.IMAGES_COMMON) + "logo1.png");
        parameters.put("LOGO2",app.getAppPath(EDirectories.IMAGES_COMMON) + "logo2.png");
        if (jrSelezione.isSelected()) {
            stampaSelezione = true;
            int[] arsel = table.getSelectedRows();
            for (int i=0; i<arsel.length; i++) {
                aldati.add(((PartecipantiMainTableModel) table.getModel()).getPartecipanteAt(arsel[i]));
            }
        } else {
            for (Partecipante par : ((PartecipantiMainTableModel) table.getModel()).getValues()) {
                if (tipo.equals("T")) {
                    if (! par.isTesseraStampata()) {
                        aldati.add(par);
                        deveAggiornare = true;
                    }
                } else {
                    if (! par.isAttestatoStampato()) {
                        aldati.add(par);
                        deveAggiornare = true;
                    }
                }
            }
        }
        
        final TaDataSource ds = new TaDataSource(aldati,evento,dataEvento);
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
                        report_name;
                JasperPrint jp = JasperFillManager.fillReport(reportFile,parameters, ds);
                if (jw != null) {
                    jpReport.removeAll();
                }
                jw = new JRViewer(jp);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        jpReport.add(jw);
                        jpReport.validate();
                        for (Partecipante par : aldati) {
                            if (tipo.equals("T")) {
                                par.setTesseraStampata(true);
                            } else {
                                par.setAttestatoStampato(true);
                            }
                        }
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
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup1 = new javax.swing.ButtonGroup();
        jpReport = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jbEsegui = new javax.swing.JButton();
        jrNormale = new javax.swing.JRadioButton();
        jrSelezione = new javax.swing.JRadioButton();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        jpReport.setLayout(new java.awt.BorderLayout());

        jpReport.setBorder(new javax.swing.border.BevelBorder(javax.swing.border.BevelBorder.LOWERED));
        add(jpReport, java.awt.BorderLayout.CENTER);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jbEsegui.setText("esegui");
        jbEsegui.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbEseguiActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 100.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        jPanel1.add(jbEsegui, gridBagConstraints);

        buttonGroup1.add(jrNormale);
        jrNormale.setSelected(true);
        jrNormale.setText("normale");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel1.add(jrNormale, gridBagConstraints);

        buttonGroup1.add(jrSelezione);
        jrSelezione.setText("selezione");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel1.add(jrSelezione, gridBagConstraints);

        add(jPanel1, java.awt.BorderLayout.NORTH);

        pack();
    }
    // </editor-fold>//GEN-END:initComponents

    private void jbEseguiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbEseguiActionPerformed
        eseguiReport();
    }//GEN-LAST:event_jbEseguiActionPerformed
    
    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        setVisible(false);
        app.savePrefs(this);
        dispose();
    }//GEN-LAST:event_closeDialog
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton jbEsegui;
    private javax.swing.JPanel jpReport;
    private javax.swing.JRadioButton jrNormale;
    private javax.swing.JRadioButton jrSelezione;
    // End of variables declaration//GEN-END:variables
    
}
