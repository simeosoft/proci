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

import java.awt.Dialog;
import java.awt.Frame;
import java.beans.PropertyVetoException;
import java.io.File;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;

/**
 * Classe statica per memorizzazione dati comuni.<br>
 * Il file di proprietà non viene più usato in Proci (sostituito da tabella UTIL)
 * @author simeo
 */
public class App {
    public static final String VERSION = "4.0";
    public static final String HTTPCONTEXT = "/proci";
    public static final int HTTPPORT = 10666;
    private String imagePath = null;
    private Frame mainFrame = null;
    private String appPath = null;
    private String sede = null;
    // preferenze
    private Preferences pre_root = null;
    private Preferences pre_gui = null;
    private int startx, starty = 0;    
    private static App app = null;

    private App() {
    }

    public static App getInstance()  {
        if (app == null) {
            app = new App();
            app.init();
        }
        return app;
    }
    
    private void init() {
        pre_root = Preferences.userRoot();
        pre_gui = pre_root.node("/proci/gui");
        // ricarica il path preferito per l'inserimento delle foto soci
        // TODO: impostare  getimagepath OPPURE metodo getpref("stringa")
        Preferences pre = pre_root.node("/proci/globalPrefs");
        app.setImagePath(pre.get("imagePath",app.getAppPath()));
        
    }
    
    public void flushPrefs() {
        Preferences pre = pre_root.node("/proci/globalPrefs");
        pre.put("imagePath", getImagePath());
        // non dovrebbe essere necessaria...
        try {
            pre.flush();
        } catch (BackingStoreException bse) {
        }
    }
    /**
     * @return the imagePath
     */
    public String getImagePath() {
        if (imagePath == null) {
            imagePath = "";
        }
        return imagePath;
    }

    /**
     * @param aImagePath the imagePath to set
     */
    public void setImagePath(String aImagePath) {
        imagePath = aImagePath;
    }

    /**
     * @return the mainFrame
     */
    public Frame getMainFrame() {
        return mainFrame;
    }

    /**
     * @param aMainFrame the mainFrame to set
     */
    public void setMainFrame(Frame aMainFrame) {
        mainFrame = aMainFrame;
    }

    /**
     * @return the appPath
     */
    public String getAppPath() {
        return appPath;
    }

    /**
     * @param dir
     * @return the appPath
     */
    public String getAppPath(EDirectories dir) {
        return appPath  + File.separator + dir.getDir() + File.separator;
    }

    /**
     * @param aAppPath the appPath to set
     */
    public void setAppPath(String aAppPath) {
        appPath = aAppPath;
    }

    /**
     * @return the sede
     */
    public String getSede() {
        return sede;
    }

    /**
     * @param aSede the sede to set
     */
    public void setSede(String aSede) {
        sede = aSede;
    }
    
    public void loadPrefs(JFrame frame) {
        Preferences pre = pre_gui.node(pre_gui.absolutePath() + "/" + frame.getClass().getSimpleName());
        startx+=10;     // valore default
        starty+=10;     // valore default
        String sbounds = pre.get("bounds", startx + "," + starty + ",800,800");
        String[] items = sbounds.split(",");
        frame.setBounds(Integer.parseInt(items[0]), Integer.parseInt(items[1]),
                Integer.parseInt(items[2]), Integer.parseInt(items[3]));
        frame.setExtendedState(pre.getInt("state", 0));
    }
    
    public void loadPrefs(JInternalFrame frame) {
        Preferences pre = pre_gui.node(pre_gui.absolutePath() + "/" + frame.getClass().getSimpleName());
        startx+=10;     // valore default
        starty+=10;     // valore default
        String sbounds = pre.get("bounds", startx + "," + starty + ",500,500");
        String[] items = sbounds.split(",");
        frame.setBounds(Integer.parseInt(items[0]), Integer.parseInt(items[1]),
                Integer.parseInt(items[2]), Integer.parseInt(items[3]));
        try {
            frame.setMaximum(pre.getBoolean("maximized", false));
        } catch (PropertyVetoException ignored) {
        }
    }
    
    public void loadPrefs(Dialog dialog) {
        Preferences pre = pre_gui.node(pre_gui.absolutePath() + "/" + dialog.getClass().getSimpleName());
        startx+=10;     // valore default
        starty+=10;     // valore default
        String sbounds = pre.get("bounds", startx + "," + starty + ",500,500");
        String[] items = sbounds.split(",");
        dialog.setBounds(Integer.parseInt(items[0]), Integer.parseInt(items[1]),
                Integer.parseInt(items[2]), Integer.parseInt(items[3]));
    }
    
    public void savePrefs(JFrame frame) {
        Preferences pre = pre_gui.node(pre_gui.absolutePath() + "/" + frame.getClass().getSimpleName());
        pre.put("bounds", frame.getX() + "," + frame.getY() + "," + frame.getWidth() + "," + frame.getHeight());
        pre.putInt("state", frame.getExtendedState());
    }
    
    public void savePrefs(JInternalFrame frame) {
        Preferences pre = pre_gui.node(pre_gui.absolutePath() + "/" + frame.getClass().getSimpleName());
        pre.put("bounds", frame.getX() + "," + frame.getY() + "," + frame.getWidth() + "," + frame.getHeight());
        pre.putBoolean("maximized", frame.isMaximum());
    }

    public void savePrefs(Dialog dialog) {
        Preferences pre = pre_gui.node(pre_gui.absolutePath() + "/" + dialog.getClass().getSimpleName());
        pre.put("bounds", dialog.getX() + "," + dialog.getY() + "," + dialog.getWidth() + "," + dialog.getHeight());
    }    
}
