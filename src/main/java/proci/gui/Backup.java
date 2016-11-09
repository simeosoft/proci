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

import com.simeosoft.swing.OpenFileChooser;
import com.simeosoft.swing.SwingUtils;
import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import javax.swing.JOptionPane;
import proci.DBHandler;
import proci.App;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author  simeo
 */
public class Backup extends java.awt.Dialog {
    static final Logger logger = LoggerFactory.getLogger(Backup.class);    

    final int BAK_WIDTH = 400;
    final int BAK_HEIGHT = 400;       
    
    File dbfile = null;
    File bakfile = null;
    FileOutputStream fos = null;
    ZipOutputStream zos = null;
    StringBuilder text = new StringBuilder();    
    int size = 0;

    private final App app = App.getInstance();
   
    /** Creates new form Statistiche */
    public Backup(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        Dimension frmSize = parent.getSize();
        Point loc = parent.getLocation();
        this.setSize(BAK_WIDTH,BAK_HEIGHT);
        this.setLocation((frmSize.width - BAK_WIDTH) / 2 + loc.x, 
                        (frmSize.height - BAK_HEIGHT) / 2 + loc.y);
        jepData.setEditable(false);
        jepData.setContentType("text/html");
        jbBackup.setEnabled(false);
        
        text.append("<html><body><font face='verdana,arial' size=+1><p align='center'>Backup/Restore</p></font>");
        dbfile = new File(app.getAppPath() + "/data/procidb");
        bakfile = new File(app.getAppPath() + "/data/proci.zip");
        if (! dbfile.exists()) {
            text.append("<p><font color='red'><h3>ERRORE! File " + dbfile.getPath() + " inesistente!</h3></font></p>");
        } else {
            text.append("<p><font color='blue'><h3>cliccare su 'backup' per copiare gli archivi!</h3></font>");
            jbBackup.setEnabled(true);
        }
        if (! bakfile.exists()) {
            text.append("<p><font color='blue'><h3>nessun backup effettuato!</h3></font>");            
        } else {
            text.append("<p><font color='blue'><h3>ultimo backup effettuato: " + new Date(bakfile.lastModified()) + "</h3></font>");
            text.append("<p><font color='blue'><h3>cliccare su 'restore' per ripristinare gli archivi!</h3></font>");
        }
        jepData.setText(text.toString());
    }

    private void abilitaFunzioni(boolean abilita) {
        jbBackup.setEnabled(abilita);
        jbRestore.setEnabled(abilita);        
        jbCancel.setEnabled(abilita);
        jcbDettagli.setEnabled(abilita);
        jcbCompleto.setEnabled(abilita);
    }
    
