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

import java.awt.Dimension;
import java.awt.Point;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import javax.swing.table.AbstractTableModel;
import proci.App;

/**
 *
 * @author  simeo
 */
public class About extends javax.swing.JDialog {
    /** A return status code - returned if OK button has been pressed */
    public static final int RET_OK = 1;
    final int ABOUT_WIDTH = 377;
    final int ABOUT_HEIGHT = 285;       
    /** Creates new form About */
    public About(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        setTitle("Gestione Soci Protezione Civile");
        initComponents();
        Dimension frmSize = parent.getSize();
        Point loc = parent.getLocation();
        this.setSize(ABOUT_WIDTH,ABOUT_HEIGHT);
        this.setLocation((frmSize.width - ABOUT_WIDTH) / 2 + loc.x, 
                        (frmSize.height - ABOUT_HEIGHT) / 2 + loc.y);
        SimpleDateFormat sdf = new SimpleDateFormat("Y");
        String anno = sdf.format(new Date());
        String aboutText = "Gestione Soci Protezione Civile" +
                "\nVersione " + App.VERSION +
                "\n\n(c) Simeosoft 2006-" + anno +
                "\n\nCarlo Simeone (simeo@simeosoft.com) " +
                "\nhttp://www.simeosoft.com/projects/proci";
        jtAbout.setFont(new java.awt.Font("SansSerif", 0, 10));
        jtAbout.setEditable(false);
        jtAbout.setText(aboutText);
        jtAbout.setLineWrap(true);
        jtAbout.setCaretPosition(0);     
        // MODIF 
        String creditsText = new String("This product includes software developed by" +
                "\nThe Apache Software Foundation (http://www.apache.org/)." +
                "\nAll rights reserved." +
                "\n\nhsqldb: Copyright (c) 2001-2004, The HSQL Development Group" +
                "\nAll rights reserved." +
                "\n\nIzPack - made by Julien Ponge (http://www.izforge.net)" );
        jtCredits.setFont(new java.awt.Font("SansSerif", 0, 10));
        jtCredits.setEditable(false);
        jtCredits.setText(creditsText);
        jtCredits.setLineWrap(true);
        jtCredits.setCaretPosition(0);        
        String licenseText = new String("Gestione Soci Protezione Civile" +
                                        "\n\nCopyright (C) Simeosoft di Carlo Simeone" +
                                        "\n\nThis program is free software; you can redistribute it and/or modify" +
                                        "\nit under the terms of the GNU General Public License as published by" +
                                        "\nthe Free Software Foundation; either version 2 of the License, or" +
                                        "\n(at your option) any later version." +
                                        "\nThis program is distributed in the hope that it will be useful," +
                                        "\nbut WITHOUT ANY WARRANTY; without even the implied warranty of" +
                                        "\nMERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the" +
                                        "\nGNU General Public License for more details." +
                                        "\nYou should have received a copy of the GNU General Public License" +
                                        "\nalong with this program; if not, write to the Free Software" +
                                        "\nFoundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.");
        jtLicense.setFont(new java.awt.Font("SansSerif", 0, 10));
        jtLicense.setEditable(false);
        jtLicense.setText(licenseText);
        jtLicense.setLineWrap(true);
        jtLicense.setCaretPosition(0); 
        jtProp.setModel(new PropModel());
    }

    public class PropModel extends AbstractTableModel {
        ArrayList<Map.Entry> alp;
        final String[] columnNames = {
            "property",
            "values",
            };
        public PropModel() {
            Properties ap = System.getProperties();
            alp = new ArrayList<Map.Entry>(ap.size());
            Iterator iter = ap.entrySet().iterator();
            while (iter.hasNext()) {
                alp.add((Map.Entry) iter.next());
            }
        }
        public String getColumnName(int col) {
            return(columnNames[col]);
        }
        public int getColumnCount() {
            return 2;
        }
        public int getRowCount() {
            return alp.size();
        }
        public Object getValueAt(int r, int c) {
            Map.Entry me = alp.get(r);
            if (c == 0) {
                return me.getKey();
            }
            return me.getValue();
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        buttonPanel = new javax.swing.JPanel();
        okButton = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel3 = new javax.swing.JPanel();
        jtAbout = new javax.swing.JTextArea();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jtCredits = new javax.swing.JTextArea();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jtLicense = new javax.swing.JTextArea();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jtProp = new javax.swing.JTable();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        buttonPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        buttonPanel.add(okButton);

        getContentPane().add(buttonPanel, java.awt.BorderLayout.SOUTH);

        jPanel3.setLayout(new java.awt.BorderLayout());

        jPanel3.add(jtAbout, java.awt.BorderLayout.CENTER);

        jTabbedPane1.addTab("about", jPanel3);

        jPanel4.setLayout(new java.awt.BorderLayout());

        jScrollPane1.setViewportView(jtCredits);

        jPanel4.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jTabbedPane1.addTab("credits", jPanel4);

        jPanel5.setLayout(new java.awt.BorderLayout());

        jScrollPane2.setViewportView(jtLicense);

        jPanel5.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        jTabbedPane1.addTab("license", jPanel5);

        jPanel6.setLayout(new java.awt.BorderLayout());

        jtProp.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane3.setViewportView(jtProp);

        jPanel6.add(jScrollPane3, java.awt.BorderLayout.CENTER);

        jTabbedPane1.addTab("system", jPanel6);

        getContentPane().add(jTabbedPane1, java.awt.BorderLayout.CENTER);

        pack();
    }//GEN-END:initComponents
    
    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        doClose(RET_OK);
    }//GEN-LAST:event_okButtonActionPerformed
        
    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        doClose(RET_OK);
    }//GEN-LAST:event_closeDialog
    
    private void doClose(int retStatus) {
        setVisible(false);
        dispose();
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new About(new javax.swing.JFrame(), true).setVisible(true);
            }
        });
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextArea jtAbout;
    private javax.swing.JTextArea jtCredits;
    private javax.swing.JTextArea jtLicense;
    private javax.swing.JTable jtProp;
    private javax.swing.JButton okButton;
    // End of variables declaration//GEN-END:variables
    
}
