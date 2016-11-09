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

import com.simeosoft.swing.SaveFileChooser;
import com.simeosoft.util.XlsUtils;
import java.awt.Dimension;
import java.awt.Point;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import proci.DBHandler;
import proci.App;
import proci.EDirectories;
import proci.util.ProciUtils;

/**
 *
 * @author  simeo
 */
public class Esporta extends java.awt.Dialog {
    
    final int ESPO_WIDTH = 400;
    final int ESPO_HEIGHT = 400;       

    private final App app = App.getInstance();    
    
    public enum EspType {
        XLS ("File Excel (.xls)"),
        CSV ("File testo separato da virgola (.csv)");
        private String ext;
        EspType(String ext) {
            this.ext = ext;
        }
        public String getExt() {
            return ext;
        }
    };
    
    EspType et = null;
    String filename = "";
    Connection conn;
    Statement stmt;
    ResultSet rs;
    
    /** Creates new form Esporta */
    public Esporta(java.awt.Frame parent, boolean modal,EspType et) {
        super(parent, modal);
        initComponents();
        this.et = et;
        Dimension frmSize = parent.getSize();
        Point loc = parent.getLocation();
        this.setSize(ESPO_WIDTH,ESPO_HEIGHT);
        this.setLocation((frmSize.width - ESPO_WIDTH) / 2 + loc.x, 
                        (frmSize.height - ESPO_HEIGHT) / 2 + loc.y);
        jepData.setEditable(false);
        jepData.setContentType("text/html");
        String text = "<html><body><font face='verdana,arial' size=+1><p align='center'>Esportazione " +
                et.toString() + "</p></font>";
        jepData.setText(text);
    }
    
    private void okActionPerformed() {
        SaveFileChooser cfc = null;
        if (et == EspType.XLS) {
            cfc = new SaveFileChooser(app.getAppPath(), ".xls", "File XLS (.xls)");
        } else {
            cfc = new SaveFileChooser(app.getAppPath(), ".csv", "File CSV (.csv)");
        }
        int rc = cfc.showSaveDialog(this);
        if (rc == JFileChooser.APPROVE_OPTION) {
            filename = cfc.getSelectedFile().getPath();
            doExport();
        }
        String text = "<html><body><font face='verdana,arial' size=+1><p align='center'>Esportazione conclusa!" +
                "</p></font>";
        jepData.setText(text);
    }
    
