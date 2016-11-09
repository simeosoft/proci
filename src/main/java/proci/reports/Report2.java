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
import com.simeosoft.string.StringUtils;
import javax.swing.SwingWorker;
import java.awt.Cursor;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.view.JRViewer;
import proci.DBHandler;
import proci.ETipoSocio;
import proci.App;
import proci.EDirectories;
import proci.gui.ProciInternalFrame;

/**
 * Report2. Lancia un report generico.<br>
 * Tipi report:
 * <ul>
 * <li>"RO" = Rubrica Operativi (RRubricaOperativi.jrxml)
 * <li>"RS" = Rubrica Soci (RRubricaSoci.jrxml)
 * <li>"D" = Dotazioni (RDies.jrxml)
 * <li>"I" = Interventi (RDies.jrxml)
 * <li>"E" = Esercitazioni (RDies.jrxml)
 * <li>"S" = Specializzazioni (RDies.jrxml)
 * </ul>
 * @author  simeo
 */
public class Report2 extends javax.swing.JInternalFrame implements ProciInternalFrame {

    private Map<String,Object> parameters = new HashMap<String,Object>();
    private JRViewer jw = null;
    private String report_name = null;
    private String tipo = "";
    private Connection conn;    
    private JRDataSource ds = null;   
    private final App app = App.getInstance();    
    
    public Report2() {
        initComponents();
        try {
            conn = DBHandler.getInstance().getConnection();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(app.getMainFrame(),e,"ERRORE!",JOptionPane.ERROR_MESSAGE);
        }          
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
        if (tipo.equals("D")) {
            setTitle("Report Dotazioni");
            parameters.put("TITOLO","Report Dotazioni");
            report_name = "RDies.jasper";
        } else if (tipo.equals("I")) {
            setTitle("Report Interventi");
            parameters.put("TITOLO","Report Interventi");
            report_name = "RDies.jasper";
        } else if (tipo.equals("E")) {
            setTitle("Report Esercitazioni");
            parameters.put("TITOLO","Report Esercitazioni");
            report_name = "RDies.jasper";
        } else if (tipo.equals("S")) {
            setTitle("Report Specializzazioni");
            parameters.put("TITOLO","Report Specializzazioni");
            report_name = "RDies.jasper";
        } else if (tipo.equals("RO")) {
            setTitle("Rubrica Operativi");
            parameters.put("TITOLO","Rubrica Operativi");        
            report_name = "RRubricaOperativi.jasper";
        } else if (tipo.equals("RS")) {
            setTitle("Rubrica Soci");
            parameters.put("TITOLO","Rubrica Soci");        
            report_name = "RRubricaSoci.jasper";
        }
    }
    
    public boolean isBusy() {
        return false;
    }
    
    public void eseguiReport() {
        jbEsegui.setEnabled(false);
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        String opString = "Data: " + StringUtils.formatDate(null,StringUtils.FORMAT_DD_MM_YYYY_HH_MM_SS);
        parameters.put("SOTTOTITOLO",opString);
        parameters.put("LOGO1",app.getAppPath(EDirectories.IMAGES_COMMON) + "logo1.png");
        parameters.put("LOGO2",app.getAppPath(EDirectories.IMAGES_COMMON) + "logo2.png");
        String[] ssort  = {"SOCOGNOME","SONOME" };
        if (tipo.equals("RO")) {
            ds = new SociDataSource(conn,0,0,ETipoSocio.OPERATIVO,null,ssort);
        } else if (tipo.equals("RS")) {
            ds = new SociDataSource(conn,0,0,null,null,ssort);
        } else {
            ds = new DiesDataSource(conn,tipo);
        }
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
        jPanel1 = new javax.swing.JPanel();
        jbEsegui = new javax.swing.JButton();
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
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 100.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 10);
        jPanel1.add(jbEsegui, gridBagConstraints);

        getContentPane().add(jPanel1, java.awt.BorderLayout.NORTH);

        jpReport.setLayout(new java.awt.BorderLayout());

        jpReport.setBorder(new javax.swing.border.BevelBorder(javax.swing.border.BevelBorder.LOWERED));
        getContentPane().add(jpReport, java.awt.BorderLayout.CENTER);

        pack();
    }
    // </editor-fold>//GEN-END:initComponents

    private void jbEseguiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbEseguiActionPerformed
        eseguiReport();
    }//GEN-LAST:event_jbEseguiActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton jbEsegui;
    private javax.swing.JPanel jpReport;
    // End of variables declaration//GEN-END:variables
    
}
