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
import com.l2fprod.common.swing.JTaskPane;
import com.l2fprod.common.swing.JTaskPaneGroup;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.HashMap;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.event.InternalFrameListener;
import proci.*;
import proci.reports.Report1;
import proci.reports.Report2;
import proci.reports.Report4;
import proci.reports.Report5;
import proci.reports.Report6;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author  simeo
 */
public class MainMenu extends javax.swing.JFrame implements InternalFrameListener {
    static final Logger logger = LoggerFactory.getLogger(MainMenu.class);    
    private static final long serialVersionUID = 4568749129288960200L;
    private final HashMap<String,JInternalFrame> hmFunz = new HashMap<>();
    
    private final MenuAction a1 = new MenuAction("Gestione Soci",GestioneSoci.class, "");
    private final MenuAction a2 = new MenuAction("Gestione Dotazioni",GestioneDotazioni.class, "");
    private final MenuAction a3 = new MenuAction("Gestione Interventi",GestioneInterventi.class, "");
    private final MenuAction a4 = new MenuAction("Gestione Esercitazioni",GestioneEsercitazioni.class, "");
    private final MenuAction a5 = new MenuAction("Gestione Specializzazioni",GestioneSpecializzazioni.class, "");
    private final MenuAction a6 = new MenuAction("Libreria Immagini",GestioneUpload.class, "");
    private final MenuAction a7 = new MenuAction("Gestione Periodi",GestionePeriodi.class, "");
    private final MenuAction a8 = new MenuAction("Gestione Quote",GestioneQuote.class, "");
    private final MenuAction s1 = new MenuAction("Schede Soci",Report1.class, "S");
    private final MenuAction s2 = new MenuAction("Dati Soci Op. (x socio) ",Report4.class, "");
    private final MenuAction s11 = new MenuAction("Dati Soci Op. (x dato)",Report5.class, "");
    private final MenuAction s3 = new MenuAction("Rubrica Operativi",Report2.class, "RO");
    private final MenuAction s4 = new MenuAction("Rubrica Soci",Report2.class, "RS");
    private final MenuAction s12 = new MenuAction("Indirizzi",Report6.class, "");    
    private final MenuAction s5 = new MenuAction("Badge",Report1.class, "B");
    private final MenuAction s6 = new MenuAction("Tessere",Report1.class, "T");
    private final MenuAction s7 = new MenuAction("Dotazioni",Report2.class, "D");
    private final MenuAction s8 = new MenuAction("Interventi",Report2.class, "I");
    private final MenuAction s9 = new MenuAction("Esercitazioni",Report2.class, "E");
    private final MenuAction s10 = new MenuAction("Specializzazioni",Report2.class, "S");
    private final MenuAction e1 = new MenuAction("Gestione Evento",proci.eventi.GestioneEvento.class, "");

    private final App app = App.getInstance();
    
    /** Creates new form MainMenu */
    public MainMenu() {
        initComponents();
        /////////////////////
        // gestione taskpane
        JTaskPane taskPaneContainer = new JTaskPane();
        taskPaneContainer.setBorder(null);
        // task pane archivi
        JTaskPaneGroup ap = new JTaskPaneGroup();
        ap.setTitle("Archivi");
        ap.setSpecial(false);
        ap.add(a1);
        ap.add(a2);
        ap.add(a3);
        ap.add(a4);
        ap.add(a5);
        ap.add(a6);
        ap.add(a7);
        ap.add(a8);
        taskPaneContainer.add(ap);
        // task pane stampe
        JTaskPaneGroup sp = new JTaskPaneGroup();
        sp.setTitle("Stampe");
        sp.add(s1);
        sp.add(s2);
        sp.add(s11);
        sp.add(s3);
        sp.add(s4);
        sp.add(s12);
        sp.add(s5);
        sp.add(s6);
        sp.add(s7);
        sp.add(s8);
        sp.add(s9);
        sp.add(s10);
        taskPaneContainer.add(sp);
        JTaskPaneGroup ep = new JTaskPaneGroup();
        ep.setTitle("Evento");
        ep.add(e1);
        taskPaneContainer.add(ep);
        jPanel2.add(taskPaneContainer);
        /////////////////////
        // gestione menu
        jmArchivi.add(a1);
        jmArchivi.add(a2);
        jmArchivi.add(a3);
        jmArchivi.add(a4);
        jmArchivi.add(a5);
        jmArchivi.add(a6);
        jmArchivi.add(a7);
        jmArchivi.add(a8);
        jmStampe.add(s1);
        jmStampe.add(s2);
        jmStampe.add(s11);
        jmStampe.add(s3);
        jmStampe.add(s4);
        jmStampe.add(s12);
        jmStampe.add(s5);
        jmStampe.add(s6);
        jmStampe.add(s7);
        jmStampe.add(s8);
        jmStampe.add(s9);
        jmStampe.add(s10);
        jmEvento.add(e1);
        /////////////////////
        app.setMainFrame(this);
        app.loadPrefs(this);
        // setta il titolo del frame principale
        setTitle("Gestione Soci Protezione Civile " + app.getSede());
    }
    