    private void doBackup() {
        abilitaFunzioni(false);
        if (bakfile.exists()) {
            bakfile.delete();
        }
        text.delete(0, text.length());
        try {
            DBHandler.getInstance().close();            
            bakfile.createNewFile();
            size = 0;
            fos = new FileOutputStream(bakfile);
            zos = new ZipOutputStream(fos);
            // installo un file filter per npn considerare eventuali immagini thumbs.db (windows)
            ThumbsFileFilter tff = new ThumbsFileFilter();
            // database
            // TODO: completare qui e su dorestore()
            
            
            File dbpath = new File(app.getAppPath() + "/data/procidb");
            File[] filenames = dbpath.listFiles(tff);
            for (File file : filenames) {
                writeEntry(file,"data/procidb/" + file.getName());
            }            
            
            
            // TODO: UTILIZZARE LOOP
            // directory immagini foto soci
            File sociimgpath = new File(app.getAppPath() + "/immagini/foto_soci");
            File[] filenames2 = sociimgpath.listFiles(tff);
            for (File file : filenames2) {
                writeEntry(file,"immagini/foto_soci/" + file.getName());
            }            
            // directory miniature
            File minimgpath = new File(app.getAppPath() + "/immagini/miniature");
            File[] filenames4 = minimgpath.listFiles(tff);
            for (File file : filenames4) {
                writeEntry(file,"immagini/miniature/" + file.getName());
            }            
            // directory upload (non strettamente necessario)
            File uploadimgpath = new File(app.getAppPath() + "/immagini/upload");
            File[] filenames5 = uploadimgpath.listFiles(tff);
            for (File file : filenames5) {
                writeEntry(file,"immagini/upload/" + file.getName());
            }            
            // directory upload_resized (non strettamente necessario)
            File uploadresimgpath = new File(app.getAppPath() + "/immagini/upload_resized");
            File[] filenames6 = uploadresimgpath.listFiles(tff);
            for (File file : filenames6) {
                writeEntry(file,"immagini/upload_resized/" + file.getName());
            }            
            if (jcbCompleto.isSelected()) {
                // directory immagini tessere etc..
                File imgpath = new File(app.getAppPath() + "/immagini/");
                File[] filenames3 = imgpath.listFiles(tff);
                for (File file : filenames3) {
                    if (!file.isDirectory()) {
                        writeEntry(file,"immagini/" + file.getName());
                    }
                }            
                // directory immagini tessere etc..
                imgpath = new File(app.getAppPath() + "/immagini/common/");
                filenames3 = imgpath.listFiles(tff);
                for (File file : filenames3) {
                    if (!file.isDirectory()) {
                        writeEntry(file,"immagini/common/" + file.getName());
                    }
                }            
            }
            zos.close();
            text.append("<p><h3>backup effettuato: " + size + " byte.</h3></font></p>" );
            text.append("<p><h3>copiare il file: <font color='red'>" + bakfile.getPath() + "</font> su floppy " +
                    "o altro media removibile.</h3></p>");
            jepData.setText(text.toString());            
            DBHandler.getInstance().open();
        } catch (IOException ioe) {
            jepData.setText("<p><font color='red'><h3>ERRORE IO! " + ioe + "</h3></font></p>" );
            logger.error("Errore IO: {}",ioe);
        } catch (Exception e) {
            jepData.setText("<p><font color='red'><h3>ERRORE DB! " + e + "</h3></font></p>" );
            logger.error("Errore DB: {}",e);
        }
        abilitaFunzioni(true);
    }

    private void writeEntry(File file,String name) throws IOException {
        if (jcbDettagli.isSelected()) {
            text.append(name + "<br>");
        }
        ZipEntry ze = new ZipEntry(name);
        ze.setTime(file.lastModified());
        zos.putNextEntry(ze);
        FileInputStream fis = new FileInputStream(file);
        int letti = 0;
        int offset = 0;
        while (fis.available() > 0) {
            byte[] arb = new byte[fis.available()];
            letti = fis.read(arb,0, fis.available());
            zos.write(arb, 0, letti);
            size += letti;
        }
        fis.close();
        zos.closeEntry();        
    }
    
    private void doRestore() {
        int rc = JOptionPane.showConfirmDialog(this,"ATTENZIONE! GLI ARCHIVI VERRANNO RIPRISTINATI!\n CONFERMI?","ATTENZIONE",
                JOptionPane.YES_NO_OPTION);
        if (rc == JOptionPane.NO_OPTION) {
            return;
        }
        boolean sovrascriviTutti = false;
        abilitaFunzioni(false);        
        OpenFileChooser ofc = new OpenFileChooser(app.getAppPath() +
                 System.getProperty("file.separator") + "data", "zip", "File ZIP (.zip)");
        rc = ofc.showOpenDialog(this);
        if (rc == ofc.CANCEL_OPTION) {
            abilitaFunzioni(true);
            return;
        }
        text.delete(0, text.length());        
        File zipfile = ofc.getSelectedFile();
        StringBuilder text = new StringBuilder();
        //
        try {
            DBHandler.getInstance().close();
            ZipFile zif = new ZipFile(zipfile);
            Enumeration entries = zif.entries();
            while (entries.hasMoreElements()) {
                ZipEntry ze = (ZipEntry) entries.nextElement();
                // ignoro eventuali file thumbs.db (windows).. il file zip
                // potrebbe essere stato creato con qualche thumbs spurio..
                if (ze.getName().endsWith("Thumbs.db")) {
                    continue;
                }
                File oldFile = new File(app.getAppPath() + "/" + ze.getName());
                // Se il vecchio file esiste e la data di modifica e' maggiore di quella
                // dell'archivio, chiedo conferma.
                // VALE SOLO PER LE IMMAGINI!
                if (oldFile.exists() 
                        && oldFile.lastModified() > ze.getTime() 
                        && oldFile.getCanonicalPath().indexOf("data") == -1
                        && oldFile.getCanonicalPath().indexOf("foto_soci") == -1
                        && oldFile.getCanonicalPath().indexOf("miniature") == -1) {
                    if (! sovrascriviTutti) {
                        BackupDialog bd = new BackupDialog(app.getMainFrame(),true,oldFile);
                        SwingUtils.centerForm(this, bd);
                        bd.setVisible(true);
                        if (bd.getReturnStatus() == bd.RET_CANCEL) {
                            continue;
                        }
                        if (bd.getReturnStatus() == bd.RET_TUTTI) {
                            sovrascriviTutti = true;
                        }
                    }
                }
                FileOutputStream fos = new FileOutputStream(app.getAppPath() + "/" + ze.getName());                
                text.append(ze.getName() + " " + ze.getSize() + "<br>");
                InputStream is = zif.getInputStream(ze);
                int letti = 0;
                int offset = 0;
                while (is.available() > 0) {
                    byte[] arb = new byte[is.available()];
                    letti = is.read(arb,0, is.available());
                    fos.write(arb, 0, letti);
                    offset += letti;
                }
                fos.close();
            }
            text.append("<p><h3>restore effettuato!</h3></font></p>" );
            jepData.setText(text.toString());            
            DBHandler.getInstance().open();
        } catch (IOException ioe) {
            jepData.setText("<p><font color='red'><h3>ERRORE IO! " + ioe + "</h3></font></p>" );
            logger.error("Errore IO: {}",ioe);
        } catch (Exception e) {
            jepData.setText("<p><font color='red'><h3>ERRORE DB! " + e + "</h3></font></p>" );
            logger.error("Errore DB: {}",e);
        }
        abilitaFunzioni(true);        
    }
    
