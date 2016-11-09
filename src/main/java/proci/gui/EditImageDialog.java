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

import com.simeosoft.string.StringUtils;
import com.simeosoft.swing.TransferData;
import proci.gui.image.ImageCropEvent;
import proci.gui.image.ImageCropPanel;
import proci.gui.image.ImageCropListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import proci.gui.image.MetaImage;
import proci.gui.image.MetaImageService;

/**
 *
 * @author simeo
 */
public class EditImageDialog extends javax.swing.JDialog implements ImageCropListener {
    static final Logger logger = LoggerFactory.getLogger(EditImageDialog.class);    
    private static final long serialVersionUID = -8570229504264247649L;
    //
    private final ImageCropPanel icp;
    private final MetaImage mi;
    private final TransferData retData;
    /**
     * Creates new form BackupDialog
     * @param parent
     * @param mi
     */
    public EditImageDialog(java.awt.Frame parent, MetaImage mi) {
        super(parent, true);
        this.mi = mi;
        setTitle(mi.getDescr());
        retData = new TransferData();
        retData.setValueStatus(TransferData.ERetValues.CANCEL_VALUE);
        initComponents();
        if (parent != null) {
            setBounds(parent.getX() + 10,parent.getY() + 10,parent.getWidth() - 20,parent.getHeight() - 20);
        } else {
            setSize(800,600);
        }
        icp = new ImageCropPanel(mi);
        icp.addImageCropListener(this);
        jspImage.getViewport().add(icp);
        // 
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
                crop();
            }
        });
        jbUndo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                icp.undo();
            }
        });
        jbSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                save();
                closeDialog(null);
            }
        });
        jbCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                retData.setValueStatus(TransferData.ERetValues.CANCEL_VALUE);
                closeDialog(null);
            }
        });
    }

    TransferData showDialog() {
        setVisible(true);
        return retData;
    }    
        
    private void crop() {
        icp.crop();
    }
    
    private void save() {
        retData.setValueStatus(TransferData.ERetValues.OK_VALUE);
        mi.setImage(icp.getImage());
    }
    

    @Override
    public void onEvent(ImageCropEvent e) {
        switch(e.getEventType()) {
            case CROP:
                jlstatus.setText("Cropped!");
                jbSave.setEnabled(true);
                jbUndo.setEnabled(true);
                break;
            case LOAD:
                jlstatus.setText("Loaded!");
                jbUndo.setEnabled(true);
                jbZoom1.setEnabled(true);
                jbZoomFit.setEnabled(true);
                jbZoomIn.setEnabled(true);
                jbZoomOut.setEnabled(true);
                jbCrop.setEnabled(true);
                break;
            //case MOUSE:
            case CLIP:
                jlstatus.setText(e.getParamString());
                break;
            case UNDO:
                jlstatus.setText("Undone!");
                jbSave.setEnabled(false);
                jbUndo.setEnabled(false);
                break;
        }
    }    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jbZoomIn = new javax.swing.JButton();
        jbZoomOut = new javax.swing.JButton();
        jbCrop = new javax.swing.JButton();
        jbUndo = new javax.swing.JButton();
        jbZoom1 = new javax.swing.JButton();
        jbZoomFit = new javax.swing.JButton();
        jbSave = new javax.swing.JButton();
        jbCancel = new javax.swing.JButton();
        jspImage = new javax.swing.JScrollPane();
        jlstatus = new javax.swing.JLabel();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        jPanel3.setLayout(new java.awt.BorderLayout());

        jbZoomIn.setText("zoom +");
        jbZoomIn.setMaximumSize(null);
        jbZoomIn.setMinimumSize(new java.awt.Dimension(0, 0));

        jbZoomOut.setText("zoom -");
        jbZoomOut.setMaximumSize(null);
        jbZoomOut.setMinimumSize(new java.awt.Dimension(0, 0));

        jbCrop.setText("ritaglia");
        jbCrop.setMaximumSize(null);
        jbCrop.setMinimumSize(new java.awt.Dimension(0, 0));

        jbUndo.setText("undo");
        jbUndo.setMaximumSize(null);
        jbUndo.setMinimumSize(new java.awt.Dimension(0, 0));

        jbZoom1.setText("1:1");
        jbZoom1.setMaximumSize(null);
        jbZoom1.setMinimumSize(new java.awt.Dimension(0, 0));

        jbZoomFit.setText("allarga");
        jbZoomFit.setMaximumSize(null);
        jbZoomFit.setMinimumSize(new java.awt.Dimension(0, 0));

        jbSave.setText("salva");
        jbSave.setEnabled(false);
        jbSave.setMaximumSize(null);
        jbSave.setMinimumSize(new java.awt.Dimension(0, 0));

        jbCancel.setText("annulla");
        jbCancel.setMaximumSize(null);
        jbCancel.setMinimumSize(new java.awt.Dimension(0, 0));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jbZoomFit, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 98, Short.MAX_VALUE)
                            .addComponent(jbCrop, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jbUndo, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                    .addComponent(jbCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(32, 32, 32)
                                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(jbZoomIn, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 98, Short.MAX_VALUE)
                                    .addComponent(jbZoomOut, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jbZoom1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addComponent(jbSave, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(31, 31, 31))))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jbZoomIn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jbZoomOut, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jbZoom1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jbZoomFit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 166, Short.MAX_VALUE)
                .addComponent(jbCrop, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(100, 100, 100)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(41, 41, 41))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jbUndo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jbCancel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)))
                .addComponent(jbSave, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jspImage.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jlstatus.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        jlstatus.setText(" ");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jspImage, javax.swing.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE)
                    .addComponent(jlstatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2))
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
     * Closes the dialog
     */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        dispose();
    }//GEN-LAST:event_closeDialog

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JButton jbCancel;
    private javax.swing.JButton jbCrop;
    private javax.swing.JButton jbSave;
    private javax.swing.JButton jbUndo;
    private javax.swing.JButton jbZoom1;
    private javax.swing.JButton jbZoomFit;
    private javax.swing.JButton jbZoomIn;
    private javax.swing.JButton jbZoomOut;
    private javax.swing.JLabel jlstatus;
    private javax.swing.JScrollPane jspImage;
    // End of variables declaration//GEN-END:variables

    public static void main(String[] args) {
        System.out.println("formati: " + StringUtils.pack(ImageIO.getWriterFormatNames(),"|"));
        System.out.println("suffissi: " + StringUtils.pack(ImageIO.getWriterFileSuffixes(),"|"));
        System.out.println("mime: " + StringUtils.pack(ImageIO.getWriterMIMETypes(),"|"));
        //File file = new File("immagini/upload/picture_1.png");
        File file = new File("immagini/upload/test.png");
        if (!file.exists()) {
            System.out.println("FILE NON ESISTENTE!!!");
            return;
        }
        MetaImageService miservice = new MetaImageService();
        MetaImage mi = miservice.getMetaImage(file);
        EditImageDialog e = new EditImageDialog(null, mi);
        TransferData td = e.showDialog();
        System.out.println("Ret: " + td.getValueStatus());
        if (td.getValueStatus() == TransferData.ERetValues.OK_VALUE) {
            file = new File("immagini/upload_resized/test.png");
            System.out.println("Scrivo su: " + file.getAbsolutePath());
            try {
                ImageIO.write(mi.getImage(),"png", file);
            } catch (IOException ex) {
                logger.error("ERRORE: ",ex);
            }
        }
    }
}
