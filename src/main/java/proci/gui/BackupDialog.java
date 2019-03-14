/*
 * BackupDialog.java
 *
 * Created on 20 dicembre 2005, 19.24
 */

package proci.gui;

import java.io.File;
import java.util.Date;

/**
 *
 * @author  simeo
 */
public class BackupDialog extends javax.swing.JDialog {
    /** A return status code - returned if Cancel button has been pressed */
    public static final int RET_CANCEL = 0;
    /** A return status code - returned if OK button has been pressed */
    public static final int RET_QUESTO = 1;
    public static final int RET_TUTTI = 2;
    
    /** Creates new form BackupDialog */
    public BackupDialog(java.awt.Frame parent, boolean modal, File file) {
        super(parent, modal);
        initComponents();
        setSize(300, 200);
        Date d = new Date(file.lastModified());
        String html = String.format("<html>il file:<br><b>%s</b><br>modificato il: %s esiste!.<br>" +
                "Clicca sul bottone <i><b>questo file</i></b> per ripristinare il file<br>" +
                "clicca sul bottone <i><b>tutti</i></b> per ripristinare tutti i file oppure<br>" +
                "clicca su <i><b>annulla</i></b> per lasciare il vecchio file inalterato",file.getName(),d);
        jlMessage.setText(html);
    }
    
    /** @return the return status of this dialog - one of RET_OK or RET_CANCEL */
    public int getReturnStatus() {
        return returnStatus;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        buttonPanel = new javax.swing.JPanel();
        questoButton = new javax.swing.JButton();
        tuttiButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        jlMessage = new javax.swing.JLabel();

        setTitle("ATTENZIONE!");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        buttonPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        questoButton.setText("questo file");
        questoButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                questoButtonActionPerformed(evt);
            }
        });

        buttonPanel.add(questoButton);

        tuttiButton.setText("tutti");
        tuttiButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tuttiButtonActionPerformed(evt);
            }
        });

        buttonPanel.add(tuttiButton);

        cancelButton.setText("annulla");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        buttonPanel.add(cancelButton);

        getContentPane().add(buttonPanel, java.awt.BorderLayout.SOUTH);

        jlMessage.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jlMessage.setText("jLabel1");
        getContentPane().add(jlMessage, java.awt.BorderLayout.CENTER);

        pack();
    }
    // </editor-fold>//GEN-END:initComponents

    private void questoButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_questoButtonActionPerformed
        doClose(RET_QUESTO);
    }//GEN-LAST:event_questoButtonActionPerformed
    
    private void tuttiButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tuttiButtonActionPerformed
        doClose(RET_TUTTI);
    }//GEN-LAST:event_tuttiButtonActionPerformed
    
    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        doClose(RET_CANCEL);
    }//GEN-LAST:event_cancelButtonActionPerformed
    
    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        doClose(RET_CANCEL);
    }//GEN-LAST:event_closeDialog
    
    private void doClose(int retStatus) {
        returnStatus = retStatus;
        setVisible(false);
        dispose();
    }
    
   
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel jlMessage;
    private javax.swing.JButton questoButton;
    private javax.swing.JButton tuttiButton;
    // End of variables declaration//GEN-END:variables
    
    private int returnStatus = RET_CANCEL;
}