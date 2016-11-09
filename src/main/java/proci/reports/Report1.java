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
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
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
import proci.ImagePanel;
import proci.App;
import proci.EDirectories;
import proci.gui.ProciInternalFrame;
import proci.model.ComboSociModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Report1. Lancia un report con possibilita' di selezionare
 * Tutti i soci, un range di soci o un socio particolare.<br>
 * Usa SociDataSource come data source per Jasper<br>
 * Tipi report:
 * <ul>
 * <li>"S" = schede soci (RSchedeSoci.jrxml)
 * <li>"B" = badge soci operativi (RBadge.jrxml)
 * <li>"T" = tessere soci (RTessere.jrxml)
 * </ul>
 * @author  simeo
 */
public class Report1 extends javax.swing.JInternalFrame implements ChangeListener, IFormListener,
                                                               ProciInternalFrame {

    static final Logger logger = LoggerFactory.getLogger(Report1.class);    

    private Map<String,Object> parameters = new HashMap<>();
    private JRViewer jw = null;
    private FormController fc;
    private Connection conn;
    private ComboSociModel cbm = null;
    //
    private String tipo_report;
    private String report_name = null;
    private String image_name = null;    
    private ETipoSocio tipo_socio = null;    
    private final App app = App.getInstance();  
    
    public Report1() {
        initComponents();
        jlImmagine.setText("");
        fc = new FormController(this,null);
        try {
            fc.addIntegerField(jtDa, "Da socio", 5, false, false);
            fc.addIntegerField(jtA, "A socio", 5, false, false);
        } catch (AddFieldException afe) {
        }
        try {
            conn = DBHandler.getInstance().getConnection();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(app.getMainFrame(),e,"ERRORE!",JOptionPane.ERROR_MESSAGE);
        }             
        jtDa.setEnabled(false);
        jtA.setEnabled(false);
        jcSocio.setEnabled(false);
        jrTutto.addChangeListener(this);
        jrRange.addChangeListener(this);
        jrCombo.addChangeListener(this);
    }
    
    public void setTipo(String tipo) {
        this.tipo_report = tipo;
        if (tipo.equals("S")) {
            report_name = "RSchedeSoci.jasper";
            setTitle("Schede Soci");
            parameters.put("TITOLO","Schede Soci");
        } else if (tipo.equals("B")) {
            report_name = "RBadge.jasper";            
            image_name = "badge.png";
            settaIcona();
            tipo_socio = ETipoSocio.OPERATIVO;
            setTitle("Badge");
            parameters.put("TITOLO","Badge");
        } else if (tipo.equals("T")) {
            report_name = "RTessere.jasper";
            image_name = "tessera.png";
            settaIcona();
            setTitle("Tessere Soci");
            parameters.put("TITOLO","Tessere Soci");
        }
        cbm = new ComboSociModel(conn, tipo_socio);
        jcSocio.setModel(cbm);
    }

    private void settaIcona() {
        try {
            BufferedImage bi = ImageIO.read(new File(app.getAppPath(EDirectories.IMAGES) + image_name));
            jlImmagine.setIcon(new ImageIcon(bi.getScaledInstance(100, -1, bi.SCALE_FAST)));
        } catch (IOException ioe) {
            logger.warn("Errore creazione icona: {}",ioe);
        }
        jlImmagine.setToolTipText(String.format("<html><body>Immagine: <b>%s</b><br>Clicca per ingrandire",
                app.getAppPath(EDirectories.IMAGES) + image_name));
    }
    
    private void showImage() {
        if (image_name != null) {
            ImagePanel ip = new ImagePanel(app.getMainFrame(),true,"/" + EDirectories.IMAGES.getDir() + "/" + image_name);
            ip.setVisible(true);
        }
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
            opString += " - Da socio: " + start_socio + " a socio: " + end_socio;
        } else if (jrTutto.isSelected()) {
            opString += " - Tutti i soci";
        } else {
            // selezionato un socio
            int id = cbm.getIdSocioAt(jcSocio.getSelectedIndex());
            start_socio = id;
            end_socio = id;
            opString += " - Socio: " + cbm.getElementAt(jcSocio.getSelectedIndex());
        }
        parameters.put("SOTTOTITOLO",opString); 
        parameters.put("LOGO1",app.getAppPath(EDirectories.IMAGES_COMMON) + "logo1.png");
        parameters.put("LOGO2",app.getAppPath(EDirectories.IMAGES_COMMON) + "logo2.png");
        String[] ssort  = {"SOCOGNOME","SONOME" };
        final SociDataSource ds = new SociDataSource(conn,start_socio,end_socio, tipo_socio,image_name, ssort);
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
                    public void run() {
                        jpReport.add(jw);
                        jpReport.validate();
                        jbEsegui.setEnabled(true);
                        setCursor(Cursor.getDefaultCursor());
                    }
                });
            } catch (JRException jre) {
                setCursor(Cursor.getDefaultCursor());
                JOptionPane.showMessageDialog(app.getMainFrame(),"<html>Errore esecuzione report: <br><b>" + jre + "</b>","ERRORE",
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
        jlImmagine = new javax.swing.JLabel();
        jpReport = new javax.swing.JPanel();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
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
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
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

        jlImmagine.setText("jlImmagine");
        jlImmagine.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jlImmagineMouseClicked(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 50.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 0);
        jPanel1.add(jlImmagine, gridBagConstraints);

        getContentPane().add(jPanel1, java.awt.BorderLayout.NORTH);

        jpReport.setLayout(new java.awt.BorderLayout());

        jpReport.setBorder(new javax.swing.border.BevelBorder(javax.swing.border.BevelBorder.LOWERED));
        getContentPane().add(jpReport, java.awt.BorderLayout.CENTER);

        pack();
    }
    // </editor-fold>//GEN-END:initComponents

    private void jlImmagineMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jlImmagineMouseClicked
        showImage();
    }//GEN-LAST:event_jlImmagineMouseClicked

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
    private javax.swing.JLabel jlImmagine;
    private javax.swing.JPanel jpReport;
    private javax.swing.JRadioButton jrCombo;
    private javax.swing.JRadioButton jrRange;
    private javax.swing.JRadioButton jrTutto;
    private javax.swing.JFormattedTextField jtA;
    private javax.swing.JFormattedTextField jtDa;
    // End of variables declaration//GEN-END:variables
    
}
