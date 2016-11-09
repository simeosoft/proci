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
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import proci.App;
import proci.DBHandler;
import proci.EDirectories;
import proci.util.ProciUtils;


/**
 * Importa dati da file CSV.<br>
 * Da intendersi come importazione iniziale (elimina tutti i dati presenti)
 * 
 * Questa versione importa solo record soci
 * 
 * @author  simeo
 */
public class Importa extends java.awt.Dialog {
    static final Logger logger = LoggerFactory.getLogger(Importa.class); 
    
    final int IMPO_WIDTH = 400;
    final int IMPO_HEIGHT = 400;       
    
    String filename = "";
    Connection conn;
    Statement stmt;
    ResultSet rs;
    PreparedStatement psSocio;
    PreparedStatement psDot;
    PreparedStatement psInt;
    PreparedStatement psEse;
    PreparedStatement psSpe;
    PreparedStatement psSodo;
    PreparedStatement psSoin;
    PreparedStatement psSoes;
    PreparedStatement psSosp;
    StringBuilder text = new StringBuilder();    
    private final App app = App.getInstance();      
    
    /** Creates new form Importa */
    public Importa(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        Dimension frmSize = parent.getSize();
        Point loc = parent.getLocation();
        this.setSize(IMPO_WIDTH,IMPO_HEIGHT);
        this.setLocation((frmSize.width - IMPO_WIDTH) / 2 + loc.x, 
                        (frmSize.height - IMPO_HEIGHT) / 2 + loc.y);
        jepData.setEditable(false);
        jepData.setContentType("text/html");
        String text = "<html><body><font face='verdana,arial' size=+1><p align='center'>Importazione</p></font>" +
                "<p><font color='red'>ATTENZIONE:</font> I dati presenti in archivio verranno eliminati!!</p>" +
                "<p><font color='red'>ATTENZIONE:</font> L'operazione di import/export non è reversibile. Usare "
                + "la funzione di backup per copiare dati tra diverse istanze della procedura!</p>";
        jepData.setText(text);
    }
    
    private void okActionPerformed() {
        OpenFileChooser ofc = new OpenFileChooser(app.getAppPath(), ".csv", "File CSV (.csv)");
        int rc = ofc.showOpenDialog(this);
        if (rc == JFileChooser.APPROVE_OPTION) {
            filename = ofc.getSelectedFile().getPath();
            doImport();
        }
    }
    