    private class ThumbsFileFilter implements FileFilter {
        public boolean accept(File file) {
            if (file.getName().equalsIgnoreCase("thumbs.db")) {
                return false;
            }
            return true;
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

        jPanel1 = new javax.swing.JPanel();
        jbBackup = new javax.swing.JButton();
        jbRestore = new javax.swing.JButton();
        jbCancel = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jepData = new javax.swing.JEditorPane();
        jPanel2 = new javax.swing.JPanel();
        jcbDettagli = new javax.swing.JCheckBox();
        jcbCompleto = new javax.swing.JCheckBox();

        setLayout(new java.awt.GridBagLayout());

        setTitle("Backup / Restore Archivi");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        jbBackup.setMnemonic('b');
        jbBackup.setText("backup");
        jbBackup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbBackupActionPerformed(evt);
            }
        });

        jPanel1.add(jbBackup);

        jbRestore.setMnemonic('r');
        jbRestore.setText("restore");
        jbRestore.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbRestoreActionPerformed(evt);
            }
        });

        jPanel1.add(jbRestore);

        jbCancel.setMnemonic('c');
        jbCancel.setText("chiudi");
        jbCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbCancelActionPerformed(evt);
            }
        });

        jPanel1.add(jbCancel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        add(jPanel1, gridBagConstraints);

        jScrollPane1.setViewportView(jepData);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 100.0;
        gridBagConstraints.weighty = 100.0;
        add(jScrollPane1, gridBagConstraints);

        jcbDettagli.setText("mostra dettagli");
        jPanel2.add(jcbDettagli);

        jcbCompleto.setText("copia immagini stampe");
        jPanel2.add(jcbCompleto);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        add(jPanel2, gridBagConstraints);

        pack();
    }
    // </editor-fold>//GEN-END:initComponents

    private void jbRestoreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbRestoreActionPerformed
        doRestore();
    }//GEN-LAST:event_jbRestoreActionPerformed

    private void jbCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbCancelActionPerformed
        closeDialog(null);
    }//GEN-LAST:event_jbCancelActionPerformed

    private void jbBackupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbBackupActionPerformed
        doBackup();
    }//GEN-LAST:event_jbBackupActionPerformed
    
    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        setVisible(false);
        dispose();
    }//GEN-LAST:event_closeDialog
      
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton jbBackup;
    private javax.swing.JButton jbCancel;
    private javax.swing.JButton jbRestore;
    private javax.swing.JCheckBox jcbCompleto;
    private javax.swing.JCheckBox jcbDettagli;
    private javax.swing.JEditorPane jepData;
    // End of variables declaration//GEN-END:variables
    
}