    private void doExport() {
        ArrayList<ArrayList<Object>> soci_values = new ArrayList<ArrayList<Object>>();        
        ArrayList<ArrayList<Object>> dotazioni_values = new ArrayList<ArrayList<Object>>();        
        ArrayList<ArrayList<Object>> interventi_values = new ArrayList<ArrayList<Object>>();        
        ArrayList<ArrayList<Object>> esercitazioni_values = new ArrayList<ArrayList<Object>>();        
        ArrayList<ArrayList<Object>> specializzazioni_values = new ArrayList<ArrayList<Object>>();
        ArrayList<ArrayList<Object>> dotsocio_values = new ArrayList<ArrayList<Object>>();
        ArrayList<ArrayList<Object>> intsocio_values = new ArrayList<ArrayList<Object>>();
        ArrayList<ArrayList<Object>> esesocio_values = new ArrayList<ArrayList<Object>>();
        ArrayList<ArrayList<Object>> spesocio_values = new ArrayList<ArrayList<Object>>();
        // intestazione soci
        ArrayList<Object> soci_int = new ArrayList<Object>(ProciUtils.socio_intest.length);
        for (String astring : ProciUtils.socio_intest) {
            soci_int.add(astring);
        }
        soci_values.add(soci_int);                    
        try {
            conn = DBHandler.getInstance().getConnection();
            stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
            rs = stmt.executeQuery("select * from SOCIO");
            while (rs.next()) {
                ArrayList<Object> al = new ArrayList<Object>(ProciUtils.socio_properties.length);                
                for (String astring : ProciUtils.socio_properties) {
                    Object obj = rs.getObject(astring);
                    if (astring.equals("SOTIPO")) {
                        String tipo = (String) obj;
                        if (tipo.equals("O")) {
                            al.add("OPERATIVO");
                        } else if (tipo.equals("N")) {
                            al.add("NON OPERATIVO");
                        } else {
                            al.add("ONORARIO");
                        }
                    } else // gestisce data
                    if (astring.equals("SODATANAS")) {
                        if (obj == null) {
                            al.add("");
                        } else {
                            Date sdata = (Date) obj;
                            DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                            al.add(format.format(sdata));
                        } 
                    } else
                    if (astring.equals("SOFOTO")) {
                        if (obj != null) {
                            al.add(app.getAppPath(EDirectories.IMAGES_PHOTOS)+ obj);
                        } else {
                            al.add("");
                        }
                    } else {
                        al.add(obj);
                    }
                }
                soci_values.add(al);
            }
        
            // intestazione dotazioni
            ArrayList<Object> intest = new ArrayList<Object>(3);
            intest.add("Id");
            intest.add("Descrizione");
            intest.add("Taglia");
            dotazioni_values.add(intest);                    
            // dotazioni
            rs = stmt.executeQuery("select * from DOTAZIONE");
            while (rs.next()) {
                ArrayList<Object> al = new ArrayList<Object>(3);
                al.add(rs.getInt("DOTID"));
                al.add(rs.getString("DOTDESCR"));
                al.add(rs.getString("DOTTAGLIA"));
                //
                dotazioni_values.add(al);
            }
            // intestazione interventi
            intest = new ArrayList<Object>(2);
            intest.add("Id");
            intest.add("Descrizione");
            interventi_values.add(intest);                    
            // interventi
            rs = stmt.executeQuery("select * from INTERVENTO");
            while (rs.next()) {
                ArrayList<Object> al = new ArrayList<Object>(2);
                al.add(rs.getInt("INTID"));
                al.add(rs.getString("INTDESCR"));
                //
                interventi_values.add(al);
            }
            // intestazione esercitazioni
            intest = new ArrayList<Object>(2);
            intest.add("Id");
            intest.add("Descrizione");
            esercitazioni_values.add(intest);                    
            // esercitazioni
            rs = stmt.executeQuery("select * from ESERCITAZIONE");
            while (rs.next()) {
                ArrayList<Object> al = new ArrayList<Object>(2);
                al.add(rs.getInt("ESEID"));
                al.add(rs.getString("ESEDESCR"));
                //
                esercitazioni_values.add(al);
            }
            // intestazione specializzazioni
            intest = new ArrayList<Object>(2);
            intest.add("Id");
            intest.add("Descrizione");
            specializzazioni_values.add(intest);                    
            // specializzazioni
            rs = stmt.executeQuery("select * from SPECIALIZZAZIONE");
            while (rs.next()) {
                ArrayList<Object> al = new ArrayList<Object>(2);
                al.add(rs.getInt("SPEID"));
                al.add(rs.getString("SPEDESCR"));
                //
                specializzazioni_values.add(al);
            }
            // dotazioni per socio
            rs = stmt.executeQuery("select * from SODO");
            while (rs.next()) {
                ArrayList<Object> al = new ArrayList<Object>(3);
                al.add(rs.getInt("SODOSO"));
                al.add(rs.getString("SODODO"));
                al.add(rs.getString("SODOQUANT"));
                //
                dotsocio_values.add(al);
            }
            // interventi per socio
            rs = stmt.executeQuery("select * from SOIN");
            while (rs.next()) {
                ArrayList<Object> al = new ArrayList<Object>(2);
                al.add(rs.getInt("SOINSO"));
                al.add(rs.getString("SOININ"));
                //
                intsocio_values.add(al);
            }
            // esercitazioni per socio
            rs = stmt.executeQuery("select * from SOES");
            while (rs.next()) {
                ArrayList<Object> al = new ArrayList<Object>(2);
                al.add(rs.getInt("SOESSO"));
                al.add(rs.getString("SOESES"));
                //
                esesocio_values.add(al);
            }
            // specializzazioni per socio
            rs = stmt.executeQuery("select * from SOSP");
            while (rs.next()) {
                ArrayList<Object> al = new ArrayList<Object>(2);
                al.add(rs.getInt("SOSPSO"));
                al.add(rs.getString("SOSPSP"));
                //
                spesocio_values.add(al);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(app.getMainFrame(),e,"ERRORE!",JOptionPane.ERROR_MESSAGE);
        }             
        
        // 
        if (et == EspType.XLS) {
            try {
                if (!filename.endsWith(".xls")) {
                    filename = filename + ".xls";
                }
                FileOutputStream os = new FileOutputStream(filename);
                HSSFWorkbook wb = XlsUtils.initXls();
                XlsUtils.addXlsWorksheet(wb, "Soci",soci_values);
                XlsUtils.addXlsWorksheet(wb, "Dotazioni",dotazioni_values);
                XlsUtils.addXlsWorksheet(wb, "Interventi",interventi_values);
                XlsUtils.addXlsWorksheet(wb, "Esercitazioni",esercitazioni_values);
                XlsUtils.addXlsWorksheet(wb, "Specializzazioni",specializzazioni_values);
                wb.write(os);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,"<html><font color='red'>ERRORE!</font>" + e,
                        "ERRORE",
                        JOptionPane.ERROR_MESSAGE);
            } 
        } else {
            try {
                if (!filename.endsWith(".csv")) {
                    filename = filename + ".csv";
                }
                FileWriter fw = new FileWriter(filename);
                for (ArrayList al : soci_values) {
                    for (Object obj : al) {
                        fw.write("\"" + obj + "\",");
                    }
                    fw.write("\n");
                }
                fw.write("\"Dotazioni:\"\n");
                for (ArrayList al : dotazioni_values) {
                    for (Object obj : al) {
                        fw.write("\"" + obj + "\",");
                    }
                    fw.write("\n");
                }
                fw.write("\"Interventi:\"\n");                
                for (ArrayList al : interventi_values) {
                    for (Object obj : al) {
                        fw.write("\"" + obj + "\",");
                    }
                    fw.write("\n");
                }
                fw.write("\"Esercitazioni:\"\n");                
                for (ArrayList al : esercitazioni_values) {
                    for (Object obj : al) {
                        fw.write("\"" + obj + "\",");
                    }
                    fw.write("\n");
                }
                fw.write("\"Specializzazioni:\"\n");
                for (ArrayList al : specializzazioni_values) {
                    for (Object obj : al) {
                        fw.write("\"" + obj + "\",");
                    }
                    fw.write("\n");
                }
                fw.write("\"Dotazioni per socio:\"\n");
                for (ArrayList al : dotsocio_values) {
                    for (Object obj : al) {
                        fw.write("\"" + obj + "\",");
                    }
                    fw.write("\n");
                }
                fw.write("\"Interventi per socio:\"\n");
                for (ArrayList al : intsocio_values) {
                    for (Object obj : al) {
                        fw.write("\"" + obj + "\",");
                    }
                    fw.write("\n");
                }
                fw.write("\"Esercitazioni per socio:\"\n");
                for (ArrayList al : esesocio_values) {
                    for (Object obj : al) {
                        fw.write("\"" + obj + "\",");
                    }
                    fw.write("\n");
                }
                fw.write("\"Specializzazioni per socio:\"\n");
                for (ArrayList al : spesocio_values) {
                    for (Object obj : al) {
                        fw.write("\"" + obj + "\",");
                    }
                    fw.write("\n");
                }
                fw.close();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,"<html><font color='red'>ERRORE!</font>" + e,
                        "ERRORE",
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
        jepData = new javax.swing.JEditorPane();
        jPanel1 = new javax.swing.JPanel();
        jbOk = new javax.swing.JButton();
        jbCancel = new javax.swing.JButton();

        setTitle("Backup");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        add(jepData, java.awt.BorderLayout.CENTER);

        jbOk.setMnemonic('o');
        jbOk.setText("ok");
        jbOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbOkActionPerformed(evt);
            }
        });

        jPanel1.add(jbOk);

        jbCancel.setMnemonic('c');
        jbCancel.setText("chiudi");
        jbCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbCancelActionPerformed(evt);
            }
        });

        jPanel1.add(jbCancel);

        add(jPanel1, java.awt.BorderLayout.SOUTH);

        pack();
    }
    // </editor-fold>//GEN-END:initComponents

    private void jbCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbCancelActionPerformed
        closeDialog(null);
    }//GEN-LAST:event_jbCancelActionPerformed

    private void jbOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbOkActionPerformed
        okActionPerformed();
    }//GEN-LAST:event_jbOkActionPerformed
    
    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        setVisible(false);
        dispose();
    }//GEN-LAST:event_closeDialog
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton jbCancel;
    private javax.swing.JButton jbOk;
    private javax.swing.JEditorPane jepData;
    // End of variables declaration//GEN-END:variables
    
}