    private class MenuAction extends AbstractAction {
        private static final long serialVersionUID = 6093718316402972295L;
        public MenuAction(String testo,Class task,String tipo) {
            putValue(Action.NAME, testo);
            putValue("task", task);
            putValue("tipo", tipo);
        }
        @Override
        public void actionPerformed(ActionEvent ae) {
            openForm((Class) getValue("task"),(String) getValue("tipo"));
        }
    }
    
    public void esporta(Esporta.EspType et) {
        Esporta esp = new Esporta(this,true, et);
        esp.setVisible(true);
    }
    
    private void closeForm() {
        logger.debug("Closing...");        
        if (hmFunz.size() > 0) {
            for (JInternalFrame frame : hmFunz.values()) {
                if (((ProciInternalFrame) frame).isBusy()) {
                    JOptionPane.showMessageDialog(this,"Terminare tutte le funzioni per uscire!","MESSAGGIO",
                            JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                app.savePrefs(frame);
            }
        }
        try {
            DBHandler.getInstance().close();
        } catch (Exception e) {
            logger.debug("exc: " + e.getLocalizedMessage());
        }
        app.savePrefs(this);
        app.flushPrefs();
        File f = new File(app.getAppPath() + "/data/run.lck");
        f.delete();
        dispose();
        logger.debug("Done.");
    }
    
    @Override
    public void internalFrameActivated(javax.swing.event.InternalFrameEvent evt) {
    }
    @Override
    public void internalFrameClosed(javax.swing.event.InternalFrameEvent evt) {
        JInternalFrame iframe = (JInternalFrame) evt.getSource();
        hmFunz.remove(iframe.getName());
        app.savePrefs(iframe);
    }
    @Override
    public void internalFrameClosing(javax.swing.event.InternalFrameEvent evt) {
    }
    @Override
    public void internalFrameDeactivated(javax.swing.event.InternalFrameEvent evt) {
    }
    @Override
    public void internalFrameDeiconified(javax.swing.event.InternalFrameEvent evt) {
    }
    @Override
    public void internalFrameIconified(javax.swing.event.InternalFrameEvent evt) {
    }
    @Override
    public void internalFrameOpened(javax.swing.event.InternalFrameEvent evt) {
    }
    
    private void openForm(Class classe,String tipo) {
        String key = classe.getName() + tipo;
        try {
            if (hmFunz.containsKey(key)) {
                JInternalFrame ist = (JInternalFrame) hmFunz.get(key);
                ist.toFront();
            } else {
                JInternalFrame ist = (JInternalFrame) classe.newInstance();
                ist.setName(key);
                ist.addInternalFrameListener(this);
                hmFunz.put(key, ist);
                desktopPane.add(ist,-1);
                app.loadPrefs(ist);
                if (classe.getName().endsWith("Report1")) {
                    ((Report1) ist).setTipo(tipo);
                }
                if (classe.getName().endsWith("Report2")) {
                    ((Report2) ist).setTipo(tipo);
                }
                ist.setVisible(true);
            }
        } catch (InstantiationException | IllegalAccessException ie) {
            JOptionPane.showMessageDialog(this,ie,"ERRORE",JOptionPane.ERROR_MESSAGE);
        }
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        desktopPane = new javax.swing.JDesktopPane();
        jpMenu = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel2 = new javax.swing.JPanel();
        menuBar = new javax.swing.JMenuBar();
        jmFile = new javax.swing.JMenu();
        jmEsci = new javax.swing.JMenuItem();
        jmArchivi = new javax.swing.JMenu();
        jmStampe = new javax.swing.JMenu();
        jmEvento = new javax.swing.JMenu();
        jmUtilities = new javax.swing.JMenu();
        jmPreferenze = new javax.swing.JMenuItem();
        jmStatistiche = new javax.swing.JMenuItem();
        jmBackup = new javax.swing.JMenuItem();
        jmEsportaXLS = new javax.swing.JMenuItem();
        jmEsportaCSV = new javax.swing.JMenuItem();
        jmImporta = new javax.swing.JMenuItem();
        jmHelp = new javax.swing.JMenu();
        jmAbout = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setName(""); // NOI18N
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
        getContentPane().add(desktopPane, java.awt.BorderLayout.CENTER);

        jpMenu.setLayout(new java.awt.BorderLayout());

        jScrollPane1.setViewportView(jPanel2);

        jpMenu.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        getContentPane().add(jpMenu, java.awt.BorderLayout.WEST);

        jmFile.setText("File");

        jmEsci.setText("Esci");
        jmEsci.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jmEsciActionPerformed(evt);
            }
        });
        jmFile.add(jmEsci);