    private void doImport() {
        jepData.setText("");
        text = new StringBuilder("<html><body>");
        try {
            conn = DBHandler.getInstance().getConnection();
            stmt = conn.createStatement();
            conn.setAutoCommit(false);            
            int delRec = 0;
            delRec = stmt.executeUpdate("delete from SOCIO");
            text.append("Cancellati: ").append(delRec).append(" soci!<br>");
            delRec = stmt.executeUpdate("delete from DOTAZIONE");
            text.append("Cancellate: ").append(delRec).append(" dotazioni!<br>");
            delRec = stmt.executeUpdate("delete from INTERVENTO");
            text.append("Cancellati: ").append(delRec).append(" interventi!<br>");
            delRec = stmt.executeUpdate("delete from ESERCITAZIONE");
            text.append("Cancellate: ").append(delRec).append(" esercitazioni!<br>");
            delRec = stmt.executeUpdate("delete from SPECIALIZZAZIONE");
            text.append("Cancellate: ").append(delRec).append(" specializzazioni!<br>");
            delRec = stmt.executeUpdate("delete from SODO");
            delRec = stmt.executeUpdate("delete from SOIN");
            delRec = stmt.executeUpdate("delete from SOES");
            delRec = stmt.executeUpdate("delete from SOSP");
            // elimina foto dei soci
            File imagedir = new File(app.getAppPath(EDirectories.IMAGES_PHOTOS));
            File[] images = imagedir.listFiles();
            for (File f : images) {
                f.delete();
            }
            text.append("Eliminate: ").append(images.length).append(" immagini<br>");
            // elimina miniature
            imagedir = new File(app.getAppPath(EDirectories.IMAGES_THUMBS));
            images = imagedir.listFiles();
            for (File f : images) {
                f.delete();
            }
            text.append("Eliminate: ").append(images.length).append(" miniature<br>");
            //
            psSocio = conn.prepareStatement("insert into SOCIO values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
            psDot = conn.prepareStatement("insert into DOTAZIONE values (?,?,?)");
            psInt = conn.prepareStatement("insert into INTERVENTO values (?,?)");
            psEse = conn.prepareStatement("insert into ESERCITAZIONE values (?,?)");
            psSpe = conn.prepareStatement("insert into SPECIALIZZAZIONE values (?,?)");
            psSodo = conn.prepareStatement("insert into SODO values (?,?,?)");
            psSoin = conn.prepareStatement("insert into SOIN values (?,?)");
            psSoes = conn.prepareStatement("insert into SOES values (?,?)");
            psSosp = conn.prepareStatement("insert into SOSP values (?,?)");
            //
            File csvData = new File(filename);
            CSVParser parser = CSVParser.parse(csvData, Charset.forName("UTF-8"),CSVFormat.RFC4180);
            for (CSVRecord csvRecord : parser) {
                // trappola intestazione
                if (csvRecord.get(0).equalsIgnoreCase("IDSocio")) {
                       continue;
                }
                logger.info("Insert Socio: {}",csvRecord);
                insertSocio(csvRecord);
//                        break;
//                    case 2:
//                        insertDot(line);
//                        break;
//                    case 3:
//                        insertInt(line);
//                        break;
//                    case 4:
//                        insertEse(line);
//                        break;
//                    case 5:
//                        insertSpe(line);
//                        break;
//                    case 6:
//                        insertSodo(line);
//                        break;
//                    case 7:
//                        insertSoin(line);
//                        break;
//                    case 8:
//                        insertSoes(line);
//                        break;
//                    case 9:
//                        insertSosp(line);
//                        break;
//                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,"<html><font color='red'>ERRORE!</font>" + e,
                    "ERRORE",
                    JOptionPane.ERROR_MESSAGE);
            text.append("ERRORE: " + e);
            jepData.setText(text.toString());
            return;
        } 
        try {
            conn.commit();
        } catch (SQLException e) {
        }
        jepData.setText(text.toString());
    }
    
    //private void insertSocio(String[] line) throws Exception {
    private void insertSocio(CSVRecord line) throws Exception {
        text.append("Elaboro socio:").append(line.get(0)).append("<br>");
        psSocio.setInt(1,Integer.parseInt(line.get(0)));
        psSocio.setString(2,line.get(1));
        psSocio.setString(3,line.get(2));
        psSocio.setString(4,line.get(3));
        psSocio.setString(5,line.get(4));
        psSocio.setDate(6,SwingUtils.formatStringToDate(line.get(5)));
        psSocio.setString(7,line.get(6));
        psSocio.setString(8,line.get(7));
        psSocio.setString(9,line.get(8));
        psSocio.setString(10,line.get(9));
        psSocio.setString(11,line.get(10));
        psSocio.setString(12,line.get(11));
        psSocio.setString(13,line.get(12));
        psSocio.setInt(14,Integer.parseInt(line.get(13)));
        psSocio.setInt(15,Integer.parseInt(line.get(14)));
        psSocio.setString(16,line.get(15));
        psSocio.setString(17,line.get(16));
        if (line.get(17).length() > 0) {
            File imageFile = new File(line.get(17));
            if (! imageFile.isAbsolute()) {
                imageFile = new File(app.getAppPath() + 
                    "/" + line.get(17));
            }
            if (! imageFile.exists()) {
                throw new Exception("ERRORE: Attenzione: il file immagine " + imageFile.getAbsolutePath() + " non esiste!! ");
            }
            ProciUtils.copiaFotoSoci(imageFile);
            ProciUtils.creaMiniatura(imageFile);
            psSocio.setString(18,imageFile.getName());
        } else {
            psSocio.setString(18,null);
        }
        String tipoSocio = "O";
        if (line.get(18).equals("ONORARIO")) {
            tipoSocio = "X";
        }
        if (line.get(18).equals("NON OPERATIVO")) {
            tipoSocio = "N";
        }
        psSocio.setString(19,tipoSocio);
        psSocio.setString(20,line.get(19));
        psSocio.setString(21,line.get(20));
        psSocio.setString(22,line.get(21));
        psSocio.setString(23,line.get(22));
        psSocio.execute();
    }
    
    /* PER ORA DISABILITATE 
    private void insertDot(String[] line) throws Exception {
        text.append("Elaboro dotazione:" + line[0] + "<br>");
        psDot.setInt(1,Integer.parseInt(line[0]));
        psDot.setString(2,line[1]);
        psDot.setString(3,line[2]);
        psDot.execute();        
    }    
    private void insertInt(String[] line) throws Exception {
        text.append("Elaboro intervento:" + line[0] + "<br>");
        psInt.setInt(1,Integer.parseInt(line[0]));
        psInt.setString(2,line[1]);
        psInt.execute();        
    }    
    private void insertEse(String[] line) throws Exception {
        text.append("Elaboro esercitazione:" + line[0] + "<br>");
        psEse.setInt(1,Integer.parseInt(line[0]));
        psEse.setString(2,line[1]);
        psEse.execute();        
    }    
    private void insertSpe(String[] line) throws Exception {
        text.append("Elaboro specializzazione:" + line[0] + "<br>");
        psSpe.setInt(1,Integer.parseInt(line[0]));
        psSpe.setString(2,line[1]);
        psSpe.execute();        
    }    
    private void insertSodo(String[] line) throws Exception {
        text.append("Elaboro dotazione socio:" + line[0] + "<br>");
        psSodo.setInt(1,Integer.parseInt(line[0]));
        psSodo.setInt(2,Integer.parseInt(line[1]));
        psSodo.setInt(3,Integer.parseInt(line[2]));
        psSodo.execute();        
    }
    private void insertSoin(String[] line) throws Exception {
        text.append("Elaboro intervento socio:" + line[0] + "<br>");
        psSoin.setInt(1,Integer.parseInt(line[0]));
        psSoin.setInt(2,Integer.parseInt(line[1]));
        psSoin.execute();        
    }        
    private void insertSoes(String[] line) throws Exception {
        text.append("Elaboro esercitazione socio:" + line[0] + "<br>");
        psSoes.setInt(1,Integer.parseInt(line[0]));
        psSoes.setInt(2,Integer.parseInt(line[1]));
        psSoes.execute();        
    }
    private void insertSosp(String[] line) throws Exception {
        text.append("Elaboro specializzazione socio:" + line[0] + "<br>");
        psSosp.setInt(1,Integer.parseInt(line[0]));
        psSosp.setInt(2,Integer.parseInt(line[1]));
        psSosp.execute();        
    }        
    */
        
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jPanel1 = new javax.swing.JPanel();
        jbOk = new javax.swing.JButton();
        jbCancel = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jepData = new javax.swing.JEditorPane();

        setTitle("Importa");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

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

        jScrollPane1.setViewportView(jepData);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);

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
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton jbCancel;
    private javax.swing.JButton jbOk;
    private javax.swing.JEditorPane jepData;
    // End of variables declaration//GEN-END:variables
    
}
