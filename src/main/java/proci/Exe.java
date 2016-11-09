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

package proci;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.flywaydb.core.Flyway;
import proci.gui.MainMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import proci.gui.InitDb;

/**
 *
 * @author simeo
 */
public class Exe {
    static final Logger logger = LoggerFactory.getLogger(Exe.class);        
    static boolean isWindows = false;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        Exe exe = new Exe();
        exe.init();
    }
        
        
    public void init() {
        App app = App.getInstance();
        logger.info("****** Proci ver.: " + App.VERSION + " starting... ******");
        Map<String,String> env = System.getenv();
        if (System.getProperty("os.name").startsWith("Windows")) {
            isWindows = true;
        }
        if (isWindows && Double.parseDouble(System.getProperty("os.version")) > 6.0) {
            app.setAppPath(env.get("LOCALAPPDATA") + File.separatorChar + "Proci" + File.separatorChar);
        } else {
            app.setAppPath(System.getProperty("user.dir") + File.separatorChar);
        }
        logger.debug("Application path: {}",app.getAppPath());
        File f = null;
        boolean created = false;
        for (EDirectories edir : EDirectories.values()) {
            f = new File(app.getAppPath() + edir.getDir());
            logger.debug("considero: {}",f.getAbsolutePath());
            if (! f.exists()) {
                logger.debug("creo: {}",f.getAbsolutePath());
                if (f.mkdirs()) {
                    created = true;
                }
            }
        }
        try{
            UIManager.setLookAndFeel("com.jgoodies.plaf.plastic.Plastic3DLookAndFeel");
            UIManager.put("jgoodies.popupDropShadowEnabled", Boolean.TRUE);
        } catch(ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            logger.debug("UI Manager ex: {}",ex.getLocalizedMessage());
        }
        // controllo per istanze multiple
        f = new File(app.getAppPath(EDirectories.DATA) + "run.lck");
        logger.debug("File lock: {}",f.getAbsolutePath());
        if (f.exists()) {
            String msg = "<html><b>ATTENZIONE!</b><br>Sembra che il programma sia già " +
                    "in esecuzione!<br>Confermi l'avvio del programma?";
            if (JOptionPane.showConfirmDialog(null,msg,
                    "ATTENZIONE",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE) == JOptionPane.CANCEL_OPTION) {
                System.exit(0);
            }
        }
        try {
            f.createNewFile();
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(null,"ERRORE CREAZIONE FILE: " + ioe,"ERRORE",
                        JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }        
        //
        logger.info("- User dir: " + app.getAppPath());
        logger.info("- created directory data: " + created);
        // caricamento server hsqldb
        try {
            DBHandler.getInstance().open();
            // conn.setAutoCommit(false);
            logger.info("Derby DB opened {}",DBHandler.getInstance().getConnection());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,"<html>ERRORE CONNESSIONE DB: <b>" + e + "</b>","ERRORE",
                    JOptionPane.ERROR_MESSAGE);
            logger.error("ERRORE CONNESSIONE DB: " + e);
            System.exit(1);
        }
        String sede;
        Statement stmt;
        try {
            Connection conn = DBHandler.getInstance().getConnection();
            // controllo consistenza db
            Flyway flyway = new Flyway();
            flyway.setDataSource(DBHandler.getInstance().getDataSource());
            flyway.setBaselineOnMigrate(true);
            flyway.migrate();
            //
            File initFlag = new File(app.getAppPath(EDirectories.DATA) + "flag_init");
            if (! initFlag.exists()) {
                InitDb idb = new InitDb(null, true);
                idb.setVisible(true);
                sede = idb.getSede();
                // aggiorna la sede
                stmt = conn.createStatement();
                stmt.execute("insert into UTIL values ('SEDE','" + sede + "')");
                initFlag.createNewFile();
            } else {
                // legge la sede dal db
                stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("select * from UTIL where UTILKEY = 'SEDE'");
                rs.next();
                sede = rs.getString("UTILDATA");
                logger.debug("Sede: " + sede);
            }
            app.setSede(sede);
        } catch (IOException | SQLException se) {
            JOptionPane.showMessageDialog(null,"<html>ERRORE INIT DB (2): <b>" + se + "</b>","ERRORE",
                    JOptionPane.ERROR_MESSAGE);
            logger.error("ERRORE DB: " + se);                
        }
        //
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MainMenu().setVisible(true);
            }
        });
    }
}