        menuBar.add(jmFile);

        jmArchivi.setText("Archivi");
        menuBar.add(jmArchivi);

        jmStampe.setText("Stampe");
        menuBar.add(jmStampe);

        jmEvento.setText(" Evento");
        menuBar.add(jmEvento);

        jmUtilities.setText("Utilities");

        jmPreferenze.setText("Preferenze");
        jmPreferenze.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jmPreferenzeActionPerformed(evt);
            }
        });
        jmUtilities.add(jmPreferenze);

        jmStatistiche.setText("Statistiche");
        jmStatistiche.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jmStatisticheActionPerformed(evt);
            }
        });
        jmUtilities.add(jmStatistiche);

        jmBackup.setText("Salva / Ripristina archivi");
        jmBackup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jmBackupActionPerformed(evt);
            }
        });
        jmUtilities.add(jmBackup);

        jmEsportaXLS.setText("Esporta in formato XLS");
        jmEsportaXLS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jmEsportaXLSActionPerformed(evt);
            }
        });
        jmUtilities.add(jmEsportaXLS);

        jmEsportaCSV.setText("Esporta in formato CSV");
        jmEsportaCSV.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jmEsportaCSVActionPerformed(evt);
            }
        });
        jmUtilities.add(jmEsportaCSV);

        jmImporta.setText("Importa dati da CSV");
        jmImporta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jmImportaActionPerformed(evt);
            }
        });
        jmUtilities.add(jmImporta);

        menuBar.add(jmUtilities);

        jmHelp.setText("Aiuto");

        jmAbout.setText("About");
        jmAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jmAboutActionPerformed(evt);
            }
        });
        jmHelp.add(jmAbout);

        menuBar.add(jmHelp);

        setJMenuBar(menuBar);

        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    private void jmPreferenzeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jmPreferenzeActionPerformed
        Preferenze pref = new Preferenze(this,true);
        pref.setVisible(true);
    }//GEN-LAST:event_jmPreferenzeActionPerformed
    
    private void jmImportaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jmImportaActionPerformed
        Importa imp = new Importa(this,true);
        imp.setVisible(true);
    }//GEN-LAST:event_jmImportaActionPerformed
    
    private void jmEsciActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jmEsciActionPerformed
        closeForm();
    }//GEN-LAST:event_jmEsciActionPerformed
    
    private void jmEsportaCSVActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jmEsportaCSVActionPerformed
        esporta(Esporta.EspType.CSV);
    }//GEN-LAST:event_jmEsportaCSVActionPerformed
    
    private void jmEsportaXLSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jmEsportaXLSActionPerformed
        esporta(Esporta.EspType.XLS);
    }//GEN-LAST:event_jmEsportaXLSActionPerformed
    
    private void jmBackupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jmBackupActionPerformed
        Backup bak = new Backup(this,true);
        bak.setVisible(true);
    }//GEN-LAST:event_jmBackupActionPerformed
    
    private void jmStatisticheActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jmStatisticheActionPerformed
        Statistiche stat = new Statistiche(this,true);
        stat.setVisible(true);
    }//GEN-LAST:event_jmStatisticheActionPerformed
    
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        closeForm();
    }//GEN-LAST:event_formWindowClosing
    
    private void jmAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jmAboutActionPerformed
        About about = new About(this,true);
        about.setVisible(true);
    }//GEN-LAST:event_jmAboutActionPerformed
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MainMenu().setVisible(true);
            }
        });
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JDesktopPane desktopPane;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JMenuItem jmAbout;
    private javax.swing.JMenu jmArchivi;
    private javax.swing.JMenuItem jmBackup;
    private javax.swing.JMenuItem jmEsci;
    private javax.swing.JMenuItem jmEsportaCSV;
    private javax.swing.JMenuItem jmEsportaXLS;
    private javax.swing.JMenu jmEvento;
    private javax.swing.JMenu jmFile;
    private javax.swing.JMenu jmHelp;
    private javax.swing.JMenuItem jmImporta;
    private javax.swing.JMenuItem jmPreferenze;
    private javax.swing.JMenu jmStampe;
    private javax.swing.JMenuItem jmStatistiche;
    private javax.swing.JMenu jmUtilities;
    private javax.swing.JPanel jpMenu;
    private javax.swing.JMenuBar menuBar;
    // End of variables declaration//GEN-END:variables
    
}
