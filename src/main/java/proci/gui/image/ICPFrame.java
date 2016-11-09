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

package proci.gui.image;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import static java.awt.event.KeyEvent.VK_C;
import static java.awt.event.KeyEvent.VK_F;
import static java.awt.event.KeyEvent.VK_R;
import static java.awt.event.KeyEvent.VK_U;
import static java.awt.event.KeyEvent.VK_X;
import static java.awt.event.KeyEvent.VK_Z;
import java.io.File;
import java.io.IOException;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.imgscalr.Scalr;
import proci.gui.image.ImageCropPanel.ECommand;

/**
 *
 * @author simeo
 */
public class ICPFrame extends javax.swing.JFrame implements ImageCropListener {
    private static final long serialVersionUID = -5107608109591783399L;
    ImageCropPanel icp = null;
    
    /**
     * Creates new form ICPFrame
     */
    public ICPFrame() {
        initComponents();
        icp = new ImageCropPanel();
        icp.addImageCropListener(this);
        jbZoomIn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                icp.zoom(ImageCropPanel.ECommand.ZOOM_IN);
            }
        });
        jbZoomOut.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                icp.zoom(ImageCropPanel.ECommand.ZOOM_OUT);
            }
        });
        jbZoomFit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                icp.zoom(ImageCropPanel.ECommand.ZOOM_FIT);
            }
        });
        jbZoom1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                icp.zoom(ImageCropPanel.ECommand.ZOOM_RESTORE);
            }
        });
        jbCrop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                icp.crop();
            }
        });
        jbUndo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                icp.undo();
            }
        });
        
        jbLoad.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO: in swingeorker?
                loadImageFile();
            }
        });
        addKeyListener(keylistener);
    }

    private void loadImageFile() {
        File curDir = new File(System.getProperty("user.dir"));
        JFileChooser jfc = new JFileChooser(curDir);
        jfc.setMultiSelectionEnabled(false);
        try {
            if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                icp.loadImageFile(jfc.getSelectedFile());
                jpImage.removeAll();
                jpImage.add(icp);
                jpImage.revalidate();
            }                
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(this,ioe,"Errore caricamento immagine!",JOptionPane.ERROR_MESSAGE);           
        }
    }
    @Override
    public void onEvent(ImageCropEvent e) {
        System.out.println("Received event: " + e);
        switch(e.getEventType()) {
            case CROP:
                jlThumb.setIcon(new ImageIcon(Scalr.resize(icp.getImage(),jlThumb.getWidth(),jlThumb.getHeight())));
                jlstatus.setText("Cropped!");
                break;
            case LOAD:
                jlstatus.setText("Loaded!");
                break;
            //case MOUSE:
            case CLIP:
                jlstatus.setText(e.getParamString());
                break;
            case UNDO:
                jlstatus.setText("Undone!");
                break;
        }
    }
    
    @Override
    public boolean isFocusTraversable() {
        return true;
    }    
    
    private final KeyAdapter keylistener = new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case VK_Z:
                    icp.zoom(ECommand.ZOOM_IN);
                    break;
                case VK_X:
                    icp.zoom(ECommand.ZOOM_OUT);
                    break;
                case VK_R:
                    icp.zoom(ECommand.ZOOM_RESTORE);
                    break;
                case VK_F:
                    icp.zoom(ECommand.ZOOM_FIT);
                    break;
                case VK_C:
                    icp.crop();
                    break;
                case VK_U:
                    icp.undo();
                    break;
                default:
            }
        }
    };
    
    private void setImage(File file) throws IOException {
        icp.loadImageFile(file); 
        jpImage.removeAll();
        jpImage.add(icp);
        jpImage.revalidate();        
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jbLoad = new javax.swing.JButton();
        jbZoomIn = new javax.swing.JButton();
        jbZoomOut = new javax.swing.JButton();
        jbCrop = new javax.swing.JButton();
        jbUndo = new javax.swing.JButton();
        jbZoom1 = new javax.swing.JButton();
        jbZoomFit = new javax.swing.JButton();
        jlThumb = new javax.swing.JLabel();
        jspImage = new javax.swing.JScrollPane();
        jpImage = new javax.swing.JPanel();
        jlstatus = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel3.setLayout(new java.awt.BorderLayout());

        jbLoad.setText("load");

        jbZoomIn.setText("zoom +");

        jbZoomOut.setText("zoom -");

        jbCrop.setText("crop");

        jbUndo.setText("undo");

        jbZoom1.setText("1:1");

        jbZoomFit.setText("fit");

        jlThumb.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jlThumb.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jlThumb.setMaximumSize(new java.awt.Dimension(66, 80));
        jlThumb.setMinimumSize(new java.awt.Dimension(66, 80));
        jlThumb.setPreferredSize(new java.awt.Dimension(66, 80));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jlThumb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jbZoomOut, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jbCrop, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jbUndo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jbZoomFit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jbZoom1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jbLoad, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jbZoomIn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jbLoad)
                .addGap(33, 33, 33)
                .addComponent(jbZoomIn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jbZoomOut)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jbZoom1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jbZoomFit)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 205, Short.MAX_VALUE)
                .addComponent(jbUndo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jbCrop)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(45, 45, 45)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jlThumb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
        );

        jpImage.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.green));
        jpImage.setLayout(new java.awt.BorderLayout());
        jspImage.setViewportView(jpImage);

        jlstatus.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        jlstatus.setText(" ");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jspImage, javax.swing.GroupLayout.DEFAULT_SIZE, 839, Short.MAX_VALUE)
                    .addComponent(jlstatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jspImage)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jlstatus))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ICPFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ICPFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ICPFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ICPFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                ICPFrame ifra = new ICPFrame();
                ifra.setVisible(true);
                try {
                    ifra.setImage(new File("immagini/upload/artico11.jpg"));
                } catch (IOException ioe) {
                    System.out.println("ERR: " + ioe);
                }
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JButton jbCrop;
    private javax.swing.JButton jbLoad;
    private javax.swing.JButton jbUndo;
    private javax.swing.JButton jbZoom1;
    private javax.swing.JButton jbZoomFit;
    private javax.swing.JButton jbZoomIn;
    private javax.swing.JButton jbZoomOut;
    private javax.swing.JLabel jlThumb;
    private javax.swing.JLabel jlstatus;
    private javax.swing.JPanel jpImage;
    private javax.swing.JScrollPane jspImage;
    // End of variables declaration//GEN-END:variables
}
